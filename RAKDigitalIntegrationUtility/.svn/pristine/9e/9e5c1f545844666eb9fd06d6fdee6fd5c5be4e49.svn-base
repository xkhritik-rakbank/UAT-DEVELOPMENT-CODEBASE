/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM
Application				: RAK BPM Utility
Module					: RAOP Status
File Name				: RAOPStatus.java
Author 					: Shubham Gupta
Date (DD/MM/YYYY)		: 15/06/2019

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/


package com.newgen.DAO.Prime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import com.newgen.DAO.AWB.AWB_Gen_file;

/*
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
*/
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;



public class Prime_cbs implements Runnable
{

	private static NGEjbClient ngEjbClientCIFVer;
	Date now=null;
	public String sdate="";
	public static String sourceDestinaton = "";
	String ExcelFilePath="";
	String ExcelSuccessPath="";
	String ExcelFailPath="";
	String ExcelInputPath="";
	String ExcelInprogressPath="";
	String prime_cloumn_name="";
	
	String CBSfilepath="";
	String CbsSuccess="";
	String CbsFail="";
	String CbsInput="";
	String CbsInprogresspath="";
	String cbs_Col_name="";
	
	int socketConnectionTimeout=0;
	int integrationWaitTime=0;
	int sleepIntervalInMin=0;
	public static int waitLoop=50;
	public static int loopCount=50;
	public static String fromMailID="";
	public static String toMailID = "";
	public static String mailSubject = "";
	public static String MailStr="";
	public static String jtsIP = "";
	public static String jtsPort = "";
	public static String ProcessDefId="";
	private String sessionID = "";
	
	//private static String excelFilePath;
   // private File exceFile;
    private Workbook excelWorkBook;
    private String excelSheetName;
    private Sheet excelSheet;
	
	public  String TimeStamp="";
	public String newFilename=null;

	static Map<String, String> DAOPrimeConfigParamMap= new HashMap<String, String>();

	@Override
	public void run()
	{
		
		String cabinetName = "";
		String queueID = "";
		int socketConnectionTimeout=0;
		int integrationWaitTime=0;
		int sleepIntervalInMin=0;


		try
		{
			DAO_prime.setLogger();
			ngEjbClientCIFVer = NGEjbClient.getSharedInstance();

			DAO_prime.DAOPrimeLogger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			DAO_prime.DAOPrimeLogger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				DAO_prime.DAOPrimeLogger.error("Could not Read Config Properties [DAO_prime]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			DAO_prime.DAOPrimeLogger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			DAO_prime.DAOPrimeLogger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			DAO_prime.DAOPrimeLogger.debug("JTSPORT: " + jtsPort);

			queueID = DAOPrimeConfigParamMap.get("queueID");
			DAO_prime.DAOPrimeLogger.debug("QueueID: " + queueID);

			socketConnectionTimeout=Integer.parseInt(DAOPrimeConfigParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			DAO_prime.DAOPrimeLogger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(DAOPrimeConfigParamMap.get("INTEGRATION_WAIT_TIME"));
			DAO_prime.DAOPrimeLogger.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(DAOPrimeConfigParamMap.get("SleepIntervalInMin"));
			DAO_prime.DAOPrimeLogger.debug("SleepIntervalInMin: "+sleepIntervalInMin);
			
			ExcelFilePath=DAOPrimeConfigParamMap.get("ExcelFilePath");
			DAO_prime.DAOPrimeLogger.debug("ExcelFilePath: "+ExcelFilePath);
			
			ExcelFailPath=ExcelFilePath+"\\"+DAOPrimeConfigParamMap.get("ExcelFailPath");
			DAO_prime.DAOPrimeLogger.debug("ExcelFailPath: "+ExcelFailPath);
			
			ExcelInputPath=ExcelFilePath+"\\"+DAOPrimeConfigParamMap.get("ExcelInputPath");
			DAO_prime.DAOPrimeLogger.debug("ExcelInputPath: "+ExcelInputPath);
			
			ExcelSuccessPath=ExcelFilePath+"\\"+DAOPrimeConfigParamMap.get("ExcelSuccessPath");
			DAO_prime.DAOPrimeLogger.debug("ExcelSuccessPath: "+ExcelSuccessPath);		
			
			ExcelInprogressPath=ExcelFilePath+"\\"+DAOPrimeConfigParamMap.get("ExcelInprogressPath");
			DAO_prime.DAOPrimeLogger.debug("ExcelInprogressPath: "+ExcelInprogressPath);
			
			prime_cloumn_name=DAOPrimeConfigParamMap.get("prime_cloumn_name");
			DAO_prime.DAOPrimeLogger.debug("prime_cloumn_name: "+prime_cloumn_name);
			
			cbs_Col_name=DAOPrimeConfigParamMap.get("cbs_Col_name");
			DAO_prime.DAOPrimeLogger.debug("cbs_Col_name: "+cbs_Col_name);
			
			CBSfilepath=DAOPrimeConfigParamMap.get("CBSfilepath");
			DAO_prime.DAOPrimeLogger.debug("CBSfilepath: "+CBSfilepath);
			
			CbsSuccess=CBSfilepath+"\\"+DAOPrimeConfigParamMap.get("CbsSuccess");
			DAO_prime.DAOPrimeLogger.debug("CbsSuccess: "+CbsSuccess);
			
			CbsFail=CBSfilepath+"\\"+DAOPrimeConfigParamMap.get("CbsFail");
			DAO_prime.DAOPrimeLogger.debug("CbsFail: "+CbsFail);
			
			CbsInput=CBSfilepath+"\\"+DAOPrimeConfigParamMap.get("CbsInput");
			DAO_prime.DAOPrimeLogger.debug("CbsInput: "+CbsInput);
			
			CbsInprogresspath=CBSfilepath+"\\"+DAOPrimeConfigParamMap.get("CbsInprogresspath");
			DAO_prime.DAOPrimeLogger.debug("CbsInprogresspaths: "+CbsInprogresspath);

			fromMailID=DAOPrimeConfigParamMap.get("fromMailID");
			DAO_prime.DAOPrimeLogger.debug("fromMailID: "+fromMailID);
			
			toMailID=DAOPrimeConfigParamMap.get("toMailID");
			DAO_prime.DAOPrimeLogger.debug("toMailID: "+toMailID);
			
			mailSubject=DAOPrimeConfigParamMap.get("mailSubject");
			DAO_prime.DAOPrimeLogger.debug("mailSubject: "+mailSubject);
			
			MailStr=DAOPrimeConfigParamMap.get("MailStr");
			DAO_prime.DAOPrimeLogger.debug("MailStr: "+MailStr);
			
			ProcessDefId=DAOPrimeConfigParamMap.get("ProcessDefId");
			DAO_prime.DAOPrimeLogger.debug("ProcessDefId: "+ProcessDefId);
			
			sessionID = CommonConnection.getSessionID(DAO_prime.DAOPrimeLogger, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				DAO_prime.DAOPrimeLogger.debug("Could Not Connect to Server!");
			}
			else
			{
				while (true) {
					DAO_prime.DAOPrimeLogger.debug("Session ID found: " + sessionID);
					//HashMap<String, String> socketDetailsMap = socketConnectionDetails(cabinetName, jtsIP, jtsPort, sessionID);
					
					DAO_prime.setLogger();
					DAO_prime.DAOPrimeLogger.debug("DAO Prime cbs AWB ...123.");
					
					// Hritik 23.6.22 - Prime code start - 
					DAO_ReadPrimefile(cabinetName, jtsIP, jtsPort,sessionID);
					System.out.println("No More Prime TXT files to Process, Sleeping...!");
					
					// Hritik 23.6.22 - CBS code start - 
					DAO_ReadCBSfile(cabinetName, jtsIP, jtsPort,sessionID);
					System.out.println("No More CBS TXT file to Process, Sleeping...!");
					
					/*// Prime code start DCC- 
					DCC_ReadPrimefile(cabinetName, jtsIP, jtsPort,sessionID);
					System.out.println("No More Prime excel files to Process, Sleeping!");
					*/
					
					// Hritik 25.6.22 - AWB code start - 
					System.out.println("AWB_run to Process start...!");
					AWB_Gen_file AWB_Gen1 = new AWB_Gen_file();
					AWB_Gen1.AWB_run();
					
					Thread.sleep(sleepIntervalInMin*60*1000);
				}
			}
		}

		catch(Exception e)
		{
			e.printStackTrace();
			DAO_prime.DAOPrimeLogger.error("Exception Occurred in DAO Prime CBS  : "+e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			DAO_prime.DAOPrimeLogger.error("Exception Occurred in DAO Prime CBS  : "+result);
		}
	}

	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "DAO_Prime.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			  {
			    String name = (String) names.nextElement();
			    DAOPrimeConfigParamMap.put(name, p.getProperty(name));
			  }
		    }
		catch (Exception e)
		{
			return -1 ;
		}
		return 0;
	}
	
