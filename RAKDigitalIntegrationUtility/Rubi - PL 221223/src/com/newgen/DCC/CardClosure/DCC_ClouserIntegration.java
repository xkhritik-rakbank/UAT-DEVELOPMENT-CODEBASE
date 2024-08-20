/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM
Application				: RAK BPM Utility
Module					: DCC Card Balance check, Clouser, Block
File Name				: RAOPStatus.java
Author 					: Ravindra Kumar	
Date (DD/MM/YYYY)		: 01/06/2022

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/

package com.newgen.DCC.CardClosure;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
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
import com.newgen.DCC.Final_Limit_Increase.DCC_FINAL_LIMIT_LOG;
import com.newgen.DCC.EFMS.DCC_EFMS_IntegrationLog;
import com.newgen.DCC.Notify.DCC_Notify_CAPS;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.XMLParser;;

public class DCC_ClouserIntegration implements Runnable {
	static Map<String, String> configParamMap = new HashMap<String, String>();
	HashMap<String, String> socketConnectionMap = null;
	int socketConnectionTimeout = 0;
	int integrationWaitTime = 0;
	int sleepIntervalInMin = 0;
	String sessionID = "";
	String cabinetName = "";
	String jtsIP = "";
	String jtsPort = "";
	String queueID = "";
	String entryDateTime = "";
	String ActivityID = "";
	
