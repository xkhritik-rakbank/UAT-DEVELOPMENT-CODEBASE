package com.newgen.DPL.awb;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import com.newgen.DAO.AWB.DAO_AWB_Log;
import com.newgen.DPL.Digital_PL_CommomMethod;
import com.newgen.DPL.Digital_PL_Log;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.omni.wf.util.excp.NGException;

public class AWB extends Digital_PL_Log {

	private static String queueID = null;
	private static NGEjbClient ngEjbClient;
	private static org.apache.log4j.Logger logger;
	private static NGEjbClient ngEjbClientCIFVer;
	public static String MailStr = "";
	public static String fromMailID = "";
	public static String toMailID = "";
	public static String mailSubject = "";
	public String sessionID = "";
	
	
	public AWB() throws NGException {

		Digital_PL_Log.setLogger(getClass().getSimpleName());
		this.ngEjbClient = NGEjbClient.getSharedInstance();
		logger = Digital_PL_Log.getLogger(getClass().getSimpleName());
	}

	public void startDPL_AWB_Gen(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String queueID,
			int socketConnectionTimeOut, int integrationWaitTime, HashMap<String, String> socketDetailsMap) {
		String ws_name = "AWB_Generation";

		try {
			sessionID = CommonConnection.getSessionID(logger, false);
			if (sessionID == null || sessionID.equalsIgnoreCase("") || sessionID.equalsIgnoreCase("null")) {
				logger.error("Could Not Get Session ID " + sessionID);
				return;
			}
			logger.debug("Fetching all Workitems for AWB DPL ");
			System.out.println("Fetching all Workitems on queue");

			String fetchWorkitemListInputXML = CommonMethods.fetchWorkItemsInput(cabinetName, sessionID, queueID);
			logger.debug("InputXML for fetchWorkList Call: " + fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML = CommonMethods.WFNGExecute(fetchWorkitemListInputXML, sJtsIp, iJtsPort,
					1);
			logger.debug("WMFetchWorkList DPL_AWB_Gen OutputXML: " + fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);
			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			logger.debug("FetchWorkItemListMainCode: " + fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			logger.debug("RetrievedCount for WMFetchWorkList Call: " + fetchWorkitemListCount);
			logger.debug("Number of workitems retrieved on DPL_AWB_Gen: " + fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on DPL_AWB_Gen: " + fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0) {
				for (int i = 0; i < fetchWorkitemListCount; i++) {
					final HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();

					String fetchWorkItemlistData = xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData = fetchWorkItemlistData.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

					logger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML DPL: " + fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID = xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					logger.debug("Current ProcessInstanceID DPL: " + processInstanceID);

					logger.debug("Processing Workitem DPL: " + processInstanceID);
					System.out.println("\nProcessing Workitem: " + processInstanceID);

					String WorkItemID = xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					logger.debug("Current WorkItemID DPL: " + WorkItemID);

					String entryDateTime = xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					logger.debug("Current EntryDateTime DPL: " + entryDateTime);

					String ActivityName = xmlParserfetchWorkItemData.getValueOf("ActivityName");
					logger.debug("ActivityName DPL: " + ActivityName);

					String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					logger.debug("ActivityID DPL: " + ActivityID);
					String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					logger.debug("ActivityType DPL: " + ActivityType);
					String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					logger.debug("ProcessDefId DPL: " + ProcessDefId);

					// Added on 301122 for Validating Reschueled case:.
					String decisionValue = "";
//					String DBQuer_validateCase = "";
//					String DBQuer_RescheduleFlag = "SELECT CardOps_Reschedule from NG_DPL_EXTTABLE with (NOLOCK ) where WIName='"
//							+ processInstanceID + "'";
//					String extTabDataIPXML_RescheduleFlag = CommonMethods.apSelectWithColumnNames(DBQuer_RescheduleFlag,
//							CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false));
//					logger.debug("extTabDataIPXML DPL: " + extTabDataIPXML_RescheduleFlag);
//					String extTabDataOPXML_RescheduleFlag = CommonMethods.WFNGExecute(extTabDataIPXML_RescheduleFlag,
//							CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
//					logger.debug("extTabDataOPXML DPL: " + extTabDataOPXML_RescheduleFlag);
//					XMLParser xmlParserData_RescheduleFlag = new XMLParser(extTabDataOPXML_RescheduleFlag);
//					if (!xmlParserData_RescheduleFlag.getValueOf("CardOps_Reschedule").equals("Y")) {
//						DBQuer_validateCase = "select WI_name from NG_DPL_EXTTABLE with (NOLOCK ) where WIName='"
//								+ processInstanceID
//								+ "' and Courier_Flag='File_received' and (AWB_Number='' or AWB_Number is null);";
//					} else {
//						DBQuer_validateCase = "select WI_name from NG_DPL_EXTTABLE with (NOLOCK ) where WIName='"
//								+ processInstanceID + "';";
//					}
					// End
					
					String DBQuer_validateCase = "select WIName from NG_DPL_EXTTABLE with (NOLOCK ) where WIName='"
							+ processInstanceID + "';";
					String extTabDataIPXML_validateCase = CommonMethods.apSelectWithColumnNames(DBQuer_validateCase,
							CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false));
					logger.debug("extTabDataIPXML DPL: " + extTabDataIPXML_validateCase);
					String extTabDataOPXML_validateCase = CommonMethods.WFNGExecute(extTabDataIPXML_validateCase,
							CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
					logger.debug("extTabDataOPXML DPL: " + extTabDataOPXML_validateCase);
					XMLParser xmlParserData_validateCase = new XMLParser(extTabDataOPXML_validateCase);

					int iTotalrec_validateCase = Integer
							.parseInt(xmlParserData_validateCase.getValueOf("TotalRetrieved"));

					if (xmlParserData_validateCase.getValueOf("MainCode").equalsIgnoreCase("0")
							&& iTotalrec_validateCase > 0) {
						String Account_creation_date = "";
						String DBQuery = "select EmployerName as 'emp_name',MobileNo as 'mobNo',WIname as 'Wi_no',FirstName as 'FirstName',LastName as 'last_name',AccountCreationDate from NG_DPL_EXTTABLE with (NOLOCK) where WIname='"
								+ processInstanceID + "'";

						String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,
								CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false));
						logger.debug("extTabDataIPXML DPL: " + extTabDataIPXML);

						String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(),
								CommonConnection.getJTSPort(), 1);
						logger.debug("extTabDataOPXML DPL: " + extTabDataOPXML);

						XMLParser xmlParserData = new XMLParser(extTabDataOPXML);

						int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));

						if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0) {
							String xmlDataExtTab = xmlParserData.getNextValueOf("Record");
							xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

							NGXmlList objWorkList = xmlParserData.createList("Records", "Record");

							// loop over the map to put value key pair.
							for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {

								CheckGridDataMap.put("emp_name", objWorkList.getVal("emp_name"));
								CheckGridDataMap.put("mobNo", objWorkList.getVal("mobNo"));
								CheckGridDataMap.put("Wi_no", objWorkList.getVal("Wi_no"));
								CheckGridDataMap.put("first_name", objWorkList.getVal("FirstName"));
								CheckGridDataMap.put("last_name", objWorkList.getVal("last_name"));
								// CheckGridDataMap.put("employement_type",
								// objWorkList.getVal("employement_type"));
								Account_creation_date = objWorkList.getVal("AccountCreationDate");
							}
						}

						String DBQuery_add = "select top 1 HouseApartmentNo as 'Flat_No',BuildingApartmentName as 'Building_name',StreetLocation as 'street_loc', CityTown as 'Emirate_City_address' from NG_DPL_GR_DemographicDetails with (NOLOCK ) where WIName='"
								+ processInstanceID + "'";

						String extTabDataIPXML_1 = CommonMethods.apSelectWithColumnNames(DBQuery_add,
								CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false));
						logger.debug("extTabDataIPXML_1: " + extTabDataIPXML_1);
						String extTabDataOPXML_1 = CommonMethods.WFNGExecute(extTabDataIPXML_1,
								CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
						logger.debug("extTabDataOPXML_2: " + extTabDataOPXML_1);

						XMLParser xmlParserData_1 = new XMLParser(extTabDataOPXML_1);

						int iTotalrec_1 = Integer.parseInt(xmlParserData_1.getValueOf("TotalRetrieved"));

						if (xmlParserData_1.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_1 > 0) {
							String xmlDataExtTab = xmlParserData_1.getNextValueOf("Record");
							xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

							NGXmlList objWorkList = xmlParserData_1.createList("Records", "Record");

							// loop over the map to put value key pair.
							for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
								CheckGridDataMap.put("Flat_No", objWorkList.getVal("Flat_No"));
								CheckGridDataMap.put("Building_name", objWorkList.getVal("Building_name"));
								CheckGridDataMap.put("street_loc", objWorkList.getVal("street_loc"));
								CheckGridDataMap.put("Emirate_City_address",
										objWorkList.getVal("Emirate_City_address"));
							}
						}

