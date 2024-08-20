package com.newgen.DCC.Update_AssignCIF;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.newgen.DAO.AWB.DAO_AWB_Log;
import com.newgen.DCC.CAMGenCIFUpdate.Digital_CCLog;
import com.newgen.DCC.DECTECHIntegration.DECTECHSystemIntegrationLog;
import com.newgen.DCC.SystemIntegration.DCCSystemIntegrationLog;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;
import com.newgen.DCC.EFMS.DCC_MurabahaDealIntegration;
import com.newgen.DCC.Final_Limit_Increase.DCC_FINAL_LIMIT_LOG;
import com.newgen.DCC.EFMS.DCC_EFMS_Integration;
import com.newgen.DCC.EFMS.DCC_EFMS_IntegrationLog;

public class DCC_Update_Assign_CIF_SysIntegration implements Runnable {



	private static NGEjbClient ngEjbClientCIFVer;

	static Map<String, String> ConfigParamMap= new HashMap<String, String>();
	HashMap<String, String> socketDetailsMap = new HashMap<String,String> ();
	public static int sessionCheckInt=0;
	public static int waitLoop=50;
	public static int loopCount=50;
	private static  String cabinetName;
	private static  String jtsIP;
	private static  String jtsPort;
	private static String ActivityType;
	private static String ProcessDefId;
	private static String ActivityName;
	private static String ActivityID;
	private String sessionID = "";
	private static String queueID = "";
	private int socketConnectionTimeout=0;
	private int integrationWaitTime=0;
	private int sleepIntervalInMin=0;
	int TrialTime = 0;
	int ErrorCount = 0;

	@Override
	public void run()
	{
		
		

		try
		{
			DCC_UpdateAssignCIFLog.setLogger();
			ngEjbClientCIFVer = NGEjbClient.getSharedInstance();

			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.error("Could not Read Config Properties [RAOPStatus]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("JTSPORT: " + jtsPort);

			queueID = ConfigParamMap.get("queueID");
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("QueueID: " + queueID);

			socketConnectionTimeout=Integer.parseInt(ConfigParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(ConfigParamMap.get("INTEGRATION_WAIT_TIME"));
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(ConfigParamMap.get("SleepIntervalInMin"));
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("SleepIntervalInMin: "+sleepIntervalInMin);
			
			TrialTime=Integer.parseInt(ConfigParamMap.get("TrialTime"));
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("TrialTime: "+TrialTime);

			ErrorCount=Integer.parseInt(ConfigParamMap.get("ErrorCount"));
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("ErrorCount: "+ErrorCount);

			sessionID = CommonConnection.getSessionID(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger, false);
			socketDetailsMap = socketConnectionDetails(cabinetName, jtsIP, jtsPort, sessionID);
			
			if(sessionID.trim().equalsIgnoreCase(""))
			{
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Could Not Connect to Server!");
			}
			else
			{
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Session ID found: " + sessionID);
				
				while(true)
				{
					DCC_UpdateAssignCIFLog.setLogger();
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("DCC CIF Upadte and Assign CIF...123.");
					startDCCUpdateAssignCIFUtility(cabinetName, jtsIP, jtsPort,sessionID,queueID,socketDetailsMap);
					System.out.println("No More workitems to Process, Sleeping!");
					Thread.sleep(sleepIntervalInMin*60*1000);
				}
			}
		}

		catch(Exception e)
		{
			e.printStackTrace();
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.error("Exception Occurred in DCC CIF Verification : "+e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.error("Exception Occurred in DCC CIF Verification : "+result);
		}
	}

	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "DCC_UpdateAndAssignCIF_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			  {
			    String name = (String) names.nextElement();
			    ConfigParamMap.put(name, p.getProperty(name));
			  }
		    }
		catch (Exception e)
		{
			return -1 ;
		}
		return 0;
	}


