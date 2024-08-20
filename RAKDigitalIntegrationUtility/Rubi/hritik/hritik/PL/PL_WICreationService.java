package com.newgen.PL;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

import com.newgen.custom.CreateWorkitem;
import com.newgen.ws.EE_EAI_HEADER;
import com.newgen.ws.exception.WICreateException;
import com.newgen.ws.request.Attribute;
import com.newgen.ws.request.Attributes;
import com.newgen.ws.request.Document;
import com.newgen.ws.request.Documents;
import com.newgen.ws.request.WICreateRequest;
import com.newgen.ws.response.WICreateResponse;
import com.newgen.ws.util.XMLParser;

public class PL_WICreationService extends CreateWorkitem {

	// CONSTANTS
	final private String SUCCESS_STATUS = "0";
	final private String WSR_PROCESS = "USR_0_WSR_PROCESS";
	final private String WSR_ATTRDETAILS = "USR_0_WSR_ATTRDETAILS";

	// Process base object
	final private String DPL_WIHISTORY = "NG_DPL_GR_DECISION_HISTORY";

	// Response Parameters
	String eCode = "";
	String eDesc = "";

	// Common class object
	WICreateResponse response = new WICreateResponse();
	EE_EAI_HEADER headerObjResponse;
	PL_WICommonMethod commonMethod = new PL_WICommonMethod();
	HashMap<String, String> hm = new HashMap();
	LinkedHashMap<String, HashMap<String, String>> hmMain = new LinkedHashMap();
	HashMap<String, String> hm1 = new HashMap<String, String>();

	static HashMap<String, String> hmExtMandDPL = new HashMap();
	static HashMap<String, String> hmRptProcessIdDPL = new HashMap();
	static HashMap<String, String> hmRptTransTableDPL = new HashMap();
	static LinkedHashMap<String, HashMap<String, String>> hmRptAttrAndColDPL = new LinkedHashMap();
	static LinkedHashMap<String, HashMap<String, String>> hmRptAttrAndMandDPL = new LinkedHashMap();
	static LinkedHashMap<String, HashMap<String, String>> hmRptAttrAndFormatDPL = new LinkedHashMap();
	static LinkedHashMap<String, HashMap<String, String>> hmRptAttrAndTypeDPL = new LinkedHashMap();
	String processID = "";
	private String sProcessName;
	private String sSubProcess;
	private String sInitiateAlso;
	private String sMsgFormat;
	private String sMsgVersion;
	private String sRequestorChannelId;
	private String sRequestorUserId;
	private String sRequestorLanguage;
	private String sRequestorSecurityInfo;
	private String sMessageId;
	private String sExtra1;
	private String sExtra2;
	private EE_EAI_HEADER headerObjRequest;
	private Attributes attributesObj;
	private Attribute attributeObj[];
	String InputMessage = "";
	boolean sessionFlag = false;

	private ResourceBundle pCodes;
	String WINAME = "";
	private String attributeTag = "";
	String trTableColumn = "";
	String trTableValue = "";
	String wiName = "";
	private String sSessionID = "";
	String sInputXML = "";
	String sOutputXML = "";
	XMLParser xmlobj;
	String sDate = "";
	String processDefID = "";
	String InitiationQueueID = "";
	String externalTableName = "";
	String transactionTableName = "";
	String RepetitiveMainTags = "";
	private String completeFircoData = "";

	public WICreateResponse wiCreate(WICreateRequest request, String attributeList[]) throws WICreateException {

		try {
			WriteLog("inside wiCreate PL");

			headerObjResponse = new EE_EAI_HEADER();

			// Load file configuration
			this.response = commonMethod.loadConfiguration();

			// Load ResourceBundle
			commonMethod.loadResourceBundle();

			// Fetching request parameters
			fetchRequestParameters(request);

			validateRequestParameters(attributeList);

			// Duplicate check based on prospect Id
			duplicateWorkitemCheckBasedOnProspect(hm.get("ProspectID").trim());
			if (!"".equalsIgnoreCase(WINAME.trim()) || "null".equalsIgnoreCase(WINAME.trim())) {
				WriteLog("returnCode: " + 3335 + " returnDesc: " + pCodes.getString("3335") + ":"
						+ hm.get("ProspectID").trim());
				this.headerObjResponse = commonMethod.setFailureParamResponse(sExtra1,sExtra2,sMessageId,sMsgFormat,sMsgVersion,sRequestorChannelId,sRequestorLanguage,sRequestorSecurityInfo,sRequestorUserId);

				

				response.setWorkitemNumber(WINAME);
				response.setErrorCode("3335");
				response.setErrorDescription(pCodes.getString("3335") + ":" + hm.get("ProspectID").trim());
				return response;
			}

			if ("Digital_PL".equalsIgnoreCase(sProcessName)) {
				validateRepetitiveRequestParameters();
			}
			attributeTag = attributeTag + createRepetativeAttributeXML();

			// Checking existing session
			checkExistingSession();

			// WFUploadWorkItemCall executed in this method
			runWICall();

			if ("Digital_PL".equalsIgnoreCase(sProcessName)) {
				// Entry into history table
				insertIntoHistoryPL();

			}

		} catch (WICreateException e) {
			WriteLog("WICreateException caught:Message- " + e.getMessage());
			WriteLog("WICreateException caught:Code- " + e.getErrorCode());
			WriteLog("WICreateException caught:Description " + e.getErrorDesc());
			WriteLog("WICreateException: " + e.toString());
			// setFailureParamResponse();
			this.headerObjResponse = commonMethod.setFailureParamResponse(sExtra1,sExtra2,sMessageId,sMsgFormat,sMsgVersion,sRequestorChannelId,sRequestorLanguage,sRequestorSecurityInfo,sRequestorUserId);
		//	response.setEE_EAI_HEADER(headerObjResponse);

			response.setErrorCode(e.getErrorCode());
			response.setErrorDescription(e.getErrorDesc());
			return response;
		} catch (IOException e) {
			this.headerObjResponse = commonMethod.setFailureParamResponse(sExtra1,sExtra2,sMessageId,sMsgFormat,sMsgVersion,sRequestorChannelId,sRequestorLanguage,sRequestorSecurityInfo,sRequestorUserId);
		//	response.setEE_EAI_HEADER(headerObjResponse);

			response.setErrorCode("1009");
			response.setErrorDescription(pCodes.getString("1009") + ": " + e.getMessage());

			return response;
		} catch (Exception e) {
			WriteLog("Exception caught:Message- " + e.getMessage());
			this.headerObjResponse = commonMethod.setFailureParamResponse(sExtra1,sExtra2,sMessageId,sMsgFormat,sMsgVersion,sRequestorChannelId,sRequestorLanguage,sRequestorSecurityInfo,sRequestorUserId);
		//	response.setEE_EAI_HEADER(headerObjResponse);

			response.setErrorCode("1010");
			response.setErrorDescription(pCodes.getString("1010") + ": " + e.getMessage());
			return response;
		} finally {
			WriteLog("Inside finally method to dispose off conenction objects and hash maps");
			try {
				// Commented below code for Invalid Session Issue.
				/*
				 * if(sessionFlag==true) deleteConnection();
				 */
			} catch (Exception e) {
				WriteLog("Exception: " + e.getMessage());
			}
			hm.clear();
			hmMain.clear();
			
			hmRptProcessIdDPL.clear();
			hmRptTransTableDPL.clear();
		}
		return response;

	}

