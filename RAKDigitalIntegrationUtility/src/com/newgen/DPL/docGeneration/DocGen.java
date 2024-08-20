package com.newgen.DPL.docGeneration;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import com.newgen.DCC.EFMS.DCC_MurabahaDealIntegration;
import com.newgen.DCC.Update_AssignCIF.DCC_DocumentGeneration;
import com.newgen.DCC.Update_AssignCIF.DCC_UpdateAssignCIFLog;
import com.newgen.DCC.Update_AssignCIF.DCC_Update_Assign_CIF_SysIntegration;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.DPL.Digital_PL_Log;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.omni.wf.util.excp.NGException;

public class DocGen{

	private static final Category DPL_DocumentGenerationLog = null;
	static NGEjbClient ngEjbClient;
	private static org.apache.log4j.Logger logger;

	private static String jtsIP;
	private static String jtsPort;
	private static String ActivityType;
	private static String ProcessDefId;
	private static String ActivityName;
	private static String ActivityID;
	private static  String cabinetName;
	
	private String sessionID = "";
	private int integrationWaitTime=0;
	private int socketConnectionTimeout=0;
	

	public DocGen() throws NGException {
		Digital_PL_Log.setLogger(getClass().getSimpleName());
		this.ngEjbClient = NGEjbClient.getSharedInstance();
		logger = Digital_PL_Log.getLogger(getClass().getSimpleName());
	}
	