						String fileLocation = new StringBuffer().append(System.getProperty("user.dir"))
								.append(System.getProperty("file.separator")).append("DPL_Integration")
								.append(System.getProperty("file.separator")).append("AWB.txt").toString();

						BufferedReader sbf = new BufferedReader(new FileReader(fileLocation));

						StringBuilder sb = new StringBuilder();
						String line = sbf.readLine();
						while (line != null) {
							sb.append(line);
							sb.append(System.lineSeparator());
							line = sbf.readLine();
						}

						String Full_name = CheckGridDataMap.get("first_name").trim() + " "
								+ CheckGridDataMap.get("last_name").trim();
						String ToAddress = CheckGridDataMap.get("Flat_No").trim() + " "
								+ CheckGridDataMap.get("Building_name").trim() + " "
								+ CheckGridDataMap.get("street_loc").trim();
						String Tostreet = CheckGridDataMap.get("Emirate_City_address").trim();

						logger.debug("Full_name" + Full_name);
						logger.debug("ToAddress" + ToAddress);
						logger.debug("Tostreet" + Tostreet);

						String AWB = sb.toString()
								.replace(">WI_name<", ">" + CheckGridDataMap.get("Wi_no").trim() + "<")
								.replace(">MobNo<", ">" + CheckGridDataMap.get("mobNo").trim() + "<")
								.replace(">Full_name<", ">" + Full_name + "<")
								.replace(">Address<", ">" + ToAddress + "<")
								.replace(">Street_location<", ">" + Tostreet + "<").replace(">Address_country<",
										">" + CheckGridDataMap.get("Emirate_City_address").trim() + "<");

