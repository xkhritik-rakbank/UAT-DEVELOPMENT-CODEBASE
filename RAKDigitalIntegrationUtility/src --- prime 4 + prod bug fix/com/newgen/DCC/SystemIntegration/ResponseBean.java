package com.newgen.DCC.SystemIntegration;


public class ResponseBean {
	
	private String ErrorCode="";
	private String ErrorDescription="";
	private String WorkitemNumber="";
	private String WorkStep="";
	//private EE_EAI_HEADER EE_EAI_HEADER;
	private String WorkitemStatusDetails="";
	private String WorkItemMainCode="";
	private String DedupeReturnCode="";
	private String FircosoftReturnCode="";
	private String BlackListReturnCode="";
	private String RiskScoreReturnCode="";
	private String IntegrationDecision="";
	private String CifNumber="";
	private String IntCallFailed="";
	private String IntFailedCode="";
	private String IntFailedReason="";
	private String RiskScore_Details="";
	private String Fircosoft_Details="";
	private String RISK_SCORE_STATUSFROMUTIL="";
	private String MsgID="";
	private String FircoHit="";
	
	public String getFircoHit() {
		return FircoHit;
	}
	public void setFircoHit(String fircoHit) {
		FircoHit = fircoHit;
	}
	public String getMsgID() {
		return MsgID;
	}
	public void setMsgID(String msgID) {
		MsgID = msgID;
	}
	public String getRISK_SCORE_STATUSFROMUTIL() {
		return RISK_SCORE_STATUSFROMUTIL;
	}
	public void setRISK_SCORE_STATUSFROMUTIL(String rISK_SCORE_STATUSFROMUTIL) {
		RISK_SCORE_STATUSFROMUTIL = rISK_SCORE_STATUSFROMUTIL;
	}
	public String getFircosoft_Details() {
		return Fircosoft_Details;
	}
	public void setFircosoft_Details(String fircosoft_Details) {
		Fircosoft_Details = fircosoft_Details;
	}
	public String getRiskScore_Details() {
		return RiskScore_Details;
	}
	public void setRiskScore_Details(String riskScore_Details) {
		RiskScore_Details = riskScore_Details;
	}
	
	public String getIntFailedReason() {
		return IntFailedReason;
	}
	public void setIntFailedReason(String intFailedReason) {
		IntFailedReason = intFailedReason;
	}
	public String getIntFailedCode() {
		return IntFailedCode;
	}
	public void setIntFailedCode(String intFailedCode) {
		IntFailedCode = intFailedCode;
	}
	public String getIntCallFailed() {
		return IntCallFailed;
	}
	public void setIntCallFailed(String intCallFailed) {
		IntCallFailed = intCallFailed;
	}
	public String getCifNumber() {
		return CifNumber;
	}
	public void setCifNumber(String cifNumber) {
		CifNumber = cifNumber;
	}
	public String getIntegrationDecision() {
		return IntegrationDecision;
	}
	public void setIntegrationDecision(String integrationDecision) {
		IntegrationDecision = integrationDecision;
	}
	public String getDedupeReturnCode() {
		return DedupeReturnCode;
	}
	public void setDedupeReturnCode(String dedupeReturnCode) {
		DedupeReturnCode = dedupeReturnCode;
	}
	public String getFircosoftReturnCode() {
		return FircosoftReturnCode;
	}
	public void setFircosoftReturnCode(String fircosoftReturnCode) {
		FircosoftReturnCode = fircosoftReturnCode;
	}
	public String getBlackListReturnCode() {
		return BlackListReturnCode;
	}
	public void setBlackListReturnCode(String blackListReturnCode) {
		BlackListReturnCode = blackListReturnCode;
	}
	public String getRiskScoreReturnCode() {
		return RiskScoreReturnCode;
	}
	public void setRiskScoreReturnCode(String riskScoreReturnCode) {
		RiskScoreReturnCode = riskScoreReturnCode;
	}
		
	public String getWorkItemMainCode() {
		return WorkItemMainCode;
	}
	public void setWorkItemMainCode(String workItemMainCode) {
		WorkItemMainCode = workItemMainCode;
	}
	public String getWorkStep() {
		return WorkStep;
	}
	public void setWorkStep(String workStep) {
		WorkStep = workStep;
	}
	
	public String getErrorCode() {
		return ErrorCode;
	}
	public void setErrorCode(String errorCode) {
		ErrorCode = errorCode;
	}
	public String getErrorDescription() {
		return ErrorDescription;
	}
	public void setErrorDescription(String errorDescription) {
		ErrorDescription = errorDescription;
	}
	public String getWorkitemNumber() {
		return WorkitemNumber;
	}
	public void setWorkitemNumber(String workitemNumber) {
		WorkitemNumber = workitemNumber;
	}
	public String getWorkitemStatusDetails() {
		return WorkitemStatusDetails;
	}
	public void setWorkitemStatusDetails(String workitemStatusDetails) {
		WorkitemStatusDetails = workitemStatusDetails;
	}
	

}
