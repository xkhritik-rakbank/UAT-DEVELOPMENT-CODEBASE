package com.newgen.DPL.lms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.newgen.DPL.Digital_PL_CommomMethod;
import com.newgen.DPL.Digital_PL_Log;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.encryption.DataEncryption;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.omni.wf.util.excp.NGException;

import adminclient.OSASecurity;

public class LMS implements Runnable {

	static NGEjbClient ngEjbClient;
	private static org.apache.log4j.Logger logger;

	static Map<String, String> configPropertyMap = new HashMap<String, String>();
	static String cabinetName;
	static String serverIP;
	static String serverPort;
	static String port;
	static String sessionID;
	static String processDefID;
	static String queueId;
	static String ws_name;
	static int sleepTime = 60000; // Default
	static String sTables;
	static String userName;
	static String password;
	static int sessionCheckInt = 0;
	static int loopCount = 2;
	static int waitLoop = 50;
	static String lastProcessInstanceId = "";
	static String lastWorkItemId = "";
	static GenerateXML objXmlGen = null;
	static List<String> workItems;
	static List<String> lmsTableName = new ArrayList<>();
	static List<String> OracleTableName = new ArrayList<>();
	static String outXml;
	private static String ActivityType;
	private static String ProcessDefId;
	private static String ActivityName;
	private static String ActivityID;
	private static String WorkItemID;
	private static String entryDateTime;

	// Database variables for oracle
	static String OracleIP;
	static String OraclePort;
	static String UsernameOracle;
	static String PasswordOracle;
	static String OracleServicename;
	static String lmsFlag = "";
	static String lmstableName="";
	int sleepIntervalInMin=0;

	static User user = new User();
	Digital_PL_CommomMethod commomMethod = new Digital_PL_CommomMethod();

	public LMS() throws NGException {
		Digital_PL_Log.setLogger(getClass().getSimpleName());
		this.ngEjbClient = NGEjbClient.getSharedInstance();
		logger = Digital_PL_Log.getLogger(getClass().getSimpleName());
	}

	public void run() {

		try {
			System.out.println("Inside Run");
			int configReadStatus = readConfig();
			
			if(configReadStatus != 0){
				logger.debug("Error in config file");
			}
			//logger.debug("After reading config file");
			serverIP = configPropertyMap.get("JTSIP");
			serverPort = configPropertyMap.get("JTSPort");
			port = configPropertyMap.get("JTSPort");
			cabinetName = configPropertyMap.get("CabinetName");
			ws_name = configPropertyMap.get("WorkstepName");
			queueId = configPropertyMap.get("QueueId");
			sleepTime = Integer.parseInt(configPropertyMap.get("SleepTime"));
			processDefID = configPropertyMap.get("ProcessDefID");
			sTables = configPropertyMap.get("Tables");
			lmstableName = configPropertyMap.get("LMSTableName");
			
			userName = user.getUsername();
			password = user.getPassword();

			// database credentials
			OracleIP = configPropertyMap.get("OracleIP");
			OraclePort = configPropertyMap.get("OraclePort");
			UsernameOracle = configPropertyMap.get("UsernameOracle");

			PasswordOracle = decryptPassword(configPropertyMap.get("PasswordOracle"));//configPropertyMap.get("PasswordOracle");//decryptPassword(configPropertyMap.get("PasswordOracle"));
			OracleServicename = configPropertyMap.get("OracleServicename");
			//logger.debug("PasswordOracle:"+PasswordOracle);
			sleepIntervalInMin=Integer.parseInt(configPropertyMap.get("SleepIntervalInMin"));
			
			while (true) {
				
				String flag = dataTrasferFromDPLTOLMS();
				
				if("fail".equalsIgnoreCase(flag)){
					logger.debug("error in JFBC connection:");
				}
				else{
					logger.debug("successfully connected:");
				}
				System.out.println("No More workitems to Process, Sleeping!");
				Thread.sleep(sleepIntervalInMin * 60 * 1000);
			}
			
//			String decisionValue = "";
//			String attributesTag = "";
//			String updateCIFIntegrationStatus = "";
//			String ErrDesc = "";

			

			//if (!"fail".equalsIgnoreCase(flag))
				//dataTrasferSqlToOracle();

		} catch (Exception e) {
			logger.error("error occured:" + e.getStackTrace());
		}
	}