	@Override
	public void run() {
		
		try {
			DCC_CardClosureLog.setLogger();

			DCC_CardClosureLog.DCC_CardClosureLogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			DCC_CardClosureLog.DCC_CardClosureLogger.debug("configReadStatus " + configReadStatus);
			if (configReadStatus != 0) {
				DCC_CardClosureLog.DCC_CardClosureLogger.error("Could not Read Config Properties [DCCNotifyAPP]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("JTSPORT: " + jtsPort);

			queueID = configParamMap.get("queueID");
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("QueueID: " + queueID);

			socketConnectionTimeout = Integer.parseInt(configParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("SocketConnectionTimeOut: " + socketConnectionTimeout);

			integrationWaitTime = Integer.parseInt(configParamMap.get("INTEGRATION_WAIT_TIME"));
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("IntegrationWaitTime: " + integrationWaitTime);

			sleepIntervalInMin = Integer.parseInt(configParamMap.get("SleepIntervalInMin"));
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("SleepIntervalInMin: " + sleepIntervalInMin);

			sessionID = CommonConnection.getSessionID(DCC_CardClosureLog.DCC_CardClosureLogger, false);

			if (sessionID.trim().equalsIgnoreCase("")) {
				DCC_CardClosureLog.DCC_CardClosureLogger.debug("Could Not Connect to Server!");
			} else {
				DCC_CardClosureLog.DCC_CardClosureLogger.debug("Session ID found: " + sessionID);
				HashMap<String, String> socketDetailsMap = CommonMethods.socketConnectionDetails(cabinetName, jtsIP, jtsPort, sessionID);
				while (true) {
					DCC_CardClosureLog.setLogger();
					DCC_CardClosureLog.DCC_CardClosureLogger.debug("DCC Notify TO DEH ...123.");
					DCC_CardClouser(cabinetName, jtsIP, jtsPort, sessionID, queueID, socketConnectionTimeout, integrationWaitTime, socketDetailsMap);
					System.out.println("No More workitems to Process, Sleeping!");
					Thread.sleep(sleepIntervalInMin * 60 * 1000);
				}
			}
		}

		catch (Exception e) {
			e.printStackTrace();
			DCC_CardClosureLog.DCC_CardClosureLogger.error("Exception Occurred in DCCNotifyAPP : " + e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			DCC_CardClosureLog.DCC_CardClosureLogger.error("Exception Occurred in DCCNotifyAPP : " + result);
		}
	}

	private void DCC_CardClouser(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String queueID, int socketConnectionTimeOut, int integrationWaitTime,
			HashMap<String, String> socketDetailsMap) {
		final String ws_name = "Sys_CardBalClsBlk";

		try {
			// Validate Session ID
			sessionId = CommonConnection.getSessionID(DCC_CardClosureLog.DCC_CardClosureLogger, false);

			if (sessionId == null || sessionId.equalsIgnoreCase("") || sessionId.equalsIgnoreCase("null")) {
				DCC_CardClosureLog.DCC_CardClosureLogger.error("Could Not Get Session ID " + sessionId);
				return;
			}

			// Fetch all Work-Items on given queueID.
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("Fetching all Workitems on DCCNotifyAPP queue");
			System.out.println("Fetching all Workitems on Card Clouser queue");
			String fetchWorkitemListInputXML = CommonMethods.fetchWorkItemsInput(cabinetName, sessionId, queueID);
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("InputXML for fetchWorkList Call: " + fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML = CommonMethods.WFNGExecute(fetchWorkitemListInputXML, sJtsIp, iJtsPort, 1);

			DCC_CardClosureLog.DCC_CardClosureLogger.debug("WMFetchWorkList Card Clouser OutputXML: " + fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("FetchWorkItemListMainCode: " + fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("RetrievedCount for WMFetchWorkList Call: " + fetchWorkitemListCount);

			DCC_CardClosureLog.DCC_CardClosureLogger.debug("Number of workitems retrieved on Card Clouser: " + fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on Card Clouser: " + fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0) {
				for (int i = 0; i < fetchWorkitemListCount; i++) {
					String fetchWorkItemlistData = xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData = fetchWorkItemlistData.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

					DCC_CardClosureLog.DCC_CardClosureLogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: " + fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID = xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					DCC_CardClosureLog.DCC_CardClosureLogger.debug("Current ProcessInstanceID: " + processInstanceID);

					DCC_CardClosureLog.DCC_CardClosureLogger.debug("Processing Workitem: " + processInstanceID);
					System.out.println("\nProcessing Workitem: " + processInstanceID);

					String WorkItemID = xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					DCC_CardClosureLog.DCC_CardClosureLogger.debug("Current WorkItemID: " + WorkItemID);

					entryDateTime = xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					DCC_CardClosureLog.DCC_CardClosureLogger.debug("Current EntryDateTime: " + entryDateTime);

					String ActivityName = xmlParserfetchWorkItemData.getValueOf("ActivityName");
					DCC_CardClosureLog.DCC_CardClosureLogger.debug("ActivityName: " + ActivityName);

					ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					DCC_CardClosureLog.DCC_CardClosureLogger.debug("ActivityID: " + ActivityID);
					
					String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					DCC_CardClosureLog.DCC_CardClosureLogger.debug("ActivityType: " + ActivityType);
					
					String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					DCC_CardClosureLog.DCC_CardClosureLogger.debug("ProcessDefId: " + ProcessDefId);
					
					
					// Hritik - 18/07/23 -- ETB 
					try{
						String StrExttable_query = "SELECT WI_NAME,NTB FROM NG_DCC_EXTTABLE with(nolock) WHERE WI_NAME='" + processInstanceID + "'";
						String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(StrExttable_query, CommonConnection.getCabinetName(), sessionId);
						DCC_CardClosureLog.DCC_CardClosureLogger.debug("StrExttable_query: " + StrExttable_query);
						String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
						DCC_CardClosureLog.DCC_CardClosureLogger.debug("extTabDataOPXML: " + extTabDataOPXML);
						XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
						String mainCode=xmlParserData.getValueOf("MainCode");
						int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
						String NTB="";
						if (mainCode!=null && !"".equalsIgnoreCase(mainCode) && mainCode.equalsIgnoreCase("0")  && iTotalrec > 0){
							NTB=xmlParserData.getValueOf("NTB");
							DCC_CardClosureLog.DCC_CardClosureLogger.debug("NTB: " + NTB);
						}
						
						Date current_date = new Date();
						DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String entrydatetime = dateFormat.format(current_date);
						DCC_CardClosureLog.DCC_CardClosureLogger.debug("entrydatetime: " + entrydatetime);
									
						if("false".equalsIgnoreCase(NTB))
						{
							String tableName="NG_DCC_GR_ETB_REQ_STATUS";
							String Col = "WI_NAME,Request_status,Workstep,entry_date";
							String val = "'"+processInstanceID+"','Reject','Sys_CardBalClsBlk','"+entrydatetime+"'";
							DCC_CardClosureLog.DCC_CardClosureLogger.debug("Col: " + Col);
							DCC_CardClosureLog.DCC_CardClosureLogger.debug("val: " + val);
							
							String inputXML = CommonMethods.apInsert(cabinetName, sessionID, Col, val, tableName);
							DCC_CardClosureLog.DCC_CardClosureLogger.info("Insert No" + inputXML);
							String outputXML = CommonMethods.WFNGExecute(inputXML, jtsIP, jtsPort, 1);
							DCC_CardClosureLog.DCC_CardClosureLogger.info("Insert No outXml" + outputXML);
							XMLParser xmlParser = new XMLParser(outputXML);
							DCC_CardClosureLog.DCC_CardClosureLogger.info("apInsert : " +processInstanceID + " ; MainCode" + xmlParser.getValueOf("MainCode"));
							
							String mainCode_apinsert=xmlParser.getValueOf("MainCode");
							if (mainCode_apinsert!=null && !"".equalsIgnoreCase(mainCode_apinsert) && mainCode_apinsert.equalsIgnoreCase("0")){
								completeWorkItem(cabinetName, sessionId, processInstanceID, "Success", entryDateTime,"ETB Request status insert success");
								continue;
							}
							else{
								completeWorkItem(cabinetName, sessionId, processInstanceID, "Failed", entryDateTime,"ETB Request status insert Failed");
								continue;
							}
						}
					}
					catch (Exception e){
						DCC_CardClosureLog.DCC_CardClosureLogger.debug("Exception ETB : " + e.getMessage());
					}
					
					DCC_Notify_CAPS obj_DCC_Notify_CAPS = new DCC_Notify_CAPS(DCC_CardClosureLog.DCC_CardClosureLogger);
					String notifyCapsStatus=obj_DCC_Notify_CAPS.DCC_Notify_CAPS_Integration(cabinetName, sessionId, sJtsIp, iJtsPort, processInstanceID, ws_name,CommonConnection.getUsername(), socketConnectionTimeOut, integrationWaitTime, socketDetailsMap);
					if("Success".equalsIgnoreCase(notifyCapsStatus))
					{
						String fb_status=fetchCardBalance(cabinetName, sJtsIp, iJtsPort, sessionId, queueID, socketConnectionTimeOut, integrationWaitTime, socketDetailsMap, processInstanceID,ws_name);
						DCC_CardClosureLog.DCC_CardClosureLogger.debug("Final status of fetch card balance: " + fb_status);
						if(fb_status==null || "".equalsIgnoreCase(fb_status)||fb_status.contains("Fail"))
							{
								String arr[];
								String ErrDesc="";
								if(fb_status.contains("Fail"))
								{
									arr=fb_status.split("~");
									if(arr.length>2)
										ErrDesc=arr[2];
								}
								completeWorkItem(cabinetName, sessionId, processInstanceID, "Failed", entryDateTime,"Card Clousre Failed:-"+ErrDesc);
								continue;
							}
							else if(fb_status.contains("Success"))
							{
								completeWorkItem(cabinetName, sessionId, processInstanceID, "Success", entryDateTime,"Card Blocked Successfully");
								continue;
							}
							
					}
					else
					{
						String arr[];
						String ErrDesc="";
						if(notifyCapsStatus.contains("Failure"))
						{
							arr=notifyCapsStatus.split("~");
							if(arr.length>2)
								ErrDesc=arr[2];
						}
						completeWorkItem(cabinetName, sessionId, processInstanceID, "Failed", entryDateTime,"Notify CAPS Failed-"+ErrDesc);
					}
					
					
				
				}
			}
		} catch (Exception e)

		{
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("Exception: " + e.getMessage());
		}
	}

	private String fetchCardBalance(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String queueID, int socketConnectionTimeOut, int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, String processInstanceId,String ws_name) {

		try {
			String DBQuery = "SELECT CIF_ID, Card_Number, Wi_Name, CRN,ecrn, Card_Status,NOTIFY_DEH_IDENTIFIER,CIF FROM NG_DCC_EXTTABLE with(nolock) WHERE WI_NAME='" + processInstanceId + "'";

			String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery, cabinetName, sessionId);
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("extTabDataIPXML: " + extTabDataIPXML);

			String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, sJtsIp, iJtsPort, 1);
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("extTabDataOPXML: " + extTabDataOPXML);

			XMLParser xmlParserData = new XMLParser(extTabDataOPXML);

			int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));

			if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0 && !xmlParserData.getValueOf("Card_Status").equalsIgnoreCase("Closed")) {
				if("Closed".equalsIgnoreCase(xmlParserData.getValueOf("Card_Status")))
					return "Success";
				String xmlDataExtTab = xmlParserData.getNextValueOf("Record");
				xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

				StringBuilder inputXml = readRequestXmlSample(xmlParserData, "CARD_BALANCE_ENQ");

				DCC_CardClosureLog.DCC_CardClosureLogger.debug("balanceEnquiryInputXML: " + inputXml);
				DCC_CardClosureLog.DCC_CardClosureLogger.debug("Card_Status : " + xmlParserData.getValueOf("Card_Status"));

				String outputResponse = "Success";
				String blockStatus ="";
				outputResponse = socketConnection(cabinetName, CommonConnection.getUsername(), sessionId, sJtsIp, iJtsPort, processInstanceId, ws_name, 60, 65, socketDetailsMap, inputXml);
				
				XMLParser xmlParserSocketDetails = new XMLParser(outputResponse);
				DCC_CardClosureLog.DCC_CardClosureLogger.debug(" xmlParserSocketDetails : " + xmlParserSocketDetails);
				String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
				DCC_CardClosureLog.DCC_CardClosureLogger.debug("Return Code: " + return_code + "WI: " + processInstanceId);
				String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
				DCC_CardClosureLog.DCC_CardClosureLogger.debug("return_desc : " + return_desc + "WI: " + processInstanceId);
				if (return_code.equalsIgnoreCase("0000")) {
					
					insertBalanceDetails(xmlParserSocketDetails, processInstanceId);
					
					Boolean isCloseCard = false; // If card have balance than chang it false;
					String CardBalInqResponses = xmlParserSocketDetails.getValueOf("CardBalInqResponse");
					DCC_CardClosureLog.DCC_CardClosureLogger.debug("CardBalInqResponse : " + CardBalInqResponses);
					String BalanceDetails[] = getTagValues(CardBalInqResponses, "BalanceDetails").split("`");
					String TotalLimit="";
					String TotalOTB="";
					for (int i = 0; i < BalanceDetails.length; i++) {
						String BalanceType = getTagValues(BalanceDetails[i], "BalanceType");
						
						if ("TotalOTB".equalsIgnoreCase(BalanceType)) {
							TotalOTB = getTagValues(BalanceDetails[i], "BalanceAmt");
						}
						else if("TotalLimit".equalsIgnoreCase(BalanceType)){
							TotalLimit=getTagValues(BalanceDetails[i], "BalanceAmt");
						}
					}
					
					if (TotalLimit != null && !TotalLimit.equals("") && TotalOTB != null && !TotalOTB.equals("")) {
						DCC_CardClosureLog.DCC_CardClosureLogger.debug("TotalLimit : " + TotalLimit+ " TotalOTB : " + TotalOTB);
					//	Double cardBalance = Double.parseDouble(BalanceAmt);
						if (Double.parseDouble(TotalLimit)==Double.parseDouble(TotalOTB)) {
							isCloseCard = true;
						}
					}

					if (!"Blocked".equalsIgnoreCase(xmlParserData.getValueOf("Card_Status"))) {
						// always run block card
						inputXml = readRequestXmlSample(xmlParserData, "CREDITCARD_MAINTAINANCE");
						DCC_CardClosureLog.DCC_CardClosureLogger.debug("inputXmlBalEnquiry CREDITCARD_MAINTAINANCE for card block : " + inputXml);
						blockStatus=blockAndCloseCard(cabinetName, sJtsIp, iJtsPort, sessionId, socketDetailsMap, processInstanceId, inputXml, return_code, return_desc, false,ws_name);
					}

					if (isCloseCard) {
						inputXml = readRequestXmlSample(xmlParserData, "CC_SERVICE_REQUEST");
						DCC_CardClosureLog.DCC_CardClosureLogger.debug("inputXmlBalEnquiry CC_SERVICE_REQUEST for card closure: " + inputXml);
						blockStatus=blockAndCloseCard(cabinetName, sJtsIp, iJtsPort, sessionId, socketDetailsMap, processInstanceId, inputXml, return_code, return_desc, true,ws_name);
					}
					
					return blockStatus;

				} else {
					DCC_CardClosureLog.DCC_CardClosureLogger.debug("Card Balance Enquiry Failed: " + return_code);
					if(return_code!=null && return_desc!=null)
					return "Fail"+return_code+"~"+return_desc;
					return "Fail";
				}
				//DCC_CardClosureLog.DCC_CardClosureLogger.debug(" xmlParserSocketDetails : " + xmlParserSocketDetails);
			}
		} catch (Exception e) {
			DCC_CardClosureLog.DCC_CardClosureLogger.debug(" Exception : " + e.getMessage());
		}
		return "";
	}

	private void insertBalanceDetails(XMLParser xmlParserSocketDetails, String WI_Name) {
		try {
			/**Delete all the records corresponding to WI**/
			DCC_CardClosureLog.DCC_CardClosureLogger.info("Detele start : " + WI_Name);
			deleteBalanceDetails(WI_Name);
			DCC_CardClosureLog.DCC_CardClosureLogger.info("Delete End : " + WI_Name);
			
			/**Insert Records **/
			String CardBalInqResponses = xmlParserSocketDetails.getValueOf("CardBalInqResponse");
			String CardGroupInformations[] = getTagValues(CardBalInqResponses, "CardGroupInformation").split("`");
			for (int i = 0; i < CardGroupInformations.length; i++) {
				String CardGroupInformation = CardGroupInformations[i];
				String FreeField1 = getTagValues(CardGroupInformation, "FreeField1");
				String FreeField2 = getTagValues(CardGroupInformation, "FreeField2");
				String FreeField3 = getTagValues(CardGroupInformation, "FreeField3");
				String BalanceDetails[] = getTagValues(CardGroupInformation, "BalanceDetails").split("`");
				String columns = "WI_Name,FreeField1,FreeField2,FreeField3";
				String values = "'" + WI_Name + "','" + FreeField1 + "','" + FreeField2 + "','" + FreeField3 + "'";
				String tableName="NG_DCC_CARD_BALANCE_RESPONSE_DATA";
				for (int j = 0; j < BalanceDetails.length; j++) {
					String col = validateValue(getTagValues(BalanceDetails[j], "BalanceType"));
					String val = validateValue(getTagValues(BalanceDetails[j], "BalanceAmt"));
					columns = columns + "," + col;
					values = values + ",'" + val + "'";
				}
				
				DCC_CardClosureLog.DCC_CardClosureLogger.info("Columns : " + columns);
				DCC_CardClosureLog.DCC_CardClosureLogger.info("values : " + values);
				
				String inputXML = CommonMethods.apInsert(cabinetName, sessionID, columns, values, tableName);
				DCC_CardClosureLog.DCC_CardClosureLogger.info("Insert No" + inputXML);
				String outputXML = CommonMethods.WFNGExecute(inputXML, jtsIP, jtsPort, 1);
				DCC_CardClosureLog.DCC_CardClosureLogger.info("Insert No outXml" + outputXML);
				XMLParser xmlParser = new XMLParser(outputXML);
				DCC_CardClosureLog.DCC_CardClosureLogger.info("apInsert : " +WI_Name + " ; MainCode" + xmlParser.getValueOf("MainCode"));
			}
		} catch (Exception e) {
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("apInsert Exception : " + e.getMessage());
		}
	}

	private void deleteBalanceDetails(String WI_Name) {
		try {
			String tableName="NG_DCC_CARD_BALANCE_RESPONSE_DATA";
			String sWhereClause = "Wi_Name = '" + WI_Name + "'";
			String inputXML = CommonMethods.apDeleteInput(cabinetName, sessionID, tableName, sWhereClause);
			DCC_CardClosureLog.DCC_CardClosureLogger.info("apDeleteInput : " + inputXML);
			String outputXML = CommonMethods.WFNGExecute(inputXML, jtsIP, jtsPort, 1);
			DCC_CardClosureLog.DCC_CardClosureLogger.info("apDelete outXml : " + outputXML);
			XMLParser xmlParser = new XMLParser(outputXML);
			DCC_CardClosureLog.DCC_CardClosureLogger.info("apDeleteInput : " +WI_Name + " ; MainCode" + xmlParser.getValueOf("MainCode"));
		} catch (Exception e) {
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("apDeleteInput Exception : " + e.getMessage());
		}
	}

	private String blockAndCloseCard(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, HashMap<String, String> socketDetailsMap, String processInstanceId, StringBuilder inputXml,
			String return_code, String return_desc, Boolean isCloseCard,String ws_name) {
		DCC_CardClosureLog.DCC_CardClosureLogger.debug("balanceEnquiryInputXML: " + inputXml);
		
		//String remarks="";

		String outputResponseBlock = socketConnection(cabinetName, CommonConnection.getUsername(), sessionId, sJtsIp, iJtsPort, processInstanceId, ws_name, 60, 65, socketDetailsMap,
				inputXml);

		XMLParser xmlParserSocketDetailsBlock = new XMLParser(outputResponseBlock);
		DCC_CardClosureLog.DCC_CardClosureLogger.debug(" xmlParserSocketDetails : " + xmlParserSocketDetailsBlock);
		String return_code_Block = xmlParserSocketDetailsBlock.getValueOf("ReturnCode");
		DCC_CardClosureLog.DCC_CardClosureLogger.debug("Return Code: " + return_code + "WI: " + processInstanceId);
		String return_descBlock = xmlParserSocketDetailsBlock.getValueOf("ReturnDesc");
		DCC_CardClosureLog.DCC_CardClosureLogger.debug("return_desc : " + return_descBlock + "WI: " + processInstanceId);
		String finaldecision = "";
		if (return_code_Block.equalsIgnoreCase("0000")) {
			finaldecision = "Success";
			/*if ("Success".equalsIgnoreCase(return_descBlock)) {
				finaldecision = "Success";
				//remarks="Card Blocked Successfully";
			} else {
				finaldecision = "Fail";
				if(return_code_Block!=null && return_descBlock!=null)
				{
					finaldecision = "Fail~"+return_code_Block+"~"+return_descBlock;
				}
				//remarks="Card Blocked Failed";
			}*/
			if (isCloseCard) {
				updateDataInExtTable(processInstanceId, "Card_Status", "Closed");
				DCC_CardClosureLog.DCC_CardClosureLogger.debug("inputXmlBalEnquiry CC_SERVICE_REQUEST : " + inputXml);
			} else {
				updateDataInExtTable(processInstanceId, "Card_Status", "Blocked");
				DCC_CardClosureLog.DCC_CardClosureLogger.debug("inputXmlBalEnquiry CREDITCARD_MAINTAINANCE: " + inputXml);
			}
			/*DCC_CardClosureLog.DCC_CardClosureLogger.debug("completeWorkItem : Starting");
			completeWorkItem(cabinetName, sessionId, processInstanceId, finaldecision, entryDateTime,remarks);
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("completeWorkItem : Completed");*/

		}
		else
		{
			finaldecision = "Fail";
			if(return_code_Block!=null && return_descBlock!=null)
			{
				finaldecision = "Fail~"+return_code_Block+"~"+return_descBlock;
				//remarks=" -Card Blocked Failed";
			}
		}
		return finaldecision;
	}

	private static StringBuilder readRequestXmlSample(XMLParser xmlParserData, String callName) {

		String CIF_ID = xmlParserData.getValueOf("CIF_ID");
		String Card_Number_ID = xmlParserData.getValueOf("Card_Number");
		String wi_name = xmlParserData.getValueOf("Wi_Name");
		String CRN = xmlParserData.getValueOf("CRN");
		String notifydeh_flag_val=xmlParserData.getValueOf("NOTIFY_DEH_IDENTIFIER");
		String cif=xmlParserData.getValueOf("CIF");

		DCC_CardClosureLog.DCC_CardClosureLogger.debug("CIF_ID: " + CIF_ID);
		DCC_CardClosureLog.DCC_CardClosureLogger.debug("Card_Number: " + Card_Number_ID);
		DCC_CardClosureLog.DCC_CardClosureLogger.debug("wi_name: " + wi_name);
		DCC_CardClosureLog.DCC_CardClosureLogger.debug("CRN: " + CRN);
		DCC_CardClosureLog.DCC_CardClosureLogger.debug("notifydeh_flag_val: " + notifydeh_flag_val);
		StringBuilder inputXML = new StringBuilder("");
		if ("CARD_BALANCE_ENQ".equalsIgnoreCase(callName)) {
			StringBuilder sb = new StringBuilder("");
			try {

				String fileLocation = new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DCC_Integration")
						.append(System.getProperty("file.separator")).append("CardBalanceEnquiry.txt").toString();
				BufferedReader sbf = new BufferedReader(new FileReader(fileLocation));

				String line = sbf.readLine();
				while (line != null) {
					sb.append(line);
					sb.append(System.lineSeparator());
					line = sbf.readLine();
				}

				String CardBalanceEnquiry_input_xml = sb.toString().replace(">CIF_ID<", ">" + xmlParserData.getValueOf("CIF_ID").trim() + "<").replace(">Card_Number_ID<",
						">" + xmlParserData.getValueOf("Card_Number").trim() + "<");

				return inputXML.append(CardBalanceEnquiry_input_xml);
			} catch (Exception e) {
				DCC_CardClosureLog.DCC_CardClosureLogger.error("In Reading CardBalanceEnquiry.txt : " + e.getMessage());
				e.printStackTrace();
			}
		}

		else if ("CC_SERVICE_REQUEST".equalsIgnoreCase(callName)) {
			StringBuilder sb = new StringBuilder("");
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String date = formatter.format(new Date());
			try {
				String ecrn=xmlParserData.getValueOf("ecrn");
				if(ecrn==null)
					ecrn="";

				String fileLocation = new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DCC_Integration")
						.append(System.getProperty("file.separator")).append("CardClosure.txt").toString();
				BufferedReader sbf = new BufferedReader(new FileReader(fileLocation));

				String line = sbf.readLine();
				while (line != null) {
					sb.append(line);
					sb.append(System.lineSeparator());
					line = sbf.readLine();
				}
				String ClosedStatus="";
				if( "CP_Reject".equalsIgnoreCase(notifydeh_flag_val))
				{
					ClosedStatus="CLSC";
				}
				else 
				{
					ClosedStatus="CLSB";
				}
				DCC_CardClosureLog.DCC_CardClosureLogger.debug("ClosedStatus: " + ClosedStatus);

				String CardClosure_input_xml = sb.toString().replace(">date<", ">" + date.trim() + "<").replace(">Card_Number_ID<", ">" + ecrn.trim() + "<").replace("$ClosedStatus$",ClosedStatus);
				DCC_CardClosureLog.DCC_CardClosureLogger.debug("CardClosure_input_xml: " + CardClosure_input_xml);
				return inputXML.append(CardClosure_input_xml);

			} catch (Exception e) {
				DCC_CardClosureLog.DCC_CardClosureLogger.error("In Reading CardBalanceEnquiry.txt : " + e.getMessage());
				e.printStackTrace();
			}
		} else if ("CREDITCARD_MAINTAINANCE".equalsIgnoreCase(callName)) {
			StringBuilder sb = new StringBuilder("");
			try {

				String fileLocation = new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DCC_Integration")
						.append(System.getProperty("file.separator")).append("CardBlock.txt").toString();
				BufferedReader sbf = new BufferedReader(new FileReader(fileLocation));

				String line = sbf.readLine();
				while (line != null) {
					sb.append(line);
					sb.append(System.lineSeparator());
					line = sbf.readLine();
				}

				String CardBlock_input_xml = sb.toString().replace(">CRN<", ">" + xmlParserData.getValueOf("CRN").trim() + "<").replace(">Card_Number_ID<",
						">" + xmlParserData.getValueOf("Card_Number").trim() + "<").replace("$CustId$",cif);
				return inputXML.append(CardBlock_input_xml);
			} catch (Exception e) {
				DCC_CardClosureLog.DCC_CardClosureLogger.error("In Reading CardBalanceEnquiry.txt : " + e.getMessage());
				e.printStackTrace();
			}
		}
		return inputXML;
	}
	

	static String socketConnection(String cabinetName, String username, String sessionId, String sJtsIp, String iJtsPort, String wi_name, String ws_name,
			int connection_timeout, int integrationWaitTime, HashMap<String, String> socketDetailsMap, StringBuilder sInputXML)
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

			DCC_CardClosureLog.DCC_CardClosureLogger.debug("userName "+ username);
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("SessionId "+ sessionId);

			socketServerIP=socketDetailsMap.get("SocketServerIP");
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("SocketServerIP "+ socketServerIP);
			socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("SocketServerPort "+ socketServerPort);

			if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0))
			{

				socket = new Socket(socketServerIP, socketServerPort);
				socket.setSoTimeout(connection_timeout*1000);
				out = socket.getOutputStream();
				socketInputStream = socket.getInputStream();
				dout = new DataOutputStream(out);
				din = new DataInputStream(socketInputStream);
				DCC_CardClosureLog.DCC_CardClosureLogger.debug("Dout " + dout);
				DCC_CardClosureLog.DCC_CardClosureLogger.debug("Din " + din);

				outputResponse = "";

				inputRequest = getRequestXML( cabinetName,sessionId ,wi_name, ws_name, username, sInputXML);

				if (inputRequest != null && inputRequest.length() > 0)
				{
					int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
					DCC_CardClosureLog.DCC_CardClosureLogger.debug("RequestLen: "+inputRequestLen + "");
					inputRequest = inputRequestLen + "##8##;" + inputRequest;
					DCC_CardClosureLog.DCC_CardClosureLogger.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
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
					DCC_CardClosureLog.DCC_CardClosureLogger.debug("OutputResponse: "+outputResponse);

					if(!"".equalsIgnoreCase(outputResponse))
						outputResponse = getResponseXML(cabinetName,sJtsIp,iJtsPort,sessionId, wi_name,outputResponse,integrationWaitTime );

					if(outputResponse.contains("&lt;"))
					{
						outputResponse=outputResponse.replaceAll("&lt;", "<");
						outputResponse=outputResponse.replaceAll("&gt;", ">");
					}
				}
				socket.close();

				outputResponse = outputResponse.replaceAll("</MessageId>","</MessageId>\n<InputMessageId>"+inputMessageID+"</InputMessageId>");

				//DCC_CardClosureLog.DCC_CardClosureLogger.debug("outputResponse "+outputResponse);
				return outputResponse;

			}

