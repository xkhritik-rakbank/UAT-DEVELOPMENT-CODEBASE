package com.newgen.DPL.lms;

import com.newgen.wfdesktop.xmlapi.WFInputXml;

public class GenerateXML {

	public static String ExecuteQuery_APSelect(String sQuery, String sEngineName, String sSessionId) {
		WFInputXml wfInputXml = new WFInputXml();

		wfInputXml.appendStartCallName("APSelect", "Input");
		wfInputXml.appendTagAndValue("Query", sQuery);
		wfInputXml.appendTagAndValue("EngineName", sEngineName);
		wfInputXml.appendTagAndValue("SessionId", sSessionId);
		wfInputXml.appendEndCallName("APSelect", "Input");
		return wfInputXml.toString();
	}

	public static String ExecuteQuery_APSelectWithColumnNames(String sQuery, String sEngineName, String sSessionId) {
		WFInputXml wfInputXml = new WFInputXml();

		wfInputXml.appendStartCallName("APSelectWithColumnNames", "Input");
		wfInputXml.appendTagAndValue("Query", sQuery);
		wfInputXml.appendTagAndValue("EngineName", sEngineName);
		wfInputXml.appendTagAndValue("SessionId", sSessionId);
		wfInputXml.appendEndCallName("APSelectWithColumnNames", "Input");
		return wfInputXml.toString();
	}

	public static String ExecuteQuery_APUpdate(String tableName, String columnName, String strValues, String sWhere,
			String cabinetName, String sessionId) {
		WFInputXml wfInputXml = new WFInputXml();
		if (strValues == null) {
			strValues = "''";
		}
		wfInputXml.appendStartCallName("APUpdate", "Input");
		wfInputXml.appendTagAndValue("TableName", tableName);
		wfInputXml.appendTagAndValue("ColName", columnName);
		wfInputXml.appendTagAndValue("Values", strValues);
		wfInputXml.appendTagAndValue("WhereClause", sWhere);
		wfInputXml.appendTagAndValue("EngineName", cabinetName);
		wfInputXml.appendTagAndValue("SessionId", sessionId);
		wfInputXml.appendEndCallName("APUpdate", "Input");
		return wfInputXml.toString();
	}

	public static String ExecuteQuery_APInsert(String tableName, String columnName, String strValues,
			String cabinetName, String sessionId) {
		WFInputXml wfInputXml = new WFInputXml();

		wfInputXml.appendStartCallName("APInsert", "Input");
		wfInputXml.appendTagAndValue("TableName", tableName);
		wfInputXml.appendTagAndValue("ColName", columnName);
		wfInputXml.appendTagAndValue("Values", strValues);
		wfInputXml.appendTagAndValue("EngineName", cabinetName);
		wfInputXml.appendTagAndValue("SessionId", sessionId);
		wfInputXml.appendEndCallName("APInsert", "Input");
		return wfInputXml.toString();
	}

	public static String ExecuteQuery_APComplete(String cabinetName, String sessionId, String wi_name) {
		WFInputXml wfInputXml = new WFInputXml();

		wfInputXml.appendStartCallName("WMCompleteWorkItem", "Input");
		wfInputXml.appendTagAndValue("EngineName", cabinetName);
		wfInputXml.appendTagAndValue("SessionId", sessionId);
		wfInputXml.appendTagAndValue("ProcessInstanceId", wi_name);
		wfInputXml.appendTagAndValue("WorkItemId", "1");
		wfInputXml.appendTagAndValue("AuditStatus", "");
		wfInputXml.appendEndCallName("WMCompleteWorkItem", "Input");

		return wfInputXml.toString();
	}

