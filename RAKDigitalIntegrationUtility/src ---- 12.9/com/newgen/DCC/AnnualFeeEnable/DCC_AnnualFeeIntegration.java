package com.newgen.DCC.AnnualFeeEnable;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import com.newgen.DCC.Final_Limit_Increase.DCC_FINAL_LIMIT_INC;
import com.newgen.DCC.Final_Limit_Increase.DCC_FINAL_LIMIT_LOG;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.XMLParser;

public class DCC_AnnualFeeIntegration {

	String sessionID = "";
	String cabinetname = "";
	String JtsIp = "";
	String JtsPort = "";
	String processInstanceId = "";
	String UserName = "";
	int socketConnectionTimeout = 0;
	int IntegrationWaitTime = 0;
	int sleepIntervalInMin = 0;
	int TrialTime = 0;
	int ErrorCount = 0;
	Socket socket = null;
	String socketServerIP = "";
	int socketServerPort = 0;
	OutputStream out = null;
	InputStream socketInputStream = null;
	DataOutputStream dout = null;
	DataInputStream din = null;
	String outputResponse = null;
	String inputRequest = null;
	String inputMessageID = null;
	String ws_Name = "";
	String ReturnDesc = "";
	String ReturnCode = "";
	String Description = "";
	String entryDateTime = "";

	final String NG_DCC_EXTTABLE = "NG_DCC_EXTTABLE";
	HashMap<String, String> socketConnectionMap = null;

	public String CardEnquiry(String cabinetName, String UserName, String sJtsIp, String iJtsPort, String sessionId,
			String processInstanceID, int socketConnectionTimeOut, int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, int trialTime, int errorCount, String ws_name,
			String entryTimeDate) throws Exception {

		String DBQuery = "SELECT Final_Limit,Card_Number,CIF_ID,Applied_card,service_fee_flag FROM NG_DCC_EXTTABLE with(nolock) WHERE WI_NAME='"
				+ processInstanceID + "'";

		String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery, cabinetName, sessionId);
		DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("extTabDataIPXML: " + extTabDataIPXML);

