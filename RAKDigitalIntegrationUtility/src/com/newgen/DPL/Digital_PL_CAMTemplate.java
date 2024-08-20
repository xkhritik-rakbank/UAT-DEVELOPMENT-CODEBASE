package com.newgen.DPL;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.newgen.DCC.SystemIntegration.DCCSystemIntegrationLog;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.omni.wf.util.excp.NGException;

public class Digital_PL_CAMTemplate {

	//private static Logger logger=null;
	 static NGEjbClient ngEjbClient;
	 private static org.apache.log4j.Logger logger;
	 
	public Digital_PL_CAMTemplate(Logger genCam) throws NGException
	{
		//logger=genCam;
		Digital_PL_Log.setLogger(getClass().getSimpleName());
		this.ngEjbClient = NGEjbClient.getSharedInstance();
		logger = Digital_PL_Log.getLogger(getClass().getSimpleName());
	}
	
	public String DPL_generate_CAM_ReportT(String pdfName, String Cif_Id, String processInstanceID, String sessionId)
			throws IOException, Exception {

		logger.debug("Inside generate cam report method: ");
		

		String prop_file_loc = System.getProperty("user.dir") + System.getProperty("file.separator") + "ConfigFiles"
				+ System.getProperty("file.separator") + "DPL_CAMGen_Config.properties";
		logger.debug("prop_file_loc: " + prop_file_loc);

		File file = new File(prop_file_loc);
		FileInputStream fileInput = new FileInputStream(file);
		Properties properties = new Properties();
		properties.load(fileInput);
		fileInput.close();

		String gtIP = properties.getProperty("gtIP");
		logger.debug("gtIP: " + gtIP);

		String gtPortProperty = properties.getProperty("gtPort");
		logger.debug("gtPortProperty: " + gtPortProperty);

		int gtPort = Integer.parseInt(gtPortProperty);
		logger.debug("gtPort: " + gtPort);
		
		// for current date time 
		Date d = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String CurrentDateTime = dateFormat.format(d);
		
		//hkj
		/*String Query = "select distinct a.IsSTP,case when a.MiddleName is null then CONCAT(a.FirstName,' ',a.LastName) else "
				+ "CONCAT(a.FirstName,' ',a.MiddleName,' ',a.LastName) end as CUSTOMERNAME ,a.WINAME, a.CIF,a.ProspectID,a.Age,a.EMI,"
				+ "a.Nationality,a.Score_range,a.AECB_Score, a.EmployerName, a.StartofJob,a.JobTitle,a.delegation_authority,"
				+ "a.EmployerCode,a.ProspectCreationDate,a.ApprovedLoanAmt,a.ProductType,a.LoanType,a.FinalDBR,a.FinalTAI,a.LoanAmount,a.LoanMultiple,a.StressDBR,a.AffordabilityRatio,"
				+ "a.RequestedLoanAmount, a.RequestedLoanTenor,a.FirstRepaymentDate,a.Decision,b.uw_income,"
				+ "a.CustomerDeclaredMonthlyIncome,d.EMPLOYER_CATEGORY_PL_EXPAT,d.EMPLOYER_CATEGORY_PL_NATIONAL,"
				+ "c.AECB_Sal_Month_1,c.AECB_Sal_Month_2,c.AECB_Sal_Month_3 ,a.DectechDecision,a.Final_Limit,a.NationalityDescription,a.Output_Maturity_Age "
				+ "from NG_DPL_EXTTABLE a with (NOLOCK) "
				+ "join NG_DPL_IncomeExpense b with (NOLOCK) on a.WINAME=b.WI_NAME "
				+ "join NG_DPL_AECB_Details c with (NOLOCK) on a.WINAME=c.WI_NAME "
				+ "join NG_DPL_EmploymentDetails d with (NOLOCK) on a.WINAME= d.WI_NAME "
				+ "where a.WINAME  ='" + processInstanceID + "'";*/

		String Query = "select distinct a.IsSTP,case when a.MiddleName is null then CONCAT(a.FirstName,' ',a.LastName) else "
				+ "CONCAT(a.FirstName,' ',a.MiddleName,' ',a.LastName) end as CUSTOMERNAME ,a.WINAME, a.CIF,a.ProspectID,a.Age,a.EMI,"
				+ "a.Nationality,a.Score_range,a.AECB_Score, a.EmployerName, a.StartofJob,a.JobTitle,a.delegation_authority,"
				+ "a.EmployerCode,a.ProspectCreationDate,a.ApprovedLoanAmt,a.ProductType,a.LoanType,a.FinalDBR,a.FinalTAI,a.LoanAmount,a.LoanMultiple,a.StressDBR,a.AffordabilityRatio,"
				+ "a.RequestedLoanAmount, a.RequestedLoanTenor,a.FirstRepaymentDate,a.Decision,"
				+ "a.CustomerDeclaredMonthlyIncome,"
				+ "c.AECB_Sal_Month_1,c.AECB_Sal_Month_2,c.AECB_Sal_Month_3 ,a.DectechDecision,a.Final_Limit,a.NationalityDescription,a.Output_Maturity_Age "
				+ "from NG_DPL_EXTTABLE a with (NOLOCK) "
				+ "join NG_DPL_AECB_Details c with (NOLOCK) on a.WINAME=c.WI_NAME "
				+ "where a.WINAME  ='" + processInstanceID + "'";
		// for user name and decision at cad analy 1
		
		logger.debug("Query : " + Query);
		
		
		String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(Query, CommonConnection.getCabinetName(),
				CommonConnection.getSessionID(logger, false));
		
		logger.debug("extTabDataIPXML: " + extTabDataIPXML);
		String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(),
				CommonConnection.getJTSPort(), 1);
		logger.debug("extTabDataOPXML: " + extTabDataOPXML);

