package com.newgen.DPL;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.newgen.DCC.CAMGenCIFUpdate.Digital_CC_CAMTemplate;
import com.newgen.DCC.DECTECHIntegration.DCC_DECTECH_Integration_Input;
import com.newgen.DCC.SystemIntegration.DCCSystemIntegrationLog;
import com.newgen.DCC.SystemIntegration.ResponseParser;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;

public class ExternalExposure {
	
	 private static String CheckGridTable = "NG_DPL_EXTTABLE";
	    
	public static String IntegratewithMW(String processInstanceID, int integrationWaitTime, int socketConnectionTimeOut, HashMap<String, String> socketDetailsMap,String WorkItemID,String ActivityID,String ActivityType,String ProcessDefId) throws IOException, Exception
    {
  	
  	Digital_PL_Log.digital_PL_Log.debug("Inside external exposure Integration " );
  	String MainStatusFlag = "Success";
  	String DBQuery = "SELECT WiName, CIF, PassportNumber, EmiratesID, MobileNo,RequestedLoanAmount as Final_Limit, "
  			+ "FirstName, MiddleName, LastName, DOB, Nationality, CustomerDeclaredMonthlyIncome, "
				+ "ProspectID, PassportExpiryDate, EXTERNAL_EXPOSURE_STATUS,"
				+ "DectechDecision,Is_CAM_generated FROM NG_DPL_EXTTABLE with(nolock) WHERE WINAME='" + processInstanceID + "'";
      
      String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(Digital_PL_Log.digital_PL_Log, false));
      Digital_PL_Log.digital_PL_Log.debug("extTabDataIPXML: " + extTabDataIPXML);
      String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
      Digital_PL_Log.digital_PL_Log.debug("extTabDataOPXML: " + extTabDataOPXML);

      XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
      int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
      String Is_CAM_Generated = xmlParserData.getValueOf("Is_CAM_generated");

