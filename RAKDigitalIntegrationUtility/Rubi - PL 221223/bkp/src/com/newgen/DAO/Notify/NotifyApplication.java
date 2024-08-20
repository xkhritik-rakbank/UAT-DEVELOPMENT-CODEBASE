/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM
Application				: RAK BPM Utility
Module					: RAOP Status
File Name				: DAO_NOtify.java
Author 					: Ravindra Kumar	
Date (DD/MM/YYYY)		: 01/06/2022

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/


package com.newgen.DAO.Notify;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ISPack.CImageServer;
import ISPack.ISUtil.JPISException;
import Jdts.DataObject.JPDBString;
import com.itextpdf.text.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.codec.TiffImage;
import com.newgen.DAO.AWB.DAO_AWB_Log;
import com.newgen.DCC.Notify.DCCNotifyLog;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;
import com.newgen.wfdesktop.xmlapi.WFXmlResponse;


public class NotifyApplication implements Runnable
{

	private static NGEjbClient ngEjbClientCIFVer;

	static Map<String, String> NotifyAppConfigParamMap= new HashMap<String, String>();


	int socketConnectionTimeout=0;
	int integrationWaitTime=0;
	int sleepIntervalInMin=0;
	public static int sessionCheckInt=0;
	public static int waitLoop=50;
	public static int loopCount=50;
	private static String propDocsPath = null;
	public String fromMailID="";
	public String toMailID = "";
	public String mailSubject = "";
	public String MailStr="";
	public String jtsIP = "";
	public String jtsPort = "";
	public String sessionID = "";
	public String cabinetName = "";
	public String MailStrOnePagerFailure="";
	public String EmployerAddMailSubject = "";
	public String EmployerAddMailStr="";
	
