package com.newgen.DPL;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.newgen.DCC.DECTECHIntegration.MapXML;
import com.newgen.DCC.EFMS.DCC_EFMS_Integration;
import com.newgen.DCC.EFMS.DCC_EFMS_IntegrationLog;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.omni.wf.util.excp.NGException;

public class Digital_PL_EFMS {

	static Map<String, String> DCCSystemIntegrationMap = new HashMap<String, String>();
	static NGEjbClient ngEjbClient;
	
	String sessionID = "";
	String cabinetName = "";
	static String jtsIP = "";
	String jtsPort = "";
	String queueID = "";
	int socketConnectionTimeout = 0;
	int integrationWaitTime = 0;
	int sleepIntervalInMin = 0;
	int Utility_retry_count = 0; // Read from the config file
	int TrialTime = 0;
	int ErrorCount = 0;
	static String fromMailID = "";
	static String toMailID = "";
	Date now = null;
	public String sdate = "";
	public String file;
	public String newFilename = null;
	public String msg;
	String updateStatus = null;
	static String tableUpdate = "NG_DPL_EFMS_RESPONSE";
	public static int sessionCheckInt = 0;
	public static int loopCount = 50;
	public static int waitLoop = 50;
	static String txtFilePath = null;
	public String TimeStamp = "";
	public static String sourceDestinaton = "";
	public static String successPath = "";
	public static String errorPath = "";
	public static String sourcePath = "";
	public static String inProgressPath = "";
	public static String movedPath = "";
	public static String failPath = "";
	public String filePath = "";
	public static String inputPath = "";
	public static String ApplicationFileName = "";
	public static String AlertFileName = "";
	public static String AlertedUpdateColumn = "";
	public static String ApplicationInsertColumn = "";
	public static String DPLExtTable = "NG_DPL_EXTTABLE";
	public static String DCCExtTableFlag = "";
	public static String ActivityName = "";
	private static String ActivityID = "";
	private static String ProcessDefId = "";
	private static org.apache.log4j.Logger logger;
	