      if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
        {
          String xmlDataExtTab = xmlParserData.getNextValueOf("Record");
          xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

          NGXmlList objWorkList = xmlParserData.createList("Records", "Record");

          HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();

          for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
            {
          	String gender=objWorkList.getVal("Gender_Code");
              if ("F".equalsIgnoreCase(gender)) {
                  gender = "1";
              } else if ("M".equalsIgnoreCase(gender)) {
                  gender = "2";
              } else {
                  gender = "3";
              }
          	CheckGridDataMap.put("Wi_Name", validateValue(objWorkList.getVal("WiName")));
              CheckGridDataMap.put("Final_Limit", validateValue(objWorkList.getVal("Final_Limit")));
              CheckGridDataMap.put("FirstNm", validateValue(objWorkList.getVal("FirstName")));
              CheckGridDataMap.put("MiddleName", validateValue(objWorkList.getVal("MiddleName")));
              CheckGridDataMap.put("LastNm", validateValue(objWorkList.getVal("LastName")));
              //CheckGridDataMap.put("CUSTOMERNAME", validateValue(objWorkList.getVal("CUSTOMERNAME")));
              //CheckGridDataMap.put("Gender", validateValue(gender));
              CheckGridDataMap.put("EXTERNAL_EXPOSURE_STATUS", validateValue(objWorkList.getVal("EXTERNAL_EXPOSURE_STATUS")));
              CheckGridDataMap.put("MobileNo", validateValue(objWorkList.getVal("MobileNo")));
              CheckGridDataMap.put("EmirateID", validateValue(objWorkList.getVal("EmiratesID")));
              CheckGridDataMap.put("PassportNo", validateValue(objWorkList.getVal("PassportNumber")));
              CheckGridDataMap.put("BirthDt", validateValue(objWorkList.getVal("dob")));
              CheckGridDataMap.put("Dectech_Flag", validateValue(objWorkList.getVal("Dectech_Flag")));
              CheckGridDataMap.put("Nationality", validateValue(objWorkList.getVal("Nationality")));
              //CheckGridDataMap.put("rerun_aecb", validateValue(objWorkList.getVal("rerun_aecb"))); // Hritik PDSC-799
              
              String flag = "Y";
              if (!(CheckGridDataMap.get("EXTERNAL_EXPOSURE_STATUS").equalsIgnoreCase("Y")))//To Do append the second condition..
              {
              	Digital_PL_Log.digital_PL_Log.error("Inside AECB Call for WI Number: " + processInstanceID);
              	
              	flag = callExternalExposure(processInstanceID, CheckGridDataMap, integrationWaitTime, socketConnectionTimeOut, socketDetailsMap);
              	
              	Digital_PL_Log.digital_PL_Log.error("AECB Flag" + processInstanceID);
              	
              	if("N".equalsIgnoreCase(flag))
                  {
                  	
                  	MainStatusFlag = "Failure~AECB call failed";
                  	Digital_PL_Log.digital_PL_Log.error("callExternalExposure status : Failure");
                  }
              	else if("Y".equalsIgnoreCase(flag)){
              		
              		String aecbDataQuery= "select AECB_Score,Range,ReferenceNo from ng_dpl_cust_extexpo_Derived with(nolock) where Wi_Name='"+processInstanceID+"' and Request_Type= 'ExternalExposure'";
              		String extTabDataINPXML = CommonMethods.apSelectWithColumnNames(aecbDataQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(Digital_PL_Log.digital_PL_Log, false));
      		        Digital_PL_Log.digital_PL_Log.debug("extTabDataIPXML: " + extTabDataINPXML);
      		        String extTabDataOUPXML = CommonMethods.WFNGExecute(extTabDataINPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
      		        Digital_PL_Log.digital_PL_Log.debug("extTabDataOPXML: " + extTabDataOUPXML);	
      				
      		        XMLParser xmlParserDataDB = new XMLParser(extTabDataOUPXML);
      		        
      		        String mainCode=xmlParserDataDB.getValueOf("MainCode");
      		        int iTotalreccam = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
      		        if("0".equalsIgnoreCase(mainCode) && iTotalreccam>0)
      		        {
      		        	String AECB_Score = xmlParserDataDB.getValueOf("AECB_Score");
      			        String range = xmlParserDataDB.getValueOf("Range");
      			        String ReferenceNo = xmlParserDataDB.getValueOf("ReferenceNo");
      			        if(range!=null && !"".equalsIgnoreCase(range))
      			        {
      			        	/*String numRegex="[0-9]+";
      			        	Pattern p = Pattern.compile(numRegex);
      			        	Matcher m = p.matcher(range);*/
      			        	if(range.matches("^[a-zA-Z]*$"))
          			        	range="Consumer Score "+range;
      			        	else if(range.matches("[0-9]+"))
          			        	range="No hit score "+range;
          			        else if(range.matches("^[a-zA-Z0-9]*$"))
          			        	range="Alternate score "+range;
          			        else
          			        	range="";
      			        }
      			        

  		        		String columnNames="AECB_Score,Score_range,bureau_reference_number";
  		        		String columnValues="'"+AECB_Score+"','"+range+"','"+ReferenceNo+"'";
  		        		String sWhereClause = "WINAME='" + processInstanceID + "'";
  		    	    	String tableName = "NG_DPL_EXTTABLE";
  		    	        String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(Digital_PL_Log.digital_PL_Log, false), 
  		    	        		tableName, columnNames, columnValues, sWhereClause);
  		    	        Digital_PL_Log.digital_PL_Log.debug("Input XML for apUpdateInput for " + tableName + " Table : " + inputXML);
  		    	        String outputXml = CommonMethods.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
  		    	        Digital_PL_Log.digital_PL_Log.debug("Output XML for apUpdateInput for " + tableName + " Table : " + outputXml);
  		    	        XMLParser sXMLParserChild = new XMLParser(outputXml);
  		    	        String StrMainCode = sXMLParserChild.getValueOf("MainCode");
  		    	        if(!"0".equals(StrMainCode))
  		    	        	MainStatusFlag = "Failure~Error in AECB Fields Update.";
  		        	
      		        }
      		        else
      		        	MainStatusFlag = "Failure~Error in AECB Fields Update.";
              	}
              }
              
              if ("Y".equalsIgnoreCase(flag)) 
              {
          		String Proc_name = "ng_dpl_insert_external_exposure";
          		Digital_PL_Log.digital_PL_Log.info("Inside process method where procedure need to be triggered");
          		String param = "'" +processInstanceID+ "'";
                  String Proc_inputXMl = CommonMethods.ExecuteQuery_APProcedure(Proc_name, param, CommonConnection.getCabinetName(), CommonConnection.getSessionID(Digital_PL_Log.digital_PL_Log, false));
                  Digital_PL_Log.digital_PL_Log.info("Input xml for procedure"+Proc_inputXMl);

                  String outXml = CommonMethods.WFNGExecute(Proc_inputXMl,CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
                  Digital_PL_Log.digital_PL_Log.info("Output xml for procedure "+outXml);
                  XMLParser xmlParserDataDB = new XMLParser(outXml);
  		        
                  String Proc_main_Code =  xmlParserDataDB.getValueOf("MainCode");
                  Digital_PL_Log.digital_PL_Log.error("Maincode for procedure : " + Proc_main_Code);
              	
              }
              
              if ("Y".equalsIgnoreCase(flag) && !(CheckGridDataMap.get("Dectech_Flag").equalsIgnoreCase("Y"))) 
              {
              	Digital_PL_Log.digital_PL_Log.error("Inside DECTECH Call for WI Number: " + processInstanceID);
              	
              	//rubi
//              	String output=DCC_DECTECH_Integration_Input.GenerateXML(processInstanceID, ActivityID, ActivityType, ProcessDefId,WorkItemID);
//              	if(!"Success".equalsIgnoreCase(output))
//              	{
//              		MainStatusFlag = "Failure~DECTECH call failed";
//              	}
//              	else{
//              		CheckGridDataMap.put("Dectech_Flag", "Y");
//              	}
              }
              //Cam generation ...
              
              Digital_PL_Log.digital_PL_Log.error("before CAM generation Is_CAM_Generated: " + Is_CAM_Generated);
              Digital_PL_Log.digital_PL_Log.error("before CAM generation Dectech_Flag: " + CheckGridDataMap.get("Dectech_Flag"));
              Digital_PL_Log.digital_PL_Log.error("before CAM generation AECB flag: " + flag);
              
              if("Y".equalsIgnoreCase(flag) &&!"Y".equalsIgnoreCase(Is_CAM_Generated))
            //	  if("Y".equalsIgnoreCase(flag) && CheckGridDataMap.get("Dectech_Flag").equalsIgnoreCase("Y") && !"Y".equalsIgnoreCase(Is_CAM_Generated))
		        {
              	
              	String DBQuery1 = "SELECT CIF,IsSTP,DectechDecision,FIRCO_Status,EFMS_Status FROM NG_DPL_EXTTABLE with(nolock) WHERE WINAME='" + processInstanceID + "'";
  		        
  		        String extTabDataINPXML = CommonMethods.apSelectWithColumnNames(DBQuery1, CommonConnection.getCabinetName(), CommonConnection.getSessionID(Digital_PL_Log.digital_PL_Log, false));
  		        Digital_PL_Log.digital_PL_Log.debug("extTabDataIPXML: " + extTabDataINPXML);
  		        String extTabDataOUPXML = CommonMethods.WFNGExecute(extTabDataINPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
  		        Digital_PL_Log.digital_PL_Log.debug("extTabDataOPXML: " + extTabDataOUPXML);	
  				
  		        XMLParser xmlParserDataDB = new XMLParser(extTabDataOUPXML);
  		        
  		        String mainCode=xmlParserDataDB.getValueOf("MainCode");
  		        int iTotalreccam = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
  		        if("0".equalsIgnoreCase(mainCode) && iTotalreccam>0)
  		        {
  		        	
  			        String Cif_Id = xmlParserDataDB.getValueOf("CIF");
  			        String Is_STP = xmlParserDataDB.getValueOf("IsSTP");
  			        String Dectech_Decision =xmlParserDataDB.getValueOf("DectechDecision");
  			        String firco=xmlParserDataDB.getValueOf("FIRCO_Status");
  			        String efms=xmlParserDataDB.getValueOf("EFMS_Status");
  			        System.out.println("");
  			        //String fts =xmlParserDataDB.getValueOf("FTS_Ack_flg");
  			        //String fircoAction =xmlParserDataDB.getValueOf("FircoUpdateAction");
  			        String pdfName = "";
  			        if ("Y".equalsIgnoreCase(Is_STP) || "D".equalsIgnoreCase(Dectech_Decision) || "A".equalsIgnoreCase(Dectech_Decision)||"CB".equalsIgnoreCase(firco)||"Confirmed Fraud".equalsIgnoreCase(efms))
  			       // 	if ("Y".equalsIgnoreCase(Is_STP) || "D".equalsIgnoreCase(Dectech_Decision) || "A".equalsIgnoreCase(Dectech_Decision)||"CB".equalsIgnoreCase(firco)||"Confirmed Fraud".equalsIgnoreCase(efms)||"D".equalsIgnoreCase(fts)||"Decline".equalsIgnoreCase(fircoAction))
  		  		        	
  			        {
  		        		 pdfName = "DPL_STP_CAM_Report";
  		        		 
  		        		 	Digital_PL_CAMTemplate obj = new Digital_PL_CAMTemplate(Digital_PL_Log.digital_PL_Log);
  	    		        	String output = obj.DPL_generate_CAM_ReportT(pdfName,Cif_Id,processInstanceID,CommonConnection.getSessionID(Digital_PL_Log.digital_PL_Log, false));
  	    		        	System.out.println("output:"+output);
  	    		        	if(output==null || !output.contains("Success~"))
  	    		        	 {
  	    		        		 MainStatusFlag = "Failure~Error in Cam Genration.";
  	    		        	 }
  	    		        	else
  	    		        	{
  	    		        		String columnNames="Is_CAM_generated";
  	    		        		String columnValues="'Y'";
  	    		        		String sWhereClause = "WINAME='" + processInstanceID + "'";
  	    		    	    	String tableName = "NG_DPL_EXTTABLE";
  	    		    	        String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(Digital_PL_Log.digital_PL_Log, false), 
  	    		    	        		tableName, columnNames, columnValues, sWhereClause);
  	    		    	        Digital_PL_Log.digital_PL_Log.debug("Input XML for apUpdateInput for " + tableName + " Table : " + inputXML);
  	    		    	        String outputXml = CommonMethods.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
  	    		    	        Digital_PL_Log.digital_PL_Log.debug("Output XML for apUpdateInput for " + tableName + " Table : " + outputXml);
  	    		    	        XMLParser sXMLParserChild = new XMLParser(outputXml);
  	    		    	        String StrMainCode = sXMLParserChild.getValueOf("MainCode");
  	    		    	        if(!"0".equals(StrMainCode))
  	    		    	        	MainStatusFlag = "Failure~Error in Cam Flag Update.";
  	    		        	}
  			        }
  			        else if(!"N".equalsIgnoreCase(Is_STP))
  			        {
  			        	MainStatusFlag = "Failure~Error in Cam Generation(STP flag is not valid).";
  			        	Digital_PL_Log.digital_PL_Log.debug("Error in Cam Generation(STP flag is not valid): " + Is_STP);
  			        }
  		        	/*else
  		        	{
  		        		 pdfName = "NON_STP_CAM_Report";
  		        	}*/
  		        	
  			        
  		        }
  		        else
  		        {
  		        	Digital_PL_Log.digital_PL_Log.debug("Error in cam generation..ApSelect for Is_CAM_Generated flag..");
  		        	MainStatusFlag = "Failure~Error in cam Generation";
  		        }
		        	
				}
		        else
		        {
		        	Digital_PL_Log.digital_PL_Log.debug("Cam Report Is Already Generated");
				}
			 }
        }
      else
        {
          Digital_PL_Log.digital_PL_Log.debug("WmgetWorkItem status: " + xmlParserData.getValueOf("MainCode"));
        }
      	return MainStatusFlag;
  }