						logger.debug("AWB: " + AWB);

						String integrationStatus = "Success";
						String attributesTag;
						String ErrDesc = "";
						StringBuilder finalString = new StringBuilder();
						finalString = finalString.append(AWB);
						// changes need to done to update the correct flag
						HashMap<String, String> socketConnectionMap = CommonMethods.socketConnectionDetails(cabinetName,
								sJtsIp, iJtsPort, sessionID);

						integrationStatus = socketConnection(cabinetName, CommonConnection.getUsername(), sessionID,
								sJtsIp, iJtsPort, processInstanceID, ws_name, 60, 65, socketConnectionMap, finalString);

						// - xml parse for getting out the return code.

						XMLParser xmlParserSocketDetails = new XMLParser(integrationStatus);
						logger.debug(" xmlParserSocketDetails : " + xmlParserSocketDetails);
						String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
						logger.debug("Return Code: " + return_code + "WI: " + processInstanceID);
						String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
						logger.debug("return_desc : " + return_desc + "WI: " + processInstanceID);

						String MsgId = "";
						if (integrationStatus.contains("<MessageId>"))
							MsgId = xmlParserSocketDetails.getValueOf("MessageId");

						logger.debug("MsgId : " + MsgId + " AWB for WI: " + processInstanceID);

						if (return_code.equalsIgnoreCase("0000")) {
							integrationStatus = "Success";
							ErrDesc = "AWB Done Successfully";
							logger.debug("AWB Done Successfully");

							// code for storing AWB No and AWb pdf---- Start
							String AWB_No = xmlParserSocketDetails.getValueOf("AWBNumber");
							String AWB_pdf = xmlParserSocketDetails.getValueOf("AWBPdf");
							String AWb_status = "R";

							updateExternalTable("NG_DPL_EXTTABLE", "AWB_Number", "'" + AWB_No + "'",
									"WIName='" + processInstanceID + "'", sJtsIp, iJtsPort, cabinetName);
							updateExternalTable("NG_DPL_EXTTABLE", "awb_pdf", "'" + AWB_pdf + "'",
									"WIName='" + processInstanceID + "'", sJtsIp, iJtsPort, cabinetName);

							insert_ng_digital_awb_status_DCC(processInstanceID, cabinetName, sessionID, sJtsIp,
									iJtsPort, ActivityName);
						}

						if ("Success".equalsIgnoreCase(integrationStatus)) {
							decisionValue = "Approve";
							logger.debug("Decision in success: " + decisionValue);
							attributesTag = "<Decision>" + decisionValue + "</Decision>";
						} else {
							ErrDesc = return_desc;
							decisionValue = "Reject";
							logger.debug("Decision in else : " + decisionValue);
							attributesTag = "<Decision>" + decisionValue + "</Decision>";

							sendMail(cabinetName, processInstanceID, sJtsIp, iJtsPort, ErrDesc, return_code,
									ProcessDefId, MsgId);

						}

