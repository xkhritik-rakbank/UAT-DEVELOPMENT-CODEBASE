package com.newgen.DPL.awb;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.DPL.Digital_PL_CommomMethod;
import com.newgen.DPL.Digital_PL_Log;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.omni.wf.util.excp.NGException;

import ISPack.CPISDocumentTxn;
import ISPack.ISUtil.JPDBRecoverDocData;
import ISPack.ISUtil.JPISException;
import ISPack.ISUtil.JPISIsIndex;

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
	public static LocalDate NextUtilityExecutionDate = LocalDate.now();
	public static LocalDate NextReportExecutionDate = LocalDate.now();
	public static LocalDate NextDialerReportExecutionDate = LocalDate.now();//dialer report
	public String onePagerTemplatesPath_source;
	public String onePagerTemplatesPath_destination;
	public String onePagerTemplatesPath_archive;
	public static String OnePagetTempReportPath="";
	public static String OnePagerReportOdFolderName;
	public static String OnePagerReportName="";
	public static String OnePagerReportSubject="";
	public static String OnePagerReportFromMail="";
	public static String OnePagerReportBody="";
	public static String OnePagerReportToMail="";
	public static String smsPort = "";
	public static String jtsIP = "";
	public static String volumeID;
	public static String FolderIndex;
	public static String ProcessDefId;
	public AWB() throws NGException {

		Digital_PL_Log.setLogger(getClass().getSimpleName());
		this.ngEjbClient = NGEjbClient.getSharedInstance();
		logger = Digital_PL_Log.getLogger(getClass().getSimpleName());
	}

	public void startDPL_AWB_Gen(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String queueID,
			int socketConnectionTimeOut, int integrationWaitTime, HashMap<String, String> socketDetailsMap,Map<String, String> AWB_GEN_MAP ) {
		String ws_name = "AWB_Generation";

		try {
			logger.debug("inside startDPL_AWB_Gen");
			sessionID = CommonConnection.getSessionID(logger, false);
			if (sessionID == null || sessionID.equalsIgnoreCase("") || sessionID.equalsIgnoreCase("null")) {
				logger.error("Could Not Get Session ID " + sessionID);
				return;
			}
			logger.debug("Fetching all Workitems for AWB DPL ");
			System.out.println("Fetching all Workitems on queue");

			//for consolidate report
			
			OnePagetTempReportPath=AWB_GEN_MAP.get("dpl_OnePagetTempReportPath");
			logger.debug("onePagerTemplatesPath: "+OnePagetTempReportPath);
			
			LocalTime TimeFromRunReportOnePager = LocalTime.parse(AWB_GEN_MAP.get("TimeFromRunReportOnePager"));
			LocalTime TimeToRunReportOnePager = LocalTime.parse(AWB_GEN_MAP.get("TimeToRunReportOnePager"));
			jtsIP = CommonConnection.getJTSIP();
			
			LocalTime TimeFromrun = LocalTime.parse(AWB_GEN_MAP.get("TimeFrom"));
			LocalTime TimeTorun = LocalTime.parse(AWB_GEN_MAP.get("TimeTo"));
			
			OnePagerReportName=AWB_GEN_MAP.get("OnePagerReportName");
			logger.debug("OnePagerReportName: "+OnePagerReportName);
			
			volumeID=AWB_GEN_MAP.get("volumeID");
			logger.debug("volumeID: "+volumeID);
			
			smsPort = CommonConnection.getsSMSPort();
			logger.debug("smsPort: "+smsPort);
			
			FolderIndex=AWB_GEN_MAP.get("DPL_folderIndex");
			logger.debug("FolderIndex: "+FolderIndex);
			
			OnePagerReportSubject=AWB_GEN_MAP.get("DPL_OnePagerReportSubject");
			logger.debug("OnePagerReportSubject: "+OnePagerReportSubject);
			
			OnePagerReportBody=AWB_GEN_MAP.get("OnePagerReportBody");
			logger.debug("OnePagerReportBody: "+OnePagerReportBody);
			
			OnePagerReportOdFolderName=AWB_GEN_MAP.get("DPLOnePagerReportOdFolderName");
			logger.debug("OnePagerReportOdFolderName: "+OnePagerReportOdFolderName);
			
			OnePagerReportFromMail=AWB_GEN_MAP.get("OnePagerReportFromMail");
			OnePagerReportToMail=AWB_GEN_MAP.get("OnePagerReportToMail");
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
					ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					logger.debug("ProcessDefId DPL: " + ProcessDefId);

					
					ValidatePrimeCBSFile(processInstanceID,sJtsIp,iJtsPort,cabinetName); 
					
					// Added for Validating Rescheduled case:.
					String decisionValue = "";
					String DBQuer_validateCase = "";
					
					String DBQuer_RescheduleFlag = "SELECT CardOps_Reschedule from NG_DPL_EXTTABLE with (NOLOCK ) where WIname='"
							+ processInstanceID + "'";
					
					String extTabDataIPXML_RescheduleFlag = CommonMethods.apSelectWithColumnNames(
							DBQuer_RescheduleFlag, CommonConnection.getCabinetName(),
							CommonConnection.getSessionID(logger, false));
					
					logger.debug("extTabDataIPXML DPL: " + extTabDataIPXML_RescheduleFlag);
					String extTabDataOPXML_RescheduleFlag = CommonMethods.WFNGExecute(extTabDataIPXML_RescheduleFlag,
							CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
					logger.debug("extTabDataOPXML DPL: " + extTabDataOPXML_RescheduleFlag);
					XMLParser xmlParserData_RescheduleFlag = new XMLParser(extTabDataOPXML_RescheduleFlag);
					
					if (!xmlParserData_RescheduleFlag.getValueOf("CardOps_Reschedule").equals("Y")) {
						/*DBQuer_validateCase = "select WIname from NG_DPL_EXTTABLE with (NOLOCK ) where WI_name='"
								+ processInstanceID
								+ "' and Courier_Flag='File_received' and (AWB_Number='' or AWB_Number is null);";*/
						DBQuer_validateCase = "select isnull(AWB_Number,'') as 'AWB_Number',winame from NG_DPL_EXTTABLE with (NOLOCK ) where WIname='"
								+ processInstanceID
								+ "' and (AWB_Number='' or AWB_Number is null) and ((IsNTB='N' and ((IsCBReq='Y' and isCBSRec='Received') or IsCBReq='N') and ((IsDBCardReq='Y' and Courier_Flag='File_received')  or IsDBCardReq='N')) or (IsNTB='Y' and ((IsCBReq='Y' and isCBSRec='Received') or IsCBReq='N') and ((IsDBCardReq='Y' and Courier_Flag='File_received') or IsDBCardReq='N')))";

					} else {
						DBQuer_validateCase = "select WIname from NG_DPL_EXTTABLE with (NOLOCK ) where WIname='"
								+ processInstanceID + "';";
					}
					// End
					
					//
					/*
					String DBQuer_validateCase = "select isnull(AWB_Number,'') as 'AWB_Number' from NG_DPL_EXTTABLE with (NOLOCK ) where WIname='"
							+ processInstanceID
							+ "' and (AWB_Number='' or AWB_Number is null) and ((IsNTB='N' and ((IsCBReq='Y' and isCBSRec='Received') or IsCBReq='N') and ((IsDBCardReq='Y' and Courier_Flag='File_received')  or IsDBCardReq='N')) or (IsNTB='Y' and ((IsCBReq='Y' and isCBSRec='Received') or IsCBReq='N') and ((IsDBCardReq='Y' and Courier_Flag='File_received') or IsDBCardReq='N')))";
					*/
						
					String extTabDataIPXML_validateCase = CommonMethods.apSelectWithColumnNames(DBQuer_validateCase,
							CommonConnection.getCabinetName(),
							CommonConnection.getSessionID(logger, false));
					
					logger.debug("extTabDataIPXML DPL: " + extTabDataIPXML_validateCase);
					String extTabDataOPXML_validateCase = CommonMethods.WFNGExecute(extTabDataIPXML_validateCase,
							CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
					logger.debug("extTabDataOPXML dpl: " + extTabDataOPXML_validateCase);
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
							updateExternalTable("NG_DPL_EXTTABLE", "awb_pdf,AWB_status", "'" + AWB_pdf+"','"+AWb_status+ "'",
									"WIName='" + processInstanceID + "'", sJtsIp, iJtsPort, cabinetName);

							insert_ng_digital_awb_status(processInstanceID, cabinetName, sessionID, sJtsIp,
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

							logger.debug("InputXML for assignWorkitemAttribute Call dpl_AWB_Gen: "
									+ assignWorkitemAttributeInputXML);

							String assignWorkitemAttributeOutputXML = CommonMethods
									.WFNGExecute(assignWorkitemAttributeInputXML, sJtsIp, iJtsPort, 1);

							logger.debug("OutputXML for assignWorkitemAttribute Call dpl_AWB_Gen: "
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
			
			LocalTime now = LocalTime.now();
			LocalDate CurrentDate = LocalDate.now();
			
			if (now.isAfter(TimeFromrun) && now.isBefore(TimeTorun)) {

				if (CurrentDate.compareTo(NextUtilityExecutionDate) >= 0) {
					//sendmailfor7days();
					//sendmailfor10days();
					//MailAwbNotGeneratedDao();
					NextUtilityExecutionDate = CurrentDate.plusDays(1);
				}
			}
			
			Calendar cal=Calendar.getInstance();
			cal.setTime(new Date());
			int dayOfWeekNum=cal.get(Calendar.DAY_OF_WEEK);
			DateFormat formatter=new SimpleDateFormat("EEEE");
			String dayofWeek=formatter.format(cal.getTime());
			logger.debug("dayofWeek: " + dayofWeek);//vk
			System.out.println(now.isAfter(TimeFromRunReportOnePager));
			System.out.println(now.isAfter(TimeToRunReportOnePager));
			System.out.println(!dayofWeek.equalsIgnoreCase("SUNDAY"));
			if (now.isAfter(TimeFromRunReportOnePager) && now.isBefore(TimeToRunReportOnePager) && (!dayofWeek.equalsIgnoreCase("SUNDAY"))) {

				if (CurrentDate.compareTo(NextReportExecutionDate) >= 0) {
					String NoofDaysData="";
					if(dayofWeek.equalsIgnoreCase("MONDAY")){
						 NoofDaysData="2";
					}
					else{
						 NoofDaysData="1";
					}
					CreateConsolidatedReportAWB( cabinetName, sessionID,NoofDaysData,sJtsIp,iJtsPort);
					NextReportExecutionDate = CurrentDate.plusDays(1);
				}
			}
			//end

		} catch (Exception e)

		{
			logger.debug("Exception: " + e.getMessage());
		}
	}

	private static void CreateConsolidatedReportAWB(String cabinetName,String sessionId,String NoofDaysData,String sJtsIp, String iJtsPort) throws Exception{
		try {
			logger.debug("Inside CreateConsolidatedReportAWB: ");
			XSSFWorkbook workbook=new XSSFWorkbook();
			XSSFSheet spreadsheet =workbook.createSheet("data");
			XSSFRow row;
			Map<Integer ,Object[]> data=new TreeMap<Integer ,Object[]>();
			int keyvalue=1;
			data.put(keyvalue, new Object[]{"Prospect_ID","Customer_name","mobile_No","ChequeBk_Req","ChequeBk_ref","ECRN","card_req","singlepager_ref_no","AWB_Number","Status","is_ntb","EmiratesID","onepager_moved","onePager_movement_date","is_prime_req","Account_no"});
			
			String DBQuery_2 ="select prospect_ID,Customer_name,mobile_No,ChequeBk_Req,ChequeBk_ref,ECRN,card_req,singlepager_ref_no,AWB_Number,Status,is_ntb,EmiratesID,onepager_moved,onePager_movement_date,Account_no from NG_Digital_AWB_Status where AWB_Number is not null and AWB_Gen_success_date between " 
					+ " getdate()-"+NoofDaysData+" and getdate() and ProcessName='DigitalPL' ";
			
				String extTabDataIPXML_2 =CommonMethods.apSelectWithColumnNames(DBQuery_2, CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false));
				logger.debug("extTabDataIPXML: " + extTabDataIPXML_2);
				String extTabDataOPXML_2 = CommonMethods.WFNGExecute(extTabDataIPXML_2, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
				logger.debug("extTabDataOPXML: " + extTabDataOPXML_2);
		    
				XMLParser xmlParserData_2 = new XMLParser(extTabDataOPXML_2);
				int iTotalrec_2 = Integer.parseInt(xmlParserData_2.getValueOf("TotalRetrieved"));
		    
		    if (xmlParserData_2.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_2 > 0)
		    {
		    	String xmlDataExtTab = xmlParserData_2.getNextValueOf("Record");
		        xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
		        NGXmlList objWorkList = xmlParserData_2.createList("Records", "Record");
		
		        for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
		        {
		        	keyvalue+=1;
		        	String  Prospect_ID= objWorkList.getVal("Prospect_ID");
		        	String  Customer_name= objWorkList.getVal("Customer_name");
		        	String mobile_No = objWorkList.getVal("mobile_No");
		        	String ChequeBk_Req = objWorkList.getVal("ChequeBk_Req");
		        	String ChequeBk_ref = objWorkList.getVal("ChequeBk_ref");
		        	String ECRN = objWorkList.getVal("ECRN");
		        	String  card_req= objWorkList.getVal("card_req");
		        	String singlepager_ref_no = objWorkList.getVal("singlepager_ref_no");
		        	String AWB_Number = objWorkList.getVal("AWB_Number");
		        	String Status= objWorkList.getVal("Status");
		        	String is_ntb= objWorkList.getVal("is_ntb");
		        	String EmiratesID= objWorkList.getVal("EmiratesID");
		        	String onepager_moved= objWorkList.getVal("onepager_moved");
		        	String onePager_movement_date= objWorkList.getVal("onePager_movement_date");
	        		String Account_no= objWorkList.getVal("Account_no");
	        		String is_prime_req=card_req;
		        	
		        	data.put(keyvalue, new Object[]{Prospect_ID,Customer_name,mobile_No,ChequeBk_Req,ChequeBk_ref,ECRN,card_req,singlepager_ref_no,AWB_Number,Status,is_ntb,EmiratesID,onepager_moved,onePager_movement_date,is_prime_req,Account_no});
		        }
		    }
		    
			Set<Integer> keyid=data.keySet();
			int rowid=0;
			for(int key:keyid){
				row=spreadsheet.createRow(rowid++);
			    Object[] objarr=data.get(key);
			    int cellid=0;
			    
				for(Object obj:objarr){
					Cell cell=row.createCell(cellid++);
					cell.setCellValue((String)obj);
					
				}
			}
		
			DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			String ReportDatetime = dateFormat.format(new Date());
			
			OnePagetTempReportPath=OnePagetTempReportPath+ File.separator +ReportDatetime;
			File ReportFolder = new File(OnePagetTempReportPath);
			if (!ReportFolder.exists()) {
				ReportFolder.mkdirs();
			}
			OnePagetTempReportPath=OnePagetTempReportPath+File.separator+OnePagerReportName+".xlsx";
			File finalFolder = new File(OnePagetTempReportPath);
			if (finalFolder.exists()) {
				File fDumpFolder = new File(OnePagetTempReportPath);
				fDumpFolder.delete();
			}
			
					
			FileOutputStream out=new FileOutputStream(new File(OnePagetTempReportPath));
			
			workbook.write(out);
			out.close();
			String query_docIndex="select  DocumentIndex,ParentFolderIndex from PDBDocumentContent where ParentFolderIndex =(select "
					+ "FolderIndex from PDBFolder where Name = '"+OnePagerReportOdFolderName+"')  and FiledDatetime<getdate()-3";
			
			
			String query_docIndex_IPXML =CommonMethods.apSelectWithColumnNames(query_docIndex, CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false));
			logger.debug("extTabDataIPXML: " + query_docIndex_IPXML);
			String query_docIndex_OUTXML = CommonMethods.WFNGExecute(query_docIndex_IPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
			logger.debug("extTabDataOPXML: " + query_docIndex_OUTXML);
			
			XMLParser xmlParserData_docIndex = new XMLParser(query_docIndex_OUTXML);
			int iTotalrec_docIndex = Integer.parseInt(xmlParserData_docIndex.getValueOf("TotalRetrieved"));
			String DocumentIndex="",ParentFolderIndex="", doc="";
	      	
			if (xmlParserData_docIndex.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_docIndex > 0) {
				String xmlDataExtTab = xmlParserData_docIndex.getNextValueOf("Record");
				xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
				NGXmlList objWorkList = xmlParserData_docIndex.createList("Records", "Record");
				
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
					DocumentIndex= objWorkList.getVal("DocumentIndex");
					ParentFolderIndex= objWorkList.getVal("ParentFolderIndex");
					doc=doc+"<Document>"
							+ "<DocumentIndex>"+DocumentIndex+"</DocumentIndex>\n"
							+ "<ParentFolderIndex>"+ParentFolderIndex+"</ParentFolderIndex>\n"
							+ "</Document>";
					}
			    }
			    
			if(!doc.equalsIgnoreCase("")) {
				
				String ngodeleteInput="<?xml version=\"1.0\"?>"
						+ "<NGODeleteDocumentExt_Input>"
						+ "<Option>NGODeleteDocumentExt</Option>"
						+ "<CabinetName>"+cabinetName+"</CabinetName>"
						+ "<UserDBId>"+sessionId+"</UserDBId>"
						+ "<Documents>"+doc+"</Documents>"
						+ "</NGODeleteDocumentExt_Input>";
			    	
				String ngodelete_OutputXml= CommonMethods.WFNGExecute(ngodeleteInput, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
				logger.debug(" Output xml For NGOAddDocument Call: "+ngodelete_OutputXml);
					
				String statusXML_deleteXml = CommonMethods.getTagValues(ngodelete_OutputXml,"Status");
				String ErrorMsg_deleteXml = CommonMethods.getTagValues(ngodelete_OutputXml,"Error");
					
				logger.debug(" statusXML: "+statusXML_deleteXml);
				logger.debug(" ErrorMsg: "+ErrorMsg_deleteXml);
					
				if(statusXML_deleteXml.equalsIgnoreCase("0")) {
					logger.debug(" The documents older than 3 days has been deleted successfully: ");
				}
			}
			
			logger.debug("OnePagetTempReportPath: "+OnePagetTempReportPath);
			String docPath=OnePagetTempReportPath;
			JPISIsIndex ISINDEX = new JPISIsIndex();
			JPDBRecoverDocData JPISDEC = new JPDBRecoverDocData();
			logger.debug("befor add document mt successful:");
			CPISDocumentTxn.AddDocument_MT(null, sJtsIp , Short.parseShort(smsPort), cabinetName, Short.parseShort(volumeID), docPath, JPISDEC, "",ISINDEX);
			logger.debug("After add document mt successful: ");
			String sISIndex = ISINDEX.m_nDocIndex + "#" + ISINDEX.m_sVolumeId;
			logger.debug(" sISIndex: "+sISIndex);
			String DocumentType = "N";
			String strDocumentName=OnePagerReportName;
			String DocNameAsProcess=OnePagerReportName;
			String strExtension="xlsx";
			
			File file=new File(OnePagetTempReportPath);
			long lLngFileSize = 0L;
			lLngFileSize = file.length();		
			String lstrDocFileSize = Long.toString(lLngFileSize);
			
			String sMappedInputXml = CommonMethods.getNGOAddDocument(FolderIndex,strDocumentName,DocumentType,strExtension,sISIndex,lstrDocFileSize,volumeID,cabinetName,sessionId);
			logger.debug("sMappedInputXml "+sMappedInputXml);
					
			String sOutputXml= CommonMethods.WFNGExecute(sMappedInputXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
			sOutputXml=sOutputXml.replace("<Document>","");
			sOutputXml=sOutputXml.replace("</Document>","");
			logger.debug(" Output xml For NGOAddDocument Call: "+sOutputXml);
			
			String statusXML = CommonMethods.getTagValues(sOutputXml,"Status");
			String ErrorMsg = CommonMethods.getTagValues(sOutputXml,"Error");
			
			logger.debug(" The maincode of the output xml file is " +statusXML);
			if(statusXML.equalsIgnoreCase("0")) {
				
			String query="Select top 1 ISnull(ImageIndex,'') as ImageIndex,ISnull(concat(NAME,'.',AppName),'') as ATTACHMENTNAMES, volumeId from pdbdocument with (nolock) "
				+ "WHERE DocumentIndex in (select DocumentIndex from PDBDocumentContent where ParentFolderIndex =(select FolderIndex from PDBFolder where Name = '"+OnePagerReportOdFolderName+"'))and"
				+ " name like '"+strDocumentName+"%' order by DocumentIndex desc";
				
			String extTabDataIPXML_3 =CommonMethods.apSelectWithColumnNames(query, CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false));
			logger.debug("extTabDataIPXML: " + extTabDataIPXML_3);
			String extTabDataOPXML_3 = CommonMethods.WFNGExecute(extTabDataIPXML_3, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
			logger.debug("extTabDataOPXML: " + extTabDataOPXML_3);
				    
			XMLParser xmlParserData_3 = new XMLParser(extTabDataOPXML_3);
			int iTotalrec_3 = Integer.parseInt(xmlParserData_3.getValueOf("TotalRetrieved"));
			String  ImageIndex="",ATTACHMENTNAMES="",volumeId="";
		        	
			if (xmlParserData_3.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_3 > 0) {
				
			  	String xmlDataExtTab = xmlParserData_3.getNextValueOf("Record");
				xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
				NGXmlList objWorkList = xmlParserData_3.createList("Records", "Record");
				
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
						ImageIndex= objWorkList.getVal("ImageIndex");
						ATTACHMENTNAMES= objWorkList.getVal("ATTACHMENTNAMES");
						volumeId= objWorkList.getVal("volumeId");    
				    }  
			}
				    
				String wfattachmentNames=ATTACHMENTNAMES+";";
				String wfattachmentIndex=ImageIndex+"#"+volumeId+"#;";
					//String processDefId=processDefId;
				
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
				String insertedDateTime = simpleDateFormat.format(new Date());
				DateFormat dateFormatnew = new SimpleDateFormat("dd-MM-yyyy");
				String ReportDate = dateFormat.format(new Date());
				String MailSubject=OnePagerReportSubject+ReportDate;
				String FinalMailStr=OnePagerReportBody;
				
				String columnName = "MAILFROM,MAILTO,MAILSUBJECT,MAILMESSAGE,MAILCONTENTTYPE,MAILPRIORITY,MAILSTATUS,INSERTEDBY,MAILACTIONTYPE,INSERTEDTIME,PROCESSDEFID,PROCESSINSTANCEID,WORKITEMID,ACTIVITYID,NOOFTRIALS,attachmentNames,attachmentISINDEX";
				String strValues = "'" + OnePagerReportFromMail + "','" + OnePagerReportToMail + "',N'" + MailSubject + "',N'" + FinalMailStr
						+ "','text/html;charset=UTF-8','1','N','CUSTOM','TRIGGER','" + insertedDateTime + "','"
						+ ProcessDefId + "','DPL Consolidatd Report','1','1','0','"+wfattachmentNames+"','"+wfattachmentIndex+"'";
	
				String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionId, columnName, strValues,	"WFMAILQUEUETABLE");
				logger.debug("APInsertInputXML: " + apInsertInputXML);
	
				String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, jtsIP,  iJtsPort, 1);
				logger.debug("APInsertOutputXML: " + apInsertOutputXML);
	
				XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
				String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
				logger.debug("Status of apInsertMaincode  " + apInsertMaincode);
	
				if (apInsertMaincode.equalsIgnoreCase("0")) {
					logger.debug("ApInsert successful: " + apInsertMaincode);
					logger.debug("Inserted in Wimailquque  table successfully.");
				}
	
				else {
					logger.debug("ApInsert failed: " + apInsertMaincode);
				}
	
			}
		}
		catch(	Exception ex)
		{
			logger.debug("createPDF : createNewPDF : ex.getMessage() : 2 :" + ex.getMessage());
			
		} catch (JPISException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void insert_ng_digital_awb_status(String Wi_name, String cabinetName, String sessionId, String sJtsIp,
			String iJtsPort, String ActivityName) {
		try {
			String process_name = "";
			final HashMap<String, String> CheckGridDataMap_awb = new HashMap<String, String>();
			
			String DBQuery_awb = "select WiName,ProspectID,FirstName,MiddleName,LastName,MobileNo,EmailID,AWB_Number,IsNTB,ECRN,EmiratesID,isDBCardReq,IsCBReq,AccountNumber,ChequeBk_ref,CardOps_Reschedule from NG_DPL_EXTTABLE where WIName='"
					+ Wi_name + "'";

			logger.debug("DBQuery_awb: " + DBQuery_awb);
			
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
					CheckGridDataMap_awb.put("account_no", objWorkList.getVal("AccountNumber"));
					CheckGridDataMap_awb.put("ChequeBk_Req", objWorkList.getVal("IsCBReq"));
					CheckGridDataMap_awb.put("AWB_status", "R");
					CheckGridDataMap_awb.put("EmirateID", objWorkList.getVal("EmiratesID"));
					CheckGridDataMap_awb.put("is_prime_req", objWorkList.getVal("isDBCardReq"));
					CheckGridDataMap_awb.put("ChequeBk_ref", objWorkList.getVal("ChequeBk_ref"));
					CheckGridDataMap_awb.put("CardOps_Reschedule", objWorkList.getVal("CardOps_Reschedule"));
				}

				String processname[] = Wi_name.split("-");
				logger.debug("processname [] : " + processname[0]);
				process_name = processname[0];
				logger.debug("processname [] : " + process_name);
			}

			

			String Query_for_cardserno = "select CardSerno,ELITE_CRN from NG_DPL_PRIME_COURIER with (nolock) where WI_name='"
					+ Wi_name + "'";
			String extTabDataIPXML_prime_cor = CommonMethods.apSelectWithColumnNames(Query_for_cardserno, cabinetName,
					sessionID);
			logger.debug("extTabDataIPXML_prime_cor: " + extTabDataIPXML_prime_cor);
			
			String extTabDataOPXML_prime_cor = CommonMethods.WFNGExecute(extTabDataIPXML_prime_cor,
					CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
			logger.debug(" extTabDataOPXML_prime_cor " + extTabDataOPXML_prime_cor);
			
			XMLParser xmlParserData_prime_cor = new XMLParser(extTabDataOPXML_prime_cor);
			
			int iTotalrec_prime_cor = Integer.parseInt(xmlParserData_prime_cor.getValueOf("TotalRetrieved"));
			logger.debug("iTotalrec_wi_table " + iTotalrec_prime_cor);

			if (xmlParserData_prime_cor.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_prime_cor > 0) {
				String xmlDataExtTab = xmlParserData_prime_cor.getNextValueOf("Record");
				xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
				NGXmlList objWorkList = xmlParserData_prime_cor.createList("Records", "Record");

				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
					CheckGridDataMap_awb.put("CardSerno", objWorkList.getVal("CardSerno"));
					CheckGridDataMap_awb.put("ELITE_CRN", objWorkList.getVal("ELITE_CRN"));
					
				}
				logger.debug(" CardSerno " + CheckGridDataMap_awb.get("CardSerno").trim()
						+ " for  DPL ELITE_CRN " + CheckGridDataMap_awb.get("ELITE_CRN").trim());
			}

			Date d = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String sDate = dateFormat.format(d);
			logger.debug("insert_ng_digital_awb_status : sDate " + sDate);
			System.out.println();
			String prospect_id = CheckGridDataMap_awb.get("Prospect_id").trim();
			String Full_name = CheckGridDataMap_awb.get("FirstName").trim() + " "
					+ CheckGridDataMap_awb.get("MiddleName").trim() + " " + CheckGridDataMap_awb.get("LastName").trim();

			
			String ChequeBk_ref = "";
			if (CheckGridDataMap_awb.containsKey("ChequeBk_ref") && !CheckGridDataMap_awb.get("ChequeBk_ref").equals("null")) {
				ChequeBk_ref = CheckGridDataMap_awb.get("ChequeBk_ref").trim();
				logger.debug("ChequeBk_ref " + ChequeBk_ref);
			}
			String ECRN = "";
			if (CheckGridDataMap_awb.containsKey("ECRN") && !CheckGridDataMap_awb.get("ECRN").equals("null")) {
				ECRN = CheckGridDataMap_awb.get("ECRN").trim();
			}
			String account_no = "";
			if (CheckGridDataMap_awb.containsKey("account_no")) {
				account_no = CheckGridDataMap_awb.get("account_no").trim();
			}

			String CardSerno = "";
			if (CheckGridDataMap_awb.containsKey("CardSerno")) {
				CardSerno = CheckGridDataMap_awb.get("CardSerno").trim();
			}

			String CardOps_Reschedule = "";
			if (CheckGridDataMap_awb.containsKey("CardOps_Reschedule") && !CheckGridDataMap_awb.get("CardOps_Reschedule").equals("null")) {
				CardOps_Reschedule = CheckGridDataMap_awb.get("CardOps_Reschedule").trim();
			}

			logger.debug("insert_ng_digital_awb_status DPL : ECRN " + ECRN);
			logger.debug("insert_ng_digital_awb_status : prospect_id " + prospect_id);
			logger.debug("insert_ng_digital_awb_status DPL  : Full_name " + Full_name);
			logger.debug("insert_ng_digital_awb_status : ChequeBk_ref " + ChequeBk_ref);
			logger.debug("insert_ng_digital_awb_status : account_no " + account_no);
			String columnNames_awbTable = "";
			String columnValues_awbTable = "";

			if (CardOps_Reschedule.equalsIgnoreCase("Y")) {
				
				columnNames_awbTable = "WI_name,Prospect_ID,Customer_name,mobile_No,email_id,ECRN,AWB_Number,"
						+ "processName,singlePager_ref_no,AWB_Gen_success_date,is_Ntb,CardSerno,card_req,EmiratesID,"
						+ "ChequeBk_Req,ChequeBk_ref,Status,Account_no";
				columnValues_awbTable = "'" + Wi_name + "','" + prospect_id + "','" + Full_name + "','"
						+ CheckGridDataMap_awb.get("MobileNo").trim() + "','"
						+ CheckGridDataMap_awb.get("email_id").trim() + "','" + ECRN.trim() + "','"
						+ CheckGridDataMap_awb.get("AWB_Number").trim() + "','" + process_name + "','" + Wi_name + "','"
						+ sDate + "','" + CheckGridDataMap_awb.get("ntb").trim() + "','" + CardSerno + "'" + ",'N','"
						+ CheckGridDataMap_awb.get("EmirateID").trim()+ "','" 
						+ 'N'+"','" +ChequeBk_ref.trim()+ "','" 
						+ CheckGridDataMap_awb.get("AWB_status").trim() + "','"
						+ account_no.trim() + "'";
			} else {
		
				/*columnNames_awbTable = "WI_name,Prospect_ID,Customer_name,mobile_No,email_id,ECRN,AWB_Number,processName,singlePager_ref_no,AWB_Gen_success_date,is_Ntb,CardSerno,EmiratesID";
				columnValues_awbTable = "'" + Wi_name + "','" + prospect_id + "','" + Full_name + "','"
						+ CheckGridDataMap_awb.get("MobileNo").trim() + "','"
						+ CheckGridDataMap_awb.get("email_id").trim() + "','" + ECRN.trim() + "','"
						+ CheckGridDataMap_awb.get("AWB_Number").trim() + "','" + process_name + "','" + Wi_name + "','"
						+ sDate + "','" + CheckGridDataMap_awb.get("ntb").trim() + "','" + "" + "','"
						+ CheckGridDataMap_awb.get("EmirateID").trim() + "'";*/
				columnNames_awbTable = "WI_name,Prospect_ID,Customer_name,mobile_No,email_id,ChequeBk_Req,ChequeBk_ref,ECRN,AWB_Number,Status,card_req,processName, singlePager_ref_no,Account_no,AWB_Gen_success_date,is_Ntb,CardSerno,EmiratesID";
				columnValues_awbTable = "'" + Wi_name + "','" + prospect_id + "','" + Full_name + "','"
						+ CheckGridDataMap_awb.get("MobileNo").trim() + "','"
						+ CheckGridDataMap_awb.get("email_id").trim() + "','"
						+ CheckGridDataMap_awb.get("ChequeBk_Req").trim() + "','" + ChequeBk_ref.trim() + "','"
						+ ECRN.trim() + "','" + CheckGridDataMap_awb.get("AWB_Number").trim() + "','"
						+ CheckGridDataMap_awb.get("AWB_status").trim() + "','"
						+ CheckGridDataMap_awb.get("is_prime_req").trim() + "','" + process_name + "','" + Wi_name + "','"
						+ account_no.trim() + "','" + sDate + "','" + CheckGridDataMap_awb.get("ntb").trim() + "','"
						+ CardSerno + "','" + CheckGridDataMap_awb.get("EmirateID").trim() + "'";
			}
				
				
			logger.debug("insert_ng_digital_awb_status : columnNames_awbTable DPL " + columnNames_awbTable);
			logger.debug("insert_ng_digital_awb_status : columnValues_awbTable DPL " + columnValues_awbTable);

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
	
private void ValidatePrimeCBSFile(String Wi_name, String sJtsIp,String iJtsPort,String cabinetName){
		
		try{
			
			String ECRN="",ChequeBk_ref="",cardserno="",Request_No="",Account_Number="";
			
			//String Query = "select top 1 ecrn as 'ECRN', WI_name as 'Wi_name' , ChequeBk_ref as 'ChequeBk_ref'  from NG_DAO_WI_UPDATE with(nolock) where Wi_name = '"+Wi_name+"'";
			String Query = "select ECRN as 'ECRN', WIname as 'Wi_name' , ChequeBk_ref as 'ChequeBk_ref'  from NG_DPL_EXTTABLE with(nolock) where Winame = '"+Wi_name+"'";
			
			String extTabDataIPXML=CommonMethods.apSelectWithColumnNames(Query, cabinetName,sessionID);
			logger.debug("Output: "+extTabDataIPXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
			logger.debug(" extTabDataOPXML "+ extTabDataOPXML);
	
			XMLParser xmlParserData= new XMLParser(extTabDataOPXML);		
			int iTotalrec1 = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
			
			if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec1>0) {
				String xmlDataExtTab=xmlParserData.getNextValueOf("Record");
				xmlDataExtTab=xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
				//NGXmlList objWorkList=xmlParserData.createList("Records", "Record");
				
				//for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
					ECRN = xmlParserData.getValueOf("ECRN");
					Wi_name = xmlParserData.getValueOf("Wi_name");
					ChequeBk_ref= xmlParserData.getValueOf("ChequeBk_ref");
				//}
			}
			
			logger.debug("ECRN: "+ECRN);
			logger.debug("ChequeBk_ref: "+ChequeBk_ref);
			
			String Query_prime = "Select top 1 CardSerno,ECRN_CRN from NG_DPL_PRIME_COURIER with(nolock) where (CardSerno='"+ECRN+"' or ECRN_CRN = '"+ECRN+"') and (wi_name is null or wi_name='')";
			
			String extTabDataIPXML_prime=CommonMethods.apSelectWithColumnNames(Query_prime, cabinetName,sessionID);
			logger.debug("extTabDataIPXML_prime: "+extTabDataIPXML_prime);
			String extTabDataOPXML_prime = CommonMethods.WFNGExecute(extTabDataIPXML_prime,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
			logger.debug(" extTabDataOPXML_prime : prime "+ extTabDataOPXML_prime);
	
			XMLParser xmlParserData_prime= new XMLParser(extTabDataOPXML_prime);
			int iTotalrec2 = Integer.parseInt(xmlParserData_prime.getValueOf("TotalRetrieved"));
			
			if(xmlParserData_prime.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec2>0) {
				
				String xmlDataExtTab=xmlParserData_prime.getNextValueOf("Record");
				xmlDataExtTab=xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
				NGXmlList objWorkList=xmlParserData.createList("Records", "Record");
				
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
						cardserno = objWorkList.getVal("CardSerno").trim();
					}
				logger.debug("cardserno: "+cardserno);
				
				logger.debug("Updating the prime received flag...");
				// UPDATE
				updateExternalTable("NG_DPL_EXTTABLE", "Is_prime", "'Y'","WI_name='" + Wi_name + "'", sJtsIp, iJtsPort, cabinetName);
				updateExternalTable("NG_DPL_PRIME_COURIER", "wi_name", "'"+Wi_name+"'","CardSerno='" + ECRN + "' or ECRN_CRN='"+ECRN+"'", sJtsIp, iJtsPort, cabinetName);
			}
			
			String Query_cbs = "Select top 1 Request_No,Account_Number from NG_DPL_CBS_FILE where Request_No='"+ChequeBk_ref+"' and (wi_name='')";
			
			String extTabDataIPXML_cbs=CommonMethods.apSelectWithColumnNames(Query_cbs, cabinetName,sessionID);
			logger.debug("extTabDataIPXML_cbs: "+extTabDataIPXML_cbs);
			String extTabDataOPXML_cbs = CommonMethods.WFNGExecute(extTabDataIPXML_cbs,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
			logger.debug(" extTabDataOPXML_cbs : "+ extTabDataOPXML_cbs);
	
			XMLParser xmlParserData_cbs= new XMLParser(extTabDataOPXML_cbs);		
			int iTotalrec3 = Integer.parseInt(xmlParserData_cbs.getValueOf("TotalRetrieved"));
			
			if(xmlParserData_prime.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec3>0) {
				String xmlDataExtTab=xmlParserData_prime.getNextValueOf("Record");
				xmlDataExtTab=xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
				NGXmlList objWorkList=xmlParserData.createList("Records", "Record");
				
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
						Request_No = objWorkList.getVal("Request_No").trim();
						Account_Number = objWorkList.getVal("Account_Number").trim();
					}
				logger.debug("Request_No: "+Request_No);
				logger.debug("Account_Number: "+Account_Number);
				
				logger.debug("Updating the CBS received flag...");
				// UPDATE
				updateExternalTable("NG_DPL_EXTTABLE", "isCBSRec", "'Y'", "WI_name='" + Wi_name + "'", sJtsIp, iJtsPort, cabinetName);
				updateExternalTable("NG_DPL_CBS_FILE", "wi_name", "'"+Wi_name+"'", "Request_No='" + ChequeBk_ref + "'", sJtsIp, iJtsPort, cabinetName);
			}			
		}
		
		catch (Exception e) {
			logger.debug("Final Exception ValidatePrimeCBSFile" + e.getStackTrace());
			logger.debug("Final Exception ValidatePrimeCBSFile" + e.getMessage());
		}
	}

}