		XMLParser xmlParserData = new XMLParser(extTabDataOPXML);

		// **************************** for second query
		// ************************
		/*String extTabDataIPXML2 = CommonMethods.apSelectWithColumnNames(Query, CommonConnection.getCabinetName(),
				CommonConnection.getSessionID(logger, false));
		logger.debug("extTabDataIPXML2: " + extTabDataIPXML2);
		String extTabDataOPXML2 = CommonMethods.WFNGExecute(extTabDataIPXML2, CommonConnection.getJTSIP(),
				CommonConnection.getJTSPort(), 1);
		logger.debug("extTabDataOPXML2: " + extTabDataOPXML2);

		XMLParser xmlParserData2 = new XMLParser(extTabDataOPXML2);*/
		// ****************************************************************************************
		
		// variables
		String attrbList = "";
		String Is_STP = xmlParserData.getValueOf("IsSTP");
		logger.debug("Is_STP: " + Is_STP);

		String CifId = xmlParserData.getValueOf("CIF");
		String CUSTOMERNAME = xmlParserData.getValueOf("CUSTOMERNAME");
		logger.debug("CUSTOMERNAME: " + CUSTOMERNAME);

		String Nationality = xmlParserData.getValueOf("Nationality");
		logger.debug("Nationality: " + Nationality);
		
		String Nationality_desc = xmlParserData.getValueOf("NationalityDescription");
		logger.debug("Nationality_desc: "+Nationality_desc);
		
		
		String FinalTAI = xmlParserData.getValueOf("FinalTAI");
		logger.debug("FinalTAI: " + FinalTAI);
		
		String Decision = xmlParserData.getValueOf("Decision");
		logger.debug("Decision: " + Decision);
		

		//String Card_Type = xmlParserData.getValueOf("Selected_Card_Type");
		//logger.debug("Card_Type: " + Card_Type);
		
		String Product_type = xmlParserData.getValueOf("ProductName");
		logger.debug("Product_Desc: " + Product_type);

		String Output_Maturity_Age = xmlParserData.getValueOf("Output_Maturity_Age");
		logger.debug("Output_Maturity_Age: " + Output_Maturity_Age);
//		String Selected_Card_Type = xmlParserData.getValueOf("Selected_Card_Type");
//		logger.debug("Selected_Card_Type: " + Selected_Card_Type);
//
//		String card_application_date = xmlParserData.getValueOf("card_application_date");
//		if(card_application_date!=null && card_application_date.length()>=10)
//		card_application_date=card_application_date.substring(0,10); //CommonMethods.parseDate(card_application_date,"yyyy-MM-dd HH:mm:ss","dd-MM-yyyy");//2022-09-30 00:00:00
//		logger.debug("card_application_date: " + card_application_date);
//		String Age = xmlParserData.getValueOf("Age");
//		logger.debug("Age: " + Age);

