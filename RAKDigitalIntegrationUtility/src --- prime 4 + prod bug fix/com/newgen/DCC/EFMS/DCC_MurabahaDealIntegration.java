package com.newgen.DCC.EFMS;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.XMLParser;
import org.apache.log4j.Logger;

public class DCC_MurabahaDealIntegration {
	String sessionID = "";
	String cabinetname = "";
	String JtsIp = "";
	String JtsPort = "";
	String processInstanceId = "";
	String ws_Name = "";
	int sleepIntervalInMin=0;
	Socket socket = null;
	String socketServerIP = "";
	int socketServerPort = 0;
	OutputStream out = null;
	InputStream socketInputStream = null;
	DataOutputStream dout = null;
	DataInputStream din = null;
	String outputResponse = null;
	String inputRequest = null;
	String inputMessageID = null;
	String ReturnDesc ="";
	String ReturnCode = "";
	String Description = "";
	static String TradeDate="";
	
	final String NG_DCC_EXTTABLE = "NG_DCC_EXTTABLE";
	HashMap<String, String> socketConnectionMap = null;
	
	private static Logger DCC_MurabahaDealLogger=null;
	
	public DCC_MurabahaDealIntegration(Logger DCC_MurabahaDealLog)
	{
		DCC_MurabahaDealLogger=DCC_MurabahaDealLog;
	}

	public String MurabahaReqDeal(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String processInstanceID, 
			int socketConnectionTimeOut, int integrationWaitTime, HashMap<String, String> socketDetailsMap, int trialTime, int errorCount,String ws_name,String callSeq) throws Exception
	{
		sessionID = sessionId;
		cabinetname = cabinetName;
		JtsIp = sJtsIp;
		JtsPort = iJtsPort;
		processInstanceId = processInstanceID;
		ws_Name = ws_name;
		String result="Error";
		String IntegrationStatus ="";
		String IntegrationStatus3 ="";
		String finaldecision="";
	//Deepak commented - 8 March for name substring to 30 digit substring(CONCAT(FirstName,' ',LastName),0,31) else substring(CONCAT(FirstName,' ',MiddleName,' ',LastName),0,31)	
//		String DBQuery = "SELECT ecrn, crn,Card_Limit,Final_Limit,Prospect_id,case when MiddleName is null then CONCAT(FirstName,' ',LastName) else CONCAT(FirstName,' ',MiddleName,' ',LastName) end as CUSTOMERNAME FROM NG_DCC_EXTTABLE with(nolock) WHERE WI_NAME='"+processInstanceID+"'";
		String DBQuery = "SELECT ecrn, crn,Card_Limit,Final_Limit,Prospect_id,case when MiddleName is null then substring(CONCAT(FirstName,' ',LastName),0,31) else substring(CONCAT(FirstName,' ',MiddleName,' ',LastName),0,31) end as CUSTOMERNAME FROM NG_DCC_EXTTABLE with(nolock) WHERE WI_NAME='"+processInstanceID+"'";

		String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery, cabinetName,sessionId);
		DCC_MurabahaDealLogger.debug("extTabDataIPXML: " + extTabDataIPXML);
		
		

