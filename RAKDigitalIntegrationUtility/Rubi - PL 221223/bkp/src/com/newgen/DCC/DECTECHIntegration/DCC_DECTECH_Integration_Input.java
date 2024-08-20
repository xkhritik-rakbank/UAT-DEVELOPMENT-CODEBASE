package com.newgen.DCC.DECTECHIntegration;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.omni.wf.util.excp.NGException;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;


public class DCC_DECTECH_Integration_Input implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
	static Map<String, String> DCCSystemIntegrationMap = new HashMap<String, String>();
	
	static NGEjbClient ngEjbClient;
	static String cabinetName = "";
	static String jtsIP = "";
	static String jtsPort = "";
	static String sessionID = "";
	public static int sessionCheckInt=0;
	public static int loopCount=50;
	public static int waitLoop=50;
	
	public static String GenerateXML(String wi_name, String activityID, String activityType, String processDefId, String WorkItemID) {
		String decision =  "Failed";
		try {
			String queueID = "";
			int socketConnectionTimeout=0;
			int integrationWaitTime=0;
			int sleepIntervalInMin=0;
	
	
			DECTECHSystemIntegrationLog.setLogger();
			ngEjbClient = NGEjbClient.getSharedInstance();
			
			
			int configReadStatus = readConfig_DCC();
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.error("Could not Read Config Properties [DCC_DECTECH_System_Integration_Config.properties]");
				return "";
			}

			cabinetName = CommonConnection.getCabinetName();
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("JTSPORT: " + jtsPort);

			queueID = DCCSystemIntegrationMap.get("queueID");
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("QueueID: " + queueID);

			socketConnectionTimeout=Integer.parseInt(DCCSystemIntegrationMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(DCCSystemIntegrationMap.get("INTEGRATION_WAIT_TIME"));
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(DCCSystemIntegrationMap.get("SleepIntervalInMin"));
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);

			/*String WorkItemID = DCCSystemIntegrationMap.get("WorkItemId");
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Current WorkItemID: " + WorkItemID);*/

			sessionID = CommonConnection.getSessionID(DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Could Not Connect to Server!");
			}
			else
			{
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Session ID found: " + sessionID);
				HashMap<String, String> socketDetailsMap= socketConnectionDetails(cabinetName, jtsIP, jtsPort, sessionID);
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("DECTECH CIF Verification...123.");
				return generateRequestXML(cabinetName, jtsIP, jtsPort, sessionID, queueID, socketConnectionTimeout,
							integrationWaitTime, socketDetailsMap, wi_name, WorkItemID, activityID, activityType, processDefId);
					//System.out.println("No More workitems to Process, Sleeping!");
			}
		} catch (NGException e) {
			e.printStackTrace();
		}
		return decision;
	}
	
	private static String generateRequestXML(String cabinetName, String jtsIP, String jtsPort, String sessionID,
			String queueID, int socketConnectionTimeout2, int integrationWaitTime2,
			HashMap<String, String> socketDetailsMap, String wi_name, String WorkItemID, String activityID, String activityType, String processDefId) {
		String ws_name = "System_Integration";
		//wi_name = "DCC-0000000809-process";
		String decisionValue="";
		String returnValue="";
		try {
			/** Select data and put in the map from NG_DCC_EXTTABLE **/
			String DBQuery = "SELECT Wi_Name, Application_Type, CIF, isnull(Prefer_product,Product) as 'Product', Product_Desc, Sub_Product, Card_Product, CUSTOMERNAME, PassportNo, EmirateID, MobileNo, employercode, "
					+ "Employer_Name, EmploymentType, EmploymentType_Desc, email_id, Final_Limit, VIPFlag, Title, Title_Desc, FirstName, MiddleName, LastName, dob, Age, Nationality, Nationality_Desc, Designation, Designation_Desc, Cust_Decl_Salary, "
					+ "Prospect_id, FinalDBR, FinalTAI, Passport_expiry, Gender_Code, Gender_Code, IndusSeg, IndusSeg_Desc, EligibleCardProduct, "
					+ "EligibleCardProduct_Desc, Date_Of_Joining, Selected_Card_Type, Prospect_Creation_Date, FIRCO_Flag, Visa_Expiry, "
					+ "Emirates_Visa, EmID_Expiry, Visa_Sponsor_Name,GCC_National, No_earning_members, Earning_members,"
					+ "Dependents, Cust_Decl_Salary,Net_Salary1,Net_Salary2,Net_Salary3,"
					+"Net_Salary1,Net_salary1_date,Net_Salary2,Net_salary2_date,Net_Salary3,Net_salary3_date,"+
					"Net_Salary4,Net_salary4_date,Net_Salary5,Net_salary5_date, Net_Salary6,Net_salary6_date,Net_Salary7,Net_salary7_date,"+
					"Addn_Perfios_EMI_1,Addn_Perfios_EMI_2,Addn_Perfios_EMI_3,Addn_Perfios_EMI_4,"+
					"Addn_Perfios_EMI_5,Addn_Perfios_EMI_6,Addn_Perfios_EMI_7,Addn_Perfios_EMI_8,"+
					"Addn_Perfios_EMI_9,Addn_Perfios_EMI_10,Addn_Perfios_EMI_11,Addn_Perfios_EMI_12,"+
					"Addn_Perfios_EMI_13,Addn_Perfios_EMI_14,Addn_Perfios_EMI_15,Addn_Perfios_EMI_16,"+
					"Addn_Perfios_EMI_17,Addn_Perfios_EMI_18,Addn_Perfios_EMI_19,Addn_Perfios_EMI_20,Addn_Perfios_CC,"
					+ "Addn_Perfios_OD_Amt,Addn_OD_date,Joint_Acct,High_Value_Deposit,Credit_Amount,Stmt_chq_rtn_last_3mnts,"
					+ "Stmt_chq_rtn_cleared_in30_last_3mnts,Stmt_chq_rtn_last_1mnt,Stmt_chq_rtn_cleared_in30_last_1mnt,"
					+ "Stmt_DDS_rtn_last_3mnts,Stmt_DDS_rtn_cleared_in30_last_3mnts,Stmt_DDS_rtn_last_1mnt,Stmt_DDS_rtn_cleared_in30_last_1mnts,"
					+ "Pensioner,Name_match,FCU_indicator,UW_reqd, requested_limit, Industry, Sub_Industry, EFR_NSTP,EStatementFlag "
					+ " FROM NG_DCC_EXTTABLE with(nolock) WHERE WI_NAME='" + wi_name + "'";
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Select NG_DCC_EXTTABLE Query: "+DBQuery);

			String[] columns = { "Wi_Name", "Application_Type", "CIF", "Product", "Product_Desc", "Sub_Product",
					"Card_Product", "CUSTOMERNAME", "PassportNo", "EmirateID", "MobileNo", "employercode",
					"Employer_Name", "EmploymentType", "EmploymentType_Desc", "email_id", "Final_Limit", "VIPFlag",
					"Title", "Title_Desc", "FirstName", "MiddleName", "LastName", "dob", "Age", "Nationality",
					"Nationality_Desc", "Designation", "Designation_Desc", "Cust_Decl_Salary", "Prospect_id",
					"FinalDBR", "FinalTAI", "Passport_expiry", "Gender", "Gender_Code", "IndusSeg", "IndusSeg_Desc",
					"EligibleCardProduct", "EligibleCardProduct_Desc", "Date_Of_Joining","Selected_Card_Type","Prospect_Creation_Date",
					"FIRCO_Flag", "Visa_Expiry", "Emirates_Visa", "EmID_Expiry","Visa_Sponsor_Name",
					"GCC_National","No_earning_members", "Earning_members" ,"Dependents","Cust_Decl_Salary",
					"Net_Salary1", "Net_salary1_date", "Net_Salary2", "Net_salary2_date", "Net_Salary3", "Net_salary3_date",
					"Net_Salary4", "Net_salary4_date", "Net_Salary5", "Net_salary5_date", "Net_Salary6",
					"Net_salary6_date", "Net_Salary7", "Net_salary7_date", "Addn_Perfios_EMI_1", "Addn_Perfios_EMI_2",
					"Addn_Perfios_EMI_3", "Addn_Perfios_EMI_4", "Addn_Perfios_EMI_5", "Addn_Perfios_EMI_6",
					"Addn_Perfios_EMI_7", "Addn_Perfios_EMI_8", "Addn_Perfios_EMI_9", "Addn_Perfios_EMI_10",
					"Addn_Perfios_EMI_11", "Addn_Perfios_EMI_12", "Addn_Perfios_EMI_13", "Addn_Perfios_EMI_14",
					"Addn_Perfios_EMI_15", "Addn_Perfios_EMI_16", "Addn_Perfios_EMI_17", "Addn_Perfios_EMI_18",
					"Addn_Perfios_EMI_19", "Addn_Perfios_EMI_20", "Addn_Perfios_CC", "Addn_Perfios_OD_Amt",
					"Addn_OD_date", "Joint_Acct", "High_Value_Deposit", "Credit_Amount", "Stmt_chq_rtn_last_3mnts",
					"Stmt_chq_rtn_cleared_in30_last_3mnts", "Stmt_chq_rtn_last_1mnt",
					"Stmt_chq_rtn_cleared_in30_last_1mnt", "Stmt_DDS_rtn_last_3mnts",
					"Stmt_DDS_rtn_cleared_in30_last_3mnts", "Stmt_DDS_rtn_last_1mnt",
					"Stmt_DDS_rtn_cleared_in30_last_1mnts", "Pensioner", "Name_match", "FCU_indicator", "UW_reqd","requested_limit","Industry","Sub_Industry","EFR_NSTP","EStatementFlag" };
			
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("DBQuery: " + DBQuery);
			Map<String,String> ApplicantDetails_Map = getDataFromDB(DBQuery, cabinetName, sessionID, jtsIP, jtsPort, columns);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Total Retrieved Records: " + ApplicantDetails_Map.get("TotalRetrieved"));
			System.out.println("Total Retrieved Records: " + ApplicantDetails_Map.get("TotalRetrieved"));
			
			
			//Added by Kamran 09012023--For Salary Details updated by UW WS
			
			//Commented for new conditions added in NG_DCC_GR_NetSalaryDetails to capture Ws name -- 17/07/23
			//String DBQuery_OldSalary = "SELECT TOP 1 wi_name, Net_Salary1, Net_Salary2, Net_Salary3 FROM NG_DCC_GR_NetSalaryDetails with(nolock) WHERE WI_NAME='" + wi_name + "'" + " ORDER BY insertion_date_time DESC";
			
			String DBQuery_OldSalary ="";
			// Estatement flag: If false then below  Changed on 18.7.23
			
			if("false".equalsIgnoreCase(ApplicantDetails_Map.get("EStatementFlag"))){
				
					DBQuery_OldSalary ="SELECT TOP 1 wi_name, Net_Salary1, Net_Salary2, Net_Salary3 FROM NG_DCC_GR_NetSalaryDetails with(nolock) WHERE WI_NAME='" + wi_name + "'"
					+ " and (Workstep ='Sys_FTS_WI_Update')";
			}
			else {
					
				DBQuery_OldSalary = "SELECT wi_name, Net_Salary1, Net_Salary2, Net_Salary3 FROM NG_DCC_GR_NetSalaryDetails with(nolock) WHERE  WI_NAME ='" + wi_name + "'  and (Workstep ='Source_Refer')"+
				"union all "+
				"select Wi_Name, Net_Salary1, Net_Salary2, Net_Salary3 from NG_DCC_EXTTABLE with(nolock) where Wi_Name ='" + wi_name + "'";
			}
			
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Select Old Salary Query: " + DBQuery_OldSalary);
			String[] columns_OldSalary = { "wi_name","Net_Salary1","Net_Salary2","Net_Salary3"};
			Map<String, String> ApplicationDetailsOldSalary_Map = getDataFromDB(DBQuery_OldSalary, cabinetName, sessionID, jtsIP, jtsPort,
					columns_OldSalary);
			//End
			
			StringBuilder stringBuilder = readRequestXmlSample();
			
			String requested_xml = stringBuilder.toString().replace(">Str_ApplicationNumber<",">"+wi_name+"<");
			
			/** Application Details Tag**/
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm");
			
			requested_xml = requested_xml.replace(">str_TimeStampyyyymmddhhmmsss<",">"+sdf1.format(new Date())+"<");
			
			//String full_eligibility_availed = validateValue(ApplicantDetails_Map.get("EligibleCardProduct_Desc"));
			//full_eligibility_availed = full_eligibility_availed == "" ? "Select" : full_eligibility_availed;
			requested_xml = requested_xml.replace(">Str_full_eligibility_availed<",">Select<")
					.replace(">Str_product_type<",">"+validateValue(ApplicantDetails_Map.get("Product"))+"<") // HRITIK - 11.10.23
					.replace(">Str_app_category<",">BAU<")
					.replace(">Str_requested_product<",">CC<")
					.replace(">Str_requested_limit<",">"+validateValue(ApplicantDetails_Map.get("requested_limit"))+"<")
					.replace(">Str_sub_product<",">Digital CC STP<")
					.replace(">Str_requested_card_product<",">"+validateValue(ApplicantDetails_Map.get("Selected_Card_Type"))+"<")
					.replace(">Str_interest_rate<",">0.00<")
					.replace(">Str_customer_type<",">NTB<")
					.replace(">Str_final_limit<",">"+validateValue(ApplicantDetails_Map.get("requested_limit"))+"<")
					.replace(">Str_emi<",">0.00<")
					.replace(">Str_manual_deviation<",">N<")
					.replace(">Str_application_date<",">"+validateValue(ApplicantDetails_Map.get("Prospect_Creation_Date")+"T00:00:00")+"<");
					//Updated above line on 29112022 - Kamran
			
			requested_xml = requested_xml.replace(">Str_Wi_Name<",">"+wi_name+"<");
			
			String app_details = sInputXmlApplicantDetails(ApplicantDetails_Map, cabinetName, sessionID, jtsIP, jtsPort);
			requested_xml = requested_xml.replace("<String_ApplicantDetails>",app_details);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("DCC sInputXmlApplicantDetails : "+ requested_xml);

			/** internal   Bureau TAG and  sub-tag **/
			String internal_Bureau = sInputXmlInternalBureau(ApplicantDetails_Map);
			requested_xml = requested_xml.replace("<String_InternalBureauData>",internal_Bureau);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("DCC sInputXmlExternalBureau : "+ internal_Bureau);		
			
			/** External Bureau sub-tag **/
			String external_Bureau = sInputXmlExternalBureau(ApplicantDetails_Map, cabinetName, sessionID, jtsIP, jtsPort);
			requested_xml = requested_xml.replace("<String_ExternalBureau>",external_Bureau);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("DCC sInputXmlExternalBureau : "+ requested_xml);
			
			/** Cheque Bounce sub-Tag **/
			String bounced_Cheques = sInputXmlExternalBouncedCheques(wi_name, cabinetName, sessionID, jtsIP, jtsPort);
			requested_xml = requested_xml.replace("<String_BouncedCheques>",bounced_Cheques);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("DCC sInputXmlExternalBouncedCheques : "+ requested_xml);
			
			/** utilization sub-Tag **/
			String utilization = sInputXmlExternalUtilization(wi_name, cabinetName, sessionID, jtsIP, jtsPort);
			requested_xml = requested_xml.replace("<String_Utilization24months>",utilization);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("DCC sInputXmlExternalBouncedCheques : "+ requested_xml);
			
			/** utilization sub-Tag **/
			String history = sInputXmlExternalHistory(wi_name, cabinetName, sessionID, jtsIP, jtsPort);
			requested_xml = requested_xml.replace("<String_History_24months>",history);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("DCC sInputXmlExternalBouncedCheques : "+ requested_xml);
			
			/** Court Cases sub-Tag **/
			String court_cases = sInputXmlExternalCourtCase(wi_name, cabinetName, sessionID, jtsIP, jtsPort);
			requested_xml = requested_xml.replace("<String_CourtCase>",court_cases);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("DCC sInputXmlExternalCourtCase : "+ requested_xml);
		
			
			//** ExternalBureau Account Details sub-Tag **//*
			String ExternalBureauAccountDetails = sInputXmlExternalBureauAccountDetails(wi_name, cabinetName, sessionID, jtsIP, jtsPort);
			requested_xml = requested_xml.replace("<String_ExternalBureauAccountDetails>",ExternalBureauAccountDetails);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("DCC sInputXmlExternalCourtCase : "+ requested_xml);
			
			//** ExternalBureau Salary Details sub-Tag **//*
			String ExternalBureauSalaryDetails = sInputXmlExternalBureauSalaryDetails(wi_name, cabinetName, sessionID, jtsIP, jtsPort);
			requested_xml = requested_xml.replace("<String_ExternalBureauSalaryDetails>",ExternalBureauSalaryDetails);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("DCC sInputXmlExternalCourtCase : "+ requested_xml);
		
			/** External Bureau Individual Products sub-Tag **/
			String individual_Products = sInputXmlExternalBureauIndividualProducts(wi_name, cabinetName, sessionID, jtsIP, jtsPort);
			requested_xml = requested_xml.replace("<String_ExternalBureauIndividualProducts>",individual_Products);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("DCC sInputXmlExternalBureauIndividualProducts : "+ requested_xml);
			
			/** External Bureau Pipeline Products sub-tag**/
			String pipeline_Products = sInputXmlExternalBureauPipelineProducts(wi_name, cabinetName, sessionID, jtsIP, jtsPort);
			requested_xml = requested_xml.replace("<String_ExternalBureauPipelineProducts>",pipeline_Products);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("DCC sInputXmlExternalBureauPipelineProducts : "+ requested_xml);
			
			/** External Bureau Pipeline Products sub-tag**/
			String perfios_details = sInputXmlPerfios(ApplicantDetails_Map, ApplicationDetailsOldSalary_Map);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("DCC perfios_details : "+ perfios_details);
			requested_xml = requested_xml.replace("<String_Perfios>",perfios_details);
			
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("DCC Final XML : "+ requested_xml);
			
			String integrationStatus="Success";
			//String attributesTag;
			//String ErrDesc = "";
			String columnNames = "";
	    	String columnValues ="";
			StringBuilder finalString=new StringBuilder(requested_xml);
			HashMap<String, String> socketConnectionMap = socketConnectionDetails(cabinetName, jtsIP, jtsPort, sessionID); 
			
			integrationStatus = socketConnection(cabinetName, CommonConnection.getUsername(), sessionID, jtsIP, jtsPort, wi_name, ws_name, 60, 65,socketConnectionMap, finalString);
			
			XMLParser xmlParserSocketDetails= new XMLParser(integrationStatus);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug(" xmlParserSocketDetails : "+xmlParserSocketDetails);
			String SystemErrorCode = xmlParserSocketDetails.getValueOf("SystemErrorCode");
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("SystemErrorCode : "+SystemErrorCode+" for WI: "+wi_name);
			String SystemErrorMessage = xmlParserSocketDetails.getValueOf("SystemErrorMessage");
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("SystemErrorMessage : "+SystemErrorMessage+" for WI: "+wi_name);
			String Deviationinsertstatus="";
			if (SystemErrorCode != null && !SystemErrorCode.equals(""))
			{
				returnValue="Failed";
				decisionValue = "Failed";
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Decision in else : " +decisionValue);
				//attributesTag="<Decision>"+decisionValue+"</Decision>";
				columnNames = "Dectech_Flag";
		    	columnValues = "'N'";
			}
			else 
			{
				// all the below fields are in <Application> tag
				String Output_Decision = xmlParserSocketDetails.getValueOf("Output_Decision");
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Output_Decision: "+Output_Decision+ "WI: "+wi_name);
				String Output_NSTP = xmlParserSocketDetails.getValueOf("Output_NSTP");
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Output_NSTP: "+Output_NSTP+ "WI: "+wi_name);
				String Output_NSTP_Reason = xmlParserSocketDetails.getValueOf("Output_NSTP_Reason");
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Output_NSTP_Reason: "+Output_NSTP_Reason+ "WI: "+wi_name);
				String Output_TAI = xmlParserSocketDetails.getValueOf("Output_TAI");
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Output_TAI: "+Output_TAI+ "WI: "+wi_name);
				String Output_Final_DBR = xmlParserSocketDetails.getValueOf("Output_Final_DBR");
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Output_Final_DBR: "+Output_Final_DBR+ "WI: "+wi_name);
				String Output_Affordable_Ratio  = xmlParserSocketDetails.getValueOf("Output_Affordable_Ratio");
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Output_Affordable_Ratio : "+Output_Affordable_Ratio + "WI: "+wi_name);
				String Output_TotalDeduction = xmlParserSocketDetails.getValueOf("Output_TotalDeduction");
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Output_TotalDeduction: "+Output_TotalDeduction+ "WI: "+wi_name);
				String Output_Stress_BufferAmt = xmlParserSocketDetails.getValueOf("Output_Stress_BufferAmt");
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Output_Stress_BufferAmt: "+Output_Stress_BufferAmt+ "WI: "+wi_name);
				String Output_Eligible_Amount = xmlParserSocketDetails.getValueOf("Output_Eligible_Amount");
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Output_Eligible_Amount: "+Output_Eligible_Amount+ "WI: "+wi_name);
				//DBR_lifeStyle_expenses
				String Output_Delegation_Authority = xmlParserSocketDetails.getValueOf("Output_Delegation_Authority");
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Output_Delegation_Authority: "+Output_Delegation_Authority+ "WI: "+wi_name);
				
				String Output_Age = xmlParserSocketDetails.getValueOf("Output_Age");
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Output_Age: "+Output_Age+ "WI: "+wi_name);
				
				String Output_NoOf_AECBHistory = xmlParserSocketDetails.getValueOf("Output_NoOf_AECBHistory");
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Output_NoOf_AECBHistory: "+Output_NoOf_AECBHistory+ "WI: "+wi_name);
							
				Output_Age= validateValue(Output_Age);
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
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Output_NSTP_Reason-- " + Output_NSTP_Reason);
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
				
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Final nstp reason-- " + final_NSTP_Reason);
			       
				//Rubi
				String Output_Eligible_Card = xmlParserSocketDetails.getValueOf("Output_Eligible_Cards");
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Output_Eligible_Card: "+Output_Eligible_Amount+ " WI: "+wi_name);
				
				String[] Output_Eligible_Cards_Arr=Output_Eligible_Card.split("\\},\\{");
				String card_Product = "";
							
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Output_Eligible_Cards_Arr.length "+ Output_Eligible_Cards_Arr.length);
				if(Output_Eligible_Cards_Arr.length>0)
				{
					for (int i=0; i<Output_Eligible_Cards_Arr.length;i++){
						DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Value iof i "+ i);
						DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Output_Eligible_Cards_Arr[i]:"+Output_Eligible_Cards_Arr[i]);
						String[] Output_Eligible_Cards_Array=Output_Eligible_Cards_Arr[i].split(",");
						
						if(Output_Eligible_Cards_Array.length==3){
							String [] CArdProducyList= Output_Eligible_Cards_Array[0].split(":");
							DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Output_Eligible_Cards_Product:"+CArdProducyList[0]+" "+CArdProducyList[1]);
							
							if(!card_Product.isEmpty()){
								DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Output_Eligible_Cards if block:"+card_Product);
								
								card_Product = card_Product+","+CArdProducyList[1];
							}else{
								DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Output_Eligible_Cards else block:"+card_Product);
								
								card_Product += CArdProducyList[1];
							}
						}
					}
				}
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("card_Product: "+card_Product+ "WI: "+wi_name);
				
				card_Product = card_Product.replaceAll("\"", "");
				card_Product=card_Product.replaceAll(",","~");
				String OutputAlternateCard = xmlParserSocketDetails.getValueOf("Output_Alternate_Card");
				
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("card_Product after replace: "+card_Product+ "WI: "+wi_name);
				
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Output_Alternate_Card: "+OutputAlternateCard+ "WI: "+wi_name);
				OutputAlternateCard = OutputAlternateCard.replaceAll("\"", "");
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("OutputAlternateCard: "+OutputAlternateCard+ "WI: "+wi_name);
				
				columnNames = "Is_STP, Dectech_Decision, Non_STP_reason, FinalDBR, FinalTAI, DBR_lifeStyle_expenses,"
						+ "Output_TotalDeduction, Output_Stress_BufferAmt,Final_Limit,Dectech_Flag,delegation_authority" +
						",AECB_history,Age,OutputAlternateCard,Output_Eligible_Card";
		    	columnValues = "'" + is_stp + "','"+ Output_Decision +"','"+ final_NSTP_Reason +"','"+ Output_Final_DBR +
		    			"','"+ Output_TAI +"','"+ Output_Affordable_Ratio +"','"+ Output_TotalDeduction +"','"+ 
		    			Output_Stress_BufferAmt+"','"+ Output_Eligible_Amount +"','Y','"+
		    			Output_Delegation_Authority+"','" +Output_NoOf_AECBHistory+"','" +Output_Age+"','"
		    			+OutputAlternateCard+"','"+card_Product+"'";
		    	
		    	DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("card_Product columnValues: "+columnValues);
		    	
				Deviationinsertstatus=addDeviations(xmlParserSocketDetails,wi_name);
		    	returnValue="Success";
				decisionValue = "Success";
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Decision in success: " +decisionValue);
				//attributesTag="<Decision>"+decisionValue+"</Decision>";
			}
			
	    	String sWhereClause = "WI_NAME='" + wi_name + "'";
	    	String tableName = "NG_DCC_EXTTABLE";
	        String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger, false), 
	        		tableName, columnNames, columnValues, sWhereClause);
	        DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Input XML for apUpdateInput for " + tableName + " Table : " + inputXML);
	        String outputXml = CommonMethods.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
	        DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Output XML for apUpdateInput for " + tableName + " Table : " + outputXml);
	        XMLParser sXMLParserChild = new XMLParser(outputXml);
	        String StrMainCode = sXMLParserChild.getValueOf("MainCode");
	        //String RetStatus = null;
	        if (!StrMainCode.equals("0")|| !"Success".equalsIgnoreCase(Deviationinsertstatus))
	        {
	        	returnValue="Failed";
	        	DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Error in Executing apUpdateInput sOutputXML for dectech field update: " + outputXml);
	        }
			
		} catch (Exception e) {
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Exception: "+e.getMessage());
			return "Failed";
		}
		return returnValue;
	}

	private static  StringBuilder readRequestXmlSample() {
		StringBuilder sb = new StringBuilder("");
		try {
			String fileLocation = new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DCC_Integration")
					.append(System.getProperty("file.separator")).append("DECTECH_Integration.txt").toString();
			BufferedReader sbf = new BufferedReader(new FileReader(fileLocation));
			
			String line = sbf.readLine();
			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = sbf.readLine();
			}
		} catch (FileNotFoundException e) {
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.error("In Reading DECTECH_Integration.txt : " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.error("In Reading DECTECH_Integration.txt : " + e.getMessage());
			e.printStackTrace();
		}
		return sb;
	}
	
	protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort, int flag)
			throws IOException, Exception {
		DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("In WF NG Execute : " + serverPort);
		try {
			if (serverPort.startsWith("33"))
				return WFCallBroker.execute(ipXML, jtsServerIP, Integer.parseInt(serverPort), 1);
			else
				return ngEjbClient.makeCall(jtsServerIP, serverPort, "WebSphere", ipXML);
		} catch (Exception e) {
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Exception Occured in WF NG Execute : " + e.getMessage());
			e.printStackTrace();
			return "Error";
		}
	}
	
	public static String getTagValue(String xml, String tag) throws ParserConfigurationException, SAXException, IOException
	{
		if (xml != null && !xml.equals("")) {
			Document doc = getDocument(xml);
			NodeList nodeList = null;
			int length = 0;
			if (doc != null) {
				nodeList = doc.getElementsByTagName(tag);
				length = nodeList.getLength();
			}

			if (length > 0) {
				Node node = nodeList.item(0);
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Node : " + node);
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

	public static Document getDocument(String xml) throws ParserConfigurationException, SAXException, IOException {
		Document doc = null;
		try {
			// Step 1: create a DocumentBuilderFactory
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

			// Step 2: create a DocumentBuilder
			DocumentBuilder db = dbf.newDocumentBuilder();

			// Step 3: parse the input file to get a Document object
			doc = db.parse(new InputSource(new StringReader(xml)));
		} catch (Exception ex) {
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug(printException(ex));
		} finally {
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Inside finally block of getDocument method");

		}
		return doc;
	}
	
	public static String printException(Exception e){
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String exception = sw.toString();
		return exception;	
	}
	
	private static  int readConfig_DCC() {
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir") + File.separator + "ConfigFiles" + File.separator + "DCC_System_Integration_Config.properties")));

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
	
	private static  HashMap<String, String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort, String sessionID) {
		HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

		try {
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Fetching Socket Connection Details.");
			System.out.println("Fetching Socket Connection Details.");

			String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'DCC' and CallingSource = 'Utility'";

			String socketDetailsInputXML = CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Socket Details APSelect InputXML: " + socketDetailsInputXML);

			String socketDetailsOutputXML = WFNGExecute(socketDetailsInputXML, sJtsIp, iJtsPort, 1);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Socket Details APSelect OutputXML: " + socketDetailsOutputXML);

			XMLParser xmlParserSocketDetails = new XMLParser(socketDetailsOutputXML);
			String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("SocketDetailsMainCode: " + socketDetailsMainCode);

			int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("SocketDetailsTotalRecords: " + socketDetailsTotalRecords);

			if (socketDetailsMainCode.equalsIgnoreCase("0") && socketDetailsTotalRecords > 0) {
				String xmlDataSocketDetails = xmlParserSocketDetails.getNextValueOf("Record");
				xmlDataSocketDetails = xmlDataSocketDetails.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

				XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

				String socketServerIP = xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("SocketServerIP: " + socketServerIP);
				socketDetailsMap.put("SocketServerIP", socketServerIP);

				String socketServerPort = xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("SocketServerPort " + socketServerPort);
				socketDetailsMap.put("SocketServerPort", socketServerPort);

				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("SocketServer Details found.");
				System.out.println("SocketServer Details found.");

			}
		} catch (Exception e) {
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger
					.debug("Exception in getting Socket Connection Details: " + e.getMessage());
			System.out.println("Exception in getting Socket Connection Details: " + e.getMessage());
		}

		return socketDetailsMap;
	}
	
	private static  String sInputXmlApplicantDetails(Map<String, String> applicantDetails_Map, String cabinetName, String sessionID, String jtsIP, String jtsPort) {
		String industry_sector = "";
		String industry_macro = validateValue(applicantDetails_Map.get("Industry"));
		String industry_micro = validateValue(applicantDetails_Map.get("Sub_Industry"));
		String COMPANY_STATUS_CC = "";
		String COMPANY_STATUS_PL = "";
		String  INCLUDED_IN_CC_ALOC = ""; 
		String  INCLUDED_IN_PL_ALOC = "";
		String  current_emp_catogery = "";
		String TYPE_OF_COMPANY= "";
		String EMPLOYER_CATEGORY_PL_EXPAT="";
		String EMPLOYER_CATEGORY_PL_NATIONAL="";
		//select Description,Code from NG_MASTER_EmployerCategory_PL with(nolock) where IsActive='Y'
		String employercode = validateValue(applicantDetails_Map.get("employercode"));
		if (!employercode.equals("")) {
			String query = "select TOP 1 INDUSTRY_SECTOR, INDUSTRY_MACRO, INDUSTRY_MICRO, COMPANY_STATUS_CC,COMPANY_STATUS_PL, "
					+ "INCLUDED_IN_CC_ALOC, INCLUDED_IN_PL_ALOC, EMPLOYER_CATEGORY_PL, TYPE_OF_COMPANY, EMPLOYER_CATEGORY_PL_EXPAT, "
					+ "EMPLOYER_CATEGORY_PL_NATIONAL from NG_RLOS_ALOC_OFFLINE_DATA WITH(nolock) where EMPLOYER_CODE=main_Employer_code "
					+ "and main_Employer_code = '" + employercode+ "'";
			try {
				String EMPLOYER_CATEGORY_PL = "";
				List<Map<String,String>> OutputXML_ref = getDataFromDBMap(query, cabinetName, sessionID, jtsIP, jtsPort);
				if(OutputXML_ref.size()>0)
				{
					industry_sector = OutputXML_ref.get(0).get("INDUSTRY_SECTOR");
					
					if (!validateValue(OutputXML_ref.get(0).get("INDUSTRY_MACRO")).equals(""))
						industry_macro = OutputXML_ref.get(0).get("INDUSTRY_MACRO");

					if (!validateValue(OutputXML_ref.get(0).get("INDUSTRY_MICRO")).equals(""))
						industry_micro=OutputXML_ref.get(0).get("INDUSTRY_MICRO");

					COMPANY_STATUS_CC=OutputXML_ref.get(0).get("COMPANY_STATUS_CC");
					COMPANY_STATUS_PL=OutputXML_ref.get(0).get("COMPANY_STATUS_PL");
					INCLUDED_IN_CC_ALOC=OutputXML_ref.get(0).get("INCLUDED_IN_CC_ALOC");
					INCLUDED_IN_PL_ALOC=OutputXML_ref.get(0).get("INCLUDED_IN_PL_ALOC");
					EMPLOYER_CATEGORY_PL=OutputXML_ref.get(0).get("EMPLOYER_CATEGORY_PL");
					TYPE_OF_COMPANY=OutputXML_ref.get(0).get("TYPE_OF_COMPANY");
					EMPLOYER_CATEGORY_PL_EXPAT=OutputXML_ref.get(0).get("EMPLOYER_CATEGORY_PL_EXPAT");
					EMPLOYER_CATEGORY_PL_NATIONAL=OutputXML_ref.get(0).get("EMPLOYER_CATEGORY_PL_NATIONAL");
				}
				if (!"".equals("EMPLOYER_CATEGORY_PL")) {
					//String queryForEmpCat= "select Description from NG_MASTER_EmployerCategory_PL with(nolock) where Code='" + EMPLOYER_CATEGORY_PL+ "' and IsActive='Y'";
					//Kamran 23112022
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
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug(" Exception occurred in ApplicantDetails Query"+ query);
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug(" Exception occurred in sInputXmlApplicantDetails()"+ e.getMessage());
			}
		}
		String world_check="N";
		//if (validateValue(applicantDetails_Map.get("FIRCO_Flag")).equalsIgnoreCase("Y") || validateValue(applicantDetails_Map.get("FIRCO_Flag")).equalsIgnoreCase("CB")) {
		if (validateValue(applicantDetails_Map.get("FIRCO_Flag")).equalsIgnoreCase("CB")) {
			world_check= "Y";
		}
		return "<ApplicantDetails>" + ""
		+"<applicant_id>"+applicantDetails_Map.get("Wi_Name")+"</applicant_id>" + ""
		+"<primary_cif>"+validateValue(applicantDetails_Map.get("CIF"))+"</primary_cif>" + ""
		+"<ref_no>"+applicantDetails_Map.get("Prospect_id")+"</ref_no>" + ""
		+"<wi_name>"+applicantDetails_Map.get("Wi_Name")+"</wi_name>" + ""
		+"<cust_name>"+validateValue(applicantDetails_Map.get("FirstName"))+ " "+validateValue(applicantDetails_Map.get("LastName"))+"</cust_name>" + ""
		+"<emp_type>"+validateValue(applicantDetails_Map.get("EmploymentType"))+"</emp_type>" + ""
		+"<dob>"+validateValue(applicantDetails_Map.get("dob"))+"</dob>" + ""
		+"<age>"+validateValue(applicantDetails_Map.get("Age"))+"</age>" + ""
		//+"<dbr>"+validateValue(applicantDetails_Map.get("FinalDBR"))+"</dbr>" + ""
		//+"<tai>25000.00</tai>" + ""
		+"<nationality>"+validateValue(applicantDetails_Map.get("Nationality"))+"</nationality>" + ""
		+"<resident_flag>Y</resident_flag>" + ""
		+"<world_check>"+world_check+"</world_check>" + ""
		//+"<blacklist_cust_type>I</blacklist_cust_type>" + ""
		//+"<negative_cust_type>I</negative_cust_type>" + ""
		+"<no_of_cheque_bounce_int_3mon_Ind>0</no_of_cheque_bounce_int_3mon_Ind>" + "" //TODO This should be if UW adds in grid in IBPS-If cheque
		+"<no_of_dds_return_int_3mon_Ind>0</no_of_dds_return_int_3mon_Ind>" + "" //TODO This should be if UW adds in grid in IBPS- If DDS
		//+"<external_blacklist_flag>I</external_blacklist_flag>" + ""
		+"<los>"+CalculatLOS(applicantDetails_Map.get("Date_Of_Joining"))+"</los>" + ""
		+"<target_segment_code>DIG</target_segment_code>" + ""
		//+"<avg_credit_turnover_3>66666.67</avg_credit_turnover_3>" + ""
		//+"<avg_bal_3>223145.2</avg_bal_3>" + ""
		+"<current_emp_catogery>"+current_emp_catogery+"</current_emp_catogery>" + ""
		//+"<year_in_uae>04.00</year_in_uae>" + ""
		//+"<ref_relationship>Friend</ref_relationship>" + "" 
		//+"<ref_phone_no>Friend</ref_phone_no>" + "" 
		//+"<visa_issue_date>Friend</visa_issue_date>" + "" 

		+"<visa_expiry_date>"+validateValue(applicantDetails_Map.get("Visa_Expiry"))+"</visa_expiry_date>" + ""
		+"<passport_expiry_date>"+validateValue(applicantDetails_Map.get("EmID_Expiry"))+"</passport_expiry_date>" + ""
		+"<emirates_visa>"+validateValue(applicantDetails_Map.get("Emirates_Visa"))+"</emirates_visa>" + ""
		+"<designation>"+validateValue(applicantDetails_Map.get("Designation"))+"</designation>" + ""
		//+"<emirates_work>DXB</emirates_work>" + ""
		+"<gender>"+validateValue(applicantDetails_Map.get("Gender_Code"))+"</gender>" + ""
		+"<cust_mobile_no>"+validateValue(applicantDetails_Map.get("MobileNo"))+"</cust_mobile_no>" + ""
		//+"<salary_with_rakbank>N</salary_with_rakbank>" + ""
		//+"<emirates_of_residence>DXB</emirates_of_residence>" + ""
		+"<emp_name>"+validateValue(applicantDetails_Map.get("Employer_Name"))+"</emp_name>" + ""
		+"<emp_code>"+validateValue(applicantDetails_Map.get("employercode"))+"</emp_code>" + ""
		+"<type_of_company>"+TYPE_OF_COMPANY+"</type_of_company>" + ""
	
		/*+"<NegatedDetails>" + ""    </target_segment_code>
		+"</NegatedDetails>" + ""
		+"<BlacklistDetails>" + ""
		+"</BlacklistDetails>" + ""*/
		//+"Str_NegatedDetails" + "" 
		//+"<NegatedDetails>" + ""
		//+"<negative_cust_type>I</negative_cust_type>" + ""
		//+"<internal_negative_flag>N</internal_negative_flag>" + ""
		//+"</NegatedDetails>" + ""
		//+"Str_BlacklistDetails" + ""
		/*+"<BlacklistDetails>" + ""
		+"<blacklist_cust_type>I</blacklist_cust_type>" + ""
		+"<internal_blacklist>N</internal_blacklist>" + ""
		+"</BlacklistDetails>" + ""*/
		//+"<cust_type>"+validateValue(applicantDetails_Map.get("Application_Type"))+"</cust_type>" + ""
		/*+"<bank_no_borrowing_relation_individual>0</bank_no_borrowing_relation_individual>" + ""
		+"<bank_no_borrowing_relation_company>0</bank_no_borrowing_relation_company>" + ""
		+"<AccountDetails>" + ""
		+"</AccountDetails>" + ""*/
		+"<industry_sector>"+industry_sector+"</industry_sector>" + ""
		+"<industry_macro>"+industry_macro+"</industry_macro>" + ""
		+"<industry_micro>"+industry_micro+"</industry_micro>" + ""
		/*+"<no_bank_other_statement_provided>3</no_bank_other_statement_provided>" + ""*/
		//+"<aggregate_exposed>25000.00</aggregate_exposed>" + ""
		//+"<bvr>N</bvr>" + ""
		+"<cc_employer_status>"+COMPANY_STATUS_CC+"</cc_employer_status>" + ""
		+"<pl_employer_status>"+COMPANY_STATUS_PL+"</pl_employer_status>" + ""
		+"<pl_employer_status_expat>"+EMPLOYER_CATEGORY_PL_EXPAT+"</pl_employer_status_expat>" + ""
		+"<pl_employer_status_national>"+EMPLOYER_CATEGORY_PL_NATIONAL+"</pl_employer_status_national>" + ""
		+"<included_pl_aloc>"+INCLUDED_IN_PL_ALOC+"</included_pl_aloc>" + ""
		+"<included_cc_aloc>"+INCLUDED_IN_CC_ALOC+"</included_cc_aloc>" + ""
		+"<visa_sponsor>"+validateValue(applicantDetails_Map.get("Visa_Sponsor_Name"))+"</visa_sponsor>" + ""
		+"<country_of_residence>AE</country_of_residence>" + ""
		+"<gcc_national>"+validateValue(applicantDetails_Map.get("GCC_National"))+"</gcc_national>" + ""
		+"<employer_type>N</employer_type>" + ""
		+"<aecb_consent>Y</aecb_consent>" + ""
		+"<No_of_dependants>"+validateValue(applicantDetails_Map.get("Dependents"))+"</No_of_dependants>" + ""
		+"<Other_household_income>"+validateValue(applicantDetails_Map.get("Earning_members"))+"</Other_household_income>" + ""
		+"<No_earning_members>"+validateValue(applicantDetails_Map.get("No_earning_members"))+"</No_earning_members>" + ""
		+"<EFR_NSTP>"+validateValue(applicantDetails_Map.get("EFR_NSTP"))+"</EFR_NSTP>" + ""

		+"<marketing_code>DIG</marketing_code>" + ""
		//+"<nmf_flag>N</nmf_flag>" + ""
		/*+"<eff_date_estba>2020-06-28</eff_date_estba>" + ""
		+"<eff_lob>2.05</eff_lob>" + ""
		+"<tlc_issue_date>2020-06-28</tlc_issue_date>" + ""
		+"<no_bank_statement>3</no_bank_statement>" + ""
		+"<no_of_partners>1</no_of_partners>" + ""*/
		//+"<standing_instruction>N</standing_instruction>" + ""
		//+"<vip_flag>"+validateValue(applicantDetails_Map.get("VIPFlag"))+"</vip_flag>" + ""
		//+"<title>"+validateValue(applicantDetails_Map.get("Title_Desc"))+"</title>" + ""
		
		//+"<customer_category>5</customer_category>" + ""
		+"</ApplicantDetails>" ;
	}      
	
	//Calculate DOJ formate should be YYYY-MM-DD
	public static Double CalculatLOS(String DOJ_Str) {
		Double LOS = 0.00;
		try {
			Integer year = Integer.parseInt(DOJ_Str.split("-")[0]);
			Integer month = Integer.parseInt(DOJ_Str.split("-")[1]);
			Integer day = Integer.parseInt(DOJ_Str.split("-")[2]);
			LocalDate DOJ = LocalDate.of(year,month,day);
			LocalDate CD = LocalDate.now();
			Period p = Period.between(DOJ, CD);
			System.out.println(p.getMonths());
			System.out.println(p.getYears());
			LOS += p.getYears();
			LOS = LOS + p.getMonths()/100d;
		} catch (Exception e) {
			e.printStackTrace();
			return LOS;
		}
		
		System.out.println(LOS);
		return LOS;
	}
	
	public static String sInputXmlInternalBureau(Map<String, String> applicantDetails_Map) {
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
		      +"<basic>"+validateValue(applicantDetails_Map.get("Cust_Decl_Salary"))+"</basic>"+""
		      +"<gross_salary>"+validateValue(applicantDetails_Map.get("Cust_Decl_Salary"))+"</gross_salary>"+""
		      +"<net_salary_mon1>"+validateValue(applicantDetails_Map.get("Net_Salary1"))+"</net_salary_mon1>"+""
		      +"<net_salary_mon2>"+validateValue(applicantDetails_Map.get("Net_Salary2"))+"</net_salary_mon2>"+""
		      +"<net_salary_mon3>"+validateValue(applicantDetails_Map.get("Net_Salary3"))+"</net_salary_mon3>"+""
		    +"</InternalBureauDBRTAICalc>"+""
		  +"</InternalBureauData>";
		DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("internal_bureau: "+internal_bureau);
		  return internal_bureau;
	}
	
	private static String sInputXmlExternalBureau(Map<String, String> applicantDetails_Map, String cabinetName, String sessionID, String jtsIP, String jtsPort) 
	{
		String Wi_Name = applicantDetails_Map.get("Wi_Name");
		
		DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("inside ExternalBureauData : ");
		String sQuery = "select top 1 CifId, fullnm,TotalOutstanding,TotalOverdue,NoOfContracts,Total_Exposure,WorstCurrentPaymentDelay,"
				+ "Worst_PaymentDelay_Last24Months,Worst_Status_Last24Months,Nof_Records,NoOf_Cheque_Return_Last3,Nof_DDES_Return_Last3Months,"
				+ "Nof_Cheque_Return_Last6,DPD30_Last6Months,(select max(ExternalWriteOffCheck) ExternalWriteOffCheck "
				+ "from ((select convert(int,isNULL(ExternalWriteOffCheck,0)) ExternalWriteOffCheck  from ng_dcc_cust_extexpo_CardDetails with(nolock) "
				+ "where Wi_Name  = '"+Wi_Name+"' and ProviderNo!='B01'  "
				
				+ "union all select convert(int,isNULL(ExternalWriteOffCheck,0)) ExternalWriteOffCheck "
				+ "from ng_dcc_cust_extexpo_LoanDetails where Wi_Name  = '"+Wi_Name+"' and ProviderNo!='B01' "
				
				+ "union all select convert(int,isNULL(ExternalWriteOffCheck,0)) ExternalWriteOffCheck from ng_dcc_cust_extexpo_AccountDetails "
				+ "where Wi_Name = '"+Wi_Name+"' and ProviderNo!='B01')) as ExternalWriteOffCheck) as 'ExternalWriteOffCheck' ,(select count(*) "
				+ "from (select DisputeAlert from ng_dcc_cust_extexpo_LoanDetails with(nolock) where Wi_Name = '"+Wi_Name+"' and DisputeAlert='1' "
				
				+ "union select DisputeAlert from ng_dcc_cust_extexpo_CardDetails with(nolock) where Wi_Name = '"+Wi_Name+"' and DisputeAlert='1') "
				+ "as tempTable) as 'DisputeAlert'  from ng_dcc_cust_extexpo_Derived with (nolock) where Wi_Name  = '"+Wi_Name+"' and Request_type= 'ExternalExposure'";
		
		DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("ExternalBureauData sQuery" + sQuery+ "");
		String AecbHistQuery = "select isnull(max(AECBHistMonthCnt),0) as AECBHistMonthCnt from ( select MAX(cast(isnull(AECBHistMonthCnt,'0') as int)) as AECBHistMonthCnt  "
						+ "from ng_dcc_cust_extexpo_CardDetails with (nolock) where  Wi_Name  = '"+ Wi_Name + "' and cardtype not in ( '85','99','Communication Services',"
						+ "'TelCo-Mobile Prepaid','101','Current/Saving Account with negative Balance','58','Overdraft') and custroletype not in ('Co-Contract Holder','Guarantor') "
						
						+ "union all select Max(cast(isnull(AECBHistMonthCnt,'0') as int)) as AECBHistMonthCnt from ng_dcc_cust_extexpo_LoanDetails with (nolock) "
						+ "where Wi_Name  = '"+ Wi_Name + "' and loantype not in ('85','99','Communication Services','TelCo-Mobile Prepaid','101',"
						+ "'Current/Saving Account with negative Balance','58','Overdraft') and custroletype not in ('Co-Contract Holder','Guarantor')) as ext_expo";
		
		String add_xml_str = "";
		try {
			
			List<Map<String,String>> OutputXML = getDataFromDBMap(sQuery, cabinetName, sessionID, jtsIP, jtsPort);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("ExternalBureauData list size" + OutputXML.size()+ "");
				
			List<Map<String,String>> AecbHistMap = getDataFromDBMap(AecbHistQuery, cabinetName, sessionID, jtsIP, jtsPort);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("ExternalBureauData list size" + AecbHistMap.size()+ "");
			
			if (OutputXML.size() == 0)
			{
				String aecb_score="";
				String range ="";
				String refNo ="";
				String query = "select top 1 ReferenceNo, AECB_Score,Range from ng_dcc_cust_extexpo_Derived with(nolock) where Wi_Name ='"+Wi_Name
						+"' and Request_Type='ExternalExposure' ORDER BY enquiryDate desc"  ;
				try {
					List<Map<String,String>> OutputXML_ref = getDataFromDBMap(query, cabinetName, sessionID, jtsIP, jtsPort);
					DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Inside externalBureauData OutputXML_ref: "+ query);
					DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Inside externalBureauData OutputXML_ref: "+ OutputXML_ref);
					if(OutputXML_ref.size()>0)
					{
						refNo=OutputXML_ref.get(0).get("ReferenceNo");
						aecb_score=OutputXML_ref.get(0).get("AECB_Score");
						range=OutputXML_ref.get(0).get("Range");
					}				
				}
				catch(Exception e)
				{
					DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug(" Exception occurred in externalBureauData Query"+ query);
					DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug(" Exception occurred in externalBureauData()"+ e.getMessage());
				}
				
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug( "aecb_score :"+aecb_score+" range :: "+range+" refNo:: "+refNo);
				
				add_xml_str +="<ExternalBureau>" + "";
				add_xml_str +="<applicant_id>" + validateValue(applicantDetails_Map.get("Wi_Name")) + "</applicant_id>" + "";
				add_xml_str +="<bureauone_ref_no>"+refNo+"</bureauone_ref_no>" + "";
				add_xml_str +="<full_name>" + validateValue(applicantDetails_Map.get("FirstName")) +" "+ validateValue(applicantDetails_Map.get("LastName")) + "</full_name>" + ""; //, MiddleName, 
				add_xml_str +="<total_out_bal></total_out_bal>" + "";

				add_xml_str +="<total_overdue></total_overdue>" + "";
				add_xml_str +="<no_default_contract></no_default_contract>" + "";
				add_xml_str +="<total_exposure></total_exposure>" + "";
				add_xml_str +="<worst_curr_pay></worst_curr_pay>" + "";
				add_xml_str +="<worst_curr_pay_24></worst_curr_pay_24>" + "";
				//add_xml_str +="<worst_status_24></worst_status_24>" + "";

				add_xml_str +="<no_of_rec></no_of_rec>" + "";
				add_xml_str +="<cheque_return_3mon></cheque_return_3mon>" + "";
				add_xml_str +="<dds_return_3mon></dds_return_3mon>" + "";
				//add_xml_str +="<cheque_return_6mon>" + Nof_Cheque_Return_Last6 + "</cheque_return_6mon>" + "";
				//add_xml_str +="<dds_return_6mon>" + DPD30_Last6Months + "</dds_return_6mon>" + "";
				//add_xml_str +="<prod_external_writeoff_amount>" + "" + "</prod_external_writeoff_amount>" + "";

				add_xml_str +="<no_months_aecb_history>" + AecbHistMap.get(0).get("AECBHistMonthCnt") + "</no_months_aecb_history>" + "";
				//changes done by shivang for 2.1 
				add_xml_str +="<aecb_score>"+aecb_score+"</aecb_score>" + "";
				add_xml_str +="<range>"+range+"</range>" + "";
				add_xml_str +="<company_flag>N</company_flag></ExternalBureau>" + "";

				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("dectech External : " + add_xml_str);
				return add_xml_str;
			} 
			else {
				for (Map<String,String> map : OutputXML){
					//String CifId = validateValue(map.get("CifId"));
					String fullnm = validateValue(map.get("fullnm"));
					String TotalOutstanding = validateValue(map.get("TotalOutstanding"));
					String TotalOverdue = validateValue(map.get("TotalOverdue"));
					String NoOfContracts = validateValue(map.get("NoOfContracts"));
					String Total_Exposure = validateValue(map.get("Total_Exposure"));
					String WorstCurrentPaymentDelay = validateValue(map.get("WorstCurrentPaymentDelay"));
					String Worst_PaymentDelay_Last24Months = validateValue(map.get("Worst_PaymentDelay_Last24Months"));
					String Worst_Status_Last24Months = validateValue(map.get("Worst_Status_Last24Months"));
					String Nof_Records = validateValue(map.get("Nof_Records"));
					String NoOf_Cheque_Return_Last3 = validateValue(map.get("NoOf_Cheque_Return_Last3"));
					String Nof_DDES_Return_Last3Months = validateValue(map.get("Nof_DDES_Return_Last3Months"));
					String Nof_Cheque_Return_Last6 = validateValue(map.get("Nof_Cheque_Return_Last6"));
					String DPD30_Last6Months = validateValue(map.get("DPD30_Last6Months"));
					//String ExternalWriteOffCheck = validateValue(map.get("ExternalWriteOffCheck"));
					String dispute_alert=validateValue(map.get("tempTable"));
					
					String aecb_score=""; 
					String range =""; 
					String refNo ="";
					String EnquiryDate="";
					DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug( "aecb_score :"+aecb_score+" range :: "+range+" refNo:: "+refNo);
					 
					if (!dispute_alert.equals("")) {
						try {
							if (Integer.parseInt(dispute_alert) > 0) {
								dispute_alert = "Y";
							} else {
								dispute_alert = "N";
							}
						} catch (NumberFormatException e) {
							dispute_alert = "N";
							DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.error( "NumberFormatException : "+e.getMessage());
						}
					} else {
						dispute_alert = "N";
					}
					
					//String Company_flag = "N";
					String Ref_query = "select ReferenceNo, AECB_Score,Range, EnquiryDate from ng_dcc_cust_extexpo_Derived with(nolock) where Wi_Name ='"+Wi_Name+"' and Request_Type='ExternalExposure'";
					try {
						List<Map<String,String>> OutputXML_ref = getDataFromDBMap(Ref_query, cabinetName, sessionID, jtsIP, jtsPort);
						if(OutputXML_ref.size()>0)
						{
							refNo=OutputXML_ref.get(0).get("ReferenceNo");
							aecb_score=OutputXML_ref.get(0).get("AECB_Score");
							range=OutputXML_ref.get(0).get("Range");
							EnquiryDate=OutputXML_ref.get(0).get("EnquiryDate");
							EnquiryDate=getEnquiryDate(EnquiryDate);
						}				
					}
					catch(Exception e)
					{
						DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug(" Exception occurred in externalBureauData Query"+ Ref_query);
						DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug(" Exception occurred in externalBureauData()"+ e.getMessage());
					}
					
					add_xml_str +="<ExternalBureau>" + "";
					add_xml_str +="<applicant_id>" + validateValue(applicantDetails_Map.get("Wi_Name"))+ "</applicant_id>" + "";
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
					//add_xml_str = add_xml_str+ "<prod_external_writeoff_amount>" +ExternalWriteOffCheck+ "</prod_external_writeoff_amount>" + "";

					add_xml_str +="<no_months_aecb_history>"+ AecbHistMap.get(0).get("AECBHistMonthCnt")+ "</no_months_aecb_history>" + "";

					add_xml_str +="<aecb_score>"+aecb_score+"</aecb_score>" + "";
					add_xml_str +="<range>"+range+"</range>" + "";
					//add_xml_str +="<esr>"++"</esr>" + ""; //TODO New Field to be added -CR for STP
					add_xml_str +="<AECB_Enquiry_date>"+EnquiryDate+"</AECB_Enquiry_date>" + ""; 

					add_xml_str +="<company_flag>N</company_flag>" + "";
					add_xml_str +="<dispute_alert>"+dispute_alert+"</dispute_alert></ExternalBureau>";

				}
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("RLOSCommon"+"Internal liab tag Cration: " + add_xml_str);
				return add_xml_str;
			}
		}

		catch (Exception e) {
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("DECTECH Exception occurred in externalBureauData()"+ e.getMessage() + " Error: "+ e.getMessage());
			return null;
		}
	}
	
	private static String getEnquiryDate(String EnquiryDate) {
		if (validateValue(EnquiryDate).equals(""))
			return "";
		
		try {
			Date parseDateCC = getParseDate(EnquiryDate);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			EnquiryDate = formatter.format(parseDateCC);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Date : " + EnquiryDate);
			return EnquiryDate;
		}
		catch(Exception e){
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("Exception occured in conversion of AECB Enquiry Date :" + e.getMessage());
			return "";
		}
	}

	private static String getMaximumOverdueDate(String overDueDate) {
		 if (validateValue(overDueDate).equals(""))
			 return "";
		
		try {
			Date parseDateCC = getParseDate(overDueDate);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			overDueDate = formatter.format(parseDateCC);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("MaxOverDueAmountDate : " + overDueDate);
			return overDueDate;
		}
		catch(Exception e){
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("Exception occured in conversion of MaxOverDueAmountDate :" + e.getMessage());
			return "";
		}
	}

	private static Date getParseDate(String parseDate) throws ParseException {
		if (parseDate.matches("([0-9]{2})/([0-9]{2})/([0-9]{4})")) {
			return new SimpleDateFormat("dd/MM/yyyy").parse(parseDate);
		} else if (parseDate.matches("([0-9]{2})-([0-9]{2})-([0-9]{4})")) {
			return new SimpleDateFormat("dd-MM-yyyy").parse(parseDate);
		}
		else if (parseDate.matches("([0-9]{4})/([0-9]{2})/([0-9]{2})")) {
			return new SimpleDateFormat("yyyy/MM/dd").parse(parseDate);
		} else {
			return new SimpleDateFormat("yyyy-MM-dd").parse(parseDate);
		}
	}
	
	private static  String sInputXmlExternalCourtCase(String Wi_Name, String cabinetName, String sessionID, String jtsIP, String jtsPort) {
		String court_cases = "";
		String QueryCaseDetails ="select CodOrganization, ProviderCaseNo, ReferenceDate, CaseCategoryCode,CaseOpenDate, isnull(CaseCloseDate,'') as CaseCloseDate, CaseStatusCode," +
				"InitialTotalClaimAmount from ng_dcc_cust_extexpo_CaseDetails where Wi_Name='"+Wi_Name+"'";
		DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Select ng_dcc_cust_extexpo_CaseDetails Query: "+QueryCaseDetails);
		List<Map<String,String>> list_map = getDataFromDBMap(QueryCaseDetails, cabinetName, sessionID, jtsIP, jtsPort);
		DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Total Retrieved Records: " + list_map.size());
		System.out.println("Total Retrieved Records: " + list_map.size());
		for (Map<String,String> map : list_map) {
			court_cases += "<CourtCase>"+ ""
			+"<CodOrganization>"+validateValue(map.get("CodOrganization"))+"</CodOrganization>"+ ""
			+"<ProviderCaseNo>"+validateValue(map.get("ProviderCaseNo"))+"</ProviderCaseNo>"+ ""
			+"<ReferenceDate>"+validateValue(map.get("ReferenceDate"))+"</ReferenceDate>"+ ""
			+"<CaseCategoryCode>"+validateValue(map.get("CaseCategoryCode"))+"</CaseCategoryCode>"+ ""
			+"<OpenDate>"+validateValue(map.get("CaseOpenDate"))+"</OpenDate>"+ ""
			+"<CloseDate>"+validateValue(map.get("CaseCloseDate"))+"</CloseDate>"+ ""
			+"<CaseStatusCode>"+validateValue(map.get("CaseStatusCode"))+"</CaseStatusCode>"+ ""
			+"<InitialTotalClaimAmount>"+validateValue(map.get("InitialTotalClaimAmount"))+"</InitialTotalClaimAmount>"+ ""
			+"</CourtCase>";
		}
		return court_cases;
	}
	//Deepak change for ExternalBureauAccountDetails from AECB to Dectech
	private static  String sInputXmlExternalBureauAccountDetails(String Wi_Name, String cabinetName, String sessionID, String jtsIP, String jtsPort) {
		String ExternalBureauAccountDetails = "";
		try{
			String QueryCaseDetails ="select AccountType,Phase,IBAN,CBAccountNo,DPAccountNo,ProviderNo,StartDate,CloseDate,DateOfLastUpdate from ng_dcc_cust_extexpo_SalCreditDtls with(nolock) where Wi_Name= '"+Wi_Name+"'";
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Select BureauAccountDetails Query: "+QueryCaseDetails);
			List<Map<String,String>> list_map = getDataFromDBMap(QueryCaseDetails, cabinetName, sessionID, jtsIP, jtsPort);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Total Retrieved Records: " + list_map.size());
			System.out.println("Total Retrieved Records: " + list_map.size());
			for (Map<String,String> map : list_map) {
				ExternalBureauAccountDetails += "<ExternalBureauAccountDetails>"+ ""
						+"<applicant_id>"+Wi_Name+"</applicant_id>"+ ""
						+"<Account_Type>"+validateValue(map.get("AccountType"))+"</Account_Type>"+ ""
						+"<Phase>"+validateValue(map.get("Phase"))+"</Phase>"+ ""
						+"<IBAN>"+validateValue(map.get("IBAN"))+"</IBAN>"+ ""
						+"<CBAccountNo>"+validateValue(map.get("CBAccountNo"))+"</CBAccountNo>"+ ""
						+"<DPAccountNo>"+validateValue(map.get("DPAccountNo"))+"</DPAccountNo>"+ ""
						+"<Provider_No>"+validateValue(map.get("ProviderNo"))+"</Provider_No>"+ ""
						+"<Start_Date>"+validateValue(map.get("StartDate"))+"</Start_Date>"+ ""
						+"<Closed_Date>"+validateValue(map.get("CloseDate"))+"</Closed_Date>"+ ""
						+"<Date_Of_Last_Update>"+validateValue(map.get("DateOfLastUpdate"))+"</Date_Of_Last_Update>"+ ""
						+"</ExternalBureauAccountDetails>";
			}
		}
		catch(Exception e){
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Exception occured while extracting ExternalBureauAccountDetails: "+ e.getMessage());
		}
		return ExternalBureauAccountDetails;
	}
	//Deepak change for ExternalBureauSalaryDetails from AECB to Dectech 
	private static  String sInputXmlExternalBureauSalaryDetails(String Wi_Name, String cabinetName, String sessionID, String jtsIP, String jtsPort) {
		String ExternalBureauSalaryDetails = "";
		try{
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Inside sInputXmlExternalBureauSalaryDetails");
			String QueryCaseDetails ="select CBAccountNo, TotalSalaryAmount, substring(ReferenceDate,0,CHARINDEX('-',ReferenceDate)) as year,"
					+ " substring(ReferenceDate, CHARINDEX('-',ReferenceDate)+1,LEN(ReferenceDate)) as month, NumberOfSalariesTransferred from ng_dcc_cust_extexpo_SalCreditHis with(nolock) where Wi_Name='"+Wi_Name+"'";
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Select SalaryDetails Query: "+QueryCaseDetails);
			List<Map<String,String>> list_map = getDataFromDBMap(QueryCaseDetails, cabinetName, sessionID, jtsIP, jtsPort);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Total Retrieved Records: " + list_map.size());
			System.out.println("Total Retrieved Records: " + list_map.size());
			for (Map<String,String> map : list_map) {
				ExternalBureauSalaryDetails += "<ExternalBureauSalaryDetails>"+ ""
						+"<applicant_id>"+Wi_Name+"</applicant_id>"+ ""
						+"<CBAccountNo>"+validateValue(map.get("CBAccountNo"))+"</CBAccountNo>"+ ""
						+"<Year>"+validateValue(map.get("year"))+"</Year>"+ ""
						+"<Month>"+validateValue(map.get("month"))+"</Month>"+ ""
						+"<Total_Salary_Amount>"+validateValue(map.get("TotalSalaryAmount"))+"</Total_Salary_Amount>"+ ""
						+"<No_Of_Salary_Transferred>"+validateValue(map.get("NumberOfSalariesTransferred"))+"</No_Of_Salary_Transferred>"+ ""
						+"</ExternalBureauSalaryDetails>";
			}
		}
		catch(Exception e){
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Exception occured while extracting ExternalBureauSalaryDetails: "+ e.getMessage());
		}
		
		return ExternalBureauSalaryDetails;
	}
	
	private static  String sInputXmlExternalBouncedCheques(String wiName, String cabinetName, String sessionID, String jtsIP, String jtsPort) {
		DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("RLOSCommon java file"+"inside ExternalBouncedCheques : ");
		String sQuery = "SELECT CifId,ChqType,number,amount,reasoncode,returndate,providerno FROM ng_dcc_cust_extexpo_ChequeDetails  with (nolock) "
				+ "where Wi_Name = '" + wiName + "' and Request_Type = 'ExternalExposure'";
		
		DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("ExternalBouncedCheques sQuery" + sQuery+ "");
		String add_xml_str = "";
			
		List<Map<String,String>> OutputXML = getDataFromDBMap(sQuery, cabinetName, sessionID, jtsIP, jtsPort);
		DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("ExternalBouncedCheques list size" + OutputXML.size()+ "");

		for (Map<String,String> map : OutputXML) {
			add_xml_str +="<ExternalBouncedCheques><applicant_id>" + wiName + "</applicant_id>"+ "";
			//add_xml_str +="<external_bounced_cheques_id></external_bounced_cheques_id>"+ "";
			add_xml_str +="<bounced_cheque>" + validateValue(map.get("ChqType")) + "</bounced_cheque>"+ "";
			add_xml_str +="<cheque_no>" + validateValue(map.get("number")) + "</cheque_no>"+ "";
			add_xml_str +="<amount>" + validateValue(map.get("amount")) + "</amount>"+ "";
			add_xml_str +="<reason>" + validateValue(map.get("reasoncode")) + "</reason>"+ "";
			add_xml_str +="<return_date>" + validateValue(map.get("returndate")) + "</return_date>"+ "";
			add_xml_str +="<provider_no>" + validateValue(map.get("providerno")) + "</provider_no><company_flag>N</company_flag></ExternalBouncedCheques>"; // to
		}
		DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("RLOSCommon"+ "Internal liab tag Cration: "+ add_xml_str);
		return add_xml_str;
	}
	
		
	private static String sInputXmlExternalUtilization(String wiName, String cabinetName, String sessionID, String jtsIP, String jtsPort)
	{
		//Deepak - 2March23 Changes done for JIRA PDSC-281 Do not send monthly utilization for loan contracts from AECB; HD 3790532
		/*
		String sQuery = "select CardEmbossNum, Utilizations24Months as UtilizationsMonths from ng_dcc_cust_extexpo_CardDetails where Wi_Name='" + wiName + "' and (History is not null or History != '') "
				+ "union all select AgreementId, Utilizations24Months as UtilizationsMonths from ng_dcc_cust_extexpo_LoanDetails where Wi_Name='" + wiName + "' and (History is not null or History != '')";
		*/
		//Deepak 28Sept2023 Changes done to exclude History!='' as history is ntext and !='' dosen't work on that.
		//String sQuery = "select CardEmbossNum, Utilizations24Months as UtilizationsMonths from ng_dcc_cust_extexpo_CardDetails where Wi_Name='" + wiName + "' and (History is not null or History != '') ";
		String sQuery = "select CardEmbossNum, Utilizations24Months as UtilizationsMonths from ng_dcc_cust_extexpo_CardDetails where Wi_Name='" + wiName + "' and (History is not null) ";		
		
		String add_xml_str = "";

		try {
			String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(sQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger, false));
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("extTabDataIPXML: " + extTabDataIPXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("extTabDataOPXML: " + extTabDataOPXML);

			XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
			int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));

			if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0) {
				String xmlDataExtTab = xmlParserData.getNextValueOf("Record");
				xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

				NGXmlList objWorkList = xmlParserData.createList("Records", "Record");
				String Utilizations24Months = "";
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
					String agreementID = validateValue(objWorkList.getVal("CardEmbossNum"));
					String UtilizationTag = validateValue(objWorkList.getVal("UtilizationsMonths"));

					UtilizationTag = UtilizationTag.replaceAll("Utilizations24Months", "Month_Utilization");
					Utilizations24Months += UtilizationTag.replaceAll("<Month_Utilization>", "<Month_Utilization><CB_application_id>" + agreementID + "</CB_application_id>");
				}
				
				if (!Utilizations24Months.equals(""))
					add_xml_str = add_xml_str + "<Utilization24months>" + Utilizations24Months + "</Utilization24months>";
			}
		} catch (Exception e) {
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Utilization24months Exception : " + e.getMessage());
			e.printStackTrace();
		}
		DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Utilization24months : " + add_xml_str);
		return add_xml_str;
	}

	private static String sInputXmlExternalHistory(String wiName, String cabinetName, String sessionID, String jtsIP, String jtsPort) {
		//Deepak 28Sept2023 Changes done to exclude History!='' as history is ntext and !='' dosen't work on that.
/*		String sQuery = "select CardEmbossNum, history as extHistory from ng_dcc_cust_extexpo_CardDetails where Wi_Name='" + wiName + "' and (History is not null or History!='') "
				+ "union all select AgreementId, history as extHistory from ng_dcc_cust_extexpo_LoanDetails where Wi_Name='" + wiName + "' and (History is not null or History!='')";
*/
		String sQuery = "select CardEmbossNum, history as extHistory from ng_dcc_cust_extexpo_CardDetails where Wi_Name='" + wiName + "' and (History is not null) "
				+ "union all select AgreementId, history as extHistory from ng_dcc_cust_extexpo_LoanDetails where Wi_Name='" + wiName + "' and (History is not null)";

		String add_xml_str = "";
		try {
			String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(sQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger, false));
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("extTabDataIPXML: " + extTabDataIPXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("extTabDataOPXML: " + extTabDataOPXML);

			XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
			int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));

			if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0) {
				String xmlDataExtTab = xmlParserData.getNextValueOf("Record");
				xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

				NGXmlList objWorkList = xmlParserData.createList("Records", "Record");
				String history = "";
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
					String agreementID = validateValue(objWorkList.getVal("CardEmbossNum"));
					String HistoryTag = validateValue(objWorkList.getVal("extHistory"));
					HistoryTag = HistoryTag.replaceAll("Key", "monthyear");

					history += HistoryTag.replaceAll("<History>", "<History><CB_application_id>" + agreementID + "</CB_application_id>");
				}
				
				if (!history.equals(""))
					add_xml_str = add_xml_str + "<History_24months>" + history + "</History_24months>";
			}
		} catch (Exception e) {
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("History_24months Exception : " + e.getMessage());
			e.printStackTrace();
		}
		DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("History_24months : " + add_xml_str);
		return add_xml_str;
	}

	private static  String sInputXmlExternalBureauIndividualProducts(String wiName, String cabinetName, String sessionID, String jtsIP, String jtsPort) {
		//DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("RLOSCommon java file"+"inside ExternalBureauIndividualProducts : ");
		String sQuery = "select CifId,AgreementId,LoanType,ProviderNo,LoanStat,CustRoleType,LoanApprovedDate,LoanMaturityDate,OutstandingAmt,TotalAmt,PaymentsAmt,"
				+ "TotalNoOfInstalments,RemainingInstalments,WriteoffStat,WriteoffStatDt,CreditLimit,OverdueAmt,NofDaysPmtDelay,MonthsOnBook,lastrepmtdt,IsCurrent,"
				+ "CurUtilRate,DPD30_Last6Months,DPD60_Last12Months,AECBHistMonthCnt,DPD5_Last3Months,'' as qc_Amnt,'' as Qc_emi,'' as Cac_indicator,Take_Over_Indicator,"
				+ "Consider_For_Obligations, case when IsDuplicate= '1' then 'Y' else 'N' end AS IsDuplicate,avg_utilization,DPD5_Last12Months,DPD60Plus_Last12Months,MaximumOverDueAmount,"
				+ "Pmtfreq, MaxOverDueAmountDate from ng_dcc_cust_extexpo_LoanDetails with (nolock) where Wi_Name= '"+ wiName + "'  and LoanStat != 'Pipeline' "
				
		+ "union select CifId,CardEmbossNum,CardType,ProviderNo,CardStatus,CustRoleType,StartDate,ClosedDate,CurrentBalance,'' as col6,"
		+ "PaymentsAmount,NoOfInstallments,'' as col5,WriteoffStat,WriteoffStatDt,CashLimit as CreditLimit ,OverdueAmount,NofDaysPmtDelay,MonthsOnBook,lastrepmtdt,IsCurrent,CurUtilRate,"
		+ "DPD30_Last6Months,DPD60_Last12Months,AECBHistMonthCnt,DPD5_Last3Months,qc_amt,qc_emi,CAC_Indicator,Take_Over_Indicator,Consider_For_Obligations,case when "
		+ "IsDuplicate= '1' then 'Y' else 'N' end AS IsDuplicate,avg_utilization,DPD5_Last12Months,DPD60Plus_Last12Months,MaximumOverDueAmount,Pmtfreq, MaxOverDueAmountDate from "
		+ "ng_dcc_cust_extexpo_CardDetails with (nolock) where Wi_Name = '" + wiName+ "' and cardstatus != 'Pipeline'   "
		
		+ "union select CifId,AcctId,AcctType,ProviderNo,AcctStat,CustRoleType,StartDate,ClosedDate,OutStandingBalance,TotalAmount,PaymentsAmount,'','',"
		+ "WriteoffStat,WriteoffStatDt,CreditLimit,OverdueAmount,"
		+ "NofDaysPmtDelay,MonthsOnBook,'',IsCurrent,CurUtilRate,DPD30_Last6Months,DPD60_Last12Months,AECBHistMonthCnt,DPD5_Last3Months,'','','','',"
		+ "isnull(Consider_For_Obligations,'true'),case when IsDuplicate= '1' then 'Y' else 'N' end AS IsDuplicate,'',DPD5_Last12Months,DPD60Plus_Last12Months,"
		+ "MaximumOverDueAmount,Pmtfreq, MaxOverDueAmountDate from ng_dcc_cust_extexpo_AccountDetails with (nolock)  where Wi_Name  =  '"+wiName+"' "
		
		+ "union select CifId,ServiceID,ServiceType,ProviderNo,ServiceStat,CustRoleType,SubscriptionDt,SvcExpDt,OverDueAmount,'','','','',WriteoffStat,WriteoffStatDt,'',OverDueAmount,"
		+ "NofDaysPmtDelay,MonthsOnBook,lastrepmtdt,IsCurrent,CurUtilRate,DPD30_Last6Months,'',AECBHistMonthCnt,DPD5_Last3Months,'','','','',isnull(Consider_For_Obligations,'true')"
		+ ",case when IsDuplicate= '1' then 'Y' else 'N' end AS IsDuplicate,'',DPD5_Last12Months,DPD60Plus_Last12Months,MaximumOverDueAmount,'',MaxOverDueAmountDate from ng_dcc_cust_extexpo_ServicesDetails with (nolock)  "
		+ "where ServiceStat='Active' and wi_name  =  '"+wiName+"'";
		
		
        
        

		
		String add_xml_str = "";
		List<Map<String,String>> OutputXML = getDataFromDBMap(sQuery, cabinetName, sessionID, jtsIP, jtsPort);
		DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("ExternalBureauIndividualProducts list size"+ OutputXML.size()+ "");
			
		for (Map<String,String> map : OutputXML){
			
			String ContractType = validateValue(map.get("LoanType"));
			String AgreementId = validateValue(map.get("AgreementId"));
			String phase = validateValue(map.get("LoanStat"));
			String CustRoleType = validateValue(map.get("CustRoleType"));
			String start_date = validateValue(map.get("LoanApprovedDate"));
			String close_date = validateValue(map.get("LoanMaturityDate"));
			String OutStanding_Balance = validateValue(map.get("OutstandingAmt"));
			String TotalAmt = validateValue(map.get("TotalAmt"));
			String PaymentsAmt = validateValue(map.get("PaymentsAmt"));
			String TotalNoOfInstalments = validateValue(map.get("TotalNoOfInstalments"));
			String RemainingInstalments = validateValue(map.get("RemainingInstalments"));
			String WorstStatus = validateValue(map.get("WriteoffStat"));
			String WorstStatusDate = validateValue(map.get("WriteoffStatDt"));
			String CreditLimit = validateValue(map.get("CreditLimit"));
			String OverdueAmt = validateValue(map.get("OverdueAmt"));
			String NofDaysPmtDelay = validateValue(map.get("NofDaysPmtDelay"));
			String MonthsOnBook = validateValue(map.get("MonthsOnBook"));
			String last_repayment_date = validateValue(map.get("lastrepmtdt"));
			//String DPD60Last12Months = validateValue(map.get("DPD60_Last12Months"));
			String AECBHistMonthCnt = validateValue(map.get("AECBHistMonthCnt"));
			String DPD30Last6Months = validateValue(map.get("DPD30_Last6Months"));
			String currently_current = validateValue(map.get("IsCurrent"));
			String current_utilization = validateValue(map.get("CurUtilRate"));
			String delinquent_in_last_3months = validateValue(map.get("DPD5_Last3Months"));
			//String QC_Amt = validateValue(map.get("qc_Amnt"));
			//String QC_emi = validateValue(map.get("Qc_emi"));
			String CAC_Indicator = validateValue(map.get("Cac_indicator"));
			String TakeOverIndicator = validateValue(map.get("Take_Over_Indicator"));
			String consider_for_obligation = validateValue(map.get("Consider_For_Obligations"));
			String Duplicate_flag=validateValue(map.get("IsDuplicate"));
			//String avg_utilization=validateValue(map.get("avg_utilization"));
			String DPD60plus_last12month=validateValue(map.get("DPD60Plus_Last12Months"));
			String DPD5_last12month=validateValue(map.get("DPD5_Last12Months"));
			String MaximumOverDueAmount = validateValue(map.get("MaximumOverDueAmount"));  
			String Pmtfreq = validateValue(map.get("Pmtfreq"));
			String MaxOverDueAmountDate = validateValue(map.get("MaxOverDueAmountDate"));
			
			if (!ContractType.equals("")) {
				try {
					String cardquery = "select code from ng_master_contract_type with (nolock) where description='"+ ContractType + "'";
					DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("ExternalBureauIndividualProducts sQuery"+ cardquery+ "");
					Map<String, String> cardqueryXML = getDataFromDB(cardquery, cabinetName, sessionID, jtsIP, jtsPort, "code");
					ContractType = cardqueryXML.get("code");
					DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("ExternalBureauIndividualProducts ContractType"+ ContractType+ "ContractType");
				} catch (Exception e) {
					DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("ExternalBureauIndividualProducts ContractType Exception"+ e+ "Exception");
				}
			}
			
			phase = phase.startsWith("A") ? "A" : "C";
			
			if (!CustRoleType.equals("")) {
				String sQueryCustRoleType = "select code from ng_master_role_of_customer with(nolock) where Description='"+CustRoleType+"'";
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("CustRoleType"+sQueryCustRoleType);
				Map<String, String> cardqueryXML = getDataFromDB(sQueryCustRoleType, cabinetName, sessionID, jtsIP, jtsPort, "code");
				try {
					if (cardqueryXML != null && cardqueryXML.size() > 0 && cardqueryXML.get("code") != null) {
						CustRoleType = cardqueryXML.get("code");
					}
				}
				catch(Exception e){
					DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("Exception occured at sQueryCombinedLimit for"+sQueryCustRoleType);
				}	
			}

			CAC_Indicator = "true".equalsIgnoreCase(CAC_Indicator) ? "Y" : "N";
			
			TakeOverIndicator = "true".equalsIgnoreCase(TakeOverIndicator) ? "Y" : "N";
			
			consider_for_obligation = "true".equalsIgnoreCase(consider_for_obligation) ? "Y" : "N";
			
			//Always N because of salaried person
			//String Company_flag="N";

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
			add_xml_str +="<maximum_overdue_amount>" + MaximumOverDueAmount + "</maximum_overdue_amount>"+ "";// added by deppanshu
			add_xml_str +="<delinquent_in_last_3months>" + delinquent_in_last_3months + "</delinquent_in_last_3months>"+ "";
			//add_xml_str +="<clean_funded>" + "" + "</clean_funded>"+ "";
			//add_xml_str +="<cac_indicator>" + CAC_Indicator + "</cac_indicator>"+ "";
			//add_xml_str +="<qc_emi>" + QC_emi + "</qc_emi>"+ "";
			//add_xml_str +="<qc_amount>" + QC_Amt + "</qc_amount>">"+ "";
			add_xml_str +="<company_flag>N</company_flag>"+ "";
			//add_xml_str +="<cac_bank_name>" + CAC_BANK_NAME+ "</cac_bank_name>"+ "";
			//add_xml_str +="<take_over_indicator>" + TakeOverIndicator + "</take_over_indicator>"+ "";
			add_xml_str +="<consider_for_obligation>Y</consider_for_obligation>"+ "";
			add_xml_str +="<duplicate_flag>"+Duplicate_flag+"</duplicate_flag>"+ "";
			//add_xml_str +="<avg_utilization>"+avg_utilization+"</avg_utilization>"+ "";
			add_xml_str +="<payment_frequency>"+Pmtfreq+"</payment_frequency>"+ "";
			add_xml_str +="<maximum_overdue_date>"+getMaximumOverdueDate(MaxOverDueAmountDate)+"</maximum_overdue_date>"+ "";
			add_xml_str +="</ExternalBureauIndividualProducts>";
			
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("Internal liab tag Cration: "	+ add_xml_str);
		}
		
		try{
			String Expense1="0",Expense2="0",Expense3="0",Expense4="0";
			
			String Expense_Query = "select isnull(Expense1,'0') as Expense1 ,isnull(Expense2,'0') as Expense2,isnull(Expense3,'0') as Expense3,isnull(Expense4,'0') as Expense4 from NG_DCC_EXTTABLE with(nolock) where WI_NAME ='"+wiName+"'";
			
			List<Map<String,String>> OutputXML_Expense = getDataFromDBMap(Expense_Query, cabinetName, sessionID, jtsIP, jtsPort);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("OutputXML_Expense output xml: "+ OutputXML_Expense+ "");
			
			
			Expense1=OutputXML_Expense.get(0).get("Expense1");
			Expense2=OutputXML_Expense.get(0).get("Expense2");
			Expense3=OutputXML_Expense.get(0).get("Expense3");
			Expense4=OutputXML_Expense.get(0).get("Expense4");
			//Updated 07122022 - Kamran Active to A
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
					      +"<contract_type>AnOthExp</contract_type>"
					      +"<phase>A</phase>"
					      +"<payments_amount>"+Expense4+"</payments_amount>"
					      +"</ExternalBureauIndividualProducts>";
			
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("ExternalBureauIndividualProducts: "	+ add_xml_str);
			add_xml_str +=Lifestyle;
		}
		catch(Exception e){
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("ExternalBureauIndividualProducts lifestyle Exception"+ e.getMessage());
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("ExternalBureauIndividualProducts lifestyle Exception"+ e);
		}
		
		DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("Internal liab tag Cration: "	+ add_xml_str);
		return add_xml_str;
	}
	
	private static  String sInputXmlExternalBureauPipelineProducts(String wiName, String cabinetName, String sessionID, String jtsIP, String jtsPort) {
		DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("inside ExternalBureauPipelineProducts : ");
		String sQuery = "select CifId, AgreementId,ProviderNo,LoanType,LoanDesc,CustRoleType,Datelastupdated,TotalAmt,TotalNoOfInstalments,CreditLimit,'' as col1,NoOfDaysInPipeline,"
				+ "isnull(Consider_For_Obligations,'true') as 'Consider_For_Obligations', case when IsDuplicate= '1' then 'Y' else 'N' end as 'IsDuplicate' from ng_dcc_cust_extexpo_LoanDetails with (nolock) "
				+ "where Wi_Name  =  '" + wiName + "' and LoanStat = 'Pipeline'"
				+ "union select CifId, CardEmbossNum,ProviderNo,CardType,CardTypeDesc, CustRoleType,LastUpdateDate,'' as col2,NoOfInstallments, '' as col3, TotalAmount, "
				+ "NoOfDaysInPipeLine,isnull(Consider_For_Obligations,'true') as 'Consider_For_Obligations',case when IsDuplicate= '1' then 'Y' else 'N' end as 'IsDuplicate' from ng_dcc_cust_extexpo_CardDetails "
				+ "with (nolock) where Wi_Name  =  '" + wiName + "' and cardstatus = 'Pipeline'";
		
		DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("ExternalBureauPipelineProducts sQuery" + sQuery+"");
		
		String add_xml_str = "";
		List<Map<String,String>> maps= getDataFromDBMap(sQuery, cabinetName, sessionID, jtsIP, jtsPort);
		DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("ExternalBureauPipelineProducts list size"+ maps.size()+ "");
			
		for (Map<String,String> map : maps) {

			String contractType = validateValue(map.get("LoanType"));
			String role = validateValue(map.get("CustRoleType"));
			//String lastUpdateDate = validateValue(map.get("Datelastupdated"));
			//String consider_for_obligation=validateValue(map.get("Consider_For_Obligations"));

			if (!contractType.equals("")) {
				try {
					String cardquery = "select code from ng_master_contract_type with (nolock) where description='"+ contractType + "'";
					DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("ExternalBureauIndividualProducts sQuery"+ cardquery+ "");
					Map<String, String> cardqueryXML = getDataFromDB(cardquery, cabinetName, sessionID, jtsIP, jtsPort, "code");
					contractType = cardqueryXML.get("code");
					DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("ExternalBureauIndividualProducts ContractType"+ contractType+ "ContractType");
				} catch (Exception e) {
					DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("ExternalBureauIndividualProducts ContractType Exception"+ e+ "Exception");
				}
			}
			
			if (!role.equals("")) {
				String sQueryCustRoleType = "select code from ng_master_role_of_customer with(nolock) where Description='"+role+"'";
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("CustRoleType"+sQueryCustRoleType);
				Map<String, String> cardqueryXML = getDataFromDB(sQueryCustRoleType, cabinetName, sessionID, jtsIP, jtsPort, "code");
				try {
					if (cardqueryXML != null && cardqueryXML.size() > 0 && cardqueryXML.get("code") != null) {
						role = cardqueryXML.get("code");
					}
				}
				catch(Exception e){
					DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("Exception occured at sQueryCombinedLimit for"+sQueryCustRoleType);
				}	
			}
			
			/*if (!"".equalsIgnoreCase(consider_for_obligation) && "true".equalsIgnoreCase(consider_for_obligation)) {
				consider_for_obligation = "Y";
			} else {
				consider_for_obligation = "N";
			}*/
			
			add_xml_str +="<ExternalBureauPipelineProducts><applicant_ID>" + wiName + "</applicant_ID>"+ "";
			add_xml_str +="<external_bureau_pipeline_products_id>" + validateValue(map.get("AgreementId")) + "</external_bureau_pipeline_products_id>"+ "";
			add_xml_str +="<ppl_provider_no>" + validateValue(map.get("ProviderNo")) + "</ppl_provider_no>"+ "";
			add_xml_str +="<ppl_type_of_contract>" + contractType + "</ppl_type_of_contract>"+ "";
			add_xml_str +="<ppl_type_of_product>" + validateValue(map.get("LoanDesc")) + "</ppl_type_of_product>"+ "";
			add_xml_str +="<ppl_phase>" + "PIPELINE" + "</ppl_phase>"+ "";
			add_xml_str +="<ppl_role>" + role + "</ppl_role>"+ "";
			add_xml_str +="<ppl_date_of_last_update>" + validateValue(map.get("Datelastupdated")) + "</ppl_date_of_last_update>"+ "";
			//add_xml_str +="<ppl_total_amount>" + validateValue(map.get("TotalAmt")) + "</ppl_total_amount>"+ "";
			add_xml_str +="<ppl_no_of_instalments>" + validateValue(map.get("TotalNoOfInstalments")) + "</ppl_no_of_instalments>"+ "";
			if (validateValue(map.get("LoanType")).toUpperCase().contains("LOAN")) {
				add_xml_str +="<ppl_total_amount>" + validateValue(map.get("TotalAmt")) + "</ppl_total_amount>"+ "";
			} else {
				add_xml_str +="<ppl_credit_limit>" + validateValue(map.get("col1")) + "</ppl_credit_limit>"+ "";
			}
			add_xml_str +="<ppl_no_of_days_in_pipeline>" + validateValue(map.get("NoOfDaysInPipeline")) + "</ppl_no_of_days_in_pipeline>"+ "";
			add_xml_str +="<company_flag>N</company_flag>"+ "";
			add_xml_str +="<ppl_consider_for_obligation>Y</ppl_consider_for_obligation>"+ "";
			add_xml_str +="<ppl_duplicate_flag>"+validateValue(map.get("IsDuplicate"))+"</ppl_duplicate_flag></ExternalBureauPipelineProducts>";
		}
		DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("RLOSCommon"+ "Internal liab tag Cration: "	+ add_xml_str);
		return add_xml_str;
	}
	

	private static  String sInputXmlPerfios(Map<String, String> applicantDetails_Map, Map<String, String> ApplicationDetailsOldSalary_Map) {
		String add_xml_str = "<Perfios>"
				+"<Stmt_Salary_1>"+ApplicationDetailsOldSalary_Map.get("Net_Salary1")+"</Stmt_Salary_1>"
				+"<Stmt_salary1_date>"+applicantDetails_Map.get("Net_salary1_date")+"</Stmt_salary1_date>"
				+"<Stmt_salary_2>"+ApplicationDetailsOldSalary_Map.get("Net_Salary2")+"</Stmt_salary_2>"
				+"<Stmt_salary2_date>"+applicantDetails_Map.get("Net_salary2_date")+"</Stmt_salary2_date>"
				+"<Stmt_salary_3>"+ApplicationDetailsOldSalary_Map.get("Net_Salary3")+"</Stmt_salary_3>"
				+"<Stmt_salary3_date>"+applicantDetails_Map.get("Net_salary3_date")+"</Stmt_salary3_date>"
				+"<Stmt_salary_4>"+applicantDetails_Map.get("Net_Salary4")+"</Stmt_salary_4>"
				+"<Stmt_salary4_date>"+applicantDetails_Map.get("Net_salary4_date")+"</Stmt_salary4_date>"
				+"<Stmt_salary_5>"+applicantDetails_Map.get("Net_Salary5")+"</Stmt_salary_5>"
				+"<Stmt_salary5_date>"+applicantDetails_Map.get("Net_salary5_date")+"</Stmt_salary5_date>"
				+"<Stmt_salary_6>"+applicantDetails_Map.get("Net_Salary6")+"</Stmt_salary_6>"
				+"<Stmt_salary6_date>"+applicantDetails_Map.get("Net_salary6_date")+"</Stmt_salary6_date>"
				+"<Stmt_salary_7>"+applicantDetails_Map.get("Net_Salary7")+"</Stmt_salary_7>"
				+"<Stmt_salary7_date>"+applicantDetails_Map.get("Net_salary7_date")+"</Stmt_salary7_date>"
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
				+"<Addn_Perfios_EMI_18>"+applicantDetails_Map.get("Addn_Perfios_EMI_18")+"</Addn_Perfios_EMI_18>"
				+"<Addn_Perfios_EMI_19>"+applicantDetails_Map.get("Addn_Perfios_EMI_19")+"</Addn_Perfios_EMI_19>"
				+"<Addn_Perfios_EMI_20>"+applicantDetails_Map.get("Addn_Perfios_EMI_20")+"</Addn_Perfios_EMI_20>"
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
				+"<Pensioner>"+applicantDetails_Map.get("Pensioner")+"</Pensioner>"
				+"<Name_match>"+applicantDetails_Map.get("Name_match")+"</Name_match>"
				+"<FCU_indicator>"+applicantDetails_Map.get("FCU_indicator")+"</FCU_indicator>"
				+"<UW_reqd>"+applicantDetails_Map.get("UW_reqd")+"</UW_reqd>"
				+"</Perfios>";
		return add_xml_str;
	}
	
	private static  String validateValue(String value) {
		if (value != null && ! value.equals("") && !value.equalsIgnoreCase("null")) {
			return value.trim();
		}
		return "";
	}
	
	private static  Map<String,String> getDataFromDB(String query, String cabinetName, String sessionID, String jtsIP, String jtsPort, String... columns) {
		try{
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Inside function getDataFromDB");
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("getDataFromDB query is: "+query);
			String InputXML = CommonMethods.apSelectWithColumnNames(query, cabinetName, sessionID);
			Map<String,String> temp = null;
			String OutXml = WFNGExecute(InputXML, jtsIP, jtsPort, 1);
			OutXml = OutXml.replaceAll("&", "#andsymb#");
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("getDataFromDB output xml is: "+OutXml);
			Document recordDoc1 = MapXML.getDocument(OutXml);
			NodeList records1 = recordDoc1.getElementsByTagName("Records");
			if (records1.getLength() > 0) {
				temp = new HashMap<String,String>();
				for(String column : columns) {
					String value= getTagValue(OutXml, column).replaceAll("#andsymb#", "&");
					//String value= getTagValue(OutXml, column);
					DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("value from getTagValue function is:"+value);
					if(null!=value && !"null".equalsIgnoreCase(value) && !"".equals(value)){
						DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Setting value of "+column+" as "+value);	
						temp.put(column, value);
					}
					else{
						DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Setting value of "+column+" as blank");
						temp.put(column, "");
					}
				}
				temp.put("TotalRetrieved", getTagValue(OutXml, "TotalRetrieved"));
			}
			return temp;	
		}
		catch(Exception ex){
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Exception in getDataFromDB method + "+printException(ex));
			return null;
		}
	}

	private static  List<Map<String,String>> getDataFromDBMap(String query, String cabinetName, String sessionID, String jtsIP, String jtsPort){
		try{
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Inside function getDataFromDB");
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("getDataFromDB query is: "+query);
			String InputXML = CommonMethods.apSelectWithColumnNames(query, cabinetName, sessionID);
			List<Map<String,String>> temp = new ArrayList<Map<String,String>>();
			String OutXml = WFNGExecute(InputXML, jtsIP, jtsPort, 1);
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
								DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("getDataFromDBMap Setting value of "+column+" as "+value);	
								t.put(column, value);
							}
							else{
								DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("getDataFromDBMap Setting value of "+column+" as blank");
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
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Exception in getDataFromDBMap method + "+printException(ex));
			return null;
		}

	}
	
	static String socketConnection(String cabinetName, String username, String sessionId, String sJtsIp, String iJtsPort, String wi_name, String ws_name,
			int connection_timeout, int integrationWaitTime, HashMap<String, String> socketDetailsMap, StringBuilder sInputXML)
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

			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("userName "+ username);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("SessionId "+ sessionId);

			socketServerIP=socketDetailsMap.get("SocketServerIP");
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("SocketServerIP "+ socketServerIP);
			socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("SocketServerPort "+ socketServerPort);

	   		if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0))
	   		{

    			socket = new Socket(socketServerIP, socketServerPort);
    			socket.setSoTimeout(connection_timeout*1000);
    			out = socket.getOutputStream();
    			socketInputStream = socket.getInputStream();
    			dout = new DataOutputStream(out);
    			din = new DataInputStream(socketInputStream);
    			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Dout " + dout);
    			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Din " + din);

    			outputResponse = "";

    			inputRequest = getRequestXML( cabinetName,sessionId ,wi_name, ws_name, username, sInputXML);

    			if (inputRequest != null && inputRequest.length() > 0)
    			{
    				int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
    				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("RequestLen: "+inputRequestLen + "");
    				inputRequest = inputRequestLen + "##8##;" + inputRequest;
    				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
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
    				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("OutputResponse: "+outputResponse);

    				if(!"".equalsIgnoreCase(outputResponse))
    					outputResponse = getResponseXML(cabinetName,sJtsIp,iJtsPort,sessionId, wi_name,outputResponse,integrationWaitTime );

    				if(outputResponse.contains("&lt;"))
    				{
    					outputResponse=outputResponse.replaceAll("&lt;", "<");
    					outputResponse=outputResponse.replaceAll("&gt;", ">");
    				}
    			}
    			socket.close();

				outputResponse = outputResponse.replaceAll("</MessageId>","</MessageId>/n<InputMessageId>"+inputMessageID+"</InputMessageId>");

				//DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("outputResponse "+outputResponse);
				return outputResponse;

    	 		}

    		else
    		{
    			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("SocketServerIp and SocketServerPort is not maintained "+"");
    			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("SocketServerIp is not maintained "+	socketServerIP);
    			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug(" SocketServerPort is not maintained "+	socketServerPort);
    			return "Socket Details not maintained";
    		}

		}

		catch (Exception e)
		{
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
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
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
				//printException(e);
			}
		}
	}
	
	private static String getRequestXML(String cabinetName, String sessionId, String wi_name, String ws_name, String userName, StringBuilder sInputXML)
	{
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("<APMQPUTGET_Input>");
		strBuff.append("<SessionId>" + sessionId + "</SessionId>");
		strBuff.append("<EngineName>" + cabinetName + "</EngineName>");
		strBuff.append("<XMLHISTORY_TABLENAME>NG_DCC_XMLLOG_HISTORY</XMLHISTORY_TABLENAME>");
		strBuff.append("<WI_NAME>" + wi_name + "</WI_NAME>");
		strBuff.append("<WS_NAME>" + ws_name + "</WS_NAME>");
		strBuff.append("<USER_NAME>" + userName + "</USER_NAME>");
		strBuff.append("<MQ_REQUEST_XML>");
		strBuff.append(sInputXML);
		strBuff.append("</MQ_REQUEST_XML>");
		strBuff.append("</APMQPUTGET_Input>");
		DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("GetRequestXML: "+ strBuff.toString());
		return strBuff.toString();
	}
	
	private static String getResponseXML(String cabinetName,String sJtsIp,String iJtsPort, String sessionId, String wi_name, String message_ID, int integrationWaitTime)
	{

		String outputResponseXML="";
		try
		{
			String QueryString = "select OUTPUT_XML from NG_DCC_XMLLOG_HISTORY with (nolock) where MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+wi_name+"'";

			String responseInputXML =CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, sessionId);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Response APSelect InputXML: "+responseInputXML);

			int Loop_count=0;
			do
			{
				String responseOutputXML=CommonMethods.WFNGExecute(responseInputXML,sJtsIp,iJtsPort,1);
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Response APSelect OutputXML: "+responseOutputXML);

			    XMLParser xmlParserSocketDetails= new XMLParser(responseOutputXML);
			    String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			    DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("ResponseMainCode: "+responseMainCode);

			    int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			    DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("ResponseTotalRecords: "+responseTotalRecords);

			    if (responseMainCode.equals("0") && responseTotalRecords > 0)
				{

					String responseXMLData=xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData =responseXMLData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

	        		XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
	        		//DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("ResponseXMLData: "+responseXMLData);

	        		outputResponseXML=xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
	        		//DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("OutputResponseXML: "+outputResponseXML);

	        		if("".equalsIgnoreCase(outputResponseXML)){
	        			outputResponseXML="Error";
	    			}
	        		break;
				}
			    Loop_count++;
			    Thread.sleep(1000);
			}
			while(Loop_count<integrationWaitTime);
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("integrationWaitTime: "+integrationWaitTime);

		}
		catch(Exception e)
		{
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Exception occurred in outputResponseXML" + e.getMessage());
			outputResponseXML="Error";
		}
			// outputResponseXML = readdummyresponse(); Never ever uncomment this unless to make the production data patch. -- Deepak 15.12.23
		return outputResponseXML;
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

				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("str_PM_Reason_Codes_Data : " + str_PM_Reason_Codes_Data);
				String PM_Reason_Codes[] = CommonMethods.getTagValues(str_PM_Reason_Codes_Data, "PM_Reason_Codes").split("`");
				DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("length of PM_Reason_Codes"+PM_Reason_Codes.length);
				
				for(int i = 0; i < PM_Reason_Codes.length; i++){
					String Reason_Code = CommonMethods.getTagValues(PM_Reason_Codes[i], "Reason_Code");
					DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Reason_Code : " + Reason_Code);
					String Reason_Description = CommonMethods.getTagValues(PM_Reason_Codes[i], "Reason_Description");
					
					//06122022 - Kamran
					if(Reason_Description.contains("&lt;") || Reason_Description.contains("&amp;") || Reason_Description.contains("&gt;")){
						Reason_Description = Reason_Description.replace("&lt;","<");
						Reason_Description = Reason_Description.replace("&gt;",">");
						Reason_Description = Reason_Description.replace("&amp;gt;","&>");
						Reason_Description = Reason_Description.replaceAll("&amp;","&");
					}
					//End
					DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Reason_Description : " + Reason_Description);
					
					if("A999".equalsIgnoreCase(Reason_Code)){
						continue;
					}
					else if(Reason_Code!=null && !"".equalsIgnoreCase(Reason_Code))
					{
						String InsertValues = "'" + wi_name + "','" + Reason_Code + "','" + Reason_Description +"'";
						String columnName = "Wi_Name,Deviation_Code,Deviation_Description";
						String tableName="NG_DCC_GR_DEVIATION_DESCRIPTIO";
						DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info(" Insert values" + InsertValues);
						sessionCheckInt = 0;
					        while(sessionCheckInt<loopCount)
					        {
					            try
					            {
					                
					            	String inputXML = CommonMethods.apInsert(cabinetName, sessionID, columnName, InsertValues, tableName);
									DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("Insert No" + inputXML);
									String outputXML = CommonMethods.WFNGExecute(inputXML, jtsIP, jtsPort, 1);
									DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("Insert No outXml" + outputXML);
					                objXMLParser.setInputXML(outputXML);
					                mainCodeforAPInsert=objXMLParser.getValueOf("MainCode");
					            }
								
								catch(Exception e)
					            {
					                e.printStackTrace();
					                deviationInsertStatus = "Failure";
					                DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("Exception in inserting Deviation data --"+ e.toString());
					                sessionCheckInt++;
					                waiteloopExecute(waitLoop);
					                continue;
					            }
					            if (mainCodeforAPInsert.equalsIgnoreCase("11")) 
					            {
					                DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("Invalid session in inserting Deviation Data for code-"+Reason_Code);
					                sessionCheckInt++;
					                deviationInsertStatus = "Failure";
					                //ThreadConnect.sessionId = ThreadConnect.getSessionID(cabinetName, jtsIP, jtsPort, userName,password);
					                sessionID=CommonConnection.getSessionID(DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger, false);
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
					            DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("Deviation Data insert successful for code "+Reason_Code);
					        }
					        else
					        {
					            DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("Deviation Data insert Unsuccessful for file "+Reason_Code);
					            deviationInsertStatus = "Failure";
					        }
					
						}
					else
					{
						DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.info("Not a valid reason code -- "+Reason_Code);
					}
				}
				
			}
				
					
		}
			
		catch(Exception e)
		{
			DECTECHSystemIntegrationLog.DECTECHSystemIntegrationLogger.debug("Exception occurred in addDeviations" + e.getMessage());
			deviationInsertStatus = "Failure";
		}
		return deviationInsertStatus;
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
	
	public static String readdummyresponse(){
		String outputxml="";
		try{
			String fileLocation= new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DAO_Integration")
		    		.append(System.getProperty("file.separator")).append("Testmq.xml").toString();
		            
		            BufferedReader sbf=new BufferedReader(new FileReader(fileLocation));
		    		
		    		StringBuilder sb=new StringBuilder();
		    		String line=sbf.readLine();
		    		while(line!=null)
		    		{
		    			sb.append(line);
		    			sb.append(System.lineSeparator());
		    			line=sbf.readLine();
		    		}
		    		outputxml = sb.toString();
		}
		catch(Exception e){
			
		}

		return outputxml;
	}

}