	@Override
	public void run()
	{
		//String cabinetName = "";
		String queueID = "";

		try
		{
			DAONotifyAPPLog.setLogger();
			ngEjbClientCIFVer = NGEjbClient.getSharedInstance();

			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			DAONotifyAPPLog.DAONotifyAPPLogger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				DAONotifyAPPLog.DAONotifyAPPLogger.error("Could not Read Config Properties [DAONotifyAPP]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("JTSPORT: " + jtsPort);

			queueID = NotifyAppConfigParamMap.get("queueID");
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("QueueID: " + queueID);
			
			propDocsPath=NotifyAppConfigParamMap.get("DOCGENERATIONPATH");
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("propDocsPath: "+propDocsPath);
			

			socketConnectionTimeout=Integer.parseInt(NotifyAppConfigParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(NotifyAppConfigParamMap.get("INTEGRATION_WAIT_TIME"));
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(NotifyAppConfigParamMap.get("SleepIntervalInMin"));
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);

			fromMailID=NotifyAppConfigParamMap.get("fromMailID");
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("fromMailID: "+fromMailID);
			
			toMailID=NotifyAppConfigParamMap.get("toMailID");
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("toMailID: "+toMailID);
			
			mailSubject=NotifyAppConfigParamMap.get("mailSubject");
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("mailSubject: "+mailSubject);
			
			MailStr=NotifyAppConfigParamMap.get("MailStr");
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("MailStr: "+MailStr);
			
			sleepIntervalInMin=Integer.parseInt(NotifyAppConfigParamMap.get("SleepIntervalInMin"));
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);

			
			MailStrOnePagerFailure=NotifyAppConfigParamMap.get("MailStrOnePagerFailure");
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("MailStrOnePagerFailure: "+MailStrOnePagerFailure);
			
			
			EmployerAddMailSubject=NotifyAppConfigParamMap.get("EmployerAddMailSubject");
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("EmployerAddMailSubject: "+EmployerAddMailSubject);
			
			EmployerAddMailStr=NotifyAppConfigParamMap.get("EmployerAddMailStr");
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("EmployerAddMailStr: "+EmployerAddMailStr);
			
			
			sessionID = CommonConnection.getSessionID(DAONotifyAPPLog.DAONotifyAPPLogger, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				DAONotifyAPPLog.DAONotifyAPPLogger.debug("Could Not Connect to Server!");
			}
			else
			{
				DAONotifyAPPLog.DAONotifyAPPLogger.debug("Session ID found: " + sessionID);
				HashMap<String, String> socketDetailsMap = socketConnectionDetails(cabinetName, jtsIP, jtsPort, sessionID);
				while (true) {
					DAONotifyAPPLog.setLogger();
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("iRBL CIF Verification...123.");
					startDAO_NotifyAppUtility(cabinetName, jtsIP, jtsPort, sessionID, queueID, socketConnectionTimeout, integrationWaitTime, socketDetailsMap);
					System.out.println("No More workitems to Process, Sleeping!");
					Thread.sleep(sleepIntervalInMin * 60 * 1000);
				}
			}
		}

		catch(Exception e)
		{
			e.printStackTrace();
			DAONotifyAPPLog.DAONotifyAPPLogger.error("Exception Occurred in DAONotifyAPP : "+e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			DAONotifyAPPLog.DAONotifyAPPLogger.error("Exception Occurred in DAONotifyAPP : "+result);
		}
	}

	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "DAO_NotifyApp_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			  {
			    String name = (String) names.nextElement();
			    NotifyAppConfigParamMap.put(name, p.getProperty(name));
			  }
		    }
		catch (Exception e)
		{
			return -1 ;
		}
		return 0;
	}


	private void startDAO_NotifyAppUtility(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String queueID, 
	int socketConnectionTimeOut, int integrationWaitTime, HashMap<String, String> socketDetailsMap){
		
		final String ws_name="DEH_Update";
		try{
			
			final HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();
			
			sessionID  = CommonConnection.getSessionID(DAONotifyAPPLog.DAONotifyAPPLogger, false);

			if (sessionID == null || sessionID.equalsIgnoreCase("") || sessionID.equalsIgnoreCase("null"))
			{
				DAONotifyAPPLog.DAONotifyAPPLogger.error("Could Not Get Session ID "+sessionID);
				return;
			}

			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Fetching all Workitems on DAONotifyAPP queue");
			System.out.println("Fetching all Workitems on DEH_Update queue");
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionID, queueID);
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);

			DAONotifyAPPLog.DAONotifyAPPLogger.debug("WMFetchWorkList DAONotifyAPP OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);

			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Number of workitems retrieved on DAONotifyAPP: "+fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on DAONotifyAPP: "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0){
				
				for(int i=0; i<fetchWorkitemListCount; i++){
					
					String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

					DAONotifyAPPLog.DAONotifyAPPLogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID=xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("Current ProcessInstanceID: "+processInstanceID);

					DAONotifyAPPLog.DAONotifyAPPLogger.debug("Processing Workitem: "+processInstanceID);
					System.out.println("\nProcessing Workitem: "+processInstanceID);

					String WorkItemID=xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("Current WorkItemID: "+WorkItemID);

					String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("Current EntryDateTime: "+entryDateTime);

					String ActivityName=xmlParserfetchWorkItemData.getValueOf("ActivityName");
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("ActivityName: "+ActivityName);
					
					String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("ActivityID: "+ActivityID);
					String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("ActivityType: "+ActivityType);
					String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("ProcessDefId: "+ProcessDefId);
					
					String decisionValue="";
					
					//vinayak employer start to add in finacle chnage start 19-090-23					
				    
		    		String query_employerDetails ="select User_Edit_name ,deh_Event,UpdProspectReqd,Company_employer_name as EmployerName,Company_employer_name_deh as EmployerName_deh,employer_code,prospect_id,is_EmployerAdd_req from NG_DAO_EXTTABLE with(nolock) where wi_name ='" + processInstanceID + "'";
			        
		    		String deh_Event_val ="";
				    String UpdProspect_Reqd_val ="";
				    String User_Edit_name_val ="";
				    String EmployerName_val="";
				    String employer_code_val="";
				    String prospect_id_val="";
				    String integrationStatus_Employer_add="";
				    String EmployerName_deh_val="";
				    String is_EmployerAdd_req_val="";
		    		
			            String employerDetails_inputXml =CommonMethods.apSelectWithColumnNames(query_employerDetails, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAONotifyAPPLog.DAONotifyAPPLogger, false));
			    	    DAONotifyAPPLog.DAONotifyAPPLogger.debug("Remarks employerDetails_inputXml: " + employerDetails_inputXml);
			    	    String employerDetails_outputXml = WFNGExecute(employerDetails_inputXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
			    	    DAONotifyAPPLog.DAONotifyAPPLogger.debug("Remarks employerDetails_outputXml: " + employerDetails_outputXml);
			            
			    	    XMLParser employerDetails_xmlParserData = new XMLParser(employerDetails_outputXml);
			            
			    	    int employerDetails_totalRetreived = Integer.parseInt(employerDetails_xmlParserData.getValueOf("TotalRetrieved"));
			    	    
			    	    if (employerDetails_xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && employerDetails_totalRetreived > 0)
			    	    {
			    	    	String employerDetails_xmlParserData_val = employerDetails_xmlParserData.getNextValueOf("Record");
			    	    	employerDetails_xmlParserData_val = employerDetails_xmlParserData_val.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
			                // replace the spcl char above.
			                NGXmlList objWorkList4 = employerDetails_xmlParserData.createList("Records", "Record");
			               		                
			                for (; objWorkList4.hasMoreElements(true); objWorkList4.skip(true))
			                {
			                	User_Edit_name_val=objWorkList4.getVal("User_Edit_name");
			                	deh_Event_val=objWorkList4.getVal("deh_Event");
			                	UpdProspect_Reqd_val=objWorkList4.getVal("UpdProspectReqd");
			                	EmployerName_val=objWorkList4.getVal("EmployerName");
			                	employer_code_val=objWorkList4.getVal("employer_code");  	
			                	prospect_id_val= objWorkList4.getVal("prospect_id"); 
			                	EmployerName_deh_val=objWorkList4.getVal("EmployerName_deh");
			                	is_EmployerAdd_req_val=objWorkList4.getVal("is_EmployerAdd_req");
			                }
			            }
			    	    
			    	    DAONotifyAPPLog.DAONotifyAPPLogger.debug("EmployerName :" + EmployerName_val);
			    	    DAONotifyAPPLog.DAONotifyAPPLogger.debug("EmployerCode " + employer_code_val);
			    	    DAONotifyAPPLog.DAONotifyAPPLogger.debug("EmployerName_deh" + EmployerName_deh_val);
			    	    DAONotifyAPPLog.DAONotifyAPPLogger.debug("is_EmployerAdd_req_val" + is_EmployerAdd_req_val);
			    	    
			    	    
			    	    
					    DAONotifyAPPLog.DAONotifyAPPLogger.debug("Employer name to be added to finacle: " + EmployerName_val);
					    
			    		if("EMP_CREATE".equalsIgnoreCase(deh_Event_val))
			    		{
			    			 integrationStatus_Employer_add="";
			    			String ErrDesc_Employer_add="";
			    			String AddEmployerfileLocation= new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DAO_Integration")
					    		    .append(System.getProperty("file.separator")).append("Notify_application_AddEmployer.txt").toString();
					    		            
			    			BufferedReader sbfAddEmployer=new BufferedReader(new FileReader(AddEmployerfileLocation));
					    		    		
					    	StringBuilder sb1=new StringBuilder();
					    	String line1=sbfAddEmployer.readLine();
					    	while(line1!=null)
					    	{
					    		    	sb1.append(line1);
					    		    	sb1.append(System.lineSeparator());
					    		    	line1=sbfAddEmployer.readLine();
					    	}
					    	String finalString_Employer_add="";
					    	if("Y".equalsIgnoreCase(is_EmployerAdd_req_val))
					    	{
					    		 finalString_Employer_add = sb1.toString().replaceAll("#Workitem_Number#",processInstanceID)
							    		.replace("#EMP_NAME#",EmployerName_val).replace("#prospect_id#",prospect_id_val).replace("#EMP_Code#","").replace("#EMP_Code_Create_Req#","TRUE");				    		
					    		
					    	}
					    	else{
					    		 finalString_Employer_add = sb1.toString().replaceAll("#Workitem_Number#",processInstanceID)
						    		.replace("#EMP_NAME#",EmployerName_val).replace("#prospect_id#",prospect_id_val).replace("#EMP_Code#",employer_code_val).replace("#EMP_Code_Create_Req#","FALSE");
					    	}
					    		    		
//					    	String finalString_Employer_add = sb1.toString().replaceAll("#Workitem_Number#",processInstanceID)
//						    		.replace("#EMP_NAME#",EmployerName_val).replace("#prospect_id#",prospect_id_val);
					    	StringBuilder Employer_add_InputXML=new StringBuilder();
					    	Employer_add_InputXML = Employer_add_InputXML.append(finalString_Employer_add);
							HashMap<String, String> socketConnectionMap =socketConnectionDetails(cabinetName, sJtsIp, iJtsPort, sessionId); 
							
							String Employer_add_integrationStatus = socketConnection(cabinetName, CommonConnection.getUsername(), sessionId, sJtsIp, iJtsPort, processInstanceID, ws_name, 60, 65,socketConnectionMap, Employer_add_InputXML);
							
							// - xml parse for getting out the return code.
							
							XMLParser xmlParserSocketDetails_Employer_add= new XMLParser(Employer_add_integrationStatus);
							DAONotifyAPPLog.DAONotifyAPPLogger.debug(" xmlParserSocketDetails : "+xmlParserSocketDetails_Employer_add);
						    String return_code = xmlParserSocketDetails_Employer_add.getValueOf("ReturnCode");
						    DAONotifyAPPLog.DAONotifyAPPLogger.debug("Return Code: "+return_code+ "WI: "+processInstanceID);
						    String return_desc = xmlParserSocketDetails_Employer_add.getValueOf("ReturnDesc");
						   
						    
						    String MsgIdIntegration="";
						    if (Employer_add_integrationStatus.contains("<MessageId>"))
								 MsgIdIntegration = xmlParserSocketDetails_Employer_add.getValueOf("MessageId");
							
						    DAONotifyAPPLog.DAONotifyAPPLogger.debug("MsgId : "+MsgIdIntegration+" for WI: "+processInstanceID);
						    //return_code="0000";
						    if("0000".equalsIgnoreCase(return_code))
						    {
						    	 String EmployerCode_new = xmlParserSocketDetails_Employer_add.getValueOf("EmployerCode");
						    	integrationStatus_Employer_add="Success";
						    	ErrDesc_Employer_add = "Employer Notification Successful";
						    	updateExternalTable("NG_DAO_EXTTABLE","is_EmployerAdd_req","'N'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
						    	updateExternalTable("NG_DAO_EXTTABLE","employer_code","'"+EmployerCode_new+"'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
						   
						    	if("True".equalsIgnoreCase(UpdProspect_Reqd_val) && ("name".equalsIgnoreCase(User_Edit_name_val) || "both".equalsIgnoreCase(User_Edit_name_val)))
						    	{
							    	updateExternalTable("NG_DAO_EXTTABLE","deh_Event","'NAME_STATUS_CHANGE'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
							    	updateExternalTable("NG_DAO_EXTTABLE","deh_Workitem_status","'APP'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
							    }
						    	else
						    	{
						    		updateExternalTable("NG_DAO_EXTTABLE","deh_Event","'STATUS_CHANGE'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
							    	updateExternalTable("NG_DAO_EXTTABLE","deh_Workitem_status","'APP'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
							    }
						    	// entry in decision history table
						    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								
								Date current_date = new Date();
								String formattedEntryDatetime=dateFormat.format(current_date);
								DAONotifyAPPLog.DAONotifyAPPLogger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);
						
//								String entrydatetime = CheckGridDataMap.get("EntryDATETIME");
//								if(entrydatetime.equalsIgnoreCase("") || entrydatetime.equalsIgnoreCase(null)){
//									entrydatetime = entryDateTime;
//								}
						    	
						    	Date d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(entryDateTime);
								String entrydatetime_format = dateFormat.format(d1);

								String columnNames="wi_name,decision_date_time,workstep,user_name,Decision,Remarks,entry_date_time";
								String columnValues="'"+processInstanceID+"','"+formattedEntryDatetime+"','"+ActivityName+"','"
								+CommonConnection.getUsername()+"','Successs','Employer Notification Successful','"+entrydatetime_format+"'";

								String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionID, columnNames, columnValues,"NG_DAO_GR_DECISION_HISTORY");
								DAONotifyAPPLog.DAONotifyAPPLogger.debug("APInsertInputXML: "+apInsertInputXML);

								String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
								DAONotifyAPPLog.DAONotifyAPPLogger.debug("APInsertOutputXML: "+ apInsertInputXML);

								XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
								String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
								DAONotifyAPPLog.DAONotifyAPPLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

								DAONotifyAPPLog.DAONotifyAPPLogger.debug("Completed On "+ ActivityName);
								
								if(apInsertMaincode.equalsIgnoreCase("0")){
									DAONotifyAPPLog.DAONotifyAPPLogger.debug("ApInsert successful: "+apInsertMaincode);
									DAONotifyAPPLog.DAONotifyAPPLogger.debug("Inserted in WiHistory table successfully.");
								}
								else
								{
									DAONotifyAPPLog.DAONotifyAPPLogger.debug("ApInsert failed: "+apInsertMaincode);
								}
						    }
						    else
						    {
				    			String ErrDesc ="Issue In Adding New Employer To Finacle";
								decisionValue = "Failed";
								String attributesTag="<Decision>"+decisionValue+"</Decision>";
								String MsgId=MsgIdIntegration;
								sendMail(cabinetName,sessionID,processInstanceID,jtsIP,jtsPort,ErrDesc,return_code,ProcessDefId,MsgId);
								doneWiDao( processInstanceID, WorkItemID, ActivityID, ProcessDefId , ActivityType, attributesTag, sJtsIp, iJtsPort,  ErrDesc , decisionValue, MsgId, ActivityName, entryDateTime,CheckGridDataMap);
								continue;
						    }
			    		}		    		
			    		//vinayak chnages ends here to add employer to finacle
					
					 decisionValue="";					
										
				    String DBQuery ="select WI_name as 'Workitem_Number',User_Edit_name as 'User_Edit_name',PEP as 'PEP_Status',prevws as 'Prev_WS', risk_score as 'Risk_Score' ,deh_Event as 'deh_Event', deh_Workitem_status as 'deh_Workitem_status' , UpdProspectReqd as 'UpdProspectReqd', AddnalDocsReqd as 'AddnalDocsReqd', prospect_id as 'prospect_id', rejectReason as 'rejectReason' ,replace(KYC_review_Date,'/','-') as 'KYC_review_Date',Given_Name as 'FirstName',Middle_Name as 'MiddleName',Surname as 'LastName',Dedupe_Match_found,CIF,Company_employer_name as EmployerName,Company_employer_name_deh as EmployerName_deh,employer_code,nstp_employer,email_id_1  from NG_DAO_EXTTABLE with(nolock) where WI_name='" + processInstanceID + "'";
				    // select method (product written) used to get the data in form of xml.
				    String extTabDataIPXML =CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAONotifyAPPLog.DAONotifyAPPLogger, false));
				    DAONotifyAPPLog.DAONotifyAPPLogger.debug("extTabDataIPXML: " + extTabDataIPXML);
				    String extTabDataOPXML = WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
				    DAONotifyAPPLog.DAONotifyAPPLogger.debug("extTabDataOPXML: " + extTabDataOPXML);
				    // using xml parser to pass the output data in desired format 
				    XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
				    // total values retrieved > 0 is a check
				    int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
				    // Main code we get if the ap select call is triggered success.
				    if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
			        {
				    	String xmlDataExtTab = xmlParserData.getNextValueOf("Record");
			            xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
			            // replace the spcl char above.
			            NGXmlList objWorkList = xmlParserData.createList("Records", "Record");
			            
			            for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
			            {
			            	CheckGridDataMap.put("Workitem_Number", objWorkList.getVal("Workitem_Number"));
			            	CheckGridDataMap.put("PEP_Status", objWorkList.getVal("PEP_Status"));
			            	CheckGridDataMap.put("Risk_Score", objWorkList.getVal("Risk_Score"));
			            	CheckGridDataMap.put("Prev_WS", objWorkList.getVal("Prev_WS"));
			            	CheckGridDataMap.put("UpdProspect_Reqd", objWorkList.getVal("UpdProspectReqd"));
			            	CheckGridDataMap.put("AddnalDocs_Reqd", objWorkList.getVal("AddnalDocsReqd"));
			            	CheckGridDataMap.put("prospect_id", objWorkList.getVal("prospect_id")); 
			            	CheckGridDataMap.put("deh_Event", objWorkList.getVal("deh_Event")); 
			            	CheckGridDataMap.put("deh_Workitem_status", objWorkList.getVal("deh_Workitem_status"));
			            	CheckGridDataMap.put("KYC_review_Date", objWorkList.getVal("KYC_review_Date")); //kyc_review_date 06-07-2028
			            	CheckGridDataMap.put("RejectReasonCode", objWorkList.getVal("rejectReason"));
			            	CheckGridDataMap.put("FirstName", objWorkList.getVal("FirstName"));
			            	CheckGridDataMap.put("MiddleName", objWorkList.getVal("MiddleName"));
			            	CheckGridDataMap.put("LastName", objWorkList.getVal("LastName"));
			            	CheckGridDataMap.put("User_Edit_name", objWorkList.getVal("User_Edit_name"));
			            	CheckGridDataMap.put("Dedupe_Match_found", objWorkList.getVal("Dedupe_Match_found"));
			            	CheckGridDataMap.put("CIF", objWorkList.getVal("CIF"));
			            	CheckGridDataMap.put("EmployerName", objWorkList.getVal("EmployerName"));
			            	CheckGridDataMap.put("employer_code", objWorkList.getVal("employer_code"));
			            	CheckGridDataMap.put("nstp_employer", objWorkList.getVal("nstp_employer"));
			            	CheckGridDataMap.put("email_id_1", objWorkList.getVal("email_id_1"));
			            	CheckGridDataMap.put("EmployerName_deh", objWorkList.getVal("EmployerName_deh"));
			            	
			            }
			        }
				       
		    			    		
				    String additioal_docs_details = "";
				    String update_prospect="";
				    String App_Details="";
				    String wi_Status="";
				    String additioal_docs_req_status = "False";
				    String First = CheckGridDataMap.get("FirstName");
				    String Middle = CheckGridDataMap.get("MiddleName");
				    String Last = CheckGridDataMap.get("LastName");
				    String CIF = CheckGridDataMap.get("CIF");
				    String DedupeMatch = CheckGridDataMap.get("Dedupe_Match_found");
				    String CustomerEmail=CheckGridDataMap.get("email_id_1");//vinayak add send mail to customer for emploeyer add
				    String EmployerName=CheckGridDataMap.get("EmployerName");
				    String EmployerName_deh=CheckGridDataMap.get("EmployerName_deh");
				    
				    String FullName = First + " " + Last;
				    
				    if(Middle==null || Middle.equalsIgnoreCase("")){
						FullName = First + " " + Last;
					}
					else{
						FullName= First +" "+ Middle + " " + Last;
					}
				    
				    if(FullName.length()>79){
						FullName=FullName.substring(0,79);
				    }
				    else{
				    	FullName=FullName;
				    }
				    DAONotifyAPPLog.DAONotifyAPPLogger.debug("FullName: " + FullName);
				    
		            // documents for notify_app - AdditionalDocumentDetails
				    if (CheckGridDataMap.get("AddnalDocs_Reqd") != null && (CheckGridDataMap.get("AddnalDocs_Reqd").equalsIgnoreCase("true") || CheckGridDataMap.get("AddnalDocs_Reqd").equalsIgnoreCase("T"))) {
				    	// document status SHOULD BE PPENDING
			            String DBQuery_doc ="select document_name,document_status,document_remarks from NG_DAO_GR_ADDIDTIONAL_DOCUMENT with(nolock) where WI_name = '" + processInstanceID + "' AND document_status = 'Pending'";
			            String extTabDataIPXML1 =CommonMethods.apSelectWithColumnNames(DBQuery_doc, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAONotifyAPPLog.DAONotifyAPPLogger, false));
			    	    DAONotifyAPPLog.DAONotifyAPPLogger.debug("extTabDataIPXML: " + extTabDataIPXML1);
			    	    String extTabDataOPXML1 = WFNGExecute(extTabDataIPXML1, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
			    	    DAONotifyAPPLog.DAONotifyAPPLogger.debug("extTabDataOPXML: " + extTabDataOPXML1);
			            
			    	    XMLParser xmlParserData1 = new XMLParser(extTabDataOPXML1);
			            
			    	    int iTotalrec1 = Integer.parseInt(xmlParserData1.getValueOf("TotalRetrieved"));
			    	    if (xmlParserData1.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec1 > 0)
			            {
							for (int j = 0; j < iTotalrec1; j++)
							{
								String fetchlistData=xmlParserData1.getNextValueOf("Record");
								fetchlistData =fetchlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
	
								DAONotifyAPPLog.DAONotifyAPPLogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
								XMLParser xmlParserfetchData = new XMLParser(fetchlistData);
								DAONotifyAPPLog.DAONotifyAPPLogger.debug("docs remarks"+xmlParserfetchData.getValueOf("document_remarks"));
								String doc_remrks = xmlParserfetchData.getValueOf("document_remarks");
								int length_remark=doc_remrks.length();
								
								if((doc_remrks!=null || !"".equalsIgnoreCase(doc_remrks))&&(length_remark>250)){
									
									doc_remrks=doc_remrks.substring(1, 249);
									DAONotifyAPPLog.DAONotifyAPPLogger.debug("docs remarks"+doc_remrks);
								}
								
										additioal_docs_details += "\n\t\t" +"<AdditionalDocumentDetails>"+ "\n" +
										"\t\t\t" +"<DocumentName>"+xmlParserfetchData.getValueOf("document_name")+"</DocumentName>"+ "\n" +
										"\t\t\t" +"<DocumentStatus>"+xmlParserfetchData.getValueOf("document_status")+"</DocumentStatus>"+ "\n" +
										"\t\t\t" +"<DocumentRemarks>"+doc_remrks+"</DocumentRemarks>"+"\n" +
										"\t\t\t" +"<FreeField1></FreeField1>"+"\n" +
										"\t\t\t" +"<FreeField2></FreeField2>"+"\n" +
										"\t\t" +"</AdditionalDocumentDetails>\n";
							}
			            }
			    	    additioal_docs_req_status="true";
				    }
				    
				    
		            // remarks for ApplicationDetails for Additional_cust_details
		            String DBQuery_ApplicationDetails ="select remarks as 'Remarks' from NG_DAO_GR_DECISION_HISTORY with(nolock) where wi_name='" + processInstanceID + "' and workstep='Additional_cust_details'";
		            
		            String extTabDataIPXML2 =CommonMethods.apSelectWithColumnNames(DBQuery_ApplicationDetails, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAONotifyAPPLog.DAONotifyAPPLogger, false));
		    	    DAONotifyAPPLog.DAONotifyAPPLogger.debug("Remarks extTabDataIPXML: " + extTabDataIPXML2);
		    	    String extTabDataOPXML2 = WFNGExecute(extTabDataIPXML2, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
		    	    DAONotifyAPPLog.DAONotifyAPPLogger.debug("Remarks extTabDataOPXML: " + extTabDataOPXML2);
		            
		    	    XMLParser xmlParserData2 = new XMLParser(extTabDataOPXML2);
		            
		    	    int iTotalrec2 = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
		    	    
		    	    if (xmlParserData2.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec2 > 0)
		            {	  
		    	    	String xmlDataExtTab2 = xmlParserData2.getNextValueOf("Record");
		                xmlDataExtTab2 = xmlDataExtTab2.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
		                // replace the spcl char above.
		                NGXmlList objWorkList2 = xmlParserData2.createList("Records", "Record");
		               				                
		                for (; objWorkList2.hasMoreElements(true); objWorkList2.skip(true))
		                {
		                	CheckGridDataMap.put("Remarks", objWorkList2.getVal("Remarks"));
		                }
		            }
		    	    // Entry date time from WF instrument table.
		    	    String DBQuery_entrydatetime ="select EntryDATETIME from WFINSTRUMENTTABLE with(nolock) where ProcessInstanceID ='" + processInstanceID + "' and ActivityName = 'DEH_Update'";
		            
		            String extTabDataIPXML3 =CommonMethods.apSelectWithColumnNames(DBQuery_entrydatetime, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAONotifyAPPLog.DAONotifyAPPLogger, false));
		    	    DAONotifyAPPLog.DAONotifyAPPLogger.debug("Remarks extTabDataIPXML: " + extTabDataIPXML3);
		    	    String extTabDataOPXML3 = WFNGExecute(extTabDataIPXML3, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
		    	    DAONotifyAPPLog.DAONotifyAPPLogger.debug("Remarks extTabDataOPXML: " + extTabDataOPXML3);
		            
		    	    XMLParser xmlParserData3 = new XMLParser(extTabDataOPXML3);
		            
		    	    int iTotalrec3 = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
		    	    
		    	    if (xmlParserData3.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec3 > 0)
		    	    {
		    	    	String xmlDataExtTab3 = xmlParserData3.getNextValueOf("Record");
		    	    	xmlDataExtTab3 = xmlDataExtTab3.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
		                // replace the spcl char above.
		                NGXmlList objWorkList3 = xmlParserData3.createList("Records", "Record");
		               		                
		                for (; objWorkList3.hasMoreElements(true); objWorkList3.skip(true))
		                {
		                	CheckGridDataMap.put("EntryDATETIME", objWorkList3.getVal("EntryDATETIME"));
		                }
		            }
		    	    
		            //Reading a txt file from folder
		            String fileLocation= new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DAO_Integration")
		    		.append(System.getProperty("file.separator")).append("Notify_appliation.txt").toString();
		            
		            BufferedReader sbf=new BufferedReader(new FileReader(fileLocation));
		    		
		    		StringBuilder sb=new StringBuilder();
		    		String line=sbf.readLine();
		    		while(line!=null)
		    		{
		    			sb.append(line);
		    			sb.append(System.lineSeparator());
		    			line=sbf.readLine();
		    		}
		    		
		    		String Notify_app_input_xml = sb.toString().replace(">Workitem_Number<",">"+CheckGridDataMap.get("Workitem_Number").trim()+"<")
		    		.replace(">prospect_id<",">"+CheckGridDataMap.get("prospect_id").trim()+"<")
		    		.replace(">STATUS<",">"+CheckGridDataMap.get("deh_Event")+"<")
		    		.replace(">WorkItem_Status<",">"+CheckGridDataMap.get("deh_Workitem_status")+"<");
		            
		    		if (CheckGridDataMap.containsKey("Remarks") && CheckGridDataMap.get("Remarks")!=null && !CheckGridDataMap.get("Remarks").equalsIgnoreCase(""))
		    		{
		    			String remarks_grid=CheckGridDataMap.get("Remarks");
		    			if(remarks_grid.length()>250){
		    				remarks_grid=remarks_grid.substring(0, 249);
		    			}
		    			
		    			App_Details = "\n\t\t" +"<ApplicationDetails>"+ "\n" +
						"\t\t\t" +"<Remarks>"+remarks_grid+"</Remarks>"+ "\n" +
						"\t\t" +"</ApplicationDetails>\n";
		    		}
		    		
		    		Notify_app_input_xml = Notify_app_input_xml.replace("App_Details",App_Details);
		    		DAONotifyAPPLog.DAONotifyAPPLogger.debug("App_Details : App_Details "+Notify_app_input_xml);
		    		
		    		// Sarthak start
					if (CheckGridDataMap.get("deh_Workitem_status").equalsIgnoreCase("REJ") || CheckGridDataMap.get("deh_Workitem_status").equalsIgnoreCase("CLO")) {
						wi_Status = "\n\t\t" + "<RejectReason>"+CheckGridDataMap.get("RejectReasonCode")+"</RejectReason>";
						Notify_app_input_xml = Notify_app_input_xml.replace("Str_RejectReasonCode", wi_Status);
					} else {
						Notify_app_input_xml = Notify_app_input_xml.replace("Str_RejectReasonCode", "");
					}
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("Str_RejectReasonCode : " + wi_Status);
		    		// Sarthak end
		    		
		    		if(CheckGridDataMap.get("AddnalDocs_Reqd").trim().equalsIgnoreCase("true"))
		    		{
		    			// Documents section (AdditionalDocumentDetails)
		    			Notify_app_input_xml=Notify_app_input_xml.replace(">AddnalDocs_Reqd<",">true<");
		    		}else 
		    		{
		    			Notify_app_input_xml=Notify_app_input_xml.replace(">AddnalDocs_Reqd<",">false<");
		    		}
		    		//POA-2951 - Hritik 26.4.23 - In case of APP, Dedupe 
		    		String Cif_upd = "";
		    		if(DedupeMatch.equalsIgnoreCase("Y") && CheckGridDataMap.get("deh_Workitem_status").equalsIgnoreCase("APP")){
		    			Cif_upd = "true";
		    		}else{
		    			Cif_upd="false";	
		    		}
		    		
		    		Notify_app_input_xml=Notify_app_input_xml.replace(">CIF_UPD<",">"+Cif_upd+"<");
		    		
		    		String CifDedupResults = "";
		    		if(DedupeMatch.equalsIgnoreCase("Y") && CheckGridDataMap.get("deh_Workitem_status").equalsIgnoreCase("APP")){
			    		if(CIF.equalsIgnoreCase("NTB"))
			    		{
			    				CifDedupResults = 
			    				"\n\t\t" +"<CifDedupResults>"+ "\n" +
								"\t\t\t" +"<IsCustomerNTB>Y</IsCustomerNTB>"+"\n" +
								"\t\t\t" +"<CifId></CifId>"+"\n" +
								"\t\t" +"</CifDedupResults>\n";
			    		}else{
			    			CifDedupResults = 
			    				"\n\t\t" +"<CifDedupResults>"+ "\n" +
								"\t\t\t" +"<IsCustomerNTB>N</IsCustomerNTB>"+"\n" +
								"\t\t\t" +"<CifId>"+CIF+"</CifId>"+"\n" +
								"\t\t" +"</CifDedupResults>\n";
			    		}
		    		}
		    				    		
		    		if(CheckGridDataMap.get("UpdProspect_Reqd").trim().equalsIgnoreCase("true") && "APP".equalsIgnoreCase(CheckGridDataMap.get("deh_Workitem_status")) )
		    		{
		    			if(CheckGridDataMap.get("User_Edit_name").equalsIgnoreCase("name")){
		    				update_prospect = "\n\t\t" +"<UpdateProspectDtlsFromBPM>"+ "\n" +
							"\t\t\t" +"<UpdatedFirstName>"+First+"</UpdatedFirstName>"+"\n" +
							"\t\t\t" +"<UpdatedMiddleName>"+Middle+"</UpdatedMiddleName>"+"\n" +
							"\t\t\t" +"<UpdatedLastName>"+Last+"</UpdatedLastName>"+"\n" +
							"\t\t\t" +"<UpdatedFullName>"+FullName+"</UpdatedFullName>"+"\n" +
		    				"\t\t\t" +"<UpdateEmploymentDtls>"+"\n" +
		    				"\t\t\t" +"<EmployerCode>"+CheckGridDataMap.get("employer_code")+"</EmployerCode>"+"\n" +
		    				"\t\t\t" +"<EmployerName>"+CheckGridDataMap.get("EmployerName")+"</EmployerName>"+"\n" +
		    				"\t\t\t" +"<IsNewEmpCodeCreationReqd>FALSE</IsNewEmpCodeCreationReqd>"+"\n" +
		    				"\t\t\t" +"</UpdateEmploymentDtls>"+"\n" +
							"\t\t" +"</UpdateProspectDtlsFromBPM>\n";
		    			}
		    			else if(CheckGridDataMap.get("User_Edit_name").equalsIgnoreCase("both"))
		    			{
		    				update_prospect = "\n\t\t" +"<UpdateProspectDtlsFromBPM>"+ "\n" +
		    				"\t\t\t" +"<PEPStatus>"+CheckGridDataMap.get("PEP_Status")+"</PEPStatus>"+ "\n" +
		    				"\t\t\t" +"<RiskScore>"+CheckGridDataMap.get("Risk_Score")+"</RiskScore>"+ "\n" +
		    				"\t\t\t" +"<KYCReviewDate>"+CheckGridDataMap.get("KYC_review_Date")+"</KYCReviewDate>"+"\n" +
		    				"\t\t\t" +"<UpdatedFirstName>"+First+"</UpdatedFirstName>"+"\n" +
		    				"\t\t\t" +"<UpdatedMiddleName>"+Middle+"</UpdatedMiddleName>"+"\n" +
		    				"\t\t\t" +"<UpdatedLastName>"+Last+"</UpdatedLastName>"+"\n" +
		    				"\t\t\t" +"<UpdatedFullName>"+FullName+"</UpdatedFullName>"+"\n" +
		    				"\t\t\t" +"<UpdateEmploymentDtls>"+"\n" +
		    				"\t\t\t" +"<EmployerCode>"+CheckGridDataMap.get("employer_code")+"</EmployerCode>"+"\n" +
		    				"\t\t\t" +"<EmployerName>"+CheckGridDataMap.get("EmployerName")+"</EmployerName>"+"\n" +
		    				"\t\t\t" +"<IsNewEmpCodeCreationReqd>FALSE</IsNewEmpCodeCreationReqd>"+"\n" +
		    				"\t\t\t" +"</UpdateEmploymentDtls>"+"\n" +
		    				"\t\t" +"</UpdateProspectDtlsFromBPM>\n";
		    			}
		    			else if(CheckGridDataMap.get("User_Edit_name").equalsIgnoreCase("none"))
		    			{
		    				update_prospect = "\n\t\t" +"<UpdateProspectDtlsFromBPM>"+ "\n" +
							"\t\t\t" +"<PEPStatus>"+CheckGridDataMap.get("PEP_Status")+"</PEPStatus>"+ "\n" +
							"\t\t\t" +"<RiskScore>"+CheckGridDataMap.get("Risk_Score")+"</RiskScore>"+ "\n" +
							"\t\t\t" +"<KYCReviewDate>"+CheckGridDataMap.get("KYC_review_Date")+"</KYCReviewDate>"+"\n" +
							"\t\t\t" +"<UpdateEmploymentDtls>"+"\n" +
							"\t\t\t" +"<EmployerCode>"+CheckGridDataMap.get("employer_code")+"</EmployerCode>"+"\n" +
		    				"\t\t\t" +"<EmployerName>"+CheckGridDataMap.get("EmployerName")+"</EmployerName>"+"\n" +
		    				"\t\t\t" +"<IsNewEmpCodeCreationReqd>FALSE</IsNewEmpCodeCreationReqd>"+"\n" +
		    				"\t\t\t" +"</UpdateEmploymentDtls>"+"\n" +
							"\t\t" +"</UpdateProspectDtlsFromBPM>\n";
		    			}
		    		
		    			Notify_app_input_xml=Notify_app_input_xml.replace("UpdProspect_Reqd","true");
		    		}
		    		
		    		/*below else if condition added by vinayak to send employer code,name in case nstp_employer flag is Y i.e. is the user 
		    		modify the employer and we have to send the update employer name and code in the call
		    		*/
		    		else if(!EmployerName_deh.equalsIgnoreCase(EmployerName))
		    		{		    			
		    			update_prospect = "\n\t\t" +"<UpdateProspectDtlsFromBPM>"+ "\n" +								
								"\t\t\t" +"<UpdateEmploymentDtls>"+"\n" +
								"\t\t\t" +"<EmployerCode>"+CheckGridDataMap.get("employer_code")+"</EmployerCode>"+"\n" +
			    				"\t\t\t" +"<EmployerName>"+CheckGridDataMap.get("EmployerName")+"</EmployerName>"+"\n" +
			    				"\t\t\t" +"<IsNewEmpCodeCreationReqd>FALSE</IsNewEmpCodeCreationReqd>"+"\n" +
			    				"\t\t\t" +"</UpdateEmploymentDtls>"+"\n" +
								"\t\t" +"</UpdateProspectDtlsFromBPM>\n";
		    			Notify_app_input_xml=Notify_app_input_xml.replace("UpdProspect_Reqd","true");
		    		}
		    		
		    		else
		    		{
		    			Notify_app_input_xml=Notify_app_input_xml.replace("UpdProspect_Reqd","false");
		    		}
		    		Notify_app_input_xml=Notify_app_input_xml.replace("update_prospect",update_prospect);
		    		DAONotifyAPPLog.DAONotifyAPPLogger.debug("UpdProspect_Reqd : UpdProspect_Reqd "+Notify_app_input_xml);
		    		
	    			Notify_app_input_xml=Notify_app_input_xml.replace("Additional_Document_Details",additioal_docs_details);
		    		
	    			Notify_app_input_xml=Notify_app_input_xml.replace(">AddnalDocs_Reqd<",">"+additioal_docs_req_status+"<");
	    			
	    			Notify_app_input_xml=Notify_app_input_xml.replace("CifDedupResults",CifDedupResults);
		    		
		    		DAONotifyAPPLog.DAONotifyAPPLogger.debug("Notify_appliation: " + Notify_app_input_xml);
		    		//Deepak End 
		    		
					//vinayak  if one pager signed chnages
		    		
		    		String deh_Event = CheckGridDataMap.get("deh_Event");
		    		DAONotifyAPPLog.DAONotifyAPPLogger.debug("Notify_appliation deh_Event: " + deh_Event);		    		
		    		if("One_pager_signed".equalsIgnoreCase(deh_Event))
		    		{
		    			
		    			String return_value=downloadAttachDocuments(processInstanceID);
						DAONotifyAPPLog.DAONotifyAPPLogger.debug("return_value: "+return_value);
						
						if("Done".equalsIgnoreCase(return_value)){
			    			updateExternalTable("NG_DAO_EXTTABLE","is_OnePager_mailed","'Y'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
							
							
			    			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Notify_appliation inside one pager signed: ");
			    			String OnePagerfileLocation= new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DAO_Integration")
			    		    .append(System.getProperty("file.separator")).append("Notify_appliation_OnePager.txt").toString();
			    		            
			    		    BufferedReader sbfOnePager=new BufferedReader(new FileReader(OnePagerfileLocation));
			    		    		
			    			StringBuilder sb1=new StringBuilder();
			    		    String line1=sbfOnePager.readLine();
			    		    while(line1!=null)
			    		    {
			    		    	sb1.append(line1);
			    		    	sb1.append(System.lineSeparator());
			    		    	line1=sbfOnePager.readLine();
			    		    }
			    		    		
			    		    Notify_app_input_xml = sb1.toString().replace(">Workitem_Number<",">"+CheckGridDataMap.get("Workitem_Number").trim()+"<")
			    			.replace(">prospect_id<",">"+CheckGridDataMap.get("prospect_id").trim()+"<");
			    		    DAONotifyAPPLog.DAONotifyAPPLogger.debug("Notify_appliation: " + Notify_app_input_xml);
						}
		    		    else
		    		    {
							updateExternalTable("NG_DAO_EXTTABLE","is_OnePager_mailed","'N'","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
							
							String MailSubject=mailSubject;
							String FinalMailStr=MailStrOnePagerFailure;
							SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
							String insertedDateTime= simpleDateFormat.format(new Date());
							
							FinalMailStr=FinalMailStr.replace("<WI_NAME>",processInstanceID);
							String columnName1="MAILFROM,MAILTO,MAILSUBJECT,MAILMESSAGE,MAILCONTENTTYPE,MAILPRIORITY,MAILSTATUS,INSERTEDBY,MAILACTIONTYPE,INSERTEDTIME,PROCESSDEFID,PROCESSINSTANCEID,WORKITEMID,ACTIVITYID,NOOFTRIALS";
							String strValues1="'"+fromMailID+"','"+toMailID+"','"+MailSubject+"','"+FinalMailStr+"','text/html;charset=UTF-8','1','N','CUSTOM','TRIGGER','"+insertedDateTime+"','"+ProcessDefId+"','"+processInstanceID+"','1','"+ActivityID+"','0'";
							
							String apInsertInputXML1=CommonMethods.apInsert(cabinetName, sessionID, columnName1, strValues1,"WFMAILQUEUETABLE");
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("APInsertInputXML: "+apInsertInputXML1);

							String apInsertOutputXML1 = CommonMethods.WFNGExecute(apInsertInputXML1,jtsIP ,jtsPort,1);
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("APInsertOutputXML: "+ apInsertOutputXML1);

							XMLParser xmlParserAPInsert1 = new XMLParser(apInsertOutputXML1);
							String apInsertMaincode1 = xmlParserAPInsert1.getValueOf("MainCode");
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode1);

							if(apInsertMaincode1.equalsIgnoreCase("0"))
							{
								DAONotifyAPPLog.DAONotifyAPPLogger.debug("ApInsert successful: "+apInsertMaincode1);
								DAONotifyAPPLog.DAONotifyAPPLogger.debug("Inserted in Wimailquque  table successfully.");
							}
							
							else
							{
								DAONotifyAPPLog.DAONotifyAPPLogger.debug("ApInsert failed: "+apInsertMaincode1);
							}
							
							String ErrDesc ="Issue In Downloading OnePager";
							decisionValue = "Failed";
							String attributesTag="<Decision>"+decisionValue+"</Decision>";
							String MsgId="";
							doneWiDao( processInstanceID, WorkItemID, ActivityID, ProcessDefId , ActivityType, attributesTag, sJtsIp, iJtsPort,  ErrDesc , decisionValue, MsgId, ActivityName, entryDateTime,CheckGridDataMap);
							continue;
						}
		    		}
		    		
		    		
		    		String integrationStatus="Success";
					String attributesTag;
					String ErrDesc = "";
					StringBuilder finalString=new StringBuilder();
					finalString = finalString.append(Notify_app_input_xml);
					//changes need to done to updae the correct flag
					HashMap<String, String> socketConnectionMap =socketConnectionDetails(cabinetName, sJtsIp, iJtsPort, sessionID); 
					
					integrationStatus = socketConnection(cabinetName, CommonConnection.getUsername(), sessionId, sJtsIp, iJtsPort, processInstanceID, ws_name, 60, 65,socketConnectionMap, finalString);
					
					// - xml parse for getting out the return code.
					
					XMLParser xmlParserSocketDetails= new XMLParser(integrationStatus);
					DAONotifyAPPLog.DAONotifyAPPLogger.debug(" xmlParserSocketDetails : "+xmlParserSocketDetails);
				    String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
				    DAONotifyAPPLog.DAONotifyAPPLogger.debug("Return Code: "+return_code+ "WI: "+processInstanceID);
				    String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
				    DAONotifyAPPLog.DAONotifyAPPLogger.debug("return_desc : "+return_desc+ "WI: "+processInstanceID);
					
				    String MsgId ="";
				    if (integrationStatus.contains("<MessageId>"))
						MsgId = xmlParserSocketDetails.getValueOf("MessageId");
					
				    DAONotifyAPPLog.DAONotifyAPPLogger.debug("MsgId : "+MsgId+" for WI: "+processInstanceID);
					
				    if("0000".equalsIgnoreCase(return_code))
				    {
				    	integrationStatus="Success";
				    	ErrDesc = "Notify Done Successfully";
				    }
				    if("112614".equalsIgnoreCase(return_code))
				    {
				    	integrationStatus="CIF Verification";
				    	ErrDesc = "Notify Unsuccessfully";
				    	updateExternalTable("NG_DAO_EXTTABLE","CIF_verification_flag","Y","WI_name='"+processInstanceID+"'", sJtsIp, iJtsPort, cabinetName);
				    }
					if ("Success".equalsIgnoreCase(integrationStatus))
					{
						decisionValue = "Success";
						if(CheckGridDataMap.get("deh_Workitem_status").equalsIgnoreCase("COM")){
							decisionValue="Final Success";
						}
						else if(CheckGridDataMap.get("deh_Workitem_status").equalsIgnoreCase("REJ")){
							decisionValue="Reject";
						}
						else if(CheckGridDataMap.get("deh_Workitem_status").equalsIgnoreCase("CLO")){
							decisionValue="Account Closed";
						}
						else if(CheckGridDataMap.get("deh_Event").equalsIgnoreCase("One_pager_signed")){
							decisionValue="Approved And Document Generated";
						}
						DAONotifyAPPLog.DAONotifyAPPLogger.debug("Decision in success: " +decisionValue);
						attributesTag="<Decision>"+decisionValue+"</Decision>";
					}
					else if("CIF Verification".equalsIgnoreCase(integrationStatus))
					{
						decisionValue = "CIF Verification";
						attributesTag="<Decision>"+decisionValue+"</Decision>";
						ErrDesc = return_desc; 
					}
					else
					{
						ErrDesc = return_desc; //integrationStatus.replace("~", ",").replace("|", "\n");
						decisionValue = "Failed";
						DAONotifyAPPLog.DAONotifyAPPLogger.debug("Decision in else : " +decisionValue);
						attributesTag="<Decision>"+decisionValue+"</Decision>";
						sendMail(cabinetName,sessionID,processInstanceID,jtsIP,jtsPort,ErrDesc,return_code,ProcessDefId,MsgId);
					}
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("decisionValue: " +decisionValue);
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("ErrDesc: " +ErrDesc);
					
					String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionID, processInstanceID,WorkItemID);
					String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML,sJtsIp,iJtsPort,1);
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("Output XML For WmgetWorkItemCall: " + getWorkItemOutputXml);

					XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
					String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);

					if (getWorkItemMainCode.trim().equals("0"))
					{
						DAONotifyAPPLog.DAONotifyAPPLogger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);

						//String assignWorkitemAttributeInputXML=CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionId,processInstanceID,WorkItemID,attributesTag);
						
						String assignWorkitemAttributeInputXML = "<?xml version=\"1.0\"?><WMAssignWorkItemAttributes_Input>"
								+ "<Option>WMAssignWorkItemAttributes</Option>"
								+ "<EngineName>"+cabinetName+"</EngineName>"
								+ "<SessionId>"+sessionID+"</SessionId>"
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
						
						DAONotifyAPPLog.DAONotifyAPPLogger.debug("InputXML for assignWorkitemAttribute Call Notify: "+assignWorkitemAttributeInputXML);

						String assignWorkitemAttributeOutputXML=WFNGExecute(assignWorkitemAttributeInputXML,sJtsIp,
								iJtsPort,1);
						
						DAONotifyAPPLog.DAONotifyAPPLogger.debug("OutputXML for assignWorkitemAttribute Call Notify: "+assignWorkitemAttributeOutputXML);
						
						XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
						String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
						DAONotifyAPPLog.DAONotifyAPPLogger.debug("AssignWorkitemAttribute MainCode: "+assignWorkitemAttributeMainCode);

						if(assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0")){
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("AssignWorkitemAttribute Successful: "+assignWorkitemAttributeMainCode);
							System.out.println(processInstanceID + "Complete Succesfully with status "+decisionValue);
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("WorkItem moved to next Workstep.");
							
							
							// vinayak changes start to send mail to customer that employer has been chnaged							
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("EmployerName_deh: " + EmployerName_deh);
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("EmployerName: " + EmployerName);
							if(!EmployerName_deh.equalsIgnoreCase(EmployerName))
							{
							sendMailToCustomer(processInstanceID,FullName,CustomerEmail,ProcessDefId,ActivityID);
							}
							//vinayak changes ends to send mail to customer that employer has been chnaged
						}
						else{
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("decisionValue : "+decisionValue);
							ErrDesc="Done WI Failed";
							sendMail(cabinetName,sessionID,processInstanceID,jtsIP,jtsPort,ErrDesc,assignWorkitemAttributeMainCode,ProcessDefId,MsgId);
						}
						
						DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						
						Date current_date = new Date();
						String formattedEntryDatetime=dateFormat.format(current_date);
						DAONotifyAPPLog.DAONotifyAPPLogger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);
				
						String entrydatetime = CheckGridDataMap.get("EntryDATETIME");
						if(entrydatetime.equalsIgnoreCase("") || entrydatetime==null){
							entrydatetime = entryDateTime;
						}
						Date d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(entrydatetime);
						String entrydatetime_format = dateFormat.format(d1);

						String columnNames="wi_name,decision_date_time,workstep,user_name,Decision,Remarks,entry_date_time";
						String columnValues="'"+processInstanceID+"','"+formattedEntryDatetime+"','"+ActivityName+"','"
						+CommonConnection.getUsername()+"','"+decisionValue+"','"+ErrDesc+"','"+entrydatetime_format+"'";

						String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionID, columnNames, columnValues,"NG_DAO_GR_DECISION_HISTORY");
						DAONotifyAPPLog.DAONotifyAPPLogger.debug("APInsertInputXML: "+apInsertInputXML);

						String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
						DAONotifyAPPLog.DAONotifyAPPLogger.debug("APInsertOutputXML: "+ apInsertInputXML);

						XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
						String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
						DAONotifyAPPLog.DAONotifyAPPLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

						DAONotifyAPPLog.DAONotifyAPPLogger.debug("Completed On "+ ActivityName);
						
						if(apInsertMaincode.equalsIgnoreCase("0")){
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("ApInsert successful: "+apInsertMaincode);
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("Inserted in WiHistory table successfully.");
						}
						else
						{
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("ApInsert failed: "+apInsertMaincode);
						}
					}
					else
					{
						getWorkItemMainCode="";
						DAONotifyAPPLog.DAONotifyAPPLogger.debug("WmgetWorkItem failed: "+getWorkItemMainCode);
						ErrDesc="WI Failed";
						sendMail(cabinetName,sessionID,processInstanceID,jtsIP,jtsPort,ErrDesc,getWorkItemMainCode,ProcessDefId,MsgId);
					}
				}
			}
		}
		catch (Exception e){
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Exception: "+e.getMessage());
		}
	}
	
	public  void sendMail(String cabinetName, String sessionId ,String wiName,String jtsIp,String jtsPort,String ErrDesc, String return_code,String ProcessDefId,String MsgId)throws Exception
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
            	DAONotifyAPPLog.DAONotifyAPPLogger.debug("workitem name to send mail---"+wiName);
            	DAONotifyAPPLog.DAONotifyAPPLogger.debug("ErrorMsg to send mail---"+ErrDesc);
            	DAONotifyAPPLog.DAONotifyAPPLogger.debug("return_code to send mail---"+return_code);
            	
            	String FinalMailStr = MailStr.toString().replace("<WI_NAME>",wiName).replace("<ret_Code>",return_code)
            	.replace("<errormsg>",ErrDesc).replace("<MsgID>",MsgId);
            	DAONotifyAPPLog.DAONotifyAPPLogger.debug("finalbody: "+FinalMailStr);

            	String columnName="MAILFROM,MAILTO,MAILSUBJECT,MAILMESSAGE,MAILCONTENTTYPE,MAILPRIORITY,MAILSTATUS,INSERTEDBY,MAILACTIONTYPE,INSERTEDTIME,PROCESSDEFID,PROCESSINSTANCEID,WORKITEMID,ACTIVITYID,NOOFTRIALS";
            	String strValues="'"+fromMailID+"','"+toMailID+"','"+mailSubject+"','"+FinalMailStr+"','text/html;charset=UTF-8','1','N','CUSTOM','TRIGGER','"+CommonMethods.getdateCurrentDateInSQLFormat()+"','"+ProcessDefId+"','"+wiName+"','1','1','0'";
                
				sInputXML = "<?xml version=\"1.0\"?>" +
                        "<APInsert_Input>" +
                        "<Option>APInsert</Option>" +
                        "<TableName>WFMAILQUEUETABLE</TableName>" +
                        "<ColName>" + columnName + "</ColName>" +
                        "<Values>" + strValues + "</Values>" +
                        "<EngineName>" + cabinetName + "</EngineName>" +
                        "<SessionId>" + sessionID + "</SessionId>" +
                        "</APInsert_Input>";
                DAONotifyAPPLog.DAONotifyAPPLogger.debug("Mail Insert InputXml::::::::::\n"+sInputXML);
                sOutputXML =WFNGExecute(sInputXML, jtsIp,jtsPort,0);
                DAONotifyAPPLog.DAONotifyAPPLogger.debug("Mail Insert OutputXml::::::::::\n"+sOutputXML);
                objXMLParser.setInputXML(sOutputXML);
                mainCodeforAPInsert=objXMLParser.getValueOf("MainCode");
                
            }
			
			catch(Exception e)
            {
                e.printStackTrace();
                DAONotifyAPPLog.DAONotifyAPPLogger.error("Exception in Sending mail", e);
                sessionCheckInt++;
                waiteloopExecute(waitLoop);
                continue;
            }
            if (mainCodeforAPInsert.equalsIgnoreCase("11")) 
            {
                DAONotifyAPPLog.DAONotifyAPPLogger.debug("Invalid session in Sending mail");
                sessionCheckInt++;
                //ThreadConnect.sessionId = ThreadConnect.getSessionID(cabinetName, jtsIP, jtsPort, userName,password);
                sessionID=CommonConnection.getSessionID(DAONotifyAPPLog.DAONotifyAPPLogger, true);
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
            DAONotifyAPPLog.DAONotifyAPPLogger.debug("mail Insert Successful");
            System.out.println("Mail Insert Successful for "+wiName+" in table WFMAILQUEUETABLE");
        }
        else
        {
            DAONotifyAPPLog.DAONotifyAPPLogger.debug("mail Insert Unsuccessful");
            System.out.println("Mail Insert Unsuccessful for "+wiName+"in table WFMAILQUEUETABLE");
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

	private HashMap<String, String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort, String sessionID) {
		HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

		try {
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Fetching Socket Connection Details.");
			System.out.println("Fetching Socket Connection Details.");

			String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'DigitalAO' and CallingSource = 'Utility'";

			String socketDetailsInputXML = CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Socket Details APSelect InputXML: " + socketDetailsInputXML);

			String socketDetailsOutputXML = WFNGExecute(socketDetailsInputXML, sJtsIp, iJtsPort, 1);
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Socket Details APSelect OutputXML: " + socketDetailsOutputXML);

			XMLParser xmlParserSocketDetails = new XMLParser(socketDetailsOutputXML);
			String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("SocketDetailsMainCode: " + socketDetailsMainCode);

			int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("SocketDetailsTotalRecords: " + socketDetailsTotalRecords);

			if (socketDetailsMainCode.equalsIgnoreCase("0") && socketDetailsTotalRecords > 0) {
				String xmlDataSocketDetails = xmlParserSocketDetails.getNextValueOf("Record");
				xmlDataSocketDetails = xmlDataSocketDetails.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

				XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

				String socketServerIP = xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
				DAONotifyAPPLog.DAONotifyAPPLogger.debug("SocketServerIP: " + socketServerIP);
				socketDetailsMap.put("SocketServerIP", socketServerIP);

				String socketServerPort = xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
				DAONotifyAPPLog.DAONotifyAPPLogger.debug("SocketServerPort " + socketServerPort);
				socketDetailsMap.put("SocketServerPort", socketServerPort);

				DAONotifyAPPLog.DAONotifyAPPLogger.debug("SocketServer Details found.");
				System.out.println("SocketServer Details found.");

			}
		} catch (Exception e) {
			DAONotifyAPPLog.DAONotifyAPPLogger
					.debug("Exception in getting Socket Connection Details: " + e.getMessage());
			System.out.println("Exception in getting Socket Connection Details: " + e.getMessage());
		}

		return socketDetailsMap;
	}

	protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort, int flag)
			throws IOException, Exception {
		DAONotifyAPPLog.DAONotifyAPPLogger.debug("In WF NG Execute : " + serverPort);
		try {
			if (serverPort.startsWith("33"))
				return WFCallBroker.execute(ipXML, jtsServerIP, Integer.parseInt(serverPort), 1);
			else
				return ngEjbClientCIFVer.makeCall(jtsServerIP, serverPort, "WebSphere", ipXML);
		} catch (Exception e) {
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Exception Occured in WF NG Execute : " + e.getMessage());
			e.printStackTrace();
			return "Error";
		}
	}
	
	String socketConnection(String cabinetName, String username, String sessionId, String sJtsIp, String iJtsPort, String processInstanceID, String ws_name,
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

			DAONotifyAPPLog.DAONotifyAPPLogger.debug("userName "+ username);
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("SessionId "+ sessionID);

			socketServerIP=socketDetailsMap.get("SocketServerIP");
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("SocketServerIP "+ socketServerIP);
			socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("SocketServerPort "+ socketServerPort);

	   		if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0))
	   		{

    			socket = new Socket(socketServerIP, socketServerPort);
    			socket.setSoTimeout(connection_timeout*1000);
    			out = socket.getOutputStream();
    			socketInputStream = socket.getInputStream();
    			dout = new DataOutputStream(out);
    			din = new DataInputStream(socketInputStream);
    			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Dout " + dout);
    			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Din " + din);

    			outputResponse = "";

    			inputRequest = getRequestXML( cabinetName,sessionID ,processInstanceID, ws_name, username, sInputXML);


    			if (inputRequest != null && inputRequest.length() > 0)
    			{
    				int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
    				DAONotifyAPPLog.DAONotifyAPPLogger.debug("RequestLen: "+inputRequestLen + "");
    				inputRequest = inputRequestLen + "##8##;" + inputRequest;
    				DAONotifyAPPLog.DAONotifyAPPLogger.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
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
    				DAONotifyAPPLog.DAONotifyAPPLogger.debug("OutputResponse: "+outputResponse);

    				if(!"".equalsIgnoreCase(outputResponse))
    					outputResponse = getResponseXML(cabinetName,sJtsIp,iJtsPort,sessionID, processInstanceID,outputResponse,integrationWaitTime );

    				if(outputResponse.contains("&lt;"))
    				{
    					outputResponse=outputResponse.replaceAll("&lt;", "<");
    					outputResponse=outputResponse.replaceAll("&gt;", ">");
    				}
    			}
    			socket.close();

				outputResponse = outputResponse.replaceAll("</MessageId>","</MessageId>/n<InputMessageId>"+inputMessageID+"</InputMessageId>");

				//DAONotifyAPPLog.DAONotifyAPPLogger.debug("outputResponse "+outputResponse);
				return outputResponse;

    	 		}

    		else
    		{
    			DAONotifyAPPLog.DAONotifyAPPLogger.debug("SocketServerIp and SocketServerPort is not maintained "+"");
    			DAONotifyAPPLog.DAONotifyAPPLogger.debug("SocketServerIp is not maintained "+	socketServerIP);
    			DAONotifyAPPLog.DAONotifyAPPLogger.debug(" SocketServerPort is not maintained "+	socketServerPort);
    			return "Socket Details not maintained";
    		}

		}

		catch (Exception e)
		{
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
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
				DAONotifyAPPLog.DAONotifyAPPLogger.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
				//printException(e);
			}
		}


	}
	private void updateExternalTable(String tablename, String columnname,String sMessage, String sWhere, String jtsIP, String jtsPort, String cabinetName)
	{
		int sessionCheckInt=0;
		int loopCount=50;
		int mainCode = 0;
		
		DAONotifyAPPLog.DAONotifyAPPLogger.debug("Inside update EXT table: ");
		
		while(sessionCheckInt<loopCount)
		{
			try
			{
				XMLParser objXMLParser = new XMLParser();
				String inputXmlcheckAPUpdate = CommonMethods.getAPUpdateIpXML(tablename,columnname,sMessage,sWhere,cabinetName,sessionID);
				DAONotifyAPPLog.DAONotifyAPPLogger.debug(("inputXmlcheckAPUpdate : " + inputXmlcheckAPUpdate));
				String outXmlCheckAPUpdate=null;
				outXmlCheckAPUpdate=WFNGExecute(inputXmlcheckAPUpdate,jtsIP,jtsPort,1);
				DAONotifyAPPLog.DAONotifyAPPLogger.debug(("outXmlCheckAPUpdate : " + outXmlCheckAPUpdate));
				objXMLParser.setInputXML(outXmlCheckAPUpdate);
				String mainCodeforCheckUpdate = null;
				mainCodeforCheckUpdate=objXMLParser.getValueOf("MainCode");
				if (!mainCodeforCheckUpdate.equalsIgnoreCase("0"))
				{
					DAONotifyAPPLog.DAONotifyAPPLogger.debug(("Exception in ExecuteQuery_APUpdate updating "+tablename+" table"));
					System.out.println("Exception in ExecuteQuery_APUpdate updating "+tablename+" table");
				}
				else
				{
					DAONotifyAPPLog.DAONotifyAPPLogger.debug(("Succesfully updated "+tablename+" table"));
					System.out.println("Succesfully updated "+tablename+" table");
					//ThreadConnect.addToTextArea("Successfully updated transaction table");
				}
				mainCode=Integer.parseInt(mainCodeforCheckUpdate);
				if (mainCode == 11)
				{
					sessionID  = CommonConnection.getSessionID(DAONotifyAPPLog.DAONotifyAPPLogger, false);
				}
				else
				{
					sessionCheckInt++;
					break;
				}

				if (outXmlCheckAPUpdate.equalsIgnoreCase("") || outXmlCheckAPUpdate == "" || outXmlCheckAPUpdate == null)
					break;

			}
			catch(Exception e)
			{
				DAONotifyAPPLog.DAONotifyAPPLogger.debug(("Inside create validateSessionID exception"+e.getMessage()));
			}
		}
	}

	
	
	private String getResponseXML(String cabinetName,String sJtsIp,String iJtsPort, String sessionId, String processInstanceID,String message_ID, int integrationWaitTime)
	{

		String outputResponseXML="";
		try
		{
			String QueryString = "select OUTPUT_XML from NG_DAO_XMLLOG_HISTORY with (nolock) where MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+processInstanceID+"'";

			String responseInputXML =CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, sessionID);
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Response APSelect InputXML: "+responseInputXML);

			int Loop_count=0;
			do
			{
				String responseOutputXML=CommonMethods.WFNGExecute(responseInputXML,sJtsIp,iJtsPort,1);
				DAONotifyAPPLog.DAONotifyAPPLogger.debug("Response APSelect OutputXML: "+responseOutputXML);

			    XMLParser xmlParserSocketDetails= new XMLParser(responseOutputXML);
			    String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			    DAONotifyAPPLog.DAONotifyAPPLogger.debug("ResponseMainCode: "+responseMainCode);



			    int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			    DAONotifyAPPLog.DAONotifyAPPLogger.debug("ResponseTotalRecords: "+responseTotalRecords);

			    if (responseMainCode.equals("0") && responseTotalRecords > 0)
				{

					String responseXMLData=xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData =responseXMLData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

	        		XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
	        		//DAONotifyAPPLog.DAONotifyAPPLogger.debug("ResponseXMLData: "+responseXMLData);

	        		outputResponseXML=xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
	        		//DAONotifyAPPLog.DAONotifyAPPLogger.debug("OutputResponseXML: "+outputResponseXML);

	        		if("".equalsIgnoreCase(outputResponseXML)){
	        			outputResponseXML="Error";
	    			}
	        		break;
				}
			    Loop_count++;
			    Thread.sleep(1000);
			}
			while(Loop_count<integrationWaitTime);
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("integrationWaitTime: "+integrationWaitTime);

		}
		catch(Exception e)
		{
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Exception occurred in outputResponseXML" + e.getMessage());
			outputResponseXML="Error";
		}

		return outputResponseXML;

	}
	
	private String getRequestXML(String cabinetName, String sessionId,
			String processInstanceID, String ws_name, String userName, StringBuilder sInputXML)
	{
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("<APMQPUTGET_Input>");
		strBuff.append("<SessionId>" + sessionID + "</SessionId>");
		strBuff.append("<EngineName>" + cabinetName + "</EngineName>");
		strBuff.append("<XMLHISTORY_TABLENAME>NG_DAO_XMLLOG_HISTORY</XMLHISTORY_TABLENAME>");
		strBuff.append("<WI_NAME>" + processInstanceID + "</WI_NAME>");
		strBuff.append("<WS_NAME>" + ws_name + "</WS_NAME>");
		strBuff.append("<USER_NAME>" + userName + "</USER_NAME>");
		strBuff.append("<MQ_REQUEST_XML>");
		strBuff.append(sInputXML);
		strBuff.append("</MQ_REQUEST_XML>");
		strBuff.append("</APMQPUTGET_Input>");
		DAONotifyAPPLog.DAONotifyAPPLogger.debug("GetRequestXML: "+ strBuff.toString());
		return strBuff.toString();
	}
	
	
	// one pager mail method
	
	private String downloadAttachDocuments(String processInstanceID){
	try
	{
			//String query123 = "select name as DOCUMENTNAME, AppName as APPNAME , ImageIndex as IMAGE_INDEX,  VolumeId as VOLUME_ID from PDBDocument with(nolock) where name in ('"+docList+"') and DocumentIndex in ( select DocumentIndex from PDBDocumentContent where ParentFolderIndex = (select itemindex from NG_DCC_EXTTABLE with(nolock) where Wi_Name='"+processInstanceID+"'))";
			String query_doc="Select top 1  name as DOCUMENTNAME, AppName as APPNAME,ISnull(ImageIndex,'') as ImageIndex, volumeId from pdbdocument with (nolock) "
				+ "WHERE DocumentIndex in (select DocumentIndex from PDBDocumentContent with (nolock) where ParentFolderIndex =(select FolderIndex from PDBFolder with (nolock) where Name = '"+processInstanceID+"'))and"
				+ " name like 'Wet_signature_form%' order by DocumentIndex desc";

			
			//String sessionID = CommonConnection.getSessionID(DAONotifyAPPLog.DAONotifyAPPLogger, false);
			String sInputXML_doc = "<?xml version=\"1.0\"?>"+
					"<APSelectWithColumnNames_Input>"+ 
					"<Option>APSelectWithColumnNames</Option>"+
					"<EngineName>" + cabinetName + "</EngineName> "+
					"<SessionId>" + sessionID + "</SessionId>"+
					"<Query>" + query_doc + "</Query>"+
					"</APSelectWithColumnNames_Input>";
					
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Input:sInputXML_doc"+sInputXML_doc);	
			String sOutputXml = CommonMethods.WFNGExecute(sInputXML_doc,jtsIP,jtsPort,1);
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("sOutputXml"+sOutputXml);
			XMLParser xmlParserData = new XMLParser(sOutputXml);
			String mainCode=xmlParserData.getValueOf("MainCode");
			if("0".equalsIgnoreCase(mainCode) && Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"))>0)
			{
				String Records = xmlParserData.getNextValueOf("Records");
				DAONotifyAPPLog.DAONotifyAPPLogger.debug("TotalRecords: "+Records);
				String parseStringArray[]=CommonMethods.getTagValues(Records, "Record").split("`");
				DAONotifyAPPLog.DAONotifyAPPLogger.debug("Total no of documents: "+parseStringArray.length);
				File file = new File(propDocsPath+System.getProperty("file.separator")+processInstanceID);
				if (!file.exists()) 
				{
					file.mkdir();
				}
				
				String pathTodownload=propDocsPath+System.getProperty("file.separator")+processInstanceID+System.getProperty("file.separator");
					WFXmlResponse parsergetlist = new WFXmlResponse(parseStringArray[0]);
					String docname=parsergetlist.getVal("DOCUMENTNAME");
					String imageindex=parsergetlist.getVal("ImageIndex");
					int volid=Integer.parseInt(parsergetlist.getVal("volumeId"));
					String ext =parsergetlist.getVal("APPNAME");
				
				
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("sOutputXml docname : "+docname);
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("sOutputXml imageindex : "+imageindex);
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("sOutputXml volid : "+volid);
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("sOutputXml  ext: "+ext);
					
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("fewfewbnfewbnfejn  ffewfewfext: "+parseStringArray.length);
									
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("temppppp"+pathTodownload);
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("dfdsfsdf"+imageindex);
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("temppppp"+jtsPort);
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("IP"+jtsIP);
					try
					{
						CImageServer cImageServer=null;
						try 
						{
							cImageServer = new CImageServer(null,jtsIP, Short.parseShort(jtsPort));
							//cImageServer = new CImageServer(null,"10.15.12.164", Short.parseShort("2809"));
							//cImageServer = new CImageServer(null,"10.15.12.164", Short.parseShort("3333"));
						}
						catch (JPISException e) 
						{
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("inside Catch ");
							DAONotifyAPPLog.DAONotifyAPPLogger.debug(e.toString());
							//msg = e.getMessage();
							return null;
			
						}
						DAONotifyAPPLog.DAONotifyAPPLogger.debug("inside tryyyy ");
						try{
						   /* JPDBString siteName = new JPDBString();
						    
						    CPISDocumentTxn.GetDocInFile_MT(null, "127.0.0.1", (short)Integer.parseInt(jtsPort),
						    cabinetName, (short)1,(short)volid, Integer.parseInt(imageindex),null, propDocsPath+docname+"."+ext, siteName);
						    */
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("jtsPort : "+jtsPort);
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("cabinetName  : "+cabinetName);
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("volid volid : "+volid);
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("doc location: "+pathTodownload+docname+"."+ext);
							int odDownloadCode=cImageServer.JPISGetDocInFile_MT(null,jtsIP, Short.parseShort(jtsPort), cabinetName, Short.parseShort("1"),Short.parseShort(String.valueOf(volid)), 
							Integer.parseInt(imageindex),"",pathTodownload+docname+"."+ext, new JPDBString());
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("odDownloadCode--"+odDownloadCode);
							if(odDownloadCode==1)
							{
								DAONotifyAPPLog.DAONotifyAPPLogger.debug("DOWNLOAD_CALL_COMPLETE");
								
								if("tif".equalsIgnoreCase(ext)||"tiff".equalsIgnoreCase(ext)){
									String downloadedDocPath=pathTodownload+docname+"."+ext;
									String pdfdocname="Personal Account Digital Application Confirmation";
									String convertedTiffPath=pathTodownload+pdfdocname+"."+"pdf";
									DAONotifyAPPLog.DAONotifyAPPLogger.debug("downloadedDocPath : "+downloadedDocPath);
									DAONotifyAPPLog.DAONotifyAPPLogger.debug("convertedTiffPath : "+convertedTiffPath);
									RandomAccessFileOrArray mytiff=new RandomAccessFileOrArray(downloadedDocPath);									
									int noPages=TiffImage.getNumberOfPages(mytiff);
									Document convertedPdf=new Document();
									PdfWriter.getInstance(convertedPdf,new FileOutputStream(convertedTiffPath));
									convertedPdf.open();
									for(int i=1;i<=noPages;i++){
										Image tempImage=TiffImage.getTiffImage(mytiff,i);//									
										Rectangle pageSize=new Rectangle(tempImage.getWidth(),tempImage.getHeight());
										convertedPdf.setPageSize(pageSize);
										convertedPdf.newPage();
										convertedPdf.add(tempImage);
									}												
										
									convertedPdf.close();
									mytiff.close();
									File fileToDelete=new File(downloadedDocPath);									
//									
									if(fileToDelete.delete()){
										DAONotifyAPPLog.DAONotifyAPPLogger.debug("File deleted successfully : ");
									}
									else{
										DAONotifyAPPLog.DAONotifyAPPLogger.debug("File not deleted: ");
									}
								}
//								
							}
							else
							{
								return null;
							}
						}catch(Exception e)
						{
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("Exception-"+e.toString());
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("sOutputXml : sadfdsfsdf");
							return null;
							//e.printStackTrace();
						} /*catch (JPISException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("Exception-"+e.toString());
							return null;
						}*/
						DAONotifyAPPLog.DAONotifyAPPLogger.debug("dddd");	
					}
					catch(Exception e)
					{
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("Exception-"+e.toString());
							DAONotifyAPPLog.DAONotifyAPPLogger.debug("sOutputXml : sadfdsfsdf");
							return null;
							//e.printStackTrace();
					}
				
			}
			else
			{
				DAONotifyAPPLog.DAONotifyAPPLogger.debug("No documents records received from apselect-"+mainCode);
			}
		}
		catch(Exception e)
		{
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Exception-"+e.toString());
			return null;
		}
		return "Done";
	}
	
	private void doneWiDao(String processInstanceID,String WorkItemID,String ActivityID,String ProcessDefId ,String ActivityType,String attributesTag,String sJtsIp,String iJtsPort, String ErrDesc ,String decisionValue,String MsgId,String ActivityName,String entryDateTime,HashMap<String, String> CheckGridDataMap){
		try
		{
		String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionID, processInstanceID,WorkItemID);
		String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML,sJtsIp,iJtsPort,1);
		DAONotifyAPPLog.DAONotifyAPPLogger.debug("Output XML For WmgetWorkItemCall: " + getWorkItemOutputXml);

		XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
		String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
		DAONotifyAPPLog.DAONotifyAPPLogger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);

		if (getWorkItemMainCode.trim().equals("0"))
		{
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);

			//String assignWorkitemAttributeInputXML=CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionId,processInstanceID,WorkItemID,attributesTag);
			
			String assignWorkitemAttributeInputXML = "<?xml version=\"1.0\"?><WMAssignWorkItemAttributes_Input>"
					+ "<Option>WMAssignWorkItemAttributes</Option>"
					+ "<EngineName>"+cabinetName+"</EngineName>"
					+ "<SessionId>"+sessionID+"</SessionId>"
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
			
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("InputXML for assignWorkitemAttribute Call Notify: "+assignWorkitemAttributeInputXML);

			String assignWorkitemAttributeOutputXML=WFNGExecute(assignWorkitemAttributeInputXML,sJtsIp,
					iJtsPort,1);
			
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("OutputXML for assignWorkitemAttribute Call Notify: "+assignWorkitemAttributeOutputXML);
			
			XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
			String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("AssignWorkitemAttribute MainCode: "+assignWorkitemAttributeMainCode);

			if(assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0")){
				DAONotifyAPPLog.DAONotifyAPPLogger.debug("AssignWorkitemAttribute Successful: "+assignWorkitemAttributeMainCode);				
				DAONotifyAPPLog.DAONotifyAPPLogger.debug("WorkItem moved to next Workstep.");
			}
			else{
				DAONotifyAPPLog.DAONotifyAPPLogger.debug("decisionValue : "+decisionValue);
				ErrDesc="Done WI Failed";
				sendMail(cabinetName,sessionID,processInstanceID,jtsIP,jtsPort,ErrDesc,assignWorkitemAttributeMainCode,ProcessDefId,MsgId);
			}
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			Date current_date = new Date();
			String formattedEntryDatetime=dateFormat.format(current_date);
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);
	
			
			Date d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(entryDateTime);
			String entrydatetime_format = dateFormat.format(d1);

			String columnNames="wi_name,decision_date_time,workstep,user_name,Decision,Remarks,entry_date_time";
			String columnValues="'"+processInstanceID+"','"+formattedEntryDatetime+"','"+ActivityName+"','"
			+CommonConnection.getUsername()+"','"+decisionValue+"','"+ErrDesc+"','"+entrydatetime_format+"'";

			String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionID, columnNames, columnValues,"NG_DAO_GR_DECISION_HISTORY");
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("APInsertInputXML: "+apInsertInputXML);

			String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("APInsertOutputXML: "+ apInsertInputXML);

			XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
			String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Completed On "+ ActivityName);
			
			if(apInsertMaincode.equalsIgnoreCase("0")){
				DAONotifyAPPLog.DAONotifyAPPLogger.debug("ApInsert successful: "+apInsertMaincode);
				DAONotifyAPPLog.DAONotifyAPPLogger.debug("Inserted in WiHistory table successfully.");
			}
			else
			{
				DAONotifyAPPLog.DAONotifyAPPLogger.debug("ApInsert failed: "+apInsertMaincode);
			}
		}
		else
		{
			getWorkItemMainCode="";
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("WmgetWorkItem failed: "+getWorkItemMainCode);
			ErrDesc="WI Failed";
			sendMail(cabinetName,sessionID,processInstanceID,jtsIP,jtsPort,ErrDesc,getWorkItemMainCode,ProcessDefId,MsgId);
		}
	}
		catch(Exception e)
		{
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Exception-"+e.toString());
			
		}
	}
	
	public void sendMailToCustomer(String processInstanceID,String FullName,String CustomerEmail,String ProcessDefId,String ActivityID)
	{
		try{
			String MailTemplate ="";
			String MailSubject ="";
			String FromMail ="";
			String query_matertable = "select MailTemplate,MailSubject,FromMail,* from ng_master_dao_email_trigger with (NOLOCK) where  workstepname='DEH_update' and mailevent='Employer Added'";
			
			String query_matertableaIPXML = CommonMethods.apSelectWithColumnNames(query_matertable, cabinetName,sessionID);
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("query_matertableaIPXML: " + query_matertableaIPXML);
			
			String ematertableOPXML = CommonMethods.WFNGExecute(query_matertableaIPXML, jtsIP, jtsPort, 1);
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("extTabDataOPXML: " + ematertableOPXML);
			
			XMLParser xmlParserDataMasteratble = new XMLParser(ematertableOPXML);
			int iTotalrecMaster = Integer.parseInt(xmlParserDataMasteratble.getValueOf("TotalRetrieved"));
			if (xmlParserDataMasteratble.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrecMaster > 0) 
			{
				for (int k = 0; k < iTotalrecMaster; k++) 
				{
					String xmlDataExtTabmaster = xmlParserDataMasteratble.getNextValueOf("Record");
					xmlDataExtTabmaster = xmlDataExtTabmaster.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
					XMLParser xmlGetWIDetailsmaster = new XMLParser(xmlDataExtTabmaster);
					
					 MailTemplate = xmlGetWIDetailsmaster.getValueOf("MailTemplate");
					 MailSubject = xmlGetWIDetailsmaster.getValueOf("MailSubject");
					 FromMail = xmlGetWIDetailsmaster.getValueOf("FromMail");
				}
			}
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
			String insertedDateTime = simpleDateFormat.format(new Date());
			
			String FinalMailStr = MailTemplate.replace("#CustomerName#", FullName);
			
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("MailTemplatesss: " + MailTemplate);
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("MailSubject: " + MailSubject);
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("FromMail: " + FromMail);
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("toMailID: " + CustomerEmail);
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("FinalMailStr: " + FinalMailStr);
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("insertedDateTime: " + insertedDateTime);
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("CustomerName: " + FullName);
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("ActivityID: " + ActivityID);
			
	
			String columnName = "MAILFROM,MAILTO,MAILSUBJECT,MAILMESSAGE,MAILCONTENTTYPE,MAILPRIORITY,MAILSTATUS,INSERTEDBY,MAILACTIONTYPE,INSERTEDTIME,PROCESSDEFID,PROCESSINSTANCEID,WORKITEMID,ACTIVITYID,NOOFTRIALS";
			String strValues = "'" + FromMail + "','" + CustomerEmail + "',N'" + MailSubject + "',N'" + FinalMailStr
					+ "','text/html;charset=UTF-8','1','N','CUSTOM','TRIGGER','" + insertedDateTime + "','"
					+ ProcessDefId + "','"+processInstanceID +"','1','"+ActivityID+"','0'";
	
			String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionID, columnName, strValues,"WFMAILQUEUETABLE");
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("APInsertInputXML: " + apInsertInputXML);
	
			String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, jtsIP, jtsPort, 1);
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("APInsertOutputXML: " + apInsertOutputXML);
	
			XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
			String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Status of apInsertMaincode  " + apInsertMaincode);
			
			if (apInsertMaincode.equalsIgnoreCase("0")) 
			{
				DAONotifyAPPLog.DAONotifyAPPLogger.debug("ApInsert successful: " + apInsertMaincode);
				DAONotifyAPPLog.DAONotifyAPPLogger.debug("Inserted in Wimailquque  table successfully.");
			}
			else
			{				
				// mail sent to support team to check mail template not
				// sent to customer
				DAONotifyAPPLog.DAONotifyAPPLogger.debug("ApInsert failed: " + apInsertMaincode);
				MailSubject = EmployerAddMailSubject;
				FinalMailStr =EmployerAddMailStr;
				FinalMailStr = FinalMailStr.replace("<WI_NAME>", processInstanceID);
				String columnName1 = "MAILFROM,MAILTO,MAILSUBJECT,MAILMESSAGE,MAILCONTENTTYPE,MAILPRIORITY,MAILSTATUS,INSERTEDBY,MAILACTIONTYPE,INSERTEDTIME,PROCESSDEFID,PROCESSINSTANCEID,WORKITEMID,ACTIVITYID,NOOFTRIALS";
				String strValues1 = "'" + fromMailID + "','" + toMailID + "','" + MailSubject + "','"
						+ FinalMailStr + "','text/html;charset=UTF-8','1','N','CUSTOM','TRIGGER','"
						+ insertedDateTime + "','" + ProcessDefId + "','" + processInstanceID + "','1','"+ActivityID+"','0'";
	
				String apInsertInputXML1 = CommonMethods.apInsert(cabinetName, sessionID, columnName1,
						strValues1, "WFMAILQUEUETABLE");
				DAONotifyAPPLog.DAONotifyAPPLogger.debug("APInsertInputXML: " + apInsertInputXML1);
	
				String apInsertOutputXML1 = CommonMethods.WFNGExecute(apInsertInputXML1, jtsIP, jtsPort, 1);
				DAONotifyAPPLog.DAONotifyAPPLogger.debug("APInsertOutputXML: " + apInsertOutputXML1);
	
				XMLParser xmlParserAPInsert1 = new XMLParser(apInsertOutputXML1);
				String apInsertMaincode1 = xmlParserAPInsert1.getValueOf("MainCode");
				DAONotifyAPPLog.DAONotifyAPPLogger.debug("Status of apInsertMaincode  " + apInsertMaincode1);
	
				if (apInsertMaincode1.equalsIgnoreCase("0")) {
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("ApInsert successful: " + apInsertMaincode1);
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("Inserted in Wimailquque  table successfully.");
				}
	
				else {
					DAONotifyAPPLog.DAONotifyAPPLogger.debug("ApInsert failed: " + apInsertMaincode1);
				}
	
			}
		}
		catch(Exception e)
		{
			DAONotifyAPPLog.DAONotifyAPPLogger.debug("Exception-"+e.toString());
			
		}
	}
	
}