		String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, sJtsIp, iJtsPort, 1);
		DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("extTabDataOPXML: " + extTabDataOPXML);

		XMLParser xmlParserData = new XMLParser(extTabDataOPXML);

		String str_iTotalrec = xmlParserData.getValueOf("TotalRetrieved");
		int iTotalrec = 0;
		if (!(str_iTotalrec == null || "".equalsIgnoreCase(str_iTotalrec))) {
			iTotalrec = Integer.parseInt(str_iTotalrec);
		}

		String fetchWorkItemListMainCode = "";
		if (!(xmlParserData.getValueOf("MainCode") == null
				|| "".equalsIgnoreCase(xmlParserData.getValueOf("MainCode")))) {
			fetchWorkItemListMainCode = xmlParserData.getValueOf("MainCode");
		}

		if (fetchWorkItemListMainCode.equalsIgnoreCase("0") && iTotalrec > 0) {
			String xmlDataExtTab = xmlParserData.getNextValueOf("Record");
			xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

			StringBuilder cardEnquiryInputXML = readRequestXmlSample(xmlParserData, processInstanceID, "CARD_ENQ");

			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("balanceEnquiryInputXML: " + cardEnquiryInputXML);

			socketConnectionMap = CommonMethods.socketConnectionDetails(cabinetName, sJtsIp, iJtsPort, sessionId);

			outputResponse = socketConnection(cabinetName, UserName, sessionId, sJtsIp, iJtsPort, processInstanceID,
					ws_name, socketConnectionTimeOut, integrationWaitTime, socketDetailsMap, cardEnquiryInputXML);

			XMLParser xmlParserSocketDetails = new XMLParser(outputResponse);
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug(" xmlParserSocketDetails : " + xmlParserSocketDetails);

			if (outputResponse.contains("<ReturnDesc>"))
				ReturnDesc = xmlParserSocketDetails.getValueOf("ReturnDesc");
			if (outputResponse.contains("<ReturnCode>"))
				ReturnCode = xmlParserSocketDetails.getValueOf("ReturnCode");
			if (outputResponse.contains("<Description>"))
				Description = xmlParserSocketDetails.getValueOf("Description");

			if ("0000".equalsIgnoreCase(xmlParserSocketDetails.getValueOf("ReturnCode"))) 
			{
				String Final_Limit = xmlParserData.getValueOf("Final_Limit");
				String balStr=xmlParserSocketDetails.getValueOf("CardGroupInformation");
				balStr=balStr.substring(balStr.indexOf("TotalLimit"));
				balStr=balStr.substring(0,balStr.indexOf("</BalanceAmt>"));
				balStr=balStr.substring(balStr.indexOf("<BalanceAmt>")+"<BalanceAmt>".length());
				Double balanceAmount = Double.parseDouble(balStr);
				Double double_Final_Limit = Double.parseDouble(Final_Limit);
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug(" Total limit at Caps : " + balanceAmount);
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug(" Total limit obtained from dectech: " + double_Final_Limit);
				if (balanceAmount.equals(double_Final_Limit)) 
				{

					try
					{
						DCC_FINAL_LIMIT_INC objlimit=new  DCC_FINAL_LIMIT_INC();
						String ServiceMaintenanceFlag=objlimit.serviceMaintainance_final_limit(processInstanceID);
						String actioneddatetime=objlimit.actioneddate;
						DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("ServiceMaintenanceFlag" +ServiceMaintenanceFlag);
						DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("actioneddatetime" +actioneddatetime);
						if("Y".equalsIgnoreCase(ServiceMaintenanceFlag))
						{
			        	  String ErrDesc="";
			        	  String decisionValue="";
			        	  String attributesTag="";
								DCC_FINAL_LIMIT_Service_Maintenance objservice= new DCC_FINAL_LIMIT_Service_Maintenance(DCC_AnnualFeeLog.DCC_AnnualFeeLogger);
							String serviceRequestIntegrationStatus=objservice.DCC_Service_Maintenance_Integration(cabinetName,CommonConnection.getJTSIP(),
									CommonConnection.getJTSPort(),sessionId,processInstanceID,ws_name,integrationWaitTime,socketConnectionTimeout,socketDetailsMap,actioneddatetime);
							DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("serviceRequestIntegrationStatus" +serviceRequestIntegrationStatus);
							String statuses [] = null;
							if (serviceRequestIntegrationStatus != null)
								statuses = serviceRequestIntegrationStatus.split("~");
							if (statuses != null && statuses.length > 0 && statuses[0].equalsIgnoreCase("0000"))
							{
								String updateStatus_service=updateDataInExtTableflag(processInstanceID,"is_service_main","Y",cabinetName,sessionId);
								if ("Success".equalsIgnoreCase(updateStatus_service)){
									String is_service_main = "Y";
						String columnNames1 = "service_fee_flag";
									String columnValues1 = "'C'";
						String sWhereClause = "Wi_Name ='" + processInstanceID + "'";

						String extTableIPUpdateXml = CommonMethods.apUpdateInput(cabinetName,sessionId,NG_DCC_EXTTABLE, columnNames1, columnValues1, sWhereClause);
						DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Input XML for apUpdateInput for "+ NG_DCC_EXTTABLE + " Table : " + extTableIPUpdateXml);

						String extTableOPUpdateXml = CommonMethods.WFNGExecute(extTableIPUpdateXml,sJtsIp,iJtsPort, 1);
						DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Output XML for apUpdateInput for "+ NG_DCC_EXTTABLE + " Table : " + extTableOPUpdateXml);

						XMLParser sXMLParserChild = new XMLParser(extTableOPUpdateXml);
						String StrMainCode = sXMLParserChild.getValueOf("MainCode");

						if (StrMainCode.equals("0")) 
						{
							DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Total Limit and Final limit are equal this time and Successful in apUpdateInput the record in : " + NG_DCC_EXTTABLE);
						} 
						else 
						{
							DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Total Limit and Final limit are equal this time but Error in Executing apUpdateInput sOutputXML : "+ extTableOPUpdateXml);
							System.out.println("Total Limit and Final limit are equal this time but WMgetWorkItemCall failed: " + processInstanceID);
							DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Total Limit and Final limit are equal this time but WMgetWorkItemCall failed: " + processInstanceID);
									}
									
								}
							} 
							else 
							{
								if (statuses != null && statuses.length > 0) {
									ErrDesc = "Service Maintenance Failed " + statuses[0] + ":";
									if (statuses.length > 1)
										ErrDesc =ErrDesc +statuses[1];
								} else {
									ErrDesc = "Service Maintenance Failed ";
								}
								decisionValue = "Failed";
								DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Decision" +decisionValue);
								attributesTag="<Decision>"+decisionValue+"</Decision>";
							}
						}
						else{
							DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("service maintenace is not applicable");
						}
					} 
					catch (Exception e) 
					{
						DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Exception in updating workitem" + processInstanceID);
					}

				}
				else
					DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug(" Total limit obtained from dectech is not equal to limit at Caps for wi "+processInstanceID);
			}

		}

		return "";
	}

	private static StringBuilder readRequestXmlSample(XMLParser xmlParserData, String processInstanceId,
			String CAllNAme) {
		
		String Card_Number = xmlParserData.getValueOf("Card_Number");
		String CIF_ID = xmlParserData.getValueOf("CIF_ID");

		DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("wi_name: " + processInstanceId);
		DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Card_Number: " + Card_Number);
		DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("CIF_ID: " + CIF_ID);

		StringBuilder finalsb = new StringBuilder("");
		if (CAllNAme == "CARD_ENQ") {
			StringBuilder sb = new StringBuilder("");

			try {

				String fileLocation = new StringBuffer().append(System.getProperty("user.dir"))
						.append(System.getProperty("file.separator")).append("DCC_Integration")
						.append(System.getProperty("file.separator")).append("CardBalanceEnquiry.txt").toString();
				BufferedReader sbf = new BufferedReader(new FileReader(fileLocation));

				String line = sbf.readLine();
				while (line != null) {
					sb.append(line);
					sb.append(System.lineSeparator());
					line = sbf.readLine();
				}
				if(CIF_ID==null)
					CIF_ID="";
				if(Card_Number==null)
					Card_Number="";

				String CardBalanceEnquiry_input_xml = sb.toString()
						.replace(">CIF_ID<", ">" + CIF_ID.trim() + "<")
						.replace(">Card_Number_ID<", ">" + Card_Number.trim() + "<");

				finalsb = finalsb.append(CardBalanceEnquiry_input_xml);
				sbf.close();
			} catch (FileNotFoundException e) {
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger.error("In Reading AnnualFeeEnquiry.txt : " + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger.error("In Reading AnnualFeeEnquiry.txt : " + e.getMessage());
				e.printStackTrace();
			}
			
			return finalsb;
		}

		return finalsb;

	}

	private static String getRequestXML(String cabinetName, String sessionId, String wi_name, String ws_name,
			String userName, StringBuilder sInputXML) {
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("<APMQPUTGET_Input>");
		strBuff.append("<SessionId>" + sessionId + "</SessionId>");
		strBuff.append("<EngineName>" + cabinetName + "</EngineName>");
		strBuff.append("<XMLHISTORY_TABLENAME>NG_DCC_XMLLOG_HISTORY</XMLHISTORY_TABLENAME>");
		strBuff.append("<WI_NAME>" + wi_name + "</WI_NAME>");
		strBuff.append("<WS_NAME>" + ws_name + "</WS_NAME>");
		strBuff.append("<USER_NAME>" + userName + "</USER_NAME>");
		strBuff.append("<MQ_REQUEST_XML>");
		strBuff.append(new StringBuffer(sInputXML));
		strBuff.append("</MQ_REQUEST_XML>");
		strBuff.append("</APMQPUTGET_Input>");
		DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("GetRequestXML: " + strBuff.toString());
		return strBuff.toString();
	}

	private static String getResponseXML(String cabinetName, String sJtsIp, String iJtsPort, String sessionId,
			String wi_name, String message_ID, int integrationWaitTime) {

		String outputResponseXML = "";
		try {
			String QueryString = "select OUTPUT_XML from NG_DCC_XMLLOG_HISTORY with (nolock) where MESSAGE_ID ='"
					+ message_ID + "' and WI_NAME = '" + wi_name + "'";

			String responseInputXML = CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, sessionId);
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Response APSelect InputXML: " + responseInputXML);

			int Loop_count = 0;
			do {
				String responseOutputXML = CommonMethods.WFNGExecute(responseInputXML, sJtsIp, iJtsPort, 1);
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Response APSelect OutputXML: " + responseOutputXML);

				XMLParser xmlParserSocketDetails = new XMLParser(responseOutputXML);
				String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("ResponseMainCode: " + responseMainCode);

				int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("ResponseTotalRecords: " + responseTotalRecords);

				if (responseMainCode.equals("0") && responseTotalRecords > 0) {

					String responseXMLData = xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData = responseXMLData.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

					XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);

					outputResponseXML = xmlParserResponseXMLData.getValueOf("OUTPUT_XML");

					if ("".equalsIgnoreCase(outputResponseXML)) {
						outputResponseXML = "Error";
					}
					break;
				}
				Loop_count++;
				Thread.sleep(1000);
			} while (Loop_count < integrationWaitTime);
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("integrationWaitTime: " + integrationWaitTime);

		} catch (Exception e) {
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Exception occurred in outputResponseXML" + e.getMessage());
			outputResponseXML = "Error";
		}

		return outputResponseXML;
	}

	static String socketConnection(String cabinetName, String username, String sessionId, String sJtsIp,
			String iJtsPort, String wi_name, String ws_name, int connection_timeout, int integrationWaitTime,
			HashMap<String, String> socketDetailsMap, StringBuilder sInputXML) {

		String socketServerIP;
		int socketServerPort;
		Socket socket = null;
		OutputStream out = null;
		InputStream socketInputStream = null;
		DataOutputStream dout = null;
		DataInputStream din = null;
		String outputResponse = "";
		String inputRequest = null;
		String inputMessageID = null;

		try {

			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("userName " + username);
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("SessionId " + sessionId);

			socketServerIP = socketDetailsMap.get("SocketServerIP");
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("SocketServerIP " + socketServerIP);
			socketServerPort = Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("SocketServerPort " + socketServerPort);

			if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort == 0)) {

				socket = new Socket(socketServerIP, socketServerPort);
				socket.setSoTimeout(connection_timeout * 1000);
				out = socket.getOutputStream();
				socketInputStream = socket.getInputStream();
				dout = new DataOutputStream(out);
				din = new DataInputStream(socketInputStream);
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Dout " + dout);
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Din " + din);

				inputRequest = getRequestXML(cabinetName, sessionId, wi_name, ws_name, username, sInputXML);

				if (inputRequest != null && inputRequest.length() > 0) {
					int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
					DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("RequestLen: " + inputRequestLen + "");
					inputRequest = inputRequestLen + "##8##;" + inputRequest;
					DCC_AnnualFeeLog.DCC_AnnualFeeLogger
							.debug("InputRequest" + "Input Request Bytes : " + inputRequest.getBytes("UTF-16LE"));
					dout.write(inputRequest.getBytes("UTF-16LE"));
					dout.flush();
				}
				byte[] readBuffer = new byte[500];
				int num = din.read(readBuffer);
				if (num > 0) {

					byte[] arrayBytes = new byte[num];
					System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
					outputResponse = outputResponse + new String(arrayBytes, "UTF-16LE");
					inputMessageID = outputResponse;
					DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("OutputResponse: " + outputResponse);

					if (!"".equalsIgnoreCase(outputResponse))
						outputResponse = getResponseXML(cabinetName, sJtsIp, iJtsPort, sessionId, wi_name,
								outputResponse, integrationWaitTime);

					if (outputResponse.contains("&lt;")) {
						outputResponse = outputResponse.replaceAll("&lt;", "<");
						outputResponse = outputResponse.replaceAll("&gt;", ">");
					}
				}
				socket.close();

				outputResponse = outputResponse.replaceAll("</MessageId>",
						"</MessageId>/n<InputMessageId>" + inputMessageID + "</InputMessageId>");

				// DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("outputResponse
				// "+outputResponse);
				return outputResponse;

			}

			else {
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger
						.debug("SocketServerIp and SocketServerPort is not maintained " + "");
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("SocketServerIp is not maintained " + socketServerIP);
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug(" SocketServerPort is not maintained " + socketServerPort);
				return "Socket Details not maintained";
			}

		}

		catch (Exception e) {
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Exception Occured Mq_connection_CC" + e.getStackTrace());
			return "";
		} finally {
			try {
				if (out != null) {
					out.close();
					out = null;
				}
				if (socketInputStream != null) {

					socketInputStream.close();
					socketInputStream = null;
				}
				if (dout != null) {

					dout.close();
					dout = null;
				}
				if (din != null) {

					din.close();
					din = null;
				}
				if (socket != null) {
					if (!socket.isClosed())
						socket.close();
					socket = null;
				}

			}

			catch (Exception e) {
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger
						.debug("Final Exception Occured Mq_connection_CC" + e.getStackTrace());
				// printException(e);
			}
		}
	}

	public void updateDataInExtTable(String processInstanceId, String columnName, String value) {

		try {
			String sWhereClause = "Wi_Name = '" + processInstanceId + "'";
			String columnNames1 = columnName;
			String columnValues1 = "'" + value + "'";

			String extTableIPUpdateXml = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(),
					CommonConnection.getSessionID(DCC_AnnualFeeLog.DCC_AnnualFeeLogger, false), NG_DCC_EXTTABLE,
					columnNames1, columnValues1, sWhereClause);
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger
					.debug("Input XML for apUpdateInput for " + NG_DCC_EXTTABLE + " Table : " + extTableIPUpdateXml);

			String extTableOPUpdateXml = CommonMethods.WFNGExecute(extTableIPUpdateXml, CommonConnection.getJTSIP(),
					CommonConnection.getJTSPort(), 1);
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger
					.debug("Output XML for apUpdateInput for " + NG_DCC_EXTTABLE + " Table : " + extTableOPUpdateXml);

			XMLParser sXMLParserChild = new XMLParser(extTableOPUpdateXml);
			String StrMainCode = sXMLParserChild.getValueOf("MainCode");

			if (StrMainCode.equals("0"))
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger
						.debug("Successful in apUpdateInput the record in : " + NG_DCC_EXTTABLE);
			else {
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger
						.debug("Error in Executing apUpdateInput sOutputXML : " + sXMLParserChild);
				System.out.println("WMgetWorkItemCall failed: " + processInstanceId);
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("WMgetWorkItemCall failed: " + processInstanceId);
			}
		} catch (Exception e) {
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Exception in updating ext table: " + e.getMessage());
			System.out.println("Exception in updating ext table: " + e.getMessage());
		}
	}


	public String updateDataInExtTableflag(String processInstanceId, String columnName , String value,String cabinetName,String sessionId)
	{
		try {
			String sWhereClause = "Wi_Name = '" + processInstanceId + "'";
			value = "'"+ value+"'";
			String columnNames1 = columnName;
			String columnValues1 = value;
			//String sWhereClause = "Workitem_no = '" + processInstanceID + "' and Firco_ID = '" + objWorkList.getVal("FIRCO_ECN_No") + "'";

			String extTableIPUpdateXml = CommonMethods.apUpdateInput(cabinetName, sessionId, "NG_DCC_EXTTABLE", columnNames1, columnValues1, sWhereClause);
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Input XML for apUpdateInput for NG_DCC_EXTTABLE Table : " + extTableIPUpdateXml);

			String extTableOPUpdateXml = CommonMethods.WFNGExecute(extTableIPUpdateXml, CommonConnection.getJTSIP(),
					CommonConnection.getJTSPort(), 1);
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Output XML for apUpdateInput for NG_DCC_EXTTABLE Table : " + extTableOPUpdateXml);

			XMLParser sXMLParserChild = new XMLParser(extTableOPUpdateXml);
			String StrMainCode = sXMLParserChild.getValueOf("MainCode");

			if (StrMainCode.equals("0"))
			{
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Successful in apUpdateInput the record in : NG_DCC_EXTTABLE");
				return "Success";
			}
				
			else
			{
				DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Error in Executing apUpdateInput sOutputXML : " + sXMLParserChild);
				return null;
			}
		}catch(Exception e){
			DCC_AnnualFeeLog.DCC_AnnualFeeLogger.debug("Exception in updating ext table: " + e.getMessage());
			System.out.println("Exception in updating ext table: " + e.getMessage());
			return null;
		}
	}
}