	/*** Read properties file ***/
	private int readConfig() {
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir") + File.separator + "ConfigFiles"
					+ File.separator + "DPL_LMS.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				configPropertyMap.put(name, p.getProperty(name));
			}
		} catch (Exception e) {
			return -1;
		}
		return 0;
	}

	/** data trasfer from DPL table to LMS Table for each wi **/
	public String dataTrasferFromDPLTOLMS() {
		//logger.debug("Inside dataTrasferFromDPLTOLMS ");
		Connection con = null;
		try {
			lastProcessInstanceId = "";
			lastWorkItemId = "";
			
			Class.forName("oracle.jdbc.OracleDriver");
			try{
				con = DriverManager.getConnection(
						"jdbc:oracle:thin:@" + OracleIP + ":" + OraclePort + ":" + OracleServicename, UsernameOracle,
						PasswordOracle);
			}catch(Exception e){
				logger.error(e.getMessage());
				logger.error("error while contion :"+e.getStackTrace());
			}
			
			Statement stmt = con.createStatement();
			//stmt.setQueryTimeout(300);
			if (con != null) {
				logger.info("Connection successful" + con);
			}

			workItems = loadWorkItems(queueId);
			try {
				for (String wi : workItems) {
					
					String query = "select LMS_Table,LMS_Columns,LMS_Query,Custom,Oracle_Table from "+lmstableName+"";
					logger.debug("query"+query);
					String inputXML = objXmlGen.ExecuteQuery_APSelectWithColumnNames(query, cabinetName, sessionID);
					try {
						outXml = CommonMethods.WFNGExecute(inputXML, serverIP, serverPort, 1);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						lmsFlag = "N";

					}
					XMLParser xmlParserLMSlist = new XMLParser(outXml);

					String MainCode = xmlParserLMSlist.getValueOf("MainCode");
					logger.debug("MainCode: " + MainCode);

					int LMSListCount = Integer.parseInt(xmlParserLMSlist.getValueOf("TotalRetrieved"));
					logger.debug("RetrievedCount Call: " + LMSListCount);
					System.out.println(LMSListCount);
					logger.debug("Number of workitems retrieved on : " + LMSListCount);

					if (MainCode.trim().equals("0") && LMSListCount > 0) {
						NGXmlList objWorkList = xmlParserLMSlist.createList("Records", "Record");
						// for (int i = 0; i < LMSListCount; i++) {
						for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)) {
							
							String LMSTable = objWorkList.getVal("LMS_Table");
							String LMSColumn = objWorkList.getVal("LMS_Columns");
							//String LMSSelectQuery = objWorkList.getVal("LMS_Query") + "'" + wi + "'";
							String LMSSelectQuery = objWorkList.getVal("LMS_Query");
							String oracleTable = objWorkList.getVal("Oracle_Table");
							
							logger.debug("LMSTable: " + LMSTable + ", LMSColumn:" + LMSColumn + ", LMSSelectQuery:"
									+ LMSSelectQuery);
							System.out.println("LMSTable:"+LMSTable);
							
							if(("NG_LMS_APPLICATION_DETAILS".equalsIgnoreCase(LMSTable))&& LMSSelectQuery.contains("#WINAME#")){
								LMSSelectQuery = LMSSelectQuery.replace("#WINAME#",wi );
							}else{
								LMSSelectQuery +=  "'" + wi + "'";
							}
							try {

								String InputXML = objXmlGen.ExecuteQuery_APSelectWithColumnNames(LMSSelectQuery,
										cabinetName, sessionID);
								List<ArrayList<String>> temp = new ArrayList<ArrayList<String>>();

								String OutXml = CommonMethods.WFNGExecute(InputXML, serverIP, serverPort, 1);

								List<String> listOfColValue = getColValue(OutXml);
								String dplvalue = "";
								String ColumnName="";
								
								try{
									String sWhereClause = "wi_name='" + wi + "'";
									String apdeleteOutputXML =CommonMethods.apDeleteInput(cabinetName, sessionID, LMSTable, sWhereClause);
									logger.debug("ibps apdeleteOutputXML: " + apdeleteOutputXML);
									String OutputdetailXml = CommonMethods.WFNGExecute(apdeleteOutputXML, serverIP, serverPort, 1);
									logger.debug("ibps OutputdetailXml: " + OutputdetailXml);
									XMLParser xmlDeletParserLMS = new XMLParser(OutputdetailXml);

									String MainCodelms = xmlDeletParserLMS.getValueOf("MainCode");
									logger.debug("ibps lms delete Main Code: " + MainCodelms);
								}catch(Exception e){
									logger.debug("error occured during deleting data from ibps LMS: " + e.getMessage());
								}
								if (listOfColValue.size() > 0) {
									for(String value:listOfColValue){
										if(value.contains("','")){
											dplvalue=value;
											String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionID, LMSColumn,
													dplvalue, LMSTable);
											logger.debug("APInsertInputXML: " + apInsertInputXML);

											String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, serverIP,
													serverPort, 1);
											logger.debug("APInsertOutputXML: " + apInsertInputXML);
											System.out.println(apInsertInputXML);
											System.out.println(apInsertOutputXML);
											XMLParser xmlInsertParserLMS = new XMLParser(outXml);

											String MainCodeInsertlms = xmlInsertParserLMS.getValueOf("MainCode");
											logger.debug("ibps lms Insert Main Code: " + MainCodeInsertlms);
											if(MainCodeInsertlms.equalsIgnoreCase("0")){
												logger.debug("ibps lms Insert successfull ");
												
											}
											
											MainCode = xmlParserLMSlist.getValueOf("MainCode");
											if (MainCode.trim().equals("0") && LMSListCount > 0) {
												ColumnName=LMSColumn.substring(8,LMSColumn.length());
												dplvalue=dplvalue.substring(31,dplvalue.length());
												System.out.println("dplvalue:"+dplvalue);
												String sql = "INSERT INTO " + oracleTable + "(" + ColumnName + ")  VALUES(" + dplvalue + ")";
												logger.info("insert query for oracle LMS tables: \n" + sql);
												System.out.println(sql);
												int returnflag = stmt.executeUpdate(sql);
												logger.info("returnflag..." + returnflag);
												logger.debug("lmsFlag:"+lmsFlag);
												if (!(returnflag == 1)) {
													lmsFlag = "N";
													break;
												} else {
													lmsFlag = "Y";

													// add code to update lms flag true in table
												}
												logger.debug("lmsFlag:"+lmsFlag);
											}else{
												
											}
											

										}
									}
								}

							} catch (Exception e) {
								e.printStackTrace();
								logger.debug("Exception Occurred in DCC CIF Verification : " + e);
								final Writer result = new StringWriter();
								final PrintWriter printWriter = new PrintWriter(result);
								e.printStackTrace(printWriter);
								logger.debug("Exception Occurred in DCC CIF Verification : " + result);
								logger.debug("Error occuried in Insertion: " + e.getMessage());
								lmsFlag = "N";
								break;
								
							}
						}
					} else {
						lmsFlag = "N";
					}
					String decisionValue = "";
					String ErrDesc = "";
					String attributesTag = "";
					logger.debug("lmsFlag:"+lmsFlag);
					if ("N".equalsIgnoreCase(lmsFlag)) {
						decisionValue = "Failed";
						ErrDesc = "LMS Pushing data Operation failed";
						attributesTag = "<Decision>" + decisionValue + "</Decision>";
						logger.debug("apselect for Fetching WI details failed");
//						doneWI(wi, decisionValue, ErrDesc, attributesTag,
//								sessionID);
						continue;
						//break;
					}else{
						decisionValue = "Success";
						ErrDesc = "LMS data Operation Successfully push";
						attributesTag = "<Decision>" + decisionValue + "</Decision>";
						logger.debug("apselect for Fetching WI details success");
						doneWI(wi, decisionValue, ErrDesc, attributesTag,
								sessionID);
						continue;
					}
				}
			} catch (Exception e) {
				con.close();
				logger.debug("Error occuried in foreach loop: " + e.getMessage());
				return "fail";
			}

		} catch (Exception e) {
			logger.debug("Error occuried " + e.getMessage());
			return "fail";
		}
		finally{
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
		}
		return "";
	}

	private void doneWI(String processInstanceID, String decisionValue,
			String ErrDesc, String attributesTag, String sessionId) {
		try {
			// Lock Workitem.
			String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID,
					WorkItemID);
			logger.debug("Output XML For getWorkItemInputXML: " + getWorkItemInputXML);
			String getWorkItemOutputXml = CommonMethods.WFNGExecute(getWorkItemInputXML, serverIP,
					serverPort, 1);
			logger.debug("Output XML For WmgetWorkItemCall: " + getWorkItemOutputXml);

			XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
			String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
			logger.debug("WmgetWorkItemCall Maincode:  " + getWorkItemMainCode);

			if (getWorkItemMainCode.trim().equals("0")) {
				logger.debug("WMgetWorkItemCall Successful: " + getWorkItemMainCode);

				String assignWorkitemAttributeInputXML = "<?xml version=\"1.0\"?><WMAssignWorkItemAttributes_Input>"
						+ "<Option>WMAssignWorkItemAttributes</Option>" + "<EngineName>" + CommonConnection.getCabinetName() + "</EngineName>"
						+ "<SessionId>" + sessionId + "</SessionId>" + "<ProcessInstanceId>" + processInstanceID
						+ "</ProcessInstanceId>" + "<WorkItemId>" + WorkItemID + "</WorkItemId>" + "<ActivityId>"
						+ ActivityID + "</ActivityId>" + "<ProcessDefId>" + ProcessDefId + "</ProcessDefId>"
						+ "<LastModifiedTime></LastModifiedTime>" + "<ActivityType>" + ActivityType + "</ActivityType>"
						+ "<complete>D</complete>" + "<AuditStatus></AuditStatus>" + "<Comments></Comments>"
						+ "<UserDefVarFlag>Y</UserDefVarFlag>" + "<Attributes>" + attributesTag + "</Attributes>"
						+ "</WMAssignWorkItemAttributes_Input>";

				logger.debug("InputXML for assignWorkitemAttribute Call: " + assignWorkitemAttributeInputXML);

				String assignWorkitemAttributeOutputXML = CommonMethods.WFNGExecute(assignWorkitemAttributeInputXML,
						 serverIP,serverPort, 1);

				logger.debug("OutputXML for assignWorkitemAttribute Call: " + assignWorkitemAttributeOutputXML);

				XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
				String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
				logger.debug("AssignWorkitemAttribute MainCode: " + assignWorkitemAttributeMainCode);

				if (assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0")) {
					logger.debug("AssignWorkitemAttribute Successful: " + assignWorkitemAttributeMainCode);
					if ("0".trim().equalsIgnoreCase("0")) {
						System.out.println(processInstanceID + "Complete Succesfully with status " + decisionValue);

						logger.debug("WorkItem moved to next Workstep.");

						SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
						SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

						Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
						String formattedEntryDatetime = outputDateFormat.format(entryDatetimeFormat);
						logger.debug("FormattedEntryDatetime: " + formattedEntryDatetime);

						Date actionDateTime = new Date();
						String formattedActionDateTime = outputDateFormat.format(actionDateTime);
						logger.debug("FormattedActionDateTime: " + formattedActionDateTime);

						// Insert in WIHistory Table.
						String columnNames="WI_NAME,Decision_Date_Time,WORKSTEP,USERNAME,DECISION,ENTRY_DATE_TIME,REMARKS";
						String columnValues="'"+processInstanceID+"','"+formattedActionDateTime+"','"+ActivityName+"','"
								+CommonConnection.getUsername()+"','"+decisionValue+"','"+formattedEntryDatetime+"','"+ErrDesc+"'";

						String apInsertInputXML = CommonMethods.apInsert(cabinetName, sessionId, columnNames,
								columnValues, "NG_DPL_GR_DECISION_HISTORY");
						logger.debug("APInsertInputXML: " + apInsertInputXML);

						String apInsertOutputXML = CommonMethods.WFNGExecute(apInsertInputXML, serverIP,
								serverPort, 1);
						logger.debug("APInsertOutputXML: " + apInsertInputXML);

						XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
						String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
						logger.debug("Status of apInsertMaincode  " + apInsertMaincode);

						logger.debug("Completed On " + ActivityName);

						if (apInsertMaincode.equalsIgnoreCase("0")) {
							logger.debug("ApInsert successful: " + apInsertMaincode);
							logger.debug("Inserted in WiHistory table successfully.");
						} else {
							logger.debug("ApInsert failed: " + apInsertMaincode);
						}
					} else {
						// completeWorkitemMaincode="";
						// logger.debug("WMCompleteWorkItem failed:
						// "+completeWorkitemMaincode);
					}
				} else if ("11".equalsIgnoreCase(assignWorkitemAttributeMainCode)) {

					sessionID = CommonConnection.getSessionID(logger, false);
					doneWI(processInstanceID, decisionValue, ErrDesc, attributesTag,
							sessionID);
				} else {
					assignWorkitemAttributeMainCode = "";
					logger.debug("AssignWorkitemAttribute failed: " + assignWorkitemAttributeMainCode);
				}
			} else {
				getWorkItemMainCode = "";
				logger.debug("WmgetWorkItem failed: " + getWorkItemMainCode);
			}
		}
		catch (Exception e) {
			logger.debug("DoneWI Exception: " + e.toString());
		}
	}
	
	private static List<ArrayList<String>> getDataFromDBMap(String query, String cabinetName, String sessionID,
			String jtsIP, String jtsPort) {
		try {
			logger.debug("Inside function getDataFromDB");
			logger.debug("getDataFromDB query is: " + query);
			String InputXML = objXmlGen.ExecuteQuery_APSelectWithColumnNames(query, cabinetName, sessionID);
			List<ArrayList<String>> temp = new ArrayList<>();

			String OutXml = CommonMethods.WFNGExecute(InputXML, jtsIP, jtsPort, 1);
			OutXml = OutXml.replaceAll("&", "#andsymb#");
			Document recordDoc1 = getDocument(OutXml);
			NodeList records1 = recordDoc1.getElementsByTagName("Record");
			if (records1.getLength() > 0) {
				for (int i = 0; i < records1.getLength(); i++) {
					Node n = records1.item(i);
					// Map<String, String> t = new LinkedHashMap<String,
					// String>();
					ArrayList<String> GridData = new ArrayList<>();
					if (n.hasChildNodes()) {
						NodeList child = n.getChildNodes();
						for (int j = 0; j < child.getLength(); j++) {
							Node n1 = child.item(j);
							String column = n1.getNodeName();
							String value = n1.getTextContent().replaceAll("#andsymb#", "&");
							if (null != value && !"null".equalsIgnoreCase(value) && !"".equals(value)
									&& !"\n\t\t\t".equals(value)) {
								logger.debug("getDataFromDBMap Setting value of " + column + " as " + value);
								// t.put(column, value);
								GridData.add(value);
							} else {
								logger.debug("getDataFromDBMap Setting value of " + column + " as blank");
								// t.put(column, "");
								// GridData.add("");
							}
						}
					}
					// temp.add(t);
					temp.add(GridData);
				}
			}
			return temp;
		} catch (Exception ex) {
			logger.debug("Exception in getDataFromDBMap method + " + ex.getMessage());
			return null;
		}
	}

	private List<String> getColValue(String OutXml)
			throws ParserConfigurationException, SAXException, IOException {

		//List<List<String>> LMSColValue = new ArrayList<>();
		List<String> LMSColValue = new ArrayList<>();
		Document recordDoc = getDocument(OutXml);
		NodeList records = recordDoc.getElementsByTagName("Record");
		List<String> columnName = new ArrayList<>();
		List<String> columnValue = new ArrayList<>();

		for (int i = 0; i < records.getLength(); i++) {
			StringBuilder col = new StringBuilder();
			StringBuilder val = new StringBuilder();
			Node node = records.item(i);
			NodeList child_node = node.getChildNodes();
			for (int rec = 0; rec < child_node.getLength(); rec++) {
				if (!child_node.item(rec).getNodeName().equalsIgnoreCase("#text")
						&& !child_node.item(rec).getNodeName().equalsIgnoreCase("wi_name")) {

					if (rec == child_node.getLength() - 2) {
						col.append(child_node.item(rec).getNodeName());
						val.append("'" + child_node.item(rec).getTextContent() + "'");


					} else {
						col.append(child_node.item(rec).getNodeName() + ",");
						val.append("'" + child_node.item(rec).getTextContent() + "',");
						
					}
				}
			}

			if (col.charAt(col.length() - 1) == ',' || val.charAt(val.length() - 1) == ',') {
				col.deleteCharAt(col.length() - 1);
				val.deleteCharAt(val.length() - 1);
			}
			logger.info("col values" + col);
			logger.info("val values" + val);
			
			LMSColValue.add(col.toString());
			LMSColValue.add(val.toString());
		}

		return LMSColValue;

	}

	private static List<String> loadWorkItems(String queueID) throws NumberFormatException, IOException, Exception {

		int mainCode = 0;
		List<String> workItemList = new ArrayList<>();
		try {
			String workItemListInputXML = "";
			String workItemListOutputXML = "";
			sessionCheckInt = 0;
			sessionID = CommonConnection.getSessionID(logger, false);
			while (sessionCheckInt < loopCount) {
				workItemListInputXML = CommonMethods.fetchWorkItemsInput(cabinetName, sessionID, queueID);
				logger.debug("InputXML for fetchWorkList Call: " + workItemListInputXML);

				try {
					String fetchWorkitemListOutputXML = CommonMethods.WFNGExecute(workItemListInputXML, serverIP,
							serverPort, 1);
					logger.debug("WMFetchWorkList OutputXML: " + fetchWorkitemListOutputXML);

					XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

					String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
					logger.debug("FetchWorkItemListMainCode: " + fetchWorkItemListMainCode);

					int fetchWorkitemListCount = Integer
							.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
					logger.debug("RetrievedCount for WMFetchWorkList Call: " + fetchWorkitemListCount);

					if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0) {
						lmsFlag = "Y";
						for (int i = 0; i < fetchWorkitemListCount; i++) {
							String fetchWorkItemlistData = xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
							fetchWorkItemlistData = fetchWorkItemlistData.replaceAll("[ ]+>", ">").replaceAll("<[ ]+",
									"<");

							logger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: " + fetchWorkItemlistData);
							XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

							String processInstanceID = xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
							logger.debug("Current ProcessInstanceID: " + processInstanceID);
							
							logger.debug("Processing Workitem: " + processInstanceID);
							System.out.println("\nProcessing Workitem: " + processInstanceID);

							WorkItemID = xmlParserfetchWorkItemData.getValueOf("WorkItemId");
							logger.debug("Current WorkItemID: " + WorkItemID);

							entryDateTime = xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
							logger.debug("Current EntryDateTime: " + entryDateTime);

							ActivityName = xmlParserfetchWorkItemData.getValueOf("ActivityName");
							logger.debug("ActivityName: " + ActivityName);

							ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
							logger.debug("ActivityID: " + ActivityID);
							ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
							logger.debug("ActivityType: " + ActivityType);
							ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
							logger.debug("ProcessDefId: " + ProcessDefId);

							workItemList.add(processInstanceID);
						}
					}

				} catch (Exception e) {
					logger.info("Exception in Execute : " + e);
					sessionCheckInt++;
					waiteloopExecute(waitLoop);
					sessionID = CommonConnection.getSessionID(logger, false);
					continue;
				}
				logger.info("workItemListOutputXML aa:" + workItemListOutputXML);
				sessionCheckInt++;
				if (CommonMethods.getTagValues(workItemListOutputXML, "MainCode").equalsIgnoreCase("11")) {
					sessionID = CommonConnection.getSessionID(logger, false);
				} else {
					sessionCheckInt++;
					break;
				}
			}
		} catch (Exception ex) {
			logger.error("Error = " + ex);
		}

		return workItemList;

	}

	public static void waiteloopExecute(long wtime) {
		try {
			for (int i = 0; i < 10; i++) {
				Thread.yield();
				Thread.sleep(wtime / 10);
			}
		} catch (InterruptedException e) {
			logger.error(e.toString());
		}
	}

	public static Document getDocument(String xml) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new InputSource(new StringReader(xml)));
		return doc;
	}

	private static String decryptPassword(String pass) {
		//logger.debug("Inside decryptPassword");
		try{
			int len = pass.length();
			byte[] data = new byte[len / 2];
			for (int i = 0; i < len; i += 2) {
				data[i / 2] = (byte) ((Character.digit(pass.charAt(i), 16) << 4) + Character.digit(pass.charAt(i + 1), 16));
			}
			String password = OSASecurity.decode(data, "UTF-8");
			return password;
		}catch(Exception e){
			logger.debug("Error decryptPassword"+e.getMessage());
		}
		return "";
	}

}