		String IPA_Limit = xmlParserData.getValueOf("ApprovedLoanAmt");
		logger.debug("IPA_Limit: " + IPA_Limit);

		String EFMS_Status = xmlParserData.getValueOf("EFMS_Status");
		logger.debug("EFMS_Status: " + EFMS_Status);

		String Requested_Limit = xmlParserData.getValueOf("RequestedLoanAmount");
		logger.debug("Requested_Limit: " + Requested_Limit);

		String FIRCO_Flag = xmlParserData.getValueOf("FIRCO_Status");
		logger.debug("FIRCO_Flag: " + FIRCO_Flag);
		
		if ("CB".equalsIgnoreCase(FIRCO_Flag) || "N".equalsIgnoreCase(FIRCO_Flag)) {
			FIRCO_Flag = "Hit";
		} else {
			FIRCO_Flag = "No-Hit";
		}
		logger.debug("FIRCO_Flag: " + FIRCO_Flag);

		String Employer_Name = xmlParserData.getValueOf("EmployerName");
		logger.debug("Employer_Name: " + Employer_Name);

		String Date_Of_Joining = xmlParserData.getValueOf("StartofJob");
		logger.debug("Date_Of_Joining: " + Date_Of_Joining);

		String Designation = xmlParserData.getValueOf("JobTitle");
		logger.debug("Designation: " + Designation);
		
		String EmployerCode = xmlParserData.getValueOf("EmployerCode");
		logger.debug("employercode: " + EmployerCode);

		// employer status pl miss

		String Final_Limit = xmlParserData.getValueOf("Final_Limit");
		logger.debug("Final_Limit: " + Final_Limit);

		// declared income miss
		String DeclIncome = xmlParserData.getValueOf("CustomerDeclaredMonthlyIncome");
		logger.debug("DeclIncome: " + DeclIncome);
		
		
		String ProspectID = xmlParserData.getValueOf("ProspectID");
		logger.debug("ProspectID: " + ProspectID);
		
		String Age = xmlParserData.getValueOf("Age");
		logger.debug("Age: " + Age);

		String AECB_Score = xmlParserData.getValueOf("AECB_Score");
		logger.debug("Aecb_score: " + AECB_Score);

		String Score_range = xmlParserData.getValueOf("Score_range");
		logger.debug("Score_range: " + Score_range);
		
		String StartofJob = xmlParserData.getValueOf("StartofJob");
		logger.debug("StartofJob: " + StartofJob);
		
		
		String JobTitle = xmlParserData.getValueOf("JobTitle");
		logger.debug("JobTitle: " + JobTitle);
		
		String LoanType = xmlParserData.getValueOf("LoanType");
		logger.debug("LoanType: " + LoanType);
		
		String FinalDBR = xmlParserData.getValueOf("FinalDBR");
		logger.debug("FinalDBR: " + FinalDBR);
		
		String RequestedLoanTenor = xmlParserData.getValueOf("RequestedLoanTenor");
		logger.debug("RequestedLoanTenor: " + RequestedLoanTenor);
		
		String FirstRepaymentDate = xmlParserData.getValueOf("FirstRepaymentDate");
		logger.debug("FirstRepaymentDate: " + FirstRepaymentDate);
		
		String CustomerDeclaredMonthlyIncome = xmlParserData.getValueOf("CustomerDeclaredMonthlyIncome");
		logger.debug("CustomerDeclaredMonthlyIncome: " + CustomerDeclaredMonthlyIncome);
		
		String AECB_Sal_Month_1 = xmlParserData.getValueOf("AECB_Sal_Month_1");
		logger.debug("AECB_Sal_Month_1: " + AECB_Sal_Month_1);
		
