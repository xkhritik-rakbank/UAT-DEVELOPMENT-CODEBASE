/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM
Application				: RAK BPM Utility
Module					: RAOP Status
File Name				: WI_update_doneWI.java
Author 					: Ravindra Kumar	
Date (DD/MM/YYYY)		: 01/06/2022

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
 */


package com.newgen.DAO.ACC_CLO;

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

import sun.security.util.Length;

import com.newgen.DAO.AWB.DAO_AWB_Log;
import com.newgen.DAO.Prime.DAO_prime;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;


public class ACC_CLO implements Runnable
{

	private static NGEjbClient ngEjbClientCIFVer;

	static Map<String, String> ACC_CLO_ConfigParamMap= new HashMap<String, String>();

	int socketConnectionTimeout=0;
	int integrationWaitTime=0;
	int sleepIntervalInMin=0;
	public static int waitLoop=50;
	public static int loopCount=50;
	public static String fromMailID="";
	public static String toMailID = "";
	public static String mailSubject = "";
	public static String MailStr="";
	public static String jtsIP = "";
	public static String jtsPort = "";
	public String sessionID = "";
	@Override
	public void run()
	{
		String cabinetName = "";
		String queueID = "";

		try
		{
			DAO_ACC_CLO_log.setLogger();
			ngEjbClientCIFVer = NGEjbClient.getSharedInstance();

			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.error("Could not Read Config Properties [DAO ACC_CLO / CARD_BLOCK_CLOSE]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("JTSPORT: " + jtsPort);

			queueID = ACC_CLO_ConfigParamMap.get("queueID");
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("QueueID: " + queueID);

			socketConnectionTimeout=Integer.parseInt(ACC_CLO_ConfigParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(ACC_CLO_ConfigParamMap.get("INTEGRATION_WAIT_TIME"));
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(ACC_CLO_ConfigParamMap.get("SleepIntervalInMin"));
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("SleepIntervalInMin: "+sleepIntervalInMin);


			fromMailID=ACC_CLO_ConfigParamMap.get("fromMailID");
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("fromMailID: "+fromMailID);

			toMailID=ACC_CLO_ConfigParamMap.get("toMailID");
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("toMailID: "+toMailID);

			mailSubject=ACC_CLO_ConfigParamMap.get("mailSubject");
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("mailSubject: "+mailSubject);

			MailStr=ACC_CLO_ConfigParamMap.get("MailStr");
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("MailStr: "+MailStr);


			sessionID = CommonConnection.getSessionID(DAO_ACC_CLO_log.DAO_ACC_CLO_logger, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Could Not Connect to Server!");
			}
			else
			{
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Session ID found: " + sessionID);
				HashMap<String, String> socketDetailsMap = socketConnectionDetails(cabinetName, jtsIP, jtsPort, sessionID);
				while (true) {
					DAO_ACC_CLO_log.setLogger();
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("DAO ACC_CLO / CARD_BLOCK_CLOSE ....123.");
					startDAO_WI_Update(cabinetName, jtsIP, jtsPort, sessionID, queueID, socketConnectionTimeout, integrationWaitTime, socketDetailsMap);
					System.out.println("No More workitems to Process for DAO ACC_CLO_CARD_BLK_CLOSE, Sleeping!");
					Thread.sleep(sleepIntervalInMin * 60 * 1000);
				}
			}
		}

		catch(Exception e)
		{
			e.printStackTrace();
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.error("Exception Occurred in DAO ACC_CLO / CARD_BLOCK_CLOSE : "+e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.error("Exception Occurred in DAO ACC_CLO / CARD_BLOCK_CLOSE : "+result);
		}
	}

	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "ACC_CLO_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			{
				String name = (String) names.nextElement();
				ACC_CLO_ConfigParamMap.put(name, p.getProperty(name));
			}
		}
		catch (Exception e)
		{
			return -1 ;
		}
		return 0;
	}