	// Data read from excel for Prime: Start
	public String readExcelForPrime(String finalSourcePath,String newFilename,String cabinetName, String sJtsIp, String iJtsPort)
	{
		String status="";
		System.out.println("Start readExcelForPrime ");
		 DAO_prime.DAOPrimeLogger.debug("Start readExcelForPrime ");
		try
		{
			String excelFilePath = finalSourcePath; // changed to .txt on 29.8.22
			// File exceFile = new File(excelFilePath);
			// String column_name="";//just for print not used in Insert query.
			// String Column_value="";
			String elite_crn="";
			String str_Card_Type="";
			String ECRN_dcc="";
			
			
			Date date = new Date();
			DateFormat EntryDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String entrydate = EntryDate.format(date);
			 DAO_prime.DAOPrimeLogger.debug("Start entrydate "+entrydate);
			/*
			if (!exceFile.exists())
	            {		
	                    throw new RuntimeException("Unable to find the file : " + excelFilePath);
	            }
				try
	            {
	                    excelWorkBook = Workbook.getWorkbook(exceFile);
	                    if (excelSheetName == null)
	                    {
	                            excelSheet = excelWorkBook.getSheet(0);
	                    }
	                    else
	                    {
	                            excelSheet = excelWorkBook.getSheet(excelSheetName);
	                    }

	            }
	            catch (BiffException biffe)
	            {
	                    biffe.printStackTrace();
	                    DAO_prime.DAOPrimeLogger.debug("biffe Exception occured in  readExcelForPrime: " +biffe.getMessage());
	            }
	            catch (IOException ioe)
	            {
	                    ioe.printStackTrace();
	                    DAO_prime.DAOPrimeLogger.debug("ioe Exception occured in  readExcelForPrime: " +ioe.getMessage());
	            }
	            int columns = excelSheet.getColumns();

	            int rows = excelSheet.getRows();

	            DAO_prime.DAOPrimeLogger.debug("Excel Sheet Name -> " + excelSheet.getName());
	            DAO_prime.DAOPrimeLogger.debug("Excel Sheet Contents ");
	          	
	            for (int row = 1; row < rows-1; row++)
	            {
	            	for (int col = 0; col < columns; col++)
	            	{
	            		String attributeName = excelSheet.getCell(col, row).getContents().trim();

	            		if (row==0)
	            		{
	            			if(col==0) {
	            				column_name = attributeName;
	            			}
	            			else {
	            				column_name = column_name+","+attributeName;
	            			}

	            		}
	            		else {
	            			if(col==0)
	            			{
	            				Column_value = "'"+attributeName+"'";
	            			}
	            			else 
	            			{
	            				Column_value = Column_value+", '"+attributeName+"'";
	            			}
	            		}

	            		String [] col_val_ecrn = Column_value.split("'");
	            		elite_crn = col_val_ecrn[1];
	            	}
	            	*/
	            	
	            	Scanner read = new Scanner(new File(finalSourcePath));
	    			String s_prime;
	    			
	    			while(read.hasNextLine())
	    			{
	    				s_prime = read.nextLine();
	    				s_prime="'"+s_prime.replace("|", "','")+"'";
	    			//	System.out.println("input " + s_prime);
	    				String [] col_val_ecrn = s_prime.split("','");
	    				elite_crn = col_val_ecrn[0].replace("'", "");
	    				str_Card_Type = col_val_ecrn[12].replace("'", "");
	    				ECRN_dcc = col_val_ecrn[13].replace("'", "");
	    				
	    				DAO_prime.DAOPrimeLogger.debug("ReadPrimeFile elite_crn: "+elite_crn);
	    				DAO_prime.DAOPrimeLogger.debug("ReadPrimeFile str_Card_Type: "+str_Card_Type);
	    				DAO_prime.DAOPrimeLogger.debug("ReadPrimeFile ECRN_dcc: "+ECRN_dcc);
	    				DAO_prime.DAOPrimeLogger.debug("ReadPrimeFile s_prime: "+s_prime);

	    				// Ap select for getting Ecrn number check and insert flag
	    				String ECRN="",Wi_name="", Query_for_ecrn="";
	    				if(str_Card_Type.equalsIgnoreCase("CREDIT")){
	    					Query_for_ecrn = "select ecrn as 'ECRN',WI_name as 'Wi_name'  from NG_DCC_EXTTABLE with(nolock) where ECRN = '"+ECRN_dcc+"'";
	    				}
	    				else{
	    					Query_for_ecrn = "select ecrn as 'ECRN',WI_name as 'Wi_name'  from NG_DAO_WI_UPDATE with(nolock) where ecrn = '"+elite_crn+"'";
	    				}
	    				String extTabDataIPXML=CommonMethods.apSelectWithColumnNames(Query_for_ecrn, cabinetName,sessionID);
	    				DAO_prime.DAOPrimeLogger.debug("Output_Query_for_ecrn: "+extTabDataIPXML);
	    				String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
	    				DAO_prime.DAOPrimeLogger.debug(" extTabDataOPXML : prime "+ extTabDataOPXML);

	    				XMLParser xmlParserData= new XMLParser(extTabDataOPXML);		

	    				if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0")){
	    					String xmlDataExtTab=xmlParserData.getNextValueOf("Record");
	    					xmlDataExtTab =xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

	    					//XMLParser xmlParserExtTabDataRecord = new XMLParser(xmlDataExtTab);
	    					NGXmlList objWorkList=xmlParserData.createList("Records", "Record");

	    					for (; objWorkList.hasMoreElements(true); objWorkList.skip(true)){
	    						ECRN = objWorkList.getVal("ECRN").trim();
	    						Wi_name = objWorkList.getVal("Wi_name").trim();
	    					}
	    					DAO_prime.DAOPrimeLogger.debug(" Wi_name "+Wi_name+" for  ECRN "+ ECRN);
	    				}

	    				// ApInsert call for insert into DB table prime - Starts

	    				s_prime=s_prime+",'"+Wi_name+"','"+entrydate+"'";

	    				DAO_prime.DAOPrimeLogger.debug("column_name : " +prime_cloumn_name);
	    				DAO_prime.DAOPrimeLogger.debug("Col_value : " +s_prime);

	    				String apInsertInputXML="";
	    				if(str_Card_Type.equalsIgnoreCase("CREDIT")){
	    					apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionID, prime_cloumn_name, s_prime,"NG_DCC_PRIME_COURIER");
	    				}
	    				else{
	    					apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionID, prime_cloumn_name, s_prime,"NG_DAO_PRIME_COURIER");
	    				}
	    				
