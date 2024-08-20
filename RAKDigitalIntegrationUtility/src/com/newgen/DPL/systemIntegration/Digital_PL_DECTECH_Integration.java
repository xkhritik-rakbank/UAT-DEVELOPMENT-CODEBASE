package com.newgen.DPL.systemIntegration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.newgen.DCC.DECTECHIntegration.DECTECHSystemIntegrationLog;
import com.newgen.DCC.DECTECHIntegration.MapXML;
import com.newgen.DCC.EFMS.DCC_EFMS_IntegrationLog;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.DPL.Digital_PL_CommomMethod;
import com.newgen.DPL.Digital_PL_Log;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.omni.wf.util.excp.NGException;

public class Digital_PL_DECTECH_Integration extends Digital_PL_CommomMethod implements java.io.Serializable{
	
	static NGEjbClient ngEjbClient;
	private static org.apache.log4j.Logger logger;
	static Map<String, String> DPLSystemIntegrationMap = new HashMap<String, String>();
	
	static String cabinetName = "";
	static String jtsIP = "";
	static String jtsPort = "";
	static String sessionID = "";
	public static int sessionCheckInt=0;
	public static int loopCount=50;
	public static int waitLoop=50;
	public static String AECB_DDS_Return="";
	public static String Worst_Status="";
	public static String AECB_Hist_Month_Count="";
	
	
	public Digital_PL_DECTECH_Integration() throws NGException{
		Digital_PL_Log.setLogger(getClass().getSimpleName());
		this.ngEjbClient = NGEjbClient.getSharedInstance();
		logger = Digital_PL_Log.getLogger(getClass().getSimpleName());
		
	}
	