		String AECB_Sal_Month_2 = xmlParserData.getValueOf("AECB_Sal_Month_2");
		logger.debug("AECB_Sal_Month_2: " + AECB_Sal_Month_2);
		
		String AECB_Sal_Month_3 = xmlParserData.getValueOf("AECB_Sal_Month_3");
		logger.debug("AECB_Sal_Month_3: " + AECB_Sal_Month_3);
		
		
		
		
		
		
		
		String LoanAmount = xmlParserData.getValueOf("LoanAmount");
		logger.debug("LoanAmount: " + LoanAmount);
		
		String LoanMultiple = xmlParserData.getValueOf("LoanMultiple");
		logger.debug("LoanMultiple: " + LoanMultiple);
		
		String StressDBR = xmlParserData.getValueOf("StressDBR");
		logger.debug("StressDBR: " + StressDBR);
		
		String AffordabilityRatio = xmlParserData.getValueOf("AffordabilityRatio");
		logger.debug("AffordabilityRatio: " + AffordabilityRatio);
		
		String delegation_authority = xmlParserData.getValueOf("delegation_authority");
		logger.debug("delegation_authority: " + delegation_authority);
		
		String ApplicationBranchDescription = xmlParserData.getValueOf("ApplicationBranchDescription");
		logger.debug("ApplicationBranchDescription: " + ApplicationBranchDescription);
		
		String SourceBranchDescription = xmlParserData.getValueOf("SourceBranchDescription");
		logger.debug("SourceBranchDescription: " + SourceBranchDescription);
		
		String EMI = xmlParserData.getValueOf("EMI");
		logger.debug("EMI: " + EMI);
		
		
		String ProspectCreationDate = xmlParserData.getValueOf("ProspectCreationDate");
		logger.debug("ProspectCreationDate: " + ProspectCreationDate);
		
		String QueryFTS ="select net_salary_month_1,net_salary_month_2,net_salary_month_3,uw_income,creditRemarkstoOPS from NG_DPL_IncomeExpense with (NOLOCK) where WI_Name ='" + processInstanceID + "'";
				
		String extTabDataFTS = CommonMethods.apSelectWithColumnNames(QueryFTS, CommonConnection.getCabinetName(),
				CommonConnection.getSessionID(logger, false));
		
		logger.debug("extTabDataFTS: " + extTabDataFTS);
		String FTSextTabDataOPXML = CommonMethods.WFNGExecute(extTabDataFTS, CommonConnection.getJTSIP(),
				CommonConnection.getJTSPort(), 1);
		logger.debug("extTabDataOPXML: " + FTSextTabDataOPXML);

		XMLParser FTSxmlParserData = new XMLParser(FTSextTabDataOPXML);
		
		String FTS_Sal_Month_1 = FTSxmlParserData.getValueOf("net_salary_month_1");
		logger.debug("FTS_Sal_Month_1: " + FTS_Sal_Month_1);
		
		String FTS_Sal_Month_2 = FTSxmlParserData.getValueOf("net_salary_month_2");
		logger.debug("FTS_Sal_Month2: " + FTS_Sal_Month_2);
		
		String FTS_Sal_Month_3 = FTSxmlParserData.getValueOf("net_salary_month_3");
		logger.debug("FTS_Sal_Month_3: " + FTS_Sal_Month_3);
		//If A -Approve, If D decline: If R with Nstp N: DECLINE; If R with NSTP Y: Refer
		
		String creditremarkstoops = FTSxmlParserData.getValueOf("creditRemarkstoOPS");
		logger.debug("creditremarkstoops: " + creditremarkstoops);
		
		String Dectech_Decision = xmlParserData.getValueOf("DectechDecision");
		logger.debug("Dectech_Decision: " + Dectech_Decision);
		
		String uw_income = xmlParserData.getValueOf("uw_income");
		logger.debug("uw_income: " + uw_income);
		
		String Dec_Remarks ="";
		Dec_Remarks= xmlParserData.getValueOf("Dec_Remarks");
		logger.debug("Dec_Remarks: " + Dec_Remarks);
		