	public static String get_WFUploadWorkItem_Input(String cabinetName, String sessionID, String processdefId,
			String attList, String attachmentString) {
		if (attachmentString == null)
			attachmentString = "";
		WFInputXml wfInputXml = new WFInputXml();

		wfInputXml.appendStartCallName("WFUploadWorkItem", "Input");
		wfInputXml.appendTagAndValue("EngineName", cabinetName);
		wfInputXml.appendTagAndValue("SessionId", sessionID);
		wfInputXml.appendTagAndValue("ProcessDefId", processdefId);
		wfInputXml.appendTagAndValue("Attributes", attList);
		wfInputXml.appendTagAndValue("Documents", attachmentString);
		wfInputXml.appendEndCallName("WFUploadWorkItem", "Input");

		return wfInputXml.toString();
	}

	public static String get_NGOAddDocument_Input(String cabinetName, String sessionID, String folderIndex,
			String docSize, String DocumentName, String strISIndex) {
		StringBuffer ipXMLBuffer = new StringBuffer();

		ipXMLBuffer.append("?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		ipXMLBuffer.append("<NGOAddDocument_Input>\n");
		ipXMLBuffer.append("<Option>NGOAddDocument</Option>\n");
		ipXMLBuffer.append("<CabinetName>");
		ipXMLBuffer.append(cabinetName);
		ipXMLBuffer.append("</CabinetName>\n");
		ipXMLBuffer.append("<UserDBId>");
		ipXMLBuffer.append(sessionID);
		ipXMLBuffer.append("</UserDBId>\n");
		ipXMLBuffer.append("<GroupIndex>0</GroupIndex>\n");
		ipXMLBuffer.append("<Document>\n");
		ipXMLBuffer.append("<ParentFolderIndex>");
		ipXMLBuffer.append(folderIndex);
		ipXMLBuffer.append("</ParentFolderIndex>\n");
		ipXMLBuffer.append("<NoOfPages>1</NoOfPages>\n");
		ipXMLBuffer.append("<AccessType>I</AccessType>\n");
		ipXMLBuffer.append("<DocumentName>");
		ipXMLBuffer.append(DocumentName);
		ipXMLBuffer.append("</DocumentName>\n");
		ipXMLBuffer.append("<CreatedByAppName>pdf</CreatedByAppName>\n");
		ipXMLBuffer.append("<ISIndex>");
		ipXMLBuffer.append(strISIndex);
		ipXMLBuffer.append("</ISIndex>\n");
		ipXMLBuffer.append("<NoOfPages>1</NoOfPages>");
		ipXMLBuffer.append("<DocumentType>N</DocumentType>\n");
		ipXMLBuffer.append("<DocumentSize>");
		ipXMLBuffer.append(docSize);
		ipXMLBuffer.append("</DocumentSize>\n");
		ipXMLBuffer.append("<ODMADocumentIndex></ODMADocumentIndex><Comment></Comment><EnableLog>Y</EnableLog>\n");
		ipXMLBuffer.append("<FTSFlag>PP</FTSFlag>");
		ipXMLBuffer.append("</Document>\n");
		ipXMLBuffer.append("</NGOAddDocument_Input>");

		return ipXMLBuffer.toString();
	}

	public static String getConnectInputXML(String cabinetName, String username, String password) {
		StringBuffer ipXMLBuffer = new StringBuffer();

		ipXMLBuffer.append("<?xml version=\"1.0\"?>");
		ipXMLBuffer.append("<WMConnect_Input>");
		ipXMLBuffer.append("<Option>WMConnect</Option>");
		ipXMLBuffer.append("<EngineName>");
		ipXMLBuffer.append(cabinetName);
		ipXMLBuffer.append("</EngineName>\n");
		ipXMLBuffer.append("<ApplicationInfo></ApplicationInfo>\n");
		ipXMLBuffer.append("<Participant>\n");
		ipXMLBuffer.append("<Name>");
		ipXMLBuffer.append(username);
		ipXMLBuffer.append("</Name>\n");
		ipXMLBuffer.append("<Password>");
		ipXMLBuffer.append(password);
		ipXMLBuffer.append("</Password>\n");
		ipXMLBuffer.append("<Scope></Scope>\n");
		ipXMLBuffer.append("<UserExist>N</UserExist>\n");
		ipXMLBuffer.append("<Locale>en-us</Locale>\n");
		ipXMLBuffer.append("<ParticipantType>U</ParticipantType>\n");
		ipXMLBuffer.append("</Participant>");
		ipXMLBuffer.append("</WMConnect_Input>");

		return ipXMLBuffer.toString();
	}

	public static String getConnectIpXML(String cabinetName, String username, String password) {
		StringBuffer ipXMLBuffer = new StringBuffer();

		ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
		ipXMLBuffer.append("<WMConnect_Input>\n");
		ipXMLBuffer.append("<Option>WMConnect</Option>\n");
		ipXMLBuffer.append("<EngineName>");
		ipXMLBuffer.append(cabinetName);
		ipXMLBuffer.append("</EngineName>\n");
		ipXMLBuffer.append("<ApplicationInfo></ApplicationInfo>\n");
		ipXMLBuffer.append("<Participant>\n");
		ipXMLBuffer.append("<Name>");
		ipXMLBuffer.append(username);
		ipXMLBuffer.append("</Name>\n");
		ipXMLBuffer.append("<Password>");
		ipXMLBuffer.append(password);
		ipXMLBuffer.append("</Password>\n");
		ipXMLBuffer.append("<Scope></Scope>\n");
		ipXMLBuffer.append("<UserExist>N</UserExist>\n");
		ipXMLBuffer.append("<Locale>en-us</Locale>\n");
		ipXMLBuffer.append("<ParticipantType>U</ParticipantType>\n");
		ipXMLBuffer.append("</Participant>");
		ipXMLBuffer.append("</WMConnect_Input>");

		return ipXMLBuffer.toString();
	}

	public static String sendMail(String cabinetname, String sessionID, String mailFrom, String mailTo,
			String mailSubject, String mailMessage, String attachmentIndex, String attachmentName) {
		if (attachmentIndex == null)
			attachmentIndex = "";
		if (attachmentName == null)
			attachmentName = "";

		WFInputXml wfInputXml = new WFInputXml();

		wfInputXml.appendStartCallName("WFAddToMailQueue", "Input");
		wfInputXml.appendTagAndValue("EngineName", cabinetname);
		wfInputXml.appendTagAndValue("SessionId", sessionID);
		wfInputXml.appendTagAndValue("MailFrom", mailFrom);
		wfInputXml.appendTagAndValue("MailTo", mailTo);
		wfInputXml.appendTagAndValue("MailCC", "");
		wfInputXml.appendTagAndValue("MailSubject", mailSubject);
		wfInputXml.appendTagAndValue("MailMessage", mailMessage);
		wfInputXml.appendTagAndValue("AttachmentISIndex", attachmentIndex);
		wfInputXml.appendTagAndValue("AttachmentNames", attachmentName);
		wfInputXml.appendTagAndValue("AttachmentExts", "");
		wfInputXml.appendEndCallName("WFAddToMailQueue", "Input");

		return wfInputXml.toString();
	}

	public static String getFetchWorkItemsInputXML(String processInstanceId, String lastWorkItemId, String sessionId,
			String cabinetName, String queueId) {
		StringBuffer ipXMLBuffer = new StringBuffer();

		ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
		ipXMLBuffer.append("<WMFetchWorkItems_Input>\n");
		ipXMLBuffer.append("<Option>WMFetchWorkItem</Option>\n");
		ipXMLBuffer.append("<EngineName>");
		ipXMLBuffer.append(cabinetName);
		ipXMLBuffer.append("</EngineName>\n");
		ipXMLBuffer.append("<SessionID>");
		ipXMLBuffer.append(sessionId);
		ipXMLBuffer.append("</SessionID>\n");
		ipXMLBuffer.append("<OrderBy>10</OrderBy>\n");
		ipXMLBuffer.append("<SortOrder>D</SortOrder>\n");
		ipXMLBuffer.append("<QueueId>");
		ipXMLBuffer.append(queueId);
		ipXMLBuffer.append("</QueueId>\n");
		ipXMLBuffer.append("<BatchInfo>\n");
		ipXMLBuffer.append("<NoOfRecordsToFetch>100</NoOfRecordsToFetch>\n");
		ipXMLBuffer.append("<LastWorkItem>");
		ipXMLBuffer.append(lastWorkItemId);
		ipXMLBuffer.append("</LastWorkItem>\n");
		ipXMLBuffer.append("<LastValue></LastValue>\n");
		ipXMLBuffer.append("<LastProcessInstance>");
		ipXMLBuffer.append(processInstanceId);
		ipXMLBuffer.append("</LastProcessInstance>\n");
		ipXMLBuffer.append("</BatchInfo>\n");
		ipXMLBuffer.append("</WMFetchWorkItems_Input>");
		return ipXMLBuffer.toString();
	}

	public static String getNextFetchWorkItemsInputXML(String processInstanceId, String lastWorkItemId,
			String sessionId, String cabinetName, String queueId) {
		StringBuffer ipXMLBuffer = new StringBuffer();

		ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
		ipXMLBuffer.append("<WMFetchWorkList_Input>\n");
		ipXMLBuffer.append("<Option>WMFetchWorkList</Option>\n");
		ipXMLBuffer.append("<EngineName>");
		ipXMLBuffer.append(cabinetName);
		ipXMLBuffer.append("</EngineName>\n");
		ipXMLBuffer.append("<SessionID>");
		ipXMLBuffer.append(sessionId);
		ipXMLBuffer.append("</SessionID>\n");
		ipXMLBuffer.append("<CountFlag>Y</CountFlag>\n");
		ipXMLBuffer.append("<OrderBy>10</OrderBy>\n");
		ipXMLBuffer.append("<SortOrder>D</SortOrder>\n");
		ipXMLBuffer.append("<DataFlag>Y</DataFlag>\n");
		ipXMLBuffer.append("<ZipBuffer>N</ZipBuffer>\n");
		ipXMLBuffer.append("<FetchLockedFlag>N</FetchLockedFlag>\n");
		ipXMLBuffer.append("<QueueId>");
		ipXMLBuffer.append(queueId);
		ipXMLBuffer.append("</QueueId>\n");
		ipXMLBuffer.append("<Filter>\n");
		ipXMLBuffer.append("<Type>256</Type>\n");
		ipXMLBuffer.append("<Comparison>0</Comparison>\n");
		ipXMLBuffer.append("<FilterString>\n");
		ipXMLBuffer.append(
				"((var_str1 = '103' or var_str1 = '103+' or var_date1 is null or			to_date(var_date1,'dd/MON/yyyy') <= to_date(sysdate,'dd/MON/yyyy')) AND PROCESSINSTANCEID like '%STP%')\n");
		ipXMLBuffer.append("</FilterString>\n");
		ipXMLBuffer.append("<Length>0</Length>\n");
		ipXMLBuffer.append("</Filter>\n");
		ipXMLBuffer.append("<BatchInfo>\n");
		ipXMLBuffer.append("<NoOfRecordsToFetch>100</NoOfRecordsToFetch>\n");
		ipXMLBuffer.append("<OrderBy>1</OrderBy>\n");
		ipXMLBuffer.append("<SortOrder>A</SortOrder>\n");
		ipXMLBuffer.append("<LastValue>1</LastValue>\n");
		ipXMLBuffer.append("<LastProcessInstance>");
		ipXMLBuffer.append(processInstanceId);
		ipXMLBuffer.append("</LastProcessInstance>\n");
		ipXMLBuffer.append("<LastWorkItem>1</LastWorkItem>\n");
		ipXMLBuffer.append("</BatchInfo>\n");
		ipXMLBuffer.append("<QueueType>I</QueueType>\n");
		ipXMLBuffer.append("<ClientOrderFlag>N</ClientOrderFlag>\n");
		ipXMLBuffer.append("</WMFetchWorkList_Input>");
		return ipXMLBuffer.toString();
	}

	public static String getGetWorkItemXML1(String processInstanceId, String workItemId, String sessionId,
			String cabinetName) {
		WFInputXml wfInputXml = new WFInputXml();

		wfInputXml.appendStartCallName("WMGetWorkItem", "Input");
		wfInputXml.appendTagAndValue("EngineName", cabinetName);
		wfInputXml.appendTagAndValue("SessionId", sessionId);
		wfInputXml.appendTagAndValue("ProcessInstanceId", processInstanceId);
		wfInputXml.appendTagAndValue("WorkItemId", "1");
		wfInputXml.appendEndCallName("WMGetWorkItem", "Input");

		return wfInputXml.toString();
	}

	public static String getUnLockWIXML(String processInstanceId, String workItemId, String sessionId,
			String cabinetName) {
		WFInputXml wfInputXml = new WFInputXml();

		wfInputXml.appendStartCallName("WMUnlockWorkItem", "Input");
		wfInputXml.appendTagAndValue("EngineName", cabinetName);
		wfInputXml.appendTagAndValue("SessionId", sessionId);
		wfInputXml.appendTagAndValue("ProcessInstanceId", processInstanceId);
		wfInputXml.appendTagAndValue("WorkItemId", workItemId);
		wfInputXml.appendEndCallName("WMUnlockWorkItem", "Input");

		return wfInputXml.toString();
	}

	public static String APProcedure_WithDBO(String ProcName, String Params, String sSessionId, String sCabname) {
		String sInputXML = "<?xml version=\"1.0\"?>\n" + "<APProcedure_WithDBO_Input>\n"
				+ "<option>APProcedure_WithDBO</option>\n" + "<ProcName>" + ProcName + "</ProcName>\n" + "<Params>"
				+ "'" + Params + "'" + "</Params>\n" + "<EngineName>" + sCabname + "</EngineName>\n" + "<SessionId>"
				+ sSessionId + "</SessionID>\n" + "</APProcedure_WithDBO_Input>";
		return sInputXML;
	}

	public static String ExecuteQuery_APProcedure(String ProcName, String Params, String cabinetName,
			String sessionId) {
		WFInputXml wfInputXml = new WFInputXml();

		wfInputXml.appendStartCallName("APProcedure_WithDBO", "Input");
		wfInputXml.appendTagAndValue("ProcName", ProcName);
		wfInputXml.appendTagAndValue("Params", Params);
		wfInputXml.appendTagAndValue("EngineName", cabinetName);
		wfInputXml.appendTagAndValue("SessionId", sessionId);
		wfInputXml.appendEndCallName("APProcedure_WithDBO", "Input");
		return wfInputXml.toString();
	}

	public static String get_WMConnect_Input(String cabinetName, String userName, String password, String forceful) {
		return "<?xml version=\"1.0\"?><WMConnect_Input><Option>WMConnect</Option><UserExist>" + forceful
				+ "</UserExist><EngineName>" + cabinetName + "</EngineName>\n" + "<Particpant>\n" + "<Name>" + userName
				+ "</Name>\n" + "<Password>" + password + "</Password>\n" + "<Scope>USER</Scope>\n"
				+ "<ParticipantType>U</ParticipantType>\n" + "</Participant>\n" + "</WMConnect_Input>";
	}

	public static String get_WMDisConnect_Input(String cabinetName, String sessionID) {
		return "<? Xml Version=\"1.0\"?>" + "<WMDisConnect_Input>" + "<Option>WMDisConnect</Option>" + "<EngineName>"
				+ cabinetName + "</EngineName>" + "<SessionID>" + sessionID + "</SessionID>" + "</WMDisConnect_Input>";
	}

	public static String APSelectWithColumnNames(String strEngineName, String strQuery) {
		WFInputXml wfInputXml = new WFInputXml();
		wfInputXml.appendStartCallName("APSelectWithColumnNames", "Input");
		wfInputXml.appendTagAndValue("EngineName", strEngineName);
		wfInputXml.appendTagAndValue("Query", strQuery);
		wfInputXml.appendEndCallName("APSelect", "Input");
		return wfInputXml.toString();
	}

}
