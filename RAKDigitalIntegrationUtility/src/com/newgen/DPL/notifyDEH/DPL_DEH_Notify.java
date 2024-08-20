package com.newgen.DPL.notifyDEH;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;

import com.newgen.DCC.EFMS.DCC_EFMS_Integration;
import com.newgen.DCC.Notify.DCCNotifyLog;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.DPL.Digital_PL_CommomMethod;
import com.newgen.DPL.Digital_PL_Log;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.omni.wf.util.excp.NGException;

public class DPL_DEH_Notify {

	private static String queueID = null;

	static NGEjbClient ngEjbClient;
	private static org.apache.log4j.Logger logger;
	Digital_PL_CommomMethod commomMethod = new Digital_PL_CommomMethod();
	private String NOTIFY_DEH_IDENTIFIER="";
	private String Notify_DEH_Identifier_child="";
	private String STL_Notify_Count="";

	public DPL_DEH_Notify() throws NGException{
		this.queueID = DCC_EFMS_Integration.DCCSystemIntegrationMap.get("dplqueueID");

		Digital_PL_Log.setLogger(getClass().getSimpleName());
		this.ngEjbClient = NGEjbClient.getSharedInstance();
		logger = Digital_PL_Log.getLogger(getClass().getSimpleName());
	}

	public void NotifyAppUtility(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String queueID, 
			int socketConnectionTimeOut, int integrationWaitTime, HashMap<String, String> socketDetailsMap)
	{
		final String ws_name="Sys_DEH_Notify";

		try
		{
			final HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();
			//Validate Session ID
			sessionId  = CommonConnection.getSessionID(logger, false);

			if (sessionId == null || sessionId.equalsIgnoreCase("") || sessionId.equalsIgnoreCase("null"))
			{
				logger.error("Could Not Get Session ID "+sessionId);
				return;
			}

			//Fetch all Work-Items on given queueID.
			logger.debug("Fetching all Workitems on DCCNotifyAPP queue");
			System.out.println("Fetching all Workitems on CIF_Update_Initial queue");

			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionId, queueID);
			logger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= CommonMethods.WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);
			logger.debug("WMFetchWorkList DCCNotifyAPP OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			logger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			logger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);

			logger.debug("Number of workitems retrieved on DCCNotifyAPP: "+fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on DCCNotifyAPP: "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0)
			{
				for(int i=0; i<fetchWorkitemListCount; i++)
				{
					String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

					logger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID=xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					logger.debug("Current ProcessInstanceID: "+processInstanceID);

					logger.debug("Processing Workitem: "+processInstanceID);
					System.out.println("\nProcessing Workitem: "+processInstanceID);

					String WorkItemID=xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					logger.debug("Current WorkItemID: "+WorkItemID);

					String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					logger.debug("Current EntryDateTime: "+entryDateTime);

					String ActivityName=xmlParserfetchWorkItemData.getValueOf("ActivityName");
					logger.debug("ActivityName: "+ActivityName);

					String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					logger.debug("ActivityID: "+ActivityID);
					String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					logger.debug("ActivityType: "+ActivityType);
					String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					logger.debug("ProcessDefId: "+ProcessDefId);


					String decisionValue="";
					String Notify_app_input_xml="";

					String DBQuery ="select a.CIF,a.IsNTB,a.IsFIRCOHit,a.FIRCO_ActionedDate,a.EFMS_Status,"
							+ "a.DectechDecision,a.Decision,a.FIRCO_Decision as ADDITIONAL_DOCUMENT_REQUIRED,"
							+ "a.ProspectID,a.NOTIFY_DEH_IDENTIFIER,a.Notify_DEH_Identifier_child,a.Nationality,CAST(a.EntryAt AS DATE) AS "
							+ "Introduction_Date,a.IsNSTP,a.FirstName,a.MiddleName,a.LastName,a.EmployerCode,"
							+ "a.EmployerName,a.FinalTAI,a.LoanAmount,a.STL_Notify_Count,a.RentOrMortgage,"
							+ "b.Lifestyle_Expenses,b.Bills,b.Others,a.LoanType,a.InstallmentPlan,a.TotalMonthlyHouseholdIncome,"
							+ "a.InterestRate,a.ProcessingFee,a.ProductType,a.Output_Affordable_EMI,a.Output_Interest_Rate,a.EMI,a.Tenure,"
							+ "a.Output_Stress_BufferAmt,a.Output_TotalDeduction,a.IsSTP,a.AffordabilityRatio,"
							+ "a.ApprovedLoanAmt,a.RequestedLoanTenor, a.PrevWS,a.InsuranceFee,a.PrevErrorWS from NG_DPL_EXTTABLE as a WITH(NOLOCK)"
							+ "INNER join NG_DPL_IncomeExpense b WITH(NOLOCK)on a.WINAME = b.WI_Name "
							+ "where a.WINAME='"+processInstanceID+"'";

					
					String extTabDataIPXML =CommonMethods.apSelectWithColumnNames(DBQuery, cabinetName, CommonConnection.getSessionID(logger, false));
					logger.debug("extTabDataIPXML: " + extTabDataIPXML);
					
					String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
					logger.debug("extTabDataOPXML: " + extTabDataOPXML);

					XMLParser xmlParserData = new XMLParser(extTabDataOPXML);

					String iTotalrec = xmlParserData.getValueOf("TotalRetrieved");

					if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec!=null && !"".equalsIgnoreCase(iTotalrec) && Integer.parseInt(iTotalrec) > 0)
					{
						String CIF = xmlParserData.getValueOf("CIF");
						Notify_DEH_Identifier_child =xmlParserData.getValueOf("Notify_DEH_Identifier_child");
						NOTIFY_DEH_IDENTIFIER=xmlParserData.getValueOf("NOTIFY_DEH_IDENTIFIER");
						STL_Notify_Count=xmlParserData.getValueOf("STL_Notify_Count");
						String prospect_id=xmlParserData.getValueOf("ProspectID");
						String ADDITIONAL_DOCUMENT_REQUIRED=xmlParserData.getValueOf("ADDITIONAL_DOCUMENT_REQUIRED");
						String Decision=xmlParserData.getValueOf("Decision");
						String firco=xmlParserData.getValueOf("IsFIRCOHit");
						String fircoAction=xmlParserData.getValueOf("FIRCO_ActionedDate");
						String efms=xmlParserData.getValueOf("EFMS_Status");
						String DectechDecision=xmlParserData.getValueOf("DectechDecision");
						String PrevWS=xmlParserData.getValueOf("PrevWS");
						String PrevErrorWS=xmlParserData.getValueOf("PrevErrorWS");
						String InsuranceFee=xmlParserData.getValueOf("InsuranceFee");
						String IsSTP=xmlParserData.getValueOf("IsSTP");
						String AffordabilityRatio=xmlParserData.getValueOf("AffordabilityRatio");
						String ApprovedLoanAmt=xmlParserData.getValueOf("ApprovedLoanAmt");
						String ProductType=xmlParserData.getValueOf("ProductType");
//						Harshit--changes
						
						String LIFESTYLE=xmlParserData.getValueOf("Lifestyle_Expenses");
						String RentOrMortgage=xmlParserData.getValueOf("RentOrMortgage");
						String Bills=xmlParserData.getValueOf("Bills");
						String Others = xmlParserData.getValueOf("Others");
						
						int CustExpense=0;
						int LIFESTYLEint=0;
						int RentOrMortgageint=0;
						int Billsint=0;
						int Otherint = 0;
						
						try{
							if(!AffordabilityRatio.isEmpty() || !"".equalsIgnoreCase(AffordabilityRatio) || AffordabilityRatio != "" || AffordabilityRatio != null){
								AffordabilityRatio=AffordabilityRatio.trim();
							}
							if(!LIFESTYLE.isEmpty() || !"".equalsIgnoreCase(LIFESTYLE) || LIFESTYLE != "" || LIFESTYLE != null){
								LIFESTYLEint = Integer.parseInt(LIFESTYLE);
							}
							if(!RentOrMortgage.isEmpty() || !"".equalsIgnoreCase(RentOrMortgage) || RentOrMortgage != "" || RentOrMortgage != null){
								RentOrMortgageint = Integer.parseInt(RentOrMortgage);
							}
							if(!Bills.isEmpty() || !"".equalsIgnoreCase(Bills) || Bills != "" || Bills != null){
								Billsint = Integer.parseInt(Bills);
							}
							if(!Others.isEmpty() || !"".equalsIgnoreCase(Others) || Others != "" || Others != null){
								Otherint = Integer.parseInt(Others);
							}
						}catch(Exception e){
							logger.debug("Error in AffordabilityRatio,LIFESTYLE,RentOrMortgage,Bills,Others: "+e.getMessage());
						}
						CustExpense = LIFESTYLEint+RentOrMortgageint+Billsint+Otherint;
						String CustomerDeclaredTotalExpenses=String.valueOf(CustExpense);
//						--end
						
						String AECB_MONTHLY=xmlParserData.getValueOf("Output_TotalDeduction");
						String STRESS=xmlParserData.getValueOf("Output_Stress_BufferAmt");
						String ASS_INCOME=xmlParserData.getValueOf("FinalTAI");
						String AFF_RATIO=xmlParserData.getValueOf("DBR_lifeStyle_expenses");
						//String cancel_in_cooling_period=xmlParserData.getValueOf("cancel_in_cooling_period");
						
						
						String LoanAmount=xmlParserData.getValueOf("LoanAmount");
						String Introduction_Date = xmlParserData.getValueOf("Introduction_Date");
						String FirstName = xmlParserData.getValueOf("FirstName");
						String MiddleName = xmlParserData.getValueOf("MiddleName");
						String LastName = xmlParserData.getValueOf("LastName");
						String FullName = "";
						String employercode = xmlParserData.getValueOf("EmployerCode");
						String Employer_Name = xmlParserData.getValueOf("EmployerName");
						String NTB=xmlParserData.getValueOf("IsNTB");

						if(!("").equalsIgnoreCase(MiddleName)){
							FullName = FirstName + " " + MiddleName + " " + LastName;
						}
						else if(("").equalsIgnoreCase(MiddleName)){
							FullName = FirstName + " " + LastName;
						} 
						
						//for offer letter
						String LoanType=xmlParserData.getValueOf("LoanType");
						String InstallmentPlan=xmlParserData.getValueOf("RequestedLoanTenor");
						String MonthlyInstallment= xmlParserData.getValueOf("Output_Affordable_EMI");
						String InterestRate = xmlParserData.getValueOf("Output_Interest_Rate");
						String ProcessingFee = xmlParserData.getValueOf("ProcessingFee");
						String EMI =xmlParserData.getValueOf("EMI");
						String tenure =xmlParserData.getValueOf("Tenure"); 
						
						//change InstallmentPlan to requestLoanTenor as per jira 774
						if(InstallmentPlan.isEmpty() || "".equalsIgnoreCase(InstallmentPlan) || InstallmentPlan == null){
							InstallmentPlan="0";
						}
						
						logger.debug("NOTIFY_DEH_IDENTIFIER: "+NOTIFY_DEH_IDENTIFIER);
						logger.debug("NOTIFY_DEH_IDENTIFIER_child: "+Notify_DEH_Identifier_child);
						
						logger.debug("prospect_id: "+prospect_id);
						logger.debug("ADDITIONAL_DOCUMENT_REQUIRED: "+ADDITIONAL_DOCUMENT_REQUIRED);

						if("Document Required".equalsIgnoreCase(ADDITIONAL_DOCUMENT_REQUIRED)){
							ADDITIONAL_DOCUMENT_REQUIRED = "Y";
						}
						logger.debug("Decision: "+Decision);
						
						

						if((NOTIFY_DEH_IDENTIFIER!=null && !"".equalsIgnoreCase(NOTIFY_DEH_IDENTIFIER)) || (!"".equalsIgnoreCase(Notify_DEH_Identifier_child)  && Notify_DEH_Identifier_child!=null))
						{
							if(("FIRCO_Exception".equalsIgnoreCase(NOTIFY_DEH_IDENTIFIER))||"FIRCO_Compliance".equalsIgnoreCase(NOTIFY_DEH_IDENTIFIER))
							{ 

								String actionDate="";
								SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");//yyyy-MM-dd HH:MM:ss
								actionDate=simpleDateFormat.format(new Date());

								String additioal_docs_details = "";
								logger.debug("Inside FIRCO actionDate : "+actionDate);

								// documents for notify_app - AdditionalDocumentDetails
								if (ADDITIONAL_DOCUMENT_REQUIRED != null && "Y".equalsIgnoreCase(ADDITIONAL_DOCUMENT_REQUIRED)) 
								{
	
									String DBQuery_doc ="select case when Document_Name='Other_Document' then Doc_Remarks else "
											+ "Document_Type end as Document_Name,Doc_Month, Doc_Year,Doc_Remarks from "
											+ "NG_DPL_GR_Additional_Documents with(nolock) where WI_name = "
											+ "'" + processInstanceID + "'";
									
									String docDataIPXML =CommonMethods.apSelectWithColumnNames(DBQuery_doc, cabinetName, CommonConnection.getSessionID(logger, false));
									logger.debug("extTabDataIPXML: " + docDataIPXML);
									String docDataOPXML = CommonMethods.WFNGExecute(docDataIPXML, CommonConnection.getJTSIP(),
											CommonConnection.getJTSPort(), 1);
									logger.debug("extTabDataOPXML: " + docDataOPXML);

									XMLParser xmlParserDocData = new XMLParser(docDataOPXML);

									int iTotalrec1 = Integer.parseInt(xmlParserDocData.getValueOf("TotalRetrieved"));

									if (xmlParserDocData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec1 > 0)
									{
										for (int j = 0; j < iTotalrec1; j++)
										{
											String fetchlistData=xmlParserDocData.getNextValueOf("Record");
											fetchlistData =fetchlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

											logger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
											XMLParser xmlParserfetchData = new XMLParser(fetchlistData);

											String monthName=xmlParserfetchData.getValueOf("Doc_Month");
											String year=xmlParserfetchData.getValueOf("Doc_Year");
											String docName=xmlParserfetchData.getValueOf("Document_Name");

											if(monthName!=null && !"".equalsIgnoreCase(monthName) && year!=null && !"".equalsIgnoreCase(year)&& monthName!=null && !"".equalsIgnoreCase(monthName))
											{
												String monthYear=getMonthNumber(monthName)+"-"+year;
												additioal_docs_details += "\t\t" +"<DocNameList>"+ "\n" +
														"\t\t\t" +"<DocumentName>"+xmlParserfetchData.getValueOf("document_name")+"</DocumentName>"+ "\n" +
														"\t\t\t" +"<MonthYear>"+monthYear+"</MonthYear>"+ "\n" +
														"\t\t" +"</DocNameList>";
											}
										}
									}

									String fileLocation=new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DPL_Integration")
											.append(System.getProperty("file.separator")).append("FIRCO_ADDN_DATA_PL.txt").toString();
									System.out.println("fileLocation"+fileLocation);

									BufferedReader sbf=new BufferedReader(new FileReader(fileLocation));

									System.out.println("fileLocation");
									StringBuilder sb=new StringBuilder();
									String line=sbf.readLine();
									while(line!=null)
									{
										sb.append(line);
										sb.append(System.lineSeparator());
										line=sbf.readLine();
									}
									Notify_app_input_xml=sb.toString();
									if(actionDate==null)
										actionDate="";
									Notify_app_input_xml=Notify_app_input_xml.replace("#WI_NAME#", processInstanceID);
									Notify_app_input_xml=Notify_app_input_xml.replace("#PROSPECTID#", prospect_id);
									Notify_app_input_xml=Notify_app_input_xml.replace("#DOCREQUIRED#", ADDITIONAL_DOCUMENT_REQUIRED);
									Notify_app_input_xml=Notify_app_input_xml.replace("#DOCUMENTLIST#", additioal_docs_details);
									Notify_app_input_xml=Notify_app_input_xml.replace("#ACTIONEDDATE#", actionDate);



									logger.debug("Notify_app_input_xml FIRCO: " + Notify_app_input_xml);

								}	      
							}
							else if("System_FircoDoc_RM_Uploaded".equalsIgnoreCase(NOTIFY_DEH_IDENTIFIER))
							{
								String fileLocation=new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DCC_Integration")
										.append(System.getProperty("file.separator")).append("FIRCO_DOCS_UPLOADED_RM.txt").toString();
								BufferedReader sbf=new BufferedReader(new FileReader(fileLocation));

								StringBuilder sb=new StringBuilder();
								String line=sbf.readLine();
								while(line!=null)
								{
									sb.append(line);
									sb.append(System.lineSeparator());
									line=sbf.readLine();
								}
								String actionDate="";
								String salary_docs_details = "";
								SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");//yyyy-MM-dd HH:MM:ss
								actionDate=simpleDateFormat.format(new Date());
								Notify_app_input_xml=sb.toString();
								Notify_app_input_xml=Notify_app_input_xml.replace("#WI_NAME#", processInstanceID);
								Notify_app_input_xml=Notify_app_input_xml.replace("#PROSPECTID#", prospect_id);
								Notify_app_input_xml=Notify_app_input_xml.replace("#ACTIONEDDATE#", actionDate);
								logger.debug("Notify_app_input_xml FIRCO_DOCS_UPLOADED_RM: " + Notify_app_input_xml);
							}
							else if("STL_Hold".equalsIgnoreCase(Notify_DEH_Identifier_child))
							{
								
								String actionDate="";
								SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");//yyyy-MM-dd HH:MM:ss
								actionDate=simpleDateFormat.format(new Date());

								String additioal_docs_details = "";
								logger.debug("Inside STL actionDate : "+actionDate);
								
								String STLQuery= "select STLDocumentList from ng_dpl_exttable with(NOLOCK) where winame='"+ processInstanceID + "'";
								
								String STLAPXML =CommonMethods.apSelectWithColumnNames(STLQuery, cabinetName, CommonConnection.getSessionID(logger, false));
								logger.debug("extTabDataIPXML: " + STLAPXML);
								
								String STLDocList = CommonMethods.WFNGExecute(STLAPXML, CommonConnection.getJTSIP(),
										CommonConnection.getJTSPort(), 1);
								logger.debug("extTabDataOPXML: " + STLDocList);

								XMLParser xmlParserDocData = new XMLParser(STLDocList);
								int iTotalrec1 = Integer.parseInt(xmlParserDocData.getValueOf("TotalRetrieved"));

								if (xmlParserDocData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec1 > 0)
								{
									//additioal_docs_details= xmlParserDocData.getValueOf("STLDocumentList");
									
									String fileLocation=new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DPL_Integration")
											.append(System.getProperty("file.separator")).append("STL_DOCS_REQD_PL.txt").toString();
									System.out.println("fileLocation"+fileLocation);

									BufferedReader sbf=new BufferedReader(new FileReader(fileLocation));

									System.out.println("fileLocation");
									StringBuilder sb=new StringBuilder();
									String line=sbf.readLine();
									while(line!=null)
									{
										sb.append(line);
										sb.append(System.lineSeparator());
										line=sbf.readLine();
									}
									Notify_app_input_xml=sb.toString();
									if(actionDate==null)
										actionDate="";
									
									additioal_docs_details += "\t\t" +"<DocNameList>"+ "\n" +
											"\t\t\t" +"<DocumentName></DocumentName>"+ "\n" +
											"\t\t\t" +"<MonthYear></MonthYear>"+ "\n" +
											"\t\t" +"</DocNameList>";
									Notify_app_input_xml=Notify_app_input_xml.replace("#WI_NAME#", processInstanceID);
									Notify_app_input_xml=Notify_app_input_xml.replace("#PROSPECTID#", prospect_id);
									Notify_app_input_xml=Notify_app_input_xml.replace("#DOCREQUIRED#", ADDITIONAL_DOCUMENT_REQUIRED);
									Notify_app_input_xml=Notify_app_input_xml.replace("#DOCUMENTLIST#", additioal_docs_details);
									Notify_app_input_xml=Notify_app_input_xml.replace("#ACTIONEDDATE#", actionDate);

									
								}	      

							}
							else if("AccountNotifyDEH".equalsIgnoreCase(NOTIFY_DEH_IDENTIFIER))
							{
								
								String actionDate="";
								SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");//yyyy-MM-dd HH:MM:ss
								actionDate=simpleDateFormat.format(new Date());

								String additioal_docs_details = "";
								logger.debug("Inside account update actionDate : "+actionDate);

									String fileLocation=new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DPL_Integration")
											.append(System.getProperty("file.separator")).append("PL_ACCOUNT_UPDATE.txt").toString();
									System.out.println("fileLocation"+fileLocation);

									BufferedReader sbf=new BufferedReader(new FileReader(fileLocation));

									System.out.println("fileLocation");
									StringBuilder sb=new StringBuilder();
									String line=sbf.readLine();
									while(line!=null)
									{
										sb.append(line);
										sb.append(System.lineSeparator());
										line=sbf.readLine();
									}
									Notify_app_input_xml=sb.toString();
									if(actionDate==null)
										actionDate="";
									
									Notify_app_input_xml=Notify_app_input_xml.replace("#WI_NAME#", processInstanceID);
									Notify_app_input_xml=Notify_app_input_xml.replace("#PROSPECTID#", prospect_id);
									Notify_app_input_xml=Notify_app_input_xml.replace("#ACTIONEDDATE#", actionDate);
									
									Notify_app_input_xml=Notify_app_input_xml.replace("#TOTALASSESSEDINCOME#", ASS_INCOME);
									Notify_app_input_xml=Notify_app_input_xml.replace("#APPROVEDLOANAMOUNT#", LoanAmount);
									Notify_app_input_xml=Notify_app_input_xml.replace("#FIRSTNAME#", FirstName);
									Notify_app_input_xml=Notify_app_input_xml.replace("#MIDDLENAME#", MiddleName);
									Notify_app_input_xml=Notify_app_input_xml.replace("#LASTNAME#", LastName);

							

									logger.debug("Notify_app_input_xml FIRCO: " + Notify_app_input_xml);

									     
							
							}
							else if("Decline_Prospect".equalsIgnoreCase(NOTIFY_DEH_IDENTIFIER))
							{
								 // WS from system assign cif
				    			String actionDate="";
				    			
				    			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");//YYYY-MM-DD
				    			actionDate=simpleDateFormat.format(new Date());
				    			

					    		String fileLocation=new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DPL_Integration")
		    		    		.append(System.getProperty("file.separator")).append("Decline_PROS_CLOSURE.txt").toString();
							    BufferedReader sbf=new BufferedReader(new FileReader(fileLocation));
					    		
					    		StringBuilder sb=new StringBuilder();
					    		String line=sbf.readLine();
					    		while(line!=null)
					    		{
					    			sb.append(line);
					    			sb.append(System.lineSeparator());
					    			line=sbf.readLine();
					    		}
					    		String Dectec_Dec = "N";
					    		String EFMS_Status = "N";
					    		String FIRCO_Status = "N";
					    		
					    		String query = "select StatusBehavior from NG_RLOS_FIRCO with(NOLOCK) where workitem_no='"+processInstanceID+"'";
					    		String extTabDataXML =CommonMethods.apSelectWithColumnNames(query, cabinetName, CommonConnection.getSessionID(logger, false));
								logger.debug("extTabDataIPXML: " + extTabDataXML);
								
								String extOPXML = CommonMethods.WFNGExecute(extTabDataXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
								logger.debug("extTabDataOPXML: " + extTabDataOPXML);

								XMLParser xmlParsData = new XMLParser(extOPXML);

								String totalrec = xmlParsData.getValueOf("TotalRetrieved");

								if (xmlParsData.getValueOf("MainCode").equalsIgnoreCase("0") && totalrec!=null && !"".equalsIgnoreCase(totalrec) && Integer.parseInt(totalrec) > 0)
								{
									String StatusBehavior = xmlParsData.getValueOf("StatusBehavior");
									logger.debug("StatusBehavior: " + StatusBehavior);

									if("FFFBAD".equalsIgnoreCase(StatusBehavior)){
										FIRCO_Status = "Y";
									}

								}
					    		
					    		if((("D".equalsIgnoreCase(DectechDecision) || "R".equalsIgnoreCase(DectechDecision)) && ("Y".equalsIgnoreCase(IsSTP))) || "Exception".equalsIgnoreCase(PrevWS))
					    		{
					    			Dectec_Dec = "Y";
					    		}
					    		
					    		if("Confirmed Fraud".equalsIgnoreCase(efms) || "Negative".equalsIgnoreCase(efms)) //To be changes as part of EFMS change 22112023 PDSC-1073
					    		{
					    			EFMS_Status="Y";
					    		}
					    		
					    		Notify_app_input_xml=sb.toString();
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#WI_NAME#", processInstanceID);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#PROSPECTID#", prospect_id);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#Dectec_Dec#", Dectec_Dec);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#EFMS_Status#", EFMS_Status);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#FIRCO_Status#", FIRCO_Status);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#Lifestyle_Expenses#", CustomerDeclaredTotalExpenses);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#output_totalDeduction#", AECB_MONTHLY);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#StressDBR#", STRESS);
					    		
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#Output_TAI#", ASS_INCOME);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#totalDeduction#", AECB_MONTHLY);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#disposableIncome#", "");
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#AAR#", AffordabilityRatio);
					    		
					    		logger.debug("Notify_app_input_xml DECLINE_PROS_CLOSURE: " + Notify_app_input_xml);
					    		
					    		
				    		
							}
							
							//-----harshit changes start
							else if("EXPIRY_PROSPECT".equalsIgnoreCase(NOTIFY_DEH_IDENTIFIER))
							{

								 // WS from system assign cif
				    			String actionDate="";
				    			
				    			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");//YYYY-MM-DD
				    			actionDate=simpleDateFormat.format(new Date());
				    			

					    		String fileLocation=new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DPL_Integration")
		    		    		.append(System.getProperty("file.separator")).append("EXPIRED_PROS_CLOSURE.txt").toString();
							    BufferedReader sbf=new BufferedReader(new FileReader(fileLocation));
					    		
					    		StringBuilder sb=new StringBuilder();
					    		String line=sbf.readLine();
					    		while(line!=null)
					    		{
					    			sb.append(line);
					    			sb.append(System.lineSeparator());
					    			line=sbf.readLine();
					    		}

					    		String deliveryClosure="N";
				    			String coolingPeriod= "N";
				    			String FircoExpiry="N";
					    		
					    		if("Courier_Hold".equalsIgnoreCase(PrevErrorWS))
					    		{
					    			deliveryClosure="Y";
					    		}
					    		
					    		if("Offer_Letter".equalsIgnoreCase(PrevErrorWS))
					    		{
					    			coolingPeriod="Y";
					    			
					    		}
					    		if("Offer_Letter".equalsIgnoreCase(PrevWS) && "Expiry".equalsIgnoreCase(Decision))
					    		{
					    			FircoExpiry="Y";
					    			
					    		}
					    		else if("WI_Update_Hold".equalsIgnoreCase(PrevWS) && "Expiry".equalsIgnoreCase(Decision))
					    		{
					    			FircoExpiry="Y";
					    			
					    		}
					    		
					    		Notify_app_input_xml=sb.toString();
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#WI_NAME#", processInstanceID);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#PROSPECTID#", prospect_id);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#Lifestyle_Expenses#", CustomerDeclaredTotalExpenses);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#FircoException#", FircoExpiry);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#Output_Stress_BufferAmt#", STRESS);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#deliveryClosure#", deliveryClosure);
				    			Notify_app_input_xml=Notify_app_input_xml.replace("#coolingPeriod#", coolingPeriod);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#Output_TAI#", ASS_INCOME);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#output_totalDeduction#", AECB_MONTHLY);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#Output_affordable_ratio#", AffordabilityRatio);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#CardType#", ProductType);
					    		
					    		logger.debug("Notify_app_input_xml Expire_Prospect: " + Notify_app_input_xml);
					    		
					    		System.out.println("Notify_app_input_xml Expire_Prospect: " + Notify_app_input_xml);
				    		
							
							}
							//-----harshit changes end
							else if("CLOSURE_Prospect".equalsIgnoreCase(NOTIFY_DEH_IDENTIFIER))
							{
								 // WS from system assign cif
				    			String actionDate="";
				    			
				    			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");//YYYY-MM-DD
				    			actionDate=simpleDateFormat.format(new Date());
				    			

					    		String fileLocation=new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DPL_Integration")
		    		    		.append(System.getProperty("file.separator")).append("CANCEL_PROS_CLOSURE.txt").toString();
							    BufferedReader sbf=new BufferedReader(new FileReader(fileLocation));
					    		
					    		StringBuilder sb=new StringBuilder();
					    		String line=sbf.readLine();
					    		while(line!=null)
					    		{
					    			sb.append(line);
					    			sb.append(System.lineSeparator());
					    			line=sbf.readLine();
					    		}
					    		String Dectec_Dec = "N";
					    		String EFMS_Status = "N";
					    		String FIRCO_Status = "Y";
					    		if(("D".equalsIgnoreCase(DectechDecision) || "R".equalsIgnoreCase(DectechDecision)) && ("Y".equalsIgnoreCase(IsSTP)))
					    		{
					    			Dectec_Dec = "Y";
					    		}
					    		
					    		if("Confirmed Fraud".equalsIgnoreCase(efms) || "Negative".equalsIgnoreCase(efms)) //To be changes as part of EFMS change 22112023 PDSC-1073
					    		{
					    			EFMS_Status="Y";
					    		}

					    		String deliveryClosure="N";
				    			String coolingPeriod= "N";
					    		//if decision, Harshit changes
				    			//after reject on offer letter wi move to ops and cancel propest execuite
					    		//if("Courier_Hold".equalsIgnoreCase(PrevWS) && "Reject".equalsIgnoreCase(Decision))
					    		if("Courier_Hold".equalsIgnoreCase(PrevErrorWS))
					    		{
					    			deliveryClosure="Y";
					    			
					    			
					    		}
					    		
					    		//if("Offer_Letter".equalsIgnoreCase(PrevErrorWS) && "Declined".equalsIgnoreCase(Decision))
					    		if("Offer_Letter".equalsIgnoreCase(PrevErrorWS))
					    		{
					    			coolingPeriod="Y";
					    			
					    		}
					    		
					    		
					    		
					    		Notify_app_input_xml=sb.toString();
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#WI_NAME#", processInstanceID);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#PROSPECTID#", prospect_id);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#Lifestyle_Expenses#", CustomerDeclaredTotalExpenses);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#output_totalDeduction#", AECB_MONTHLY);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#StressDBR#", STRESS);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#deliveryClosure#", deliveryClosure);
				    			Notify_app_input_xml=Notify_app_input_xml.replace("#coolingPeriod#", coolingPeriod);
					    		
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#Output_TAI#", ASS_INCOME);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#totalDeduction#", AECB_MONTHLY);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#disposableIncome#", "");
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#AAR#", AffordabilityRatio);
					    		
					    		logger.debug("Notify_app_input_xml DECLINE_PROS_CLOSURE: " + Notify_app_input_xml);
					    		
					    		
				    		
							}
							else if("LOAN_PROS_CLOSURE".equalsIgnoreCase(NOTIFY_DEH_IDENTIFIER))
							{
								String actionDate="";
				    			
				    			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");//YYYY-MM-DD
				    			actionDate=simpleDateFormat.format(new Date());
				    			

					    		String fileLocation=new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DPL_Integration")
		    		    		.append(System.getProperty("file.separator")).append("LOAN_PROS_CLOSURE.txt").toString();
							    BufferedReader sbf=new BufferedReader(new FileReader(fileLocation));
					    		
					    		StringBuilder sb=new StringBuilder();
					    		String line=sbf.readLine();
					    		while(line!=null)
					    		{
					    			sb.append(line);
					    			sb.append(System.lineSeparator());
					    			line=sbf.readLine();
					    		}
					    		String Dectec_Dec = "N";
					    		String EFMS_Status = "N";
					    		String FIRCO_Status = "Y";
					    		if(("D".equalsIgnoreCase(DectechDecision) || "R".equalsIgnoreCase(DectechDecision)) && ("Y".equalsIgnoreCase(IsSTP)))
					    		{
					    			Dectec_Dec = "Y";
					    		}
					    		
					    		if("Confirmed Fraud".equalsIgnoreCase(efms) || "Negative".equalsIgnoreCase(efms)) //To be changes as part of EFMS change 22112023 PDSC-1073
					    		{
					    			EFMS_Status="Y";
					    		}

					    		String deliveryClosure="N";
				    			String coolingPeriod= "N";
					    		if("Courier_Hold".equalsIgnoreCase(PrevErrorWS))
					    		{
					    			deliveryClosure="Y";
					    		}
					    		if("Offer_Letter".equalsIgnoreCase(PrevErrorWS))
					    		{
					    			coolingPeriod="Y";
					    		}
					    		
					    		Notify_app_input_xml=sb.toString();
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#WI_NAME#", processInstanceID);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#PROSPECTID#", prospect_id);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#Lifestyle_Expenses#", CustomerDeclaredTotalExpenses);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#output_totalDeduction#", AECB_MONTHLY);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#StressDBR#", STRESS);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#deliveryClosure#", deliveryClosure);
				    			Notify_app_input_xml=Notify_app_input_xml.replace("#coolingPeriod#", coolingPeriod);
					    		
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#Output_TAI#", ASS_INCOME);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#totalDeduction#", AECB_MONTHLY);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#disposableIncome#", "");
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#AAR#", AffordabilityRatio);
					    		
					    		logger.debug("Notify_app_input_xml DECLINE_PROS_CLOSURE: " + Notify_app_input_xml);
					    		
							}

							else if("OFFER_LETTER_DECISION".equalsIgnoreCase(NOTIFY_DEH_IDENTIFIER)){
								String actionDate="";
				    			
				    			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");//YYYY-MM-DD
				    			actionDate=simpleDateFormat.format(new Date());
				    			

					    		String fileLocation=new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DPL_Integration")
		    		    		.append(System.getProperty("file.separator")).append("OFFER_LETTER_DECISION.txt").toString();
							    BufferedReader sbf=new BufferedReader(new FileReader(fileLocation));
					    		
					    		StringBuilder sb=new StringBuilder();
					    		String line=sbf.readLine();
					    		while(line!=null)
					    		{
					    			sb.append(line);
					    			sb.append(System.lineSeparator());
					    			line=sbf.readLine();
					    		}
					    		
					    		Notify_app_input_xml=sb.toString();
//					    		String type = "";
//					    		String amount="";
//					    		if("I".equalsIgnoreCase(ProductType)||"ISL".equalsIgnoreCase(ProductType)||"Islamic".equalsIgnoreCase(ProductType)){
//					    			type = "<LoanType>"+LoanType+"</LoanType>";
//					    			amount="<LoanAmount>"+LoanAmount+"</LoanAmount>";
//					    		}else{
//					    			type = "<FinanceType>"+""+"</FinanceType>";
//					    			amount="<SalamFinanceAmount>"+LoanAmount+"</SalamFinanceAmount>";
//					    		}
					    		if(tenure.isEmpty() || "".equalsIgnoreCase(tenure) || tenure == null){
									tenure="0";
								}
					    		if(EMI.isEmpty() || "".equalsIgnoreCase(EMI) || EMI == null){
					    			EMI="0";
								}
					    		logger.debug("tenure;:"+tenure);
					    		logger.debug("EMI;:"+EMI);
								double intTenure = Double.parseDouble(tenure);
								double intEMI = Double.parseDouble(EMI);
								logger.debug("intTenure:"+intTenure);
					    		logger.debug("intEMI:"+intEMI);
					    		
					    		double total =intEMI*intTenure;
					    		String TotalRepaymentAmt = String.valueOf(total);
					    		if(TotalRepaymentAmt.length()>9){
					    			TotalRepaymentAmt=TotalRepaymentAmt.substring(0, 9);
					    		}
					    		
					    		logger.debug("TotalRepaymentAmt " + TotalRepaymentAmt);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#WI_NAME#", processInstanceID);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#PROSPECTID#", prospect_id);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#CustomerName#",FullName );
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#LoanType#", "New");
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#LoanAmount#", LoanAmount);
					    		//loanamount currency field not present in external table.
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#LoanAmountCurrency#", "AED");
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#TotalInterest#", "");
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#InsuranceFees#", InsuranceFee);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#TotalRepaymentAmt#", TotalRepaymentAmt);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#InstallmentTerm#", InstallmentPlan);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#MonthlyInstallment#", EMI);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#InterestRate#", InterestRate);
					    		
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#ProcessingFees#",ProcessingFee );
					    		
					    		
					    		//this field add after discussion of nasima
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#Lifestyle_Expenses#", CustomerDeclaredTotalExpenses);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#output_totalDeduction#", AECB_MONTHLY);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#StressDBR#", STRESS);
					    		
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#Output_TAI#", ASS_INCOME);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#totalDeduction#", AECB_MONTHLY);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#disposableIncome#", "");
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#AAR#", AffordabilityRatio);
					    		
					    		
					    		logger.debug("Notify_app_input_xml DECLINE_PROS_CLOSURE: " + Notify_app_input_xml);
					    		
							}
							else if("NEW_LOAN_CREATION".equalsIgnoreCase(NOTIFY_DEH_IDENTIFIER))
							{
								String actionDate="";
				    			
				    			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");//YYYY-MM-DD
				    			actionDate=simpleDateFormat.format(new Date());
				    			

					    		String fileLocation=new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DPL_Integration")
		    		    		.append(System.getProperty("file.separator")).append("NEW_LOAN_CREATION.txt").toString();
							    BufferedReader sbf=new BufferedReader(new FileReader(fileLocation));
					    		
					    		StringBuilder sb=new StringBuilder();
					    		String line=sbf.readLine();
					    		while(line!=null)
					    		{
					    			sb.append(line);
					    			sb.append(System.lineSeparator());
					    			line=sbf.readLine();
					    		}
					    		
					    		Notify_app_input_xml=sb.toString();
								Notify_app_input_xml=Notify_app_input_xml.replace("#WI_NAME#", processInstanceID);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#PROSPECTID#", prospect_id);
					    		Notify_app_input_xml=Notify_app_input_xml.replace("#ApplicationID#", processInstanceID.substring(10,20));
							}
							
						}
					}

					logger.debug("Notify_appliation: " + Notify_app_input_xml);

					String integrationStatus="Success";
					String attributesTag;
					String ErrDesc = "";
					StringBuilder finalString=new StringBuilder();
					finalString = finalString.append(Notify_app_input_xml);
					//changes need to done to updae the correct flag
					//HashMap<String, String> socketConnectionMap =socketConnectionDetails(cabinetName, sJtsIp, iJtsPort, sessionId); 
					Digital_PL_CommomMethod commomMethod = new Digital_PL_CommomMethod();
					integrationStatus = commomMethod.socketConnection(cabinetName, CommonConnection.getUsername(), sessionId, sJtsIp, iJtsPort,
							processInstanceID, ws_name, 60, 65,socketDetailsMap, finalString);

					XMLParser xmlParserSocketDetails= new XMLParser(integrationStatus);
					logger.debug(" xmlParserSocketDetails : "+xmlParserSocketDetails);
					String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
					logger.debug("Return Code: "+return_code+ "WI: "+processInstanceID);
					String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
					logger.debug("return_desc : "+return_desc+ "WI: "+processInstanceID);

					String MsgId ="";
					int STLCount = 0;
					if (integrationStatus.contains("<MessageId>"))
						MsgId = xmlParserSocketDetails.getValueOf("MessageId");

					logger.debug("MsgId : "+MsgId+" for WI: "+processInstanceID);

					if(return_code.equalsIgnoreCase("0000"))
					{
						integrationStatus="Success";
						ErrDesc = "Notify Done Successfully";
						decisionValue = "Success";
						logger.debug("Decision in success: " +decisionValue);
						attributesTag="<Decision>"+decisionValue+"</Decision>";
						
						if("STL_Hold".equalsIgnoreCase(NOTIFY_DEH_IDENTIFIER)){
							logger.debug("NOTIFY_DEH_IDENTIFIER."+NOTIFY_DEH_IDENTIFIER);
							if("".equalsIgnoreCase(STL_Notify_Count)){
								logger.debug("STL_Notify_Count is null");
								STLCount=1;
							}else{
								STLCount=Integer.parseInt(STL_Notify_Count);
								logger.debug("STL_Notify_Count:"+STLCount);
								STLCount++;
								logger.debug("STL_Notify_Count:"+STLCount);

							}
							
						}else{
							
						}
					}
					else
					{
						integrationStatus="Failed";
						ErrDesc = "Error in Notify DEH";
						ErrDesc = return_desc;
						decisionValue = "Failed";
						logger.debug("Decision in else : " +decisionValue);
						attributesTag="<Decision>"+decisionValue+"</Decision>";

					}
					//To be modified according to output of Integration Call.

					String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID,WorkItemID);
					String getWorkItemOutputXml = CommonMethods.WFNGExecute(getWorkItemInputXML,sJtsIp,iJtsPort,1);
					logger.debug("Output XML For WmgetWorkItemCall: " + getWorkItemOutputXml);

					XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
					String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
					logger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);

					if (getWorkItemMainCode.trim().equals("0") || true )
					{
						logger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);

						String assignWorkitemAttributeInputXML = "<?xml version=\"1.0\"?><WMAssignWorkItemAttributes_Input>"
								+ "<Option>WMAssignWorkItemAttributes</Option>"
								+ "<EngineName>"+cabinetName+"</EngineName>"
								+ "<SessionId>"+sessionId+"</SessionId>"
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

						logger.debug("InputXML for assignWorkitemAttribute Call Notify: "+assignWorkitemAttributeInputXML);

						String assignWorkitemAttributeOutputXML=CommonMethods.WFNGExecute(assignWorkitemAttributeInputXML,sJtsIp,
								iJtsPort,1);

						logger.debug("OutputXML for assignWorkitemAttribute Call Notify: "+assignWorkitemAttributeOutputXML);

						XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
						String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
						logger.debug("AssignWorkitemAttribute MainCode: "+assignWorkitemAttributeMainCode);

						if(assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0"))
						{
							logger.debug("AssignWorkitemAttribute Successful: "+assignWorkitemAttributeMainCode);
							System.out.println(processInstanceID + "Complete Succesfully with status "+decisionValue);
							logger.debug("WorkItem moved to next Workstep.");
						}
						else
						{
							logger.debug("decisionValue : "+decisionValue);
						}

						DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

						Date current_date = new Date();
						String formattedActionDatetime=dateFormat.format(current_date);
						logger.debug("FormattedActionDatetime: "+formattedActionDatetime);

						SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
						SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

						Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
						String formattedEntryDatetime = outputDateFormat.format(entryDatetimeFormat);
						logger.debug("FormattedEntryDatetime: " + formattedEntryDatetime);

						String columnNames="WI_NAME,Decision_Date_Time,WORKSTEP,USERNAME,DECISION,ENTRY_DATE_TIME,REMARKS";
						String columnValues="'"+processInstanceID+"','"+formattedEntryDatetime+"','"+ActivityName+"','"
								+CommonConnection.getUsername()+"','"+decisionValue+"','"+formattedActionDatetime+"','"+ErrDesc+"'";

						String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionId, columnNames, columnValues,"NG_DPL_GR_DECISION_HISTORY");
						logger.debug("APInsertInputXML: "+apInsertInputXML);

						String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
						logger.debug("APInsertOutputXML: "+ apInsertInputXML);

						XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
						String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
						logger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

						logger.debug("Completed On "+ ActivityName);

						if(apInsertMaincode.equalsIgnoreCase("0"))
						{
							logger.debug("ApInsert successful: "+apInsertMaincode);
							logger.debug("Inserted in WiHistory table successfully.");
							
							String trTableColumn = "";
							String trTableValue = "";
							if("STL_Hold".equalsIgnoreCase(NOTIFY_DEH_IDENTIFIER)){
								logger.debug("NOTIFY_DEH_IDENTIFIER."+NOTIFY_DEH_IDENTIFIER);
								logger.debug("STLCount:"+STLCount);
								trTableColumn = "STLDocProvided,STL_Notify_Count";
								trTableValue = "'N','"+STLCount+"'";
							}else{
								trTableColumn = "IsFTSDocProvided";
								trTableValue = "'N'";
							}
						
							String whereClause = "winame='"+processInstanceID+"'";

							String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false), 
	  		    	        		"NG_DPL_EXTTABLE", trTableColumn, trTableValue, whereClause);
	  		    	        logger.debug("Input XML for apUpdateInput for " + "NG_DPL_EXTTABLE" + " Table : " + inputXML);
	  		    	        String outputXml = CommonMethods.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
	  		    	        logger.debug("Output XML for apUpdateInput for " + "NG_DPL_EXTTABLE" + " Table : " + outputXml);
	  		    	        
							
							xmlParserAPInsert = new XMLParser(apInsertOutputXML);
							apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
							logger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

							logger.debug("Completed On "+ ActivityName);

							if(apInsertMaincode.equalsIgnoreCase("0")){
								logger.debug("APUpdate successful: "+apInsertMaincode);
								logger.debug("Updated in external table successfully.");
							}
							else
							{
								logger.debug("APUpdate failed: "+apInsertMaincode);
							}
						}
						else
						{
							logger.debug("ApInsert failed: "+apInsertMaincode);
						}
					}
					else
					{
						getWorkItemMainCode="";
						logger.debug("WmgetWorkItem failed: "+getWorkItemMainCode);
					}
				}
			}
		}
		catch (Exception e)

		{
			logger.debug("Exception: "+e.getMessage());
		}
	}
	/*private String insertIntoExtTable(String tableName,String colName,String colValues,String sSessionID,
			String whereClause,String cabinetName,String sJtsIp,String iJtsPort){
		String sInputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false), 
	        		tableName, colName, colValues, whereClause);
		//String sInputXML=commomMethod.getAPUpdateInputXML(tableName,colName,colValues,sSessionID,whereClause,cabinetName);
		logger.debug("The input XML getDBUpdateDocsReqdFlagDCC "+sInputXML);
		String sOutputXML = CommonMethods.WFNGExecute(sInputXML,sJtsIp,iJtsPort);
		logger.debug("APInsert Output for transaction update table getDBUpdateDocsReqdFlagD: " + sOutputXML);
		return sOutputXML;
	}*/
	private String getMonthNumber(String month)
	{
		String ans="";
		try
		{
			switch(month)
			{
			case "January" :
				ans="01";
				break;
			case "February" :
				ans="02";
				break;
			case "March" :
				ans="03";
				break;
			case "April" :
				ans="04";
				break;
			case "May" :
				ans="05";
				break;
			case "June" :
				ans="06";
				break;
			case "July" :
				ans="07";
				break;
			case "August" :
				ans="08";
				break;
			case "September" :
				ans="09";
				break;
			case "October" :
				ans="10";
				break;
			case "November" :
				ans="11";
				break;
			case "December" :
				ans="12";
				break;
			}
		}
		catch(Exception e)
		{
			logger.debug("Exceptione in getting month no from month name--"+ e.toString());
		}

		return ans;
	}


}