	    				DAO_prime.DAOPrimeLogger.debug("APInsertInputXML: "+apInsertInputXML);

	    				String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
	    				DAO_prime.DAOPrimeLogger.debug("APInsertOutputXML: "+ apInsertInputXML);

	    				XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
	    				String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
	    				DAO_prime.DAOPrimeLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

	    				if("11".equalsIgnoreCase(apInsertMaincode)){

	    					sessionID  = CommonConnection.getSessionID(DAO_prime.DAOPrimeLogger, false);
	    					DAO_prime.DAOPrimeLogger.debug("new sessionId:  "+ sessionID);
	    					apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
	    					DAO_prime.DAOPrimeLogger.debug("APInsertOutputXML: "+ apInsertInputXML);

	    					xmlParserAPInsert = new XMLParser(apInsertOutputXML);
	    					apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
	    					DAO_prime.DAOPrimeLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);
	    				}

	    				if(apInsertMaincode.equalsIgnoreCase("0")){
	    					DAO_prime.DAOPrimeLogger.debug("ApInsert successful: "+apInsertMaincode);
	    					DAO_prime.DAOPrimeLogger.debug("Inserted in PRIME_COURIER table successfully.");
	    					if(str_Card_Type.equalsIgnoreCase("CREDIT")){
	    						updateExternalTable("NG_DCC_EXTTABLE","Courier_Flag","'File_received'","WI_name='"+Wi_name+"'", sJtsIp, iJtsPort, cabinetName);
	    					}else{
	    						if(ECRN.equals(elite_crn)){
	    							updateExternalTable("NG_DAO_EXTTABLE","Is_prime","'Y'","WI_name='"+Wi_name+"'", sJtsIp, iJtsPort, cabinetName);
	    						}
	    					}
	    					status="Success";
	    				}
	    				else{
	    					DAO_prime.DAOPrimeLogger.debug("ApInsert failed: "+apInsertMaincode);
	    					if(str_Card_Type.equalsIgnoreCase("CREDIT")){
	    						updateExternalTable("NG_DCC_EXTTABLE","Courier_Flag","'File_Error'","WI_name='"+Wi_name+"'", sJtsIp, iJtsPort, cabinetName);
	    					}else{
	    						updateExternalTable("NG_DAO_EXTTABLE","Is_prime","'E'","WI_name='"+Wi_name+"'", sJtsIp, iJtsPort, cabinetName);
	    					}
	    					status="fail";
	    					break;
	    				}
	    				// ApInsert call for insert into DB table prime - end
	    			}
	    	read.close();
		 }
		catch(Exception e){
			status="fail";
			DAO_prime.DAOPrimeLogger.debug("Exception occurred prime: "+e.getMessage());
			System.out.println("Exception occurred : prime"+e.getMessage());
		}
		
		return status;
	}
