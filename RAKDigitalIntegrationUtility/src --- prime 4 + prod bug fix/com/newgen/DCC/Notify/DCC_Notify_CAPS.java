package com.newgen.DCC.Notify;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.newgen.DCC.AnnualFeeEnable.DCC_AnnualFeeLog;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;

public class DCC_Notify_CAPS {
	
	
	public static Logger DCC_Notify_CAPSLog=null;
	public DCC_Notify_CAPS(Logger DCC_Notify_CAPSLogName)
	{
		DCC_Notify_CAPSLog=DCC_Notify_CAPSLogName;
	}
	
	public String DCC_Notify_CAPS_Integration(String cabinetName,String sessionId,String sJtsIp, String iJtsPort , String processInstanceID,
			String ws_name,String username, int socket_connection_timeout,int integrationWaitTime,
			HashMap<String, String> socketDetailsMap)
	{
		String FinalStatus="";
		try
		{
			DCC_Notify_CAPSLog.debug("Inside notify CAPS for Work Item--"+processInstanceID);
			String sInputXML="";
			String Net_salary1_date="";
			String salary_day="";
			String CreditStatus="";
			String genBridge="";
			
			String fileLocation=new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DCC_Integration")
		    		.append(System.getProperty("file.separator")).append("DCC_NOTIFY_CAPS.txt").toString();
			BufferedReader sbf=new BufferedReader(new FileReader(fileLocation));
    		StringBuilder sb=new StringBuilder();
    		String line=sbf.readLine();
    		while(line!=null)
    		{
    			sb.append(line);
    			sb.append(System.lineSeparator());
    			line=sbf.readLine();
    		}
    		sInputXML=sb.toString();
			String DBQuery = "SELECT CIF,Prospect_id,NOTIFY_CAPS_STATUS,ECRN,CRN,RM_Code,Credit_Shield_Flag,FinalTAI,Net_salary1_date,FinalDBR,Final_Limit,cast(SUBSTRING(Net_salary1_date,9,2)as int)+2 as salary_day,delegation_authority as ApprovalLevelCode,"+
							" (select Description from NG_MASTER_DCC_DELEGATION_AUTHORITY with (nolock) where Code=delegation_authority) as delegation_authority , deviation_description,bureau_reference_number,CURR_WSNAME,Product FROM NG_DCC_EXTTABLE with(nolock) WHERE WI_NAME='"+processInstanceID+"'";

			String extTableDataIPXml = CommonMethods.apSelectWithColumnNames(DBQuery,cabinetName, sessionId);
			DCC_Notify_CAPSLog.debug("Notify CAPS data input: "+ extTableDataIPXml);
			String extTabDataOPXML = CommonMethods.WFNGExecute(extTableDataIPXml,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
			DCC_Notify_CAPSLog.debug("Notify CAPS data output: "+ extTabDataOPXML);

			XMLParser xmlParserData= new XMLParser(extTabDataOPXML);
			int iTotalrec =0;
			String iTotalrecStr=xmlParserData.getValueOf("TotalRetrieved");
			if(iTotalrecStr!=null && !"".equalsIgnoreCase(iTotalrecStr))
			iTotalrec = Integer.parseInt(iTotalrecStr);
			if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec>0)
			{
				String notifyCapsStatus=xmlParserData.getValueOf("NOTIFY_CAPS_STATUS");
				if("Done".equalsIgnoreCase(notifyCapsStatus))
					return "Success";
				Net_salary1_date=xmlParserData.getValueOf("Net_salary1_date");
				if(Net_salary1_date!=null && !"".equalsIgnoreCase(Net_salary1_date))
					Net_salary1_date=CommonMethods.parseDate(Net_salary1_date,"yyyy-MM-dd","dd");
				
				salary_day=xmlParserData.getValueOf("salary_day");
				
				try{
					String SalaryDayQuery = "select top 1 code from NG_DCC_MASTER_StatementCycle where Month_day >='"+salary_day+"' order by Month_day";
					String SalaryDayIPXML = CommonMethods.apSelectWithColumnNames(SalaryDayQuery,cabinetName, sessionId);
					DCC_Notify_CAPSLog.debug("Notify CAPS data input: "+ SalaryDayIPXML);
					String SalaryDayOPXML = CommonMethods.WFNGExecute(SalaryDayIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
					DCC_Notify_CAPSLog.debug("Notify CAPS data output: "+ SalaryDayOPXML);
					XMLParser SalaryDayData= new XMLParser(SalaryDayOPXML);
					salary_day=SalaryDayData.getValueOf("code");
					DCC_Notify_CAPSLog.debug("Final Salary day:- "+ SalaryDayOPXML);
				}
				catch(Exception e){
					DCC_Notify_CAPSLog.debug("Notify CAPS data output: "+ e);
				}
				
				String CURR_WSNAME=xmlParserData.getValueOf("CURR_WSNAME");
				if("Sys_CardBalClsBlk".equalsIgnoreCase(CURR_WSNAME))
				{
					CreditStatus="Declined";
					genBridge="N";
					sInputXML=sInputXML.replace("#SecurityChequeDetails#", "");
					sInputXML=sInputXML.replace("#CreditLimit#", "");
					
				}
				else if("Sys_Limit_Increase".equalsIgnoreCase(CURR_WSNAME))
				{
					CreditStatus="Approved";
					genBridge="Y";
					String secCheck="\n\t\t<SecurityChequeDetailsFlg>Y</SecurityChequeDetailsFlg>"+
				      "\n\t\t<SecurityChqNo>#SecurityChqNo#</SecurityChqNo>"+
				      "\n\t\t<SecuritychqBank>RAKBANK</SecuritychqBank>"+
				      "\n\t\t<SecurityChqAmt>#Final_Limit#</SecurityChqAmt>";
					sInputXML=sInputXML.replace("#SecurityChequeDetails#", secCheck);
					sInputXML=sInputXML.replace("#CreditLimit#", "\n\t\t<CreditLimit>#Final_Limit#</CreditLimit>");
				}
					
			}
			else
			{
				FinalStatus = "Failure";
			}
			String deviationCodes="";
			String DeviationQuery="select  top 5 Deviation_Code from NG_DCC_GR_DEVIATION_DESCRIPTIO with(nolock) where Wi_Name ='"+processInstanceID+"'";
			String extTabDataIPXMLDeviation = CommonMethods.apSelectWithColumnNames(DeviationQuery,cabinetName, sessionId);
			DCC_Notify_CAPSLog.debug("CIF Update data input: "+ extTabDataIPXMLDeviation);
			String extTabDataOPXMLDeviation = CommonMethods.WFNGExecute(extTabDataIPXMLDeviation,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
			DCC_Notify_CAPSLog.debug("CIF Update data output: "+ extTabDataOPXMLDeviation);
			XMLParser xmlParserDataDeviation= new XMLParser(extTabDataOPXMLDeviation);						
			int iTotalrecDeviation = Integer.parseInt(xmlParserDataDeviation.getValueOf("TotalRetrieved"));
			if(xmlParserDataDeviation.getValueOf("MainCode").equalsIgnoreCase("0")&& iTotalrecDeviation>0)
			{
				
				NGXmlList objWorkList=xmlParserDataDeviation.createList("Records", "Record");
				
				int i=1;
				for (; objWorkList.hasMoreElements(true) && i<=5; objWorkList.skip(true))
				{
					String code=objWorkList.getVal("Deviation_Code");
					deviationCodes+="\n\t\t<DeviationCode"+i+">"+code+"</DeviationCode"+i+">";
					i++;
				}
				
				
			}
			
			String TranSettlementDate="";
			String TranMaturityDate="";
			String TranPrincipal="";
			String TranQuantityUnit="";
			String TransactionNo="";
			String TranAgreedProfitRate="";
			String TranPrice="";
			String TranClientName="";
			String TranTotalPrincipal="";
			String TranOriginalSettlementDate="";
			String TranQuantity="";
			String TranTradeDate="";
			if("ISL".equalsIgnoreCase(xmlParserData.getValueOf("Product")))
			{
				String Str_MurabahaDetails = "\n\t\t<MurabahaTranCurrency>AED</MurabahaTranCurrency>"+
						"\n\t\t<MurabahaTranSettlementDate>#TranSettlementDate#</MurabahaTranSettlementDate>"+
						"\n\t\t<TranMaturityDate>#TranMaturityDate#</TranMaturityDate>"+
						"\n\t\t<TranTradeDate>#TranTradeDate#</TranTradeDate>"+
						"\n\t\t<MurabahaTranPrincipal>#TranPrincipal#</MurabahaTranPrincipal>"+
						"\n\t\t<MurabahaTranQuantityUnit>#TranQuantityUnit#</MurabahaTranQuantityUnit>"+
						"\n\t\t<MurabahaTransactionNo>#TransactionNo#</MurabahaTransactionNo>"+
						"\n\t\t<MurabahaTranAgreedProfitRate>#TranAgreedProfitRate#</MurabahaTranAgreedProfitRate>"+
						"\n\t\t<MurabahaTranPrice>#TranPrice#</MurabahaTranPrice>"+
						"\n\t\t<MurabahaTranClientName>#TranClientName#</MurabahaTranClientName>"+
						"\n\t\t<MurabahaTranTotalPrincipal>#TranTotalPrincipal#</MurabahaTranTotalPrincipal>"+
						"\n\t\t<MurabahaTranOriginalSettlementDate>#TranOriginalSettlementDate#</MurabahaTranOriginalSettlementDate>"+
						"\n\t\t<MurabahaTranQuantity>#TranQuantity#</MurabahaTranQuantity>"+
						"\n\t\t<MurabahaTranCommodity>Palladium</MurabahaTranCommodity>"+
						"\n\t\t<MurabahaTranClass>STANDARD</MurabahaTranClass>";
				sInputXML=sInputXML.replace("#str_MurabahaDetails#", Str_MurabahaDetails);
				//Deepak changes done as part of PDSC-311 || CAPS call- Murabaha client name to be truancated to 30 in NOTIFY_CARD_APPLICATION call -- 01March23 to 
				//String MurabhaFieldsQuery="select top 1 TranSettlementDate,TranMaturityDate,TradeDate,TranPrincipal,TranQuantityUnit,TransactionNo,TranAgreedProfitRate,TranPrice,TranClientName,TranTotalPrincipal,TranOriginalSettlementDate,TranQuantity from NG_DCC_MURABAHA_RESPONSE_DATA with(nolock) where Wi_Name ='"+processInstanceID+"' order by CALL_SEQ desc";
				String MurabhaFieldsQuery="select top 1 TranSettlementDate,TranMaturityDate,TradeDate,TranPrincipal,TranQuantityUnit,TransactionNo,TranAgreedProfitRate,TranPrice,SUBSTRING(TranClientName,0,31) as TranClientName,TranTotalPrincipal,TranOriginalSettlementDate,TranQuantity from NG_DCC_MURABAHA_RESPONSE_DATA with(nolock) where Wi_Name ='"+processInstanceID+"' order by CALL_SEQ desc";
				String MurabhaFieldsIPXML = CommonMethods.apSelectWithColumnNames(MurabhaFieldsQuery,cabinetName, sessionId);
				DCC_Notify_CAPSLog.debug("Notify CAPS Update Murabha data input: "+ MurabhaFieldsIPXML);
				String MurabhaFieldsOPXML= CommonMethods.WFNGExecute(MurabhaFieldsIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
				DCC_Notify_CAPSLog.debug("Notify CAPS Update Murabhaa output: "+ MurabhaFieldsOPXML);
				//HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();
				XMLParser xmlParserMurabhaFields= new XMLParser(MurabhaFieldsOPXML);						
				int iTotalrecMurabhaFields = Integer.parseInt(xmlParserMurabhaFields.getValueOf("TotalRetrieved"));
				if(xmlParserMurabhaFields.getValueOf("MainCode").equalsIgnoreCase("0")&& iTotalrecMurabhaFields>0)
				{
					TranSettlementDate=xmlParserMurabhaFields.getValueOf("TranSettlementDate");
					TranMaturityDate=xmlParserMurabhaFields.getValueOf("TranMaturityDate");
					TranQuantityUnit=xmlParserMurabhaFields.getValueOf("TranQuantityUnit");
					TransactionNo=xmlParserMurabhaFields.getValueOf("TransactionNo");
					TranAgreedProfitRate=xmlParserMurabhaFields.getValueOf("TranAgreedProfitRate");
					TranPrice=xmlParserMurabhaFields.getValueOf("TranPrice");
					TranClientName=xmlParserMurabhaFields.getValueOf("TranClientName");
					TranTotalPrincipal=xmlParserMurabhaFields.getValueOf("TranTotalPrincipal");
					TranOriginalSettlementDate=xmlParserMurabhaFields.getValueOf("TranOriginalSettlementDate");
					TranQuantity=xmlParserMurabhaFields.getValueOf("TranQuantity");
					TranTradeDate=xmlParserMurabhaFields.getValueOf("TradeDate");
					TranPrincipal=xmlParserMurabhaFields.getValueOf("TranPrincipal");
					if (TranTradeDate != null && !"".equalsIgnoreCase(TranTradeDate))
						TranTradeDate=CommonMethods.parseDate(TranTradeDate,"d MMM yyyy","dd-MM-yyyy");
					
					if (TranMaturityDate != null && !"".equalsIgnoreCase(TranMaturityDate))
						TranMaturityDate=CommonMethods.parseDate(TranMaturityDate,"d MMM yyyy","dd-MM-yyyy");
					
					
					if (TranSettlementDate != null && !"".equalsIgnoreCase(TranSettlementDate))
						TranSettlementDate=CommonMethods.parseDate(TranSettlementDate,"d MMM yyyy","dd-MM-yyyy");
					
					if (TranOriginalSettlementDate != null && !"".equalsIgnoreCase(TranOriginalSettlementDate))
						TranOriginalSettlementDate=CommonMethods.parseDate(TranOriginalSettlementDate,"d MMM yyyy","dd-MM-yyyy");
					
				}
				else
					FinalStatus = "Failure";
			}
			else
			{
				//for Conventional to to remove MurabahaDetails
				sInputXML=sInputXML.replace("#str_MurabahaDetails#", "");
			}
			

			DCC_Notify_CAPSLog.debug("WINAME : "+processInstanceID);
			sInputXML=sInputXML.replace("#CIF#", validateValue(xmlParserData.getValueOf("CIF")));
			sInputXML=sInputXML.replace("#ECRN#", validateValue(xmlParserData.getValueOf("ECRN")));
			sInputXML=sInputXML.replace("#RM_Code#", validateValue(xmlParserData.getValueOf("RM_Code")));
			sInputXML=sInputXML.replace("#Credit_Shield_Flag#", validateValue(xmlParserData.getValueOf("Credit_Shield_Flag")));
			sInputXML=sInputXML.replace("#Wi_Name#",processInstanceID.substring(4,14));
			sInputXML=sInputXML.replace("#FinalTAI#", validateValue(xmlParserData.getValueOf("FinalTAI")));
			sInputXML=sInputXML.replace("#Net_salary1_date#", Net_salary1_date);
			sInputXML=sInputXML.replace("#FinalDBR#", validateValue(xmlParserData.getValueOf("FinalDBR")));
			sInputXML=sInputXML.replace("#Final_Limit#", validateValue(xmlParserData.getValueOf("Final_Limit")));
			sInputXML=sInputXML.replace("#Net_salary1_date2#", validateValue(salary_day));
			sInputXML=sInputXML.replace("#ApprovalLevelCode#", validateValue(xmlParserData.getValueOf("ApprovalLevelCode")));
			sInputXML=sInputXML.replace("#delegation_authority#", validateValue(xmlParserData.getValueOf("delegation_authority")));
			sInputXML=sInputXML.replace("#str_DeviationCode#", deviationCodes);
			sInputXML=sInputXML.replace("#CREDITSTATUS#", CreditStatus);
			sInputXML=sInputXML.replace("#bureau_reference_number#", validateValue(xmlParserData.getValueOf("bureau_reference_number")));
			sInputXML=sInputXML.replace("#SecurityChqNo#", validateValue(xmlParserData.getValueOf("Prospect_id")));
			sInputXML=sInputXML.replace("#TranSettlementDate#", validateValue(TranSettlementDate));
			sInputXML=sInputXML.replace("#TranMaturityDate#", validateValue(TranMaturityDate));
			sInputXML=sInputXML.replace("#TranPrincipal#", validateValue(TranPrincipal));
			sInputXML=sInputXML.replace("#TranQuantityUnit#", validateValue(TranQuantityUnit));
			sInputXML=sInputXML.replace("#TransactionNo#", validateValue(TransactionNo));
			sInputXML=sInputXML.replace("#TranAgreedProfitRate#", validateValue(TranAgreedProfitRate));
			sInputXML=sInputXML.replace("#TranPrice#", validateValue(TranPrice));
			sInputXML=sInputXML.replace("#TranClientName#", validateValue(TranClientName));
			sInputXML=sInputXML.replace("#TranTotalPrincipal#", validateValue(TranTotalPrincipal));
			sInputXML=sInputXML.replace("#TranOriginalSettlementDate#", validateValue(TranOriginalSettlementDate));
			sInputXML=sInputXML.replace("#TranQuantity#", validateValue(TranQuantity));
			sInputXML=sInputXML.replace("#TranTradeDate#", validateValue(TranTradeDate));
			sInputXML=sInputXML.replace("#CRN#", validateValue(xmlParserData.getValueOf("CRN")));
			sInputXML=sInputXML.replace("#GenerateLimitBridge#", genBridge);
			DCC_Notify_CAPSLog.debug("sInputXML for Notify CAPS : "+sInputXML);
			
			if(!"Failure".equalsIgnoreCase(FinalStatus))
			{
				String responseXML=socketConnection( cabinetName,  username,  sessionId,  sJtsIp,
						 iJtsPort,  processInstanceID,  ws_name,
						 socket_connection_timeout,  integrationWaitTime, socketDetailsMap,  sInputXML);

				DCC_Notify_CAPSLog.debug("CIF Update integrationStatus: " +responseXML);
				
				DCC_Notify_CAPSLog.debug("responseXML: "+responseXML);

				XMLParser xmlParserResponse= new XMLParser(responseXML);
			    String return_code = xmlParserResponse.getValueOf("ReturnCode");
			    DCC_Notify_CAPSLog.debug("Return Code: "+return_code);
			    String return_desc = xmlParserResponse.getValueOf("ReturnDesc");
			    DCC_Notify_CAPSLog.debug("Return Desc: "+return_desc);
				if (return_desc.trim().equalsIgnoreCase(""))
					return_desc = xmlParserResponse.getValueOf("Description");
				String MsgId = "";
				if (responseXML.contains("<MessageId>"))
					MsgId = xmlParserResponse.getValueOf("MessageId");
				//return (return_code + "~" + return_desc + "~"+ MsgId +"~End");
				if("0000".equalsIgnoreCase(return_code))
				{
					FinalStatus = "Success";
					try {
						String columnNames1 = "NOTIFY_CAPS_STATUS";
						String columnValues1 = "'Done'";
						String sWhereClause = "Wi_Name = '" + processInstanceID + "' ";

						String extTableIPUpdateXml = CommonMethods.apUpdateInput(cabinetName,sessionId, "ng_dcc_exttable",columnNames1, columnValues1, sWhereClause);
						DCC_Notify_CAPSLog.debug("Input XML for apUpdateInput for " + "ng_dcc_exttable" + " Table : " + extTableIPUpdateXml);

						String extTableOPUpdateXml = CommonMethods.WFNGExecute(extTableIPUpdateXml,sJtsIp,iJtsPort, 1);
						DCC_Notify_CAPSLog.debug("Output XML for apUpdateInput for " + "ng_dcc_exttable" + " Table : " + extTableOPUpdateXml);

						XMLParser sXMLParserChild = new XMLParser(extTableOPUpdateXml);
						String StrMainCode = sXMLParserChild.getValueOf("MainCode");

						if (StrMainCode.equals("0"))
							DCC_Notify_CAPSLog.debug("Successful in apUpdateInput the record in : " + "ng_dcc_exttable");
						else
						{
							DCC_Notify_CAPSLog.debug("Error in Executing apUpdateInput sOutputXML : " + extTableOPUpdateXml);
							System.out.println("WMgetWorkItemCall failed: " + processInstanceID);
							DCC_Notify_CAPSLog.debug("WMgetWorkItemCall failed: " + processInstanceID);
							FinalStatus = "Failure~ For ECRN: "+xmlParserData.getValueOf("ECRN")+"~ MsgStatus: Some technical error in updating Credit Limit Flag..~ MsgId: "+MsgId;
						}
					}
					catch (Exception e) 
					{
						DCC_Notify_CAPSLog.debug("Exception in updating notify CAPS: " + e.getMessage());
						FinalStatus = "Failure~ For ECRN: "+xmlParserData.getValueOf("ECRN")+"~ MsgStatus: Some technical error in updating Credit Limit Flag..~ MsgId: "+MsgId;
					}
				} 
				else
				{
					FinalStatus = "Failure~ For ECRN: "+xmlParserData.getValueOf("ECRN")+"~ MsgStatus: "+return_desc+"~ MsgId: "+MsgId;
				}
			}
			
		}
		catch(Exception e)
		{
			DCC_Notify_CAPSLog.debug("Exception in DCC_Notify_CAPS_Integration: " +e.toString());
		}
		return FinalStatus;
	}
	String socketConnection(String cabinetName, String username, String sessionId, String sJtsIp,
			String iJtsPort, String processInstanceID, String ws_name,
			int connection_timeout, int integrationWaitTime,HashMap<String, String> socketDetailsMap, String sInputXML)
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

			DCC_Notify_CAPSLog.debug("userName "+ username);
			DCC_Notify_CAPSLog.debug("SessionId "+ sessionId);

			socketServerIP=socketDetailsMap.get("SocketServerIP");
			DCC_Notify_CAPSLog.debug("SocketServerIP "+ socketServerIP);
			socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			DCC_Notify_CAPSLog.debug("SocketServerPort "+ socketServerPort);

	   		if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0))
	   		{

    			socket = new Socket(socketServerIP, socketServerPort);
    			socket.setSoTimeout(connection_timeout*1000);
    			out = socket.getOutputStream();
    			socketInputStream = socket.getInputStream();
    			dout = new DataOutputStream(out);
    			din = new DataInputStream(socketInputStream);
    			DCC_Notify_CAPSLog.debug("Dout " + dout);
    			DCC_Notify_CAPSLog.debug("Din " + din);

    			outputResponse = "";



    			inputRequest = getRequestXML( cabinetName,sessionId ,processInstanceID, ws_name, username, sInputXML);


    			if (inputRequest != null && inputRequest.length() > 0)
    			{
    				int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
    				DCC_Notify_CAPSLog.debug("RequestLen: "+inputRequestLen + "");
    				inputRequest = inputRequestLen + "##8##;" + inputRequest;
    				DCC_Notify_CAPSLog.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
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
    				DCC_Notify_CAPSLog.debug("OutputResponse: "+outputResponse);

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

				//DCC_Notify_CAPSLog.debug("outputResponse "+outputResponse);
				return outputResponse;

    	 		}

    		else
    		{
    			DCC_Notify_CAPSLog.debug("SocketServerIp and SocketServerPort is not maintained "+"");
    			DCC_Notify_CAPSLog.debug("SocketServerIp is not maintained "+	socketServerIP);
    			DCC_Notify_CAPSLog.debug(" SocketServerPort is not maintained "+	socketServerPort);
    			return "Socket Details not maintained";
    		}

		}

		catch (Exception e)
		{
			DCC_Notify_CAPSLog.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
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
				DCC_Notify_CAPSLog.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
				//printException(e);
			}
		}


	}
	private String getRequestXML(String cabinetName, String sessionId,
			String processInstanceID, String ws_name, String userName, String sInputXML)
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
		DCC_Notify_CAPSLog.debug("GetRequestXML: "+ strBuff.toString());
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
			DCC_Notify_CAPSLog.debug("Response APSelect InputXML: "+responseInputXML);

			int Loop_count=0;
			do
			{
				String responseOutputXML=CommonMethods.WFNGExecute(responseInputXML,sJtsIp,iJtsPort,1);
				DCC_Notify_CAPSLog.debug("Response APSelect OutputXML: "+responseOutputXML);

			    XMLParser xmlParserSocketDetails= new XMLParser(responseOutputXML);
			    String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			    DCC_Notify_CAPSLog.debug("ResponseMainCode: "+responseMainCode);



			    int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			    DCC_Notify_CAPSLog.debug("ResponseTotalRecords: "+responseTotalRecords);

			    if (responseMainCode.equals("0") && responseTotalRecords > 0)
				{

					String responseXMLData=xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData =responseXMLData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

	        		XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
	        		//DCC_Notify_CAPSLog.debug("ResponseXMLData: "+responseXMLData);

	        		outputResponseXML=xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
	        		//DCC_Notify_CAPSLog.debug("OutputResponseXML: "+outputResponseXML);

	        		if("".equalsIgnoreCase(outputResponseXML)){
	        			outputResponseXML="Error";
	    			}
	        		break;
				}
			    Loop_count++;
			    Thread.sleep(1000);
			}
			while(Loop_count<integrationWaitTime);
			DCC_Notify_CAPSLog.debug("integrationWaitTime: "+integrationWaitTime);

		}
		catch(Exception e)
		{
			DCC_Notify_CAPSLog.debug("Exception occurred in outputResponseXML" + e.getMessage());
			DCC_Notify_CAPSLog.debug("Exception occurred in outputResponseXML" + e.getStackTrace());
			outputResponseXML="Error";
		}

		return outputResponseXML;

	}
	
	private static  String validateValue(String value) {
		if (value != null && ! value.equals("") && !value.equalsIgnoreCase("null")) {
			return value.trim();
		}
		return "";
	}
}
