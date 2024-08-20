package com.newgen.PL;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import com.newgen.custom.CreateWorkitem;
import com.newgen.ws.EE_EAI_HEADER;
import com.newgen.ws.exception.WICreateException;
import com.newgen.ws.request.Attribute;
import com.newgen.ws.request.Attributes;
import com.newgen.ws.request.WICreateRequest;
import com.newgen.ws.response.WICreateResponse;
import com.newgen.ws.util.XMLParser;

public class Pl_WIUpdateService extends CreateWorkitem{

	final private String SUCCESS_STATUS="0";
	final private String WSR_PROCESS="USR_0_WSR_UPDATE_PROCESS";
	final private String WSR_ATTRDETAILS="USR_0_WSR_UPDATE_ATTRDETAILS";
	final private String WSR_POSSIBLEQUEUES="USR_0_WSR_UPDATE_POSSIBLEQUEUES";

	EE_EAI_HEADER headerObjResponse;
	WICreateResponse response = new WICreateResponse();
	PL_WICommonMethod commonMethod = new PL_WICommonMethod();
	
	private String sProcessName;
	private String sSubProcess;
	private String InputMessage = "";
	private String sMsgFormat;
	private String sMsgVersion;
	private String sRequestorChannelId;
	private String sRequestorUserId;
	private String sRequestorLanguage;
	private String sRequestorSecurityInfo;
	private String sMessageId;
	private String sExtra1;
	private String sExtra2;
	private String processDefID = "";
	private String processID = "";
	private String WINumber = "";
	private String ActivityName="";
	private String PossibleQueues="";
	private String HISTORYTABLE = "";
	private String txnTableName="NG_DPL_EXTTABLE";
	private String sSessionID = "";
	private String attributeTag = "";
	private String sDate = "";
	private String pingDataColNames = "";
	private String pingDataColValues = "";
	private String workitemID = "";
	private String extColNames = "";
	private String extColValues = "";
	private String trTableColumn = "";
	private String trTableValue = "";
	private String sRemarksAddInfo="";
	private String FircoUpdateActionDPL="";
	private Attributes attributesObj;
	private Attribute attributeObj[];
	private ResourceBundle pCodes;
	
	boolean sessionFlag = false;
	
	String eCode = "";
	String eDesc = ""; 
	String sInputXML = "";
	String sOutputXML = "";
	String RepetitiveMainTags = "";
	
	XMLParser xmlobj;
	
  	
	private EE_EAI_HEADER headerObjRequest;
	
