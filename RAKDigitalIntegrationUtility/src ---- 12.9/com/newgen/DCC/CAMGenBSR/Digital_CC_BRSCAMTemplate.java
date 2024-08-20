package com.newgen.DCC.CAMGenBSR;

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
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;

public class Digital_CC_BRSCAMTemplate {

	private static Logger DCC_CamGenrateteLog=null;
	
	private String sessionID = "";
	private String JtsIp =  "";
	private String JtsPort = "";
	
//	public String fromMailID="";
//	public String toMailID = "";
//	public String mailSubject = "";
//	public String MailStr="";
	private static NGEjbClient ngEjbClientCIFVer;
	public static int sessionCheckInt=0;
	public static int waitLoop=50;
	public static int loopCount=50;
	
	final HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();
		
	public Digital_CC_BRSCAMTemplate(Logger genCam)
	{
		DCC_CamGenrateteLog=genCam;
	}
	

	public String generate_BSR_CAM_Report(String cabinetName,String pdfName, String Cif_Id, String processInstanceID, 
			String sessionId,String sJtsIp, String iJtsPort,String entryDateTime,String WorkItemID,String createdDateTime, String ProcessDefId)
			throws IOException, Exception {

		DCC_CamGenrateteLog.debug("Inside generate BSR CAM report method: ");
		
		sessionID = sessionId;
		JtsIp = sJtsIp;
		JtsPort = iJtsPort;
		String prop_file_loc = System.getProperty("user.dir") + System.getProperty("file.separator") + "ConfigFiles"
				+ System.getProperty("file.separator") + "DCC_CAMGen_Config.properties";
		DCC_CamGenrateteLog.debug("prop_file_loc: " + prop_file_loc);

		File file = new File(prop_file_loc);
		FileInputStream fileInput = new FileInputStream(file);
		Properties properties = new Properties();
		properties.load(fileInput);
		fileInput.close();

		String gtIP = properties.getProperty("gtIP");
		DCC_CamGenrateteLog.debug("gtIP: " + gtIP);

		String gtPortProperty = properties.getProperty("gtPort");
		DCC_CamGenrateteLog.debug("gtPortProperty: " + gtPortProperty);

		int gtPort = Integer.parseInt(gtPortProperty);
		DCC_CamGenrateteLog.debug("gtPort: " + gtPort);
		
		// for current date time 
		Date d = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String CurrentDateTime = dateFormat.format(d);
	
		String Query = "select Title,FirstName,MiddleName,LastName,dob,Nationality_Desc,Gender_Desc," +
				"MaritalStatus_Desc,ETB_CIFS,Enroll_for_Falcon,FYF,Skyward_Number," +
				"GCC_National,EmirateID,EmID_Expiry,PassportNo,Passport_expiry,Visa_Number,Visa_Expiry," +
				"MobileNo,email_id,Employer_Name,EmploymentType_Desc,employercode,Designation_Desc,Product," +
				"Requested_Limit,Preferred_address,Place_birth,(select CD_DESC from NG_MASTER_DAO_COUNTRY where CM_CODE=country_birth ) as country_birth," +
				"Credit_Shield_Flag,Self_Supp_Card_Embossing_Name,CIF,Prospect_id,Prospect_Creation_Date," +
				"bureau_reference_number,Tax_Pay_in_oth_country,los," +
				"DBR_lifeStyle_expenses,No_earning_members,RM_Code,Expense1,Expense2,Expense3,Expense4,Tin_reason, " +
				"Fatca,FATCA_Tin_Number,Dependents,Mother_Name,FTS_Ref_No,E_Registration,Industry,Sub_Industry," +
				"Earning_members,(select Card_Type_Desc from ng_dcc_master_cardtype " +
				"where card_type_code=Selected_Card_Type ) as Selected_Card_Type from NG_DCC_EXTTABLE with (NOLOCK) where Wi_Name ='"
				+ processInstanceID + "'";
		
		DCC_CamGenrateteLog.debug("Query : " + Query);
		
		DCC_CamGenrateteLog.debug("Current EntryDateTime: " + createdDateTime);
		
		String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(Query, CommonConnection.getCabinetName(),
				CommonConnection.getSessionID(DCC_CamGenrateteLog, false));
		DCC_CamGenrateteLog.debug("extTabDataIPXML: " + extTabDataIPXML);
		String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(),
				CommonConnection.getJTSPort(), 1);
		DCC_CamGenrateteLog.debug("extTabDataOPXML: " + extTabDataOPXML);

		XMLParser xmlParserData = new XMLParser(extTabDataOPXML);

