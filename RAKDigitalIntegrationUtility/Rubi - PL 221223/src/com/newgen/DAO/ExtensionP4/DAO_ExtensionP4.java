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


package com.newgen.DAO.ExtensionP4;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.newgen.DCC.AnnualFeeEnable.DCC_AnnualFeeLog;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;;


public class DAO_ExtensionP4 implements Runnable
{

	private static NGEjbClient ngEjbClientCIFVer;

	static Map<String, String> DAOExtensionConfigParamMap= new HashMap<String, String>();


	int socketConnectionTimeout=0;
	int integrationWaitTime=0;
	int sleepIntervalInMin=0;
	public static int sessionCheckInt=0;
	public static int waitLoop=50;
	public static int loopCount=50;
	public String fromMailID="";
	public String toMailID = "";
	public String mailSubject = "";
	public String MailStr="";
	public String jtsIP = "";
	public String jtsPort = "";
	public String sessionID = "";
	
	@Override
	public void run()
	{
		String cabinetName = "";
			//String queueID = "";
		String queueIDDAO = "";
		String queueIDDCC = "";

		try
		{
			DAOExtensionLogger.setLogger();
			ngEjbClientCIFVer = NGEjbClient.getSharedInstance();

			DAOExtensionLogger.DAOExtensionLogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			DAOExtensionLogger.DAOExtensionLogger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				DAOExtensionLogger.DAOExtensionLogger.error("Could not Read Config Properties [DAO_ExtensionP4]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			DAOExtensionLogger.DAOExtensionLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			DAOExtensionLogger.DAOExtensionLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			DAOExtensionLogger.DAOExtensionLogger.debug("JTSPORT: " + jtsPort);

			queueIDDAO = DAOExtensionConfigParamMap.get("queueIDDAO");
			DAOExtensionLogger.DAOExtensionLogger.debug("QueueID dao: " + queueIDDAO);
			queueIDDCC = DAOExtensionConfigParamMap.get("queueIDDCC");
			DAOExtensionLogger.DAOExtensionLogger.debug("QueueID dcc: " + queueIDDCC);

			socketConnectionTimeout=Integer.parseInt(DAOExtensionConfigParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			DAOExtensionLogger.DAOExtensionLogger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(DAOExtensionConfigParamMap.get("INTEGRATION_WAIT_TIME"));
			DAOExtensionLogger.DAOExtensionLogger.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(DAOExtensionConfigParamMap.get("SleepIntervalInMin"));
			DAOExtensionLogger.DAOExtensionLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);

			fromMailID=DAOExtensionConfigParamMap.get("fromMailID");
			DAOExtensionLogger.DAOExtensionLogger.debug("fromMailID: "+fromMailID);
			
			toMailID=DAOExtensionConfigParamMap.get("toMailID");
			DAOExtensionLogger.DAOExtensionLogger.debug("toMailID: "+toMailID);
			
			mailSubject=DAOExtensionConfigParamMap.get("mailSubject");
			DAOExtensionLogger.DAOExtensionLogger.debug("mailSubject: "+mailSubject);
			
			MailStr=DAOExtensionConfigParamMap.get("MailStr");
			DAOExtensionLogger.DAOExtensionLogger.debug("MailStr: "+MailStr);
			
			sleepIntervalInMin=Integer.parseInt(DAOExtensionConfigParamMap.get("SleepIntervalInMin"));
			DAOExtensionLogger.DAOExtensionLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);

			sessionID = CommonConnection.getSessionID(DAOExtensionLogger.DAOExtensionLogger, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				DAOExtensionLogger.DAOExtensionLogger.debug("Could Not Connect to Server!");
			}
			else
			{
				DAOExtensionLogger.DAOExtensionLogger.debug("Session ID found: " + sessionID);
				HashMap<String, String> socketDetailsMap = socketConnectionDetails(cabinetName, jtsIP, jtsPort, sessionID);
				while (true) {
					DAOExtensionLogger.setLogger();
					DAOExtensionLogger.DAOExtensionLogger.debug("iRBL CIF Verification...123.");
					startDAO_EtensionP4_Utility(cabinetName, jtsIP, jtsPort, sessionID, queueIDDAO, socketConnectionTimeout, integrationWaitTime, socketDetailsMap);
					
					startDCC_EtensionP4_Utility(cabinetName, jtsIP, jtsPort, sessionID, queueIDDCC, socketConnectionTimeout, integrationWaitTime, socketDetailsMap);
					System.out.println("queue ID dcc!"+queueIDDAO);
					System.out.println("No More workitems to Process, Sleeping!");
					Thread.sleep(sleepIntervalInMin * 60 * 1000);
				}
			}
		}

		catch(Exception e)
		{
			e.printStackTrace();
			DAOExtensionLogger.DAOExtensionLogger.error("Exception Occurred in DAO_ExtensionP4 : "+e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			DAOExtensionLogger.DAOExtensionLogger.error("Exception Occurred in DAO_ExtensionP4 : "+result);
		}
	}

	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "DAOExtensionP4.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			  {
			    String name = (String) names.nextElement();
			    DAOExtensionConfigParamMap.put(name, p.getProperty(name));
			  }
		    }
		catch (Exception e)
		{
			return -1 ;
		}
		return 0;
	}


	private void startDAO_EtensionP4_Utility(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String queueID, 
	int socketConnectionTimeOut, int integrationWaitTime, HashMap<String, String> socketDetailsMap){
		
		final String ws_name="Extension_P4";
		try{
			
			final HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();
			
			sessionID  = CommonConnection.getSessionID(DAOExtensionLogger.DAOExtensionLogger, false);

			if (sessionID == null || sessionID.equalsIgnoreCase("") || sessionID.equalsIgnoreCase("null"))
			{
				DAOExtensionLogger.DAOExtensionLogger.error("Could Not Get Session ID "+sessionID);
				return;
			}

			DAOExtensionLogger.DAOExtensionLogger.debug("Fetching all Workitems on DAO_ExtensionP4 queue");
			System.out.println("Fetching all Workitems on CIF_Update_Initial queue");
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionID, queueID);
			DAOExtensionLogger.DAOExtensionLogger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);

			DAOExtensionLogger.DAOExtensionLogger.debug("WMFetchWorkList DAO_ExtensionP4 OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			DAOExtensionLogger.DAOExtensionLogger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			DAOExtensionLogger.DAOExtensionLogger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);

			DAOExtensionLogger.DAOExtensionLogger.debug("Number of workitems retrieved on DAO_ExtensionP4: "+fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on DAO_ExtensionP4: "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0){
				
				for(int i=0; i<fetchWorkitemListCount; i++){
					
					String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

					DAOExtensionLogger.DAOExtensionLogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID=xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					DAOExtensionLogger.DAOExtensionLogger.debug("Current ProcessInstanceID: "+processInstanceID);

					DAOExtensionLogger.DAOExtensionLogger.debug("Processing Workitem: "+processInstanceID);
					System.out.println("\nProcessing Workitem: "+processInstanceID);

					String WorkItemID=xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					DAOExtensionLogger.DAOExtensionLogger.debug("Current WorkItemID: "+WorkItemID);

					String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					DAOExtensionLogger.DAOExtensionLogger.debug("Current EntryDateTime: "+entryDateTime);

					String ActivityName=xmlParserfetchWorkItemData.getValueOf("ActivityName");
					DAOExtensionLogger.DAOExtensionLogger.debug("ActivityName: "+ActivityName);
					
					String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					DAOExtensionLogger.DAOExtensionLogger.debug("ActivityID: "+ActivityID);
					String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					DAOExtensionLogger.DAOExtensionLogger.debug("ActivityType: "+ActivityType);
					String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					DAOExtensionLogger.DAOExtensionLogger.debug("ProcessDefId: "+ProcessDefId);
					
					String decisionValue="";
					
				    String DBQuery ="select convert(varchar, delivery_date,103) as delivery_date,CardSerno,WI_name from NG_Digital_AWB_Status where WI_name='" + processInstanceID + "'";
				    // select method (product written) used to get the data in form of xml.
				    String extTabDataIPXML =CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAOExtensionLogger.DAOExtensionLogger, false));
				    DAOExtensionLogger.DAOExtensionLogger.debug("extTabDataIPXML: " + extTabDataIPXML);
				    String extTabDataOPXML = WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
				    DAOExtensionLogger.DAOExtensionLogger.debug("extTabDataOPXML: " + extTabDataOPXML);
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
			            	CheckGridDataMap.put("WI_name", objWorkList.getVal("WI_name"));
			            	CheckGridDataMap.put("delivery_date", objWorkList.getVal("delivery_date"));
			            	CheckGridDataMap.put("CardSerno", objWorkList.getVal("CardSerno"));
			            }
			        }
				    
		    	    // Entry date time from WF instrument table.
		    	    String DBQuery_entrydatetime ="select EntryDATETIME from WFINSTRUMENTTABLE with(nolock) where ProcessInstanceID ='" + processInstanceID + "' and ActivityName = 'Extension_P4'";
		            
		            String extTabDataIPXML3 =CommonMethods.apSelectWithColumnNames(DBQuery_entrydatetime, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAOExtensionLogger.DAOExtensionLogger, false));
		    	    DAOExtensionLogger.DAOExtensionLogger.debug("Remarks extTabDataIPXML: " + extTabDataIPXML3);
		    	    String extTabDataOPXML3 = WFNGExecute(extTabDataIPXML3, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
		    	    DAOExtensionLogger.DAOExtensionLogger.debug("Remarks extTabDataOPXML: " + extTabDataOPXML3);
		            
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
		    		.append(System.getProperty("file.separator")).append("ExtensionP4.txt").toString();
		            
		            BufferedReader sbf=new BufferedReader(new FileReader(fileLocation));
		    		
		    		StringBuilder sb=new StringBuilder();
		    		String line=sbf.readLine();
		    		while(line!=null)
		    		{
		    			sb.append(line);
		    			sb.append(System.lineSeparator());
		    			line=sbf.readLine();
		    		}
		    		
		    		String Extension = sb.toString().replace(">CARD_NO<",">"+CheckGridDataMap.get("CardSerno").trim()+"<")
		    		.replace(">DATE_DEL<",">"+CheckGridDataMap.get("delivery_date").trim()+"<");
		    		
		    		DAOExtensionLogger.DAOExtensionLogger.debug("Extension : "+Extension);
		    		DAOExtensionLogger.DAOExtensionLogger.debug("delivery_date : "+CheckGridDataMap.get("delivery_date"));
		    		//Deepak End
					
		    		String integrationStatus="Success";
					String attributesTag;
					String ErrDesc = "";
					StringBuilder finalString=new StringBuilder();
					finalString = finalString.append(Extension);
					//changes need to done to updae the correct flag
					HashMap<String, String> socketConnectionMap =socketConnectionDetails(cabinetName, sJtsIp, iJtsPort, sessionID); 
					
					integrationStatus = socketConnection(cabinetName, CommonConnection.getUsername(), sessionId, sJtsIp, iJtsPort, processInstanceID, ws_name, 60, 65,socketConnectionMap, finalString);
					
					// - xml parse for getting out the return code.
					
					XMLParser xmlParserSocketDetails= new XMLParser(integrationStatus);
					DAOExtensionLogger.DAOExtensionLogger.debug(" xmlParserSocketDetails : "+xmlParserSocketDetails);
				    String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
				    DAOExtensionLogger.DAOExtensionLogger.debug("Return Code: "+return_code+ "WI: "+processInstanceID);
				    String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
				    DAOExtensionLogger.DAOExtensionLogger.debug("return_desc : "+return_desc+ "WI: "+processInstanceID);
					
				    String MsgId ="";
				    if (integrationStatus.contains("<MessageId>"))
						MsgId = xmlParserSocketDetails.getValueOf("MessageId");
					
				    DAOExtensionLogger.DAOExtensionLogger.debug("MsgId : "+MsgId+" for WI: "+processInstanceID);
					
				    if("0000".equalsIgnoreCase(return_code) || "PRIME4 : Extension field already exists for the entity".equalsIgnoreCase(return_desc))
				    {
				    	integrationStatus="Success";
				    	ErrDesc = "ExtensionP4 Done Successfully";
				    }
					if ("Success".equalsIgnoreCase(integrationStatus))
					{
						decisionValue = "Success";
						DAOExtensionLogger.DAOExtensionLogger.debug("Decision in success: " +decisionValue);
						attributesTag="<Decision>"+decisionValue+"</Decision>";
						
						
						
					}
					else
					{
						ErrDesc = return_desc; //integrationStatus.replace("~", ",").replace("|", "\n");
						decisionValue = "Failed";
						DAOExtensionLogger.DAOExtensionLogger.debug("Decision in else : " +decisionValue);
						attributesTag="<Decision>"+decisionValue+"</Decision>";					
												
						sendMail(cabinetName,sessionID,processInstanceID,jtsIP,jtsPort,ErrDesc,return_code,ProcessDefId,MsgId);
					}
					DAOExtensionLogger.DAOExtensionLogger.debug("decisionValue: " +decisionValue);
					DAOExtensionLogger.DAOExtensionLogger.debug("ErrDesc: " +ErrDesc);
					
					String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionID, processInstanceID,WorkItemID);
					String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML,sJtsIp,iJtsPort,1);
					DAOExtensionLogger.DAOExtensionLogger.debug("Output XML For WmgetWorkItemCall: " + getWorkItemOutputXml);

					XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
					String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
					DAOExtensionLogger.DAOExtensionLogger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);

					if (getWorkItemMainCode.trim().equals("0"))
					{
						DAOExtensionLogger.DAOExtensionLogger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);

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
						
						DAOExtensionLogger.DAOExtensionLogger.debug("InputXML for assignWorkitemAttribute Call ExtensionP4: "+assignWorkitemAttributeInputXML);

						String assignWorkitemAttributeOutputXML=WFNGExecute(assignWorkitemAttributeInputXML,sJtsIp,
								iJtsPort,1);
						
						DAOExtensionLogger.DAOExtensionLogger.debug("OutputXML for assignWorkitemAttribute Call ExtensionP4: "+assignWorkitemAttributeOutputXML);
						
						XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
						String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
						DAOExtensionLogger.DAOExtensionLogger.debug("AssignWorkitemAttribute MainCode: "+assignWorkitemAttributeMainCode);

						if(assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0")){
							DAOExtensionLogger.DAOExtensionLogger.debug("AssignWorkitemAttribute Successful: "+assignWorkitemAttributeMainCode);
							System.out.println(processInstanceID + "Complete Succesfully with status "+decisionValue);
							DAOExtensionLogger.DAOExtensionLogger.debug("WorkItem moved to next Workstep.");
						}
						else{
							DAOExtensionLogger.DAOExtensionLogger.debug("decisionValue : "+decisionValue);
							ErrDesc="Done WI Failed";
							sendMail(cabinetName,sessionID,processInstanceID,jtsIP,jtsPort,ErrDesc,assignWorkitemAttributeMainCode,ProcessDefId,MsgId);
						}
						
						DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						
						Date current_date = new Date();
						String formattedEntryDatetime=dateFormat.format(current_date);
						DAOExtensionLogger.DAOExtensionLogger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);
				
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
						DAOExtensionLogger.DAOExtensionLogger.debug("APInsertInputXML: "+apInsertInputXML);

						String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
						DAOExtensionLogger.DAOExtensionLogger.debug("APInsertOutputXML: "+ apInsertInputXML);

						XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
						String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
						DAOExtensionLogger.DAOExtensionLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

						DAOExtensionLogger.DAOExtensionLogger.debug("Completed On "+ ActivityName);
						
						if(apInsertMaincode.equalsIgnoreCase("0")){
							
							DAOExtensionLogger.DAOExtensionLogger.debug("ApInsert successful: "+apInsertMaincode);
							DAOExtensionLogger.DAOExtensionLogger.debug("Inserted in WiHistory table successfully.");
						}
						else{
							DAOExtensionLogger.DAOExtensionLogger.debug("ApInsert failed: "+apInsertMaincode);
						}
					}
					else
					{
						getWorkItemMainCode="";
						DAOExtensionLogger.DAOExtensionLogger.debug("WmgetWorkItem failed: "+getWorkItemMainCode);
						ErrDesc="WI Failed";
						sendMail(cabinetName,sessionID,processInstanceID,jtsIP,jtsPort,ErrDesc,getWorkItemMainCode,ProcessDefId,MsgId);
					}
				}
			}
		}
		catch (Exception e){
			DAOExtensionLogger.DAOExtensionLogger.debug("Exception: "+e.getMessage());
		}
	}
	
	private void startDCC_EtensionP4_Utility(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String queueID, 
			int socketConnectionTimeOut, int integrationWaitTime, HashMap<String, String> socketDetailsMap){
				final String ws_name="ExtensionP4"; // add
				try{
					final HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();
					sessionID  = CommonConnection.getSessionID(DAOExtensionLogger.DAOExtensionLogger, false);
					if (sessionID == null || sessionID.equalsIgnoreCase("") || sessionID.equalsIgnoreCase("null"))
					{
						DAOExtensionLogger.DAOExtensionLogger.error("Could Not Get Session ID "+sessionID);
						return;
					}
					DAOExtensionLogger.DAOExtensionLogger.debug("Fetching all Workitems on DCC_ExtensionP4 queue");
					System.out.println("Fetching all Workitems on DCC P4 queue");
					
					String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionID, queueID);
					DAOExtensionLogger.DAOExtensionLogger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);
					
					String fetchWorkitemListOutputXML= WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);
					DAOExtensionLogger.DAOExtensionLogger.debug("WMFetchWorkList DCC_ExtensionP4 OutputXML: "+fetchWorkitemListOutputXML);
					
					XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);
					String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
					DAOExtensionLogger.DAOExtensionLogger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);
					
					int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
					DAOExtensionLogger.DAOExtensionLogger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);
					DAOExtensionLogger.DAOExtensionLogger.debug("Number of workitems retrieved on DAO_ExtensionP4: "+fetchWorkitemListCount);
					System.out.println("Number of workitems retrieved on DCC_ExtensionP4: "+fetchWorkitemListCount);
					
					if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0){
						for(int i=0; i<fetchWorkitemListCount; i++){
							String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
							fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
							DAOExtensionLogger.DAOExtensionLogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
							
							XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);
							String processInstanceID=xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
							DAOExtensionLogger.DAOExtensionLogger.debug("Current ProcessInstanceID: "+processInstanceID);
							DAOExtensionLogger.DAOExtensionLogger.debug("Processing Workitem: "+processInstanceID);
							System.out.println("\nProcessing Workitem: "+processInstanceID);
							
							String WorkItemID=xmlParserfetchWorkItemData.getValueOf("WorkItemId");
							DAOExtensionLogger.DAOExtensionLogger.debug("Current WorkItemID: "+WorkItemID);
							
							String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
							DAOExtensionLogger.DAOExtensionLogger.debug("Current EntryDateTime: "+entryDateTime);
							
							String ActivityName=xmlParserfetchWorkItemData.getValueOf("ActivityName");
							DAOExtensionLogger.DAOExtensionLogger.debug("ActivityName: "+ActivityName);
							
							String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
							DAOExtensionLogger.DAOExtensionLogger.debug("ActivityID: "+ActivityID);
							
							String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
							DAOExtensionLogger.DAOExtensionLogger.debug("ActivityType: "+ActivityType);
							
							String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
							DAOExtensionLogger.DAOExtensionLogger.debug("ProcessDefId: "+ProcessDefId);
							String decisionValue="";
							
						    String DBQuery ="select convert(varchar, delivery_date,103) as delivery_date,CardSerno,WI_name from NG_Digital_AWB_Status where WI_name='" + processInstanceID + "'";
						    DAOExtensionLogger.DAOExtensionLogger.debug("DBQuery: " + DBQuery);
						    
						    String extTabDataIPXML =CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAOExtensionLogger.DAOExtensionLogger, false));
						    DAOExtensionLogger.DAOExtensionLogger.debug("extTabDataIPXML: " + extTabDataIPXML);
						    
						    String extTabDataOPXML = WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
						    DAOExtensionLogger.DAOExtensionLogger.debug("extTabDataOPXML: " + extTabDataOPXML);
						    
						    XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
						    int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
						    if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
					        {
						    	String xmlDataExtTab = xmlParserData.getNextValueOf("Record");
					            xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
					            NGXmlList objWorkList = xmlParserData.createList("Records", "Record");
					            for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
					            {
					            	CheckGridDataMap.put("WI_name", objWorkList.getVal("WI_name"));
					            	CheckGridDataMap.put("delivery_date", objWorkList.getVal("delivery_date"));
					            	CheckGridDataMap.put("CardSerno", objWorkList.getVal("CardSerno"));
					            }
					        }
				    	    String DBQuery_entrydatetime ="select EntryDATETIME from WFINSTRUMENTTABLE with(nolock) where ProcessInstanceID ='" + processInstanceID + "' and ActivityName = 'ExtensionP4'"; // add
				    	    DAOExtensionLogger.DAOExtensionLogger.debug(" DBQuery_entrydatetime: " + DBQuery_entrydatetime);
				    	    String extTabDataIPXML3 =CommonMethods.apSelectWithColumnNames(DBQuery_entrydatetime, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAOExtensionLogger.DAOExtensionLogger, false));
				    	    DAOExtensionLogger.DAOExtensionLogger.debug(" extTabDataIPXML: " + extTabDataIPXML3);
				    	    
				    	    String extTabDataOPXML3 = WFNGExecute(extTabDataIPXML3, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
				    	    DAOExtensionLogger.DAOExtensionLogger.debug(" extTabDataOPXML: " + extTabDataOPXML3);
				    	    
				    	    XMLParser xmlParserData3 = new XMLParser(extTabDataOPXML3);
				    	    int iTotalrec3 = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
				    	    
				    	    if (xmlParserData3.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec3 > 0)
				    	    {
				    	    	String xmlDataExtTab3 = xmlParserData3.getNextValueOf("Record");
				    	    	xmlDataExtTab3 = xmlDataExtTab3.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
				                NGXmlList objWorkList3 = xmlParserData3.createList("Records", "Record");
				                for (; objWorkList3.hasMoreElements(true); objWorkList3.skip(true))
				                {
				                	CheckGridDataMap.put("EntryDATETIME", objWorkList3.getVal("EntryDATETIME"));
				                }
				            }
				            String fileLocation= new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DAO_Integration")
				    		.append(System.getProperty("file.separator")).append("ExtensionP4.txt").toString();
				            BufferedReader sbf=new BufferedReader(new FileReader(fileLocation));
				    		StringBuilder sb=new StringBuilder();
				    		String line=sbf.readLine();
				    		while(line!=null)
				    		{
				    			sb.append(line);
				    			sb.append(System.lineSeparator());
				    			line=sbf.readLine();
				    		}
				    		
				    		String Extension = sb.toString().replace(">CARD_NO<",">"+CheckGridDataMap.get("CardSerno").trim()+"<")
				    		.replace(">DATE_DEL<",">"+CheckGridDataMap.get("delivery_date").trim()+"<");
				    		
				    		//change by rubi
				    		/*String Extension = sb.toString().replace("CARD_NO",CheckGridDataMap.get("CardSerno").trim())
						    		.replace("DATE_DEL",CheckGridDataMap.get("delivery_date").trim());*/
						    		
				    		
				    		DAOExtensionLogger.DAOExtensionLogger.debug("Extension : "+Extension);
				    		DAOExtensionLogger.DAOExtensionLogger.debug("delivery_date : "+CheckGridDataMap.get("delivery_date"));
				    		String integrationStatus="Success";
							String attributesTag;
							String ErrDesc = "";
							StringBuilder finalString=new StringBuilder();
							finalString = finalString.append(Extension);
							
							HashMap<String, String> socketConnectionMap =socketConnectionDetails(cabinetName, sJtsIp, iJtsPort, sessionID); 
							integrationStatus = socketConnection(cabinetName, CommonConnection.getUsername(), sessionId, sJtsIp, iJtsPort, processInstanceID, ws_name, 60, 65,socketConnectionMap, finalString);
							
							XMLParser xmlParserSocketDetails= new XMLParser(integrationStatus);
							DAOExtensionLogger.DAOExtensionLogger.debug(" xmlParserSocketDetails : "+xmlParserSocketDetails);
						    String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
						    DAOExtensionLogger.DAOExtensionLogger.debug("Return Code: "+return_code+ "WI: "+processInstanceID);
						    String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
						    DAOExtensionLogger.DAOExtensionLogger.debug("return_desc : "+return_desc+ "WI: "+processInstanceID);
						    String MsgId ="";
						    
						    if (integrationStatus.contains("<MessageId>"))
								MsgId = xmlParserSocketDetails.getValueOf("MessageId");
						    DAOExtensionLogger.DAOExtensionLogger.debug("MsgId : "+MsgId+" for WI: "+processInstanceID);
						    if("0000".equalsIgnoreCase(return_code))
						    {
						    	integrationStatus="Success";
						    	ErrDesc = "ExtensionP4 Done Successfully";
						    }
							if ("Success".equalsIgnoreCase(integrationStatus))
							{
								decisionValue = "Success";
								DAOExtensionLogger.DAOExtensionLogger.debug("Decision in success: " +decisionValue);
								attributesTag="<Decision>"+decisionValue+"</Decision>";								
								String columnNames="is_extensionCall";
								String columnValues="'Y'";

								String sWhereClause = "Wi_Name ='" + processInstanceID + "'";

								String extTableIPUpdateXml = CommonMethods.apUpdateInput(cabinetName,sessionId,"NG_DCC_EXTTABLE", columnNames, columnValues, sWhereClause);
								DAOExtensionLogger.DAOExtensionLogger.debug("Input XML for apUpdateInput for "+ "NG_DCC_EXTTABLE" + " Table : " + extTableIPUpdateXml);

								String extTableOPUpdateXml = CommonMethods.WFNGExecute(extTableIPUpdateXml,sJtsIp,iJtsPort, 1);
								DAOExtensionLogger.DAOExtensionLogger.debug("Output XML for apUpdateInput for "+" NG_DCC_EXTTABLE "+ " Table : " + extTableOPUpdateXml);

								XMLParser sXMLParserChild = new XMLParser(extTableOPUpdateXml);
								String StrMainCode = sXMLParserChild.getValueOf("MainCode");

								if (StrMainCode.equals("0")) 
								{
									DAOExtensionLogger.DAOExtensionLogger.debug("Total Limit and Final limit are equal this time and Successful in apUpdateInput the record in :  NG_DCC_EXTTABLE " );
								} 
								else 
								{
									DAOExtensionLogger.DAOExtensionLogger.debug("Total Limit and Final limit are equal this time but Error in Executing apUpdateInput sOutputXML : "+ extTableOPUpdateXml);
									
									DAOExtensionLogger.DAOExtensionLogger.debug("Total Limit and Final limit are equal this time but WMgetWorkItemCall failed: " + processInstanceID);
								}
								
							}
							else
							{
								ErrDesc = return_desc; //integrationStatus.replace("~", ",").replace("|", "\n");
								decisionValue = "Failed";
								DAOExtensionLogger.DAOExtensionLogger.debug("Decision in else : " +decisionValue);
								attributesTag="<Decision>"+decisionValue+"</Decision>";
								
								String columnNames="is_extensionCall";
								String columnValues="'N'";
								String sWhereClause = "Wi_Name ='" + processInstanceID + "'";

								String extTableIPUpdateXml = CommonMethods.apUpdateInput(cabinetName,sessionId,"NG_DCC_EXTTABLE", columnNames, columnValues, sWhereClause);
								DAOExtensionLogger.DAOExtensionLogger.debug("Input XML for apUpdateInput for "+ "NG_DCC_EXTTABLE" + " Table : " + extTableIPUpdateXml);

								String extTableOPUpdateXml = CommonMethods.WFNGExecute(extTableIPUpdateXml,sJtsIp,iJtsPort, 1);
								DAOExtensionLogger.DAOExtensionLogger.debug("Output XML for apUpdateInput for "+ "NG_DCC_EXTTABLE" + " Table : " + extTableOPUpdateXml);

								XMLParser sXMLParserChild = new XMLParser(extTableOPUpdateXml);
								String StrMainCode = sXMLParserChild.getValueOf("MainCode");

								if (StrMainCode.equals("0")) 
								{
									DAOExtensionLogger.DAOExtensionLogger.debug("Total Limit and Final limit are equal this time and Successful in apUpdateInput the record in : NG_DCC_EXTTABLE" );
								} 
								else 
								{
									DAOExtensionLogger.DAOExtensionLogger.debug("Total Limit and Final limit are equal this time but Error in Executing apUpdateInput sOutputXML : "+ extTableOPUpdateXml);
									System.out.println("Total Limit and Final limit are equal this time but WMgetWorkItemCall failed: " + processInstanceID);
									DAOExtensionLogger.DAOExtensionLogger.debug("Total Limit and Final limit are equal this time but WMgetWorkItemCall failed: " + processInstanceID);
								}
								
								sendMail(cabinetName,sessionID,processInstanceID,jtsIP,jtsPort,ErrDesc,return_code,ProcessDefId,MsgId);
							}
							
							DAOExtensionLogger.DAOExtensionLogger.debug("decisionValue: " +decisionValue);
							DAOExtensionLogger.DAOExtensionLogger.debug("ErrDesc: " +ErrDesc);
							String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionID, processInstanceID,WorkItemID);
							String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML,sJtsIp,iJtsPort,1);
							DAOExtensionLogger.DAOExtensionLogger.debug("Output XML For WmgetWorkItemCall: " + getWorkItemOutputXml);
							XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
							String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
							DAOExtensionLogger.DAOExtensionLogger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);
							if (getWorkItemMainCode.trim().equals("0"))
							{
								DAOExtensionLogger.DAOExtensionLogger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);
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
								DAOExtensionLogger.DAOExtensionLogger.debug("InputXML for assignWorkitemAttribute Call ExtensionP4: "+assignWorkitemAttributeInputXML);
								String assignWorkitemAttributeOutputXML=WFNGExecute(assignWorkitemAttributeInputXML,sJtsIp,
										iJtsPort,1);
								DAOExtensionLogger.DAOExtensionLogger.debug("OutputXML for assignWorkitemAttribute Call ExtensionP4: "+assignWorkitemAttributeOutputXML);
								XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
								String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
								DAOExtensionLogger.DAOExtensionLogger.debug("AssignWorkitemAttribute MainCode: "+assignWorkitemAttributeMainCode);
								if(assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0")){
									DAOExtensionLogger.DAOExtensionLogger.debug("AssignWorkitemAttribute Successful: "+assignWorkitemAttributeMainCode);
									System.out.println(processInstanceID + "Complete Succesfully with status "+decisionValue);
									DAOExtensionLogger.DAOExtensionLogger.debug("WorkItem moved to next Workstep.");
								}
								else{
									DAOExtensionLogger.DAOExtensionLogger.debug("decisionValue : "+decisionValue);
									ErrDesc="Done WI Failed";
									sendMail(cabinetName,sessionID,processInstanceID,jtsIP,jtsPort,ErrDesc,assignWorkitemAttributeMainCode,ProcessDefId,MsgId);
								}
								DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								Date current_date = new Date();
								String formattedEntryDatetime=dateFormat.format(current_date);
								DAOExtensionLogger.DAOExtensionLogger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);
								String entrydatetime = CheckGridDataMap.get("EntryDATETIME");
								if(entrydatetime.equalsIgnoreCase("") || entrydatetime==null){
									entrydatetime = entryDateTime;
								}
								Date d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(entrydatetime);
								String entrydatetime_format = dateFormat.format(d1);
								String columnNames="wi_name,dec_date,workstep,user_name,Decision,Remarks,ENTRY_DATE_TIME";
								String columnValues="'"+processInstanceID+"','"+formattedEntryDatetime+"','"+ActivityName+"','"
								+CommonConnection.getUsername()+"','"+decisionValue+"','"+ErrDesc+"','"+entrydatetime_format+"'";
								String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionID, columnNames, columnValues,"NG_DCC_GR_DECISION_HISTORY");
								DAOExtensionLogger.DAOExtensionLogger.debug("APInsertInputXML: "+apInsertInputXML);
								String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
								DAOExtensionLogger.DAOExtensionLogger.debug("APInsertOutputXML: "+ apInsertInputXML);
								XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
								String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
								DAOExtensionLogger.DAOExtensionLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);
								DAOExtensionLogger.DAOExtensionLogger.debug("Completed On "+ ActivityName);
								if(apInsertMaincode.equalsIgnoreCase("0")){
									DAOExtensionLogger.DAOExtensionLogger.debug("ApInsert successful: "+apInsertMaincode);
									DAOExtensionLogger.DAOExtensionLogger.debug("Inserted in WiHistory table successfully.");
								}
								else{
									DAOExtensionLogger.DAOExtensionLogger.debug("ApInsert failed: "+apInsertMaincode);
								}
							}
							else
							{
								getWorkItemMainCode="";
								DAOExtensionLogger.DAOExtensionLogger.debug("WmgetWorkItem failed: "+getWorkItemMainCode);
								ErrDesc="WI Failed";
								sendMail(cabinetName,sessionID,processInstanceID,jtsIP,jtsPort,ErrDesc,getWorkItemMainCode,ProcessDefId,MsgId);
							}
						}
					}
				}
				catch (Exception e){
					DAOExtensionLogger.DAOExtensionLogger.debug("Exception: "+e.getMessage());
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
            	DAOExtensionLogger.DAOExtensionLogger.debug("workitem name to send mail---"+wiName);
            	DAOExtensionLogger.DAOExtensionLogger.debug("ErrorMsg to send mail---"+ErrDesc);
            	DAOExtensionLogger.DAOExtensionLogger.debug("return_code to send mail---"+return_code);
            	
            	String FinalMailStr = MailStr.toString().replace("<WI_NAME>",wiName).replace("<ret_Code>",return_code)
            	.replace("<errormsg>",ErrDesc).replace("<MsgID>",MsgId);
            	DAOExtensionLogger.DAOExtensionLogger.debug("finalbody: "+FinalMailStr);

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
                DAOExtensionLogger.DAOExtensionLogger.debug("Mail Insert InputXml::::::::::\n"+sInputXML);
                sOutputXML =WFNGExecute(sInputXML, jtsIp,jtsPort,0);
                DAOExtensionLogger.DAOExtensionLogger.debug("Mail Insert OutputXml::::::::::\n"+sOutputXML);
                objXMLParser.setInputXML(sOutputXML);
                mainCodeforAPInsert=objXMLParser.getValueOf("MainCode");
                
            }
			
			catch(Exception e)
            {
                e.printStackTrace();
                DAOExtensionLogger.DAOExtensionLogger.error("Exception in Sending mail", e);
                sessionCheckInt++;
                waiteloopExecute(waitLoop);
                continue;
            }
            if (mainCodeforAPInsert.equalsIgnoreCase("11")) 
            {
                DAOExtensionLogger.DAOExtensionLogger.debug("Invalid session in Sending mail");
                sessionCheckInt++;
                //ThreadConnect.sessionId = ThreadConnect.getSessionID(cabinetName, jtsIP, jtsPort, userName,password);
                sessionID=CommonConnection.getSessionID(DAOExtensionLogger.DAOExtensionLogger, true);
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
            DAOExtensionLogger.DAOExtensionLogger.debug("mail Insert Successful");
            System.out.println("Mail Insert Successful for "+wiName+" in table WFMAILQUEUETABLE");
        }
        else
        {
            DAOExtensionLogger.DAOExtensionLogger.debug("mail Insert Unsuccessful");
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
			DAOExtensionLogger.DAOExtensionLogger.debug("Fetching Socket Connection Details.");
			System.out.println("Fetching Socket Connection Details.");

			String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'DigitalAO' and CallingSource = 'Utility'";

			String socketDetailsInputXML = CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
			DAOExtensionLogger.DAOExtensionLogger.debug("Socket Details APSelect InputXML: " + socketDetailsInputXML);

			String socketDetailsOutputXML = WFNGExecute(socketDetailsInputXML, sJtsIp, iJtsPort, 1);
			DAOExtensionLogger.DAOExtensionLogger.debug("Socket Details APSelect OutputXML: " + socketDetailsOutputXML);

			XMLParser xmlParserSocketDetails = new XMLParser(socketDetailsOutputXML);
			String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			DAOExtensionLogger.DAOExtensionLogger.debug("SocketDetailsMainCode: " + socketDetailsMainCode);

			int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			DAOExtensionLogger.DAOExtensionLogger.debug("SocketDetailsTotalRecords: " + socketDetailsTotalRecords);

			if (socketDetailsMainCode.equalsIgnoreCase("0") && socketDetailsTotalRecords > 0) {
				String xmlDataSocketDetails = xmlParserSocketDetails.getNextValueOf("Record");
				xmlDataSocketDetails = xmlDataSocketDetails.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

				XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

				String socketServerIP = xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
				DAOExtensionLogger.DAOExtensionLogger.debug("SocketServerIP: " + socketServerIP);
				socketDetailsMap.put("SocketServerIP", socketServerIP);

				String socketServerPort = xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
				DAOExtensionLogger.DAOExtensionLogger.debug("SocketServerPort " + socketServerPort);
				socketDetailsMap.put("SocketServerPort", socketServerPort);

				DAOExtensionLogger.DAOExtensionLogger.debug("SocketServer Details found.");
				System.out.println("SocketServer Details found.");

			}
		} catch (Exception e) {
			DAOExtensionLogger.DAOExtensionLogger
					.debug("Exception in getting Socket Connection Details: " + e.getMessage());
			System.out.println("Exception in getting Socket Connection Details: " + e.getMessage());
		}

		return socketDetailsMap;
	}

	protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort, int flag)
			throws IOException, Exception {
		DAOExtensionLogger.DAOExtensionLogger.debug("In WF NG Execute : " + serverPort);
		try {
			if (serverPort.startsWith("33"))
				return WFCallBroker.execute(ipXML, jtsServerIP, Integer.parseInt(serverPort), 1);
			else
				return ngEjbClientCIFVer.makeCall(jtsServerIP, serverPort, "WebSphere", ipXML);
		} catch (Exception e) {
			DAOExtensionLogger.DAOExtensionLogger.debug("Exception Occured in WF NG Execute : " + e.getMessage());
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

			DAOExtensionLogger.DAOExtensionLogger.debug("userName "+ username);
			DAOExtensionLogger.DAOExtensionLogger.debug("SessionId "+ sessionID);

			socketServerIP=socketDetailsMap.get("SocketServerIP");
			DAOExtensionLogger.DAOExtensionLogger.debug("SocketServerIP "+ socketServerIP);
			socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			DAOExtensionLogger.DAOExtensionLogger.debug("SocketServerPort "+ socketServerPort);

	   		if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0))
	   		{

    			socket = new Socket(socketServerIP, socketServerPort);
    			socket.setSoTimeout(connection_timeout*1000);
    			out = socket.getOutputStream();
    			socketInputStream = socket.getInputStream();
    			dout = new DataOutputStream(out);
    			din = new DataInputStream(socketInputStream);
    			DAOExtensionLogger.DAOExtensionLogger.debug("Dout " + dout);
    			DAOExtensionLogger.DAOExtensionLogger.debug("Din " + din);

    			outputResponse = "";
    			
    			String History_tablename="";
    			if("DCC".equalsIgnoreCase(processInstanceID.substring(0, 3))){
    				History_tablename= "NG_DCC_XMLLOG_HISTORY";
    			}
    			else{
    					History_tablename= "NG_DAO_XMLLOG_HISTORY";
    			}
    			
    			
    			inputRequest = getRequestXML( cabinetName,sessionID ,processInstanceID, ws_name, username, sInputXML,History_tablename);


    			if (inputRequest != null && inputRequest.length() > 0)
    			{
    				int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
    				DAOExtensionLogger.DAOExtensionLogger.debug("RequestLen: "+inputRequestLen + "");
    				inputRequest = inputRequestLen + "##8##;" + inputRequest;
    				DAOExtensionLogger.DAOExtensionLogger.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
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
    				DAOExtensionLogger.DAOExtensionLogger.debug("OutputResponse: "+outputResponse);

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

				//DAOExtensionLogger.DAOExtensionLogger.debug("outputResponse "+outputResponse);
				return outputResponse;

    	 		}

    		else
    		{
    			DAOExtensionLogger.DAOExtensionLogger.debug("SocketServerIp and SocketServerPort is not maintained "+"");
    			DAOExtensionLogger.DAOExtensionLogger.debug("SocketServerIp is not maintained "+	socketServerIP);
    			DAOExtensionLogger.DAOExtensionLogger.debug(" SocketServerPort is not maintained "+	socketServerPort);
    			return "Socket Details not maintained";
    		}

		}

		catch (Exception e)
		{
			DAOExtensionLogger.DAOExtensionLogger.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
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
				DAOExtensionLogger.DAOExtensionLogger.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
				//printException(e);
			}
		}


	}

	private String getResponseXML(String cabinetName,String sJtsIp,String iJtsPort, String sessionId, String processInstanceID,String message_ID, int integrationWaitTime)
	{

		String outputResponseXML="";
		try
		{
			String QueryString ="";
			if("DCC".equalsIgnoreCase(processInstanceID.substring(0, 3))){
				QueryString ="select OUTPUT_XML from NG_DCC_XMLLOG_HISTORY with (nolock) where MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+processInstanceID+"'";
			}

		else
			 QueryString = "select OUTPUT_XML from NG_DAO_XMLLOG_HISTORY with (nolock) where MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+processInstanceID+"'";

			String responseInputXML =CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, sessionID);
			DAOExtensionLogger.DAOExtensionLogger.debug("Response APSelect InputXML: "+responseInputXML);

			int Loop_count=0;
			do
			{
				String responseOutputXML=CommonMethods.WFNGExecute(responseInputXML,sJtsIp,iJtsPort,1);
				DAOExtensionLogger.DAOExtensionLogger.debug("Response APSelect OutputXML: "+responseOutputXML);

			    XMLParser xmlParserSocketDetails= new XMLParser(responseOutputXML);
			    String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			    DAOExtensionLogger.DAOExtensionLogger.debug("ResponseMainCode: "+responseMainCode);



			    int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			    DAOExtensionLogger.DAOExtensionLogger.debug("ResponseTotalRecords: "+responseTotalRecords);

			    if (responseMainCode.equals("0") && responseTotalRecords > 0)
				{

					String responseXMLData=xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData =responseXMLData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

	        		XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
	        		//DAOExtensionLogger.DAOExtensionLogger.debug("ResponseXMLData: "+responseXMLData);

	        		outputResponseXML=xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
	        		//DAOExtensionLogger.DAOExtensionLogger.debug("OutputResponseXML: "+outputResponseXML);

	        		if("".equalsIgnoreCase(outputResponseXML)){
	        			outputResponseXML="Error";
	    			}
	        		break;
				}
			    Loop_count++;
			    Thread.sleep(1000);
			}
			while(Loop_count<integrationWaitTime);
			DAOExtensionLogger.DAOExtensionLogger.debug("integrationWaitTime: "+integrationWaitTime);

		}
		catch(Exception e)
		{
			DAOExtensionLogger.DAOExtensionLogger.debug("Exception occurred in outputResponseXML" + e.getMessage());
			outputResponseXML="Error";
		}

		return outputResponseXML;

	}
	
	private String getRequestXML(String cabinetName, String sessionId,
			String processInstanceID, String ws_name, String userName, StringBuilder sInputXML, String tablename)
	{
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("<APMQPUTGET_Input>");
		strBuff.append("<SessionId>" + sessionID + "</SessionId>");
		strBuff.append("<EngineName>" + cabinetName + "</EngineName>");
		strBuff.append("<XMLHISTORY_TABLENAME>"+tablename+"</XMLHISTORY_TABLENAME>");
		strBuff.append("<WI_NAME>" + processInstanceID + "</WI_NAME>");
		strBuff.append("<WS_NAME>" + ws_name + "</WS_NAME>");
		strBuff.append("<USER_NAME>" + userName + "</USER_NAME>");
		strBuff.append("<MQ_REQUEST_XML>");
		strBuff.append(sInputXML);
		strBuff.append("</MQ_REQUEST_XML>");
		strBuff.append("</APMQPUTGET_Input>");
		DAOExtensionLogger.DAOExtensionLogger.debug("GetRequestXML: "+ strBuff.toString());
		return strBuff.toString();
	}
}



