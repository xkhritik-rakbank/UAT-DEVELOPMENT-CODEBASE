package com.newgen.DCC.CAMGenBSR;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.XMLParser;

public class Digital_CC_BCR_GenCAMReport implements Runnable {

	static Map<String, String> configParamMap = new HashMap<String, String>();
	public String sessionID = "";
	public static String fromMailID="";
	public static String toMailID = "";
	public static String mailSubject = "";
	public static String MailStr="";
	public String ProcessDefId = "";
	
	public void run() {
		
		String cabinetName = "";
		String jtsIP = "";
		String jtsPort = "";
		String queueID = "";
		String queueID_ETB_hold = "";
		String UserName = "";
		
		int socketConnectionTimeout = 0;
		int integrationWaitTime = 0;
		int sleepIntervalInMin = 0;
		try {
			Digital_CC_log.setLogger();

			Digital_CC_log.Digital_CC.debug("Inside try Catch ...");

			Digital_CC_log.Digital_CC.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			Digital_CC_log.Digital_CC.debug("configReadStatus " + configReadStatus);
			if (configReadStatus != 0){
				Digital_CC_log.Digital_CC.error("Could not Read Config Properties");
				return;
			}
			
			cabinetName = CommonConnection.getCabinetName();
			Digital_CC_log.Digital_CC.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			Digital_CC_log.Digital_CC.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			Digital_CC_log.Digital_CC.debug("JTSPORT: " + jtsPort);

			queueID = configParamMap.get("queueID");
			Digital_CC_log.Digital_CC.debug("QueueID: " + queueID);
			
			queueID_ETB_hold= configParamMap.get("queueID_ETB_hold");
			Digital_CC_log.Digital_CC.debug("queueID_ETB_hold: " + queueID_ETB_hold);
			
			socketConnectionTimeout = Integer.parseInt(configParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			Digital_CC_log.Digital_CC.debug("SocketConnectionTimeOut: " + socketConnectionTimeout);

			integrationWaitTime = Integer.parseInt(configParamMap.get("INTEGRATION_WAIT_TIME"));
			Digital_CC_log.Digital_CC.debug("IntegrationWaitTime: " + integrationWaitTime);

			sleepIntervalInMin = Integer.parseInt(configParamMap.get("SleepIntervalInMin"));
			Digital_CC_log.Digital_CC.debug("SleepIntervalInMin: " + sleepIntervalInMin);
			
			fromMailID= configParamMap.get("fromMailID");
			Digital_CC_log.Digital_CC.debug("fromMailID: "+fromMailID);
			
			toMailID=configParamMap.get("toMailID");
			Digital_CC_log.Digital_CC.debug("toMailID: "+toMailID);
			
			mailSubject=configParamMap.get("mailSubject");
			Digital_CC_log.Digital_CC.debug("mailSubject: "+mailSubject);
			
			MailStr=configParamMap.get("MailStr");
			Digital_CC_log.Digital_CC.debug("MailStr: "+MailStr);
		


			sessionID = CommonConnection.getSessionID(Digital_CC_log.Digital_CC, false); 

			if (sessionID.trim().equalsIgnoreCase("")) {
				Digital_CC_log.Digital_CC.debug("Could Not Connect to Server!");
			} else {
				HashMap<String, String> socketDetailsMap = CommonMethods.socketConnectionDetails(cabinetName, jtsIP,
						jtsPort, sessionID);
				while (true) {
					Digital_CC_log.Digital_CC.debug("Session ID found: " + sessionID);
					Digital_CC_log.setLogger();

					genrateBSRCam(cabinetName, UserName, jtsIP, jtsPort, sessionID, queueID, socketConnectionTimeout
							,integrationWaitTime, socketDetailsMap);
					
					//if("Sys_ETB_Hold".)
					ETB_Hold(cabinetName, UserName, jtsIP, jtsPort, sessionID, queueID_ETB_hold, socketConnectionTimeout,integrationWaitTime, socketDetailsMap);

					Thread.sleep(sleepIntervalInMin * 60 * 1000);
				}
			}
		} catch (Exception e) {
			Digital_CC_log.Digital_CC.error("Exception Occurred in DCC  : ", e);
		}
	}

	// method define here
	private void genrateBSRCam(String cabinetName, String UserName, String sJtsIp, String iJtsPort, String sessionId,
			String queueID, int socketConnectionTimeOut, int integrationWaitTime,
			HashMap<String, String> socketDetailsMap) {
		// Rubi -- temp check with attacheddoc WS
		final String ws_name = "Digital_CC_ETB_Intro"; // write workstep name

		try {
			
			// Validate Session ID
			sessionID = CommonConnection.getSessionID(Digital_CC_log.Digital_CC, false);

			if (sessionID == null || sessionID.equalsIgnoreCase("") || sessionID.equalsIgnoreCase("null")) {
				Digital_CC_log.Digital_CC.debug("Could Not Get Session ID " + sessionID);
				return;
			}

			// Fetch all Work-Items on given queueID.
			Digital_CC_log.Digital_CC.debug("Fetching all Workitems on System_Error queue");
			System.out.println("Fetching all Workitems on System_Error queue");

			String fetchWorkitemListInputXML = CommonMethods.fetchWorkItemsInput(cabinetName, sessionID, queueID);
			Digital_CC_log.Digital_CC.debug("InputXML for fetchWorkList Call: " + fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML = CommonMethods.WFNGExecute(fetchWorkitemListInputXML, sJtsIp, iJtsPort,
					1);
			Digital_CC_log.Digital_CC.debug("WMFetchWorkList OutputXML: " + fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			Digital_CC_log.Digital_CC.debug("FetchWorkItemListMainCode: " + fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			Digital_CC_log.Digital_CC.debug("RetrievedCount for WMFetchWorkList Call: " + fetchWorkitemListCount);
			Digital_CC_log.Digital_CC.debug("Number of workitems retrieved on System_Error: " + fetchWorkitemListCount);
			System.out.println("Number of workitems retrieved on System_Error: " + fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0) {
				for (int i = 0; i < fetchWorkitemListCount; i++) {
					String fetchWorkItemlistData = xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData = fetchWorkItemlistData.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

					Digital_CC_log.Digital_CC.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "
							+ fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID = xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					Digital_CC_log.Digital_CC.debug("Current ProcessInstanceID: " + processInstanceID);
					Digital_CC_log.Digital_CC.debug("Processing Workitem: " + processInstanceID);

					String WorkItemID = xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					Digital_CC_log.Digital_CC.debug("Current WorkItemID: " + WorkItemID);

					String entryDateTime = xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					Digital_CC_log.Digital_CC.debug("Current EntryDateTime: " + entryDateTime);
					
					String Createddatetime = xmlParserfetchWorkItemData.getValueOf("IntroductionDATETIME");
					Digital_CC_log.Digital_CC.debug("Current IntroductionDATETIME: " + Createddatetime);

					ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					Digital_CC_log.Digital_CC.debug("ProcessDefId: "+ProcessDefId);
					
					// Rubi ---for this add new column in external table with
					// the name of "IS_BSR_CAM_generated"
					System.out.println("wi: "+processInstanceID);
					String DBQuery = "SELECT IS_BSR_CAM_generated,CIF FROM NG_DCC_EXTTABLE with(nolock) WHERE WI_NAME='"
							+ processInstanceID + "'";

					String extTabDataINPXML = CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), sessionID);
					Digital_CC_log.Digital_CC.debug("extTabDataIPXML: " + extTabDataINPXML);
					String extTabDataOUPXML = CommonMethods.WFNGExecute(extTabDataINPXML, CommonConnection.getJTSIP(),CommonConnection.getJTSPort(), 1);
					Digital_CC_log.Digital_CC.debug("extTabDataOPXML: " + extTabDataOUPXML);

					XMLParser xmlParserDataDB = new XMLParser(extTabDataOUPXML);

					String IS_BSR_CAM_generated = xmlParserDataDB.getValueOf("IS_BSR_CAM_generated");
					String Cif_Id = xmlParserDataDB.getValueOf("CIF");
					// String Is_STP = xmlParserDataDB.getValueOf("Is_STP");
					String pdfName = "";
					if (IS_BSR_CAM_generated.equalsIgnoreCase("N") || "".equalsIgnoreCase(IS_BSR_CAM_generated)) {

						pdfName = "ApplicationForm_BSR";

						Digital_CC_BRSCAMTemplate obj = new Digital_CC_BRSCAMTemplate(Digital_CC_log.Digital_CC);
						String attrbList = obj.generate_BSR_CAM_Report(cabinetName, pdfName, Cif_Id, processInstanceID,sessionID, sJtsIp, iJtsPort, entryDateTime, WorkItemID, Createddatetime,ProcessDefId);

					} else {
						Digital_CC_log.Digital_CC.info("Cam Report Is Already Generated");
					}
				}
			}
		} catch (Exception e) {
			Digital_CC_log.Digital_CC.debug("Exception: " + e.getMessage());
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	private void ETB_Hold(String cabinetName, String UserName, String sJtsIp, String iJtsPort, String sessionId,String queueID, int socketConnectionTimeOut, int integrationWaitTime,
			HashMap<String, String> socketDetailsMap) {
		
		final String ws_name = "Sys_ETB_Hold"; 
		try {
			
			sessionID = CommonConnection.getSessionID(Digital_CC_log.Digital_CC, false);

			if (sessionID == null || sessionID.equalsIgnoreCase("") || sessionID.equalsIgnoreCase("null")) {
				Digital_CC_log.Digital_CC.debug("Could Not Get Session ID " + sessionID);
				return;
			}
			Digital_CC_log.Digital_CC.debug("Fetching all Workitems on Sys_ETB_Hold queue");
			System.out.println("Fetching all Workitems on Sys_ETB_Hold queue");

			String fetchWorkitemListInputXML = CommonMethods.fetchWorkItemsInput(cabinetName, sessionID, queueID);
			Digital_CC_log.Digital_CC.debug("InputXML for fetchWorkList Call: " + fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML = CommonMethods.WFNGExecute(fetchWorkitemListInputXML, sJtsIp, iJtsPort,1);
			Digital_CC_log.Digital_CC.debug("WMFetchWorkList OutputXML: " + fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			Digital_CC_log.Digital_CC.debug("FetchWorkItemListMainCode: " + fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			Digital_CC_log.Digital_CC.debug("RetrievedCount for WMFetchWorkList Call: " + fetchWorkitemListCount);
			Digital_CC_log.Digital_CC.debug("Number of workitems retrieved on Sys_ETB_Hold: " + fetchWorkitemListCount);
			System.out.println("Number of workitems retrieved on Sys_ETB_Hold: " + fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0) {
				for (int i = 0; i <= fetchWorkitemListCount; i++) {
					String fetchWorkItemlistData = xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData = fetchWorkItemlistData.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

					Digital_CC_log.Digital_CC.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+ fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID = xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					Digital_CC_log.Digital_CC.debug("Current ProcessInstanceID: " + processInstanceID);
					Digital_CC_log.Digital_CC.debug("Processing Workitem: " + processInstanceID);

					String WorkItemID = xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					Digital_CC_log.Digital_CC.debug("Current WorkItemID: " + WorkItemID);

					String entryDateTime = xmlParserfetchWorkItemData.getValueOf("IntroductionDATETIME");
					Digital_CC_log.Digital_CC.debug("Current EntryDateTime: " + entryDateTime);

					String DBQuery = "SELECT DCC_WI_No,BSR_WI_No,BSR_UPDATE_TIME,CRN,BSR_WI_Status,FINAL_LIMIT,BSR_WI_CREATE_TIME,CIF FROM NG_DCC_BSR_UPDATE with(nolock) where DCC_WI_No='"
					+ processInstanceID + "'";
					
					String extTabDataINPXML = CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), sessionID);
					Digital_CC_log.Digital_CC.debug("extTabDataIPXML: " + extTabDataINPXML);
					String extTabDataOUPXML = CommonMethods.WFNGExecute(extTabDataINPXML, CommonConnection.getJTSIP(),CommonConnection.getJTSPort(), 1);
					Digital_CC_log.Digital_CC.debug("extTabDataOPXML: " + extTabDataOUPXML);
					
					XMLParser xmlParserDataDB = new XMLParser(extTabDataOUPXML);
					String StrMainCode = xmlParserDataDB.getValueOf("MainCode");
					if (StrMainCode.equals("0")) {
						
						String DCC_WI_No ="";
						String BSR_WI_No ="";
						String BSR_UPDATE_TIME ="";
						String CRN ="";
						String BSR_WI_Status ="";
						String FINAL_LIMIT ="";
						String BSR_WI_CREATE_TIME ="";
						String CIF ="";
						
						DCC_WI_No = xmlParserDataDB.getValueOf("DCC_WI_No");
						BSR_WI_No = xmlParserDataDB.getValueOf("BSR_WI_No");
						BSR_UPDATE_TIME = xmlParserDataDB.getValueOf("BSR_UPDATE_TIME");
						CRN = xmlParserDataDB.getValueOf("CRN");
						BSR_WI_Status = xmlParserDataDB.getValueOf("BSR_WI_Status");
						FINAL_LIMIT = xmlParserDataDB.getValueOf("FINAL_LIMIT");
						BSR_WI_CREATE_TIME = xmlParserDataDB.getValueOf("BSR_WI_CREATE_TIME");
						CIF = xmlParserDataDB.getValueOf("CIF");
						
						Digital_CC_log.Digital_CC.debug("DCC_WI_No: " + DCC_WI_No);
						Digital_CC_log.Digital_CC.debug("BSR_WI_No: " + BSR_WI_No);
						Digital_CC_log.Digital_CC.debug("BSR_UPDATE_TIME: " + BSR_UPDATE_TIME);
						Digital_CC_log.Digital_CC.debug("CRN: " + CRN);
						Digital_CC_log.Digital_CC.debug("FINAL_LIMIT: " + FINAL_LIMIT);
						Digital_CC_log.Digital_CC.debug("BSR_WI_CREATE_TIME: " + BSR_WI_CREATE_TIME);
						Digital_CC_log.Digital_CC.debug("CIF: " + CIF);
						
						SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
						SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

						
						if ("Approve".equalsIgnoreCase(BSR_WI_Status)) {
							String columnName = "CRN,Final_Limit,Decision,CIF";
							String value = "'" + CRN + "','" + FINAL_LIMIT + "','" + BSR_WI_Status + "','" + CIF + "'";
							String sWhereClause = "Wi_Name='" + processInstanceID + "'";
							String extTableIPUpdateXml = CommonMethods.apUpdateInput(cabinetName,sessionID,"NG_DCC_EXTTABLE",columnName,value,sWhereClause);
							Digital_CC_log.Digital_CC.debug("Input XML for apUpdateInput for NG_DCC_EXTTABLE Table : "+ extTableIPUpdateXml);
	
							String extTableOPUpdateXml = CommonMethods.WFNGExecute(extTableIPUpdateXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
							Digital_CC_log.Digital_CC.debug("Output XML for apUpdateInput for NG_DCC_EXTTABLE Table : "+ extTableOPUpdateXml);
	
							XMLParser sXMLParserChild = new XMLParser(extTableOPUpdateXml);
							String StrMainCode_1 = sXMLParserChild.getValueOf("MainCode");
							
							if (StrMainCode_1.equals("0")) {
								
//								SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
//								SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

								Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
								String formattedEntryDatetime = outputDateFormat.format(entryDatetimeFormat);
								Digital_CC_log.Digital_CC.debug("FormattedEntryDatetime: " + formattedEntryDatetime);

								Date actionDateTime = new Date();
								String formattedActionDateTime = outputDateFormat.format(actionDateTime);
								Digital_CC_log.Digital_CC.debug("FormattedActionDateTime: " + formattedActionDateTime);

								// Insert in WIHistory Table.
								
								String columnNames = "WI_NAME,dec_date,WORKSTEP,USER_NAME,DECISION,decision_date_time,Remarks";
								String columnValues = "'" + processInstanceID + "','" + formattedActionDateTime + "','Sys_ETB_Hold','" + CommonConnection.getUsername() + "','" + BSR_WI_Status + "','"
								+ formattedEntryDatetime + "','BSR WI Update'";

								String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionID, columnNames, columnValues, "NG_DCC_GR_DECISION_HISTORY");
								Digital_CC_log.Digital_CC.debug("APInsertInputXML: " + apInsertInputXML);

								String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML,CommonConnection.getJTSIP(),
										CommonConnection.getJTSPort(), 1);
								Digital_CC_log.Digital_CC.debug("APInsertOutputXML: " + apInsertOutputXML);

								XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
								String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
								Digital_CC_log.Digital_CC.debug("Status of apInsertMaincode  " + apInsertMaincode);
								if (apInsertMaincode.equalsIgnoreCase("0")) {
									Digital_CC_log.Digital_CC.debug("ApInsert successful: " + apInsertMaincode);
									
									Calendar calendar = Calendar.getInstance();
									Date today = new Date();
									SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm");
									String DateExtra2 = sdf1.format(today);
									calendar.add(Calendar.SECOND, 30);
									Date addSec = calendar.getTime();
									DateExtra2 = sdf1.format(addSec);
		
									Digital_CC_log.Digital_CC.debug("DateExtra2 today : " + DateExtra2);
									Digital_CC_log.Digital_CC.debug("DateExtra2 addSec  30: " + DateExtra2);
		
									String columnName_1 = "ValidTill";
									String value_1 = "'"+DateExtra2+"'";
									String sWhereClause_1 = "ProcessInstanceID='" + processInstanceID+ "' and  ActivityName='Sys_ETB_Hold' and ValidTill is null";
									String extTableIPUpdateXml_1 = CommonMethods.apUpdateInput(cabinetName, sessionID,"WFINSTRUMENTTABLE", columnName_1, value_1, sWhereClause_1);
									Digital_CC_log.Digital_CC.debug("Input XML for apUpdateInput for WFINSTRUMENTTABLE Table : "+ extTableIPUpdateXml_1);
									
									String extTableOPUpdateXml_1 = CommonMethods.WFNGExecute(extTableIPUpdateXml_1,CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
									Digital_CC_log.Digital_CC.debug("Output XML for apUpdateInput for WFINSTRUMENTTABLE Table : "+ extTableOPUpdateXml_1);
									
									XMLParser sXMLParserChild_1 = new XMLParser(extTableOPUpdateXml_1);
									String StrMainCode_2 = sXMLParserChild_1.getValueOf("MainCode");
		
									if (StrMainCode_2.equals("0")) {
										Digital_CC_log.Digital_CC.info("Update to WFINSTRUMENTTABLE");
									} else {
										Digital_CC_log.Digital_CC.info("Update Failed to WFINSTRUMENTTABLE");
									}
									Digital_CC_log.Digital_CC.debug("Inserted in WiHistory table successfully.");
								} else {
									Digital_CC_BRSCAMTemplate obj = new Digital_CC_BRSCAMTemplate(Digital_CC_log.Digital_CC);
									obj.sendMail(cabinetName,sessionID,processInstanceID,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),"","",ProcessDefId,"");
									
									Digital_CC_log.Digital_CC.error("ApInsert failed: " + apInsertMaincode);
								}
							} else {
								
								Digital_CC_BRSCAMTemplate obj = new Digital_CC_BRSCAMTemplate(Digital_CC_log.Digital_CC);
								obj.sendMail(cabinetName,sessionID,processInstanceID,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),"","",ProcessDefId,"");
								
								Digital_CC_log.Digital_CC.info("Update Failed to NG_DCC_EXTTABLE");
							}
						}
						else if("Reject".equalsIgnoreCase(BSR_WI_Status)){
							
							String columnName = "Decision";
							String value = "'"+BSR_WI_Status+"'";
							String sWhereClause = "Wi_Name='" + processInstanceID + "'";
							String extTableIPUpdateXml = CommonMethods.apUpdateInput(cabinetName,sessionID, "NG_DCC_EXTTABLE",columnName,value,sWhereClause);
							Digital_CC_log.Digital_CC.debug("Input XML for apUpdateInput for NG_DCC_EXTTABLE Table : " + extTableIPUpdateXml);
	
							String extTableOPUpdateXml = CommonMethods.WFNGExecute(extTableIPUpdateXml,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(), 1);
							Digital_CC_log.Digital_CC.debug("Output XML for apUpdateInput for NG_DCC_EXTTABLE Table : " + extTableOPUpdateXml);
	
							XMLParser sXMLParserChild = new XMLParser(extTableOPUpdateXml);
							String StrMainCode_3 = sXMLParserChild.getValueOf("MainCode");
							
							if (StrMainCode_3.equals("0"))
							{
								Calendar calendar = Calendar.getInstance();
								Date today = new Date();
								SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm");
								String DateExtra2 = sdf1.format(today);
								calendar.add(Calendar.SECOND,30);
								Date addSec = calendar.getTime();
								DateExtra2 = sdf1.format(addSec);
								
								Digital_CC_log.Digital_CC.debug("DateExtra2 today : " + DateExtra2);
								Digital_CC_log.Digital_CC.debug("DateExtra2 addSec  30: " + DateExtra2);
								
								//rubi
								Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
								String formattedEntryDatetime = outputDateFormat.format(entryDatetimeFormat);
								Digital_CC_log.Digital_CC.debug("FormattedEntryDatetime: " + formattedEntryDatetime);

								Date actionDateTime = new Date();
								String formattedActionDateTime = outputDateFormat.format(actionDateTime);
								Digital_CC_log.Digital_CC.debug("FormattedActionDateTime: " + formattedActionDateTime);

								// Insert in WIHistory Table.
								
								String columnNames = "WI_NAME,dec_date,WORKSTEP,USER_NAME,DECISION,decision_date_time,Remarks";
								String columnValues = "'" + processInstanceID + "','" + formattedActionDateTime + "','Sys_ETB_Hold','" + CommonConnection.getUsername() + "','" + BSR_WI_Status + "','"
								+ formattedEntryDatetime + "','BSR WI Update'";

								String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionID, columnNames, columnValues, "NG_DCC_GR_DECISION_HISTORY");
								Digital_CC_log.Digital_CC.debug("APInsertInputXML: " + apInsertInputXML);

								String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML,CommonConnection.getJTSIP(),
										CommonConnection.getJTSPort(), 1);
								Digital_CC_log.Digital_CC.debug("APInsertOutputXML: " + apInsertOutputXML);

								XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
								String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
								Digital_CC_log.Digital_CC.debug("Status of apInsertMaincode  " + apInsertMaincode);

								if (apInsertMaincode.equalsIgnoreCase("0")) {
									
									Digital_CC_log.Digital_CC.debug("ApInsert successful: " + apInsertMaincode);
									Digital_CC_log.Digital_CC.debug("DateExtra2 today : " + DateExtra2);
									Digital_CC_log.Digital_CC.debug("DateExtra2 addSec  30: " + DateExtra2);						
							
								String columnName_1 = "ValidTill";
								String value_1 =  "'"+DateExtra2+"'";
								String sWhereClause_1 = "ProcessInstanceID='"+processInstanceID+"' and  ActivityName='Sys_ETB_Hold' and ValidTill is null ";
								String extTableIPUpdateXml_1 = CommonMethods.apUpdateInput(cabinetName,sessionID,"WFINSTRUMENTTABLE",columnName_1,value_1, sWhereClause_1);
								Digital_CC_log.Digital_CC.debug("Input XML for apUpdateInput for WFINSTRUMENTTABLE Table : " + extTableIPUpdateXml_1);
	
								String extTableOPUpdateXml_1 = CommonMethods.WFNGExecute(extTableIPUpdateXml_1,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(), 1);
								Digital_CC_log.Digital_CC.debug("Output XML for apUpdateInput for WFINSTRUMENTTABLE Table : " + extTableOPUpdateXml_1);
	
								XMLParser sXMLParserChild_1 = new XMLParser(extTableOPUpdateXml_1);
								String StrMainCode_4 = sXMLParserChild_1.getValueOf("MainCode");
	
								if (StrMainCode_4.equals("0")){
									Digital_CC_log.Digital_CC.info("Update to WFINSTRUMENTTABLE");
								}
								else{
									Digital_CC_log.Digital_CC.info("Update Failed to WFINSTRUMENTTABLE");
								}
							}else{
								Digital_CC_BRSCAMTemplate obj = new Digital_CC_BRSCAMTemplate(Digital_CC_log.Digital_CC);
								obj.sendMail(cabinetName,sessionID,processInstanceID,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),"","",ProcessDefId,"");
								Digital_CC_log.Digital_CC.info("Update in NG_DCC_BSR_UPDATE not arrived for WI: "+processInstanceID);
								
							}//rubi
							}else{
								Digital_CC_BRSCAMTemplate obj = new Digital_CC_BRSCAMTemplate(Digital_CC_log.Digital_CC);
								obj.sendMail(cabinetName,sessionID,processInstanceID,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),"","",ProcessDefId,"");
								Digital_CC_log.Digital_CC.info("Update in NG_DCC_BSR_UPDATE not arrived for WI: "+processInstanceID);
								
							}
						} else {
							Digital_CC_log.Digital_CC.info("Update in NG_DCC_BSR_UPDATE not arrived for WI: "+processInstanceID);
						}
					}
					else{
						Digital_CC_log.Digital_CC.info("Select from NG_DCC_BSR_UPDATE failed for WI: "+processInstanceID);
					}
					if(i == 99){
						String lastProcessInstanceId=processInstanceID;
						String lastWorkItemId=WorkItemID;
						String CreationDateTime=entryDateTime;
						fetchWorkitemListInputXML = CommonMethods.getFetchWorkItemsInputXML(lastProcessInstanceId, lastWorkItemId, sessionId, cabinetName, queueID,CreationDateTime);
						Digital_CC_log.Digital_CC.debug("InputXML for fetchWorkList Call: " + fetchWorkitemListInputXML);

						fetchWorkitemListOutputXML = CommonMethods.WFNGExecute(fetchWorkitemListInputXML, sJtsIp, iJtsPort,1);
						Digital_CC_log.Digital_CC.debug("WMFetchWorkList OutputXML: " + fetchWorkitemListOutputXML);

						xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

						fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
						Digital_CC_log.Digital_CC.debug("FetchWorkItemListMainCode: " + fetchWorkItemListMainCode);

						fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
						Digital_CC_log.Digital_CC.debug("RetrievedCount for WMFetchWorkList Call: " + fetchWorkitemListCount);
						Digital_CC_log.Digital_CC.debug("Number of workitems retrieved on Sys_ETB_Hold: " + fetchWorkitemListCount);
						System.out.println("Number of workitems retrieved on Sys_ETB_Hold: " + fetchWorkitemListCount);
						i=0;
					}
				}
			}
		} catch (Exception e) {
			Digital_CC_log.Digital_CC.debug("Exception in ETB HOLD: " + e.getMessage());
		}
	}

	private int readConfig()
	{
		Properties p = null;
		try {
			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir") + File.separator + "ConfigFiles"
					+ File.separator + "DCC_BSRCAM_Generate_Logger.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				configParamMap.put(name, p.getProperty(name));
			}
		} catch (Exception e) {
			return -1;
		}
		return 0;
	}
}
