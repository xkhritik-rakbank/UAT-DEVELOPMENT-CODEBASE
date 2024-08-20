package com.newgen.DCC.CAMGenCIFUpdate;

import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.XMLParser;

public class Digital_CC_GenCAMReport implements Runnable{

// code by deepanshu for cam report on 08/09
	
	static Map<String, String> configParamMap = new HashMap<String, String>();

	public void run()
	{
		String sessionID = "";
		String cabinetName = "";
		String jtsIP = "";
		String jtsPort = "";
		String queueID = "";
		String UserName = "";
		int socketConnectionTimeout=0;
		int integrationWaitTime=0;
		int sleepIntervalInMin=0;
		try
		{
			
			Digital_CCLog.setLogger();
			
			Digital_CCLog.Digital_CC.debug("Inside try Catch ...");

			Digital_CCLog.Digital_CC.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			Digital_CCLog.Digital_CC.debug("configReadStatus "+configReadStatus);
			if (configReadStatus != 0) {
				Digital_CCLog.Digital_CC.error("Could not Read Config Properties");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			Digital_CCLog.Digital_CC.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			Digital_CCLog.Digital_CC.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			Digital_CCLog.Digital_CC.debug("JTSPORT: " + jtsPort);

			queueID = configParamMap.get("queueID");
			Digital_CCLog.Digital_CC.debug("QueueID: " + queueID);
			
			socketConnectionTimeout=Integer.parseInt(configParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			Digital_CCLog.Digital_CC.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(configParamMap.get("INTEGRATION_WAIT_TIME"));
			Digital_CCLog.Digital_CC.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(configParamMap.get("SleepIntervalInMin"));
			Digital_CCLog.Digital_CC.debug("SleepIntervalInMin: "+sleepIntervalInMin);
			
			// error remove needed
			sessionID = CommonConnection.getSessionID(Digital_CCLog.Digital_CC, false); // need to be clearify this log 

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				Digital_CCLog.Digital_CC.debug("Could Not Connect to Server!");
			}
			else
			{
				HashMap<String, String> socketDetailsMap = CommonMethods.socketConnectionDetails(cabinetName, jtsIP, jtsPort, sessionID);
				while (true) {
					Digital_CCLog.Digital_CC.debug("Session ID found: " + sessionID);
					Digital_CCLog.setLogger();
					
					genrateCamAndUpdateCIF(cabinetName, UserName, jtsIP, jtsPort, sessionID, queueID, socketConnectionTimeout, integrationWaitTime, socketDetailsMap);
					
					Thread.sleep(sleepIntervalInMin*60*1000);
				}
			}
		}
		catch(Exception e)
		{
			// watch here
			Digital_CCLog.Digital_CC.error("Exception Occurred in DCC  : " , e);
		
		}
	}
	
	// method define here 
	private void genrateCamAndUpdateCIF(String cabinetName, String UserName, String sJtsIp, String iJtsPort, String sessionId, String queueID, 
			int socketConnectionTimeOut, int integrationWaitTime, HashMap<String, String> socketDetailsMap)
	{
		final String ws_name="Digital_CC_Sys_CAMGen_UpCIF"; // write workstep name 
		
		try
		{
			//Validate Session ID
			sessionId  = CommonConnection.getSessionID(Digital_CCLog.Digital_CC, false);
			
			if(sessionId==null || sessionId.equalsIgnoreCase("") || sessionId.equalsIgnoreCase("null"))
			{
				Digital_CCLog.Digital_CC.debug("Could Not Get Session ID "+sessionId);
				return;
			}

			//Fetch all Work-Items on given queueID.
			Digital_CCLog.Digital_CC.debug("Fetching all Workitems on System_Error queue");
			System.out.println("Fetching all Workitems on System_Error queue");
			
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionId, queueID);
			Digital_CCLog.Digital_CC.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= CommonMethods.WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);
			Digital_CCLog.Digital_CC.debug("WMFetchWorkList OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			Digital_CCLog.Digital_CC.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			Digital_CCLog.Digital_CC.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);
			Digital_CCLog.Digital_CC.debug("Number of workitems retrieved on System_Error: "+fetchWorkitemListCount);
			System.out.println("Number of workitems retrieved on System_Error: "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount >0)
			{
				for(int i=0; i<fetchWorkitemListCount; i++)
				{
					String fetchWorkItemlistData = xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData = fetchWorkItemlistData.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

					Digital_CCLog.Digital_CC.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: " + fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID = xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					Digital_CCLog.Digital_CC.debug("Current ProcessInstanceID: " + processInstanceID);
					Digital_CCLog.Digital_CC.debug("Processing Workitem: " + processInstanceID);

			        String DBQuery = "SELECT Is_CAM_generated,CIF,Is_STP FROM NG_DCC_EXTTABLE with(nolock) WHERE WI_NAME='" + processInstanceID + "'";
			        
			        String extTabDataINPXML = CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(Digital_CCLog.Digital_CC, false));
			        Digital_CCLog.Digital_CC.debug("extTabDataIPXML: " + extTabDataINPXML);
			        String extTabDataOUPXML = CommonMethods.WFNGExecute(extTabDataINPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
			        Digital_CCLog.Digital_CC.debug("extTabDataOPXML: " + extTabDataOUPXML);	
					
			        XMLParser xmlParserDataDB = new XMLParser(extTabDataOUPXML);
			        
			        String Is_CAM_Generated = xmlParserDataDB.getValueOf("Is_CAM_generated");
			        String Cif_Id = xmlParserDataDB.getValueOf("CIF");
			        String Is_STP = xmlParserDataDB.getValueOf("Is_STP");
			        String pdfName = "";
					if (Is_CAM_Generated.equalsIgnoreCase("N")) {
						if (Is_STP.equalsIgnoreCase("Y")) {
							pdfName = "STP_CAM_Report";
						} else {
							pdfName = "NON_STP_CAM_Report";
						}
						Digital_CC_CAMTemplate obj = new Digital_CC_CAMTemplate(Digital_CCLog.Digital_CC);
						String attrbList = obj.generate_CAM_ReportT(pdfName, Cif_Id, processInstanceID, sessionId);

					} else {
						Digital_CCLog.Digital_CC.info("Cam Report Is Already Generated");
					}
				}
			}
		}
		catch (Exception e)
		{
			Digital_CCLog.Digital_CC.debug("Exception: "+e.getMessage());
		}
	}
	
	// read the config files 
	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "DCC_CAM_Generate_Logger.properties")));

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
	
}