		// variables
		String attrbList = "";
		
		attrbList += "&<currentDateTime>&" + CurrentDateTime;
		
		String Title = xmlParserData.getValueOf("Title");
		DCC_CamGenrateteLog.debug("Title: " + Title);

		String FirstName = xmlParserData.getValueOf("FirstName");
		DCC_CamGenrateteLog.debug("FirstName: " + FirstName);

		String MiddleName = xmlParserData.getValueOf("MiddleName");
		DCC_CamGenrateteLog.debug("MiddleName: " + MiddleName);

		String LastName = xmlParserData.getValueOf("LastName");
		DCC_CamGenrateteLog.debug("LastName: " + LastName);

		String dob = xmlParserData.getValueOf("dob");
		DCC_CamGenrateteLog.debug("dob: " + dob);

		String Nationality = xmlParserData.getValueOf("Nationality_Desc");
		DCC_CamGenrateteLog.debug("Nationality: " + Nationality);

		String Gender_Code = xmlParserData.getValueOf("Gender_Desc");
		DCC_CamGenrateteLog.debug("Gender_Code: " + Gender_Code);

		String MaritalStatus = xmlParserData.getValueOf("MaritalStatus_Desc");
		DCC_CamGenrateteLog.debug("MaritalStatus: " + MaritalStatus);

		String GCC_National = xmlParserData.getValueOf("GCC_National");
		DCC_CamGenrateteLog.debug("GCC_National: " + GCC_National);
		
		String EmirateID = xmlParserData.getValueOf("EmirateID");
		DCC_CamGenrateteLog.debug("EmirateID: " + EmirateID);
		
		String EmID_Expiry = xmlParserData.getValueOf("EmID_Expiry");
		DCC_CamGenrateteLog.debug("EmID_Expiry: " + EmID_Expiry);
		
		String PassportNo = xmlParserData.getValueOf("PassportNo");
		DCC_CamGenrateteLog.debug("PassportNo: " + PassportNo);
		
		String Passport_expiry = xmlParserData.getValueOf("Passport_expiry");
		DCC_CamGenrateteLog.debug("Passport_expiry: " + Passport_expiry);
		
		String Visa_Number = xmlParserData.getValueOf("Visa_Number");
		DCC_CamGenrateteLog.debug("Visa_Number: " + Visa_Number);
		
		String Visa_Expiry = xmlParserData.getValueOf("Visa_Expiry");
		DCC_CamGenrateteLog.debug("Visa_Expiry: " + Visa_Expiry);
		
		String MobileNo = xmlParserData.getValueOf("MobileNo");
		DCC_CamGenrateteLog.debug("MobileNo: " + MobileNo);
		
		String email_id = xmlParserData.getValueOf("email_id");
		DCC_CamGenrateteLog.debug("email_id: " + email_id);
		
		String Employer_Name = xmlParserData.getValueOf("Employer_Name");
		DCC_CamGenrateteLog.debug("Employer_Name: " + Employer_Name);
		
		
		String EmploymentType = xmlParserData.getValueOf("EmploymentType_Desc");
		DCC_CamGenrateteLog.debug("EmploymentType: " + EmploymentType);
		
		
		String employercode = xmlParserData.getValueOf("employercode");
		DCC_CamGenrateteLog.debug("employercode: " + employercode);
		
		String Designation = xmlParserData.getValueOf("Designation_Desc");
		DCC_CamGenrateteLog.debug("Designation: " + Designation);
		
		String Card_Product = xmlParserData.getValueOf("Product");
		DCC_CamGenrateteLog.debug("Card_Product: " + Card_Product);
		
		if("CON".equalsIgnoreCase(Card_Product)){
			Card_Product = "Conventional";
			
		}else{
			Card_Product = "Islamic";
		}
		
		String Requested_Limit = xmlParserData.getValueOf("Requested_Limit");
		DCC_CamGenrateteLog.debug("Requested_Limit: " + Requested_Limit);
		
		String Credit_Shield_Flag = xmlParserData.getValueOf("Credit_Shield_Flag");
		DCC_CamGenrateteLog.debug("Credit_Shield_Flag: " + Credit_Shield_Flag);
		
		String Self_Supp_Card_Embossing_Name = FirstName+" "+LastName;//xmlParserData.getValueOf("Self_Supp_Card_Embossing_Name");
		DCC_CamGenrateteLog.debug("Self_Supp_Card_Embossing_Name: " + Self_Supp_Card_Embossing_Name);
		
