/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM
Application				: RAK BPM Utility
Module					: RAOP Status
File Name				: WI_update_doneWI.java
Author 					: Ravindra Kumar	
Date (DD/MM/YYYY)		: 01/06/2022

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/


package com.newgen.DAO.WI_Update;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;


public class WI_update_done2 implements Runnable
{

	private static NGEjbClient ngEjbClientCIFVer;

	static Map<String, String> WI_UpdareConfigParamMap= new HashMap<String, String>();

	int socketConnectionTimeout=0;
	int integrationWaitTime=0;
	int sleepIntervalInMin=0;
	public static int waitLoop=50;
	public static int loopCount=50;
	public static String fromMailID="";
	public static String toMailID = "";
	public static String mailSubject = "";
	public static String MailStr="";
	public static String jtsIP = "";
	public static String jtsPort = "";
	public String sessionID = "";
	public String Auto_Rej_No_response;
	public String Auto_Rej_reason;
	public String onePagerTemplatesPath_source;
	public String onePagerTemplatesPath_destination;

	
	@Override
	public void run()
	{
		String cabinetName = "";
		String queueID = "";

		try
		{
			DAO_WI_Update.setLogger();
			ngEjbClientCIFVer = NGEjbClient.getSharedInstance();

			DAO_WI_Update.DAOWIUPDATELogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			DAO_WI_Update.DAOWIUPDATELogger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				DAO_WI_Update.DAOWIUPDATELogger.error("Could not Read Config Properties [DAO_WI_Update]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			DAO_WI_Update.DAOWIUPDATELogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			DAO_WI_Update.DAOWIUPDATELogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			DAO_WI_Update.DAOWIUPDATELogger.debug("JTSPORT: " + jtsPort);

			queueID = WI_UpdareConfigParamMap.get("queueID");
			DAO_WI_Update.DAOWIUPDATELogger.debug("QueueID: " + queueID);

			socketConnectionTimeout=Integer.parseInt(WI_UpdareConfigParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			DAO_WI_Update.DAOWIUPDATELogger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(WI_UpdareConfigParamMap.get("INTEGRATION_WAIT_TIME"));
			DAO_WI_Update.DAOWIUPDATELogger.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(WI_UpdareConfigParamMap.get("SleepIntervalInMin"));
			DAO_WI_Update.DAOWIUPDATELogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);
			
			
			fromMailID=WI_UpdareConfigParamMap.get("fromMailID");
			DAO_WI_Update.DAOWIUPDATELogger.debug("fromMailID: "+fromMailID);
			
			toMailID=WI_UpdareConfigParamMap.get("toMailID");
			DAO_WI_Update.DAOWIUPDATELogger.debug("toMailID: "+toMailID);
			
			mailSubject=WI_UpdareConfigParamMap.get("mailSubject");
			DAO_WI_Update.DAOWIUPDATELogger.debug("mailSubject: "+mailSubject);
			
			MailStr=WI_UpdareConfigParamMap.get("MailStr");
			DAO_WI_Update.DAOWIUPDATELogger.debug("MailStr: "+MailStr);
			
			Auto_Rej_No_response=WI_UpdareConfigParamMap.get("Auto_Rej_No_response");
			DAO_WI_Update.DAOWIUPDATELogger.debug("Auto_Rej_No_response: "+Auto_Rej_No_response);
			
			Auto_Rej_reason=WI_UpdareConfigParamMap.get("Auto_Rej_reason");
			DAO_WI_Update.DAOWIUPDATELogger.debug("Auto_Rej_reason: "+Auto_Rej_reason);

			onePagerTemplatesPath_source=WI_UpdareConfigParamMap.get("onePagerTemplatesPath_source");
			DAO_WI_Update.DAOWIUPDATELogger.debug("onePagerTemplatesPath_source: "+onePagerTemplatesPath_source);
			
			onePagerTemplatesPath_destination=WI_UpdareConfigParamMap.get("onePagerTemplatesPath_destination");
			DAO_WI_Update.DAOWIUPDATELogger.debug("onePagerTemplatesPath_destination: "+onePagerTemplatesPath_destination);

			sessionID = CommonConnection.getSessionID(DAO_WI_Update.DAOWIUPDATELogger, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				DAO_WI_Update.DAOWIUPDATELogger.debug("Could Not Connect to Server!");
			}
			else
			{
				DAO_WI_Update.DAOWIUPDATELogger.debug("Session ID found: " + sessionID);
				HashMap<String, String> socketDetailsMap = socketConnectionDetails(cabinetName, jtsIP, jtsPort, sessionID);
				while (true) {
					DAO_WI_Update.setLogger();
					DAO_WI_Update.DAOWIUPDATELogger.debug("iRBL CIF Verification...123.");
					//Deepak changes to extract more than 100 cases
					//startDAO_WI_Update(cabinetName, jtsIP, jtsPort, sessionID, queueID, socketConnectionTimeout, integrationWaitTime, socketDetailsMap);
					startDAO_WI_Update(cabinetName, jtsIP, jtsPort, sessionID, queueID, socketConnectionTimeout, integrationWaitTime, socketDetailsMap,"","","");
					System.out.println("No More workitems to Process, Sleeping!");
					Thread.sleep(sleepIntervalInMin * 60 * 1000);
				}
			}
		}

		catch(Exception e)
		{
			e.printStackTrace();
			DAO_WI_Update.DAOWIUPDATELogger.error("Exception Occurred in DAO_WI_Update : "+e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			DAO_WI_Update.DAOWIUPDATELogger.error("Exception Occurred in DAO_WI_Update : "+result);
		}
	}

	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "DAO_WIUpdate_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			  {
			    String name = (String) names.nextElement();
			    WI_UpdareConfigParamMap.put(name, p.getProperty(name));
			  }
		    }
		catch (Exception e)
		{
			return -1 ;
		}
		return 0;
	}

	//Deepak changes to extract more than 100 cases
	/*private void startDAO_WI_Update(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String queueID, 
			int socketConnectionTimeOut, int integrationWaitTime, HashMap<String, String> socketDetailsMap)
	{*/
	private void startDAO_WI_Update(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String queueID, 
			int socketConnectionTimeOut, int integrationWaitTime, HashMap<String, String> socketDetailsMap,String processInstanceId, String lastWorkItemId,String entryDateTime_str)
	{
		final String ws_name="WI_status_update";
		String entryDateTime="";
		
		try{
			
			final HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();
			//Validate Session ID
			sessionID  = CommonConnection.getSessionID(DAO_WI_Update.DAOWIUPDATELogger, false);

			if (sessionID == null || sessionID.equalsIgnoreCase("") || sessionID.equalsIgnoreCase("null"))
			{
				DAO_WI_Update.DAOWIUPDATELogger.error("Could Not Get Session ID "+sessionID);
				return;
			}

			//Fetch all Work-Items on given queueID.
			DAO_WI_Update.DAOWIUPDATELogger.debug("Fetching all Workitems on DAO_WI_Update queue");
			System.out.println("Fetching all Workitems on DAO_WI_Update");
			//String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionID, queueID);
			String fetchWorkitemListInputXML=CommonMethods.getFetchWorkItemsInputXML(processInstanceId, lastWorkItemId,  sessionId, cabinetName, queueID,entryDateTime_str);
			
			
			DAO_WI_Update.DAOWIUPDATELogger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);
			
			DAO_WI_Update.DAOWIUPDATELogger.debug(" DAO_WI_Update WMFetchWorkList OutputXML: "+fetchWorkitemListOutputXML);
			
			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);
			
			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			DAO_WI_Update.DAOWIUPDATELogger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);
			
			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			DAO_WI_Update.DAOWIUPDATELogger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);
			
			DAO_WI_Update.DAOWIUPDATELogger.debug("Number of workitems retrieved on DAO_WI_Update: "+fetchWorkitemListCount);
			
			System.out.println("Number of workitems retrieved on DAO_WI_Update: "+fetchWorkitemListCount);
			
			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0)
				{
					for(int i=0; i<fetchWorkitemListCount; i++)
					{
						String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
						fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
	
						DAO_WI_Update.DAOWIUPDATELogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
						XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);
	
						String processInstanceID=xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
						DAO_WI_Update.DAOWIUPDATELogger.debug("Current ProcessInstanceID: "+processInstanceID);
	
						DAO_WI_Update.DAOWIUPDATELogger.debug("Processing Workitem: "+processInstanceID);
						System.out.println("\nProcessing Workitem: "+processInstanceID);
	
						String WorkItemID=xmlParserfetchWorkItemData.getValueOf("WorkItemId");
						DAO_WI_Update.DAOWIUPDATELogger.debug("Current WorkItemID: "+WorkItemID);
	
						entryDateTime = xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
						DAO_WI_Update.DAOWIUPDATELogger.debug("Current EntryDateTime: "+entryDateTime);
						
						
	
						String ActivityName=xmlParserfetchWorkItemData.getValueOf("ActivityName");
						DAO_WI_Update.DAOWIUPDATELogger.debug("ActivityName: "+ActivityName);
						
						String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
						DAO_WI_Update.DAOWIUPDATELogger.debug("ActivityID: "+ActivityID);
						String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
						DAO_WI_Update.DAOWIUPDATELogger.debug("ActivityType: "+ActivityType);
						String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
						DAO_WI_Update.DAOWIUPDATELogger.debug("ProcessDefId: "+ProcessDefId);
						//Deepak changes to extract more than 100 cases
						if(i==99){
							processInstanceId = processInstanceID;
							lastWorkItemId = WorkItemID;
						}
						
						String file_rec="N";
						
					    String DBQuery ="select wi_name,ECRN,ChequeBk_ref from ng_dao_wi_update with (nolock) where wi_name='"+ processInstanceID +"' and  status='R'";
					    
					    String extTabDataIPXML =CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAO_WI_Update.DAOWIUPDATELogger, false));
					    DAO_WI_Update.DAOWIUPDATELogger.debug("extTabDataIPXML: " + extTabDataIPXML);
					    String extTabDataOPXML = WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
					    DAO_WI_Update.DAOWIUPDATELogger.debug("extTabDataOPXML: " + extTabDataOPXML);
					    // using xml parser to pass the output data in desired format 
					    XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
					    // total values retrieved > 0 is a check
					    int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
					    // Main code we get if the ap select call is triggered success.
					    if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
				        {
					    	file_rec="Y";
					    	String xmlDataExtTab = xmlParserData.getNextValueOf("Record");
				            xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
				            // replace the spcl char above.
				            NGXmlList objWorkList = xmlParserData.createList("Records", "Record");
				            
				            // loop over the map to put value key pair.
				            for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
				            {
				            	CheckGridDataMap.put("Workitem_Number", objWorkList.getVal("wi_name"));
				            	CheckGridDataMap.put("ECRN", objWorkList.getVal("ECRN"));
				            	CheckGridDataMap.put("ChequeBk_ref", objWorkList.getVal("ChequeBk_ref"));
				            }
				        }				    	    
					    String DBQuery_1 ="select is_Ntb,account_no,is_prime_req,is_cbs_req,event,deh_Event,deh_Workitem_status,entry_date_time,DATEDIFF(DAY,convert(datetime,entry_date_time),GETDATE()) as 'DAY_DIFF',WI_name,Given_Name,Middle_Name,Surname,Nationality,Emirates_id,Product_Category,country_master.CD_DESC,prospect_id from NG_DAO_EXTTABLE with(nolock) inner join NG_MASTER_DAO_COUNTRY  as country_master with(nolock) on NG_DAO_EXTTABLE.Nationality=country_master.CM_CODE  where wi_name='"+ processInstanceID +"'";
					    
					    String extTabDataIPXML_1 =CommonMethods.apSelectWithColumnNames(DBQuery_1, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAO_WI_Update.DAOWIUPDATELogger, false));
					    DAO_WI_Update.DAOWIUPDATELogger.debug("extTabDataIPXML: " + extTabDataIPXML_1);
					    String extTabDataOPXML_1 = WFNGExecute(extTabDataIPXML_1, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
					    DAO_WI_Update.DAOWIUPDATELogger.debug("extTabDataOPXML: " + extTabDataOPXML_1);
					    
					    XMLParser xmlParserData_1 = new XMLParser(extTabDataOPXML_1);
					    int iTotalrec_1 = Integer.parseInt(xmlParserData_1.getValueOf("TotalRetrieved"));
					    
					    if (xmlParserData_1.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_1 > 0)
				        {
					    	String xmlDataExtTab = xmlParserData_1.getNextValueOf("Record");
				            xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
				            NGXmlList objWorkList = xmlParserData_1.createList("Records", "Record");
				            
				            for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
				            {
				            	CheckGridDataMap.put("is_prime_req", objWorkList.getVal("is_prime_req"));
				            	CheckGridDataMap.put("is_cbs_req", objWorkList.getVal("is_cbs_req"));
				            	CheckGridDataMap.put("event", objWorkList.getVal("event"));
				            	CheckGridDataMap.put("account_no", objWorkList.getVal("account_no"));
				            	CheckGridDataMap.put("is_Ntb", objWorkList.getVal("is_Ntb"));
				            	CheckGridDataMap.put("deh_Event", objWorkList.getVal("deh_Event"));
				            	CheckGridDataMap.put("deh_Workitem_status", objWorkList.getVal("deh_Workitem_status"));
				            	CheckGridDataMap.put("entry_date_time", objWorkList.getVal("entry_date_time"));
				            	CheckGridDataMap.put("DAY_DIFF", objWorkList.getVal("DAY_DIFF"));
				            	CheckGridDataMap.put("WI_name", objWorkList.getVal("WI_name"));
				            	CheckGridDataMap.put("Given_Name", objWorkList.getVal("Given_Name"));
				            	CheckGridDataMap.put("Middle_Name", objWorkList.getVal("Middle_Name"));
				            	CheckGridDataMap.put("Surname", objWorkList.getVal("Surname"));
				            	CheckGridDataMap.put("Nationality", objWorkList.getVal("Nationality"));
				            	CheckGridDataMap.put("Emirates_id", objWorkList.getVal("Emirates_id"));
				            	CheckGridDataMap.put("Product_Category", objWorkList.getVal("Product_Category"));
				            	CheckGridDataMap.put("CD_DESC", objWorkList.getVal("CD_DESC"));//prospect_id
				            	CheckGridDataMap.put("prospect_id", objWorkList.getVal("prospect_id"));
				            	
				            }
				        }
					    
					    String DBQuery_2 ="select EntryDATETIME from WFINSTRUMENTTABLE  with(nolock) where ProcessInstanceID ='"+ processInstanceID +"' and ActivityName = 'WI_status_update'";
					    
					    String extTabDataIPXML_2 =CommonMethods.apSelectWithColumnNames(DBQuery_2, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAO_WI_Update.DAOWIUPDATELogger, false));
					    DAO_WI_Update.DAOWIUPDATELogger.debug("extTabDataIPXML: " + extTabDataIPXML_2);
					    String extTabDataOPXML_2 = WFNGExecute(extTabDataIPXML_2, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
					    DAO_WI_Update.DAOWIUPDATELogger.debug("extTabDataOPXML: " + extTabDataOPXML_2);
					    
					    XMLParser xmlParserData_2 = new XMLParser(extTabDataOPXML_2);
					    int iTotalrec_2 = Integer.parseInt(xmlParserData_2.getValueOf("TotalRetrieved"));
					    
					    if (xmlParserData_2.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_2 > 0)
				        {
					    	String xmlDataExtTab = xmlParserData_2.getNextValueOf("Record");
				            xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
				            NGXmlList objWorkList = xmlParserData_2.createList("Records", "Record");
				    
				            for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
				            {
				            	CheckGridDataMap.put("EntryDATETIME", objWorkList.getVal("EntryDATETIME"));
				            }
				        }
					    
					    String prime = CheckGridDataMap.get("is_prime_req");
				    	String cbs = CheckGridDataMap.get("is_cbs_req");
				    	String cbs_no = CheckGridDataMap.get("ChequeBk_ref");
				    	String decisionValue=CheckGridDataMap.get("event");
				    	String EntryDATETIME = CheckGridDataMap.get("EntryDATETIME");
				    	
				    	String prime_no = "";
				    	if(CheckGridDataMap.containsKey("ECRN")){
				    		DAO_WI_Update.DAOWIUPDATELogger.debug("prime_no");
				    		prime_no = CheckGridDataMap.get("ECRN");	
				    	}
				    	String ntb="";
				    	if(CheckGridDataMap.containsKey("is_Ntb")){
				    		DAO_WI_Update.DAOWIUPDATELogger.debug("is_Ntb");
				    		ntb = CheckGridDataMap.get("is_Ntb");	
				    	}
				    	String account_no = null;
				    	if(CheckGridDataMap.containsKey("account_no")){
				    		DAO_WI_Update.DAOWIUPDATELogger.debug("In");
				    		account_no = CheckGridDataMap.get("account_no");	
				    	}
				    	
				    	String deh_Event = CheckGridDataMap.get("deh_Event");
				    	String deh_Workitem_status = CheckGridDataMap.get("deh_Workitem_status");
				    	String entry_date_time = CheckGridDataMap.get("entry_date_time");
				    	String DAY_DIFF = CheckGridDataMap.get("DAY_DIFF");
				    	int no_of_day = Integer.parseInt(Auto_Rej_No_response);
				    	int no_of_day_forWi = Integer.parseInt(DAY_DIFF);
				    	
				    	DAO_WI_Update.DAOWIUPDATELogger.debug("file_rec "+file_rec);
				    	DAO_WI_Update.DAOWIUPDATELogger.debug("prime "+prime);
				    	DAO_WI_Update.DAOWIUPDATELogger.debug("cbs "+cbs);
				    	DAO_WI_Update.DAOWIUPDATELogger.debug("prime_no "+prime_no);
				    	DAO_WI_Update.DAOWIUPDATELogger.debug("cbs_no "+cbs_no);
				    	DAO_WI_Update.DAOWIUPDATELogger.debug("decisionValue "+decisionValue);
				    	DAO_WI_Update.DAOWIUPDATELogger.debug("account_no "+account_no);
				    	
				    	DAO_WI_Update.DAOWIUPDATELogger.debug("deh_Event "+deh_Event);
				    	DAO_WI_Update.DAOWIUPDATELogger.debug("deh_Workitem_status "+deh_Workitem_status);
				    	DAO_WI_Update.DAOWIUPDATELogger.debug("entry_date_time "+entry_date_time);
				    	DAO_WI_Update.DAOWIUPDATELogger.debug("DAY_DIFF "+DAY_DIFF);
				    					    	
//				    	if("N".equalsIgnoreCase(ntb)){ vinayak singhal
//				    		prime="N"; // to handle ETB Cases.
//				    		DAO_WI_Update.DAOWIUPDATELogger.debug("Changing the value to N for ETB cases for Wi : "+processInstanceID);
//				    	}
				    	
				    	// To route cases to deh notify ws and reject them - Add the routing condition to check where event = No_Response
				    	if("REQUEST_CUSTOMER_QUERY".equalsIgnoreCase(deh_Event) && "INP".equalsIgnoreCase(deh_Workitem_status) && no_of_day_forWi>no_of_day ){
				    		
				    		updateExternalTable("NG_DAO_EXTTABLE","deh_Event","'STATUS_CHANGE'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
				    		updateExternalTable("NG_DAO_EXTTABLE","deh_Workitem_status","'REJ'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
				    		updateExternalTable("NG_DAO_EXTTABLE","Event","'No_Response'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
				    		updateExternalTable("NG_DAO_EXTTABLE","rejectReason","'"+Auto_Rej_reason+"'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
				    		updateExternalTable("NG_DAO_EXTTABLE","AddnalDocsReqd","'false'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
				    		
				    		doneworkitem(cabinetName,sJtsIp,iJtsPort,sessionID,queueID,
							socketConnectionTimeOut,integrationWaitTime,
							socketDetailsMap,processInstanceID,WorkItemID,ActivityID,
							ProcessDefId,ActivityType,decisionValue,ActivityName,EntryDATETIME,CheckGridDataMap);
				    	}
				    	
					    if(file_rec.equalsIgnoreCase("N"))
					    {
					    	DAO_WI_Update.DAOWIUPDATELogger.debug("Inside N case");
					    	
					    	if(prime.equalsIgnoreCase("N") && cbs.equalsIgnoreCase("N") && (account_no !=null && !"".equalsIgnoreCase(account_no)))
					    	{
					    		DAO_WI_Update.DAOWIUPDATELogger.debug("Inside both prime cbs N case");
					    		
					    		doneworkitem(cabinetName,sJtsIp,iJtsPort,sessionID,queueID,
						    	socketConnectionTimeOut,integrationWaitTime,
						    	socketDetailsMap,processInstanceID,WorkItemID,ActivityID,
						    	ProcessDefId,ActivityType,decisionValue,ActivityName,EntryDATETIME,CheckGridDataMap);
					    	}
					    }
					    else if(file_rec.equalsIgnoreCase("Y"))
					    {
					    	DAO_WI_Update.DAOWIUPDATELogger.debug("Inside Y case");
					    	
					    	if(prime.equalsIgnoreCase("Y") && cbs.equalsIgnoreCase("N")){
					    		if(prime_no!=null && !"".equalsIgnoreCase(prime_no)){
					    			DAO_WI_Update.DAOWIUPDATELogger.debug("Inside prime Y case");

					    			doneworkitem(cabinetName,sJtsIp,iJtsPort,sessionID,queueID,
					    			socketConnectionTimeOut,integrationWaitTime,
					    			socketDetailsMap,processInstanceID,WorkItemID,ActivityID,
					    			ProcessDefId,ActivityType,decisionValue,ActivityName,EntryDATETIME,CheckGridDataMap);
					    		}
					    	}
					    	else if(prime.equalsIgnoreCase("N") && cbs.equalsIgnoreCase("Y")){
					    		if(cbs_no!=null && !"".equalsIgnoreCase(cbs_no)){
					    			DAO_WI_Update.DAOWIUPDATELogger.debug("Inside cbs_no Y case");
					    			
					    			doneworkitem(cabinetName,sJtsIp,iJtsPort,sessionID,queueID,
							    	socketConnectionTimeOut,integrationWaitTime,
							    	socketDetailsMap,processInstanceID,WorkItemID,ActivityID,
							    	ProcessDefId,ActivityType,decisionValue,ActivityName,EntryDATETIME,CheckGridDataMap);
					    		}
					    	}
					    	else if(prime.equalsIgnoreCase("Y") && cbs.equalsIgnoreCase("Y")){
					    		if((prime_no!=null && !"".equalsIgnoreCase(prime_no)) && (cbs_no!=null && !"".equalsIgnoreCase(cbs_no))){
					    			DAO_WI_Update.DAOWIUPDATELogger.debug("Inside cbs & prime  Y case");
					    			
					    			doneworkitem(cabinetName,sJtsIp,iJtsPort,sessionID,queueID,
							    	socketConnectionTimeOut,integrationWaitTime,
							    	socketDetailsMap,processInstanceID,WorkItemID,ActivityID,
							    	ProcessDefId,ActivityType,decisionValue,ActivityName,EntryDATETIME,CheckGridDataMap);
					    		}
					    	}
					 }
				}
			}	
			//Deepak changes to extract more than 100 cases
			if(fetchWorkitemListCount>99){
				DAO_WI_Update.DAOWIUPDATELogger.debug("Inside if condition to fech next set of cases post 100 processInstanceId: "+processInstanceId+ " lastWorkItemId: " + lastWorkItemId );
				startDAO_WI_Update(cabinetName, sJtsIp, iJtsPort, sessionId, queueID, socketConnectionTimeOut, integrationWaitTime, socketDetailsMap,processInstanceId,lastWorkItemId,entryDateTime);
			}
		}
			catch (Exception e)
		{
			DAO_WI_Update.DAOWIUPDATELogger.debug("Exception DAO_WI_Update : "+e.getMessage());
		}
	}

	private HashMap<String, String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort, String sessionID) {
		HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

		try {
			DAO_WI_Update.DAOWIUPDATELogger.debug("Fetching Socket Connection Details.");
			System.out.println("Fetching Socket Connection Details.");

			String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'DigitalAO' and CallingSource = 'Utility'";

			String socketDetailsInputXML = CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
			DAO_WI_Update.DAOWIUPDATELogger.debug("Socket Details APSelect InputXML: " + socketDetailsInputXML);

			String socketDetailsOutputXML = WFNGExecute(socketDetailsInputXML, sJtsIp, iJtsPort, 1);
			DAO_WI_Update.DAOWIUPDATELogger.debug("Socket Details APSelect OutputXML: " + socketDetailsOutputXML);

			XMLParser xmlParserSocketDetails = new XMLParser(socketDetailsOutputXML);
			String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			DAO_WI_Update.DAOWIUPDATELogger.debug("SocketDetailsMainCode: " + socketDetailsMainCode);

			int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			DAO_WI_Update.DAOWIUPDATELogger.debug("SocketDetailsTotalRecords: " + socketDetailsTotalRecords);

			if (socketDetailsMainCode.equalsIgnoreCase("0") && socketDetailsTotalRecords > 0) {
				String xmlDataSocketDetails = xmlParserSocketDetails.getNextValueOf("Record");
				xmlDataSocketDetails = xmlDataSocketDetails.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

				XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

				String socketServerIP = xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
				DAO_WI_Update.DAOWIUPDATELogger.debug("SocketServerIP: " + socketServerIP);
				socketDetailsMap.put("SocketServerIP", socketServerIP);

				String socketServerPort = xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
				DAO_WI_Update.DAOWIUPDATELogger.debug("SocketServerPort " + socketServerPort);
				socketDetailsMap.put("SocketServerPort", socketServerPort);

				DAO_WI_Update.DAOWIUPDATELogger.debug("SocketServer Details found.");
				System.out.println("SocketServer Details found.");
			}
		} catch (Exception e) {
			DAO_WI_Update.DAOWIUPDATELogger
					.debug("Exception in getting Socket Connection Details: " + e.getMessage());
			System.out.println("Exception in getting Socket Connection Details: " + e.getMessage());
		}

		return socketDetailsMap;
	}

	protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort, int flag)
			throws IOException, Exception {
		DAO_WI_Update.DAOWIUPDATELogger.debug("In WF NG Execute : " + serverPort);
		try {
			if (serverPort.startsWith("33"))
				return WFCallBroker.execute(ipXML, jtsServerIP, Integer.parseInt(serverPort), 1);
			else
				return ngEjbClientCIFVer.makeCall(jtsServerIP, serverPort, "WebSphere", ipXML);
		} catch (Exception e) {
			DAO_WI_Update.DAOWIUPDATELogger.debug("Exception Occured in WF NG Execute : " + e.getMessage());
			e.printStackTrace();
			return "Error";
		}
	}
	
	String socketConnection(String cabinetName, String username, String sessionId, String sJtsIp, String iJtsPort, String processInstanceID, String ws_name,
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

		try{
			
			DAO_WI_Update.DAOWIUPDATELogger.debug("userName "+ username);
			DAO_WI_Update.DAOWIUPDATELogger.debug("SessionId "+ sessionID);

			socketServerIP=socketDetailsMap.get("SocketServerIP");
			DAO_WI_Update.DAOWIUPDATELogger.debug("SocketServerIP "+ socketServerIP);
			socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			DAO_WI_Update.DAOWIUPDATELogger.debug("SocketServerPort "+ socketServerPort);

	   		if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0)){
	   			
    			socket = new Socket(socketServerIP, socketServerPort);
    			socket.setSoTimeout(connection_timeout*1000);
    			out = socket.getOutputStream();
    			socketInputStream = socket.getInputStream();
    			dout = new DataOutputStream(out);
    			din = new DataInputStream(socketInputStream);
    			DAO_WI_Update.DAOWIUPDATELogger.debug("Dout " + dout);
    			DAO_WI_Update.DAOWIUPDATELogger.debug("Din " + din);

    			outputResponse = "";

    			inputRequest = getRequestXML( cabinetName,sessionID ,processInstanceID, ws_name, username, sInputXML);

    			if (inputRequest != null && inputRequest.length() > 0){
    				
    				int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
    				DAO_WI_Update.DAOWIUPDATELogger.debug("RequestLen: "+inputRequestLen + "");
    				inputRequest = inputRequestLen + "##8##;" + inputRequest;
    				DAO_WI_Update.DAOWIUPDATELogger.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
    				dout.write(inputRequest.getBytes("UTF-16LE"));dout.flush();
    			}
    			byte[] readBuffer = new byte[500];
    			int num = din.read(readBuffer);
    			if (num > 0){

    				byte[] arrayBytes = new byte[num];
    				System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
    				outputResponse = outputResponse+ new String(arrayBytes, "UTF-16LE");
					inputMessageID = outputResponse;
    				DAO_WI_Update.DAOWIUPDATELogger.debug("OutputResponse: "+outputResponse);

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
				return outputResponse;
    	 		}
    		else
    		{
    			DAO_WI_Update.DAOWIUPDATELogger.debug("SocketServerIp and SocketServerPort is not maintained "+"");
    			DAO_WI_Update.DAOWIUPDATELogger.debug("SocketServerIp is not maintained "+	socketServerIP);
    			DAO_WI_Update.DAOWIUPDATELogger.debug(" SocketServerPort is not maintained "+	socketServerPort);
    			return "Socket Details not maintained";
    		}
		}

		catch (Exception e)
		{
			DAO_WI_Update.DAOWIUPDATELogger.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
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
			catch(Exception e){
				
				DAO_WI_Update.DAOWIUPDATELogger.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
			}
		}

	}
	private String getResponseXML(String cabinetName,String sJtsIp,String iJtsPort, String sessionId, String processInstanceID,String message_ID, int integrationWaitTime)
	{
		String outputResponseXML="";
		try
		{
			String QueryString = "select OUTPUT_XML from NG_DAO_XMLLOG_HISTORY with (nolock) where MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+processInstanceID+"'";

			String responseInputXML =CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, sessionID);
			DAO_WI_Update.DAOWIUPDATELogger.debug("Response APSelect InputXML: "+responseInputXML);

			int Loop_count=0;
			do
			{
				String responseOutputXML=CommonMethods.WFNGExecute(responseInputXML,sJtsIp,iJtsPort,1);
				DAO_WI_Update.DAOWIUPDATELogger.debug("Response APSelect OutputXML: "+responseOutputXML);

			    XMLParser xmlParserSocketDetails= new XMLParser(responseOutputXML);
			    String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			    DAO_WI_Update.DAOWIUPDATELogger.debug("ResponseMainCode: "+responseMainCode);



			    int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			    DAO_WI_Update.DAOWIUPDATELogger.debug("ResponseTotalRecords: "+responseTotalRecords);

			    if (responseMainCode.equals("0") && responseTotalRecords > 0)
				{

					String responseXMLData=xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData =responseXMLData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

	        		XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
	        		//DAONotifyAPPLog.DAONotifyAPPLogger.debug("ResponseXMLData: "+responseXMLData);

	        		outputResponseXML=xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
	        		//DAONotifyAPPLog.DAONotifyAPPLogger.debug("OutputResponseXML: "+outputResponseXML);

	        		if("".equalsIgnoreCase(outputResponseXML)){
	        			outputResponseXML="Error";
	    			}
	        		break;
				}
			    Loop_count++;
			    Thread.sleep(1000);
			}
			while(Loop_count<integrationWaitTime);
			DAO_WI_Update.DAOWIUPDATELogger.debug("integrationWaitTime: "+integrationWaitTime);

		}
		catch(Exception e)
		{
			DAO_WI_Update.DAOWIUPDATELogger.debug("Exception occurred in outputResponseXML" + e.getMessage());
			outputResponseXML="Error";
		}

		return outputResponseXML;

	}
	 public void doneworkitem(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String queueID, 
				int socketConnectionTimeOut, int integrationWaitTime, HashMap<String, String> socketDetailsMap,String processInstanceID,String WorkItemID,String ActivityID,
				String ProcessDefId,String ActivityType,String decisionValue, String ActivityName,String EntryDATETIME,HashMap<String, String> CheckGridDataMap)
	  {
		 try{
			 
			 String event=CheckGridDataMap.get("event");
			   if(event.equalsIgnoreCase("Account Created"))
			   {
			    String WI_name=CheckGridDataMap.get("WI_name");
			    String Given_Name=CheckGridDataMap.get("Given_Name");
			    String Middle_Name=CheckGridDataMap.get("Middle_Name");
			    String Surname=CheckGridDataMap.get("Surname");
			    String Nationality=CheckGridDataMap.get("Nationality");
			    String Emirates_id=CheckGridDataMap.get("Emirates_id");
			    String country_name=CheckGridDataMap.get("CD_DESC");//prospect_id
			    String prospect_id=CheckGridDataMap.get("prospect_id");
			    String CustomerName="";
			    String file_name="";
			    String Product_Category=CheckGridDataMap.get("Product_Category");
			    if("".equalsIgnoreCase(Middle_Name) || Middle_Name == null){
					CustomerName = Given_Name + " " + Surname;
				}
				else{				 
					CustomerName = Given_Name + " " +Middle_Name+" "+ Surname;
				}
			    DAO_WI_Update.DAOWIUPDATELogger.debug("Product_Category :" + Product_Category);
			    if(Product_Category.equalsIgnoreCase("I"))
			    {
			    	file_name="islamic.pdf";
			    }
			    else if(Product_Category.equalsIgnoreCase("C"))
			    {
			    	file_name="non_islamic.pdf";
			    }
				
			    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");					
				Date current_date = new Date();
				String formattedEntryDatetime=dateFormat.format(current_date);

			    String sourceName=onePagerTemplatesPath_source+File.separator+file_name;
			    DAO_WI_Update.DAOWIUPDATELogger.debug("sourceName :" + sourceName);
			    
				String destPath=onePagerTemplatesPath_destination;
				//DAO_WI_Update.DAOWIUPDATELogger.debug("destPath :" + destPath);
				File newfolder=new File(destPath);
				if(!newfolder.exists()){
					newfolder.mkdirs();
				}
				String onePagerName=prospect_id+"_"+processInstanceID;
				 destPath=onePagerTemplatesPath_destination+File.separator+File.separator+ onePagerName + ".pdf";
				Map<String,String> XMLMap = new HashMap<String,String>();
				XMLMap.put("winame", WI_name);
				XMLMap.put("winame_islamic",WI_name );
				XMLMap.put("name",CustomerName );
				XMLMap.put("name_islamic",CustomerName);
				XMLMap.put("nationality",country_name );
				XMLMap.put("nationality_islamic",country_name);
				XMLMap.put("emirates",Emirates_id);
				XMLMap.put("emirates_islamic",Emirates_id );
				XMLMap.put("date", formattedEntryDatetime);
				XMLMap.put("date_islamic",formattedEntryDatetime);
				
				
				 if("Success".equalsIgnoreCase(generatePDF(sourceName, destPath, XMLMap))) {
					 	 
					DateFormat dateFormat_1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");					
					Date current_date_1 = new Date();
					String formattedEntryDatetime_1=dateFormat_1.format(current_date_1);
					
					DAO_WI_Update.DAOWIUPDATELogger.debug("createPDF : Single pager Generated Succesfully at location :" + destPath);
					updateExternalTable("NG_DAO_EXTTABLE","onepager_generated_byibps","'Y'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
					updateExternalTable("NG_DAO_EXTTABLE","onepager_generated_time","'"+formattedEntryDatetime_1+"'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);

				 }
				 else {
					 updateExternalTable("NG_DAO_EXTTABLE","onepager_generated_byibps","'N'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
					 updateExternalTable("NG_DAO_EXTTABLE","Decision","'Error In Generating One Pager'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
					 decisionValue="Error In Generating One Pager";
					 DAO_WI_Update.DAOWIUPDATELogger.debug("createPDF : Error in Single pager Generation :" + destPath);
				 }					 
				 
			}////vinayak changes to download one pager ends

			 
			//Lock Workitem.
			String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID, WorkItemID);
			String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML,sJtsIp,iJtsPort,1);
			DAO_WI_Update.DAOWIUPDATELogger.debug("Output XML For WmgetWorkItemCall: " + getWorkItemOutputXml);

			XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
			String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
			DAO_WI_Update.DAOWIUPDATELogger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);

			if (getWorkItemMainCode.trim().equals("0"))
			{
				DAO_WI_Update.DAOWIUPDATELogger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);

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
						+ "<Attributes></Attributes>"
						+ "</WMAssignWorkItemAttributes_Input>";
				
				DAO_WI_Update.DAOWIUPDATELogger.debug("InputXML for assignWorkitemAttribute Call Notify: "+assignWorkitemAttributeInputXML);

				String assignWorkitemAttributeOutputXML=WFNGExecute(assignWorkitemAttributeInputXML,sJtsIp,
						iJtsPort,1);
				
				DAO_WI_Update.DAOWIUPDATELogger.debug("OutputXML for assignWorkitemAttribute Call Notify: "+assignWorkitemAttributeOutputXML);
				
				XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
				String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
				DAO_WI_Update.DAOWIUPDATELogger.debug("AssignWorkitemAttribute MainCode: "+assignWorkitemAttributeMainCode);

				if(assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0"))
				{
					DAO_WI_Update.DAOWIUPDATELogger.debug("AssignWorkitemAttribute Successful: "+assignWorkitemAttributeMainCode);
					System.out.println(processInstanceID + "Complete Succesfully with status "+decisionValue);
					DAO_WI_Update.DAOWIUPDATELogger.debug("WorkItem moved to next Workstep.");
				}
				else
				{
					String ErrDesc = "Done WI Failed";
					DAO_WI_Update.DAOWIUPDATELogger.debug("decisionValue : "+decisionValue);
					String return_code = assignWorkitemAttributeMainCode.trim();
					//start
					sendMail(cabinetName,sessionID,processInstanceID,jtsIP,jtsPort,ErrDesc,return_code,ProcessDefId);
				}
				
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				Date current_date = new Date();
				String formattedEntryDatetime=dateFormat.format(current_date);
				DAO_WI_Update.DAOWIUPDATELogger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);
				
				// parse to change and store and reflect as txt box on front end.
				Date d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(EntryDATETIME);
				String entrydatetime_format = dateFormat.format(d1);
				DAO_WI_Update.DAOWIUPDATELogger.debug("EntryDATETIME: "+entrydatetime_format);

				String columnNames="wi_name,decision_date_time,workstep,user_name,Decision,Remarks,entry_date_time";
				String columnValues="'"+processInstanceID+"','"+formattedEntryDatetime+"','"+ActivityName+"','"
				+CommonConnection.getUsername()+"','"+decisionValue+"','','"+entrydatetime_format+"'";
		
				String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionID, columnNames, columnValues,"NG_DAO_GR_DECISION_HISTORY");
				DAO_WI_Update.DAOWIUPDATELogger.debug("APInsertInputXML: "+apInsertInputXML);

				String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
				DAO_WI_Update.DAOWIUPDATELogger.debug("APInsertOutputXML: "+ apInsertInputXML);

				XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
				String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
				DAO_WI_Update.DAOWIUPDATELogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

				DAO_WI_Update.DAOWIUPDATELogger.debug("Completed On "+ ActivityName);


				if(apInsertMaincode.equalsIgnoreCase("0"))
				{
					DAO_WI_Update.DAOWIUPDATELogger.debug("ApInsert successful: "+apInsertMaincode);
					DAO_WI_Update.DAOWIUPDATELogger.debug("Inserted in WiHistory table successfully.");
				}
				else
				{
					DAO_WI_Update.DAOWIUPDATELogger.debug("ApInsert failed: "+apInsertMaincode);
				}
				
			}
			else
			{
				getWorkItemMainCode="";
				DAO_WI_Update.DAOWIUPDATELogger.debug("WmgetWorkItem failed: "+getWorkItemMainCode);
				String ErrDesc = "WI Failed";
				sendMail(cabinetName,sessionID,processInstanceID,jtsIP,jtsPort,ErrDesc,getWorkItemMainCode,ProcessDefId);
			}
		 }
		 catch(Exception e)
		 {
			 DAO_WI_Update.DAOWIUPDATELogger.debug("WmgetWorkItem Exception: "+e.getMessage());
		 }
		   }
	
	private String getRequestXML(String cabinetName, String sessionId,
			String processInstanceID, String ws_name, String userName, StringBuilder sInputXML)
	{
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("<APMQPUTGET_Input>");
		strBuff.append("<SessionId>" + sessionID + "</SessionId>");
		strBuff.append("<EngineName>" + cabinetName + "</EngineName>");
		strBuff.append("<XMLHISTORY_TABLENAME>NG_DAO_XMLLOG_HISTORY</XMLHISTORY_TABLENAME>");
		strBuff.append("<WI_NAME>" + processInstanceID + "</WI_NAME>");
		strBuff.append("<WS_NAME>" + ws_name + "</WS_NAME>");
		strBuff.append("<USER_NAME>" + userName + "</USER_NAME>");
		strBuff.append("<MQ_REQUEST_XML>");
		strBuff.append(sInputXML);
		strBuff.append("</MQ_REQUEST_XML>");
		strBuff.append("</APMQPUTGET_Input>");
		DAO_WI_Update.DAOWIUPDATELogger.debug("GetRequestXML: "+ strBuff.toString());
		return strBuff.toString();
	}
	public void sendMail(String cabinetName, String sessionId ,String wiName,String jtsIp,String jtsPort,String ErrDesc, String return_code,String ProcessDefId)throws Exception
    {
        XMLParser objXMLParser = new XMLParser();
        String sInputXML="";
        String sOutputXML="";
        String mainCodeforAPInsert=null;
        int sessionCheckInt = 0;
		while(sessionCheckInt<loopCount)
        {
            try
            {
            	DAO_WI_Update.DAOWIUPDATELogger.debug("workitem name to send mail---"+wiName);
            	DAO_WI_Update.DAOWIUPDATELogger.debug("ErrorMsg to send mail---"+ErrDesc);
            	DAO_WI_Update.DAOWIUPDATELogger.debug("return_code to send mail---"+return_code);
            	
            	String FinalMailStr = MailStr.toString().replace("<WI_NAME>",wiName).replace("<ret_Code>",return_code)
            	.replace("<errormsg>",ErrDesc);
            	DAO_WI_Update.DAOWIUPDATELogger.debug("finalbody: "+FinalMailStr);

            	String columnName="MAILFROM,MAILTO,MAILSUBJECT,MAILMESSAGE,MAILCONTENTTYPE,MAILPRIORITY,MAILSTATUS,INSERTEDBY,MAILACTIONTYPE,INSERTEDTIME,PROCESSDEFID,PROCESSINSTANCEID,WORKITEMID,ACTIVITYID,NOOFTRIALS";
            	String strValues="'"+fromMailID+"','"+toMailID+"','"+mailSubject+"','"+FinalMailStr+"','text/html;charset=UTF-8','1','N','CUSTOM','TRIGGER','"+CommonMethods.getdateCurrentDateInSQLFormat()+"','"+ProcessDefId+"','"+wiName+"','1','1','0'";
                
				sInputXML = "<?xml version=\"1.0\"?>" +
                        "<APInsert_Input>" +
                        "<Option>APInsert</Option>" +
                        "<TableName>WFMAILQUEUETABLE</TableName>" +
                        "<ColName>" + columnName + "</ColName>" +
                        "<Values>" + strValues + "</Values>" +
                        "<EngineName>" + cabinetName + "</EngineName>" +
                        "<SessionId>" + sessionID + "</SessionId>" +
                        "</APInsert_Input>";
                DAO_WI_Update.DAOWIUPDATELogger.debug("Mail Insert InputXml::::::::::\n"+sInputXML);
                sOutputXML =WFNGExecute(sInputXML, jtsIp,jtsPort,0);
                DAO_WI_Update.DAOWIUPDATELogger.debug("Mail Insert OutputXml::::::::::\n"+sOutputXML);
                objXMLParser.setInputXML(sOutputXML);
                mainCodeforAPInsert=objXMLParser.getValueOf("MainCode");
                
            }
			
			catch(Exception e)
            {
                e.printStackTrace();
                DAO_WI_Update.DAOWIUPDATELogger.error("Exception in Sending mail", e);
                sessionCheckInt++;
                waiteloopExecute(waitLoop);
                continue;
            }
            if (mainCodeforAPInsert.equalsIgnoreCase("11")) 
            {
                DAO_WI_Update.DAOWIUPDATELogger.debug("Invalid session in Sending mail");
                sessionCheckInt++;
                //ThreadConnect.sessionId = ThreadConnect.getSessionID(cabinetName, jtsIP, jtsPort, userName,password);
                sessionID=CommonConnection.getSessionID(DAO_WI_Update.DAOWIUPDATELogger, false);
                continue;
            }
            else
            {
                sessionCheckInt++;
                break;
            }
        }
        if(mainCodeforAPInsert.equalsIgnoreCase("0"))
        {
            DAO_WI_Update.DAOWIUPDATELogger.debug("mail Insert Successful");
            System.out.println("Mail Insert Successful for "+wiName+" in table WFMAILQUEUETABLE");
        }
        else
        {
            DAO_WI_Update.DAOWIUPDATELogger.debug("mail Insert Unsuccessful");
            System.out.println("Mail Insert Unsuccessful for "+wiName+"in table WFMAILQUEUETABLE");
        }
    }
	
	private void updateExternalTable(String tablename, String columnname,String sMessage, String sWhere, String jtsIP, String jtsPort, String cabinetName)
	{
		int sessionCheckInt=0;
		int loopCount=50;
		int mainCode = 0;
		
		DAO_WI_Update.DAOWIUPDATELogger.debug("Inside update EXT table: ");
		
		while(sessionCheckInt<loopCount)
		{
			try
			{
				XMLParser objXMLParser = new XMLParser();
				String inputXmlcheckAPUpdate = CommonMethods.getAPUpdateIpXML(tablename,columnname,sMessage,sWhere,cabinetName,sessionID);
				DAO_WI_Update.DAOWIUPDATELogger.debug(("inputXmlcheckAPUpdate : " + inputXmlcheckAPUpdate));
				String outXmlCheckAPUpdate=null;
				outXmlCheckAPUpdate=WFNGExecute(inputXmlcheckAPUpdate,jtsIP,jtsPort,1);
				DAO_WI_Update.DAOWIUPDATELogger.debug(("outXmlCheckAPUpdate : " + outXmlCheckAPUpdate));
				objXMLParser.setInputXML(outXmlCheckAPUpdate);
				String mainCodeforCheckUpdate = null;
				mainCodeforCheckUpdate=objXMLParser.getValueOf("MainCode");
				if (!mainCodeforCheckUpdate.equalsIgnoreCase("0"))
				{
					DAO_WI_Update.DAOWIUPDATELogger.debug(("Exception in ExecuteQuery_APUpdate updating "+tablename+" table"));
					System.out.println("Exception in ExecuteQuery_APUpdate updating "+tablename+" table");
				}
				else
				{
					DAO_WI_Update.DAOWIUPDATELogger.debug(("Succesfully updated "+tablename+" table"));
					System.out.println("Succesfully updated "+tablename+" table");
					//ThreadConnect.addToTextArea("Successfully updated transaction table");
				}
				mainCode=Integer.parseInt(mainCodeforCheckUpdate);
				if (mainCode == 11)
				{
					sessionID  = CommonConnection.getSessionID(DAO_WI_Update.DAOWIUPDATELogger, false);
				}
				else
				{
					sessionCheckInt++;
					break;
				}

				if (outXmlCheckAPUpdate.equalsIgnoreCase("") || outXmlCheckAPUpdate == "" || outXmlCheckAPUpdate == null)
					break;

			}
			catch(Exception e)
			{
				DAO_WI_Update.DAOWIUPDATELogger.debug(("Inside create validateSessionID exception"+e.getMessage()));
			}
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
	
	

	
	public String generatePDF(String sourceName, String destPath, Map<String, String> ht)
	{
		
        DAO_WI_Update.DAOWIUPDATELogger.debug("createPDF : createNewPDF : start :");
        DAO_WI_Update.DAOWIUPDATELogger.debug("createPDF : createNewPDF : ht.size() :" + ht.size());
		
        try {
        	
            PdfReader reader = new PdfReader(sourceName);
            DAO_WI_Update.DAOWIUPDATELogger.debug("createPDF : createNewPDF : Created reader object from source template pdf:");

            PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(destPath));
            DAO_WI_Update.DAOWIUPDATELogger.debug("createPDF : createNewPDF : Created stamper object in destination pdf:");

            AcroFields form = stamp.getAcroFields();
            //./DOA_Generated_Documents/SinglePagerGeneration/Templates/arabtype.ttf
            BaseFont unicode = BaseFont.createFont(onePagerTemplatesPath_source+File.separator+"arabtype.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            DAO_WI_Update.DAOWIUPDATELogger.debug("createPDF : createNewPDF : Created arabtype font:");

            ArrayList<BaseFont> al = new ArrayList<BaseFont>();
            al.add(unicode);

            form.setSubstitutionFonts(al);

            PdfWriter p = stamp.getWriter();
            p.setRunDirection(p.RUN_DIRECTION_RTL);
			
            BaseFont bf1 = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1252, BaseFont.EMBEDDED);
            form.addSubstitutionFont(bf1);
            DAO_WI_Update.DAOWIUPDATELogger.debug("createPDF : createNewPDF :  Created writer, set font times roman :");
            
            Set<String> PDFSet = ht.keySet();
            Iterator<String> PDFIt = PDFSet.iterator();
            DAO_WI_Update.DAOWIUPDATELogger.debug("createPDF : createNewPDF : Replacing values from XMLMap:");
			
            while (PDFIt.hasNext()) {
                String HT_Key = (String) PDFIt.next();
                String HT_Value = (String) ht.get(HT_Key);
                form.setField(HT_Key, HT_Value);
            }

            DAO_WI_Update.DAOWIUPDATELogger.debug("createPDF : createNewPDF : Values replaced from XMLMap:");
            stamp.setFormFlattening(true);
			
            stamp.close();
            DAO_WI_Update.DAOWIUPDATELogger.debug("createPDF : createNewPDF : Stamper closed:");
            return "Success";

        } 
        catch (Exception ex) 
        {
            DAO_WI_Update.DAOWIUPDATELogger.debug("createPDF : createNewPDF : ex.getMessage() : 2 :" + ex.getMessage());
            return "Error";
        }
	}
	
	

}