	private static String callExternalExposure(String wiName, HashMap<String, String> CheckGridDataMap, int integrationWaitTime, int socketConnectionTimeOut, HashMap<String, String> socketDetailsMap)
		      throws IOException, Exception
		      {
				SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm");
				String DateExtra2 = sdf1.format(new Date())+"+04:00";
				
				String flag = "";
		        StringBuilder sInputXML = new StringBuilder();
		        sInputXML = sInputXml(wiName,  DateExtra2, CheckGridDataMap);
		        Digital_PL_Log.digital_PL_Log.debug("Request XML for ExternalExposure  " + sInputXML);
		        
		        String responseXML = socketConnection(CommonConnection.getCabinetName(), CommonConnection.getUsername(), CommonConnection.getSessionID(Digital_PL_Log.digital_PL_Log, false), 
		        		CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), wiName, "ExternalExposure", socketConnectionTimeOut, integrationWaitTime, socketDetailsMap, sInputXML);
		        
		        Digital_PL_Log.digital_PL_Log.debug("Request XML for responseXML  " + responseXML);
		        
		        flag = ResponseParser.getOutputXMLValues(responseXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),
		          CommonConnection.getSessionID(Digital_PL_Log.digital_PL_Log, false), CommonConnection.getCabinetName(), 
		          wiName, CheckGridDataMap.get("Product"), CheckGridDataMap.get("SubProduct"), CheckGridDataMap.get("CIF"), 
		          CheckGridDataMap.get("CUSTOMER_TYPE"));
		        flag = flag == "true" ? "Y" : "N";

		    	String columnNames = "EXTERNAL_EXPOSURE_STATUS";
		    	String columnValues = "'" + flag + "'";
		    	String sWhereClause = "WINAME='" + wiName + "'";
		        String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(Digital_PL_Log.digital_PL_Log, false), 
		        		CheckGridTable, columnNames, columnValues, sWhereClause);
		        Digital_PL_Log.digital_PL_Log.debug("Input XML for apUpdateInput for " + CheckGridTable + " Table : " + inputXML);
		        String outputXml = CommonMethods.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
		        Digital_PL_Log.digital_PL_Log.debug("Output XML for apUpdateInput for " + CheckGridTable + " Table : " + outputXml);
		        XMLParser sXMLParserChild = new XMLParser(outputXml);
		        String StrMainCode = sXMLParserChild.getValueOf("MainCode");
		        //String RetStatus = null;
		        if (StrMainCode.equals("0"))
		          {
		            Digital_PL_Log.digital_PL_Log.debug("Successful in apUpdateInput the record in : " + CheckGridTable);
		            //RetStatus = "Success in apUpdateInput the record";
		          }
		        else
		          {
		            Digital_PL_Log.digital_PL_Log.debug("Error in Executing apUpdateInput sOutputXML : " + outputXml);
		            //RetStatus = "Error in Executing apUpdateInput";
		          }
		        return flag;
		      }
	
	private static StringBuilder sInputXml(String wiName, String DateExtra2, HashMap<String, String> CheckGridDataMap) {
		String emi_id = CheckGridDataMap.get("EmirateID");
		if(emi_id.length()>14){
		emi_id = emi_id.substring(0, 3) + "-" + emi_id.substring(3, 7) + "-" + emi_id.substring(7, 14) + "-"+ emi_id.substring(14, 15);
		}
		String rerunAECB="false";
		String Override_period = "0";
		Digital_PL_Log.digital_PL_Log.debug("rerunAECB : " + rerunAECB);
		//rerunAECB=CheckGridDataMap.get("rerun_aecb");
		if(rerunAECB.equalsIgnoreCase("true")){
			Override_period="1";
		}// END
		Digital_PL_Log.digital_PL_Log.debug("Override_preiod : " + Override_period);
		
		
		String First= CheckGridDataMap.get("FirstNm");
	    String Middle= CheckGridDataMap.get("MiddleName");
	    String Last= CheckGridDataMap.get("LastNm");
	    String FullName= First + " " + Last;
	    if(Middle==null || "".equalsIgnoreCase(Middle)){
			FullName = First + " " + Last;
		}
		else{
			FullName= First +" "+ Middle + " " + Last;
		}
	    Digital_PL_Log.digital_PL_Log.debug("FullName : " + FullName);
		
//		StringBuilder sb = new StringBuilder(
//				"<EE_EAI_MESSAGE>" + "\n"
//                +"<EE_EAI_HEADER>" + "\n"
//                +"<MsgFormat>CUSTOMER_EXPOSURE</MsgFormat>" + "\n"
//                +"<MsgVersion>0001</MsgVersion>" + "\n"
//                +"<RequestorChannelId>CAS</RequestorChannelId>" + "\n"
//                +"<RequestorUserId>RAKUSER</RequestorUserId>" + "\n"
//                +"<RequestorLanguage>E</RequestorLanguage>" + "\n"
//                +"<RequestorSecurityInfo>secure</RequestorSecurityInfo>" + "\n"
//                +"<ReturnCode>911</ReturnCode>" + "\n"
//                +"<ReturnDesc>IssuerTimedOut</ReturnDesc>" + "\n"
//                +"<MessageId>CUSTOMER_EXPOSUER_0V27</MessageId>" + "\n"
//                +"<Extra1>REQ||SHELL.JOHN</Extra1>" + "\n"
//                +"<Extra2>" + DateExtra2+"</Extra2>" + "\n"
//			+"</EE_EAI_HEADER>" + "\n"
//			+"<CustomerExposureRequest>" + "\n"
//                +"<BankId>RAK</BankId>" + "\n"
//                +"<BranchId>RAK123</BranchId>" + "\n"
//                +"<RequestType>ExternalExposure</RequestType>" + "\n"
//                +"<CIFId>" + "\n"
//				+"<CIFIdType>Primary</CIFIdType>" + "\n"
//				+"<CIFIdValue>asd12</CIFIdValue>" + "\n"
//				+"</CIFId>" + "\n"
//				+"<CIFId>" + "\n"
//				+"<CIFIdType>Secondary</CIFIdType>" + "\n"
//				+"<CIFIdValue>asd1</CIFIdValue>" + "\n"
//				+"</CIFId>" + "\n"
//                +"<CustType>1</CustType>" + "\n"
//                +"<UserId>deepak</UserId>" + "\n"
//                +"<AcctId></AcctId>" + "\n"
//                +"<TxnAmount>" + CheckGridDataMap.get("Final_Limit") + "</TxnAmount>" + "\n"
//                +"<NoOfInstallments></NoOfInstallments>" + "\n"
//                +"<DurationOfAgreement></DurationOfAgreement>" + "\n"
//                +"<FirstNm>"+ CheckGridDataMap.get("FirstNm") +"</FirstNm>" + "\n"
//                +"<LastNm>"+ CheckGridDataMap.get("LastNm") +"</LastNm>" + "\n"
//                +"<FullNm>"+FullName+"</FullNm>" + "\n"  // HRITIK - 22.08.23 - Production Issue
//                +"<BirthDt>"+ CheckGridDataMap.get("BirthDt") +"</BirthDt>" + "\n"
//                +"<Gender>"+ CheckGridDataMap.get("Gender") +"</Gender>" + "\n"
//                +"<Nationality>"+ CheckGridDataMap.get("Nationality") +"</Nationality>" + "\n"
//                +"<InquiryPurpose>2</InquiryPurpose>" + "\n" 
//                +"<ProviderApplNo>" + wiName.split("-")[1] + (new Date()).getTime() + "</ProviderApplNo>" + "\n"
//                +"<CBApplNo></CBApplNo>" + "\n"
//                +"<IsCoApplicant>0</IsCoApplicant>" + "\n"
//                +"<LosIndicator>1</LosIndicator>" + "\n"
//                +"<ContractType>1</ContractType>" + "\n"
//                +"<OverridePeriod>"+Override_period+"</OverridePeriod>" + "\n"
//                +"<PrimaryMobileNo>" + CheckGridDataMap.get("MobileNo") +"</PrimaryMobileNo>" + "\n"
//                +"<ShareHolderPercentage>20</ShareHolderPercentage>" + "\n"
//                +"<ConsentFlag>1</ConsentFlag>" + "\n"
//                +"<BureauCategory>Retail</BureauCategory>" + "\n"
//                +"<BureauId>10</BureauId>" + "\n"
//                +"<CallType>Synchronous</CallType>" + "\n"
//                +"<TradeName>KARCO OVERSEAS TRADING LLC</TradeName>" + "\n"
//				+"<TradeNameAR>dsfahsad</TradeNameAR>" + "\n"
//				+"<TradeLicenseNumber>100651</TradeLicenseNumber>" + "\n"
//				+"<TradeLicensePlace>3</TradeLicensePlace>" + "\n"
//				+"<EconomicActivity>8</EconomicActivity>" + "\n"
//                +"<ScoreType>0</ScoreType>" + "\n"
//                +"<LegalDocInfo>" + "\n"
//                                +"<DocType>Emirates id</DocType>" + "\n"
//                                +"<DocNum>"+ emi_id +"</DocNum>" + "\n"
//                +"</LegalDocInfo>" + "\n"
//                +"<LegalDocInfo>" + "\n"
//                                +"<DocType>Passport Number</DocType>" + "\n"
//                                +"<DocNum>"+CheckGridDataMap.get("PassportNumner")+"</DocNum>" + "\n"
//                +"</LegalDocInfo>" + "\n"
//		+"</CustomerExposureRequest>" + "\n"
//		+"</EE_EAI_MESSAGE>"
//		);
	    
	    StringBuilder sb = new StringBuilder(
				"<EE_EAI_MESSAGE>" + "\n"
                +"<EE_EAI_HEADER>" + "\n"
                +"<MsgFormat>CUSTOMER_EXPOSURE</MsgFormat>" + "\n"
                +"<MsgVersion>0001</MsgVersion>" + "\n"
                +"<RequestorChannelId>CAS</RequestorChannelId>" + "\n"
                +"<RequestorUserId>RAKUSER</RequestorUserId>" + "\n"
                +"<RequestorLanguage>E</RequestorLanguage>" + "\n"
                +"<RequestorSecurityInfo>secure</RequestorSecurityInfo>" + "\n"
                +"<ReturnCode>911</ReturnCode>" + "\n"
                +"<ReturnDesc>IssuerTimedOut</ReturnDesc>" + "\n"
                +"<MessageId>CUSTOMER_EXPOSUER_0V27</MessageId>" + "\n"
                +"<Extra1>REQ||SHELL.JOHN</Extra1>" + "\n"
                +"<Extra2>" + DateExtra2+"</Extra2>" + "\n"
			+"</EE_EAI_HEADER>" + "\n"
			+"<CustomerExposureRequest>" + "\n"
                +"<BankId>RAK</BankId>" + "\n"
                +"<BranchId>RAK123</BranchId>" + "\n"
                +"<RequestType>ExternalExposure</RequestType>" + "\n"
                +"<CustType>1</CustType>" + "\n"
                +"<UserId>deepak</UserId>" + "\n"
                +"<AcctId></AcctId>" + "\n"
                +"<TxnAmount>5000</TxnAmount>" + "\n"
                +"<NoOfInstallments></NoOfInstallments>" + "\n"
                +"<DurationOfAgreement></DurationOfAgreement>" + "\n"
                +"<FirstNm>"+ CheckGridDataMap.get("FirstNm") +"</FirstNm>" + "\n"
                +"<LastNm>"+ CheckGridDataMap.get("LastNm") +"</LastNm>" + "\n"
                +"<FullNm>"+FullName+"</FullNm>" + "\n"  // HRITIK - 22.08.23 - Production Issue
                +"<BirthDt>"+ CheckGridDataMap.get("BirthDt") +"</BirthDt>" + "\n"
                +"<Gender>2</Gender>" + "\n"
                +"<Nationality>"+ CheckGridDataMap.get("Nationality") +"</Nationality>" + "\n"
                +"<InquiryPurpose>2</InquiryPurpose>" + "\n" 
                +"<ProviderApplNo>" + wiName.split("-")[1] + (new Date()).getTime() + "</ProviderApplNo>" + "\n"
                +"<CBApplNo></CBApplNo>" + "\n"
                +"<IsCoApplicant>0</IsCoApplicant>" + "\n"
                +"<LosIndicator>1</LosIndicator>" + "\n"
                +"<ContractType>1</ContractType>" + "\n"
                +"<OverridePeriod>"+Override_period+"</OverridePeriod>" + "\n"
                +"<PrimaryMobileNo>" + CheckGridDataMap.get("MobileNo") +"</PrimaryMobileNo>" + "\n"
                +"<ConsentFlag>1</ConsentFlag>" + "\n"
                +"<BureauCategory>Retail</BureauCategory>" + "\n"
                +"<BureauId>10</BureauId>" + "\n"
                +"<CallType>Synchronous</CallType>" + "\n"
                +"<ScoreType>0</ScoreType>" + "\n"
                +"<LegalDocInfo>" + "\n"
                                +"<DocType>Emirates id</DocType>" + "\n"
                                +"<DocNum>"+ emi_id +"</DocNum>" + "\n"
                +"</LegalDocInfo>" + "\n"
                +"<LegalDocInfo>" + "\n"
                                +"<DocType>Passport Number</DocType>" + "\n"
                                +"<DocNum>"+CheckGridDataMap.get("PassportNo")+"</DocNum>" + "\n"
                +"</LegalDocInfo>" + "\n"
		+"</CustomerExposureRequest>" + "\n"
		+"</EE_EAI_MESSAGE>"
		);

		return  sb;
	}


	static String socketConnection(String cabinetName, String username, String sessionId, String sJtsIp,
			String iJtsPort, String processInstanceID, String ws_name,
			int connection_timeout, int integrationWaitTime,HashMap<String, String> socketDetailsMap, StringBuilder sInputXML)
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
			Digital_PL_Log.digital_PL_Log.debug("userName "+ username);
			Digital_PL_Log.digital_PL_Log.debug("SessionId "+ sessionId);

			socketServerIP=socketDetailsMap.get("SocketServerIP");
			Digital_PL_Log.digital_PL_Log.debug("SocketServerIP "+ socketServerIP);
			socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			Digital_PL_Log.digital_PL_Log.debug("SocketServerPort "+ socketServerPort);

	   		if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0))
	   		{

    			socket = new Socket(socketServerIP, socketServerPort);
    			socket.setSoTimeout(connection_timeout*1000);
    			out = socket.getOutputStream();
    			socketInputStream = socket.getInputStream();
    			dout = new DataOutputStream(out);
    			din = new DataInputStream(socketInputStream);
    			Digital_PL_Log.digital_PL_Log.debug("Dout " + dout);
    			Digital_PL_Log.digital_PL_Log.debug("Din " + din);

    			outputResponse = "";

    			inputRequest = getRequestXML( cabinetName,sessionId ,processInstanceID, ws_name, username, sInputXML);

    			if (inputRequest != null && inputRequest.length() > 0)
    			{
    				int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
    				Digital_PL_Log.digital_PL_Log.debug("RequestLen: "+inputRequestLen + "");
    				inputRequest = inputRequestLen + "##8##;" + inputRequest;
    				Digital_PL_Log.digital_PL_Log.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
    				dout.write(inputRequest.getBytes("UTF-16LE"));
    				dout.flush();
    			}
    			byte[] readBuffer = new byte[1000];
    			System.out.println(readBuffer.length);
    			int num = din.read(readBuffer);
    			if (num > 0)
				{

					byte[] arrayBytes = new byte[num];
					System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
					outputResponse = outputResponse + new String(arrayBytes, "UTF-16LE");
					inputMessageID = outputResponse;
					Digital_PL_Log.digital_PL_Log.debug("OutputResponse: " + outputResponse);

					if (!"".equalsIgnoreCase(outputResponse))

						outputResponse = getResponseXML(cabinetName, sJtsIp, iJtsPort, sessionId, processInstanceID,
								outputResponse, integrationWaitTime);

					if (outputResponse.contains("&lt;")) {
						outputResponse = outputResponse.replaceAll("&lt;", "<");
						outputResponse = outputResponse.replaceAll("&gt;", ">");
					}
				}
    			socket.close();

				outputResponse = outputResponse.replaceAll("</MessageId>","</MessageId><InputMessageId>"+inputMessageID+"</InputMessageId>");

				Digital_PL_Log.digital_PL_Log.debug("outputResponse "+outputResponse);
				return outputResponse;

    	 	}
    		else
    		{
    			Digital_PL_Log.digital_PL_Log.debug("SocketServerIp and SocketServerPort is not maintained "+"");
    			Digital_PL_Log.digital_PL_Log.debug("SocketServerIp is not maintained "+	socketServerIP);
    			Digital_PL_Log.digital_PL_Log.debug(" SocketServerPort is not maintained "+	socketServerPort);
    			return "Socket Details not maintained";
    		}
		}
		catch (Exception e)
		{
			Digital_PL_Log.digital_PL_Log.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
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
				Digital_PL_Log.digital_PL_Log.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
			}
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
		Digital_PL_Log.digital_PL_Log.debug("GetRequestXML: "+ strBuff.toString());
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
			Digital_PL_Log.digital_PL_Log.debug("Response APSelect InputXML: "+responseInputXML);

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
			Digital_PL_Log.digital_PL_Log.debug("integrationWaitTime: "+integrationWaitTime);

		}
		catch(Exception e)
		{
			Digital_PL_Log.digital_PL_Log.debug("Exception occurred in outputResponseXML" + e.getMessage());
			Digital_PL_Log.digital_PL_Log.debug("Exception occurred in outputResponseXML" + e.getStackTrace());
			outputResponseXML="Error";
		}

		return outputResponseXML;

	}
	
	private static String validateValue(String value) {
		if (value != null && ! value.equals("") && !value.equalsIgnoreCase("null")) {
			return value.toString();
		}
		return "";
	}

}