	public void startDPLDocumentUtility(String cabinetName, String sJtsIp, String iJtsPort, String sessionId,
			String queueID, HashMap<String, String> socketDetailsMap, Map<String, String> ConfigParamMap) {

		try {
			final String ws_name="Doc_Generation";  //ConfigParamMap.get("WS_NAME");
			final String Queuename="Digital_PL_Doc_Generation";//ConfigParamMap.get("QueueName");
			integrationWaitTime=Integer.parseInt(ConfigParamMap.get("INTEGRATION_WAIT_TIME"));
			logger.debug("IntegrationWaitTime: "+integrationWaitTime);

			socketConnectionTimeout=Integer.parseInt(ConfigParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			logger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			// Validate Session ID
			sessionID = CommonConnection.getSessionID(logger, false);
			if (sessionId == null || sessionId.equalsIgnoreCase("") || sessionId.equalsIgnoreCase("null")) {
				logger.error("Could Not Get Session ID " + sessionId);
				return;
			}

			// Fetch all Work-Items on given queueID.
			logger.debug("Fetching all Workitems on " + Queuename + " queue");
			System.out.println("Fetching all Workitems on " + Queuename + " queue");
			
			String fetchWorkitemListInputXML = CommonMethods.fetchWorkItemsInput(cabinetName, sessionId, queueID);
			logger.debug("InputXML for fetchWorkList Call: " + fetchWorkitemListInputXML);
			System.out.print("");
			String fetchWorkitemListOutputXML = CommonMethods.WFNGExecute(fetchWorkitemListInputXML, sJtsIp, iJtsPort,1);
			logger.debug("WMFetchWorkList OutputXML: " + fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			logger.debug("FetchWorkItemListMainCode: " + fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			logger.debug("RetrievedCount for WMFetchWorkList Call: " + fetchWorkitemListCount);

			logger.debug("Number of workitems retrieved on CIF_Update_Initial: " + fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on CIF_Update_Initial: " + fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0) {
				for (int i = 0; i < fetchWorkitemListCount; i++) {
					String fetchWorkItemlistData = xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData = fetchWorkItemlistData.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

					logger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: " + fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID = xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					logger.debug("Current ProcessInstanceID: " + processInstanceID);

					logger.debug("Processing Workitem: " + processInstanceID);
					System.out.println("\nProcessing Workitem: " + processInstanceID);

					String WorkItemID = xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					logger.debug("Current WorkItemID: " + WorkItemID);

					String entryDateTime = xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					logger.debug("Current EntryDateTime: " + entryDateTime);

					ActivityName = xmlParserfetchWorkItemData.getValueOf("ActivityName");
					logger.debug("ActivityName: " + ActivityName);

					ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					logger.debug("ActivityID: " + ActivityID);
					ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					logger.debug("ActivityType: " + ActivityType);
					ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					logger.debug("ProcessDefId: " + ProcessDefId);

					//TIN is optional now. changed by rubi
//					String DB_Query = "SELECT a.IsNTB,a.ProductType,a.IsFIRCOHit,a.EFMS_Status,a.IsFTSReq,a.isstp,a.DectechDecision,a.ProductName,a.PreferredLanguage,a.Nationality,b.TIN FROM NG_DPL_EXTTABLE a  with(nolock) inner join NG_DPL_GR_FATCA_CRS_details b with(nolock) on a.WINAME=b.WIName  WHERE a.WINAME='"
//							+ processInstanceID + "'";
					
					
					String DB_Query = "SELECT a.IsNTB,a.ProductType,a.IsFIRCOHit,a.EFMS_Status,a.IsFTSReq,a.isstp,a.DectechDecision,a.ProductName,a.PreferredLanguage,"
							+ "a.Nationality FROM NG_DPL_EXTTABLE a  with(nolock) WHERE a.WINAME='"
							+ processInstanceID + "'";
					String extTabDataINPXML = CommonMethods.apSelectWithColumnNames(DB_Query,
							CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false));
					logger.debug("extTabDataIPXML: " + extTabDataINPXML);
					String extTabDataOUPXML = CommonMethods.WFNGExecute(extTabDataINPXML, CommonConnection.getJTSIP(),
							CommonConnection.getJTSPort(), 1);
					logger.debug("extTabDataOPXML: " + extTabDataOUPXML);

					XMLParser xmlParserData = new XMLParser(extTabDataOUPXML);
					int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));

					String decisionValue = "";
					String attributesTag = "";
					String updateCIFIntegrationStatus = "";
					String ErrDesc = "";

					if (!xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec == 0) {
						decisionValue = "Failed";
						ErrDesc = "apselect for Fetching WI details failed";
						attributesTag = "<Decision>" + decisionValue + "</Decision>";
						logger.debug("apselect for Fetching WI details failed");
						doneWI(processInstanceID, WorkItemID, decisionValue, entryDateTime, ErrDesc, attributesTag,
								sessionId);
						continue;
					}

					String NTB = xmlParserData.getValueOf("IsNTB");

					

					if (!"true".equalsIgnoreCase(NTB)) {

						String Product = xmlParserData.getValueOf("ProductType");
						String nationality = xmlParserData.getValueOf("Nationality");
						//String Preferred_Language = xmlParserData.getValueOf("PreferredLanguage");
						
						String FatchaDB_Query = "SELECT a.TIN FROM NG_DPL_GR_FATCA_CRS_details a  with(nolock) WHERE a.WIName='"
								+ processInstanceID + "'";
						
						String fatca_extTabDataINPXML = CommonMethods.apSelectWithColumnNames(FatchaDB_Query,
						CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false));
						logger.debug("extTabDataIPXML: " + fatca_extTabDataINPXML);
						
						String fatca_extTabDataOUPXML = CommonMethods.WFNGExecute(fatca_extTabDataINPXML, CommonConnection.getJTSIP(),
						CommonConnection.getJTSPort(), 1);
						logger.debug("extTabDataOPXML: " + fatca_extTabDataOUPXML);

						XMLParser fatcha_xmlParserData = new XMLParser(fatca_extTabDataOUPXML);
						int fatca_Totalrec = Integer.parseInt(fatcha_xmlParserData.getValueOf("TotalRetrieved"));
						logger.debug("fatca_Totalrec: " + fatca_Totalrec);
						
						String TIN="";
						if(fatcha_xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && fatca_Totalrec>0){
							logger.debug("fatca_Totalrec: " + fatca_Totalrec);
							TIN = fatcha_xmlParserData.getValueOf("TIN");
						}
						
						//String TIN = xmlParserData.getValueOf("TIN");
						
						//re generate inplementation
						String Reschedule_Query = "select reschedule_document from ng_dpl_reschedule_documents  with(nolock) WHERE winame='"
								+ processInstanceID + "'";
						
						String Reschedule_extTabDataINPXML = CommonMethods.apSelectWithColumnNames(Reschedule_Query,
						CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false));
						logger.debug("extTabDataIPXML: " + Reschedule_extTabDataINPXML);
						
						String Reschedule_extTabDataOUPXML = CommonMethods.WFNGExecute(Reschedule_extTabDataINPXML, CommonConnection.getJTSIP(),
						CommonConnection.getJTSPort(), 1);
						logger.debug("extTabDataOPXML: " + Reschedule_extTabDataOUPXML);

						XMLParser Reschedule_xmlParserData = new XMLParser(Reschedule_extTabDataOUPXML);
						int Reschedule_Totalrec = Integer.parseInt(Reschedule_xmlParserData.getValueOf("TotalRetrieved"));
						logger.debug("fatca_Totalrec: " + Reschedule_Totalrec);

						String MainCode = Reschedule_xmlParserData.getValueOf("MainCode");
						logger.debug("MainCode: " + MainCode);
						String docToBeGen = "";
						if (MainCode.trim().equals("0") && Reschedule_Totalrec > 0) {
							NGXmlList objWorkList = Reschedule_xmlParserData.createList("Records", "Record");
							
							for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
								String reschedule_document = objWorkList.getVal("reschedule_document");
								
								docToBeGen = docToBeGen + "~"+reschedule_document;
							}
						}else{
						
						//String docToBeGen = "";
						if ("US".equalsIgnoreCase(nationality)) {
							docToBeGen = docToBeGen + "~DPL_W9Form";
						} else if (!"US".equalsIgnoreCase(nationality) && TIN != null && !"".equalsIgnoreCase(TIN)) {
							docToBeGen = docToBeGen + "~DPL_W8-Form";
						}
						docToBeGen = docToBeGen + "~DPL_Security_Cheque";
						docToBeGen = docToBeGen + "~DPL_OnePager";
						if("Islamic".equalsIgnoreCase(Product) || "ISL".equalsIgnoreCase(Product) ||"I".equalsIgnoreCase(Product)){
							docToBeGen = docToBeGen + "~DPL_SalamContract";
							docToBeGen = docToBeGen + "~DPL_AGENCY_LETTER";
						}
						}
						String docGenStatus = generate_Document_Customer_Consent(docToBeGen,
								processInstanceID, sessionId,TIN);

						logger.debug("docGenStatus:--" + docGenStatus);
						if (docGenStatus == null || docGenStatus.contains("Error")) {
							decisionValue = "Failed";
							ErrDesc = "Doc Genration Failed";
							String err[] = docGenStatus.split("~");
							if (err.length > 1)
								ErrDesc = "Doc Genration Failed for document " + err[1];
						} else if (docGenStatus.contains("Success")) {
							decisionValue = "Success";
							if(!"true".equalsIgnoreCase(NTB)){
				        		insert_ng_digital_awb_status(processInstanceID,cabinetName,sessionID,sJtsIp,iJtsPort,ActivityName);
				        	}
						}
						logger.debug("Decision" + decisionValue);
						attributesTag = "<Decision>" + decisionValue + "</Decision>";
						doneWI(processInstanceID, WorkItemID, decisionValue, entryDateTime, ErrDesc, attributesTag,
								sessionId);
						continue;
					}

				}

			}
		} catch (Exception e) {
			logger.debug("Exception: " + e.getMessage());
		}

	}
	private void insert_ng_digital_awb_status(String Wi_name,String cabinetName,String  sessionId, String sJtsIp,String iJtsPort,String ActivityName)
	{
		try
		{
			String process_name="";
			// select the values from ext table to insert into ng_digital_awb_status
			final HashMap<String, String> CheckGridDataMap_awb = new HashMap<String, String>();
			String DBQuery_awb ="select WiName,Prospect_id,FirstName,MiddleName,LastName,MobileNo,email_id,AWB_Number,ntb,ECRN,isnull(CardOps_Reschedule,'N') as CardOps_Reschedule,EmirateID from NG_DPL_EXTTABLE where WIName='" + Wi_name + "'";
			
			String extTabDataIPXML_awb =CommonMethods.apSelectWithColumnNames(DBQuery_awb, CommonConnection.getCabinetName(), CommonConnection.getSessionID(logger, false));
			logger.debug("extTabDataOPXML_awb: " + extTabDataIPXML_awb);
			String extTabDataOPXML_awb = CommonMethods.WFNGExecute(extTabDataIPXML_awb, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
			logger.debug("extTabDataOPXML_awb: " + extTabDataOPXML_awb);
			XMLParser xmlParserData_awb = new XMLParser(extTabDataOPXML_awb);
			
			int iTotalrec = Integer.parseInt(xmlParserData_awb.getValueOf("TotalRetrieved"));
			
			if (xmlParserData_awb.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
			{
				String xmlDataExtTab = xmlParserData_awb.getNextValueOf("Record");
				xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
				NGXmlList objWorkList = xmlParserData_awb.createList("Records", "Record");
			
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)){
					CheckGridDataMap_awb.put("Wi_Name", objWorkList.getVal("WiName"));
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
				logger.debug("processname [] : "+processname[0]);
				process_name=processname[0];
				logger.debug("processname [] : "+process_name);
			}
			
			Date d= new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String sDate = dateFormat.format(d);
			logger.debug("insert_ng_digital_awb_status : sDate "+sDate);
			
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
			
			logger.debug("insert_ng_digital_awb_status DCC : ECRN "+ECRN);
			logger.debug("insert_ng_digital_awb_status DCC : prospect_id "+prospect_id);
			logger.debug("insert_ng_digital_awb_status DCC  : Full_name "+Full_name);
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
			
			logger.debug("insert_ng_digital_awb_status : columnNames_awbTable DCC "+columnNames_awbTable);
			logger.debug("insert_ng_digital_awb_status : columnValues_awbTable DCC "+columnValues_awbTable);

			String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionID, columnNames_awbTable, columnValues_awbTable,"ng_digital_awb_status");
			logger.debug("APInsertInputXML: ng_digital_awb_status DCC "+apInsertInputXML);

			String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
			logger.debug("APInsertOutputXML: ng_digital_awb_status DCC "+ apInsertInputXML);
			
			XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
			String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
			logger.debug("Status of apInsertMaincode  ng_digital_awb_status DCC "+ apInsertMaincode);

			logger.debug("Completed On ng_digital_awb_status DCC "+ ActivityName);
			
			if(apInsertMaincode.equalsIgnoreCase("0"))
			{
				logger.debug("ApInsert successful: ng_digital_awb_status "+apInsertMaincode);
				logger.debug("ApInsert successful: ng_digital_awb_status "+apInsertMaincode);
				System.out.println("ApInsert successful: ng_digital_awb_status DCC "+Wi_name);
				CheckGridDataMap_awb.clear();
			}
			else
			{
				logger.debug("ApInsert failed for ng_digital_awb_status: "+apInsertMaincode);
				System.out.println("ApInsert failed: ng_digital_awb_status DCC "+Wi_name);
			}
			
			}
			catch(Exception e)
			{
				logger.debug("insert_ng_digital_awb_status : "+e.getMessage());
			}
		
	}
	
	
	
	private void doneWI(String processInstanceID, String WorkItemID, String decisionValue, String entryDateTime,
			String ErrDesc, String attributesTag, String sessionId) {
		try {
			// Lock Workitem.
			String getWorkItemInputXML = CommonMethods.getWorkItemInput(CommonConnection.getCabinetName(), sessionId, processInstanceID,
					WorkItemID);
			String getWorkItemOutputXml = CommonMethods.WFNGExecute(getWorkItemInputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
			logger.debug("Output XML For WmgetWorkItemCall: " + getWorkItemOutputXml);

			XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
			String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
			logger.debug("WmgetWorkItemCall Maincode:  " + getWorkItemMainCode);

			if (getWorkItemMainCode.trim().equals("0")) {
				logger.debug("WMgetWorkItemCall Successful: " + getWorkItemMainCode);

				String assignWorkitemAttributeInputXML = "<?xml version=\"1.0\"?><WMAssignWorkItemAttributes_Input>"
						+ "<Option>WMAssignWorkItemAttributes</Option>" + "<EngineName>" + CommonConnection.getCabinetName() + "</EngineName>"
						+ "<SessionId>" + sessionId + "</SessionId>" + "<ProcessInstanceId>" + processInstanceID
						+ "</ProcessInstanceId>" + "<WorkItemId>" + WorkItemID + "</WorkItemId>" + "<ActivityId>"
						+ ActivityID + "</ActivityId>" + "<ProcessDefId>" + ProcessDefId + "</ProcessDefId>"
						+ "<LastModifiedTime></LastModifiedTime>" + "<ActivityType>" + ActivityType + "</ActivityType>"
						+ "<complete>D</complete>" + "<AuditStatus></AuditStatus>" + "<Comments></Comments>"
						+ "<UserDefVarFlag>Y</UserDefVarFlag>" + "<Attributes>" + attributesTag + "</Attributes>"
						+ "</WMAssignWorkItemAttributes_Input>";

				logger.debug("InputXML for assignWorkitemAttribute Call: " + assignWorkitemAttributeInputXML);

				String assignWorkitemAttributeOutputXML = CommonMethods.WFNGExecute(assignWorkitemAttributeInputXML,
						CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);

				logger.debug("OutputXML for assignWorkitemAttribute Call: " + assignWorkitemAttributeOutputXML);

				XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
				String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
				logger.debug("AssignWorkitemAttribute MainCode: " + assignWorkitemAttributeMainCode);

				if (assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0")) {
					logger.debug("AssignWorkitemAttribute Successful: " + assignWorkitemAttributeMainCode);
					if ("0".trim().equalsIgnoreCase("0")) {
						System.out.println(processInstanceID + "Complete Succesfully with status " + decisionValue);

						logger.debug("WorkItem moved to next Workstep.");

						SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
						SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

						Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
						String formattedEntryDatetime = outputDateFormat.format(entryDatetimeFormat);
						logger.debug("FormattedEntryDatetime: " + formattedEntryDatetime);

						Date actionDateTime = new Date();
						String formattedActionDateTime = outputDateFormat.format(actionDateTime);
						logger.debug("FormattedActionDateTime: " + formattedActionDateTime);

						// Insert in WIHistory Table.
						String columnNames="WI_NAME,Decision_Date_Time,WORKSTEP,USERNAME,DECISION,ENTRY_DATE_TIME,REMARKS";
						String columnValues="'"+processInstanceID+"','"+formattedActionDateTime+"','"+ActivityName+"','"
								+CommonConnection.getUsername()+"','"+decisionValue+"','"+formattedEntryDatetime+"','"+ErrDesc+"'";

						String apInsertInputXML = CommonMethods.apInsert(CommonConnection.getCabinetName(), sessionId, columnNames,
								columnValues, "NG_DPL_GR_DECISION_HISTORY");
						logger.debug("APInsertInputXML: " + apInsertInputXML);

						String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, jtsIP, jtsPort, 1);
						logger.debug("APInsertOutputXML: " + apInsertInputXML);

						XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
						String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
						logger.debug("Status of apInsertMaincode  " + apInsertMaincode);

						logger.debug("Completed On " + ActivityName);

						if (apInsertMaincode.equalsIgnoreCase("0")) {
							logger.debug("ApInsert successful: " + apInsertMaincode);
							logger.debug("Inserted in WiHistory table successfully.");
						} else {
							logger.debug("ApInsert failed: " + apInsertMaincode);
						}
					} else {
						// completeWorkitemMaincode="";
						// logger.debug("WMCompleteWorkItem failed:
						// "+completeWorkitemMaincode);
					}
				} else if ("11".equalsIgnoreCase(assignWorkitemAttributeMainCode)) {

					sessionID = CommonConnection.getSessionID(logger, false);
					doneWI(processInstanceID, WorkItemID, decisionValue, entryDateTime, ErrDesc, attributesTag,
							sessionID);
				} else {
					assignWorkitemAttributeMainCode = "";
					logger.debug("AssignWorkitemAttribute failed: " + assignWorkitemAttributeMainCode);
				}
			} else {
				getWorkItemMainCode = "";
				logger.debug("WmgetWorkItem failed: " + getWorkItemMainCode);
			}
		}
		catch (Exception e) {
			logger.debug("DoneWI Exception: " + e.toString());
		}
	}
	public String generate_Document_Customer_Consent(String pdfName, String processInstanceID, String sessionId,String TIN)
	throws IOException, Exception {
		
		String attrbList = "";
		String Output = "";
		logger.debug("Inside the generate_template Method: ");
		
		String prop_file_loc = System.getProperty("user.dir") + System.getProperty("file.separator") + "ConfigFiles"
		+ System.getProperty("file.separator") + "DCC_CAMGen_Config.properties";
		logger.debug("prop_file_loc: " + prop_file_loc);
		
		File file = new File(prop_file_loc);
		FileInputStream fileInput = new FileInputStream(file);
		Properties properties = new Properties();
		properties.load(fileInput);
		fileInput.close();
		
		String gtIP = properties.getProperty("gtIP");
		logger.debug("gtIP: " + gtIP);
		
		String gtPortProperty = properties.getProperty("gtPort");
		logger.debug("gtPortProperty: " + gtPortProperty);
		
		int gtPort = Integer.parseInt(gtPortProperty);
		logger.debug("gtPort: " + gtPort);
		
		// for current date time 
		Date d = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String CurrentDateTime = dateFormat.format(d);
		
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy");
		String wiCreatedDate = dateFormat1.format(d);
		
	
		String tbQuery = "select a.LoanAmount as Amount,a.Nationality,a.EmiratesID,a.prospectID,"+
				"a.ProspectID,a.DOB,a.WINAME,a.WICreatedDate as Prospect_Creation_Date,"+
				"CONCAT(a.FirstName,' ',a.MiddleName,' ',a.LastName) AS CustomerName "+
				"from NG_DPL_EXTTABLE a with (NOLOCK)  where a.WIName ='"
					+ processInstanceID + "'"; 
		// second for normal query
		logger.debug("tbQuery : " + tbQuery);
		
		String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(tbQuery, CommonConnection.getCabinetName(),sessionId);
		logger.debug("extTabDataIPXML template: " + extTabDataIPXML);
		String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(),
				CommonConnection.getJTSPort(), 1);
		logger.debug("extTabDataOPXML template: " + extTabDataOPXML);

		XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
		//logger.debug("xmlParserData template: " + xmlParserData);
		
		String strMainCode = xmlParserData.getValueOf("MainCode");
		logger.debug("apSelectWithColumnNames for main code: "+strMainCode);
		
		String RetrievedCount = xmlParserData.getValueOf("TotalRetrieved");
		logger.debug("RetrievedCount for apSelectWithColumnNames Call for retr: "+RetrievedCount);

		//if condition
		try {
			if ("0".equalsIgnoreCase(strMainCode) && !(RetrievedCount==null || "".equalsIgnoreCase(RetrievedCount)) && Integer.parseInt(RetrievedCount) >0){
				String CUSTOMERNAMETemplate = xmlParserData.getValueOf("CUSTOMERNAME");
				String NationalityTemplate = xmlParserData.getValueOf("Nationality");
				String EmirateIDTemplate = xmlParserData.getValueOf("EmiratesID");
				//String Card_ProductTemplate = xmlParserDataCardDescription.getValueOf("card_type_desc");
				String WI_Creation_date = xmlParserData.getValueOf("Prospect_Creation_Date");
				String Date_Of_Birth = xmlParserData.getValueOf("dob");
				String Tin_Number = TIN;//xmlParserData.getValueOf("TIN");
				
				
				String MrbQ= "select top 1 HouseApartmentNo,BuildingApartmentName,StreetLocation,CityTown AS City,PostcodeZipCode AS PO_Box_Address,AddressType from NG_DPL_GR_DemographicDetails with(nolock) " +
						"where winame = '" + processInstanceID +"'";
				
				logger.debug("MrbQ : " + MrbQ);
				String extTabDataIPXMLMrbQ = CommonMethods.apSelectWithColumnNames(MrbQ, CommonConnection.getCabinetName(),sessionId);
				logger.debug("extTabDataIPXMLMrbQ : " + extTabDataIPXMLMrbQ);
				
				String extTabDataOPXMLMrbQ = CommonMethods.WFNGExecute(extTabDataIPXMLMrbQ, CommonConnection.getJTSIP(),CommonConnection.getJTSPort(), 1);
				logger.debug("extTabDataOPXMLMrbQ template: " + extTabDataOPXMLMrbQ);
				
				XMLParser xmlParserDataMrbQ = new XMLParser(extTabDataOPXMLMrbQ);
				
				
				String nationalityQuery = "select CD_Desc,CM_Code from NG_MASTER_DAO_COUNTRY with (NOLOCK) where cd_desc='"+NationalityTemplate+"' or cm_code='"+NationalityTemplate+"'";
							
				// second for normal query
				logger.debug("tbQuery : " + tbQuery);
				
				String nationalityQueryXML = CommonMethods.apSelectWithColumnNames(nationalityQuery, CommonConnection.getCabinetName(),sessionId);
				logger.debug("extTabDataIPXML template: " + nationalityQueryXML);
				String nationalityOPXML = CommonMethods.WFNGExecute(nationalityQueryXML, CommonConnection.getJTSIP(),
						CommonConnection.getJTSPort(), 1);
				logger.debug("extTabDataOPXML template: " + extTabDataOPXML);

				XMLParser xmlParserData1 = new XMLParser(nationalityOPXML);
				//logger.debug("xmlParserData template: " + xmlParserData);
				String RetrievedCount1 = xmlParserData1.getValueOf("TotalRetrieved");
				String natMainCode = xmlParserData1.getValueOf("MainCode");
				logger.debug("apSelectWithColumnNames for main code: "+natMainCode);
				String CD_Desc="";
				String CM_Code ="";
				
					if ("0".equalsIgnoreCase(natMainCode) && !(RetrievedCount1==null || "".equalsIgnoreCase(RetrievedCount1)) && Integer.parseInt(RetrievedCount) >0){
						CD_Desc = xmlParserData1.getValueOf("CD_Desc");
						CM_Code = xmlParserData1.getValueOf("CM_Code");
					}
//				
				String PO_Box  = "";
				String PO_Box_Other = "";
				String Address_Line1 = "";
				String Address_Line2 = "";
				String Address_Line3 = "";
				String city ="";
				String Country  = "";
				String Country_Other = "";
				String portal_no="";
				String HouseApartmentNo="";
				String addressType="";
				String city_emirates = "";
				String Wi_No = xmlParserData.getValueOf("WIName");
				String PO_Box_Mrb="";
				String Street="";
				String BuildingApartmentName="";
				String prospectID="";
				prospectID= xmlParserData.getValueOf("prospectID");
				PO_Box_Mrb= xmlParserDataMrbQ.getValueOf("PO_Box_Address");
				
				city= xmlParserDataMrbQ.getValueOf("City");
				HouseApartmentNo=xmlParserDataMrbQ.getValueOf("HouseApartmentNo");
				Street= xmlParserDataMrbQ.getValueOf("StreetLocation");
				BuildingApartmentName= xmlParserDataMrbQ.getValueOf("BuildingApartmentName");
				
				Address_Line1=HouseApartmentNo+" "+Street+" "+BuildingApartmentName;
				Address_Line2=city+" "+PO_Box_Mrb;
				
				addressType= xmlParserDataMrbQ.getValueOf("AddressType");
				
				attrbList += "&<CurrentDate>&" + CurrentDateTime+"@10";
				attrbList += "&<CustomerName>&" + CUSTOMERNAMETemplate+"@10";
				attrbList += "&<Nationality>&" + CD_Desc+"@10";
//				
				attrbList += "&<Date>&" + CurrentDateTime+"@10";
				attrbList += "&<Date_Islamic>&" + CurrentDateTime+"@10";
				
				attrbList += "&<WI_NO>&" + Wi_No+"@10";
				attrbList += "&<Customer_Name>&" + CUSTOMERNAMETemplate+"@10";				
				attrbList += "&<Customer_Name_Islamic>&" + CUSTOMERNAMETemplate+"@10";
				attrbList += "&<EID_Number>&" + EmirateIDTemplate+"@10";
				attrbList += "&<EID_Number_Islamic>&" + EmirateIDTemplate+"@10";
				//attrbList += "&<Card_Name>&" + Card_ProductTemplate+"@10";
				//attrbList += "&<Card_Name_Islamic>&" + Card_ProductTemplate+"@10";
				attrbList += "&<Nationality>&" + CD_Desc+"@10";
				attrbList += "&<Nationality_Islamic>&" + CD_Desc+"@10";
				
				
				attrbList += "&<Date_Floor_Limit>&" + wiCreatedDate+"@10";
				attrbList += "&<Customer_Name_OCR>&" + CUSTOMERNAMETemplate+"@10"; 
				attrbList += "&<ProspectID>&" + prospectID+"@10"; 
				if ("Residence".equalsIgnoreCase(addressType)) {	
					attrbList += "&<PO_Box>&" + PO_Box+"@10";
					attrbList += "&<Address>&" + city_emirates+"@10";
				} else {	
					attrbList += "&<PO_Box>&" + PO_Box_Other+"@10";
					attrbList += "&<Address>&" + city_emirates+"@10";
				}
				
				// attribute for W8
				attrbList += "&<Customer_Name_W8>&" + CUSTOMERNAMETemplate+"@10";
				//attrbList += "&<Nationality_desc_W8>&" + NationalityTemplate+"@10";
				attrbList += "&<Nationality_desc_W8>&" + CD_Desc+"@10";
				attrbList += "&<Date_Of_Birth_W8>&" + Date_Of_Birth+"@10";
				attrbList += "&<Current_Date_W8>&" + CurrentDateTime+"@10";
				attrbList += "&<Tin_Number_W8>&" + Tin_Number+"@10";
				attrbList += "&<TIN>&" + Tin_Number+"@10";
				attrbList += "&<DOB>&" + Date_Of_Birth+"@10";
				attrbList += "&<Country>&" + CM_Code+"@10";
				
				
				//Attribute for One Pager 
				attrbList += "&<EmiratesID>&" + EmirateIDTemplate+"@10";
				attrbList += "&<WINAME>&" + Wi_No+"@10";
				
				
				// if condition for getting the address details  for preffered address
				if ("Residence".equalsIgnoreCase(addressType)) {	
					attrbList += "&<Home_Country_Address_W8>&" + Address_Line1+"@10";
					attrbList += "&<Home_country_City_W8>&" + city+"@10";
					attrbList += "&<Home_Country_W8>&" + Country+"@10";		
				} else {	
					attrbList += "&<Home_Country_Address_W8>&" + Address_Line1+"@10";
					attrbList += "&<Home_country_City_W8>&" + city+"@10";
					attrbList += "&<Home_Country_W8>&" + Country_Other+"@10";
				}
				
				// attribute for w9
				attrbList += "&<Customer_Name_W9>&" + CUSTOMERNAMETemplate+"@10";
				attrbList += "&<Current_Date_W9>&" + CurrentDateTime+"@10";
				attrbList += "&<Tin_Number_W9>&" + Tin_Number+"@10";
				
				if ("Residence".equalsIgnoreCase(addressType)){
					attrbList += "&<StreetAptNumber>&" + Address_Line1+"@10";
					attrbList += "&<CityTownPinCode>&" + Address_Line2+"@10";		
				} else{
					attrbList += "&<StreetAptNumber>&" + Address_Line1+"@10";
					attrbList += "&<CityTownPinCode>&" + Address_Line2+"@10";
				}
				
				try{
					attrbList += "&<portal_ref>&" + xmlParserData.getValueOf("ProspectId")+"@10";
					attrbList += "&<customer_name>&" + CUSTOMERNAMETemplate+"@10";
					Date date = new Date();
					SimpleDateFormat mmddyyyy = new SimpleDateFormat("dd/MM/yyyy");
					String today = mmddyyyy.format(date);
					attrbList = attrbList+"&<date_today>&"+today.replaceAll("/", "")+"@10";
					String amountStr =xmlParserData.getValueOf("Amount");
					if (amountStr==null || "".equalsIgnoreCase(amountStr) || "null".equalsIgnoreCase(amountStr)){
						attrbList =attrbList+"&<Amount>&"+"0@10";
						attrbList =attrbList+"&<AmountInWords>&"+"ZERO";
					}

					else{
						String number = xmlParserData.getValueOf("Amount");
						double amount = Double.parseDouble(number);
						DecimalFormat formatter = new DecimalFormat("#,###.00");
						number = formatter.format(amount);
						System.out.println("Converted Amount is "+number);
						attrbList =attrbList+"&<Amount>&"+number;
						if(amountStr.contains("."))
							amountStr=amountStr.substring(0,amountStr.indexOf("."));
						if(!amountStr.matches("[0-9]+")){
							logger.debug("Not a valid amount--" + amountStr);
							attrbList =attrbList+"&<AmountInWords>&"+amountStr+" DIRHAMS ";
						}
						else
						attrbList =attrbList+"&<AmountInWords>&"+numberToWord(Integer.parseInt(amountStr))+" DIRHAMS "+"@10";		
					}
				}
				catch(Exception e){
					logger.debug("Exception occured while converting amount into no" + e.getMessage());
					System.out.println("Converted Amount is "+e.getMessage());
				}
				
				logger.debug("attrbList" + attrbList);
				logger.debug("doc list to be genrated" + pdfName);
				
				String docList[] = pdfName.split("~");
				String finalDocList="";
				for(String tempName:docList){
					if(!tempName.equals("")){
						logger.debug("Temp Name-- "+tempName); 
						
						
						
						Output= makeSocketCall(attrbList, processInstanceID, tempName, sessionId, gtIP, gtPort, "", "", "",portal_no);
						logger.debug("output for template "+tempName+":-" + Output);
						if(Output==null || !Output.contains("Success~")){
							return "Error~"+tempName;
						}
						else if(Output!=null && Output.contains("Success~")){
							String str[] = Output.split("~");
							if(str.length>1)
							{
								String addDocXML = str[1];
								XMLParser xmlParseraddDocXML= new XMLParser(addDocXML);
								String docTypeName = xmlParseraddDocXML.getValueOf("DocumentName");
								String ISIndex = xmlParseraddDocXML.getValueOf("ISIndex");
								if(ISIndex!=null && ISIndex.contains("#")){
									ISIndex=ISIndex.substring(0,ISIndex.indexOf("#"));
								}
								if(docTypeName!=null){
									if("".equalsIgnoreCase(finalDocList))
										finalDocList=docTypeName;
									else
										finalDocList=finalDocList+"\n"+docTypeName;
								}
							}
						}
					}
					else{
						
					}
				}
				String columnNames="GeneratedDocumentList";
				String columnValues="'"+finalDocList+"'";
				String sWhereClause = "WINAME='" + processInstanceID + "'";
		    	String tableName = "NG_DPL_EXTTABLE";
		        String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), sessionId, tableName, columnNames, columnValues, sWhereClause);
		        logger.debug("Input XML for apUpdateInput for " + tableName + " Table : " + inputXML);
		        String outputXml = CommonMethods.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
		        logger.debug("Output XML for apUpdateInput for " + tableName + " Table : " + outputXml);
		        XMLParser sXMLParserChild = new XMLParser(outputXml);
		        String StrMainCode = sXMLParserChild.getValueOf("MainCode");
		        //String RetStatus = null;
		        if (StrMainCode.equals("0")){
		        	return "Success";
		        }
		        else{
		        	logger.debug("Error in Executing apUpdateInput for genearted documents: " + outputXml);
		        	return "Error~DocListupdate";
		        }
			}
			else{
				logger.debug("main code is not 0 try again :");	
			}
			
		}
		catch (Exception e){
			logger.debug("Exception: "+e.getMessage());
		}
	return Output;
	}
	public String makeSocketCall(String argumentString, String wi_name, String docName, String sessionId, String gtIP,
			int gtPort,String prequired, String pvalue,String userEmail,String portal_no) {
		String socketParams = argumentString + "~" + wi_name + "~" + docName + "~" + sessionId+"~"+prequired+"~"+pvalue+"~"+userEmail+"~"+portal_no;

		System.out.println("socketParams -- " + socketParams);
		logger.debug("socketParams:-\n" + socketParams);

		Socket template_socket = null;
		DataOutputStream template_dout = null;
		DataInputStream template_in = null;
		String result = "";
		try {
			// Socket write code started
			template_socket = new Socket(gtIP, gtPort);
			logger.debug("template_socket" + template_socket);

			template_dout = new DataOutputStream(template_socket.getOutputStream());
			logger.debug("template_dout" + template_dout);

			if (socketParams != null && socketParams.length() > 0) {
				int outPut_len = socketParams.getBytes("UTF-8").length;
				logger.debug("outPut_len" + outPut_len);
				// CreditCard.mLogger.info("Final XML output len:
				// "+outPut_len +
				// "");
				socketParams = outPut_len + "##8##;" + socketParams;
				logger.debug("socketParams--" + socketParams);
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
				logger.debug("result--" + result);
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
	
	private String numberToWord(Integer number) {
		try{

			// variable to hold string representation of number 
			String words = "";
			String unitsArray[] = { "zero", "one", "two", "three", "four", "five", "six", 
					"seven", "eight", "nine", "ten", "eleven", "twelve",
					"thirteen", "fourteen", "fifteen", "sixteen", "seventeen", 
					"eighteen", "nineteen" };
			String tensArray[] = { "zero", "ten", "twenty", "thirty", "forty", "fifty","sixty", "seventy", "eighty", "ninety" };

			if (number == 0) {
				return "zero";
			}
			// add minus before conversion if the number is less than 0
			if (number < 0) { 
				// convert the number to a string
				String numberStr = "" + number; 
				// remove minus before the number 
				numberStr = numberStr.substring(1); 
				// add minus before the number and convert the rest of number 
				return "minus " + numberToWord(Integer.parseInt(numberStr)); 
			} 
			// check if number is divisible by 1 million
			if ((number / 1000000) > 0) {
				words += numberToWord(number / 1000000) + " million ";
				number %= 1000000;
			}
			// check if number is divisible by 1 thousand
			if ((number / 1000) > 0) {
				words += numberToWord(number / 1000) + " thousand ";
				number %= 1000;
			}
			// check if number is divisible by 1 hundred
			if ((number / 100) > 0) {
				words += numberToWord(number / 100) + " hundred ";
				number %= 100;
			}
			if (number > 0) {
				// check if number is within teens
				if (number < 20) { 
					// fetch the appropriate value from unit array
					words += unitsArray[number];
				} else { 
					// fetch the appropriate value from tens array
					words += tensArray[number / 10]; 
					if ((number % 10) > 0) {
						words += "-" + unitsArray[number % 10];
					}  
				}
			}
			return words.toUpperCase();
		}
		catch(Exception e){
			logger.debug("Exception occured while converting amount in numberToWord method" + e.getMessage());
			System.out.println("Converted Amount is numberToWord method "+e.getMessage());
			return "";
		}
	}

	
}