						String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionID,
								processInstanceID, WorkItemID);
						String getWorkItemOutputXml = CommonMethods.WFNGExecute(getWorkItemInputXML, sJtsIp, iJtsPort,
								1);
						logger.debug("Output XML For WmgetWorkItemCall: " + getWorkItemOutputXml);

						XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
						String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
						logger.debug("WmgetWorkItemCall Maincode:  " + getWorkItemMainCode);

						if (getWorkItemMainCode.trim().equals("0")) {
							logger.debug("WMgetWorkItemCall Successful: " + getWorkItemMainCode);

							String assignWorkitemAttributeInputXML = "<?xml version=\"1.0\"?><WMAssignWorkItemAttributes_Input>"
									+ "<Option>WMAssignWorkItemAttributes</Option>" + "<EngineName>" + cabinetName
									+ "</EngineName>" + "<SessionId>" + sessionID + "</SessionId>"
									+ "<ProcessInstanceId>" + processInstanceID + "</ProcessInstanceId>"
									+ "<WorkItemId>" + WorkItemID + "</WorkItemId>" + "<ActivityId>" + ActivityID
									+ "</ActivityId>" + "<ProcessDefId>" + ProcessDefId + "</ProcessDefId>"
									+ "<LastModifiedTime></LastModifiedTime>" + "<ActivityType>" + ActivityType
									+ "</ActivityType>" + "<complete>D</complete>" + "<AuditStatus></AuditStatus>"
									+ "<Comments></Comments>" + "<UserDefVarFlag>Y</UserDefVarFlag>" + "<Attributes>"
									+ attributesTag + "</Attributes>" + "</WMAssignWorkItemAttributes_Input>";

							logger.debug("InputXML for assignWorkitemAttribute Call DAO_AWB_Gen: "
									+ assignWorkitemAttributeInputXML);

							String assignWorkitemAttributeOutputXML = CommonMethods
									.WFNGExecute(assignWorkitemAttributeInputXML, sJtsIp, iJtsPort, 1);

							logger.debug("OutputXML for assignWorkitemAttribute Call DAO_AWB_Gen: "
									+ assignWorkitemAttributeOutputXML);

							XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
							String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
							// For AWB number get:
							String AWB_Number = xmlParserWorkitemAttribute.getValueOf("AWBNumber");
							String AWB_PDF = xmlParserWorkitemAttribute.getValueOf("AWBPdf");

							logger.debug("AssignWorkitemAttribute MainCode: " + assignWorkitemAttributeMainCode);
							logger.debug("AWB_Number : " + AWB_Number);
							logger.debug("AWB_PDF : " + AWB_PDF);

							if (assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0")) {
								logger.debug(
										"Assign Workitem Attribute Successful: " + assignWorkitemAttributeMainCode);

								System.out.println(
										processInstanceID + "Complete Succesfully with status " + decisionValue);

								logger.debug("WorkItem moved to next Workstep.");

							} else {

								logger.debug("decisionValue : " + decisionValue);
								ErrDesc = "Done WI failed";
								sendMail(cabinetName, processInstanceID, sJtsIp, iJtsPort, ErrDesc,
										assignWorkitemAttributeMainCode, ProcessDefId, MsgId);
							}

							DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String formattedEntryDatetime = dateFormat.format(new Date());
							logger.debug("FormattedEntryDatetime: " + formattedEntryDatetime);

							
							if (formattedEntryDatetime.equalsIgnoreCase("") || formattedEntryDatetime == null) {
								formattedEntryDatetime = entryDateTime;
							}
							Date d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(formattedEntryDatetime);
							String entrydatetime_format = dateFormat.format(d1);

							String columnNames = "wi_name,decision_date_time,workstep,Decision,Remarks,entry_date_time";
							String columnValues = "'" + processInstanceID + "','" + formattedEntryDatetime + "','"
									+ ActivityName + "','" + decisionValue + "','" + ErrDesc + "','"
									+ entrydatetime_format + "'";

							String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionID, columnNames,
									columnValues, "NG_DPL_GR_DECISION_HISTORY");
							logger.debug("APInsertInputXML: " + apInsertInputXML);

							String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, sJtsIp, iJtsPort, 1);
							logger.debug("APInsertOutputXML: " + apInsertInputXML);

							XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
							String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
							logger.debug("Status of apInsertMaincode  " + apInsertMaincode);

							logger.debug("Completed On " + ActivityName);

							if (apInsertMaincode.equalsIgnoreCase("0")) {
								logger.debug("ApInsert successful: " + apInsertMaincode);
								logger.debug("Inserted in WiHistory table successfully.");
							} else {
								logger.debug("ApInsert failed: " + apInsertMaincode);
							}
						} else {
							getWorkItemMainCode = "";
							logger.debug("WmgetWorkItem failed: " + getWorkItemMainCode);
							ErrDesc = "WI Failed";
							sendMail(cabinetName, processInstanceID, sJtsIp, iJtsPort, ErrDesc, getWorkItemMainCode,
									ProcessDefId, MsgId);
						}
					}
				}
			}
		} catch (Exception e)

		{
			logger.debug("Exception: " + e.getMessage());
		}
	}

	private void insert_ng_digital_awb_status_DCC(String Wi_name, String cabinetName, String sessionId, String sJtsIp,
			String iJtsPort, String ActivityName) {
		try {
			String process_name = "";
			final HashMap<String, String> CheckGridDataMap_awb = new HashMap<String, String>();
			
			String DBQuery_awb = "select WiName,ProspectID,FirstName,MiddleName,LastName,MobileNo,EmailID,AWB_Number,IsNTB,ECRN,EmiratesID from NG_DPL_EXTTABLE where WIName='"
					+ Wi_name + "'";

			String extTabDataIPXML_awb = CommonMethods.apSelectWithColumnNames(DBQuery_awb,
					CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false));
			logger.debug("extTabDataOPXML_awb: " + extTabDataIPXML_awb);
			
			String extTabDataOPXML_awb = CommonMethods.WFNGExecute(extTabDataIPXML_awb, CommonConnection.getJTSIP(),
					CommonConnection.getJTSPort(), 1);
			logger.debug("extTabDataOPXML_awb: " + extTabDataOPXML_awb);
			
			XMLParser xmlParserData_awb = new XMLParser(extTabDataOPXML_awb);

			int iTotalrec = Integer.parseInt(xmlParserData_awb.getValueOf("TotalRetrieved"));

			if (xmlParserData_awb.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0) {
				String xmlDataExtTab = xmlParserData_awb.getNextValueOf("Record");
				xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
				NGXmlList objWorkList = xmlParserData_awb.createList("Records", "Record");

				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
					CheckGridDataMap_awb.put("Wi_Name", objWorkList.getVal("WiName"));
					CheckGridDataMap_awb.put("Prospect_id", objWorkList.getVal("ProspectID"));
					CheckGridDataMap_awb.put("FirstName", objWorkList.getVal("FirstName"));
					CheckGridDataMap_awb.put("MiddleName", objWorkList.getVal("MiddleName"));
					CheckGridDataMap_awb.put("LastName", objWorkList.getVal("LastName"));
					CheckGridDataMap_awb.put("MobileNo", objWorkList.getVal("MobileNo"));
					CheckGridDataMap_awb.put("email_id", objWorkList.getVal("EmailID"));
					CheckGridDataMap_awb.put("AWB_Number", objWorkList.getVal("AWB_Number"));
					CheckGridDataMap_awb.put("ntb", objWorkList.getVal("IsNTB"));
					CheckGridDataMap_awb.put("ECRN", objWorkList.getVal("ECRN"));
					//CheckGridDataMap_awb.put("CardOps_Reschedule", objWorkList.getVal("CardOps_Reschedule"));
					CheckGridDataMap_awb.put("EmirateID", objWorkList.getVal("EmiratesID"));
				}

				String processname[] = Wi_name.split("-");
				logger.debug("processname [] : " + processname[0]);
				process_name = processname[0];
				logger.debug("processname [] : " + process_name);
			}

			