		if("A".equalsIgnoreCase(Dectech_Decision))
			Dectech_Decision="Approve";
		else if("D".equalsIgnoreCase(Dectech_Decision))
			Dectech_Decision="Decline";
		else if("R".equalsIgnoreCase(Dectech_Decision))
			{
				if("Y".equalsIgnoreCase(Is_STP))
					Dectech_Decision="Decline";
				else if("N".equalsIgnoreCase(Is_STP))
					Dectech_Decision="Refer";
			}
	
		
		String sQueryDecisionHistory ="select top 1 (select Description from NG_MASTER_EmployerStatusCC where Code= COMPANY_STATUS_CC) as \"COMPANY_STATUS_CC\",(select Description from NG_MASTER_EmployerStatusPL where Code=COMPANY_STATUS_PL) as \"COMPANY_STATUS_PL\", (select Description from NG_MASTER_EmployerCategory_PL where Code=EMPLOYER_CATEGORY_PL) as \"EMPLOYER_CATEGORY_PL\"," +
        "EMPLOYER_CATEGORY_PL_EXPAT,EMPLOYER_CATEGORY_PL_NATIONAL,INCLUDED_IN_CC_ALOC,INCLUDED_IN_PL_ALOC,cast(DATE_OF_INCLUSION_IN_CC_ALOC as date) as DATE_OF_INCLUSION_IN_CC_ALOC," +
        "cast(DATE_OF_INCLUSION_IN_PL_ALOC as date) as DATE_OF_INCLUSION_IN_PL_ALOC,INDUSTRY_SECTOR,ALOC_REMARKS_CC,ALOC_REMARKS_PL from NG_RLOS_ALOC_OFFLINE_DATA with (nolock) where EMPLOYER_CODE='"+EmployerCode+"'";

		String extTabDataIPXMLDecisionHistory = CommonMethods.apSelectWithColumnNames(sQueryDecisionHistory, CommonConnection.getCabinetName(),
				CommonConnection.getSessionID(logger, false));
		logger.debug("extTabDataIPXMLDecisionHistory: " + extTabDataIPXMLDecisionHistory);
		String extTabDataOPXMLDecisionHistory = CommonMethods.WFNGExecute(extTabDataIPXMLDecisionHistory, CommonConnection.getJTSIP(),
				CommonConnection.getJTSPort(), 1);
		logger.debug("extTabDataOPXMLDecisionHistory: " + extTabDataOPXMLDecisionHistory);

		XMLParser xmlParserDataDecisionHistory = new XMLParser(extTabDataOPXMLDecisionHistory);
		
		//********************************************************
		
		// variables define 
		
		String COMPANY_STATUS_CC = xmlParserDataDecisionHistory.getValueOf("COMPANY_STATUS_CC");
		logger.debug("COMPANY_STATUS_CC: " + COMPANY_STATUS_CC);
		
		String COMPANY_STATUS_PL = xmlParserDataDecisionHistory.getValueOf("COMPANY_STATUS_PL");
		logger.debug("COMPANY_STATUS_PL: " + COMPANY_STATUS_PL);
		
		String ALOC_REMARKS_PL = xmlParserDataDecisionHistory.getValueOf("ALOC_REMARKS_PL").replaceAll("'", "");
		logger.debug("ALOC_REMARKS_PL: " + ALOC_REMARKS_PL);


		
		String ALOC_REMARKS_CC = xmlParserDataDecisionHistory.getValueOf("ALOC_REMARKS_CC").replaceAll("'", "");
		logger.debug("ALOC_REMARKS_CC: " + ALOC_REMARKS_CC);
		
		
		String INDUSTRY_SECTOR = xmlParserDataDecisionHistory.getValueOf("INDUSTRY_SECTOR");
		logger.debug("INDUSTRY_SECTOR: " + INDUSTRY_SECTOR);
		
		String EMPLOYER_CATEGORY_PL = xmlParserDataDecisionHistory.getValueOf("EMPLOYER_CATEGORY_PL");
		logger.debug("EMPLOYER_CATEGORY_PL: " + EMPLOYER_CATEGORY_PL);
		if(EMPLOYER_CATEGORY_PL==null)
			EMPLOYER_CATEGORY_PL="";
		
		
		