			else
			{
				DCC_CardClosureLog.DCC_CardClosureLogger.debug("SocketServerIp and SocketServerPort is not maintained "+"");
				DCC_CardClosureLog.DCC_CardClosureLogger.debug("SocketServerIp is not maintained "+	socketServerIP);
				DCC_CardClosureLog.DCC_CardClosureLogger.debug(" SocketServerPort is not maintained "+	socketServerPort);
				return "Socket Details not maintained";
			}

		}

		catch (Exception e)
		{
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
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
				DCC_CardClosureLog.DCC_CardClosureLogger.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
				//printException(e);
			}
		}
	}

	private static String getRequestXML(String cabinetName, String sessionId, String wi_name, String ws_name, String userName, StringBuilder sInputXML)
	{
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("<APMQPUTGET_Input>");
		strBuff.append("<SessionId>" + sessionId + "</SessionId>");
		strBuff.append("<EngineName>" + cabinetName + "</EngineName>");
		strBuff.append("<XMLHISTORY_TABLENAME>NG_DCC_XMLLOG_HISTORY</XMLHISTORY_TABLENAME>");
		strBuff.append("<WI_NAME>" + wi_name + "</WI_NAME>");
		strBuff.append("<WS_NAME>" + ws_name + "</WS_NAME>");
		strBuff.append("<USER_NAME>" + userName + "</USER_NAME>");
		strBuff.append("<MQ_REQUEST_XML>");
		strBuff.append(new StringBuffer(sInputXML));
		strBuff.append("</MQ_REQUEST_XML>");
		strBuff.append("</APMQPUTGET_Input>");
		DCC_CardClosureLog.DCC_CardClosureLogger.debug("GetRequestXML: "+ strBuff.toString());
		return strBuff.toString();
	}

	private static String getResponseXML(String cabinetName,String sJtsIp,String iJtsPort, String sessionId, String wi_name, String message_ID, int integrationWaitTime)
	{

		String outputResponseXML="";
		try
		{
			String QueryString = "select OUTPUT_XML from NG_DCC_XMLLOG_HISTORY with (nolock) where MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+wi_name+"'";

			String responseInputXML =CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, sessionId);
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("Response APSelect InputXML: "+responseInputXML);

			int Loop_count=0;
			do
			{
				String responseOutputXML=CommonMethods.WFNGExecute(responseInputXML,sJtsIp,iJtsPort,1);
				DCC_CardClosureLog.DCC_CardClosureLogger.debug("Response APSelect OutputXML: "+responseOutputXML);

				XMLParser xmlParserSocketDetails= new XMLParser(responseOutputXML);
				String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
				DCC_CardClosureLog.DCC_CardClosureLogger.debug("ResponseMainCode: "+responseMainCode);



				int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
				DCC_CardClosureLog.DCC_CardClosureLogger.debug("ResponseTotalRecords: "+responseTotalRecords);

				if (responseMainCode.equals("0") && responseTotalRecords > 0)
				{

					String responseXMLData=xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData =responseXMLData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

					XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
					//DCC_CardClosureLog.DCC_CardClosureLogger.debug("ResponseXMLData: "+responseXMLData);

					outputResponseXML=xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
					//DCC_CardClosureLog.DCC_CardClosureLogger.debug("OutputResponseXML: "+outputResponseXML);

					if("".equalsIgnoreCase(outputResponseXML)){
						outputResponseXML="Error";
					}
					break;
				}
				Loop_count++;
				Thread.sleep(1000);
			}
			while(Loop_count<integrationWaitTime);
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("integrationWaitTime: "+integrationWaitTime);

		}
		catch(Exception e)
		{
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("Exception occurred in outputResponseXML" + e.getMessage());
			outputResponseXML="Error";
		}

		return outputResponseXML;
	}
	
	private static String getTagValues(String sXML, String sTagName) {
		String sTagValues = "";
		String sStartTag = "<" + sTagName + ">";
		String sEndTag = "</" + sTagName + ">";
		String tempXML = sXML;
		try {

			for (int i = 0; i < sXML.split(sEndTag).length; i++) {
				if (tempXML.indexOf(sStartTag) != -1) {
					sTagValues += tempXML.substring(tempXML.indexOf(sStartTag) + sStartTag.length(), tempXML.indexOf(sEndTag));
					//System.out.println("sTagValues : "+sTagValues);
					tempXML = tempXML.substring(tempXML.indexOf(sEndTag) + sEndTag.length(), tempXML.length());
				}
				if (tempXML.indexOf(sStartTag) != -1) {
					sTagValues += "`";
					//System.out.println("sTagValues"+sTagValues);
				}
				//System.out.println("sTagValues"+sTagValues);
			}
			//System.out.println(" Final sTagValues"+sTagValues);
		} catch (Exception e) {
		}
		return sTagValues;
	}
	
	
	private int readConfig() {
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir") + File.separator + "ConfigFiles" + File.separator + "DCC_CardClosure_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				configParamMap.put(name, p.getProperty(name));
			}
		} catch (Exception e) {
			return -1;
		}
		return 0;
	}
	
	public void updateDataInExtTable(String processInstanceId, String columnName , String value){
		
		try {
			String sWhereClause = "Wi_Name = '" + processInstanceId + "'";
			value = "'"+ value+"'";
			String columnNames1 = columnName;
			String columnValues1 = value;
			//String sWhereClause = "Workitem_no = '" + processInstanceID + "' and Firco_ID = '" + objWorkList.getVal("FIRCO_ECN_No") + "'";

			String extTableIPUpdateXml = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCC_CardClosureLog.DCC_CardClosureLogger, false), "NG_DCC_EXTTABLE", columnNames1, columnValues1, sWhereClause);
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("Input XML for apUpdateInput for NG_DCC_EXTTABLE Table : " + extTableIPUpdateXml);

			String extTableOPUpdateXml = CommonMethods.WFNGExecute(extTableIPUpdateXml, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("Output XML for apUpdateInput for NG_DCC_EXTTABLE Table : " + extTableOPUpdateXml);

			XMLParser sXMLParserChild = new XMLParser(extTableOPUpdateXml);
			String StrMainCode = sXMLParserChild.getValueOf("MainCode");

			if (StrMainCode.equals("0"))
				DCC_CardClosureLog.DCC_CardClosureLogger.debug("Successful in apUpdateInput the record in : NG_DCC_EXTTABLE");
			else {
				DCC_CardClosureLog.DCC_CardClosureLogger.debug("Error in Executing apUpdateInput sOutputXML : " + sXMLParserChild);
			}
		}catch(Exception e){
			DCC_CardClosureLog.DCC_CardClosureLogger.debug("Exception in updating ext table: " + e.getMessage());
			System.out.println("Exception in updating ext table: " + e.getMessage());
		}
	}
	
	private String completeWorkItem(String cabinetName, String sessionID, String processInstanceID, String decision, String entryDateTime,String remarks) {
		String workItemId = "1";
		try {
			String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionID, processInstanceID, workItemId);
			String getWorkItemOutputXml = CommonMethods.WFNGExecute(getWorkItemInputXML, jtsIP, jtsPort, 1);

			DCC_CardClosureLog.DCC_CardClosureLogger.debug("Output XML for getWorkItem is " + getWorkItemOutputXml);

			XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
			String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");

			if ("0".equals(getWorkItemMainCode)) {
				DCC_CardClosureLog.DCC_CardClosureLogger.info("get Workitem call successfull for " + processInstanceID);
				String attrbuteTag = "<Decision>" + decision + "</Decision>";
				String assignWorkitemAttributeInputXML = CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionID, processInstanceID, workItemId, attrbuteTag);
				DCC_CardClosureLog.DCC_CardClosureLogger.debug("Input XML for assign Attribute is " + assignWorkitemAttributeInputXML);

				String assignWorkitemAttributeOutputXML = CommonMethods.WFNGExecute(assignWorkitemAttributeInputXML, jtsIP, jtsPort, 1);
				DCC_CardClosureLog.DCC_CardClosureLogger.debug("Output XML for assign Attribues is " + assignWorkitemAttributeOutputXML);

				XMLParser xmlParserAssignAtt = new XMLParser(assignWorkitemAttributeOutputXML);

				String mainCodeAssignAtt = xmlParserAssignAtt.getValueOf("MainCode");
				if ("0".equals(mainCodeAssignAtt.trim())) {
					String completeWorkItemInputXML = CommonMethods.completeWorkItemInput(cabinetName, sessionID, processInstanceID, workItemId);

					DCC_CardClosureLog.DCC_CardClosureLogger.debug("Input XML for complete WI is " + completeWorkItemInputXML);

					DCC_CardClosureLog.DCC_CardClosureLogger.debug("Input XML for wmcompleteWorkItem: " + completeWorkItemInputXML);

					String completeWorkItemOutputXML = CommonMethods.WFNGExecute(completeWorkItemInputXML, jtsIP, jtsPort, 1);
					DCC_CardClosureLog.DCC_CardClosureLogger.debug("Output XML for wmcompleteWorkItem: " + completeWorkItemOutputXML);

					XMLParser xmlParserCompleteWorkitem = new XMLParser(completeWorkItemOutputXML);
					String completeWorkitemMaincode = xmlParserCompleteWorkitem.getValueOf("MainCode");
					DCC_CardClosureLog.DCC_CardClosureLogger.debug("Status of wmcompleteWorkItem  " + completeWorkitemMaincode);

					if ("0".equals(completeWorkitemMaincode)) {
						// inserting into history table
						DCC_CardClosureLog.DCC_CardClosureLogger.debug("WmCompleteWorkItem successful: " + completeWorkitemMaincode);
						// System.out.println(processInstanceID + "Complete Sussesfully with status "+objResponseBean.getIntegrationDecision());

						DCC_CardClosureLog.DCC_CardClosureLogger.debug("WorkItem moved to next Workstep.");

						SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
						SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

						Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
						String formattedEntryDatetime = outputDateFormat.format(entryDatetimeFormat);
						DCC_CardClosureLog.DCC_CardClosureLogger.debug("FormattedEntryDatetime: " + formattedEntryDatetime);

						Date actionDateTime = new Date();
						String formattedActionDateTime = outputDateFormat.format(actionDateTime);
						DCC_CardClosureLog.DCC_CardClosureLogger.debug("FormattedActionDateTime: " + formattedActionDateTime);

						// Insert in WIHistory Table.
						
						String columnNames = "WI_NAME,dec_date,WORKSTEP,USER_NAME,DECISION,ENTRY_DATE_TIME,Remarks";
						String columnValues = "'" + processInstanceID + "','" + formattedActionDateTime + "','Card Clouser','" + CommonConnection.getUsername() + "','" + decision + "','"
								+ formattedEntryDatetime + "','"+remarks+"'";

						String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionID, columnNames, columnValues, "NG_DCC_GR_DECISION_HISTORY");
						DCC_CardClosureLog.DCC_CardClosureLogger.debug("APInsertInputXML: " + apInsertInputXML);

						String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, jtsIP, jtsPort, 1);
						DCC_CardClosureLog.DCC_CardClosureLogger.debug("APInsertOutputXML: " + apInsertOutputXML);

						XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
						String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
						DCC_CardClosureLog.DCC_CardClosureLogger.debug("Status of apInsertMaincode  " + apInsertMaincode);
						if (apInsertMaincode.equalsIgnoreCase("0")) {
							DCC_CardClosureLog.DCC_CardClosureLogger.debug("ApInsert successful: " + apInsertMaincode);
							DCC_CardClosureLog.DCC_CardClosureLogger.debug("Inserted in WiHistory table successfully.");
						} else {
							DCC_CardClosureLog.DCC_CardClosureLogger.error("ApInsert failed: " + apInsertMaincode);
						}
					} else {
						DCC_CardClosureLog.DCC_CardClosureLogger.error("Error in completeWI call for " + processInstanceID);
					}
				} else {
					DCC_CardClosureLog.DCC_CardClosureLogger.error("Error in Assign Attribute call for WI " + processInstanceID);
				}

			} else {
				DCC_CardClosureLog.DCC_CardClosureLogger.error("Error in getWI call for WI " + processInstanceID);
			}

		}

		catch (Exception e) {
			DCC_CardClosureLog.DCC_CardClosureLogger.error("Exception " + e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			DCC_CardClosureLog.DCC_CardClosureLogger.error("Exception Occurred in TAO Integration Thread : " + result);
			System.out.println("Exception " + e);

		}
		return "";
	}
	

	private static  String validateValue(String value) {
		if (value != null && ! value.equals("") && !value.equalsIgnoreCase("null")) {
			return value.trim();
		}
		return "";
	}
}
