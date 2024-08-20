package Test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.encryption.DataEncryption;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;


public class TestMQService {

	private static NGEjbClient ngEjbClientCIFVer;
	private static Map<String, String> mainPropMap= new HashMap<String, String>();
	static Map<String, String> DAOExtensionConfigParamMap= new HashMap<String, String>();


	static int socketConnectionTimeout=0;
	static int integrationWaitTime=0;
	static int sleepIntervalInMin=0;
	public static int sessionCheckInt=0;
	public static int waitLoop=50;
	public static int loopCount=50;
	public String fromMailID="";
	public String toMailID = "";
	public String mailSubject = "";
	public String MailStr="";
	public static String jtsIP = "";
	public static String jtsPort = "";
	public static String sessionID = "";
	
	public static void main(String[] args) {
		String cabinetName = "";
			//String queueID = "";
		String queueIDDAO = "";
		String queueIDDCC = "";

		try
		{
			testExtensionLogger.setLogger();
			ngEjbClientCIFVer = NGEjbClient.getSharedInstance();

			testExtensionLogger.DAOExtensionLogger.debug("Connecting to Cabinet.");

			int mainPropFileReadCode = readMainPropFile();

			if(mainPropFileReadCode!=0)
			{
				System.out.println("Error in Readin Main Property FIle");
				
				return;
			}

			try
			{
				int socketPort =  Integer.parseInt(mainPropMap.get("Utility_Port"));
				if(socketPort==0)
				{
					System.out.println("Not able to Get Utility Port");
					return;
				}
				ServerSocket serverSocket = new ServerSocket(socketPort);

				CommonConnection.setUsername(mainPropMap.get("UserName"));
				CommonConnection.setPassword(DataEncryption.decrypt(mainPropMap.get("Password")));
				CommonConnection.setJTSIP(mainPropMap.get("JTSIP"));
				CommonConnection.setJTSPort(mainPropMap.get("JTSPort"));
				CommonConnection.setsSMSPort(mainPropMap.get("SMSPort"));
				CommonConnection.setCabinetName(mainPropMap.get("CabinetName"));
				CommonConnection.setsVolumeID(mainPropMap.get("VolumeID"));
				CommonConnection.setsSiteID(mainPropMap.get("SiteID"));
				
				CommonConnection.setOFCabinetName(mainPropMap.get("OFCabinetName"));
				CommonConnection.setOFBAISProcessDefId(mainPropMap.get("OFBAISProcessDefId"));
				CommonConnection.setOFJTSIP(mainPropMap.get("OFJTSIP"));
				CommonConnection.setOFJTSPort(mainPropMap.get("OFJTSPort"));
				CommonConnection.setOFVOLUMNID(mainPropMap.get("OFVOLUMNID"));
				CommonConnection.setOFUserName(mainPropMap.get("OFUserName"));
				CommonConnection.setOFPassword(mainPropMap.get("OFPassword"));
				
			}
			catch(Exception e){
				
			}

			
			cabinetName = CommonConnection.getCabinetName();
			testExtensionLogger.DAOExtensionLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			testExtensionLogger.DAOExtensionLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			testExtensionLogger.DAOExtensionLogger.debug("JTSPORT: " + jtsPort);

			socketConnectionTimeout=60;
			testExtensionLogger.DAOExtensionLogger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=65;
			testExtensionLogger.DAOExtensionLogger.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=5;
			testExtensionLogger.DAOExtensionLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);

			sessionID = CommonConnection.getSessionID(testExtensionLogger.DAOExtensionLogger, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				testExtensionLogger.DAOExtensionLogger.debug("Could Not Connect to Server!");
			}
			else
			{
				testExtensionLogger.DAOExtensionLogger.debug("Session ID found: " + sessionID);
				HashMap<String, String> socketDetailsMap = socketConnectionDetails(cabinetName, jtsIP, jtsPort, sessionID);
				while (true) {
					testExtensionLogger.setLogger();
					testExtensionLogger.DAOExtensionLogger.debug("iRBL CIF Verification...123.");
					TestInputXML(cabinetName, jtsIP, jtsPort, sessionID, queueIDDAO, socketConnectionTimeout, integrationWaitTime, socketDetailsMap);
					System.out.println("queue ID dcc!"+queueIDDAO);
					System.out.println("No More workitems to Process, Sleeping!");
					Thread.sleep(sleepIntervalInMin * 60 * 1000);
				}
			}
		}