	private void insertIntoHistoryPL() throws WICreateException, Exception {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.println(dateFormat.format(cal.getTime())); // 2014/08/06
																// 16:00:22

		trTableColumn = "WI_NAME,Workstep,Decision,Decision_Date_Time,Remarks,UserName,ENTRY_DATE_TIME";
		WriteLog("trTableColumn dor DAC" + trTableColumn);
		trTableValue = "'" + wiName + "','Initiation','Submit','" + sDate + "','Workitem created through webservice','"
				+ commonMethod.sUsername + "','" + sDate + "'";
		WriteLog("trTableValue for DBO" + trTableValue);
		sInputXML = commonMethod.getAPInsertInputXML(DPL_WIHISTORY, trTableColumn, trTableValue, sSessionID);
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = commonMethod.executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}

	private void checkCallsMainCode(XMLParser obj) throws WICreateException {
		WriteLog("Inside checkCallsMainCode");
		if (!xmlobj.getValueOf("MainCode").trim().equalsIgnoreCase(SUCCESS_STATUS)) {
			if (!xmlobj.getValueOf("SubErrorCode").equalsIgnoreCase("")) {
				eCode = xmlobj.getValueOf("SubErrorCode");
				eDesc = xmlobj.getValueOf("Description");
			} else if (!xmlobj.getValueOf("Output").equalsIgnoreCase("")) {
				eCode = xmlobj.getValueOf("MainCode");
				eDesc = xmlobj.getValueOf("Output");
			} else {
				eCode = xmlobj.getValueOf("Status");
				eDesc = xmlobj.getValueOf("Error");
			}
			throw new WICreateException(eCode, eDesc);
		}
	}

	private void fetchRequestParameters(WICreateRequest req) throws WICreateException {
		try {
			WriteLog("Inside fetchRequestParameters");
			sProcessName = (req.getProcessName() == null) ? "" : req.getProcessName().trim();
			sSubProcess = (req.getSubProcess() == null) ? "" : req.getSubProcess().trim();
			sInitiateAlso = (req.getInitiateAlso() == null) ? "" : req.getInitiateAlso().trim();

			headerObjRequest = req.getEE_EAI_HEADER();

			InputMessage = (req.getInputMessage() == null) ? "" : req.getInputMessage().trim();
			InputMessage = (InputMessage == null) ? "" : InputMessage.replace("&amp;", "&");
			InputMessage = (InputMessage == null) ? "" : InputMessage.replace("&AMP;", "&");
			sMsgFormat = (headerObjRequest.getMsgFormat() == null) ? "" : headerObjRequest.getMsgFormat().trim();
			sMsgVersion = (headerObjRequest.getMsgVersion() == null) ? "" : headerObjRequest.getMsgVersion().trim();
			sMsgFormat = (headerObjRequest.getMsgFormat() == null) ? "" : headerObjRequest.getMsgFormat().trim();
			sRequestorChannelId = (headerObjRequest.getRequestorChannelId() == null) ? ""
					: headerObjRequest.getRequestorChannelId().trim();
			sRequestorUserId = (headerObjRequest.getRequestorUserId() == null) ? ""
					: headerObjRequest.getRequestorUserId().trim();
			sRequestorLanguage = (headerObjRequest.getRequestorLanguage() == null) ? ""
					: headerObjRequest.getRequestorLanguage().trim();
			sRequestorSecurityInfo = (headerObjRequest.getRequestorSecurityInfo() == null) ? ""
					: headerObjRequest.getRequestorSecurityInfo().trim();
			sExtra2 = (headerObjRequest.getExtra2() == null) ? "" : headerObjRequest.getExtra2().trim();
			sExtra1 = (headerObjRequest.getExtra1() == null) ? "" : headerObjRequest.getExtra1().trim();
			sMessageId = (headerObjRequest.getMessageId() == null) ? "" : headerObjRequest.getMessageId().trim();

			attributesObj = req.getAttributes();
			attributeObj = attributesObj.getAttribute();
			WriteLog("attributeObj.length" + attributeObj.length);
			// Fetch Name Value Attributes in Hash Map
			for (int i = 0; i < attributeObj.length; i++) {
				String attrValue = attributeObj[i].getValue().trim();
				attrValue = (attrValue == null) ? "" : attrValue.replace("&amp;", "&");
				attrValue = (attrValue == null) ? "" : attrValue.replace("&AMP;", "&");
				hm.put(attributeObj[i].getName(), attrValue);
			}

			WriteLog("sProcessName: " + sProcessName + ", sSubProcess: " + sSubProcess + " sInitiateAlso: "
					+ sInitiateAlso);
		} catch (Exception e) {
			WriteLog("Exception: " + e.getMessage());
			throw new WICreateException("1007", pCodes.getString("1007") + " : " + e.getMessage());
		}
	}