	private void startDAO_WI_Update(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String queueID, 
			int socketConnectionTimeOut, int integrationWaitTime, HashMap<String, String> socketDetailsMap)
	{
		final String ws_name="ACC_CLOSE";

		try{

			final HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();
			//Validate Session ID
			sessionID  = CommonConnection.getSessionID(DAO_ACC_CLO_log.DAO_ACC_CLO_logger, false);

			if (sessionID == null || sessionID.equalsIgnoreCase("") || sessionID.equalsIgnoreCase("null"))
			{
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.error("Could Not Get Session ID "+sessionID);
				return;
			}

			//Fetch all Work-Items on given queueID.
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Fetching all Workitems on DAO ACC_CLO / CARD_BLOCK_CLOSE queue");
			System.out.println("Fetching all Workitems on DAO_WI_Update");
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionID, queueID);
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);

			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("DAO ACC_CLO / CARD_BLOCK_CLOSE WMFetchWorkList OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);

			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Number of workitems retrieved on DAO ACC_CLO / CARD_BLOCK_CLOSE: "+fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on DAO ACC_CLO / CARD_BLOCK_CLOSE: "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0)
			{
				for(int i=0; i<fetchWorkitemListCount; i++)
				{
					String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID=xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Current ProcessInstanceID: "+processInstanceID);

					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Processing Workitem: "+processInstanceID);
					System.out.println("\nProcessing Workitem: "+processInstanceID);

					String WorkItemID=xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Current WorkItemID: "+WorkItemID);

					String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Current EntryDateTime: "+entryDateTime);

					String ActivityName=xmlParserfetchWorkItemData.getValueOf("ActivityName");
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("ActivityName: "+ActivityName);

					String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("ActivityID: "+ActivityID);
					String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("ActivityType: "+ActivityType);
					String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("ProcessDefId: "+ProcessDefId);

					String DBQuery ="select ELITE_CRN,CardSerno,WI_name,ECRN_CRN from NG_DAO_PRIME_COURIER with (nolock) where WI_name='"+ processInstanceID +"'";

					String extTabDataIPXML =CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAO_ACC_CLO_log.DAO_ACC_CLO_logger, false));
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("extTabDataIPXML: " + extTabDataIPXML);
					String extTabDataOPXML = WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("extTabDataOPXML: " + extTabDataOPXML);
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