	HashMap<String, String> hm = new HashMap(); 
	LinkedHashMap<String, HashMap<String, String>> hmMain = new LinkedHashMap();
	static HashMap<String, String> hmRptProcessId = new HashMap();
	static HashMap<String, String> hmRptTransTable = new HashMap();
    static LinkedHashMap<String, HashMap<String, String>> hmRptAttrAndCol = new LinkedHashMap();
    static LinkedHashMap<String, HashMap<String, String>> hmRptAttrAndMand = new LinkedHashMap();
    static LinkedHashMap<String, HashMap<String, String>> hmRptAttrAndFormat = new LinkedHashMap();
    static LinkedHashMap<String, HashMap<String, String>> hmRptAttrAndType = new LinkedHashMap();
   
	
	public WICreateResponse wiUpdate(WICreateRequest request,String attributeList[]) throws WICreateException{
		
		 
		
		try
		{
			WriteLog("inside wiUpdate for DBO process");
			
			headerObjResponse = new EE_EAI_HEADER();

			// Load file configuration
			this.response = commonMethod.loadConfiguration();

			// Load ResourceBundle
			commonMethod.loadResourceBundle();

			//Fetching request parameters 
			fetchRequestParameters(request);
								
			//Validating Input Parameters and Check Attribute Name from USR_0_WSR_UPDATE_ATTRDETAILS
			validateRequestParameters(attributeList);
			
			validateRepetitiveRequestParameters();
			
			//Checking existing session
			checkExistingSession();
			
			//attributeTag=attributeTag+createRepetativeAttributeXML();
			
			String decisionInHistory = "";
    		String remark = "";
    		
			if ("STATEMENT_ANALYZED".equalsIgnoreCase(sSubProcess))
    		{
    			WriteLog("WI update for STATEMENT_ANALYZED- extColNames: "+extColNames);
    			WriteLog("WI update for STATEMENT_ANALYZED- extColValues: "+extColValues);
    			
    			trTableColumn=extColNames+"IsFTSDocProvided, Decision";
    			trTableValue=extColValues+"'Y','Success'";
    			
    			WriteLog("WI update for STATEMENT_ANALYZED- trTableColumn: "+trTableColumn);
    			WriteLog("WI update for STATEMENT_ANALYZED- trTableValue: "+trTableValue);
    			
    			decisionInHistory = "FTS Details Received";
    			remark="FTS Details Updated";
    			
    			String whereClause = "winame='"+hm.get("WINUMBER")+"'";
    			insertIntoExtTable("NG_DPL_EXTTABLE",trTableColumn,trTableValue,sSessionID,whereClause);
				insertIntoHistory(remark, decisionInHistory);
				getWorkitemID();
				doneworkitem();
				
				//Insert Net Salary in the table - NG_DCC_GR_NetSalaryDetails.
				Date d= new Date();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				sDate = dateFormat.format(d);
				
				String net1 =  hm.get("Stmt_Salary1");
    			String net2 =  hm.get("Stmt_Salary2");
    			String net3 =  hm.get("Stmt_Salary3");
				String net4 =  hm.get("Stmt_Salary4");
				String net5 =  hm.get("Stmt_Salary5");
				String net6 =  hm.get("Stmt_Salary6");
				String net7 =  hm.get("Stmt_Salary7");
				
				String net1date =  hm.get("Stmt_salary1_date");
				String net2date =  hm.get("Stmt_salary2_date");
    			String net3date =  hm.get("Stmt_salary3_date");
    			String net4date =  hm.get("Stmt_salary4_date");
    			String net5date =  hm.get("Stmt_salary5_date");
    			String net6date =  hm.get("Stmt_salary6_date");
    			String net7date =  hm.get("Stmt_salary7_date");
    			
				String cf_month1 =  hm.get("Cummulative_flag_month1");
    			String cf_month2 =  hm.get("Cummulative_flag_month2");
    			String cf_month3 =  hm.get("Cummulative_flag_month3");
    			
				String emi1 =  hm.get("Addn_Perfios_EMI_1");
    			String emi2 =  hm.get("Addn_Perfios_EMI_2");
    			String emi3 =  hm.get("Addn_Perfios_EMI_3");
    			String emi4 =  hm.get("Addn_Perfios_EMI_4");
    			String emi5 =  hm.get("Addn_Perfios_EMI_5");
    			String emi6 =  hm.get("Addn_Perfios_EMI_6");
    			String emi7 =  hm.get("Addn_Perfios_EMI_7");
				String emi8 =  hm.get("Addn_Perfios_EMI_8");
    			String emi9 =  hm.get("Addn_Perfios_EMI_9");
    			String emi10 =  hm.get("Addn_Perfios_EMI_10");
    			String emi11 =  hm.get("Addn_Perfios_EMI_11");
    			String emi12 =  hm.get("Addn_Perfios_EMI_12");
				String emi13 =  hm.get("Addn_Perfios_EMI_13");
    			String emi14 =  hm.get("Addn_Perfios_EMI_14");
    			String emi15 =  hm.get("Addn_Perfios_EMI_15");
				String emi16 =  hm.get("Addn_Perfios_EMI_16");
    			String emi17 =  hm.get("Addn_Perfios_EMI_17");
				
				String addional1 =  hm.get("Addional_CC_payment_1");
				String addional2 =  hm.get("Addional_CC_payment_2");
				String addional3 =  hm.get("Addional_CC_payment_3");
				String addional4 =  hm.get("Addional_CC_payment_4");
				String addional5 =  hm.get("Addional_CC_payment_5");
    			
				String Addn_Perfios_OD_Amt =  hm.get("Addn_Perfios_OD_Amt");
    			String Addn_OD_date =  hm.get("Addn_OD_date");
    			String Joint_Acct =  hm.get("Joint_Acct");
    			String High_Value_Deposit =  hm.get("High_Value_Deposit");
    			String Credit_Amount =  hm.get("Credit_Amount");
    			
    			String Stmt_chq_rtn_last_3mnts =  hm.get("Stmt_chq_rtn_last_3mnts");
				String Stmt_chq_rtn_cleared_in30_last_3mnts =  hm.get("Stmt_chq_rtn_cleared_in30_last_3mnts");
    			String Stmt_chq_rtn_last_1mnt =  hm.get("Stmt_chq_rtn_last_1mnt");
    			String Stmt_chq_rtn_cleared_in30_last_1mnt =  hm.get("Stmt_chq_rtn_cleared_in30_last_1mnt");
				
				String Stmt_DDS_rtn_last_3mnts =  hm.get("Stmt_DDS_rtn_last_3mnts");
				String Stmt_DDS_rtn_cleared_in30_last_3mnts =  hm.get("Stmt_DDS_rtn_cleared_in30_last_3mnts");
				String Stmt_DDS_rtn_last_1mnt =  hm.get("Stmt_DDS_rtn_last_1mnt");
    			String Stmt_DDS_rtn_cleared_in30_last_1mnts =  hm.get("Stmt_DDS_rtn_cleared_in30_last_1mnts");
    			
				String Name_match =  hm.get("Name_match");
    			String Pensioner_flag =  hm.get("Pensioner_flag");
    			String Statement_Key =  hm.get("Statement_Key");
    			String Immediate_Withdraw = hm.get("Immediate_Withdraw");
    			
				String WI =  hm.get("WINUMBER");
    			
    			String NetSal_Col="Net_Salary_Month_1,Net_Salary_Month_2,Net_Salary_Month_3,Net_Salary_Month_4,Net_Salary_Month_5,Net_Salary_Month_6,Net_Salary_Month_7,Salary_date_Month_1,"
    					+ "Salary_date_Month_2,Salary_date_Month_3,Salary_date_Month_4,Salary_date_Month_5,Salary_date_Month_6,Stmt_salary7_date,"
    					+ "Cummulative_flag_month1,Cummulative_flag_month2,Cummulative_flag_month3,Addn_Perfios_EMI_1,Addn_Perfios_EMI_2,"
    					+ "Addn_Perfios_EMI_3,Addn_Perfios_EMI_4,Addn_Perfios_EMI_5,Addn_Perfios_EMI_6,Addn_Perfios_EMI_7,Addn_Perfios_EMI_8,"
    					+ "Addn_Perfios_EMI_9,Addn_Perfios_EMI_10,Addn_Perfios_EMI_11,Addn_Perfios_EMI_12,Addn_Perfios_EMI_13,Addn_Perfios_EMI_14,"
    					+ "Addn_Perfios_EMI_15,Addn_Perfios_EMI_16,Addn_Perfios_EMI_17,Addional_CC_payment_1,Addional_CC_payment_2,"
    					+ "Addional_CC_payment_3,Addional_CC_payment_4,Addional_CC_payment_5,Addn_Perfios_OD_Amt,Addn_OD_date,Joint_Acct,"
    					+ "High_Value_Deposit,Credit_Amount,Stmt_chq_rtn_last_3mnts,Stmt_chq_rtn_cleared_in30_last_3mnts,Stmt_chq_rtn_last_1mnt,"
    					+ "Stmt_chq_rtn_cleared_in30_last_1mnt,Stmt_DDS_rtn_last_3mnts,Stmt_DDS_rtn_cleared_in30_last_3mnts,Stmt_DDS_rtn_last_1mnt,"
    					+ "Stmt_DDS_rtn_cleared_in30_last_1mnts,Name_match,Pensioner_flag,Statement_Key,immediate_withdraw,Wi_Name";
    			
    			String NetSal_val = "'"+net1+"','"+net2+"','"+net3+"','"+net4+"','"+net5+"','"+net6+"','"+net7+"',"
    					+ "'"+net1date+"','"+net2date+"','"+net3date+"','"+net4date+"','"+net5date+"','"+net6date+"','"+net7date+"',"
    							+ "'"+cf_month1+"','"+cf_month2+"','"+cf_month3+"','"+emi1+"','"+emi2+"','"+emi3+"','"+emi4+"','"+emi5+"',"
    									+ "'"+emi6+"','"+emi7+"','"+emi8+"','"+emi9+"','"+emi10+"','"+emi11+"','"+emi12+"','"+emi13+"','"+emi14+"',"
    											+ "'"+emi15+"','"+emi16+"','"+emi17+"','"+addional1+"','"+addional2+"','"+addional3+"','"+addional4+"',"
    													+ "'"+addional5+"','"+Addn_Perfios_OD_Amt+"','"+Addn_OD_date+"','"+Joint_Acct+"','"+High_Value_Deposit+"',"
    															+ "'"+Credit_Amount+"','"+Stmt_chq_rtn_last_3mnts+"','"+Stmt_chq_rtn_cleared_in30_last_3mnts+"',"
    																	+ "'"+Stmt_chq_rtn_last_1mnt+"','"+Stmt_chq_rtn_cleared_in30_last_1mnt+"','"+Stmt_DDS_rtn_last_3mnts+"',"
    																			+ "'"+Stmt_DDS_rtn_cleared_in30_last_3mnts+"','"+Stmt_DDS_rtn_last_1mnt+"','"+Stmt_DDS_rtn_cleared_in30_last_1mnts+"',"
    																					+ "'"+Name_match+"','"+Pensioner_flag+"','"+Statement_Key+"','"+Immediate_Withdraw+"','"+WI+"'";
        		
    			
    			sInputXML = commonMethod.getAPInsertInputXML("NG_DPL_IncomeExpense", NetSal_Col, NetSal_val, sSessionID);
    			
				WriteLog("The input XML NG_DPL_IncomeExpense "+sInputXML);
				sOutputXML = commonMethod.executeAPI(sInputXML);
				WriteLog("APInsert Output for transaction update table getInsert_NG_DPL_IncomeExpense: " + sOutputXML);
				xmlobj = new XMLParser(sOutputXML);
				checkCallsMainCode(xmlobj);
				WriteLog("Insert Net Salary in the table -NG_DPL_IncomeExpense  END.");
    		}
			
    		
    		// returning success response
        	returnResponse();
		}
		catch(WICreateException e)
		{
			WriteLog("WICreateException caught:Code- "+e.getErrorCode());
			WriteLog("WICreateException caught:Description "+e.getErrorDesc());
			WriteLog("WICreateException: "+e.toString());
			this.headerObjResponse = commonMethod.setFailureParamResponse(sExtra1,sExtra2,sMessageId,sMsgFormat,sMsgVersion,sRequestorChannelId,sRequestorLanguage,sRequestorSecurityInfo,sRequestorUserId);
			response.setErrorCode(e.getErrorCode());
			response.setErrorDescription(e.getErrorDesc());
			return response;
		}
		catch(IOException e)
		{
			this.headerObjResponse = commonMethod.setFailureParamResponse(sExtra1,sExtra2,sMessageId,sMsgFormat,sMsgVersion,sRequestorChannelId,sRequestorLanguage,sRequestorSecurityInfo,sRequestorUserId);
			//commonMethod.setFailureParamResponse();
			response.setErrorCode("1009");
    		response.setErrorDescription(pCodes.getString("1009")+": "+e.getMessage());

    		return response;
		}
		catch(Exception e)
		{
			this.headerObjResponse = commonMethod.setFailureParamResponse(sExtra1,sExtra2,sMessageId,sMsgFormat,sMsgVersion,sRequestorChannelId,sRequestorLanguage,sRequestorSecurityInfo,sRequestorUserId);
			//commonMethod.setFailureParamResponse();
			response.setErrorCode("1010");
    		response.setErrorDescription(pCodes.getString("1010")+": "+e.getMessage());
    		return response;
		}
		finally
		{			
			WriteLog("Inside finally method to dispose off conenction objects and hash maps");
			try
			{
				// Commented below code for Invalid Session Issue.
				/*if(sessionFlag==true)
					deleteConnection();*/
			}
			catch (Exception e) 
			{
				WriteLog("Exception: "+e.getMessage());					
			}
			hm.clear();
			hmMain.clear();
		}
		return response;
	

	}
	
