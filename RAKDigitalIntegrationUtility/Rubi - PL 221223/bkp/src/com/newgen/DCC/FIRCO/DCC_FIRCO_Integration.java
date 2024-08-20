/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK
Application				: RAK Utility
Module					: FIRCO Status
File Name				: DCC_FIRCO_Integration.java
Author 					: Ravindra Kumar
Date (DD/MM/YYYY)		: 12/07/2022

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/

package com.newgen.DCC.FIRCO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.newgen.DCC.EFMS.DCC_EFMS_IntegrationLog;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;

public class DCC_FIRCO_Integration implements Runnable {

	private static NGEjbClient ngEjbClient;

	static Map<String, String> DCCFIRCOIntegrationmap = new HashMap<String, String>();

	@Override
	public void run() {
		String sessionID = "";
		String cabinetName = "";
		String jtsIP = "";
		String jtsPort = "";
		String queueID = "";
		int socketConnectionTimeout = 0;
		int integrationWaitTime = 0;
		int sleepIntervalInMin = 0;

		try {
			DCC_FIRCO_IntegrationLog.setLogger();
			ngEjbClient = NGEjbClient.getSharedInstance();

			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("configReadStatus " + configReadStatus);
			if (configReadStatus != 0) {
				DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.error("Could not Read Config Properties");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("JTSPORT: " + jtsPort);

			queueID = DCCFIRCOIntegrationmap.get("queueID");
			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("QueueID: " + queueID);

			socketConnectionTimeout = Integer.parseInt(DCCFIRCOIntegrationmap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("SocketConnectionTimeOut: " + socketConnectionTimeout);

			integrationWaitTime = Integer.parseInt(DCCFIRCOIntegrationmap.get("INTEGRATION_WAIT_TIME"));
			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("IntegrationWaitTime: " + integrationWaitTime);

			sleepIntervalInMin = Integer.parseInt(DCCFIRCOIntegrationmap.get("SleepIntervalInMin"));
			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("SleepIntervalInMin: " + sleepIntervalInMin);

			sessionID = CommonConnection.getSessionID(DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger, false);

			if (sessionID.trim().equalsIgnoreCase("")) {
				DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Could Not Connect to Server!");
			} else {
				DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Session ID found: " + sessionID);
				HashMap<String, String> socketDetailsMap = socketConnectionDetails(cabinetName, jtsIP, jtsPort, sessionID);
				while (true) {
					DCC_FIRCO_IntegrationLog.setLogger();
					DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug(" Firco ...123.");
					startFircoUtility(cabinetName, jtsIP, jtsPort, sessionID, queueID, socketConnectionTimeout, integrationWaitTime, socketDetailsMap,"","","");
					System.out.println("No More workitems to Process, Sleeping!");
					Thread.sleep(sleepIntervalInMin * 60 * 1000);
				}
			}
		}

		catch (Exception e) {
			e.printStackTrace();
			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.error("Exception Occurred in  FircoHold : " + e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.error("Exception Occurred in  FircoHold : " + result);
		}
	}

	private int readConfig() {
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir") + File.separator + "ConfigFiles" + File.separator + "DCC_FIRCO_Integration_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				DCCFIRCOIntegrationmap.put(name, p.getProperty(name));
			}
		} catch (Exception e) {
			return -1;
		}
		return 0;
	}

	private void startFircoUtility(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String queueID, int socketConnectionTimeOut, int integrationWaitTime,
			HashMap<String, String> socketDetailsMap,String processInstanceId, String lastWorkItemId,String entryDateTime_str) {
		//final String ws_name = "Firco_Clearance";
		final String NG_RLOS_FIRCO = "NG_RLOS_FIRCO";
		final String NG_DCC_EXTTABLE = "NG_DCC_EXTTABLE";
		String entryDateTime="";

		try {
			// Validate Session ID
			sessionId = CommonConnection.getSessionID(DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger, false);

			if (sessionId == null || sessionId.equalsIgnoreCase("") || sessionId.equalsIgnoreCase("null")) {
				DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.error("Could Not Get Session ID " + sessionId);
				return;
			}

			// Fetch all Work-Items on given queueID.
			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Fetching all Workitems on Firco_Clearance queue");
			System.out.println("Fetching all Workitems on Firco_Clearance queue");
			//String fetchWorkitemListInputXML = CommonMethods.fetchWorkItemsInput(cabinetName, sessionId, queueID);
			String fetchWorkitemListInputXML = CommonMethods.getFetchWorkItemsInputXML(processInstanceId, lastWorkItemId,  sessionId, cabinetName, queueID,entryDateTime_str);
			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("InputXML for fetchWorkList Call: " + fetchWorkitemListInputXML);
			
			

			String fetchWorkitemListOutputXML = CommonMethods.WFNGExecute(fetchWorkitemListInputXML, sJtsIp, iJtsPort, 1);

			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("WMFetchWorkList OutputXML: " + fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("FetchWorkItemListMainCode: " + fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("RetrievedCount for WMFetchWorkList Call: " + fetchWorkitemListCount);

			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Number of workitems retrieved on Firco_Clearance: " + fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on Firco_Clearance: " + fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0) {
				for (int i = 0; i < fetchWorkitemListCount; i++) {
					String fetchWorkItemlistData = xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData = fetchWorkItemlistData.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

					DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: " + fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID = xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Current ProcessInstanceID: " + processInstanceID);

					DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Processing Workitem: " + processInstanceID);
					System.out.println("\nProcessing Workitem: " + processInstanceID);

					String WorkItemID = xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Current WorkItemID: " + WorkItemID);

					entryDateTime = xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Current EntryDateTime: " + entryDateTime);

					String ActivityName = xmlParserfetchWorkItemData.getValueOf("ActivityName");
					DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("ActivityName: " + ActivityName);

					String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("ActivityID: " + ActivityID);
					String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("ActivityType: " + ActivityType);
					String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("ProcessDefId: " + ProcessDefId);
					if(i==99){
						processInstanceId = processInstanceID;
						lastWorkItemId = WorkItemID;
					}

					String DBQuery = "SELECT Prospect_id, wi_name FROM NG_DCC_EXTTABLE with(nolock) WHERE WI_NAME='" + processInstanceID + "'";
			        
			        String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger, false));
			        DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("extTabDataIPXML: " + extTabDataIPXML);
			        String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
			        DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("extTabDataOPXML: " + extTabDataOPXML);

			        XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
			        int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
			        if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0) {
						String xmlDataExtTab = xmlParserData.getNextValueOf("Record");
						xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

						NGXmlList objWorkList = xmlParserData.createList("Records", "Record");
						DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Prospect_id: " + objWorkList.getVal("Prospect_id"));
						DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("wi_name: " + objWorkList.getVal("wi_name"));

						String DBQuery2 = "SELECT Firco_ID,Workstep_name,Call_type,StatusBehavior,StatusName, isnull(Remarks,'') as Remarks" + " FROM " + NG_RLOS_FIRCO + " WITH (NOLOCK) "
								+ "Where Workitem_no = '" + processInstanceID + "' and Firco_ID = '" + objWorkList.getVal("Prospect_id") + "'";

						String FircoTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery2, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger, false));
						DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug(" NG_RLOS_FIRCO IPXML: " + FircoTabDataIPXML);
						String FircoTabDataOPXML = CommonMethods.WFNGExecute(FircoTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
						DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug(" NG_RLOS_FIRCO OPXML: " + FircoTabDataOPXML);

						XMLParser xmlParserData2 = new XMLParser(FircoTabDataOPXML);
						int Totalrec2 = Integer.parseInt(xmlParserData2.getValueOf("TotalRetrieved"));

						if (xmlParserData2.getValueOf("MainCode").equalsIgnoreCase("0") && Totalrec2 > 0) {
							NGXmlList objWorkList2 = xmlParserData2.createList("Records", "Record");
							for (; objWorkList2.hasMoreElements(true); objWorkList2.skip(true)) {
								String StatusName = objWorkList2.getVal("StatusName");
								String StatusBehavior = objWorkList2.getVal("StatusBehavior");
								String decisionValue = "";
								DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("StatusName : " + StatusName);
								DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("StatusBehavior : " + StatusBehavior);
								DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Firco_ID : " + objWorkList.getVal("Firco_ID"));
								/* when StatusBehavior is
								0000 � Move the case forward 
								FFF002 for QALERT , wait for the case 
								FFF_OK for QDECISION if the record returns no alert or if it is authorized in the front-end (false status). - Move the case forward 
								FFFBAD for QDECISION if the status of the record in the database is true. - Move the case forward and reject the case 
								FFFPEN for QDECISION if the status of the record in the database is new or pending. - wait for the case 
								FFFERR - In case of Input buffer has some errors. � Some error reported, wait for the case */

								if (StatusBehavior.equalsIgnoreCase("0000") || StatusBehavior.equalsIgnoreCase("FFF_OK") || StatusBehavior.equalsIgnoreCase("FFFBAD")) 
								{
									DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("StatusBehavior : " + StatusBehavior);
									String ErrDesc = "";
									String attributesTag = "";
									String fircoFlag = "Y";
									if (StatusBehavior.equalsIgnoreCase("FFFBAD")) 
									{
										decisionValue = "Reject";
										fircoFlag = "CB";
										ErrDesc = "Confirm Bad";
										
									} 
									else if (StatusBehavior.equalsIgnoreCase("0000") || StatusBehavior.equalsIgnoreCase("FFF_OK"))
									{
										decisionValue = "Approve";
										fircoFlag = "FP";
										ErrDesc = "False Positive";
											
									}
									DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Decision" + decisionValue);
									attributesTag = "<Decision>" + decisionValue + "</Decision>";

									String columnNames1 = "Newgen_status";
									String columnValues1 = "'Done'";
									String sWhereClause = "Workitem_no = '" + processInstanceID + "' and Firco_ID = '" + objWorkList.getVal("Prospect_id") + "'";

									String extTableIPUpdateXml = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger, false), NG_RLOS_FIRCO, columnNames1, columnValues1, sWhereClause);
									DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Input XML for apUpdateInput for " + NG_RLOS_FIRCO + " Table : " + extTableIPUpdateXml);

									String extTableOPUpdateXml = CommonMethods.WFNGExecute(extTableIPUpdateXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
									DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Output XML for apUpdateInput for " + NG_RLOS_FIRCO + " Table : " + extTableOPUpdateXml);

									XMLParser sXMLParserChild = new XMLParser(extTableOPUpdateXml);
									String StrMainCode = sXMLParserChild.getValueOf("MainCode");

									if (StrMainCode.equals("0"))
										DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Successful in apUpdateInput the record in : " + NG_RLOS_FIRCO);
									else {
										DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Error in Executing apUpdateInput sOutputXML : " + extTabDataOPXML);
										System.out.println("WMgetWorkItemCall failed: "+processInstanceID);
										DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("WMgetWorkItemCall failed: "+processInstanceID);
									}
									
									
									DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("decisionValue : " + decisionValue);
									// All Firco Hits are cleared
									
									DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("attributesTag: " + attributesTag);
									DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("ErrDesc: " + ErrDesc);
									
									// Lock Workitem.
									String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID, WorkItemID);
									String getWorkItemOutputXml = CommonMethods.WFNGExecute(getWorkItemInputXML, sJtsIp, iJtsPort, 1);
									DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Output XML For WmgetWorkItemCall: " + getWorkItemOutputXml);

									XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
									String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
									DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("WmgetWorkItemCall Maincode:  " + getWorkItemMainCode);

									if (getWorkItemMainCode.trim().equals("0")) {
										
										DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("WMgetWorkItemCall Successful: " + getWorkItemMainCode);

										String columnNames2 = "FIRCO_Status,FIRCO_Flag,Decision";
										String columnValues2 = "'DONE','"+ fircoFlag+"','"+ decisionValue+"'";
										
										String sWhereClause2 = "Wi_Name = '" + processInstanceID + "' and Prospect_id = '" + objWorkList.getVal("Prospect_id") + "'";

										String extTableIPUpdateXml1 = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger, false), NG_DCC_EXTTABLE, columnNames2, columnValues2, sWhereClause2);
										DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Input XML for apUpdateInput for " + NG_DCC_EXTTABLE + " Table : " + extTableIPUpdateXml1);

										String extTableOPUpdateXml1 = CommonMethods.WFNGExecute(extTableIPUpdateXml1, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
										DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Output XML for apUpdateInput for " + NG_DCC_EXTTABLE + " Table : " + extTableOPUpdateXml1);

										XMLParser sXMLParserChild1 = new XMLParser(extTableOPUpdateXml1);
										String StrMainCode1 = sXMLParserChild1.getValueOf("MainCode");

										if (StrMainCode1.equals("0")){
										
											DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Successful in apUpdateInput the record in : " + "NG_DCC_EXTTABLE");
										
											String assignWorkitemAttributeInputXML = "<?xml version=\"1.0\"?><WMAssignWorkItemAttributes_Input>" 
													+ "<Option>WMAssignWorkItemAttributes</Option>" 
													+ "<EngineName>" + cabinetName + "</EngineName>" 
													+ "<SessionId>" + sessionId + "</SessionId>" 
													+ "<ProcessInstanceId>" + processInstanceID + "</ProcessInstanceId>" 
													+ "<WorkItemId>" + WorkItemID + "</WorkItemId>" 
													+ "<ActivityId>" + ActivityID + "</ActivityId>" 
													+ "<ProcessDefId>" + ProcessDefId + "</ProcessDefId>"
													+ "<LastModifiedTime></LastModifiedTime>" 
													+ "<ActivityType>" + ActivityType + "</ActivityType>" 
													+ "<complete>D</complete>" 
													+ "<AuditStatus></AuditStatus>"
													+ "<Comments></Comments>" 
													+ "<UserDefVarFlag>Y</UserDefVarFlag>" 
													+ "<Attributes>" + attributesTag + "</Attributes>" 
													+ "</WMAssignWorkItemAttributes_Input>";
	
											DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("InputXML for assignWorkitemAttribute Call: " + assignWorkitemAttributeInputXML);
	
											String assignWorkitemAttributeOutputXML = CommonMethods.WFNGExecute(assignWorkitemAttributeInputXML, sJtsIp, iJtsPort, 1);
	
											DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("OutputXML for assignWorkitemAttribute Call: " + assignWorkitemAttributeOutputXML);
	
											XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
											String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
											DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("AssignWorkitemAttribute MainCode: " + assignWorkitemAttributeMainCode);
	
											if (assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0")) {
												DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("AssignWorkitemAttribute Successful: " + assignWorkitemAttributeMainCode);
	
												// Move Workitem to next Workstep
	
												System.out.println(processInstanceID + "Complete Succesfully with status " + decisionValue);
	
												DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("WorkItem moved to next Workstep.");
	
												SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
												SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");
	
												Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
												String formattedEntryDatetime = outputDateFormat.format(entryDatetimeFormat);
												DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("FormattedEntryDatetime: " + formattedEntryDatetime);
	
												Date actionDateTime = new Date();
												String formattedActionDateTime = outputDateFormat.format(actionDateTime);
												DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("FormattedActionDateTime: " + formattedActionDateTime);
	
												// Insert in WIHistory Table. wi_name, workstep, Decision, decision_date_time, Remarks, user_name, dec_date
												String columnNames = "WI_NAME,dec_date,WORKSTEP,USER_NAME,DECISION,ENTRY_DATE_TIME,REMARKS";
												String columnValues = "'" + processInstanceID + "','" + formattedActionDateTime + "','" + ActivityName + "'," + "'System','" + decisionValue + "','"
														+ formattedEntryDatetime + "','" + ErrDesc + "'";
	
												String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionId, columnNames, columnValues, "NG_DCC_GR_DECISION_HISTORY");
												DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("APInsertInputXML: " + apInsertInputXML);
	
												String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, sJtsIp, iJtsPort, 1);
												DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("APInsertOutputXML: " + apInsertInputXML);
	
												XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
												String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
												DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Status of apInsertMaincode  " + apInsertMaincode);
	
												if (apInsertMaincode.equalsIgnoreCase("0")) {
													DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("ApInsert successful: " + apInsertMaincode);
												} else {
													DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("ApInsert failed: " + apInsertMaincode);
												}
											} else {
												DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("AssignWorkitemAttribute failed: " + assignWorkitemAttributeMainCode);
											}
										} else {
											DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Error in Executing apUpdateInput sOutputXML : " + extTableOPUpdateXml1);
											System.out.println("WMgetWorkItemCall failed: "+processInstanceID);
											DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("WMgetWorkItemCall failed: "+processInstanceID);
										}
											
									} else {
										DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("WmgetWorkItem failed: " + getWorkItemMainCode);
									}
								} else {
									DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Firco Hits are pending for WI : " + processInstanceID);
								}
							}
						}
					} else
						DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Data in Apselect " + xmlParserData.getValueOf("MainCode") + " for the table : " + NG_RLOS_FIRCO + " with " + iTotalrec + " records");
				}
			}
			if(fetchWorkitemListCount>99){
				DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Inside if condition to fech next set of cases post 100 processInstanceId: "+processInstanceId+ " lastWorkItemId: " + lastWorkItemId );
				startFircoUtility(cabinetName, sJtsIp, iJtsPort, sessionId, queueID, socketConnectionTimeOut, integrationWaitTime, socketDetailsMap,processInstanceId,lastWorkItemId,entryDateTime);
			}
		} catch (Exception e) {
			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Exception: " + e.getMessage());
		}
	}

	private HashMap<String, String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort, String sessionID) {
		HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

		try {
			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Fetching Socket Connection Details.");
			System.out.println("Fetching Socket Connection Details.");

			String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'RAK' and CallingSource = 'Utility'";

			String socketDetailsInputXML = CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Socket Details APSelect InputXML: " + socketDetailsInputXML);

			String socketDetailsOutputXML = CommonMethods.WFNGExecute(socketDetailsInputXML, sJtsIp, iJtsPort, 1);
			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Socket Details APSelect OutputXML: " + socketDetailsOutputXML);

			XMLParser xmlParserSocketDetails = new XMLParser(socketDetailsOutputXML);
			String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("SocketDetailsMainCode: " + socketDetailsMainCode);

			int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("SocketDetailsTotalRecords: " + socketDetailsTotalRecords);

			if (socketDetailsMainCode.equalsIgnoreCase("0") && socketDetailsTotalRecords > 0) {
				String xmlDataSocketDetails = xmlParserSocketDetails.getNextValueOf("Record");
				xmlDataSocketDetails = xmlDataSocketDetails.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

				XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

				String socketServerIP = xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
				DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("SocketServerIP: " + socketServerIP);
				socketDetailsMap.put("SocketServerIP", socketServerIP);

				String socketServerPort = xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
				DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("SocketServerPort " + socketServerPort);
				socketDetailsMap.put("SocketServerPort", socketServerPort);

				DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("SocketServer Details found.");
				System.out.println("SocketServer Details found.");

			}
		} catch (Exception e) {
			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Exception in getting Socket Connection Details: " + e.getMessage());
			System.out.println("Exception in getting Socket Connection Details: " + e.getMessage());
		}

		return socketDetailsMap;
	}

	protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort, int flag) throws IOException, Exception {
		DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("In WF NG Execute : " + serverPort);
		try {
			if (serverPort.startsWith("33"))
				return WFCallBroker.execute(ipXML, jtsServerIP, Integer.parseInt(serverPort), 1);
			else
				return ngEjbClient.makeCall(jtsServerIP, serverPort, "WebSphere", ipXML);
		} catch (Exception e) {
			DCC_FIRCO_IntegrationLog.DCC_FIRCOIntegrationLogger.debug("Exception Occured in WF NG Execute : " + e.getMessage());
			e.printStackTrace();
			return "Error";
		}
	}
}
