/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/"U_ID"roduct			: RAK BPM
Application				: RAK BPM Utility
Module					: RAOP Status
File Name				: RAOPStatus.java
Author 					: Hritik 
Date (DD/MM/YYYY)		: 08/02/2024

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/


package com.newgen.TF_Firco;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.itextpdf.text.*;
import com.itextpdf.text.Font.FontFamily;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;

import ISPack.CPISDocumentTxn;
import ISPack.ISUtil.JPDBRecoverDocData;
import ISPack.ISUtil.JPISException;
import ISPack.ISUtil.JPISIsIndex;


public class TF_Firco implements Runnable {
	private static NGEjbClient ngEjbClient;	
	static Map<String, String> configParamMap = new HashMap<String, String>();
	private static String RequestingUnitName = "TRADEFINANCEDEPT";
	private static String recordType;
	private static String ReferenceNo;
	private static String FullName;
	private static String gender;
	private static String DOB;
	private static String NATIONALITY;
	private static String RESIDENCEADDRCOUNTRY;
	private static String PASSPORT_NUMBER;
	private static String rowVal;
	private static String trTableColumn;
	private static String trTableValue;
	private static String sessionID;
	private static String processInstanceID;
	private static String decision;
	private static String entryDateTime;
	private static String remarks;
	private static String return_code;
	private static String return_desc;
	private static String cabinetName;
	private static String jtsIP = "";
	private static String jtsPort = "";
	private static String TemplatepathfromPrp;
	private static String VolumeId;
	private static String SiteId;
	private static String ProcessDefId;
	private static String TF_GENERTATED_PDF_PATH;
	private static String TF_Form_Template_Path;
	private String EXT_TABLE = "";
	