	private void fetchRequestParameters(WICreateRequest req) throws WICreateException
	{		
		try
		{
			WriteLog("Inside fetchRequestParameters");
			
			sProcessName=(req.getProcessName()==null)? "" : req.getProcessName().trim();
			sSubProcess=(req.getSubProcess()==null)? "" : req.getSubProcess().trim();
			headerObjRequest=req.getEE_EAI_HEADER();
			
			InputMessage=(req.getInputMessage()==null)? "" : req.getInputMessage().trim();
			InputMessage=(InputMessage==null)? "" : InputMessage.replace("&amp;","&");
			InputMessage=(InputMessage==null)? "" : InputMessage.replace("&AMP;","&");
			
			sMsgFormat=(headerObjRequest.getMsgFormat()==null)? "" : headerObjRequest.getMsgFormat().trim();
			sMsgVersion=(headerObjRequest.getMsgVersion()==null)? "" : headerObjRequest.getMsgVersion().trim();
			sMsgFormat=(headerObjRequest.getMsgFormat()==null)? "" : headerObjRequest.getMsgFormat().trim();
			sRequestorChannelId=(headerObjRequest.getRequestorChannelId()==null)? "" : headerObjRequest.getRequestorChannelId().trim();
			sRequestorUserId=(headerObjRequest.getRequestorUserId()==null)? "" : headerObjRequest.getRequestorUserId().trim();
			sRequestorLanguage=(headerObjRequest.getRequestorLanguage()==null)? "" : headerObjRequest.getRequestorLanguage().trim();
			sRequestorSecurityInfo=(headerObjRequest.getRequestorSecurityInfo()==null)? "" : headerObjRequest.getRequestorSecurityInfo().trim();
			sExtra2=(headerObjRequest.getExtra2()==null)? "" : headerObjRequest.getExtra2().trim();
			sExtra1=(headerObjRequest.getExtra1()==null)? "" : headerObjRequest.getExtra1().trim();
			sMessageId=(headerObjRequest.getMessageId()==null)? "" : headerObjRequest.getMessageId().trim();
			
			attributesObj=req.getAttributes();
			attributeObj=attributesObj.getAttribute();
			WriteLog("attributeObj.length"+attributeObj.length);
			//Fetch Name Value Attributes in Hash Map
			for(int i=0;i<attributeObj.length;i++)
			{
				String attrValue=attributeObj[i].getValue().trim();
				attrValue=(attrValue==null)? "" : attrValue.replace("&amp;","&");
				attrValue=(attrValue==null)? "" : attrValue.replace("&AMP;","&");
				hm.put(attributeObj[i].getName(), attrValue);
			}
						
			WriteLog("sProcessName: "+sProcessName+", sSubProcess: "+sSubProcess);
		}
		catch(Exception e)
		{
			WriteLog("Exception: "+e.getMessage());
			throw new WICreateException("1007",pCodes.getString("1007")+" : "+e.getMessage());
		}
	}
	
	private void validateRequestParameters(String attributeList[]) throws WICreateException,Exception
	{
		WriteLog("Inside validateRequestParameters");
		if(sProcessName.equalsIgnoreCase("?") || sProcessName.equalsIgnoreCase(""))
			throw new WICreateException("1001",pCodes.getString("1001"));
		
		if(("?".equalsIgnoreCase(sSubProcess) || "".equalsIgnoreCase(sSubProcess)))
			throw new WICreateException("1002",pCodes.getString("1002"));
		
		if(sRequestorChannelId.equalsIgnoreCase("?") || sRequestorChannelId.equalsIgnoreCase(""))
			throw new WICreateException("1011",pCodes.getString("1011"));
		
		//Fetch ProcessDefID
		getProcessDefID();	
		
		// Checking update is possible for workitem or not		
		getWorkitemStage(attributeList);
		
		if (ActivityName.equalsIgnoreCase("")) // Activityname blank means, workitem doesn't exists in flow
		{
			throw new WICreateException("5052",pCodes.getString("5052")+": "+WINumber);
		}	
				
		getPossibleUpdateQueues();
		if (ActivityName.contains(PossibleQueues))
		{
			WriteLog("Update request is possible at this queue: "+ActivityName);
		}
		else 
		{
			WriteLog("Update request is not possible at this queue: "+ActivityName);	
			throw new WICreateException("5050",pCodes.getString("5050"));
		}
		
		
		//Check if all Mandatory attributes present in USR_0_WSR_UPDATE_ATTRDETAILS have come
		checkMandatoryAttribute();
		
		/*if ("Digital_PL".equalsIgnoreCase(sProcessName)){
			String Attributes = commonMethod.getTagValues(InputMessage, "Attributes");
			String AttributesTmp [] = commonMethod.getTagValues(Attributes, "Attribute").split("`");
			for(int i=0;i<AttributesTmp.length;i++)
			{
				checkAttributeTable(commonMethod.getTagValues(attributeList[i],"Name"),commonMethod.
						getTagValues(attributeList[i],"Value"));
			}
		}*/
		
		
		getTableName();
		
		// validate fields and prepare update column and value data
		checkExtTableAttrIBPS(hm);
		
		
		
		// validating date field format in received Request
		//validateInputValuesFunction(attributeList);
		
	}
	
	private void getProcessDefID() throws WICreateException, Exception
	{
		sInputXML=commonMethod.getAPSelectWithColumnNamesXML("select a.PROCESSDEFID, b.PROCESSID from processdeftable a with(nolock),"+WSR_PROCESS+" b with(nolock) where a.processname='"+sProcessName+"' and b.SUBPROCESSNAME='"+sSubProcess+"' and a.processname=b.processname and b.isactive='Y'");
		WriteLog("APSelectWithColumnNames Input: "+sInputXML);
		sOutputXML=commonMethod.executeAPI(sInputXML);
		WriteLog("APSelectWithColumnNames Output: "+sOutputXML);
    	xmlobj=new XMLParser(sOutputXML);
    	//Check Main Code
		checkCallsMainCode(xmlobj); 
		processDefID=commonMethod.getTagValues(sOutputXML, "PROCESSDEFID");
		WriteLog("processDefID: "+processDefID);
		processID=commonMethod.getTagValues(sOutputXML, "PROCESSID");
		WriteLog("processID: "+processID);
		if(processID.equalsIgnoreCase(""))
			throw new WICreateException("1019",pCodes.getString("1019")+":"+sProcessName+"/"+sSubProcess);
		
	}
	
