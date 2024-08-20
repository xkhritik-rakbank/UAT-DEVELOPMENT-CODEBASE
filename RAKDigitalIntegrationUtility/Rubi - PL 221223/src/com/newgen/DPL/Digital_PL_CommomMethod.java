package com.newgen.DPL;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.Period;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.XMLParser;

public class Digital_PL_CommomMethod extends CommonMethods{
	
	//Read data from property file
	public static Map<String,String> readConfig(String FileName) {
		Properties p = null;
		Map<String, String> digital_PL_Map = new HashMap<String, String>();
		try {
			
			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir") + File.separator + "ConfigFiles" + File.separator + FileName)));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				digital_PL_Map.put(name, p.getProperty(name));
			}
			
			return digital_PL_Map;
		} catch (Exception e) {
			return null;
		}
		
	}
	
	public static HashMap<String, String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort, String sessionID) {
		HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

		try {
			System.out.println("Fetching Socket Connection Details.");

			String socketDetailsQuery = "SELECT SocketServerIP, SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'DCC' and CallingSource = 'Utility'";

			String socketDetailsInputXML = CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
			
			String socketDetailsOutputXML = CommonMethods.WFNGExecute(socketDetailsInputXML, sJtsIp, iJtsPort, 1);
			
			XMLParser xmlParserSocketDetails = new XMLParser(socketDetailsOutputXML);
			String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			
			int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			
			if (socketDetailsMainCode.equalsIgnoreCase("0") && socketDetailsTotalRecords > 0) {
				String xmlDataSocketDetails = xmlParserSocketDetails.getNextValueOf("Record");
				xmlDataSocketDetails = xmlDataSocketDetails.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

				XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

				String socketServerIP = xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
				socketDetailsMap.put("SocketServerIP", socketServerIP);

				String socketServerPort = xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
				socketDetailsMap.put("SocketServerPort", socketServerPort);

				System.out.println("SocketServer Details found.");

			}
		} catch (Exception e) {
			System.out.println("Exception in getting Socket Connection Details: " + e.getMessage());
		}

		return socketDetailsMap;
	}

	//Calculate DOJ formate should be YYYY-MM-DD
		public static Double CalculatLOS(String DOJ_Str) {
			Double LOS = 0.00;
			try {
				Integer year = Integer.parseInt(DOJ_Str.split("-")[0]);
				Integer month = Integer.parseInt(DOJ_Str.split("-")[1]);
				Integer day = Integer.parseInt(DOJ_Str.split("-")[2]);
				LocalDate DOJ = LocalDate.of(year,month,day);
				LocalDate CD = LocalDate.now();
				Period p = Period.between(DOJ, CD);
				System.out.println(p.getMonths());
				System.out.println(p.getYears());
				
				LOS += p.getYears();
				LOS = LOS + p.getMonths()/100d;
			} catch (Exception e) {
				e.printStackTrace();
				return LOS;
			}
			
			System.out.println(LOS);
			return LOS;
		}
		
		protected static String validateValue(String value) {
			if (value != null && !value.equals("") && !value.equalsIgnoreCase("null")) {
				return value.trim();
			}
			return "";
		}
		
		
}