	private void validateRequestParameters(String attributeList[]) throws WICreateException, Exception {
		WriteLog("Inside validateRequestParameters");
		if (sProcessName.equalsIgnoreCase("?") || sProcessName.equalsIgnoreCase(""))
			throw new WICreateException("1001", pCodes.getString("1001"));
		if ((sSubProcess.equalsIgnoreCase("?") || sSubProcess.equalsIgnoreCase("")))
			throw new WICreateException("1002", pCodes.getString("1002"));
		if (!(sInitiateAlso.equalsIgnoreCase("Y") || sInitiateAlso.equalsIgnoreCase("N")))
			throw new WICreateException("1003", pCodes.getString("1003"));
		if (sRequestorChannelId.equalsIgnoreCase("?") || sRequestorChannelId.equalsIgnoreCase(""))
			throw new WICreateException("1011", pCodes.getString("1011"));

		// Fetch ProcessDefID
		getProcessDefID();

		// Fetch queue Id where wi need to be created
		getQueueDefID();

		// Check if all Mandatory attributes present in USR_0_WSR_ATTRDETAILS
		// have come
		if ("Digital_PL".equalsIgnoreCase(sProcessName)) {
			checkMandatoryAttribute();
		}

		Date d = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sDate = dateFormat.format(d);

		if ("Digital_PL".equalsIgnoreCase(sProcessName)) {
			checkExtTableAttrIBPS(hm);
		}

		getTableName();
	}