	private void startDCCUpdateAssignCIFUtility(String cabinetName,String sJtsIp,String iJtsPort,String sessionId,String queueID,HashMap<String, String> socketDetailsMap)
	{
		final String ws_name=ConfigParamMap.get("WS_NAME");
		final String Queuename=ConfigParamMap.get("QueueName");

		try
		{
			//Validate Session ID
			sessionId  = CommonConnection.getSessionID(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger, false);
			sessionID = sessionId;
			if(sessionId==null || sessionId.equalsIgnoreCase("") || sessionId.equalsIgnoreCase("null"))
			{
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.error("Could Not Get Session ID "+sessionId);
				return;
			}

			//Fetch all Work-Items on given queueID.
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Fetching all Workitems on "+Queuename+" queue");
			System.out.println("Fetching all Workitems on "+Queuename+" queue");
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionId, queueID);
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);

			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("WMFetchWorkList OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);

			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Number of workitems retrieved on CIF_Update_Initial: "+fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on CIF_Update_Initial: "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0)
			{
				for(int i=0; i<fetchWorkitemListCount; i++)
				{
					String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID = xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Current ProcessInstanceID: "+processInstanceID);

					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Processing Workitem: "+processInstanceID);
					System.out.println("\nProcessing Workitem: "+processInstanceID);

					String WorkItemID= xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Current WorkItemID: "+WorkItemID);

					String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Current EntryDateTime: "+entryDateTime);

					ActivityName=xmlParserfetchWorkItemData.getValueOf("ActivityName");
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("ActivityName: "+ActivityName);
					
					ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("ActivityID: "+ActivityID);
					ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("ActivityType: "+ActivityType);
					ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("ProcessDefId: "+ProcessDefId);
					
					String DB_Query = "SELECT NTB,Product,IS_virtual_Card_Created,Is_CIF_ASSIGNED,CardOps_Reschedule,Is_CIF_UPDATED,FIRCO_Flag,EFMS_Status,FTS_Ack_flg,is_stp,Dectech_Decision,FircoUpdateAction,UW_Decision,Product,Preferred_Language,Nationality,FATCA_Tin_Number FROM NG_DCC_EXTTABLE with(nolock) WHERE WI_NAME='" + processInstanceID + "'";
			        
			        String extTabDataINPXML = CommonMethods.apSelectWithColumnNames(DB_Query, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger, false));
			        DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("extTabDataIPXML: " + extTabDataINPXML);
			        String extTabDataOUPXML = CommonMethods.WFNGExecute(extTabDataINPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
			        DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("extTabDataOPXML: " + extTabDataOUPXML);	
					
			        XMLParser xmlParserData = new XMLParser(extTabDataOUPXML);
			        int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
			        
			        String decisionValue="";
					String attributesTag="";
					String updateCIFIntegrationStatus="";
					String ErrDesc = "";
					
			        if (!xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec == 0)
			        {
						decisionValue = "Failed";
						ErrDesc="apselect for Fetching WI details failed" ;
						attributesTag="<Decision>"+decisionValue+"</Decision>";
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("apselect for Fetching WI details failed");
						doneWI(processInstanceID, WorkItemID, decisionValue, entryDateTime, ErrDesc, attributesTag, sessionId);
						continue;
					}
			        
			        String Is_CIF_UPDATED = xmlParserData.getValueOf("Is_CIF_UPDATED");
			        String Is_CIF_ASSIGNED = xmlParserData.getValueOf("Is_CIF_ASSIGNED");
			        String CardOps_Reschedule = xmlParserData.getValueOf("CardOps_Reschedule");
			        String IS_virtual_Card_Created = xmlParserData.getValueOf("IS_virtual_Card_Created");
			        String NTB = xmlParserData.getValueOf("NTB");
			        
			        DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.error("IS_virtual_Card_Created: "+IS_virtual_Card_Created);
			        
			        if(!"Y".equalsIgnoreCase(IS_virtual_Card_Created) && "true".equalsIgnoreCase(NTB))
					{
			        	DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.error("Murabha at Update Assign CIF ");
						String status=executeMurahabhaCalls(processInstanceID,ws_name,WorkItemID,entryDateTime,ActivityType);
						if("Error".equalsIgnoreCase(status)){
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.error("Murabha status "+status );
							continue;
						}							
					}

			        if (!"Y".equalsIgnoreCase(CardOps_Reschedule) && !"Y".equalsIgnoreCase(Is_CIF_UPDATED) && "Y".equalsIgnoreCase(IS_virtual_Card_Created) && "true".equalsIgnoreCase(NTB))
			        {
			        	DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.error("Inside UPDATE CIF");
			        	DCC_CIFUpdate objUpadteCIF = new DCC_CIFUpdate(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger);
						updateCIFIntegrationStatus = objUpadteCIF.customIntegration(cabinetName,sessionId, sJtsIp,iJtsPort,processInstanceID,ws_name,integrationWaitTime,socketConnectionTimeout,socketDetailsMap);
						if (!"Success".equalsIgnoreCase(updateCIFIntegrationStatus)) {
							ErrDesc = updateCIFIntegrationStatus.replace("~", ",").replace("|", "\n");
							decisionValue = "Failed";
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Decision" +decisionValue);
							attributesTag="<Decision>"+decisionValue+"</Decision>";
							doneWI(processInstanceID,WorkItemID,decisionValue,entryDateTime,ErrDesc,attributesTag,sessionId);
							continue;
						} else {
							/**Update Is_CIF_UPDATED value in exttable to Y**/
							if (updateFlagInExtTable("Y", "Is_CIF_UPDATED", processInstanceID, entryDateTime, sessionId)){
								Is_CIF_UPDATED = "Y";
							} else
								continue;
						}
			        }
			        
			        if((!"Y".equalsIgnoreCase(CardOps_Reschedule) && "Y".equalsIgnoreCase(Is_CIF_UPDATED) && !"Y".equalsIgnoreCase(Is_CIF_ASSIGNED) && "Y".equalsIgnoreCase(IS_virtual_Card_Created) && "true".equalsIgnoreCase(NTB))
			        ||(!"Y".equalsIgnoreCase(CardOps_Reschedule) && !"Y".equalsIgnoreCase(Is_CIF_UPDATED) && !"Y".equalsIgnoreCase(Is_CIF_ASSIGNED) && !"Y".equalsIgnoreCase(IS_virtual_Card_Created) && !"Y".equalsIgnoreCase(Is_CIF_ASSIGNED) && "true".equalsIgnoreCase(NTB)))
			        {
			        	DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.error("Inside ASSIGN CIF");
			        	DCC_Assign_CIF objAssignCIF= new DCC_Assign_CIF(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger);
						
			        	String firco=xmlParserData.getValueOf("FIRCO_Flag");
			        	String fircoAction=xmlParserData.getValueOf("FircoUpdateAction");
			        	String efms=xmlParserData.getValueOf("EFMS_Status");
			        	String fts =xmlParserData.getValueOf("FTS_Ack_flg");
			        	String stp =xmlParserData.getValueOf("is_stp");
			        	String DectechDecision =xmlParserData.getValueOf("Dectech_Decision");
			        	String UWDecision=xmlParserData.getValueOf("UW_Decision");
			        	
			        	DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.error("Inside ASSIGN CIF");
			        	
			        	if("Y".equalsIgnoreCase(IS_virtual_Card_Created))
			        	{
				        	if("Reject".equalsIgnoreCase(UWDecision) || "D".equalsIgnoreCase(DectechDecision) || "CB".equalsIgnoreCase(firco) 
				        	|| "Confirmed Fraud".equalsIgnoreCase(efms)||("Y".equalsIgnoreCase(stp)&&"R".equalsIgnoreCase(DectechDecision)) 
				        	|| "D".equalsIgnoreCase(fts) || "Decline".equalsIgnoreCase(fircoAction))
				        	{
				        		String NOTIFY_DEH_IDENTIFIER="Decline_Prospect";
				        		if("D".equalsIgnoreCase(fts)|| "Decline".equalsIgnoreCase(fircoAction))
				        			NOTIFY_DEH_IDENTIFIER="Expire_Prospect";
				        		decisionValue="Reject";
								attributesTag="<Decision>"+decisionValue+"</Decision>"+
											"<NOTIFY_DEH_IDENTIFIER>"+NOTIFY_DEH_IDENTIFIER+"</NOTIFY_DEH_IDENTIFIER>";
								ErrDesc = "CIF Update Done Successfully";
								doneWI(processInstanceID,WorkItemID,decisionValue,entryDateTime,ErrDesc,attributesTag,sessionId);
								continue;
				        	}
			        	}
				        
						/*
						String assignCIFIntegrationStatus=objAssignCIF.DCC_Assign_CIF_Integration(cabinetName,jtsIP,jtsPort,sessionId,processInstanceID,ws_name,integrationWaitTime,socketConnectionTimeout,socketDetailsMap);
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("assignCIFIntegrationStatus" +assignCIFIntegrationStatus);
						String statuses [] = null;
						
						if (assignCIFIntegrationStatus != null)
							statuses = assignCIFIntegrationStatus.split("~");
							
						if (statuses != null && statuses.length > 0 && statuses[0].equalsIgnoreCase("0000"))
						{
							//Update Is_CIF_ASSIGNED value in exttable to Y
							if (updateFlagInExtTable("Y", "Is_CIF_ASSIGNED", processInstanceID, entryDateTime, sessionId)){
								Is_CIF_ASSIGNED = "Y";											
							} else
								continue;		        	
						} else {
							if (statuses != null && statuses.length > 0) {
								ErrDesc = "Assign CIF Failed " + statuses[0] + ":";
								if (statuses.length > 1)
									ErrDesc =ErrDesc +statuses[1];
							} else {
								ErrDesc = "Assign CIF Failed ";
							}
							decisionValue = "Failed";
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Decision" +decisionValue);
							attributesTag="<Decision>"+decisionValue+"</Decision>";
							doneWI(processInstanceID,WorkItemID,decisionValue,entryDateTime,ErrDesc,attributesTag,sessionId);
							continue;
						}
						*/
			        }
			        
					//VINAYAK CHNGAES
			        
			       //chnages to execute service maintenance call only for ntb cases 
			        if("true".equalsIgnoreCase(NTB))
			        {
					Is_CIF_ASSIGNED="Y";
			        } 
			        if ( "Y".equalsIgnoreCase(Is_CIF_ASSIGNED)  && !"Y".equalsIgnoreCase(CardOps_Reschedule) && "true".equalsIgnoreCase(NTB)) {
			      
			        	
			        	//is_service_main
		        	DCC_Service_Maintenance objservice= new DCC_Service_Maintenance(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger);

					String serviceRequestIntegrationStatus=objservice.DCC_Service_Maintenance_Integration(cabinetName,jtsIP,jtsPort,sessionId,processInstanceID,ws_name,integrationWaitTime,socketConnectionTimeout,socketDetailsMap);
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("serviceRequestIntegrationStatus" +serviceRequestIntegrationStatus);
					String statuses [] = null;
					
					if (serviceRequestIntegrationStatus != null)
						statuses = serviceRequestIntegrationStatus.split("~");
						
					if (statuses != null && statuses.length > 0 && statuses[0].equalsIgnoreCase("0000"))
					{
						/**Update Is_CIF_ASSIGNED value in exttable to Y**/
						if (updateFlagInExtTable("Y", "is_service_main", processInstanceID, entryDateTime, sessionId)){
							String is_service_main = "Y";
						} else
							continue;
								        	
					} 
					else 
					{
						if (statuses != null && statuses.length > 0) {
							ErrDesc = "Service Maintenance Failed " + statuses[0] + ":";
							if (statuses.length > 1)
								ErrDesc =ErrDesc +statuses[1];
						} else {
							ErrDesc = "Service Maintenance Failed ";
						}
						decisionValue = "Failed";
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Decision" +decisionValue);
						attributesTag="<Decision>"+decisionValue+"</Decision>";
						doneWI(processInstanceID,WorkItemID,decisionValue,entryDateTime,ErrDesc,attributesTag,sessionId);
						continue;
					}
					
					//vinayak changes ends
				}
					
			        //Updated 14122022
			        if(("Y".equalsIgnoreCase(Is_CIF_UPDATED) && "Y".equalsIgnoreCase(Is_CIF_ASSIGNED)  && !"Y".equalsIgnoreCase(CardOps_Reschedule) && "Y".equalsIgnoreCase(IS_virtual_Card_Created) && "true".equalsIgnoreCase(NTB))
			        ||(!"Y".equalsIgnoreCase(Is_CIF_UPDATED) && "Y".equalsIgnoreCase(Is_CIF_ASSIGNED)  && !"Y".equalsIgnoreCase(CardOps_Reschedule) && !"Y".equalsIgnoreCase(IS_virtual_Card_Created)&& "true".equalsIgnoreCase(NTB))
			        ||(!"Y".equalsIgnoreCase(Is_CIF_UPDATED) && !"Y".equalsIgnoreCase(Is_CIF_ASSIGNED) && !"Y".equalsIgnoreCase(CardOps_Reschedule))&& !"true".equalsIgnoreCase(NTB)) {
			        	
						String Product=xmlParserData.getValueOf("Product");
						String nationality=xmlParserData.getValueOf("Nationality");
			        	String Preferred_Language=xmlParserData.getValueOf("Preferred_Language");
			        	String TIN=xmlParserData.getValueOf("FATCA_Tin_Number");
			        	
			        	
			        	
						DCC_DocumentGeneration objDocGen = new DCC_DocumentGeneration(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger);
						String docToBeGen="";
						if("ISL".equalsIgnoreCase(Product))
						{
							docToBeGen="MRBH_Agency_Agreement";
							if("AR".equalsIgnoreCase(Preferred_Language))
								docToBeGen=docToBeGen+"~Customer_Consent_Form_Islamic-Arabic";
							else
								docToBeGen=docToBeGen+"~Customer_Consent_Form_Islamic-English";
						}
						else
						{
							if ("AR".equalsIgnoreCase(Preferred_Language)){
								docToBeGen = "Customer_Consent_Form_Conv-Arabic";
							}
							else{
								docToBeGen = "Customer_Consent_Form_Conv-English";
							}
						}
						
						if("US".equalsIgnoreCase(nationality))
						{
							docToBeGen=docToBeGen+"~W-9_Form";
						}
						else if(!"US".equalsIgnoreCase(nationality) && TIN!=null && !"".equalsIgnoreCase(TIN))
						{
							docToBeGen=docToBeGen+"~W-8_Form";
						}
						docToBeGen=docToBeGen+"~Security_Cheque";
					
						String docGenStatus=objDocGen.generate_Document_Customer_Consent(docToBeGen,processInstanceID, sessionId);
					
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("docGenStatus:--" +docGenStatus);
						if (docGenStatus == null || docGenStatus.contains("Error"))
						{
							decisionValue = "Failed";
							ErrDesc = "Doc Genration Failed";
							String err[] = docGenStatus.split("~");
							if(err.length>1)
								ErrDesc = "Doc Genration Failed for document "+err[1];
						}
						else if(docGenStatus.contains("Success"))
						{
							decisionValue = "Success";
							// Hritik --- 19.7.23  -- ETB Case       	
				        	if(!"true".equalsIgnoreCase(NTB)){
				        		insert_ng_digital_awb_status_DCC(processInstanceID,cabinetName,sessionID,sJtsIp,iJtsPort,ActivityName);
				        	}
						}
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Decision" +decisionValue);
						attributesTag="<Decision>"+decisionValue+"</Decision>";
						doneWI(processInstanceID,WorkItemID,decisionValue,entryDateTime,ErrDesc,attributesTag,sessionId);
						continue;
			        }
			        			        
			        if (("Y".equalsIgnoreCase(Is_CIF_UPDATED)&& "Y".equalsIgnoreCase(Is_CIF_ASSIGNED)  && "Y".equalsIgnoreCase(CardOps_Reschedule) && "Y".equalsIgnoreCase(IS_virtual_Card_Created) && "true".equalsIgnoreCase(NTB))
			        ||(!"Y".equalsIgnoreCase(Is_CIF_UPDATED) && "Y".equalsIgnoreCase(Is_CIF_ASSIGNED)  && "Y".equalsIgnoreCase(CardOps_Reschedule) && !"Y".equalsIgnoreCase(IS_virtual_Card_Created)&& "true".equalsIgnoreCase(NTB))
			        ||(!"Y".equalsIgnoreCase(Is_CIF_UPDATED) && !"Y".equalsIgnoreCase(Is_CIF_ASSIGNED) && "Y".equalsIgnoreCase(CardOps_Reschedule))&& !"true".equalsIgnoreCase(NTB))			        
			        {
			        	
			        	DCC_DocumentGeneration objDocGen = new DCC_DocumentGeneration(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger);
			        	String Preferred_Language=xmlParserData.getValueOf("Preferred_Language");
			        	String Product=xmlParserData.getValueOf("Product");
						String nationality=xmlParserData.getValueOf("Nationality");
			        	String TIN=xmlParserData.getValueOf("FATCA_Tin_Number");
			        	
			        	
			        	
						String doc_DB_Query = "select Doc_Name from NG_DCC_GR_DOCUMENT_NAME with(nolock) WHERE WI_NAME='" + processInstanceID + "'";
				        
				        String docGRDataINPXML = CommonMethods.apSelectWithColumnNames(doc_DB_Query, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger, false));
				        DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("docGRDataINPXML: " + docGRDataINPXML);
				        String docGRDataOUPXML = CommonMethods.WFNGExecute(docGRDataINPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
				        DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("docGRDataOUPXML: " + docGRDataOUPXML);	
						
				        XMLParser xmlParserData1 = new XMLParser(docGRDataOUPXML);
				        int iTotalrec1 = Integer.parseInt(xmlParserData1.getValueOf("TotalRetrieved"));
				        
				        String decisionValue1="";
						String attributesTag1="";
						String ErrDesc1 = "";
						String docToBeGen1 = "";
						
				        if (!xmlParserData1.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec1 == 0)
				        {
							decisionValue1 = "Failed";
							ErrDesc1="apselect for Fetching WI details failed" ;
							attributesTag1="<Decision>"+decisionValue1+"</Decision>";
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("apselect for Fetching WI doc name failed");
							doneWI(processInstanceID, WorkItemID, decisionValue, entryDateTime, ErrDesc1, attributesTag1, sessionId);
							continue;
						}
						
						if(xmlParserData1.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec1 > 0){
						
						for(int k = 0;k < iTotalrec1;k++){
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("----Reschedule flag document Name -- "+ k);
							String doc_name_record =xmlParserData1.getNextValueOf("Record");
							doc_name_record = doc_name_record.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
							XMLParser docxmlParser = new XMLParser(doc_name_record);
							String doc_name = docxmlParser.getValueOf("doc_name");
							
							if(doc_name.equalsIgnoreCase("MRBH_Agency_Agreement")){
								docToBeGen1=docToBeGen1+"~MRBH_Agency_Agreement";
							}
							
							if(doc_name.equalsIgnoreCase("Customer_Consent_Form")){
								if("ISL".equalsIgnoreCase(Product))
								{
									if("AR".equalsIgnoreCase(Preferred_Language)){
									docToBeGen1=docToBeGen1+"~Customer_Consent_Form_Islamic-Arabic";
									
								}	
									else{
										docToBeGen1=docToBeGen1+"~Customer_Consent_Form_Islamic-English";
									}
										
								}
								else
								{
									if ("AR".equalsIgnoreCase(Preferred_Language)){
										docToBeGen1 = "Customer_Consent_Form_Conv-Arabic";
									}
									else{
										docToBeGen1 = "Customer_Consent_Form_Conv-English";
									}
								}
							}
							
							if(doc_name.equalsIgnoreCase("US") || doc_name.equalsIgnoreCase("W8/W9")){
								
								if("US".equalsIgnoreCase(nationality))
								{
									docToBeGen1=docToBeGen1+"~W-9_Form";
								}
								else if(!"US".equalsIgnoreCase(nationality) && TIN!=null && !"".equalsIgnoreCase(TIN))
								{
									docToBeGen1=docToBeGen1+"~W-8_Form";
								}
							}
							
							if(doc_name.equalsIgnoreCase("W-9_Form") || doc_name.equalsIgnoreCase("W-9") || doc_name.equalsIgnoreCase("W9")){
								
									docToBeGen1=docToBeGen1+"~W-9_Form";
								
							}
							if(doc_name.equalsIgnoreCase("W-8_Form") || doc_name.equalsIgnoreCase("W-8") || doc_name.equalsIgnoreCase("W8")){
						
								docToBeGen1=docToBeGen1+"~W-8_Form";
							
							}
							
							if(doc_name.equalsIgnoreCase("Security_Cheque")){
								
								docToBeGen1=docToBeGen1+"~Security_Cheque";
							}
							
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("----Reschedule flag document Name -- Final Doc to be generated 123 "+docToBeGen1);	
							
						}
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("----Reschedule flag document Name -- Final Doc to be generated "+docToBeGen1);
						String docGenStatus=objDocGen.generate_Document_Customer_Consent(docToBeGen1,processInstanceID, sessionId);
						
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("docGenStatus:--" +docGenStatus);
						if (docGenStatus == null || docGenStatus.contains("Error"))
						{
							decisionValue = "Failed";
							ErrDesc = "Doc Genration Failed";
							String err[] = docGenStatus.split("~");
							if(err.length>1)
								ErrDesc = "Doc Genration Failed for document "+err[1];
						}
						else if(docGenStatus.contains("Success"))
						{
							decisionValue = "Success";
							/* comment vinayak on 25-07-23 as per mail
							// Hritik --- 19.7.23  -- ETB Cases
							if(!"true".equalsIgnoreCase(NTB)){
				        		insert_ng_digital_awb_status_DCC(processInstanceID,cabinetName,sessionID,sJtsIp,iJtsPort,ActivityName);
				        	}*/ 
						}
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Decision" +decisionValue);
						attributesTag="<Decision>"+decisionValue+"</Decision>";
						doneWI(processInstanceID,WorkItemID,decisionValue,entryDateTime,ErrDesc,attributesTag,sessionId);
						continue;
						}
						
						if(xmlParserData1.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec1 ==0){
							 Product=xmlParserData.getValueOf("Product");
							 nationality=xmlParserData.getValueOf("Nationality");
				        	 Preferred_Language=xmlParserData.getValueOf("Preferred_Language");
				        	 TIN=xmlParserData.getValueOf("FATCA_Tin_Number");
				        	
							 objDocGen = new DCC_DocumentGeneration(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger);
							String docToBeGen="";
							if("ISL".equalsIgnoreCase(Product))
							{
								docToBeGen="MRBH_Agency_Agreement";
								if("AR".equalsIgnoreCase(Preferred_Language))
									docToBeGen=docToBeGen+"~Customer_Consent_Form_Islamic-Arabic";
								else
									docToBeGen=docToBeGen+"~Customer_Consent_Form_Islamic-English";
							}
							else
							{
								if ("AR".equalsIgnoreCase(Preferred_Language)){
									docToBeGen = "Customer_Consent_Form_Conv-Arabic";
								}
								else{
									docToBeGen = "Customer_Consent_Form_Conv-English";
								}
							}
							
							if("US".equalsIgnoreCase(nationality))
							{
								docToBeGen=docToBeGen+"~W-9_Form";
							}
							else if(!"US".equalsIgnoreCase(nationality) && TIN!=null && !"".equalsIgnoreCase(TIN))
							{
								docToBeGen=docToBeGen+"~W-8_Form";
							}
							docToBeGen=docToBeGen+"~Security_Cheque";
						
							String docGenStatus=objDocGen.generate_Document_Customer_Consent(docToBeGen,processInstanceID, sessionId);
						
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("docGenStatus:--" +docGenStatus);
							if (docGenStatus == null || docGenStatus.contains("Error"))
							{
								decisionValue = "Failed";
								ErrDesc = "Doc Genration Failed";
								String err[] = docGenStatus.split("~");
								if(err.length>1)
									ErrDesc = "Doc Genration Failed for document "+err[1];
							}
							else if(docGenStatus.contains("Success"))
							{
								decisionValue = "Success";
							}
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Decision" +decisionValue);
							attributesTag="<Decision>"+decisionValue+"</Decision>";
							doneWI(processInstanceID,WorkItemID,decisionValue,entryDateTime,ErrDesc,attributesTag,sessionId);
							continue;
						}
					}
					//
			        }
			        
			}
		}
		catch (Exception e)
		{
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Exception: "+e.getMessage());
		}
	}
	
	public String executeMurahabhaCalls(String processInstanceID,String ws_name,String WorkItemID,String entryDateTime,String ActivityType)
	{
		String status="";
		String errorDesc="";
		try
		{
			String islamicflag=isIslamic(processInstanceID);
			if("Y".equalsIgnoreCase(islamicflag))
			{
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.error("Execute Murabha calls--");
				String query="select ResponseFlag from NG_DCC_MURABAHA_RESPONSE_DATA  with(nolock) where wi_name='"+processInstanceID+"'";
				
				String MURABAHAIPXML = CommonMethods.apSelectWithColumnNames(query,cabinetName,sessionID);
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("extTabDataIPXML: " + MURABAHAIPXML);
				String MURABAHAIPXMLOPXML = CommonMethods.WFNGExecute(MURABAHAIPXML,jtsIP,jtsPort, 1);
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("extTabDataOPXML: " + MURABAHAIPXMLOPXML);
	
				XMLParser xmlParserData = new XMLParser(MURABAHAIPXMLOPXML);
				int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
				String mainCode=xmlParserData.getValueOf("MainCode");
				String responseflag= xmlParserData.getValueOf("ResponseFlag");
				if("0".equalsIgnoreCase(mainCode) && (iTotalrec==0 || !"SUCCESS".equalsIgnoreCase(responseflag)) )
				{
					DCC_MurabahaDealIntegration  MurahabaObj = new DCC_MurabahaDealIntegration(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger);
					String MurabhaCallsStatus=MurahabaObj.MurabahaReqDeal(cabinetName, jtsIP, jtsPort, sessionID, processInstanceID, socketConnectionTimeout, integrationWaitTime, socketDetailsMap, TrialTime, ErrorCount, ws_name,"1");
					if("Success".equalsIgnoreCase(MurabhaCallsStatus))
					{
						return "Success";
					}
					else
					{
						status="F";
					}
				}
				else if(iTotalrec>0 && "SUCCESS".equalsIgnoreCase(responseflag))
				{
					return "Success";
				}
				else
				{
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.error("Some error occured in getting Murahabha Response flag--" +mainCode);
					status="F";
				}
			}
			else if("N".equalsIgnoreCase(islamicflag))
			{
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.error("Case is not islamic!");
				return "Success";
			}
			else
			{
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.error("Some error occured in getting ISLAMIC flag-");
				status="F";
			}
			
		}
		catch(Exception e)
		{
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.error("Exception in executing Murahabha calls-"+e.toString());
			status="F";
		}
		
		if("F".equalsIgnoreCase(status))
		{
			try {
			//	sendMail( cabinetName,sessionID, "Murabha" ,processInstanceID, jtsIP, jtsPort);
				routeToErrorHandling(cabinetName,jtsIP,jtsPort,sessionID,processInstanceID, WorkItemID, entryDateTime, ActivityID, ActivityType, ProcessDefId,"Error in Murabha Execution");
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.error("Exception in sending mail-"+e.toString());
			}
			return "Error";
		}
		return "Success";
	}
	
	private void routeToErrorHandling(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String processInstanceID, String WorkItemID, String entryDateTime, String ActivityID, String ActivityType,
			String ProcessDefId,String ErrDesc) throws IOException, Exception, ParseException {
		

			String decisionValue = "Failed";
			String attributesTag = "";
			
			attributesTag = "<Decision>" + decisionValue + "</Decision>";
				
			// Lock Workitem.
			String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID, WorkItemID);
			String getWorkItemOutputXml = CommonMethods.WFNGExecute(getWorkItemInputXML, sJtsIp, iJtsPort, 1);
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Output XML For WmgetWorkItemCall: " + getWorkItemOutputXml);

			XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
			String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("WmgetWorkItemCall Maincode:  " + getWorkItemMainCode);

			if (getWorkItemMainCode.trim().equals("0")) {
					
				//DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Successful in apUpdateInput the record in : " + "NG_DCC_EXTTABLE");
			
				String assignWorkitemAttributeInputXML = "<?xml version=\"1.0\"?><WMAssignWorkItemAttributes_Input>" 
						+ "<Option>WMAssignWorkItemAttributes</Option>" 
						+ "<EngineName>" + cabinetName + "</EngineName>" 
						+ "<SessionId>" + sessionId + "</SessionId>" 
						+ "<ProcessInstanceId>" + processInstanceID + "</ProcessInstanceId>" 
						+ "<WorkItemId>" + WorkItemID + "</WorkItemId>" 
						+ "<ActivityId>" + ActivityID + "</ActivityId>" 
						+ "<ProcessDefId>" + ProcessDefId + "</ProcessDefId>"
						+ "<LastModifiedTime></LastModifiedTime>" 
						+ "<ActivityType>" + ActivityType + "</ActivityType>" 
						+ "<complete>D</complete>" 
						+ "<AuditStatus></AuditStatus>"
						+ "<Comments></Comments>" 
						+ "<UserDefVarFlag>Y</UserDefVarFlag>" 
						+ "<Attributes>" + attributesTag + "</Attributes>" 
						+ "</WMAssignWorkItemAttributes_Input>";
	
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("InputXML for assignWorkitemAttribute Call: " + assignWorkitemAttributeInputXML);
	
				String assignWorkitemAttributeOutputXML = CommonMethods.WFNGExecute(assignWorkitemAttributeInputXML, sJtsIp, iJtsPort, 1);
	
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("OutputXML for assignWorkitemAttribute Call: " + assignWorkitemAttributeOutputXML);
	
				XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
				String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("AssignWorkitemAttribute MainCode: " + assignWorkitemAttributeMainCode);
	
				if (assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0")) {
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("AssignWorkitemAttribute Successful: " + assignWorkitemAttributeMainCode);
	
					// Move Workitem to next Workstep
	
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("WorkItem moved to next Workstep.");
	
					SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
					SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");
	
					Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
					String formattedEntryDatetime = outputDateFormat.format(entryDatetimeFormat);
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("FormattedEntryDatetime: " + formattedEntryDatetime);
	
					Date actionDateTime = new Date();
					String formattedActionDateTime = outputDateFormat.format(actionDateTime);
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("FormattedActionDateTime: " + formattedActionDateTime);
				
					String columnNames = "WI_NAME,dec_date,WORKSTEP,USER_NAME,DECISION,decision_date_time,REMARKS";
					String columnValues = "'" + processInstanceID + "','" + formattedActionDateTime + "','" + ActivityName + "'," + "'System','" + decisionValue + "','"
							+ formattedEntryDatetime + "','" + ErrDesc + "'";
	
					String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionId, columnNames, columnValues, "NG_DCC_GR_DECISION_HISTORY");
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("APInsertInputXML: " + apInsertInputXML);
	
					String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, sJtsIp, iJtsPort, 1);
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("APInsertOutputXML: " + apInsertOutputXML);
	
					XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
					String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Status of apInsertMaincode  " + apInsertMaincode);
	
					if (apInsertMaincode.equalsIgnoreCase("0")) {
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("ApInsert successful: " + apInsertMaincode);
					} else {
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("ApInsert failed: " + apInsertMaincode);
					}
				} else {
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("AssignWorkitemAttribute failed: " + assignWorkitemAttributeMainCode);
				}
			}
	}
	
	private String isIslamic(String processInstanceID)
	{
		String isIslamic="";
		try
		{
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Check if the case  is Islamic--");
			String query="select Product from NG_DCC_EXTTABLE  with(nolock) where wi_name='"+processInstanceID+"'";
			
			String ISLAMIC_CHECK_IPXML = CommonMethods.apSelectWithColumnNames(query,cabinetName,sessionID);
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("extTabDataIPXML: " + ISLAMIC_CHECK_IPXML);
			String ISLAMIC_CHECK_OPXML = CommonMethods.WFNGExecute(ISLAMIC_CHECK_IPXML,jtsIP,jtsPort, 1);
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("extTabDataOPXML: " + ISLAMIC_CHECK_OPXML);

			XMLParser xmlParserData = new XMLParser(ISLAMIC_CHECK_OPXML);
			
			String mainCode=xmlParserData.getValueOf("MainCode");
			String poduct= xmlParserData.getValueOf("Product");
			if("0".equalsIgnoreCase(mainCode) )
			{
				int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
				if(iTotalrec>0)
				{
					if("ISL".equalsIgnoreCase(poduct))
					{
						isIslamic="Y";
					}
					else
						isIslamic="N";	
				}
				else
				{
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Islamic data is not available for this item--" +processInstanceID);
					isIslamic="E";	
				}
			}
			else
			{
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Some error occured in getting product--" +mainCode);
				isIslamic="E";	
			}
		}
		catch (Exception e)
		{
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Exception in isIslamic method--" +e.toString());
			isIslamic="E";
		}
		return isIslamic;
	}

	private boolean updateFlagInExtTable(String columnValue, String columnNames, String processInstanceID, String entryDateTime, String sessionId) {
		columnValue = "'" + columnValue + "'";
		DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("column Names for AP udpate call : " +columnNames);
		
		String sWhereClause = "WI_NAME='" + processInstanceID + "'";
		String tableName = "NG_DCC_EXTTABLE";
		try {
		    String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger, false), 
		    		tableName, columnNames, columnValue, sWhereClause);
		    DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Input XML for apUpdateInput for " + tableName + " Table : " + inputXML);
		    String outputXml = CommonMethods.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
		
		    DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Output XML for apUpdateInput for " + tableName + " Table : " + outputXml);
		    XMLParser sXMLParserChild = new XMLParser(outputXml);
		    String StrMainCode = sXMLParserChild.getValueOf("MainCode");
		    if (StrMainCode.equals("0")) {
		    	DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Output XML for apUpdateInput for : Success" );
		    	return true;
		    } 
		    else 
		    {
		    	DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Output XML for apUpdateInput for : Failed" );
		    	String decisionValue = "Failed";
		    	String ErrDesc="apupdate failed : " +  columnNames;
		    	String attributesTag="<Decision>"+decisionValue+"</Decision>";
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("apupdate for : " + columnNames);
				doneWI(processInstanceID, processInstanceID, decisionValue, entryDateTime, ErrDesc, attributesTag, sessionId);
				return false;
		    }
		} catch (Exception e) {
			String decisionValue = "Failed";
	    	String ErrDesc="Some Exception occured while updating : " +  e.getMessage();
	    	String attributesTag="<Decision>"+decisionValue+"</Decision>";
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("some error occured while ");
			doneWI(processInstanceID, processInstanceID, decisionValue, entryDateTime, ErrDesc, attributesTag, sessionId);
			return false;
		}
		
	}

	private void doneWI(String processInstanceID,String WorkItemID,String decisionValue,String entryDateTime ,String ErrDesc,String attributesTag,String sessionId)
	{
		try
		{
			//Lock Workitem.
			String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID,WorkItemID);
			String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML,jtsIP,jtsPort,1);
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Output XML For WmgetWorkItemCall: "+ getWorkItemOutputXml);

			XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
			String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);

			if (getWorkItemMainCode.trim().equals("0"))
			{
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);

				//String assignWorkitemAttributeInputXML=CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionId,processInstanceID,WorkItemID,attributesTag);
				
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
				
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("InputXML for assignWorkitemAttribute Call: "+assignWorkitemAttributeInputXML);

				String assignWorkitemAttributeOutputXML=WFNGExecute(assignWorkitemAttributeInputXML,jtsIP, jtsPort,1);

				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("OutputXML for assignWorkitemAttribute Call: "+assignWorkitemAttributeOutputXML);

				XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
				String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("AssignWorkitemAttribute MainCode: "+assignWorkitemAttributeMainCode);

				if(assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0"))
				{
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("AssignWorkitemAttribute Successful: "+assignWorkitemAttributeMainCode);	
					if ("0".trim().equalsIgnoreCase("0"))
					{
						//DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("WmCompleteWorkItem successful: "+completeWorkitemMaincode);
						System.out.println(processInstanceID + "Complete Succesfully with status "+decisionValue);

						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("WorkItem moved to next Workstep.");

						SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
						SimpleDateFormat outputDateFormat=new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

						Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
						String formattedEntryDatetime=outputDateFormat.format(entryDatetimeFormat);
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);

						Date actionDateTime= new Date();
						String formattedActionDateTime=outputDateFormat.format(actionDateTime);
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("FormattedActionDateTime: "+formattedActionDateTime);

						//Insert in WIHistory Table.
						String columnNames="WI_NAME,dec_date,WORKSTEP,USER_NAME,DECISION,ENTRY_DATE_TIME,REMARKS";
						String columnValues="'"+processInstanceID+"','"+formattedActionDateTime+"','"+ActivityName+"','"
						+CommonConnection.getUsername()+"','"+decisionValue+"','"+formattedEntryDatetime+"','"+ErrDesc+"'";

						String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionId, columnNames, columnValues,"NG_DCC_GR_DECISION_HISTORY");
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("APInsertInputXML: "+apInsertInputXML);

						String apInsertOutputXML = WFNGExecute(apInsertInputXML,jtsIP,jtsPort,1);
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("APInsertOutputXML: "+ apInsertInputXML);

						XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
						String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Completed On "+ ActivityName);


						if(apInsertMaincode.equalsIgnoreCase("0"))
						{
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("ApInsert successful: "+apInsertMaincode);
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Inserted in WiHistory table successfully.");
						}
						else
						{
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("ApInsert failed: "+apInsertMaincode);
						}
					}
					else
					{
						//completeWorkitemMaincode="";
						//DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("WMCompleteWorkItem failed: "+completeWorkitemMaincode);
					}
				}
				else if("11".equalsIgnoreCase(assignWorkitemAttributeMainCode)){
					
					sessionID = CommonConnection.getSessionID(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger, false);
					doneWI(processInstanceID,WorkItemID,decisionValue,entryDateTime ,ErrDesc,attributesTag,sessionID);
				}
				else
				{
					assignWorkitemAttributeMainCode="";
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("AssignWorkitemAttribute failed: "+assignWorkitemAttributeMainCode);
				}
			}
			else
			{
				getWorkItemMainCode="";
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("WmgetWorkItem failed: "+getWorkItemMainCode);
			}
		}
		
		catch (Exception e)
		{
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("DoneWI Exception: "+e.toString());
		}
	}


			private HashMap<String,String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort,String sessionID)
			{
				HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

				try
				{
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Fetching Socket Connection Details.");
					System.out.println("Fetching Socket Connection Details.");

					String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'DCC' and CallingSource = 'Utility'";

					String socketDetailsInputXML =CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Socket Details APSelect InputXML: "+socketDetailsInputXML);

					String socketDetailsOutputXML=WFNGExecute(socketDetailsInputXML,sJtsIp,iJtsPort,1);
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Socket Details APSelect OutputXML: "+socketDetailsOutputXML);

					XMLParser xmlParserSocketDetails= new XMLParser(socketDetailsOutputXML);
					String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("SocketDetailsMainCode: "+socketDetailsMainCode);

					int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("SocketDetailsTotalRecords: "+socketDetailsTotalRecords);

					if(socketDetailsMainCode.equalsIgnoreCase("0")&& socketDetailsTotalRecords>0)
					{
						String xmlDataSocketDetails=xmlParserSocketDetails.getNextValueOf("Record");
						xmlDataSocketDetails =xmlDataSocketDetails.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

						XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

						String socketServerIP=xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("SocketServerIP: "+socketServerIP);
						socketDetailsMap.put("SocketServerIP", socketServerIP);

						String socketServerPort=xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("SocketServerPort " + socketServerPort);
						socketDetailsMap.put("SocketServerPort", socketServerPort);

						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("SocketServer Details found.");
						System.out.println("SocketServer Details found.");

					}
				}
				catch (Exception e)
				{
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Exception in getting Socket Connection Details: "+e.getMessage());
					System.out.println("Exception in getting Socket Connection Details: "+e.getMessage());
				}

				return socketDetailsMap;
			}
			protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort,
					int flag) throws IOException, Exception
			{
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("In WF NG Execute : " + serverPort);
				try
				{
					if (serverPort.startsWith("33"))
						return WFCallBroker.execute(ipXML, jtsServerIP,
								Integer.parseInt(serverPort), 1);
					else
						return ngEjbClientCIFVer.makeCall(jtsServerIP, serverPort,
								"WebSphere", ipXML);
				}
				catch (Exception e)
				{
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Exception Occured in WF NG Execute : "+ e.getMessage());
					e.printStackTrace();
					return "Error";
				}
			}
			
			private  String getPreviousWorkStep( String sWorkItemName, String sWorkitemId )
			{
				String prevWS="";
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.info("Start of function getPreviousWorkStep ");
				String outputXML=null;
				String mainCode=null;
				try
				{

					sessionCheckInt=0;
					while(sessionCheckInt<loopCount)
					{
						try 
						{
							XMLParser objXMLParser = new XMLParser();
							String sqlQuery = "select PreviousStage from WFINSTRUMENTTABLE with(nolock) where ProcessInstanceID = '"+sWorkItemName+"' and WorkItemId='"+sWorkitemId+"'";
							String InputXML = CommonMethods.apSelectWithColumnNames(sqlQuery,cabinetName, sessionID);
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.info("Getting PreviousWorkStep from instrument table "+InputXML);
							outputXML = WFNGExecute(InputXML, jtsIP, jtsPort, 1);
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.info("OutputXML for getting PreviousWorkStep from external table "+outputXML);
							objXMLParser.setInputXML(outputXML);
							mainCode=objXMLParser.getValueOf("MainCode");
							if (mainCode.equalsIgnoreCase("0")) 
							{
								prevWS = CommonMethods.getTagValues(outputXML, "PreviousStage");
							}
						} 
						catch (Exception e) 
						{
							sessionCheckInt++;
							waiteloopExecute(waitLoop);
							continue;
						}
						if(mainCode.equalsIgnoreCase("0")){
							break;
						}
						else if (mainCode.equalsIgnoreCase("11"))
						{
							sessionID  = CommonConnection.getSessionID(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger, false);
						}
						else
						{
							sessionCheckInt++;
							break;
						}
					}


				}
				catch(Exception e)
				{
					e.printStackTrace();
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Inside catch of getPreviousWorkStep function with exception.."+e);
				}
				return prevWS;
			}
			private  String getUWDecision( String sWorkItemName, String sWorkitemId )
			{
				String decision="";
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.info("Start of function getUWDecision ");
				String outputXML=null;
				String mainCode=null;
				try
				{

					sessionCheckInt=0;
					while(sessionCheckInt<loopCount)
					{
						try 
						{
							XMLParser objXMLParser = new XMLParser();
							String sqlQuery = "select UW_Decision from NG_DCC_EXTTABLE with(nolock) where Wi_Name = '"+sWorkItemName+"'";
							String InputXML = CommonMethods.apSelectWithColumnNames(sqlQuery,cabinetName, sessionID);
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.info("Getting UW_Decision from NG_DCC_EXTTABLE table "+InputXML);
							outputXML = WFNGExecute(InputXML, jtsIP, jtsPort, 1);
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.info("OutputXML for getting UW_Decision from external table "+outputXML);
							objXMLParser.setInputXML(outputXML);
							mainCode=objXMLParser.getValueOf("MainCode");
							if (mainCode.equalsIgnoreCase("0")) 
							{
								decision = CommonMethods.getTagValues(outputXML, "UW_Decision");
								if(decision==null)
									decision="";
							}
						} 
						catch (Exception e) 
						{
							sessionCheckInt++;
							waiteloopExecute(waitLoop);
							continue;
						}
						if(mainCode.equalsIgnoreCase("0")){
							break;
						}
						else if (mainCode.equalsIgnoreCase("11"))
						{
							sessionID  = CommonConnection.getSessionID(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger, false);
						}
						else
						{
							sessionCheckInt++;
							break;
						}
					}

					
				}
				catch(Exception e)
				{
					e.printStackTrace();
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Inside catch of getUWDecision function with exception.."+e);
				}
				return decision;
			}
			
			private void insert_ng_digital_awb_status_DCC(String Wi_name,String cabinetName,String  sessionId, String sJtsIp,String iJtsPort,String ActivityName)
			{
				try
				{
					String process_name="";
					// select the values from ext table to insert into ng_digital_awb_status
					final HashMap<String, String> CheckGridDataMap_awb = new HashMap<String, String>();
					String DBQuery_awb ="select Wi_Name,Prospect_id,FirstName,MiddleName,LastName,MobileNo,email_id,AWB_Number,ntb,ECRN,isnull(CardOps_Reschedule,'N') as CardOps_Reschedule,EmirateID from NG_DCC_EXTTABLE where WI_name='" + Wi_name + "'";
					
					String extTabDataIPXML_awb =CommonMethods.apSelectWithColumnNames(DBQuery_awb, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger, false));
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("extTabDataOPXML_awb: " + extTabDataIPXML_awb);
					String extTabDataOPXML_awb = WFNGExecute(extTabDataIPXML_awb, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("extTabDataOPXML_awb: " + extTabDataOPXML_awb);
					XMLParser xmlParserData_awb = new XMLParser(extTabDataOPXML_awb);
					
					int iTotalrec = Integer.parseInt(xmlParserData_awb.getValueOf("TotalRetrieved"));
					
					if (xmlParserData_awb.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
					{
						String xmlDataExtTab = xmlParserData_awb.getNextValueOf("Record");
						xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
						NGXmlList objWorkList = xmlParserData_awb.createList("Records", "Record");
					
						for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)){
							CheckGridDataMap_awb.put("Wi_Name", objWorkList.getVal("Wi_Name"));
							CheckGridDataMap_awb.put("Prospect_id", objWorkList.getVal("Prospect_id"));
							CheckGridDataMap_awb.put("FirstName", objWorkList.getVal("FirstName"));
							CheckGridDataMap_awb.put("MiddleName", objWorkList.getVal("MiddleName"));
							CheckGridDataMap_awb.put("LastName", objWorkList.getVal("LastName"));
							CheckGridDataMap_awb.put("MobileNo", objWorkList.getVal("MobileNo"));
							CheckGridDataMap_awb.put("email_id", objWorkList.getVal("email_id"));
							CheckGridDataMap_awb.put("AWB_Number", objWorkList.getVal("AWB_Number"));
							CheckGridDataMap_awb.put("ntb", objWorkList.getVal("ntb"));
							CheckGridDataMap_awb.put("ECRN", objWorkList.getVal("ECRN"));
							CheckGridDataMap_awb.put("CardOps_Reschedule", objWorkList.getVal("CardOps_Reschedule"));
							CheckGridDataMap_awb.put("EmirateID", objWorkList.getVal("EmirateID"));
							
						}
						
						String processname[] = Wi_name.split("-");
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("processname [] : "+processname[0]);
						process_name=processname[0];
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("processname [] : "+process_name);
					}
					
					// hritik 08..=09.2022 - CR (CardSerno)
					
			/*		String Query_for_cardserno = "select CardSerno,ELITE_CRN from NG_DCC_PRIME_COURIER with (nolock) where WI_name='"+Wi_name+"'";
					String extTabDataIPXML_prime_cor=CommonMethods.apSelectWithColumnNames(Query_for_cardserno, cabinetName, sessionID);
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("extTabDataIPXML_prime_cor: "+extTabDataIPXML_prime_cor);
					String extTabDataOPXML_prime_cor = CommonMethods.WFNGExecute(extTabDataIPXML_prime_cor,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug(" extTabDataOPXML_prime_cor "+ extTabDataOPXML_prime_cor);
					XMLParser xmlParserData_prime_cor= new XMLParser(extTabDataOPXML_prime_cor);		
					int iTotalrec_prime_cor = Integer.parseInt(xmlParserData_prime_cor.getValueOf("TotalRetrieved"));
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("iTotalrec_wi_table "+iTotalrec_prime_cor);
					
					if(xmlParserData_prime_cor.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_prime_cor > 0 ){
						String xmlDataExtTab=xmlParserData_prime_cor.getNextValueOf("Record");
						xmlDataExtTab =xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
						NGXmlList objWorkList=xmlParserData_prime_cor.createList("Records", "Record");
						
						for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)){
							CheckGridDataMap_awb.put("CardSerno", objWorkList.getVal("CardSerno"));
							CheckGridDataMap_awb.put("ELITE_CRN", objWorkList.getVal("ELITE_CRN"));
						}
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug(" CardSerno "+CheckGridDataMap_awb.get("CardSerno").trim()+" for  DCC ELITE_CRN "+ CheckGridDataMap_awb.get("ELITE_CRN").trim());
					} */ 
					// No data in NG_DCC_PRIME_COURIER for ETB cases.
					
					Date d= new Date();
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String sDate = dateFormat.format(d);
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("insert_ng_digital_awb_status : sDate "+sDate);
					
					String prospect_id= CheckGridDataMap_awb.get("Prospect_id").trim();
					String Full_name=CheckGridDataMap_awb.get("FirstName").trim() +" "+ CheckGridDataMap_awb.get("MiddleName").trim() +" "+CheckGridDataMap_awb.get("LastName").trim();
				
					String ECRN="";
					if(CheckGridDataMap_awb.containsKey("ECRN")){
						ECRN = CheckGridDataMap_awb.get("ECRN").trim();
					}
					
					String AWB_Number="";
					if(CheckGridDataMap_awb.containsKey("AWB_Number")){
						AWB_Number = CheckGridDataMap_awb.get("AWB_Number").trim();
					}
					
					String CardSerno="";
					if(CheckGridDataMap_awb.containsKey("CardSerno")){
						CardSerno = CheckGridDataMap_awb.get("CardSerno").trim();
					}
					
					String CardOps_Reschedule="";
					if(CheckGridDataMap_awb.containsKey("CardOps_Reschedule")){
						CardOps_Reschedule = CheckGridDataMap_awb.get("CardOps_Reschedule").trim();
					}
					
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("insert_ng_digital_awb_status DCC : ECRN "+ECRN);
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("insert_ng_digital_awb_status DCC : prospect_id "+prospect_id);
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("insert_ng_digital_awb_status DCC  : Full_name "+Full_name);
					String columnNames_awbTable="";
					String columnValues_awbTable="";
					
					if(CardOps_Reschedule.equalsIgnoreCase("Y")){
						columnNames_awbTable="WI_name,Prospect_ID,Customer_name,mobile_No,email_id,ECRN,AWB_Number,processName,singlePager_ref_no,AWB_Gen_success_date,is_Ntb,CardSerno,card_req,EmiratesID,Status";
						columnValues_awbTable="'"+Wi_name+"','"+prospect_id+"','"+Full_name+"','"
						+CheckGridDataMap_awb.get("MobileNo").trim()
						+"','"+CheckGridDataMap_awb.get("email_id").trim()+
						"','"+ECRN.trim()+"','"+AWB_Number
						+"','"+process_name+"','"+Wi_name+"','"+sDate+"','"+CheckGridDataMap_awb.get("ntb").trim()+
						"','"+CardSerno+"'"+",'N','"+CheckGridDataMap_awb.get("EmirateID").trim()+"','R'";
					}
					else{
						columnNames_awbTable="WI_name,Prospect_ID,Customer_name,mobile_No,email_id,ECRN,AWB_Number,processName,singlePager_ref_no,AWB_Gen_success_date,is_Ntb,CardSerno,EmiratesID,Status";
						columnValues_awbTable="'"+Wi_name+"','"+prospect_id+"','"+Full_name+"','"
						+CheckGridDataMap_awb.get("MobileNo").trim()
						+"','"+CheckGridDataMap_awb.get("email_id").trim()+"','"+ECRN.trim()+"','"+
						AWB_Number+"','"+process_name+
						"','"+Wi_name+"','"+sDate+"','"+CheckGridDataMap_awb.get("ntb").trim()+"','"+CardSerno+"','"
						+CheckGridDataMap_awb.get("EmirateID").trim()+"','R'";
					}
					
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("insert_ng_digital_awb_status : columnNames_awbTable DCC "+columnNames_awbTable);
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("insert_ng_digital_awb_status : columnValues_awbTable DCC "+columnValues_awbTable);
					
					String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionID, columnNames_awbTable, columnValues_awbTable,"ng_digital_awb_status");
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("APInsertInputXML: ng_digital_awb_status DCC "+apInsertInputXML);
					
					String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("APInsertOutputXML: ng_digital_awb_status DCC "+ apInsertInputXML);
					
					XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
					String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Status of apInsertMaincode  ng_digital_awb_status DCC "+ apInsertMaincode);
					
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Completed On ng_digital_awb_status DCC "+ ActivityName);
					
					if(apInsertMaincode.equalsIgnoreCase("0"))
					{
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("ApInsert successful: ng_digital_awb_status "+apInsertMaincode);
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("ApInsert successful: ng_digital_awb_status "+apInsertMaincode);
						System.out.println("ApInsert successful: ng_digital_awb_status DCC "+Wi_name);
						CheckGridDataMap_awb.clear();
					}
					else
					{
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("ApInsert failed for ng_digital_awb_status: "+apInsertMaincode);
						System.out.println("ApInsert failed: ng_digital_awb_status DCC "+Wi_name);
					}
					
					}
					catch(Exception e)
					{
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("insert_ng_digital_awb_status : "+e.getMessage());
					}
				
			}
			
			
			
			private  String getDocName( String sWorkItemName)
			{
				String decision="";
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.info("Start of function getDocName ");
				String outputXML=null;
				String mainCode=null;
				try
				{

					sessionCheckInt=0;
					while(sessionCheckInt<loopCount)
					{
						try 
						{
							XMLParser objXMLParser = new XMLParser();
							String sqlQuery = "select Product,Preferred_Language from NG_DCC_EXTTABLE with(nolock) where Wi_Name = '"+sWorkItemName+"'";
							String InputXML = CommonMethods.apSelectWithColumnNames(sqlQuery,cabinetName, sessionID);
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.info("Getting getDocName from NG_DCC_EXTTABLE table "+InputXML);
							outputXML = WFNGExecute(InputXML, jtsIP, jtsPort, 1);
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.info("OutputXML for getting getDocName from external table "+outputXML);
							objXMLParser.setInputXML(outputXML);
							mainCode=objXMLParser.getValueOf("MainCode");
							if (mainCode.equalsIgnoreCase("0")) 
							{
								decision = CommonMethods.getTagValues(outputXML, "UW_Decision");
								if(decision==null)
									decision="";
							}
						} 
						catch (Exception e) 
						{
							sessionCheckInt++;
							waiteloopExecute(waitLoop);
							continue;
						}
						if(mainCode.equalsIgnoreCase("0")){
							break;
						}
						else if (mainCode.equalsIgnoreCase("11"))
						{
							sessionID  = CommonConnection.getSessionID(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger, false);
						}
						else
						{
							sessionCheckInt++;
							break;
						}
					}

					
				}
				catch(Exception e)
				{
					e.printStackTrace();
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Inside catch of getUWDecision function with exception.."+e);
				}
				return decision;
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
