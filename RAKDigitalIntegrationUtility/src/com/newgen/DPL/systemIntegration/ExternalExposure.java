package com.newgen.DPL.systemIntegration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.newgen.DCC.SystemIntegration.ResponseParser;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.DPL.Digital_PL_CAMTemplate;
import com.newgen.DPL.Digital_PL_Log;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.omni.wf.util.excp.NGException;

public class ExternalExposure {
	
	 private static String CheckGridTable = "NG_DPL_EXTTABLE";
	 static NGEjbClient ngEjbClient;
	 private static org.apache.log4j.Logger logger;
	 
		
	 public ExternalExposure() throws NGException{
		Digital_PL_Log.setLogger(getClass().getSimpleName());
		this.ngEjbClient = NGEjbClient.getSharedInstance();
		logger = Digital_PL_Log.getLogger(getClass().getSimpleName());
		
	}
	 
	//Rubi for DPL
	public void startDPLSysIntegration(String cabinetName, String UserName, String sJtsIp, String iJtsPort,
				String sessionId, String dplQueueID, int socketConnectionTimeOut, int integrationWaitTime,
				HashMap<String, String> socketDetailsMap)
	{
			final String ws_name="Sys_Checks_Integration";
			try
			{
				//Validate Session ID
				sessionId  = CommonConnection.getSessionID(logger, false);

				if(sessionId==null || sessionId.equalsIgnoreCase("") || sessionId.equalsIgnoreCase("null"))
				{
					logger.error("Could Not Get Session ID "+sessionId);
					return;
				}

				//Fetch all Work-Items on given queueID. 
				logger.debug("Fetching all Workitems on DPL_SysCheckIntegration queue");
				System.out.println("Fetching all Workitems on DCC_SysCheckIntegration queue");
				
				String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionId, dplQueueID);
				logger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

				String fetchWorkitemListOutputXML= CommonMethods.WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);
				logger.debug("WMFetchWorkList OutputXML: "+fetchWorkitemListOutputXML);

				XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

				String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
				logger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