		catch(Exception e)
		{
			e.printStackTrace();
			testExtensionLogger.DAOExtensionLogger.error("Exception Occurred in DAO_ExtensionP4 : "+e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			testExtensionLogger.DAOExtensionLogger.error("Exception Occurred in DAO_ExtensionP4 : "+result);
		}
	}

	private static int readMainPropFile()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "Main_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			{
			    String name = (String) names.nextElement();
			    mainPropMap.put(name, p.getProperty(name));
			}

		} catch (Exception e) {

			return -1 ;
		}
		return 0;
	}


	private static void TestInputXML(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String queueID, 
	int socketConnectionTimeOut, int integrationWaitTime, HashMap<String, String> socketDetailsMap){
		
		final String ws_name="Extension_P4";
		try{
			
			final HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();
			
			sessionID  = CommonConnection.getSessionID(testExtensionLogger.DAOExtensionLogger, false);

			if (sessionID == null || sessionID.equalsIgnoreCase("") || sessionID.equalsIgnoreCase("null"))
			{
				testExtensionLogger.DAOExtensionLogger.error("Could Not Get Session ID "+sessionID);
				return;
			}

					String processInstanceID="test1234";
					testExtensionLogger.DAOExtensionLogger.debug("Current ProcessInstanceID: "+processInstanceID);

					testExtensionLogger.DAOExtensionLogger.debug("Processing Workitem: "+processInstanceID);
					System.out.println("\nProcessing Workitem: "+processInstanceID);

					String WorkItemID="1";
					testExtensionLogger.DAOExtensionLogger.debug("Current WorkItemID: "+WorkItemID);

					String entryDateTime="";
					testExtensionLogger.DAOExtensionLogger.debug("Current EntryDateTime: "+entryDateTime);

					String ActivityName="test";
					testExtensionLogger.DAOExtensionLogger.debug("ActivityName: "+ActivityName);
					
					String ActivityID = "test";
					testExtensionLogger.DAOExtensionLogger.debug("ActivityID: "+ActivityID);
					String ActivityType = "test";
					testExtensionLogger.DAOExtensionLogger.debug("ActivityType: "+ActivityType);
					String ProcessDefId = "1";
					testExtensionLogger.DAOExtensionLogger.debug("ProcessDefId: "+ProcessDefId);
					
					String decisionValue="";
					
				
		    	    
		            //Reading a txt file from folder
		            String fileLocation= new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DAO_Integration")
		    		.append(System.getProperty("file.separator")).append("Testmq.xml").toString();
		            
		            BufferedReader sbf=new BufferedReader(new FileReader(fileLocation));
		    		
		    		StringBuilder sb=new StringBuilder();
		    		String line=sbf.readLine();
		    		while(line!=null)
		    		{
		    			sb.append(line);
		    			sb.append(System.lineSeparator());
		    			line=sbf.readLine();
		    		}
		    		
		    		String Extension = sb.toString();
		    		String integrationStatus="Success";
					String attributesTag;
					String ErrDesc = "";
					StringBuilder finalString=new StringBuilder();
					finalString = finalString.append(Extension);
					//changes need to done to updae the correct flag
					HashMap<String, String> socketConnectionMap =socketConnectionDetails(cabinetName, sJtsIp, iJtsPort, sessionID); 
					
					integrationStatus = socketConnection(cabinetName, CommonConnection.getUsername(), sessionId, sJtsIp, iJtsPort, processInstanceID, ws_name, 60, 65,socketConnectionMap, finalString);
					
					// - xml parse for getting out the return code.
					System.out.println("Response: "+integrationStatus);
					XMLParser xmlParserSocketDetails= new XMLParser(integrationStatus);
					testExtensionLogger.DAOExtensionLogger.debug(" xmlParserSocketDetails : "+xmlParserSocketDetails);
				    String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
				    testExtensionLogger.DAOExtensionLogger.debug("Return Code: "+return_code+ "WI: "+processInstanceID);
				    String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
				    testExtensionLogger.DAOExtensionLogger.debug("return_desc : "+return_desc+ "WI: "+processInstanceID);
					
				    String MsgId ="";
				    if (integrationStatus.contains("<MessageId>"))
						MsgId = xmlParserSocketDetails.getValueOf("MessageId");
					
				    testExtensionLogger.DAOExtensionLogger.debug("MsgId : "+MsgId+" for WI: "+processInstanceID);
					
				    if("0000".equalsIgnoreCase(return_code) || "PRIME4 : Extension field already exists for the entity".equalsIgnoreCase(return_desc))
				    {
				    	integrationStatus="Success";
				    	ErrDesc = "ExtensionP4 Done Successfully";
				    }
					if ("Success".equalsIgnoreCase(integrationStatus))
					{
						decisionValue = "Success";
						testExtensionLogger.DAOExtensionLogger.debug("Decision in success: " +decisionValue);
						attributesTag="<Decision>"+decisionValue+"</Decision>";
						
						
						
					}
					else
					{
						ErrDesc = return_desc; //integrationStatus.replace("~", ",").replace("|", "\n");
						decisionValue = "Failed";
						testExtensionLogger.DAOExtensionLogger.debug("Decision in else : " +decisionValue);
						attributesTag="<Decision>"+decisionValue+"</Decision>";					
												
					}
					testExtensionLogger.DAOExtensionLogger.debug("decisionValue: " +decisionValue);
					testExtensionLogger.DAOExtensionLogger.debug("ErrDesc: " +ErrDesc);
					
					String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionID, processInstanceID,WorkItemID);
					String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML,sJtsIp,iJtsPort,1);
					testExtensionLogger.DAOExtensionLogger.debug("Output XML For WmgetWorkItemCall: " + getWorkItemOutputXml);

					XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
					String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
					testExtensionLogger.DAOExtensionLogger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);

					if (getWorkItemMainCode.trim().equals("0"))
					{
						testExtensionLogger.DAOExtensionLogger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);

						//String assignWorkitemAttributeInputXML=CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionId,processInstanceID,WorkItemID,attributesTag);
						
						String assignWorkitemAttributeInputXML = "<?xml version=\"1.0\"?><WMAssignWorkItemAttributes_Input>"
								+ "<Option>WMAssignWorkItemAttributes</Option>"
								+ "<EngineName>"+cabinetName+"</EngineName>"
								+ "<SessionId>"+sessionID+"</SessionId>"
								+ "<ProcessInstanceId>"+processInstanceID+"</ProcessInstanceId>"
								+ "<WorkItemId>"+WorkItemID+"</WorkItemId>"
								+ "<ActivityId>"+ActivityID+"</ActivityId>"
								+ "<ProcessDefId>"+ProcessDefId+"</ProcessDefId>"
								+ "<LastModifiedTime></LastModifiedTime>"
								+ "<ActivityType>"+ActivityType+"</ActivityType>"
								+ "<complete>D</complete>"
								+ "<AuditStatus></AuditStatus>"
								+ "<Comments></Comments>"
								+ "<UserDefVarFlag>Y</UserDefVarFlag>"
								+ "<Attributes>"+attributesTag+"</Attributes>"
								+ "</WMAssignWorkItemAttributes_Input>";
						
						testExtensionLogger.DAOExtensionLogger.debug("InputXML for assignWorkitemAttribute Call ExtensionP4: "+assignWorkitemAttributeInputXML);

						String assignWorkitemAttributeOutputXML=WFNGExecute(assignWorkitemAttributeInputXML,sJtsIp,
								iJtsPort,1);
						
						testExtensionLogger.DAOExtensionLogger.debug("OutputXML for assignWorkitemAttribute Call ExtensionP4: "+assignWorkitemAttributeOutputXML);
						
						XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
						String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
						testExtensionLogger.DAOExtensionLogger.debug("AssignWorkitemAttribute MainCode: "+assignWorkitemAttributeMainCode);

						if(assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0")){
							testExtensionLogger.DAOExtensionLogger.debug("AssignWorkitemAttribute Successful: "+assignWorkitemAttributeMainCode);
							System.out.println(processInstanceID + "Complete Succesfully with status "+decisionValue);
							testExtensionLogger.DAOExtensionLogger.debug("WorkItem moved to next Workstep.");
						}
						else{
							testExtensionLogger.DAOExtensionLogger.debug("decisionValue : "+decisionValue);
							ErrDesc="Done WI Failed";
							}
						
						DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						
						Date current_date = new Date();
						String formattedEntryDatetime=dateFormat.format(current_date);
						testExtensionLogger.DAOExtensionLogger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);
				
						String entrydatetime = CheckGridDataMap.get("EntryDATETIME");
						if(entrydatetime.equalsIgnoreCase("") || entrydatetime==null){
							entrydatetime = entryDateTime;
						}
						Date d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(entrydatetime);
						String entrydatetime_format = dateFormat.format(d1);

						String columnNames="wi_name,decision_date_time,workstep,user_name,Decision,Remarks,entry_date_time";
						String columnValues="'"+processInstanceID+"','"+formattedEntryDatetime+"','"+ActivityName+"','"
						+CommonConnection.getUsername()+"','"+decisionValue+"','"+ErrDesc+"','"+entrydatetime_format+"'";

						String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionID, columnNames, columnValues,"NG_DAO_GR_DECISION_HISTORY");
						testExtensionLogger.DAOExtensionLogger.debug("APInsertInputXML: "+apInsertInputXML);

						String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
						testExtensionLogger.DAOExtensionLogger.debug("APInsertOutputXML: "+ apInsertInputXML);

						XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
						String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
						testExtensionLogger.DAOExtensionLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

						testExtensionLogger.DAOExtensionLogger.debug("Completed On "+ ActivityName);
						
						if(apInsertMaincode.equalsIgnoreCase("0")){
							
							testExtensionLogger.DAOExtensionLogger.debug("ApInsert successful: "+apInsertMaincode);
							testExtensionLogger.DAOExtensionLogger.debug("Inserted in WiHistory table successfully.");
						}
						else{
							testExtensionLogger.DAOExtensionLogger.debug("ApInsert failed: "+apInsertMaincode);
						}
					}
					else
					{
						getWorkItemMainCode="";
						testExtensionLogger.DAOExtensionLogger.debug("WmgetWorkItem failed: "+getWorkItemMainCode);
						ErrDesc="WI Failed";
					}
				//}
			//}
		}
		catch (Exception e){
			testExtensionLogger.DAOExtensionLogger.debug("Exception: "+e.getMessage());
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

	private static HashMap<String, String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort, String sessionID) {
		HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

		try {
			testExtensionLogger.DAOExtensionLogger.debug("Fetching Socket Connection Details.");
			System.out.println("Fetching Socket Connection Details.");

			String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'DigitalAO' and CallingSource = 'Utility'";

			String socketDetailsInputXML = CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
			testExtensionLogger.DAOExtensionLogger.debug("Socket Details APSelect InputXML: " + socketDetailsInputXML);

			String socketDetailsOutputXML = WFNGExecute(socketDetailsInputXML, sJtsIp, iJtsPort, 1);
			testExtensionLogger.DAOExtensionLogger.debug("Socket Details APSelect OutputXML: " + socketDetailsOutputXML);

			XMLParser xmlParserSocketDetails = new XMLParser(socketDetailsOutputXML);
			String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			testExtensionLogger.DAOExtensionLogger.debug("SocketDetailsMainCode: " + socketDetailsMainCode);

			int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			testExtensionLogger.DAOExtensionLogger.debug("SocketDetailsTotalRecords: " + socketDetailsTotalRecords);

			if (socketDetailsMainCode.equalsIgnoreCase("0") && socketDetailsTotalRecords > 0) {
				String xmlDataSocketDetails = xmlParserSocketDetails.getNextValueOf("Record");
				xmlDataSocketDetails = xmlDataSocketDetails.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

				XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

				String socketServerIP = xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
				testExtensionLogger.DAOExtensionLogger.debug("SocketServerIP: " + socketServerIP);
				socketDetailsMap.put("SocketServerIP", socketServerIP);

				String socketServerPort = xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
				testExtensionLogger.DAOExtensionLogger.debug("SocketServerPort " + socketServerPort);
				socketDetailsMap.put("SocketServerPort", socketServerPort);

				testExtensionLogger.DAOExtensionLogger.debug("SocketServer Details found.");
				System.out.println("SocketServer Details found.");

			}
		} catch (Exception e) {
			testExtensionLogger.DAOExtensionLogger
					.debug("Exception in getting Socket Connection Details: " + e.getMessage());
			System.out.println("Exception in getting Socket Connection Details: " + e.getMessage());
		}

		return socketDetailsMap;
	}

	protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort, int flag)
			throws IOException, Exception {
		testExtensionLogger.DAOExtensionLogger.debug("In WF NG Execute : " + serverPort);
		try {
			if (serverPort.startsWith("33"))
				return WFCallBroker.execute(ipXML, jtsServerIP, Integer.parseInt(serverPort), 1);
			else
				return ngEjbClientCIFVer.makeCall(jtsServerIP, serverPort, "WebSphere", ipXML);
		} catch (Exception e) {
			testExtensionLogger.DAOExtensionLogger.debug("Exception Occured in WF NG Execute : " + e.getMessage());
			e.printStackTrace();
			return "Error";
		}
	}
	
	static String socketConnection(String cabinetName, String username, String sessionId, String sJtsIp, String iJtsPort, String processInstanceID, String ws_name,
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

			testExtensionLogger.DAOExtensionLogger.debug("userName "+ username);
			testExtensionLogger.DAOExtensionLogger.debug("SessionId "+ sessionID);

			socketServerIP=socketDetailsMap.get("SocketServerIP");
			testExtensionLogger.DAOExtensionLogger.debug("SocketServerIP "+ socketServerIP);
			socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			testExtensionLogger.DAOExtensionLogger.debug("SocketServerPort "+ socketServerPort);

	   		if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0))
	   		{

    			socket = new Socket(socketServerIP, socketServerPort);
    			socket.setSoTimeout(connection_timeout*1000);
    			out = socket.getOutputStream();
    			socketInputStream = socket.getInputStream();
    			dout = new DataOutputStream(out);
    			din = new DataInputStream(socketInputStream);
    			testExtensionLogger.DAOExtensionLogger.debug("Dout " + dout);
    			testExtensionLogger.DAOExtensionLogger.debug("Din " + din);

    			outputResponse = "";
    			
    			String History_tablename="";
    			if("DCC".equalsIgnoreCase(processInstanceID.substring(0, 3))){
    				History_tablename= "NG_DCC_XMLLOG_HISTORY";
    			}
    			else{
    					History_tablename= "NG_DAO_XMLLOG_HISTORY";
    			}
    			
    			
    			inputRequest = getRequestXML( cabinetName,sessionID ,processInstanceID, ws_name, username, sInputXML,History_tablename);


    			if (inputRequest != null && inputRequest.length() > 0)
    			{
    				int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
    				testExtensionLogger.DAOExtensionLogger.debug("RequestLen: "+inputRequestLen + "");
    				inputRequest = inputRequestLen + "##8##;" + inputRequest;
    				testExtensionLogger.DAOExtensionLogger.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
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
    				testExtensionLogger.DAOExtensionLogger.debug("OutputResponse: "+outputResponse);

    				if(!"".equalsIgnoreCase(outputResponse))
    					outputResponse = getResponseXML(cabinetName,sJtsIp,iJtsPort,sessionID, processInstanceID,outputResponse,integrationWaitTime );

    				if(outputResponse.contains("&lt;"))
    				{
    					outputResponse=outputResponse.replaceAll("&lt;", "<");
    					outputResponse=outputResponse.replaceAll("&gt;", ">");
    				}
    			}
    			socket.close();

				outputResponse = outputResponse.replaceAll("</MessageId>","</MessageId>/n<InputMessageId>"+inputMessageID+"</InputMessageId>");

				//testExtensionLogger.DAOExtensionLogger.debug("outputResponse "+outputResponse);
				return outputResponse;

    	 		}

    		else
    		{
    			testExtensionLogger.DAOExtensionLogger.debug("SocketServerIp and SocketServerPort is not maintained "+"");
    			testExtensionLogger.DAOExtensionLogger.debug("SocketServerIp is not maintained "+	socketServerIP);
    			testExtensionLogger.DAOExtensionLogger.debug(" SocketServerPort is not maintained "+	socketServerPort);
    			return "Socket Details not maintained";
    		}

		}

		catch (Exception e)
		{
			testExtensionLogger.DAOExtensionLogger.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
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
				testExtensionLogger.DAOExtensionLogger.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
				//printException(e);
			}
		}


	}

	private static String getResponseXML(String cabinetName,String sJtsIp,String iJtsPort, String sessionId, String processInstanceID,String message_ID, int integrationWaitTime)
	{

		String outputResponseXML="";
		try
		{
			String QueryString ="";
			if("DCC".equalsIgnoreCase(processInstanceID.substring(0, 3))){
				QueryString ="select OUTPUT_XML from NG_DCC_XMLLOG_HISTORY with (nolock) where MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+processInstanceID+"'";
			}

		else
			 QueryString = "select OUTPUT_XML from NG_DAO_XMLLOG_HISTORY with (nolock) where MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+processInstanceID+"'";

			String responseInputXML =CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, sessionID);
			testExtensionLogger.DAOExtensionLogger.debug("Response APSelect InputXML: "+responseInputXML);

			int Loop_count=0;
			do
			{
				String responseOutputXML=CommonMethods.WFNGExecute(responseInputXML,sJtsIp,iJtsPort,1);
				testExtensionLogger.DAOExtensionLogger.debug("Response APSelect OutputXML: "+responseOutputXML);

			    XMLParser xmlParserSocketDetails= new XMLParser(responseOutputXML);
			    String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			    testExtensionLogger.DAOExtensionLogger.debug("ResponseMainCode: "+responseMainCode);



			    int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			    testExtensionLogger.DAOExtensionLogger.debug("ResponseTotalRecords: "+responseTotalRecords);

			    if (responseMainCode.equals("0") && responseTotalRecords > 0)
				{

					String responseXMLData=xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData =responseXMLData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

	        		XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
	        		//testExtensionLogger.DAOExtensionLogger.debug("ResponseXMLData: "+responseXMLData);

	        		outputResponseXML=xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
	        		//testExtensionLogger.DAOExtensionLogger.debug("OutputResponseXML: "+outputResponseXML);

	        		if("".equalsIgnoreCase(outputResponseXML)){
	        			outputResponseXML="Error";
	    			}
	        		break;
				}
			    Loop_count++;
			    Thread.sleep(1000);
			}
			while(Loop_count<integrationWaitTime);
			testExtensionLogger.DAOExtensionLogger.debug("integrationWaitTime: "+integrationWaitTime);

		}
		catch(Exception e)
		{
			testExtensionLogger.DAOExtensionLogger.debug("Exception occurred in outputResponseXML" + e.getMessage());
			outputResponseXML="Error";
		}

		return outputResponseXML;

	}
	
	private static String getRequestXML(String cabinetName, String sessionId,
			String processInstanceID, String ws_name, String userName, StringBuilder sInputXML, String tablename)
	{
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("<APMQPUTGET_Input>");
		strBuff.append("<SessionId>" + sessionID + "</SessionId>");
		strBuff.append("<EngineName>" + cabinetName + "</EngineName>");
		strBuff.append("<XMLHISTORY_TABLENAME>"+tablename+"</XMLHISTORY_TABLENAME>");
		strBuff.append("<WI_NAME>" + processInstanceID + "</WI_NAME>");
		strBuff.append("<WS_NAME>" + ws_name + "</WS_NAME>");
		strBuff.append("<USER_NAME>" + userName + "</USER_NAME>");
		strBuff.append("<MQ_REQUEST_XML>");
		strBuff.append(sInputXML);
		strBuff.append("</MQ_REQUEST_XML>");
		strBuff.append("</APMQPUTGET_Input>");
		testExtensionLogger.DAOExtensionLogger.debug("GetRequestXML: "+ strBuff.toString());
		return strBuff.toString();
	}
}