		String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,sJtsIp ,iJtsPort , 1);
		DCC_MurabahaDealLogger.debug("extTabDataOPXML: " + extTabDataOPXML);

		XMLParser xmlParserData = new XMLParser(extTabDataOPXML);

		int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
		if("11".equalsIgnoreCase(xmlParserData.getValueOf("MainCode"))){
			sessionID = CommonConnection.getSessionID(DCC_EFMS_IntegrationLog.DCC_EFMSIntegrationLogger, false);
			sessionId=sessionID;
			extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery, cabinetName,sessionId);
			DCC_MurabahaDealLogger.debug("extTabDataIPXML: " + extTabDataIPXML);
			
			extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,sJtsIp ,iJtsPort , 1);
			DCC_MurabahaDealLogger.debug("extTabDataOPXML: " + extTabDataOPXML);

			xmlParserData = new XMLParser(extTabDataOPXML);			
		}
		
		if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0) {
			String xmlDataExtTab = xmlParserData.getNextValueOf("Record");
			xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");



			StringBuilder murabahaDealInputXML = readRequestXmlSample(xmlParserData , "MURABAHA_REQUESTDEAL_REQ",callSeq);

			DCC_MurabahaDealLogger.debug("MURABAHA_REQUESTDEAL_REQInputXML: " + murabahaDealInputXML);

			socketConnectionMap =socketConnectionDetails(cabinetName, sJtsIp, iJtsPort, sessionId); 


			outputResponse = socketConnection(cabinetName, CommonConnection.getUsername(), sessionId, sJtsIp, iJtsPort, processInstanceID, ws_name, 60, 65,socketConnectionMap, murabahaDealInputXML );


			XMLParser xmlParserSocketDetails= new XMLParser(outputResponse);
			DCC_MurabahaDealLogger.debug(" xmlParserSocketDetails : "+xmlParserSocketDetails);

			if(outputResponse.contains("<ReturnDesc>"))
				ReturnDesc=xmlParserSocketDetails.getValueOf("ReturnDesc");
			if(outputResponse.contains("<ReturnCode>"))
				ReturnCode=xmlParserSocketDetails.getValueOf("ReturnCode");
			if(outputResponse.contains("<Description>"))
				Description=xmlParserSocketDetails.getValueOf("Description");
			

			if("0000".equalsIgnoreCase(ReturnCode))
			{
				String URL = xmlParserSocketDetails.getValueOf("DocumentsURI");
				String KEY = xmlParserSocketDetails.getValueOf("TransactionKey");
				
				IntegrationStatus =MurabahaConfirmIntegrationCall(xmlParserSocketDetails,callSeq);
				
				if("Success".equalsIgnoreCase(IntegrationStatus))
				{
					IntegrationStatus3 =MurabahaConfirmIntegrationCall2(xmlParserSocketDetails,callSeq);
					if("Success".equalsIgnoreCase(IntegrationStatus3))
					{
						return "Success";
					}
					return "Fail_3";
				}
				return "Fail_2";
				
			}
			return "Fail_1";
			
			//completeWorkItem(cabinetName,sessionId,processInstanceID,WorkItemID,finaldecision,entryTimeDate);
		}
		return "";
	}
	
	private static String getTagValues(String sXML, String sTagName) {
		String sTagValues = "";
		String sStartTag = "<" + sTagName + ">";
		String sEndTag = "</" + sTagName + ">";
		String tempXML = sXML;
		try {

			for (int i = 0; i < sXML.split(sEndTag).length; i++) {
				if (tempXML.indexOf(sStartTag) != -1) {
					sTagValues += tempXML.substring(tempXML.indexOf(sStartTag) + sStartTag.length(), tempXML.indexOf(sEndTag));
					tempXML = tempXML.substring(tempXML.indexOf(sEndTag) + sEndTag.length(), tempXML.length());
				}
				if (tempXML.indexOf(sStartTag) != -1) {
					sTagValues += "`";
					//System.out.println("sTagValues"+sTagValues);
				}
				//System.out.println("sTagValues"+sTagValues);
			}
			//System.out.println(" Final sTagValues"+sTagValues);
		} catch (Exception e) {
		}
		return sTagValues;
	}
	

	private static  StringBuilder readRequestXmlSample(XMLParser xmlParserData, String CAllNAme,String callSeq) {

		String CUSTOMERNAME = xmlParserData.getValueOf("CUSTOMERNAME");
		String Card_Limit = xmlParserData.getValueOf("Card_Limit");
		String Prospect_id = xmlParserData.getValueOf("Prospect_id");
		String crn = xmlParserData.getValueOf("crn");
		String ecrn = xmlParserData.getValueOf("ecrn");
		String Final_Limit = xmlParserData.getValueOf("Final_Limit");
		String tranPrincipal="";
		if("2".equals(callSeq)){
			tranPrincipal=Final_Limit;
		}	
		else{
			if(Card_Limit==null || "".equalsIgnoreCase(Card_Limit)){
				tranPrincipal="500";
			}
			else{
				tranPrincipal=Card_Limit;
			}
		}
		if(tranPrincipal==null)
			tranPrincipal="";
		DCC_MurabahaDealLogger.debug("CUSTOMERNAME: " + CUSTOMERNAME);
		DCC_MurabahaDealLogger.debug("Card_Limit: " + Card_Limit);
		DCC_MurabahaDealLogger.debug("Final_Limit: " + Final_Limit);
		DCC_MurabahaDealLogger.debug("Prospect_id: " + Prospect_id);
		DCC_MurabahaDealLogger.debug("crn: " + crn);
		DCC_MurabahaDealLogger.debug("ecrn: " + ecrn);
		StringBuilder finalsb = new StringBuilder("");
		if("MURABAHA_REQUESTDEAL_REQ".equalsIgnoreCase(CAllNAme)){
			 StringBuilder sb = new StringBuilder("");
			 SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
			 TradeDate = formatter.format(new Date());
			 String SettlementDate = formatter.format(new Date());
			 String MaturityDate = formatter.format(new Date());
			 String arr[] = SettlementDate.split(" ");
			 if(arr.length>2)
				 MaturityDate=arr[0]+" "+arr[1]+" "+(Integer.parseInt(arr[2])+10);
			try {

				String fileLocation = new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DCC_Integration")
						.append(System.getProperty("file.separator")).append("MURABAHA_REQUESTDEAL.txt").toString();
				BufferedReader sbf = new BufferedReader(new FileReader(fileLocation));

				String line = sbf.readLine();
				while (line != null) {
					sb.append(line);
					sb.append(System.lineSeparator());
					line = sbf.readLine();
				}
				
				String murabahaDeal_input_xml = sb.toString().replace(">CUSTOMERNAME<",">"+xmlParserData.getValueOf("CUSTOMERNAME").trim()+"<")
	    				.replace(">Card_Limit<",">"+tranPrincipal.trim()+"<")
	    				.replace(">Prospect_id<",">"+xmlParserData.getValueOf("Prospect_id").trim()+"<")
	    				.replace(">CRN_Str<",">"+xmlParserData.getValueOf("crn").trim()+"<")
	    				.replace(">ECRN_Str<",">"+xmlParserData.getValueOf("ecrn").trim()+"<")
	    				.replace(">TradeDate<",">"+TradeDate.trim()+"<")
	    				.replace(">SettlementDate<",">"+SettlementDate.trim()+"<")
	    				.replace(">MaturityDate<",">"+MaturityDate.trim()+"<");
				
				finalsb = finalsb.append(murabahaDeal_input_xml);
				
			} catch (FileNotFoundException e) {
				DCC_MurabahaDealLogger.error("In Reading MURABAHA_REQUESTDEAL.txt : " + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				DCC_MurabahaDealLogger.error("In Reading MURABAHA_REQUESTDEAL.txt : " + e.getMessage());
				e.printStackTrace();
			}
			return finalsb;
		}

		else if("MURABAHA_CONFIRMDEAL_REQ2".equalsIgnoreCase(CAllNAme)){
			StringBuilder sb = new StringBuilder("");
			
			try {
				String key = xmlParserData.getValueOf("TransactionKey");
				String url = xmlParserData.getValueOf("DocumentsURI");
				
				String fileLocation = new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DCC_Integration")
						.append(System.getProperty("file.separator")).append("MURABAHA_CONFIRMDEAL.txt").toString();
				BufferedReader sbf = new BufferedReader(new FileReader(fileLocation));

				String line = sbf.readLine();
				while (line != null) {
					sb.append(line);
					sb.append(System.lineSeparator());
					line = sbf.readLine();
				}
				DCC_MurabahaDealLogger.error("MURABAHA_CONFIRMDEAL_REQ2 input xml : " + line);
				String murabahaDealinput_xml = sb.toString().replace(">URL<",">"+url.trim()+"<")
	    				.replace(">KEY<",">"+key.trim()+"<");
				DCC_MurabahaDealLogger.error("MURABAHA_CONFIRMDEAL_REQ2 input xml after replacing: " + murabahaDealinput_xml);
				finalsb = finalsb.append(murabahaDealinput_xml);
				
				
			} catch (FileNotFoundException e) {
				DCC_MurabahaDealLogger.error("In Reading MURABAHA_CONFIRMDEAL.txt : " + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				DCC_MurabahaDealLogger.error("In Reading MURABAHA_CONFIRMDEAL.txt : " + e.getMessage());
				e.printStackTrace();
			}
			return finalsb;
		}
		else if("MURABAHA_CONFIRMDEAL_REQ3".equalsIgnoreCase(CAllNAme)){
			StringBuilder sb = new StringBuilder("");
			
			try {
				String key = xmlParserData.getValueOf("TransactionKey");
				String url = xmlParserData.getValueOf("DocumentsURI");
				
				String fileLocation = new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DCC_Integration")
						.append(System.getProperty("file.separator")).append("MURABAHA_CONFIRMDEAL3.txt").toString();
				BufferedReader sbf = new BufferedReader(new FileReader(fileLocation));

				String line = sbf.readLine();
				while (line != null) {
					sb.append(line);
					sb.append(System.lineSeparator());
					line = sbf.readLine();
				}
				DCC_MurabahaDealLogger.error("MURABAHA_CONFIRMDEAL_REQ2 input xml : " + line);
				String murabahaDealinput_xml = sb.toString().replace(">URL<",">"+url.trim()+"<")
	    				.replace(">KEY<",">"+key.trim()+"<");
				DCC_MurabahaDealLogger.error("MURABAHA_CONFIRMDEAL_REQ2 input xml after replacing: " + murabahaDealinput_xml);
				finalsb = finalsb.append(murabahaDealinput_xml);
				
				
			} catch (FileNotFoundException e) {
				DCC_MurabahaDealLogger.error("In Reading MURABAHA_CONFIRMDEAL.txt : " + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				DCC_MurabahaDealLogger.error("In Reading MURABAHA_CONFIRMDEAL.txt : " + e.getMessage());
				e.printStackTrace();
			}
			return finalsb;
		}
		return finalsb;
		
	}

	private static String getRequestXML(String cabinetName, String sessionId, String wi_name, String ws_name, String userName, StringBuilder sInputXML)
	{
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("<APMQPUTGET_Input>");
		strBuff.append("<SessionId>" + sessionId + "</SessionId>");
		strBuff.append("<EngineName>" + cabinetName + "</EngineName>");
		strBuff.append("<XMLHISTORY_TABLENAME>NG_DCC_XMLLOG_HISTORY</XMLHISTORY_TABLENAME>");
		strBuff.append("<WI_NAME>" + wi_name + "</WI_NAME>");
		strBuff.append("<WS_NAME>" + ws_name + "</WS_NAME>");
		strBuff.append("<USER_NAME>" + userName + "</USER_NAME>");
		strBuff.append("<MQ_REQUEST_XML>");
		strBuff.append(new StringBuffer(sInputXML));
		strBuff.append("</MQ_REQUEST_XML>");
		strBuff.append("</APMQPUTGET_Input>");
		DCC_MurabahaDealLogger.debug("GetRequestXML: "+ strBuff.toString());
		return strBuff.toString();
	}

	private static String getResponseXML(String cabinetName,String sJtsIp,String iJtsPort, String sessionId, String wi_name, String message_ID, int integrationWaitTime)
	{

		String outputResponseXML="";
		try
		{
			String QueryString = "select OUTPUT_XML from NG_DCC_XMLLOG_HISTORY with (nolock) where MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+wi_name+"'";

			String responseInputXML =CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, sessionId);
			DCC_MurabahaDealLogger.debug("Response APSelect InputXML: "+responseInputXML);

			int Loop_count=0;
			do
			{
				String responseOutputXML=CommonMethods.WFNGExecute(responseInputXML,sJtsIp,iJtsPort,1);
				DCC_MurabahaDealLogger.debug("Response APSelect OutputXML: "+responseOutputXML);

				XMLParser xmlParserSocketDetails= new XMLParser(responseOutputXML);
				String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
				DCC_MurabahaDealLogger.debug("ResponseMainCode: "+responseMainCode);



				int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
				DCC_MurabahaDealLogger.debug("ResponseTotalRecords: "+responseTotalRecords);

				if (responseMainCode.equals("0") && responseTotalRecords > 0)
				{

					String responseXMLData=xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData =responseXMLData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

					XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
					//DCC_MurabahaDealLogger.debug("ResponseXMLData: "+responseXMLData);

					outputResponseXML=xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
					//DCC_MurabahaDealLogger.debug("OutputResponseXML: "+outputResponseXML);

					if("".equalsIgnoreCase(outputResponseXML)){
						outputResponseXML="Error";
					}
					break;
				}
				Loop_count++;
				Thread.sleep(1000);
			}
			while(Loop_count<integrationWaitTime);
			DCC_MurabahaDealLogger.debug("integrationWaitTime: "+integrationWaitTime);

		}
		catch(Exception e)
		{
			DCC_MurabahaDealLogger.debug("Exception occurred in outputResponseXML" + e.getMessage());
			outputResponseXML="Error";
		}

		return outputResponseXML;
	}

	static String socketConnection(String cabinetName, String username, String sessionId, String sJtsIp, String iJtsPort, String wi_name, String ws_name,
			int connection_timeout, int integrationWaitTime, HashMap<String, String> socketDetailsMap, StringBuilder sInputXML)
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

			DCC_MurabahaDealLogger.debug("userName "+ username);
			DCC_MurabahaDealLogger.debug("SessionId "+ sessionId);

			socketServerIP=socketDetailsMap.get("SocketServerIP");
			DCC_MurabahaDealLogger.debug("SocketServerIP "+ socketServerIP);
			socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			DCC_MurabahaDealLogger.debug("SocketServerPort "+ socketServerPort);

			if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0))
			{

				socket = new Socket(socketServerIP, socketServerPort);
				socket.setSoTimeout(connection_timeout*1000);
				out = socket.getOutputStream();
				socketInputStream = socket.getInputStream();
				dout = new DataOutputStream(out);
				din = new DataInputStream(socketInputStream);
				DCC_MurabahaDealLogger.debug("Dout " + dout);
				DCC_MurabahaDealLogger.debug("Din " + din);

				outputResponse = "";

				inputRequest = getRequestXML( cabinetName,sessionId ,wi_name, ws_name, username, sInputXML);

				if (inputRequest != null && inputRequest.length() > 0)
				{
					int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
					DCC_MurabahaDealLogger.debug("RequestLen: "+inputRequestLen + "");
					inputRequest = inputRequestLen + "##8##;" + inputRequest;
					DCC_MurabahaDealLogger.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
					dout.write(inputRequest.getBytes("UTF-16LE"));dout.flush();
				}
				byte[] readBuffer = new byte[500];
				int num = din.read(readBuffer);
				if (num > 0)
				{

					byte[] arrayBytes = new byte[num];
					System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
					outputResponse = outputResponse+ new String(arrayBytes, "UTF-16LE");
					inputMessageID = outputResponse;
					DCC_MurabahaDealLogger.debug("OutputResponse: "+outputResponse);

					if(!"".equalsIgnoreCase(outputResponse))
						outputResponse = getResponseXML(cabinetName,sJtsIp,iJtsPort,sessionId, wi_name,outputResponse,integrationWaitTime );

					if(outputResponse.contains("&lt;"))
					{
						outputResponse=outputResponse.replaceAll("&lt;", "<");
						outputResponse=outputResponse.replaceAll("&gt;", ">");
					}
				}
				socket.close();

				outputResponse = outputResponse.replaceAll("</MessageId>","</MessageId>/n<InputMessageId>"+inputMessageID+"</InputMessageId>");

				//DCC_MurabahaDealLogger.debug("outputResponse "+outputResponse);
				return outputResponse;

			}

			else
			{
				DCC_MurabahaDealLogger.debug("SocketServerIp and SocketServerPort is not maintained "+"");
				DCC_MurabahaDealLogger.debug("SocketServerIp is not maintained "+	socketServerIP);
				DCC_MurabahaDealLogger.debug(" SocketServerPort is not maintained "+	socketServerPort);
				return "Socket Details not maintained";
			}

		}

		catch (Exception e)
		{
			DCC_MurabahaDealLogger.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
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
				DCC_MurabahaDealLogger.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
				//printException(e);
			}
		}
	}

	private static  HashMap<String, String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort, String sessionID) {
		HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

		try {
			DCC_MurabahaDealLogger.debug("Fetching Socket Connection Details.");
			System.out.println("Fetching Socket Connection Details.");

			String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'DCC' and CallingSource = 'Utility'";

			String socketDetailsInputXML = CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
			DCC_MurabahaDealLogger.debug("Socket Details APSelect InputXML: " + socketDetailsInputXML);

			String socketDetailsOutputXML = CommonMethods.WFNGExecute(socketDetailsInputXML, sJtsIp, iJtsPort, 1);
			DCC_MurabahaDealLogger.debug("Socket Details APSelect OutputXML: " + socketDetailsOutputXML);

			XMLParser xmlParserSocketDetails = new XMLParser(socketDetailsOutputXML);
			String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			DCC_MurabahaDealLogger.debug("SocketDetailsMainCode: " + socketDetailsMainCode);

			int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			DCC_MurabahaDealLogger.debug("SocketDetailsTotalRecords: " + socketDetailsTotalRecords);

			if (socketDetailsMainCode.equalsIgnoreCase("0") && socketDetailsTotalRecords > 0) {
				String xmlDataSocketDetails = xmlParserSocketDetails.getNextValueOf("Record");
				xmlDataSocketDetails = xmlDataSocketDetails.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

				XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

				String socketServerIP = xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
				DCC_MurabahaDealLogger.debug("SocketServerIP: " + socketServerIP);
				socketDetailsMap.put("SocketServerIP", socketServerIP);

				String socketServerPort = xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
				DCC_MurabahaDealLogger.debug("SocketServerPort " + socketServerPort);
				socketDetailsMap.put("SocketServerPort", socketServerPort);

				DCC_MurabahaDealLogger.debug("SocketServer Details found.");
				System.out.println("SocketServer Details found.");

			}
		} catch (Exception e) {
			DCC_MurabahaDealLogger.debug("Exception in getting Socket Connection Details: " + e.getMessage());
			System.out.println("Exception in getting Socket Connection Details: " + e.getMessage());
		}

		return socketDetailsMap;
	}




	public String MurabahaConfirmIntegrationCall(XMLParser xmlParserSocketDetails,String callSeq)
	{

		try{
			StringBuilder murabahaConfirmInputXML = readRequestXmlSample(xmlParserSocketDetails , "MURABAHA_CONFIRMDEAL_REQ2",callSeq);

			socketConnectionMap =socketConnectionDetails(cabinetname, JtsIp, JtsPort, sessionID); 


			outputResponse = socketConnection(cabinetname, CommonConnection.getUsername(), sessionID, JtsIp, JtsPort, processInstanceId, ws_Name, 60, 65,socketConnectionMap, murabahaConfirmInputXML );


			XMLParser xmlParserSocketDetails1= new XMLParser(outputResponse);
			DCC_MurabahaDealLogger.debug(" xmlParserSocketDetails : "+xmlParserSocketDetails1);

			if(outputResponse.contains("<ReturnDesc>"))
				ReturnDesc=xmlParserSocketDetails1.getValueOf("ReturnDesc");
			if(outputResponse.contains("<ReturnCode>"))
				ReturnCode=xmlParserSocketDetails1.getValueOf("ReturnCode");


			if("0000".equalsIgnoreCase(ReturnCode))
			{
				return "Success";
			}

		}catch(Exception e){
			return "Fail";
		}
		return "";
	}
	
	public String MurabahaConfirmIntegrationCall2(XMLParser xmlParserSocketDetails,String callSeq)
	{

		try{
			StringBuilder murabahaConfirmInputXML2 = readRequestXmlSample(xmlParserSocketDetails ,"MURABAHA_CONFIRMDEAL_REQ3",callSeq);

			socketConnectionMap =socketConnectionDetails(cabinetname, JtsIp, JtsPort, sessionID); 

			
			outputResponse = socketConnection(cabinetname, CommonConnection.getUsername(), sessionID, JtsIp, JtsPort, processInstanceId, ws_Name, 60, 65,socketConnectionMap, murabahaConfirmInputXML2 );


			XMLParser xmlParserSocketDetails2= new XMLParser(outputResponse);
			DCC_MurabahaDealLogger.debug(" xmlParserSocketDetails : "+xmlParserSocketDetails2);

			if(outputResponse.contains("<ReturnDesc>"))
				ReturnDesc=xmlParserSocketDetails2.getValueOf("ReturnDesc");
			if(outputResponse.contains("<ReturnCode>"))
				ReturnCode=xmlParserSocketDetails2.getValueOf("ReturnCode");


			if("0000".equalsIgnoreCase(ReturnCode))
			{
				
				// Insert in Murabaha Table. values from response xml tags
				String APMQPUTGET_Output = xmlParserSocketDetails.getValueOf("APMQPUTGET_Output");
				String ResponseFlag = getTagValues(APMQPUTGET_Output, "ResponseFlag");
				String TranCurrency = getTagValues(APMQPUTGET_Output, "TranCurrency");
				String TranSettlementDate = getTagValues(APMQPUTGET_Output, "TranSettlementDate");
				String TranOriginalMaturityDate = getTagValues(APMQPUTGET_Output, "TranOriginalMaturityDate");
				String TranOriginalTradeDate = getTagValues(APMQPUTGET_Output, "TranOriginalTradeDate");
				String TranPrincipal = getTagValues(APMQPUTGET_Output, "TranPrincipal");
				String TranQuantityUnit = getTagValues(APMQPUTGET_Output, "TranQuantityUnit");
				String TransactionNo = getTagValues(APMQPUTGET_Output, "TransactionNo");
				String TranAgreedProfitRate = getTagValues(APMQPUTGET_Output, "TranAgreedProfitRate");
				String TranPrice = getTagValues(APMQPUTGET_Output, "TranPrice");
				String TranClientName = getTagValues(APMQPUTGET_Output, "TranClientName");
				String TranMaturityDate = getTagValues(APMQPUTGET_Output, "TranMaturityDate");
				String TranTotalPrincipal = getTagValues(APMQPUTGET_Output, "TranTotalPrincipal");
				String TranOriginalSettlementDate = getTagValues(APMQPUTGET_Output, "TranOriginalSettlementDate");
				String TranQuantity = getTagValues(APMQPUTGET_Output, "TranQuantity");
				String URL = xmlParserSocketDetails.getValueOf("DocumentsURI");
				String KEY = xmlParserSocketDetails.getValueOf("TransactionKey");
				
				String columnNames = "WI_NAME,CALL_SEQ,ResponseFlag,"
						+"TranCurrency,TranSettlementDate,TranOriginalMaturityDate,TranOriginalTradeDate,TranPrincipal,"
						+"TranQuantityUnit,TransactionNo,TranAgreedProfitRate,TranPrice,TranClientName,TranMaturityDate,"
						+"TranTotalPrincipal,TranOriginalSettlementDate,TranQuantity,DocumentsURI,TransactionKey,TradeDate";
				String columnValues = "'" + processInstanceId + "','"+callSeq+"','"+ResponseFlag + "','" + TranCurrency + "','" 
						+ TranSettlementDate + "','" + TranOriginalMaturityDate + "','"+ TranOriginalTradeDate+ "','"+TranPrincipal  +"','"
						+ TranQuantityUnit + "','"+ TransactionNo + "','"+ TranAgreedProfitRate+ "','" +  TranPrice+"','"
						+  TranClientName +"','"+  TranMaturityDate +"','"+  TranTotalPrincipal +"','"+  TranOriginalSettlementDate +"','"+  TranQuantity  + "','"+URL+"','"+KEY+"','"+TradeDate+"'";

				String apInsertInputXML = CommonMethods.apInsert(cabinetname, sessionID, columnNames, columnValues, "NG_DCC_MURABAHA_RESPONSE_DATA");
				DCC_MurabahaDealLogger.debug("APInsertInputXML: " + apInsertInputXML);

				String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, JtsIp, JtsPort, 1);
				DCC_MurabahaDealLogger.debug("APInsertOutputXML: " + apInsertOutputXML);

				XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
				String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
				DCC_MurabahaDealLogger.debug("Status of apInsertMaincode  " + apInsertMaincode);

				if (apInsertMaincode.equalsIgnoreCase("0")) {
					DCC_MurabahaDealLogger.debug("ApInsert successful: " + apInsertMaincode);
					return "Success";
				} else {
					DCC_MurabahaDealLogger.debug("ApInsert failed: " + apInsertMaincode);
					return "Fail";
				}
				//updateDataInExtTable(processInstanceId,"Card_Status","Blocked");
				
			}

		}catch(Exception e){
			DCC_MurabahaDealLogger.debug("Exception in MurabahaConfirmIntegrationCall2 method " + e.toString());
			return "Fail";
		}
		return "";
	}
	
	
}