	protected void checkCallsMainCode(XMLParser obj) throws WICreateException {
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
	
	private void getWorkitemStage(String attributeList[]) throws WICreateException, Exception
	{
		//from tables here
		String attrName="";
	    String attrValue="";
	    String strOperationName="";
		
	    if(hm.containsKey("WorkitemNumber"))
	    	WINumber = hm.get("WorkitemNumber");
	    else if(hm.containsKey("WINUMBER"))
	    	WINumber = hm.get("WINUMBER");
	    
		String getTableNameQry="select ACTIVITYNAME  from QUEUEVIEW with (nolock) where PROCESSINSTANCEID='"+WINumber+"' and var_str18 ='FTS_Hold'";
		sInputXML=commonMethod.getAPSelectWithColumnNamesXML(getTableNameQry);
		WriteLog("Input XML to Get Activity Name: "+sInputXML);
		sOutputXML=commonMethod.executeAPI(sInputXML);
		WriteLog("Output XML to Get Activity Name: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		ActivityName=commonMethod.getTagValues(sOutputXML, "ACTIVITYNAME");
		WriteLog("ActivityName from QueueView: "+ActivityName);
		response.setWorkitemNumber(WINumber);
	}
	
	private void getPossibleUpdateQueues() throws WICreateException, Exception
	{
		//from tables here
		String getTableNameQry="select QUEUENAME from "+WSR_POSSIBLEQUEUES+" with (nolock) where ProcessName='"+sProcessName+"' and SubProcessName = '"+sSubProcess+"' ";
		sInputXML=commonMethod.getAPSelectWithColumnNamesXML(getTableNameQry);
		WriteLog("Input XML to Get Possible Update Queues: "+sInputXML);
		sOutputXML=commonMethod.executeAPI(sInputXML);
		WriteLog("Output XML to Get Possible Update Queues: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		PossibleQueues=commonMethod.getTagValues(sOutputXML, "QUEUENAME");
		WriteLog("PossibleQueues from Master: "+PossibleQueues);
	}
	
	private String getNotifiedEventStatus() throws WICreateException,Exception
	{
		String getTableNameQry="select NotifyDEHAction from NG_DPL_EXTTABLE with(nolock) where WINAME='"+WINumber+"'";
		sInputXML=commonMethod.getAPSelectWithColumnNamesXML(getTableNameQry);
		WriteLog("Input XML to Get DBO NotifyDEHAction Status: "+sInputXML);
		sOutputXML=commonMethod.executeAPI(sInputXML);
		WriteLog("Output XML to Get DBO NotifyDEHAction Status: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String NotifyDEHAction =commonMethod.getTagValues(sOutputXML, "NotifyDEHAction");
		WriteLog("DBO NotifyDEHAction Status from External Table: "+NotifyDEHAction);
		return NotifyDEHAction;
	
	}
	
	private void checkMandatoryAttribute() throws WICreateException, Exception
	{
		WriteLog("inside checkMandatoryAttribute");
		
		sInputXML=commonMethod.getAPSelectWithColumnNamesXML("select ATTRIBUTENAME from "+WSR_ATTRDETAILS+" where PROCESSID='"+processID+"' and ISMANDATORY='Y' and ISACTIVE='Y'");
		WriteLog("Input XML: "+sInputXML);
		sOutputXML=commonMethod.executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String attributeList []=commonMethod.getTagValues(sOutputXML,"ATTRIBUTENAME").split("`");
		if(attributeList.length>0)
		{
			for(int i=0;i<attributeList.length;i++)
			{
				String flag="N";
				//Iterate through the Hash Map
				Set<?> set = hm.entrySet();
			    // Get an iterator
			    Iterator<?> j = set.iterator();
			    // Display elements
			    while(j.hasNext())
			    {
			        Map.Entry me = (Map.Entry)j.next();
			        if(me.getKey().toString().equalsIgnoreCase(attributeList[i]))
			        	flag="Y";
			   
			    }
			    
			    if(flag.equalsIgnoreCase("N"))
					throw new WICreateException("1020",pCodes.getString("1020")+": "+attributeList[i]);
			}
		}
		else
		{
			throw new WICreateException("1021",pCodes.getString("1021")+" for process id: "+processID);
		}
			
	}
	
	private void checkAttributeTable(String attributeName, String attributeValue) throws WICreateException, Exception
	{		WriteLog("inside checkAttributeTable_DCC");
		String getExtTransQry="";
		String IsIncludedInUpdate = "Y";
		if(sProcessName.equalsIgnoreCase("Digital_CC")) // for iBPS Processes
		{
			getExtTransQry="select ATTRIBUTENAME,EXTERNALTABLECOLNAME, TRANSACTIONTABLECOLNAME, ISMANDATORY,ATTRIBUTE_FORMAT,ATTRIBUTE_TYPE from "+WSR_ATTRDETAILS+" with(nolock) where ATTRIBUTENAME='"+attributeName+"' and PROCESSID='"+processID+"'";
		}
		
		sInputXML=commonMethod.getAPSelectWithColumnNamesXML(getExtTransQry);
		WriteLog("Input XML: "+sInputXML);
		sOutputXML=commonMethod.executeAPI(sInputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String attNameFromTable=commonMethod.getTagValues(sOutputXML, "ATTRIBUTENAME");
		String extFlag=commonMethod.getTagValues(sOutputXML, "EXTERNALTABLECOLNAME");
		String transFlag=commonMethod.getTagValues(sOutputXML, "TRANSACTIONTABLECOLNAME");
		String isMandatory=commonMethod.getTagValues(sOutputXML, "ISMANDATORY");
		String ExtCol="";
		String ExtVal="";
		
		if(processID.equalsIgnoreCase("DPL_UPDATE_CIF_CARD_DETAILS")
			&& attributeName.equalsIgnoreCase("Updated_CardType_Desc")){
			if(isMandatory.contains("`")){
				String[] mand_arr = isMandatory.split("`");
				isMandatory= mand_arr[0];
			}
			if(transFlag.contains("`")){
				transFlag="";
			}
		}
		
		WriteLog("EXTERNALTABLECOLNAME: "+extFlag+ "TRANSACTIONTABLECOLNAME: "+transFlag+" PROCESSID: "+processID+" ISMANDATORY: "+isMandatory+" attNameFromTable:"+attNameFromTable);
		
		if(!attNameFromTable.equalsIgnoreCase(""))
		{
			WriteLog("Attribute Name: "+attributeName);
			if(((isMandatory.equalsIgnoreCase("Y") || isMandatory.equalsIgnoreCase("N") || isMandatory.equalsIgnoreCase("C")) && !attributeValue.trim().equalsIgnoreCase("")) 
					|| ((isMandatory.equalsIgnoreCase("N") || isMandatory.equalsIgnoreCase("C")) && attributeValue.trim().equalsIgnoreCase("")))
			{
				if (!extFlag.equalsIgnoreCase("") && transFlag.equalsIgnoreCase(""))
				{
					if (attributeValue.contains("'"))
						attributeValue = attributeValue.replace("'", "");
					
					if (attributeValue.contains(","))
						attributeValue = attributeValue.replace(",", " ");
					
					if("DPL_FIRCO_ADD_DOCS".equalsIgnoreCase(processID))
					{	
						if(attNameFromTable.equalsIgnoreCase("FircoAdditionalRemarks"))
						{	sRemarksAddInfo = attributeValue;
							WriteLog("sRemarksAddInfo 11 DCC Firco---"+sRemarksAddInfo);
							IsIncludedInUpdate="N";
						}
						if(attNameFromTable.equalsIgnoreCase("FircoUpdateAction"))
						{	FircoUpdateActionDPL = attributeValue;
							WriteLog("FircoUpdateAction 11 DCC Firco---"+FircoUpdateActionDPL);
						}
						if(attNameFromTable.equalsIgnoreCase("WINUMBER"))
						{	
							IsIncludedInUpdate="N";
						}
					}
					
					if(	processID.equalsIgnoreCase("DPL_FIRCO_ADD_DOCS") || processID.equalsIgnoreCase("DPL_FIRCO_NO_ACTION") || 
						processID.equalsIgnoreCase("DPL_FTS_DETAILS") || processID.equalsIgnoreCase("DPL_NO_BANK_STMT_ANALYSIS") || 
						processID.equalsIgnoreCase("DPL_BS_NO_ACTION") || processID.equalsIgnoreCase("DPL_END_COOLING") || 
						processID.equalsIgnoreCase("DPL_CARD_DEL_DOC_REC") || processID.equalsIgnoreCase("DPL_SALARY_NO_ACTION")||
						processID.equalsIgnoreCase("DPL_UPDATE_CIF_CARD_DETAILS") || processID.equalsIgnoreCase("DPL_CARD_DETAILS_NO_ACTN"))
					{
						if(attNameFromTable.equalsIgnoreCase("WINUMBER") || attNameFromTable.equalsIgnoreCase("Prospect_id"))
						{
							IsIncludedInUpdate="N";
						}
					}
					
					if (processID.equalsIgnoreCase("DPL_FIRCO_ADD_DOCS") && attNameFromTable.equalsIgnoreCase("Document_Name_List")) {
						IsIncludedInUpdate = "N";
					}
					
					if (processID.equalsIgnoreCase("DPL_FIRCO_NO_ACTION")) {
						IsIncludedInUpdate = "N";
					}
					
					if (processID.equalsIgnoreCase("DPL_SALARY_NO_ACTION")) {
						IsIncludedInUpdate = "N";
					}
					
					if (processID.equalsIgnoreCase("DPL_BS_NO_ACTION")) {
						if (attNameFromTable.equalsIgnoreCase("Initaited_Date") || attNameFromTable.equalsIgnoreCase("Expiry_Date"))
							IsIncludedInUpdate = "N";
					}
					if(processID.equalsIgnoreCase("DPL_UPDATE_CIF_CARD_DETAILS") && attributeName.equalsIgnoreCase("Updated_CardType_Desc")){
						String[] ExtCol_arr=extFlag.split("`");

						for (int i = 0; i < ExtCol_arr.length; i++){
							if(ExtCol.equals("")){
								ExtCol=ExtCol_arr[i];
								ExtVal="'"+attributeValue+"'"+",'";
							}
							else{
								ExtCol+=","+ExtCol_arr[i];
								ExtVal+=attributeValue+"'"+",";
							}
							WriteLog("element"+ExtCol);
						}
						extFlag=ExtCol;
	
					}
				
					if(IsIncludedInUpdate.equalsIgnoreCase("Y"))
					{
						attributeTag=attributeTag+extFlag+(char)21+attributeValue.trim()+(char)25;
						extColNames=extColNames+extFlag+",";
						if(processID.equalsIgnoreCase("DCC_UPDATE_CIF_CARD_DETAILS") && attributeName.equalsIgnoreCase("Updated_CardType_Desc")){ // Hritik 09.10.23
							extColValues=extColValues+ExtVal;
						}else
							extColValues=extColValues+"'"+attributeValue.trim()+"'"+",";
					}
				}
				else
					 throw new WICreateException("1015",pCodes.getString("1015")+" :"+attributeName);
			}
			else if(isMandatory.equalsIgnoreCase("Y") && attributeValue.equalsIgnoreCase(""))
			{
				 throw new WICreateException("1016",pCodes.getString("1016")+" :"+attributeName);
			}
		}
		else
		{	
			WriteLog("No Value Mapped for name");
			throw new WICreateException("1017",pCodes.getString("1017")+" :"+attributeName);
		}
		
	}
	
	private void checkExtTableAttrIBPS(HashMap<String, String> hmt) throws WICreateException, Exception
	{
		String txnTableAttributes="\n<Q_"+txnTableName+">";
		//WriteLog("inside checkAttributeTable");
		String attributeNames = "";
		for (String name: hmt.keySet()){
			if(attributeNames.equalsIgnoreCase(""))
				attributeNames = "'"+name.toString()+"'";
			else 
				attributeNames = attributeNames+",'"+name.toString()+"'";
		}
		String getExtTransQry= "";
		
		getExtTransQry="select ATTRIBUTENAME, isnull(nullif(EXTERNALTABLECOLNAME,''),'#') as EXTERNALTABLECOLNAME,isnull(nullif(TRANSACTIONTABLECOLNAME,''),'#') as TRANSACTIONTABLECOLNAME, isnull(nullif(ATTRIBUTE_FORMAT,''),'#') as ATTRIBUTE_FORMAT, isnull(nullif(ATTRIBUTE_TYPE,''),'#') as ATTRIBUTE_TYPE, ISMANDATORY from "+WSR_ATTRDETAILS+" with(nolock) where ATTRIBUTENAME in ("+attributeNames+") and PROCESSID='"+processID+"' and ISACTIVE='Y' ";
		
		sInputXML=commonMethod.getAPSelectWithColumnNamesXML(getExtTransQry);
		WriteLog("Input XML: "+sInputXML);
		sOutputXML=commonMethod.executeAPI(sInputXML);
		xmlobj=new XMLParser(sOutputXML);
		WriteLog("Output XML: "+sOutputXML);
		checkCallsMainCode(xmlobj);
		String attNameFromTable=commonMethod.getTagValues(sOutputXML, "ATTRIBUTENAME");
		String extFlag=commonMethod.getTagValues(sOutputXML, "EXTERNALTABLECOLNAME");
		String isMandatory=commonMethod.getTagValues(sOutputXML, "ISMANDATORY");
		String txnColumnName=commonMethod.getTagValues(sOutputXML, "TRANSACTIONTABLECOLNAME");
		String attributesFormat=commonMethod.getTagValues(sOutputXML, "ATTRIBUTE_FORMAT");
		String attributesType=commonMethod.getTagValues(sOutputXML, "ATTRIBUTE_TYPE");
		//WriteLog("txnColumnName1086:-"+txnColumnName);
		//WriteLog("exttablecolumn1086:-"+extFlag);
		String AttrTagsName []=attNameFromTable.split("`");
		String ExtColsName []=extFlag.split("`");
		String MandateList []=isMandatory.split("`");
		String txnColsName []=txnColumnName.split("`");
		String attributeFormat []=attributesFormat.split("`");
		String attributeType []=attributesType.split("`");
		//WriteLog("AttrTagsName.length:-"+AttrTagsName.length);
		//WriteLog("MandateList.length:-"+MandateList.length);
		//WriteLog("ExtColsName.length:-"+ExtColsName.length);
		//WriteLog("txnColsName.length:-"+txnColsName.length);
		//WriteLog("attributeFormat.length:-"+attributeFormat.length);
		//WriteLog("attributeType.length:-"+attributeType.length);
		// validation to check received attributes tags in request are configured in table
		for (String name: hmt.keySet()){
			String attrtag = name.toString().trim();
			String flg = "N";
			//WriteLog("Attr:"+attNameFromTable);
			for(int i=0;i<AttrTagsName.length;i++)
			{	//WriteLog("Attr111:"+AttrTagsName[i]);
				if(AttrTagsName[i].trim().equalsIgnoreCase(attrtag))
				{	
					flg = "Y";
					break;
				}
			}
			if(flg.equalsIgnoreCase("N"))
			{
				WriteLog("No Value Mapped for attribute:-"+attrtag);
				throw new WICreateException("1017",pCodes.getString("1017")+" :"+attrtag);
			}
		}
		//*******************************************
		attributeTag=attributeTag+"\n<WIUpdateRecSubProc>"+sSubProcess+"</WIUpdateRecSubProc>";
		for(int i=0;i<AttrTagsName.length;i++)
		{
			//WriteLog("AttrTagsName[i]:"+AttrTagsName[i]);	
			//WriteLog("ExtColsName[i]:"+ExtColsName[i]);	
			//WriteLog("MandateList[i]:"+MandateList[i]);	
			String attributeValue = hmt.get(AttrTagsName[i]).trim();
			WriteLog("attribute name:-"+AttrTagsName[i]);
			WriteLog("attribute value before decode:-"+attributeValue);
			
			if(!"".equalsIgnoreCase(attributeValue) && !"".equalsIgnoreCase(attributeFormat[i]) && !"".equalsIgnoreCase(attributeType[i]) && !"#".equalsIgnoreCase(attributeFormat[i]) && !"#".equalsIgnoreCase(attributeType[i]))
			{
				commonMethod.checkAttributeFormatAndLength(AttrTagsName[i],attributeValue,attributeFormat[i],attributeType[i]);
			}
			//WriteLog(i+" attributeValue:-"+attributeValue+"\nattributeTag"+attributeTag);
			
			attributeValue=commonMethod.replaceXChars(attributeValue);//added to handle special characters in request
			
			if(((MandateList[i].trim().equalsIgnoreCase("Y") || MandateList[i].trim().equalsIgnoreCase("N")) 
					&& !attributeValue.equalsIgnoreCase("")) || (MandateList[i].trim().equalsIgnoreCase("N") && attributeValue.equalsIgnoreCase("")))
			{
				//WriteLog("extn table column name:-"+ExtColsName[i]);
				//WriteLog("txn table column name:-"+txnColsName[i]);
				
				if(!"DPL_COURIER_UPDATE".equalsIgnoreCase(processID))
				{
					if (!ExtColsName[i].trim().equalsIgnoreCase("") && !ExtColsName[i].trim().equalsIgnoreCase("#"))
					{
						WriteLog("this is a extn table attribute:-"+AttrTagsName[i]);
						attributeTag=attributeTag+"\n<"+ExtColsName[i]+">"+attributeValue+"</"+ExtColsName[i]+">";
						//WriteLog("attributeTag:-"+attributeTag);
						if("RiskScore".equalsIgnoreCase(AttrTagsName[i]) && !"".equalsIgnoreCase(attributeValue))
						{
							double riskScore = Double.parseDouble(attributeValue);
							String isHighRisk = "N";
							if(riskScore >= 4.05)
								isHighRisk = "Y";
							attributeTag=attributeTag+"\n<IsHighRisk>"+isHighRisk+"</IsHighRisk>";
						}
					}
					else if(!txnColsName[i].trim().equalsIgnoreCase("") && !txnColsName[i].trim().equalsIgnoreCase("#"))
					{
						WriteLog("this is a txn table attribute:-"+AttrTagsName[i]);
						txnTableAttributes=txnTableAttributes+"\n<"+txnColsName[i]+">"+attributeValue+"</"+txnColsName[i]+">";
						//WriteLog("txnTableAttributes:-"+txnTableAttributes);
					}
					else
					{
						WriteLog("some error occured for attribute:-"+AttrTagsName[i]);
						throw new WICreateException("1015",pCodes.getString("1015")+" :"+AttrTagsName[i]);
					}
				}
				else if("DPL_COURIER_UPDATE".equalsIgnoreCase(processID))
				{
					if(!"WI_NAME".equalsIgnoreCase(txnColsName[i].trim()) && !"AWB_Number".equalsIgnoreCase(txnColsName[i].trim()))
					{
						//commented by rubi
						/*if("".equalsIgnoreCase(awbColNames))
							awbColNames=txnColsName[i].trim();
						else
							awbColNames=awbColNames+","+txnColsName[i].trim();
						
						if("".equalsIgnoreCase(awbColValues))
							awbColValues="'"+attributeValue.trim()+"'";
						else
							awbColValues=awbColValues+","+"'"+attributeValue.trim()+"'";*/
						
					}
					if("AWB_Number".equalsIgnoreCase(txnColsName[i].trim()))
					{
						//awbNumber = attributeValue.trim();
					}
					if("Delivery_Status_Code".equalsIgnoreCase(txnColsName[i].trim()))
					{
						//awbDeliveryStatusCode = attributeValue.trim();
					}
					if("Delivery_Status".equalsIgnoreCase(txnColsName[i].trim()))
					{
						//awbDeliveryStatusDesc = attributeValue.trim();
					}
					
				}
					 
			}
			else if(MandateList[i].trim().equalsIgnoreCase("Y") && attributeValue.equalsIgnoreCase(""))
			{
				WriteLog("some error occured for attribute2:-"+AttrTagsName[i]);
				 throw new WICreateException("1016",pCodes.getString("1016")+" :"+AttrTagsName[i]);
			}
		}
		
		//WriteLog("Final txn table attributeTag:-"+txnTableAttributes);
		if(!("\n<Q_"+txnTableName+">").equalsIgnoreCase(txnTableAttributes))
			txnTableAttributes=txnTableAttributes+"\n</Q_"+txnTableName+">";
		else
			txnTableAttributes = "";
		
		attributeTag=attributeTag+txnTableAttributes;
		WriteLog("Final attributeTag:-"+attributeTag);
		
				
	}
	
	private void getTableName() throws WICreateException, Exception
	{
		//from tables here
		String getTableNameQry="select EXTERNALTABLE ,HISTORYTABLE  from "+WSR_PROCESS+" where PROCESSID='"+processID+"'";
		sInputXML=commonMethod.getAPSelectWithColumnNamesXML(getTableNameQry);
		WriteLog("Input XML: "+sInputXML);
		sOutputXML=commonMethod.executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		HISTORYTABLE=commonMethod.getTagValues(sOutputXML, "EXTERNALTABLE");
		WriteLog("External table Name: "+HISTORYTABLE);
		txnTableName=commonMethod.getTagValues(sOutputXML, "HISTORYTABLE");
		WriteLog("Transaction table Name: "+txnTableName);
	}
	
	private void validateInputValuesFunction(String attributeList[]) throws WICreateException
	{
		if (sProcessName.equalsIgnoreCase("RMT"))
		{
			String attrName="";
		    String attrValue="";
		    String find="";
			for(int i=0;i<attributeList.length;i++)
			{
				attrName=commonMethod.getTagValues(attributeList[i],"Name");
				attrValue=commonMethod.getTagValues(attributeList[i],"Value");
				find=commonMethod.validateInputValues(attrName,attrValue);
				if(find.equalsIgnoreCase("false"))
				{
					break;
				}
			}
			//System.out.println("outside "+find+" attrName "+attrName);
			if(find.equalsIgnoreCase("false"))
			{
				
				if(attrName.equalsIgnoreCase("PASSPORTEXPIRYDATE")||
				attrName.equalsIgnoreCase("VISAISSUEDATE")||
				attrName.equalsIgnoreCase("VISAEXPIRYDATE"))
				{
					throw new WICreateException("1005", "Invalid date format for field:" +attrName+" Format should be YYYY-MM-DD format");
				}
			}
		}
	}

	private void validateRepetitiveRequestParameters() throws WICreateException, Exception
	{
		String repetitiveListMain[];
		String repetitiveList[];
		
		RepetitiveMainTags = checkMandatoryRepetitiveTags();
		if(!"".equalsIgnoreCase(RepetitiveMainTags))
		{
			String attributeList []=RepetitiveMainTags.split("`");
			if(attributeList.length>0)
			{
				for(int i=0;i<attributeList.length;i++)
				{
					if(InputMessage.contains(attributeList[i])) 
					{
						repetitiveListMain=commonMethod.getTagValues(InputMessage, attributeList[i]).split("`");
			        	for(int j=0;j<repetitiveListMain.length;j++)
			        	{	
			        		HashMap<String, String> hm1 = new HashMap(); 
			            	repetitiveList=commonMethod.getTagValues(repetitiveListMain[j], "Attribute").split("`");
			        		for(int k=0;k<repetitiveList.length;k++)
			            	{
			        			String attrName=commonMethod.getTagValues(repetitiveList[k],"Name");
				        		String attrValue=commonMethod.getTagValues(repetitiveList[k],"Value");
				    			
				        		hm1.put(attrName, attrValue);
			        		}
			        		if(hm1.size() != 0)
			        		{
			        			String RepetitiveProcessID = hmRptProcessId.get(attributeList[i]);
			        			checkMandatoryRepetitiveAttribute(RepetitiveProcessID,"Y",hm1);
				        		hmMain.put(attributeList[i]+"-"+Integer.toString(j), hm1);
				        	}
			        	}
					}
				}
			}
		}
			
	}
	
	private String checkMandatoryRepetitiveTags() throws WICreateException, Exception
	{
		WriteLog("inside checkMandatoryRepetitiveTags");
		String sInputXML=commonMethod.getAPSelectWithColumnNamesXML("select REPETITIVETAGNAME,PROCESSID,TRANSACTIONTABLE,ISMANDATORY from USR_0_WSR_UPDATE_PROCESS_REPETITIVE with(nolock) where ProcessName='"+sProcessName+"' and SUBPROCESSNAME='"+sSubProcess+"' and ISACTIVE='Y'");
		WriteLog("Input XML: "+sInputXML);
		String sOutputXML=commonMethod.executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String RepetitiveMainTags = commonMethod.getTagValues(sOutputXML,"REPETITIVETAGNAME");
		String RepProcessId[] = commonMethod.getTagValues(sOutputXML,"PROCESSID").split("`");
		String RepTransTable[] = commonMethod.getTagValues(sOutputXML,"TRANSACTIONTABLE").split("`");
		String isMandatory = commonMethod.getTagValues(sOutputXML,"ISMANDATORY");
		String RepetitiveList []=RepetitiveMainTags.split("`");
		String MandateList []=isMandatory.split("`");
		if(MandateList.length>0)
		{
			for(int i=0;i<MandateList.length;i++)
			{
				String flag="Y";
				
				if("Y".equalsIgnoreCase(MandateList[i]))
				{
				    if(InputMessage.contains(RepetitiveList[i]))
				    {
				    	// nothing to do
				    }	
				    else 
				    	flag = "N";
				}
				else //Conditional Validations for repetitive tags in DBO WI update added by om.tiwari
				{
					if("FIRCOHITDetails".equalsIgnoreCase(RepetitiveList[i]))
					{
						String fircoHitAggregates = commonMethod.getTagValues(InputMessage, "FIRCOHITDetails");
						
						String fircoAggregates[] = fircoHitAggregates.split("`");
						boolean emptyAggregateFlag=false;
						for(int k=0;(k<fircoAggregates.length && !"".equalsIgnoreCase(fircoAggregates[k].trim()));k++)
						{
							String allFircoAttributes= commonMethod.getTagValues(fircoAggregates[k], "Attribute");
							String fircoAttributes[] = allFircoAttributes.split("`");
							if(fircoAttributes.length>0 && !"".equalsIgnoreCase(allFircoAttributes.trim()))
							{
								emptyAggregateFlag=true;
								break;
							}
						}
						if("INFO_UPDATE".equalsIgnoreCase(sSubProcess) && hm.containsKey("IsFIRCOHit") && "Y".equalsIgnoreCase(hm.get("IsFIRCOHit")))
						{
							if("".equalsIgnoreCase(fircoHitAggregates))
							{
									throw new WICreateException("12006",pCodes.getString("12006")+": "+RepetitiveList[i]);
							}
							else if(fircoAggregates.length>0)
							{
								if(!emptyAggregateFlag)
									throw new WICreateException("12006",pCodes.getString("12006")+": "+RepetitiveList[i]);
							}
						}
						else if("INFO_UPDATE".equalsIgnoreCase(sSubProcess) && hm.containsKey("IsFIRCOHit") && "N".equalsIgnoreCase(hm.get("IsFIRCOHit")))
						{
							if(emptyAggregateFlag)
							{
								throw new WICreateException("12007",pCodes.getString("12007")+": "+RepetitiveList[i]);
							}
						}
					}
					
				}
			    if(flag.equalsIgnoreCase("N"))
					throw new WICreateException("1020",pCodes.getString("1020")+": "+RepetitiveList[i]);
			
			    hmRptProcessId.put(RepetitiveList[i], RepProcessId[i]);
			    hmRptTransTable.put(RepProcessId[i], RepTransTable[i]);
			}
		}
		return RepetitiveMainTags;
	}
	
	private String checkMandatoryRepetitiveAttribute(String RepetitiveProcessId,String isValidatingMandate, HashMap<String, String> RepetitiveReqAttr) throws WICreateException, Exception
	{
		WriteLog("inside checkMandatoryRepetitiveAttributeIBPS");
		String AttrTagName = "";
		String TransColName = "";
		String AttrFormat = "";
		String AttrType = "";
		String isMandatory = "";
		if(!hmRptAttrAndCol.containsKey(RepetitiveProcessId))
		{
			String sInputXML=commonMethod.getAPSelectWithColumnNamesXML("select ATTRIBUTENAME,TRANSACTIONTABLECOLNAME,isnull(nullif(ATTRIBUTE_FORMAT,''),'#') as ATTRIBUTE_FORMAT, isnull(nullif(ATTRIBUTE_TYPE,''),'#') as ATTRIBUTE_TYPE,ISMANDATORY from USR_0_WSR_UPDATE_ATTRDETAILS_REPETITIVE with(nolock) where PROCESSID='"+RepetitiveProcessId+"' and ISACTIVE='Y' order by AttributeName");
			WriteLog("Input XML: "+sInputXML);
			String sOutputXML=commonMethod.executeAPI(sInputXML);
			WriteLog("Output XML: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			AttrTagName = commonMethod.getTagValues(sOutputXML,"ATTRIBUTENAME");
			TransColName = commonMethod.getTagValues(sOutputXML,"TRANSACTIONTABLECOLNAME");
			AttrFormat = commonMethod.getTagValues(sOutputXML,"ATTRIBUTE_FORMAT");
			AttrType = commonMethod.getTagValues(sOutputXML,"ATTRIBUTE_TYPE");
			isMandatory = commonMethod.getTagValues(sOutputXML,"ISMANDATORY");
		} 
		else
		{
			HashMap<String, String> hmAttr = new HashMap();
			hmAttr = hmRptAttrAndCol.get(RepetitiveProcessId);
			for (String name: hmAttr.keySet()){
				if(AttrTagName.equalsIgnoreCase(""))
					AttrTagName = name.toString();
				else 
					AttrTagName = AttrTagName+"`"+name.toString();
				
				if(TransColName.equalsIgnoreCase(""))
					TransColName = hmAttr.get(name).toString(); 
				else
					TransColName = TransColName+"`"+hmAttr.get(name).toString();
			}
			
			HashMap<String, String> hmMand = new HashMap();
			hmMand = hmRptAttrAndMand.get(RepetitiveProcessId);
			for (String name1: hmMand.keySet()){
				if(isMandatory.equalsIgnoreCase(""))
					isMandatory = hmMand.get(name1).toString(); 
				else
					isMandatory = isMandatory+"`"+hmMand.get(name1).toString();
			}
			
			HashMap<String, String> hmFormat = new HashMap();
			hmFormat = hmRptAttrAndFormat.get(RepetitiveProcessId);
			for (String name1: hmFormat.keySet()){
				if(AttrFormat.equalsIgnoreCase(""))
					AttrFormat = hmFormat.get(name1).toString(); 
				else
					AttrFormat = AttrFormat+"`"+hmFormat.get(name1).toString();
			}
			
			HashMap<String, String> hmType = new HashMap();
			hmType = hmRptAttrAndType.get(RepetitiveProcessId);
			for (String name1: hmType.keySet()){
				if(AttrType.equalsIgnoreCase(""))
					AttrType = hmType.get(name1).toString(); 
				else
					AttrType = AttrType+"`"+hmType.get(name1).toString();
			}
			
		}
		
		if(isValidatingMandate.equalsIgnoreCase("Y"))
		{	
			String AttrTagsName []=AttrTagName.split("`");
			String TransColsName []=TransColName.split("`");
			String MandateList []=isMandatory.split("`");
			String AttrFormatList []=AttrFormat.split("`");
			String AttrTypeList []=AttrType.split("`");
			if(MandateList.length>0)
			{
				for(int i=0;i<MandateList.length;i++)
				{
					String flag="Y";
					
					if("Y".equalsIgnoreCase(MandateList[i]))
					{
						if(RepetitiveReqAttr.containsKey(AttrTagsName[i]))
						{	
						    if(RepetitiveReqAttr.get(AttrTagsName[i]).trim().equalsIgnoreCase(""))
						    	flag = "N";
						    else {
						    	// nothing to do
						    }
						}
						else
							flag = "N";	
					}
					else // to validate conditional mandatory attributes for repetitive tags , added by om.tiwari // need to review this block
					{
						if("DebitCardRefNo".equalsIgnoreCase(AttrTagsName[i]))
						{
							String isDebitCardReq=RepetitiveReqAttr.get("IsDebitCardRequired").trim();
							if(("FULFILLMENT".equalsIgnoreCase(sSubProcess) || "FE_CORRECTION".equalsIgnoreCase(sSubProcess)) 
									&& ("Y".equalsIgnoreCase(isDebitCardReq) ||"Yes".equalsIgnoreCase(isDebitCardReq)) 
									&& (!RepetitiveReqAttr.containsKey("DebitCardRefNo") || "".equalsIgnoreCase(RepetitiveReqAttr.get("DebitCardRefNo"))))
							{
								throw new WICreateException("1026",pCodes.getString("1026")+": "+"DebitCardRefNo for Related Party:"+RepetitiveReqAttr.get("RelatedPartyID"));
							}
						}
					}
				    if(flag.equalsIgnoreCase("N"))
						throw new WICreateException("1020",pCodes.getString("1020")+": "+AttrTagsName[i]+" for "+ RepetitiveProcessId);
				    
				   
				    //block written to validate field level validation
				    //try{
				    if(RepetitiveReqAttr.containsKey(AttrTagsName[i].trim()))
				    {	
					    String attrVal = RepetitiveReqAttr.get(AttrTagsName[i]).trim();
						if(!"".equalsIgnoreCase(attrVal) && !"".equalsIgnoreCase(AttrFormatList[i]) && !"".equalsIgnoreCase(AttrTypeList[i]) && !"#".equalsIgnoreCase(AttrFormatList[i]) && !"#".equalsIgnoreCase(AttrTypeList[i]))
						{
							commonMethod.checkAttributeFormatAndLength(AttrTagsName[i],attrVal,AttrFormatList[i],AttrTypeList[i]);
						}
				    }	
				   
				}
			}
			else
			{
				throw new WICreateException("1021",pCodes.getString("1021")+" for process id: "+processID);
			}
			
			if(!hmRptAttrAndCol.containsKey(RepetitiveProcessId))
			{
				if(AttrTagsName.length>0)
				{
					HashMap<String, String> hmtmp = new HashMap();
					for(int i=0;i<AttrTagsName.length;i++)
					{
						hmtmp.put(AttrTagsName[i], TransColsName[i]);
					}
					hmRptAttrAndCol.put(RepetitiveProcessId, hmtmp);
					
					HashMap<String, String> hmtmp1 = new HashMap();
					for(int i=0;i<AttrTagsName.length;i++)
					{
						hmtmp1.put(AttrTagsName[i], MandateList[i]);
					}
					hmRptAttrAndMand.put(RepetitiveProcessId, hmtmp1);
					
					HashMap<String, String> hmtmp2 = new HashMap();
					for(int i=0;i<AttrTagsName.length;i++)
					{
						hmtmp2.put(AttrTagsName[i], AttrFormatList[i]);
					}
					hmRptAttrAndFormat.put(RepetitiveProcessId, hmtmp2);
					
					HashMap<String, String> hmtmp3 = new HashMap();
					for(int i=0;i<AttrTagsName.length;i++)
					{
						hmtmp3.put(AttrTagsName[i], AttrTypeList[i]);
					}
					hmRptAttrAndType.put(RepetitiveProcessId, hmtmp3);
				}	
			}
		}
		return AttrTagName;
	}
	
	private void checkExistingSession() throws Exception, WICreateException
	{
		WriteLog("inside checkExistingSession");
		
		String getSessionQry="select randomnumber from pdbconnection where userindex in (select userindex from pdbuser where username='"+commonMethod.sUsername+"')";
		sInputXML=commonMethod.getAPSelectWithColumnNamesXML(getSessionQry);
		WriteLog("Input XML: "+sInputXML);
		sOutputXML=commonMethod.executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		//Check Main Code
		checkCallsMainCode(xmlobj);
		sSessionID=commonMethod.getTagValues(sOutputXML,"randomnumber");
		WriteLog("SessionID: "+sSessionID);
		if(sSessionID.equalsIgnoreCase(""))
		{
			sInputXML=commonMethod.getWMConnectXML();
			//WriteLog("WM Connect Input: "+sInputXML);
			sOutputXML=commonMethod.executeAPI(sInputXML);
			WriteLog("WM Connect Output: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			//Check Main Code
			checkCallsMainCode(xmlobj);
        	sSessionID=commonMethod.getTagValues(sOutputXML,"SessionId");
        	sessionFlag=true;
		}
		response.setSessionId(sSessionID);
	}
	
	private String getWorkitemID() throws WICreateException, Exception
	{
		//String WorkitemId="";
		//String strActivity_name="";
		try{
			
			String getWorkitemIDQry="select workitemid,ActivityName  from QUEUEVIEW with (nolock) where PROCESSINSTANCEID='"+WINumber+"' and ActivityName = '"+ActivityName+"'";
			sInputXML=commonMethod.getAPSelectWithColumnNamesXML(getWorkitemIDQry);
			WriteLog("Input XML: "+sInputXML);
			sOutputXML=commonMethod.executeAPI(sInputXML);
			WriteLog("Output XML: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			workitemID=commonMethod.getTagValues(sOutputXML, "workitemid");
			ActivityName = commonMethod.getTagValues(sOutputXML, "ActivityName");
			WriteLog("WorkitemId: "+workitemID);
			WriteLog("ActivityName: "+ActivityName);
		}
		catch(Exception e){
			WriteLog("Exception occured while retriving workitem ID: "+e.getMessage());
			workitemID="1";
		}
		//from tables here
		
		return workitemID;
	}
	
	private void insertIntoHistory(String remarks, String decisionInHistory) throws WICreateException, Exception {
		Date d= new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		sDate = dateFormat.format(d);
		trTableColumn = "WI_NAME,Workstep,Decision,Decision_Date_Time,Remarks,UserName,ENTRY_DATE_TIME";
		
		WriteLog("trTableColumn" + trTableColumn);
		
		trTableValue = "'"+ WINumber + "','"+PossibleQueues+"','"+decisionInHistory+"','" + sDate + "','"+remarks+"','System','" +sDate+"'";
		WriteLog("trTableValue" + trTableValue);
		sInputXML = commonMethod.getAPInsertInputXML("NG_DPL_GR_DECISION_HISTORY", trTableColumn, trTableValue, sSessionID);
				
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = commonMethod.executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
	}
	
	private void doneworkitem() throws WICreateException {
		try {
			// Lock Workitem.
			String getWorkItemInputXML = commonMethod.getWorkItemInput(WINumber,workitemID,sSessionID);
			String getWorkItemOutputXml = commonMethod.executeAPI(getWorkItemInputXML);
			WriteLog("Input XML For WmgetWorkItemCall: " + getWorkItemInputXML);
			WriteLog("Output XML For WmgetWorkItemCall: " + getWorkItemOutputXml);

			XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
			String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
			WriteLog("WmgetWorkItemCall Maincode:  " + getWorkItemMainCode);

			if (getWorkItemMainCode.trim().equals("0")) {
				WriteLog("WMgetWorkItemCall Successful: " + getWorkItemMainCode);

				// Move Workitem to next Workstep
				String completeWorkItemInputXML = commonMethod.completeWorkItemInput(WINumber,workitemID,sSessionID);
				WriteLog("Input XML for wmcompleteWorkItem: " + completeWorkItemInputXML);

				String completeWorkItemOutputXML = commonMethod.executeAPI(completeWorkItemInputXML);
				WriteLog("Output XML for wmcompleteWorkItem: " + completeWorkItemOutputXML);

				XMLParser xmlParserCompleteWorkitem = new XMLParser(completeWorkItemOutputXML);
				String completeWorkitemMaincode = xmlParserCompleteWorkitem.getValueOf("MainCode");
				WriteLog("Status of wmcompleteWorkItem  " + completeWorkitemMaincode);

				if (completeWorkitemMaincode.trim().equalsIgnoreCase("0")) {
					WriteLog("WmCompleteWorkItem successful: " + completeWorkitemMaincode);
					System.out.println(WINumber + "Complete Sussesfully with status ");
					WriteLog("WorkItem moved to next Workstep.");

				} else {
					completeWorkitemMaincode = "";
					WriteLog("WMCompleteWorkItem failed: " + completeWorkitemMaincode);
				}

			} else {
				getWorkItemMainCode = "";
				WriteLog("WmgetWorkItem failed: " + getWorkItemMainCode);
			}

		} catch (Exception e) {
			WriteLog("Exception: " + e.getMessage());
			throw new WICreateException("1007", pCodes.getString("1007") + " : " + e.getMessage());
		}
	}

	private void returnResponse() throws WICreateException,Exception
	{
		this.response = commonMethod.setSuccessParamResponse(sExtra1,sExtra2,sMessageId,sMsgFormat,sMsgVersion,sRequestorChannelId,sRequestorLanguage,sRequestorSecurityInfo,sRequestorUserId);
		response.setWorkitemNumber(WINumber);
		
	}

	private void insertIntoExtTable(String tableName,String colName,String colValues,String sSessionID,String whereClause) throws WICreateException {
		sInputXML=commonMethod.getAPUpdateInputXML(tableName,colName,colValues,sSessionID,whereClause);
		WriteLog("The input XML getDBUpdateDocsReqdFlagDCC "+sInputXML);
		sOutputXML = commonMethod.executeAPI(sInputXML);
		WriteLog("APInsert Output for transaction update table getDBUpdateDocsReqdFlagD: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
	}
}