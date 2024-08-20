package com.newgen.DCC.Update_AssignCIF;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;

public class DCC_CIFUpdate {
	
	
	private static Logger DCC_CIFUpdateLog=null;
	DCC_CIFUpdate(Logger DCC_CIFUpdateLogName)
	{
		DCC_CIFUpdateLog=DCC_CIFUpdateLogName;
	}

	
	public	String customIntegration(String cabinetName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap)
	{
		String FinalStatus = "";
		try
		{
			
			
			String DBQuery = "SELECT CIF, FATCA_Tin_Number,Tin_reason,FinalTAI,nationality,FirstName,MiddleName,LastName,EFR_NSTP FROM NG_DCC_EXTTABLE with(nolock) WHERE WI_NAME='"+processInstanceID+"'";

			String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery,CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCC_CIFUpdateLog, false));
			DCC_CIFUpdateLog.debug("CIF Update data input223: "+ extTabDataIPXML);
			String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
			DCC_CIFUpdateLog.debug("CIF Update data output223: "+ extTabDataOPXML);

			XMLParser xmlParserData= new XMLParser(extTabDataOPXML);						
			int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));

			
			if(iTotalrec == 0)
				return "Success";
			
			if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0")&& iTotalrec>0)
			{

				String xmlDataExtTab=xmlParserData.getNextValueOf("Record");
				xmlDataExtTab =xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
				
				//XMLParser xmlParserExtTabDataRecord = new XMLParser(xmlDataExtTab);
				NGXmlList objWorkList=xmlParserData.createList("Records", "Record");

				HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();
											
				for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
				{		
					CheckGridDataMap.put("CIF_ID", objWorkList.getVal("CIF"));
					CheckGridDataMap.put("FATCA_Tin_Number", objWorkList.getVal("FATCA_Tin_Number"));
					CheckGridDataMap.put("Tin_reason", objWorkList.getVal("Tin_reason"));
					CheckGridDataMap.put("FinalTAI", objWorkList.getVal("FinalTAI"));
					CheckGridDataMap.put("nationality", objWorkList.getVal("nationality"));
					CheckGridDataMap.put("FirstName", objWorkList.getVal("FirstName"));
					CheckGridDataMap.put("MiddleName", objWorkList.getVal("MiddleName"));
					CheckGridDataMap.put("LastName", objWorkList.getVal("LastName"));
					CheckGridDataMap.put("EFR_NSTP", objWorkList.getVal("EFR_NSTP"));
					
		
					for(Map.Entry<String, String> map : CheckGridDataMap.entrySet())
					{
						DCC_CIFUpdateLog.debug("CheckGridDataMap map key: " +map.getKey()+" map value :"+map.getValue());
					}
					
					
						DCC_CIFUpdateLog.debug("WINAME : "+processInstanceID);

						String integrationStatus=CIFUpdateCall(CommonConnection.getCabinetName(),CommonConnection.getUsername(),sessionId, CommonConnection.getJTSIP(),
								CommonConnection.getJTSPort(),processInstanceID,ws_name,integrationWaitTime,socket_connection_timeout, socketDetailsMap, CheckGridDataMap);

						DCC_CIFUpdateLog.debug("CIF Update integrationStatus: " +integrationStatus);
						String statuses [] = integrationStatus.split("~");
						if(statuses[0].equalsIgnoreCase("0000"))
						{
							FinalStatus = "Success";
							return FinalStatus;
						} 
						else
						{
							FinalStatus = "Failure~ For CIF: "+CheckGridDataMap.get("CIF_ID")+"~ MsgStatus: "+statuses[1]+"~ MsgId: "+statuses[2];
							return FinalStatus;
						}	
					
				}
			
			}
			else
			{
				FinalStatus = "Failure";
			}


		}
		catch(Exception e)
		{
			return "Exception";
		}
		return FinalStatus;
	}

	
	public String CIFUpdateCall( String cabinetName,String UserName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, HashMap<String, String> CheckGridDataMap)
	{
		try
		{
			String CIF_ID = CheckGridDataMap.get("CIF_ID");
			String FATCA_Tin_Number = CheckGridDataMap.get("FATCA_Tin_Number");
			String Tin_reason = CheckGridDataMap.get("Tin_reason");
			String FinalTAI = CheckGridDataMap.get("FinalTAI");
			String nationality = CheckGridDataMap.get("nationality");
			String FirstName = CheckGridDataMap.get("FirstName");
			String MiddleName = CheckGridDataMap.get("MiddleName");
			String LastName = CheckGridDataMap.get("LastName");
			String EFR_NSTP = CheckGridDataMap.get("EFR_NSTP");
			String FullName = "";
			
			if(!("").equalsIgnoreCase(MiddleName)){
				 FullName = FirstName + " " + MiddleName + " " + LastName;
			}
			else if(("").equalsIgnoreCase(MiddleName)){
				 FullName = FirstName + " " + LastName;
			}
			
			String UsRelation = "";
			String DocCollected = "";
			
			if(nationality.equalsIgnoreCase("US")){
				UsRelation = "O";
			}else{
				UsRelation = "O";
			}
			if(UsRelation.equalsIgnoreCase("R")){
				DocCollected = "W8";
				Tin_reason = "US CITIIZEN";
			}else{
				DocCollected = "";
				FATCA_Tin_Number = "";
				Tin_reason = "";
			}
			
			
				String custDetails = "";
			    custDetails = "<CustDet>\n";
			    custDetails = custDetails + "<CustId>"+CIF_ID.trim()+"</CustId>\n";
			    custDetails = custDetails + "<RetCorpFlag>R</RetCorpFlag>\n";
			    custDetails = custDetails + "<CIFType>Retail</CIFType>\n";
			    custDetails = custDetails+"</CustDet>\n";
			    
			  //Kamran 11052023 EFR Name
				
			  //Kamran 14062023 Commenting below to remove Validatoin as per EFR NSTP Flag	
				   /* if(!"Y".equalsIgnoreCase(EFR_NSTP)){
						PersonalDetails="";
				    	DCC_CIFUpdateLog.debug("CIF Update EFR_NSTP: " +EFR_NSTP);
				    }*/
			    String PersonalDetails = "";
						PersonalDetails = PersonalDetails + "<FirstName>"+FirstName.trim()+"</FirstName>\n";
						PersonalDetails = PersonalDetails + "<MiddleName>"+MiddleName.trim()+"</MiddleName>\n";
						PersonalDetails = PersonalDetails + "<LastName>"+LastName.trim()+"</LastName>\n";
						PersonalDetails = PersonalDetails + "<FullName>"+FullName+"</FullName>\n";
					
				String FatcaDetails = "";
				// Industry Details to be passed for all cifs including main cif and all related parties
			    	FatcaDetails = FatcaDetails + "<RtlAddnlDet>\n";
			    	FatcaDetails = FatcaDetails + PersonalDetails ;
			    	FatcaDetails = FatcaDetails + "<GrossSalary>"+FinalTAI.trim()+"</GrossSalary>\n";
					FatcaDetails = FatcaDetails + "<AssessedIncome>"+FinalTAI.trim()+"</AssessedIncome>\n";
					FatcaDetails = FatcaDetails + "<FatcaDetails>\n";
					FatcaDetails = FatcaDetails + "<USRelation>"+UsRelation+"</USRelation>\n";
					FatcaDetails = FatcaDetails + "<TIN>"+FATCA_Tin_Number.trim()+"</TIN>\n";
					FatcaDetails = FatcaDetails + "<FatcaReason>"+Tin_reason.trim()+"</FatcaReason>\n";
					//FatcaDetails = FatcaDetails + "<DocumentsCollected>"+DocCollected.trim()+"</DocumentsCollected>\n";
					FatcaDetails = FatcaDetails + "</FatcaDetails>\n";
					FatcaDetails = FatcaDetails + "</RtlAddnlDet>\n";	
			    
				
				
			java.util.Date d1 = new Date();
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm");
			String DateExtra2 = sdf1.format(d1)+"+04:00";
			StringBuilder sInputXML = new StringBuilder("<EE_EAI_MESSAGE>\n" +
					"<EE_EAI_HEADER>\n" +
					"<MsgFormat>CUSTOMER_UPDATE_REQ</MsgFormat>\n" +
					"<MsgVersion>001</MsgVersion>\n" +
					"<RequestorChannelId>CAS</RequestorChannelId>\n" +
					"<RequestorUserId>RAKUSER</RequestorUserId>\n" +
					"<RequestorLanguage>E</RequestorLanguage>\n" +
					"<RequestorSecurityInfo>secure</RequestorSecurityInfo>\n" +
					"<ReturnCode>911</ReturnCode>\n" +
					"<ReturnDesc>Issuer Timed Out</ReturnDesc>\n" +
					"<MessageId>cifupdate001</MessageId>\n" +
					"<Extra1>REQ||SHELL.dfgJOHN</Extra1>\n" +
					"<Extra2>2014-01-19T12:20:58.000+04:00</Extra2>\n" +
				"</EE_EAI_HEADER>\n" +
				"<CustomerDetailsUpdateReq>\n" +
					"<BankId>RAK</BankId>\n" +
					"<CIFId>"+CIF_ID+"</CIFId>\n" +
					"<RetCorpFlag>R</RetCorpFlag>\n" +
					"<ProductProccessor>FINACLECORE</ProductProccessor>\n"+
					//"<CustClassification>B</CustClassification>\n" + // passing hardcoded as B for all CIFs POLP - 9866
					"<ActionRequired>U</ActionRequired>\n" +
					FatcaDetails+
					//custDetails+
				"</CustomerDetailsUpdateReq>\n" +
				"</EE_EAI_MESSAGE>");

			DCC_CIFUpdateLog.debug("CIF Update Integration input XML: "+sInputXML.toString());

			String responseXML =socketConnection(cabinetName, CommonConnection.getUsername(), sessionId,sJtsIp,
					 iJtsPort,  processInstanceID,  ws_name, integrationWaitTime, socket_connection_timeout,
					  socketDetailsMap, sInputXML);

			DCC_CIFUpdateLog.debug("responseXML: "+responseXML);

			XMLParser xmlParserSocketDetails= new XMLParser(responseXML);
		    String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
		    DCC_CIFUpdateLog.debug("Return Code: "+return_code);

		    String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
			
			if (return_desc.trim().equalsIgnoreCase(""))
				return_desc = xmlParserSocketDetails.getValueOf("Description");
				
			String MsgId = "";
			if (responseXML.contains("<MessageId>"))
				MsgId = xmlParserSocketDetails.getValueOf("MessageId");
			
			/*if (return_code.equalsIgnoreCase("0000")) {
				String MainGridColNames = "";
				String MainGridColValues = "";
				String sTableName = "";
				String sWhere = "";
				
				//String status= UpdateGridTableMWResponse(MainGridColNames,MainGridColValues,sTableName,sWhere);
			    //DCC_CIFUpdateLog.debug("UpdateGridTableMWResponse CheckGridTable status : " +status);
			}*/
			
		    DCC_CIFUpdateLog.debug("Return Desc: "+return_desc);
		    
		    return (return_code + "~" + return_desc + "~"+ MsgId +"~End");
		}
		catch(Exception e)
		{
			return "Exception in CIF Update";
		}
	}
		

	String socketConnection(String cabinetName, String username, String sessionId, String sJtsIp,
			String iJtsPort, String processInstanceID, String ws_name,
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



		try
		{

			DCC_CIFUpdateLog.debug("userName "+ username);
			DCC_CIFUpdateLog.debug("SessionId "+ sessionId);

			socketServerIP=socketDetailsMap.get("SocketServerIP");
			DCC_CIFUpdateLog.debug("SocketServerIP "+ socketServerIP);
			socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			DCC_CIFUpdateLog.debug("SocketServerPort "+ socketServerPort);

	   		if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0))
	   		{

    			socket = new Socket(socketServerIP, socketServerPort);
    			socket.setSoTimeout(connection_timeout*1000);
    			out = socket.getOutputStream();
    			socketInputStream = socket.getInputStream();
    			dout = new DataOutputStream(out);
    			din = new DataInputStream(socketInputStream);
    			DCC_CIFUpdateLog.debug("Dout " + dout);
    			DCC_CIFUpdateLog.debug("Din " + din);

    			outputResponse = "";



    			inputRequest = getRequestXML( cabinetName,sessionId ,processInstanceID, ws_name, username, sInputXML);


    			if (inputRequest != null && inputRequest.length() > 0)
    			{
    				int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
    				DCC_CIFUpdateLog.debug("RequestLen: "+inputRequestLen + "");
    				inputRequest = inputRequestLen + "##8##;" + inputRequest;
    				DCC_CIFUpdateLog.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
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
    				DCC_CIFUpdateLog.debug("OutputResponse: "+outputResponse);

    				if(!"".equalsIgnoreCase(outputResponse))

    					outputResponse = getResponseXML(cabinetName,sJtsIp,iJtsPort,sessionId,
    							processInstanceID,outputResponse,integrationWaitTime );




    				if(outputResponse.contains("&lt;"))
    				{
    					outputResponse=outputResponse.replaceAll("&lt;", "<");
    					outputResponse=outputResponse.replaceAll("&gt;", ">");
    				}
    			}
    			socket.close();

				outputResponse = outputResponse.replaceAll("</MessageId>","</MessageId>/n<InputMessageId>"+inputMessageID+"</InputMessageId>");

				//DCC_CIFUpdateLog.debug("outputResponse "+outputResponse);
				return outputResponse;

    	 		}

    		else
    		{
    			DCC_CIFUpdateLog.debug("SocketServerIp and SocketServerPort is not maintained "+"");
    			DCC_CIFUpdateLog.debug("SocketServerIp is not maintained "+	socketServerIP);
    			DCC_CIFUpdateLog.debug(" SocketServerPort is not maintained "+	socketServerPort);
    			return "Socket Details not maintained";
    		}

		}

		catch (Exception e)
		{
			DCC_CIFUpdateLog.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
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
				DCC_CIFUpdateLog.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
				//printException(e);
			}
		}


	}

	private String getRequestXML(String cabinetName, String sessionId,
			String processInstanceID, String ws_name, String userName, StringBuilder sInputXML)
	{
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("<APMQPUTGET_Input>");
		strBuff.append("<SessionId>" + sessionId + "</SessionId>");
		strBuff.append("<EngineName>" + cabinetName + "</EngineName>");
		strBuff.append("<XMLHISTORY_TABLENAME>NG_DCC_XMLLOG_HISTORY</XMLHISTORY_TABLENAME>");
		strBuff.append("<WI_NAME>" + processInstanceID + "</WI_NAME>");
		strBuff.append("<WS_NAME>" + ws_name + "</WS_NAME>");
		strBuff.append("<USER_NAME>" + userName + "</USER_NAME>");
		strBuff.append("<MQ_REQUEST_XML>");
		strBuff.append(sInputXML);
		strBuff.append("</MQ_REQUEST_XML>");
		strBuff.append("</APMQPUTGET_Input>");
		DCC_CIFUpdateLog.debug("GetRequestXML: "+ strBuff.toString());
		return strBuff.toString();

	}

	private String getResponseXML(String cabinetName,String sJtsIp,String iJtsPort, String
			sessionId, String processInstanceID,String message_ID, int integrationWaitTime)
	{

		String outputResponseXML="";
		try
		{
			String QueryString = "select OUTPUT_XML from NG_DCC_XMLLOG_HISTORY with (nolock) where " +
					"MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+processInstanceID+"'";

			String responseInputXML =CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, sessionId);
			DCC_CIFUpdateLog.debug("Response APSelect InputXML: "+responseInputXML);

			int Loop_count=0;
			do
			{
				String responseOutputXML=CommonMethods.WFNGExecute(responseInputXML,sJtsIp,iJtsPort,1);
				DCC_CIFUpdateLog.debug("Response APSelect OutputXML: "+responseOutputXML);

			    XMLParser xmlParserSocketDetails= new XMLParser(responseOutputXML);
			    String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			    DCC_CIFUpdateLog.debug("ResponseMainCode: "+responseMainCode);



			    int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			    DCC_CIFUpdateLog.debug("ResponseTotalRecords: "+responseTotalRecords);

			    if (responseMainCode.equals("0") && responseTotalRecords > 0)
				{

					String responseXMLData=xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData =responseXMLData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

	        		XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
	        		//DCC_CIFUpdateLog.debug("ResponseXMLData: "+responseXMLData);

	        		outputResponseXML=xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
	        		//DCC_CIFUpdateLog.debug("OutputResponseXML: "+outputResponseXML);

	        		if("".equalsIgnoreCase(outputResponseXML)){
	        			outputResponseXML="Error";
	    			}
	        		break;
				}
			    Loop_count++;
			    Thread.sleep(1000);
			}
			while(Loop_count<integrationWaitTime);
			DCC_CIFUpdateLog.debug("integrationWaitTime: "+integrationWaitTime);

		}
		catch(Exception e)
		{
			DCC_CIFUpdateLog.debug("Exception occurred in outputResponseXML" + e.getMessage());
			DCC_CIFUpdateLog.debug("Exception occurred in outputResponseXML" + e.getStackTrace());
			outputResponseXML="Error";
		}

		return outputResponseXML;

	}
	
	public String UpdateGridTableMWResponse(String columnNames, String columnValues, String TransactionTable, String sWhereClause) throws IOException, Exception
	{	
		String RetStatus="";
		String QueryString="";
		String sInputXML="";
		String sOutputXML="";
			//Updating records
			sInputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCC_CIFUpdateLog, false), TransactionTable, columnNames, columnValues, sWhereClause);
			DCC_CIFUpdateLog.debug("Input XML for apUpdateInput from "+TransactionTable+" Table "+sInputXML);

			sOutputXML=CommonMethods.WFNGExecute(sInputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
			DCC_CIFUpdateLog.debug("Output XML for apUpdateInput Table "+sOutputXML);

			XMLParser sXMLParserChild= new XMLParser(sOutputXML);
		    String StrMainCode = sXMLParserChild.getValueOf("MainCode");
		    DCC_CIFUpdateLog.debug("StrMainCode: "+StrMainCode);

		    if (StrMainCode.equals("0"))
			{
		    	DCC_CIFUpdateLog.debug("Successful in apUpdateInput the record in : "+TransactionTable);
		    	RetStatus="Success in apUpdateInput the record";
			}
		    else
		    {
		    	DCC_CIFUpdateLog.debug("Error in Executing apUpdateInput sOutputXML : "+TransactionTable);
		    	RetStatus="Error in Executing apUpdateInput";
		    }
			
		return RetStatus;
	}
	
}