	public Digital_PL_EFMS() throws NGException{
		this.sourceDestinaton = DCC_EFMS_Integration.DCCSystemIntegrationMap.get("ProcessingFilePath");
		this.successPath=DCC_EFMS_Integration.DCCSystemIntegrationMap.get("SuccessPath");
		this.errorPath=DCC_EFMS_Integration.DCCSystemIntegrationMap.get("FailPath");
		this.inputPath=DCC_EFMS_Integration.DCCSystemIntegrationMap.get("InputPath");
		this.txtFilePath =DCC_EFMS_Integration.DCCSystemIntegrationMap.get("DPLTxtFilePath");
		this.tableUpdate =DCC_EFMS_Integration.DCCSystemIntegrationMap.get("dplTableUpdate");
		this.ApplicationFileName =DCC_EFMS_Integration.DCCSystemIntegrationMap.get("ApplicationFileName");
		this.AlertFileName =DCC_EFMS_Integration.DCCSystemIntegrationMap.get("AlertFileName");
		this.AlertedUpdateColumn =DCC_EFMS_Integration.DCCSystemIntegrationMap.get("AlertedUpdateColumn");
		this.ApplicationInsertColumn =DCC_EFMS_Integration.DCCSystemIntegrationMap.get("ApplicationInsertColumn");
		this.DPLExtTable =DCC_EFMS_Integration.DCCSystemIntegrationMap.get("dplExtTable");
		this.DCCExtTableFlag =DCC_EFMS_Integration.DCCSystemIntegrationMap.get("DCCExtTableFlag");
		this.fromMailID =DCC_EFMS_Integration.DCCSystemIntegrationMap.get("FromMailID");
		this.toMailID =DCC_EFMS_Integration.DCCSystemIntegrationMap.get("ToMailID");
		this.queueID = DCC_EFMS_Integration.DCCSystemIntegrationMap.get("dplqueueID");
		this.socketConnectionTimeout = Integer.parseInt(DCC_EFMS_Integration.DCCSystemIntegrationMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
		
		this.jtsPort = CommonConnection.getJTSPort();
		this.jtsIP = CommonConnection.getJTSIP();
		this.cabinetName = CommonConnection.getCabinetName();
		Digital_PL_Log.setLogger(getClass().getSimpleName());
		this.ngEjbClient = NGEjbClient.getSharedInstance();
		logger = Digital_PL_Log.getLogger(getClass().getSimpleName());
		

	}
	public void insertDataInEFMSTable(String cabinetName, String sJtsIp, String iJtsPort, String sessionId,
			String queueID, String processInstanceId, String lastWorkItemId, String entryDateTime_str) {

		final String ws_name = "Sys_Int_EFMS";
		String processingFlag = "";
		String errorType = "";
		String entryDateTime = "";
		try {

			//Digital_PL_Log.setLogger("DPL_EFMSLog.xml");
			// Validate Session ID
			sessionId = CommonConnection.getSessionID(logger, false);

			logger.info("SessionId:" + sessionId);
			if (sessionId == null || sessionId.equalsIgnoreCase("") || sessionId.equalsIgnoreCase("null")) {
				logger.error("Could Not Get Session ID " + sessionId);
				return;
			}

			// Fetch all Work-Items on given queueID.
			logger.debug("Fetching all Workitems on DCC_SysCheckIntegration queue");
			System.out.println("Fetching all Workitems on EFMS queue");

			// String fetchWorkitemListInputXML =
			// CommonMethods.fetchWorkItemsInput(cabinetName, sessionId,
			// queueID);
			String fetchWorkitemListInputXML = CommonMethods.getFetchWorkItemsInputXML(processInstanceId,
					lastWorkItemId, sessionId, cabinetName, queueID, entryDateTime_str);
			logger.debug("InputXML for fetchWorkList Call: " + fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML = CommonMethods.WFNGExecute(fetchWorkitemListInputXML, sJtsIp, iJtsPort,
					1);
			logger.debug("WMFetchWorkList OutputXML: " + fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			logger.debug("FetchWorkItemListMainCode: " + fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			logger.debug("RetrievedCount for WMFetchWorkList Call: " + fetchWorkitemListCount);
			// logger.debug("Number of workitems
			// retrieved on EFMS: " + fetchWorkitemListCount);
			System.out.println("Number of workitems retrieved on EFMS: " + fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0) {
				for (int i = 0; i < fetchWorkitemListCount; i++) {
					String fetchWorkItemlistData = xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData = fetchWorkItemlistData.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

					logger
							.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: " + fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID = xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					// logger.debug("Current
					// ProcessInstanceID: " + processInstanceID);

					// logger.debug("Processing Workitem:
					// " + processInstanceID);
					System.out.println("\nProcessing Workitem: " + processInstanceID);

					String WorkItemID = xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					// logger.debug("Current WorkItemID:
					// " + WorkItemID);

					entryDateTime = xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					// logger.debug("Current
					// EntryDateTime: " + entryDateTime);

					ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					// logger.debug("ActivityID: " +
					// ActivityID);

					String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					// logger.debug("ActivityType: " +
					// ActivityType);

					ActivityName = xmlParserfetchWorkItemData.getValueOf("ActivityName");
					// logger.debug("ActivityName: " +
					// ActivityName);

					ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					// logger.debug("ProcessDefId: " +
					// ProcessDefId);

					// commented by rubi
					/*
					 * String IS_virtual_Card_Created=""; String
					 * DBQuery_VirtualCard =
					 * "select IS_virtual_Card_Created,Wi_Name from NG_DCC_EXTTABLE with(nolock) WHERE WI_NAME='"
					 * + processInstanceID + "'"; String
					 * extTabDataIPXML_VirtualCard =
					 * CommonMethods.apSelectWithColumnNames(
					 * DBQuery_VirtualCard, CommonConnection.getCabinetName(),
					 * CommonConnection.getSessionID(Digital_PL_Log.
					 * digital_PL_Log, false));
					 * logger.debug(
					 * "extTabDataIPXML_VirtualCard: " +
					 * extTabDataIPXML_VirtualCard); String
					 * extTabDataOPXML_VirtualCard =
					 * CommonMethods.WFNGExecute(extTabDataIPXML_VirtualCard,
					 * CommonConnection.getJTSIP(),
					 * CommonConnection.getJTSPort(), 1);
					 * logger.debug(
					 * "extTabDataOPXML_VirtualCard: " +
					 * extTabDataOPXML_VirtualCard); XMLParser
					 * xmlParserData_VirtualCard = new
					 * XMLParser(extTabDataOPXML_VirtualCard); int
					 * iTotalrec_VirtualCard =
					 * Integer.parseInt(xmlParserData_VirtualCard.getValueOf(
					 * "TotalRetrieved")); String
					 * mainCode_VirtualCard=xmlParserData_VirtualCard.getValueOf
					 * ("MainCode"); if (mainCode_VirtualCard!=null &&
					 * !"".equalsIgnoreCase(mainCode_VirtualCard) &&
					 * mainCode_VirtualCard.equalsIgnoreCase("0")){
					 * if(iTotalrec_VirtualCard>0){
					 * IS_virtual_Card_Created=xmlParserData_VirtualCard.
					 * getValueOf("IS_virtual_Card_Created"); } }
					 * 
					 * logger.debug(
					 * "IS_virtual_Card_Created: " + IS_virtual_Card_Created);
					 * 
					 * /** Murabha execution **
					 * if("Y".equalsIgnoreCase(IS_virtual_Card_Created)) {
					 * String
					 * status=executeMurahabhaCalls(processInstanceID,ws_name,
					 * WorkItemID,entryDateTime,ActivityType);
					 * if("Error".equalsIgnoreCase(status)) continue; } /** We
					 * can remove this because its run for all the cases
					 **/
					// Deepak change done to fetch more than 100 cases
					if (i == 99) {
						processInstanceId = processInstanceID;
						lastWorkItemId = WorkItemID;
					}

					String application_number = processInstanceID.split("-")[1];
					String DBQuery = "SELECT STATUS FROM NG_DPL_EFMS_CONTROLTABLE with(nolock) WHERE WI_NAME='"
							+ application_number + "'";

					String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,
							CommonConnection.getCabinetName(),
							CommonConnection.getSessionID(logger, false));
					logger.debug("extTabDataIPXML: " + extTabDataIPXML);
					String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(),
							CommonConnection.getJTSPort(), 1);
					logger.debug("extTabDataOPXML: " + extTabDataOPXML);

					XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
					int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
					String mainCode = xmlParserData.getValueOf("MainCode");
					if (mainCode != null && !"".equalsIgnoreCase(mainCode) && mainCode.equalsIgnoreCase("0")) {
						if (iTotalrec == 0) {
							updateExttable(cabinetName, sJtsIp, iJtsPort, sessionId, processInstanceID);
							String applicant_resident_id = "";
							String application_type = "Credit Card";
							String application_status = "Waiting for Approval";
							String applicant_customer_id = "";
							String applicant_name = "";
							String applicant_first_name = "";
							String applicant_middle_name = "";
							String applicant_last_name = "";
							String applicant_dob = "";
							String applicant_residential_address_line1 = "";
							String applicant_residential_address_line2 = "";
							String applicant_residential_address_line3 = "";
							String applicant_residential_address_city = "";
							String applicant_residential_address_state = "";
							String applicant_residential_address_country = "";
							String applicant_residential_address_POST_BOX = "";
							String applicant_office_address_line1 = "";
							String applicant_office_address_line2 = "";
							String applicant_office_address_line3 = "";
							String applicant_office_address_city = "";
							String applicant_office_address_state = "";
							String applicant_office_address_country = "";
							String applicant_office_address_POST_BOX = "";
							String applicant_home_country_address_line1 = "";
							String applicant_home_country_address_line2 = "";
							String applicant_home_country_address_line3 = "";
							String applicant_home_country_city = "";
							String applicant_home_country_state = "";
							String applicant_home_country = "";
							String applicant_home_POST_BOX = "";
							String applicant_home_country_number = "";
							String applicant_mobile_number1 = "";
							String preferred_address = "";
							String applicant_nationality = "";
							String applicant_email = "";
							String applicant_occupation = "";
							String applicant_occupation_type = "";
							String applicant_employer_name = "";
							String applicant_designation = "";
							String applicant_passport_id = "";
							String applicant_passport_expiry_date = "";
							String applicant_salary = "";
							String applicant_total_income = "";
							String period_of_service = "";
							String product_category = "";
							String product_code = "";
							String limit_for_cards = "";
							String new_credit_limit_requested = "";
							String industry_segment_retail = "";
							String industry_sub_segment_retail = "";
							String application_request_datetime_stamp = "";
							String application_approval_datetime_stamp = "";
							String APP_CHANNEL_TYPE = "NEW";
							String FUTURE_PURPOSE_1 = "";
							String FUTURE_PURPOSE_2 = "";
							String FUTURE_PURPOSE_3 = "";
							String FUTURE_PURPOSE_4 = "";
							String FUTURE_PURPOSE_6 = "DIG";
							String FUTURE_PURPOSE_7 = "";
							String FUTURE_PURPOSE_8 = "YES";
							String FUTURE_PURPOSE_9 = "YES";
							String FUTURE_PURPOSE_10 = "";
							String REJECTED_BY_USER_OVRLD_FLG = "";
							String SOURCED_BY_DSA_OVRLD_FLG = "";
							String AECB_Score = "";
							String Name_On_EID = "";

							// Deepak Below 2
							// column(rm_user_id,applicant_customer_type) are
							// added for Application Dim table based on Simi
							// Email for Production correction.
							String rm_user_id = "";
							String applicant_customer_type = "INDIVIDUAL";

							// CIF replaced with Prospect_id Due to new changes
							// 07072023 - Discussed With Simi.

							DBQuery = "SELECT a.EmiratesID AS applicant_resident_id, a.ProspectID AS applicant_customer_id, CASE WHEN a.MiddleName "
									+ "is null THEN CONCAT(a.FirstName, ' ', a.LastName) ELSE CONCAT(a.FirstName,' ', a.MiddleName,' ', a.LastName) "
									+ "END AS applicant_name, a.FirstName AS applicant_first_name, a.MiddleName AS applicant_middle_name, a.LastName AS "
									+ "applicant_last_name, a.DOB AS applicant_dob, a.Nationality AS applicant_nationality,a.EmailID as applicant_email,  "
									+ "a.EmployerName AS applicant_employer_name, a.PassportNumber AS applicant_passport_id, a.PassportExpiryDate AS "
									+ "applicant_passport_expiry_date, a.CustomerDeclaredMonthlyIncome AS applicant_total_income, a.ProductName AS "
									+ "product_category, a.IPA_Amount AS limit_for_cards, a.ApprovedLoanAmt, "
									+ "a.RequestedLoanAmount AS new_credit_limit_requested, a.EmiratesExpiryDate AS FUTURE_PURPOSE_1, a.SalaryIBAN as "
									+ "FUTURE_PURPOSE_7,a.EmployerCode as FUTURE_PURPOSE_10, a.NameOnEID as applicant_name_as_per_EID, a.MobileNo as "
									+ "applicant_mobile_number1 from  NG_DPL_EXTTABLE a with(nolock) WHERE WINAME='"
									+ processInstanceID + "'";

							extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,
									CommonConnection.getCabinetName(),
									CommonConnection.getSessionID(logger, false));

							// logger.debug("extTabDataIPXML:
							// " + extTabDataIPXML);
							extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(),
									CommonConnection.getJTSPort(), 1);
							logger.debug("extTabDataOPXML: " + extTabDataOPXML);

							XMLParser xmlParserData_Ext = new XMLParser(extTabDataOPXML);
							int iTotalrec_Ext = Integer.parseInt(xmlParserData_Ext.getValueOf("TotalRetrieved"));

							if (xmlParserData_Ext.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_Ext > 0) {

								DBQuery = "select top 1 AddressType, HouseApartmentNo AS applicant_residential_address_line1,BuildingApartmentName AS "
										+ "applicant_residential_address_line2,StreetLocation AS applicant_residential_address_line3,CityTown AS "
										+ "applicant_residential_address_city,PostcodeZipCode AS applicant_residential_address_POST_BOX from "
										+ "NG_DPL_GR_DemographicDetails with(nolock) where winame = '"
										+ processInstanceID + "' and AddressType not like  '%Home%'";

								// DBQuery = "select top 1 Address_Type,
								// House_No AS
								// applicant_residential_address_line1,Building_Name
								// AS applicant_residential_address_line2,"
								// + "Street_Name AS
								// applicant_residential_address_line3,City_Desc
								// AS
								// applicant_residential_address_city,State_Desc
								// AS applicant_residential_address_state,"
								// + "Country_Desc AS
								// applicant_residential_address_country,PO_Box_Address
								// AS applicant_residential_address_POST_BOX
								// from "
								// + "NG_DCC_GR_ADDRESS_DETAIL with(nolock)
								// where wi_name = '" + processInstanceID + "'
								// and Address_Type not like '%Home%'";

								extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,
										CommonConnection.getCabinetName(),
										CommonConnection.getSessionID(logger, false));
								logger.debug("extTabDataIPXML: " + extTabDataIPXML);
								extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,
										CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
								logger.debug("extTabDataOPXML: " + extTabDataOPXML);

								XMLParser xmlParserData_Add = new XMLParser(extTabDataOPXML);
								int iTotalrec_add = Integer.parseInt(xmlParserData_Add.getValueOf("TotalRetrieved"));
								if (xmlParserData_Add.getValueOf("MainCode").equalsIgnoreCase("0")
										&& iTotalrec_add > 0) {
									preferred_address = xmlParserData_Add.getValueOf("AddressType");
									if ("Residence".equalsIgnoreCase(preferred_address)) {
										applicant_residential_address_line1 = xmlParserData_Add
												.getValueOf("applicant_residential_address_line1");
										applicant_residential_address_line2 = xmlParserData_Add
												.getValueOf("applicant_residential_address_line2");
										applicant_residential_address_line3 = xmlParserData_Add
												.getValueOf("applicant_residential_address_line3");
										applicant_residential_address_city = xmlParserData_Add
												.getValueOf("applicant_residential_address_city");
										// applicant_residential_address_state =
										// xmlParserData_Add.getValueOf("applicant_residential_address_state");
										// applicant_residential_address_country
										// =
										// xmlParserData_Add.getValueOf("applicant_residential_address_country");
										applicant_residential_address_POST_BOX = xmlParserData_Add
												.getValueOf("applicant_residential_address_POST_BOX");
									} else {
										applicant_office_address_line1 = xmlParserData_Add
												.getValueOf("applicant_residential_address_line1");
										applicant_office_address_line2 = xmlParserData_Add
												.getValueOf("applicant_residential_address_line2");
										applicant_office_address_line3 = xmlParserData_Add
												.getValueOf("applicant_residential_address_line3");
										applicant_office_address_city = xmlParserData_Add
												.getValueOf("applicant_residential_address_city");
										// applicant_office_address_state =
										// xmlParserData_Add.getValueOf("applicant_residential_address_state");
										// applicant_office_address_country =
										// xmlParserData_Add.getValueOf("applicant_residential_address_country");
										applicant_office_address_POST_BOX = xmlParserData_Add
												.getValueOf("applicant_residential_address_POST_BOX");
									}
								}

								DBQuery = "select top 1 AddressType, HouseApartmentNo AS applicant_residential_address_line1,BuildingApartmentName AS "
										+ "applicant_residential_address_line2,StreetLocation AS applicant_residential_address_line3,CityTown AS "
										+ "applicant_residential_address_city,PostcodeZipCode AS applicant_residential_address_POST_BOX from "
										+ "NG_DPL_GR_DemographicDetails with(nolock) where winame = '"
										+ processInstanceID + "' and AddressType like  '%Home%'";

								extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,
										CommonConnection.getCabinetName(),
										CommonConnection.getSessionID(logger, false));
								logger.debug("extTabDataIPXML: " + extTabDataIPXML);
								extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,
										CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
								logger.debug("extTabDataOPXML: " + extTabDataOPXML);

								XMLParser xmlParserData_HomeAdd = new XMLParser(extTabDataOPXML);
								iTotalrec_add = Integer.parseInt(xmlParserData_HomeAdd.getValueOf("TotalRetrieved"));
								if (xmlParserData_HomeAdd.getValueOf("MainCode").equalsIgnoreCase("0")
										&& iTotalrec_add > 0) {
									applicant_home_country_address_line1 = xmlParserData_HomeAdd
											.getValueOf("applicant_residential_address_line1");
									applicant_home_country_address_line2 = xmlParserData_HomeAdd
											.getValueOf("applicant_residential_address_line2");
									applicant_home_country_address_line3 = xmlParserData_HomeAdd
											.getValueOf("applicant_residential_address_line3");
									applicant_home_country_city = xmlParserData_HomeAdd
											.getValueOf("applicant_residential_address_city");
									// applicant_home_country_state =
									// xmlParserData_HomeAdd.getValueOf("applicant_residential_address_state");
									// applicant_home_country =
									// xmlParserData_HomeAdd.getValueOf("applicant_residential_address_country");
									applicant_home_POST_BOX = xmlParserData_HomeAdd
											.getValueOf("applicant_residential_address_POST_BOX");
									applicant_home_country_number = xmlParserData_HomeAdd.getValueOf("Country_No");
								}

								applicant_resident_id = xmlParserData_Ext.getValueOf("applicant_resident_id");
								applicant_customer_id = xmlParserData_Ext.getValueOf("applicant_customer_id");
								applicant_name = xmlParserData_Ext.getValueOf("applicant_name");
								applicant_first_name = xmlParserData_Ext.getValueOf("applicant_first_name");
								applicant_middle_name = xmlParserData_Ext.getValueOf("applicant_middle_name");
								applicant_last_name = xmlParserData_Ext.getValueOf("applicant_last_name");
								applicant_dob = xmlParserData_Ext.getValueOf("applicant_dob");
								applicant_mobile_number1 = xmlParserData_Ext.getValueOf("applicant_mobile_number1");
								applicant_nationality = xmlParserData_Ext.getValueOf("applicant_nationality");
								applicant_email = xmlParserData_Ext.getValueOf("applicant_email");
								applicant_occupation = xmlParserData_Ext.getValueOf("applicant_occupation");
								applicant_occupation_type = xmlParserData_Ext.getValueOf("applicant_occupation_type");
								applicant_employer_name = xmlParserData_Ext.getValueOf("applicant_employer_name");
								applicant_designation = xmlParserData_Ext.getValueOf("applicant_designation");
								applicant_passport_id = xmlParserData_Ext.getValueOf("applicant_passport_id");
								applicant_passport_expiry_date = xmlParserData_Ext
										.getValueOf("applicant_passport_expiry_date");
								applicant_salary = xmlParserData_Ext.getValueOf("applicant_total_income");
								applicant_total_income = xmlParserData_Ext.getValueOf("applicant_total_income");
								period_of_service = "";//Digital_PL_CommomMethod.CalculatLOS(xmlParserData_Ext.getValueOf("Date_Of_Joining")).toString();
								product_category = xmlParserData_Ext.getValueOf("product_category");
								product_code = xmlParserData_Ext.getValueOf("product_code");
								limit_for_cards = xmlParserData_Ext.getValueOf("limit_for_cards");
								industry_segment_retail = xmlParserData_Ext.getValueOf("industry_segment_retail");
								industry_sub_segment_retail = xmlParserData_Ext
										.getValueOf("industry_sub_segment_retail");
								application_request_datetime_stamp = xmlParserData_Ext
										.getValueOf("application_request_datetime_stamp");
								application_approval_datetime_stamp = xmlParserData_Ext
										.getValueOf("application_request_datetime_stamp");
								FUTURE_PURPOSE_1 = xmlParserData_Ext.getValueOf("FUTURE_PURPOSE_1");
								FUTURE_PURPOSE_7 = xmlParserData_Ext.getValueOf("FUTURE_PURPOSE_7");
								FUTURE_PURPOSE_10 = xmlParserData_Ext.getValueOf("FUTURE_PURPOSE_10");
								REJECTED_BY_USER_OVRLD_FLG = xmlParserData_Ext.getValueOf("REJECTED_BY_USER_OVRLD_FLG");
								SOURCED_BY_DSA_OVRLD_FLG = xmlParserData_Ext.getValueOf("SOURCED_BY_DSA_OVRLD_FLG");
								AECB_Score = xmlParserData_Ext.getValueOf("applicant_aecb_score");
								Name_On_EID = xmlParserData_Ext.getValueOf("applicant_name_as_per_EID");
								rm_user_id = xmlParserData_Ext.getValueOf("rm_user_id");

								/**
								 * New_credit_limit requested in application DIM
								 * should be tagged to 'Requested_Limit'; If
								 * this is null this should be tagged to
								 * Approved limit
								 **/
								new_credit_limit_requested = Digital_PL_CommomMethod
										.validateValue(xmlParserData_Ext.getValueOf("new_credit_limit_requested"));
								if (new_credit_limit_requested.equals("")) {
									new_credit_limit_requested = Digital_PL_CommomMethod
											.validateValue(xmlParserData_Ext.getValueOf("ApprovedLimit"));
								}

								String employercode = Digital_PL_CommomMethod
										.validateValue(xmlParserData_Ext.getValueOf("FUTURE_PURPOSE_10")); // employercode
								if (!employercode.equals("")) {
									String query = "select top 1 INDUSTRY_SECTOR, INDUSTRY_MACRO, INDUSTRY_MICRO,COMPANY_STATUS_CC,COMPANY_STATUS_PL,"
											+ "EMPLOYER_CATEGORY_PL from ng_rlos_aloc_offline_data with(nolock) where EMPLOYER_CODE = '"
											+ employercode + "'";
									try {
										List<Map<String, String>> OutputXML_ref = getDataFromDBMap(query, cabinetName,
												sessionId, sJtsIp, iJtsPort);
										if (OutputXML_ref.size() > 0) {
											if (!Digital_PL_CommomMethod
													.validateValue(OutputXML_ref.get(0).get("INDUSTRY_MACRO"))
													.equals(""))
												industry_segment_retail = OutputXML_ref.get(0).get("INDUSTRY_MACRO");

											if (!Digital_PL_CommomMethod
													.validateValue(OutputXML_ref.get(0).get("INDUSTRY_MICRO"))
													.equals(""))
												industry_sub_segment_retail = OutputXML_ref.get(0)
														.get("INDUSTRY_MICRO");

											FUTURE_PURPOSE_2 = OutputXML_ref.get(0).get("COMPANY_STATUS_CC");
											FUTURE_PURPOSE_3 = OutputXML_ref.get(0).get("COMPANY_STATUS_PL");
											FUTURE_PURPOSE_4 = OutputXML_ref.get(0).get("EMPLOYER_CATEGORY_PL");

										}
									} catch (Exception e) {
										logger
												.debug(" Exception occurred in ApplicantDetails Query" + query);
										logger.debug(
												" Exception occurred in sInputXmlApplicantDetails()" + e.getMessage());
									}
								}

								String columnNames = "application_number, applicant_resident_id,application_type,application_status,applicant_customer_id,applicant_name,"
										+ "applicant_first_name,applicant_middle_name,applicant_last_name,applicant_dob,applicant_residential_address_line1,"
										+ "applicant_residential_address_line2,applicant_residential_address_line3,applicant_residential_address_city,"
										+ "applicant_residential_address_state,applicant_residential_address_country,applicant_residential_address_POST_BOX,"
										+ "applicant_office_address_line1,applicant_office_address_line2,applicant_office_address_line3,applicant_office_address_city,"
										+ "applicant_office_address_state,applicant_office_address_country,applicant_office_address_POST_BOX,applicant_home_country_address_line1,"
										+ "applicant_home_country_address_line2,applicant_home_country_address_line3,applicant_home_country_city,"
										+ "applicant_home_country_state,applicant_home_country,applicant_home_POST_BOX,applicant_home_country_number,applicant_mobile_number1,preferred_address,"
										+ "applicant_nationality,applicant_email,applicant_occupation,applicant_occupation_type,applicant_employer_name,"
										+ "applicant_designation,applicant_passport_id,applicant_passport_expiry_date,applicant_salary,applicant_total_income,"
										+ "period_of_service,product_category,product_code,limit_for_cards,new_credit_limit_requested,industry_segment_retail,"
										+ "industry_sub_segment_retail,application_request_datetime_stamp,application_approval_datetime_stamp,APP_CHANNEL_TYPE,"
										+ "FUTURE_PURPOSE_1,FUTURE_PURPOSE_2,FUTURE_PURPOSE_3,FUTURE_PURPOSE_4,FUTURE_PURPOSE_6,FUTURE_PURPOSE_7,FUTURE_PURPOSE_8,"
										+ "FUTURE_PURPOSE_9,FUTURE_PURPOSE_10,REJECTED_BY_USER_OVRLD_FLG,SOURCED_BY_DSA_OVRLD_FLG,AECB_Score,Name_On_EID,rm_user_id,entry_by_user_id,applicant_customer_type";

								String columnValues = "'" + application_number + "','" + applicant_resident_id + "','"
										+ application_type + "','" + application_status + "','" + applicant_customer_id
										+ "','" + applicant_name + "','" + applicant_first_name + "','"
										+ applicant_middle_name + "','" + applicant_last_name + "','" + applicant_dob
										+ "','" + applicant_residential_address_line1 + "','"
										+ applicant_residential_address_line2 + "','"
										+ applicant_residential_address_line3 + "','"
										+ applicant_residential_address_city + "','"
										+ applicant_residential_address_state + "','"
										+ applicant_residential_address_country + "','"
										+ applicant_residential_address_POST_BOX + "','"
										+ applicant_office_address_line1 + "','" + applicant_office_address_line2
										+ "','" + applicant_office_address_line3 + "','" + applicant_office_address_city
										+ "','" + applicant_office_address_state + "','"
										+ applicant_office_address_country + "','" + applicant_office_address_POST_BOX
										+ "','" + applicant_home_country_address_line1 + "','"
										+ applicant_home_country_address_line2 + "','"
										+ applicant_home_country_address_line3 + "','" + applicant_home_country_city
										+ "','" + applicant_home_country_state + "','" + applicant_home_country + "','"
										+ applicant_home_POST_BOX + "','" + applicant_home_country_number + "','"
										+ applicant_mobile_number1 + "','" + preferred_address + "','"
										+ applicant_nationality + "','" + applicant_email + "','" + applicant_occupation
										+ "','" + applicant_occupation_type + "','" + applicant_employer_name + "','"
										+ applicant_designation + "','" + applicant_passport_id + "','"
										+ applicant_passport_expiry_date + "','" + applicant_salary + "','"
										+ applicant_total_income + "','" + period_of_service + "','" + product_category
										+ "','" + product_code + "','" + limit_for_cards + "','"
										+ new_credit_limit_requested + "','"
										+ industry_segment_retail.replace("'", "''") + "','"
										+ industry_sub_segment_retail.replace("'", "''") + "','"
										+ application_request_datetime_stamp + "','"
										+ application_approval_datetime_stamp + "','" + APP_CHANNEL_TYPE + "','"
										+ FUTURE_PURPOSE_1 + "','" + FUTURE_PURPOSE_2 + "','" + FUTURE_PURPOSE_3 + "','"
										+ FUTURE_PURPOSE_4 + "','" + FUTURE_PURPOSE_6 + "','" + FUTURE_PURPOSE_7 + "','"
										+ FUTURE_PURPOSE_8 + "','" + FUTURE_PURPOSE_9 + "','" + FUTURE_PURPOSE_10
										+ "','" + REJECTED_BY_USER_OVRLD_FLG + "','" + SOURCED_BY_DSA_OVRLD_FLG + "','"
										+ AECB_Score + "','" + Name_On_EID + "', '" + rm_user_id + "', '" + rm_user_id
										+ "', '" + applicant_customer_type + "'";

								String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionId, columnNames,
										columnValues, "NG_DPL_EFMS_APPLICATION_DIM");
								logger.debug("APInsertInputXML: " + apInsertInputXML);

								String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, sJtsIp, iJtsPort,
										1);
								logger.debug("APInsertOutputXML: " + apInsertOutputXML);

								XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
								String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
								logger.debug("Status of apInsertMaincode  " + apInsertMaincode);

								if ("0".equalsIgnoreCase(apInsertMaincode)) {
									SimpleDateFormat inputDateTimeformat = new SimpleDateFormat(
											"yyyy-MM-dd HH:mm:ss.S");
									SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd");

									Date actionDateTime = new Date();
									String formattedActionDateTime = inputDateTimeformat.format(actionDateTime);
									logger
											.debug("FormattedActionDateTime: " + formattedActionDateTime);

									String formattedDateformat = inputDateformat.format(actionDateTime);
									logger.debug("formattedDateformat: " + formattedDateformat);

									logger.debug("ApInsert successful: " + apInsertMaincode);
									// NG_DCC_EFMS_CONTROLTABLE(WI_NAME,STATUS,Initiation_Type)
									// values(@ApplicationNumber,'Ready',@InitiationType);

									// STATUS -- Done Ready
									columnNames = "WI_NAME,STATUS,Initiation_Type";
									columnValues = "'" + application_number + "','Ready','Initiation'";

									apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionId, columnNames,
											columnValues, "NG_DPL_EFMS_CONTROLTABLE");
									logger.debug("APInsertInputXML: " + apInsertInputXML);

									apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, sJtsIp, iJtsPort,
											1);
									logger.debug("APInsertOutputXML: " + apInsertOutputXML);

									XMLParser xmlParserAPInsert_CT = new XMLParser(apInsertOutputXML);
									String apInsertMaincode_CT = xmlParserAPInsert_CT.getValueOf("MainCode");
									logger
											.debug("Status of apInsertMaincode  " + apInsertMaincode_CT);

									if (apInsertMaincode_CT.equalsIgnoreCase("0")) {
										logger
												.debug("ApInsert successful: " + apInsertMaincode_CT);
									} else {
										processingFlag = "FAIL";
										errorType = "ServerSideError";
										logger.debug("ApInsert failed: " + apInsertMaincode_CT);
									}
								} else {
									logger
											.debug("ApInsert failed with maincode: " + apInsertMaincode);
									processingFlag = "FAIL";
									errorType = "ServerSideError";
								}

							} else {
								logger.debug(
										"Apselect failed with main code: " + xmlParserData_Ext.getValueOf("MainCode"));
								processingFlag = "FAIL";
								errorType = "ServerSideError";
							}
						} else if (iTotalrec > 0) {
							String xmlDataExtTab = xmlParserData.getNextValueOf("Record");
							xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
							if (xmlParserData.getValueOf("STATUS").equals("Done")) {

								// DBQuery = "select top 1
								// isnull(Case_status,'') as
								// Case_status,isnull(APPLICATION_STATUS,'') as
								// APPLICATION_STATUS from "+ tableUpdate+" with
								// (nolock) where Application_Number
								// ='"+application_number+"' and isValid = 'Y'
								// order by SNO desc";
								DBQuery = "select top 1 isnull(APPLICATION_STATUS,'') as APPLICATION_STATUS from "
										+ tableUpdate + " with(nolock) where Application_Number ='" + application_number
										+ "' and isValid = 'Y' order by SNO desc";
								extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,
										CommonConnection.getCabinetName(),
										CommonConnection.getSessionID(logger, false));
								logger.debug(
										"Input xml for selecting statusfrom efms response table : " + extTabDataIPXML);
								extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,
										CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
								logger.debug(
										"output xml for selecting statusfrom efms response table : " + extTabDataOPXML);

								xmlParserData = new XMLParser(extTabDataOPXML);
								iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
								if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0")) {
									String APPLICATION_STATUS = "";
									// String Case_status = "";
									if (!"".equalsIgnoreCase(xmlParserData.getValueOf("APPLICATION_STATUS").trim())) {
										APPLICATION_STATUS = xmlParserData.getValueOf("APPLICATION_STATUS").trim();
									}
									/*
									 * if(!"".equalsIgnoreCase(xmlParserData.
									 * getValueOf("Case_status").trim())) {
									 * Case_status =
									 * (xmlParserData.getValueOf("Case_status").
									 * trim()).toLowerCase(); }
									 */
									if ((APPLICATION_STATUS != null && !"".equalsIgnoreCase(APPLICATION_STATUS)
											&& !"Alerted".equalsIgnoreCase(APPLICATION_STATUS))) {
										doneWI(cabinetName, sJtsIp, iJtsPort, sessionId, processInstanceID, WorkItemID,
												entryDateTime, ActivityID, ActivityType, ProcessDefId,
												APPLICATION_STATUS);
									}
								} else {
									logger.debug(
											"Apselect failed with main code: " + xmlParserData.getValueOf("MainCode"));
									processingFlag = "FAIL";
									errorType = "ServerSideError";
								}

							}

						}
					} else {
						logger.debug("Apselect failed with main code: " + mainCode);
						processingFlag = "FAIL";
						errorType = "ServerSideError";
					}
					if ("FAIL".equalsIgnoreCase(processingFlag)) {
						logger.debug("Apselect failed with main code: " + mainCode);
						// sendMail( cabinetName,sessionId, errorType
						// ,processInstanceID, sJtsIp, iJtsPort);
						routeToErrorHandling(cabinetName, sJtsIp, iJtsPort, sessionId, processInstanceID, WorkItemID,
								entryDateTime, ActivityID, ActivityType, ProcessDefId, "Error in getting EFMS Status");
					}
				}
			}
			// Deepak change done to fetch more than 100 cases
			if (fetchWorkitemListCount > 99) {
				logger
						.debug("Inside if condition to fech next set of cases post 100 processInstanceId: "
								+ processInstanceId + " lastWorkItemId: " + lastWorkItemId);
				insertDataInEFMSTable(cabinetName, sJtsIp, iJtsPort, sessionId, queueID, processInstanceId,
						lastWorkItemId, entryDateTime);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void updateExttable(String cabinetName, String sJtsIp, String iJtsPort, String sessionId,
			String processInstanceID) {
		try {
			String StrExttable_query = "SELECT FirstName, MiddleName, LastName FROM NG_DPL_EXTTABLE with(nolock) WHERE WINAME='"
					+ processInstanceID + "'";
			String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(StrExttable_query,
					CommonConnection.getCabinetName(), sessionId);

			logger.debug("StrExttable_query: " + StrExttable_query);
			String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(),
					CommonConnection.getJTSPort(), 1);

			XMLParser xmlParserData = new XMLParser(extTabDataOPXML);

			String FirstName = "";
			String MiddleName = "";
			String LastName = "";
			String CustomerName = "";

			String mainCode = xmlParserData.getValueOf("MainCode");

			if (mainCode != null && !"".equalsIgnoreCase(mainCode) && mainCode.equalsIgnoreCase("0")) {
				FirstName = xmlParserData.getValueOf("FirstName");
				MiddleName = xmlParserData.getValueOf("MiddleName");
				LastName = xmlParserData.getValueOf("LastName");
				if (!"".equalsIgnoreCase(MiddleName)) {
					CustomerName = FirstName + " " + MiddleName + " " + LastName;
				} else {
					CustomerName = FirstName + " " + LastName;
				}

				String columnNames = "CUSTOMERNAME";
				String columnValues = "'" + CustomerName + "'";
				String sWhereClause = "WiName = '" + processInstanceID + "'";

				String extTableIPUpdateXml1 = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), sessionId,
						"NG_DPL_EXTTABLE", columnNames, columnValues, sWhereClause);
				logger
						.debug("Input XML for apUpdateInput for  NG_DPL_EXTTABLE  Table : " + extTableIPUpdateXml1);

				String extTableOPUpdateXml1 = CommonMethods.WFNGExecute(extTableIPUpdateXml1,
						CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
				logger
						.debug("Output XML for apUpdateInput for NG_DPL_EXTTABLE Table : " + extTableOPUpdateXml1);
			}

		} catch (Exception e) {
			logger.debug("Exception occured in updateExttable : " + e);
		}
	}

	private static List<Map<String, String>> getDataFromDBMap(String query, String cabinetName, String sessionID,
			String jtsIP, String jtsPort) {
		try {
			logger.debug("Inside function getDataFromDB");
			logger.debug("getDataFromDB query is: " + query);
			String InputXML = CommonMethods.apSelectWithColumnNames(query, cabinetName, sessionID);
			List<Map<String, String>> temp = new ArrayList<Map<String, String>>();
			String OutXml = CommonMethods.WFNGExecute(InputXML, jtsIP, jtsPort, 1);
			OutXml = OutXml.replaceAll("&", "#andsymb#");
			Document recordDoc1 = MapXML.getDocument(OutXml);
			NodeList records1 = recordDoc1.getElementsByTagName("Record");
			if (records1.getLength() > 0) {
				for (int i = 0; i < records1.getLength(); i++) {
					Node n = records1.item(i);
					Map<String, String> t = new HashMap<String, String>();
					if (n.hasChildNodes()) {
						NodeList child = n.getChildNodes();
						for (int j = 0; j < child.getLength(); j++) {
							Node n1 = child.item(j);
							String column = n1.getNodeName();

							String value = n1.getTextContent().replaceAll("#andsymb#", "&");
							if (null != value && !"null".equalsIgnoreCase(value) && !"".equals(value)) {
								logger
										.debug("getDataFromDBMap Setting value of " + column + " as " + value);
								t.put(column, value);
							} else {
								logger
										.debug("getDataFromDBMap Setting value of " + column + " as blank");
								t.put(column, "");
							}
						}
					}
					temp.add(t);
				}
			}
			return temp;
		} catch (Exception ex) {
			logger.debug("Exception in getDataFromDBMap method + " + ex.getMessage());
			return null;
		}

	}

	private void doneWI(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String processInstanceID,
			String WorkItemID, String entryDateTime, String ActivityID, String ActivityType, String ProcessDefId,
			String AlertStatusFlag) throws IOException, Exception, ParseException {

		String status = "";

		String columnNames1 = "EFMS_Status";
		String columnValues1 = "'" + AlertStatusFlag + "'";
		String sWhereClause2 = "WiName = '" + processInstanceID + "'";

		String extTableIPUpdateXml1 = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(),
				CommonConnection.getSessionID(logger, false), "NG_DPL_EXTTABLE", columnNames1,
				columnValues1, sWhereClause2);
		logger
				.debug("Input XML apUpdateInput for  NG_DPL_EXTTABLE  Table : " + extTableIPUpdateXml1);

		String extTableOPUpdateXml1 = CommonMethods.WFNGExecute(extTableIPUpdateXml1, CommonConnection.getJTSIP(),
				CommonConnection.getJTSPort(), 1);
		logger
				.debug("Output XML for apUpdateInput for NG_DPL_EXTTABLE Table : " + extTableOPUpdateXml1);

		String decisionValue = "";
		String attributesTag = "";
		String ErrDesc = "";
		if (AlertStatusFlag.equalsIgnoreCase("Non-Alerted") || AlertStatusFlag.equalsIgnoreCase("Closed")) {
			decisionValue = "Approve";
			attributesTag = "<Decision>" + decisionValue + "</Decision>";
			if (AlertStatusFlag.equalsIgnoreCase("Non-Alerted"))
				ErrDesc = "EFMS status is Non-Alerted";
			else
				ErrDesc = "EFMS closed as False Positive";
		} else {
			decisionValue = "Reject";
			attributesTag = "<Decision>" + decisionValue + "</Decision>";
			ErrDesc = "EFMS status is Confirm Fraud";
		}

		XMLParser sXMLParserChild1 = new XMLParser(extTableOPUpdateXml1);
		String StrMainCode = sXMLParserChild1.getValueOf("MainCode");

		if (StrMainCode.equals("0")) {

			logger.debug("WorkItem moved to next Workstep.");

			SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
			SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

			Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
			String formattedEntryDatetime = outputDateFormat.format(entryDatetimeFormat);
			logger.debug("FormattedEntryDatetime: " + formattedEntryDatetime);

			Date actionDateTime = new Date();
			String formattedActionDateTime = outputDateFormat.format(actionDateTime);
			logger.debug("FormattedActionDateTime: " + formattedActionDateTime);

			// Insert in WIHistory Table. wi_name, workstep, Decision,
			// decision_date_time, Remarks, user_name, dec_date
			String columnNames = "WI_NAME,Decision_Date_Time,WORKSTEP,USERNAME,DECISION,ENTRY_DATE_TIME,REMARKS";
			String columnValues = "'" + processInstanceID + "','" + formattedActionDateTime + "','" + ActivityName
					+ "'," + "'System','" + decisionValue + "','" + formattedEntryDatetime + "','" + ErrDesc + "'";

			String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionId, columnNames, columnValues,
					"NG_DPL_GR_DECISION_HISTORY");
			logger.debug("APInsertInputXML: " + apInsertInputXML);

			String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, sJtsIp, iJtsPort, 1);
			logger.debug("APInsertOutputXML: " + apInsertOutputXML);

			XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
			String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
			logger.debug("Status of apInsertMaincode  " + apInsertMaincode);

			if (apInsertMaincode.equalsIgnoreCase("0")) {
				logger.debug("ApInsert into History successful: " + apInsertMaincode);

				// Lock Workitem.
				String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID,
						WorkItemID);
				String getWorkItemOutputXml = CommonMethods.WFNGExecute(getWorkItemInputXML, sJtsIp, iJtsPort, 1);
				logger.debug("Output XML For WmgetWorkItemCall: " + getWorkItemOutputXml);

				XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
				String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
				logger.debug("WmgetWorkItemCall Maincode:  " + getWorkItemMainCode);

				if (getWorkItemMainCode.trim().equals("0")) {

					// logger.debug("Successful in
					// apUpdateInput the record in : " + "NG_DCC_EXTTABLE");

					String assignWorkitemAttributeInputXML = "<?xml version=\"1.0\"?><WMAssignWorkItemAttributes_Input>"
							+ "<Option>WMAssignWorkItemAttributes</Option>" + "<EngineName>" + cabinetName
							+ "</EngineName>" + "<SessionId>" + sessionId + "</SessionId>" + "<ProcessInstanceId>"
							+ processInstanceID + "</ProcessInstanceId>" + "<WorkItemId>" + WorkItemID + "</WorkItemId>"
							+ "<ActivityId>" + ActivityID + "</ActivityId>" + "<ProcessDefId>" + ProcessDefId
							+ "</ProcessDefId>" + "<LastModifiedTime></LastModifiedTime>" + "<ActivityType>"
							+ ActivityType + "</ActivityType>" + "<complete>D</complete>"
							+ "<AuditStatus></AuditStatus>" + "<Comments></Comments>"
							+ "<UserDefVarFlag>Y</UserDefVarFlag>" + "<Attributes>" + attributesTag + "</Attributes>"
							+ "</WMAssignWorkItemAttributes_Input>";

					logger
							.debug("InputXML for assignWorkitemAttribute Call: " + assignWorkitemAttributeInputXML);

					String assignWorkitemAttributeOutputXML = CommonMethods.WFNGExecute(assignWorkitemAttributeInputXML,
							sJtsIp, iJtsPort, 1);

					logger
							.debug("OutputXML for assignWorkitemAttribute Call: " + assignWorkitemAttributeOutputXML);

					XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
					String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
					logger
							.debug("AssignWorkitemAttribute MainCode: " + assignWorkitemAttributeMainCode);

					if (assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0")) {
						logger
								.debug("AssignWorkitemAttribute Successful: " + assignWorkitemAttributeMainCode);

					} else {
						status = "NotDone";
						logger
								.debug("AssignWorkitemAttribute failed: " + assignWorkitemAttributeMainCode);
					}
				}

			} else {
				status = "NotDone";
				logger.debug("ApInsert  into History failed: " + apInsertMaincode);
			}
		} else {
			logger
					.debug("Error in Executing apUpdateInput sOutputXML : " + extTableOPUpdateXml1);
			System.out.println("WMgetWorkItemCall failed: " + processInstanceID);
			logger.debug("WMgetWorkItemCall failed: " + processInstanceID);
			status = "NotDone";
		}

		if ("NotDone".equalsIgnoreCase(status)) {
			// sendMail( cabinetName,sessionId, "DoneWI" ,processInstanceID,
			// sJtsIp, iJtsPort);
		}
	}

	private void routeToErrorHandling(String cabinetName, String sJtsIp, String iJtsPort, String sessionId,
			String processInstanceID, String WorkItemID, String entryDateTime, String ActivityID, String ActivityType,
			String ProcessDefId, String ErrDesc) throws IOException, Exception, ParseException {

		String decisionValue = "Failed";
		String attributesTag = "";

		attributesTag = "<Decision>" + decisionValue + "</Decision>";

		// Lock Workitem.
		String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID,
				WorkItemID);
		String getWorkItemOutputXml = CommonMethods.WFNGExecute(getWorkItemInputXML, sJtsIp, iJtsPort, 1);
		logger.debug("Output XML For WmgetWorkItemCall: " + getWorkItemOutputXml);

		XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
		String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
		logger.debug("WmgetWorkItemCall Maincode:  " + getWorkItemMainCode);

		if (getWorkItemMainCode.trim().equals("0")) {

			// logger.debug("Successful in apUpdateInput
			// the record in : " + "NG_DCC_EXTTABLE");

			String assignWorkitemAttributeInputXML = "<?xml version=\"1.0\"?><WMAssignWorkItemAttributes_Input>"
					+ "<Option>WMAssignWorkItemAttributes</Option>" + "<EngineName>" + cabinetName + "</EngineName>"
					+ "<SessionId>" + sessionId + "</SessionId>" + "<ProcessInstanceId>" + processInstanceID
					+ "</ProcessInstanceId>" + "<WorkItemId>" + WorkItemID + "</WorkItemId>" + "<ActivityId>"
					+ ActivityID + "</ActivityId>" + "<ProcessDefId>" + ProcessDefId + "</ProcessDefId>"
					+ "<LastModifiedTime></LastModifiedTime>" + "<ActivityType>" + ActivityType + "</ActivityType>"
					+ "<complete>D</complete>" + "<AuditStatus></AuditStatus>" + "<Comments></Comments>"
					+ "<UserDefVarFlag>Y</UserDefVarFlag>" + "<Attributes>" + attributesTag + "</Attributes>"
					+ "</WMAssignWorkItemAttributes_Input>";

			logger
					.debug("InputXML for assignWorkitemAttribute Call: " + assignWorkitemAttributeInputXML);

			String assignWorkitemAttributeOutputXML = CommonMethods.WFNGExecute(assignWorkitemAttributeInputXML, sJtsIp,
					iJtsPort, 1);

			logger
					.debug("OutputXML for assignWorkitemAttribute Call: " + assignWorkitemAttributeOutputXML);

			XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
			String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
			logger.debug("AssignWorkitemAttribute MainCode: " + assignWorkitemAttributeMainCode);

			if (assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0")) {
				logger
						.debug("AssignWorkitemAttribute Successful: " + assignWorkitemAttributeMainCode);

				// Move Workitem to next Workstep

				logger.debug("WorkItem moved to next Workstep.");

				SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
				SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

				Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
				String formattedEntryDatetime = outputDateFormat.format(entryDatetimeFormat);
				logger.debug("FormattedEntryDatetime: " + formattedEntryDatetime);

				Date actionDateTime = new Date();
				String formattedActionDateTime = outputDateFormat.format(actionDateTime);
				logger.debug("FormattedActionDateTime: " + formattedActionDateTime);

				// Insert in WIHistory Table. wi_name, workstep, Decision,
				// decision_date_time, Remarks, user_name, dec_date
				String columnNames = "WI_NAME,WORKSTEP,USERNAME,DECISION,decision_date_time,REMARKS";
				String columnValues = "'" + processInstanceID  + "','" + ActivityName
						+ "'," + "'System','" + decisionValue + "','" + formattedEntryDatetime + "','" + ErrDesc + "'";

				String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionId, columnNames, columnValues,
						"NG_DPL_GR_DECISION_HISTORY");
				logger.debug("APInsertInputXML: " + apInsertInputXML);

				String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, sJtsIp, iJtsPort, 1);
				logger.debug("APInsertOutputXML: " + apInsertOutputXML);

				XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
				String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
				logger.debug("Status of apInsertMaincode  " + apInsertMaincode);

				if (apInsertMaincode.equalsIgnoreCase("0")) {
					logger.debug("ApInsert successful: " + apInsertMaincode);
				} else {
					logger.debug("ApInsert failed: " + apInsertMaincode);
				}
			} else {
				logger
						.debug("AssignWorkitemAttribute failed: " + assignWorkitemAttributeMainCode);
			}
		}
	}
	private static int readConfig(String fileName) {
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir") + File.separator + "ConfigFiles" + File.separator + fileName)));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				DCCSystemIntegrationMap.put(name, p.getProperty(name));
			}
		} catch (Exception e) {
			return -1;
		}
		return 0;
	}

	public void readEFMSFiles() {
		try {
			
			now = new Date();
			Format formatter = new SimpleDateFormat("dd-MMM-yy");
			sdate = formatter.format(now);
			String sourcePath = txtFilePath + "\\" + inputPath;
			String movedPath = txtFilePath + "\\" + successPath;
			String failPath = txtFilePath + "\\" + errorPath;
			String inProgressPath = txtFilePath + "\\" + sourceDestinaton;
			File folder = new File(sourcePath);

			filePath = sourcePath;

			File files[] = folder.listFiles();
			//logger.info("Number of files is " + files.length);
			if (files.length == 0) {
				Thread.sleep(10000);
			} else {
				logger.info("files is not null");
				logger.info("Total files for processing folder: " + files.length);

				for (int i = files.length - 1; i >= 0; i--) {
					String TempsourcePath = "";
					String TempfailPath = "";
					String TempinProgressPath = "";
					String TempmovedPath = "";
					file = files[i].getName();
					msg = getMsgAsString(file);// returns the file content as a
												// String
					if (msg.length() <= 0 || msg.equalsIgnoreCase("blank")) {
						logger.info("No Data in File : " + file);
						TempsourcePath = "" + sourcePath + "\\" + file + "";
						TempfailPath = failPath + "\\" + sdate;
						TimeStamp = get_timestamp();
						newFilename = Move(TempfailPath, TempsourcePath, TimeStamp, true);// file
																							// is
																							// moved
																							// to
																							// NoDataFile
																							// flder
						continue;
					} else if ((msg.equalsIgnoreCase("error"))) {
						logger.info("Error in source file : " + file);
						TempsourcePath = "" + sourcePath + "\\" + file + "";
						TempfailPath = failPath + "\\" + sdate;
						TimeStamp = get_timestamp();
						newFilename = Move(TempfailPath, TempsourcePath, TimeStamp, true);// file
																							// is
																							// moved
																							// to
																							// NoDataFile
																							// flder
						continue;
					} else {
						logger.info("Move file to In progress folder: " + file);
						TempinProgressPath = inProgressPath;
						TempsourcePath = "" + sourcePath + "\\" + file + "";
						TimeStamp = get_timestamp();
						newFilename = Move(TempinProgressPath, TempsourcePath, TimeStamp, false);
						logger.info(" file moved to In progress folder: " + file);

						String finalSourcePath = TempinProgressPath + "\\" + newFilename;
						logger.info(" finalSourcePath: " + finalSourcePath);
						updateStatus = readTextFile(finalSourcePath, newFilename, cabinetName, sessionID, jtsIP,
								Integer.parseInt(jtsPort), tableUpdate);
						if (updateStatus.equalsIgnoreCase("Success")) {
							// System.out.println("ng_RLOS_SR_IPP_OFFLINE table
							// successfully updated");
							logger.info("ng_efms_response table successfully updated");

							// TempsourcePath = ""+sourcePath+"\\"+file+"";
							TempmovedPath = movedPath + "\\" + sdate;
							TimeStamp = get_timestamp();
							newFilename = Move(TempmovedPath, finalSourcePath, TimeStamp, true);// file
																								// is
																								// moved
																								// to
																								// NoDataFile
																								// flder
							continue;

						} else {
							// System.out.println("ng_RLOS_SR_IPP_OFFLINE table
							// updation failed");
							logger.info("ng_efms_response table updation failed");

							TempsourcePath = "" + sourcePath + "\\" + file + "";
							TempfailPath = failPath + "\\" + sdate;
							TimeStamp = get_timestamp();
							newFilename = Move(TempfailPath, finalSourcePath, TimeStamp, true);// file
																								// is
																								// moved
																								// to
																								// NoDataFile
																								// flder
							continue;
						}
					}
				}
			}
		}

		catch (Exception e) {
			// e.printStackTrace();
			logger.error("Exception: " + e.toString());
		} finally {
			System.gc();
		}
	}

	public String getMsgAsString(String file) throws IOException {
		FileReader fr = null;
		BufferedReader br = null;
		String msg = "";
		try {
			if (filePath != null && file != null) {
				// ProcessData.addToTextArea("inside get msg as string method");
				logger.info("inside get msg as string method");
				char[] c = { 0x0D, 0x0A };
				String crlf = new String(c);
				fr = new FileReader(filePath + "\\" + file);
				br = new BufferedReader(fr);
				String line = br.readLine();
				while (line != null) {
					msg += line.trim() + crlf;
					line = br.readLine();
				}
				int msgLength = msg.length();
				if (msgLength == 0) {
					msg = "blank";
				} else {
					msg = msg.substring(0, msgLength - crlf.length());
				}
				logger.info("completion : get msg as string method");
			}
		} catch (Exception e) {
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			// e.printStackTrace(printWriter);
			logger
					.info("Exception while converting the file into String : " + result.toString());
			msg = "error";
		} finally {
			if (null != fr) {
				fr.close();
			}
			if (null != br) {
				br.close();
			}
		}
		return msg;
	}

	public String get_timestamp() {
		Date present = new Date();
		Format pformatter = new SimpleDateFormat("dd-MM-yyyy-hhmmss");
		TimeStamp = pformatter.format(present);
		return TimeStamp;
	}

	public String Move(String pstrDestFolderPath, String pstrFilePathToMove, String append, boolean flag) {
		logger.info("Inside Move  : " + pstrDestFolderPath + " : " + pstrFilePathToMove);
		String lstrExceptionId = "Text_Read.Move";
		try {

			// Destination directory
			File lobjDestFolder = new File(pstrDestFolderPath);

			if (!lobjDestFolder.exists()) {

				lobjDestFolder.mkdirs();

				// delete destination file if it already exists
				//////////////
			}
			File lobjFileTemp;
			File lobjFileToMove = new File(pstrFilePathToMove);
			String orgFileName = lobjFileToMove.getName();

			if (flag) {
				newFilename = orgFileName.substring(0, orgFileName.indexOf(".")) + "_" + append
						+ orgFileName.substring(orgFileName.indexOf("."));
				lobjFileTemp = new File(pstrDestFolderPath + File.separator + newFilename);
			} else {
				logger.info("orgFileName::" + orgFileName);
				newFilename = orgFileName;
				lobjFileTemp = new File(pstrDestFolderPath + File.separator + newFilename);
				logger.info("lobjFileTemp::" + lobjFileTemp);
			}
			if (lobjFileTemp.exists()) {
				logger.info("lobjFileTemp exists");
				if (!lobjFileTemp.isDirectory()) {
					lobjFileTemp.delete();
				} else {
					deleteDir(lobjFileTemp);
				}
			} else {
				logger.info("lobjFileTemp dont exists");
				lobjFileTemp = null;
			}
			File lobjNewFolder;
			// if(flag){
			lobjNewFolder = new File(lobjDestFolder, newFilename);
			/*
			 * }else{ lobjNewFolder = lobjDestFolder; }
			 */

			boolean lbSTPuccess = false;
			try {
				logger.info("lobjFileToMove::" + lobjFileToMove);
				logger.info("lobjNewFolder::" + lobjNewFolder);
				lbSTPuccess = lobjFileToMove.renameTo(lobjNewFolder);
				logger.info("lbSTPuccess::" + lbSTPuccess);
			} catch (SecurityException lobjExp) {

				logger.info("SecurityException " + lobjExp.toString());
			} catch (NullPointerException lobjNPExp) {

				logger.info("NullPointerException " + lobjNPExp.toString());
			} catch (Exception lobjExp) {

				logger.info("Exception " + lobjExp.toString());
			}
			if (!lbSTPuccess) {
				logger.info("Failure while moving " + lobjFileToMove.getAbsolutePath());
			} else {
				logger
						.info("Success while moving " + lobjFileToMove.getName() + "to" + pstrDestFolderPath);
				logger
						.info("Success while moving " + lobjFileToMove.getName() + "to" + lobjNewFolder);
			}
			lobjDestFolder = null;
			lobjFileToMove = null;
			lobjNewFolder = null;
		} catch (Exception lobjExp) {
			logger.info(lstrExceptionId + " : " + "Exception occurred while moving "
					+ pstrFilePathToMove + " to " + ":" + lobjExp.toString());

		}

		return newFilename;
	}

	public static boolean deleteDir(File dir) throws Exception {
		if (dir.isDirectory()) {
			String[] lstrChildren = dir.list();
			for (int i = 0; i < lstrChildren.length; i++) {
				boolean success = deleteDir(new File(dir, lstrChildren[i]));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}

	private Map<String, ArrayList<ArrayList<String>>> readTextFile(String file) {
		FileInputStream fileInputStream = null;
		DataInputStream DataInputStream = null;
		BufferedReader InputStreamReader = null;

		Map<String, ArrayList<ArrayList<String>>> mCompletevalue = new LinkedHashMap<String, ArrayList<ArrayList<String>>>();

		// mLogger.info("file readTextFile "+ file);
		try

		{
			fileInputStream = new FileInputStream(file);
			DataInputStream = new DataInputStream(fileInputStream);
			InputStreamReader = new BufferedReader(new InputStreamReader(DataInputStream));
			String strLine;

			// String last, line;
			String[] fields;
			String[] columnName;
			String[] columnNameFirst = null;
			int count = 0;
			// String finalColumns="";

			String finalValues = "";
			String firstValues = "";
			ArrayList<ArrayList<String>> ArrLstFileHeader = new ArrayList<ArrayList<String>>();
			ArrayList<ArrayList<String>> ArrLstFileRecords = new ArrayList<ArrayList<String>>();
			while ((strLine = InputStreamReader.readLine()) != null) {
				count = ++count;
				ArrayList<String> ArrLstFileValues = new ArrayList<String>();
				ArrayList<String> ArrLstFileFirst = new ArrayList<String>();

				if (count == 1) {
					fields = strLine.split("H\\|", +1);
					for (int i = 0; i < fields.length; i++) {
						columnNameFirst = fields[i].split("\\|", -1);
						/*
						 * datecreation = columnNameFirst[1]; bussinessName =
						 * columnNameFirst[2]; fileName = columnNameFirst[3];
						 */
					}

					firstValues = columnNameFirst[0] + "," + columnNameFirst[1] + "," + columnNameFirst[2] + ","
							+ columnNameFirst[3] + "," + columnNameFirst[4];
					ArrLstFileFirst.add(firstValues);
					ArrLstFileHeader.add(ArrLstFileFirst);
					mCompletevalue.put("1", ArrLstFileHeader);
				} else {
					if ("T".equalsIgnoreCase(strLine.substring(0, 1))) {
					} else {
						fields = strLine.split("H\\|", +1);
						for (int i = 0; i < fields.length; i++) {
							columnName = fields[i].split("\\|", -1);
							for (int j = 0; j < columnName.length; j++) {
								finalValues = finalValues + "," + columnName[j];
							}
							finalValues = finalValues.substring(1, finalValues.length());
							ArrLstFileValues.add(finalValues);
							ArrLstFileRecords.add(ArrLstFileValues);
							mCompletevalue.put("2", ArrLstFileRecords);
							finalValues = "";
						}
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Error is reading configuration file = " + e);
			try {
				if (DataInputStream != null) {
					DataInputStream.close();
				}
				if (InputStreamReader != null) {
					InputStreamReader.close();
				}
				if (fileInputStream != null) {
					fileInputStream.close();
				}
			} catch (Exception x) {

			}

		} finally {
			try {
				if (DataInputStream != null) {
					DataInputStream.close();
				}
				if (InputStreamReader != null) {
					InputStreamReader.close();
				}
				if (fileInputStream != null) {
					fileInputStream.close();
				}
			} catch (Exception e) {
			}
		}
		return mCompletevalue;
	}

	public String readTextFile(String txtFilePath, String fileName, String cabinetName, String sessionId, String jtsIP,
			int jtsPort, String tableUpdate) {
		String updateFlag = "";
		try {
			Map<String, ArrayList<ArrayList<String>>> mCompletevalue = new LinkedHashMap<String, ArrayList<ArrayList<String>>>();

			// System.out.println("txtFilePath "+ txtFilePath);
			logger.info("txtFilePath " + txtFilePath);
			mCompletevalue = readTextFile(txtFilePath);

			// ArrayList<ArrayList<String>> ArrLstFileHeader= new
			// ArrayList<ArrayList<String>>();
			// ArrayList<String> ArrLstFileColumn= new ArrayList<String>();
			ArrayList<ArrayList<String>> ArrLstFileRecords = new ArrayList<ArrayList<String>>();

			String sSplit = "";
			String sSplitColumn = "";
			// String finalColumns="";
			String firstValues = "";
			// String headerArray[];
			for (Entry<String, ArrayList<ArrayList<String>>> entry : mCompletevalue.entrySet()) {
				if (entry.getKey().equalsIgnoreCase("2")) {

					ArrLstFileRecords = entry.getValue();
					for (int iIncCnt = 0; iIncCnt < ArrLstFileRecords.size(); iIncCnt++) {
						sSplit = ArrLstFileRecords.get(iIncCnt).toString().trim();
						updateFlag = updateDBTable(sSplit, fileName, tableUpdate, cabinetName);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return updateFlag;

	}

	public String updateDBTable(String finalValues, String fileName, String tableUpdate, String cabinetName) {

		String columnName = "";
		String[] columnValues;
		String sWhere = "";
		String tableStatus = "success";
		String outputXML = null;
		String inputXml2 = "";
		String UpdateValues = "";
		String inputXML = "";
		String InsertValues = "";
		String ApplicationNumber = "";
		String CaseOwner = "";
		String CaseStatus = "";
		String CloseDateTime = "";
		String mainCodeforAPInsert = "";
		XMLParser objXMLParser = new XMLParser();

		try {
			if (finalValues.contains("[") || finalValues.contains("]"))
				finalValues = finalValues.replace("[", "").replace("]", "");
			columnValues = finalValues.split(",", -1);
			if (fileName.contains(ApplicationFileName)) {
				columnName = ApplicationInsertColumn;
				SimpleDateFormat formatter = new SimpleDateFormat("ddMMMyy:HH:mm:ss");
				String dateInString = columnValues[3];
				Date date = formatter.parse(dateInString);
				SimpleDateFormat formatter1 = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
				String sDate = formatter1.format(date);
				logger.info(" date value" + sDate);

				InsertValues = "'" + columnValues[0] + "','" + columnValues[1] + "','" + columnValues[2] + "',"
						+ "Cast('" + sDate + "' as datetime)" + ",'" + columnValues[4] + "','Y'";
				logger.info(" Insert values" + InsertValues);

				sessionCheckInt = 0;
				while (sessionCheckInt < loopCount) {
					try {

						inputXML = CommonMethods.apInsert(cabinetName, CommonConnection.getSessionID(logger, true), columnName, InsertValues,
								tableUpdate);
						logger.info("Insert No" + inputXML);
						outputXML = CommonMethods.WFNGExecute(inputXML, jtsIP, jtsPort, 1);
						logger.info("Insert No outXml" + outputXML);
						objXMLParser.setInputXML(outputXML);
						mainCodeforAPInsert = objXMLParser.getValueOf("MainCode");
					}

					catch (Exception e) {
						e.printStackTrace();
						tableStatus = "Failure";
						logger.error("Exception in inserting efms file data --", e);
						sessionCheckInt++;
						waiteloopExecute(waitLoop);
						continue;
					}
					if (mainCodeforAPInsert.equalsIgnoreCase("11")) {
						logger.info("Invalid session in inserting efms file data");
						sessionCheckInt++;
						tableStatus = "Failure";
						// ThreadConnect.sessionId =
						// ThreadConnect.getSessionID(cabinetName, jtsIP,
						// jtsPort, userName,password);
						sessionID = CommonConnection.getSessionID(logger, true);
						continue;
					} else {
						sessionCheckInt++;
						break;
					}
				}
				if (mainCodeforAPInsert.equalsIgnoreCase("0")) {
					logger.info("EFMS Data insert successful for file " + fileName);
				} else {
					logger.info("EFMS Data Insert Unsuccessful for file " + fileName);
					tableStatus = "Failure";
				}
			} else if (fileName.contains(AlertFileName)) {
				columnName = AlertedUpdateColumn;
				ApplicationNumber = columnValues[0];
				CaseOwner = columnValues[2];
				CaseStatus = columnValues[3];
				CloseDateTime = columnValues[4];
				SimpleDateFormat formatter = new SimpleDateFormat("ddMMMyy:HH:mm:ss");
				Date date = formatter.parse(CloseDateTime);
				SimpleDateFormat formatter1 = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
				String sDate = formatter1.format(date);
				// logger.info(" date value"+sDate);
				// columnName = columnName+",isValid";
				// columnName = columnName;
				// UpdateValues = "'"+ CaseOwner +"','"+ CaseStatus
				// +"',"+"Cast('"+sDate+"' as datetime),'Y'";
				UpdateValues = "'" + CaseOwner + "','" + CaseStatus + "'," + "Cast('" + sDate + "' as datetime)";
				logger.info("Alerted File update values" + UpdateValues);
				sWhere = "APPLICATION_NUMBER ='" + ApplicationNumber + "'";

				sessionCheckInt = 0;
				while (sessionCheckInt < loopCount) {
					try {

						inputXml2 = CommonMethods.apUpdateInput(cabinetName, CommonConnection.getSessionID(logger, true), tableUpdate, columnName,
								UpdateValues, sWhere);
						logger.info(" update No" + inputXml2);
						
						outputXML = CommonMethods.WFNGExecute(inputXml2,CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
						logger.info("update No outXml" + outputXML);
						objXMLParser.setInputXML(outputXML);
						mainCodeforAPInsert = objXMLParser.getValueOf("MainCode");
						if (mainCodeforAPInsert.equalsIgnoreCase("0")) {
							logger.info("EFMS Data upadte successful for file " + fileName);
							break;
						} else if (mainCodeforAPInsert.equalsIgnoreCase("11")) {
							sessionCheckInt++;
							logger.info("Invalid session in updating efms file data");
							sessionCheckInt++;
							// tableStatus = "Failure";
							// ThreadConnect.sessionId =
							// ThreadConnect.getSessionID(cabinetName, jtsIP,
							// jtsPort, userName,password);
							sessionID = CommonConnection.getSessionID(logger, true);
							continue;
						} else {
							tableStatus = "Failure";
							sessionCheckInt++;
							logger.info("EFMS Data update Unsuccessful for file " + fileName);
							break;
						}
					}

					catch (Exception e) {
						e.printStackTrace();
						tableStatus = "Failure";
						logger.error("Exception in updating efms file data --", e);
						sessionCheckInt++;
						waiteloopExecute(waitLoop);
						continue;
					}

				}

			}
			/*
			 * XMLParser lobjXMLParser = new XMLParser();
			 * lobjXMLParser.setInputXML(outputXML); String outCode =
			 * lobjXMLParser.getValueOf("MainCode");
			 * if(!"0".equalsIgnoreCase(outCode)) { tableStatus = "Failure";
			 * }else if("1".equalsIgnoreCase(outCode)){
			 * //checkAndCompleteWI(wi_name); } return tableStatus;
			 */
		} catch (Exception e) {
			tableStatus = "Failure";
			logger.info("Error in reading file: " + e.getMessage());
		}
		return tableStatus;
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