		String CIF = xmlParserData.getValueOf("ETB_CIFS");
		DCC_CamGenrateteLog.debug("CIF: " + CIF);
		
		String applicationNo = xmlParserData.getValueOf("Prospect_id");
		DCC_CamGenrateteLog.debug("applicationNo: " + applicationNo);
		
		String ReceivedDate = xmlParserData.getValueOf("Prospect_Creation_Date");
		DCC_CamGenrateteLog.debug("ReceivedDate: " + ReceivedDate);
		
		String bureau_reference_number = xmlParserData.getValueOf("bureau_reference_number");
		DCC_CamGenrateteLog.debug("bureau_reference_number: " + bureau_reference_number);
		
		String DBR_lifeStyle_expenses = xmlParserData.getValueOf("DBR_lifeStyle_expenses");
		DCC_CamGenrateteLog.debug("DBR_lifeStyle_expenses: " + DBR_lifeStyle_expenses);
		
		String RM_Code = xmlParserData.getValueOf("RM_Code");
		DCC_CamGenrateteLog.debug("RM_Code: " + RM_Code);
		
		String Expense1 = xmlParserData.getValueOf("Expense1");
		DCC_CamGenrateteLog.debug("Expense1: " + Expense1);
		
		String Expense2 = xmlParserData.getValueOf("Expense2");
		DCC_CamGenrateteLog.debug("Expense2: " + Expense2);
		
		String Expense3 = xmlParserData.getValueOf("Expense3");
		DCC_CamGenrateteLog.debug("Expense3: " + Expense3);
		
		String Dependents = xmlParserData.getValueOf("Dependents");
		DCC_CamGenrateteLog.debug("Dependents: " + Dependents);
		
		String Fatca = xmlParserData.getValueOf("Fatca");
		DCC_CamGenrateteLog.debug("Fatca: " + Fatca);
		
		String FTS_Ref_No = xmlParserData.getValueOf("FTS_Ref_No");
		DCC_CamGenrateteLog.debug("FTS_Ref_No: " + FTS_Ref_No);
		
		
		String FATCA_Tin_Number = xmlParserData.getValueOf("FATCA_Tin_Number");
		DCC_CamGenrateteLog.debug("FATCA_Tin_Number: " + FATCA_Tin_Number);
		
		String Tin_reason = xmlParserData.getValueOf("Tin_reason");
		DCC_CamGenrateteLog.debug("Tin_reason: " + Tin_reason);
		
		String Expense4 = xmlParserData.getValueOf("Expense4");
		DCC_CamGenrateteLog.debug("Expense4: " + Expense4);
		
		String No_earning_members = xmlParserData.getValueOf("No_earning_members");
		DCC_CamGenrateteLog.debug("No_earning_members: " + No_earning_members);
		
		String Industry = xmlParserData.getValueOf("Industry");
		DCC_CamGenrateteLog.debug("Industry: " + Industry);
		
		String Sub_Industry = xmlParserData.getValueOf("Sub_Industry");
		DCC_CamGenrateteLog.debug("Sub_Industry: " + Sub_Industry);
		
		String Selected_Card_Type = xmlParserData.getValueOf("Selected_Card_Type");
		DCC_CamGenrateteLog.debug("Selected_Card_Type: " + Selected_Card_Type);
		
		
		String Mother_Name = xmlParserData.getValueOf("Mother_Name");
		DCC_CamGenrateteLog.debug("Mother_Name: " + Mother_Name);
		
		
		String Earning_members = xmlParserData.getValueOf("Earning_members");
		DCC_CamGenrateteLog.debug("Earning_members: " + Earning_members);
		
		String Tax_Pay_in_oth_country = xmlParserData.getValueOf("Tax_Pay_in_oth_country");
		DCC_CamGenrateteLog.debug("Earning_members: " + Tax_Pay_in_oth_country);
		
		
		String Preferred_address = xmlParserData.getValueOf("Preferred_address");
		DCC_CamGenrateteLog.debug("Preferred_address: " + Preferred_address);
		
		
		String E_Registration = xmlParserData.getValueOf("E_Registration");
		DCC_CamGenrateteLog.debug("Preferred_address: " + E_Registration);
		
		String country_birth = "";
		country_birth = xmlParserData.getValueOf("country_birth");
		DCC_CamGenrateteLog.debug("Preferred_address: " + country_birth);
		
		String Place_birth = xmlParserData.getValueOf("Place_birth");
		DCC_CamGenrateteLog.debug("Place_birth: " + Place_birth);
		