						// loop over the map to put value key pair.
						for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
						{
							CheckGridDataMap.put("ELITE_CRN", objWorkList.getVal("ELITE_CRN"));
							CheckGridDataMap.put("CardSerno", objWorkList.getVal("CardSerno"));
							CheckGridDataMap.put("WI_name", objWorkList.getVal("WI_name"));
							CheckGridDataMap.put("ECRN_CRN", objWorkList.getVal("ECRN_CRN"));
						}
					}				    	    
					String DBQuery_1 ="select is_Ntb,Is_prime,is_prime_req,WI_name,CIF,account_no,ECRN from NG_DAO_EXTTABLE with(nolock) where wi_name='"+ processInstanceID +"'";

					String extTabDataIPXML_1 =CommonMethods.apSelectWithColumnNames(DBQuery_1, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAO_ACC_CLO_log.DAO_ACC_CLO_logger, false));
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("extTabDataIPXML: " + extTabDataIPXML_1);
					String extTabDataOPXML_1 = WFNGExecute(extTabDataIPXML_1, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("extTabDataOPXML: " + extTabDataOPXML_1);

					XMLParser xmlParserData_1 = new XMLParser(extTabDataOPXML_1);
					int iTotalrec_1 = Integer.parseInt(xmlParserData_1.getValueOf("TotalRetrieved"));

					if (xmlParserData_1.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_1 > 0)
					{
						String xmlDataExtTab = xmlParserData_1.getNextValueOf("Record");
						xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
						NGXmlList objWorkList = xmlParserData_1.createList("Records", "Record");

						for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
						{
							CheckGridDataMap.put("is_prime_req", objWorkList.getVal("is_prime_req"));
							CheckGridDataMap.put("Is_prime", objWorkList.getVal("Is_prime"));
							CheckGridDataMap.put("is_Ntb", objWorkList.getVal("is_Ntb"));
							CheckGridDataMap.put("WI_name", objWorkList.getVal("WI_name"));
							CheckGridDataMap.put("CIF", objWorkList.getVal("CIF"));
							CheckGridDataMap.put("account_no", objWorkList.getVal("account_no"));
							CheckGridDataMap.put("ECRN", objWorkList.getVal("ECRN"));
							
						}
					}
					// change the activitynaME
					String DBQuery_2 ="select EntryDATETIME from WFINSTRUMENTTABLE  with(nolock) where ProcessInstanceID ='"+ processInstanceID +"' and ActivityName = 'WI_status_update'";
					
					String extTabDataIPXML_2 =CommonMethods.apSelectWithColumnNames(DBQuery_2, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DAO_ACC_CLO_log.DAO_ACC_CLO_logger, false));
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("extTabDataIPXML: " + extTabDataIPXML_2);
					String extTabDataOPXML_2 = WFNGExecute(extTabDataIPXML_2, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("extTabDataOPXML: " + extTabDataOPXML_2);

					XMLParser xmlParserData_2 = new XMLParser(extTabDataOPXML_2);
					int iTotalrec_2 = Integer.parseInt(xmlParserData_2.getValueOf("TotalRetrieved"));

					if (xmlParserData_2.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec_2 > 0)
					{
						String xmlDataExtTab = xmlParserData_2.getNextValueOf("Record");
						xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
						NGXmlList objWorkList = xmlParserData_2.createList("Records", "Record");

						for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
						{
							CheckGridDataMap.put("EntryDATETIME", objWorkList.getVal("EntryDATETIME"));
						}
					}

					String WI_name = CheckGridDataMap.get("WI_name");
					String EntryDATETIME = CheckGridDataMap.get("EntryDATETIME");
					String is_prime_req = CheckGridDataMap.get("is_prime_req");
					String is_Ntb = CheckGridDataMap.get("is_Ntb");

					String Is_prime = "";
					if(CheckGridDataMap.containsKey("Is_prime")){
						DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Is_prime");
						Is_prime = CheckGridDataMap.get("Is_prime");	
					}
					String ELITE_CRN = "";
					if(CheckGridDataMap.containsKey("ELITE_CRN")){
						DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("ELITE_CRN");
						ELITE_CRN = CheckGridDataMap.get("ELITE_CRN");	
					}
					String CardSerno = "";
					if(CheckGridDataMap.containsKey("CardSerno")){
						DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("CardSerno");
						CardSerno = CheckGridDataMap.get("CardSerno");	
					}
					String ntb="";
					if(CheckGridDataMap.containsKey("is_Ntb")){
						DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("is_Ntb");
						ntb = CheckGridDataMap.get("is_Ntb");	
					}
					String ECRN_CRN="";
					if(CheckGridDataMap.containsKey("ECRN_CRN")){
						DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("ECRN_CRN");
						ECRN_CRN = CheckGridDataMap.get("ECRN_CRN");	
					}
					String account_no="";
					if(CheckGridDataMap.containsKey("account_no")){
						DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("account_no");
						ECRN_CRN = CheckGridDataMap.get("account_no");	
					}
					String CIF="";
					if(CheckGridDataMap.containsKey("CIF")){
						DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("CIF");
						ECRN_CRN = CheckGridDataMap.get("CIF");	
					}
					String ECRN_from_ext="";
					if(CheckGridDataMap.containsKey("ECRN")){
						DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("ECRN");
						ECRN_from_ext = CheckGridDataMap.get("ECRN");	
					}
					
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("ELITE_CRN "+ELITE_CRN);
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("CardSerno "+CardSerno);
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("WI_name "+WI_name);
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("ECRN_CRN "+ECRN_CRN);
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("is_prime_req "+is_prime_req);
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Is_prime "+Is_prime);
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("is_Ntb "+ntb);
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("ECRN_from_ext "+ECRN_from_ext);
					

					// CARD BLOCK - DAO
					String decisionValue="";
					String attributesTag="";
					String ErrDesc = "";
					if( ECRN_from_ext != null || !"".equalsIgnoreCase(ECRN_from_ext))
					{
						String fileLocation=new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DAO_Integration")
						.append(System.getProperty("file.separator")).append("CardBlock.txt").toString();

						BufferedReader sbf=new BufferedReader(new FileReader(fileLocation));

						StringBuilder sb=new StringBuilder();
						String line=sbf.readLine();
						while(line!=null)
						{
							sb.append(line);
							sb.append(System.lineSeparator());
							line=sbf.readLine();
						}

						String Card_block = "";
						Card_block =  sb.toString().replace(">CRN<",">"+ECRN_CRN.trim()+"<").replace(">Card_Number_ID<",">"+CardSerno.trim()+"<");
						DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Card_block :"+Card_block);

						String integrationStatus="Success";
						StringBuilder finalString=new StringBuilder();
						finalString = finalString.append(Card_block);
						HashMap<String, String> socketConnectionMap =socketConnectionDetails(cabinetName, sJtsIp, iJtsPort, sessionID); 

						integrationStatus = socketConnection(cabinetName, CommonConnection.getUsername(), sessionID, sJtsIp, iJtsPort, processInstanceID, ws_name, 60, 65,socketConnectionMap, finalString);

						XMLParser xmlParserSocketDetails= new XMLParser(integrationStatus);
						DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug(" xmlParserSocketDetails : "+xmlParserSocketDetails);
						String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
						DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Return Code: "+return_code+ "WI: "+processInstanceID);
						String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
						DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("return_desc : "+return_desc+ "WI: "+processInstanceID);

						String MsgId ="";
						if (integrationStatus.contains("<MessageId>"))
							MsgId = xmlParserSocketDetails.getValueOf("MessageId");

						DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("MsgId : "+MsgId+" ACC CLO / CRD BLK for DAO WI: "+processInstanceID);
						if(return_code.equalsIgnoreCase("0000"))
						{
							integrationStatus="Success";
							ErrDesc = "Card Block Success";
							DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Card Block Done Successfully DAO : Going for Card close call");

							String fileLocation1=new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DAO_Integration")
							.append(System.getProperty("file.separator")).append("CardClosure.txt").toString();

							BufferedReader sbf_1=new BufferedReader(new FileReader(fileLocation1));

							StringBuilder sb_1=new StringBuilder();
							String line_1=sbf_1.readLine();
							while(line_1!=null)
							{
								sb_1.append(line_1);
								sb_1.append(System.lineSeparator());
								line_1=sbf_1.readLine();
							}

							SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
							String date = formatter.format(new Date());

							String Card_Close = "";
							Card_Close =  sb_1.toString().replace(">date<",">"+date.trim()+"<").replace(">Card_Number_ID<",">"+CardSerno.trim()+"<");
							DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Card_Close :"+Card_Close);

							finalString = finalString.append(Card_Close);

							integrationStatus = socketConnection(cabinetName, CommonConnection.getUsername(), sessionID, sJtsIp, iJtsPort, processInstanceID, ws_name, 60, 65,socketConnectionMap, finalString);

							XMLParser xmlParserSocketDetails_card_clo= new XMLParser(integrationStatus);
							DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug(" xmlParserSocketDetails : "+xmlParserSocketDetails_card_clo);
							String return_code_card_clo = xmlParserSocketDetails_card_clo.getValueOf("ReturnCode");
							DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Return Code: "+return_code_card_clo+ "WI: "+processInstanceID);
							String return_desc_crd_clo = xmlParserSocketDetails_card_clo.getValueOf("ReturnDesc");
							DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("return_desc : "+return_desc_crd_clo+ "WI: "+processInstanceID);

							String MsgId_card_clo="";
							if (integrationStatus.contains("<MessageId>"))
								MsgId_card_clo = xmlParserSocketDetails_card_clo.getValueOf("MessageId");

							DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("MsgId_card_clo : "+MsgId_card_clo+" ACC CLO / CRD CLO for DAO WI: "+processInstanceID);

							if(return_code_card_clo.equalsIgnoreCase("0000"))
							{
								integrationStatus="Success";
								ErrDesc = "Card Block and Card Close Success";
								DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Card Block and Card Close Done Successfully DAO");


							}
							if ("Success".equalsIgnoreCase(integrationStatus))
							{
								decisionValue = "Approve";
								DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Decision in success: " +decisionValue);
								attributesTag="<Decision>"+decisionValue+"</Decision>";
							}
							else
							{
								DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("CARD CLOSE FAIL DAO");
								ErrDesc = return_desc_crd_clo;//integrationStatus.replace("~", ",").replace("|", "\n");
								decisionValue = "Fail";
								DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Decision in else : " +decisionValue);
								attributesTag="<Decision>"+decisionValue+"</Decision>";

								sendMail(cabinetName,sessionID,processInstanceID,jtsIP,jtsPort,ErrDesc,return_code_card_clo,ProcessDefId);

								doneworkitem(cabinetName,sJtsIp,iJtsPort,sessionID,queueID,
								socketConnectionTimeOut,integrationWaitTime,
								socketDetailsMap,processInstanceID,WorkItemID,ActivityID,
								ProcessDefId,ActivityType,decisionValue,ActivityName,EntryDATETIME,attributesTag);

								continue;
							}
						}
						if ("Success".equalsIgnoreCase(integrationStatus))
						{
							decisionValue = "Approve";
							DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Decision in success: " +decisionValue);
							attributesTag="<Decision>"+decisionValue+"</Decision>";
						}
						else
						{	
							DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("CARD BLOCK FAIL DAO");
							ErrDesc = return_desc;//integrationStatus.replace("~", ",").replace("|", "\n");
							decisionValue = "Fail";
							DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Decision in else : " +decisionValue);
							attributesTag="<Decision>"+decisionValue+"</Decision>";

							sendMail(cabinetName,sessionID,processInstanceID,jtsIP,jtsPort,ErrDesc,return_code,ProcessDefId);
						}
					}
					
					String fileLocation2=new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DAO_Integration")
					.append(System.getProperty("file.separator")).append("CardClosure.txt").toString();

					BufferedReader sbf_2=new BufferedReader(new FileReader(fileLocation2));

					StringBuilder sb_2=new StringBuilder();
					String line_2=sbf_2.readLine();
					while(line_2!=null)
					{
						sb_2.append(line_2);
						sb_2.append(System.lineSeparator());
						line_2=sbf_2.readLine();
					}

					String Acc_Close = "";
					Acc_Close =  sb_2.toString().replace(">CIF<",">"+CIF.trim()+"<").replace(">ACC_NO<",">"+account_no.trim()+"<");
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Acc_Close :"+Acc_Close);
					
					StringBuilder finalString=new StringBuilder();
					finalString = finalString.append(Acc_Close);

					String integrationStatus_acc_clo="Success";
					HashMap<String, String> socketConnectionMap =socketConnectionDetails(cabinetName, sJtsIp, iJtsPort, sessionID); 
					integrationStatus_acc_clo = socketConnection(cabinetName, CommonConnection.getUsername(), sessionID, sJtsIp, iJtsPort, processInstanceID, ws_name, 60, 65,socketConnectionMap, finalString);

					XMLParser xmlParserSocketDetails_Acc_Close= new XMLParser(integrationStatus_acc_clo);
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug(" xmlParserSocketDetails : "+xmlParserSocketDetails_Acc_Close);
					String return_code_Acc_Close = xmlParserSocketDetails_Acc_Close.getValueOf("ReturnCode");
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Return Code: "+return_code_Acc_Close+ "WI: "+processInstanceID);
					String return_desc_Acc_Close = xmlParserSocketDetails_Acc_Close.getValueOf("ReturnDesc");
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("return_desc : "+return_desc_Acc_Close+ "WI: "+processInstanceID);
					String MsgId_Acc_Close="";
					if (integrationStatus_acc_clo.contains("<MessageId>"))
						MsgId_Acc_Close = xmlParserSocketDetails_Acc_Close.getValueOf("MessageId");

					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("MsgId_card_clo : "+MsgId_Acc_Close+" ACC CLO / CRD CLO for DAO WI: "+processInstanceID);

					if(return_code_Acc_Close.equalsIgnoreCase("0000"))
					{
						integrationStatus_acc_clo="Success";
						ErrDesc = "Account Closure Success";
						DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Card Block, Card Close and Account Closure Done Successfully DAO");
					}

					if ("Success".equalsIgnoreCase(integrationStatus_acc_clo))
					{
						decisionValue = "Approve";
						DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Decision in success: " +decisionValue);
						attributesTag="<Decision>"+decisionValue+"</Decision>";
					}
					else
					{
						DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("CARD CLOSE FAIL DAO");
						ErrDesc = return_desc_Acc_Close; //integrationStatus.replace("~", ",").replace("|", "\n");
						decisionValue = "Fail";
						DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Decision in else : " +decisionValue);
						attributesTag="<Decision>"+decisionValue+"</Decision>";

						String ErrorCode="";
						String ErrorDesc="";
						xmlParserSocketDetails_Acc_Close.getValueOf("ErrorCode");
						xmlParserSocketDetails_Acc_Close.getValueOf("ErrorDesc");
						DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("ErrorCode : "+ErrorCode+ "WI: "+processInstanceID);
						DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("ErrorDesc : "+ErrorDesc+ "WI: "+processInstanceID);

						String[] array_ErrorCode = ErrorCode.split(",");
						String[] array_ErrorDesc = ErrorDesc.split(",");
						DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("array_ErrorCode : "+array_ErrorCode+ "WI: "+processInstanceID);
						DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("array_ErrorDesc : "+array_ErrorDesc+ "WI: "+processInstanceID);

						for(int i1 = 0;i1<array_ErrorCode.length;i1++)
						{
							String input = "'"+array_ErrorCode[i]+"','"+array_ErrorDesc[i]+"','"+processInstanceID+"'";
							DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("input : "+input+ "WI: "+processInstanceID);

							String columnNames="ErrorCode,ErrorDesc,wi_name";
							String columnValues=input;

							String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionID, columnNames, columnValues,"NG_DAO_GR_ACC_CLO_EXCEP");
							DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("APInsertInputXML NG_DAO_GR_ACC_CLO_EXCEP : "+apInsertInputXML);

							String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
							DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("APInsertOutputXML NG_DAO_GR_ACC_CLO_EXCEP: "+ apInsertInputXML);

							XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
							String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
							DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Status of apInsertMaincode NG_DAO_GR_ACC_CLO_EXCEP  "+ apInsertMaincode);

							DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Completed On "+ ActivityName);

							if("11".equalsIgnoreCase(apInsertMaincode)){

								sessionID  = CommonConnection.getSessionID(DAO_ACC_CLO_log.DAO_ACC_CLO_logger, false);
								DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("new sessionId:  "+ sessionID);
								apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
								DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("APInsertOutputXML: "+ apInsertInputXML);

								xmlParserAPInsert = new XMLParser(apInsertOutputXML);
								apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
								DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Status of apInsertMaincode  "+ apInsertMaincode);
							}

							if(apInsertMaincode.equalsIgnoreCase("0"))
							{
								DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("ApInsert successful: "+apInsertMaincode);
								DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Inserted in NG_DAO_GR_ACC_CLO_EXCEP table successfully.");
							}
							else
							{
								DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("ApInsert failed NG_DAO_GR_ACC_CLO_EXCEP: "+apInsertMaincode);
							}
						}

						sendMail(cabinetName,sessionID,processInstanceID,jtsIP,jtsPort,ErrDesc,return_code_Acc_Close,ProcessDefId);

						doneworkitem(cabinetName,sJtsIp,iJtsPort,sessionID,queueID,
						socketConnectionTimeOut,integrationWaitTime,
						socketDetailsMap,processInstanceID,WorkItemID,ActivityID,
						ProcessDefId,ActivityType,decisionValue,ActivityName,EntryDATETIME,attributesTag);
					
						continue;
					}
				}
			}
		}
		catch (Exception e)
		{
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Exception ACC_CLO : "+e.getMessage());
		}
	}

	private HashMap<String, String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort, String sessionID) {
		HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

		try {
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Fetching Socket Connection Details.");
			System.out.println("Fetching Socket Connection Details.");

			String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'DigitalAO' and CallingSource = 'Utility'";

			String socketDetailsInputXML = CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Socket Details APSelect InputXML: " + socketDetailsInputXML);

			String socketDetailsOutputXML = WFNGExecute(socketDetailsInputXML, sJtsIp, iJtsPort, 1);
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Socket Details APSelect OutputXML: " + socketDetailsOutputXML);

			XMLParser xmlParserSocketDetails = new XMLParser(socketDetailsOutputXML);
			String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("SocketDetailsMainCode: " + socketDetailsMainCode);

			int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("SocketDetailsTotalRecords: " + socketDetailsTotalRecords);

			if (socketDetailsMainCode.equalsIgnoreCase("0") && socketDetailsTotalRecords > 0) {
				String xmlDataSocketDetails = xmlParserSocketDetails.getNextValueOf("Record");
				xmlDataSocketDetails = xmlDataSocketDetails.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

				XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

				String socketServerIP = xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("SocketServerIP: " + socketServerIP);
				socketDetailsMap.put("SocketServerIP", socketServerIP);

				String socketServerPort = xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("SocketServerPort " + socketServerPort);
				socketDetailsMap.put("SocketServerPort", socketServerPort);

				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("SocketServer Details found.");
				System.out.println("SocketServer Details found.");
			}
		} catch (Exception e) {
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger
			.debug("Exception in getting Socket Connection Details: " + e.getMessage());
			System.out.println("Exception in getting Socket Connection Details: " + e.getMessage());
		}

		return socketDetailsMap;
	}

	protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort, int flag)
	throws IOException, Exception {
		DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("In WF NG Execute : " + serverPort);
		try {
			if (serverPort.startsWith("33"))
				return WFCallBroker.execute(ipXML, jtsServerIP, Integer.parseInt(serverPort), 1);
			else
				return ngEjbClientCIFVer.makeCall(jtsServerIP, serverPort, "WebSphere", ipXML);
		} catch (Exception e) {
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Exception Occured in WF NG Execute : " + e.getMessage());
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

		try{

			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("userName "+ username);
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("SessionId "+ sessionID);

			socketServerIP=socketDetailsMap.get("SocketServerIP");
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("SocketServerIP "+ socketServerIP);
			socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("SocketServerPort "+ socketServerPort);

			if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0)){

				socket = new Socket(socketServerIP, socketServerPort);
				socket.setSoTimeout(connection_timeout*1000);
				out = socket.getOutputStream();
				socketInputStream = socket.getInputStream();
				dout = new DataOutputStream(out);
				din = new DataInputStream(socketInputStream);
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Dout " + dout);
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Din " + din);

				outputResponse = "";

				inputRequest = getRequestXML( cabinetName,sessionID ,processInstanceID, ws_name, username, sInputXML);

				if (inputRequest != null && inputRequest.length() > 0){

					int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("RequestLen: "+inputRequestLen + "");
					inputRequest = inputRequestLen + "##8##;" + inputRequest;
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
					dout.write(inputRequest.getBytes("UTF-16LE"));dout.flush();
				}
				byte[] readBuffer = new byte[500];
				int num = din.read(readBuffer);
				if (num > 0){

					byte[] arrayBytes = new byte[num];
					System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
					outputResponse = outputResponse+ new String(arrayBytes, "UTF-16LE");
					inputMessageID = outputResponse;
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("OutputResponse: "+outputResponse);

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
				return outputResponse;
			}
			else
			{
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("SocketServerIp and SocketServerPort is not maintained "+"");
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("SocketServerIp is not maintained "+	socketServerIP);
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug(" SocketServerPort is not maintained "+	socketServerPort);
				return "Socket Details not maintained";
			}
		}

		catch (Exception e)
		{
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
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
			catch(Exception e){

				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
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
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Response APSelect InputXML: "+responseInputXML);

			int Loop_count=0;
			do
			{
				String responseOutputXML=CommonMethods.WFNGExecute(responseInputXML,sJtsIp,iJtsPort,1);
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Response APSelect OutputXML: "+responseOutputXML);

				XMLParser xmlParserSocketDetails= new XMLParser(responseOutputXML);
				String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("ResponseMainCode: "+responseMainCode);



				int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("ResponseTotalRecords: "+responseTotalRecords);

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
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("integrationWaitTime: "+integrationWaitTime);

		}
		catch(Exception e)
		{
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Exception occurred in outputResponseXML" + e.getMessage());
			outputResponseXML="Error";
		}

		return outputResponseXML;

	}
	public void doneworkitem(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String queueID, 
			int socketConnectionTimeOut, int integrationWaitTime, HashMap<String, String> socketDetailsMap,String processInstanceID,String WorkItemID,String ActivityID,
			String ProcessDefId,String ActivityType,String decisionValue, String ActivityName,String EntryDATETIME ,
			String attributesTag)
	{
		try{
			//Lock Workitem.
			String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID, WorkItemID);
			String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML,sJtsIp,iJtsPort,1);
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Output XML For WmgetWorkItemCall: " + getWorkItemOutputXml);

			XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
			String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);

			if (getWorkItemMainCode.trim().equals("0"))
			{
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);

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

				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("InputXML for assignWorkitemAttribute Call Notify: "+assignWorkitemAttributeInputXML);

				String assignWorkitemAttributeOutputXML=WFNGExecute(assignWorkitemAttributeInputXML,sJtsIp,
						iJtsPort,1);

				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("OutputXML for assignWorkitemAttribute Call Notify: "+assignWorkitemAttributeOutputXML);

				XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
				String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("AssignWorkitemAttribute MainCode: "+assignWorkitemAttributeMainCode);

				if(assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0"))
				{
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("AssignWorkitemAttribute Successful: "+assignWorkitemAttributeMainCode);
					System.out.println(processInstanceID + "Complete Succesfully with status "+decisionValue);
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("WorkItem moved to next Workstep.");
				}
				else
				{
					String ErrDesc = "Done WI Failed";
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("decisionValue : "+decisionValue);
					String return_code = assignWorkitemAttributeMainCode.trim();
					//start
					sendMail(cabinetName,sessionID,processInstanceID,jtsIP,jtsPort,ErrDesc,return_code,ProcessDefId);
				}

				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

				Date current_date = new Date();
				String formattedEntryDatetime=dateFormat.format(current_date);
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);

				// parse to change and store and reflect as txt box on front end.
				Date d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(EntryDATETIME);
				String entrydatetime_format = dateFormat.format(d1);
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("EntryDATETIME: "+entrydatetime_format);

				String columnNames="wi_name,decision_date_time,workstep,user_name,Decision,Remarks,entry_date_time";
				String columnValues="'"+processInstanceID+"','"+formattedEntryDatetime+"','"+ActivityName+"','"
				+CommonConnection.getUsername()+"','"+decisionValue+"','','"+entrydatetime_format+"'";

				String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionID, columnNames, columnValues,"NG_DAO_GR_DECISION_HISTORY");
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("APInsertInputXML: "+apInsertInputXML);

				String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("APInsertOutputXML: "+ apInsertInputXML);

				XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
				String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Completed On "+ ActivityName);


				if(apInsertMaincode.equalsIgnoreCase("0"))
				{
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("ApInsert successful: "+apInsertMaincode);
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Inserted in WiHistory table successfully.");
				}
				else
				{
					DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("ApInsert failed: "+apInsertMaincode);
				}

			}
			else
			{
				getWorkItemMainCode="";
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("WmgetWorkItem failed: "+getWorkItemMainCode);
				String ErrDesc = "WI Failed";
				sendMail(cabinetName,sessionID,processInstanceID,jtsIP,jtsPort,ErrDesc,getWorkItemMainCode,ProcessDefId);
			}
		}
		catch(Exception e)
		{
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("WmgetWorkItem Exception: "+e.getMessage());
		}
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
		DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("GetRequestXML: "+ strBuff.toString());
		return strBuff.toString();
	}
	public void sendMail(String cabinetName, String sessionId ,String wiName,String jtsIp,String jtsPort,String ErrDesc, String return_code,String ProcessDefId)throws Exception
	{
		XMLParser objXMLParser = new XMLParser();
		String sInputXML="";
		String sOutputXML="";
		String mainCodeforAPInsert=null;
		int sessionCheckInt = 0;
		while(sessionCheckInt<loopCount)
		{
			try
			{
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("workitem name to send mail---"+wiName);
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("ErrorMsg to send mail---"+ErrDesc);
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("return_code to send mail---"+return_code);

				String FinalMailStr = MailStr.toString().replace("<WI_NAME>",wiName).replace("<ret_Code>",return_code)
				.replace("<errormsg>",ErrDesc);
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("finalbody: "+FinalMailStr);

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
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Mail Insert InputXml::::::::::\n"+sInputXML);
				sOutputXML =WFNGExecute(sInputXML, jtsIp,jtsPort,0);
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Mail Insert OutputXml::::::::::\n"+sOutputXML);
				objXMLParser.setInputXML(sOutputXML);
				mainCodeforAPInsert=objXMLParser.getValueOf("MainCode");

			}

			catch(Exception e)
			{
				e.printStackTrace();
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.error("Exception in Sending mail", e);
				sessionCheckInt++;
				waiteloopExecute(waitLoop);
				continue;
			}
			if (mainCodeforAPInsert.equalsIgnoreCase("11")) 
			{
				DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("Invalid session in Sending mail");
				sessionCheckInt++;
				//ThreadConnect.sessionId = ThreadConnect.getSessionID(cabinetName, jtsIP, jtsPort, userName,password);
				sessionID=CommonConnection.getSessionID(DAO_ACC_CLO_log.DAO_ACC_CLO_logger, false);
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
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("mail Insert Successful");
			System.out.println("Mail Insert Successful for "+wiName+" in table WFMAILQUEUETABLE");
		}
		else
		{
			DAO_ACC_CLO_log.DAO_ACC_CLO_logger.debug("mail Insert Unsuccessful");
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
}



