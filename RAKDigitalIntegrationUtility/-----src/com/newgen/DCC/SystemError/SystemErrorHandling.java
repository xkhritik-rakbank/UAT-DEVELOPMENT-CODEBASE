/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM
Application				: RAK BPM Utility
Module					: RAOP Status
File Name				: RAOPStatus.java
Author 					: Shubham Gupta
Date (DD/MM/YYYY)		: 15/06/2019

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/


package com.newgen.DCC.SystemError;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;



public class SystemErrorHandling implements Runnable
{

	private static NGEjbClient ngEjbClient;
	
	static Map<String, String> configParamMap = new HashMap<String, String>();

	private String EXT_TABLE = "NG_DCC_EXTTABLE";
	
	@Override
	public void run()
	{
		String sessionID = "";
		String cabinetName = "";
		String jtsIP = "";
		String jtsPort = "";
		String queueID = "";
		String UserName = "";
		int socketConnectionTimeout=0;
		int integrationWaitTime=0;
		int sleepIntervalInMin=0;
		int TrialTime = 0;
		String RouteAll = "";
		int ErrorCount = 0;
		try
		{
			SystemErrorHandlingLog.setLogger();
			ngEjbClient = NGEjbClient.getSharedInstance();

			SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				SystemErrorHandlingLog.SystemErrorHandlingLogger.error("Could not Read Config Properties [DAO_prime]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("JTSPORT: " + jtsPort);

			queueID = configParamMap.get("queueID");
			SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("QueueID: " + queueID);
			
			UserName = configParamMap.get("UserName");
			SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("UserName: " + UserName);

			socketConnectionTimeout=Integer.parseInt(configParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(configParamMap.get("INTEGRATION_WAIT_TIME"));
			SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(configParamMap.get("SleepIntervalInMin"));
			SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);
			
			TrialTime=Integer.parseInt(configParamMap.get("TrialTime"));
			SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("TrialTime: "+TrialTime);
			
			RouteAll = configParamMap.get("RouteAll");
			SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("RouteAll: "+RouteAll);
			
			String ErrorCountStr = configParamMap.get("ErrorCount") == null ? "0" : configParamMap.get("ErrorCount");
			ErrorCount=Integer.parseInt(ErrorCountStr);
			SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("ErrorCount: "+ErrorCount);
			
			sessionID = CommonConnection.getSessionID(SystemErrorHandlingLog.SystemErrorHandlingLogger, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Could Not Connect to Server!");
			}
			else
			{
				HashMap<String, String> socketDetailsMap = CommonMethods.socketConnectionDetails(cabinetName, jtsIP, jtsPort, sessionID);
				while (true) {
					SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Session ID found: " + sessionID);
					SystemErrorHandlingLog.setLogger();
					
					startSystemErrorUtility(cabinetName, UserName, jtsIP, jtsPort, sessionID, queueID, socketConnectionTimeout, integrationWaitTime, socketDetailsMap, TrialTime, RouteAll,
					ErrorCount);
					
					Thread.sleep(sleepIntervalInMin*60*1000);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			SystemErrorHandlingLog.SystemErrorHandlingLogger.error("Exception Occurred in DAO Prime CBS  : " + e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			SystemErrorHandlingLog.SystemErrorHandlingLogger.error("Exception Occurred in DAO Prime CBS  : " + result);
		}
	}
	
	private void startSystemErrorUtility(String cabinetName, String UserName, String sJtsIp, String iJtsPort, String sessionId, String queueID, 
			int socketConnectionTimeOut, int integrationWaitTime, HashMap<String, String> socketDetailsMap, int trialTime, String routeAll, int errorCount)
	{
		final String ws_name="Sys_Error_Handling";
		try
		{
			//Validate Session ID
			sessionId  = CommonConnection.getSessionID(SystemErrorHandlingLog.SystemErrorHandlingLogger, false);

			if(sessionId==null || sessionId.equalsIgnoreCase("") || sessionId.equalsIgnoreCase("null"))
			{
				SystemErrorHandlingLog.SystemErrorHandlingLogger.error("Could Not Get Session ID "+sessionId);
				return;
			}

			//Fetch all Work-Items on given queueID.
			SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Fetching all Workitems on System_Error queue");
			System.out.println("Fetching all Workitems on System_Error queue");
			
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionId, queueID);
			SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= CommonMethods.WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);
			SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("WMFetchWorkList OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);
			SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Number of workitems retrieved on System_Error: "+fetchWorkitemListCount);
			System.out.println("Number of workitems retrieved on System_Error: "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0)
			{
				for(int i=0; i<fetchWorkitemListCount; i++)
				{
					String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

					SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID=xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Current ProcessInstanceID: "+processInstanceID);

					SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Processing Workitem: "+processInstanceID);
					System.out.println("\nProcessing Workitem: "+processInstanceID);

					String WorkItemID=xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Current WorkItemID: "+WorkItemID);

					String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Current EntryDateTime: "+entryDateTime);

					String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("ActivityID: "+ActivityID);

					String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("ActivityType: "+ActivityType);

					String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("ProcessDefId: "+ProcessDefId);
					
					String DBQuery = "SELECT entry_date_time, prev_error_ws, error_count FROM NG_DCC_EXTTABLE with(nolock) WHERE WI_NAME='" + processInstanceID + "'";
			        
			        String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(SystemErrorHandlingLog.SystemErrorHandlingLogger, false));
			        SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("extTabDataIPXML: " + extTabDataIPXML);
			        String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
			        SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("extTabDataOPXML: " + extTabDataOPXML);

			        XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
			        int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
			        if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0) {
			        	  String xmlDataExtTab = xmlParserData.getNextValueOf("Record");
			              xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

			              NGXmlList objWorkList = xmlParserData.createList("Records", "Record");
			              SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("entry_date_time : " + objWorkList.getVal("entry_date_time"));
			              SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("prev_error_ws: " + objWorkList.getVal("prev_error_ws"));
			              SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("error_count: " + objWorkList.getVal("error_count"));
			              Integer error_count_wi  = 0;
			              if (objWorkList.getVal("error_count") != null && !objWorkList.getVal("error_count").equals(""))
			            	  error_count_wi =Integer.parseInt(objWorkList.getVal("error_count"));
			              
			              /** TODO check condition if (entry_date_time_time+ trry time < current time || route_all == "Y") && error_count < tried how many times (keep field in config file.)
			               * , int trialTime, String routeAll, int errorCount
			               * route to prev_error_ws  **/
			              String entry_date_time_str = objWorkList.getVal("entry_date_time"); // entry date time
			              long entry_date_time = 0l;
			              if (entry_date_time_str == null || entry_date_time_str.equals("")) {
			            	  entry_date_time = new Date().getTime();
			              } else {
			            	  entry_date_time = convertDateToLong(entry_date_time_str) + trialTime * 1000l; // 30 minutes
			              }
			              
			              long current_date = new Date().getTime();
			              if ((configParamMap.get("RouteAll").equalsIgnoreCase("Y") || current_date > entry_date_time) && error_count_wi <= errorCount) {
			            	
							//Lock Workitem
							String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID,WorkItemID);
							String getWorkItemOutputXml = CommonMethods.WFNGExecute(getWorkItemInputXML,sJtsIp,iJtsPort,1);
							SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Output XML For WmgetWorkItemCall: "+ getWorkItemOutputXml);
	
							XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
							String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
							SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);
							if (getWorkItemMainCode.trim().equals("0"))
							{
								SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);
	
								String attributesTag = "<Decision>" + "Retry" + "</Decision>";

								SystemErrorHandlingLog.SystemErrorHandlingLogger.info("get Workitem call successfull for "+processInstanceID);
	
								String assignWorkitemAttributeInputXML = "<?xml version=\"1.0\"?><WMAssignWorkItemAttributes_Input>"
										+ "<Option>WMAssignWorkItemAttributes</Option>"
										+ "<EngineName>"+CommonConnection.getCabinetName()+"</EngineName>"
										+ "<SessionId>"+CommonConnection.getSessionID(SystemErrorHandlingLog.SystemErrorHandlingLogger, false)+"</SessionId>"
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
								
								SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Input XML for assign Attribute is "+assignWorkitemAttributeInputXML);
	
								String assignWorkitemAttributeOutputXML = CommonMethods.WFNGExecute(assignWorkitemAttributeInputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
								SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Output XML for assign Attribues is " + assignWorkitemAttributeOutputXML);

								XMLParser xmlParserAssignAtt=new XMLParser(assignWorkitemAttributeOutputXML);
	
								String mainCodeAssignAtt=xmlParserAssignAtt.getValueOf("MainCode");
								if("0".equals(mainCodeAssignAtt.trim()))
								{
									String inputXmlStr = CommonMethods.completeWorkItemInput(cabinetName, sessionId, processInstanceID, WorkItemID);
									SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Output XML for complete workitem "+inputXmlStr);
									String outputXmlStr= CommonMethods.WFNGExecute(inputXmlStr,CommonConnection.getJTSIP(),
											CommonConnection.getJTSPort(),1);
									SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Output XML for complete workitem "+outputXmlStr);
									
									XMLParser xmlParserCompleteWi=new XMLParser(outputXmlStr);
									String mainCodeCompleteWi=xmlParserCompleteWi.getValueOf("MainCode");
									if("0".equals(mainCodeCompleteWi.trim()))
									{
									SystemErrorHandlingLog.SystemErrorHandlingLogger.error("Successfully route to previous WS : " + processInstanceID);
									}
									else
									{
										SystemErrorHandlingLog.SystemErrorHandlingLogger.error("Error in route to previous WS "+processInstanceID);
									}
								}
								else
								{
									SystemErrorHandlingLog.SystemErrorHandlingLogger.error("Error in route to previous WS "+processInstanceID);
								}					
							}
							else
							{
								//entry_date_time, prev_error_ws, error_ws, error_count
								error_count_wi += 1;
								String current_date_time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Calendar.getInstance().getTime());
								String columnNames = "error_count, entry_date_time";
								String columnValues = "'" + error_count_wi + "', '" + current_date_time +"'";
								String sWhereClause = "WI_NAME = '" + processInstanceID + "'";

								String extTableIPUpdateXml = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(SystemErrorHandlingLog.SystemErrorHandlingLogger, false), EXT_TABLE, columnNames, columnValues, sWhereClause);
								SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Input XML for apUpdateInput for " + EXT_TABLE + " Table : " + extTableIPUpdateXml);

								String extTableOPUpdateXml = CommonMethods.WFNGExecute(extTableIPUpdateXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
								SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Output XML for apUpdateInput for " + EXT_TABLE + " Table : " + extTableOPUpdateXml);

								XMLParser sXMLParserChild = new XMLParser(extTableOPUpdateXml);
								String StrMainCode = sXMLParserChild.getValueOf("MainCode");

								if (StrMainCode.equals("0"))
									SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Successful in apUpdateInput the record in : " + EXT_TABLE);
								else {
									SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Error in Executing apUpdateInput sOutputXML : " + extTabDataOPXML);
									System.out.println("WMgetWorkItemCall failed: "+processInstanceID);
									SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("WMgetWorkItemCall failed: "+processInstanceID);
								}
							}
				         }
					} else
					{
						SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("WI NOT FOUND : "+processInstanceID);
					}
				}
			}
		}
		catch (Exception e)
		{
			SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Exception: "+e.getMessage());
		}
	}

	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "DCC_Error_Handling_Logger.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			  {
			    String name = (String) names.nextElement();
			    configParamMap.put(name, p.getProperty(name));
			  }
		    }
		catch (Exception e)
		{
			return -1 ;
		}
		return 0;
	}
	
	
	// date format should be in yyyy-MM-dd hh:mm:ss format.
	public static long convertDateToLong(String date)
	{
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date date_long = null;
		try {
			date_long = simpleDateFormat.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date_long.getTime();
	}
	

	protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort, int flag)
			throws IOException, Exception {
		SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("In WF NG Execute : " + serverPort);
		try {
			if (serverPort.startsWith("33"))
				return WFCallBroker.execute(ipXML, jtsServerIP, Integer.parseInt(serverPort), 1);
			else
				return ngEjbClient.makeCall(jtsServerIP, serverPort, "WebSphere", ipXML);
		} catch (Exception e) {
			SystemErrorHandlingLog.SystemErrorHandlingLogger.debug("Exception Occured in WF NG Execute : " + e.getMessage());
			e.printStackTrace();
			return "Error";
		}
	}

}



