package com.newgen.DCC.Update_AssignCIF;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.newgen.DCC.DECTECHIntegration.DECTECHSystemIntegrationLog;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;

public class DCC_Service_Maintenance {

	private static NGEjbClient ngEjbClientCIFVer;
	private static Logger DCCServiceMaintenanceLog=null;
	DCC_Service_Maintenance(Logger DCCServiceMaintenanceLogName)
	{
		DCCServiceMaintenanceLog=DCCServiceMaintenanceLogName;
	}
	

	public String DCC_Service_Maintenance_Integration(String cabinetName, String sJtsIp, String iJtsPort, String sessionId, String processInstanceID, String ws_name,
				int integrationWaitTime,int socketConnectionTimeOut,  HashMap<String, String> socketDetailsMap)
		{
			String card_number="";
			
			StringBuilder finalMQCreateWIAttribute = new StringBuilder();
			try
			{
				
						
				//Assign CIF Call Here
				
				String DBQuery ="select Card_Number from NG_DCC_EXTTABLE with(nolock) WHERE Wi_Name='" + processInstanceID + "'";
				
				String extTabDataIPXML =CommonMethods.apSelectWithColumnNames(DBQuery, cabinetName, sessionId);
				DCCServiceMaintenanceLog.debug("extTabDataIPXML: " + extTabDataIPXML);
				String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,sJtsIp, iJtsPort, 1);
				DCCServiceMaintenanceLog.debug("extTabDataOPXML: " + extTabDataOPXML);
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
		            card_number = xmlGetWIDetails.getValueOf("Card_Number");
		            
		            StringBuilder stringBuilder = readRequestXmlSample();
					
					String requested_xml = stringBuilder.toString();
					
					/** Application Details Tag**/
					SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm");
					
				//	requested_xml = requested_xml.replace("",sdf1.format(new Date()));
					requested_xml = requested_xml.replace("$CardSerno$",card_number);
					
					StringBuilder finalString=new StringBuilder(requested_xml);
		            
		            //Socket Connection Starts Here
		            DCCServiceMaintenanceLog.debug("Socket Connection Starts Here");
		            HashMap<String, String> socketConnectionMap = socketConnectionDetails(cabinetName, sJtsIp, iJtsPort,
							sessionId);
		            String integrationStatus = socketConnection(cabinetName, CommonConnection.getUsername(), sessionId, sJtsIp,
							iJtsPort, processInstanceID, ws_name, 60, 65, socketConnectionMap, finalString);
		            
		            XMLParser xmlParserSocketDetails= new XMLParser(integrationStatus);
					DCCServiceMaintenanceLog.debug(" xmlParserSocketDetails : "+xmlParserSocketDetails);
				    String return_code = xmlParserSocketDetails.getValueOf("ReturnCode");
				    DCCServiceMaintenanceLog.debug("Return Code: "+return_code+ "WI: "+processInstanceID);
				    String return_desc = xmlParserSocketDetails.getValueOf("ReturnDesc");
				    DCCServiceMaintenanceLog.debug("return_desc : "+return_desc+ "WI: "+processInstanceID);
					
				    String MsgId ="";
				    if (integrationStatus.contains("<MessageId>"))
						MsgId = xmlParserSocketDetails.getValueOf("MessageId");
				    
				    /*if(return_code==null || !"0000".equalsIgnoreCase(return_code))
				    	return "Failure";*/
					
				    DCCServiceMaintenanceLog.debug("MsgId : "+MsgId+" for WI: "+processInstanceID);
					
				    return return_code +"~" +return_desc + "~"+ MsgId;
		        }  
		    }
			else
			{
				DCCServiceMaintenanceLog.debug("No record Avaialble at the given Queue : ");
			}
		} 
		catch (Exception e)
		{
			DCCServiceMaintenanceLog.debug("DCC service maintenance Call Exception : " + e.getMessage());
			return "Error";
		}
		return "Success";
	}
					
					
					private HashMap<String, String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort,
							String sessionID) {
						HashMap<String, String> socketDetailsMap = new HashMap<String, String>();
	
						try {
							DCCServiceMaintenanceLog.debug("Fetching Socket Connection Details.");
							System.out.println("Fetching Socket Connection Details.");
	
							String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'DCC' and CallingSource = 'Utility'";
	
							String socketDetailsInputXML = CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName,
									sessionID);
							DCCServiceMaintenanceLog.debug("Socket Details APSelect InputXML: " + socketDetailsInputXML);
	
							String socketDetailsOutputXML = CommonMethods.WFNGExecute(socketDetailsInputXML, sJtsIp, iJtsPort, 1);
							DCCServiceMaintenanceLog.debug("Socket Details APSelect OutputXML: " + socketDetailsOutputXML);
	
							XMLParser xmlParserSocketDetails = new XMLParser(socketDetailsOutputXML);
							String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
							DCCServiceMaintenanceLog.debug("SocketDetailsMainCode: " + socketDetailsMainCode);
	
							int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
							DCCServiceMaintenanceLog.debug("SocketDetailsTotalRecords: " + socketDetailsTotalRecords);
	
							if (socketDetailsMainCode.equalsIgnoreCase("0") && socketDetailsTotalRecords > 0) {
								String xmlDataSocketDetails = xmlParserSocketDetails.getNextValueOf("Record");
								xmlDataSocketDetails = xmlDataSocketDetails.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
	
								XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);
	
								String socketServerIP = xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
								DCCServiceMaintenanceLog.debug("SocketServerIP: " + socketServerIP);
								socketDetailsMap.put("SocketServerIP", socketServerIP);
	
								String socketServerPort = xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
								DCCServiceMaintenanceLog.debug("SocketServerPort " + socketServerPort);
								socketDetailsMap.put("SocketServerPort", socketServerPort);
	
								DCCServiceMaintenanceLog.debug("SocketServer Details found.");
								System.out.println("SocketServer Details found.");
	
							}
						} catch (Exception e) {
							DCCServiceMaintenanceLog
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
	
							DCCServiceMaintenanceLog.debug("userName " + username);
							DCCServiceMaintenanceLog.debug("SessionId " + sessionId);
	
							socketServerIP = socketDetailsMap.get("SocketServerIP");
							DCCServiceMaintenanceLog.debug("SocketServerIP " + socketServerIP);
							socketServerPort = Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
							DCCServiceMaintenanceLog.debug("SocketServerPort " + socketServerPort);
	
							if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort == 0)) {
	
								socket = new Socket(socketServerIP, socketServerPort);
								socket.setSoTimeout(connection_timeout * 1000);
								out = socket.getOutputStream();
								socketInputStream = socket.getInputStream();
								dout = new DataOutputStream(out);
								din = new DataInputStream(socketInputStream);
								DCCServiceMaintenanceLog.debug("Dout " + dout);
								DCCServiceMaintenanceLog.debug("Din " + din);
	
								outputResponse = "";
	
								inputRequest = getRequestXML(cabinetName, sessionId, processInstanceID, ws_name, username, sInputXML);
	
								if (inputRequest != null && inputRequest.length() > 0) {
									int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
									DCCServiceMaintenanceLog.debug("RequestLen: " + inputRequestLen + "");
									inputRequest = inputRequestLen + "##8##;" + inputRequest;
									DCCServiceMaintenanceLog
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
									DCCServiceMaintenanceLog.debug("OutputResponse: " + outputResponse);
	
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
	
								// DCCServiceMaintenanceLog.debug("outputResponse "+outputResponse);
								return outputResponse;
	
							}
	
							else {
								DCCServiceMaintenanceLog.debug("SocketServerIp and SocketServerPort is not maintained " + "");
								DCCServiceMaintenanceLog.debug("SocketServerIp is not maintained " + socketServerIP);
								DCCServiceMaintenanceLog.debug(" SocketServerPort is not maintained " + socketServerPort);
								return "Socket Details not maintained";
							}
	
						}
	
						catch (Exception e) {
							DCCServiceMaintenanceLog.debug("Exception Occured Mq_connection_CC" + e.getStackTrace());
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
								DCCServiceMaintenanceLog
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
						DCCServiceMaintenanceLog.debug("GetRequestXML: " + strBuff.toString());
						return strBuff.toString();
	
					}
			public static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort, int flag)
					throws IOException, Exception {
				DCCServiceMaintenanceLog.debug("In WF NG Execute : " + serverPort);
				try {
					if (serverPort.startsWith("33"))
						return WFCallBroker.execute(ipXML, jtsServerIP, Integer.parseInt(serverPort), 1);
					else
						return ngEjbClientCIFVer.makeCall(jtsServerIP, serverPort, "WebSphere", ipXML);
				} catch (Exception e) {
					DCCServiceMaintenanceLog.debug("Exception Occured in WF NG Execute : " + e.getMessage());
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
					DCCServiceMaintenanceLog.debug("Response APSelect InputXML: " + responseInputXML);
	
					int Loop_count = 0;
					do {
						String responseOutputXML = CommonMethods.WFNGExecute(responseInputXML, sJtsIp, iJtsPort, 1);
						DCCServiceMaintenanceLog.debug("Response APSelect OutputXML: " + responseOutputXML);
	
						XMLParser xmlParserSocketDetails = new XMLParser(responseOutputXML);
						String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
						DCCServiceMaintenanceLog.debug("ResponseMainCode: " + responseMainCode);
	
						int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
						DCCServiceMaintenanceLog.debug("ResponseTotalRecords: " + responseTotalRecords);
	
						if (responseMainCode.equals("0") && responseTotalRecords > 0) {
	
							String responseXMLData = xmlParserSocketDetails.getNextValueOf("Record");
							responseXMLData = responseXMLData.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");
	
							XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
							// DCCServiceMaintenanceLog.debug("ResponseXMLData:
							// "+responseXMLData);
	
							outputResponseXML = xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
							// DCCServiceMaintenanceLog.debug("OutputResponseXML:
							// "+outputResponseXML);
	
							if ("".equalsIgnoreCase(outputResponseXML)) {
								outputResponseXML = "Error";
							}
							break;
						}
						Loop_count++;
						Thread.sleep(1000);
					} while (Loop_count < integrationWaitTime);
					DCCServiceMaintenanceLog.debug("integrationWaitTime: " + integrationWaitTime);
	
				} catch (Exception e) {
					DCCServiceMaintenanceLog.debug("Exception occurred in outputResponseXML" + e.getMessage());
					outputResponseXML = "Error";
				}
	
				return outputResponseXML;
	
			}
			
			
			private static  StringBuilder readRequestXmlSample() {
				StringBuilder sb = new StringBuilder("");
				try {
					String fileLocation = new StringBuffer().append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append("DCC_Integration")
							.append(System.getProperty("file.separator")).append("DCCServiceMaintenance.txt").toString();
					BufferedReader sbf = new BufferedReader(new FileReader(fileLocation));
					
					String line = sbf.readLine();
					while (line != null) {
						sb.append(line);
						sb.append(System.lineSeparator());
						line = sbf.readLine();
					}
				} catch (FileNotFoundException e) {
					DCCServiceMaintenanceLog.error("In Reading DECTECH_Integration.txt : " + e.getMessage());
					e.printStackTrace();
				} catch (IOException e) {
					DCCServiceMaintenanceLog.error("In Reading DECTECH_Integration.txt : " + e.getMessage());
					e.printStackTrace();
				}
				return sb;
			}
					
	


}
