package com.newgen.PL;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import com.newgen.custom.CreateWorkitem;
import com.newgen.ws.EE_EAI_HEADER;
import com.newgen.ws.exception.WICreateException;
import com.newgen.ws.request.Attribute;
import com.newgen.ws.request.Attributes;
import com.newgen.ws.request.Document;
import com.newgen.ws.request.Documents;
import com.newgen.ws.response.WICreateResponse;

import adminclient.OSASecurity;

public class PL_WICommonMethod extends CreateWorkitem{

	WICreateResponse response = new WICreateResponse();	
	EE_EAI_HEADER headerObjResponse = new EE_EAI_HEADER();;
	
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
	private Attributes attributesObj;
	private Attribute attributeObj[];
	private Documents documentsObj;
	private Document documentObj[];
	private String sDocumentName;
	private String sDocumentType;
	private String sDocBase64;
	private String sCabinetName = "";
	private String sJtsIp = "";
	private int iJtsPort;
	protected String sUsername = "";
	private String sPassword = "";
	private String sTempLoc = "";
	private int iVolId;
	private ResourceBundle pCodes;
	
	
	//For DecryptPassword
	private String decryptPassword(String pass)
	{
		int len = pass.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(pass.charAt(i), 16) << 4)
					+ Character.digit(pass.charAt(i+1), 16));
		}
		String password=OSASecurity.decode(data,"UTF-8");
		return password;
	}

	//For Load Configuration File  --rubi
	public WICreateResponse loadConfiguration() throws IOException, Exception
	{
		//load file configuration
		WriteLog("Loading Configuration file 123");
		try
		{
			
			Properties p = new Properties();
			String sConfigFile=new StringBuilder().append(System.getProperty("user.dir"))
					.append(System.getProperty("file.separator")).append("BPMCustomWebservicesConf")
					.append(System.getProperty("file.separator")).append("config.properties").toString();
			
			WriteLog("config file path is "+sConfigFile);
			
			p.load(new FileInputStream(sConfigFile));
			WriteLog("After p load");
			
		    sCabinetName=p.getProperty("CabinetName");
		    sJtsIp=p.getProperty("JtsIp");
		    iJtsPort=Integer.parseInt(p.getProperty("JtsPort"));
		    sUsername=p.getProperty("username");
		    sPassword=decryptPassword(p.getProperty("password"));	
			sTempLoc=p.getProperty("TempDocumentLoc");
		    iVolId=Integer.parseInt(p.getProperty("volid"));
		    
		    WriteLog("CabinetName: "+sCabinetName+", JtsIp: "+sJtsIp+", JtsPort: "+iJtsPort+" ,Username: "+sUsername+", Password: "+p.getProperty("password")+" ,VolumeID: "+iVolId);
		    WriteLog("Configuration file loaded successfuly");
		    
		    response.setCabinetName(sCabinetName);
		    response.setJtsIp(sJtsIp);
		    response.setJtsPort(iJtsPort);
		    response.setUsername(sUsername);
		    
		    return response;
		}
		catch(Exception e)
		{
			WriteLog("Inside exception of log decryption");
			throw new WICreateException("3001",pCodes.getString("3001"));
		}  
	}
	
	
	void loadResourceBundle()
	{
		WriteLog("inside loadResourceBundle");
		pCodes= PropertyResourceBundle.getBundle("com.newgen.ws.config.StatusCodes");
		if(pCodes==null)
			WriteLog("Error in loading status codes");
		else
			WriteLog("Status Codes loaded successfully");

	}

	protected EE_EAI_HEADER setFailureParamResponse(String sExtra1,String sExtra2,String sMessageId,String sMsgFormat,String sMsgVersion,
			String sRequestorChannelId,String sRequestorLanguage,String sRequestorSecurityInfo,String sRequestorUserId)
	{
		headerObjResponse.setExtra1(sExtra1);
		headerObjResponse.setExtra2(sExtra2);
		headerObjResponse.setMessageId(sMessageId);
		headerObjResponse.setMsgFormat(sMsgFormat);
		headerObjResponse.setMsgVersion(sMsgVersion);
		headerObjResponse.setRequestorChannelId(sRequestorChannelId);
		headerObjResponse.setRequestorLanguage(sRequestorLanguage);
		headerObjResponse.setRequestorSecurityInfo(sRequestorSecurityInfo);
		headerObjResponse.setRequestorUserId(sRequestorUserId);
		headerObjResponse.setReturnCode("-1");
		headerObjResponse.setReturnDesc("Failure");
		
		response.setEE_EAI_HEADER(headerObjResponse);
		
		return headerObjResponse;
	}
	
	protected String getAPInsertInputXML(String tableName,String colName,String colValues,String sSessionID) {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + tableName
				+ "</TableName>" + "<ColName>" + colName + "</ColName>"
				+ "<Values>" + colValues + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	protected String getAPUpdateInputXML(String tableName,String colName,String colValues,String sSessionID,String whereClause)
	{
		return "<?xml version=\"1.0\"?>" + "<APUpdate_Input>"
		+ "<Option>APUpdate</Option>" + "<TableName>"+tableName+
		"</TableName>" + "<ColName>" + colName + "</ColName>"
		+ "<Values>" + colValues + "</Values>" + "<WhereClause>"+whereClause+"</WhereClause>"
		+ "<EngineName>"
		+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
		+ "</SessionId>" + "</APUpdate_Input>";
	}
	
	public  String getWorkItemInput(String WINumber,String sWorkitemid,String sSessionID)
	{
		return "<?xml version=\"1.0\"?>" + "<WMGetWorkItem_Input>"
				+ "<Option>WMGetWorkItem</Option>" + "<ProcessInstanceId>" + WINumber
				+ "</ProcessInstanceId>" + "<WorkItemId>" + sWorkitemid + "</WorkItemId>"
				+ "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</WMGetWorkItem_Input>";
	}
	protected String executeAPI(String sInputXML) throws WICreateException
	{
		String sOutputXML="";
		try
		{
			 Socket sock = null;
			 sock = new Socket(sJtsIp, iJtsPort);
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
			throw new WICreateException("1006",pCodes.getString("1006")+" : "+e.getMessage());
		}
		return sOutputXML;
		
	}
	
	protected String getAPSelectWithColumnNamesXML(String sQuery)
	{
		
		return 	 "<?xml version='1.0'?>" +
				 "<APSelectWithColumnNames_Input>" +
				 "<Option>APSelectWithColumnNames</Option>" +
				 "<Query>" + sQuery + "</Query>" +
				 "<EngineName>"+sCabinetName+"</EngineName>" +
				 "</APSelectWithColumnNames_Input>";
		
	}
	protected static String getTagValues (String sXML, String sTagName) 
	{  
			String sTagValues = "";
			String sStartTag = "<" + sTagName + ">";
			String sEndTag = "</" + sTagName + ">";
			String tempXML = sXML;
			
	    try	{
		  
			for(int i=0;i<sXML.split(sEndTag).length;i++) 
			{
				if(tempXML.indexOf(sStartTag) != -1) 
				{
					sTagValues += tempXML.substring(tempXML.indexOf(sStartTag) + sStartTag.length(), tempXML.indexOf(sEndTag));
					//System.//out.println("sTagValues"+sTagValues);
					tempXML=tempXML.substring(tempXML.indexOf(sEndTag) + sEndTag.length(), tempXML.length());
		        }
				if(tempXML.indexOf(sStartTag) != -1) 
				{    
					sTagValues +="`";
					
				}
				//System.//out.println("sTagValues"+sTagValues);
			}
			//System.//out.println(" Final sTagValues"+sTagValues);
		}

		catch(Exception e) 
		{   
		}
			return sTagValues;
	}

	protected String replaceXChars(String value)
    {
           // handling of special characters
           if(!"".equalsIgnoreCase(value) && !"null".equalsIgnoreCase(value)  && value != null)
           {
                  if(value.contains("&amp;"))
                        value = value.replace("&amp;", "AAMPRRSNDD");
                  if(value.contains("&lt;"))
                        value = value.replace("&lt;", "LLSSTNSPX");
                  if(value.contains("&gt;"))
                        value = value.replace("&gt;", "GGRTTNSPX");
                  if(value.contains("&"))
                        value = value.replace("&", "&amp;");
                  
                  if(value.contains("AAMPRRSNDD"))
                        value = value.replace("AAMPRRSNDD", "&amp;");
                  if(value.contains("LLSSTNSPX"))
                        value = value.replace("LLSSTNSPX", "&lt;");
                  if(value.contains("GGRTTNSPX"))
                        value = value.replace("GGRTTNSPX", "&gt;");
                  
                  if(value.contains("<"))
                        value = value.replace("<", "&lt;");
                  if(value.contains(">"))
                        value = value.replace(">", "&gt;");
           }
           return value;
    }

	protected String getWMConnectXML()
	{
		
		return "<?xml version=\"1.0\"?>"+
	    "<WMConnect_Input>"+
		"<Option>WMConnect</Option>"+
		"<EngineName>"+sCabinetName+"</EngineName>"+
		"<Participant>"+
		"<Name>"+sUsername+"</Name>"+
		"<Password>"+sPassword+"</Password>"+
		"</Participant>"+
		"</WMConnect_Input>";		
			
	}

	protected String getWFUploadWorkItemXML(String sSessionID,String processDefID,String InitiationQueueID,String attributeTag,String sInitiateAlso)
	{
				
			return  "<?xml version=\"1.0\"?>\n"+
					"<WFUploadWorkItem_Input>\n"+
					"<Option>WFUploadWorkItem</Option>\n"+
					"<EngineName>"+sCabinetName+"</EngineName>\n"+
					"<SessionId>"+sSessionID+"</SessionId>\n"+
					"<ProcessDefId>"+processDefID+"</ProcessDefId>\n"+
					"<QueueId>"+InitiationQueueID+"</QueueId>\n"+
					"<InitiateAlso>"+sInitiateAlso+"</InitiateAlso>\n"+
					"<Attributes>"+attributeTag+"</Attributes>\n"+
					"<UserDefVarFlag>Y</UserDefVarFlag>\n"+
					"</WFUploadWorkItem_Input>";
		
	}
	
	public  String completeWorkItemInput(String WINumber,String sWorkitemid, String sSessionID)
	{
		return "<?xml version=\"1.0\"?>" + "<WMCompleteWorkItem_Input>"
				+ "<Option>WMCompleteWorkItem</Option>" + "<ProcessInstanceId>" + WINumber
				+ "</ProcessInstanceId>" + "<WorkItemId>" + sWorkitemid + "</WorkItemId>"
				+ "<AuditStatus></AuditStatus>"
				+ "<Comments></Comments>"
				+ "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</WMCompleteWorkItem_Input>";
	}
	protected WICreateResponse setSuccessParamResponse(String sExtra1,String sExtra2,String sMessageId,String sMsgFormat,String sMsgVersion,
			String sRequestorChannelId,String sRequestorLanguage,String sRequestorSecurityInfo,String sRequestorUserId)
	{
		try{
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
		
		return response;
		}catch(Exception e){
			WriteLog(e.getMessage());
		}
		return response;
	}
	
	protected String validateInputValues(String attrName,String attrValues)
	{
		String found="true";
		
		if((attrName.equalsIgnoreCase("PASSPORTEXPIRYDATE") && !(attrValues.trim().length()==0))
		||(attrName.equalsIgnoreCase("VISAISSUEDATE") && !(attrValues.trim().length()==0))
		||(attrName.equalsIgnoreCase("VISAEXPIRYDATE") && !(attrValues.trim().length()==0)))
		{
			return validateDateFormat(attrValues);
		}
		
		return found;
	}
	
	//This function validate the date format in YYYY-MM-DD format
	private static String validateDateFormat(String input) 
	{
		Date date = null;
		try 
		{
		    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		    date = sdf.parse(input);
		    if (!input.equals(sdf.format(date))) 
		    {
		        date = null;
		    }
		} 
		catch (ParseException ex) 
		{
	     return "false";
		}
		if (date == null) 
		{
			return "false";
		} 
		else 
		{
			return "true";
		}	
	}

	protected void checkAttributeFormatAndLength(String attributeName, String attributeValue, String attrFormat, String attType) throws WICreateException, Exception
	{
		WriteLog("inside checkAttributeFormatAndLength attributeName-"+attributeName+", attType-"+attType+", attrFormat-"+attrFormat+", attributeValue-"+attributeValue);
		
		if(!(attributeValue.equalsIgnoreCase("")))
		{
			int attributelength = attributeValue.length();
			if(!attType.equalsIgnoreCase(""))
			{
				if(attType.equalsIgnoreCase("DATE"))
				{
					Date date = null;
					SimpleDateFormat sdf = new SimpleDateFormat(attrFormat);
				    date = sdf.parse(attributeValue);
				    if (!attributeValue.equals(sdf.format(date))) 
				    {
				    	throw new WICreateException("1116",pCodes.getString("1116")+" :"+attributeName);
				    }
				}
				else if(attType.equalsIgnoreCase("AMOUNT"))
				{
					try
					{
						if(!attributeValue.contains("."))
							attributeValue=attributeValue+".00";
						Double.parseDouble(attributeValue);
						
					}
					catch(Exception ex)
					{
						throw new WICreateException("1117",pCodes.getString("1117")+" :"+attributeName);
					}
					
				}
				else
				{
					//WriteLog("11 inside checkAttributeFormatAndLength attributeName-"+attributeName+", attType-"+attType+", attrFormat-"+attrFormat+", attributeValue-"+attributeValue);	
					int attributesize = Integer.parseInt(attrFormat);
					if(attributelength>attributesize)
					{
						throw new WICreateException("1111",pCodes.getString("1111")+" :"+attributeName);
					}
					//WriteLog("22 inside checkAttributeFormatAndLength attributeName-"+attributeName+", attType-"+attType+", attrFormat-"+attrFormat+", attributeValue-"+attributeValue);
					String patternMatch = "";
										
					if(attType.equalsIgnoreCase("NUMERIC"))
					{	
						patternMatch="[0-9]+";
						if(!Pattern.matches(patternMatch, attributeValue))
						{
							//WriteLog("inside 995");
							throw new WICreateException("1112",pCodes.getString("1112")+" :"+attributeName);
						}
					}
					
					if(attType.equalsIgnoreCase("ALPHANUMERIC"))
					{
						patternMatch="^[a-zA-Z0-9 ]*$";
						if(!Pattern.matches(patternMatch, attributeValue))
						{
							throw new WICreateException("1113",pCodes.getString("1113")+" :"+attributeName);
						}
					}
					
					if(attType.equalsIgnoreCase("ALPHAONLY"))
					{
						patternMatch="^[a-zA-Z ]*$";
						if(!Pattern.matches(patternMatch, attributeValue))
						{
							throw new WICreateException("1114",pCodes.getString("1114")+" :"+attributeName);
						}
					}
					
					
					if(attType.equalsIgnoreCase("APLPHANUMERICWITHSPACE"))
					{
						patternMatch="^[a-zA-Z0-9 ]*$";
						if(!Pattern.matches(patternMatch, attributeValue))
						{
							throw new WICreateException("1115",pCodes.getString("1115")+" :"+attributeName);
						}
					}
					
					if(attType.equalsIgnoreCase("APLPHANUMERICWITHSPECIALCHAR"))
					{
						patternMatch="^[a-zA-Z0-9-#_!.@()+/%&\\s|~\\[\\]$^*={};:\",<>?\\n\\r\\t\\\\ ]*$";
						if(!Pattern.matches(patternMatch, attributeValue))
						{
							throw new WICreateException("1118",pCodes.getString("1118")+" :"+attributeName);
							
						}
					}
				}
			}
		}
		
	}
	
}
