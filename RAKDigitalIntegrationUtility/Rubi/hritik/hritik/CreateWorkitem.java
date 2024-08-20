/**-------------------------------------------------------------------------
				NEWGEN SOFTWARE TECHNOLOGIES LIMITED
		Group						: AP2
		Product / Project			: BPM Convergence WebService
		Module						: Message Driven Bean
		File Name					: CreateWorkitem.java
		Author						: Aishwarya Gupta
		Date written (DD/MM/YYYY)	: 10/02/2016
		Description					: Message Driven Bean to read the input from
									  listener and respond back based on replyToQueue
-------------------------------------------------------------------------
					CHANGE HISTORY
-------------------------------------------------------------------------
	Date			 Change By	 	Change Description (Bug No. (If Any))
 (01/05/2014)		Aishwarya Gupta Initial Draft
 ---------------------------------------------------------------------------*/

package com.newgen.custom;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
//import javax.enterprise.context.spi.Context;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.ibm.jms.JMSBytesMessage;
import com.newgen.ws.EE_EAI_HEADER;
import com.newgen.ws.WICreateService;
import com.newgen.ws.WIStatusService;
import com.newgen.ws.WIUpdateService;
import com.newgen.ws.request.Attribute;
import com.newgen.ws.request.Attributes;
import com.newgen.ws.request.Document;
import com.newgen.ws.request.Documents;
import com.newgen.ws.request.WICreateRequest;
import com.newgen.ws.response.WICreateResponse;
import com.newgen.DBO.DBO_WICreateService;
import com.newgen.DBO.DBO_WIUpdateService;
import com.newgen.NBTL.NBTL_WICreateService;
import com.newgen.NBTL.NBTL_WIStatusService;
import com.newgen.NBTL.NBTL_WIUpdateService;
import com.newgen.PL.PL_WICreationService;
import com.newgen.PL.Pl_WIUpdateService;
import com.newgen.DBO.DBO_WIStatusService;
import com.newgen.CPMS.CPMS_WICreateService;
import com.newgen.CPMS.CPMS_WIUpdateService;
import com.newgen.CPMS.CPMS_WIStatusService;

/**
 * Message-Driven Bean implementation class for: CreateWorkitem
 */
@MessageDriven(
		activationConfig = { @ActivationConfigProperty(
				propertyName = "destinationType", propertyValue = "javax.jms.Queue")
		})
@TransactionManagement(TransactionManagementType.BEAN)
public class CreateWorkitem implements MessageListener 
{

	private MessageDrivenContext mdc;
	//Context jndiContext;
	
	//Web service parameters
	WICreateService mainObj;
    WICreateRequest request;
    EE_EAI_HEADER headerObj;
    Attribute attributeObj[];
    Attributes attributesObj;
    Documents documentsObj;
    Document documentObj[];
    WICreateResponse response;
    
    WIUpdateService mainObjUpdate;
    
    WIStatusService mainObjStatus;
    
    String requestXML="";
    String outputMessage="";
    String attributeList[];
    String attrName="";
    String attrValue="";
    String documentList[];
    String docName="";
    String docType="";
    String base64="";
    String messageId = null;
	String MsgFormatName = "";
	String MsgIDInReq = "";
	
	DBO_WICreateService mainCreateObjDBO;
	DBO_WIUpdateService mainUpdateObjDBO;
	DBO_WIStatusService mainStatusObjDBO;
	CPMS_WICreateService mainCreateObjCPMS;
	CPMS_WIUpdateService mainUpdateObjCPMS;
	CPMS_WIStatusService mainStatusObjCPMS;
	NBTL_WICreateService mainCreateObjNBTL;
	NBTL_WIUpdateService mainUpdateObjNBTL;
	NBTL_WIStatusService mainStatusObjNBTL;
	PL_WICreationService mainCreateObjPL;
	Pl_WIUpdateService mainUpdateObjDPL;
	/**
     * Default constructor. 
     */
    public CreateWorkitem() {
        // TODO Auto-generated constructor stub
    }
	