		String Enroll_for_Falcon = xmlParserData.getValueOf("Enroll_for_Falcon");
		DCC_CamGenrateteLog.debug("Enroll_for_Falcon: " + Enroll_for_Falcon);
		
		String FYF =" ";
		FYF= xmlParserData.getValueOf("FYF");
		DCC_CamGenrateteLog.debug("FYF: " + FYF);
		
		String los = xmlParserData.getValueOf("los");
		DCC_CamGenrateteLog.debug("los: " + los);
		
		String FFP ="";
		FFP= xmlParserData.getValueOf("Skyward_Number");
		if(FFP.isEmpty()){
			FFP = " ";
		}
		DCC_CamGenrateteLog.debug("FFP: " + FFP);
		
		
		int totalExpen = Integer.parseInt(Expense1)+Integer.parseInt(Expense2)+Integer.parseInt(Expense3)+Integer.parseInt(Expense4);
		String totalExpense = String.valueOf(totalExpen);
		
		String card_application_date = xmlParserData.getValueOf("card_application_date");
		if(card_application_date!=null && card_application_date.length()>=10)
		card_application_date=card_application_date.substring(0,10); //CommonMethods.parseDate(card_application_date,"yyyy-MM-dd HH:mm:ss","dd-MM-yyyy");//2022-09-30 00:00:00
		DCC_CamGenrateteLog.debug("card_application_date: " + card_application_date);
		String Age = xmlParserData.getValueOf("Age");
		DCC_CamGenrateteLog.debug("Age: " + Age);
				
		
		String Address_Query ="select top 1 House_No,Building_Name, PO_Box_Address, City_Desc,Country_Desc,Address_Type from NG_DCC_GR_ADDRESS_DETAIL with (nolock) where (Address_Type='OFFICE' or Address_Type='RESIDENCE') and Wi_Name='" + processInstanceID + "'";
		String extTabDataIPXML_1 =CommonMethods.apSelectWithColumnNames(Address_Query, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCC_CamGenrateteLog, false));
		DCC_CamGenrateteLog.debug("extTabDataIPXML_1: " + extTabDataIPXML_1);
		String extTabDataOPXML_1 = CommonMethods.WFNGExecute(extTabDataIPXML_1, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
		DCC_CamGenrateteLog.debug("extTabDataOPXML_2: " + extTabDataOPXML_1);
		XMLParser xmlParserData_1 = new XMLParser(extTabDataOPXML_1);
		int iTotalrec_1 = Integer.parseInt(xmlParserData_1.getValueOf("TotalRetrieved"));
		String ZipCode = "";
		String City = "";
		String Country = "UAE";
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
				
				String AddressType = objWorkList.getVal("Address_Type");
				DCC_CamGenrateteLog.debug("AddressType: " + AddressType);
				DCC_CamGenrateteLog.debug("ZipCode: " + ZipCode);
				DCC_CamGenrateteLog.debug("City: " + City);
			
				DCC_CamGenrateteLog.debug("AddressType: " + AddressType);
					
				 ZipCode = objWorkList.getVal("PO_Box_Address");
				 City = objWorkList.getVal("City_Desc");
				 DCC_CamGenrateteLog.debug("ZipCode inside if : " + ZipCode);
				 DCC_CamGenrateteLog.debug("City: " + City);
					
				 if(!"".equalsIgnoreCase(objWorkList.getVal("Country_Desc"))){
					 Country = objWorkList.getVal("Country_Desc");
				 }
			}
		}

		
		if(!"".equalsIgnoreCase(employercode)){
			String employeQuery = "";
		}
		
		attrbList += "&<Title>&" + Title;
		attrbList += "&<First_Name>&" + FirstName;
		attrbList += "&<Middle_Name>&" + MiddleName;
		attrbList += "&<Last_Name>&" + LastName;
		attrbList += "&<Date_of_Birth>&" + dob;
		
		attrbList += "&<Nationality>&" + Nationality;
		attrbList += "&<Gender>&" + Gender_Code;
		attrbList += "&<MothersName>&" + Mother_Name;
		attrbList += "&<MaritalStatus>&" + MaritalStatus;
		attrbList += "&<NonResident>&" + "";
		attrbList += "&<ExpatGCC>&" + GCC_National;
		attrbList += "&<EmiratesID>&" + EmirateID;
		attrbList += "&<EmiratesIDExpiry>&" + EmID_Expiry;
		attrbList += "&<PassportNumber>&" + PassportNo;
		
		DCC_CamGenrateteLog.debug("attrbList1: " + attrbList);

		attrbList += "&<PassportExpiryDate>&" + Passport_expiry;
		attrbList += "&<VisaNo>&" + Visa_Number;
		attrbList += "&<VisaExpiryDate>&" + Visa_Expiry;
		attrbList += "&<MobileNumber>&" + MobileNo;
		attrbList += "&<PrimaryEmailID>&" + email_id;
		attrbList += "&<employerName>&" + Employer_Name;
		attrbList += "&<Type_of_Employment>&" + EmploymentType;
		attrbList += "&<Designation>&" + Designation;
		attrbList += "&<CardType>&" + Selected_Card_Type;
		attrbList += "&<CardProduct>&" + Card_Product;
		attrbList += "&<LimitRequested>&" + Requested_Limit;
		attrbList += "&<EnrollCreditShield>&" + Credit_Shield_Flag;
		attrbList += "&<CardEmbossingName>&" + Self_Supp_Card_Embossing_Name;
		attrbList += "&<SourceCode>&" + RM_Code;
		attrbList += "&<CIF>&" + CIF;
		attrbList += "&<ApplicationNo>& " + applicationNo;
		
		//DCC_CamGenrateteLog.debug("FinalIncome: " + FinalIncome + "DeclIncome: "+DeclIncome);
		attrbList += "&<BUREAUREFERENCENO>&" + bureau_reference_number;
		
		attrbList += "&<TotalLifestyleExpenses>&" + totalExpense;
		attrbList += "&<NoofEarningMembers>&" + No_earning_members;
		attrbList += "&<TotalHouseholdIncome>&" + Earning_members;
		attrbList += "&<NoOfDependents>&" + Dependents;
		
		attrbList += "&<US Citizen_TaxResident>&"+Fatca;
		attrbList +="&<another_country>&"+Tax_Pay_in_oth_country;
		attrbList +="&<TaxIdentificationNumber>&"+FATCA_Tin_Number;
		attrbList +="&<Reason>&"+Tin_reason;
		
		attrbList +="&<SendStatementTo>&"+Preferred_address;
		attrbList +="&<SMSOptOut>&"+"";
		attrbList +="&<DispatchChannel>&"+"998";
		attrbList +="&<ERegistration>&"+E_Registration;
		
		
		attrbList +="&<ReceivedDate>&"+ReceivedDate;
		attrbList +="&<FTSReferenceNumber>&"+FTS_Ref_No;
		attrbList +="&<Zip Code>&"+ZipCode;
		attrbList +="&<EmirateCity>&"+City;
		attrbList +="&<Country>&"+Country;
		attrbList +="&<PeriodinCurrentJob>&"+los;
		attrbList +="&<IndustrySubSegment>&"+Sub_Industry;
		attrbList +="&<IndustrySegment>&"+Industry;
		
		attrbList +="&<SecurityCheque>&"+applicationNo;
		attrbList +="&<CardCurrency>&"+"AED";
		
		attrbList +="&<Place_birth>&"+Place_birth;
		attrbList +="&<country_birth>&"+country_birth;
		
		attrbList +="&<EnrollforFalcon>&"+Enroll_for_Falcon;
		attrbList +="&<FYF>&"+FYF;
		attrbList +="&<FFP>&"+FFP;
		
		
		DCC_CamGenrateteLog.debug("attrbList2: " + attrbList);
		// **************//
		//attrbList += "&<NoofEarningMembers>&" + "";
		
		DCC_CamGenrateteLog.debug("attrbList3: " + attrbList);
		
		
		DCC_CamGenrateteLog.debug("attrbList" + attrbList);
		
		String output = makeSocketCall(attrbList, processInstanceID, pdfName, sessionId, gtIP,gtPort);
		
		DCC_CamGenrateteLog.debug("attrbList" + attrbList);
		DCC_CamGenrateteLog.debug("output" + output);
		
		String sWhereClause = "Wi_Name = '" + processInstanceID + "'";
		
		if(output.equalsIgnoreCase("Failure")){
			DCC_CamGenrateteLog.debug("Error in generating Template :");
			completeWorkItem(cabinetName,processInstanceID, "Failed", entryDateTime,"error while generating Template",WorkItemID, createdDateTime,applicationNo);
			sendMail(cabinetName,sessionID,processInstanceID,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),"","",ProcessDefId,"");

			return output;
			
		}else
		{
			String columnName = "IS_BSR_CAM_generated";
			String value = "'Y'";
			String extTableIPUpdateXml = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(),
			CommonConnection.getSessionID(DCC_CamGenrateteLog, false), "NG_DCC_EXTTABLE",columnName,value, sWhereClause);
			
			DCC_CamGenrateteLog.debug("Input XML for apUpdateInput for NG_DCC_EXTTABLE Table : " + extTableIPUpdateXml);
	
			String extTableOPUpdateXml = CommonMethods.WFNGExecute(extTableIPUpdateXml,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(), 1);
			DCC_CamGenrateteLog.debug("Output XML for apUpdateInput for NG_DCC_EXTTABLE Table : " + extTableOPUpdateXml);
	
			XMLParser sXMLParserChild = new XMLParser(extTableOPUpdateXml);
			String StrMainCode = sXMLParserChild.getValueOf("MainCode");
	
			if (StrMainCode.equals("0"))
			{
				DCC_CamGenrateteLog.debug("Successful in apUpdateInput the record in : NG_DCC_EXTTABLE");
				completeWorkItem(cabinetName,processInstanceID, "Success", entryDateTime,"BSR Template Generated",WorkItemID,createdDateTime,applicationNo);
			}
			else
			{
				completeWorkItem(cabinetName,processInstanceID, "Failed", entryDateTime,"error while generating Template",WorkItemID,createdDateTime,applicationNo);
				DCC_CamGenrateteLog.debug("Error in Executing apUpdateInput sOutputXML : " + sXMLParserChild);
				sendMail(cabinetName,sessionID,processInstanceID,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),"","",ProcessDefId,"");
			}
		}
		return output;

	}
	
	public String completeWorkItem(String cabinetName, String processInstanceID, String decision, String entryDateTime,String remarks,String WorkItemID,String createdDateTime,String applicationNo) {
		
		try {
			
			String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName,sessionID , processInstanceID, WorkItemID);
			String getWorkItemOutputXml = CommonMethods.WFNGExecute(getWorkItemInputXML,CommonConnection.getJTSIP(),
					CommonConnection.getJTSPort(), 1);

			DCC_CamGenrateteLog.debug("Output XML for getWorkItem is " + getWorkItemOutputXml);

			XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
			String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");

			if ("0".equals(getWorkItemMainCode)) {
				DCC_CamGenrateteLog.info("get Workitem call successfull for " + processInstanceID);
				String attrbuteTag = "<Decision>" + decision + "</Decision>";
				String assignWorkitemAttributeInputXML = CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionID, processInstanceID, WorkItemID, attrbuteTag);
				DCC_CamGenrateteLog.debug("Input XML for assign Attribute is " + assignWorkitemAttributeInputXML);

				String assignWorkitemAttributeOutputXML = CommonMethods.WFNGExecute(assignWorkitemAttributeInputXML,CommonConnection.getJTSIP(),
						CommonConnection.getJTSPort(), 1);
				DCC_CamGenrateteLog.debug("Output XML for assign Attribues is " + assignWorkitemAttributeOutputXML);

				XMLParser xmlParserAssignAtt = new XMLParser(assignWorkitemAttributeOutputXML);

				String mainCodeAssignAtt = xmlParserAssignAtt.getValueOf("MainCode");
				if ("0".equals(mainCodeAssignAtt.trim())) {
					String completeWorkItemInputXML = CommonMethods.completeWorkItemInput(cabinetName, sessionID, processInstanceID, WorkItemID);

					DCC_CamGenrateteLog.debug("Input XML for complete WI is " + completeWorkItemInputXML);

					DCC_CamGenrateteLog.debug("Input XML for wmcompleteWorkItem: " + completeWorkItemInputXML);

					String completeWorkItemOutputXML = CommonMethods.WFNGExecute(completeWorkItemInputXML,CommonConnection.getJTSIP(),
							CommonConnection.getJTSPort(), 1);
					DCC_CamGenrateteLog.debug("Output XML for wmcompleteWorkItem: " + completeWorkItemOutputXML);

					XMLParser xmlParserCompleteWorkitem = new XMLParser(completeWorkItemOutputXML);
					String completeWorkitemMaincode = xmlParserCompleteWorkitem.getValueOf("MainCode");
					DCC_CamGenrateteLog.debug("Status of wmcompleteWorkItem  " + completeWorkitemMaincode);

					if ("0".equals(completeWorkitemMaincode)) {
						// inserting into history table
						DCC_CamGenrateteLog.debug("WmCompleteWorkItem successful: " + completeWorkitemMaincode);
						// System.out.println(processInstanceID + "Complete Sussesfully with status "+objResponseBean.getIntegrationDecision());

						DCC_CamGenrateteLog.debug("WorkItem moved to next Workstep.");

						SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
						SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

						Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
						String formattedEntryDatetime = outputDateFormat.format(entryDatetimeFormat);
						DCC_CamGenrateteLog.debug("FormattedEntryDatetime: " + formattedEntryDatetime);

						Date actionDateTime = new Date();
						String formattedActionDateTime = outputDateFormat.format(actionDateTime);
						DCC_CamGenrateteLog.debug("FormattedActionDateTime: " + formattedActionDateTime);

						// Insert in WIHistory Table.
						
						String columnNames = "WI_NAME,dec_date,WORKSTEP,USER_NAME,DECISION,decision_date_time,Remarks";
						String columnValues = "'" + processInstanceID + "','" + formattedActionDateTime + "','Sys_ETB_init','" + CommonConnection.getUsername() + "','" + decision + "','"
								+ formattedEntryDatetime + "','"+remarks+"'";

						String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionID, columnNames, columnValues, "NG_DCC_GR_DECISION_HISTORY");
						DCC_CamGenrateteLog.debug("APInsertInputXML: " + apInsertInputXML);

						String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML,CommonConnection.getJTSIP(),
								CommonConnection.getJTSPort(), 1);
						DCC_CamGenrateteLog.debug("APInsertOutputXML: " + apInsertOutputXML);

						XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
						String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
						DCC_CamGenrateteLog.debug("Status of apInsertMaincode  " + apInsertMaincode);
						if (apInsertMaincode.equalsIgnoreCase("0")) {
							DCC_CamGenrateteLog.debug("ApInsert successful: " + apInsertMaincode);
							DCC_CamGenrateteLog.debug("Inserted in WiHistory table successfully.");
							
							DCC_CamGenrateteLog.debug("createdDateTime "+createdDateTime);
							
							if("Success".equalsIgnoreCase(decision)){
							String columnNames1 = "DCC_WI_No,dcc_WI_created_date_TIME,Prospect_ID";
							String columnValues1 = "'" + processInstanceID + "','" + createdDateTime + "','" + applicationNo+ "'";
							
							String apInsertInputXML1 = CommonMethods.apInsert(cabinetName, sessionID, columnNames1,
							columnValues1, "NG_DCC_BSR_update");
							DCC_CamGenrateteLog.debug("APInsertInputXML: " + apInsertInputXML1);
							
							String apInsertOutputXML1 = CommonMethods.WFNGExecute(apInsertInputXML1, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
							DCC_CamGenrateteLog.debug("APInsertOutputXML: " + apInsertInputXML1);
							XMLParser xmlParserAPInsert1 = new XMLParser(apInsertOutputXML1);
							String apInsertMaincode1 = xmlParserAPInsert1.getValueOf("MainCode");
							DCC_CamGenrateteLog.debug("Status of apInsertMaincode  " + apInsertMaincode1);
							if (apInsertMaincode1.equalsIgnoreCase("0")){
								DCC_CamGenrateteLog.debug("ApInsert successful: " + apInsertMaincode1);
								DCC_CamGenrateteLog.debug("Inserted in WiHistory table successfully.");
								
							}else{
								DCC_CamGenrateteLog.error("ApInsert failed: " + apInsertMaincode1);
							}
							}
						} else {
							DCC_CamGenrateteLog.error("ApInsert failed: " + apInsertMaincode);
						}
					} else {
						DCC_CamGenrateteLog.error("Error in completeWI call for " + processInstanceID);
					}
				} else {
					DCC_CamGenrateteLog.error("Error in Assign Attribute call for WI " + processInstanceID);
				}
			} else {
				DCC_CamGenrateteLog.error("Error in getWI call for WI " + processInstanceID);
			}
		}
		catch (Exception e) {
			DCC_CamGenrateteLog.error("Exception " + e);
			System.out.println("Exception " + e);

		}
		return "";
	}
	
	public void sendMail(String cabinetName, String sessionId ,String wiName,String jtsIp,String jtsPort,String ErrDesc, String return_code,String ProcessDefId,String MsgId)throws Exception
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
            	DCC_CamGenrateteLog.error("workitem name to send mail---"+wiName);
            	DCC_CamGenrateteLog.error("ErrorMsg to send mail---"+ErrDesc);
            	DCC_CamGenrateteLog.error("return_code to send mail---"+return_code); 
            	
            	String mailString = Digital_CC_BCR_GenCAMReport.MailStr;
            	
            	DCC_CamGenrateteLog.error("return_code to send mail---"+mailString); 
            	
            	String FinalMailStr = mailString.toString().replace("<WI_NAME>",wiName).replace("<ret_Code>",return_code)
            	.replace("<errormsg>",ErrDesc).replace("<MsgID>",MsgId);
            	
            	DCC_CamGenrateteLog.error("finalbody: "+FinalMailStr);
            	
            	String columnName="MAILFROM,MAILTO,MAILSUBJECT,MAILMESSAGE,MAILCONTENTTYPE,MAILPRIORITY,MAILSTATUS,INSERTEDBY,MAILACTIONTYPE,INSERTEDTIME,PROCESSDEFID,PROCESSINSTANCEID,WORKITEMID,ACTIVITYID,NOOFTRIALS";
            	String strValues="'"+Digital_CC_BCR_GenCAMReport.fromMailID+"','"+Digital_CC_BCR_GenCAMReport.toMailID+"','"+Digital_CC_BCR_GenCAMReport.mailSubject+"','"+FinalMailStr+"','text/html;charset=UTF-8','1','N','CUSTOM','TRIGGER','"+CommonMethods.getdateCurrentDateInSQLFormat()+"','"+ProcessDefId+"','"+wiName+"','1','1','0'";
                
            	sInputXML = "<?xml version=\"1.0\"?>" + "<APInsert_Input>" + "<Option>APInsert</Option>"
						+ "<TableName>WFMAILQUEUETABLE</TableName>" + "<ColName>" + columnName + "</ColName>"
						+ "<Values>" + strValues + "</Values>" + "<EngineName>" + cabinetName + "</EngineName>"
						+ "<SessionId>" + sessionID + "</SessionId>" + "</APInsert_Input>";
				
                DCC_CamGenrateteLog.error("Mail Insert InputXml::::::::::\n"+sInputXML);
                sOutputXML =CommonMethods.WFNGExecute(sInputXML, CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),0);
                DCC_CamGenrateteLog.error("Mail Insert OutputXml::::::::::\n"+sOutputXML);
                objXMLParser.setInputXML(sOutputXML);
                mainCodeforAPInsert=objXMLParser.getValueOf("MainCode");    
            }
			catch(Exception e)
            {
                e.printStackTrace();
                DCC_CamGenrateteLog.error("Exception in Sending mail", e);
                sessionCheckInt++;
                waiteloopExecute(waitLoop);
                continue;
            }
            if (mainCodeforAPInsert.equalsIgnoreCase("11")) 
            {
                DCC_CamGenrateteLog.error("Invalid session in Sending mail");
                sessionCheckInt++;
               
                sessionID=CommonConnection.getSessionID(Digital_CC_log.Digital_CC, true);
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
            DCC_CamGenrateteLog.error("mail Insert Successful");
            System.out.println("Mail Insert Successful for "+wiName+" in table WFMAILQUEUETABLE");
        }
        else
        {
            DCC_CamGenrateteLog.error("mail Insert Unsuccessful");
            System.out.println("Mail Insert Unsuccessful for "+wiName+"in table WFMAILQUEUETABLE");
        }
    }
	
	public void waiteloopExecute(long wtime) {
		try {
			for (int i = 0; i < 10; i++) {
				Thread.yield();
				Thread.sleep(wtime / 10);
			}
		} catch (InterruptedException e) {
		}
	}


	public String makeSocketCall(String argumentString, String wi_name, String docName, String sessionId, String gtIP,
			int gtPort) {
		String socketParams = argumentString + "~" + wi_name + "~" + docName + "~" + sessionId;

		System.out.println("socketParams -- " + socketParams);
		DCC_CamGenrateteLog.debug("socketParams" + socketParams);

		Socket template_socket = null;
		DataOutputStream template_dout = null;
		DataInputStream template_in = null;
		String result = "";
		try {
			// Socket write code started
			template_socket = new Socket(gtIP, gtPort);
			DCC_CamGenrateteLog.debug("template_socket" + template_socket);

			template_dout = new DataOutputStream(template_socket.getOutputStream());
			DCC_CamGenrateteLog.debug("template_dout" + template_dout);

			if (socketParams != null && socketParams.length() > 0) {
				int outPut_len = socketParams.getBytes("UTF-8").length;
				DCC_CamGenrateteLog.debug("outPut_len" + outPut_len);
				// CreditCard.mLogger.info("Final XML output len:
				// "+outPut_len +
				// "");
				socketParams = outPut_len + "##8##;" + socketParams;
				DCC_CamGenrateteLog.debug("socketParams--" + socketParams);
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
				DCC_CamGenrateteLog.debug("result--" + result);
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