	private void duplicateWorkitemCheckBasedOnProspect(String prospect) throws Exception {
		String sQuery = "select top 1 WINAME,isnull(WICreatedDate,'') from NG_DPL_EXTTABLE with (nolock) where ProspectID='"
				+ prospect + "'" + " order by WICreatedDate desc";
		sInputXML = commonMethod.getAPSelectWithColumnNamesXML(sQuery);
		WriteLog("Input: " + sInputXML);
		sOutputXML = commonMethod.executeAPI(sInputXML);
		WriteLog("Output: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
		WINAME = xmlobj.getValueOf("WINAME");
		String WI_create_date = xmlobj.getValueOf("WICreatedDate");
		WriteLog("Workitem number: " + WINAME);
		WriteLog("CREATEDAT of the request" + WI_create_date);
	}

	private void validateRepetitiveRequestParameters() throws WICreateException, Exception {
		String repetitiveListMain[];
		String repetitiveList[];

		RepetitiveMainTags = checkMandatoryRepetitiveTags();
		System.out.println("RepetitiveMainTags"+RepetitiveMainTags);
		if (!"".equalsIgnoreCase(RepetitiveMainTags)) {
			String attributeList[] = RepetitiveMainTags.split("`");
			if (attributeList.length > 0) {
				for (int i = 0; i < attributeList.length; i++) {
					if (InputMessage.contains(attributeList[i])) {
						repetitiveListMain = commonMethod.getTagValues(InputMessage, attributeList[i]).split("`");
						for (int j = 0; j < repetitiveListMain.length; j++) {
							HashMap<String, String> hm1 = new HashMap();
							repetitiveList = commonMethod.getTagValues(repetitiveListMain[j], "Attribute").split("`");
							for (int k = 0; k < repetitiveList.length; k++) {
								String attrName = commonMethod.getTagValues(repetitiveList[k], "Name");
								String attrValue = commonMethod.getTagValues(repetitiveList[k], "Value");

								hm1.put(attrName, attrValue);
							}
							if (hm1.size() != 0) {
								String RepetitiveProcessID = hmRptProcessIdDPL.get(attributeList[i]);
								checkMandatoryRepetitiveAttribute(RepetitiveProcessID, "Y", hm1);
								hmMain.put(attributeList[i] + "-" + Integer.toString(j), hm1);
							}
						}
					}
				}
			}
		}

	}

	private String checkMandatoryRepetitiveTags() throws WICreateException, Exception {
		WriteLog("inside checkMandatoryRepetitiveTagsDBO");
		String sInputXML = commonMethod.getAPSelectWithColumnNamesXML(
				"select REPETITIVETAGNAME,PROCESSID,TRANSACTIONTABLE,ISMANDATORY from USR_0_WSR_PROCESS_REPETITIVE with(nolock) where ProcessName='"
						+ sProcessName + "' and SUBPROCESSNAME='" + sSubProcess + "' and ISACTIVE='Y'");
		WriteLog("Input XML: " + sInputXML);
		
		System.out.println("sInputXML"+sInputXML);
		String sOutputXML = commonMethod.executeAPI(sInputXML);
		WriteLog("Output XML: " + sOutputXML);
		System.out.println("sOutputXML"+sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String RepetitiveMainTags = commonMethod.getTagValues(sOutputXML, "REPETITIVETAGNAME");
		String RepProcessId[] = commonMethod.getTagValues(sOutputXML, "PROCESSID").split("`");
		String RepTransTable[] = commonMethod.getTagValues(sOutputXML, "TRANSACTIONTABLE").split("`");
		String isMandatory = commonMethod.getTagValues(sOutputXML, "ISMANDATORY");
		String RepetitiveList[] = RepetitiveMainTags.split("`");
		String MandateList[] = isMandatory.split("`");
		if (MandateList.length > 0) {
			for (int i = 0; i < MandateList.length; i++) {
				String flag = "Y";

				if ("Y".equalsIgnoreCase(MandateList[i])) {
					if (InputMessage.contains(RepetitiveList[i])) {
						// nothing to do
					} else
						flag = "N";
				}
				if (flag.equalsIgnoreCase("N"))
					throw new WICreateException("1020", pCodes.getString("1020") + ": " + RepetitiveList[i]);

				hmRptProcessIdDPL.put(RepetitiveList[i], RepProcessId[i]);
				hmRptTransTableDPL.put(RepProcessId[i], RepTransTable[i]);
			}
		}
		return RepetitiveMainTags;
	}

	private String createRepetativeAttributeXML() throws WICreateException, Exception {
		if (hmMain.size() != 0) {
			WriteLog("Inside InsertRecordsForRepetitiveTagsIBPS");
			Date d = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sDate = dateFormat.format(d);
			String RepetitiveTagsAttribute = "";
			for (String name : hmMain.keySet()) {
				String key = name.toString();
				String keyvalue = hmMain.get(name).toString();
				// WriteLog("hmMain: "+key + " " + keyvalue);
				String RepetitiveTags[] = key.split("-");
				String RepetitiveProcessID = hmRptProcessIdDPL.get(RepetitiveTags[0]);
				String RepetitiveTagTableName = hmRptTransTableDPL.get(RepetitiveProcessID);
				if ("".equalsIgnoreCase(RepetitiveTagsAttribute)) {
					RepetitiveTagsAttribute = "\n<Q_" + RepetitiveTagTableName + ">";
				} else {
					RepetitiveTagsAttribute = RepetitiveTagsAttribute + "\n<Q_" + RepetitiveTagTableName + ">";
				}

				HashMap<String, String> hm1 = new HashMap();
				hm1 = hmMain.get(key);

				HashMap<String, String> hmtmp = new HashMap();
				hmtmp = hmRptAttrAndColDPL.get(RepetitiveProcessID);
				String AttrList = "";
				String TransColList = "";

				for (String name1 : hmtmp.keySet()) {
					if (AttrList.equalsIgnoreCase(""))
						AttrList = name1.toString();
					else
						AttrList = AttrList + "`" + name1.toString();

					if (TransColList.equalsIgnoreCase(""))
						TransColList = hmtmp.get(name1).toString();
					else
						TransColList = TransColList + "`" + hmtmp.get(name1).toString();
				}
				// WriteLog("AttrList:"+AttrList);
				// WriteLog("TransColList:"+TransColList);
				if (!"".equalsIgnoreCase(AttrList.trim())) {
					String AttrListArr[] = AttrList.split("`");
					String TransColListArr[] = TransColList.split("`");
					String insertColumns = "";
					String insertValues = "";
					for (int j = 0; j < AttrListArr.length; j++) {
						if (hm1.containsKey(AttrListArr[j])) {
							String value = hm1.get(AttrListArr[j]);
							if (!value.trim().equalsIgnoreCase("")) {
								value = value.replace("'", "");
								value = commonMethod.replaceXChars(value);// added
																			// to
																			// handle
																			// special
																			// characters
																			// in
																			// request
							}
							if (!value.trim().equalsIgnoreCase("")) {
								RepetitiveTagsAttribute = RepetitiveTagsAttribute + "\n<" + TransColListArr[j] + ">"
										+ value + "</" + TransColListArr[j] + ">";

								//
							}
						}
					}
					// insertIntoRepetitiveGridTable(RepetitiveTagTableName,
					// insertColumns, insertValues);
				}

				RepetitiveTagsAttribute = RepetitiveTagsAttribute + "\n</Q_" + RepetitiveTagTableName + ">";

			}
			return RepetitiveTagsAttribute + completeFircoData;
		} else
			return "";
	}

	private void checkExistingSession() throws Exception, WICreateException {
		WriteLog("inside checkExistingSession");

		String getSessionQry = "select randomnumber from pdbconnection with(nolock) where userindex in (select userindex from pdbuser with(nolock) where username='"
				+ commonMethod.sUsername + "')";
		sInputXML = commonMethod.getAPSelectWithColumnNamesXML(getSessionQry);
		WriteLog("Input XML: " + sInputXML);
		sOutputXML = commonMethod.executeAPI(sInputXML);
		WriteLog("Output XML: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);

		sSessionID = commonMethod.getTagValues(sOutputXML, "randomnumber");
		WriteLog("SessionID: " + sSessionID);
		if (sSessionID.equalsIgnoreCase("")) {
			sInputXML = commonMethod.getWMConnectXML();
			// WriteLog("WM Connect Input: "+sInputXML);
			sOutputXML = commonMethod.executeAPI(sInputXML);
			WriteLog("WM Connect Output: " + sOutputXML);
			xmlobj = new XMLParser(sOutputXML);
			// Check Main Code
			checkCallsMainCode(xmlobj);
			sSessionID = commonMethod.getTagValues(sOutputXML, "SessionId");
			sessionFlag = true;
		}
		response.setSessionId(sSessionID);
	}

	private void getProcessDefID() throws WICreateException, Exception {
		sInputXML = commonMethod.getAPSelectWithColumnNamesXML(
				"select a.PROCESSDEFID, b.PROCESSID from processdeftable a with(nolock)," + WSR_PROCESS
						+ " b with(nolock) where a.processname='" + sProcessName + "' and b.SUBPROCESSNAME='"
						+ sSubProcess + "' and a.processname=b.processname and isactive='Y'");
		WriteLog("APSelectWithColumnNames Input: " + sInputXML);
		sOutputXML = commonMethod.executeAPI(sInputXML);
		WriteLog("APSelectWithColumnNames Output: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
		processDefID = commonMethod.getTagValues(sOutputXML, "PROCESSDEFID");
		WriteLog("processDefID: " + processDefID);
		processID = commonMethod.getTagValues(sOutputXML, "PROCESSID");
		WriteLog("processID: " + processID);
		if (processID.equalsIgnoreCase(""))
			throw new WICreateException("1019", pCodes.getString("1019") + ":" + sProcessName + "/" + sSubProcess);

	}

	private void getQueueDefID() throws WICreateException, Exception {
		try {
			sInputXML = commonMethod
					.getAPSelectWithColumnNamesXML("select QueueID from QUEUEDEFTABLE with(nolock) where ProcessName='"
							+ sProcessName + "' and QueueName='Digital_PL_Introduction'");
			WriteLog("APSelectWithColumnNames Input: " + sInputXML);
			sOutputXML = commonMethod.executeAPI(sInputXML);
			WriteLog("APSelectWithColumnNames Output: " + sOutputXML);
			xmlobj = new XMLParser(sOutputXML);
			// Check Main Code
			checkCallsMainCode(xmlobj);
			InitiationQueueID = commonMethod.getTagValues(sOutputXML, "QueueID");
			WriteLog("QueueID: " + InitiationQueueID);
			if (InitiationQueueID.equalsIgnoreCase(""))
				throw new WICreateException("6015", pCodes.getString("6015") + ":" + sProcessName + "/" + sSubProcess);

		} catch (Exception e) {
			WriteLog("error msg" + e.getMessage());
			WriteLog(""+e.fillInStackTrace());
		}
	}

	private void checkExtTableAttrIBPS(HashMap<String, String> hmt) throws WICreateException, Exception {
		try {
			String attributeNames = "";
			for (String name : hmt.keySet()) {
				if (attributeNames.equalsIgnoreCase(""))
					attributeNames = "'" + name.toString() + "'";
				else
					attributeNames = attributeNames + ",'" + name.toString() + "'";
			}
			String getExtTransQry = "";

			getExtTransQry = "select ATTRIBUTENAME, isnull(nullif(EXTERNALTABLECOLNAME,''),'#') as EXTERNALTABLECOLNAME,isnull(nullif(TRANSACTIONTABLECOLNAME,''),'#') as TRANSACTIONTABLECOLNAME, isnull(nullif(ATTRIBUTE_FORMAT,''),'#') as ATTRIBUTE_FORMAT, isnull(nullif(ATTRIBUTE_TYPE,''),'#') as ATTRIBUTE_TYPE, ISMANDATORY from "
					+ WSR_ATTRDETAILS + " with(nolock) where ATTRIBUTENAME in (" + attributeNames + ") and PROCESSID='"
					+ processID + "' and ISACTIVE='Y' ";

			sInputXML = commonMethod.getAPSelectWithColumnNamesXML(getExtTransQry);
			
			sOutputXML = commonMethod.executeAPI(sInputXML);
			xmlobj = new XMLParser(sOutputXML);
			WriteLog("Output XML: " + sOutputXML);
			checkCallsMainCode(xmlobj);
			String attNameFromTable = commonMethod.getTagValues(sOutputXML, "ATTRIBUTENAME");
			String extFlag = commonMethod.getTagValues(sOutputXML, "EXTERNALTABLECOLNAME");
			String isMandatory = commonMethod.getTagValues(sOutputXML, "ISMANDATORY");
			String txnColumnName = commonMethod.getTagValues(sOutputXML, "TRANSACTIONTABLECOLNAME");
			String attributesFormat = commonMethod.getTagValues(sOutputXML, "ATTRIBUTE_FORMAT");
			String attributesType = commonMethod.getTagValues(sOutputXML, "ATTRIBUTE_TYPE");
			String AttrTagsName[] = attNameFromTable.split("`");
			String ExtColsName[] = extFlag.split("`");
			String MandateList[] = isMandatory.split("`");
			String txnColsName[] = txnColumnName.split("`");
			String attributeFormat[] = attributesFormat.split("`");
			String attributeType[] = attributesType.split("`");
			for (String name : hmt.keySet()) {
				System.out.println("name:"+name.toString());
				String attrtag = name.toString().trim();
				
				String flg = "N";
				 WriteLog("Attr:"+attNameFromTable);
				 System.out.println("Length:"+AttrTagsName.length);
				for (int i = 0; i < AttrTagsName.length; i++) { // WriteLog("Attr111:"+AttrTagsName[i]);
					System.out.println("attrTagName:"+AttrTagsName[i]);
					String attrTagName = AttrTagsName[i].trim();
					if (AttrTagsName[i].trim().equalsIgnoreCase(attrtag)) {
						flg = "Y";
						break;
					}
				}
				if (flg.equalsIgnoreCase("N")) {
					WriteLog("No Value Mapped for attribute:-" + attrtag);
					throw new WICreateException("1017", pCodes.getString("1017") + " :" + attrtag);
				}
			}
			// *******************************************

			for (int i = 0; i < AttrTagsName.length; i++) {
				
				System.out.println("ExtColsName:"+ExtColsName[i]);
				
				
				String attriName= AttrTagsName[i];
				String attributeValueWithoutTrim = hmt.get(AttrTagsName[i]);
				String attributeValue = hmt.get(AttrTagsName[i]).trim();
				String extCol = ExtColsName[i].trim();
				System.out.println("attributeValue:"+attributeValue);
				String attribute =attributeValue.trim();
				System.out.println("MandateList:"+MandateList[i]);
				String Mandate= MandateList[i].trim();

				WriteLog("attribute name:-" + AttrTagsName[i]);
				WriteLog("attribute value before decode:-" + attributeValue);
				if (!"".equalsIgnoreCase(attributeValue) && !"".equalsIgnoreCase(attributeFormat[i])
						&& !"".equalsIgnoreCase(attributeType[i]) && !"#".equalsIgnoreCase(attributeFormat[i])
						&& !"#".equalsIgnoreCase(attributeType[i])) {
					checkAttributeFormatAndLength(AttrTagsName[i], attributeValue, attributeFormat[i],
							attributeType[i]);
				}
				
				System.out.print(""+attributeValue);
				attributeValue = commonMethod.replaceXChars(attributeValue);// added
																			// to
																			// handle
																			// special
																			// characters
																			// in
																			// request
				
				if (((MandateList[i].trim().equalsIgnoreCase("Y") || MandateList[i].trim().equalsIgnoreCase("N"))
						&& !attributeValue.equalsIgnoreCase(""))
						|| (MandateList[i].trim().equalsIgnoreCase("N") && attributeValue.equalsIgnoreCase(""))) {
					
					if (!ExtColsName[i].trim().equalsIgnoreCase("") && !ExtColsName[i].trim().equalsIgnoreCase("#")) {
						WriteLog("this is a extn table attribute:-" + AttrTagsName[i]);

						attributeTag = attributeTag + "\n<" + ExtColsName[i] + ">" + attributeValue + "</"
								+ ExtColsName[i] + ">";
						// WriteLog("attributeTag:-"+attributeTag);
						if ("Digital_PL".equalsIgnoreCase(sProcessName)) {
							
						}
					} else if (!txnColsName[i].trim().equalsIgnoreCase("")
							&& !txnColsName[i].trim().equalsIgnoreCase("#")) {
					} else {
						WriteLog("some error occured for attribute:-" + AttrTagsName[i]);
						throw new WICreateException("1015", pCodes.getString("1015") + " :" + AttrTagsName[i]);
					}

				} else if (MandateList[i].trim().equalsIgnoreCase("Y") && attributeValue.equalsIgnoreCase("")) {
					WriteLog("some error occured for attribute2:-" + AttrTagsName[i]);
					throw new WICreateException("1016", pCodes.getString("1016") + " :" + AttrTagsName[i]);
				}
			}

			// WriteLog("Final txn table attributeTag:-"+txnTableAttributes);
			// if(!("\n<Q_"+txnTableName+">").equalsIgnoreCase(txnTableAttributes))
			// txnTableAttributes=txnTableAttributes+"\n</Q_"+txnTableName+">";
			// else
			// txnTableAttributes="";
			// attributeTag=attributeTag+txnTableAttributes;
			WriteLog("Final attributeTag:-" + attributeTag);
		} catch (Exception e) {
			WriteLog("Final error mag:-" + e.getMessage());
			System.out.println("Final error mag:-" + e.getMessage());
		}

	}

	private void getTableName() throws WICreateException, Exception {
		// from tables here
		String getTableNameQry = "select EXTERNALTABLE ,TRANSACTIONTABLE  from " + WSR_PROCESS
				+ " with(nolock) where PROCESSID='" + processID + "'";
		sInputXML = commonMethod.getAPSelectWithColumnNamesXML(getTableNameQry);
		WriteLog("Input XML: " + sInputXML);
		sOutputXML = commonMethod.executeAPI(sInputXML);
		WriteLog("Output XML: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		externalTableName = commonMethod.getTagValues(sOutputXML, "EXTERNALTABLE");
		WriteLog("External table Name: " + externalTableName);
		transactionTableName = commonMethod.getTagValues(sOutputXML, "TRANSACTIONTABLE");
		WriteLog("Transaction table Name: " + transactionTableName);
	}

	private String checkMandatoryRepetitiveAttribute(String RepetitiveProcessId, String isValidatingMandate,
			HashMap<String, String> RepetitiveReqAttr) throws WICreateException, Exception {
		WriteLog("inside checkMandatoryRepetitiveAttributeIBPS");
		String AttrTagName = "";
		String TransColName = "";
		String AttrFormat = "";
		String AttrType = "";
		String isMandatory = "";
		if (!hmRptAttrAndColDPL.containsKey(RepetitiveProcessId)) {
			String sInputXML = commonMethod.getAPSelectWithColumnNamesXML(
					"select ATTRIBUTENAME,TRANSACTIONTABLECOLNAME,isnull(nullif(ATTRIBUTE_FORMAT,''),'#') as ATTRIBUTE_FORMAT, isnull(nullif(ATTRIBUTE_TYPE,''),'#') as ATTRIBUTE_TYPE,ISMANDATORY from USR_0_WSR_ATTRDETAILS_REPETITIVE with(nolock) where PROCESSID='"
							+ RepetitiveProcessId + "' and ISACTIVE='Y' order by AttributeName");
			WriteLog("Input XML: " + sInputXML);
			String sOutputXML = commonMethod.executeAPI(sInputXML);
			WriteLog("Output XML: " + sOutputXML);
			xmlobj = new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			AttrTagName = commonMethod.getTagValues(sOutputXML, "ATTRIBUTENAME");
			TransColName = commonMethod.getTagValues(sOutputXML, "TRANSACTIONTABLECOLNAME");
			AttrFormat = commonMethod.getTagValues(sOutputXML, "ATTRIBUTE_FORMAT");
			AttrType = commonMethod.getTagValues(sOutputXML, "ATTRIBUTE_TYPE");
			isMandatory = commonMethod.getTagValues(sOutputXML, "ISMANDATORY");
		} else {
			HashMap<String, String> hmAttr = new HashMap();
			hmAttr = hmRptAttrAndColDPL.get(RepetitiveProcessId);
			for (String name : hmAttr.keySet()) {
				if (AttrTagName.equalsIgnoreCase(""))
					AttrTagName = name.toString();
				else
					AttrTagName = AttrTagName + "`" + name.toString();

				if (TransColName.equalsIgnoreCase(""))
					TransColName = hmAttr.get(name).toString();
				else
					TransColName = TransColName + "`" + hmAttr.get(name).toString();
			}

			HashMap<String, String> hmMand = new HashMap();
			hmMand = hmRptAttrAndMandDPL.get(RepetitiveProcessId);
			for (String name1 : hmMand.keySet()) {
				if (isMandatory.equalsIgnoreCase(""))
					isMandatory = hmMand.get(name1).toString();
				else
					isMandatory = isMandatory + "`" + hmMand.get(name1).toString();
			}

			HashMap<String, String> hmFormat = new HashMap();
			hmFormat = hmRptAttrAndFormatDPL.get(RepetitiveProcessId);
			for (String name1 : hmFormat.keySet()) {
				if (AttrFormat.equalsIgnoreCase(""))
					AttrFormat = hmFormat.get(name1).toString();
				else
					AttrFormat = AttrFormat + "`" + hmFormat.get(name1).toString();
			}

			HashMap<String, String> hmType = new HashMap();
			hmType = hmRptAttrAndTypeDPL.get(RepetitiveProcessId);
			for (String name1 : hmType.keySet()) {
				if (AttrType.equalsIgnoreCase(""))
					AttrType = hmType.get(name1).toString();
				else
					AttrType = AttrType + "`" + hmType.get(name1).toString();
			}

		}

		if (isValidatingMandate.equalsIgnoreCase("Y")) {
			WriteLog("Check111");
			String AttrTagsName[] = AttrTagName.split("`");
			String TransColsName[] = TransColName.split("`");
			String MandateList[] = isMandatory.split("`");
			String AttrFormatList[] = AttrFormat.split("`");
			String AttrTypeList[] = AttrType.split("`");
			if (MandateList.length > 0) {
				for (int i = 0; i < MandateList.length; i++) {
					String flag = "Y";
					
					if ("Y".equalsIgnoreCase(MandateList[i])) {
						if (RepetitiveReqAttr.containsKey(AttrTagsName[i])) {
							if (RepetitiveReqAttr.get(AttrTagsName[i]).trim().equalsIgnoreCase(""))
								flag = "N";
							else {
								// nothing to do
							}
						} else
							flag = "N";
					}
					if (flag.equalsIgnoreCase("N"))
						throw new WICreateException("1020",
								pCodes.getString("1020") + ": " + AttrTagsName[i] + " for " + RepetitiveProcessId);

					WriteLog("Check222 - " + AttrTagsName[i]);
					// block written to validate field level validation
					// try{
					if (RepetitiveReqAttr.containsKey(AttrTagsName[i].trim())) {
						String attrVal = RepetitiveReqAttr.get(AttrTagsName[i]).trim();
						if (!"".equalsIgnoreCase(attrVal) && !"".equalsIgnoreCase(AttrFormatList[i])
								&& !"".equalsIgnoreCase(AttrTypeList[i]) && !"#".equalsIgnoreCase(AttrFormatList[i])
								&& !"#".equalsIgnoreCase(AttrTypeList[i])) {
							checkAttributeFormatAndLength(AttrTagsName[i], attrVal, AttrFormatList[i], AttrTypeList[i]);
						}
					}
					/*
					 * } catch(Exception e){ WriteLog(
					 * "tag isnt available while validating format and type for repetitve tags"
					 * +e.getMessage()); }
					 */
					// **************************************************
					WriteLog("Check333 - " + AttrTagsName[i]);
				}
			} else {
				throw new WICreateException("1021", pCodes.getString("1021") + " for process id: " + processID);
			}

			if (!hmRptAttrAndColDPL.containsKey(RepetitiveProcessId)) {
				if (AttrTagsName.length > 0) {
					HashMap<String, String> hmtmp = new HashMap();
					for (int i = 0; i < AttrTagsName.length; i++) {
						hmtmp.put(AttrTagsName[i], TransColsName[i]);
					}
					hmRptAttrAndColDPL.put(RepetitiveProcessId, hmtmp);

					HashMap<String, String> hmtmp1 = new HashMap();
					for (int i = 0; i < AttrTagsName.length; i++) {
						hmtmp1.put(AttrTagsName[i], MandateList[i]);
					}
					hmRptAttrAndMandDPL.put(RepetitiveProcessId, hmtmp1);

					HashMap<String, String> hmtmp2 = new HashMap();
					for (int i = 0; i < AttrTagsName.length; i++) {
						hmtmp2.put(AttrTagsName[i], AttrFormatList[i]);
					}
					hmRptAttrAndFormatDPL.put(RepetitiveProcessId, hmtmp2);

					HashMap<String, String> hmtmp3 = new HashMap();
					for (int i = 0; i < AttrTagsName.length; i++) {
						hmtmp3.put(AttrTagsName[i], AttrTypeList[i]);
					}
					hmRptAttrAndTypeDPL.put(RepetitiveProcessId, hmtmp3);
				}
			}

		}
		return AttrTagName;
	}

	private void checkAttributeFormatAndLength(String attributeName, String attributeValue, String attrFormat,
			String attType) throws WICreateException, Exception {
		WriteLog("inside checkAttributeFormatAndLength attributeName-" + attributeName + ", attType-" + attType
				+ ", attrFormat-" + attrFormat + ", attributeValue-" + attributeValue);

		if (!(attributeValue.equalsIgnoreCase(""))) {
			int attributelength = attributeValue.length();
			if (!attType.equalsIgnoreCase("")) {
				if (attType.equalsIgnoreCase("DATE")) {
					try {
						Date date = null;
						SimpleDateFormat sdf = new SimpleDateFormat(attrFormat);
						date = sdf.parse(attributeValue);
						if (!attributeValue.equals(sdf.format(date))) {
							throw new WICreateException("1116", pCodes.getString("1116") + " :" + attributeName);
						}
					} catch (Exception ex) {
						throw new WICreateException("1116", pCodes.getString("1116") + " :" + attributeName);
					}
				} else if (attType.equalsIgnoreCase("AMOUNT")) {
					try {
						if (!attributeValue.contains("."))
							attributeValue = attributeValue + ".00";
						Double.parseDouble(attributeValue);

					} catch (Exception ex) {
						throw new WICreateException("1117", pCodes.getString("1117") + " :" + attributeName);
					}

				} else {
					// WriteLog("inside 991--"+attributeName);
					int attributesize = Integer.parseInt(attrFormat);
					if (attributelength > attributesize) {
						throw new WICreateException("1111", pCodes.getString("1111") + " :" + attributeName);
					}
					// WriteLog("inside 992--"+attributeName);
					String patternMatch = "";

					if (attType.equalsIgnoreCase("NUMERIC")) {
						patternMatch = "[0-9]+";
						System.out.println("attributeValue"+attributeValue);
						System.out.println("patternMatch"+patternMatch);
						if (!Pattern.matches(patternMatch, attributeValue)) {
							// WriteLog("inside 995");
							throw new WICreateException("1112", pCodes.getString("1112") + " :" + attributeName);
						}
					}

					if (attType.equalsIgnoreCase("ALPHANUMERIC")) {
						patternMatch = "^[a-zA-Z0-9 ]*$";
						if (!Pattern.matches(patternMatch, attributeValue)) {
							throw new WICreateException("1113", pCodes.getString("1113") + " :" + attributeName);
						}
					}

					if (attType.equalsIgnoreCase("ALPHAONLY")) {
						patternMatch = "^[a-zA-Z ]*$";
						if (!Pattern.matches(patternMatch, attributeValue)) {
							throw new WICreateException("1114", pCodes.getString("1114") + " :" + attributeName);
						}
					}

					if (attType.equalsIgnoreCase("APLPHANUMERICWITHSPACE")) {
						patternMatch = "^[a-zA-Z0-9 ]*$";
						if (!Pattern.matches(patternMatch, attributeValue)) {
							throw new WICreateException("1115", pCodes.getString("1115") + " :" + attributeName);
						}
					}

					if (attType.equalsIgnoreCase("APLPHANUMERICWITHSPECIALCHAR")) {
						WriteLog("inside APLPHANUMERICWITHSPECIALCHAR-" + attributeValue);
						// patternMatch="^[a-zA-Z0-9-#_!.@()+/%&\\s|~ ]*$";
						patternMatch = "^[a-zA-Z0-9-#_!.@()+/%&\\s|~\\[\\]$^*={};:\",<>?\\n\\r\\t\\\\ ]*$";
						if (!Pattern.matches(patternMatch, attributeValue)) {
							throw new WICreateException("1118", pCodes.getString("1118") + " :" + attributeName);
						}
					}
				}
			}
		}

	}

	private void checkMandatoryAttribute() throws WICreateException, Exception {
		WriteLog("inside checkMandatoryAttributeDBO");
		String attributeList[];
			if (hmExtMandDPL.size() == 0) {
				sInputXML = commonMethod.getAPSelectWithColumnNamesXML("select ATTRIBUTENAME from " + WSR_ATTRDETAILS
						+ " with(nolock) where PROCESSID='" + processID + "' and ISMANDATORY='Y' and ISACTIVE='Y'");
				WriteLog("Input XML: " + sInputXML);
				sOutputXML = commonMethod.executeAPI(sInputXML);
				WriteLog("Output XML: " + sOutputXML);
				xmlobj = new XMLParser(sOutputXML);
				checkCallsMainCode(xmlobj);
				attributeList = commonMethod.getTagValues(sOutputXML, "ATTRIBUTENAME").split("`");
				if (attributeList.length > 0) {
					for (int i = 0; i < attributeList.length; i++) {
						hmExtMandDPL.put(attributeList[i], "");
					}
				}
			} else {
				String AttrTagName = "";
				for (String name : hmExtMandDPL.keySet()) {
					if (AttrTagName.equalsIgnoreCase(""))
						AttrTagName = name.toString();
					else
						AttrTagName = AttrTagName + "`" + name.toString();
				}
				attributeList = AttrTagName.split("`");
			}

			if (attributeList.length > 0) {
				for (int i = 0; i < attributeList.length; i++) {
					String flag = "N";
					// Iterate through the Hash Map
					
					Set<?> set = hm.entrySet();
					// Get an iterator
					Iterator<?> j = set.iterator();
					// Display elements
					while (j.hasNext()) {
						Map.Entry me = (Map.Entry) j.next();
						// WriteLog("The key is "+me.getKey());
					/*	String att =attributeList[i];
						String mapValur =me.getKey().toString();
					*/	if (me.getKey().toString().equalsIgnoreCase(attributeList[i])) {
							flag = "Y";
							break;
						}
					}
					// WriteLog("Value of flag: "+flag);
					if (flag.equalsIgnoreCase("N"))
						throw new WICreateException("1020", pCodes.getString("1020") + ": " + attributeList[i]);
				}
			} else {
				throw new WICreateException("1021", pCodes.getString("1021") + " for process id: " + processID);
			}
	}

	private void runWICall() throws WICreateException, Exception {
		try {
			sInputXML = commonMethod.getWFUploadWorkItemXML(sSessionID, processDefID, InitiationQueueID, attributeTag,sInitiateAlso);
			WriteLog("WFUploadWorkItemXML Input: " + sInputXML);
			sOutputXML = commonMethod.executeAPI(sInputXML);
			WriteLog("WFUploadWorkItemXML Output: " + sOutputXML);
			xmlobj = new XMLParser(sOutputXML);
			// Check Main Code
			checkCallsMainCode(xmlobj);
			wiName = commonMethod.getTagValues(sOutputXML, "ProcessInstanceId");
			WriteLog("WINAME: " + wiName);
			// Update associated trans table
			setSuccessParamResponse();
			response.setWorkitemNumber(wiName);
		} catch (Exception e) {
			WriteLog("error msg" + e.getMessage());
		}

	}

	private void setSuccessParamResponse() {
		try {
			headerObjResponse.setExtra1(sExtra1);
			headerObjResponse.setExtra2(sExtra2);
			headerObjResponse.setMessageId(sMessageId);
			headerObjResponse.setMsgFormat(sMsgFormat);
			headerObjResponse.setMsgVersion(sMsgVersion);
			headerObjResponse.setRequestorChannelId(sRequestorChannelId);
			headerObjResponse.setRequestorLanguage(sRequestorLanguage);
			headerObjResponse.setRequestorSecurityInfo(sRequestorSecurityInfo);
			headerObjResponse.setRequestorUserId(sRequestorUserId);
			headerObjResponse.setReturnCode("0");
			headerObjResponse.setReturnDesc("Success");
			
			response.setEE_EAI_HEADER(headerObjResponse);

		} catch (Exception e) {
			WriteLog(e.getMessage());
		}

	}
}