	@Override
	public void run() {
		String queueID = "";
		String UserName = "";
		int socketConnectionTimeout=0;
		int integrationWaitTime=0;
		int sleepIntervalInMin=0;
		int TrialTime = 0;
		String RouteAll = "";
		int ErrorCount = 0;
		
		try {
			TF_Firco_Handling_Log.setLogger();
			ngEjbClient = NGEjbClient.getSharedInstance();

			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0) {
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.error("Could not Read Config Properties [TF_Firco]");
				return;
			}
			
			cabinetName = CommonConnection.getCabinetName();
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("JTSPORT: " + jtsPort);

			queueID = configParamMap.get("queueID");
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("QueueID: " + queueID);
		
			socketConnectionTimeout=Integer.parseInt(configParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(configParamMap.get("INTEGRATION_WAIT_TIME"));
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(configParamMap.get("SleepIntervalInMin"));
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);
			
			TrialTime=Integer.parseInt(configParamMap.get("TrialTime"));
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("TrialTime: "+TrialTime);
			
			String ErrorCountStr = configParamMap.get("ErrorCount") == null ? "0" : configParamMap.get("ErrorCount");
			ErrorCount=Integer.parseInt(ErrorCountStr);
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("ErrorCount: "+ErrorCount);
			
			TemplatepathfromPrp=configParamMap.get("TemplateFilePath");
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("TemplatepathfromPrp: " + TemplatepathfromPrp);
			
			VolumeId=configParamMap.get("VolumeId");
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("VolumeId: " + VolumeId);

			SiteId=configParamMap.get("SiteId");
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("SiteId: " + SiteId);
			
			ProcessDefId=configParamMap.get("ProcessDefId");
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("ProcessDefId: " + ProcessDefId);
			
			TF_GENERTATED_PDF_PATH=configParamMap.get("TF_GENERTATED_PDF_PATH");
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("TF_GENERTATED_PDF_PATH: " + TF_GENERTATED_PDF_PATH);
			
			TF_Form_Template_Path=configParamMap.get("TF_Form_Template_Path");
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("TF_Form_Template_Path: " + TF_Form_Template_Path);
			
			
			sessionID = CommonConnection.getSessionID(TF_Firco_Handling_Log.TF_Firco_HandlingLogger, false);

			if(sessionID.trim().equalsIgnoreCase("")) {
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Could Not Connect to Server!");
			}
			else {
				HashMap<String, String> socketDetailsMap = CommonMethods.socketConnectionDetails(cabinetName, jtsIP, jtsPort, sessionID);
				while (true) {
					TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Session ID found: " + sessionID);
					TF_Firco_Handling_Log.setLogger();
					startFircoAPIUtility(cabinetName, UserName, jtsIP, jtsPort, sessionID, queueID, socketConnectionTimeout, integrationWaitTime, socketDetailsMap, TrialTime, RouteAll,
					ErrorCount);
					
					Thread.sleep(sleepIntervalInMin*60*1000);
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.error("Exception Occurred in TF Firoc  : " + e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.error("Exception Occurred in TF Firoc  : " + result);
		}
	}
	
	private void startFircoAPIUtility(String cabinetName, String UserName, String sJtsIp, String iJtsPort, String sessionId, String queueID, 
	int socketConnectionTimeOut, int integrationWaitTime, HashMap<String, String> socketDetailsMap, int trialTime, String routeAll, int errorCount) {
	
		final String ws_name="Sys_FIRCO_check";
		
		try {
			sessionId  = CommonConnection.getSessionID(TF_Firco_Handling_Log.TF_Firco_HandlingLogger, false);
			if(sessionId==null || sessionId.equalsIgnoreCase("") || sessionId.equalsIgnoreCase("null")) {
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.error("Could Not Get Session ID "+sessionId);
				return;
			}
			
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Fetching all Workitems on TF Firoc queue");
			System.out.println("Fetching all Workitems on TF Firoc system queue");
			
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionId, queueID);
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= CommonMethods.WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WMFetchWorkList OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Number of workitems retrieved on System_Error: "+fetchWorkitemListCount);
			System.out.println("Number of workitems retrieved on System_Error: "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0) {
			for(int i=0; i<fetchWorkitemListCount; i++) {
						
				String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
				fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
				
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
				XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);
				
				String processInstanceID=xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Current ProcessInstanceID: "+processInstanceID);
				
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Processing Workitem: "+processInstanceID);
				System.out.println("\nProcessing Workitem: "+processInstanceID);
				
				String WorkItemID=xmlParserfetchWorkItemData.getValueOf("WorkItemId");
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Current WorkItemID: "+WorkItemID);
				
				String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Current EntryDateTime: "+entryDateTime);
				
				String ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("ActivityID: "+ActivityID);
				
				String ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("ActivityType: "+ActivityType);
				
				String ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("ProcessDefId: "+ProcessDefId);
				
			//	generate_firco_temp(processInstanceID,"Firco_Template");
				
				String WINAME="",Entity_Type="",NAME="",Fetch_Firco_Status="",SELECT_ID="",decision="Success",remarks="";;			
				
				String DBQuery = "SELECT WINAME,Entity_Type,Entity_Name,Fetch_Firco_Status,SELECT_ID FROM USR_0_TF_FIRCO_DTLS_GRID WITH (NOLOCK) Where (Fetch_Firco_Status is null OR Fetch_Firco_Status='' OR Fetch_Firco_Status='E') AND WINAME='" + processInstanceID + "'";
				List<Map<String, String>> DataFromDB = new ArrayList<Map<String, String>>();
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("DBQuery: "+DBQuery);
				DataFromDB = getDataFromDBMap(DBQuery, cabinetName, sessionId, sJtsIp, iJtsPort);
						
					for (Map<String, String> entry : DataFromDB) {
						
						WINAME=entry.get("WINAME");
						Entity_Type=entry.get("Entity_Type");
						NAME=entry.get("Entity_Name");
						Fetch_Firco_Status=entry.get("Fetch_Firco_Status");
						SELECT_ID=entry.get("SELECT_ID");
						
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME: "+WINAME);
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Entity_Type: "+Entity_Type);
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("NAME: "+NAME);
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Fetch_Firco_Status: "+Fetch_Firco_Status);
						
						if("Individual".equalsIgnoreCase(Entity_Type)){
							recordType="I";
						}else if ("Corporate".equalsIgnoreCase(Entity_Type)){
							recordType="C";
						}else if ("Vessel".equalsIgnoreCase(Entity_Type)){
							recordType="V";
						}else if("Others".equalsIgnoreCase(Entity_Type)){
							recordType="O";
						}else{
							recordType="";
						}
						
						java.util.Date d1 = new Date();
						SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String [] split = processInstanceID.split("-");
						String splitVal = split[0]+"-"+split[1];
						ReferenceNo=splitVal+"_"+sdf1.format(d1);
						
						String APIInputXML = FircoAPI_Input(RequestingUnitName,recordType,ReferenceNo,NAME);
						StringBuilder finalString=new StringBuilder(APIInputXML);
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("InputXML: "+finalString);
						
						String responseXML="";
						
						HashMap<String, String> socketConnectionMap = socketConnectionDetails(cabinetName, sJtsIp, iJtsPort, sessionId); 
						responseXML = socketConnection(cabinetName, CommonConnection.getUsername(), sessionId, sJtsIp, iJtsPort, processInstanceID, ws_name, 60, 65,socketConnectionMap, finalString);
						XMLParser xmlParserSocketDetails= new XMLParser(responseXML);
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(" xmlParserSocketDetails : "+xmlParserSocketDetails);
						String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Return Code: "+return_code+ "WI: "+processInstanceID);
						String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc").replace("'", "");
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("return_desc: "+return_desc+ "WI: "+processInstanceID);
						if (return_desc.trim().equalsIgnoreCase(""))
							return_desc = xmlParserSocketDetails.getValueOf("Description").replace("'", "");
						String MsgId = "";
						if (responseXML.contains("<MessageId>"))
							MsgId = xmlParserSocketDetails.getValueOf("MessageId");
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("MsgId: "+MsgId+ "WI: "+processInstanceID);
						
						String CallStatus = "";
						if(return_code.equals("0000") || return_code.equals("FFF002") || return_code.equals("FFF_OK") || return_code.equals("FFFBAD") || return_code.equals("FFFPEN")) 
						{
							CallStatus="Success";
							decision="Success";
								
							if(responseXML.contains("<AlertDetails>")) {
							String[] arrOfStr1 = null; 
							
							String AlertDetailsTagResponse=responseXML.substring(responseXML.indexOf("<AlertDetails>")+"</AlertDetails>".length()-1,responseXML.indexOf("</AlertDetails>"));
							rowVal = responseXML.substring(responseXML.indexOf("<AlertDetails>")+"</AlertDetails>".length()-1,responseXML.indexOf("</AlertDetails>"));
							
							String StatusBehavior = "";
							String StatusName = "";
							
							if(responseXML.contains("<StatusBehavior>"))
							StatusBehavior = responseXML.substring(responseXML.indexOf("<StatusBehavior>")+"</StatusBehavior>".length()-1,responseXML.indexOf("</StatusBehavior>"));						   
							if(responseXML.contains("<StatusName>"))
								StatusName = responseXML.substring(responseXML.indexOf("<StatusName>")+"</StatusName>".length()-1,responseXML.indexOf("</StatusName>"));
							
							TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("MsgId: "+MsgId+ "StatusBehavior: "+StatusBehavior);
							TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("MsgId: "+MsgId+ "StatusName: "+StatusName);
							
							if (rowVal.contains("Suspect detected #1")) {			   	
							arrOfStr1 = rowVal.split("=============================");
							if(arrOfStr1.length==1) {							   }
								else if(arrOfStr1.length>1) {
								   try {
									   	Map<String,String> Columnvalues = new HashMap<String,String>(); 
										for(int j=1;j<arrOfStr1.length;j++) {
											String sRecords=arrOfStr1[j].replace(": \n", ":"); 
											sRecords=sRecords.replace(":\n", ":");
											sRecords=sRecords.replace("'", "''"); // added to handle the PCUG 99 - firco impacted cases.
											
											if (sRecords.contains("Suspect detected")) {
											BufferedReader bufReader = new BufferedReader(new StringReader(sRecords));
											String line=null;
											while((line=bufReader.readLine()) != null ) {
												String[] PDFColumns = {"OFAC ID", "NAME", "MATCHINGTEXT", "ORIGIN", "DESIGNATION", "DATE OF BIRTH", "USER DATA 1", "NATIONALITY", "PASSPORT", "ADDITIONAL INFOS"};
												for(int k=0;k<PDFColumns.length;k++) {
													if(line.contains(PDFColumns[k]+":")) {
														String colData = "";
														String [] tmp = line.split(":");
																	 //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("tmp.length : "+tmp.length+", line : "+line);
									
																	 //********below loop added for handling hardcoded Fircosoft XML in offshore dev server
														if(tmp.length == 1)
															colData="";
														else if(tmp[1].trim().equalsIgnoreCase("Synonyms") || tmp[1].trim().equalsIgnoreCase("none") || tmp[1].trim().equalsIgnoreCase(""))
															colData="";
														else {
															//colData=tmp[1].trim();
															for(int m=1; m<tmp.length; m++) {
																colData=colData+" "+tmp[m].trim();
															}
														}
														
														if("DATE OF BIRTH".equalsIgnoreCase(PDFColumns[k].trim())) {
															colData=colData.trim();
															if(colData.length()==4)
																colData="01-01-"+colData;
															else if(colData.length()>10)
																colData = colData.substring(0,10);
														}	
														
														Columnvalues.put(PDFColumns[k],colData);
														TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Columnvalues " + Columnvalues);
														TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("colData " + colData);
														TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("PDFColumns " + PDFColumns[k]);
													}
												}
											}
											
											String addinfo = Columnvalues.get("ADDITIONAL INFOS");
											addinfo=addinfo.replaceAll("'", "''");
											TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("addinfo " + addinfo);
														
											String Name = Columnvalues.get("NAME");
											Name=Name.replaceAll("'", "''");
											TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Name " + Name);
														
											trTableColumn = "WI_name,U_ID,Additiona_info,Name,Date_of_birth,Designation,Matchingtext,Origin,Nationality,Passport,user_data_1,Entity,EntityName,Reference_No";
											TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("trTableColumn" + trTableColumn);
														
											trTableValue = "'" + processInstanceID + "','" + Columnvalues.get("OFAC ID").toString().trim() + "'," + "'"
													+ addinfo + "','" + Name + "','" + Columnvalues.get("DATE OF BIRTH") + "'," + "'" + Columnvalues.get("DESIGNATION")
													+ "','" + Columnvalues.get("MATCHINGTEXT") + "','" + Columnvalues.get("ORIGIN") + "'," + "'" + Columnvalues.get("NATIONALITY") + "','" + Columnvalues.get("PASSPORT") + "','"
													+ Columnvalues.get("USER DATA 1")+"','"+Entity_Type+"','"+NAME+"','"+ReferenceNo+"'" ;
														
											String inputXML = getInputXMLInsert("USR_0_TF_GR_FIRCO_UID",cabinetName,sessionId);
											TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("ApInsert USR_0_TF_GR_FIRCO_UID: "+inputXML);
											String OutXml = WFNGExecute(inputXML, sJtsIp, iJtsPort,1);
											XMLParser xmlParserAPInsert = new XMLParser(OutXml);
											String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
											TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("ApInsert USR_0_TF_GR_FIRCO_UID: "+apInsertMaincode);
											if(apInsertMaincode.equalsIgnoreCase("0")) {
												
												TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("ApInsert successful: "+apInsertMaincode);
												TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Inserted in WiHistory table successfully.");
												
												updateExternalTable("USR_0_TF_FIRCO_DTLS_GRID","Fetch_Firco_Status","'Y'","WINAME='"+WINAME+"' and Entity_Type='"+Entity_Type+"' and Entity_Name='"+NAME+"' and SELECT_ID='"+SELECT_ID+"'", sJtsIp, iJtsPort, cabinetName);
												updateExternalTable("USR_0_TF_FIRCO_DTLS_GRID","Reference_No","'"+ReferenceNo+"'","WINAME='"+WINAME+"' and Entity_Type='"+Entity_Type+"' and Entity_Name='"+NAME+"' and SELECT_ID='"+SELECT_ID+"'", sJtsIp, iJtsPort, cabinetName);
												updateExternalTable("USR_0_TF_FIRCO_DTLS_GRID","Fetch_Firco_Date","'"+sdf1.format(d1)+"'","WINAME='"+WINAME+"' and Entity_Type='"+Entity_Type+"' and Entity_Name='"+NAME+"' and SELECT_ID='"+SELECT_ID+"'", sJtsIp, iJtsPort, cabinetName);
												
												// Ng rlos firco table only one row will insert for one request for each entity...
												
											    if (j==1) {
												   String FircoTable="NG_RLOS_FIRCO";
												   String Call_Type="Primary";									    
												   Calendar cal = Calendar.getInstance();
												   SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			   
												   String CurrDateTime = sdf.format(cal.getTime());

												   String columnnames="Process_name, Workitem_no, Firco_ID, StatusBehavior,StatusName, Request_datatime, Workstep_name, Newgen_status, AlertDetails, passport, Call_type, call_valid";
												   String columnvalues="'TF','"+processInstanceID+"','"+ReferenceNo+"','"+StatusBehavior+"','"+StatusName+"','"+CurrDateTime+"','"+ws_name+"','Pending','"+AlertDetailsTagResponse+"','"+PASSPORT_NUMBER+"','"+Call_Type+"','Y'";
												   String InputXML = CommonMethods.apInsert(cabinetName, sessionId, columnnames, columnvalues, FircoTable);
												   TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Input XML for apInsert "+FircoTable+" Table : "+InputXML);
												   String OutputXML=WFNGExecute(InputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
												   TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Output XML for apInsert "+FircoTable+" Table : "+OutputXML);
			
												   XMLParser sXMLParserChild = new XMLParser(OutputXML);
												   String StrMainCode = sXMLParserChild.getValueOf("MainCode");
													
												   if (StrMainCode.equals("0")) {
													   TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Successful in Inserting (NG_RLOS_FIRCO) the record in : "+FircoTable);	
												   }
												   else {
													   remarks="Error in NG_RLOS_FIRCO Insert Data";
													   decision="Fail";
													   TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Error in Executing apInsert NG_RLOS_FIRCO sOutputXML : "+OutputXML);
													   updateExternalTable("USR_0_TF_FIRCO_DTLS_GRID","Fetch_Firco_Status","'E'","WINAME='"+WINAME+"' and Entity_Type='"+Entity_Type+"' and Entity_Name='"+NAME+"' and SELECT_ID='"+SELECT_ID+"'", sJtsIp, iJtsPort, cabinetName);
												   }
											    }
											}
											else {
												remarks="Error in USR_0_TF_GR_FIRCO_UID Insert Data";
												decision="Fail";
												TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("ApInsert USR_0_TF_GR_FIRCO_UID failed: "+apInsertMaincode);
												updateExternalTable("USR_0_TF_FIRCO_DTLS_GRID","Fetch_Firco_Status","'E'","WINAME='"+WINAME+"' and Entity_Type='"+Entity_Type+"' and Entity_Name='"+NAME+"' and SELECT_ID='"+SELECT_ID+"'", sJtsIp, iJtsPort, cabinetName);
											}
											Columnvalues.clear();
											}
										}
								   }
								   catch(Exception e) {
									   
									   e.printStackTrace();
									   TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(" Exception After ng_FIRCO_InsertData: "+e.getMessage());
									   remarks="Error in Firco Insert Data";
									   decision="Fail";
									   updateExternalTable("USR_0_TF_FIRCO_DTLS_GRID","Fetch_Firco_Status","'E'","WINAME='"+WINAME+"' and Entity_Type='"+Entity_Type+"' and Entity_Name='"+NAME+"' and SELECT_ID='"+SELECT_ID+"'", sJtsIp, iJtsPort, cabinetName);
								   }
								}
							}
							if(rowVal.contains("No suspect detected")){
								
								updateExternalTable("USR_0_TF_FIRCO_DTLS_GRID","Fetch_Firco_Status","'Y'","WINAME='"+WINAME+"' and Entity_Type='"+Entity_Type+"' and Entity_Name='"+NAME+"' and SELECT_ID='"+SELECT_ID+"'", sJtsIp, iJtsPort, cabinetName);
								updateExternalTable("USR_0_TF_FIRCO_DTLS_GRID","Reference_No","'"+ReferenceNo+"'","WINAME='"+WINAME+"' and Entity_Type='"+Entity_Type+"' and Entity_Name='"+NAME+"'  and SELECT_ID='"+SELECT_ID+"'", sJtsIp, iJtsPort, cabinetName);
								updateExternalTable("USR_0_TF_FIRCO_DTLS_GRID","Fetch_Firco_Date","'"+sdf1.format(d1)+"'","WINAME='"+WINAME+"' and Entity_Type='"+Entity_Type+"' and Entity_Name='"+NAME+"'  and SELECT_ID='"+SELECT_ID+"'", sJtsIp, iJtsPort, cabinetName);
								
							}
						}
					}
					else {
						decision="Fail";
						remarks="Error in Firco Response : " ;
						updateExternalTable("USR_0_TF_FIRCO_DTLS_GRID","Fetch_Firco_Status","'E'","WINAME='"+WINAME+"' and Entity_Type='"+Entity_Type+"' and Entity_Name='"+NAME+"' and SELECT_ID='"+SELECT_ID+"'", sJtsIp, iJtsPort, cabinetName);
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+processInstanceID+", WSNAME: "+ws_name+", Error in Response of Fircosoft call"+return_code);
						}
					}
					//String FircoTableCheck= startFircoTableCheck(processInstanceID,Entity_Type,NAME);
					//TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("FircoTableCheck: "+FircoTableCheck);
										
					if("Fail".equalsIgnoreCase(decision)) {
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("decision: "+decision);
						completeWorkItem(cabinetName, sessionID, processInstanceID, decision, entryDateTime, remarks, sJtsIp, iJtsPort, ws_name);
					}
					else if("Success".equalsIgnoreCase(decision)) {
						String output = generate_firco_temp(processInstanceID,"Firco_Template");
						String[] docupload = output.split("~");
						String resp = docupload[0];
						
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("output Firco_Template: "+output);
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("decision: "+decision);
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("completeWorkItem");
						remarks="Firco Response success : ";
						
						if("0000".equalsIgnoreCase(resp)) {
							updateExternalTable("USR_0_TF_GR_FIRCO_UID","TEMP_GEN","'Y'","WI_name='"+processInstanceID+"'",  jtsIP, jtsPort, cabinetName);
							completeWorkItem(cabinetName, sessionID, processInstanceID, decision, entryDateTime, remarks, sJtsIp, iJtsPort, ws_name);
						}	
						else {
							decision="Fail";
							remarks="Error in Firco Template " ;
							completeWorkItem(cabinetName, sessionID, processInstanceID, decision, entryDateTime, remarks, sJtsIp, iJtsPort, ws_name);
						}
					}
					else {
						continue;
					}
				}
			}
		}
		catch (Exception e) {
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Exception: "+e.getMessage());
		}
	}

