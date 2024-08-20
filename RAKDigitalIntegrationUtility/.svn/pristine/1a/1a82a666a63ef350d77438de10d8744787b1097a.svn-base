package com.newgen.DCC.AnnualFeeEnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimerTask;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;

public class DCC_AnnualFeeSysIntegration extends TimerTask{

	private static NGEjbClient ngEjbClient;

	static Map<String, String> configParamMap = new HashMap<String, String>();
	DCC_AnnualFeeIntegration obj_DCC_AnnualFeeIntegration = new DCC_AnnualFeeIntegration();
	private static String sessionID = "";

	@Override
	public void run()
	{
		
		String cabinetName = "";
		String jtsIP = "";
		String jtsPort = "";
		String queueID = "";
		String UserName = "";
		int socketConnectionTimeout=0;
		int integrationWaitTime=0;
		int sleepIntervalInMin=0;
		int TrialTime = 0;
		int ErrorCount = 0;
		String ws_name = "";
		try
		{
			DCC_AnnualFeeLog.setLogger();
			ngEjbClient = NGEjbClient.getSharedInstance();

			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger.error("Could not Read Config Properties");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("JTSPORT: " + jtsPort);

			UserName = CommonConnection.getUsername();
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("UserName: " + UserName);

			socketConnectionTimeout=Integer.parseInt(configParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(configParamMap.get("INTEGRATION_WAIT_TIME"));
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(configParamMap.get("SleepIntervalInMin"));
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);

			TrialTime=Integer.parseInt(configParamMap.get("TrialTime"));
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("TrialTime: "+TrialTime);

			ErrorCount=Integer.parseInt(configParamMap.get("ErrorCount"));
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("ErrorCount: "+ErrorCount);
			
			ws_name=configParamMap.get("ws_name");
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("ws_name: "+ws_name);
			
			LocalTime TimeFromrun = LocalTime.parse(configParamMap.get("TimeFrom"));
			LocalTime TimeTorun = LocalTime.parse(configParamMap.get("TimeTo"));
			sessionID = CommonConnection.getSessionID(DCC_AnnualFeeLog.DCC_AnnualFeeLogger, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Could Not Connect to Server!");
			}
			else
			{
				HashMap<String, String> socketDetailsMap = CommonMethods.socketConnectionDetails(cabinetName, jtsIP, jtsPort, sessionID);
				while (true) {
					DCC_AnnualFeeLog.setLogger();
					LocalTime now = LocalTime.now();
					System.out.println(TimeFromrun);
					DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("TimeFromrun: " + TimeFromrun);
					System.out.println(now);
					DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("currTime: " + now);
					System.out.println(TimeTorun);
					DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("TimeTorun: " + TimeTorun);
					if(now.isAfter(TimeFromrun) && now.isBefore(TimeTorun))
					{
						DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Session ID found: " + sessionID);
						DCC_AnnualFeeLog.setLogger();
						updateflagToComplete(cabinetName, jtsIP, jtsPort);
						startProcessingFeeUtility(cabinetName, UserName, jtsIP, jtsPort, queueID, socketConnectionTimeout, integrationWaitTime, socketDetailsMap, TrialTime,
								ErrorCount,ws_name);
					}
					Thread.sleep(sleepIntervalInMin*60*1000);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.error("Exception Occurred in DAO Prime CBS  : " + e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.error("Exception Occurred in DAO Prime CBS  : " + result);
		}
	}

	private void startProcessingFeeUtility(String cabinetName, String UserName, String sJtsIp, String iJtsPort, String queueID, 
			int socketConnectionTimeOut, int integrationWaitTime, HashMap<String, String> socketDetailsMap, int trialTime, int errorCount,String ws_name)
	{

		try {
			// Validate Session ID
			sessionID = CommonConnection.getSessionID(DCC_AnnualFeeLog.DCC_AnnualFeeLogger, false);

			if (sessionID == null || sessionID.equalsIgnoreCase("") || sessionID.equalsIgnoreCase("null")) {
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger.error("Could Not Get Session ID " + sessionID);
				return;
			}
			// fetch all workitem in which Service fee flag is R
			String DBQuery1 = "select Wi_Name,entry_date_time from ng_dcc_exttable  with(nolock) where service_fee_flag = 'R'";

			String extTabDataIPXML1 = CommonMethods.apSelectWithColumnNames(DBQuery1, cabinetName,sessionID);
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("extTabDataIPXML: " + extTabDataIPXML1);

			String extTabDataOPXML1 = CommonMethods.WFNGExecute(extTabDataIPXML1,sJtsIp ,iJtsPort , 1);
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("extTabDataOPXML: " + extTabDataOPXML1);

			XMLParser xmlParserData1 = new XMLParser(extTabDataOPXML1);
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Fetching all Workitems in which Service fee flag is R");
			System.out.println("Fetching all Workitems in which Service fee flag is R");
			
			String fetchWorkItemListMainCode = "";
			if(!(xmlParserData1.getValueOf("MainCode") == null || "".equalsIgnoreCase(xmlParserData1.getValueOf("MainCode")))){
		    fetchWorkItemListMainCode = xmlParserData1.getValueOf("MainCode");
			}
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("FetchWorkItemListMainCode: " + fetchWorkItemListMainCode);

			String str_fetchWorkitemListCount = xmlParserData1.getValueOf("TotalRetrieved");
			int fetchWorkitemListCount = 0;
			if(!(str_fetchWorkitemListCount == null || "".equalsIgnoreCase(str_fetchWorkitemListCount))){
			fetchWorkitemListCount = Integer.parseInt(str_fetchWorkitemListCount);
			}
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("RetrievedCount for WMFetchWorkList Call: " + fetchWorkitemListCount);
			
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Number of workitems retrievedin which Service fee flag is R: " + fetchWorkitemListCount);
			System.out.println("Number of workitems retrieved in which Service fee flag is R: " + fetchWorkitemListCount);
			

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0 ) {
				for (int i = 0; i < fetchWorkitemListCount; i++) {
					String xmlDataExtTab = xmlParserData1.getNextValueOf("Record");
					xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
					
					DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Parsing <Records> in WMFetchWorkList OutputXML: " + xmlDataExtTab);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(xmlDataExtTab);

					String processInstanceID = xmlParserfetchWorkItemData.getValueOf("Wi_Name");
					DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Current ProcessInstanceID: " + processInstanceID);

					DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Processing Workitem: " + processInstanceID);
					System.out.println("\nProcessing Workitem: " + processInstanceID);

					String entryDateTime = xmlParserfetchWorkItemData.getValueOf("entry_date_time");
					DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Current EntryDateTime: " + entryDateTime);

					obj_DCC_AnnualFeeIntegration.CardEnquiry(cabinetName,UserName,  sJtsIp, iJtsPort,sessionID,processInstanceID, 
							socketConnectionTimeOut, integrationWaitTime, socketDetailsMap,  trialTime, errorCount,ws_name,entryDateTime);

				}
			}
		}catch (Exception e) {
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Exception: " + e.getMessage());
		}





	}
	private void updateflagToComplete(String cabinetName, String sJtsIp, String iJtsPort)
	{

		try {
			// Validate Session ID
			sessionID = CommonConnection.getSessionID(DCC_AnnualFeeLog.DCC_AnnualFeeLogger, false);

			if (sessionID == null || sessionID.equalsIgnoreCase("") || sessionID.equalsIgnoreCase("null")) {
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger.error("Could Not Get Session ID " + sessionID);
				return;
			}
			// fetch all workitem in which Service fee flag is R
			String DBQuery1 = "select Wi_Name,entry_date_time from ng_dcc_exttable  with(nolock) where service_fee_flag = 'D'";

			String extTabDataIPXML1 = CommonMethods.apSelectWithColumnNames(DBQuery1, cabinetName,sessionID);
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("extTabDataIPXML: " + extTabDataIPXML1);

			String extTabDataOPXML1 = CommonMethods.WFNGExecute(extTabDataIPXML1,sJtsIp ,iJtsPort , 1);
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("extTabDataOPXML: " + extTabDataOPXML1);

			XMLParser xmlParserData1 = new XMLParser(extTabDataOPXML1);
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Fetching all Workitems in which Service fee flag is R");
			System.out.println("Fetching all Workitems in which Service fee flag is R");
			
			String fetchWorkItemListMainCode = "";
			if(!(xmlParserData1.getValueOf("MainCode") == null || "".equalsIgnoreCase(xmlParserData1.getValueOf("MainCode")))){
		    fetchWorkItemListMainCode = xmlParserData1.getValueOf("MainCode");
			}
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("FetchWorkItemListMainCode: " + fetchWorkItemListMainCode);

			String str_fetchWorkitemListCount = xmlParserData1.getValueOf("TotalRetrieved");
			int fetchWorkitemListCount = 0;
			if(!(str_fetchWorkitemListCount == null || "".equalsIgnoreCase(str_fetchWorkitemListCount))){
			fetchWorkitemListCount = Integer.parseInt(str_fetchWorkitemListCount);
			}
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("RetrievedCount for WMFetchWorkList Call: " + fetchWorkitemListCount);
			
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Number of workitems retrievedin which Service fee flag is R: " + fetchWorkitemListCount);
			System.out.println("Number of workitems retrieved in which Service fee flag is R: " + fetchWorkitemListCount);
		
			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0 ) {
				for (int i = 0; i < fetchWorkitemListCount; i++) {
					String xmlDataExtTab = xmlParserData1.getNextValueOf("Record");
					xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
					
					DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Parsing <Records> in WMFetchWorkList OutputXML: " + xmlDataExtTab);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(xmlDataExtTab);

					String processInstanceID = xmlParserfetchWorkItemData.getValueOf("Wi_Name");
					DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Current ProcessInstanceID: " + processInstanceID);

					DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Processing Workitem: " + processInstanceID);
					System.out.println("\nProcessing Workitem: " + processInstanceID);
					
					try {
						String columnNames1 = "service_fee_flag";
						String columnValues1 = "'C'";
						String sWhereClause = "Wi_Name = '" + processInstanceID + "' ";

						String extTableIPUpdateXml = CommonMethods.apUpdateInput(cabinetName,sessionID, "ng_dcc_exttable",columnNames1, columnValues1, sWhereClause);
						DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Input XML for apUpdateInput for " + "ng_dcc_exttable" + " Table : " + extTableIPUpdateXml);

						String extTableOPUpdateXml = CommonMethods.WFNGExecute(extTableIPUpdateXml,sJtsIp,iJtsPort, 1);
						DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Output XML for apUpdateInput for " + "ng_dcc_exttable" + " Table : " + extTableOPUpdateXml);

						XMLParser sXMLParserChild = new XMLParser(extTableOPUpdateXml);
						String StrMainCode = sXMLParserChild.getValueOf("MainCode");

						if (StrMainCode.equals("0"))
							DCC_AnnualFeeLog.DCC_AnnualFeeLogger
									.debug("Successful in apUpdateInput the record in : " + "ng_dcc_exttable");
						else {
							DCC_AnnualFeeLog.DCC_AnnualFeeLogger
									.debug("Error in Executing apUpdateInput sOutputXML : " + extTableOPUpdateXml);
							System.out.println("WMgetWorkItemCall failed: " + processInstanceID);
							DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("WMgetWorkItemCall failed: " + processInstanceID);
						}
					} catch (Exception e) {
						DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Exception: " + e.getMessage());
					}

				}
			}
		}catch (Exception e) {
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Exception: " + e.getMessage());
		}

	}

	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "DCC_AnnualFeeConfig.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			{
				String name = (String) names.nextElement();
				configParamMap.put(name, p.getProperty(name));
			}
		}
		catch (Exception e)
		{
			return -1 ;
		}
		return 0;
	}


	// date format should be in yyyy-MM-dd hh:mm:ss format.
	public static long convertDateToLong(String date)
	{
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date date_long = null;
		try {
			date_long = simpleDateFormat.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date_long.getTime();
	}


	protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort, int flag)
			throws IOException, Exception {
		DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("In WF NG Execute : " + serverPort);
		try {
			if (serverPort.startsWith("33"))
				return WFCallBroker.execute(ipXML, jtsServerIP, Integer.parseInt(serverPort), 1);
			else
				return ngEjbClient.makeCall(jtsServerIP, serverPort, "WebSphere", ipXML);
		} catch (Exception e) {
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Exception Occured in WF NG Execute : " + e.getMessage());
			e.printStackTrace();
			return "Error";
		}
	}

}