//			String Query_for_cardserno = "select CardSerno,ELITE_CRN from NG_DCC_PRIME_COURIER with (nolock) where WI_name='"
//					+ Wi_name + "'";
//			String extTabDataIPXML_prime_cor = CommonMethods.apSelectWithColumnNames(Query_for_cardserno, cabinetName,
//					sessionID);
//			logger.debug("extTabDataIPXML_prime_cor: " + extTabDataIPXML_prime_cor);
//			String extTabDataOPXML_prime_cor = CommonMethods.WFNGExecute(extTabDataIPXML_prime_cor,
//					CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
//			logger.debug(" extTabDataOPXML_prime_cor " + extTabDataOPXML_prime_cor);
//			XMLParser xmlParserData_prime_cor = new XMLParser(extTabDataOPXML_prime_cor);
//			int iTotalrec_prime_cor = Integer.parseInt(xmlParserData_prime_cor.getValueOf("TotalRetrieved"));
//			logger.debug("iTotalrec_wi_table " + iTotalrec_prime_cor);

//			if (xmlParserData_prime_cor.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_prime_cor > 0) {
//				String xmlDataExtTab = xmlParserData_prime_cor.getNextValueOf("Record");
//				xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
//				NGXmlList objWorkList = xmlParserData_prime_cor.createList("Records", "Record");
//
//				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
//					CheckGridDataMap_awb.put("CardSerno", objWorkList.getVal("CardSerno"));
//					CheckGridDataMap_awb.put("ELITE_CRN", objWorkList.getVal("ELITE_CRN"));
//				}
//				logger.debug(" CardSerno " + CheckGridDataMap_awb.get("CardSerno").trim()
//						+ " for  DCC ELITE_CRN " + CheckGridDataMap_awb.get("ELITE_CRN").trim());
//			}

			Date d = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String sDate = dateFormat.format(d);
			logger.debug("insert_ng_digital_awb_status : sDate " + sDate);
			System.out.println();
			String prospect_id = CheckGridDataMap_awb.get("Prospect_id").trim();
			String Full_name = CheckGridDataMap_awb.get("FirstName").trim() + " "
					+ CheckGridDataMap_awb.get("MiddleName").trim() + " " + CheckGridDataMap_awb.get("LastName").trim();

			String ECRN = "";
			if (CheckGridDataMap_awb.containsKey("ECRN")) {
				ECRN = CheckGridDataMap_awb.get("ECRN").trim();
			}

//			String CardSerno = "";
//			if (CheckGridDataMap_awb.containsKey("CardSerno")) {
//				CardSerno = CheckGridDataMap_awb.get("CardSerno").trim();
//			}
//			String CardOps_Reschedule = "";
//			if (CheckGridDataMap_awb.containsKey("CardOps_Reschedule")) {
//				CardOps_Reschedule = CheckGridDataMap_awb.get("CardOps_Reschedule").trim();
//			}

			logger.debug("insert_ng_digital_awb_status DCC : ECRN " + ECRN);
			logger.debug("insert_ng_digital_awb_status DCC : prospect_id " + prospect_id);
			logger.debug("insert_ng_digital_awb_status DCC  : Full_name " + Full_name);
			String columnNames_awbTable = "";
			String columnValues_awbTable = "";