// End	
	
	// Data read from excel for Prime: Start
		/*public String readExcelForPrime_DCC(String finalSourcePath,String newFilename,String cabinetName, String sJtsIp, String iJtsPort, String sessionId)
		{
			String status="";
			System.out.println("Start readExcelForPrime ");
			 DAO_prime.DAOPrimeLogger.debug("Start readExcelForPrime ");
			try
			{
					String excelFilePath = finalSourcePath; // changed to .txt on 29.8.22
					// File exceFile = new File(excelFilePath);
					// String column_name="";//just for print not used in Insert query.
					// String Column_value="";
					String elite_crn="";
					
					
					if (!exceFile.exists())
			            {		
			                    throw new RuntimeException("Unable to find the file : " + excelFilePath);
			            }
						try
			            {
			                    excelWorkBook = Workbook.getWorkbook(exceFile);
			                    if (excelSheetName == null)
			                    {
			                            excelSheet = excelWorkBook.getSheet(0);
			                    }
			                    else
			                    {
			                            excelSheet = excelWorkBook.getSheet(excelSheetName);
			                    }

			            }
			            catch (BiffException biffe)
			            {
			                    biffe.printStackTrace();
			                    DAO_prime.DAOPrimeLogger.debug("biffe Exception occured in  readExcelForPrime: " +biffe.getMessage());
			            }
			            catch (IOException ioe)
			            {
			                    ioe.printStackTrace();
			                    DAO_prime.DAOPrimeLogger.debug("ioe Exception occured in  readExcelForPrime: " +ioe.getMessage());
			            }
			            int columns = excelSheet.getColumns();

			            int rows = excelSheet.getRows();

			            DAO_prime.DAOPrimeLogger.debug("Excel Sheet Name -> " + excelSheet.getName());
			            DAO_prime.DAOPrimeLogger.debug("Excel Sheet Contents ");
			          
			            for (int row = 1; row < rows-1; row++)
			            {
			            	for (int col = 0; col < columns; col++)
			            	{
			            		String attributeName = excelSheet.getCell(col, row).getContents().trim();

			            		if (row==0)
			            		{
			            			if(col==0) {
			            				column_name = attributeName;
			            			}
			            			else {
			            				column_name = column_name+","+attributeName;
			            			}

			            		}
			            		else {
			            			if(col==0)
			            			{
			            				Column_value = "'"+attributeName+"'";
			            			}
			            			else 
			            			{
			            				Column_value = Column_value+", '"+attributeName+"'";
			            			}
			            		}

			            		String [] col_val_ecrn = Column_value.split("'");
			            		elite_crn = col_val_ecrn[1];
			            	}
			            	
			            	
			            	
			            	Scanner read = new Scanner(new File(finalSourcePath));
			    			String s_prime = null;
			    			
			    				while(read.hasNextLine())
			    				{
			    					s_prime = read.nextLine();
			    					s_prime="'"+s_prime.replace("|", "','")+"'";
			    					System.out.println("input " + s_prime);
			    					String [] col_val_ecrn = s_prime.split("'");
			    				 	elite_crn = col_val_ecrn[3];
			    				 	DAO_prime.DAOPrimeLogger.debug("ReadPrimeFile : input "+col_val_ecrn[3]);
			    				 	DAO_prime.DAOPrimeLogger.debug("ReadPrimeFile : input "+s_prime);
			    				 	

			            	// Ap select for getting Ecrn number check and insert flag
			            	String ECRN="",Wi_name="";
			            	String Query_for_ecrn = "select ecrn as 'ECRN',WI_name as 'Wi_name'  from NG_DAO_EXTTABLE with(nolock) where ecrn = '"+elite_crn+"'";
			            	String extTabDataIPXML=CommonMethods.apSelectWithColumnNames(Query_for_ecrn, cabinetName, sessionId);
			            	DAO_prime.DAOPrimeLogger.debug("Output_Query_for_ecrn: "+extTabDataIPXML);
			            	String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
			            	DAO_prime.DAOPrimeLogger.debug(" extTabDataOPXML : prime "+ extTabDataOPXML);

			            	XMLParser xmlParserData= new XMLParser(extTabDataOPXML);		

			            	if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0"))
			            	{
			            		String xmlDataExtTab=xmlParserData.getNextValueOf("Record");
			            		xmlDataExtTab =xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

			            		//XMLParser xmlParserExtTabDataRecord = new XMLParser(xmlDataExtTab);
			            		NGXmlList objWorkList=xmlParserData.createList("Records", "Record");


			            		for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
			            		{
			            			ECRN = objWorkList.getVal("ECRN").trim();
			            			Wi_name = objWorkList.getVal("Wi_name").trim();
			            		}
			            		DAO_prime.DAOPrimeLogger.debug(" Wi_name "+Wi_name+" for  ECRN "+ ECRN);
			            	}

			            	// ApInsert call for insert into DB table prime - Starts

			            	s_prime=s_prime+",'"+Wi_name+"'";
			            	
			            	DAO_prime.DAOPrimeLogger.debug("column_name : " +prime_cloumn_name);
			            	DAO_prime.DAOPrimeLogger.debug("Col_value : " +s_prime);

			            	String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionId, prime_cloumn_name, s_prime,"NG_DAO_PRIME_COURIER");
			            	DAO_prime.DAOPrimeLogger.debug("APInsertInputXML: "+apInsertInputXML);

			            	String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
			            	DAO_prime.DAOPrimeLogger.debug("APInsertOutputXML: "+ apInsertInputXML);

			            	XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
			            	String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
			            	DAO_prime.DAOPrimeLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

			            	if(apInsertMaincode.equalsIgnoreCase("0"))
			            	{
			            		DAO_prime.DAOPrimeLogger.debug("ApInsert successful: "+apInsertMaincode);
			            		DAO_prime.DAOPrimeLogger.debug("Inserted in NG_DAO_PRIME_COURIER table successfully.");

			            		if(ECRN.equals(elite_crn))
			            			updateExternalTable("NG_DAO_EXTTABLE","Is_prime","'Y'","WI_name='"+Wi_name+"'", sJtsIp, iJtsPort, cabinetName, sessionId);
			            				            		
			            		status="Success";
			            	}
			            	else
			            	{
			            		DAO_prime.DAOPrimeLogger.debug("ApInsert failed: "+apInsertMaincode);
			            		updateExternalTable("NG_DAO_EXTTABLE","Is_prime","'E'","WI_name='"+Wi_name+"'", sJtsIp, iJtsPort, cabinetName, sessionId);
			            		status="fail";
			            	}
			            	// ApInsert call for insert into DB table prime - end

			            }
			}
			catch(Exception e)
			{
				status="fail";
				DAO_prime.DAOPrimeLogger.debug("Exception occurred prime: "+e.getMessage());
				System.out.println("Exception occurred : prime"+e.getMessage());
			}
			return status;
		}*/
	// End	
		
	public String ReadCSBEFile(String finalSourcePath,String newFilename,String cabinetName, String sJtsIp, String iJtsPort)
	{
		DAO_prime.DAOPrimeLogger.debug("ReadCSBEFile : Start ");
		String status="";
		try{
			Scanner read = new Scanner(new File(finalSourcePath));
			String insert_val = null;

			Date date = new Date();
			DateFormat EntryDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String entrydate = EntryDate.format(date);
			 DAO_prime.DAOPrimeLogger.debug("Start entrydate "+entrydate);
			
				while(read.hasNextLine())
				{
					insert_val = read.nextLine();
					insert_val="'"+insert_val.replace("|", "','")+"'";
					System.out.println("input " + insert_val);
				 	String [] chq_Bk_ref = insert_val.split("'");
				 	String chqBk_ref = chq_Bk_ref[3];
				 	DAO_prime.DAOPrimeLogger.debug("ReadCSBEFile : input "+chq_Bk_ref[3]);
				 	DAO_prime.DAOPrimeLogger.debug("ReadCSBEFile : input "+insert_val);
				 	
				 	//check chqBk_ref no length and format(if there) if incorrect then move the file to error or send fail from this. 
				 	
				 	// Ap select for chqBookRef
				 	
				 	String ChequeBk_ref="",account_no="",WI_name="";
					String Query_for_ecrn = "select ChequeBk_ref as 'ChequeBk_ref', WI_name as 'WI_name' from NG_DAO_WI_UPDATE with(nolock) where ChequeBk_ref='"+chqBk_ref+"'";
					String extTabDataIPXML=CommonMethods.apSelectWithColumnNames(Query_for_ecrn, cabinetName, sessionID);
					DAO_prime.DAOPrimeLogger.debug("Output_Query_for_ecrn: "+extTabDataIPXML);
					String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML,CommonConnection.getJTSIP(),CommonConnection.getJTSPort(),1);
					DAO_prime.DAOPrimeLogger.debug(" extTabDataOPXML : prime "+ extTabDataOPXML);
					
					XMLParser xmlParserData= new XMLParser(extTabDataOPXML);		
					
					if(xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0"))
					{
						String xmlDataExtTab=xmlParserData.getNextValueOf("Record");
						xmlDataExtTab =xmlDataExtTab.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
						
						//XMLParser xmlParserExtTabDataRecord = new XMLParser(xmlDataExtTab);
						NGXmlList objWorkList=xmlParserData.createList("Records", "Record");
						
						for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
						{
							ChequeBk_ref = objWorkList.getVal("ChequeBk_ref").trim();
							WI_name =objWorkList.getVal("WI_name").trim();
						}
						DAO_prime.DAOPrimeLogger.debug(" Wi_name "+WI_name+" for  ChequeBk_ref "+ ChequeBk_ref+ " and account No: "+account_no);
					}
					// Ap select - end:
					
				 	//Ap insert call start:
					String apInsertMaincode="";
					XMLParser xmlParserAPInsert=null;
					DAO_prime.DAOPrimeLogger.debug("cbs_Col_name : " +cbs_Col_name);
				 	insert_val = insert_val+",'"+WI_name+"','"+entrydate+"'";
					DAO_prime.DAOPrimeLogger.debug("Col_value +wi_name : " +insert_val);
					
					String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionID, cbs_Col_name,insert_val,"NG_DAO_CBS_FILE");
					DAO_prime.DAOPrimeLogger.debug("APInsertInputXML: "+apInsertInputXML);
					
					String apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
					DAO_prime.DAOPrimeLogger.debug("APInsertOutputXML: "+ apInsertInputXML);

					xmlParserAPInsert = new XMLParser(apInsertOutputXML);
					apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
					DAO_prime.DAOPrimeLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

					//Changes done to validate the session 
					if("11".equalsIgnoreCase(apInsertMaincode)){
						
						sessionID  = CommonConnection.getSessionID(DAO_prime.DAOPrimeLogger, false);
						DAO_prime.DAOPrimeLogger.debug("new sessionId:  "+ sessionID);
						apInsertOutputXML = WFNGExecute(apInsertInputXML,sJtsIp,iJtsPort,1);
						DAO_prime.DAOPrimeLogger.debug("APInsertOutputXML: "+ apInsertInputXML);

						xmlParserAPInsert = new XMLParser(apInsertOutputXML);
						apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
						DAO_prime.DAOPrimeLogger.debug("Status of apInsertMaincode  "+ apInsertMaincode);
					}
					
					if(apInsertMaincode.equalsIgnoreCase("0"))
					{
						DAO_prime.DAOPrimeLogger.debug("ApInsert successful: "+apInsertMaincode);
						DAO_prime.DAOPrimeLogger.debug("Inserted in  table successfully.");
						
						if(ChequeBk_ref.equals(chqBk_ref))
							updateExternalTable("NG_DAO_EXTTABLE","Is_CBS","'Y'","WI_name='"+WI_name+"'", sJtsIp, iJtsPort, cabinetName);
						
						status="Success";
					}
					
					else
					{
						DAO_prime.DAOPrimeLogger.debug("ApInsert failed: "+apInsertMaincode);
						updateExternalTable("NG_DAO_EXTTABLE","Is_CBS","'E'","WI_name='"+WI_name+"'", sJtsIp, iJtsPort, cabinetName);
						status="fail";
						break;
					}
					//Ap insert call end:
				}
				read.close();
		}
		
		catch(Exception e)
		{
			DAO_prime.DAOPrimeLogger.debug("Exception : cbs "+e.getCause());
			System.out.println("Exception : "+e.getCause());
		}
		return status;
	}

	
	
	public String get_timestamp()
	{
		Date present = new Date();
		Format pformatter = new SimpleDateFormat("dd-MM-yyyy-hhmmss");
		TimeStamp=pformatter.format(present);
		return TimeStamp;
	}
	
	private void updateExternalTable(String tablename, String columnname,String sMessage, String sWhere, String jtsIP, String jtsPort, String cabinetName){
		
		int sessionCheckInt=0;
		int loopCount=50;
		int mainCode = 0;
		
		DAO_prime.DAOPrimeLogger.debug("Inside update EXT table: ");
		
		while(sessionCheckInt<loopCount){
			
			try{
				
				XMLParser objXMLParser = new XMLParser();
				String inputXmlcheckAPUpdate = CommonMethods.getAPUpdateIpXML(tablename,columnname,sMessage,sWhere,cabinetName,sessionID);
				DAO_prime.DAOPrimeLogger.debug(("inputXmlcheckAPUpdate : " + inputXmlcheckAPUpdate));
				String outXmlCheckAPUpdate=null;
				outXmlCheckAPUpdate=WFNGExecute(inputXmlcheckAPUpdate,jtsIP,jtsPort,1);
				DAO_prime.DAOPrimeLogger.debug(("outXmlCheckAPUpdate : " + outXmlCheckAPUpdate));
				objXMLParser.setInputXML(outXmlCheckAPUpdate);
				String mainCodeforCheckUpdate = null;
				mainCodeforCheckUpdate=objXMLParser.getValueOf("MainCode");
				if (!mainCodeforCheckUpdate.equalsIgnoreCase("0")){
					
					DAO_prime.DAOPrimeLogger.debug(("Exception in ExecuteQuery_APUpdate updating "+tablename+" table"));
					System.out.println("Exception in ExecuteQuery_APUpdate updating "+tablename+" table");
				}
				else
				{
					DAO_prime.DAOPrimeLogger.debug(("Succesfully updated "+tablename+" table"));
					System.out.println("Succesfully updated "+tablename+" table");
					//ThreadConnect.addToTextArea("Successfully updated transaction table");
				}
				mainCode=Integer.parseInt(mainCodeforCheckUpdate);
				if (mainCode == 11)
				{
					sessionID  = CommonConnection.getSessionID(DAO_prime.DAOPrimeLogger, false);
				}
				else
				{
					sessionCheckInt++;
					break;
				}

				if (outXmlCheckAPUpdate.equalsIgnoreCase("") || outXmlCheckAPUpdate == "" || outXmlCheckAPUpdate == null)
					break;

			}
			catch(Exception e)
			{
				DAO_prime.DAOPrimeLogger.debug(("Inside create validateSessionID exception"+e.getMessage()));
			}
		}
	}
	
	public void DAO_ReadPrimefile(String cabinetName,String serverIP,String serverPort, String sessionID)  {
	
		try{
			
			now = new Date();
			Format formatter = new SimpleDateFormat("dd-MMM-yy");
			sdate = formatter.format(now);
			
			DAO_prime.DAOPrimeLogger.debug("Date: DAO_ReadPrimefile: "+sdate);
			
			File folder = new File(ExcelInputPath);
			File files[] = folder.listFiles();
			
			if(files.length == 0){
				DAO_prime.DAOPrimeLogger.debug("Thread prime going to sleep....");
				//Thread.sleep(120000);
			}
			else{
				
				for(int i=files.length-1;i>=0;i--){
					String file = files[i].getName();
					String TempsourcePath = "";
					String TempfailPath = "";
					String TempinProgressPath = "";
					String TempmovedPath = "";
					
					//ExcelInputPath+"\\"+file
					// String msg = "success"; //validateexcelsheet(File_input);
					// String file = files[i].getName();
					String msg = validatePrimefile(ExcelInputPath+"\\"+file);
					String updateStatus="";
					
					if(msg.equalsIgnoreCase("")){
						DAO_prime.DAOPrimeLogger.debug("Blank file : ");
						TempsourcePath=ExcelInputPath+"\\"+file;
						TempfailPath = ExcelFailPath+"\\"+sdate;
						DAO_prime.DAOPrimeLogger.debug("Blank file Prime : TempsourcePath: "+TempsourcePath+" ExcelFailPath: "+ExcelFailPath);
						TimeStamp=get_timestamp();
						newFilename = Move(TempfailPath,TempsourcePath,TimeStamp,true);//file is moved to NoDataFile flder
						//start
						sendMail(cabinetName,serverPort,jtsIP,jtsPort,file,ProcessDefId);
						continue;
					}
					else if((msg.equalsIgnoreCase("error"))){
						DAO_prime.DAOPrimeLogger.debug("error Prime file : ");
						TempsourcePath = ExcelInputPath+"\\"+file;
						TempfailPath = ExcelFailPath+"\\"+sdate;
						TimeStamp=get_timestamp();
						DAO_prime.DAOPrimeLogger.debug("Blank file : TempsourcePath: "+TempsourcePath+" ExcelFailPath: "+ExcelFailPath);
						newFilename = Move(TempfailPath,TempsourcePath,TimeStamp,true);  //file is moved to NoDataFile flder
						//start
						sendMail(cabinetName,serverPort,jtsIP,jtsPort,file,ProcessDefId);
						continue;
					}
					else{
						
						TempinProgressPath = ExcelInprogressPath;
						TempsourcePath = ExcelInputPath+"\\"+file;
						TimeStamp=get_timestamp();
						newFilename = Move(TempinProgressPath,TempsourcePath,TimeStamp,false);
						
						String finalSourcePath = TempinProgressPath+"\\"+newFilename;
						
						updateStatus = readExcelForPrime(finalSourcePath,newFilename,cabinetName,serverIP,serverPort);
						
						if(updateStatus.equalsIgnoreCase("success")){
							
							DAO_prime.DAOPrimeLogger.debug(" NG_DAO_PRIME_COURIER table insert successfully...");
							//TempsourcePath = ""+sourcePath+"\\"+file+"";
							TempmovedPath = ExcelSuccessPath+"\\"+sdate;
							TimeStamp=get_timestamp();
							newFilename = Move(TempmovedPath,finalSourcePath,TimeStamp,true);//file is moved to NoDataFile flder
							continue;
						}
						else if (updateStatus.equalsIgnoreCase("fail")){
							
							DAO_prime.DAOPrimeLogger.debug("Fail to insert in DB NG_DAO_PRIME_COURIER : ");
							
							TempsourcePath = ExcelInputPath+"\\"+file;
							TempfailPath = ExcelFailPath+"\\"+sdate;
							TimeStamp=get_timestamp();
							newFilename = Move(TempfailPath,finalSourcePath,TimeStamp,true);//file is moved to NoDataFile flder
							sendMail(cabinetName,serverPort,jtsIP,jtsPort,file,ProcessDefId);
							continue;		
						}
						
					}
					
				}			
			}
		}
		
		catch (Exception e){
			
			//e.printStackTrace();
			DAO_prime.DAOPrimeLogger.debug("Exception toString "  + e.toString());
			DAO_prime.DAOPrimeLogger.debug("Exception getMessage "  + e.getMessage());
		}	
		finally{
			
			System.gc();
		}
	
	}
	/*public void DCC_ReadPrimefile(String cabinetName,String serverIP,String serverPort, String sessionID)  {
		
		try{
			
			now = new Date();
			Format formatter = new SimpleDateFormat("dd-MMM-yy");
			sdate = formatter.format(now);
			
			DAO_prime.DAOPrimeLogger.debug("Date: DAO_ReadPrimefile: "+sdate);
			
			File folder = new File(ExcelInputPath);
			File files[] = folder.listFiles();
			
			if(files.length == 0){
				
				DAO_prime.DAOPrimeLogger.debug("Thread prime going to sleep....");
				//Thread.sleep(120000);
			}
			else{
				
				for(int i=files.length-1;i>=0;i--){
					
					String file = files[i].getName();
					String TempsourcePath = "";
					String TempfailPath = "";
					String TempinProgressPath = "";
					String TempmovedPath = "";
					
					//ExcelInputPath+"\\"+file
					// String msg = "success"; //validateexcelsheet(File_input);
					// String file = files[i].getName();
					String msg = validatePrimefile(ExcelInputPath+"\\"+file);
					String updateStatus="";
					
					if(msg.equalsIgnoreCase("")){
						
						DAO_prime.DAOPrimeLogger.debug("Blank file : ");
						TempsourcePath=ExcelInputPath+"\\"+file;
						TempfailPath = ExcelFailPath+"\\"+sdate;
						DAO_prime.DAOPrimeLogger.debug("Blank file Prime : TempsourcePath: "+TempsourcePath+" ExcelFailPath: "+ExcelFailPath);
						TimeStamp=get_timestamp();
						newFilename = Move(TempfailPath,TempsourcePath,TimeStamp,true);//file is moved to NoDataFile flder
						continue;
					}
					else if((msg.equalsIgnoreCase("error"))){
						
						DAO_prime.DAOPrimeLogger.debug("error Prime file : ");
						TempsourcePath = ExcelInputPath+"\\"+file;
						TempfailPath = ExcelFailPath+"\\"+sdate;
						TimeStamp=get_timestamp();
						DAO_prime.DAOPrimeLogger.debug("Blank file : TempsourcePath: "+TempsourcePath+" ExcelFailPath: "+ExcelFailPath);
						newFilename = Move(TempfailPath,TempsourcePath,TimeStamp,true);//file is moved to NoDataFile flder
						continue;
					}
					else{
						
						TempinProgressPath = ExcelInprogressPath;
						TempsourcePath = ExcelInputPath+"\\"+file;
						TimeStamp=get_timestamp();
						newFilename = Move(TempinProgressPath,TempsourcePath,TimeStamp,false);
						
						String finalSourcePath = TempinProgressPath+"\\"+newFilename;
						
						updateStatus = readExcelForPrime_DCC(finalSourcePath,newFilename,cabinetName,serverIP,serverPort,sessionID);
						
						if(updateStatus.equalsIgnoreCase("success")){
							
							DAO_prime.DAOPrimeLogger.debug(" NG_DAO_PRIME_COURIER table insert successfully...");
							//TempsourcePath = ""+sourcePath+"\\"+file+"";
							TempmovedPath = ExcelSuccessPath+"\\"+sdate;
							TimeStamp=get_timestamp();
							newFilename = Move(TempmovedPath,finalSourcePath,TimeStamp,true);//file is moved to NoDataFile flder
							continue;
						}
						else if (updateStatus.equalsIgnoreCase("fail")){
							
							DAO_prime.DAOPrimeLogger.debug("Fail to insert in DB NG_DAO_PRIME_COURIER : ");
							
							TempsourcePath = ExcelInputPath+"\\"+file;
							TempfailPath = ExcelFailPath+"\\"+sdate;
							TimeStamp=get_timestamp();
							newFilename = Move(TempfailPath,finalSourcePath,TimeStamp,true);//file is moved to NoDataFile flder
							continue;		
						}
						
					}
					
				}			
			}
		}
		
		catch (Exception e){
			
			//e.printStackTrace();
			DAO_prime.DAOPrimeLogger.debug("Exception toString "  + e.toString());
			DAO_prime.DAOPrimeLogger.debug("Exception getMessage "  + e.getMessage());
		}	
		finally{
			
			System.gc();
		}
	
	}*/
	
	public void DAO_ReadCBSfile(String cabinetName,String serverIP,String serverPort, String sessionID)  {
		
		try
		{
			now = new Date();
			Format formatter = new SimpleDateFormat("dd-MMM-yy");
			sdate = formatter.format(now);
			
			DAO_prime.DAOPrimeLogger.debug("Date: DAO_ReadCBSfile: "+sdate);
			
			File folder = new File(CbsInput);
			File files[] = folder.listFiles();
			
			if(files.length == 0)
			{
				DAO_prime.DAOPrimeLogger.debug("Thread CBS going to sleep....");
				//Thread.sleep(120000);
			}
			else
			{
				for(int i=files.length-1;i>=0;i--)	
				{
					String file = files[i].getName();
					String TempsourcePath = "";
					String TempfailPath = "";
					String TempinProgressPath = "";
					String TempmovedPath = "";
					
					String msg = validatecbsfile(CbsInput+"\\"+file);
						// validate txt file is empty or not.
					
					String updateStatus="";
					
					if(msg.equalsIgnoreCase(""))
					{
						DAO_prime.DAOPrimeLogger.debug("Blank file DAO_ReadCBSfile : ");
						TempsourcePath=CbsInput+"\\"+file;
						TempfailPath = CbsFail+"\\"+sdate;
						DAO_prime.DAOPrimeLogger.debug("Blank file : TempsourcePath: "+TempsourcePath+" TempfailPath: "+TempfailPath);
						TimeStamp=get_timestamp();
						newFilename = Move(TempfailPath,TempsourcePath,TimeStamp,true);//file is moved to NoDataFile flder
						sendMail(cabinetName,serverPort,jtsIP,jtsPort,file,ProcessDefId);
						continue;
					}
					else if((msg.equalsIgnoreCase("error")))
					{
						DAO_prime.DAOPrimeLogger.debug("error file DAO_ReadCBSfile : ");
						TempsourcePath = CbsInput+"\\"+file;
						TempfailPath = CbsFail+"\\"+sdate;
						TimeStamp=get_timestamp();
						DAO_prime.DAOPrimeLogger.debug("Blank file : TempsourcePath: "+TempsourcePath+" TempfailPath: "+TempfailPath);
						newFilename = Move(TempfailPath,TempsourcePath,TimeStamp,true);//file is moved to NoDataFile folder
						sendMail(cabinetName,serverPort,jtsIP,jtsPort,file,ProcessDefId);
						continue;
					}
					else
					{
						TempinProgressPath = CbsInprogresspath;
						TempsourcePath = CbsInput+"\\"+file;
						TimeStamp=get_timestamp();
						newFilename = Move(TempinProgressPath,TempsourcePath,TimeStamp,false);
						
						String finalSourcePath = TempinProgressPath+"\\"+newFilename;
						updateStatus = ReadCSBEFile(finalSourcePath,newFilename,cabinetName,serverIP,serverPort);
						
						if(updateStatus.equalsIgnoreCase("success"))
						{
							DAO_prime.DAOPrimeLogger.debug(" NG_DAO_CBS_FILE table insert successfully...");
							//TempsourcePath = ""+sourcePath+"\\"+file+"";
							TempmovedPath = CbsSuccess+"\\"+sdate;
							TimeStamp=get_timestamp();
							newFilename = Move(TempmovedPath,finalSourcePath,TimeStamp,true);//file is moved to NoDataFile flder
							continue;
						}
						else if (updateStatus.equalsIgnoreCase("fail"))
						{
							DAO_prime.DAOPrimeLogger.debug("Fail to insert in DB NG_DAO_CBS_FILE : ");
							
							TempsourcePath = CbsInput+"\\"+file;
							TempfailPath = CbsFail+"\\"+sdate;
							TimeStamp=get_timestamp();
							newFilename = Move(TempfailPath,finalSourcePath,TimeStamp,true);//file is moved to NoDataFile flder
							//start
							sendMail(cabinetName,serverPort,jtsIP,jtsPort,file,ProcessDefId);
							continue;	
						}
					}
					
				}			
			}
		}
		
		catch (Exception e)
		{
			//e.printStackTrace();
			DAO_prime.DAOPrimeLogger.debug("Exception toString "  + e.toString());
			DAO_prime.DAOPrimeLogger.debug("Exception getMessage "  + e.getMessage());
		}	
		finally
		{
			System.gc();

		}
	}

	/*
	public String validateexcelsheet(FileInputStream File_input) throws IOException
	{	
		String msg="";
		try
		{
			 // Reading the excel file from input folder at server.
			HSSFWorkbook wb = new HSSFWorkbook(File_input);
			
			for(int sheetNo = 0;sheetNo<wb.getNumberOfSheets();sheetNo++) // Reading the sheets in the excel file.
			{
				msg = isSheetempty(wb.getSheetAt(sheetNo));
			}
		}
		catch(Exception e)
		{
			msg="error";
			System.out.println("Exception: " +e.getMessage());
			DAO_prime.DAOPrimeLogger.debug("Exception : validateexcelsheet :"+e.getMessage());
		}
		return msg;
	}
	private String isSheetempty (Sheet Sheet)
	{	
		String msg="";
		try
		{
			Iterator rows = Sheet.rowIterator();
			Row row=null;
			Cell cell=null;
			while(rows.hasNext()){
				row=(Row) rows.next();
				Iterator cells = row.cellIterator();
				while(cells.hasNext()){
					cell=(Cell) cells.next();
					if(!cell.getStringCellValue().isEmpty()){
						msg="Not blank";
					}
				}
			}
		}
		catch(Exception e)
		{	
			msg="error";
			DAO_prime.DAOPrimeLogger.debug("Exception : isSheetempty :"+e.getMessage());
		}
		return msg;
	}
	*/
	
	public String validateexcelsheet(String finalSourcePath) throws BiffException, IOException
	{
		String msg="";
		
		String excelFilePath = ExcelInputPath;
		File exceFile = new File(excelFilePath);
		
		if (!exceFile.exists())
            {		
                    throw new RuntimeException("Unable to find the file validateexcelsheet : " + excelFilePath);
            }
			try
            {
                    excelWorkBook = Workbook.getWorkbook(exceFile);
                    if (excelSheetName == null)
                    {
                            excelSheet = excelWorkBook.getSheet(0);
                    }
                    else
                    {
                            excelSheet = excelWorkBook.getSheet(excelSheetName);
                    }
            }
            catch (BiffException biffe)
            {
                    biffe.printStackTrace();
                    DAO_prime.DAOPrimeLogger.debug("biffe Exception occured in  validateexcelsheet: " +biffe.getMessage());
            }
            catch (IOException ioe)
            {
                    ioe.printStackTrace();
                    DAO_prime.DAOPrimeLogger.debug("ioe Exception occured in  validateexcelsheet: " +ioe.getMessage());
            }
            int columns = excelSheet.getColumns();
            int rows = excelSheet.getRows();
            
            if(columns==0 && rows==0)
            {
            	
            }
            
		return msg;
		
	}
	
	public String validatecbsfile(String path)
	{
		String msg="";
		DAO_prime.DAOPrimeLogger.debug("Path : "+path);
		File file =  new File(path);
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			if(br.readLine()==null && file.length()==0){
				msg="error";
				DAO_prime.DAOPrimeLogger.debug("validatecbsfile : File length "+file.length()+"msg"+msg);
			}
			else{
				msg="Not blank";
				DAO_prime.DAOPrimeLogger.debug("validatecbsfile :"+msg);
			}
			br.close();
		}
		catch(Exception e){
			msg="error";
			DAO_prime.DAOPrimeLogger.debug("Exception : validatecbsfile :"+e.getMessage());
			
		}
		return msg;
		
	}
	
	public String validatePrimefile(String path)
	{
		String msg="";
		DAO_prime.DAOPrimeLogger.debug("Path : "+path);
		File file =  new File(path);
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			if(br.readLine()==null && file.length()==0){
				msg="error";
				DAO_prime.DAOPrimeLogger.debug("validatecbsfile : File length "+file.length()+"msg"+msg);
			}
			else{
				msg="Not blank";
				DAO_prime.DAOPrimeLogger.debug("validatecbsfile :"+msg);
			}
			br.close();
		}
		catch(Exception e){
			msg="error";
			DAO_prime.DAOPrimeLogger.debug("Exception : validatecbsfile :"+e.getMessage());
		}
		
		
		return msg;
		
	}
	
	
	public String Move(String pstrDestFolderPath, String pstrFilePathToMove,String append,boolean flag ) 
	{
		String newFilename="";
		String lstrExceptionId = "Text_Read.Move";
		try {
			// Destination directory
			File lobjDestFolder = new File(pstrDestFolderPath);

			if (!lobjDestFolder.exists()) {
				lobjDestFolder.mkdirs();
				//delete destination file if it already exists
			}
			File lobjFileTemp;
			File lobjFileToMove = new File(pstrFilePathToMove);
			String orgFileName=lobjFileToMove.getName();

			if(flag){
				newFilename=orgFileName.substring(0,orgFileName.indexOf("."))+"_"+append+orgFileName.substring(orgFileName.indexOf("."));
				lobjFileTemp = new File(pstrDestFolderPath + File.separator + newFilename);
			}else{
				//DAO_prime.DAOPrimeLogger.debug.info("orgFileName::"+orgFileName);
				newFilename=orgFileName;
				lobjFileTemp = new File(pstrDestFolderPath+ File.separator + newFilename );
				//DAO_prime.DAOPrimeLogger.debug.info("lobjFileTemp::"+lobjFileTemp);
			}
			if (lobjFileTemp.exists()) {
				//DAO_prime.DAOPrimeLogger.debug.info("lobjFileTemp exists");
				if (!lobjFileTemp.isDirectory()){
					lobjFileTemp.delete();
				} 
				else {
					deleteDir(lobjFileTemp);
				}
			} 
			else {
				//DAO_prime.DAOPrimeLogger.debug.info("lobjFileTemp dont exists");
				// lobjFileTemp = null;
			}
			File lobjNewFolder ;
			// if(flag){
			lobjNewFolder = new File(lobjDestFolder, newFilename);
			/* }else{
            	 lobjNewFolder = lobjDestFolder;
            }*/


			boolean lbSTPuccess = false;
			try 
			{
				//DAO_prime.DAOPrimeLogger.debug.info("lobjFileToMove::"+lobjFileToMove);
				//DAO_prime.DAOPrimeLogger.debug.info("lobjNewFolder::"+lobjNewFolder);
				lbSTPuccess = lobjFileToMove.renameTo(lobjNewFolder);
				//DAO_prime.DAOPrimeLogger.debug.info("lbSTPuccess::"+lbSTPuccess);
			} 
			catch (SecurityException lobjExp) 
			{

				DAO_prime.DAOPrimeLogger.debug("SecurityException " + lobjExp.toString());
			} 
			catch (NullPointerException lobjNPExp) 
			{

				DAO_prime.DAOPrimeLogger.debug("NullPointerException " + lobjNPExp.toString());
			} 
			catch (Exception lobjExp) 
			{

				DAO_prime.DAOPrimeLogger.debug("Exception " + lobjExp.toString());
			}
			if (!lbSTPuccess) 
			{
				// File was not successfully moved


				//DAO_prime.DAOPrimeLogger.debug("Failure while moving " + lobjFileToMove.getAbsolutePath() + "===" +
				//	lobjFileToMove.canWrite());
			} 
			else 
			{

				//DAO_prime.DAOPrimeLogger.debug("Success while moving " + lobjFileToMove.getName() + "to" + pstrDestFolderPath);
				//DAO_prime.DAOPrimeLogger.debug("Success while moving " + lobjFileToMove.getName() + "to" + lobjNewFolder);
			}
			lobjDestFolder = null;
			lobjFileToMove = null;
			lobjNewFolder = null;
		} 
		catch (Exception lobjExp) 
		{
			DAO_prime.DAOPrimeLogger.debug(lstrExceptionId + " : " + "Exception occurred while moving " + pstrFilePathToMove + " to " +
					":" + lobjExp.toString());

		}

		return newFilename;
	}
	
	public static boolean deleteDir(File dir) throws Exception {
		if (dir.isDirectory()) {
			String[] lstrChildren = dir.list();
			for (int i = 0; i < lstrChildren.length; i++) {
				boolean success = deleteDir(new File(dir, lstrChildren[i]));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}
	
	/*private HashMap<String, String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort,
			String sessionID) {
		HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

		try {
			DAO_prime.DAOPrimeLogger.debug("Fetching Socket Connection Details.");
			System.out.println("Fetching Socket Connection Details.");

			String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'DigitalAO' and CallingSource = 'Utility'";

			String socketDetailsInputXML = CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
			DAO_prime.DAOPrimeLogger.debug("Socket Details APSelect InputXML: " + socketDetailsInputXML);

			String socketDetailsOutputXML = WFNGExecute(socketDetailsInputXML, sJtsIp, iJtsPort, 1);
			DAO_prime.DAOPrimeLogger.debug("Socket Details APSelect OutputXML: " + socketDetailsOutputXML);

			XMLParser xmlParserSocketDetails = new XMLParser(socketDetailsOutputXML);
			String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			DAO_prime.DAOPrimeLogger.debug("SocketDetailsMainCode: " + socketDetailsMainCode);

			int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			DAO_prime.DAOPrimeLogger.debug("SocketDetailsTotalRecords: " + socketDetailsTotalRecords);

			if (socketDetailsMainCode.equalsIgnoreCase("0") && socketDetailsTotalRecords > 0) {
				String xmlDataSocketDetails = xmlParserSocketDetails.getNextValueOf("Record");
				xmlDataSocketDetails = xmlDataSocketDetails.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

				XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

				String socketServerIP = xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
				DAO_prime.DAOPrimeLogger.debug("SocketServerIP: " + socketServerIP);
				socketDetailsMap.put("SocketServerIP", socketServerIP);

				String socketServerPort = xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
				DAO_prime.DAOPrimeLogger.debug("SocketServerPort " + socketServerPort);
				socketDetailsMap.put("SocketServerPort", socketServerPort);

				DAO_prime.DAOPrimeLogger.debug("SocketServer Details found.");
				System.out.println("SocketServer Details found.");

			}
		} catch (Exception e) {
			DAO_prime.DAOPrimeLogger
					.debug("Exception in getting Socket Connection Details: " + e.getMessage());
			System.out.println("Exception in getting Socket Connection Details: " + e.getMessage());
		}

		return socketDetailsMap;
	}*/

	protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort, int flag)
			throws IOException, Exception {
		DAO_prime.DAOPrimeLogger.debug("In WF NG Execute : " + serverPort);
		try {
			if (serverPort.startsWith("33"))
				return WFCallBroker.execute(ipXML, jtsServerIP, Integer.parseInt(serverPort), 1);
			else
				return ngEjbClientCIFVer.makeCall(jtsServerIP, serverPort, "WebSphere", ipXML);
		} catch (Exception e) {
			DAO_prime.DAOPrimeLogger.debug("Exception Occured in WF NG Execute : " + e.getMessage());
			e.printStackTrace();
			return "Error";
		}
	}
	
	public static void waiteloopExecute(long wtime) {
		try {
			for (int i = 0; i < 10; i++) {
				Thread.yield();
				Thread.sleep(wtime / 10);
			}
		} catch (InterruptedException e) {
		}
	}
	
	public   void sendMail(String cabinetName ,String wiName,String jtsIp,String jtsPort,String file,String ProcessDefId)throws Exception
    {
        XMLParser objXMLParser = new XMLParser();
        String sInputXML="";
        String sOutputXML="";
        String mainCodeforAPInsert=null;
        int sessionCheckInt = 0;
		while(sessionCheckInt<loopCount)
        {
            try
            {	
            	String FinalMailStr = MailStr.toString().replace("<#file#>",file);
            	
            	DAO_prime.DAOPrimeLogger.debug("finalbody: "+FinalMailStr);

            	String columnName="MAILFROM,MAILTO,MAILSUBJECT,MAILMESSAGE,MAILCONTENTTYPE,MAILPRIORITY,MAILSTATUS,INSERTEDBY,MAILACTIONTYPE,INSERTEDTIME,PROCESSDEFID,PROCESSINSTANCEID,WORKITEMID,ACTIVITYID,NOOFTRIALS";
            	String strValues="'"+fromMailID+"','"+toMailID+"','"+mailSubject+"','"+FinalMailStr+"','text/html;charset=UTF-8','1','N','CUSTOM','TRIGGER','"+CommonMethods.getdateCurrentDateInSQLFormat()+"','"+ProcessDefId+"','"+wiName+"','1','1','0'";
                
				sInputXML = "<?xml version=\"1.0\"?>" +
                        "<APInsert_Input>" +
                        "<Option>APInsert</Option>" +
                        "<TableName>WFMAILQUEUETABLE</TableName>" +
                        "<ColName>" + columnName + "</ColName>" +
                        "<Values>" + strValues + "</Values>" +
                        "<EngineName>" + cabinetName + "</EngineName>" +
                        "<SessionId>" + sessionID + "</SessionId>" +
                        "</APInsert_Input>";
                DAO_prime.DAOPrimeLogger.debug("Mail Insert InputXml::::::::::\n"+sInputXML);
                sOutputXML =WFNGExecute(sInputXML, jtsIp,jtsPort,0);
                DAO_prime.DAOPrimeLogger.debug("Mail Insert OutputXml::::::::::\n"+sOutputXML);
                objXMLParser.setInputXML(sOutputXML);
                mainCodeforAPInsert=objXMLParser.getValueOf("MainCode");
                
            }
			
			catch(Exception e)
            {
                e.printStackTrace();
                DAO_prime.DAOPrimeLogger.debug("Exception in Sending mail", e);
                sessionCheckInt++;
                waiteloopExecute(waitLoop);
                continue;
            }
            if (mainCodeforAPInsert.equalsIgnoreCase("11")) 
            {
                DAO_prime.DAOPrimeLogger.debug("Invalid session in Sending mail");
                sessionCheckInt++;
                //ThreadConnect.sessionId = ThreadConnect.getSessionID(cabinetName, jtsIP, jtsPort, userName,password);
                sessionID  = CommonConnection.getSessionID(DAO_prime.DAOPrimeLogger, false);
                continue;
            }
            else
            {
                sessionCheckInt++;
                break;
            }
        }
        if(mainCodeforAPInsert.equalsIgnoreCase("0"))
        {
            DAO_prime.DAOPrimeLogger.debug("mail Insert Successful");
            System.out.println("Mail Insert Successful for "+wiName+" in table WFMAILQUEUETABLE");
        }
        else
        {
            DAO_prime.DAOPrimeLogger.debug("mail Insert Unsuccessful");
            System.out.println("Mail Insert Unsuccessful for "+wiName+"in table WFMAILQUEUETABLE");
        }
    }
}



