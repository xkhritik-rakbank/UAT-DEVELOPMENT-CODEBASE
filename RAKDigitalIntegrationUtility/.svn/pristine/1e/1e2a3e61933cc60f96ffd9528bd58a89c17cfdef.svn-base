/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM
Application				: RAK BPM Utility
Module					: RAOP Status
File Name				: AWB_Gen.java
Author 					: Ravindra Kumar	
Date (DD/MM/YYYY)		: 01/06/2022

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/


package com.newgen.DAO.AWB;

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
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.newgen.DCC.Notify.DCCNotifyLog;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;;


public class AWB_Gen_file
{

	private static NGEjbClient ngEjbClientCIFVer;

	static Map<String, String> AWB_GEN_MAP= new HashMap<String, String>();


	static int socketConnectionTimeout=0;
	static int integrationWaitTime=0;
	static int sleepIntervalInMin=0;
	static int sessionCheckInt =0;
	public static int waitLoop=50;
	public static int loopCount=50;
	public static String fromMailID="";
	public static String toMailID = "";
	public static String mailSubject = "";
	public static String MailStr="";
	public static String jtsIP = "";
	public static String jtsPort = "";
	public String sessionID = "";
	
	public void AWB_run()
	{
		String cabinetName = "";
		String queueID = "";
		String queueID_DCC = "";

		try
		{
			DAO_AWB_Log.setLogger();
			ngEjbClientCIFVer = NGEjbClient.getSharedInstance();
			
			DAO_AWB_Log.DAO_AWB_Log.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			DAO_AWB_Log.DAO_AWB_Log.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				DAO_AWB_Log.DAO_AWB_Log.error("Could not Read Config Properties [DAO_AWB_Gen]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			DAO_AWB_Log.DAO_AWB_Log.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			DAO_AWB_Log.DAO_AWB_Log.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			DAO_AWB_Log.DAO_AWB_Log.debug("JTSPORT: " + jtsPort);

			queueID = AWB_GEN_MAP.get("queueID");
			DAO_AWB_Log.DAO_AWB_Log.debug("QueueID: " + queueID);
			
			queueID_DCC = AWB_GEN_MAP.get("queueID_DCC");
			DAO_AWB_Log.DAO_AWB_Log.debug("QueueID: " + queueID_DCC);
			
			socketConnectionTimeout=Integer.parseInt(AWB_GEN_MAP.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			DAO_AWB_Log.DAO_AWB_Log.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(AWB_GEN_MAP.get("INTEGRATION_WAIT_TIME"));
			DAO_AWB_Log.DAO_AWB_Log.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(AWB_GEN_MAP.get("SleepIntervalInMin"));
			DAO_AWB_Log.DAO_AWB_Log.debug("SleepIntervalInMin: "+sleepIntervalInMin);
			
			
			fromMailID=AWB_GEN_MAP.get("fromMailID");
			DAO_AWB_Log.DAO_AWB_Log.debug("fromMailID: "+fromMailID);
			
			toMailID=AWB_GEN_MAP.get("toMailID");
			DAO_AWB_Log.DAO_AWB_Log.debug("toMailID: "+toMailID);
			
			mailSubject=AWB_GEN_MAP.get("mailSubject");
			DAO_AWB_Log.DAO_AWB_Log.debug("mailSubject: "+mailSubject);
			
			MailStr=AWB_GEN_MAP.get("MailStr");
			DAO_AWB_Log.DAO_AWB_Log.debug("MailStr: "+MailStr);


			sessionID = CommonConnection.getSessionID(DAO_AWB_Log.DAO_AWB_Log, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				DAO_AWB_Log.DAO_AWB_Log.debug("Could Not Connect to Server!");
			}
			else
			{
				DAO_AWB_Log.DAO_AWB_Log.debug("Session ID found: " + sessionID);
				HashMap<String, String> socketDetailsMap= socketConnectionDetails(cabinetName, jtsIP, jtsPort, sessionID);
				DAO_AWB_Log.setLogger();
				DAO_AWB_Log.DAO_AWB_Log.debug("AWB_Gen_file...123.");
				startDAO_AWB_Gen(cabinetName, jtsIP, jtsPort,sessionID, queueID,socketConnectionTimeout, integrationWaitTime,socketDetailsMap);
				startDCC_AWB_Gen(cabinetName, jtsIP, jtsPort,sessionID, queueID_DCC,socketConnectionTimeout, integrationWaitTime,socketDetailsMap);
				System.out.println("No More workitems to Process, Sleeping!");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			DAO_AWB_Log.DAO_AWB_Log.error("Exception Occurred in AWB_Gen_file : "+e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			DAO_AWB_Log.DAO_AWB_Log.error("Exception Occurred in AWB_Gen_file : "+result);
		}
	}

	private void startDAO_AWB_Gen(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String queueID, int socketConnectionTimeOut, int integrationWaitTime, HashMap<String, String> socketDetailsMap)
	{
		String ws_name="dispatch";
		
		try
		{
			//Validate Session ID
			sessionID  = CommonConnection.getSessionID(DAO_AWB_Log.DAO_AWB_Log, false);

			if (sessionID == null || sessionID.equalsIgnoreCase("") || sessionID.equalsIgnoreCase("null"))
			{
				DAO_AWB_Log.DAO_AWB_Log.error("Could Not Get Session ID "+sessionID);
				return;
			}

			//Fetch all Work-Items on given queueID.
			DAO_AWB_Log.DAO_AWB_Log.debug("Fetching all Workitems for AWB DAO ");
			System.out.println("Fetching all Workitems on queue");
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionID, queueID);
			DAO_AWB_Log.DAO_AWB_Log.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);

			DAO_AWB_Log.DAO_AWB_Log.debug("WMFetchWorkList DAO_AWB_Gen OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			DAO_AWB_Log.DAO_AWB_Log.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			DAO_AWB_Log.DAO_AWB_Log.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);

			DAO_AWB_Log.DAO_AWB_Log.debug("Number of workitems retrieved on DAO_AWB_Gen: "+fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on DAO_AWB_Gen: "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0)
			{
				for(int i=0; i<fetchWorkitemListCount; i++)
				{
					final HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();
					
					String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

					DAO_AWB_Log.DAO_AWB_Log.debug("Parsing <Instrument> in WMFetchWorkList OutputXML DAO: "+fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID=xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					DAO_AWB_Log.DAO_AWB_Log.debug("Current ProcessInstanceID DAO: "+processInstanceID);

					DAO_AWB_Log.DAO_AWB_Log.debug("Processing Workitem DAO: "+processInstanceID);
					System.out.println("\nProcessing Workitem: "+processInstanceID);

					String WorkItemID=xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					DAO_AWB_Log.DAO_AWB_Log.debug("Current WorkItemID DAO: "+WorkItemID);

					String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					DAO_AWB_Log.DAO_AWB_Log.debug("Current EntryDateTime DAO: "+entryDateTime);

					String ActivityName=xmlParserfetchWorkItemData.getValueOf("ActivityName");
					DAO_AWB_Log.DAO_AWB_Log.debug("ActivityName DAO: "+ActivityName);

					String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					DAO_AWB_Log.DAO_AWB_Log.debug("ActivityID DAO: "+ActivityID);
					String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					DAO_AWB_Log.DAO_AWB_Log.debug("ActivityType DAO: "+ActivityType);
					String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					DAO_AWB_Log.DAO_AWB_Log.debug("ProcessDefId DAO: "+ProcessDefId);

					String decisionValue="";
					String DBQuer_validateCase ="select isnull(AWB_Number,'') as 'AWB_Number' from NG_DAO_EXTTABLE with (NOLOCK ) where WI_name='" + processInstanceID + "' and (AWB_Number='' or AWB_Number is null) and ((is_ntb='N' and ((is_cbs_req='Y' and Is_CBS='Y') or is_cbs_req='N')) or (is_ntb='Y' and ((is_cbs_req='Y' and Is_CBS='Y') or is_cbs_req='N') and ((is_prime_req='Y' and Is_prime='Y') or is_prime_req='N')))";
					// select method (product written) used to get the data in form of xml.
					String extTabDataIPXML_validateCase =CommonMethods.apSelectWithColumnNames(DBQuer_validateCase, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAO_AWB_Log.DAO_AWB_Log, false));
					DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataIPXML DAO: " + extTabDataIPXML_validateCase);
					String extTabDataOPXML_validateCase = WFNGExecute(extTabDataIPXML_validateCase, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
					DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataOPXML DAO: " + extTabDataOPXML_validateCase);
					// using xml parser to pass the output data in desired format 
					XMLParser xmlParserData_validateCase = new XMLParser(extTabDataOPXML_validateCase);
					// total values retrieved > 0 is a check
					int iTotalrec_validateCase = Integer.parseInt(xmlParserData_validateCase.getValueOf("TotalRetrieved"));
					// Main code we get if the ap select call is triggered success.
					if (xmlParserData_validateCase.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_validateCase > 0)
					{
						String DBQuery ="select employement_type as 'employement_type' ,Company_employer_name as 'emp_name',mobile_no_1 as 'mobNo',WI_name as 'Wi_no',Given_Name as 'first_name',Surname as 'last_name' from NG_DAO_EXTTABLE with (NOLOCK ) where WI_name='" + processInstanceID + "'";
						// select method (product written) used to get the data in form of xml.
						String extTabDataIPXML =CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAO_AWB_Log.DAO_AWB_Log, false));
						DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataIPXML DAO: " + extTabDataIPXML);
						String extTabDataOPXML = WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
						DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataOPXML DAO: " + extTabDataOPXML);
						// using xml parser to pass the output data in desired format 
						XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
					
						int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
						// Main code we get if the ap select call is triggered success.
						if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
						{
							String xmlDataExtTab = xmlParserData.getNextValueOf("Record");
							xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
							// replace the spcl char above.
							NGXmlList objWorkList = xmlParserData.createList("Records", "Record");

							// loop over the map to put value key pair.
							for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
							{
								CheckGridDataMap.put("emp_name", objWorkList.getVal("emp_name"));
								CheckGridDataMap.put("mobNo", objWorkList.getVal("mobNo"));
								CheckGridDataMap.put("Wi_no", objWorkList.getVal("Wi_no"));
								CheckGridDataMap.put("first_name", objWorkList.getVal("first_name"));
								CheckGridDataMap.put("last_name", objWorkList.getVal("last_name"));
								CheckGridDataMap.put("employement_type", objWorkList.getVal("employement_type"));
							}
						}

						String DBQuery_add ="select top 1 Flat_Villa_No as 'Flat_No',Building_Villa_Name as 'Building_name',Street_Location as 'street_loc',Country_address as 'country_Add', Emirate_City_address as 'Emirate_City_address' from NG_DAO_GR_ADDRESS_DETAIL with (NOLOCK ) where WI_name='" + processInstanceID + "'";
						// select method (product written) used to get the data in form of xml.
						String extTabDataIPXML_1 =CommonMethods.apSelectWithColumnNames(DBQuery_add, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAO_AWB_Log.DAO_AWB_Log, false));
						DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataIPXML_1: " + extTabDataIPXML_1);
						String extTabDataOPXML_1 = WFNGExecute(extTabDataIPXML_1, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
						DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataOPXML_2: " + extTabDataOPXML_1);
						// using xml parser to pass the output data in desired format 
						XMLParser xmlParserData_1 = new XMLParser(extTabDataOPXML_1);
						// total values retrieved > 0 is a check
						int iTotalrec_1 = Integer.parseInt(xmlParserData_1.getValueOf("TotalRetrieved"));
						// Main code we get if the ap select call is triggered success.
						if (xmlParserData_1.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_1 > 0)
						{
							String xmlDataExtTab = xmlParserData_1.getNextValueOf("Record");
							xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
							// replace the spcl char above.
							NGXmlList objWorkList = xmlParserData_1.createList("Records", "Record");

							// loop over the map to put value key pair.
							for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
							{
								CheckGridDataMap.put("Flat_No", objWorkList.getVal("Flat_No"));
								CheckGridDataMap.put("Building_name", objWorkList.getVal("Building_name"));
								CheckGridDataMap.put("street_loc", objWorkList.getVal("street_loc"));
								CheckGridDataMap.put("country_Add", objWorkList.getVal("country_Add"));
								CheckGridDataMap.put("Emirate_City_address", objWorkList.getVal("Emirate_City_address"));
							}
						}

						String DBQuery_country_add ="select CD_DESC as 'country_add_desc' from NG_MASTER_DAO_COUNTRY with (NOLOCK) where CM_CODE='" + CheckGridDataMap.get("country_Add") + "'";
						// select method (product written) used to get the data in form of xml.
						extTabDataIPXML =CommonMethods.apSelectWithColumnNames(DBQuery_country_add, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAO_AWB_Log.DAO_AWB_Log, false));
						DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataIPXML_1: " + extTabDataIPXML);
						extTabDataOPXML = WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
						DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataOPXML_2: " + extTabDataOPXML);
						// using xml parser to pass the output data in desired format 
						XMLParser xmlParserData_2 = new XMLParser(extTabDataOPXML);
						// total values retrieved > 0 is a check
						int iTotalrec_2 = Integer.parseInt(xmlParserData_2.getValueOf("TotalRetrieved"));
						// Main code we get if the ap select call is triggered success.
						if (xmlParserData_2.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_2 > 0)
						{
							String xmlDataExtTab = xmlParserData_2.getNextValueOf("Record");
							xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
							// replace the spcl char above.
							NGXmlList objWorkList = xmlParserData_2.createList("Records", "Record");

							// loop over the map to put value key pair.
							for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
							{
								CheckGridDataMap.put("country_add_desc", objWorkList.getVal("country_add_desc"));
								DAO_AWB_Log.DAO_AWB_Log.debug("country_add_desc : "+CheckGridDataMap.get("country_add_desc"));
							}
						}
						
						String DBQuery_emirate_add ="select CD_DESC as 'Emirate_City_add_Desc' from NG_MASTER_DAO_EMIRATES_CITY with (NOLOCK ) where CM_CODE='" + CheckGridDataMap.get("Emirate_City_address") + "'";
						// select method (product written) used to get the data in form of xml.
						extTabDataIPXML =CommonMethods.apSelectWithColumnNames(DBQuery_emirate_add, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAO_AWB_Log.DAO_AWB_Log, false));
						DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataIPXML_1: " + extTabDataIPXML);
						extTabDataOPXML = WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
						DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataOPXML_2: " + extTabDataOPXML);
						// using xml parser to pass the output data in desired format 
						XMLParser xmlParserData_3 = new XMLParser(extTabDataOPXML);
						// total values retrieved > 0 is a check
						int iTotalrec_3 = Integer.parseInt(xmlParserData_3.getValueOf("TotalRetrieved"));
						// Main code we get if the ap select call is triggered success.
						if (xmlParserData_3.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_3 > 0)
						{
							String xmlDataExtTab = xmlParserData_3.getNextValueOf("Record");
							xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
							// replace the spcl char above.
							NGXmlList objWorkList = xmlParserData_3.createList("Records", "Record");
							
							// loop over the map to put value key pair.
							for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
							{
								CheckGridDataMap.put("Emirate_City_add_Desc", objWorkList.getVal("Emirate_City_add_Desc"));
								DAO_AWB_Log.DAO_AWB_Log.debug("Emirate_City_add_Desc : "+CheckGridDataMap.get("Emirate_City_add_Desc"));
							}
						}
						
						if(CheckGridDataMap.get("employement_type").equalsIgnoreCase("Self employed")){
							
							String company_name="select top 1 Company_Name as 'Company_Name' from NG_DAO_GR_COMPANY_DETAILS with(nolock) where WI_name='" + processInstanceID + "'";
							
							extTabDataIPXML =CommonMethods.apSelectWithColumnNames(company_name, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAO_AWB_Log.DAO_AWB_Log, false));
							DAO_AWB_Log.DAO_AWB_Log.debug(" company_name extTabDataIPXML_1: " + extTabDataIPXML);
							extTabDataOPXML = WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
							DAO_AWB_Log.DAO_AWB_Log.debug(" company_name extTabDataOPXML_2: " + extTabDataOPXML);
							// using xml parser to pass the output data in desired format 
							XMLParser xmlParserData_4 = new XMLParser(extTabDataOPXML);
							// total values retrieved > 0 is a check
							int iTotalrec_4 = Integer.parseInt(xmlParserData_4.getValueOf("TotalRetrieved"));
							// Main code we get if the ap select call is triggered success.
							if (xmlParserData_4.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_4 > 0)
							{
								String xmlDataExtTab = xmlParserData_4.getNextValueOf("Record");
								xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
								// replace the spcl char above.
								NGXmlList objWorkList = xmlParserData_4.createList("Records", "Record");

								// loop over the map to put value key pair.
								for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
								{
									CheckGridDataMap.put("Company_Name", objWorkList.getVal("Company_Name"));
									DAO_AWB_Log.DAO_AWB_Log.debug("Company_Name : "+CheckGridDataMap.get("Company_Name"));
								}
							}
						}
						
						String DBQuery_entrydatetime ="select EntryDATETIME from WFINSTRUMENTTABLE with(nolock) where ProcessInstanceID ='" +processInstanceID+ "' and ActivityName='dispatch'";
						// select method (product written) used to get the data in form of xml.
						extTabDataIPXML =CommonMethods.apSelectWithColumnNames(DBQuery_entrydatetime, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAO_AWB_Log.DAO_AWB_Log, false));
						DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataIPXML_1: " + extTabDataIPXML);
						extTabDataOPXML = WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
						DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataOPXML_2: " + extTabDataOPXML);
						// using xml parser to pass the output data in desired format 
						XMLParser xmlParserData_5 = new XMLParser(extTabDataOPXML);
						// total values retrieved > 0 is a check
						int iTotalrec_4 = Integer.parseInt(xmlParserData_5.getValueOf("TotalRetrieved"));
						// Main code we get if the ap select call is triggered success.
						if (xmlParserData_5.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_4 > 0)
						{
							String xmlDataExtTab = xmlParserData_5.getNextValueOf("Record");
							xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
							
							NGXmlList objWorkList = xmlParserData_5.createList("Records", "Record");
							
							// loop over the map to put value key pair.
							for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
							{
								CheckGridDataMap.put("EntryDATETIME", objWorkList.getVal("EntryDATETIME"));
								DAO_AWB_Log.DAO_AWB_Log.debug("EntryDATETIME : "+CheckGridDataMap.get("EntryDATETIME"));
							}
						}
						
						String fileLocation=new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DAO_Integration")
								.append(System.getProperty("file.separator")).append("AWB_GEN.txt").toString();

						BufferedReader sbf=new BufferedReader(new FileReader(fileLocation));

						StringBuilder sb=new StringBuilder();
						String line=sbf.readLine();
						while(line!=null)
						{
							sb.append(line);
							sb.append(System.lineSeparator());
							line=sbf.readLine();
						}

						String Full_name = CheckGridDataMap.get("first_name").trim() +" "+ CheckGridDataMap.get("last_name").trim();
						String ToAddress=CheckGridDataMap.get("Flat_No").trim()+ " "+CheckGridDataMap.get("Building_name").trim() +" "+CheckGridDataMap.get("street_loc").trim();
						String Tostreet =CheckGridDataMap.get("Emirate_City_add_Desc").trim();

						DAO_AWB_Log.DAO_AWB_Log.debug("Full_name"+Full_name);
						DAO_AWB_Log.DAO_AWB_Log.debug("ToAddress"+ToAddress);
						DAO_AWB_Log.DAO_AWB_Log.debug("Tostreet"+Tostreet);
						
						String AWB =  sb.toString().replace(">WI_name<",">"+CheckGridDataMap.get("Wi_no").trim()+"<")
								.replace(">MobNo<",">"+CheckGridDataMap.get("mobNo").trim()+"<")
								.replace(">Full_name<",">"+Full_name+"<").replace(">Address<",">"+ToAddress+"<")
								.replace(">Street_location<",">"+Tostreet+"<")
								.replace(">Address_country<",">"+CheckGridDataMap.get("country_add_desc").trim()+"<");
						
						if(CheckGridDataMap.get("employement_type").equalsIgnoreCase("Self employed")){
							AWB=AWB.replace(">employer_name<",">"+CheckGridDataMap.get("Company_Name").trim()+"<");
						}else if(CheckGridDataMap.get("employement_type").equalsIgnoreCase("Salaried")){
							AWB=AWB.replace(">employer_name<",">"+CheckGridDataMap.get("emp_name").trim()+"<");
						}else{
							DAO_AWB_Log.DAO_AWB_Log.debug("other case emp_name :  "+CheckGridDataMap.get("emp_name").trim());
							DAO_AWB_Log.DAO_AWB_Log.debug("other case Company_Name :  "+CheckGridDataMap.get("Company_Name").trim());
							DAO_AWB_Log.DAO_AWB_Log.debug(">employer_name< : other case.");
							System.out.println("other case in employment type");
						}
						
						DAO_AWB_Log.DAO_AWB_Log.debug("AWB: "+AWB);
						
						String integrationStatus="Success";
						String attributesTag;
						String ErrDesc = "";
						StringBuilder finalString=new StringBuilder();
						finalString = finalString.append(AWB);
						//changes need to done to update the correct flag
						HashMap<String, String> socketConnectionMap =socketConnectionDetails(cabinetName, sJtsIp, iJtsPort, sessionID); 
						
						integrationStatus = socketConnection(cabinetName, CommonConnection.getUsername(), sessionID, sJtsIp, iJtsPort, processInstanceID, ws_name, 60, 65,socketConnectionMap, finalString);
						
						// - xml parse for getting out the return code.
						
						XMLParser xmlParserSocketDetails= new XMLParser(integrationStatus);
						DAO_AWB_Log.DAO_AWB_Log.debug(" xmlParserSocketDetails : "+xmlParserSocketDetails);
						String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
						DAO_AWB_Log.DAO_AWB_Log.debug("Return Code: "+return_code+ "WI: "+processInstanceID);
						String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
						DAO_AWB_Log.DAO_AWB_Log.debug("return_desc : "+return_desc+ "WI: "+processInstanceID);
						
						String MsgId ="";
						if (integrationStatus.contains("<MessageId>"))
							MsgId = xmlParserSocketDetails.getValueOf("MessageId");

						DAO_AWB_Log.DAO_AWB_Log.debug("MsgId : "+MsgId+" AWB for WI: "+processInstanceID);

						if(return_code.equalsIgnoreCase("0000"))
						{
							integrationStatus="Success";
							ErrDesc = "AWB Done Successfully";
							DAO_AWB_Log.DAO_AWB_Log.debug("AWB Done Successfully");
							
							// code for storing AWB No and AWb pdf---- Start
							String AWB_No = xmlParserSocketDetails.getValueOf("AWBNumber");
							String AWB_pdf = xmlParserSocketDetails.getValueOf("AWBPdf");
							String AWb_status = "R";
							
							updateExternalTable("NG_DAO_EXTTABLE","AWB_Number","'"+AWB_No+"'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
							updateExternalTable("NG_DAO_EXTTABLE","Awb_pdf","'"+AWB_pdf+"'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
							updateExternalTable("NG_DAO_EXTTABLE","AWB_status","'"+AWb_status+"'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
							// code for updating  AWB No and AWb pdf---- End
							
							// Code for insert into ng_digital_awb_status entry.
							insert_ng_digital_awb_status(processInstanceID,cabinetName,sessionID,sJtsIp,iJtsPort,ActivityName);
						}
						if ("Success".equalsIgnoreCase(integrationStatus))
						{
							decisionValue = "Approve";
							DAO_AWB_Log.DAO_AWB_Log.debug("Decision in success: " +decisionValue);
							attributesTag="<Decision>"+decisionValue+"</Decision>";
						}
						else
						{
							ErrDesc = return_desc;//integrationStatus.replace("~", ",").replace("|", "\n");
							decisionValue = "Reject";
							DAO_AWB_Log.DAO_AWB_Log.debug("Decision in else : " +decisionValue);
							attributesTag="<Decision>"+decisionValue+"</Decision>";
							
							sendMail(cabinetName,processInstanceID,jtsIP ,jtsPort,ErrDesc,return_code,ProcessDefId,MsgId);
							
						}

						//To be modified according to output of Integration Call.

						//Lock Workitem.
						String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionID, processInstanceID,WorkItemID);
						String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML,sJtsIp,iJtsPort,1);
						DAO_AWB_Log.DAO_AWB_Log.debug("Output XML For WmgetWorkItemCall: "+ getWorkItemOutputXml);

						XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
						String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
						DAO_AWB_Log.DAO_AWB_Log.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);

						if (getWorkItemMainCode.trim().equals("0"))
						{
							DAO_AWB_Log.DAO_AWB_Log.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);

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

							DAO_AWB_Log.DAO_AWB_Log.debug("InputXML for assignWorkitemAttribute Call DAO_AWB_Gen: "+assignWorkitemAttributeInputXML);

							String assignWorkitemAttributeOutputXML=WFNGExecute(assignWorkitemAttributeInputXML,sJtsIp,
									iJtsPort,1);

							DAO_AWB_Log.DAO_AWB_Log.debug("OutputXML for assignWorkitemAttribute Call DAO_AWB_Gen: "+assignWorkitemAttributeOutputXML);

							XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
							String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
							// For AWB number get:
							String AWB_Number = xmlParserWorkitemAttribute.getValueOf("AWBNumber");
							String AWB_PDF = xmlParserWorkitemAttribute.getValueOf("AWBPdf");

							DAO_AWB_Log.DAO_AWB_Log.debug("AssignWorkitemAttribute MainCode: "+assignWorkitemAttributeMainCode);
							DAO_AWB_Log.DAO_AWB_Log.debug("AWB_Number : "+AWB_Number);
							DAO_AWB_Log.DAO_AWB_Log.debug("AWB_PDF : "+AWB_PDF);
							
							if(assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0"))
							{
								DAO_AWB_Log.DAO_AWB_Log.debug("Assign Workitem Attribute Successful: "+assignWorkitemAttributeMainCode);

								System.out.println(processInstanceID + "Complete Succesfully with status "+decisionValue);

								DAO_AWB_Log.DAO_AWB_Log.debug("WorkItem moved to next Workstep.");

								//Insert in WIHistory Table.decisionValue
								//decisionValue = "Approve";
							}
							else
							{
								//assignWorkitemAttributeMainCode="";
								DAO_AWB_Log.DAO_AWB_Log.debug("decisionValue : "+decisionValue);
								//decisionValue = "Reject";
								//start
								ErrDesc="Done WI failed";
								sendMail(cabinetName,processInstanceID,jtsIP ,jtsPort,ErrDesc,assignWorkitemAttributeMainCode,ProcessDefId,MsgId);
							}

							DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String formattedEntryDatetime = dateFormat.format(new Date());
							DAO_AWB_Log.DAO_AWB_Log.debug("FormattedEntryDatetime: "+formattedEntryDatetime);
							
							// parse to change and store and reflect as txt box on front end.
							String entrydatetime = CheckGridDataMap.get("EntryDATETIME");
							if(entrydatetime.equalsIgnoreCase("") || entrydatetime==null){
								entrydatetime=entryDateTime;
							}
							Date d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(entrydatetime);
							String entrydatetime_format = dateFormat.format(d1);


							String columnNames="wi_name,decision_date_time,workstep,user_name,Decision,Remarks,entry_date_time";
							String columnValues="'"+processInstanceID+"','"+formattedEntryDatetime+"','"+ActivityName+"','"
									+CommonConnection.getUsername()+"','"+decisionValue+"','"+ErrDesc+"','"+entrydatetime_format+"'";
							
							String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionID, columnNames, columnValues,"NG_DAO_GR_DECISION_HISTORY");
							DAO_AWB_Log.DAO_AWB_Log.debug("APInsertInputXML: "+apInsertInputXML);

							String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
							DAO_AWB_Log.DAO_AWB_Log.debug("APInsertOutputXML: "+ apInsertInputXML);

							XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
							String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
							DAO_AWB_Log.DAO_AWB_Log.debug("Status of apInsertMaincode  "+ apInsertMaincode);

							DAO_AWB_Log.DAO_AWB_Log.debug("Completed On "+ ActivityName);

							if(apInsertMaincode.equalsIgnoreCase("0"))
							{
								DAO_AWB_Log.DAO_AWB_Log.debug("ApInsert successful: "+apInsertMaincode);
								DAO_AWB_Log.DAO_AWB_Log.debug("Inserted in WiHistory table successfully.");
							}
							else
							{
								DAO_AWB_Log.DAO_AWB_Log.debug("ApInsert failed: "+apInsertMaincode);
							}
						}
						else
						{
							getWorkItemMainCode="";
							DAO_AWB_Log.DAO_AWB_Log.debug("WmgetWorkItem failed: "+getWorkItemMainCode);
						}
					}
				}
			}
		}
			catch (Exception e)

		{
			DAO_AWB_Log.DAO_AWB_Log.debug("Exception: "+e.getMessage());
		}
	}
	
	private void startDCC_AWB_Gen(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String queueID, int socketConnectionTimeOut, int integrationWaitTime, HashMap<String, String> socketDetailsMap)
	{
		String ws_name="Sys_PrimeAWB_Gen";
		
		try
		{
			sessionID  = CommonConnection.getSessionID(DAO_AWB_Log.DAO_AWB_Log, false);

			if (sessionID == null || sessionID.equalsIgnoreCase("") || sessionID.equalsIgnoreCase("null"))
			{
				DAO_AWB_Log.DAO_AWB_Log.error("Could Not Get Session ID "+sessionID);
				return;
			}
			
			DAO_AWB_Log.DAO_AWB_Log.debug("Fetching all Workitems for AWB DCC ");
			System.out.println("Fetching all Workitems on queue");
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionID, queueID);
			DAO_AWB_Log.DAO_AWB_Log.debug("InputXML for DCC fetchWorkList Call: "+fetchWorkitemListInputXML);
			String fetchWorkitemListOutputXML= WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);
			DAO_AWB_Log.DAO_AWB_Log.debug("WMFetchWorkList DCC OutputXML: "+fetchWorkitemListOutputXML);
			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);
			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			DAO_AWB_Log.DAO_AWB_Log.debug("FetchWorkItemListMainCode DCC: "+fetchWorkItemListMainCode);
			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			DAO_AWB_Log.DAO_AWB_Log.debug("RetrievedCount for WMFetchWorkList Call DCC: "+fetchWorkitemListCount);
			DAO_AWB_Log.DAO_AWB_Log.debug("Number of workitems retrieved on DCC: "+fetchWorkitemListCount);
			System.out.println("Number of workitems retrieved on DCC: "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0)
			{
				for(int i=0; i<fetchWorkitemListCount; i++)
				{
					try{

						final HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();
						
						String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
						fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

						DAO_AWB_Log.DAO_AWB_Log.debug("Parsing <Instrument> in WMFetchWorkList OutputXML DCC: "+fetchWorkItemlistData);
						XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

						String processInstanceID=xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
						DAO_AWB_Log.DAO_AWB_Log.debug("Current ProcessInstanceID DCC: "+processInstanceID);

						DAO_AWB_Log.DAO_AWB_Log.debug("Processing Workitem: "+processInstanceID);
						System.out.println("\nProcessing Workitem DCC: "+processInstanceID);

						String WorkItemID=xmlParserfetchWorkItemData.getValueOf("WorkItemId");
						DAO_AWB_Log.DAO_AWB_Log.debug("Current WorkItemID DCC: "+WorkItemID);

						String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
						DAO_AWB_Log.DAO_AWB_Log.debug("Current EntryDateTime DCC: "+entryDateTime);

						String ActivityName=xmlParserfetchWorkItemData.getValueOf("ActivityName");
						DAO_AWB_Log.DAO_AWB_Log.debug("ActivityName DCC: "+ActivityName);

						String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
						DAO_AWB_Log.DAO_AWB_Log.debug("ActivityID DCC: "+ActivityID);
						String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
						DAO_AWB_Log.DAO_AWB_Log.debug("ActivityType DCC : "+ActivityType);
						String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
						DAO_AWB_Log.DAO_AWB_Log.debug("ProcessDefId DCC: "+ProcessDefId);
						
						
						//Added on 301122 for Validating Reschueled case:.
						String decisionValue="";
						String DBQuer_validateCase="";
						String DBQuer_RescheduleFlag = "SELECT CardOps_Reschedule from NG_DCC_EXTTABLE with (NOLOCK ) where WI_name='" + processInstanceID + "'";
						String extTabDataIPXML_RescheduleFlag =CommonMethods.apSelectWithColumnNames(DBQuer_RescheduleFlag, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAO_AWB_Log.DAO_AWB_Log, false));
						DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataIPXML DCC: " + extTabDataIPXML_RescheduleFlag);
						String extTabDataOPXML_RescheduleFlag = WFNGExecute(extTabDataIPXML_RescheduleFlag, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
						DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataOPXML DCC: " + extTabDataOPXML_RescheduleFlag);
						XMLParser xmlParserData_RescheduleFlag = new XMLParser(extTabDataOPXML_RescheduleFlag);
						if(!xmlParserData_RescheduleFlag.getValueOf("CardOps_Reschedule").equals("Y")){
							DBQuer_validateCase ="select WI_name from NG_DCC_EXTTABLE with (NOLOCK ) where WI_name='" + processInstanceID + "' and Courier_Flag='File_received' and (AWB_Number='' or AWB_Number is null);";
						}
						else{
							DBQuer_validateCase ="select WI_name from NG_DCC_EXTTABLE with (NOLOCK ) where WI_name='" + processInstanceID + "';";
						}
						//End
						
						//Deepak code
						String extTabDataIPXML_validateCase =CommonMethods.apSelectWithColumnNames(DBQuer_validateCase, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAO_AWB_Log.DAO_AWB_Log, false));
						DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataIPXML DCC: " + extTabDataIPXML_validateCase);
						String extTabDataOPXML_validateCase = WFNGExecute(extTabDataIPXML_validateCase, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
						DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataOPXML DCC: " + extTabDataOPXML_validateCase);
						XMLParser xmlParserData_validateCase = new XMLParser(extTabDataOPXML_validateCase);
						int iTotalrec_validateCase = Integer.parseInt(xmlParserData_validateCase.getValueOf("TotalRetrieved"));
						if (xmlParserData_validateCase.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_validateCase > 0)
						{
							String DBQuery ="select Wi_Name,Employer_Name,EmploymentType_Desc,MobileNo,FirstName,MiddleName,LastName from NG_DCC_EXTTABLE with (nolock) where Wi_Name='" + processInstanceID + "'";
							
							String extTabDataIPXML =CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAO_AWB_Log.DAO_AWB_Log, false));
							DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataIPXML DCC: " + extTabDataIPXML);
							String extTabDataOPXML = WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
							DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataOPXML DCC: " + extTabDataOPXML);
							XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
							int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
							
							if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
							{
								String xmlDataExtTab = xmlParserData.getNextValueOf("Record");
								xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
								NGXmlList objWorkList = xmlParserData.createList("Records", "Record");
								for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
								{
									CheckGridDataMap.put("Wi_Name", objWorkList.getVal("Wi_Name"));
									CheckGridDataMap.put("Employer_Name", objWorkList.getVal("Employer_Name"));
									CheckGridDataMap.put("EmploymentType_Desc", objWorkList.getVal("EmploymentType_Desc"));
									CheckGridDataMap.put("MobileNo", objWorkList.getVal("MobileNo"));
									CheckGridDataMap.put("FirstName", objWorkList.getVal("FirstName"));
									CheckGridDataMap.put("MiddleName", objWorkList.getVal("MiddleName"));
									CheckGridDataMap.put("LastName", objWorkList.getVal("LastName"));
								}
							}

							String DBQuery_add ="select top 1 House_No,Building_Name, PO_Box_Address, City_Desc,Country_Desc from NG_DCC_GR_ADDRESS_DETAIL with (nolock) where (Address_Type='OFFICE' or Address_Type='RESIDENCE') and Wi_Name='" + processInstanceID + "'";
							String extTabDataIPXML_1 =CommonMethods.apSelectWithColumnNames(DBQuery_add, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAO_AWB_Log.DAO_AWB_Log, false));
							DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataIPXML_1: " + extTabDataIPXML_1);
							String extTabDataOPXML_1 = WFNGExecute(extTabDataIPXML_1, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
							DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataOPXML_2: " + extTabDataOPXML_1);
							XMLParser xmlParserData_1 = new XMLParser(extTabDataOPXML_1);
							int iTotalrec_1 = Integer.parseInt(xmlParserData_1.getValueOf("TotalRetrieved"));
							
							if (xmlParserData_1.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_1 > 0)
							{
								String xmlDataExtTab = xmlParserData_1.getNextValueOf("Record");
								xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
								NGXmlList objWorkList = xmlParserData_1.createList("Records", "Record");

								for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
								{
									CheckGridDataMap.put("House_No", objWorkList.getVal("House_No"));
									CheckGridDataMap.put("Building_Name", objWorkList.getVal("Building_Name"));
									CheckGridDataMap.put("PO_Box_Address", objWorkList.getVal("PO_Box_Address"));
									CheckGridDataMap.put("City_Desc", objWorkList.getVal("City_Desc"));
									CheckGridDataMap.put("Country_Desc", objWorkList.getVal("Country_Desc"));
								}
							}

							String fileLocation=new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DCC_Integration")
									.append(System.getProperty("file.separator")).append("AWB.txt").toString();

							BufferedReader sbf=new BufferedReader(new FileReader(fileLocation));

							StringBuilder sb=new StringBuilder();
							String line=sbf.readLine();
							while(line!=null)
							{
								sb.append(line);
								sb.append(System.lineSeparator());
								line=sbf.readLine();
							}
							
							String Full_name = CheckGridDataMap.get("FirstName").trim()+" "+CheckGridDataMap.get("MiddleName").trim() +" "+ CheckGridDataMap.get("LastName").trim();
							String ToAddress=CheckGridDataMap.get("House_No").trim()+ " "+CheckGridDataMap.get("Building_Name").trim() +" "+CheckGridDataMap.get("PO_Box_Address").trim();
							String Tostreet =CheckGridDataMap.get("City_Desc").trim();

							DAO_AWB_Log.DAO_AWB_Log.debug("Full_name"+Full_name);
							DAO_AWB_Log.DAO_AWB_Log.debug("ToAddress"+ToAddress);
							DAO_AWB_Log.DAO_AWB_Log.debug("Tostreet"+Tostreet);
							
							String AWB =  sb.toString().replace(">WI_name<",">"+CheckGridDataMap.get("Wi_Name").trim()+"<")
									.replace(">MobNo<",">"+CheckGridDataMap.get("MobileNo").trim()+"<")
									.replace(">Full_name<",">"+Full_name+"<").replace(">Address<",">"+ToAddress+"<")
									.replace(">employer_name<",">"+CheckGridDataMap.get("Employer_Name").trim()+"<")
									.replace(">Street_location<",">"+Tostreet+"<")
									.replace(">Address_country<",">"+CheckGridDataMap.get("Country_Desc").trim()+"<");
							
							DAO_AWB_Log.DAO_AWB_Log.debug("AWB DCC: "+AWB);
							
							String integrationStatus="Success";
							String attributesTag;
							String ErrDesc = "";
							StringBuilder finalString=new StringBuilder();
							finalString = finalString.append(AWB);
							//changes need to done to update the correct flag
							HashMap<String, String> socketConnectionMap =socketConnectionDetails(cabinetName, sJtsIp, iJtsPort, sessionID); 
							
							integrationStatus = socketConnection(cabinetName, CommonConnection.getUsername(), sessionID, sJtsIp, iJtsPort, processInstanceID, ws_name, 60, 65,socketConnectionMap, finalString);
							
							XMLParser xmlParserSocketDetails= new XMLParser(integrationStatus);
							DAO_AWB_Log.DAO_AWB_Log.debug(" xmlParserSocketDetails : "+xmlParserSocketDetails);
							String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
							DAO_AWB_Log.DAO_AWB_Log.debug("Return Code: "+return_code+ "WI: "+processInstanceID);
							String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
							DAO_AWB_Log.DAO_AWB_Log.debug("return_desc : "+return_desc+ "WI: "+processInstanceID);
							
							String MsgId ="";
							if (integrationStatus.contains("<MessageId>"))
								MsgId = xmlParserSocketDetails.getValueOf("MessageId");

							DAO_AWB_Log.DAO_AWB_Log.debug("MsgId : "+MsgId+" AWB for DCC WI: "+processInstanceID);

							if(return_code.equalsIgnoreCase("0000"))
							{
								integrationStatus="Success";
								ErrDesc = "AWB Done Successfully";
								DAO_AWB_Log.DAO_AWB_Log.debug("AWB Done Successfully DCC");
								
								// code for storing AWB No and AWb pdf---- Start
								String AWB_No = xmlParserSocketDetails.getValueOf("AWBNumber");
								String AWB_pdf = xmlParserSocketDetails.getValueOf("AWBPdf");
								String AWb_status = "R";
								
								updateExternalTable("NG_DCC_EXTTABLE","AWB_Number","'"+AWB_No+"'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
								updateExternalTable("NG_DCC_EXTTABLE","awb_pdf","'"+AWB_pdf+"'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
								
								//updateExternalTable("NG_DAO_EXTTABLE","AWB_status","'"+AWb_status+"'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName, sessionId);
								// code for updating  AWB No and AWb pdf---- End
			
								// Code for insert into ng_digital_awb_status entry.
								insert_ng_digital_awb_status_DCC(processInstanceID,cabinetName,sessionID,sJtsIp,iJtsPort,ActivityName);
							}
							if ("Success".equalsIgnoreCase(integrationStatus))
							{
								decisionValue = "Approve";
								DAO_AWB_Log.DAO_AWB_Log.debug("Decision in success DCC: " +decisionValue);
								attributesTag="<Decision>"+decisionValue+"</Decision>";
							}
							else
							{
								ErrDesc = return_desc;
								decisionValue = "Failed";
								DAO_AWB_Log.DAO_AWB_Log.debug("Decision in else DCC : " +decisionValue);
								attributesTag="<Decision>"+decisionValue+"</Decision>";
								sendMail(cabinetName,processInstanceID,jtsIP ,jtsPort,ErrDesc,return_code,ProcessDefId,MsgId);
							}

							String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionID, processInstanceID,WorkItemID);
							String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML,sJtsIp,iJtsPort,1);
							DAO_AWB_Log.DAO_AWB_Log.debug("Output XML For WmgetWorkItemCall: "+ getWorkItemOutputXml);

							XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
							String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
							DAO_AWB_Log.DAO_AWB_Log.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);

							if (getWorkItemMainCode.trim().equals("0"))
							{
								DAO_AWB_Log.DAO_AWB_Log.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);

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
								
								DAO_AWB_Log.DAO_AWB_Log.debug("InputXML for assignWorkitemAttribute Call Notify: "+assignWorkitemAttributeInputXML);

								String assignWorkitemAttributeOutputXML=WFNGExecute(assignWorkitemAttributeInputXML,sJtsIp,iJtsPort,1);

								DAO_AWB_Log.DAO_AWB_Log.debug("OutputXML for assignWorkitemAttribute Call Notify: "+assignWorkitemAttributeOutputXML);

								XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
								String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
								// For AWB number get:
								String AWB_Number = xmlParserWorkitemAttribute.getValueOf("AWBNumber");
								String AWB_PDF = xmlParserWorkitemAttribute.getValueOf("AWBPdf");
								
								DAO_AWB_Log.DAO_AWB_Log.debug("AssignWorkitemAttribute MainCode: "+assignWorkitemAttributeMainCode);
								DAO_AWB_Log.DAO_AWB_Log.debug("AWB_Number : "+AWB_Number);
								DAO_AWB_Log.DAO_AWB_Log.debug("AWB_PDF : "+AWB_PDF);
								
								if(assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0"))
								{
									DAO_AWB_Log.DAO_AWB_Log.debug("Assign Workitem Attribute Successful: "+assignWorkitemAttributeMainCode);
									System.out.println(processInstanceID + "Complete Succesfully with status "+decisionValue);
									DAO_AWB_Log.DAO_AWB_Log.debug("WorkItem moved to next Workstep.");
								}
								else
								{
									DAO_AWB_Log.DAO_AWB_Log.debug("decisionValue : "+decisionValue);
									ErrDesc="Done WI Failed";
									sendMail(cabinetName,processInstanceID,jtsIP ,jtsPort,ErrDesc,assignWorkitemAttributeMainCode,ProcessDefId,MsgId);
								}

								/*DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								String formattedEntryDatetime = dateFormat.format(new Date());
								DAO_AWB_Log.DAO_AWB_Log.debug("FormattedEntryDatetime: "+formattedEntryDatetime);*/
								
								// parse to change and store and reflect as txt box on front end.
								/*String entrydatetime = CheckGridDataMap.get("EntryDATETIME");
								Date d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(entrydatetime);
								String entrydatetime_format = dateFormat.format(d1);*/
								DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								
								Date current_date = new Date();
								String formattedActionDatetime=dateFormat.format(current_date);
								DAO_AWB_Log.DAO_AWB_Log.debug("FormattedActionDatetime: "+formattedActionDatetime);
								
								SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
								SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

								Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
								String formattedEntryDatetime = outputDateFormat.format(entryDatetimeFormat);
								DAO_AWB_Log.DAO_AWB_Log.debug("FormattedEntryDatetime: " + formattedEntryDatetime);
								
								String columnNames="wi_name,dec_date,ENTRY_DATE_TIME,workstep,user_name,Decision,Remarks";
								String columnValues="'"+processInstanceID+"','"+formattedActionDatetime+"','"+formattedEntryDatetime+"','"+ActivityName+"','"
										+CommonConnection.getUsername()+"','"+decisionValue+"','"+ErrDesc+"'";
								
								String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionID, columnNames, columnValues,"NG_DCC_GR_DECISION_HISTORY");
								DAO_AWB_Log.DAO_AWB_Log.debug("APInsertInputXML: "+apInsertInputXML);

								String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
								DAO_AWB_Log.DAO_AWB_Log.debug("APInsertOutputXML: "+ apInsertInputXML);

								XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
								String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
								DAO_AWB_Log.DAO_AWB_Log.debug("Status of apInsertMaincode  "+ apInsertMaincode);

								DAO_AWB_Log.DAO_AWB_Log.debug("Completed On "+ ActivityName);

								if(apInsertMaincode.equalsIgnoreCase("0"))
								{
									DAO_AWB_Log.DAO_AWB_Log.debug("ApInsert successful: "+apInsertMaincode);
									DAO_AWB_Log.DAO_AWB_Log.debug("Inserted in WiHistory table successfully.");
								}
								else
								{
									DAO_AWB_Log.DAO_AWB_Log.debug("ApInsert failed: "+apInsertMaincode);
								}
							}
							else
							{
								getWorkItemMainCode="";
								DAO_AWB_Log.DAO_AWB_Log.debug("WmgetWorkItem failed: "+getWorkItemMainCode);
								ErrDesc="WI Failed";
								sendMail(cabinetName,processInstanceID,jtsIP ,jtsPort,ErrDesc,getWorkItemMainCode,ProcessDefId,MsgId);
							}
						}
					
					}
					catch(Exception e){
						DAO_AWB_Log.DAO_AWB_Log.debug("Exception: "+e.getMessage());
					}
				}
			}
		}
			catch (Exception e)
		{
			DAO_AWB_Log.DAO_AWB_Log.debug("Exception: "+e.getMessage());
		}
	}
	
	private void insert_ng_digital_awb_status_DCC(String Wi_name,String cabinetName,String  sessionId, String sJtsIp,String iJtsPort,String ActivityName)
	{
		try
		{
		String process_name="";
		// select the values from ext table to insert into ng_digital_awb_status
		final HashMap<String, String> CheckGridDataMap_awb = new HashMap<String, String>();
		String DBQuery_awb ="select Wi_Name,Prospect_id,FirstName,MiddleName,LastName,MobileNo,email_id,AWB_Number,ntb,ECRN,isnull(CardOps_Reschedule,'N') as CardOps_Reschedule from NG_DCC_EXTTABLE where WI_name='" + Wi_name + "'";
		
		String extTabDataIPXML_awb =CommonMethods.apSelectWithColumnNames(DBQuery_awb, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAO_AWB_Log.DAO_AWB_Log, false));
		DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataOPXML_awb: " + extTabDataIPXML_awb);
		String extTabDataOPXML_awb = WFNGExecute(extTabDataIPXML_awb, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
		DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataOPXML_awb: " + extTabDataOPXML_awb);
		XMLParser xmlParserData_awb = new XMLParser(extTabDataOPXML_awb);
		
		int iTotalrec = Integer.parseInt(xmlParserData_awb.getValueOf("TotalRetrieved"));
		
		if (xmlParserData_awb.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
		{
			String xmlDataExtTab = xmlParserData_awb.getNextValueOf("Record");
			xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
			NGXmlList objWorkList = xmlParserData_awb.createList("Records", "Record");
		
			for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)){
				CheckGridDataMap_awb.put("Wi_Name", objWorkList.getVal("Wi_Name"));
				CheckGridDataMap_awb.put("Prospect_id", objWorkList.getVal("Prospect_id"));
				CheckGridDataMap_awb.put("FirstName", objWorkList.getVal("FirstName"));
				CheckGridDataMap_awb.put("MiddleName", objWorkList.getVal("MiddleName"));
				CheckGridDataMap_awb.put("LastName", objWorkList.getVal("LastName"));
				CheckGridDataMap_awb.put("MobileNo", objWorkList.getVal("MobileNo"));
				CheckGridDataMap_awb.put("email_id", objWorkList.getVal("email_id"));
				CheckGridDataMap_awb.put("AWB_Number", objWorkList.getVal("AWB_Number"));
				CheckGridDataMap_awb.put("ntb", objWorkList.getVal("ntb"));
				CheckGridDataMap_awb.put("ECRN", objWorkList.getVal("ECRN"));
				CheckGridDataMap_awb.put("CardOps_Reschedule", objWorkList.getVal("CardOps_Reschedule"));
			}
			
			String processname[] = Wi_name.split("-");
			DAO_AWB_Log.DAO_AWB_Log.debug("processname [] : "+processname[0]);
			process_name=processname[0];
			DAO_AWB_Log.DAO_AWB_Log.debug("processname [] : "+process_name);
		}
		
		// hritik 08..=09.2022 - CR (CardSerno)
		
		String Query_for_cardserno = "select CardSerno,ELITE_CRN from NG_DCC_PRIME_COURIER with (nolock) where WI_name='"+Wi_name+"'";
		String extTabDataIPXML_prime_cor=CommonMethods.apSelectWithColumnNames(Query_for_cardserno, cabinetName, sessionID);
		DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataIPXML_prime_cor: "+extTabDataIPXML_prime_cor);
		String extTabDataOPXML_prime_cor = CommonMethods.WFNGExecute(extTabDataIPXML_prime_cor,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
		DAO_AWB_Log.DAO_AWB_Log.debug(" extTabDataOPXML_prime_cor "+ extTabDataOPXML_prime_cor);
		XMLParser xmlParserData_prime_cor= new XMLParser(extTabDataOPXML_prime_cor);		
		int iTotalrec_prime_cor = Integer.parseInt(xmlParserData_prime_cor.getValueOf("TotalRetrieved"));
		DAO_AWB_Log.DAO_AWB_Log.debug("iTotalrec_wi_table "+iTotalrec_prime_cor);
		
		if(xmlParserData_prime_cor.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_prime_cor > 0 ){
			String xmlDataExtTab=xmlParserData_prime_cor.getNextValueOf("Record");
			xmlDataExtTab =xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
			NGXmlList objWorkList=xmlParserData_prime_cor.createList("Records", "Record");
			
			for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)){
				CheckGridDataMap_awb.put("CardSerno", objWorkList.getVal("CardSerno"));
				CheckGridDataMap_awb.put("ELITE_CRN", objWorkList.getVal("ELITE_CRN"));
			}
			DAO_AWB_Log.DAO_AWB_Log.debug(" CardSerno "+CheckGridDataMap_awb.get("CardSerno").trim()+" for  DCC ELITE_CRN "+ CheckGridDataMap_awb.get("ELITE_CRN").trim());
		}
		
		Date d= new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sDate = dateFormat.format(d);
		DAO_AWB_Log.DAO_AWB_Log.debug("insert_ng_digital_awb_status : sDate "+sDate);
		
		String prospect_id= CheckGridDataMap_awb.get("Prospect_id").trim();
		String Full_name=CheckGridDataMap_awb.get("FirstName").trim() +" "+ CheckGridDataMap_awb.get("MiddleName").trim() +" "+CheckGridDataMap_awb.get("LastName").trim();
	
		String ECRN="";
		if(CheckGridDataMap_awb.containsKey("ECRN")){
			ECRN = CheckGridDataMap_awb.get("ECRN").trim();
		}
		
		String CardSerno="";
		if(CheckGridDataMap_awb.containsKey("CardSerno")){
			CardSerno = CheckGridDataMap_awb.get("CardSerno").trim();
		}
		String CardOps_Reschedule="";
		if(CheckGridDataMap_awb.containsKey("CardOps_Reschedule")){
			CardOps_Reschedule = CheckGridDataMap_awb.get("CardOps_Reschedule").trim();
		}
		
		DAO_AWB_Log.DAO_AWB_Log.debug("insert_ng_digital_awb_status DCC : ECRN "+ECRN);
		DAO_AWB_Log.DAO_AWB_Log.debug("insert_ng_digital_awb_status DCC : prospect_id "+prospect_id);
		DAO_AWB_Log.DAO_AWB_Log.debug("insert_ng_digital_awb_status DCC  : Full_name "+Full_name);
		String columnNames_awbTable="";
		String columnValues_awbTable="";
		
		if(CardOps_Reschedule.equalsIgnoreCase("Y")){
			columnNames_awbTable="WI_name,Prospect_ID,Customer_name,mobile_No,email_id,ECRN,AWB_Number,processName,singlePager_ref_no,AWB_Gen_success_date,is_Ntb,CardSerno,card_req";
			columnValues_awbTable="'"+Wi_name+"','"+prospect_id+"','"+Full_name+"','"
							+CheckGridDataMap_awb.get("MobileNo").trim()
							+"','"+CheckGridDataMap_awb.get("email_id").trim()+
							"','"+ECRN.trim()+"','"+
							CheckGridDataMap_awb.get("AWB_Number").trim()
							+"','"+process_name+
							"','"+Wi_name+"','"+sDate+
							"','"+CheckGridDataMap_awb.get("ntb").trim()+
							"','"+CardSerno+"'"+
							",'N'";
		}
		else{
			columnNames_awbTable="WI_name,Prospect_ID,Customer_name,mobile_No,email_id,ECRN,AWB_Number,processName,singlePager_ref_no,AWB_Gen_success_date,is_Ntb,CardSerno";
			columnValues_awbTable="'"+Wi_name+"','"+prospect_id+"','"+Full_name+"','"
							+CheckGridDataMap_awb.get("MobileNo").trim()
							+"','"+CheckGridDataMap_awb.get("email_id").trim()+
							"','"+ECRN.trim()+"','"+
							CheckGridDataMap_awb.get("AWB_Number").trim()
							+"','"+process_name+
							"','"+Wi_name+"','"+sDate+
							"','"+CheckGridDataMap_awb.get("ntb").trim()+
							"','"+CardSerno+"'";
		}
		
		
		DAO_AWB_Log.DAO_AWB_Log.debug("insert_ng_digital_awb_status : columnNames_awbTable DCC "+columnNames_awbTable);
		DAO_AWB_Log.DAO_AWB_Log.debug("insert_ng_digital_awb_status : columnValues_awbTable DCC "+columnValues_awbTable);

		String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionID, columnNames_awbTable, columnValues_awbTable,"ng_digital_awb_status");
		DAO_AWB_Log.DAO_AWB_Log.debug("APInsertInputXML: ng_digital_awb_status DCC "+apInsertInputXML);

		String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
		DAO_AWB_Log.DAO_AWB_Log.debug("APInsertOutputXML: ng_digital_awb_status DCC "+ apInsertInputXML);
		
		XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
		String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
		DAO_AWB_Log.DAO_AWB_Log.debug("Status of apInsertMaincode  ng_digital_awb_status DCC "+ apInsertMaincode);

		DAO_AWB_Log.DAO_AWB_Log.debug("Completed On ng_digital_awb_status DCC "+ ActivityName);
		
		if(apInsertMaincode.equalsIgnoreCase("0"))
		{
			DAO_AWB_Log.DAO_AWB_Log.debug("ApInsert successful: ng_digital_awb_status "+apInsertMaincode);
			DAO_AWB_Log.DAO_AWB_Log.debug("ApInsert successful: ng_digital_awb_status "+apInsertMaincode);
			System.out.println("ApInsert successful: ng_digital_awb_status DCC "+Wi_name);
			CheckGridDataMap_awb.clear();
		}
		else
		{
			DAO_AWB_Log.DAO_AWB_Log.debug("ApInsert failed for ng_digital_awb_status: "+apInsertMaincode);
			System.out.println("ApInsert failed: ng_digital_awb_status DCC "+Wi_name);
		}
		
		}
		catch(Exception e)
		{
			DAO_AWB_Log.DAO_AWB_Log.debug("insert_ng_digital_awb_status : "+e.getMessage());
		}
		
	}
	
	private void insert_ng_digital_awb_status(String Wi_name,String cabinetName,String  sessionId, String sJtsIp,String iJtsPort,String ActivityName)
	{
		try
		{
		String process_name="";
		// select the values from ext table to insert into ng_digital_awb_status
		final HashMap<String, String> CheckGridDataMap_awb = new HashMap<String, String>();
		String DBQuery_awb ="select WI_name,account_no,prospect_id,Given_Name,Surname,mobile_no_1,email_id_1,ChequeBk_Req,ChequeBk_ref,ECRN,AWB_Number,is_prime_req,AWB_status,is_Ntb from NG_DAO_EXTTABLE where WI_name='" + Wi_name + "'";
		
		String extTabDataIPXML_awb =CommonMethods.apSelectWithColumnNames(DBQuery_awb, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAO_AWB_Log.DAO_AWB_Log, false));
		DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataOPXML_awb: " + extTabDataIPXML_awb);
		String extTabDataOPXML_awb = WFNGExecute(extTabDataIPXML_awb, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
		DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataOPXML_awb: " + extTabDataOPXML_awb);
		XMLParser xmlParserData_awb = new XMLParser(extTabDataOPXML_awb);
		
		int iTotalrec = Integer.parseInt(xmlParserData_awb.getValueOf("TotalRetrieved"));
		
		if (xmlParserData_awb.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
		{
			String xmlDataExtTab = xmlParserData_awb.getNextValueOf("Record");
			xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
			NGXmlList objWorkList = xmlParserData_awb.createList("Records", "Record");
			
			for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)){
				
				CheckGridDataMap_awb.put("WI_name", objWorkList.getVal("WI_name"));
				CheckGridDataMap_awb.put("prospect_id", objWorkList.getVal("prospect_id"));
				CheckGridDataMap_awb.put("Given_Name", objWorkList.getVal("Given_Name"));
				CheckGridDataMap_awb.put("Surname", objWorkList.getVal("Surname"));
				CheckGridDataMap_awb.put("mobile_no_1", objWorkList.getVal("mobile_no_1"));
				CheckGridDataMap_awb.put("email_id_1", objWorkList.getVal("email_id_1"));
				CheckGridDataMap_awb.put("ChequeBk_Req", objWorkList.getVal("ChequeBk_Req"));
				CheckGridDataMap_awb.put("AWB_Number", objWorkList.getVal("AWB_Number"));
				CheckGridDataMap_awb.put("is_prime_req", objWorkList.getVal("is_prime_req"));
				CheckGridDataMap_awb.put("AWB_status", objWorkList.getVal("AWB_status"));
				CheckGridDataMap_awb.put("account_no", objWorkList.getVal("account_no"));
				CheckGridDataMap_awb.put("is_Ntb", objWorkList.getVal("is_Ntb"));
				
			}
			String processname[] = Wi_name.split("-");
			DAO_AWB_Log.DAO_AWB_Log.debug("processname [] : "+processname[0]);
			process_name=processname[0];
			DAO_AWB_Log.DAO_AWB_Log.debug("processname [] : "+process_name);
		}
		
		String Query_for_ecrn = "select ChequeBk_ref as 'ChequeBk_ref', ECRN as 'ECRN',WI_name from NG_DAO_WI_UPDATE with(nolock) where WI_name='"+Wi_name+"'";
		String extTabDataIPXMLwi_table=CommonMethods.apSelectWithColumnNames(Query_for_ecrn, cabinetName, sessionID);
		DAO_AWB_Log.DAO_AWB_Log.debug("Output_Query_for_ecrn: "+extTabDataIPXMLwi_table);
		String extTabDataOPXMLwi_table = CommonMethods.WFNGExecute(extTabDataIPXMLwi_table,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
		DAO_AWB_Log.DAO_AWB_Log.debug(" extTabDataOPXML : prime "+ extTabDataOPXMLwi_table);
		XMLParser xmlParserData_wi_table= new XMLParser(extTabDataOPXMLwi_table);		
		int iTotalrec_wi_table = Integer.parseInt(xmlParserData_wi_table.getValueOf("TotalRetrieved"));
		DAO_AWB_Log.DAO_AWB_Log.debug("iTotalrec_wi_table "+iTotalrec_wi_table);
		
		if(xmlParserData_wi_table.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_wi_table > 0 ){
			
			String xmlDataExtTab=xmlParserData_wi_table.getNextValueOf("Record");
			xmlDataExtTab =xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
			
			NGXmlList objWorkList=xmlParserData_wi_table.createList("Records", "Record");
			
			for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)){

				CheckGridDataMap_awb.put("ChequeBk_ref", objWorkList.getVal("ChequeBk_ref"));
				CheckGridDataMap_awb.put("ECRN", objWorkList.getVal("ECRN"));
			}
			DAO_AWB_Log.DAO_AWB_Log.debug(" ECRN "+CheckGridDataMap_awb.get("ECRN").trim()+" for  ChequeBk_ref "+ CheckGridDataMap_awb.get("ChequeBk_ref").trim());
		}
		
		// hritik 08..=09.2022 - CR (CardSerno)
		
		String Query_for_cardserno = "select CardSerno,ELITE_CRN from NG_DAO_PRIME_COURIER with (nolock) where WI_name='"+Wi_name+"'";
		String extTabDataIPXML_prime_cor=CommonMethods.apSelectWithColumnNames(Query_for_cardserno, cabinetName, sessionID);
		DAO_AWB_Log.DAO_AWB_Log.debug("extTabDataIPXML_prime_cor: "+extTabDataIPXML_prime_cor);
		String extTabDataOPXML_prime_cor = CommonMethods.WFNGExecute(extTabDataIPXML_prime_cor,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
		DAO_AWB_Log.DAO_AWB_Log.debug(" extTabDataOPXML_prime_cor "+ extTabDataOPXML_prime_cor);
		XMLParser xmlParserData_prime_cor= new XMLParser(extTabDataOPXML_prime_cor);		
		int iTotalrec_prime_cor = Integer.parseInt(xmlParserData_prime_cor.getValueOf("TotalRetrieved"));
		DAO_AWB_Log.DAO_AWB_Log.debug("iTotalrec_wi_table "+iTotalrec_prime_cor);
		
		if(xmlParserData_prime_cor.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_prime_cor > 0 ){
			String xmlDataExtTab=xmlParserData_prime_cor.getNextValueOf("Record");
			xmlDataExtTab =xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
			NGXmlList objWorkList=xmlParserData_prime_cor.createList("Records", "Record");
			
			for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)){
				CheckGridDataMap_awb.put("CardSerno", objWorkList.getVal("CardSerno"));
				CheckGridDataMap_awb.put("ELITE_CRN", objWorkList.getVal("ELITE_CRN"));
			}
			DAO_AWB_Log.DAO_AWB_Log.debug(" CardSerno "+CheckGridDataMap_awb.get("CardSerno").trim()+" for  ELITE_CRN "+ CheckGridDataMap_awb.get("ELITE_CRN").trim());
		}
		
		Date d= new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sDate = dateFormat.format(d);
		DAO_AWB_Log.DAO_AWB_Log.debug("insert_ng_digital_awb_status : sDate "+sDate);
		
		String prospect_id= CheckGridDataMap_awb.get("prospect_id").trim();
		String Full_name=CheckGridDataMap_awb.get("Given_Name").trim() +" "+CheckGridDataMap_awb.get("Surname").trim();
		
		String ChequeBk_ref ="";
		if(CheckGridDataMap_awb.containsKey("ChequeBk_ref")){
			ChequeBk_ref = CheckGridDataMap_awb.get("ChequeBk_ref").trim();
		}
		String ECRN="";
		if(CheckGridDataMap_awb.containsKey("ECRN")){
			ECRN = CheckGridDataMap_awb.get("ECRN").trim();
		}
		String account_no="";
		if(CheckGridDataMap_awb.containsKey("account_no")){
			account_no = CheckGridDataMap_awb.get("account_no").trim();
		}
		
		String CardSerno="";
		if(CheckGridDataMap_awb.containsKey("CardSerno")){
			CardSerno = CheckGridDataMap_awb.get("CardSerno").trim();
		}
		
		DAO_AWB_Log.DAO_AWB_Log.debug("insert_ng_digital_awb_status : ChequeBk_ref "+ChequeBk_ref);
		DAO_AWB_Log.DAO_AWB_Log.debug("insert_ng_digital_awb_status : ECRN "+ECRN);
		DAO_AWB_Log.DAO_AWB_Log.debug("insert_ng_digital_awb_status : account_no "+account_no);
		DAO_AWB_Log.DAO_AWB_Log.debug("insert_ng_digital_awb_status : prospect_id "+prospect_id);
		DAO_AWB_Log.DAO_AWB_Log.debug("insert_ng_digital_awb_status : Full_name "+Full_name);
		
		String columnNames_awbTable="WI_name,Prospect_ID,Customer_name,mobile_No,email_id,ChequeBk_Req,ChequeBk_ref,ECRN,AWB_Number,Status,card_req,processName, singlePager_ref_no,Account_no,AWB_Gen_success_date,is_Ntb,CardSerno";
		String columnValues_awbTable="'"+Wi_name+"','"+prospect_id+"','"+Full_name+"','"
							+CheckGridDataMap_awb.get("mobile_no_1").trim()
							+"','"+CheckGridDataMap_awb.get("email_id_1").trim()+
							"','"+CheckGridDataMap_awb.get("ChequeBk_Req").trim()+
							"','"+ChequeBk_ref.trim()
							+"','"+ECRN.trim()+"','"+
							CheckGridDataMap_awb.get("AWB_Number").trim()
							+"','"+CheckGridDataMap_awb.get("AWB_status").trim()+
							"','"+CheckGridDataMap_awb.get("is_prime_req").trim()+
							"','"+process_name+
							"','"+Wi_name+"','"+
							account_no.trim()+"','"+sDate+
							"','"+CheckGridDataMap_awb.get("is_Ntb").trim()+
							"','"+CardSerno+"'";
		
		DAO_AWB_Log.DAO_AWB_Log.debug("insert_ng_digital_awb_status : columnNames_awbTable "+columnNames_awbTable);
		DAO_AWB_Log.DAO_AWB_Log.debug("insert_ng_digital_awb_status : columnValues_awbTable "+columnValues_awbTable);

		String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionID, columnNames_awbTable, columnValues_awbTable,"ng_digital_awb_status");
		DAO_AWB_Log.DAO_AWB_Log.debug("APInsertInputXML: ng_digital_awb_status "+apInsertInputXML);

		String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
		DAO_AWB_Log.DAO_AWB_Log.debug("APInsertOutputXML: ng_digital_awb_status "+ apInsertInputXML);
		
		XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
		String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
		DAO_AWB_Log.DAO_AWB_Log.debug("Status of apInsertMaincode  ng_digital_awb_status "+ apInsertMaincode);

		DAO_AWB_Log.DAO_AWB_Log.debug("Completed On ng_digital_awb_status "+ ActivityName);
		
		if(apInsertMaincode.equalsIgnoreCase("0"))
		{
			DAO_AWB_Log.DAO_AWB_Log.debug("ApInsert successful: ng_digital_awb_status "+apInsertMaincode);
			DAO_AWB_Log.DAO_AWB_Log.debug("ApInsert successful: ng_digital_awb_status "+apInsertMaincode);
			System.out.println("ApInsert successful: ng_digital_awb_status "+Wi_name);
			CheckGridDataMap_awb.clear();
		}
		else
		{
			DAO_AWB_Log.DAO_AWB_Log.debug("ApInsert failed for ng_digital_awb_status: "+apInsertMaincode);
			System.out.println("ApInsert failed: ng_digital_awb_status "+Wi_name);
		}
		
		}
		catch(Exception e)
		{
			DAO_AWB_Log.DAO_AWB_Log.debug("insert_ng_digital_awb_status : "+e.getMessage());
		}
		
	}
	// Code for insert into ng_digital_awb_status entry. -- end
	private  HashMap<String, String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort, String sessionid) {
		HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

		try {
			DAO_AWB_Log.DAO_AWB_Log.debug("Fetching Socket Connection Details.");
			System.out.println("Fetching Socket Connection Details.");

			String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'DigitalAO' and CallingSource = 'Utility'";

			String socketDetailsInputXML = CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
			DAO_AWB_Log.DAO_AWB_Log.debug("Socket Details APSelect InputXML: " + socketDetailsInputXML);

			String socketDetailsOutputXML = WFNGExecute(socketDetailsInputXML, sJtsIp, iJtsPort, 1);
			DAO_AWB_Log.DAO_AWB_Log.debug("Socket Details APSelect OutputXML: " + socketDetailsOutputXML);

			XMLParser xmlParserSocketDetails = new XMLParser(socketDetailsOutputXML);
			String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			DAO_AWB_Log.DAO_AWB_Log.debug("SocketDetailsMainCode: " + socketDetailsMainCode);

			int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			DAO_AWB_Log.DAO_AWB_Log.debug("SocketDetailsTotalRecords: " + socketDetailsTotalRecords);

			if (socketDetailsMainCode.equalsIgnoreCase("0") && socketDetailsTotalRecords > 0) {
				String xmlDataSocketDetails = xmlParserSocketDetails.getNextValueOf("Record");
				xmlDataSocketDetails = xmlDataSocketDetails.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

				XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

				String socketServerIP = xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
				DAO_AWB_Log.DAO_AWB_Log.debug("SocketServerIP: " + socketServerIP);
				socketDetailsMap.put("SocketServerIP", socketServerIP);

				String socketServerPort = xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
				DAO_AWB_Log.DAO_AWB_Log.debug("SocketServerPort " + socketServerPort);
				socketDetailsMap.put("SocketServerPort", socketServerPort);

				DAO_AWB_Log.DAO_AWB_Log.debug("SocketServer Details found.");
				System.out.println("SocketServer Details found.");

			}
		} catch (Exception e) {
			DAO_AWB_Log.DAO_AWB_Log
					.debug("Exception in getting Socket Connection Details: " + e.getMessage());
			System.out.println("Exception in getting Socket Connection Details: " + e.getMessage());
		}
		
		return socketDetailsMap;
	}

	protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort, int flag)
			throws IOException, Exception {
		DAO_AWB_Log.DAO_AWB_Log.debug("In WF NG Execute : " + serverPort);
		try {
			if (serverPort.startsWith("33"))
				return WFCallBroker.execute(ipXML, jtsServerIP, Integer.parseInt(serverPort), 1);
			else
				return ngEjbClientCIFVer.makeCall(jtsServerIP, serverPort, "WebSphere", ipXML);
		} catch (Exception e) {
			DAO_AWB_Log.DAO_AWB_Log.debug("Exception Occured in WF NG Execute : " + e.getMessage());
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



		try
		{

			DAO_AWB_Log.DAO_AWB_Log.debug("userName "+ username);
			DAO_AWB_Log.DAO_AWB_Log.debug("SessionId "+ sessionID);

			socketServerIP=socketDetailsMap.get("SocketServerIP");
			DAO_AWB_Log.DAO_AWB_Log.debug("SocketServerIP "+ socketServerIP);
			socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			DAO_AWB_Log.DAO_AWB_Log.debug("SocketServerPort "+ socketServerPort);

	   		if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0))
	   		{

    			socket = new Socket(socketServerIP, socketServerPort);
    			socket.setSoTimeout(connection_timeout*1000);
    			out = socket.getOutputStream();
    			socketInputStream = socket.getInputStream();
    			dout = new DataOutputStream(out);
    			din = new DataInputStream(socketInputStream);
    			DAO_AWB_Log.DAO_AWB_Log.debug("Dout " + dout);
    			DAO_AWB_Log.DAO_AWB_Log.debug("Din " + din);

    			outputResponse = "";
    			String History_tablename="";
    			
    			DAO_AWB_Log.DAO_AWB_Log.debug("processInstanceID substring: "+ processInstanceID.substring(0, 3));
    			
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
    				DAO_AWB_Log.DAO_AWB_Log.debug("RequestLen: "+inputRequestLen + "");
    				inputRequest = inputRequestLen + "##8##;" + inputRequest;
    				DAO_AWB_Log.DAO_AWB_Log.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
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
    				DAO_AWB_Log.DAO_AWB_Log.debug("OutputResponse: "+outputResponse);

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

				//DAO_AWB_Log.DAO_AWB_Log.debug("outputResponse "+outputResponse);
				return outputResponse;

    	 		}

    		else
    		{
    			DAO_AWB_Log.DAO_AWB_Log.debug("SocketServerIp and SocketServerPort is not maintained "+"");
    			DAO_AWB_Log.DAO_AWB_Log.debug("SocketServerIp is not maintained "+	socketServerIP);
    			DAO_AWB_Log.DAO_AWB_Log.debug(" SocketServerPort is not maintained "+	socketServerPort);
    			return "Socket Details not maintained";
    		}

		}

		catch (Exception e)
		{
			DAO_AWB_Log.DAO_AWB_Log.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
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
				DAO_AWB_Log.DAO_AWB_Log.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
				//printException(e);
			}
		}


	}
	private String getResponseXML(String cabinetName,String sJtsIp,String iJtsPort, String sessionId, String processInstanceID,String message_ID, int integrationWaitTime)
	{

		String outputResponseXML="";
		try
		{
			
			String QueryString ="";
			if("DCC".equalsIgnoreCase(processInstanceID.substring(0, 3))){
				QueryString ="select OUTPUT_XML from NG_DCC_XMLLOG_HISTORY with (nolock) where MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+processInstanceID+"'";
			}
			else{
				QueryString ="select OUTPUT_XML from NG_DAO_XMLLOG_HISTORY with (nolock) where MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+processInstanceID+"'";
			}
			

			String responseInputXML =CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, sessionID);
			DAO_AWB_Log.DAO_AWB_Log.debug("Response APSelect InputXML: "+responseInputXML);

			int Loop_count=0;
			do
			{
				String responseOutputXML=CommonMethods.WFNGExecute(responseInputXML,sJtsIp,iJtsPort,1);
				DAO_AWB_Log.DAO_AWB_Log.debug("Response APSelect OutputXML: "+responseOutputXML);

			    XMLParser xmlParserSocketDetails= new XMLParser(responseOutputXML);
			    String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			    DAO_AWB_Log.DAO_AWB_Log.debug("ResponseMainCode: "+responseMainCode);



			    int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			    DAO_AWB_Log.DAO_AWB_Log.debug("ResponseTotalRecords: "+responseTotalRecords);

			    if (responseMainCode.equals("0") && responseTotalRecords > 0)
				{

					String responseXMLData=xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData =responseXMLData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

	        		XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
	        		//DAO_AWB_Log.DAO_AWB_Log.debug("ResponseXMLData: "+responseXMLData);

	        		outputResponseXML=xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
	        		//DAO_AWB_Log.DAO_AWB_Log.debug("OutputResponseXML: "+outputResponseXML);

	        		if("".equalsIgnoreCase(outputResponseXML)){
	        			outputResponseXML="Error";
	    			}
	        		break;
				}
			    Loop_count++;
			    Thread.sleep(1000);
			}
			while(Loop_count<integrationWaitTime);
			DAO_AWB_Log.DAO_AWB_Log.debug("integrationWaitTime: "+integrationWaitTime);

		}
		catch(Exception e)
		{
			DAO_AWB_Log.DAO_AWB_Log.debug("Exception occurred in outputResponseXML" + e.getMessage());
			outputResponseXML="Error";
		}

		return outputResponseXML;

	}
	
	private String getRequestXML(String cabinetName, String sessionId,
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
		DAO_AWB_Log.DAO_AWB_Log.debug("GetRequestXML: "+ strBuff.toString());
		return strBuff.toString();
	}
	
	private void updateExternalTable(String tablename, String columnname,String sMessage, String sWhere, String jtsIP, String jtsPort, String cabinetName)
	{
		int sessionCheckInt=0;
		int loopCount=50;
		int mainCode = 0;
		
		DAO_AWB_Log.DAO_AWB_Log.debug("Inside update EXT table: ");
		
		while(sessionCheckInt<loopCount)
		{
			try
			{
				XMLParser objXMLParser = new XMLParser();
				String inputXmlcheckAPUpdate = CommonMethods.getAPUpdateIpXML(tablename,columnname,sMessage,sWhere,cabinetName,sessionID);
				DAO_AWB_Log.DAO_AWB_Log.debug(("inputXmlcheckAPUpdate : " + inputXmlcheckAPUpdate));
				String outXmlCheckAPUpdate=null;
				outXmlCheckAPUpdate=WFNGExecute(inputXmlcheckAPUpdate,jtsIP,jtsPort,1);
				DAO_AWB_Log.DAO_AWB_Log.debug(("outXmlCheckAPUpdate : " + outXmlCheckAPUpdate));
				objXMLParser.setInputXML(outXmlCheckAPUpdate);
				String mainCodeforCheckUpdate = null;
				mainCodeforCheckUpdate=objXMLParser.getValueOf("MainCode");
				if (!mainCodeforCheckUpdate.equalsIgnoreCase("0"))
				{
					DAO_AWB_Log.DAO_AWB_Log.debug(("Exception in ExecuteQuery_APUpdate updating "+tablename+" table"));
					System.out.println("Exception in ExecuteQuery_APUpdate updating "+tablename+" table");
				}
				else
				{
					DAO_AWB_Log.DAO_AWB_Log.debug(("Succesfully updated "+tablename+" table"));
					System.out.println("Succesfully updated "+tablename+" table");
					//ThreadConnect.addToTextArea("Successfully updated transaction table");
				}
				mainCode=Integer.parseInt(mainCodeforCheckUpdate);
				if (mainCode == 11)
				{
					sessionID  = CommonConnection.getSessionID(DAO_AWB_Log.DAO_AWB_Log, false);
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
				DAO_AWB_Log.DAO_AWB_Log.debug(("Inside create validateSessionID exception"+e.getMessage()));
			}
		}
	}

	private static int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "DAO_AWB_Gen_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			  {
			    String name = (String) names.nextElement();
			    AWB_GEN_MAP.put(name, p.getProperty(name));
			  }
		    }
		catch (Exception e)
		{
			return -1 ;
		}
		return 0;
	}
	
	public void sendMail(String cabinetName,String wiName,String jtsIp,String jtsPort,String ErrDesc, String return_code,String ProcessDefId,String MsgId)throws Exception
    {
        XMLParser objXMLParser = new XMLParser();
        String sInputXML="";
        String sOutputXML="";
        String mainCodeforAPInsert=null;
        sessionCheckInt=0;
        while(sessionCheckInt<loopCount)
        {
            try
            {
            	DAO_AWB_Log.DAO_AWB_Log.debug("workitem name to send mail---"+wiName);
            	DAO_AWB_Log.DAO_AWB_Log.debug("ErrorMsg to send mail---"+ErrDesc);
            	DAO_AWB_Log.DAO_AWB_Log.debug("return_code to send mail---"+return_code);
            	
            	String FinalMailStr = MailStr.toString().replace("<WI_NAME>",wiName).replace("<ret_Code>",return_code)
            	.replace("<errormsg>",ErrDesc).replace("<MsgID>", MsgId);
            	DAO_AWB_Log.DAO_AWB_Log.debug("finalbody: "+FinalMailStr);

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
                DAO_AWB_Log.DAO_AWB_Log.debug("Mail Insert InputXml::::::::::\n"+sInputXML);
                sOutputXML =WFNGExecute(sInputXML, jtsIp,jtsPort,0);
                DAO_AWB_Log.DAO_AWB_Log.debug("Mail Insert OutputXml::::::::::\n"+sOutputXML);
                objXMLParser.setInputXML(sOutputXML);
                mainCodeforAPInsert=objXMLParser.getValueOf("MainCode");
                
            }
			
			catch(Exception e)
            {
                e.printStackTrace();
                DAO_AWB_Log.DAO_AWB_Log.error("Exception in Sending mail", e);
                sessionCheckInt++;
                waiteloopExecute(waitLoop);
                continue;
            }
            if (mainCodeforAPInsert.equalsIgnoreCase("11")) 
            {
                DAO_AWB_Log.DAO_AWB_Log.debug("Invalid session in Sending mail");
                sessionCheckInt++;
                //ThreadConnect.sessionId = ThreadConnect.getSessionID(cabinetName, jtsIP, jtsPort, userName,password);
                sessionID=CommonConnection.getSessionID(DAO_AWB_Log.DAO_AWB_Log, false);
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
            DAO_AWB_Log.DAO_AWB_Log.debug("mail Insert Successful");
            System.out.println("Mail Insert Successful for "+wiName+" in table WFMAILQUEUETABLE");
        }
        else
        {
            DAO_AWB_Log.DAO_AWB_Log.debug("mail Insert Unsuccessful");
            System.out.println("Mail Insert Unsuccessful for "+wiName+"in table WFMAILQUEUETABLE");
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
}