	private static String startFircoTableCheck(String processInstanceID,String Entity_Type,String NAME ) {
		
		java.util.Date d1 = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:.SSS");
		
		String AllStatusUpdate="";
		String firco_status="";
		String StatusBehavior="",Workitem_no="",Firco_ID="",Newgen_status="";
		
		String DBQuery = "select StatusBehavior,Workitem_no,Firco_ID,Newgen_status from NG_RLOS_FIRCO with (nolock) where Workitem_no ='" + processInstanceID + "'";
		List<Map<String, String>> DataFromDB = new ArrayList<Map<String, String>>();
		TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("DBQuery: "+DBQuery);
		DataFromDB = getDataFromDBMap(DBQuery, cabinetName, sessionID, jtsIP, jtsPort);
		
		for (Map<String, String> entry : DataFromDB) {
			
			StatusBehavior=entry.get("StatusBehavior");
			Workitem_no=entry.get("Workitem_no");
			Firco_ID=entry.get("Firco_ID");
			Newgen_status=entry.get("Newgen_status");
			
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("StatusBehavior: "+StatusBehavior);
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Workitem_no: "+Workitem_no);
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Firco_ID: "+Firco_ID);
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Newgen_status: "+Newgen_status);
			
			if (StatusBehavior.equalsIgnoreCase("0000") || StatusBehavior.equalsIgnoreCase("FFF_OK") || StatusBehavior.equalsIgnoreCase("FFFBAD")) {
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("StatusBehavior : " + StatusBehavior);
				if (StatusBehavior.equalsIgnoreCase("FFFBAD")) {
					firco_status="BAD";
					updateExternalTable("NG_RLOS_FIRCO","Newgen_status","Done","Firco_ID='"+Firco_ID+"' and Workitem_no='"+Workitem_no+"'",  jtsIP, jtsPort, cabinetName);
					updateExternalTable("USR_0_TF_FIRCO_DTLS_GRID","Status_update_date","'"+sdf1.format(d1)+"'","WINAME='"+processInstanceID+"' and Entity_Type='"+Entity_Type+"' and Entity_Name='"+NAME+"' and Fetch_Firco_Status is null", jtsIP, jtsPort, cabinetName);
					updateExternalTable("USR_0_TF_FIRCO_DTLS_GRID","Firco_status","'"+firco_status+"'","WINAME='"+processInstanceID+"' and Entity_Type='"+Entity_Type+"' and Entity_Name='"+NAME+"'", jtsIP, jtsPort, cabinetName);
				} 
				else if (StatusBehavior.equalsIgnoreCase("0000") || StatusBehavior.equalsIgnoreCase("FFF_OK")) {
					firco_status="Clear";
					updateExternalTable("NG_RLOS_FIRCO","Newgen_status","Done","Firco_ID='"+Firco_ID+"' and Workitem_no='"+Workitem_no+"'",  jtsIP, jtsPort, cabinetName);	
					updateExternalTable("USR_0_TF_FIRCO_DTLS_GRID","Status_update_date","'"+sdf1.format(d1)+"'","WINAME='"+processInstanceID+"' and Entity_Type='"+Entity_Type+"' and Entity_Name='"+NAME+"'", jtsIP, jtsPort, cabinetName);
					updateExternalTable("USR_0_TF_FIRCO_DTLS_GRID","Firco_status","'"+firco_status+"'","WINAME='"+processInstanceID+"' and Entity_Type='"+Entity_Type+"' and Entity_Name='"+NAME+"'", jtsIP, jtsPort, cabinetName);
					
				}
			}
		}
		
		String DBQuery_Done = "select count(Workitem_no) as 'Cases_pending_WI' from NG_RLOS_FIRCO with (nolock) where Newgen_status ='Pending' and Workitem_no ='" + processInstanceID + "'";
		List<Map<String, String>> DataFromDB_1 = new ArrayList<Map<String, String>>();
		TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("DBQuery_Done: "+DBQuery_Done);
		DataFromDB_1 = getDataFromDBMap(DBQuery_Done, cabinetName, sessionID, jtsIP, jtsPort);
		
		String Count="";
		
		for (Map<String, String> entry : DataFromDB) {
			Count=entry.get("Cases_pending_WI");
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Cases_pending_WI: "+Count);
		}
		if("0".equalsIgnoreCase(Count)) {
			AllStatusUpdate="Y";
		}
		else {
			AllStatusUpdate="N";
		}
		
		TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("AllStatusUpdate: "+AllStatusUpdate);
		return AllStatusUpdate;
	}
	
	
	private String getInputXMLInsert(String tableName,String sCabinetName,String sSessionID) {
		
		return "<?xml version=\"1.0\"?>" +
				"<APInsert_Input>" +
				"<Option>APInsert</Option>" +
				"<TableName>" + tableName + "</TableName>" +
				"<ColName>" + trTableColumn + "</ColName>" +
				"<Values>" + trTableValue + "</Values>" +
				"<EngineName>" + sCabinetName + "</EngineName>" +
				"<SessionId>" + sSessionID + "</SessionId>" +
	            "</APInsert_Input>";
	}
	