		String EMPLOYER_CATEGORY_PL_EXPAT = xmlParserDataDecisionHistory.getValueOf("EMPLOYER_CATEGORY_PL_EXPAT");
		logger.debug("EMPLOYER_CATEGORY_PL_EXPAT: " + EMPLOYER_CATEGORY_PL_EXPAT);
		
		String EMPLOYER_CATEGORY_PL_NATIONAL = xmlParserDataDecisionHistory.getValueOf("EMPLOYER_CATEGORY_PL_NATIONAL");
		logger.debug("EMPLOYER_CATEGORY_PL_NATIONAL: " + EMPLOYER_CATEGORY_PL_NATIONAL);
		//Aloc data save in db added by rubi
		

    	//String columnNames = "EXTERNAL_EXPOSURE_STATUS";
    	//String columnValues = "'" + flag + "'";
    	
    	String queryForEmploymentDeatils = "select COMPANY_STATUS_CC,COMPANY_STATUS_PL,EMPLOYER_CATEGORY_PL_NATIONAL,Employer_Category_PL_Expat "
    			+ "from NG_DPL_EmploymentDetails with (nolock) where WI_NAME= '"+processInstanceID+"'";
    	
    	String inputXML = CommonMethods.apSelectWithColumnNames(queryForEmploymentDeatils, CommonConnection.getCabinetName(),
    			CommonConnection.getSessionID(logger, false));
    	logger.debug("extTabDataIPXML2: " + inputXML);
    	
    	String extTabDataOPXML2 = CommonMethods.WFNGExecute(inputXML, CommonConnection.getJTSIP(),
    					CommonConnection.getJTSPort(), 1);
    	logger.debug("extTabDataOPXML2: " + extTabDataOPXML2);

    	XMLParser xmlParserData2 = new XMLParser(extTabDataOPXML2);
    	
    	String MainCode = xmlParserData2.getValueOf("MainCode");
    	logger.debug("MainCode: " + MainCode);
		//DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("SocketDetailsMainCode: " + socketDetailsMainCode);

		int TotalRecords = Integer.parseInt(xmlParserData2.getValueOf("TotalRetrieved"));
		logger.debug("TotalRecords: " + TotalRecords);
		
		if ("0".equalsIgnoreCase(MainCode) && TotalRecords > 0) {
    			
			String columnNames = "COMPANY_STATUS_CC,COMPANY_STATUS_PL,Employer_Category_PL_Expat,EMPLOYER_CATEGORY_PL";
			String columnValues = "'" + COMPANY_STATUS_CC + "','" + COMPANY_STATUS_PL + "','" + EMPLOYER_CATEGORY_PL_EXPAT+"','"+EMPLOYER_CATEGORY_PL + "'";
			String sWhereClause = "WI_NAME='" + processInstanceID + "'";
	    	
			inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(Digital_PL_Log.digital_PL_Log, false), 
        		"NG_DPL_EmploymentDetails", columnNames, columnValues, sWhereClause);
			logger.debug("Input XML for apUpdateInput for  Table : " + inputXML);
			
			String outputXml = CommonMethods.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
			logger.debug("Output XML for apUpdateInput for  Table : " + outputXml);
        