	public static String GenerateXML(String wi_name, String activityID, String activityType, String processDefId, 
			String WorkItemID) {
		String decision =  "Failed";
		try {
			String queueID = "";
			int socketConnectionTimeout=0;
			int integrationWaitTime=0;
			int sleepIntervalInMin=0;
	
	
			System.out.println("");
			DPLSystemIntegrationMap = Digital_PL_CommomMethod.readConfig("DCC_System_Integration_Config.properties");
			//logger.debug("configReadStatus ");
			if(DPLSystemIntegrationMap.isEmpty())
			{
				logger.error("Could not Read Config Properties [DPL_DECTECH_System_Integration_Config.properties]");
				return "";
			}

			cabinetName = CommonConnection.getCabinetName();
			//logger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			logger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			logger.debug("JTSPORT: " + jtsPort);

			queueID = DPLSystemIntegrationMap.get("dplQueueID");
			logger.debug("QueueID: " + queueID);

			socketConnectionTimeout=Integer.parseInt(DPLSystemIntegrationMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			logger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(DPLSystemIntegrationMap.get("INTEGRATION_WAIT_TIME"));
			logger.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(DPLSystemIntegrationMap.get("SleepIntervalInMin"));
			logger.debug("SleepIntervalInMin: "+sleepIntervalInMin);

			sessionID = CommonConnection.getSessionID(logger, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				logger.debug("Could Not Connect to Server!");
			}
			else
			{
				logger.debug("Session ID found: " + sessionID);
				HashMap<String, String> socketDetailsMap=Digital_PL_CommomMethod.socketConnectionDetails(cabinetName, jtsIP, jtsPort, sessionID);
				logger.debug("DECTECH CIF Verification...123.");
				return generateRequestXML(cabinetName, jtsIP, jtsPort, sessionID, queueID, socketConnectionTimeout,
							integrationWaitTime, socketDetailsMap, wi_name, WorkItemID, activityID, activityType, processDefId);
					//System.out.println("No More workitems to Process, Sleeping!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decision;
	}
	
	private static String generateRequestXML(String cabinetName, String jtsIP, String jtsPort, String sessionID,
			String queueID, int socketConnectionTimeout2, int integrationWaitTime2,
			HashMap<String, String> socketDetailsMap, String wi_name, String WorkItemID, String activityID, String activityType, String processDefId) {
		
		String ws_name = "System_Integration";
		
		String decisionValue="";
		String returnValue="";
		
		try {
			/** Select data and put in the map from NG__EXTTABLE **/
			String DBQuery = "SELECT a.WIName, a.CIF, a.ProductType, a.CustomerName, a.PassportNumber,"
					+ " a.EmiratesID, a.MobileNo,a.IsSTP, a.EmployerCode,a.EmployerName,a.EmailID,"
					+ "a.FirstName, a.MiddleName, a.LastName, a.DOB, a.age, a.Nationality,a.TotalMonthlyHouseholdIncome,"
					+ "a.ProspectID, a.PassportExpiryDate,a.producttype as  "
					+ "Selected_Card_Type, a.IsFIRCOHit as FIRCO_Flag, a.VisaExpiryDate,a.Tenure, a.EmiratesExpiryDate,"
					+ "a.JobTitle,a.EMI,a.StartofJob,a.GCC_National,a.VisaIssueDate,a.InterestRate,a.ProspectCreationDate, "
					+ "a.NoOfEarningMembers,a.NoOfDependants, a.CustomerDeclaredMonthlyIncome,a.EmiratesVisa,a.VisaSponsor,a.Gender,"
					+ "c.Salary_date_Month_1,c.Salary_date_Month_2,c.Salary_date_Month_3,c.Net_Salary_Month_1,"
					+ "c.Salary_date_Month_2,c.Net_Salary_Month_2,c.Salary_date_Month_3,c.Net_Salary_Month_3,"
					+ "c.Salary_date_Month_4,c.Net_Salary_Month_4,c.Salary_date_Month_5,c.Net_Salary_Month_5, "
					+ "c.Salary_date_Month_6,c.Net_Salary_Month_6,c.Net_Salary_Month_7,c.Stmt_salary7_date,"
					+ "c.Addn_Perfios_EMI_1,c.Addn_Perfios_EMI_2,"
					+ "c.Addn_Perfios_EMI_3,c.Addn_Perfios_EMI_4,c.Addn_Perfios_EMI_5,c.Addn_Perfios_EMI_6,"
					+ "c.Addn_Perfios_EMI_7,c.Addn_Perfios_EMI_8,c.Addn_Perfios_EMI_9,c.Addn_Perfios_EMI_10,"
					+ "c.Addn_Perfios_EMI_11,c.Addn_Perfios_EMI_12,c.Addn_Perfios_EMI_13,c.Addn_Perfios_EMI_14,"
					+ "c.Addn_Perfios_EMI_15,c.Addn_Perfios_EMI_16,c.Addn_Perfios_EMI_17,"
					+ "c.Addn_Perfios_EMI_20,c.Addn_Perfios_EMI_19,c.Addn_Perfios_EMI_18,c.Addn_Perfios_CC,"
					+ "c.Addn_Perfios_OD_Amt,c.Addn_OD_date,c.Joint_Acct,c.High_Value_Deposit,c.Credit_Amount,"
					+ "c.Stmt_chq_rtn_last_3mnts,c.Stmt_chq_rtn_cleared_in30_last_3mnts,c.Stmt_chq_rtn_last_1mnt,"
					+ "c.Stmt_chq_rtn_cleared_in30_last_1mnt,c.Stmt_DDS_rtn_last_3mnts,"
					+ "c.Stmt_DDS_rtn_cleared_in30_last_3mnts,c.Stmt_DDS_rtn_last_1mnt,"
					+ "c.Stmt_DDS_rtn_cleared_in30_last_1mnts,c.Pensioner_flag,c.Name_match,c.UW_reqd,c.FCUindicator,"
					+ "a.RequestedLoanAmount, a.IsSTP, a.EFR_NSTP,a.IsFTSReq as FTS_Flag,a.IsSTLReq,a.Moratorium,c.ProcessingFeePercentage,c.InsuranceFeePercentage "
					+ "FROM NG_DPL_EXTTABLE a with(nolock) inner join NG_DPL_IncomeExpense c on a.WINAME = c.WI_Name WHERE "
					+ "a.WINAME ='"+ wi_name + "'";
			
			logger.debug("Select NG_DPL_EXTTABLE Query: "+DBQuery);

			String[] columns = {"WIName", "CIF", "ProductType", "CustomerName", "PassportNumber", "EmiratesID", "MobileNo","IsSTP", 
					"EmployerCode","EmployerName","EmailID","FirstName", "MiddleName", "LastName", 
					"DOB", "age", "Nationality","TotalMonthlyHouseholdIncome","ProspectID", "PassportExpiryDate",
					"Selected_Card_Type","FIRCO_Flag", "VisaExpiryDate","Tenure", "EmiratesExpiryDate","JobTitle","EMI", "StartofJob",
					"GCC_National","VisaIssueDate","InterestRate","ProspectCreationDate", "NoOfEarningMembers",
					"NoOfDependants","CustomerDeclaredMonthlyIncome","EmiratesVisa","VisaSponsor","Gender","Salary_date_Month_1","Salary_date_Month_2",
					"Salary_date_Month_3","Net_Salary_Month_1","Salary_date_Month_2","Net_Salary_Month_2",
					"Salary_date_Month_3","Net_Salary_Month_3","Salary_date_Month_4","Net_Salary_Month_4",
					"Salary_date_Month_5","Net_Salary_Month_5", "Salary_date_Month_6","Net_Salary_Month_6","Net_Salary_Month_7","Stmt_salary7_date",
					"Addn_Perfios_EMI_1","Addn_Perfios_EMI_2","Addn_Perfios_EMI_3","Addn_Perfios_EMI_4",
					"Addn_Perfios_EMI_5","Addn_Perfios_EMI_6","Addn_Perfios_EMI_7","Addn_Perfios_EMI_8",
					"Addn_Perfios_EMI_9","Addn_Perfios_EMI_10","Addn_Perfios_EMI_11","Addn_Perfios_EMI_12",
					"Addn_Perfios_EMI_13","Addn_Perfios_EMI_14","Addn_Perfios_EMI_15","Addn_Perfios_EMI_16",
					"Addn_Perfios_EMI_17","Addn_Perfios_CC","Addn_Perfios_OD_Amt","Addn_OD_date","Joint_Acct",
					"High_Value_Deposit","Credit_Amount","Stmt_chq_rtn_last_3mnts","Stmt_chq_rtn_cleared_in30_last_3mnts",
					"Stmt_chq_rtn_last_1mnt","Stmt_chq_rtn_cleared_in30_last_1mnt","Stmt_DDS_rtn_last_3mnts",
					"Stmt_DDS_rtn_cleared_in30_last_3mnts","Stmt_DDS_rtn_last_1mnt","Stmt_DDS_rtn_cleared_in30_last_1mnts",
					"Pensioner_flag","Name_match","UW_reqd","FCUindicator","RequestedLoanAmount","IsSTP", "EFR_NSTP","FTS_Flag","IsSTLReq","Moratorium","ProcessingFeePercentage","InsuranceFeePercentage" };
			
			logger.debug("DBQuery: " + DBQuery);
			System.out.println("DBQuery: " + DBQuery);
			Map<String,String> ApplicantDetails_Map = getDataFromDB(DBQuery, cabinetName, sessionID, jtsIP, jtsPort, columns);
			logger.debug("Total Retrieved Records: " + ApplicantDetails_Map.get("TotalRetrieved"));
			System.out.println("Total Retrieved Records: " + ApplicantDetails_Map.get("TotalRetrieved"));
			
			System.out.println();
			String DBQuery_OldSalary ="";
			
			
			DBQuery_OldSalary = "SELECT wi_name, Net_Salary_Month_1, Net_Salary_Month_2, Net_Salary_Month_3 FROM NG_DPL_IncomeExpense with(nolock) "
					+ "WHERE  WI_NAME ='" + wi_name + "'";
				
			logger.debug("Select Old Salary Query: " + DBQuery_OldSalary);
			
			String[] columns_OldSalary = { "wi_name","Net_Salary_Month_1","Net_Salary_Month_2","Net_Salary_Month_3"};
			Map<String, String> ApplicationDetailsOldSalary_Map = getDataFromDB(DBQuery_OldSalary, cabinetName, sessionID, jtsIP, jtsPort,
					columns_OldSalary);
			
			
			StringBuilder stringBuilder = readRequestXmlSample();
			
			String requested_xml = stringBuilder.toString().replace(">Str_ApplicationNumber<",">"+wi_name+"<");
			
			requested_xml = requested_xml.replace(">Str_Cust_Declared_Nationality<",">"+Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("Nationality"))+"<");
			
			/** Application Details Tag**/
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm");
			
			String Prospect_Creation_Date = ApplicantDetails_Map.get("ProspectCreationDate");
			
			
			//in application_Date we have to pass date with time as per jira - 1206
			if(Prospect_Creation_Date == null || Prospect_Creation_Date.isEmpty()){
				Prospect_Creation_Date = "0";
			}else{
				
				String Prospect_Date=Prospect_Creation_Date.substring(0, 10);
				SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MM-yyyy");

				Date entryDatetimeFormat = outputDateFormat.parse(Prospect_Date);
				String formattedEntryDatetime = inputDateformat.format(entryDatetimeFormat);
				logger.debug("FormattedEntryDatetime: " + formattedEntryDatetime);
				
				String Prospect_time=Prospect_Creation_Date.substring(11,Prospect_Creation_Date.length());
				logger.debug("Prospect_time: " + Prospect_time);
				Prospect_Creation_Date=formattedEntryDatetime+"T"+Prospect_time;

			}
			
			String product=ApplicantDetails_Map.get("ProductType");
			logger.debug("product: " + product);
			
			String ProcessingFeePercentage =Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("ProcessingFeePercentage"));
			String InsuranceFeePercentage =Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("InsuranceFeePercentage"));
			
			logger.debug("ProcessingFeePercentage: " + ProcessingFeePercentage);
			logger.debug("InsuranceFeePercentage: " + InsuranceFeePercentage);
			
			if("Islamic".equalsIgnoreCase(product)||"I".equalsIgnoreCase(product)||"ISL".equalsIgnoreCase(product)){
				product = "ISL";
			}else{
				product = "CON";
			}
			
			String application_type = "";
			if(("UAE".equalsIgnoreCase(Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("Nationality"))) || "AE".equalsIgnoreCase(Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("Nationality")))) && ("Y".equalsIgnoreCase(Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("IsSTLReq"))))){
				application_type="NEWN";
			}
			else if(("UAE".equalsIgnoreCase(Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("Nationality"))) || "AE".equalsIgnoreCase(Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("Nationality")))) && ("N".equalsIgnoreCase(Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("IsSTLReq"))))){
				application_type="NSTLN";
			}
			else if((!"UAE".equalsIgnoreCase(Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("Nationality"))) || !"AE".equalsIgnoreCase(Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("Nationality")))) && ("Y".equalsIgnoreCase(Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("IsSTLReq"))))){
				application_type="NEWE";
			}
			else if((!"UAE".equalsIgnoreCase(Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("Nationality"))) || !"AE".equalsIgnoreCase(Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("Nationality")))) && ("N".equalsIgnoreCase(Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("IsSTLReq"))))){
				application_type="NSTLE";
			}
			
			logger.debug("Product Code: " + product);
			requested_xml = requested_xml.replace(">str_TimeStampyyyymmddhhmmsss<",">"+sdf1.format(new Date())+"<");
			
			requested_xml = requested_xml.replace(">Str_full_eligibility_availed<",">Select<")
					.replace(">Str_product_type<",">"+Digital_PL_CommomMethod.validateValue(product)+"<")
					.replace(">Str_app_category<",">BAU<")
					.replace(">Str_requested_product<",">PL<")
					.replace(">Str_requested_limit<",">"+Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("RequestedLoanAmount"))+"<")
					.replace(">Str_sub_product<",">Digital PL<")
					//.replace(">Str_requested_card_product<",">"+Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("Selected_Card_Type"))+"<")
					.replace(">Str_requested_card_product<","><")
					.replace(">Str_application_type<",">"+application_type+"<")//Application_type mapping change "NEWE" as per Test_Dectech_MappingV3 by rubi
					.replace(">Str_tenure<",">"+Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("Tenure"))+"<")
					.replace(">Str_interest_rate<",">"+Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("InterestRate"))+"<")
					.replace(">Str_customer_type<",">NTB<")
					.replace(">Str_final_limit<",">"+Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("RequestedLoanAmount"))+"<")
					.replace(">Str_emi<",">"+Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("EMI"))+"<")
					.replace(">Str_manual_deviation<",">N<")
					.replace(">Str_Moratorium_days<",">"+Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("Moratorium"))+"<")
					.replace(">Str_application_date<",">"+Prospect_Creation_Date+"<");


			
			requested_xml = requested_xml.replace(">Str_Wi_Name<",">"+wi_name+"<");
			
			String app_details = sInputXmlApplicantDetails(ApplicantDetails_Map, cabinetName, sessionID, jtsIP, jtsPort);
			requested_xml = requested_xml.replace("<String_ApplicantDetails>",app_details);
			logger.debug("DPL sInputXmlApplicantDetails : "+ requested_xml);

			/** internal   Bureau TAG and  sub-tag **/
			String internal_Bureau = sInputXmlInternalBureau(ApplicantDetails_Map);
			requested_xml = requested_xml.replace("<String_InternalBureauData>",internal_Bureau);
			logger.debug("DPL sInputXmlExternalBureau : "+ internal_Bureau);		
			
			/** External Bureau sub-tag **/
			String external_Bureau = sInputXmlExternalBureau(ApplicantDetails_Map, cabinetName, sessionID, jtsIP, jtsPort);
			requested_xml = requested_xml.replace("<String_ExternalBureau>",external_Bureau);
			logger.debug("DPL sInputXmlExternalBureau : "+ requested_xml);
			
			/** External Bureau Employment Detail sub-tag **/
			String external_Bureau_EmploymentDetail = sInputXmlExternalBureauEmploymentDetail(wi_name, cabinetName, sessionID, jtsIP, jtsPort);
			requested_xml = requested_xml.replace("<String_ExternalBureauEmploymentDetails>",external_Bureau_EmploymentDetail);
			logger.debug("DPL sInputXmlExternalBureau : "+ requested_xml);
			
			/** Cheque Bounce sub-Tag **/
			String bounced_Cheques = sInputXmlExternalBouncedCheques(wi_name, cabinetName, sessionID, jtsIP, jtsPort);
			requested_xml = requested_xml.replace("<String_BouncedCheques>",bounced_Cheques);
			logger.debug("DPL sInputXmlExternalBouncedCheques : "+ requested_xml);
			
			/** utilization sub-Tag **/
			String utilization = sInputXmlExternalUtilization(wi_name, cabinetName, sessionID, jtsIP, jtsPort);
			requested_xml = requested_xml.replace("<String_Utilization24months>",utilization);
			logger.debug("DPL sInputXmlExternalBouncedCheques : "+ requested_xml);
			
			/** utilization sub-Tag **/
			String history = sInputXmlExternalHistory(wi_name, cabinetName, sessionID, jtsIP, jtsPort);
			requested_xml = requested_xml.replace("<String_History_24months>",history);
			logger.debug("DPL sInputXmlExternalBouncedCheques : "+ requested_xml);
			
			/** Court Cases sub-Tag **/
			String court_cases = sInputXmlExternalCourtCase(wi_name, cabinetName, sessionID, jtsIP, jtsPort);
			requested_xml = requested_xml.replace("<String_CourtCase>",court_cases);
			logger.debug("DPL sInputXmlExternalCourtCase : "+ requested_xml);
		
			
			//** ExternalBureau Account Details sub-Tag **//*
			String ExternalBureauAccountDetails = sInputXmlExternalBureauAccountDetails(wi_name, cabinetName, sessionID, jtsIP, jtsPort,ApplicantDetails_Map.get("ProspectID"));
			requested_xml = requested_xml.replace("<String_ExternalBureauAccountDetails>",ExternalBureauAccountDetails);
			logger.debug("DPL sInputXmlExternalCourtCase : "+ requested_xml);
			
			//** ExternalBureau Salary Details sub-Tag **//*
			String ExternalBureauSalaryDetails = sInputXmlExternalBureauSalaryDetails(wi_name, cabinetName, sessionID, jtsIP, jtsPort);
			requested_xml = requested_xml.replace("<String_ExternalBureauSalaryDetails>",ExternalBureauSalaryDetails);
			logger.debug("DPL sInputXmlExternalCourtCase : "+ requested_xml);
		
			/** External Bureau Individual Products sub-Tag **/
			String individual_Products = sInputXmlExternalBureauIndividualProducts(wi_name, cabinetName, sessionID, jtsIP, jtsPort);
			requested_xml = requested_xml.replace("<String_ExternalBureauIndividualProducts>",individual_Products);
			logger.debug("DPL sInputXmlExternalBureauIndividualProducts : "+ requested_xml);
			
			/** External Bureau Pipeline Products sub-tag**/
			String pipeline_Products = sInputXmlExternalBureauPipelineProducts(wi_name, cabinetName, sessionID, jtsIP, jtsPort);
			requested_xml = requested_xml.replace("<String_ExternalBureauPipelineProducts>",pipeline_Products);
			logger.debug("DPL sInputXmlExternalBureauPipelineProducts : "+ requested_xml);
			
			/** External Bureau Pipeline Products sub-tag**/
			logger.debug("DPL FTS_FLAG : "+ Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("FTS_FLAG")));	
			if(!"N".equalsIgnoreCase(Digital_PL_CommomMethod.validateValue(ApplicantDetails_Map.get("FTS_FLAG")))){
				String perfios_details = sInputXmlPerfios(ApplicantDetails_Map, ApplicationDetailsOldSalary_Map);
				logger.debug("DPL perfios_details : "+ perfios_details);
				requested_xml = requested_xml.replace("<String_Perfios>",perfios_details);
			}
			
			logger.debug("DPL Final XML : "+ requested_xml);
			
			String integrationStatus="Success";
			String columnNames = "";
	    	String columnValues ="";
			
	    	StringBuilder finalString=new StringBuilder(requested_xml);
			HashMap<String, String> socketConnectionMap = Digital_PL_CommomMethod.socketConnectionDetails(cabinetName, jtsIP, jtsPort, sessionID); 
			
			Digital_PL_CommomMethod commonMethod = new Digital_PL_CommomMethod();
			integrationStatus = commonMethod.socketConnection(cabinetName, CommonConnection.getUsername(), sessionID, jtsIP, jtsPort, wi_name, ws_name, 60, 65,socketConnectionMap, finalString);
			
			XMLParser xmlParserSocketDetails= new XMLParser(integrationStatus);
			logger.debug(" xmlParserSocketDetails : "+xmlParserSocketDetails);
			
			String SystemErrorCode = xmlParserSocketDetails.getValueOf("SystemErrorCode");
			logger.debug("SystemErrorCode : "+SystemErrorCode+" for WI: "+wi_name);
			
			String SystemErrorMessage = xmlParserSocketDetails.getValueOf("SystemErrorMessage");
			logger.debug("SystemErrorMessage : "+SystemErrorMessage+" for WI: "+wi_name);
			
			String Deviationinsertstatus="";
			if (SystemErrorCode != null && !SystemErrorCode.equals(""))
			{
				returnValue="Failed";
				decisionValue = "Failed";
				logger.debug("Decision in else : " +decisionValue);
				
				columnNames = "Dectech_Flag";
		    	columnValues = "'N'";
			}
			else 
			{
				String Output_Decision = xmlParserSocketDetails.getValueOf("Output_Decision");
				logger.debug("Output_Decision: "+Output_Decision+ "WI: "+wi_name);
				
				String Output_NSTP = xmlParserSocketDetails.getValueOf("Output_NSTP");
				logger.debug("Output_NSTP: "+Output_NSTP+ "WI: "+wi_name);
				
				String Output_NSTP_Reason = xmlParserSocketDetails.getValueOf("Output_NSTP_Reason");
				logger.debug("Output_NSTP_Reason: "+Output_NSTP_Reason+ "WI: "+wi_name);
				
				String Output_TAI = xmlParserSocketDetails.getValueOf("Output_TAI");
				logger.debug("Output_TAI: "+Output_TAI+ "WI: "+wi_name);
				
				String Output_Final_DBR = xmlParserSocketDetails.getValueOf("Output_Final_DBR");
				logger.debug("Output_Final_DBR: "+Output_Final_DBR+ "WI: "+wi_name);
				
				String Output_Affordable_Ratio  = xmlParserSocketDetails.getValueOf("Output_Affordable_Ratio");
				logger.debug("Output_Affordable_Ratio : "+Output_Affordable_Ratio + "WI: "+wi_name);
				
				String Output_Affordable_EMI  = xmlParserSocketDetails.getValueOf("Output_Affordable_EMI");
				logger.debug("Output_Affordable_EMI : "+Output_Affordable_Ratio + "WI: "+wi_name);
				
				String Output_Interest_Rate  = xmlParserSocketDetails.getValueOf("Output_Interest_Rate");
				logger.debug("Output_Interest_Rate : "+Output_Affordable_Ratio + "WI: "+wi_name);
				
				
				String Output_TotalDeduction = xmlParserSocketDetails.getValueOf("Output_TotalDeduction");
				logger.debug("Output_TotalDeduction: "+Output_TotalDeduction+ "WI: "+wi_name);
				
				String Output_Stress_BufferAmt = xmlParserSocketDetails.getValueOf("Output_Stress_BufferAmt");
				logger.debug("Output_Stress_BufferAmt: "+Output_Stress_BufferAmt+ "WI: "+wi_name);
				
				String Output_Eligible_Amount = xmlParserSocketDetails.getValueOf("Output_Eligible_Amount");
				logger.debug("Output_Eligible_Amount: "+Output_Eligible_Amount+ "WI: "+wi_name);
				
				String Output_Delegation_Authority = xmlParserSocketDetails.getValueOf("Output_Delegation_Authority");
				logger.debug("Output_Delegation_Authority: "+Output_Delegation_Authority+ "WI: "+wi_name);
				
				String Output_Age = xmlParserSocketDetails.getValueOf("Output_Age");
				logger.debug("Output_Age: "+Output_Age+ "WI: "+wi_name);
				
				String Output_Maturity_Age = xmlParserSocketDetails.getValueOf("Output_Maturity_Age");
				logger.debug("Output_Maturity_Age: "+Output_Maturity_Age+ "WI: "+wi_name);
				
				String Output_NoOf_AECBHistory = xmlParserSocketDetails.getValueOf("Output_NoOf_AECBHistory");
				logger.debug("Output_NoOf_AECBHistory: "+Output_NoOf_AECBHistory+ "WI: "+wi_name);
				
				String Output_EMI = xmlParserSocketDetails.getValueOf("Output_EMI");
				logger.debug("Output_EMI: "+Output_EMI+ "WI: "+wi_name);
				
				
				String Output_PerMon_Interest = xmlParserSocketDetails.getValueOf("Output_PerMon_Interest");
				logger.debug("Output_PerMon_Interest: "+Output_PerMon_Interest+ "WI: "+wi_name);
				
				// override wi creation tenure to dectech outout_tenure after discussion with simi 
				String Tenure = xmlParserSocketDetails.getValueOf("Output_Tenor");
				logger.debug("Output_EMI: "+Tenure+ "WI: "+wi_name);
				
				
				
				
				String Output_AECB_Salary_1 = xmlParserSocketDetails.getValueOf("Output_AECB_Salary_1");
				String Output_AECB_Salary_2 = xmlParserSocketDetails.getValueOf("Output_AECB_Salary_2");
				String Output_AECB_Salary_3 = xmlParserSocketDetails.getValueOf("Output_AECB_Salary_3");
				
				logger.debug("Output_AECB_Salary_1: "+Output_AECB_Salary_1+ "WI: "+wi_name);
				logger.debug("Output_AECB_Salary_2: "+Output_AECB_Salary_2+ "WI: "+wi_name);
				logger.debug("Output_AECB_Salary_3: "+Output_AECB_Salary_3+ "WI: "+wi_name);
				
				
				String Output_Final_Amount = xmlParserSocketDetails.getValueOf("Output_Final_Amount");
				logger.debug("Output_Final_Amount: "+Output_Final_Amount+ "WI: "+wi_name);
				
				String Output_Salary_Multiples = xmlParserSocketDetails.getValueOf("Output_Salary_Multiples");
				logger.debug("Output_Salary_Multiples: "+Output_Salary_Multiples+ "WI: "+wi_name);
				
				String Output_Stress_DBR = xmlParserSocketDetails.getValueOf("Output_Stress_DBR");
				logger.debug("Output_Stress_DBR: "+Output_Stress_DBR+ "WI: "+wi_name);
				
				Output_Age= Digital_PL_CommomMethod.validateValue(Output_Age);
				if (Output_Age.length() > 5){
					Output_Age=Output_Age.substring(0,Output_Age.lastIndexOf("."));
				}
				String is_stp = "";
				if (Output_NSTP != null && !"".equals(Output_NSTP)) {
					if (Output_NSTP.equals("Y"))
						is_stp = "N";
					else if (Output_NSTP.equals("N"))
						is_stp = "Y";
				}
				
				logger.debug("Output_NSTP_Reason-- " + Output_NSTP_Reason);
				String final_NSTP_Reason ="";
				
				if(Output_NSTP_Reason!=null && !"".equalsIgnoreCase(Output_NSTP_Reason))
				{
					String reasons[]=Output_NSTP_Reason.split(",");
					for(int i =0;i<reasons.length;i++)
					{
						String temp = reasons[i];
						if(i==0)
							final_NSTP_Reason += temp.substring(temp.indexOf(":")+2,temp.indexOf("}")-1);
						else
							final_NSTP_Reason += "~"+temp.substring(temp.indexOf(":")+2,temp.indexOf("}")-1);
					}
				}
				
				logger.debug("Final nstp reason-- " + final_NSTP_Reason);
			       
				
				String Output_Eligible_Card = xmlParserSocketDetails.getValueOf("Output_Eligible_Cards");
				logger.debug("Output_Eligible_Card: "+Output_Eligible_Amount+ " WI: "+wi_name);
				
				String[] Output_Eligible_Cards_Arr=Output_Eligible_Card.split("\\},\\{");
				String card_Product = "";
							
				logger.debug("Output_Eligible_Cards_Arr.length "+ Output_Eligible_Cards_Arr.length);
				if(Output_Eligible_Cards_Arr.length>0)
				{
					for (int i=0; i<Output_Eligible_Cards_Arr.length;i++){
						logger.debug("Value iof i "+ i);
						logger.debug("Output_Eligible_Cards_Arr[i]:"+Output_Eligible_Cards_Arr[i]);
						String[] Output_Eligible_Cards_Array=Output_Eligible_Cards_Arr[i].split(",");
						
						if(Output_Eligible_Cards_Array.length==3){
							String [] CArdProducyList= Output_Eligible_Cards_Array[0].split(":");
							logger.debug("Output_Eligible_Cards_Product:"+CArdProducyList[0]+" "+CArdProducyList[1]);
							
							if(!card_Product.isEmpty()){
								logger.debug("Output_Eligible_Cards if block:"+card_Product);
								
								card_Product = card_Product+","+CArdProducyList[1];
							}else{
								logger.debug("Output_Eligible_Cards else block:"+card_Product);
								
								card_Product += CArdProducyList[1];
							}
						}
					}
				}
				logger.debug("card_Product: "+card_Product+ "WI: "+wi_name);
				
				card_Product = card_Product.replaceAll("\"", "");
				card_Product=card_Product.replaceAll(",","~");
				String OutputAlternateCard = xmlParserSocketDetails.getValueOf("Output_Alternate_Card");
				
				logger.debug("card_Product after replace: "+card_Product+ "WI: "+wi_name);
				
				logger.debug("Output_Alternate_Card: "+OutputAlternateCard+ "WI: "+wi_name);
				OutputAlternateCard = OutputAlternateCard.replaceAll("\"", "");
				logger.debug("OutputAlternateCard: "+OutputAlternateCard+ "WI: "+wi_name);
				
				//calculate processing fees and insurance fees
				double ProcessingFee=0;
				double InsuranceFee=0;
				try{
				
				if(ProcessingFeePercentage != null && Output_Final_Amount != null)
				ProcessingFee = Double.parseDouble(ProcessingFeePercentage) * Double.parseDouble(Output_Final_Amount)/100;
				
				//ProcessingFee
				if(InsuranceFeePercentage != null && Output_Final_Amount != null)
				InsuranceFee = Double.parseDouble(InsuranceFeePercentage) * Double.parseDouble(Output_Final_Amount)/100;
				
				if(ProcessingFee > 2625){
					ProcessingFee = 2625;
				}else if(ProcessingFee < 525){
					ProcessingFee = 525;
				}
				
				if(InsuranceFee > 2625){
					InsuranceFee = 2625;
				}else if(InsuranceFee < 525){
					InsuranceFee = 525;
				}
				}catch(Exception e){
					logger.debug("error in ProcessingFeePercentage & InsuranceFeePercentage: "+e.getMessage()+ "WI: "+wi_name);
				}
				logger.debug("ProcessingFee: "+ProcessingFee+ "WI: "+wi_name);
				logger.debug("InsuranceFee: "+InsuranceFee+ "WI: "+wi_name);
				
				columnNames = "IsSTP,DectechDecision,Non_STP_reason,Output_TotalDeduction,"
						+ "Output_Stress_BufferAmt,Final_Limit,Dectech_Flag,delegation_authority,age,OutputAlternateCard"
						+ ",Output_Eligible_Card,FinalDBR,FinalTAI,LoanAmount,LoanMultiple,StressDBR,AffordabilityRatio,Output_Affordable_EMI,"
						+ "Output_Interest_Rate,Output_Maturity_Age,EMI,ProcessingFee,InsuranceFee,Tenure,Output_PerMon_Interest";
				
				columnValues = "'" + is_stp + "','"+ Output_Decision +"','"+ final_NSTP_Reason +
		    			"','"+ Output_TotalDeduction +"','"+ Output_Stress_BufferAmt+"','"+ Output_Eligible_Amount +
		    			"','Y','"+Output_Delegation_Authority+"','" +Output_Age+"','"+OutputAlternateCard+"','"+
		    			card_Product+"','"+Output_Final_DBR+"','"+Output_TAI+"','"+Output_Final_Amount+"',"
		    					+ "'"+Output_Salary_Multiples+"','"+Output_Stress_DBR+"','"+Output_Affordable_Ratio+"','"
		    			+Output_Affordable_EMI+"','"+Output_Interest_Rate+"','"+Output_Maturity_Age+"','"+Output_EMI+"','"+ProcessingFee+"','"+InsuranceFee+"','"+Tenure+"','"+Output_PerMon_Interest+"'";
		    	
				
				logger.debug("card_Product columnValues: "+columnValues);
		    	
				Deviationinsertstatus=addDeviations(xmlParserSocketDetails,wi_name);
		    	returnValue="Success";
				decisionValue = "Success";
				logger.debug("Decision in success: " +decisionValue);
				//attributesTag="<Decision>"+decisionValue+"</Decision>";
				
				//add Worst_Status description 
				//String worstStatusQuery = "select CONCAT(code, ' - ' , description) as wostStatus from USR_0_iRBL_Worst_Status_Master with(nolock) where Code = '"+Worst_Status+"'";
				String worstStatusQuery = "select description as wostStatus from USR_0_iRBL_Worst_Status_Master with(nolock) where Code = '"+Worst_Status+"'";
				
				String worstStatusInput = CommonMethods.apSelectWithColumnNames(worstStatusQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false));
	        	logger.info("queryDataInput No" + worstStatusInput);
	        	
	        	String worstStatusOutputXML = CommonMethods.WFNGExecute(worstStatusInput, jtsIP, jtsPort, 1);
				logger.info("Insert No outXml" + worstStatusOutputXML);
				XMLParser sworstStatusXMLParserChild = new XMLParser(worstStatusOutputXML);
				Worst_Status = sworstStatusXMLParserChild.getValueOf("wostStatus");
				
				
				
				String columnName = "AECB_history,AECB_Sal_Month_1,AECB_Sal_Month_2,AECB_Sal_Month_3,WI_Name,AECB_DDS_Return,WorstStatus";
	        	String columnValue = "'"+AECB_Hist_Month_Count+"','"+Output_AECB_Salary_1+"','"+Output_AECB_Salary_2+"','"+Output_AECB_Salary_3+"','"+wi_name+"','"+AECB_DDS_Return+"','"+Worst_Status+"'";
	        	
	        	
	        	String checkquery= "select wi_name from NG_DPL_AECB_Details with(nolock) where wi_name = '" + wi_name + "'";
	        	logger.info("checkquery No" + checkquery);
	        	
	        	String queryDataInput = CommonMethods.apSelectWithColumnNames(checkquery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false));
	        	logger.info("queryDataInput No" + queryDataInput);
	        	
	        	String outputXML = CommonMethods.WFNGExecute(queryDataInput, jtsIP, jtsPort, 1);
				logger.info("Insert No outXml" + outputXML);
	        	
				XMLParser sXMLParserChild = new XMLParser(outputXML);
				String StrMainCode = sXMLParserChild.getValueOf("MainCode");
				int iTotalrec = Integer.parseInt(sXMLParserChild.getValueOf("TotalRetrieved"));
				
				if (sXMLParserChild.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0){
					String sWhereClause = "WI_NAME='" + wi_name + "'";
					logger.info("columnName:" + columnName);
					logger.info("columnValue:" + columnValue);
					String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false), 
			        		"NG_DPL_AECB_Details", columnName, columnValue, sWhereClause);
			        logger.debug("Input XML for apUpdateInput for " + "NG_DPL_AECB_Details" + " Table : " + inputXML);
			        
			        String outputXml = CommonMethods.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
			        logger.debug("Output XML for apUpdateInput for " + "NG_DPL_AECB_Details" + " Table : " + outputXml);
			        
			       sXMLParserChild = new XMLParser(outputXml);
			       StrMainCode = sXMLParserChild.getValueOf("MainCode");
			        //String RetStatus = null;
			        if (!StrMainCode.equals("0")|| !"Success".equalsIgnoreCase(Deviationinsertstatus))
			        {
			        	returnValue="Failed";
			        	logger.debug("Error in Executing apUpdateInput sOutputXML for dectech field update: " + outputXml);
			        }
				}else{
					String inputXML = CommonMethods.apInsert(CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false), 
		        			columnName, columnValue, "NG_DPL_AECB_Details");
		        	
		        	logger.info("Insert No" + inputXML);
					outputXML = CommonMethods.WFNGExecute(inputXML, jtsIP, jtsPort, 1);
					logger.info("Insert No outXml" + outputXML);
					
					sXMLParserChild = new XMLParser(outputXML);
			        StrMainCode = sXMLParserChild.getValueOf("MainCode");
			        
					if (!StrMainCode.equals("0"))
			        {
			        	returnValue="Failed";
			        	logger.debug("Error in Executing AP Insert sOutputXML for dectech field update: ");
			        }else{
			        	logger.debug("Success fully Execute AP Insert: ");
			        }
				}
				
	        	
			}
			
	    	String sWhereClause = "WINAME='" + wi_name + "'";
	    	String tableName = "NG_DPL_EXTTABLE";
	        
	    	String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false), 
	        		tableName, columnNames, columnValues, sWhereClause);
	        logger.debug("Input XML for apUpdateInput for " + tableName + " Table : " + inputXML);
	        
	        String outputXml = CommonMethods.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
	        logger.debug("Output XML for apUpdateInput for " + tableName + " Table : " + outputXml);
	        
	        XMLParser sXMLParserChild = new XMLParser(outputXml);
	        String StrMainCode = sXMLParserChild.getValueOf("MainCode");
	        //String RetStatus = null;
	        if (!StrMainCode.equals("0")|| !"Success".equalsIgnoreCase(Deviationinsertstatus))
	        {
	        	returnValue="Failed";
	        	logger.debug("Error in Executing apUpdateInput sOutputXML for dectech field update: " + outputXml);
	        }
		} catch (Exception e) {
			logger.debug("Exception: "+e.getMessage());
			return "Failed";
		}
		return returnValue;
	}


	private static  Map<String,String> getDataFromDB(String query, String cabinetName, String sessionID, String jtsIP, String jtsPort, String... columns) {
		try{
			logger.debug("Inside function getDataFromDB");
			logger.debug("getDataFromDB query is: "+query);
			
			String InputXML = CommonMethods.apSelectWithColumnNames(query, cabinetName, sessionID);
			Map<String,String> temp = null;
			
			String OutXml = CommonMethods.WFNGExecute(InputXML, jtsIP, jtsPort, 1);
			OutXml = OutXml.replaceAll("&", "#andsymb#");
			logger.debug("getDataFromDB output xml is: "+OutXml);
			
			Document recordDoc1 = MapXML.getDocument(OutXml);
			NodeList records1 = recordDoc1.getElementsByTagName("Records");
			if (records1.getLength() > 0) {
				temp = new HashMap<String,String>();
				for(String column : columns) {
					System.out.println("column:"+column);
					String value= getTagValue(OutXml, column).replaceAll("#andsymb#", "&");
					
					logger.debug("value from getTagValue function is:"+value);
					if(null!=value && !"null".equalsIgnoreCase(value) && !"".equals(value)){
						logger.debug("Setting value of "+column+" as "+value);	
						System.out.println("Setting value of "+column+" as "+value);
						temp.put(column, value);
					}
					else{
						logger.debug("Setting value of "+column+" as blank");
						temp.put(column, "");
					}
				}
				temp.put("TotalRetrieved",getTagValue(OutXml, "TotalRetrieved"));
			}
			return temp;	
		}
		catch(Exception ex){
			logger.debug("Exception in getDataFromDB method + "+ex.getMessage());
			return null;
		}
	}

	private static  List<Map<String,String>> getDataFromDBMap(String query, String cabinetName, String sessionID, String jtsIP, String jtsPort){
		try{
			logger.debug("Inside function getDataFromDB");
			logger.debug("getDataFromDB query is: "+query);
			
			String InputXML = CommonMethods.apSelectWithColumnNames(query, cabinetName, sessionID);
			List<Map<String,String>> temp = new ArrayList<Map<String,String>>();
			
			String OutXml = CommonMethods.WFNGExecute(InputXML, jtsIP, jtsPort, 1);
			OutXml = OutXml.replaceAll("&", "#andsymb#");
			Document recordDoc1 = MapXML.getDocument(OutXml);
			NodeList records1 = recordDoc1.getElementsByTagName("Record");
			
			if (records1.getLength() > 0) {
				for(int i=0;i<records1.getLength();i++){
					Node n = records1.item(i);
					Map<String,String> t = new HashMap<String,String>();
					if(n.hasChildNodes()) {
						NodeList child = n.getChildNodes();
						for(int j=0;j<child.getLength();j++) {
							Node n1 = child.item(j);
							String column = n1.getNodeName();
							String value = n1.getTextContent().replaceAll("#andsymb#", "&");
							if(null!=value && !"null".equalsIgnoreCase(value) && !"".equals(value)){
								logger.debug("getDataFromDBMap Setting value of "+column+" as "+value);	
								t.put(column, value);
							}
							else{
								logger.debug("getDataFromDBMap Setting value of "+column+" as blank");
								t.put(column, "");
							}
						}
					}
					temp.add(t);
				}
			}
			return temp;	
		}
		catch(Exception ex){
			logger.debug("Exception in getDataFromDBMap method + "+ex.getMessage());
			return null;
		}

	}
	
	private static  StringBuilder readRequestXmlSample() {
		StringBuilder sb = new StringBuilder("");
		try {
			String fileLocation = new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DPL_Integration")
					.append(System.getProperty("file.separator")).append("DECTECH_Integration.txt").toString();
			BufferedReader sbf = new BufferedReader(new FileReader(fileLocation));
			
			String line = sbf.readLine();
			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = sbf.readLine();
			}
		} catch (FileNotFoundException e) {
			logger.error("In Reading DECTECH_Integration.txt : " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("In Reading DECTECH_Integration.txt : " + e.getMessage());
			e.printStackTrace();
		}
		return sb;
	}
	
	private static  String sInputXmlApplicantDetails(Map<String, String> applicantDetails_Map, String cabinetName, String sessionID, String jtsIP, String jtsPort) {
		
		String industry_sector = "";
		String industry_macro = Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("Industry"));
		String industry_micro = Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("Sub_Industry"));
		
		String COMPANY_STATUS_CC = "";
		String COMPANY_STATUS_PL = "";
		String  INCLUDED_IN_CC_ALOC = ""; 
		String  INCLUDED_IN_PL_ALOC = "";
		String  current_emp_catogery = "";
		String TYPE_OF_COMPANY= "";
		String EMPLOYER_CATEGORY_PL_EXPAT="";
		String EMPLOYER_CATEGORY_PL_NATIONAL="";
		String PAYROLL_FLAG="";
		
		String employercode = Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("EmployerCode"));
		
		if (!employercode.equals("")) {
			String query = "select TOP 1 INDUSTRY_SECTOR, INDUSTRY_MACRO, INDUSTRY_MICRO, COMPANY_STATUS_CC,COMPANY_STATUS_PL, "
					+ "INCLUDED_IN_CC_ALOC, INCLUDED_IN_PL_ALOC, EMPLOYER_CATEGORY_PL, TYPE_OF_COMPANY, EMPLOYER_CATEGORY_PL_EXPAT,PAYROLL_FLAG, "
					+ "EMPLOYER_CATEGORY_PL_NATIONAL from NG_RLOS_ALOC_OFFLINE_DATA WITH(nolock) where EMPLOYER_CODE=main_Employer_code "
					+ "and main_Employer_code = '" + employercode+ "'";
			try {
				
				String EMPLOYER_CATEGORY_PL = "";
				
				List<Map<String,String>> OutputXML_ref = getDataFromDBMap(query, cabinetName, sessionID, jtsIP, jtsPort);
				if(OutputXML_ref.size()>0)
				{
					industry_sector = OutputXML_ref.get(0).get("INDUSTRY_SECTOR");
					
					if (!Digital_PL_CommomMethod.validateValue(OutputXML_ref.get(0).get("INDUSTRY_MACRO")).equals(""))
						industry_macro = OutputXML_ref.get(0).get("INDUSTRY_MACRO");

					if (!Digital_PL_CommomMethod.validateValue(OutputXML_ref.get(0).get("INDUSTRY_MICRO")).equals(""))
						industry_micro=OutputXML_ref.get(0).get("INDUSTRY_MICRO");

					COMPANY_STATUS_CC=OutputXML_ref.get(0).get("COMPANY_STATUS_CC");
					COMPANY_STATUS_PL=OutputXML_ref.get(0).get("COMPANY_STATUS_PL");
					INCLUDED_IN_CC_ALOC=OutputXML_ref.get(0).get("INCLUDED_IN_CC_ALOC");
					INCLUDED_IN_PL_ALOC=OutputXML_ref.get(0).get("INCLUDED_IN_PL_ALOC");
					EMPLOYER_CATEGORY_PL=OutputXML_ref.get(0).get("EMPLOYER_CATEGORY_PL");
					TYPE_OF_COMPANY=OutputXML_ref.get(0).get("TYPE_OF_COMPANY");
					EMPLOYER_CATEGORY_PL_EXPAT=OutputXML_ref.get(0).get("EMPLOYER_CATEGORY_PL_EXPAT");
					EMPLOYER_CATEGORY_PL_NATIONAL=OutputXML_ref.get(0).get("EMPLOYER_CATEGORY_PL_NATIONAL");
					PAYROLL_FLAG=OutputXML_ref.get(0).get("PAYROLL_FLAG");
					
				}
				if (!"".equals(EMPLOYER_CATEGORY_PL)) {
					String queryForEmpCat= "select Code from NG_MASTER_EmployerCategory_PL with(nolock) where Code='" + EMPLOYER_CATEGORY_PL+ "' and IsActive='Y'";
					List<Map<String,String>> OutputXML4EmpCat = getDataFromDBMap(queryForEmpCat, cabinetName, sessionID, jtsIP, jtsPort);
					if(OutputXML4EmpCat.size()>0)
					{
						current_emp_catogery=OutputXML4EmpCat.get(0).get("Code");
					}
				}
			}
			catch(Exception e)
			{
				logger.debug(" Exception occurred in ApplicantDetails Query"+ query);
				logger.debug(" Exception occurred in sInputXmlApplicantDetails()"+ e.getMessage());
			}
		}
		
		String world_check="N";
		//if (validateValue(applicantDetails_Map.get("FIRCO_Flag")).equalsIgnoreCase("Y") || validateValue(applicantDetails_Map.get("FIRCO_Flag")).equalsIgnoreCase("CB")) {
		if (Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("FIRCO_Flag")).equalsIgnoreCase("CB")) {
			world_check= "Y";
		}
		
		String no_bank_other_statement_provided = "";
		logger.debug("FTS_Flag"+ Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("FTS_Flag")));
		//change FIRCO_Flag to ISFTSReq as per the test_Dectech_MappingV3 by rubi
		if("Y".equalsIgnoreCase(Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("FTS_Flag")))){
			no_bank_other_statement_provided = "6";
		}
		logger.debug("no_bank_other_statement_provided:"+ no_bank_other_statement_provided);
		
		String gender_code="";
		if("FEMALE".equalsIgnoreCase(Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("Gender")))){
			gender_code="F";
		}else if("MALE".equalsIgnoreCase(Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("Gender")))){
			gender_code="M";
		}else{
			gender_code="U";
		}
		
		return "<ApplicantDetails>" + ""
		+"<applicant_id>"+applicantDetails_Map.get("WIName")+"</applicant_id>" + ""
		+"<primary_cif>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("CIF"))+"</primary_cif>" + ""
		+"<ref_no>"+applicantDetails_Map.get("ProspectID")+"</ref_no>" + ""
		+"<wi_name>"+applicantDetails_Map.get("WIName")+"</wi_name>" + ""
		+"<cust_name>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("CustomerName"))+"</cust_name>" + ""
		+"<emp_type>"+"S"+"</emp_type>" + ""
		+"<dob>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("DOB"))+"</dob>" + ""
		+"<age>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("Age"))+"</age>" + ""
		+"<nationality>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("Nationality"))+"</nationality>" + ""
		+"<resident_flag>Y</resident_flag>" + ""
		+"<world_check>"+world_check+"</world_check>" + ""
		+"<no_of_cheque_bounce_int_3mon_Ind></no_of_cheque_bounce_int_3mon_Ind>" + "" //TODO This should be if UW adds in grid in IBPS-If cheque
		+"<no_of_dds_return_int_3mon_Ind></no_of_dds_return_int_3mon_Ind>" + "" //TODO This should be if UW adds in grid in IBPS- If DDS
		+"<los>"+Digital_PL_CommomMethod.CalculatLOS(applicantDetails_Map.get("StartofJob"))+"</los>" + ""
		+"<target_segment_code>DIG</target_segment_code>" + ""
		+"<current_emp_catogery>"+current_emp_catogery+"</current_emp_catogery>" + ""
		+"<visa_issue_date>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("VisaIssueDate"))+"</visa_issue_date>" + ""
		+"<visa_expiry_date>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("VisaExpiryDate"))+"</visa_expiry_date>" + ""
		+"<passport_expiry_date>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("PassportExpiryDate"))+"</passport_expiry_date>" + ""
		+"<emirates_visa>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("EmiratesVisa"))+"</emirates_visa>" + ""
		+"<designation>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("JobTitle"))+"</designation>" + ""
		+"<gender>"+gender_code+"</gender>" + ""
		+"<cust_mobile_no>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("MobileNo"))+"</cust_mobile_no>" + ""
		+"<emp_name>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("EmployerName"))+"</emp_name>" + ""
		+"<emp_code>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("EmployerCode"))+"</emp_code>" + ""
		+"<type_of_company>"+TYPE_OF_COMPANY+"</type_of_company>" + ""
		+"<industry_sector>"+industry_sector+"</industry_sector>" + ""
		+"<industry_macro>"+industry_macro+"</industry_macro>" + ""
		+"<industry_micro>"+industry_micro+"</industry_micro>" + ""
		+"<no_bank_other_statement_provided>"+no_bank_other_statement_provided+"</no_bank_other_statement_provided>" + ""
		+"<cc_employer_status>"+COMPANY_STATUS_CC+"</cc_employer_status>" + ""
		+"<pl_employer_status>"+COMPANY_STATUS_PL+"</pl_employer_status>" + ""
		+"<pl_employer_status_expat>"+EMPLOYER_CATEGORY_PL_EXPAT+"</pl_employer_status_expat>" + ""
		+"<pl_employer_status_national>"+EMPLOYER_CATEGORY_PL_NATIONAL+"</pl_employer_status_national>" + ""
		+"<ces>"+""+"</ces>" + ""
		+"<included_pl_aloc>"+INCLUDED_IN_PL_ALOC+"</included_pl_aloc>" + ""
		+"<included_cc_aloc>"+INCLUDED_IN_CC_ALOC+"</included_cc_aloc>" + ""
		+"<visa_sponsor>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("VisaSponsor"))+"</visa_sponsor>" + ""
		+"<country_of_residence>AE</country_of_residence>" + ""
		+"<gcc_national>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("GCC_National"))+"</gcc_national>" + ""
		+"<company_type>N</company_type>" + ""
		+"<freezone_comp></freezone_comp>" + ""
		+"<aecb_consent>Y</aecb_consent>" + ""
		+"<No_of_dependants>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("NoOfDependants"))+"</No_of_dependants>" + "" 
		+"<Other_household_income>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("TotalMonthlyHouseholdIncome"))+"</Other_household_income>" + ""
		+"<No_earning_members>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("NoOfEarningMembers"))+"</No_earning_members>" + ""
		+"<EFR_NSTP>"+""+"</EFR_NSTP>" + ""
		+"<marketing_code>DIG</marketing_code>" + ""
		+"<payroll_flag>"+PAYROLL_FLAG+"</payroll_flag>" + ""
		+"</ApplicantDetails>" ;
	}      
	
	public static String sInputXmlInternalBureau(Map<String, String> applicantDetails_Map) {
		String IsSTLReq=applicantDetails_Map.get("IsSTLReq");
		
		String internal_bureau=  "<InternalBureauData>"+""
			    +"<InternalBureau>"+""
			      +"<company_flag>N</company_flag>"+""
			    +"</InternalBureau>"+""
			    +"<InternalBouncedCheques>"+""
			      +"<company_flag>N</company_flag>"+""
			    +"</InternalBouncedCheques>"+""
			    +"<InternalBureauIndividualProducts>"+""
			      +"<company_flag>N</company_flag>"+""
			    +"</InternalBureauIndividualProducts>"+""
			    +"<InternalBureauPipelineProducts>"+""
			      +"<company_flag>N</company_flag>"+""
			    +"</InternalBureauPipelineProducts>"+""
			    +"<InternalBureauDBRTAICalc>"+""
			      +"<basic>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("CustomerDeclaredMonthlyIncome"))+"</basic>"+""
			      +"<gross_salary>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("CustomerDeclaredMonthlyIncome"))+"</gross_salary>"+""
			      +"<net_salary_mon1>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("Net_Salary_Month_1"))+"</net_salary_mon1>"+""
			      +"<net_salary_mon2>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("Net_Salary_Month_2"))+"</net_salary_mon2>"+""
			      +"<net_salary_mon3>"+Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("Net_Salary_Month_3"))+"</net_salary_mon3>"+""
			      +"<salary_flag>"+Digital_PL_CommomMethod.validateValue(IsSTLReq)+"</salary_flag>"+""
			      +"</InternalBureauDBRTAICalc>"+""
			  +"</InternalBureauData>";
			
			logger.debug("internal_bureau: "+internal_bureau);
			  return internal_bureau;
		}
	
	private static String sInputXmlExternalBureau(Map<String, String> applicantDetails_Map, String cabinetName, String sessionID, String jtsIP, String jtsPort) 
	{
		String Wi_Name = applicantDetails_Map.get("WIName");
		
		logger.debug("inside ExternalBureauData : ");
		String sQuery = "select top 1 CifId, fullnm,TotalOutstanding,TotalOverdue,NoOfContracts,Total_Exposure,WorstCurrentPaymentDelay,"
				+ "Worst_PaymentDelay_Last24Months,Worst_Status_Last24Months,Nof_Records,NoOf_Cheque_Return_Last3,Nof_DDES_Return_Last3Months,"
				+ "Nof_Cheque_Return_Last6,DPD30_Last6Months,(select max(ExternalWriteOffCheck) ExternalWriteOffCheck "
				+ "from ((select convert(int,isNULL(ExternalWriteOffCheck,0)) ExternalWriteOffCheck  from ng_dpl_cust_extexpo_CardDetails with(nolock) "
				+ "where Wi_Name  = '"+Wi_Name+"' and ProviderNo!='B01'  "
				+ "union all select convert(int,isNULL(ExternalWriteOffCheck,0)) ExternalWriteOffCheck "
				+ "from ng_dpl_cust_extexpo_LoanDetails where Wi_Name  = '"+Wi_Name+"' and ProviderNo!='B01' "
				+ "union all select convert(int,isNULL(ExternalWriteOffCheck,0)) ExternalWriteOffCheck from ng_dpl_cust_extexpo_AccountDetails "
				+ "where Wi_Name = '"+Wi_Name+"' and ProviderNo!='B01')) as ExternalWriteOffCheck) as 'ExternalWriteOffCheck' ,(select count(*) "
				+ "from (select DisputeAlert from ng_dpl_cust_extexpo_LoanDetails with(nolock) where Wi_Name = '"+Wi_Name+"' and DisputeAlert='1' "
				
				+ "union select DisputeAlert from ng_dpl_cust_extexpo_CardDetails with(nolock) where Wi_Name = '"+Wi_Name+"' and DisputeAlert='1') "
				+ "as tempTable) as 'DisputeAlert'  from ng_dpl_cust_extexpo_Derived with (nolock) where Wi_Name  = '"+Wi_Name+"' and Request_type= 'ExternalExposure'";
		
		logger.debug("ExternalBureauData sQuery" + sQuery+ "");
		String AecbHistQuery = "select isnull(max(AECBHistMonthCnt),0) as AECBHistMonthCnt from ( select MAX(cast(isnull(AECBHistMonthCnt,'0') as int)) as AECBHistMonthCnt  "
						+ "from ng_dpl_cust_extexpo_CardDetails with (nolock) where  Wi_Name  = '"+ Wi_Name + "' and cardtype not in ( '85','99','Communication Services',"
						+ "'TelCo-Mobile Prepaid','101','Current/Saving Account with negative Balance','58','Overdraft') and custroletype not in ('Co-Contract Holder','Guarantor') "
						
						+ "union all select Max(cast(isnull(AECBHistMonthCnt,'0') as int)) as AECBHistMonthCnt from ng_dpl_cust_extexpo_LoanDetails with (nolock) "
						+ "where Wi_Name  = '"+ Wi_Name + "' and loantype not in ('85','99','Communication Services','TelCo-Mobile Prepaid','101',"
						+ "'Current/Saving Account with negative Balance','58','Overdraft') and custroletype not in ('Co-Contract Holder','Guarantor')) as ext_expo";
		
		String add_xml_str = "";
		try {
			
			List<Map<String,String>> OutputXML = getDataFromDBMap(sQuery, cabinetName, sessionID, jtsIP, jtsPort);
			logger.debug("ExternalBureauData list size" + OutputXML.size()+ "");
				
			List<Map<String,String>> AecbHistMap = getDataFromDBMap(AecbHistQuery, cabinetName, sessionID, jtsIP, jtsPort);
			logger.debug("ExternalBureauData list size" + AecbHistMap.size()+ "");
			
			if (OutputXML.size() == 0)
			{
				String aecb_score="";
				String range ="";
				String refNo ="";
				String Worst_Status_Last24Months="";
				String query = "select top 1 ReferenceNo, AECB_Score,Range,Worst_Status_Last24Months from ng_dpl_cust_extexpo_Derived with(nolock) where Wi_Name ='"+Wi_Name
						+"' and Request_Type='ExternalExposure' ORDER BY enquiryDate desc"  ;
				try {
					List<Map<String,String>> OutputXML_ref = getDataFromDBMap(query, cabinetName, sessionID, jtsIP, jtsPort);
					logger.debug("Inside externalBureauData OutputXML_ref: "+ query);
					logger.debug("Inside externalBureauData OutputXML_ref: "+ OutputXML_ref);
					if(OutputXML_ref.size()>0)
					{
						refNo=OutputXML_ref.get(0).get("ReferenceNo");
						aecb_score=OutputXML_ref.get(0).get("AECB_Score");
						range=OutputXML_ref.get(0).get("Range");
						Worst_Status_Last24Months=OutputXML_ref.get(0).get("Worst_Status_Last24Months");
					}				
				}
				catch(Exception e)
				{
					logger.debug(" Exception occurred in externalBureauData Query"+ query);
					logger.debug(" Exception occurred in externalBureauData()"+ e.getMessage());
				}
				
				logger.debug( "aecb_score :"+aecb_score+" range :: "+range+" refNo:: "+refNo);
				
				add_xml_str +="<ExternalBureau>" + "";
				add_xml_str +="<applicant_id>" + Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("WIName")) + "</applicant_id>" + "";
				add_xml_str +="<bureauone_ref_no>"+refNo+"</bureauone_ref_no>" + "";
				add_xml_str +="<full_name>" + Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("FirstName")) +" "+ Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("LastName")) + "</full_name>" + ""; //, MiddleName, 
				add_xml_str +="<total_out_bal></total_out_bal>" + "";
				add_xml_str +="<total_overdue></total_overdue>" + "";
				add_xml_str +="<no_default_contract></no_default_contract>" + "";
				add_xml_str +="<total_exposure></total_exposure>" + "";
				add_xml_str +="<worst_curr_pay></worst_curr_pay>" + "";
				add_xml_str +="<worst_curr_pay_24></worst_curr_pay_24>" + "";
				add_xml_str +="<worst_curr_pay_24></worst_curr_pay_24>" + "";
				add_xml_str +="<no_of_rec></no_of_rec>" + "";
				add_xml_str +="<cheque_return_3mon></cheque_return_3mon>" + "";
				add_xml_str +="<dds_return_3mon></dds_return_3mon>" + "";
				add_xml_str +="<no_months_aecb_history>" + AecbHistMap.get(0).get("AECBHistMonthCnt") + "</no_months_aecb_history>" + "";
				add_xml_str +="<aecb_score>"+aecb_score+"</aecb_score>" + "";
				add_xml_str +="<range>"+range+"</range>" + "";
				add_xml_str +="<company_flag>N</company_flag></ExternalBureau>" + "";

				logger.debug("dectech External : " + add_xml_str);
				return add_xml_str;
			} 
			else {
				for (Map<String,String> map : OutputXML){
					//String CifId = validateValue(map.get("CifId"));
					String fullnm = Digital_PL_CommomMethod.validateValue(map.get("fullnm"));
					String TotalOutstanding = Digital_PL_CommomMethod.validateValue(map.get("TotalOutstanding"));
					String TotalOverdue = Digital_PL_CommomMethod.validateValue(map.get("TotalOverdue"));
					String NoOfContracts = Digital_PL_CommomMethod.validateValue(map.get("NoOfContracts"));
					String Total_Exposure = Digital_PL_CommomMethod.validateValue(map.get("Total_Exposure"));
					String WorstCurrentPaymentDelay = Digital_PL_CommomMethod.validateValue(map.get("WorstCurrentPaymentDelay"));
					String Worst_PaymentDelay_Last24Months = Digital_PL_CommomMethod.validateValue(map.get("Worst_PaymentDelay_Last24Months"));
					String Worst_Status_Last24Months = Digital_PL_CommomMethod.validateValue(map.get("Worst_Status_Last24Months"));
					String Nof_Records = Digital_PL_CommomMethod.validateValue(map.get("Nof_Records"));
					String NoOf_Cheque_Return_Last3 = Digital_PL_CommomMethod.validateValue(map.get("NoOf_Cheque_Return_Last3"));					
					String Nof_DDES_Return_Last3Months = Digital_PL_CommomMethod.validateValue(map.get("Nof_DDES_Return_Last3Months"));
					String Nof_Cheque_Return_Last6 = Digital_PL_CommomMethod.validateValue(map.get("Nof_Cheque_Return_Last6"));
					String DPD30_Last6Months = Digital_PL_CommomMethod.validateValue(map.get("DPD30_Last6Months"));
					String dispute_alert= Digital_PL_CommomMethod.validateValue(map.get("tempTable"));
					
					String aecb_score=""; 
					String range =""; 
					String refNo ="";
					String EnquiryDate="";
					
					Worst_Status=Digital_PL_CommomMethod.validateValue(map.get("Worst_Status_Last24Months"));
					logger.debug( "aecb_score :"+aecb_score+" range :: "+range+" refNo:: "+refNo);
					 
					AECB_DDS_Return=Digital_PL_CommomMethod.validateValue(map.get("NoOf_Cheque_Return_Last3"));;
					
					if (!dispute_alert.equals("")) {
						try {
							if (Integer.parseInt(dispute_alert) > 0) {
								dispute_alert = "Y";
							} else {
								dispute_alert = "N";
							}
						} catch (NumberFormatException e) {
							dispute_alert = "N";
							logger.error( "NumberFormatException : "+e.getMessage());
						}
					} else {
						dispute_alert = "N";
					}
					
					//String Company_flag = "N";
					String Ref_query = "select ReferenceNo, AECB_Score,Range, EnquiryDate from ng_dpl_cust_extexpo_Derived with(nolock) where Wi_Name ='"+Wi_Name+"' and Request_Type='ExternalExposure'";
					try {
						List<Map<String,String>> OutputXML_ref = getDataFromDBMap(Ref_query, cabinetName, sessionID, jtsIP, jtsPort);
						if(OutputXML_ref.size()>0)
						{
							refNo=OutputXML_ref.get(0).get("ReferenceNo");
							aecb_score=OutputXML_ref.get(0).get("AECB_Score");
							range=OutputXML_ref.get(0).get("Range");
							EnquiryDate=OutputXML_ref.get(0).get("EnquiryDate");
							System.out.println("EnquiryDate:-"+EnquiryDate);
							EnquiryDate=Digital_PL_CommomMethod.getEnquiryDate(EnquiryDate);
						}				
					}
					catch(Exception e)
					{
						logger.debug(" Exception occurred in externalBureauData Query"+ Ref_query);
						logger.debug(" Exception occurred in externalBureauData()"+ e.getMessage());
					}
					
					add_xml_str +="<ExternalBureau>" + "";
					add_xml_str +="<applicant_id>" + Digital_PL_CommomMethod.validateValue(applicantDetails_Map.get("WIName"))+ "</applicant_id>" + "";
					add_xml_str +="<bureauone_ref_no>"+refNo+"</bureauone_ref_no>" + "";
					add_xml_str +="<full_name>" + fullnm+ "</full_name>" + "";
					add_xml_str +="<total_out_bal>"+ TotalOutstanding + "</total_out_bal>" + "";
					add_xml_str +="<total_overdue>"+ TotalOverdue + "</total_overdue>" + "";
					add_xml_str +="<no_default_contract>"+ NoOfContracts + "</no_default_contract>" + "";
					add_xml_str +="<total_exposure>"+ Total_Exposure + "</total_exposure>" + "";
					add_xml_str +="<worst_curr_pay>"+ WorstCurrentPaymentDelay + "</worst_curr_pay>" + "";
					add_xml_str +="<worst_curr_pay_24>"+ Worst_PaymentDelay_Last24Months+ "</worst_curr_pay_24>" + "";
					add_xml_str +="<worst_status_24>"+ Worst_Status_Last24Months + "</worst_status_24>" + "";
					add_xml_str +="<no_of_rec>" + Nof_Records+ "</no_of_rec>" + "";
					add_xml_str +="<cheque_return_3mon>"+ NoOf_Cheque_Return_Last3+ "</cheque_return_3mon>" + "";
					add_xml_str +="<dds_return_3mon>"+ Nof_DDES_Return_Last3Months+ "</dds_return_3mon>" + "";
					add_xml_str +="<cheque_return_6mon>"+ Nof_Cheque_Return_Last6 + "</cheque_return_6mon>" + "";
					add_xml_str +="<dds_return_6mon>"+ DPD30_Last6Months + "</dds_return_6mon>" + "";
					add_xml_str +="<no_months_aecb_history>"+ AecbHistMap.get(0).get("AECBHistMonthCnt")+ "</no_months_aecb_history>" + "";
					add_xml_str +="<aecb_score>"+aecb_score+"</aecb_score>" + "";
					add_xml_str +="<range>"+range+"</range>" + "";
					add_xml_str +="<AECB_Enquiry_date>"+EnquiryDate+"</AECB_Enquiry_date>" + ""; 
					add_xml_str +="<company_flag>N</company_flag>" + "";
					add_xml_str +="<dispute_alert>"+dispute_alert+"</dispute_alert></ExternalBureau>";
					AECB_Hist_Month_Count=AecbHistMap.get(0).get("AECBHistMonthCnt");
				}
				logger.debug("RLOSCommon"+"Internal liab tag Cration: " + add_xml_str);
				return add_xml_str;
			}
		}

		catch (Exception e) {
			logger.debug("DECTECH Exception occurred in externalBureauData()"+ e.getMessage() + " Error: "+ e.getMessage());
			return null;
		}
	}
	
	private static  String sInputXmlExternalBouncedCheques(String wiName, String cabinetName, String sessionID, String jtsIP, String jtsPort) {
		
		logger.debug("RLOSCommon java file"+"inside ExternalBouncedCheques : ");
		
		String sQuery = "SELECT CifId,ChqType,number,amount,reasoncode,returndate,providerno FROM ng_dpl_cust_extexpo_ChequeDetails  with (nolock) "
				+ "where Wi_Name = '" + wiName + "' and Request_Type = 'ExternalExposure'";
		
		logger.debug("ExternalBouncedCheques sQuery" + sQuery+ "");
		String add_xml_str = "";
			
		List<Map<String,String>> OutputXML = getDataFromDBMap(sQuery, cabinetName, sessionID, jtsIP, jtsPort);
		logger.debug("ExternalBouncedCheques list size" + OutputXML.size()+ "");

		for (Map<String,String> map : OutputXML) {
			
			add_xml_str +="<ExternalBouncedCheques><applicant_id>" + wiName + "</applicant_id>"+ "";
			add_xml_str +="<bounced_cheque>" + Digital_PL_CommomMethod.validateValue(map.get("ChqType")) + "</bounced_cheque>"+ "";
			add_xml_str +="<cheque_no>" + Digital_PL_CommomMethod.validateValue(map.get("number")) + "</cheque_no>"+ "";
			add_xml_str +="<amount>" + Digital_PL_CommomMethod.validateValue(map.get("amount")) + "</amount>"+ "";
			add_xml_str +="<reason>" + Digital_PL_CommomMethod.validateValue(map.get("reasoncode")) + "</reason>"+ "";
			add_xml_str +="<return_date>" + Digital_PL_CommomMethod.validateValue(map.get("returndate")) + "</return_date>"+ "";
			add_xml_str +="<provider_no>" + Digital_PL_CommomMethod.validateValue(map.get("providerno")) + "</provider_no><company_flag>N</company_flag></ExternalBouncedCheques>"; // to
		
		}
		logger.debug("RLOSCommon"+ "Internal liab tag Cration: "+ add_xml_str);
		return add_xml_str;
	}
	
	
	private static  String sInputXmlExternalBureauEmploymentDetail(String wiName, String cabinetName, String sessionID, String jtsIP, String jtsPort) {
		
		logger.debug("RLOSCommon java file"+"inside ExternalBureauEmploymentDetails : ");
		
		String add_xml_str = "";
			
			add_xml_str +="<ExternalBureauEmploymentDetails><applicant_id>" + wiName + "</applicant_id>"+ "";
			add_xml_str +="<Employment_Name></Employment_Name>"+ "";
			add_xml_str +="<Employment_Type></Employment_Type>"+ "";
			add_xml_str +="<Gross_Annual_Income></Gross_Annual_Income>"+ "";
			add_xml_str +="<Date_of_Employment></Date_of_Employment>"+ "";
			add_xml_str +="<Date_of_termination></Date_of_termination>"+ "";
			add_xml_str +="<Provider_no></Provider_no>"+ "";
			add_xml_str +="<Date_of_last_update></Date_of_last_update>"+ "";
			add_xml_str +="<Actual_Flag></Actual_Flag>"+ "</ExternalBureauEmploymentDetails>";
			
		logger.debug("RLOSCommon"+ "Internal liab tag Cration: "+ add_xml_str);
		return add_xml_str;
	}
	
	private static String sInputXmlExternalUtilization(String wiName, String cabinetName, String sessionID, String jtsIP, String jtsPort)
	{
		
		String sQuery = "select CardEmbossNum, Utilizations24Months as UtilizationsMonths from ng_dpl_cust_extexpo_CardDetails where Wi_Name='" + wiName + "' and (History is not null) ";		
		
		String add_xml_str = "";

		try {
			String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(sQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false));
			logger.debug("extTabDataIPXML: " + extTabDataIPXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
			logger.debug("extTabDataOPXML: " + extTabDataOPXML);

			XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
			int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));

			if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0) {
				String xmlDataExtTab = xmlParserData.getNextValueOf("Record");
				xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

				NGXmlList objWorkList = xmlParserData.createList("Records", "Record");
				String Utilizations24Months = "";
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
					String agreementID = Digital_PL_CommomMethod.validateValue(objWorkList.getVal("CardEmbossNum"));
					String UtilizationTag = Digital_PL_CommomMethod.validateValue(objWorkList.getVal("UtilizationsMonths"));

					UtilizationTag = UtilizationTag.replaceAll("Utilizations24Months", "Month_Utilization");
					Utilizations24Months += UtilizationTag.replaceAll("<Month_Utilization>", "<Month_Utilization><CB_application_id>" + agreementID + "</CB_application_id>");
				}
				
				if (!Utilizations24Months.equals(""))
					add_xml_str = add_xml_str + "<Utilization24months>" + Utilizations24Months + "</Utilization24months>";
			}
		} catch (Exception e) {
			logger.debug("Utilization24months Exception : " + e.getMessage());
			e.printStackTrace();
		}
		logger.debug("Utilization24months : " + add_xml_str);
		return add_xml_str;
	}

	private static String sInputXmlExternalHistory(String wiName, String cabinetName, String sessionID, String jtsIP, String jtsPort) {
		
		String sQuery = "select CardEmbossNum, history as extHistory from ng_dpl_cust_extexpo_CardDetails where Wi_Name='" + wiName + "' and (History is not null) "
				+ "union all select AgreementId, history as extHistory from ng_dpl_cust_extexpo_LoanDetails where Wi_Name='" + wiName + "' and (History is not null)";

		String add_xml_str = "";
		try {
			String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(sQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false));
			logger.debug("extTabDataIPXML: " + extTabDataIPXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
			logger.debug("extTabDataOPXML: " + extTabDataOPXML);

			XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
			int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));

			if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0) {
				String xmlDataExtTab = xmlParserData.getNextValueOf("Record");
				xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

				NGXmlList objWorkList = xmlParserData.createList("Records", "Record");
				String history = "";
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
					String agreementID = Digital_PL_CommomMethod.validateValue(objWorkList.getVal("CardEmbossNum"));
					String HistoryTag = Digital_PL_CommomMethod.validateValue(objWorkList.getVal("extHistory"));
					HistoryTag = HistoryTag.replaceAll("Key", "monthyear");

					history += HistoryTag.replaceAll("<History>", "<History><CB_application_id>" + agreementID + "</CB_application_id>");
				}
				
				if (!history.equals(""))
					add_xml_str = add_xml_str + "<History_24months>" + history + "</History_24months>";
			}
		} catch (Exception e) {
			logger.debug("History_24months Exception : " + e.getMessage());
			e.printStackTrace();
		}
		logger.debug("History_24months : " + add_xml_str);
		return add_xml_str;
	}

	private static  String sInputXmlExternalBureauIndividualProducts(String wiName, String cabinetName, String sessionID, String jtsIP, String jtsPort) {
		
		String sQuery = "select CifId,AgreementId,LoanType,ProviderNo,LoanStat,CustRoleType,LoanApprovedDate,LoanMaturityDate,OutstandingAmt,TotalAmt,PaymentsAmt,"
				+ "TotalNoOfInstalments,RemainingInstalments,WriteoffStat,WriteoffStatDt,CreditLimit,OverdueAmt,NofDaysPmtDelay,MonthsOnBook,lastrepmtdt,IsCurrent,"
				+ "CurUtilRate,DPD30_Last6Months,DPD60_Last12Months,AECBHistMonthCnt,DPD5_Last3Months,'' as qc_Amnt,'' as Qc_emi,'' as Cac_indicator,Take_Over_Indicator,"
				+ "Consider_For_Obligations, case when IsDuplicate= '1' then 'Y' else 'N' end AS IsDuplicate,avg_utilization,DPD5_Last12Months,DPD60Plus_Last12Months,MaximumOverDueAmount,"
				+ "Pmtfreq, MaxOverDueAmountDate from ng_dpl_cust_extexpo_LoanDetails with (nolock) where Wi_Name= '"+ wiName + "'  and LoanStat != 'Pipeline' "
				
		+ "union select CifId,CardEmbossNum,CardType,ProviderNo,CardStatus,CustRoleType,StartDate,ClosedDate,CurrentBalance,'' as col6,"
		+ "PaymentsAmount,NoOfInstallments,'' as col5,WriteoffStat,WriteoffStatDt,CashLimit as CreditLimit ,OverdueAmount,NofDaysPmtDelay,MonthsOnBook,lastrepmtdt,IsCurrent,CurUtilRate,"
		+ "DPD30_Last6Months,DPD60_Last12Months,AECBHistMonthCnt,DPD5_Last3Months,qc_amt,qc_emi,CAC_Indicator,Take_Over_Indicator,Consider_For_Obligations,case when "
		+ "IsDuplicate= '1' then 'Y' else 'N' end AS IsDuplicate,avg_utilization,DPD5_Last12Months,DPD60Plus_Last12Months,MaximumOverDueAmount,Pmtfreq, MaxOverDueAmountDate from "
		+ "ng_dpl_cust_extexpo_CardDetails with (nolock) where Wi_Name = '" + wiName+ "' and cardstatus != 'Pipeline'   "
		
		+ "union select CifId,AcctId,AcctType,ProviderNo,AcctStat,CustRoleType,StartDate,ClosedDate,OutStandingBalance,TotalAmount,PaymentsAmount,'','',"
		+ "WriteoffStat,WriteoffStatDt,CreditLimit,OverdueAmount,"
		+ "NofDaysPmtDelay,MonthsOnBook,'',IsCurrent,CurUtilRate,DPD30_Last6Months,DPD60_Last12Months,AECBHistMonthCnt,DPD5_Last3Months,'','','','',"
		+ "isnull(Consider_For_Obligations,'true'),case when IsDuplicate= '1' then 'Y' else 'N' end AS IsDuplicate,'',DPD5_Last12Months,DPD60Plus_Last12Months,"
		+ "MaximumOverDueAmount,Pmtfreq, MaxOverDueAmountDate from ng_dpl_cust_extexpo_AccountDetails with (nolock)  where Wi_Name  =  '"+wiName+"' "
		
		+ "union select CifId,ServiceID,ServiceType,ProviderNo,ServiceStat,CustRoleType,SubscriptionDt,SvcExpDt,OverDueAmount,'','','','',WriteoffStat,WriteoffStatDt,'',OverDueAmount,"
		+ "NofDaysPmtDelay,MonthsOnBook,lastrepmtdt,IsCurrent,CurUtilRate,DPD30_Last6Months,'',AECBHistMonthCnt,DPD5_Last3Months,'','','','',isnull(Consider_For_Obligations,'true')"
		+ ",case when IsDuplicate= '1' then 'Y' else 'N' end AS IsDuplicate,'',DPD5_Last12Months,DPD60Plus_Last12Months,MaximumOverDueAmount,'',MaxOverDueAmountDate from ng_dpl_cust_extexpo_ServicesDetails with (nolock)  "
		+ "where ServiceStat='Active' and wi_name  =  '"+wiName+"'";
		
				
		String add_xml_str = "";
		List<Map<String,String>> OutputXML = getDataFromDBMap(sQuery, cabinetName, sessionID, jtsIP, jtsPort);
		logger.info("ExternalBureauIndividualProducts list size"+ OutputXML.size()+ "");
			
		for (Map<String,String> map : OutputXML){
			
			String ContractType = Digital_PL_CommomMethod.validateValue(map.get("LoanType"));
			String AgreementId = Digital_PL_CommomMethod.validateValue(map.get("AgreementId"));
			String phase = Digital_PL_CommomMethod.validateValue(map.get("LoanStat"));
			String CustRoleType = Digital_PL_CommomMethod.validateValue(map.get("CustRoleType"));
			String start_date = Digital_PL_CommomMethod.validateValue(map.get("LoanApprovedDate"));
			String close_date = Digital_PL_CommomMethod.validateValue(map.get("LoanMaturityDate"));
			String OutStanding_Balance = Digital_PL_CommomMethod.validateValue(map.get("OutstandingAmt"));
			String TotalAmt = Digital_PL_CommomMethod.validateValue(map.get("TotalAmt"));
			String PaymentsAmt = Digital_PL_CommomMethod.validateValue(map.get("PaymentsAmt"));
			String TotalNoOfInstalments = Digital_PL_CommomMethod.validateValue(map.get("TotalNoOfInstalments"));
			String RemainingInstalments = Digital_PL_CommomMethod.validateValue(map.get("RemainingInstalments"));
			String WorstStatus = Digital_PL_CommomMethod.validateValue(map.get("WriteoffStat"));
			String WorstStatusDate = Digital_PL_CommomMethod.validateValue(map.get("WriteoffStatDt"));
			String CreditLimit = Digital_PL_CommomMethod.validateValue(map.get("CreditLimit"));
			String OverdueAmt = Digital_PL_CommomMethod.validateValue(map.get("OverdueAmt"));
			String NofDaysPmtDelay = Digital_PL_CommomMethod.validateValue(map.get("NofDaysPmtDelay"));
			String MonthsOnBook = Digital_PL_CommomMethod.validateValue(map.get("MonthsOnBook"));
			String last_repayment_date = Digital_PL_CommomMethod.validateValue(map.get("lastrepmtdt"));
			String AECBHistMonthCnt = Digital_PL_CommomMethod.validateValue(map.get("AECBHistMonthCnt"));
			String DPD30Last6Months = Digital_PL_CommomMethod.validateValue(map.get("DPD30_Last6Months"));
			String currently_current = Digital_PL_CommomMethod.validateValue(map.get("IsCurrent"));
			String current_utilization = Digital_PL_CommomMethod.validateValue(map.get("CurUtilRate"));
			String delinquent_in_last_3months = Digital_PL_CommomMethod.validateValue(map.get("DPD5_Last3Months"));
			String CAC_Indicator = Digital_PL_CommomMethod.validateValue(map.get("Cac_indicator"));
			String TakeOverIndicator = Digital_PL_CommomMethod.validateValue(map.get("Take_Over_Indicator"));
			String consider_for_obligation = Digital_PL_CommomMethod.validateValue(map.get("Consider_For_Obligations"));
			String Duplicate_flag=Digital_PL_CommomMethod.validateValue(map.get("IsDuplicate"));
			String DPD60plus_last12month=Digital_PL_CommomMethod.validateValue(map.get("DPD60Plus_Last12Months"));
			String DPD5_last12month=Digital_PL_CommomMethod.validateValue(map.get("DPD5_Last12Months"));
			String MaximumOverDueAmount = Digital_PL_CommomMethod.validateValue(map.get("MaximumOverDueAmount"));  
			String Pmtfreq = Digital_PL_CommomMethod.validateValue(map.get("Pmtfreq"));
			String MaxOverDueAmountDate = Digital_PL_CommomMethod.validateValue(map.get("MaxOverDueAmountDate"));
			
			if (!ContractType.equals("")) {
				try {
					String cardquery = "select code from ng_master_contract_type with (nolock) where description='"+ ContractType + "'";
					logger.info("ExternalBureauIndividualProducts sQuery"+ cardquery+ "");
					Map<String, String> cardqueryXML = getDataFromDB(cardquery, cabinetName, sessionID, jtsIP, jtsPort, "code");
					ContractType = cardqueryXML.get("code");
					logger.info("ExternalBureauIndividualProducts ContractType"+ ContractType+ "ContractType");
				} catch (Exception e) {
					logger.info("ExternalBureauIndividualProducts ContractType Exception"+ e+ "Exception");
				}
			}
			
			phase = phase.startsWith("A") ? "A" : "C";
			
			if (!CustRoleType.equals("")) {
				
				String sQueryCustRoleType = "select code from ng_master_role_of_customer with(nolock) where Description='"+CustRoleType+"'";
				logger.info("CustRoleType"+sQueryCustRoleType);
				
				Map<String, String> cardqueryXML = getDataFromDB(sQueryCustRoleType, cabinetName, sessionID, jtsIP, jtsPort, "code");
				try {
					if (cardqueryXML != null && cardqueryXML.size() > 0 && cardqueryXML.get("code") != null) {
						CustRoleType = cardqueryXML.get("code");
					}
				}
				catch(Exception e){
					logger.info("Exception occured at sQueryCombinedLimit for"+sQueryCustRoleType);
				}	
			}

			CAC_Indicator = "true".equalsIgnoreCase(CAC_Indicator) ? "Y" : "N";
			
			TakeOverIndicator = "true".equalsIgnoreCase(TakeOverIndicator) ? "Y" : "N";
			
			consider_for_obligation = "true".equalsIgnoreCase(consider_for_obligation) ? "Y" : "N";
			
			
			add_xml_str +="<ExternalBureauIndividualProducts><applicant_id>" + wiName + "</applicant_id>"+ "";
			add_xml_str +="<external_bureau_individual_products_id>" + AgreementId + "</external_bureau_individual_products_id>"+ "";
			add_xml_str +="<contract_type>"+ContractType+"</contract_type>"+ "";
			add_xml_str +="<provider_no>" + map.get("ProviderNo") + "</provider_no>"+ "";
			add_xml_str +="<phase>"+phase+"</phase>"+ ""; //Default
			add_xml_str +="<role_of_customer>" + CustRoleType + "</role_of_customer>"+ "";
			add_xml_str +="<start_date>" + start_date + "</start_date>"+ "";
			add_xml_str +="<close_date>" + close_date + "</close_date>"+ "";
			add_xml_str +="<outstanding_balance>" + OutStanding_Balance + "</outstanding_balance>"+ "";
			add_xml_str +="<total_amount>" + TotalAmt + "</total_amount>"+ "";
			add_xml_str +="<payments_amount>" + PaymentsAmt + "</payments_amount>"+ "";
			add_xml_str +="<total_no_of_instalments>" + TotalNoOfInstalments + "</total_no_of_instalments>"+ "";
			add_xml_str +="<no_of_remaining_instalments>" + RemainingInstalments + "</no_of_remaining_instalments>"+ "";
			add_xml_str +="<worst_status>" + WorstStatus + "</worst_status>"+ "";
			add_xml_str +="<worst_status_date>" + WorstStatusDate + "</worst_status_date>"+ "";
			add_xml_str +="<credit_limit>" + CreditLimit + "</credit_limit>"+ "";
			add_xml_str +="<overdue_amount>" + OverdueAmt + "</overdue_amount>"+ "";
			add_xml_str +="<no_of_days_payment_delay>" + NofDaysPmtDelay + "</no_of_days_payment_delay>"+ "";
			add_xml_str +="<mob>" + MonthsOnBook + "</mob>"+ "";
			add_xml_str +="<last_repayment_date>" + last_repayment_date + "</last_repayment_date>"+ "";

			if (currently_current != null && "1".equalsIgnoreCase(currently_current)) {
				add_xml_str +="<currently_current>Y</currently_current>"+ "";
			} else {
				add_xml_str +="<currently_current>N</currently_current>"+ "";
			}
		
			add_xml_str +="<current_utilization>" + current_utilization + "</current_utilization>"+ "";
			add_xml_str +="<dpd_5_in_last_12_mon>" + DPD5_last12month + "</dpd_5_in_last_12_mon>"+ "";
			add_xml_str +="<dpd_30_last_6_mon>" + DPD30Last6Months + "</dpd_30_last_6_mon>"+ "";
			add_xml_str +="<dpd_60p_in_last_12_mon>" + DPD60plus_last12month + "</dpd_60p_in_last_12_mon>"+ "";
			add_xml_str +="<no_months_aecb_history>" + AECBHistMonthCnt + "</no_months_aecb_history>"+ "";
			//change the maximun overdue account as discuss with nashima added by rubi
			//add_xml_str +="<maximum_overdue_amount>" + MaximumOverDueAmount + "</maximum_overdue_amount>"+ "";// added by deppanshu
			add_xml_str +="<maximum_overdue_amount>" + OverdueAmt + "</maximum_overdue_amount>"+ "";
			add_xml_str +="<delinquent_in_last_3months>" + delinquent_in_last_3months + "</delinquent_in_last_3months>"+ "";
			add_xml_str +="<company_flag>N</company_flag>"+ "";
			add_xml_str +="<consider_for_obligation>Y</consider_for_obligation>"+ "";
			add_xml_str +="<duplicate_flag>"+Duplicate_flag+"</duplicate_flag>"+ "";
			add_xml_str +="<payment_frequency>"+Pmtfreq+"</payment_frequency>"+ "";
			add_xml_str +="<maximum_overdue_date>"+Digital_PL_CommomMethod.getMaximumOverdueDate(MaxOverDueAmountDate)+"</maximum_overdue_date>"+ "";
			add_xml_str +="</ExternalBureauIndividualProducts>";
			
			logger.info("Internal liab tag Cration: "	+ add_xml_str);
		}
		
		try{
			String Expense1="0",Expense2="0",Expense3="0",Expense4="0";
			//String Expense_Query = "select Lifestyle_Expenses from NG_DPL_IncomeExpense with(nolock) where WI_Name ='"+wiName+"'";
			String Expense_Query = "select i.Lifestyle_Expenses,i.Bills,i.Others,e.RentOrMortgage from NG_DPL_IncomeExpense as i join NG_DPL_EXTTABLE e with(nolock) on e.WINAME  = i.WI_Name where i.WI_Name ='"+wiName+"'"; 
			List<Map<String,String>> OutputXML_Expense = getDataFromDBMap(Expense_Query, cabinetName, sessionID, jtsIP, jtsPort);
			
			logger.info("OutputXML_Expense output xml: "+ OutputXML_Expense+ "");
			
			Expense1=OutputXML_Expense.get(0).get("Lifestyle_Expenses");
			Expense2=OutputXML_Expense.get(0).get("Bills");
			Expense3=OutputXML_Expense.get(0).get("RentOrMortgage");
			Expense4=OutputXML_Expense.get(0).get("Others");
//			
			String Lifestyle = "<ExternalBureauIndividualProducts>"
							      +"<applicant_id>"+wiName+"</applicant_id>"
							      +"<external_bureau_individual_products_id>"+wiName+"</external_bureau_individual_products_id>"
							      +"<contract_type>L1</contract_type>"
							      +"<phase>A</phase>"
							      +"<payments_amount>"+Expense1+"</payments_amount>"
							      +"</ExternalBureauIndividualProducts>";
			
			Lifestyle += "<ExternalBureauIndividualProducts>"
					      +"<applicant_id>"+wiName+"</applicant_id>"
					      +"<external_bureau_individual_products_id>"+wiName+"</external_bureau_individual_products_id>"
					      +"<contract_type>SerExp</contract_type>"
					      +"<phase>A</phase>"
					      +"<payments_amount>"+Expense2+"</payments_amount>"
					      +"</ExternalBureauIndividualProducts>";
			
			Lifestyle += "<ExternalBureauIndividualProducts>"
					      +"<applicant_id>"+wiName+"</applicant_id>"
					      +"<external_bureau_individual_products_id>"+wiName+"</external_bureau_individual_products_id>"
					      +"<contract_type>AdLnTake</contract_type>"
					      +"<phase>A</phase>"
					      +"<payments_amount>"+Expense3+"</payments_amount>"
					      +"</ExternalBureauIndividualProducts>";
			
			Lifestyle += "<ExternalBureauIndividualProducts>"
					      +"<applicant_id>"+wiName+"</applicant_id>"
					      +"<external_bureau_individual_products_id>"+wiName+"</external_bureau_individual_products_id>"
					      +"<contract_type>AnOtheExp</contract_type>"
					      +"<phase>A</phase>"
					      +"<payments_amount>"+Expense4+"</payments_amount>"
					      +"</ExternalBureauIndividualProducts>";
			
			logger.info("ExternalBureauIndividualProducts: "	+ Lifestyle);
			add_xml_str +=Lifestyle;
			logger.info("ExternalBureauIndividualProducts: "	+ add_xml_str);
		}
		catch(Exception e){
			logger.info("ExternalBureauIndividualProducts lifestyle Exception"+ e.getMessage());
			logger.info("ExternalBureauIndividualProducts lifestyle Exception"+ e);
		}
		
		logger.info("Internal liab tag Cration: "	+ add_xml_str);
		return add_xml_str;
	}
	
	private static  String sInputXmlExternalBureauPipelineProducts(String wiName, String cabinetName, String sessionID, String jtsIP, String jtsPort) {
		
		logger.debug("inside ExternalBureauPipelineProducts : ");
		
		String sQuery = "select CifId, AgreementId,ProviderNo,LoanType,LoanDesc,CustRoleType,Datelastupdated,TotalAmt,TotalNoOfInstalments,CreditLimit,'' as col1,NoOfDaysInPipeline,"
				+ "isnull(Consider_For_Obligations,'true') as 'Consider_For_Obligations', case when IsDuplicate= '1' then 'Y' else 'N' end as 'IsDuplicate' from ng_dpl_cust_extexpo_LoanDetails with (nolock) "
				+ "where Wi_Name  =  '" + wiName + "' and LoanStat = 'Pipeline'"
				+ "union select CifId, CardEmbossNum,ProviderNo,CardType,CardTypeDesc, CustRoleType,LastUpdateDate,'' as col2,NoOfInstallments, '' as col3, TotalAmount, "
				+ "NoOfDaysInPipeLine,isnull(Consider_For_Obligations,'true') as 'Consider_For_Obligations',case when IsDuplicate= '1' then 'Y' else 'N' end as 'IsDuplicate' from ng_dpl_cust_extexpo_CardDetails "
				+ "with (nolock) where Wi_Name  =  '" + wiName + "' and cardstatus = 'Pipeline'";
		
		logger.debug("ExternalBureauPipelineProducts sQuery" + sQuery+"");
		
		String add_xml_str = "";
		List<Map<String,String>> maps= getDataFromDBMap(sQuery, cabinetName, sessionID, jtsIP, jtsPort);
		logger.info("ExternalBureauPipelineProducts list size"+ maps.size()+ "");
			
		for (Map<String,String> map : maps) {

			String contractType = Digital_PL_CommomMethod.validateValue(map.get("LoanType"));
			String role = Digital_PL_CommomMethod.validateValue(map.get("CustRoleType"));
			
			if (!contractType.equals("")) {
				try {
					String cardquery = "select code from ng_master_contract_type with (nolock) where description='"+ contractType + "'";
					logger.info("ExternalBureauIndividualProducts sQuery"+ cardquery+ "");
					Map<String, String> cardqueryXML = getDataFromDB(cardquery, cabinetName, sessionID, jtsIP, jtsPort, "code");
					contractType = cardqueryXML.get("code");
					logger.info("ExternalBureauIndividualProducts ContractType"+ contractType+ "ContractType");
				} catch (Exception e) {
					logger.info("ExternalBureauIndividualProducts ContractType Exception"+ e+ "Exception");
				}
			}
			
			if (!role.equals("")) {
				String sQueryCustRoleType = "select code from ng_master_role_of_customer with(nolock) where Description='"+role+"'";
				logger.info("CustRoleType"+sQueryCustRoleType);
				Map<String, String> cardqueryXML = getDataFromDB(sQueryCustRoleType, cabinetName, sessionID, jtsIP, jtsPort, "code");
				try {
					if (cardqueryXML != null && cardqueryXML.size() > 0 && cardqueryXML.get("code") != null) {
						role = cardqueryXML.get("code");
					}
				}
				catch(Exception e){
					logger.info("Exception occured at sQueryCombinedLimit for"+sQueryCustRoleType);
				}	
			}
			
			
			add_xml_str +="<ExternalBureauPipelineProducts><applicant_ID>" + wiName + "</applicant_ID>"+ "";
			add_xml_str +="<external_bureau_pipeline_products_id>" + Digital_PL_CommomMethod.validateValue(map.get("AgreementId")) + "</external_bureau_pipeline_products_id>"+ "";
			add_xml_str +="<ppl_provider_no>" + Digital_PL_CommomMethod.validateValue(map.get("ProviderNo")) + "</ppl_provider_no>"+ "";
			add_xml_str +="<ppl_type_of_contract>" + contractType + "</ppl_type_of_contract>"+ "";
			add_xml_str +="<ppl_type_of_product>" + Digital_PL_CommomMethod.validateValue(map.get("LoanDesc")) + "</ppl_type_of_product>"+ "";
			add_xml_str +="<ppl_phase>" + "PIPELINE" + "</ppl_phase>"+ "";
			add_xml_str +="<ppl_role>" + role + "</ppl_role>"+ "";
			add_xml_str +="<ppl_date_of_last_update>" + Digital_PL_CommomMethod.validateValue(map.get("Datelastupdated")) + "</ppl_date_of_last_update>"+ "";
			add_xml_str +="<ppl_no_of_instalments>" + Digital_PL_CommomMethod.validateValue(map.get("TotalNoOfInstalments")) + "</ppl_no_of_instalments>"+ "";
			if (Digital_PL_CommomMethod.validateValue(map.get("LoanType")).toUpperCase().contains("LOAN")) {
				add_xml_str +="<ppl_total_amount>" + Digital_PL_CommomMethod.validateValue(map.get("TotalAmt")) + "</ppl_total_amount>"+ "";
			} else {
				add_xml_str +="<ppl_credit_limit>" + Digital_PL_CommomMethod.validateValue(map.get("col1")) + "</ppl_credit_limit>"+ "";
			}
			add_xml_str +="<ppl_no_of_days_in_pipeline>" + Digital_PL_CommomMethod.validateValue(map.get("NoOfDaysInPipeline")) + "</ppl_no_of_days_in_pipeline>"+ "";
			add_xml_str +="<company_flag>N</company_flag>"+ "";
			add_xml_str +="<ppl_consider_for_obligation>Y</ppl_consider_for_obligation>"+ "";
			add_xml_str +="<ppl_duplicate_flag>"+Digital_PL_CommomMethod.validateValue(map.get("IsDuplicate"))+"</ppl_duplicate_flag></ExternalBureauPipelineProducts>";
		}
		logger.debug("RLOSCommon"+ "Internal liab tag Cration: "	+ add_xml_str);
		return add_xml_str;
	}
	
	private static  String sInputXmlExternalCourtCase(String Wi_Name, String cabinetName, String sessionID, String jtsIP, String jtsPort) {
		String court_cases = "";
		String QueryCaseDetails ="select CodOrganization, ProviderCaseNo, ReferenceDate, CaseCategoryCode,CaseOpenDate, isnull(CaseCloseDate,'') as CaseCloseDate, CaseStatusCode," +
				"InitialTotalClaimAmount from ng_dpl_cust_extexpo_CaseDetails where Wi_Name='"+Wi_Name+"'";
		logger.debug("Select ng_dpl_cust_extexpo_CaseDetails Query: "+QueryCaseDetails);
		List<Map<String,String>> list_map = getDataFromDBMap(QueryCaseDetails, cabinetName, sessionID, jtsIP, jtsPort);
		logger.debug("Total Retrieved Records: " + list_map.size());
		System.out.println("Total Retrieved Records: " + list_map.size());
		for (Map<String,String> map : list_map) {
			court_cases += "<CourtCase>"+ ""
			+"<CodOrganization>"+Digital_PL_CommomMethod.validateValue(map.get("CodOrganization"))+"</CodOrganization>"+ ""
			+"<ProviderCaseNo>"+Digital_PL_CommomMethod.validateValue(map.get("ProviderCaseNo"))+"</ProviderCaseNo>"+ ""
			+"<ReferenceDate>"+Digital_PL_CommomMethod.validateValue(map.get("ReferenceDate"))+"</ReferenceDate>"+ ""
			+"<CaseCategoryCode>"+Digital_PL_CommomMethod.validateValue(map.get("CaseCategoryCode"))+"</CaseCategoryCode>"+ ""
			+"<OpenDate>"+Digital_PL_CommomMethod.validateValue(map.get("CaseOpenDate"))+"</OpenDate>"+ ""
			+"<CloseDate>"+Digital_PL_CommomMethod.validateValue(map.get("CaseCloseDate"))+"</CloseDate>"+ ""
			+"<CaseStatusCode>"+Digital_PL_CommomMethod.validateValue(map.get("CaseStatusCode"))+"</CaseStatusCode>"+ ""
			+"<InitialTotalClaimAmount>"+Digital_PL_CommomMethod.validateValue(map.get("InitialTotalClaimAmount"))+"</InitialTotalClaimAmount>"+ ""
			+"</CourtCase>";
		}
		return court_cases;
	}

	private static  String sInputXmlExternalBureauAccountDetails(String Wi_Name, String cabinetName, String sessionID, String jtsIP, String jtsPort, String prospactId) {
		String ExternalBureauAccountDetails = "";
		try{
			
			//change field mapping as per the document Dectech mapping v3 by Rubi.
			String QueryCaseDetails ="select a.AcctType,a.AcctStat,a.AcctNm,a.ProviderNo,b.Code,a.StartDate,a.ClosedDate from "
					+ "ng_dpl_cust_extexpo_AccountDetails a with(nolock) join NG_MASTER_contract_type b "
					+ "on a.AcctType=b.Descriptionwhere Wi_Name= '"+Wi_Name+"'";

			
			//String QueryCaseDetails ="select AcctType,AcctStat,AcctNm,ProviderNo from ng_dpl_cust_extexpo_AccountDetails with(nolock) where Wi_Name= '"+Wi_Name+"'";
			logger.debug("Select BureauAccountDetails Query: "+QueryCaseDetails);
			List<Map<String,String>> list_map = getDataFromDBMap(QueryCaseDetails, cabinetName, sessionID, jtsIP, jtsPort);
			logger.debug("Total Retrieved Records: " + list_map.size());
			System.out.println("Total Retrieved Records: " + list_map.size());
			for (Map<String,String> map : list_map) {
				//Change Acctstat as per the Test_Dectech_mappingV3 by rubi
				String acctStatCode="";
				if("Active".equalsIgnoreCase(Digital_PL_CommomMethod.validateValue(map.get("AcctStat")))){
					acctStatCode="A";
				}else{
					acctStatCode="C";
				}
				ExternalBureauAccountDetails += "<ExternalBureauAccountDetails>"+ ""
						+"<applicant_id>"+Wi_Name+"</applicant_id>"+ "" // change applicant_id value prospact to wi_no as per the Test_dectech_mappingV3 by rubi
						+"<Account_Type>"+Digital_PL_CommomMethod.validateValue(map.get("Code"))+"</Account_Type>"+ ""
						+"<Phase>"+acctStatCode+"</Phase>"+ ""
						+"<IBAN></IBAN>"+ ""
						+"<CBAccountNo>"+Digital_PL_CommomMethod.validateValue(map.get("AcctNm"))+"</CBAccountNo>"+ ""
						+"<DPAccountNo></DPAccountNo>"+ ""
						+"<Provider_No>"+Digital_PL_CommomMethod.validateValue(map.get("ProviderNo"))+"</Provider_No>"+ ""
						+"<Start_Date>"+Digital_PL_CommomMethod.validateValue(map.get("StartDate"))+"</Start_Date>"+ ""
						+"<Closed_Date>"+Digital_PL_CommomMethod.validateValue(map.get("ClosedDate"))+"</Closed_Date>"+ ""
						+"<Date_Of_Last_Update></Date_Of_Last_Update>"+ ""
						+"</ExternalBureauAccountDetails>";
			}
			/*String QueryCaseDetails ="select AccountType,Phase,IBAN,CBAccountNo,DPAccountNo,ProviderNo,StartDate,CloseDate,DateOfLastUpdate from ng_dpl_cust_extexpo_SalCreditDtls with(nolock) where Wi_Name= '"+Wi_Name+"'";
			logger.debug("Select BureauAccountDetails Query: "+QueryCaseDetails);
			List<Map<String,String>> list_map = getDataFromDBMap(QueryCaseDetails, cabinetName, sessionID, jtsIP, jtsPort);
			logger.debug("Total Retrieved Records: " + list_map.size());
			System.out.println("Total Retrieved Records: " + list_map.size());
			for (Map<String,String> map : list_map) {
				ExternalBureauAccountDetails += "<ExternalBureauAccountDetails>"+ ""
						+"<applicant_id>"+Wi_Name+"</applicant_id>"+ ""
						+"<Account_Type>"+Digital_PL_CommomMethod.validateValue(map.get("AccountType"))+"</Account_Type>"+ ""
						+"<Phase>"+Digital_PL_CommomMethod.validateValue(map.get("Phase"))+"</Phase>"+ ""
						+"<IBAN>"+Digital_PL_CommomMethod.validateValue(map.get("IBAN"))+"</IBAN>"+ ""
						+"<CBAccountNo>"+Digital_PL_CommomMethod.validateValue(map.get("CBAccountNo"))+"</CBAccountNo>"+ ""
						+"<DPAccountNo>"+Digital_PL_CommomMethod.validateValue(map.get("DPAccountNo"))+"</DPAccountNo>"+ ""
						+"<Provider_No>"+Digital_PL_CommomMethod.validateValue(map.get("ProviderNo"))+"</Provider_No>"+ ""
						+"<Start_Date>"+Digital_PL_CommomMethod.validateValue(map.get("StartDate"))+"</Start_Date>"+ ""
						+"<Closed_Date>"+Digital_PL_CommomMethod.validateValue(map.get("CloseDate"))+"</Closed_Date>"+ ""
						+"<Date_Of_Last_Update>"+Digital_PL_CommomMethod.validateValue(map.get("DateOfLastUpdate"))+"</Date_Of_Last_Update>"+ ""
						+"</ExternalBureauAccountDetails>";
			}*/
		}
		catch(Exception e){
			logger.debug("Exception occured while extracting ExternalBureauAccountDetails: "+ e.getMessage());
		}
		return ExternalBureauAccountDetails;
	}
	
	private static  String sInputXmlExternalBureauSalaryDetails(String Wi_Name, String cabinetName, String sessionID, String jtsIP, String jtsPort) {
		String ExternalBureauSalaryDetails = "";
		try{
			logger.debug("Inside sInputXmlExternalBureauSalaryDetails");
			//change field mapping as per the document Dectech mapping v3 by Rubi.
			
			
			String QueryCaseDetails ="select CBAccountNo,IBAN, TotalSalaryAmount, substring(ReferenceDate,0,CHARINDEX('-',ReferenceDate)) as year,"
					+ " substring(ReferenceDate, CHARINDEX('-',ReferenceDate)+1,LEN(ReferenceDate)) as month, NumberOfSalariesTransferred from ng_dpl_cust_extexpo_SalCreditHis with(nolock) where Wi_Name='"+Wi_Name+"'";
			logger.debug("Select SalaryDetails Query: "+QueryCaseDetails);
			List<Map<String,String>> list_map = getDataFromDBMap(QueryCaseDetails, cabinetName, sessionID, jtsIP, jtsPort);
			logger.debug("Total Retrieved Records: " + list_map.size());
			System.out.println("Total Retrieved Records: " + list_map.size());
			for (Map<String,String> map : list_map) {
				ExternalBureauSalaryDetails += "<ExternalBureauSalaryDetails>"+ ""
						+"<applicant_id>"+Digital_PL_CommomMethod.validateValue(map.get("IBAN"))+"</applicant_id>"+ ""
						+"<CBAccountNo>"+Digital_PL_CommomMethod.validateValue(map.get("CBAccountNo"))+"</CBAccountNo>"+ ""
						+"<Year>"+Digital_PL_CommomMethod.validateValue(map.get("year"))+"</Year>"+ ""
						+"<Month>"+Digital_PL_CommomMethod.validateValue(map.get("month"))+"</Month>"+ ""
						+"<Total_Salary_Amount>"+Digital_PL_CommomMethod.validateValue(map.get("TotalSalaryAmount"))+"</Total_Salary_Amount>"+ ""
						+"<No_Of_Salary_Transferred>"+Digital_PL_CommomMethod.validateValue(map.get("NumberOfSalariesTransferred"))+"</No_Of_Salary_Transferred>"+ ""
						+"</ExternalBureauSalaryDetails>";
			}
		}
		catch(Exception e){
			logger.debug("Exception occured while extracting ExternalBureauSalaryDetails: "+ e.getMessage());
		}
		
		return ExternalBureauSalaryDetails;
	}
	
	private static  String sInputXmlPerfios(Map<String, String> applicantDetails_Map, Map<String, String> ApplicationDetailsOldSalary_Map) {
		String EMI18 = applicantDetails_Map.get("Addn_Perfios_EMI_18")==null?"0":applicantDetails_Map.get("Addn_Perfios_EMI_18");
		String EMI19 = applicantDetails_Map.get("Addn_Perfios_EMI_19")==null?"0":applicantDetails_Map.get("Addn_Perfios_EMI_19");
		String EMI20 = applicantDetails_Map.get("Addn_Perfios_EMI_20")==null?"0":applicantDetails_Map.get("Addn_Perfios_EMI_20");
		
		String add_xml_str = "<Perfios>"
				+"<Stmt_Salary_1>"+ApplicationDetailsOldSalary_Map.get("Net_Salary_Month_1")+"</Stmt_Salary_1>"
				+"<Stmt_salary1_date>"+applicantDetails_Map.get("Salary_date_Month_1")+"</Stmt_salary1_date>"
				+"<Stmt_salary_2>"+ApplicationDetailsOldSalary_Map.get("Net_Salary_Month_2")+"</Stmt_salary_2>"
				+"<Stmt_salary2_date>"+applicantDetails_Map.get("Salary_date_Month_2")+"</Stmt_salary2_date>"
				+"<Stmt_salary_3>"+ApplicationDetailsOldSalary_Map.get("Net_Salary_Month_3")+"</Stmt_salary_3>"
				+"<Stmt_salary3_date>"+applicantDetails_Map.get("Salary_date_Month_3")+"</Stmt_salary3_date>"
				+"<Stmt_salary_4>"+applicantDetails_Map.get("Net_Salary_Month_4")+"</Stmt_salary_4>"
				+"<Stmt_salary4_date>"+applicantDetails_Map.get("Salary_date_Month_4")+"</Stmt_salary4_date>"
				+"<Stmt_salary_5>"+applicantDetails_Map.get("Net_Salary_Month_5")+"</Stmt_salary_5>"
				+"<Stmt_salary5_date>"+applicantDetails_Map.get("Salary_date_Month_5")+"</Stmt_salary5_date>"
				+"<Stmt_salary_6>"+applicantDetails_Map.get("Net_Salary_Month_6")+"</Stmt_salary_6>"
				+"<Stmt_salary6_date>"+applicantDetails_Map.get("Salary_date_Month_6")+"</Stmt_salary6_date>"
				+"<Stmt_salary_7>"+applicantDetails_Map.get("Net_Salary_Month_7")+"</Stmt_salary_7>"
				+"<Stmt_salary7_date>"+applicantDetails_Map.get("Salary_date_Month_7")+"</Stmt_salary7_date>"
				+"<Addn_Perfios_EMI_1>"+applicantDetails_Map.get("Addn_Perfios_EMI_1")+"</Addn_Perfios_EMI_1>"
				+"<Addn_Perfios_EMI_2>"+applicantDetails_Map.get("Addn_Perfios_EMI_2")+"</Addn_Perfios_EMI_2>"
				+"<Addn_Perfios_EMI_3>"+ applicantDetails_Map.get("Addn_Perfios_EMI_3")+"</Addn_Perfios_EMI_3>" 
				+"<Addn_Perfios_EMI_4>"+applicantDetails_Map.get("Addn_Perfios_EMI_4")+"</Addn_Perfios_EMI_4>"
				+"<Addn_Perfios_EMI_5>"+ applicantDetails_Map.get("Addn_Perfios_EMI_5")+"</Addn_Perfios_EMI_5>" 
				+"<Addn_Perfios_EMI_6>"+applicantDetails_Map.get("Addn_Perfios_EMI_6")+"</Addn_Perfios_EMI_6>"
				+"<Addn_Perfios_EMI_7>"+ applicantDetails_Map.get("Addn_Perfios_EMI_7")+"</Addn_Perfios_EMI_7>" 
				+"<Addn_Perfios_EMI_8>"+applicantDetails_Map.get("Addn_Perfios_EMI_8")+"</Addn_Perfios_EMI_8>"
				+"<Addn_Perfios_EMI_9>"+applicantDetails_Map.get("Addn_Perfios_EMI_9")+"</Addn_Perfios_EMI_9>"
				+"<Addn_Perfios_EMI_10>"+applicantDetails_Map.get("Addn_Perfios_EMI_10")+"</Addn_Perfios_EMI_10>"
				+"<Addn_Perfios_EMI_11>"+applicantDetails_Map.get("Addn_Perfios_EMI_11")+"</Addn_Perfios_EMI_11>"
				+"<Addn_Perfios_EMI_12>"+applicantDetails_Map.get("Addn_Perfios_EMI_12")+"</Addn_Perfios_EMI_12>"
				+"<Addn_Perfios_EMI_13>"+applicantDetails_Map.get("Addn_Perfios_EMI_13")+"</Addn_Perfios_EMI_13>"
				+"<Addn_Perfios_EMI_14>"+applicantDetails_Map.get("Addn_Perfios_EMI_14")+"</Addn_Perfios_EMI_14>"
				+"<Addn_Perfios_EMI_15>"+applicantDetails_Map.get("Addn_Perfios_EMI_15")+"</Addn_Perfios_EMI_15>"
				+"<Addn_Perfios_EMI_16>"+applicantDetails_Map.get("Addn_Perfios_EMI_16")+"</Addn_Perfios_EMI_16>"
				+"<Addn_Perfios_EMI_17>"+applicantDetails_Map.get("Addn_Perfios_EMI_17")+"</Addn_Perfios_EMI_17>"
				+"<Addn_Perfios_EMI_18>"+EMI18+"</Addn_Perfios_EMI_18>"
				+"<Addn_Perfios_EMI_19>"+EMI19+"</Addn_Perfios_EMI_19>"
				+"<Addn_Perfios_EMI_20>"+EMI20+"</Addn_Perfios_EMI_20>"
				+"<Addn_Perfios_CC>"+applicantDetails_Map.get("Addn_Perfios_CC")+"</Addn_Perfios_CC>"
				+"<Addn_Perfios_OD_Amt>"+applicantDetails_Map.get("Addn_Perfios_OD_Amt")+"</Addn_Perfios_OD_Amt>"
				+"<Addn_OD_date>"+applicantDetails_Map.get("Addn_OD_date")+"</Addn_OD_date>"
				+"<Joint_Acct>"+applicantDetails_Map.get("Joint_Acct")+"</Joint_Acct>"
				+"<High_value_deposit>"+applicantDetails_Map.get("High_Value_Deposit")+"</High_value_deposit>"
				+"<Credit_amount>"+applicantDetails_Map.get("Credit_Amount")+"</Credit_amount>"
				+"<Stmt_chq_rtn_last_3mnts>"+applicantDetails_Map.get("Stmt_chq_rtn_last_3mnts")+"</Stmt_chq_rtn_last_3mnts>"
				+"<Stmt_chq_rtn_cleared_in30_last_3mnts>"+applicantDetails_Map.get("Stmt_chq_rtn_cleared_in30_last_3mnts")+"</Stmt_chq_rtn_cleared_in30_last_3mnts>"
				+"<Stmt_chq_rtn_last_1mnt>"+applicantDetails_Map.get("Stmt_chq_rtn_last_1mnt")+"</Stmt_chq_rtn_last_1mnt>"
				+"<Stmt_chq_rtn_cleared_in30_last_1mnt>"+applicantDetails_Map.get("Stmt_chq_rtn_cleared_in30_last_1mnt")+"</Stmt_chq_rtn_cleared_in30_last_1mnt>"
				+"<Stmt_DDS_rtn_last_3mnts>"+applicantDetails_Map.get("Stmt_DDS_rtn_last_3mnts")+"</Stmt_DDS_rtn_last_3mnts>"
				+"<Stmt_DDS_rtn_cleared_in30_last_3mnts>"+applicantDetails_Map.get("Stmt_DDS_rtn_cleared_in30_last_3mnts")+"</Stmt_DDS_rtn_cleared_in30_last_3mnts>"
				+"<Stmt_DDS_rtn_last_1mnt>"+applicantDetails_Map.get("Stmt_DDS_rtn_last_1mnt")+"</Stmt_DDS_rtn_last_1mnt>"
				+"<Stmt_DDS_rtn_cleared_in30_last_1mnts>"+applicantDetails_Map.get("Stmt_DDS_rtn_cleared_in30_last_1mnts")+"</Stmt_DDS_rtn_cleared_in30_last_1mnts>"
				+"<Pensioner>"+applicantDetails_Map.get("Pensioner_flag")+"</Pensioner>"
				+"<Name_match>"+applicantDetails_Map.get("Name_match")+"</Name_match>"
				+"<FCU_indicator>"+applicantDetails_Map.get("FCUindicator")+"</FCU_indicator>"
				+"<UW_reqd>"+applicantDetails_Map.get("UW_reqd")+"</UW_reqd>"
				+"</Perfios>";
		return add_xml_str;
	}
	
	private static String addDeviations(XMLParser xmlParserSocketDetails,String wi_name)
	{
		String deviationInsertStatus="Success";
		try
		{
			String mainCodeforAPInsert="";
			XMLParser objXMLParser = new XMLParser();
			for(int j = 0; j < 2; j++){
				
				String str_PM_Reason_Codes_Data = xmlParserSocketDetails.getNextValueOf("PM_Reason_Codes_Data");
				logger.debug("str_PM_Reason_Codes_Data : " + str_PM_Reason_Codes_Data);
				
				String PM_Reason_Codes[] = CommonMethods.getTagValues(str_PM_Reason_Codes_Data, "PM_Reason_Codes").split("`");
				logger.debug("length of PM_Reason_Codes"+PM_Reason_Codes.length);
				
				for(int i = 0; i < PM_Reason_Codes.length; i++){
					
					String Reason_Code = CommonMethods.getTagValues(PM_Reason_Codes[i], "Reason_Code");
					logger.debug("Reason_Code : " + Reason_Code);
					String Reason_Description = CommonMethods.getTagValues(PM_Reason_Codes[i], "Reason_Description");
					
					if(Reason_Description.contains("&lt;") || Reason_Description.contains("&amp;") || Reason_Description.contains("&gt;")){
						Reason_Description = Reason_Description.replace("&lt;","<");
						Reason_Description = Reason_Description.replace("&gt;",">");
						Reason_Description = Reason_Description.replace("&amp;gt;","&>");
						Reason_Description = Reason_Description.replaceAll("&amp;","&");
					}
					
					logger.debug("Reason_Description : " + Reason_Description);
					
					if("A999".equalsIgnoreCase(Reason_Code)){
						continue;
					}
					else if(Reason_Code!=null && !"".equalsIgnoreCase(Reason_Code))
					{
						String InsertValues = "'" + wi_name + "','" + Reason_Code + "','" + Reason_Description +"'";
						String columnName = "Wi_Name,Deviation_Code,Deviation_Description";
						String tableName="NG_DPL_GR_DEVIATION_DESCRIPTIO";
						logger.info(" Insert values" + InsertValues);
						
						sessionCheckInt = 0;
					        while(sessionCheckInt<loopCount)
					        {
					            try
					            {
					                
					            	String inputXML = CommonMethods.apInsert(cabinetName, sessionID, columnName, InsertValues, tableName);
									logger.info("Insert No" + inputXML);
									String outputXML = CommonMethods.WFNGExecute(inputXML, jtsIP, jtsPort, 1);
									logger.info("Insert No outXml" + outputXML);
					                objXMLParser.setInputXML(outputXML);
					                mainCodeforAPInsert=objXMLParser.getValueOf("MainCode");
					            }
								
								catch(Exception e)
					            {
					                e.printStackTrace();
					                deviationInsertStatus = "Failure";
					                logger.info("Exception in inserting Deviation data --"+ e.toString());
					                sessionCheckInt++;
					                Digital_PL_CommomMethod.waiteloopExecute(waitLoop);
					                continue;
					            }
					            if (mainCodeforAPInsert.equalsIgnoreCase("11")) 
					            {
					                logger.info("Invalid session in inserting Deviation Data for code-"+Reason_Code);
					                sessionCheckInt++;
					                deviationInsertStatus = "Failure";
					                sessionID=CommonConnection.getSessionID(logger, false);
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
					        	deviationInsertStatus = "Success";
					            logger.info("Deviation Data insert successful for code "+Reason_Code);
					        }
					        else
					        {
					            logger.info("Deviation Data insert Unsuccessful for file "+Reason_Code);
					            deviationInsertStatus = "Failure";
					        }
					
						}
					else
					{
						logger.info("Not a valid reason code -- "+Reason_Code);
					}
				}
				
			}
				
					
		}
			
		catch(Exception e)
		{
			logger.debug("Exception occurred in addDeviations" + e.getMessage());
			deviationInsertStatus = "Failure";
		}
		return deviationInsertStatus;
	}
	
	public static String getTagValue(String xml, String tag) throws ParserConfigurationException, SAXException, IOException
	{
		if (xml != null && !xml.equals("")) {
			Document doc = Digital_PL_CommomMethod.getDocument(xml);
			NodeList nodeList = null;
			int length = 0;
			if (doc != null) {
				nodeList = doc.getElementsByTagName(tag);
				length = nodeList.getLength();
			}

			if (length > 0) {
				Node node = nodeList.item(0);
				logger.debug("Node : " + node);
				System.out.println(""+Node.ELEMENT_NODE);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					NodeList childNodes = node.getChildNodes();
					String value = "";
					int count = childNodes.getLength();
					for (int i = 0; i < count; i++) {
						Node item = childNodes.item(i);
						if (item.getNodeType() == Node.TEXT_NODE) {
							value += item.getNodeValue();
						}
					}
					return value;
				} else if (node.getNodeType() == Node.TEXT_NODE) {
					return node.getNodeValue();
				}
			}
		}
		return "";
	}

}