	private int readConfig() {
		Properties p = null;
		try {
			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "TF_Firco_Config.properties")));
			Enumeration<?> names = p.propertyNames();
			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
			    configParamMap.put(name, p.getProperty(name));
			}
		}
		catch (Exception e) {
			return -1 ;
		}
		return 0;
	}
	
	public static long convertDateToLong(String date) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date date_long = null;
		try {
			date_long = simpleDateFormat.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date_long.getTime();
	}
	
	public static String FircoAPI_Input(String RequestingUnitName,String recordType,String ReferenceNo,String FullName) {
		
		java.util.Date d1 = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm");
		String DateExtra2 = sdf1.format(d1)+"+04:00";

		String sInputXML = "<EE_EAI_MESSAGE>\n"+
				"<EE_EAI_HEADER>\n"+
				"<MsgFormat>COMPLIANCE_CHECK</MsgFormat>\n"+
				"<MsgVersion>0001</MsgVersion>\n"+
				"<RequestorChannelId>BPM</RequestorChannelId>\n"+
				"<RequestorUserId>RAKUSER</RequestorUserId>\n"+
				"<RequestorLanguage>E</RequestorLanguage>\n"+
				"<RequestorSecurityInfo>secure</RequestorSecurityInfo>\n"+
				"<ReturnCode>911</ReturnCode>\n"+
				"<ReturnDesc>Issuer Timed Out</ReturnDesc>\n"+
				"<MessageId>Test123456</MessageId>\n"+
				"<Extra1>REQ||PERCOMER.PERCOMER</Extra1>\n"+
				"<Extra2>"+DateExtra2+"</Extra2>\n"+
				"</EE_EAI_HEADER>\n"+
				"<ComplianceCheckRequest><DisplayAlertsFlag>0</DisplayAlertsFlag>" +
				"<RetryRequiredFlag>N</RetryRequiredFlag><RequestingUnitName>"+RequestingUnitName+"</RequestingUnitName>" +
				"<RecordType>"+recordType+"</RecordType>"+
				"<ReferenceNo>"+ReferenceNo+"</ReferenceNo>" +
				"<EntityName>"+FullName+"</EntityName>" +
				"</ComplianceCheckRequest>"+
				"</EE_EAI_MESSAGE>";
		
		return sInputXML;
	}
	

	protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort, int flag)
			throws IOException, Exception {
		TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("In WF NG Execute : " + serverPort);
		try {
			if (serverPort.startsWith("33"))
				return WFCallBroker.execute(ipXML, jtsServerIP, Integer.parseInt(serverPort), 1);
			else
				return ngEjbClient.makeCall(jtsServerIP, serverPort, "WebSphere", ipXML);
		} catch (Exception e) {
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Exception Occured in WF NG Execute : " + e.getMessage());
			e.printStackTrace();
			return "Error";
		}
	}
	
	private static List<Map<String, String>> getDataFromDBMap(String query, String cabinetName, String sessionID, String jtsIP, String jtsPort) {
		List<Map<String, String>> temp = new ArrayList<Map<String, String>>();
		
		try {
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.info("Inside function getDataFromDB");
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.info("getDataFromDB query is: " + query);
			String InputXML = CommonMethods.apSelectWithColumnNames(query, cabinetName, sessionID);
			String OutXml = WFNGExecute(InputXML, jtsIP, jtsPort, 1);
			OutXml = OutXml.replaceAll("&", "#andsymb#");
			Document recordDoc1 = MapXML.getDocument(OutXml);
			NodeList records1 = recordDoc1.getElementsByTagName("Record");
			if (records1.getLength() > 0) {
				for (int i = 0; i < records1.getLength(); i++) {
					Node n = records1.item(i);
					Map<String, String> t = new HashMap<String, String>();
					if (n.hasChildNodes()) {
						NodeList child = n.getChildNodes();
						for (int j = 0; j < child.getLength(); j++) {
							Node n1 = child.item(j);
							String column = n1.getNodeName();
							String value = n1.getTextContent().replaceAll("#andsymb#", "&");
							if (null != value && !"null".equalsIgnoreCase(value) && !"".equals(value)) {
								TF_Firco_Handling_Log.TF_Firco_HandlingLogger.info("getDataFromDBMap Setting value of " + column + " as " + value);
								t.put(column, value);
							} else {
								TF_Firco_Handling_Log.TF_Firco_HandlingLogger.info("getDataFromDBMap Setting value of " + column + " as blank");
								t.put(column, "");
							}
						}
					}
					temp.add(t);
				}
			}

		} catch (Exception e) {
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.info("Exception occured in getDataFromDBMap method" + e.getMessage());
		}
		return temp;

	}
	
	static String socketConnection(String cabinetName, String username, String sessionId, String sJtsIp, String iJtsPort, String wi_name, String ws_name,
		int connection_timeout, int integrationWaitTime, HashMap<String, String> socketDetailsMap, StringBuilder sInputXML) {

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
		
		try {

			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("userName "+ username);
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("SessionId "+ sessionId);

			socketServerIP=socketDetailsMap.get("SocketServerIP");
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("SocketServerIP "+ socketServerIP);
			socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("SocketServerPort "+ socketServerPort);

	   		if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0)) {

    			socket = new Socket(socketServerIP, socketServerPort);
    			socket.setSoTimeout(connection_timeout*1000);
    			out = socket.getOutputStream();
    			socketInputStream = socket.getInputStream();
    			dout = new DataOutputStream(out);
    			din = new DataInputStream(socketInputStream);
    			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Dout " + dout);
    			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Din " + din);

    			outputResponse = "";

    			inputRequest = getRequestXML( cabinetName,sessionId ,wi_name, ws_name, username, sInputXML);

    			if (inputRequest != null && inputRequest.length() > 0) {
    				int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
    				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("RequestLen: "+inputRequestLen + "");
    				inputRequest = inputRequestLen + "##8##;" + inputRequest;
    				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
    				dout.write(inputRequest.getBytes("UTF-16LE"));dout.flush();
    			}
    			 
    			byte[] readBuffer = new byte[500];
    			int num = din.read(readBuffer);
    			if (num > 0) {

    				byte[] arrayBytes = new byte[num];
    				System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
    				outputResponse = outputResponse+ new String(arrayBytes, "UTF-16LE");
					inputMessageID = outputResponse;
    				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("OutputResponse: "+outputResponse);

    				if(!"".equalsIgnoreCase(outputResponse))
    					outputResponse = getResponseXML(cabinetName,sJtsIp,iJtsPort,sessionId, wi_name,outputResponse,integrationWaitTime );

    				if(outputResponse.contains("&lt;")) {
    					outputResponse=outputResponse.replaceAll("&lt;", "<");
    					outputResponse=outputResponse.replaceAll("&gt;", ">");
    				}
    			}
    			socket.close();

				outputResponse = outputResponse.replaceAll("</MessageId>","</MessageId>/n<InputMessageId>"+inputMessageID+"</InputMessageId>");
				return outputResponse;

    	 		}

    		else {
    			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("SocketServerIp and SocketServerPort is not maintained "+"");
    			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("SocketServerIp is not maintained "+	socketServerIP);
    			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(" SocketServerPort is not maintained "+	socketServerPort);
    			return "Socket Details not maintained";
    		}
		}

		catch (Exception e) {
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
			return "";
		}
		finally {
			try {
				if(out != null) {
					out.close();
					out=null;
				}
				if(socketInputStream != null) {

					socketInputStream.close();
					socketInputStream=null;
				}
				if(dout != null) {

					dout.close();
					dout=null;
				}
				if(din != null) {

					din.close();
					din=null;
				}
				if(socket != null) {
					if(!socket.isClosed())
						socket.close();
					socket=null;
				}

			}

			catch(Exception e) {
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
			}
		}
	}
	
	private static String getResponseXML(String cabinetName,String sJtsIp,String iJtsPort, String sessionId, String wi_name, String message_ID, int integrationWaitTime) {

		String outputResponseXML="";
		try {
			String QueryString = "select OUTPUT_XML from NG_TF_XMLLOG_HISTORY with (nolock) where MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+wi_name+"'";

			String responseInputXML =CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, sessionId);
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Response APSelect InputXML: "+responseInputXML);

			int Loop_count=0;
			do {
				String responseOutputXML=CommonMethods.WFNGExecute(responseInputXML,sJtsIp,iJtsPort,1);
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Response APSelect OutputXML: "+responseOutputXML);

			    XMLParser xmlParserSocketDetails= new XMLParser(responseOutputXML);
			    String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			    TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("ResponseMainCode: "+responseMainCode);

			    int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			    TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("ResponseTotalRecords: "+responseTotalRecords);

			    if (responseMainCode.equals("0") && responseTotalRecords > 0) {

					String responseXMLData=xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData =responseXMLData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

	        		XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
	        		//TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("ResponseXMLData: "+responseXMLData);

	        		outputResponseXML=xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
	        		//TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("OutputResponseXML: "+outputResponseXML);

	        		if("".equalsIgnoreCase(outputResponseXML)){
	        			outputResponseXML="Error";
	    			}
	        		break;
				}
			    Loop_count++;
			    Thread.sleep(1000);
			}
			while(Loop_count<integrationWaitTime);
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("integrationWaitTime: "+integrationWaitTime);

		}
		catch(Exception e) {
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Exception occurred in outputResponseXML" + e.getMessage());
			outputResponseXML="Error";
		}
		return outputResponseXML;
	}
	
	private static String getRequestXML(String cabinetName, String sessionId, String wi_name, String ws_name, String userName, StringBuilder sInputXML) {
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("<APMQPUTGET_Input>");
		strBuff.append("<SessionId>" + sessionId + "</SessionId>");
		strBuff.append("<EngineName>" + cabinetName + "</EngineName>");
		strBuff.append("<XMLHISTORY_TABLENAME>NG_TF_XMLLOG_HISTORY</XMLHISTORY_TABLENAME>");
		strBuff.append("<WI_NAME>" + wi_name + "</WI_NAME>");
		strBuff.append("<WS_NAME>" + ws_name + "</WS_NAME>");
		strBuff.append("<USER_NAME>" + userName + "</USER_NAME>");
		strBuff.append("<MQ_REQUEST_XML>");
		strBuff.append(sInputXML);
		strBuff.append("</MQ_REQUEST_XML>");
		strBuff.append("</APMQPUTGET_Input>");
		TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("GetRequestXML: "+ strBuff.toString());
		return strBuff.toString();
	}
	
	private static  HashMap<String, String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort, String sessionID) {
		HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

		try {
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Fetching Socket Connection Details.");
			System.out.println("Fetching Socket Connection Details.");

			String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'TF' and CallingSource = 'Utility'";

			String socketDetailsInputXML = CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Socket Details APSelect InputXML: " + socketDetailsInputXML);

			String socketDetailsOutputXML = WFNGExecute(socketDetailsInputXML, sJtsIp, iJtsPort, 1);
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Socket Details APSelect OutputXML: " + socketDetailsOutputXML);

			XMLParser xmlParserSocketDetails = new XMLParser(socketDetailsOutputXML);
			String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("SocketDetailsMainCode: " + socketDetailsMainCode);

			int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("SocketDetailsTotalRecords: " + socketDetailsTotalRecords);

			if (socketDetailsMainCode.equalsIgnoreCase("0") && socketDetailsTotalRecords > 0) {
				String xmlDataSocketDetails = xmlParserSocketDetails.getNextValueOf("Record");
				xmlDataSocketDetails = xmlDataSocketDetails.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

				XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

				String socketServerIP = xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("SocketServerIP: " + socketServerIP);
				socketDetailsMap.put("SocketServerIP", socketServerIP);

				String socketServerPort = xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("SocketServerPort " + socketServerPort);
				socketDetailsMap.put("SocketServerPort", socketServerPort);

				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("SocketServer Details found.");
				System.out.println("SocketServer Details found.");

			}
		} catch (Exception e) {
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Exception in getting Socket Connection Details: " + e.getMessage());
			System.out.println("Exception in getting Socket Connection Details: " + e.getMessage());
		}

		return socketDetailsMap;
	}
	
	private String completeWorkItem(String cabinetName, String sessionID, String processInstanceID, String decision, String entryDateTime,String remarks, String jtsIP,String jtsPort,String ws_name ) {
		String workItemId = "1";
		try {
			String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionID, processInstanceID, workItemId);
			String getWorkItemOutputXml = CommonMethods.WFNGExecute(getWorkItemInputXML, jtsIP, jtsPort, 1);

			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Output XML for getWorkItem is " + getWorkItemOutputXml);

			XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
			String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");

			if ("0".equals(getWorkItemMainCode)) {
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.info("get Workitem call successfull for " + processInstanceID);
				String attrbuteTag = "<Decision>" + decision + "</Decision>";
				String assignWorkitemAttributeInputXML = CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionID, processInstanceID, workItemId, attrbuteTag);
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Input XML for assign Attribute is " + assignWorkitemAttributeInputXML);

				String assignWorkitemAttributeOutputXML = CommonMethods.WFNGExecute(assignWorkitemAttributeInputXML, jtsIP, jtsPort, 1);
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Output XML for assign Attribues is " + assignWorkitemAttributeOutputXML);

				XMLParser xmlParserAssignAtt = new XMLParser(assignWorkitemAttributeOutputXML);

				String mainCodeAssignAtt = xmlParserAssignAtt.getValueOf("MainCode");
				if ("0".equals(mainCodeAssignAtt.trim())) {
					String completeWorkItemInputXML = CommonMethods.completeWorkItemInput(cabinetName, sessionID, processInstanceID, workItemId);

					TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Input XML for complete WI is " + completeWorkItemInputXML);

					TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Input XML for wmcompleteWorkItem: " + completeWorkItemInputXML);

					String completeWorkItemOutputXML = CommonMethods.WFNGExecute(completeWorkItemInputXML, jtsIP, jtsPort, 1);
					TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Output XML for wmcompleteWorkItem: " + completeWorkItemOutputXML);

					XMLParser xmlParserCompleteWorkitem = new XMLParser(completeWorkItemOutputXML);
					String completeWorkitemMaincode = xmlParserCompleteWorkitem.getValueOf("MainCode");
					TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Status of wmcompleteWorkItem  " + completeWorkitemMaincode);

					if ("0".equals(completeWorkitemMaincode)) {
						
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WmCompleteWorkItem successful: " + completeWorkitemMaincode);
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WorkItem moved to next Workstep.");

						SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
						SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

						Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
						String formattedEntryDatetime = outputDateFormat.format(entryDatetimeFormat);
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("FormattedEntryDatetime: " + formattedEntryDatetime);

						Date actionDateTime = new Date();
						String formattedActionDateTime = outputDateFormat.format(actionDateTime);
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("FormattedActionDateTime: " + formattedActionDateTime);
						
						String columnNames = "winame,actiondatetime,wsname,username,decision,entrydatetime,remarks,actual_wsname";
						String columnValues = "'" + processInstanceID + "','" + formattedActionDateTime + "','"+ws_name+"','" + CommonConnection.getUsername() + "','" + decision + "','"
								+ formattedEntryDatetime + "','"+remarks+"','"+ws_name+"'";
						
						String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionID, columnNames, columnValues, "usr_0_tf_wihistory");
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("APInsertInputXML: " + apInsertInputXML);

						String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, jtsIP, jtsPort, 1);
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("APInsertOutputXML: " + apInsertOutputXML);

						XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
						String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Status of apInsertMaincode  " + apInsertMaincode);
						if (apInsertMaincode.equalsIgnoreCase("0")) {
							TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("ApInsert successful: " + apInsertMaincode);
							TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Inserted in WiHistory table successfully.");
						} else {
							TF_Firco_Handling_Log.TF_Firco_HandlingLogger.error("ApInsert failed: " + apInsertMaincode);
						}
					} else {
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.error("Error in completeWI call for " + processInstanceID);
					}
				} else {
					TF_Firco_Handling_Log.TF_Firco_HandlingLogger.error("Error in Assign Attribute call for WI " + processInstanceID);
				}

			} else {
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.error("Error in getWI call for WI " + processInstanceID);
			}
		}

		catch (Exception e) {
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.error("Exception " + e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.error("Exception Occurred in TAO Integration Thread : " + result);
			System.out.println("Exception " + e);

		}
		return "";
	}
	
	private static void updateExternalTable(String tablename, String columnname,String sMessage, String sWhere, String jtsIP, String jtsPort, String cabinetName) {
		int sessionCheckInt=0;
		int loopCount=50;
		int mainCode = 0;
		TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Inside update EXT table: ");
		
		while(sessionCheckInt<loopCount) {
			try {
				XMLParser objXMLParser = new XMLParser();
				String inputXmlcheckAPUpdate = CommonMethods.getAPUpdateIpXML(tablename,columnname,sMessage,sWhere,cabinetName,sessionID);
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(("inputXmlcheckAPUpdate : " + inputXmlcheckAPUpdate));
				String outXmlCheckAPUpdate=null;
				outXmlCheckAPUpdate=WFNGExecute(inputXmlcheckAPUpdate,jtsIP,jtsPort,1);
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(("outXmlCheckAPUpdate : " + outXmlCheckAPUpdate));
				objXMLParser.setInputXML(outXmlCheckAPUpdate);
				String mainCodeforCheckUpdate = null;
				mainCodeforCheckUpdate=objXMLParser.getValueOf("MainCode");
				if (!mainCodeforCheckUpdate.equalsIgnoreCase("0")) {
					TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(("Exception in ExecuteQuery_APUpdate updating "+tablename+" table"));
					System.out.println("Exception in ExecuteQuery_APUpdate updating "+tablename+" table");
				}
				else {
					TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(("Succesfully updated "+tablename+" table"));
				}
				mainCode=Integer.parseInt(mainCodeforCheckUpdate);
				if (mainCode == 11) {
					sessionID  = CommonConnection.getSessionID(TF_Firco_Handling_Log.TF_Firco_HandlingLogger, false);
				}
				else {
					sessionCheckInt++;
					break;
				}
				if (outXmlCheckAPUpdate.equalsIgnoreCase("") || outXmlCheckAPUpdate == "" || outXmlCheckAPUpdate == null)
					break;
			}
			catch(Exception e) {
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(("Exception in "+tablename+" table"));
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(("Inside create validateSessionID exception"+e.getMessage()));
			}
		}
	}
	
	
	private static String generate_firco_temp(String WINAME,String pdfName) {
		
		TemplatepathfromPrp=configParamMap.get("TemplateFilePath");
		TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("TemplatepathfromPrp " + TemplatepathfromPrp);
		
		try {
			
			String response="";
			String TemplatePath = TemplatepathfromPrp+ System.getProperty("file.separator") +WINAME + pdfName + ".pdf";
			com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4.rotate());
			com.itextpdf.text.pdf.PdfWriter.getInstance(document, new FileOutputStream(TemplatePath));
			document.open();
			Font bold = new Font(FontFamily.HELVETICA, 12, Font.BOLD);
			
			Date d = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String CurrentDateTime = dateFormat.format(d);
			
			document.add(Image.getInstance(TemplatepathfromPrp+ System.getProperty("file.separator") + "Logo.png"));
			   
			 // heading 
			 Paragraph rakbankHeading = new Paragraph("\t"+"\t"+"\t"+"                                                                                     FIRCO DETAILS ");
			 document.add(rakbankHeading);
			 
			 //space
			 document.add(new Paragraph("\n"));
			 
			 // Wi name and Date 
			 
			 Paragraph wiName_date = new Paragraph(   "                       WI Name : " +" "+ WINAME+ "\n" + "                       Current Date : " + CurrentDateTime);
			 document.add(wiName_date);
			 
			 document.add(new Paragraph("\n"));
			 
			 document.add(new Paragraph("\n"));
			 PdfPTable personalInfoTable = new PdfPTable(2);
			 PdfPCell personalInfoCell = new PdfPCell(new Phrase(""));
			 personalInfoCell.setBackgroundColor(new BaseColor(255,251,240));
			 personalInfoTable.addCell(personalInfoCell);
			 
			 personalInfoCell = new PdfPCell(new Phrase(""));
			 personalInfoCell.setBackgroundColor(new BaseColor(235, 235, 224));
			 personalInfoTable.addCell(personalInfoCell);
			 personalInfoTable.setHeaderRows(1);
			 
/*			 personalInfoTable.addCell("Customer Name As Per Passport");
		//	 personalInfoTable.addCell(CustomerPP);
			 personalInfoTable.addCell("CIF");
		//	 personalInfoTable.addCell(CIF);
*/			
			 document.add(personalInfoTable);
			
			document.add(new Paragraph("\n"));
			 
			Font fontRed = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD,new BaseColor(230, 0, 0));
			 
			Paragraph UID  = new Paragraph("                      Alert Details ", fontRed);
			document.add(UID);
			 
			document.add(new Paragraph("\n"));
	
			PdfPTable UID_pdf = new PdfPTable(13);
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("UID_pdf :"+ UID_pdf);
			
			int[] columnWidths = {9,9,9,9,9,9,9,9,9,25,9,9,9};
	       
			UID_pdf.setWidths(columnWidths);
			UID_pdf.setWidthPercentage(100); 
	       
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("After width set");
	       
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , Before PdfPTable 1:");
			PdfPCell c1 = new PdfPCell(new Phrase("OFAC ID"));
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable UID 1:");
				
	       PdfPCell c2 = new PdfPCell(new Phrase("Matching Text"));
	       TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable 2:");
	       
	       PdfPCell c3 = new PdfPCell(new Phrase("Name"));
	       TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable 3:");
	       
	       PdfPCell c4 = new PdfPCell(new Phrase("Origin"));
	       TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable 4:");
	       
	       PdfPCell c5 = new PdfPCell(new Phrase("Designation"));
	       TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable UID 5:");
	       
	       PdfPCell c6 = new PdfPCell(new Phrase("Date of Birth"));
	       TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable 6:");
	       
	       PdfPCell c7 = new PdfPCell(new Phrase("User Data 1"));
	       TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable 7:");
	       
	       PdfPCell c8 = new PdfPCell(new Phrase("Nationality"));
	       TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable 8:");
	       
	       PdfPCell c9 = new PdfPCell(new Phrase("Passport"));
	        TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable 9:");
	       
	       PdfPCell c10 = new PdfPCell(new Phrase("Additional Info"));
	       TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable UID 10:");
	       
	       PdfPCell c11 = new PdfPCell(new Phrase("Entity"));
	       TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable UID 11:");
	
	       PdfPCell c12 = new PdfPCell(new Phrase("Entity Name"));
	       TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable UID c12:");
	
	       PdfPCell c13 = new PdfPCell(new Phrase("Reference Number"));
	       TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable UID c13:");
	       
	       try {
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable UID append");  
				UID_pdf.addCell(c1);
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable append 1:");  
				UID_pdf.addCell(c2);
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable append 2:");  
				UID_pdf.addCell(c3);
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable append 3 :");  
				UID_pdf.addCell(c4);
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable append 4:"); 
				UID_pdf.addCell(c5);
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable append 5:"); 
				UID_pdf.addCell(c6);
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable append 6:"); 
				UID_pdf.addCell(c7);
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable append 7:"); 
				UID_pdf.addCell(c8);
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable append 8:"); 
				UID_pdf.addCell(c9);
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable append 8:"); 
				UID_pdf.addCell(c10);
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable append 8:"); 
				UID_pdf.addCell(c11);
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable append 8:"); 
				UID_pdf.addCell(c12);
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable append 8:"); 
				UID_pdf.addCell(c13);
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , After PdfPTable append 8:"); 
			}
			catch(Exception e) {
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("WINAME : "+WINAME+", WSNAME: , In catch After UID_pdf UID : "+e.getMessage());
			}
	       
	       String DBQuery = "SELECT U_ID,Matchingtext,Name,Origin,Designation,Date_of_birth,user_data_1,Nationality,Passport,Additiona_info,Entity,EntityName,Reference_No FROM USR_0_TF_GR_FIRCO_UID WITH (NOLOCK) Where WI_name='" + WINAME + "' and TEMP_GEN is NULL";
	       List<Map<String, String>> DataFromDB = new ArrayList<Map<String, String>>();
	       TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("DBQuery: "+DBQuery);
	       DataFromDB = getDataFromDBMap(DBQuery, cabinetName, sessionID, jtsIP, jtsPort);
			
	       for (Map<String, String> entry : DataFromDB) {
	    	   int size = DataFromDB.size();
	    	   TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("size : "+size+"");
				
	    	   c1 = new PdfPCell(new Phrase(entry.get("U_ID")));
	    	   TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("OFAC_ID "+entry.get("U_ID"));
				c1.setBackgroundColor(new BaseColor(255,251,240));
				c1.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
				UID_pdf.addCell(c1);
				
				c2 = new PdfPCell(new Phrase(entry.get("Matchingtext")));
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(entry.get("Matchingtex"));
				c2.setBackgroundColor(new BaseColor(255,251,240));
	            c2.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
				UID_pdf.addCell(c2);
				
				c3 = new PdfPCell(new Phrase(entry.get("Name")));
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(entry.get("Name"));
				c3.setBackgroundColor(new BaseColor(255,251,240));
	            c3.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
				UID_pdf.addCell(c3);
				
				c4 = new PdfPCell(new Phrase(entry.get("Origin")));
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(entry.get("Origin"));
				c4.setBackgroundColor(new BaseColor(255,251,240));
	            c4.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
				UID_pdf.addCell(c4);
				
				c5 = new PdfPCell(new Phrase(entry.get("Designation")));
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(entry.get("Designation"));
				c5.setBackgroundColor(new BaseColor(255,251,240));
				c5.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
				UID_pdf.addCell(c5);
				
				c6 = new PdfPCell(new Phrase(entry.get("Date_of_birth")));
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(entry.get("Date_of_birth"));
				c6.setBackgroundColor(new BaseColor(255,251,240));
				c6.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
				UID_pdf.addCell(c6);
				
				c7 = new PdfPCell(new Phrase(entry.get("user_data_1")));
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(entry.get("user_data_1"));
				c7.setBackgroundColor(new BaseColor(255,251,240));
				c7.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
				UID_pdf.addCell(c7);
				
				c8 = new PdfPCell(new Phrase(entry.get("Nationality")));
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(entry.get("Nationality"));
				c8.setBackgroundColor(new BaseColor(255,251,240));
				c8.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
				UID_pdf.addCell(c8);
				
				c9 = new PdfPCell(new Phrase(entry.get("Passport")));
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(entry.get("Passport"));
				c9.setBackgroundColor(new BaseColor(255,251,240));
				c9.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
				UID_pdf.addCell(c9);
				
				c10 = new PdfPCell(new Phrase(entry.get("Additiona_info")));
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(entry.get("Additiona_info"));
				c10.setBackgroundColor(new BaseColor(255,251,240));
				c10.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
				UID_pdf.addCell(c10);
				
				c11 = new PdfPCell(new Phrase(entry.get("Entity")));
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(entry.get("Entity"));
				c11.setBackgroundColor(new BaseColor(255,251,240));
				c11.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
				UID_pdf.addCell(c11);
				
				c12 = new PdfPCell(new Phrase(entry.get("EntityName")));
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(entry.get("EntityName"));
				c12.setBackgroundColor(new BaseColor(255,251,240));
				c12.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
				UID_pdf.addCell(c12);
				
				c13 = new PdfPCell(new Phrase(entry.get("Reference_No")));
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(entry.get("Reference_No"));
				c13.setBackgroundColor(new BaseColor(255,251,240));
				c13.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
				UID_pdf.addCell(c13);
			}
			document.add(UID_pdf);
			document.add(new Paragraph("\n"));
			document.close();
			
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("generate_firco_temp " + document);
			response = AttachDocumentWithWI(WINAME,pdfName );
			
			return response;
		}
		catch(Exception e){
			System.out.print("Exception generate_dedupe_temp : "+e.getMessage());
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Exception generate_dedupe_temp ;" + e.getMessage());
			return "error";
		}
	}
	
	public static String SearchExistingDoc( String pid, String FrmType, String sCabname, String sSessionId, String sJtsIp, int iJtsPort_int, String sFilepath) {
		
		try {
			String strFolderIndex = "";
			String strImageIndex = "";
			
			String strInputQry1 = "SELECT FOLDERINDEX,ImageVolumeIndex FROM PDBFOLDER WITH(NOLOCK) WHERE NAME='" + pid + "'";
			
			short iJtsPort = (short) iJtsPort_int;
			
			List<Map<String, String>> DataFromDB = new ArrayList<Map<String, String>>();
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("DBQuery: "+strInputQry1);
			DataFromDB = getDataFromDBMap(strInputQry1, cabinetName, sessionID, jtsIP, jtsPort);
			for (Map<String, String> entry : DataFromDB) {
				strFolderIndex = entry.get("FOLDERINDEX");
				strImageIndex = VolumeId;
			}
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("strFolderIndex: " + strFolderIndex);
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("strImageIndex: " + strImageIndex);

			String strInputQry2 = "SELECT a.documentindex,b.ParentFolderIndex FROM PDBDOCUMENT A WITH (NOLOCK), PDBDOCUMENTCONTENT B WITH (NOLOCK)"
			+ "WHERE A.DOCUMENTINDEX= B.DOCUMENTINDEX AND A.NAME IN ('" + FrmType+ "','') AND B.PARENTFOLDERINDEX ='" + strFolderIndex + "'";
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("sInputXML: " + strInputQry2);
			
			List<Map<String, String>> dataFromDB2 = new ArrayList<Map<String, String>>();
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("dataFromDB2: " + dataFromDB2);
			dataFromDB2 = getDataFromDBMap(strInputQry2, cabinetName, sessionID, jtsIP, jtsPort);

			ArrayList<String> strdocumentindex = new ArrayList<String>(dataFromDB2.size());
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("strdocumentindex: " + strdocumentindex);
			ArrayList<String> strParentFolderIndex = new ArrayList<String>(dataFromDB2.size());
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("strParentFolderIndex: " + strParentFolderIndex);

			for (Map<String, String> tableFrmDB2 : dataFromDB2) {
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("tableFrmDB2: " + tableFrmDB2);
				strdocumentindex.add(tableFrmDB2.get("documentindex").trim());
				strParentFolderIndex.add(tableFrmDB2.get("ParentFolderIndex").trim());
			}
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("strdocumentindex: " + strdocumentindex);
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("strParentFolderIndex: " + strParentFolderIndex);
			
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("dataFromDB2.size();: " + dataFromDB2.size());
				
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("dataFromDB2.isEmpty: " + dataFromDB2.isEmpty());
			
			try {
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Inside Adding PN File: ");
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("sFilepath: " + sFilepath);
				String filepath = sFilepath;

				File newfile = new File(filepath);
				String name = newfile.getName();
				String ext = "";
				String sMappedInputXml = "";
				if (name.contains(".")) {
					ext = name.substring(name.lastIndexOf("."), name.length());
				}
				JPISIsIndex ISINDEX = new JPISIsIndex();
				JPDBRecoverDocData JPISDEC = new JPDBRecoverDocData();
				String strDocumentPath = sFilepath;
				File processFile = null;
				long lLngFileSize = 0L;
				processFile = new File(strDocumentPath);

				lLngFileSize = processFile.length();
				String lstrDocFileSize = "";
				lstrDocFileSize = Long.toString(lLngFileSize);

				String createdbyappname = "";
				createdbyappname = ext.replaceFirst(".", "");
				Short volIdShort = Short.valueOf(strImageIndex);

				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("lLngFileSize: --" + lLngFileSize);
					
				if (lLngFileSize != 0L) {
					TF_Firco_Handling_Log.TF_Firco_HandlingLogger
						.debug("sJtsIp --" + sJtsIp + " iJtsPort-- " + iJtsPort + " sCabname--" + sCabname
						+ " volIdShort.shortValue() --" + volIdShort.shortValue() + " strDocumentPath--"
						+ strDocumentPath + " JPISDEC --" + JPISDEC + "  ISINDEX-- " + ISINDEX);
						CPISDocumentTxn.AddDocument_MT(null, sJtsIp, iJtsPort, sCabname, volIdShort.shortValue(),strDocumentPath, JPISDEC, "", ISINDEX);
				}
				
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("dataFromDB2.size(): --" + dataFromDB2.size());
				/*if (dataFromDB2.size() > 0) {
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
					Date date = new Date(System.currentTimeMillis());
					String strCurrDateTime = formatter.format(date);
					for (int i = 0; i < dataFromDB2.size(); i++) {
						TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("NGOChangeDocumentProperty_Input section");
						sMappedInputXml = "<?xml version=\"1.0\"?>" + "<NGOChangeDocumentProperty_Input>"
								+ "<Option>NGOChangeDocumentProperty</Option>" + "<CabinetName>" + sCabname
								+ "</CabinetName>" + "<UserDBId>" + sSessionId
								+ "</UserDBId><Document><DocumentIndex>" + strdocumentindex.get(i)
								+ "</DocumentIndex><NoOfPages>1</NoOfPages>" + "<DocumentName>" + FrmType
								+ "</DocumentName>" + "<AccessDateTime>" + strCurrDateTime + "</AccessDateTime>"
								+ "<ExpiryDateTime>2099-12-12 0:0:0.0</ExpiryDateTime>" + "<CreatedByAppName>"
								+ createdbyappname + "</CreatedByAppName>" + "<VersionFlag>N</VersionFlag>"
								+ "<AccessType>S</AccessType>" + "<ISIndex>" + ISINDEX.m_nDocIndex + "#"
								+ ISINDEX.m_sVolumeId + "</ISIndex><TextISIndex>0#0#</TextISIndex>"
								+ "<DocumentType>N</DocumentType>" + "<DocumentSize>" + lstrDocFileSize
								+ "</DocumentSize><Comment>" + createdbyappname
								+ "</Comment><RetainAnnotation>N</RetainAnnotation></Document>"
								+ "</NGOChangeDocumentProperty_Input>";
						}
					} else {*/
						sMappedInputXml = "<?xml version=\"1.0\"?>" + "<NGOAddDocument_Input>"
								+ "<Option>NGOAddDocument</Option>" + "<CabinetName>" + sCabname + "</CabinetName>"
								+ "<UserDBId>" + sSessionId + "</UserDBId>" + "<GroupIndex>0</GroupIndex>"
								+ "<VersionFlag>N</VersionFlag>" + "<ParentFolderIndex>" + strFolderIndex
								+ "</ParentFolderIndex>" + "<DocumentName>" + FrmType + "</DocumentName>"
								+ "<CreatedByAppName>" + createdbyappname + "</CreatedByAppName>" + "<Comment>"
								+ FrmType + "</Comment>" + "<VolumeIndex>" + ISINDEX.m_sVolumeId + "</VolumeIndex>"
								+ "<FilePath>" + strDocumentPath + "</FilePath>" + "<ISIndex>" + ISINDEX.m_nDocIndex
								+ "#" + ISINDEX.m_sVolumeId + "</ISIndex>" + "<NoOfPages>1</NoOfPages>"
								+ "<DocumentType>N</DocumentType>" + "<DocumentSize>" + lstrDocFileSize
								+ "</DocumentSize>" + "</NGOAddDocument_Input>";

				//	}
					TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Document Addition sInputXML: " + sMappedInputXml);
					
					String sOutputXML = CommonMethods.WFNGExecute(sMappedInputXml, jtsIP, jtsPort, 1);
					XMLParser xmlParserData = new XMLParser(sOutputXML);
					TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Document Addition sOutputXml: " + sOutputXML);
					String status_D = xmlParserData.getValueOf("Status");
					if (status_D.equalsIgnoreCase("0")) {
						delete(sFilepath);
						return sOutputXML;
					} else {
						return "Error in Document Addition";
					}
				}
			
				catch (JPISException e) {
					return "Error in Document Addition at Volume";
				}
				
				catch (Exception e) {
					return "Exception Occurred in Document Addition";
				}
		} catch (Exception e) {
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Exception: " + e.getMessage());
			return "Exception Occurred in SearchDocument";
		}
	}
	
	public static String AttachDocumentWithWI(String pid, String pdfName) {
		
		String docxml = "";
		String documentindex = "";
		String doctype = "";

		try {
			
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("inside ODAddDocument");
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Proess Instance Id: " + pid);
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Integration call: " + pdfName);

			String sCabname = cabinetName;
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("sCabname" + sCabname);
			String sSessionId = sessionID;
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("sSessionId" + sSessionId);
			String sJtsIp = jtsIP;
			int iJtsPort_int = Integer.parseInt(jtsPort);
			
			String pdfTemplatePath = "";
			String generatedPdfPath = "";
			
			// Reading path from property file
			String dynamicPdfName = pid + pdfName + ".pdf";
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("\nGeneratedPdfPathCheck :" + generatedPdfPath);
			pdfTemplatePath = TF_Form_Template_Path;
			
			generatedPdfPath = TF_Form_Template_Path;

			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("\nGeneratedPdfPathCheck :" + generatedPdfPath);
			generatedPdfPath = generatedPdfPath + System.getProperty("file.separator") + dynamicPdfName;
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("\nGeneratedPdfPath1 :" + generatedPdfPath);

			docxml = SearchExistingDoc(pid, pdfName, sCabname, sSessionId, sJtsIp, iJtsPort_int, generatedPdfPath);
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Final Document Output: " + docxml);
			documentindex = GetTagValue(docxml, "DocumentIndex");

			doctype = "new";

			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(docxml + "~" + documentindex + "~" + doctype + "~" + dynamicPdfName);
			String Output = "0000~" + docxml + "~" + documentindex + "~" + doctype + "~" + dynamicPdfName;
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug(" Output: " + Output);
			return Output;
			
		} catch (Exception e) {
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Exception while adding the document: " + e);
			return "Exception while adding the document: " + e;
		}
	}
	
	public static String GetTagValue(String XML, String Tagname) {
		
		String starttag = "<" + Tagname + ">";
		String endtag = "</" + Tagname + ">";
		TF_Firco_Handling_Log.TF_Firco_HandlingLogger.info("GetTagValue " + starttag);
		if (XML.indexOf(starttag) >= 0) {
			if ("MATURITYDATE".equals(Tagname)) {
				String date = XML.substring(XML.indexOf(starttag) + (starttag.length()), XML.indexOf(endtag));
				return date.substring(6, 8) + date.substring(4, 6) + date.substring(0, 4);
			}
			return XML.substring(XML.indexOf(starttag) + (starttag.length()), XML.indexOf(endtag));
		} else {
			return "";
		}
	}
	
	private static void delete(String path) {
		
		File file = new File (path);
		try {
			if (file.delete()){
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("File deleted");
			}else{
				TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("File not deleted");
			}
		} catch(Exception e) {
			TF_Firco_Handling_Log.TF_Firco_HandlingLogger.debug("Error in File not deletion");
		}
	}
}



