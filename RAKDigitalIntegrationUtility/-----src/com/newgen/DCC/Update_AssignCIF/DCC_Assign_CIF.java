package com.newgen.DCC.Update_AssignCIF;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.newgen.DCC.Notify.DCCNotifyLog;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;

public class DCC_Assign_CIF
{
	private static NGEjbClient ngEjbClientCIFVer;
	private static Logger DCC_AssignCIFLog=null;
	DCC_Assign_CIF(Logger DCC_AssignCIFLogName)
	{
		DCC_AssignCIFLog=DCC_AssignCIFLogName;
	}
	

	public String DCC_Assign_CIF_Integration(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String processInstanceID, String ws_name,
				int integrationWaitTime,int socketConnectionTimeOut,  HashMap<String, String> socketDetailsMap)
		{
			String ProspectID="";
			String ProductType="";
			String SubProductType="";
			String ProdCategory="";
			String Emirates="";
			StringBuilder finalMQCreateWICall = new StringBuilder();
			StringBuilder finalMQCreateWIHeader = new StringBuilder();
			StringBuilder finalMQCreateWIAttribute = new StringBuilder();
			try
			{
				
						
				//Assign CIF Call Here
				
				String DBQuery ="SELECT a.Wi_Name AS 'WI_NAME',a.Prospect_id AS 'ProspectID',a.Product AS 'ProductType',a.Card_Product_Sub_Type AS 'ProductSubType',"+
								"'C' AS 'ProdCategory','DXB' AS 'Emirate' from NG_DCC_EXTTABLE a WHERE a.Wi_Name='" + processInstanceID + "'";
				
				String extTabDataIPXML =CommonMethods.apSelectWithColumnNames(DBQuery, cabinetName, sessionId);
				DCC_AssignCIFLog.debug("extTabDataIPXML: " + extTabDataIPXML);
				String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,sJtsIp, iJtsPort, 1);
				DCC_AssignCIFLog.debug("extTabDataOPXML: " + extTabDataOPXML);
				// using xml parser to pass the output data in desired format 
			    XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
			    // total values retrieved > 0 is a check
			    int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
			   // Main code we get if the ap select call is triggered success.
			    if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
		        {
			    	for (int k = 0; k < iTotalrec; k++) {
			    	String xmlDataExtTab = xmlParserData.getNextValueOf("Record");
		            xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
		            // replace the spcl char above.
		            XMLParser xmlGetWIDetails = new XMLParser(xmlDataExtTab);
		            ProspectID = xmlGetWIDetails.getValueOf("ProspectID");
		            DCC_AssignCIFLog.debug("ProspectID: " + ProspectID);
		            ProductType = xmlGetWIDetails.getValueOf("ProductType");
		            DCC_AssignCIFLog.debug("ProductType: " + ProductType);
		            SubProductType = xmlGetWIDetails.getValueOf("ProductSubType");
		            DCC_AssignCIFLog.debug("SubProductType: " + SubProductType);
		            ProdCategory = xmlGetWIDetails.getValueOf("ProdCategory");
		            DCC_AssignCIFLog.debug("ProdCategory: " + ProdCategory);
		            Emirates = xmlGetWIDetails.getValueOf("Emirate");
		            DCC_AssignCIFLog.debug("Emirates: " + Emirates);
		            //Change for by om.tiwari 08/10/2022
		            if(ProductType!=null && !"".equalsIgnoreCase(ProductType))
		            	ProdCategory=ProductType.substring(0,1);
		            //Creation of MW Input
		            DCC_AssignCIFLog.debug("Input XML Starts Here ");
		            finalMQCreateWIHeader.append("\n<EE_EAI_MESSAGE>");
		            finalMQCreateWIHeader.append("\n	<EE_EAI_HEADER>");
		            finalMQCreateWIHeader.append("\n		<MsgFormat>ASSIGN_CIF</MsgFormat>");
		            finalMQCreateWIHeader.append("\n		<MsgVersion>0000</MsgVersion>");
		            finalMQCreateWIHeader.append("\n		<RequestorChannelId>CAS</RequestorChannelId>");
		            finalMQCreateWIHeader.append("\n		<RequestorUserId>RAKUSER</RequestorUserId>");
		            finalMQCreateWIHeader.append("\n		<RequestorLanguage>E</RequestorLanguage>");
		            finalMQCreateWIHeader.append("\n		<RequestorSecurityInfo>secure</RequestorSecurityInfo>");
		            finalMQCreateWIHeader.append("\n		<ReturnCode>0000</ReturnCode>");
		            finalMQCreateWIHeader.append("\n		<ReturnDesc>Successful</ReturnDesc>");
		            finalMQCreateWIHeader.append("\n		<MessageId>143300521454466445</MessageId>");
		            finalMQCreateWIHeader.append("\n		<Extra1>REQ||LAXMANRET.LAXMANRET</Extra1>");
		            finalMQCreateWIHeader.append("\n		<Extra2>2015-05-30T22:30:14.544+05:30</Extra2>");
		            finalMQCreateWIHeader.append("\n	</EE_EAI_HEADER>");
		            finalMQCreateWIAttribute.append("\n	<AssignCIFIdRequest>");
		            finalMQCreateWIAttribute.append("\n		<BankId>RAK</BankId>");
		            finalMQCreateWIAttribute.append("\n		<ProspectId>"+ProspectID+"</ProspectId>");
		           // finalMQCreateWIAttribute.append("\n		<ProductType>"+ProductType+"</ProductType>");
		            //--Deepak: Card hardcoded for ProductType as per DEH team confirmation 01/10/22
		            finalMQCreateWIAttribute.append("\n		<ProductType>CARD</ProductType>");
		            finalMQCreateWIAttribute.append("\n		<ProductSubType>"+SubProductType+"</ProductSubType>");
		            finalMQCreateWIAttribute.append("\n		<ProductCategory>"+ProdCategory+"</ProductCategory>");
		            finalMQCreateWIAttribute.append("\n		<Emirate>"+Emirates+"</Emirate>");
		            finalMQCreateWIAttribute.append("\n	</AssignCIFIdRequest>");
		            finalMQCreateWIAttribute.append("\n</EE_EAI_MESSAGE> ");
		            
		            finalMQCreateWICall = finalMQCreateWIHeader.append(finalMQCreateWIAttribute);
		            DCC_AssignCIFLog.debug("Assign CIF Input XML : " + finalMQCreateWICall);
		            
		            //Socket Connection Starts Here
		            DCC_AssignCIFLog.debug("Socket Connection Starts Here");
		            HashMap<String, String> socketConnectionMap = socketConnectionDetails(cabinetName, sJtsIp, iJtsPort,
							sessionId);
		            String integrationStatus = socketConnection(cabinetName, CommonConnection.getUsername(), sessionId, sJtsIp,
							iJtsPort, processInstanceID, ws_name, 60, 65, socketConnectionMap, finalMQCreateWICall);
		            
		            XMLParser xmlParserSocketDetails= new XMLParser(integrationStatus);
					DCC_AssignCIFLog.debug(" xmlParserSocketDetails : "+xmlParserSocketDetails);
				    String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
				    DCC_AssignCIFLog.debug("Return Code: "+return_code+ "WI: "+processInstanceID);
				    String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
				    DCC_AssignCIFLog.debug("return_desc : "+return_desc+ "WI: "+processInstanceID);
					
				    String MsgId ="";
				    if (integrationStatus.contains("<MessageId>"))
						MsgId = xmlParserSocketDetails.getValueOf("MessageId");
				    
				    /*if(return_code==null || !"0000".equalsIgnoreCase(return_code))
				    	return "Failure";*/
					
				    DCC_AssignCIFLog.debug("MsgId : "+MsgId+" for WI: "+processInstanceID);
					
				    return return_code +"~" +return_desc + "~"+ MsgId;
		        }  
		    }
			else
			{
				DCC_AssignCIFLog.debug("No record Avaialble at the given Queue : ");
			}
		} 
		catch (Exception e)
		{
			DCC_AssignCIFLog.debug("Assign CIF Call Exception : " + e.getMessage());
			return "Error";
		}
		return "Success";
	}
					
					
					private HashMap<String, String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort,
							String sessionID) {
						HashMap<String, String> socketDetailsMap = new HashMap<String, String>();
	
						try {
							DCC_AssignCIFLog.debug("Fetching Socket Connection Details.");
							System.out.println("Fetching Socket Connection Details.");
	
							String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'DCC' and CallingSource = 'Utility'";
	
							String socketDetailsInputXML = CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName,
									sessionID);
							DCC_AssignCIFLog.debug("Socket Details APSelect InputXML: " + socketDetailsInputXML);
	
							String socketDetailsOutputXML = CommonMethods.WFNGExecute(socketDetailsInputXML, sJtsIp, iJtsPort, 1);
							DCC_AssignCIFLog.debug("Socket Details APSelect OutputXML: " + socketDetailsOutputXML);
	
							XMLParser xmlParserSocketDetails = new XMLParser(socketDetailsOutputXML);
							String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
							DCC_AssignCIFLog.debug("SocketDetailsMainCode: " + socketDetailsMainCode);
	
							int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
							DCC_AssignCIFLog.debug("SocketDetailsTotalRecords: " + socketDetailsTotalRecords);
	
							if (socketDetailsMainCode.equalsIgnoreCase("0") && socketDetailsTotalRecords > 0) {
								String xmlDataSocketDetails = xmlParserSocketDetails.getNextValueOf("Record");
								xmlDataSocketDetails = xmlDataSocketDetails.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
	
								XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);
	
								String socketServerIP = xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
								DCC_AssignCIFLog.debug("SocketServerIP: " + socketServerIP);
								socketDetailsMap.put("SocketServerIP", socketServerIP);
	
								String socketServerPort = xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
								DCC_AssignCIFLog.debug("SocketServerPort " + socketServerPort);
								socketDetailsMap.put("SocketServerPort", socketServerPort);
	
								DCC_AssignCIFLog.debug("SocketServer Details found.");
								System.out.println("SocketServer Details found.");
	
							}
						} catch (Exception e) {
							DCC_AssignCIFLog
									.debug("Exception in getting Socket Connection Details: " + e.getMessage());
							System.out.println("Exception in getting Socket Connection Details: " + e.getMessage());
						}
	
						return socketDetailsMap;
					}
					
					
					
					String socketConnection(String cabinetName, String username, String sessionId, String sJtsIp, String iJtsPort,
							String processInstanceID, String ws_name, int connection_timeout, int integrationWaitTime,
							HashMap<String, String> socketDetailsMap, StringBuilder sInputXML) {
	
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
	
							DCC_AssignCIFLog.debug("userName " + username);
							DCC_AssignCIFLog.debug("SessionId " + sessionId);
	
							socketServerIP = socketDetailsMap.get("SocketServerIP");
							DCC_AssignCIFLog.debug("SocketServerIP " + socketServerIP);
							socketServerPort = Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
							DCC_AssignCIFLog.debug("SocketServerPort " + socketServerPort);
	
							if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort == 0)) {
	
								socket = new Socket(socketServerIP, socketServerPort);
								socket.setSoTimeout(connection_timeout * 1000);
								out = socket.getOutputStream();
								socketInputStream = socket.getInputStream();
								dout = new DataOutputStream(out);
								din = new DataInputStream(socketInputStream);
								DCC_AssignCIFLog.debug("Dout " + dout);
								DCC_AssignCIFLog.debug("Din " + din);
	
								outputResponse = "";
	
								inputRequest = getRequestXML(cabinetName, sessionId, processInstanceID, ws_name, username, sInputXML);
	
								if (inputRequest != null && inputRequest.length() > 0) {
									int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
									DCC_AssignCIFLog.debug("RequestLen: " + inputRequestLen + "");
									inputRequest = inputRequestLen + "##8##;" + inputRequest;
									DCC_AssignCIFLog
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
									DCC_AssignCIFLog.debug("OutputResponse: " + outputResponse);
	
									if (!"".equalsIgnoreCase(outputResponse))
	
										outputResponse = getResponseXML(cabinetName, sJtsIp, iJtsPort, sessionId, processInstanceID,
												outputResponse, integrationWaitTime);
	
									if (outputResponse.contains("&lt;")) {
										outputResponse = outputResponse.replaceAll("&lt;", "<");
										outputResponse = outputResponse.replaceAll("&gt;", ">");
									}
								}
								socket.close();
	
								outputResponse = outputResponse.replaceAll("</MessageId>",
										"</MessageId>/n<InputMessageId>" + inputMessageID + "</InputMessageId>");
	
								// DCC_AssignCIFLog.debug("outputResponse "+outputResponse);
								return outputResponse;
	
							}
	
							else {
								DCC_AssignCIFLog.debug("SocketServerIp and SocketServerPort is not maintained " + "");
								DCC_AssignCIFLog.debug("SocketServerIp is not maintained " + socketServerIP);
								DCC_AssignCIFLog.debug(" SocketServerPort is not maintained " + socketServerPort);
								return "Socket Details not maintained";
							}
	
						}
	
						catch (Exception e) {
							DCC_AssignCIFLog.debug("Exception Occured Mq_connection_CC" + e.getStackTrace());
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
								DCC_AssignCIFLog
										.debug("Final Exception Occured Mq_connection_CC" + e.getStackTrace());
								// printException(e);
							}
						}
	
					}
	
					private String getRequestXML(String cabinetName, String sessionId, String processInstanceID, String ws_name,
							String userName, StringBuilder sInputXML) {
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
						DCC_AssignCIFLog.debug("GetRequestXML: " + strBuff.toString());
						return strBuff.toString();
	
					}
			public static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort, int flag)
					throws IOException, Exception {
				DCC_AssignCIFLog.debug("In WF NG Execute : " + serverPort);
				try {
					if (serverPort.startsWith("33"))
						return WFCallBroker.execute(ipXML, jtsServerIP, Integer.parseInt(serverPort), 1);
					else
						return ngEjbClientCIFVer.makeCall(jtsServerIP, serverPort, "WebSphere", ipXML);
				} catch (Exception e) {
					DCC_AssignCIFLog.debug("Exception Occured in WF NG Execute : " + e.getMessage());
					e.printStackTrace();
					return "Error";
				}
			}
	
			private String getResponseXML(String cabinetName, String sJtsIp, String iJtsPort, String sessionId,
					String processInstanceID, String message_ID, int integrationWaitTime) {
	
				String outputResponseXML = "";
				try {
					String QueryString = "select OUTPUT_XML from NG_DCC_XMLLOG_HISTORY with (nolock) where " + "MESSAGE_ID ='"
							+ message_ID + "' and WI_NAME = '" + processInstanceID + "'";
	
					String responseInputXML = CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, sessionId);
					DCC_AssignCIFLog.debug("Response APSelect InputXML: " + responseInputXML);
	
					int Loop_count = 0;
					do {
						String responseOutputXML = CommonMethods.WFNGExecute(responseInputXML, sJtsIp, iJtsPort, 1);
						DCC_AssignCIFLog.debug("Response APSelect OutputXML: " + responseOutputXML);
	
						XMLParser xmlParserSocketDetails = new XMLParser(responseOutputXML);
						String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
						DCC_AssignCIFLog.debug("ResponseMainCode: " + responseMainCode);
	
						int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
						DCC_AssignCIFLog.debug("ResponseTotalRecords: " + responseTotalRecords);
	
						if (responseMainCode.equals("0") && responseTotalRecords > 0) {
	
							String responseXMLData = xmlParserSocketDetails.getNextValueOf("Record");
							responseXMLData = responseXMLData.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
	
							XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
							// DCC_AssignCIFLog.debug("ResponseXMLData:
							// "+responseXMLData);
	
							outputResponseXML = xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
							// DCC_AssignCIFLog.debug("OutputResponseXML:
							// "+outputResponseXML);
	
							if ("".equalsIgnoreCase(outputResponseXML)) {
								outputResponseXML = "Error";
							}
							break;
						}
						Loop_count++;
						Thread.sleep(1000);
					} while (Loop_count < integrationWaitTime);
					DCC_AssignCIFLog.debug("integrationWaitTime: " + integrationWaitTime);
	
				} catch (Exception e) {
					DCC_AssignCIFLog.debug("Exception occurred in outputResponseXML" + e.getMessage());
					outputResponseXML = "Error";
				}
	
				return outputResponseXML;
	
			}	
					
	
}