//			if (CardOps_Reschedule.equalsIgnoreCase("Y")) {
//				columnNames_awbTable = "WI_name,Prospect_ID,Customer_name,mobile_No,email_id,ECRN,AWB_Number,processName,singlePager_ref_no,AWB_Gen_success_date,is_Ntb,CardSerno,card_req,EmiratesID";
//				columnValues_awbTable = "'" + Wi_name + "','" + prospect_id + "','" + Full_name + "','"
//						+ CheckGridDataMap_awb.get("MobileNo").trim() + "','"
//						+ CheckGridDataMap_awb.get("email_id").trim() + "','" + ECRN.trim() + "','"
//						+ CheckGridDataMap_awb.get("AWB_Number").trim() + "','" + process_name + "','" + Wi_name + "','"
//						+ sDate + "','" + CheckGridDataMap_awb.get("ntb").trim() + "','" + CardSerno + "'" + ",'N','"
//						+ CheckGridDataMap_awb.get("EmirateID").trim() + "'";
//			} else {
			System.out.println();
				columnNames_awbTable = "WI_name,Prospect_ID,Customer_name,mobile_No,email_id,ECRN,AWB_Number,processName,singlePager_ref_no,AWB_Gen_success_date,is_Ntb,CardSerno,EmiratesID";
				columnValues_awbTable = "'" + Wi_name + "','" + prospect_id + "','" + Full_name + "','"
						+ CheckGridDataMap_awb.get("MobileNo").trim() + "','"
						+ CheckGridDataMap_awb.get("email_id").trim() + "','" + ECRN.trim() + "','"
						+ CheckGridDataMap_awb.get("AWB_Number").trim() + "','" + process_name + "','" + Wi_name + "','"
						+ sDate + "','" + CheckGridDataMap_awb.get("ntb").trim() + "','" + "" + "','"
						+ CheckGridDataMap_awb.get("EmirateID").trim() + "'";
