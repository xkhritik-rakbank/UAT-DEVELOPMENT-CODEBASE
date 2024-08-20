package com.newgen.DPL;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.newgen.DCC.DECTECHIntegration.DECTECHSystemIntegrationLog;
import com.newgen.DCC.Notify.DCCNotifyLog;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.omni.wf.util.excp.NGException;

public class Digital_PL_CommomMethod extends CommonMethods{
	
	static NGEjbClient ngEjbClient;
	private static org.apache.log4j.Logger logger;
	
	public Digital_PL_CommomMethod() throws NGException{
		Digital_PL_Log.setLogger(getClass().getSimpleName());
		this.ngEjbClient = NGEjbClient.getSharedInstance();
		logger = Digital_PL_Log.getLogger(getClass().getSimpleName());
		
	}
	
	//Read data from property file
	public static Map<String,String> readConfig(String FileName) {
		Properties p = null;
		Map<String, String> digital_PL_Map = new HashMap<String, String>();
		try {
			
			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir") + File.separator + "ConfigFiles" + File.separator + FileName)));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				digital_PL_Map.put(name, p.getProperty(name));
			}
			
			return digital_PL_Map;
		} catch (Exception e) {
			return null;
		}
		
	}
	
	public static void waiteloopExecute(long wtime) {
		try {
			for (int i = 0; i < 10; i++) {
				Thread.yield();
				Thread.sleep(wtime / 10);
			}
		} catch (InterruptedException e) {
		}
	}
	
	public static HashMap<String, String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort, String sessionID) {
		HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

		try {
			System.out.println("Fetching Socket Connection Details.");

			String socketDetailsQuery = "SELECT SocketServerIP, SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'DPL' and CallingSource = 'Utility'";

			String socketDetailsInputXML = CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
			
			String socketDetailsOutputXML = CommonMethods.WFNGExecute(socketDetailsInputXML, sJtsIp, iJtsPort, 1);
			
			XMLParser xmlParserSocketDetails = new XMLParser(socketDetailsOutputXML);
			String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			
			int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			
			if (socketDetailsMainCode.equalsIgnoreCase("0") && socketDetailsTotalRecords > 0) {
				String xmlDataSocketDetails = xmlParserSocketDetails.getNextValueOf("Record");
				xmlDataSocketDetails = xmlDataSocketDetails.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

				XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

				String socketServerIP = xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
				socketDetailsMap.put("SocketServerIP", socketServerIP);

				String socketServerPort = xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
				socketDetailsMap.put("SocketServerPort", socketServerPort);

				System.out.println("SocketServer Details found.");

			}
		} catch (Exception e) {
			System.out.println("Exception in getting Socket Connection Details: " + e.getMessage());
		}

		return socketDetailsMap;
	}

	//Calculate DOJ formate should be YYYY-MM-DD
		public static Double CalculatLOS(String DOJ_Str) {
			Double LOS = 0.00;
			try {
				Integer year = Integer.parseInt(DOJ_Str.split("-")[0]);
				Integer month = Integer.parseInt(DOJ_Str.split("-")[1]);
				Integer day = Integer.parseInt(DOJ_Str.split("-")[2]);
				LocalDate DOJ = LocalDate.of(year,month,day);
				LocalDate CD = LocalDate.now();
				Period p = Period.between(DOJ, CD);
				System.out.println(p.getMonths());
				System.out.println(p.getYears());
				
				LOS += p.getYears();
				LOS = LOS + p.getMonths()/100d;
			} catch (Exception e) {
				e.printStackTrace();
				return LOS;
			}
			
			System.out.println(LOS);
			return LOS;
		}
		
		protected static String validateValue(String value) {
			if (value != null && !value.equals("") && !value.equalsIgnoreCase("null")) {
				return value.trim();
			}
			return "";
		}
	
		protected String socketConnection(String cabinetName, String username, String sessionId, String sJtsIp, String iJtsPort, String processInstanceID, String ws_name,
				int connection_timeout, int integrationWaitTime,HashMap<String, String> socketDetailsMap, StringBuilder sInputXML)
		{

			String socketServerIP;
			int socketServerPort;
			Socket socket = null;
			OutputStream out = null;
			InputStream socketInputStream = null;
			DataOutputStream dout = null;
			DataInputStream din = null;
			String outputResponse = null;
			String inputRequest = null;
			String inputMessageID = null;

			try
			{

				logger.debug("userName "+ username);
				logger.debug("SessionId "+ sessionId);

				socketServerIP=socketDetailsMap.get("SocketServerIP");
				//logger.debug("SocketServerIP "+ socketServerIP);
				socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
				//logger.debug("SocketServerPort "+ socketServerPort);

		   		if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0))
		   		{

	    			socket = new Socket(socketServerIP, socketServerPort);
	    			socket.setSoTimeout(connection_timeout*1000);
	    			out = socket.getOutputStream();
	    			socketInputStream = socket.getInputStream();
	    			dout = new DataOutputStream(out);
	    			din = new DataInputStream(socketInputStream);
	    			logger.debug("Dout " + dout);
	    			logger.debug("Din " + din);

	    			outputResponse = "";

	    			inputRequest = getRequestXML(cabinetName,sessionId ,processInstanceID, ws_name, username, sInputXML);


	    			if (inputRequest != null && inputRequest.length() > 0)
	    			{
	    				int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
	    				logger.debug("RequestLen: "+inputRequestLen + "");
	    				inputRequest = inputRequestLen + "##8##;" + inputRequest;
	    				logger.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
	    				dout.write(inputRequest.getBytes("UTF-16LE"));dout.flush();
	    			}
	    			byte[] readBuffer = new byte[500];
	    			System.out.println(""+readBuffer);
	    			
	    			int num = din.read(readBuffer);
	    			System.out.println("num:-"+num);
	    			if (num > 0)
	    			{
	    				byte[] arrayBytes = new byte[num];
	    				System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
	    				outputResponse = outputResponse+ new String(arrayBytes, "UTF-16LE");
						inputMessageID = outputResponse;
	    				logger.debug("OutputResponse: "+outputResponse);

	    				if(!"".equalsIgnoreCase(outputResponse))
	    					outputResponse = getResponseXML(cabinetName,sJtsIp,iJtsPort,sessionId, processInstanceID,outputResponse,integrationWaitTime);

	    				if(outputResponse.contains("&lt;"))
	    				{
	    					outputResponse=outputResponse.replaceAll("&lt;", "<");
	    					outputResponse=outputResponse.replaceAll("&gt;", ">");
	    				}
	    			}
	    			socket.close();

					outputResponse = outputResponse.replaceAll("</MessageId>","</MessageId>/n<InputMessageId>"+inputMessageID+"</InputMessageId>");

					logger.debug("outputResponse "+outputResponse);
					return outputResponse;

	    	 		}

	    		else
	    		{
	    			logger.debug("SocketServerIp and SocketServerPort is not maintained "+"");
	    			logger.debug("SocketServerIp is not maintained "+	socketServerIP);
	    			logger.debug(" SocketServerPort is not maintained "+	socketServerPort);
	    			return "Socket Details not maintained";
	    		}

			}

			catch (Exception e)
			{
				System.out.println(e.getMessage());
				logger.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
				return "";
			}
			finally
			{
				try
				{
					if(out != null)
					{
						out.close();
						out=null;
					}
					if(socketInputStream != null)
					{

						socketInputStream.close();
						socketInputStream=null;
					}
					if(dout != null)
					{

						dout.close();
						dout=null;
					}
					if(din != null)
					{

						din.close();
						din=null;
					}
					if(socket != null)
					{
						if(!socket.isClosed())
							socket.close();
						socket=null;
					}

				}

				catch(Exception e)
				{
					//logger.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
					//printException(e);
				}
			}


		}
		
		
		public String getResponseXML(String cabinetName,String sJtsIp,String iJtsPort, String sessionId, String processInstanceID,String message_ID, int integrationWaitTime)
		{

			String outputResponseXML="";
			try
			{
				String QueryString = "select OUTPUT_XML from NG_DPL_XMLLOG_HISTORY with (nolock) where MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+processInstanceID+"'";

				String responseInputXML =CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, sessionId);
				//logger.debug("Response APSelect InputXML: "+responseInputXML);

				int Loop_count=0;
				do
				{
					String responseOutputXML=CommonMethods.WFNGExecute(responseInputXML,sJtsIp,iJtsPort,1);
					//logger.debug("Response APSelect OutputXML: "+responseOutputXML);

				    XMLParser xmlParserSocketDetails= new XMLParser(responseOutputXML);
				    String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
				    //logger.debug("ResponseMainCode: "+responseMainCode);



				    int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
				    //logger.debug("ResponseTotalRecords: "+responseTotalRecords);

				    if (responseMainCode.equals("0") && responseTotalRecords > 0)
					{

						String responseXMLData=xmlParserSocketDetails.getNextValueOf("Record");
						responseXMLData =responseXMLData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

		        		XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
		        		//DCCNotifyAPPLog.DCCNotifyLogger.debug("ResponseXMLData: "+responseXMLData);

		        		outputResponseXML=xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
		        		//DCCNotifyAPPLog.DCCNotifyLogger.debug("OutputResponseXML: "+outputResponseXML);

		        		if("".equalsIgnoreCase(outputResponseXML)){
		        			outputResponseXML="Error";
		    			}
		        		break;
					}
				    Loop_count++;
				    Thread.sleep(1000);
				}
				while(Loop_count<integrationWaitTime);
				//logger.debug("integrationWaitTime: "+integrationWaitTime);

			}
			catch(Exception e)
			{
				//logger.debug("Exception occurred in outputResponseXML" + e.getMessage());
				outputResponseXML="Error";
			}

			return outputResponseXML;

		}
		
		public String getRequestXML(String cabinetName, String sessionId,
				String processInstanceID, String ws_name, String userName, StringBuilder sInputXML)
		{
			StringBuffer strBuff = new StringBuffer();
			strBuff.append("<APMQPUTGET_Input>");
			strBuff.append("<SessionId>" + sessionId + "</SessionId>");
			strBuff.append("<EngineName>" + cabinetName + "</EngineName>");
			strBuff.append("<XMLHISTORY_TABLENAME>NG_DPL_XMLLOG_HISTORY</XMLHISTORY_TABLENAME>");
			strBuff.append("<WI_NAME>" + processInstanceID + "</WI_NAME>");
			strBuff.append("<WS_NAME>" + ws_name + "</WS_NAME>");
			strBuff.append("<USER_NAME>" + userName + "</USER_NAME>");
			strBuff.append("<MQ_REQUEST_XML>");
			strBuff.append(sInputXML);
			strBuff.append("</MQ_REQUEST_XML>");
			strBuff.append("</APMQPUTGET_Input>");
			//logger.debug("GetRequestXML: "+ strBuff.toString());
			return strBuff.toString();
		}
		
		
		protected String getAPUpdateInputXML(String tableName,String colName,String colValues,String sSessionID,
				String whereClause,String sCabinetName)
		{
			return "<?xml version=\"1.0\"?>" + "<APUpdate_Input>"
			+ "<Option>APUpdate</Option>" + "<TableName>"+tableName+
			"</TableName>" + "<ColName>" + colName + "</ColName>"
			+ "<Values>" + colValues + "</Values>" + "<WhereClause>"+whereClause+"</WhereClause>"
			+ "<EngineName>"
			+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
			+ "</SessionId>" + "</APUpdate_Input>";
		}
		
		protected String executeAPI(String sInputXML,String sJtsIp,String JtsPort)
		{
			String sOutputXML="";
			try
			{
				 Socket sock = null;
				 int iJtsPort = Integer.parseInt(JtsPort);
				 sock = new Socket(sJtsIp, iJtsPort);
				 DataOutputStream oOut = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
				 DataInputStream oIn = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
				 byte[] SendStream = sInputXML.getBytes("8859_1");
				 int strLen = SendStream.length;
		         oOut.writeInt(strLen);
		    	 oOut.write(SendStream, 0, strLen);
				 oOut.flush();
				 int length = 0;
				 length = oIn.readInt();
				 byte[] readStream = new byte[length];
				 oIn.readFully(readStream);
				 sOutputXML= new String(readStream, "8859_1");
				 sock.close(); 
			}
			catch (Exception e)
			{
				logger.debug("Exception: "+e.getMessage());
				
			}
			return sOutputXML;
			
		}
		
		protected static String getEnquiryDate(String EnquiryDate) {
			if (validateValue(EnquiryDate).equals(""))
				return "";
			
			try {
				Date parseDateCC = getParseDate(EnquiryDate);
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				EnquiryDate = formatter.format(parseDateCC);
				//logger.debug("Date : " + EnquiryDate);
				return EnquiryDate;
			}
			catch(Exception e){
				logger.info("Exception occured in conversion of AECB Enquiry Date :" + e.getMessage());
				return "";
			}
		}
		
		protected static String getMaximumOverdueDate(String overDueDate) {
			 if (validateValue(overDueDate).equals(""))
				 return "";
			
			try {
				Date parseDateCC = getParseDate(overDueDate);
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				overDueDate = formatter.format(parseDateCC);
				logger.debug("MaxOverDueAmountDate : " + overDueDate);
				return overDueDate;
			}
			catch(Exception e){
				logger.info("Exception occured in conversion of MaxOverDueAmountDate :" + e.getMessage());
				return "";
			}
		}

		private static Date getParseDate(String parseDate) throws ParseException {
			if (parseDate.matches("([0-9]{2})/([0-9]{2})/([0-9]{4})")) {
				return new SimpleDateFormat("dd/MM/yyyy").parse(parseDate);
			} else if (parseDate.matches("([0-9]{2})-([0-9]{2})-([0-9]{4})")) {
				return new SimpleDateFormat("dd-MM-yyyy").parse(parseDate);
			}
			else if (parseDate.matches("([0-9]{4})/([0-9]{2})/([0-9]{2})")) {
				return new SimpleDateFormat("yyyy/MM/dd").parse(parseDate);
			} else {
				return new SimpleDateFormat("yyyy-MM-dd").parse(parseDate);
			}
		}
}