				int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
				logger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);
				logger.debug("Number of workitems retrieved on DPL_SysCheckIntegration: "+fetchWorkitemListCount);
				System.out.println("Number of workitems retrieved on DCC_SysCheckIntegration: "+fetchWorkitemListCount);

				if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0)
				{
					for(int i=0; i<fetchWorkitemListCount; i++)
					{
						try
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

							String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
							logger.debug("ActivityID: "+ActivityID);

							String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
							logger.debug("ActivityType: "+ActivityType);

							String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
							logger.debug("ProcessDefId: "+ProcessDefId);

							String ActivityName=xmlParserfetchWorkItemData.getValueOf("ActivityName");
							logger.debug("ActivityName: "+ActivityName);
							
							String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID,WorkItemID);
							String getWorkItemOutputXml = CommonMethods.WFNGExecute(getWorkItemInputXML,sJtsIp,iJtsPort,1);
							logger.debug("Output XML For WmgetWorkItemCall: "+ getWorkItemOutputXml);

							XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
							String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
							String ExtExpoStatus = "";
							String decisionValue="";
							logger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);
							if (getWorkItemMainCode.trim().equals("0"))
							{
								logger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);
								try
								{
									//ExternalExposure externalExposure = new ExternalExposure();
									ExtExpoStatus = IntegratewithMW(processInstanceID,integrationWaitTime, socketConnectionTimeOut, socketDetailsMap, WorkItemID, ActivityID, ActivityType, ProcessDefId);					
								}
								catch(Exception e)
								{
									logger.debug("Exception in executing external exposure: "+e.toString());
								}
								
								String attributesTag="";
								String remarks = "";
								if("Success".equalsIgnoreCase(ExtExpoStatus))
								{
									logger.debug("Status of external exposure: "+ExtExpoStatus);
									decisionValue="Success";
									attributesTag="<Decision>"+decisionValue+"</Decision>";
									remarks = "System Check(AECB,DecTech) completed";
								}
								else
								{
									if(ExtExpoStatus!=null && ExtExpoStatus.contains("Failure"))
									{
										String arr[] = ExtExpoStatus.split("~");
										if(arr.length==2)
										remarks=arr[1];
									}
									decisionValue="Failed";
									attributesTag="<Decision>"+decisionValue+"</Decision>";
								}

								logger.info("get Workitem call successfull for "+processInstanceID);

								String assignWorkitemAttributeInputXML = "<?xml version=\"1.0\"?><WMAssignWorkItemAttributes_Input>"
										+ "<Option>WMAssignWorkItemAttributes</Option>"
										+ "<EngineName>"+CommonConnection.getCabinetName()+"</EngineName>"
										+ "<SessionId>"+CommonConnection.getSessionID(logger, false)+"</SessionId>"
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
								logger.debug("Input XML for assign Attribute is "+assignWorkitemAttributeInputXML);

								String assignWorkitemAttributeOutputXML= CommonMethods.WFNGExecute(assignWorkitemAttributeInputXML,CommonConnection.getJTSIP(),
										CommonConnection.getJTSPort(),1);
								logger.debug("Output XML for assign Attribues is "+assignWorkitemAttributeOutputXML);

								XMLParser xmlParserAssignAtt=new XMLParser(assignWorkitemAttributeOutputXML);

								String mainCodeAssignAtt=xmlParserAssignAtt.getValueOf("MainCode");
								if("0".equals(mainCodeAssignAtt.trim()))
								{
									//logger.debug("WmCompleteWorkItem successful: "+completeWorkitemMaincode);
									System.out.println(processInstanceID + " Completed Sussesfully with status "+decisionValue);

									logger.debug("WorkItem moved to next Workstep.");

									SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
									SimpleDateFormat outputDateFormat=new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

									Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
									String formattedEntryDatetime=outputDateFormat.format(entryDatetimeFormat);
									logger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);

									Date actionDateTime= new Date();
									String formattedActionDateTime=outputDateFormat.format(actionDateTime);
									logger.debug("FormattedActionDateTime: "+formattedActionDateTime);
									//Insert in WIHistory Table.
									//String columnNames="wi_name,dec_date,workstep,user_name,Decision,Remarks,ENTRY_DATE_TIME";
									//String columnValues="'"+processInstanceID+"','"+formattedActionDateTime+"','"+ws_name+"','" +CommonConnection.getUsername()+"','"+decisionValue+"','"+remarks+"','"+formattedEntryDatetime+"'";

									String columnNames="WI_NAME,Decision_Date_Time,WORKSTEP,USERNAME,DECISION,ENTRY_DATE_TIME,REMARKS";
									String columnValues="'"+processInstanceID+"','"+formattedActionDateTime+"','"+ActivityName+"','"
											+CommonConnection.getUsername()+"','"+decisionValue+"','"+formattedEntryDatetime+"','"+remarks+"'";

									
									String apInsertInputXML=CommonMethods.apInsert(CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false), columnNames, columnValues, "NG_DPL_GR_DECISION_HISTORY");
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
								else
								{
									//System.out.println("Error in Assign Attribute call for "+processInstanceID);
									logger.error("Error in Assign Attribute call for WI "+processInstanceID);
								}					
							}	
							else
							{
								System.out.println("WMgetWorkItemCall failed: "+processInstanceID);
								logger.debug("WMgetWorkItemCall failed: "+processInstanceID);
							}
						
						}
						catch(Exception e)
						{
							logger.debug("Exception: "+e.getMessage());
						}
					}
				}
			}
			catch (Exception e)
			{
				logger.debug("Exception: "+e.getMessage());
			}
		}
		
	public static String IntegratewithMW(String processInstanceID, int integrationWaitTime, int socketConnectionTimeOut, HashMap<String, String> socketDetailsMap,String WorkItemID,String ActivityID,String ActivityType,String ProcessDefId) throws IOException, Exception
    {
		//Digital_PL_Log.setLogger("DPL_ExternalExposure.xml");
		
		logger.debug("Inside external exposure Integration " );
		String MainStatusFlag = "Success";
		String DBQuery = "SELECT WiName, CIF, PassportNumber, EmiratesID, MobileNo,RequestedLoanAmount as Final_Limit, "
  			+ "FirstName, MiddleName, LastName, DOB, Nationality, CustomerDeclaredMonthlyIncome,Gender, "
				+ "ProspectID, PassportExpiryDate, EXTERNAL_EXPOSURE_STATUS,"
				+ "DectechDecision,Is_CAM_generated FROM NG_DPL_EXTTABLE with(nolock) WHERE WINAME='" + processInstanceID + "'";
      
      String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false));
      logger.debug("extTabDataIPXML: " + extTabDataIPXML);
      String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
      logger.debug("extTabDataOPXML: " + extTabDataOPXML);

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
          	String gender=objWorkList.getVal("Gender");
              if ("Female".equalsIgnoreCase(gender)) {
                  gender = "1";
              } else if ("Male".equalsIgnoreCase(gender)) {
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
              CheckGridDataMap.put("Gender", validateValue(gender));
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
            	  logger.error("Inside AECB Call for WI Number: " + processInstanceID);
              	
              		flag = callExternalExposure(processInstanceID, CheckGridDataMap, integrationWaitTime, socketConnectionTimeOut, socketDetailsMap);
              	
              		logger.error("AECB Flag" + processInstanceID);
              	
              	if("N".equalsIgnoreCase(flag))
                  {
                  	
                  	MainStatusFlag = "Failure~AECB call failed";
                logger.error("callExternalExposure status : Failure");
                  }
              	else if("Y".equalsIgnoreCase(flag)){
              		
              		String aecbDataQuery= "select AECB_Score,Range,ReferenceNo from ng_dpl_cust_extexpo_Derived with(nolock) where Wi_Name='"+processInstanceID+"' and Request_Type= 'ExternalExposure'";
              		String extTabDataINPXML = CommonMethods.apSelectWithColumnNames(aecbDataQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false));
      		        logger.debug("extTabDataIPXML: " + extTabDataINPXML);
      		        String extTabDataOUPXML = CommonMethods.WFNGExecute(extTabDataINPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
      		        logger.debug("extTabDataOPXML: " + extTabDataOUPXML);	
      				
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
      			        System.out.println();

  		        		String columnNames="AECB_Score,Score_range,bureau_reference_number";
  		        		String columnValues="'"+AECB_Score+"','"+range+"','"+ReferenceNo+"'";
  		        		String sWhereClause = "WINAME='" + processInstanceID + "'";
  		    	    	String tableName = "NG_DPL_EXTTABLE";
  		    	        String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false), 
  		    	        		tableName, columnNames, columnValues, sWhereClause);
  		    	        logger.debug("Input XML for apUpdateInput for " + tableName + " Table : " + inputXML);
  		    	        String outputXml = CommonMethods.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
  		    	        logger.debug("Output XML for apUpdateInput for " + tableName + " Table : " + outputXml);
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
          		logger.info("Inside process method where procedure need to be triggered");
          		String param = "'" +processInstanceID+ "'";
                  String Proc_inputXMl = CommonMethods.ExecuteQuery_APProcedure(Proc_name, param, CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false));
                  logger.info("Input xml for procedure"+Proc_inputXMl);

                  String outXml = CommonMethods.WFNGExecute(Proc_inputXMl,CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
                  logger.info("Output xml for procedure "+outXml);
                  XMLParser xmlParserDataDB = new XMLParser(outXml);
  		        
                  String Proc_main_Code =  xmlParserDataDB.getValueOf("MainCode");
                  logger.error("Maincode for procedure : " + Proc_main_Code);
              	
              }
              
              if ("Y".equalsIgnoreCase(flag) && !(CheckGridDataMap.get("Dectech_Flag").equalsIgnoreCase("Y"))) 
              {
            logger.error("Inside DECTECH Call for WI Number: " + processInstanceID);
              	
              	//rubi
            	Digital_PL_DECTECH_Integration decttech_Integration = new Digital_PL_DECTECH_Integration();
              	String output=decttech_Integration.GenerateXML(processInstanceID, ActivityID, ActivityType, ProcessDefId,WorkItemID);
              	if(!"Success".equalsIgnoreCase(output))
              	{
              		MainStatusFlag = "Failure~DECTECH call failed";
              	}
              	else{
              		CheckGridDataMap.put("Dectech_Flag", "Y");
              	}
              }
              //Cam generation ...
              
              logger.error("before CAM generation Is_CAM_Generated: " + Is_CAM_Generated);
              logger.error("before CAM generation Dectech_Flag: " + CheckGridDataMap.get("Dectech_Flag"));
              logger.error("before CAM generation AECB flag: " + flag);
              
             // if("Y".equalsIgnoreCase(flag) &&!"Y".equalsIgnoreCase(Is_CAM_Generated))
            if("Y".equalsIgnoreCase(flag) && CheckGridDataMap.get("Dectech_Flag").equalsIgnoreCase("Y") && !"Y".equalsIgnoreCase(Is_CAM_Generated))
		        {
              	
              	String DBQuery1 = "SELECT CIF,IsSTP,DectechDecision,IsFIRCOHit,EFMS_Status,FTS_Decision,IsAECBSalary,BandStatus,EFR_NSTP FROM NG_DPL_EXTTABLE with(nolock) WHERE WINAME='" + processInstanceID + "'";
  		        
  		        String extTabDataINPXML = CommonMethods.apSelectWithColumnNames(DBQuery1, CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false));
  		        logger.debug("extTabDataIPXML: " + extTabDataINPXML);
  		        String extTabDataOUPXML = CommonMethods.WFNGExecute(extTabDataINPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
  		        logger.debug("extTabDataOPXML: " + extTabDataOUPXML);	
  				
  		        XMLParser xmlParserDataDB = new XMLParser(extTabDataOUPXML);
  		        
  		        String mainCode=xmlParserDataDB.getValueOf("MainCode");
  		        int iTotalreccam = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
  		        if("0".equalsIgnoreCase(mainCode) && iTotalreccam>0)
  		        {
  		        	
  			        String Cif_Id = xmlParserDataDB.getValueOf("CIF");
  			        String Is_STP = xmlParserDataDB.getValueOf("IsSTP");
  			        String Dectech_Decision =xmlParserDataDB.getValueOf("DectechDecision");
  			        String firco=xmlParserDataDB.getValueOf("IsFIRCOHit");
  			        String efms=xmlParserDataDB.getValueOf("EFMS_Status");
  			        String FTS_Decision=xmlParserDataDB.getValueOf("FTS_Decision");
  			        String IsAECBSalary=xmlParserDataDB.getValueOf("IsAECBSalary");
  			        String EFR_NSTP=xmlParserDataDB.getValueOf("EFR_NSTP");
  			        System.out.println("");
  			        //String fts =xmlParserDataDB.getValueOf("FTS_Ack_flg");
  			        String fircoAction =xmlParserDataDB.getValueOf("FIRCO_ActionedDate");
  			        //add for jira 1245 by rubi
  			      String BandStatus =xmlParserDataDB.getValueOf("BandStatus");
  			      
  			        String pdfName = "";
  			        
  				
  			        if(!Is_STP.isEmpty()){
  			        	//changes as per jira 1309
  			        	if("A".equalsIgnoreCase(Dectech_Decision))
  			        	{
  			        		if(("Failure".equalsIgnoreCase(FTS_Decision)&&"N".equalsIgnoreCase(IsAECBSalary)) || "Y".equalsIgnoreCase(firco) || "Y".equalsIgnoreCase(EFR_NSTP) ){
  			        			pdfName = "DPL_NON_STP_CAM_Report";
  			        		}
  			        		else{
  			        			pdfName="DPL_STP_CAM_Report";
  			        		}
  			        	}
  			        	else if("A".equalsIgnoreCase(Dectech_Decision) && "High".equalsIgnoreCase(BandStatus)){
  			        		pdfName = "DPL_NON_STP_CAM_Report";
  			        	}
  			        	else if(("Y".equalsIgnoreCase(Is_STP) &&("D".equalsIgnoreCase(Dectech_Decision) || "R".equalsIgnoreCase(Dectech_Decision) || !"High".equalsIgnoreCase(BandStatus))))
  			        	{
  			        		pdfName = "DPL_STP_CAM_Report";
  			        	}
  			        	
  			        	else if(("N".equalsIgnoreCase(Is_STP) &&("D".equalsIgnoreCase(Dectech_Decision) || "R".equalsIgnoreCase(Dectech_Decision))))
  			        	{
  			        		pdfName = "DPL_NON_STP_CAM_Report";
  			        	}
  			        	
  			        	
  			        	/*if(("N".equalsIgnoreCase(Is_STP) &&("D".equalsIgnoreCase(Dectech_Decision) || "R".equalsIgnoreCase(Dectech_Decision)))||("Failure".equalsIgnoreCase(FTS_Decision)&&"N".equalsIgnoreCase(IsAECBSalary)) || "Y".equalsIgnoreCase(firco)){
  			        		pdfName = "DPL_NON_STP_CAM_Report";
  			        	}else{
  			        		 pdfName = "DPL_STP_CAM_Report";
  			        	}*/
  			        	
  			        	Digital_PL_CAMTemplate obj = new Digital_PL_CAMTemplate(logger);
	    		        String output = obj.DPL_generate_CAM_ReportT(pdfName,Cif_Id,processInstanceID,CommonConnection.getSessionID(logger, false));
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
	    		    	        String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false), 
	    		    	        		tableName, columnNames, columnValues, sWhereClause);
	    		    	        logger.debug("Input XML for apUpdateInput for " + tableName + " Table : " + inputXML);
	    		    	        String outputXml = CommonMethods.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
	    		    	        logger.debug("Output XML for apUpdateInput for " + tableName + " Table : " + outputXml);
	    		    	        XMLParser sXMLParserChild = new XMLParser(outputXml);
	    		    	        String StrMainCode = sXMLParserChild.getValueOf("MainCode");
	    		    	        if(!"0".equals(StrMainCode))
	    		    	        	MainStatusFlag = "Failure~Error in Cam Flag Update.";
	    		        	}

  			        	
  			        }else{
  			        	MainStatusFlag = "Failure~Error in Cam Generation(STP flag is not valid).";
  			        	logger.debug("Error in Cam Generation(STP flag is not valid): " + Is_STP);
  			        }
  			        //if ("Y".equalsIgnoreCase(Is_STP) || "D".equalsIgnoreCase(Dectech_Decision) || "A".equalsIgnoreCase(Dectech_Decision)||"CB".equalsIgnoreCase(firco)||"Confirmed Fraud".equalsIgnoreCase(efms)||"D".equalsIgnoreCase(fts)||"Decline".equalsIgnoreCase(fircoAction))

  			       
  			        		
  			        		
  			        		
  			        /*if ("Y".equalsIgnoreCase(Is_STP))	
  			        {
  		        		 pdfName = "DPL_STP_CAM_Report";
  		        		 
  		        		 	Digital_PL_CAMTemplate obj = new Digital_PL_CAMTemplate(logger);
  	    		        	String output = obj.DPL_generate_CAM_ReportT(pdfName,Cif_Id,processInstanceID,CommonConnection.getSessionID(logger, false));
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
  	    		    	        String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false), 
  	    		    	        		tableName, columnNames, columnValues, sWhereClause);
  	    		    	        logger.debug("Input XML for apUpdateInput for " + tableName + " Table : " + inputXML);
  	    		    	        String outputXml = CommonMethods.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
  	    		    	        logger.debug("Output XML for apUpdateInput for " + tableName + " Table : " + outputXml);
  	    		    	        XMLParser sXMLParserChild = new XMLParser(outputXml);
  	    		    	        String StrMainCode = sXMLParserChild.getValueOf("MainCode");
  	    		    	        if(!"0".equals(StrMainCode))
  	    		    	        	MainStatusFlag = "Failure~Error in Cam Flag Update.";
  	    		        	}
  			        }
  			       	else if("N".equalsIgnoreCase(Is_STP)){
  			       		pdfName = "DPL_NON_STP_CAM_Report";
		        		 
	        		 	Digital_PL_CAMTemplate obj = new Digital_PL_CAMTemplate(logger);
	        		 	String output = obj.DPL_generate_CAM_ReportT(pdfName,Cif_Id,processInstanceID,CommonConnection.getSessionID(logger, false));
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
	        		 		String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false), 
  		    	        		tableName, columnNames, columnValues, sWhereClause);
	        		 		logger.debug("Input XML for apUpdateInput for " + tableName + " Table : " + inputXML);
	        		 		
	        		 		String outputXml = CommonMethods.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
	        		 		logger.debug("Output XML for apUpdateInput for " + tableName + " Table : " + outputXml);
	        		 		
	        		 		XMLParser sXMLParserChild = new XMLParser(outputXml);
	        		 		String StrMainCode = sXMLParserChild.getValueOf("MainCode");
	        		 		if(!"0".equals(StrMainCode))
	        		 			MainStatusFlag = "Failure~Error in Cam Flag Update.";
	        		 		}
  			       	}
  			        else if(!"N".equalsIgnoreCase(Is_STP))
  			        {
  			        	MainStatusFlag = "Failure~Error in Cam Generation(STP flag is not valid).";
  			        	logger.debug("Error in Cam Generation(STP flag is not valid): " + Is_STP);
  			        }*/
  		        	/*else
  		        	{
  		        		 pdfName = "NON_STP_CAM_Report";
  		        	}*/
  		        	
  			        
  		        }
  		        else
  		        {
  		      logger.debug("Error in cam generation..ApSelect for Is_CAM_Generated flag..");
  		        	MainStatusFlag = "Failure~Error in cam Generation";
  		        }
		        	
				}
		        else
		        {
		      logger.debug("Cam Report Is Already Generated");
				}
			 }
        }
      else
        {
          logger.debug("WmgetWorkItem status: " + xmlParserData.getValueOf("MainCode"));
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
		        logger.debug("Request XML for ExternalExposure  " + sInputXML);
		        
				CommonConnection.setsProcessNameDPL("Digital_PL");
				logger.debug("setsProcessNameDPL" + CommonConnection.getsProcessNameDPL());
		        
		        String responseXML = socketConnection(CommonConnection.getCabinetName(), CommonConnection.getUsername(), CommonConnection.getSessionID(logger, false), 
		        		CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), wiName, "ExternalExposure", socketConnectionTimeOut, integrationWaitTime, socketDetailsMap, sInputXML);
		        
		        logger.debug("Request XML for responseXML  " + responseXML);
		        
		        flag = ResponseParser.getOutputXMLValues(responseXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),
		          CommonConnection.getSessionID(logger, false), CommonConnection.getCabinetName(), 
		          wiName, CheckGridDataMap.get("Product"), CheckGridDataMap.get("SubProduct"), CheckGridDataMap.get("CIF"), 
		          CheckGridDataMap.get("CUSTOMER_TYPE"));
		        flag = flag == "true" ? "Y" : "N";

		    	String columnNames = "EXTERNAL_EXPOSURE_STATUS";
		    	String columnValues = "'" + flag + "'";
		    	String sWhereClause = "WINAME='" + wiName + "'";
		        String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false), 
		        		CheckGridTable, columnNames, columnValues, sWhereClause);
		        logger.debug("Input XML for apUpdateInput for " + CheckGridTable + " Table : " + inputXML);
		        String outputXml = CommonMethods.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
		        logger.debug("Output XML for apUpdateInput for " + CheckGridTable + " Table : " + outputXml);
		        XMLParser sXMLParserChild = new XMLParser(outputXml);
		        String StrMainCode = sXMLParserChild.getValueOf("MainCode");
		        //String RetStatus = null;
		        if (StrMainCode.equals("0"))
		          {
		            logger.debug("Successful in apUpdateInput the record in : " + CheckGridTable);
		            //RetStatus = "Success in apUpdateInput the record";
		          }
		        else
		          {
		            logger.debug("Error in Executing apUpdateInput sOutputXML : " + outputXml);
		            //RetStatus = "Error in Executing apUpdateInput";
		          }
		        return flag;
		      }
	
	private static StringBuilder sInputXml(String wiName, String DateExtra2, HashMap<String, String> CheckGridDataMap) {
		String emi_id = CheckGridDataMap.get("EmirateID");
		//String Override_period =CheckGridDataMap.get("OverridePeriod");
		if(emi_id.length()>14){
		emi_id = emi_id.substring(0, 3) + "-" + emi_id.substring(3, 7) + "-" + emi_id.substring(7, 14) + "-"+ emi_id.substring(14, 15);
		}
		String rerunAECB="false";
		String Override_period = "0";
		logger.debug("rerunAECB : " + rerunAECB);
		//rerunAECB=CheckGridDataMap.get("rerun_aecb");
		if(rerunAECB.equalsIgnoreCase("true")){
			Override_period="1";
		}// END
		logger.debug("Override_preiod : " + Override_period);
		
		
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
	    logger.debug("FullName : " + FullName);
		
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
			logger.debug("userName "+ username);
			logger.debug("SessionId "+ sessionId);

			socketServerIP=socketDetailsMap.get("SocketServerIP");
			logger.debug("SocketServerIP "+ socketServerIP);
			socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			logger.debug("SocketServerPort "+ socketServerPort);

	   		if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0))
	   		{

    			socket = new Socket(socketServerIP, socketServerPort);
    			socket.setSoTimeout(connection_timeout*1000);
    			out = socket.getOutputStream();
    			socketInputStream = socket.getInputStream();
    			dout = new DataOutputStream(out);
    			din = new DataInputStream(socketInputStream);
    			logger.debug("Dout " + dout);
    			logger.debug("Din " + din);

    			outputResponse = "";

    			inputRequest = getRequestXML( cabinetName,sessionId ,processInstanceID, ws_name, username, sInputXML);

    			if (inputRequest != null && inputRequest.length() > 0)
    			{
    				int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
    				logger.debug("RequestLen: "+inputRequestLen + "");
    				inputRequest = inputRequestLen + "##8##;" + inputRequest;
    				logger.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
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
					logger.debug("OutputResponse: " + outputResponse);

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

				logger.debug("outputResponse "+outputResponse);
				return outputResponse;

    	 	}
    		else
    		{
    			logger.debug("SocketServerIp and SocketServerPort is not maintained "+"");
    			logger.debug("SocketServerIp is not maintained "+	socketServerIP);
    			logger.debug(" SocketServerPort is not maintained "+	socketServerPort);
    			return "Socket Details not maintained";
    		}
		}
		catch (Exception e)
		{
			logger.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
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
				logger.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
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
	
	private static String validateValue(String value) {
		if (value != null && ! value.equals("") && !value.equalsIgnoreCase("null")) {
			return value.toString();
		}
		return "";
	}

}