			XMLParser sXMLParserChild = new XMLParser(outputXml);
			String StrMainCode = sXMLParserChild.getValueOf("MainCode");
		}else{
			
			String columnNames="WI_NAME,Employment_Type,Date_Of_Joining,Designation,Company_Status_CC,Company_Status_PL,Employer_Category_PL_Expat,Employer_Category_PL_National,CC_ALOC_Remarks,PL_ALOC_Remarks,Industry,Sub_Industry,EMPLOYER_CATEGORY_PL";
			String columnValues="'"+processInstanceID+"','','"+StartofJob+"','"+JobTitle+"','"+COMPANY_STATUS_CC+"','"+COMPANY_STATUS_PL+"','"+EMPLOYER_CATEGORY_PL_EXPAT+"','"+EMPLOYER_CATEGORY_PL_NATIONAL+"','"+ALOC_REMARKS_CC+"','"+ALOC_REMARKS_PL+"','"+INDUSTRY_SECTOR+"','','"+EMPLOYER_CATEGORY_PL+"'";

			String apInsertInputXML=CommonMethods.apInsert(CommonConnection.getCabinetName(), CommonConnection.getSessionID(Digital_PL_Log.digital_PL_Log, false), columnNames, columnValues, "NG_DPL_EmploymentDetails");
			logger.debug("APInsertInputXML: "+apInsertInputXML);

			String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML,CommonConnection.getJTSIP(),
					CommonConnection.getJTSPort(),1);
			logger.debug("APInsertOutputXML: "+ apInsertOutputXML);

			XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
			String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
			logger.debug("Status of apInsertMaincode  "+ apInsertMaincode);
			if(apInsertMaincode.equalsIgnoreCase("0"))
			{
				logger.debug("ApInsert successful: "+apInsertMaincode);
				logger.debug("Inserted in WiHistory table successfully.");
			}
			else
			{
				logger.error("ApInsert failed: "+apInsertMaincode);
			}
		}
		Double los =Digital_PL_CommomMethod.CalculatLOS(StartofJob);
		
		attrbList += "&<currentDateTime>&" + CurrentDateTime;
		attrbList += "&<customerName>&" + CUSTOMERNAME;
		attrbList += "&<cardType>&" + "";
		attrbList += "&<cardApplicationDate>&" + "";
		attrbList += "&<nationality>&" + Nationality_desc;
		attrbList += "&<finalTAI>&" + FinalTAI;
		attrbList += "&<decision>&" + Decision;
		attrbList += "&<incomeAtCredit>&" + uw_income;
		
		attrbList += "&<CIF>&" + Cif_Id;
		attrbList += "&<age>&" + "";
		attrbList += "&<productType>&" + Product_type;
		attrbList += "&<IPALimit>&" + IPA_Limit;
		attrbList += "&<EFMSStatus>&" + EFMS_Status;
		attrbList += "&<requestedLimit>&" + Requested_Limit;
		attrbList += "&<fircoStatus>&" + FIRCO_Flag;
		
		
		attrbList += "&<agreementNumber>&" + ProspectID;
		attrbList += "&<currentAge>&" +Age;
		attrbList += "&<ageAtMaturity>&" +Output_Maturity_Age;
		attrbList += "&<sourceBranch>&" +"";
		attrbList += "&<AECBRange>&" +Score_range;
		attrbList += "&<AECBScore>&"+ AECB_Score;
		
		//new field added as per jira 646 added by rubi
		attrbList += "&<appBranchDescription>&" +ApplicationBranchDescription;
		attrbList += "&<sourceBranchDescription>&" +SourceBranchDescription;
		
		logger.debug("attrbList1: " + attrbList);

		attrbList += "&<employerName>&" + Employer_Name;
		attrbList += "&<Date_Of_Joining>&" + StartofJob;
		attrbList += "&<Designation>&" + JobTitle;
		attrbList += "&<employerCode>&" + EmployerCode;
		
		attrbList += "&<yearsInEmployment>&" +los;
		attrbList += "&<employerStatusCC>&"+COMPANY_STATUS_CC;//
		attrbList += "&<employerCategoryPL>&" +EMPLOYER_CATEGORY_PL;// ---(NG_DPL_EmploymentDetails)
		attrbList += "&<EMPLOYER_STATUS_PL_EXPAT>&"+EMPLOYER_CATEGORY_PL_EXPAT;// ---(NG_DPL_EmploymentDetails)
		attrbList += "&<EMPLOYER_STATUS_PL_NATIONAL>&"+EMPLOYER_CATEGORY_PL_NATIONAL; // ---(NG_DPL_EmploymentDetails)
		attrbList += "&<NATURE_OF_BUSINESS>&"+"";
		attrbList += "&<loanType>&"+LoanType;
		attrbList += "&<loanApplicationDate>&"+ProspectCreationDate;
		attrbList += "&<loanAmount>&"+LoanAmount;
		
		attrbList += "&<Tenor>&"+RequestedLoanTenor;
		attrbList += "&<firstRepaymentDetail>&"+FirstRepaymentDate;
		attrbList += "&<loanMultiple>&"+LoanMultiple;
		attrbList += "&<loanEMI>&"+EMI;
		attrbList += "&<declaredIncome>&"+CustomerDeclaredMonthlyIncome;
		attrbList += "&<totalIncome>&"+"";
		attrbList += "&<fianlDBR>&"+FinalDBR;
		
		attrbList += "&<netSalary1>&"+FTS_Sal_Month_1;
		attrbList += "&<netSalary2>&"+FTS_Sal_Month_2;
		attrbList += "&<netSalary3>&"+FTS_Sal_Month_3;
		attrbList += "&<stressDBR>&"+StressDBR;
		attrbList += "&<affordabilityRatio>&"+AffordabilityRatio;
		
		attrbList += "&<creditRemarkstoOPS>&"+creditremarkstoops;
		
		attrbList += "&<AECBsalary1>&"+AECB_Sal_Month_1;
		attrbList += "&<AECBsalary2>&"+AECB_Sal_Month_2;
		attrbList += "&<AECBsalary3>&"+AECB_Sal_Month_3;
		
		attrbList += "&<OutputDelegationAuthority>&"+delegation_authority;
		
		attrbList += "&<finalDecision>&"+Dectech_Decision;
		
		

		attrbList += "&<reviewedBy>&" + CommonConnection.getUsername();
		attrbList += "&<creditRemarks>&" + Dec_Remarks;
		attrbList += "&<workitemNumber>&" + processInstanceID;
		logger.debug("attrbList" + attrbList);
		
		String output = makeSocketCall(attrbList, processInstanceID, pdfName, sessionId, gtIP, gtPort);
		
		logger.debug("attrbList" + attrbList);
		logger.debug("output" + output);
		
	

		return output;

	}

	public String makeSocketCall(String argumentString, String wi_name, String docName, String sessionId, String gtIP,
			int gtPort) {
		String socketParams = argumentString + "~" + wi_name + "~" + docName + "~" + sessionId;

		System.out.println("socketParams -- " + socketParams);
		logger.debug("socketParams" + socketParams);

		Socket template_socket = null;
		DataOutputStream template_dout = null;
		DataInputStream template_in = null;
		String result = "";
		try {
			// Socket write code started
			template_socket = new Socket(gtIP, gtPort);
			logger.debug("template_socket" + template_socket);

			template_dout = new DataOutputStream(template_socket.getOutputStream());
			logger.debug("template_dout" + template_dout);

			if (socketParams != null && socketParams.length() > 0) {
				int outPut_len = socketParams.getBytes("UTF-8").length;
				logger.debug("outPut_len" + outPut_len);
				// CreditCard.mLogger.info("Final XML output len:
				// "+outPut_len +
				// "");
				socketParams = outPut_len + "##8##;" + socketParams;
				logger.debug("socketParams--" + socketParams);
				// CreditCard.mLogger.info("MqInputRequest"+"Input Request
				// Bytes : "+
				// mqInputRequest.getBytes("UTF-16LE"));

				template_dout.write(socketParams.getBytes("UTF-8"));
				template_dout.flush();
			} else {
				notify();
			}
			// Socket write code ended and read code started
			template_socket.setSoTimeout(60 * 1000);
			template_in = new DataInputStream(new BufferedInputStream(template_socket.getInputStream()));
			byte[] readBuffer = new byte[50000];
			int num = template_in.read(readBuffer);
			if (num > 0) {
				byte[] arrayBytes = new byte[num];
				System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
				result = new String(arrayBytes, "UTF-8");
				logger.debug("result--" + result);
			}
		}

		catch (SocketException se) {
			se.printStackTrace();
		} catch (IOException i) {
			i.printStackTrace();
		} catch (Exception io) {
			io.printStackTrace();
		} finally {
			try {
				if (template_dout != null) {
					template_dout.close();
					template_dout = null;
				}
				if (template_in != null) {
					template_in.close();
					template_in = null;
				}
				if (template_socket != null) {
					if (!template_socket.isClosed()) {
						template_socket.close();
					}
					template_socket = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

}