//			}

			logger
					.debug("insert_ng_digital_awb_status : columnNames_awbTable DPL " + columnNames_awbTable);
			logger
					.debug("insert_ng_digital_awb_status : columnValues_awbTable DPL " + columnValues_awbTable);

			String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionID, columnNames_awbTable,
					columnValues_awbTable, "ng_digital_awb_status");
			logger.debug("APInsertInputXML: ng_digital_awb_status DPL " + apInsertInputXML);

			String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, sJtsIp, iJtsPort, 1);
			logger.debug("APInsertOutputXML: ng_digital_awb_status DPL " + apInsertInputXML);

			XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
			String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
			logger.debug("Status of apInsertMaincode  ng_digital_awb_status DPL " + apInsertMaincode);

			logger.debug("Completed On ng_digital_awb_status DPL " + ActivityName);

			if (apInsertMaincode.equalsIgnoreCase("0")) {
				logger.debug("ApInsert successful: ng_digital_awb_status " + apInsertMaincode);
				logger.debug("ApInsert successful: ng_digital_awb_status " + apInsertMaincode);
				System.out.println("ApInsert successful: ng_digital_awb_status DPL " + Wi_name);
				CheckGridDataMap_awb.clear();
			} else {
				logger.debug("ApInsert failed for ng_digital_awb_status: " + apInsertMaincode);
				System.out.println("ApInsert failed: ng_digital_awb_status DPL " + Wi_name);
			}

		} catch (Exception e) {
			logger.debug("insert_ng_digital_awb_status : " + e.getMessage());
		}

	}

	
	private void updateExternalTable(String tablename, String columnname, String sMessage, String sWhere, String jtsIP,
			String jtsPort, String cabinetName) {
		int sessionCheckInt = 0;
		int loopCount = 50;
		int mainCode = 0;
		String outXmlCheckAPUpdate = null;
		String mainCodeforCheckUpdate = null;

		logger.debug("Inside update EXT table: ");

		while (sessionCheckInt < loopCount) {
			try {
				XMLParser objXMLParser = new XMLParser();

				String inputXmlcheckAPUpdate = CommonMethods.getAPUpdateIpXML(tablename, columnname, sMessage, sWhere,
						cabinetName, sessionID);
				logger.debug(("inputXmlcheckAPUpdate : " + inputXmlcheckAPUpdate));

				outXmlCheckAPUpdate = CommonMethods.WFNGExecute(inputXmlcheckAPUpdate, jtsIP, jtsPort, 1);
				logger.debug(("outXmlCheckAPUpdate : " + outXmlCheckAPUpdate));

				objXMLParser.setInputXML(outXmlCheckAPUpdate);

				mainCodeforCheckUpdate = objXMLParser.getValueOf("MainCode");
				if (!mainCodeforCheckUpdate.equalsIgnoreCase("0")) {
					logger.debug(("Exception in ExecuteQuery_APUpdate updating " + tablename + " table"));
					System.out.println("Exception in ExecuteQuery_APUpdate updating " + tablename + " table");
				} else {
					logger.debug(("Succesfully updated " + tablename + " table"));
					System.out.println("Succesfully updated " + tablename + " table");

				}
				mainCode = Integer.parseInt(mainCodeforCheckUpdate);
				if (mainCode == 11) {
					sessionID = CommonConnection.getSessionID(logger, false);
				} else {
					sessionCheckInt++;
					break;
				}

				if (outXmlCheckAPUpdate.equalsIgnoreCase("") || outXmlCheckAPUpdate == ""
						|| outXmlCheckAPUpdate == null)
					break;

			} catch (Exception e) {
				logger.debug(("Inside create validateSessionID exception" + e.getMessage()));
			}
		}
	}

	String socketConnection(String cabinetName, String username, String sessionId, String sJtsIp, String iJtsPort,
			String processInstanceID, String ws_name, int connection_timeout, int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, StringBuilder sInputXML) {

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

		try {

			logger.debug("userName " + username);
			logger.debug("SessionId " + sessionID);

			socketServerIP = socketDetailsMap.get("SocketServerIP");
			logger.debug("SocketServerIP " + socketServerIP);
			socketServerPort = Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			logger.debug("SocketServerPort " + socketServerPort);

			if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort == 0)) {

				socket = new Socket(socketServerIP, socketServerPort);
				socket.setSoTimeout(connection_timeout * 1000);
				out = socket.getOutputStream();
				socketInputStream = socket.getInputStream();
				dout = new DataOutputStream(out);
				din = new DataInputStream(socketInputStream);
				logger.debug("Dout " + dout);
				logger.debug("Din " + din);

				outputResponse = "";
				String History_tablename = "NG_DPL_XMLLOG_HISTORY";

				logger.debug("rubi processInstanceID substring: " + processInstanceID.substring(0, 3));
				
				//Digital_PL_CommomMethod comMethod= new Digital_PL_CommomMethod();
				inputRequest = getRequestXML(cabinetName, sessionID, processInstanceID, ws_name, username, sInputXML
						);

				logger.debug("inputRequest substring: " + inputRequest);
				if (inputRequest != null && inputRequest.length() > 0) {
					int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
					logger.debug("RequestLen: " + inputRequestLen + "");
					inputRequest = inputRequestLen + "##8##;" + inputRequest;
					logger.debug("InputRequest" + "Input Request Bytes : " + inputRequest.getBytes("UTF-16LE"));
					dout.write(inputRequest.getBytes("UTF-16LE"));
					dout.flush();
				}
				byte[] readBuffer = new byte[5000];
				int num = din.read(readBuffer);
				if (num > 0) {

					byte[] arrayBytes = new byte[num];
					System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
					outputResponse = outputResponse + new String(arrayBytes, "UTF-16LE");
					inputMessageID = outputResponse;
					logger.debug("OutputResponse: " + outputResponse);

					if (!"".equalsIgnoreCase(outputResponse))
						outputResponse = getResponseXML(cabinetName, sJtsIp, iJtsPort, sessionID, processInstanceID,
								outputResponse, integrationWaitTime);

					if (outputResponse.contains("&lt;")) {
						outputResponse = outputResponse.replaceAll("&lt;", "<");
						outputResponse = outputResponse.replaceAll("&gt;", ">");
					}
				}
				socket.close();

				outputResponse = outputResponse.replaceAll("</MessageId>",
						"</MessageId>/n<InputMessageId>" + inputMessageID + "</InputMessageId>");

				// logger.debug("outputResponse
				// "+outputResponse);
				return outputResponse;

			}

			else {
				logger.debug("SocketServerIp and SocketServerPort is not maintained " + "");
				logger.debug("SocketServerIp is not maintained " + socketServerIP);
				logger.debug(" SocketServerPort is not maintained " + socketServerPort);
				return "Socket Details not maintained";
			}

		}

		catch (Exception e) {
			logger.debug("Exception Occured Mq_connection_CC" + e.getStackTrace());
			return "";
		} finally {
			try {
				if (out != null) {
					out.close();
					out = null;
				}
				if (socketInputStream != null) {

					socketInputStream.close();
					socketInputStream = null;
				}
				if (dout != null) {

					dout.close();
					dout = null;
				}
				if (din != null) {

					din.close();
					din = null;
				}
				if (socket != null) {
					if (!socket.isClosed())
						socket.close();
					socket = null;
				}

			}

			catch (Exception e) {
				logger.debug("Final Exception Occured Mq_connection_CC" + e.getStackTrace());
				// printException(e);
			}
		}

	}

	public void sendMail(String cabinetName, String wiName, String jtsIp, String jtsPort, String ErrDesc,
			String return_code, String ProcessDefId, String MsgId) throws Exception {
		XMLParser objXMLParser = new XMLParser();
		String sInputXML = "";
		String sOutputXML = "";
		String mainCodeforAPInsert = null;
		int sessionCheckInt = 0;
		int loopCount = 50;
		int waitLoop = 50;

		while (sessionCheckInt < loopCount) {
			try {
				logger.debug("workitem name to send mail---" + wiName);
				logger.debug("ErrorMsg to send mail---" + ErrDesc);
				logger.debug("return_code to send mail---" + return_code);

				String FinalMailStr = MailStr.toString().replace("<WI_NAME>", wiName).replace("<ret_Code>", return_code)
						.replace("<errormsg>", ErrDesc).replace("<MsgID>", MsgId);
				logger.debug("finalbody: " + FinalMailStr);

				String columnName = "MAILFROM,MAILTO,MAILSUBJECT,MAILMESSAGE,MAILCONTENTTYPE,MAILPRIORITY,MAILSTATUS,INSERTEDBY,MAILACTIONTYPE,INSERTEDTIME,PROCESSDEFID,PROCESSINSTANCEID,WORKITEMID,ACTIVITYID,NOOFTRIALS";
				String strValues = "'" + fromMailID + "','" + toMailID + "','" + mailSubject + "','" + FinalMailStr
						+ "','text/html;charset=UTF-8','1','N','CUSTOM','TRIGGER','"
						+ CommonMethods.getdateCurrentDateInSQLFormat() + "','" + ProcessDefId + "','" + wiName
						+ "','1','1','0'";

				sInputXML = "<?xml version=\"1.0\"?>" + "<APInsert_Input>" + "<Option>APInsert</Option>"
						+ "<TableName>WFMAILQUEUETABLE</TableName>" + "<ColName>" + columnName + "</ColName>"
						+ "<Values>" + strValues + "</Values>" + "<EngineName>" + cabinetName + "</EngineName>"
						+ "<SessionId>" + sessionID + "</SessionId>" + "</APInsert_Input>";
				logger.debug("Mail Insert InputXml::::::::::\n" + sInputXML);
				sOutputXML = CommonMethods.WFNGExecute(sInputXML, jtsIp, jtsPort, 0);
				logger.debug("Mail Insert OutputXml::::::::::\n" + sOutputXML);
				objXMLParser.setInputXML(sOutputXML);
				mainCodeforAPInsert = objXMLParser.getValueOf("MainCode");

			}

			catch (Exception e) {
				e.printStackTrace();
				logger.error("Exception in Sending mail", e);
				sessionCheckInt++;
				waiteloopExecute(waitLoop);
				continue;
			}
			if (mainCodeforAPInsert.equalsIgnoreCase("11")) {
				logger.debug("Invalid session in Sending mail");
				sessionCheckInt++;
				// ThreadConnect.sessionId =
				// ThreadConnect.getSessionID(cabinetName, jtsIP, jtsPort,
				// userName,password);
				sessionID = CommonConnection.getSessionID(logger, false);
				continue;
			} else {
				sessionCheckInt++;
				break;
			}
		}
		if (mainCodeforAPInsert.equalsIgnoreCase("0")) {
			logger.debug("mail Insert Successful");
			System.out.println("Mail Insert Successful for " + wiName + " in table WFMAILQUEUETABLE");
		} else {
			logger.debug("mail Insert Unsuccessful");
			System.out.println("Mail Insert Unsuccessful for " + wiName + "in table WFMAILQUEUETABLE");
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

	private static String getRequestXML(String cabinetName, String sessionId,
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
		logger.debug("GetRequestXML: "+ strBuff.toString());
		return strBuff.toString();
	}
	
	private static String getResponseXML(String cabinetName,String sJtsIp,String iJtsPort, String
			sessionId, String processInstanceID,String message_ID, int integrationWaitTime)
	{

		String outputResponseXML="";
		try
		{
			String QueryString = "select OUTPUT_XML from NG_DPL_XMLLOG_HISTORY with (nolock) where MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+processInstanceID+"'";
			String responseInputXML = CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, sessionId);
			logger.debug("Response APSelect InputXML: "+responseInputXML);

			int Loop_count=0;
			do
			{
				String responseOutputXML=CommonMethods.WFNGExecute(responseInputXML,sJtsIp,iJtsPort,1);
				//DCCExternalExposureLog.DCCExternalExposureLogger.debug("Response APSelect OutputXML: "+responseOutputXML);

			    XMLParser xmlParserSocketDetails= new XMLParser(responseOutputXML);
			    String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			    //DCCExternalExposureLog.DCCExternalExposureLogger.debug("ResponseMainCode: "+responseMainCode);



			    int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			    //DCCExternalExposureLog.DCCExternalExposureLogger.debug("ResponseTotalRecords: "+responseTotalRecords);

			    if (responseMainCode.equals("0") && responseTotalRecords > 0)
				{

					String responseXMLData=xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData =responseXMLData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

	        		XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
	        		//DCCExternalExposureLog.DCCExternalExposureLogger.debug("ResponseXMLData: "+responseXMLData);

	        		outputResponseXML=xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
	        		//DCCExternalExposureLog.DCCExternalExposureLogger.debug("OutputResponseXML: "+outputResponseXML);
	        		
	        		if(outputResponseXML.contains("<MQ_RESPONSE_XML>"))
	        		{
	        			XMLParser xmlParserResponseXMLData1 = new XMLParser(outputResponseXML);
	        			outputResponseXML = xmlParserResponseXMLData1.getValueOf("MQ_RESPONSE_XML");
	        		}
	        		if("".equalsIgnoreCase(outputResponseXML)){
	        			outputResponseXML="Error";
	    			}
	        		break;
				}
			    Loop_count++;
			    Thread.sleep(1000);
			}
			while(Loop_count<integrationWaitTime);
			logger.debug("integrationWaitTime: "+integrationWaitTime);

		}
		catch(Exception e)
		{
			logger.debug("Exception occurred in outputResponseXML" + e.getMessage());
			logger.debug("Exception occurred in outputResponseXML" + e.getStackTrace());
			outputResponseXML="Error";
		}

		return outputResponseXML;

	}
	
}