	/**Function will be called as message is received in listener
     * @see MessageListener#onMessage(Message)
     */
    public void onMessage(Message inMessage) {
        // TODO Auto-generated method stub
    	TextMessage msgA = null;
    	JMSBytesMessage msgB = null;
    	Destination dest = null;
 		//String messageId = null;
 		//String MsgFormatName = "";
 		
 		createLogFile();
 		logger.info("Start of On Message Function.");
    	
 		SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.mmm");
		String requestedDateTime="";
 		
 		try {
    		//checking the type of message received
            if (inMessage instanceof TextMessage) // for TextMessage
            {
                msgA = (TextMessage) inMessage;
                WriteLog("MESSAGE BEAN: TextMessage received: " +  msgA.getText());
                try{
                	dest = msgA.getJMSReplyTo(); //getting reply to Queue
                	WriteLog("Obtained JMS destination = " +dest.toString());
                }catch(JMSException e)
                {
                	WriteLog("Not able to get reply to Queue: " + e.getMessage());
                	StringWriter errors = new StringWriter();
        			e.printStackTrace(new PrintWriter(errors));
        			WriteLog(errors.toString());
                }
                messageId = msgA.getJMSMessageID(); // getting messageId
                
                requestXML=msgA.getText();
                MsgFormatName= getTagValues(requestXML,"MsgFormat");
                MsgIDInReq = getTagValues(requestXML,"MessageId");
                
                Date localDate = new Date();
        		requestedDateTime = localSimpleDateFormat.format(localDate);
                
                WriteLog("MsgFormatName: "+MsgFormatName);
                if (MsgFormatName.equalsIgnoreCase("UPDATE_SR"))
                	 callWIUpdateService(requestXML);   
                else if (MsgFormatName.equalsIgnoreCase("STATUS_SR"))
                	callWIStatusService(requestXML);   
            	else	
                	 callWICreateService(requestXML); 
                
            } 
            else if (inMessage instanceof JMSBytesMessage) //for JMSBytesMessage 
            {
                msgB = (JMSBytesMessage) inMessage;
                int length = new Long(msgB.getBodyLength()).intValue();
                byte[] b = new byte[length];
                msgB.readBytes(b, length);
                String text = new String(b, "UTF-8");
                WriteLog("MESSAGE BEAN: JMSBytesMessage received: " +text);
                try{
                	dest = msgB.getJMSReplyTo(); //getting reply to Queue
                	WriteLog("Obtained JMS destination = " +dest.toString());
                }catch(JMSException e)
                {
                	WriteLog("Not able to get reply to Queue: " + e.getMessage());
                	StringWriter errors = new StringWriter();
        			e.printStackTrace(new PrintWriter(errors));
        			WriteLog(errors.toString());
                }
                messageId = msgB.getJMSMessageID(); //getting messageId
               
                
                requestXML=text;
                MsgFormatName= getTagValues(requestXML,"MsgFormat");
                MsgIDInReq = getTagValues(requestXML,"MessageId");
                
                Date localDate = new Date();
        		requestedDateTime = localSimpleDateFormat.format(localDate);
                
                WriteLog("MsgFormatName: "+MsgFormatName);
                if (MsgFormatName.equalsIgnoreCase("UPDATE_SR"))
                	callWIUpdateService(requestXML);   
                else if (MsgFormatName.equalsIgnoreCase("STATUS_SR"))
                	callWIStatusService(requestXML);   
            	else
                	callWICreateService(requestXML); 
            }
            else {
            	WriteLog("Message of wrong type: " + inMessage.getClass().getName());
            	
            }
        } catch (JMSException e) {
            //e.printStackTrace();
        	WriteLog("JMS Exception caught in on message function");
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			WriteLog(errors.toString());
            mdc.setRollbackOnly();
        } catch (Exception te) {
            //te.printStackTrace();
        	WriteLog("Exception caught in on message function");
			StringWriter errors = new StringWriter();
			te.printStackTrace(new PrintWriter(errors));
			WriteLog(errors.toString());
			
        }
    	//call to put the message in response queue
    	try {
			WriteLog("Message ID send response " + messageId);	
    		WriteLog("outputMessage send response " + outputMessage);
    		WriteLog("dest send response " + dest);
    		sendResponse(messageId,outputMessage,dest);
    		
    		//Inserting logs in process specific xml tables for iBPS processes added on 27/05/2020 by Angad
    		if("CDOB".equalsIgnoreCase(request.getProcessName())
    				|| "DAC".equalsIgnoreCase(request.getProcessName()) 
    				|| "RAOP".equalsIgnoreCase(request.getProcessName())
    				|| "CMP".equalsIgnoreCase(request.getProcessName()) 
    				|| "CBP".equalsIgnoreCase(request.getProcessName())
    				|| "iRBL".equalsIgnoreCase(request.getProcessName())
    				|| "DigitalAO".equalsIgnoreCase(request.getProcessName()) 
    				|| "Digital_CC".equalsIgnoreCase(request.getProcessName())
					|| "DBO".equalsIgnoreCase(request.getProcessName())
					|| "CPMS".equalsIgnoreCase(request.getProcessName())
					|| "NBTL".equalsIgnoreCase(request.getProcessName())
					|| "Digital_PL".equalsIgnoreCase(request.getProcessName()))
    			
    			insertReqRespINDatabase(requestedDateTime);
    		//**********************************************************
    		
    		
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
    //function added to test the code at offshore
    public String getResponseOffshore(String inMessage) {
    	logger.info("inMessage: " + inMessage);
		messageId = getTagValues(inMessage, "MessageId"); // getting messageId
		requestXML = inMessage;
		MsgFormatName = getTagValues(inMessage, "MsgFormat");
		createLogFile();
		logger.info("MsgFormatName: " + MsgFormatName);

    	Date localDate = new Date();
		SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.mmm");
		String requestedDateTime=localSimpleDateFormat.format(localDate);;
    	
        if (MsgFormatName.equalsIgnoreCase("UPDATE_SR"))
        	callWIUpdateService(inMessage);
        else if (MsgFormatName.equalsIgnoreCase("STATUS_SR"))
        	callWIStatusService(inMessage);
        else
       	 	callWICreateService(inMessage);
    	
        
      //Inserting logs in process specific xml tables for iBPS processes added on 27/05/2020 by Angad
		/*if("CDOB".equalsIgnoreCase(request.getProcessName()) || "DAC".equalsIgnoreCase(request.getProcessName()) || "RAOP".equalsIgnoreCase(request.getProcessName())
				|| "CMP".equalsIgnoreCase(request.getProcessName()) || "CBP".equalsIgnoreCase(request.getProcessName()))
			insertReqRespINDatabase(requestedDateTime);*/
		//**********************************************************
        
        return outputMessage;
        
    }
    /**
     * Function to call the Create WI webservice code
     * @param requestXML
    */
    public void callWICreateService(String inputMessage)
    {
		
		try{
    	WriteLog("Entering the callCreateWIWebservice method");
		
		//Creating object of main Service file
    	mainObj=new WICreateService();
    	WriteLog("callWI : A");
    	//Creating object of Request file
    	request=new WICreateRequest();
    	WriteLog("callWI : B");
    	
    	headerObj=new EE_EAI_HEADER();
    	//Creating request parameters
    	WriteLog("Objects created");   	
    	//Setting Header parameters
    	headerObj.setExtra1(getTagValues(inputMessage,"Extra1"));
    	headerObj.setExtra2(getTagValues(inputMessage,"Extra2"));
    	headerObj.setMessageId(getTagValues(inputMessage,"MessageId"));
    	headerObj.setMsgFormat(getTagValues(inputMessage,"MsgFormat"));
    	headerObj.setMsgVersion(getTagValues(inputMessage, "MsgVersion"));
    	headerObj.setRequestorChannelId(getTagValues(inputMessage, "RequestorChannelId"));
    	headerObj.setRequestorLanguage(getTagValues(inputMessage,"RequestorLanguage"));
    	headerObj.setRequestorSecurityInfo(getTagValues(inputMessage,"RequestorSecurityInfo"));
    	headerObj.setRequestorUserId(getTagValues(inputMessage, "RequestorUserId"));
    	headerObj.setReturnCode(getTagValues(inputMessage, "ReturnCode"));
    	
    	WriteLog("Header parameters set");
    	
    	request.setEE_EAI_HEADER(headerObj);
    	
    	//Setting Attributes 
    	request.setInputMessage(inputMessage);
    	String allAttributes = getTagValues(inputMessage, "Attributes");
    	attributesObj=new Attributes();
    	attributeList=getTagValues(allAttributes, "Attribute").split("`");
    	attributeObj=new Attribute[attributeList.length];
    	WriteLog("Attribute Object Initialized");
    	for(int i=0;i<attributeList.length;i++)
    	{
    		attributeObj[i]=new Attribute();
    		attrName=getTagValues(attributeList[i],"Name");
    		attrValue=getTagValues(attributeList[i],"Value");
			
			/*if(getTagValues(inputMessage, "ProcessName").equals("CAC"))
    		{
				String strAttrNameValue[]=validateNameValue(attrName,attrValue);
				attrName=strAttrNameValue[0];
				attrValue=strAttrNameValue[1];
    		}*/
    		attributeObj[i].setName(attrName);
    		attributeObj[i].setValue(attrValue);  		
    	
    	}
    	    	    	
    	attributesObj.setAttribute(attributeObj);
    	request.setAttributes(attributesObj);
    	WriteLog("Workitem Attributes set");
    	//Setting Document Attributes
    	documentsObj=new Documents();
    	documentList=getTagValues(inputMessage, "Document").split("`");
    	documentObj=new Document[documentList.length];
    	WriteLog("Document Object Initialized");
    	for(int i=0;i<documentList.length;i++)
    	{
    		documentObj[i]=new Document();
    		docName=getTagValues(documentList[i],"DocumentName");
    		docType=getTagValues(documentList[i], "DocumentType");
    		base64=getTagValues(documentList[i], "Base64String");
    		documentObj[i].setBase64String(base64);
    		documentObj[i].setDocumentName(docName);
    		documentObj[i].setDocumentType(docType);
    	}
    	documentsObj.setDocument(documentObj);
    	request.setDocuments(documentsObj);
    	WriteLog("Document Attributes set");
    	//Setting other request parameters
    	request.setInitiateAlso(getTagValues(inputMessage, "InitiateAlso"));
    	request.setProcessName(getTagValues(inputMessage, "ProcessName"));
    	if(inputMessage.contains("SubProcessName"))
    		request.setSubProcess(getTagValues(inputMessage, "SubProcessName"));
    	else if(inputMessage.contains("SubProcess"))
    		request.setSubProcess(getTagValues(inputMessage, "SubProcess"));
    	
    	if("DOB".equals(getTagValues(inputMessage, "ProcessName"))){
    		request.setProcessName("CDOB");
        	request.setSubProcess("CDOB");
    	}
    	
    	
    	WriteLog("Setting the mandatory params");
    	if("DBO".equalsIgnoreCase(request.getProcessName().trim()))
    	{
    		mainCreateObjDBO=new DBO_WICreateService();
    		WriteLog("attributeList--"+attributeList);
    		response=mainCreateObjDBO.wiCreate(request,attributeList);
    	}
    	else if("CPMS".equalsIgnoreCase(request.getProcessName().trim()))
    	{
    		mainCreateObjCPMS=new CPMS_WICreateService();
    		WriteLog("attributeList--"+attributeList);
    		response=mainCreateObjCPMS.wiCreate(request,attributeList);
    	}
    	else if("NBTL".equalsIgnoreCase(request.getProcessName().trim()))
    	{
    		mainCreateObjNBTL=new NBTL_WICreateService();
    		WriteLog("attributeList--"+attributeList);
    		response=mainCreateObjNBTL.wiCreate(request,attributeList);
    	}
    	else if("Digital_PL".equalsIgnoreCase(request.getProcessName().trim())){
    		mainCreateObjPL=new PL_WICreationService();
    		WriteLog("attributeList--"+attributeList);
    		response=mainCreateObjPL.wiCreate(request,attributeList);
    	}
    	else
    		response=mainObj.wiCreate(request,attributeList);
    	
    	headerObj=response.getEE_EAI_HEADER();
    	WriteLog("Header obtained");
    	String returnCode="";
    	String returnDesc="";
    	String errorCode="";
    	String errorDesc="";
    	boolean isTechnical=false;
    	if(headerObj.getReturnCode().equalsIgnoreCase("0"))
    	{
    		returnCode="0000";
    		returnDesc="Success";
    		WriteLog("returnCode: "+returnCode+" returnDesc: "+returnDesc);
    	}
    	else if(headerObj.getReturnCode().equalsIgnoreCase("-1"))
    	{
    		if(response.getErrorCode().indexOf("9")==0)
    		{
    			returnCode="2222";
    			returnDesc="Failure";
    			errorCode=response.getErrorCode().substring(1,response.getErrorCode().length());
    			errorDesc=response.getErrorDescription();
    			isTechnical=false;
    		}
    		else if(response.getErrorCode().indexOf("1")==0||response.getErrorCode().indexOf("4")==0)
    		{
    			returnCode=response.getErrorCode();
    			returnDesc="Failure:"+response.getErrorDescription();
    			isTechnical=true;
    		}
    		else if(response.getErrorCode().indexOf("2")==0)
    		{
    			returnCode="2222";
    			returnDesc="Failure";
    			errorCode=response.getErrorCode();
    			errorDesc=response.getErrorDescription();
    			isTechnical=false;
    		}  
    		else if(response.getErrorCode().equalsIgnoreCase("3333") || response.getErrorCode().equalsIgnoreCase("3334"))
    		{
    			returnCode=response.getErrorCode();
    			returnDesc=response.getErrorDescription();
    			isTechnical=false;
    		}
    		else if(response.getErrorCode().indexOf("-")==0)
    		{
    			returnCode=response.getErrorCode();
    			returnDesc="Failure:"+response.getErrorDescription();
    			isTechnical=true;
    		}  		
			else
    		{
    			returnCode=response.getErrorCode();
    			returnDesc="Failure:"+response.getErrorDescription();
    			isTechnical=true;
    		}
    		WriteLog("returnCode: "+returnCode+" returnDesc: "+returnDesc);
    	}
    	    	
    	outputMessage="<?xml version=\"1.0\"?>" +
    	"<EE_EAI_MESSAGE>"+
    	"<EE_EAI_HEADER>"+
    	"<MsgFormat>"+headerObj.getMsgFormat()+"</MsgFormat>"+
    	"<MsgVersion>"+headerObj.getMsgVersion()+"</MsgVersion>"+
    	"<RequestorChannelId>"+headerObj.getRequestorChannelId()+"</RequestorChannelId>"+
    	"<RequestorUserId>"+headerObj.getRequestorUserId()+"</RequestorUserId>"+
    	"<RequestorLanguage>"+headerObj.getRequestorLanguage()+"</RequestorLanguage>"+
    	"<RequestorSecurityInfo>"+headerObj.getRequestorSecurityInfo()+"</RequestorSecurityInfo>"+
    	"<ReturnCode>"+returnCode+"</ReturnCode>"+
    	"<ReturnDesc>"+returnDesc+"</ReturnDesc>"+
    	"<MessageId>"+headerObj.getMessageId()+"</MessageId>"+
    	"<Extra1>"+headerObj.getExtra1()+"</Extra1>"+
    	"<Extra2>"+headerObj.getExtra2()+"</Extra2>"+
    	"</EE_EAI_HEADER>";
    	if(returnCode.equalsIgnoreCase("0000") || returnCode.equalsIgnoreCase("3333") || returnCode.equalsIgnoreCase("3334") ||returnCode.equalsIgnoreCase("3335") )
    		outputMessage=outputMessage+"<CreateWorkitemRes>"+
    					   				"<WorkitemNumber>"+response.getWorkitemNumber()+"</WorkitemNumber>"+
    					   				"</CreateWorkitemRes>"+
    					   				"</EE_EAI_MESSAGE>";
    	else if(returnCode.equalsIgnoreCase("2222"))
    		outputMessage=outputMessage+"<CreateWorkitemRes>"+
    									"<Errors>"+
    										"<Error>"+
    											"<ErrorCode>"+errorCode+"</ErrorCode>"+
    											"<ErrorDescription>"+errorDesc+"</ErrorDescription>"+
    										"</Error>"+
    									"</Errors>"+
    									"</CreateWorkitemRes>"+
    									"</EE_EAI_MESSAGE>";
    	WriteLog("outputMessage: "+outputMessage);
    	//Code to append the header tag in case of technical errors
    	if(outputMessage.indexOf("</EE_EAI_MESSAGE>")<0)
    		outputMessage = outputMessage+"</EE_EAI_MESSAGE>";
    	
    	WriteLog("Exiting method callWICreateService");
		}catch(Exception e)
		{
			WriteLog("Exception in creating workitem");
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			WriteLog(errors.toString());
		}
		
	}
    
    public void callWIUpdateService(String inputMessage)
    {
		
		try{
    	WriteLog("Entering the callUpdateWIWebservice method");
		
		//Creating object of main Service file
    	mainObjUpdate=new WIUpdateService();
    	WriteLog("callWI : A");
    	//Creating object of Request file
    	request=new WICreateRequest();
    	WriteLog("callWI : B");
    	
    	headerObj=new EE_EAI_HEADER();
    	//Creating request parameters
    	WriteLog("Objects created");   	
    	//Setting Header parameters
    	headerObj.setExtra1(getTagValues(inputMessage,"Extra1"));
    	headerObj.setExtra2(getTagValues(inputMessage,"Extra2"));
    	headerObj.setMessageId(getTagValues(inputMessage,"MessageId"));
    	headerObj.setMsgFormat(getTagValues(inputMessage,"MsgFormat"));
    	headerObj.setMsgVersion(getTagValues(inputMessage, "MsgVersion"));
    	headerObj.setRequestorChannelId(getTagValues(inputMessage, "RequestorChannelId"));
    	headerObj.setRequestorLanguage(getTagValues(inputMessage,"RequestorLanguage"));
    	headerObj.setRequestorSecurityInfo(getTagValues(inputMessage,"RequestorSecurityInfo"));
    	headerObj.setRequestorUserId(getTagValues(inputMessage, "RequestorUserId"));
    	headerObj.setReturnCode(getTagValues(inputMessage, "ReturnCode"));
    	
    	WriteLog("Header parameters set");
    	
    	request.setEE_EAI_HEADER(headerObj);
    	
    	//Setting Attributes //Deepak code added to set inputMessage 
    	request.setInputMessage(inputMessage);
    	
    	String allAttributes = getTagValues(inputMessage, "Attributes");
    	
    	//Setting Attributes 
    	attributesObj=new Attributes();
    	if("DBO".equalsIgnoreCase(getTagValues(inputMessage, "ProcessName").trim()))
    		attributeList=getTagValues(allAttributes, "Attribute").split("`");
    	else
    		attributeList=getTagValues(inputMessage, "Attribute").split("`");
    	attributeObj=new Attribute[attributeList.length];
    	WriteLog("Attribute Object Initialized");
    	for(int i=0;i<attributeList.length;i++)
    	{
    		attributeObj[i]=new Attribute();
    		attrName=getTagValues(attributeList[i],"Name");
    		attrValue=getTagValues(attributeList[i],"Value");
			
			attributeObj[i].setName(attrName);
    		attributeObj[i].setValue(attrValue);  		
    		
    	}
    	
    	attributesObj.setAttribute(attributeObj);
    	request.setAttributes(attributesObj);
    	WriteLog("Workitem Attributes set");
    	//Setting other request parameters
    	request.setProcessName(getTagValues(inputMessage, "ProcessName"));
    	
    	if(inputMessage.contains("SubProcessName"))
    		request.setSubProcess(getTagValues(inputMessage, "SubProcessName"));
    	else if(inputMessage.contains("SubProcess"))
    		request.setSubProcess(getTagValues(inputMessage, "SubProcess"));
    	
    	WriteLog("Setting the mandatory params");
    	if("DBO".equalsIgnoreCase(request.getProcessName().trim()))
    	{
    		mainUpdateObjDBO=new DBO_WIUpdateService();
    		response=mainUpdateObjDBO.wiUpdate(request,attributeList);
    	}
    	else if("CPMS".equalsIgnoreCase(request.getProcessName().trim()))
    	{
    		mainUpdateObjCPMS=new CPMS_WIUpdateService();
    		response=mainUpdateObjCPMS.wiUpdate(request,attributeList);
    	}
    	else if("Digital_PL".equalsIgnoreCase(request.getProcessName().trim()))
    	{
    		mainUpdateObjDPL=new Pl_WIUpdateService();
    		response=mainUpdateObjDPL.wiUpdate(request,attributeList);
    	}
    	else
    		response=mainObjUpdate.wiUpdate(request,attributeList);
    	
    	headerObj=response.getEE_EAI_HEADER();
    	WriteLog("Header obtained");
    	String returnCode="";
    	String returnDesc="";
    	String errorCode="";
    	String errorDesc="";
    	boolean isTechnical=false;
    	if(headerObj.getReturnCode().equalsIgnoreCase("0"))
    	{
    		returnCode="0000";
    		returnDesc="Success";
    		WriteLog("returnCode: "+returnCode+" returnDesc: "+returnDesc);
    	}
    	else if(headerObj.getReturnCode().equalsIgnoreCase("-1"))
    	{
    		if(response.getErrorCode().indexOf("9")==0)
    		{
    			returnCode="2222";
    			returnDesc="Failure";
    			errorCode=response.getErrorCode().substring(1,response.getErrorCode().length());
    			errorDesc=response.getErrorDescription();
    			isTechnical=false;
    		}
    		else if(response.getErrorCode().indexOf("1")==0||response.getErrorCode().indexOf("4")==0)
    		{
    			returnCode=response.getErrorCode();
    			returnDesc="Failure:"+response.getErrorDescription();
    			isTechnical=true;
    		}
    		else if(response.getErrorCode().indexOf("2")==0)
    		{
    			returnCode="2222";
    			returnDesc="Failure";
    			errorCode=response.getErrorCode();
    			errorDesc=response.getErrorDescription();
    			isTechnical=false;
    		}    		
    		else if(response.getErrorCode().indexOf("-")==0)
    		{
    			returnCode=response.getErrorCode();
    			returnDesc="Failure:"+response.getErrorDescription();
    			isTechnical=true;
    		}  		
			else
    		{
    			returnCode=response.getErrorCode();
    			returnDesc="Failure:"+response.getErrorDescription();
    			isTechnical=true;
    		}
    		WriteLog("returnCode: "+returnCode+" returnDesc: "+returnDesc);
    	}
    	    	
    	outputMessage="<?xml version=\"1.0\"?>" +
    	"<EE_EAI_MESSAGE>"+
    	"<EE_EAI_HEADER>"+
    	"<MsgFormat>"+headerObj.getMsgFormat()+"</MsgFormat>"+
    	"<MsgVersion>"+headerObj.getMsgVersion()+"</MsgVersion>"+
    	"<RequestorChannelId>"+headerObj.getRequestorChannelId()+"</RequestorChannelId>"+
    	"<RequestorUserId>"+headerObj.getRequestorUserId()+"</RequestorUserId>"+
    	"<RequestorLanguage>"+headerObj.getRequestorLanguage()+"</RequestorLanguage>"+
    	"<RequestorSecurityInfo>"+headerObj.getRequestorSecurityInfo()+"</RequestorSecurityInfo>"+
    	"<ReturnCode>"+returnCode+"</ReturnCode>"+
    	"<ReturnDesc>"+returnDesc+"</ReturnDesc>"+
    	"<MessageId>"+headerObj.getMessageId()+"</MessageId>"+
    	"<Extra1>"+headerObj.getExtra1()+"</Extra1>"+
    	"<Extra2>"+headerObj.getExtra2()+"</Extra2>"+
    	"</EE_EAI_HEADER>";
    	if(returnCode.equalsIgnoreCase("0000"))
    		outputMessage=outputMessage+"<UpdateWorkitemRes>"+
    					   				"<WorkitemNumber>"+response.getWorkitemNumber()+"</WorkitemNumber>"+
    					   				"</UpdateWorkitemRes>"+
    					   				"</EE_EAI_MESSAGE>";
    	else if(returnCode.equalsIgnoreCase("2222"))
    		outputMessage=outputMessage+"<UpdateWorkitemRes>"+
    									"<Errors>"+
    										"<Error>"+
    											"<ErrorCode>"+errorCode+"</ErrorCode>"+
    											"<ErrorDescription>"+errorDesc+"</ErrorDescription>"+
    										"</Error>"+
    									"</Errors>"+
    									"</UpdateWorkitemRes>"+
    									"</EE_EAI_MESSAGE>";
    	WriteLog("outputMessage: "+outputMessage);
    	//Code to append the header tag in case of technical errors
    	if(outputMessage.indexOf("</EE_EAI_MESSAGE>")<0)
    		outputMessage = outputMessage+"</EE_EAI_MESSAGE>";
    	
    	WriteLog("Exiting method callWIUpdateService");
		}catch(Exception e)
		{
			WriteLog("Exception in update workitem");
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			WriteLog(errors.toString());
		}
		
	}
    
    public void callWIStatusService(String inputMessage)
    {
		
		try{
    	WriteLog("Entering the callStatusWIWebservice method");
		
		//Creating object of main Service file
    	mainObjStatus=new WIStatusService();
    	WriteLog("callWI : A");
    	//Creating object of Request file
    	request=new WICreateRequest();
    	WriteLog("callWI : B");
    	
    	headerObj=new EE_EAI_HEADER();
    	//Creating request parameters
    	WriteLog("Objects created");   	
    	//Setting Header parameters
    	headerObj.setExtra1(getTagValues(inputMessage,"Extra1"));
    	headerObj.setExtra2(getTagValues(inputMessage,"Extra2"));
    	headerObj.setMessageId(getTagValues(inputMessage,"MessageId"));
    	headerObj.setMsgFormat(getTagValues(inputMessage,"MsgFormat"));
    	headerObj.setMsgVersion(getTagValues(inputMessage, "MsgVersion"));
    	headerObj.setRequestorChannelId(getTagValues(inputMessage, "RequestorChannelId"));
    	headerObj.setRequestorLanguage(getTagValues(inputMessage,"RequestorLanguage"));
    	headerObj.setRequestorSecurityInfo(getTagValues(inputMessage,"RequestorSecurityInfo"));
    	headerObj.setRequestorUserId(getTagValues(inputMessage, "RequestorUserId"));
    	headerObj.setReturnCode(getTagValues(inputMessage, "ReturnCode"));
    	
    	WriteLog("Header parameters set");
    	
    	request.setEE_EAI_HEADER(headerObj);
    	
    	//Setting Attributes 
    	attributesObj=new Attributes();
    	attributeList=getTagValues(inputMessage, "Attribute").split("`");
    	attributeObj=new Attribute[attributeList.length];
    	WriteLog("Attribute Object Initialized");
    	for(int i=0;i<attributeList.length;i++)
    	{
    		attributeObj[i]=new Attribute();
    		attrName=getTagValues(attributeList[i],"Name");
    		attrValue=getTagValues(attributeList[i],"Value");
			
			attributeObj[i].setName(attrName);
    		attributeObj[i].setValue(attrValue);  		
    		
    	}
    	
    	attributesObj.setAttribute(attributeObj);
    	request.setAttributes(attributesObj);
    	WriteLog("Workitem Attributes set");
    	//Setting other request parameters
    	request.setProcessName(getTagValues(inputMessage, "ProcessName"));
    	request.setSubProcess(getTagValues(inputMessage, "SubProcess"));
    	
    	WriteLog("Setting the mandatory params");
    	if("DBO".equalsIgnoreCase(request.getProcessName().trim()))
    	{
    		mainStatusObjDBO=new DBO_WIStatusService();
    		response=mainStatusObjDBO.wiStatus(request,attributeList);
    	}
    	else if("CPMS".equalsIgnoreCase(request.getProcessName().trim()))
    	{
    		mainStatusObjCPMS=new CPMS_WIStatusService();
    		response=mainStatusObjCPMS.wiStatus(request,attributeList);
    	}
    	else
    		response=mainObjStatus.wiStatus(request,attributeList);
    	headerObj=response.getEE_EAI_HEADER();
    	WriteLog("Header obtained");
    	String returnCode="";
    	String returnDesc="";
    	String errorCode="";
    	String errorDesc="";
    	boolean isTechnical=false;
    	if(headerObj.getReturnCode().equalsIgnoreCase("0"))
    	{
    		returnCode="0000";
    		returnDesc="Success";
    		WriteLog("returnCode: "+returnCode+" returnDesc: "+returnDesc);
    	}
    	else if(headerObj.getReturnCode().equalsIgnoreCase("-1"))
    	{
    		if(response.getErrorCode().indexOf("9")==0)
    		{
    			returnCode="2222";
    			returnDesc="Failure";
    			errorCode=response.getErrorCode().substring(1,response.getErrorCode().length());
    			errorDesc=response.getErrorDescription();
    			isTechnical=false;
    		}
    		else if(response.getErrorCode().indexOf("1")==0||response.getErrorCode().indexOf("4")==0)
    		{
    			returnCode=response.getErrorCode();
    			returnDesc="Failure:"+response.getErrorDescription();
    			isTechnical=true;
    		}
    		else if(response.getErrorCode().indexOf("2")==0)
    		{
    			returnCode="2222";
    			returnDesc="Failure";
    			errorCode=response.getErrorCode();
    			errorDesc=response.getErrorDescription();
    			isTechnical=false;
    		}    		
    		else if(response.getErrorCode().indexOf("-")==0)
    		{
    			returnCode=response.getErrorCode();
    			returnDesc="Failure:"+response.getErrorDescription();
    			isTechnical=true;
    		}  		
			else
    		{
    			returnCode=response.getErrorCode();
    			returnDesc="Failure:"+response.getErrorDescription();
    			isTechnical=true;
    		}
    		WriteLog("returnCode: "+returnCode+" returnDesc: "+returnDesc);
    	}
    	    	
    	outputMessage="<?xml version=\"1.0\"?>" +
    	"<EE_EAI_MESSAGE>"+
    	"<EE_EAI_HEADER>"+
    	"<MsgFormat>"+headerObj.getMsgFormat()+"</MsgFormat>"+
    	"<MsgVersion>"+headerObj.getMsgVersion()+"</MsgVersion>"+
    	"<RequestorChannelId>"+headerObj.getRequestorChannelId()+"</RequestorChannelId>"+
    	"<RequestorUserId>"+headerObj.getRequestorUserId()+"</RequestorUserId>"+
    	"<RequestorLanguage>"+headerObj.getRequestorLanguage()+"</RequestorLanguage>"+
    	"<RequestorSecurityInfo>"+headerObj.getRequestorSecurityInfo()+"</RequestorSecurityInfo>"+
    	"<ReturnCode>"+returnCode+"</ReturnCode>"+
    	"<ReturnDesc>"+returnDesc+"</ReturnDesc>"+
    	"<MessageId>"+headerObj.getMessageId()+"</MessageId>"+
    	"<Extra1>"+headerObj.getExtra1()+"</Extra1>"+
    	"<Extra2>"+headerObj.getExtra2()+"</Extra2>"+
    	"</EE_EAI_HEADER>";
    	if(returnCode.equalsIgnoreCase("0000"))
    	{	
    		String statusDataRes = response.getWorkitemStatusDetails();
    		String finalStatusDataRes = "";
    		WriteLog("statusDataRes: "+statusDataRes);
    		if(!statusDataRes.equalsIgnoreCase(""))
    		{
    			//if(statusDataRes.contains("~,~"))
    			//{
    				String tempData [] = statusDataRes.split("~,~"); // record separator
    				for(int i = 0; i < tempData.length; i++)
    				{
    					String rowData [] = tempData[i].split("~"); // row data separator
    					finalStatusDataRes = finalStatusDataRes + "<Workitem>"+
					   				"<WorkitemNumber>"+rowData[0]+"</WorkitemNumber>"+
					   				"<RequestInitiationDate>"+rowData[1]+"</RequestInitiationDate>"+
			    					"<CurrentQueue>"+rowData[2]+"</CurrentQueue>"+
			    					"<StatusCode>"+rowData[3]+"</StatusCode>"+
			    					"<StatusDesc>"+rowData[4]+"</StatusDesc>"+
				   				"</Workitem>";
    				}
    			//}
    		}	
    		outputMessage=outputMessage+"<GetWorkitemStatusRes>"+
    									"<Workitems>"+
    										finalStatusDataRes+
    					   				"</Workitems>"+
    					   				"</GetWorkitemStatusRes>"+
    					   				"</EE_EAI_MESSAGE>";
    	}	
    	else if(returnCode.equalsIgnoreCase("2222"))
    		outputMessage=outputMessage+"<GetWorkitemStatusRes>"+
    									"<Errors>"+
    										"<Error>"+
    											"<ErrorCode>"+errorCode+"</ErrorCode>"+
    											"<ErrorDescription>"+errorDesc+"</ErrorDescription>"+
    										"</Error>"+
    									"</Errors>"+
    									"</GetWorkitemStatusRes>"+
    									"</EE_EAI_MESSAGE>";
    	WriteLog("outputMessage: "+outputMessage);
    	//Code to append the header tag in case of technical errors
    	if(outputMessage.indexOf("</EE_EAI_MESSAGE>")<0)
    		outputMessage = outputMessage+"</EE_EAI_MESSAGE>";
    	
    	WriteLog("Exiting method callWIStatusService");
		}catch(Exception e)
		{
			WriteLog("Exception in status workitem");
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			WriteLog(errors.toString());
		}
		
	}
    
private String[] validateNameValue(String attrName, String attrValue) {
		String strOut[]={attrName,attrValue};
    	try{
    		if("PrimaryTitle".equals(attrName) || "JointTitle".equalsIgnoreCase(attrName)){
    			if(attrValue==null || attrValue.equals(""))
    				strOut[1]="--Select--";
    			else if(attrValue.equalsIgnoreCase("Mr")||attrValue.equalsIgnoreCase("Mr."))
    				strOut[1]="Mr.";
    			else if(attrValue.equalsIgnoreCase("Mrs")||attrValue.equalsIgnoreCase("Mrs."))
    				strOut[1]="Mrs.";
    			else if(attrValue.equalsIgnoreCase("Ms")||attrValue.equalsIgnoreCase("Ms."))
    				strOut[1]="Ms.";
    			
    		} else if("PrimaryGender".equals(attrName) || "JointGender".equalsIgnoreCase(attrName)){
    			if(attrValue==null || attrValue.equals(""))
    				strOut[1]="--Select--";
    			else if(attrValue.equalsIgnoreCase("M")||attrValue.equalsIgnoreCase("Male"))
    				strOut[1]="Male";
    			else if(attrValue.equalsIgnoreCase("F")||attrValue.equalsIgnoreCase("Female"))
    				strOut[1]="Female";
    		} else if("PrimaryResCountry".equals(attrName) || "JointResCountry".equalsIgnoreCase(attrName)){
    			if(attrValue==null || attrValue.equals(""))
    				strOut[1]="--Select--";
    			else 
    				strOut[1]=attrValue.toUpperCase();
    		} else if("PrimaryProfession".equals(attrName) || "JointProfession".equalsIgnoreCase(attrName)){
    			if(attrValue==null || attrValue.equals(""))
    				strOut[1]="--Select--";
    			else if(attrValue.equalsIgnoreCase("Doctor"))
    				strOut[1]="Doctor";
    			else if(attrValue.equalsIgnoreCase("Engineer"))
    				strOut[1]="Engineer";
    			else if(attrValue.equalsIgnoreCase("Bank Manager/Officer"))
    				strOut[1]="Bank Manager/Officer";
    			else if(attrValue.equalsIgnoreCase("Chartered Accountant"))
    				strOut[1]="Chartered Accountant";
    			else if(attrValue.equalsIgnoreCase("Housewife"))
    				strOut[1]="Housewife";
    			else if(attrValue.equalsIgnoreCase("IT Specialist"))
    				strOut[1]="IT Specialist";
    			else if(attrValue.equalsIgnoreCase("Nurse"))
    				strOut[1]="Nurse";
    			else if(attrValue.equalsIgnoreCase("Officer/Military"))
    				strOut[1]="Officer/Military";    			
    			else
    				strOut[1]="Other";    			
    		} else if("PrimaryMobNoCCode".equals(attrName) || "JointMobNoCCode".equalsIgnoreCase(attrName)){
    			if(attrValue==null || attrValue.equals(""))
    				strOut[1]="";
    			else if(attrValue.equals("00971")||attrValue.equals("971")||attrValue.equals("+971"))
    				strOut[1]="00971";
    			else if(attrValue.equals("00941")||attrValue.equals("941")||attrValue.equals("+941"))
    				strOut[1]="00941";
    			
    		} 
    		/*else if("PrimaryEmpType".equals(attrName) || "JointEmpType".equalsIgnoreCase(attrName))
    		{
    			if(attrValue.equalsIgnoreCase("Salaried"))
    				strOut[1]="Salaried";
    			else if(attrValue.equalsIgnoreCase("Pensioner"))
    				strOut[1]="Pensioner";
    			else if(attrValue.equalsIgnoreCase("Self Employed")||attrValue.equalsIgnoreCase("Self-Employed"))
    				strOut[1]="Self Employed";
    			else
    				strOut[1]="Others";   
    			
    			//System.out.println(" PrimaryEmpType "+strOut[1]);
    			
    		} */
    		else if("PrimaryMonthInc".equals(attrName) || "JointMonthInc".equalsIgnoreCase(attrName)){
    			if(attrValue==null || attrValue.equals(""))
    				strOut[1]="--Select--";
    			else if(attrValue.equalsIgnoreCase("3-5 K"))
    				strOut[1]="3-5K";
    			else if(attrValue.equalsIgnoreCase("5-15 K"))
    				strOut[1]="5-15K";
    			else if(attrValue.equalsIgnoreCase("15-30 K"))
    				strOut[1]="15-30K";
    			else if(attrValue.equalsIgnoreCase("30-50 K"))
    				strOut[1]="30-50K";
    			else if(attrValue.equalsIgnoreCase("50-75 K"))
    				strOut[1]="50-75K";
    			else if(attrValue.equalsIgnoreCase("75-100 K"))
    				strOut[1]="75-100K";
    			else if(attrValue.equalsIgnoreCase(">=100 K"))
    				strOut[1]=">=100K";
    			/*else
    				strOut[1]="--Select--";*/    			
    		}else if("AddressType".equalsIgnoreCase(attrName)){
    			if(attrValue==null || attrValue.equals(""))
    				strOut[1]="";
    			else if(attrValue.equalsIgnoreCase("Residence")||attrValue.equalsIgnoreCase("residence"))
    				strOut[1]="Residence";
    			else if(attrValue.equalsIgnoreCase("Work")||attrValue.equalsIgnoreCase("work"))
    				strOut[1]="Work";
    			else if(attrValue.equalsIgnoreCase("Home Country")||attrValue.equalsIgnoreCase("home Country"))
    				strOut[1]="Home Country";   			
    		}
    		else if("JointMaritalStatus".equalsIgnoreCase(attrName)){
    			if(attrValue.equalsIgnoreCase("Single"))
    				strOut[1]="Single";
    			else if(attrValue.equalsIgnoreCase("Married"))
    				strOut[1]="Married";
    			else if(attrValue.equalsIgnoreCase("Others"))
    				strOut[1]="Others";
    			else
    				strOut[1]="Others";    			
    		}
    		else if("JointDocType".equalsIgnoreCase(attrName) || "PrimaryDocType".equalsIgnoreCase(attrName)){
    			if(attrValue==null || attrValue.equals(""))
    				strOut[1]="--Select--";
    			else if(attrValue.equalsIgnoreCase("Passport"))
    				strOut[1]="Passport";
    			else if(attrValue.equalsIgnoreCase("Khulasat QAID number"))
    				strOut[1]="Khulasat QAID Number";
    			else 
    			strOut[1]="--Select--";   			
    		}else if("USSSNJoint".equalsIgnoreCase(attrName) || "USSSNPrimary".equalsIgnoreCase(attrName)){
    			if(attrValue==null || attrValue.equals(""))
    				strOut[1]="N";
    			else 
    				strOut[1]="Y";    			   			
    		}
    		
    	}catch(Exception ex){}
    	
		return strOut;
	}
    /**
     * Function to send the response back to reply to Queue
     * @param messageId
     * @param responseXML
     * @param jmsDestination
     * @throws JMSException
     */
    public static void sendResponse(String messageId, String responseXML, Destination jmsDestination) throws JMSException {
		
		WriteLog("Entering the send response method");
		
		QueueConnection conn = null;
		QueueSession session = null;
		MessageProducer producer = null;
		
		try {
			WriteLog("JMS destination = " +jmsDestination.toString());
			
			InitialContext iniCtx = new InitialContext();
			WriteLog("JMS A:");
	        Object tmp = iniCtx.lookup("jms/BPMConvService_CF");
	        WriteLog("JMS B:");
	        QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;
	        WriteLog("JMS C:");
	        conn = qcf.createQueueConnection();
	        WriteLog("JMS D:");
	        WriteLog("Connection obained = " +jmsDestination.toString());
	        session = conn.createQueueSession(false,QueueSession.AUTO_ACKNOWLEDGE);
	        WriteLog("JMS E:");
	        conn.start();
			// Create the Producer and start the connection.
			producer = session.createProducer(null);
			WriteLog("JMS F:");
			
			TextMessage textMsg = session.createTextMessage(responseXML);
			WriteLog("Setting the text message ");
			textMsg.setJMSCorrelationID(messageId); // Set unique correlation ID and send the message.
			producer.send(jmsDestination, textMsg);
			WriteLog("The message sent to the queue is : " + textMsg);

		} catch (JMSException je){
			WriteLog(je.getErrorCode()+je.getMessage());
		} catch (NamingException e) {
			//printing the stack trace to log
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			WriteLog(errors.toString());
		} finally {
			closeConnections(session, conn);
			WriteLog("Exiting the MessageHelper::sendMessage() method");
		}
	}
	/**
	 * Function to close connections
	 * @param session
	 * @param conn
	 * @throws JMSException
	 */
	private static void closeConnections(final QueueSession session,
			final QueueConnection conn) throws JMSException {
		WriteLog("Entering the MessageHelper::closeConnections method");
		try {
			if (session != null) {
				session.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (JMSException e) {
			WriteLog(e.getErrorCode()+e.getMessage());
		}
		WriteLog("Exiting the MessageHelper::closeConnections method");
	}
    /**
     * Function to generate custom log
     * @param strMsg
     * @throws IOException
     */
	public static void WriteLog(String strMsg)
	{
		StringBuffer strFilePath = new StringBuffer(50);
		File dir= null;
		FileOutputStream fos = null;
		Writer wrt = null;
		
		DateFormat dtFormat = new SimpleDateFormat("ddMMyyyy");
		String sFName = "MQServiceBPMLog" + dtFormat.format(new java.util.Date())+".Log";	
	
		try
		{
			strFilePath.append(System.getProperty("user.dir"));
			
			dir = new File(strFilePath.toString(), "CustomLog");
			if (!dir.exists()) 
			{
				dir.mkdir();
			}
			strFilePath.append(File.separatorChar);
			strFilePath.append("CustomLog");
			strFilePath.append(File.separatorChar);
			strFilePath.append(sFName);
			java.util.Date objDate=new java.util.Date();
			fos = new FileOutputStream(strFilePath.toString(),true);
			wrt = new BufferedWriter(new OutputStreamWriter(fos));
			wrt.write("[" + objDate.toString() + "]\n" + strMsg + "\n\n" );
			wrt.flush();
			wrt.close();
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
		}	
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
	
	final static Logger logger = Logger.getLogger("CustomLog");
	public static void createLogFile()
	{
		try
		{
			DateFormat dtFormat = new SimpleDateFormat("ddMMyyyy");
			String sFName = "MQServiceBPMLog" + dtFormat.format(new java.util.Date())+".Log";
			
			Properties p = new Properties();
			String log4JPropertyFile=new StringBuilder().append(System.getProperty("user.dir"))
					.append(System.getProperty("file.separator")).append("BPMCustomWebservicesConf")
					.append(System.getProperty("file.separator")).append("log4j.properties").toString();
			
			p.load(new FileInputStream(log4JPropertyFile));
			
			String dynamicLog = System.getProperty("user.dir")+"/CustomLog/"+sFName;
			
			File fl = new File(dynamicLog);
			if(!fl.exists())
				fl.createNewFile();
			
			p.put( "log4j.appender.CustomLog.File", dynamicLog ); // overwrite "log.dir"
			
			PropertyConfigurator.configure( p );
		}
		catch(Exception e)
		{
			WriteLog("exception in createLogFile:"+e );
		}
	}

	public boolean insertReqRespINDatabase(String requestedDateTime)
	{
		try
		{
			Date localDate = new Date();
			SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.mmm");
			String actionDateTime = localSimpleDateFormat.format(localDate);
			String XMLHISTORY_TABLENAME ="";
			
			if("DigitalAO".equalsIgnoreCase(request.getProcessName())){
				XMLHISTORY_TABLENAME = "NG_DAO_XMLLOG_HISTORY";
			}
			else if("Digital_CC".equalsIgnoreCase(request.getProcessName())){
				XMLHISTORY_TABLENAME = "NG_DCC_XMLLOG_HISTORY";
			}
			else if("Digital_PL".equalsIgnoreCase(request.getProcessName())){
				XMLHISTORY_TABLENAME = "NG_DPL_XMLLOG_HISTORY";
			}
			else{
				XMLHISTORY_TABLENAME = "NG_"+request.getProcessName()+"_XMLLOG_HISTORY";
			}
			
            String params="'"+XMLHISTORY_TABLENAME+"'"
					+",'" +response.getWorkitemNumber()+"'"
					+",'" +MsgFormatName+"'"
					+",'" +MsgFormatName+"'"
					+",'" +response.getUsername()+"'"
					+",'" +MsgIDInReq+"'"
					+",'" +requestXML.replaceAll("'", "''")+"'"
					+",'" +outputMessage.replaceAll("'", "''")+"'"
					+",'" +requestedDateTime+"'"
					+",'" +actionDateTime+"'"
					;
			String inputXML = getAPProcedureInputXML(response.getCabinetName(),response.getSessionId(),"NG_XML_INSERT_PROC",params);
			//WriteLog("inputXML AP Procedure new params: "+params);
			WriteLog("inputXML AP Procedure XML Entry "+inputXML);
			String sOutputXML=executeAPI(inputXML);
			WriteLog("outputXML AP Procedure XML Entry "+sOutputXML);			

			if(sOutputXML.indexOf("<MainCode>0</MainCode>")>-1)	
			{
				WriteLog("inputXML AP Procedure Insert Successful");	
				return true;
			}	
			else
			{
				WriteLog("inputXML AP Procedure Insert Failed");
				return false;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	private String getAPProcedureInputXML(String engineName,String sSessionId,String procName,String Params)
	{
		StringBuffer bfrInputXML = new StringBuffer();
		bfrInputXML.append("<?xml version=\"1.0\"?>\n");
		bfrInputXML.append("<APProcedure_WithDBO_Input>\n");
		bfrInputXML.append("<Option>APProcedure_WithDBO</Option>\n");
		bfrInputXML.append("<ProcName>");
		bfrInputXML.append(procName);
		bfrInputXML.append("</ProcName>");
		bfrInputXML.append("<Params>");
		bfrInputXML.append(Params);
		bfrInputXML.append("</Params>");
		bfrInputXML.append("<EngineName>");
		bfrInputXML.append(engineName);
		bfrInputXML.append("</EngineName>");
		bfrInputXML.append("<SessionId>");
		bfrInputXML.append(sSessionId);
		bfrInputXML.append("</SessionId>");
		bfrInputXML.append("</APProcedure_WithDBO_Input>");		
		return bfrInputXML.toString();
	}
	private String executeAPI(String sInputXML)
	{
		String sOutputXML="";
		try
		{
			 Socket sock = null;
			 sock = new Socket(response.getJtsIp(), response.getJtsPort());
			 DataOutputStream oOut = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
			 DataInputStream oIn = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
			 byte[] SendStream = sInputXML.getBytes("8859_1");
			 int strLen = SendStream.length;
	         oOut.writeInt(strLen);
	    	 oOut.write(SendStream, 0, strLen);
			 oOut.flush();
			 int length = 0;
			 length = oIn.readInt();
			 byte[] readStream = new byte[length];
			 oIn.readFully(readStream);
			 sOutputXML= new String(readStream, "8859_1");
			 sock.close(); 
		}
		catch (Exception e)
		{
			WriteLog("Exception: "+e.getMessage());
			
		}
		return sOutputXML;
    }

}
