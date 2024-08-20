/*
---------------------------------------------------------------------------------------------------------
                  NEWGEN SOFTWARE TECHNOLOGIES LIMITED

Group                   : Application - Projects
Project/Product			: RAK BPM
Application				: RAK BPM Utility
Module					: Main
File Name				: RAKBankUtility.java
Author 					: Sakshi Grover
Date (DD/MM/YYYY)		: 30/04/2019

---------------------------------------------------------------------------------------------------------
                 	CHANGE HISTORY
---------------------------------------------------------------------------------------------------------

Problem No/CR No        Change Date           Changed By             Change Description
---------------------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------------------
*/


package com.newgen.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.PropertyConfigurator;

import com.newgen.DAO.ExtensionP4.DAO_ExtensionP4;
import com.newgen.DAO.Notify.NotifyApplication;
import com.newgen.DAO.Prime.Prime_cbs;
import com.newgen.DAO.WI_Update.WI_update_done;
import com.newgen.DCC.AnnualFeeEnable.DCC_AnnualFeeSysIntegration;
import com.newgen.DCC.CAMGenCIFUpdate.Digital_CC_GenCAMReport;
import com.newgen.DCC.CAMGenBSR.Digital_CC_BCR_GenCAMReport;
import com.newgen.DCC.CardClosure.DCC_ClouserIntegration;
import com.newgen.DCC.EFMS.DCC_EFMS_Integration;
import com.newgen.DCC.FIRCO.DCC_FIRCO_Integration;
import com.newgen.DCC.Final_Limit_Increase.DCC_FINAL_LIMIT_INC;
import com.newgen.DCC.Notify.DCC_Notify_App;
// import com.newgen.DCC.Notify.DCC_Notify_App;
import com.newgen.DCC.SystemError.SystemErrorHandling;
import com.newgen.DCC.SystemIntegration.DCC_System_Integration;
import com.newgen.DCC.Update_AssignCIF.DCC_Update_Assign_CIF_SysIntegration;
import com.newgen.common.CommonConnection;
import com.newgen.encryption.DataEncryption;


public class RAKDigitalIntegrationUtility
{
	private static Map<String, String> mainPropMap= new HashMap<String, String>();
	private static String loggerName = "MainLogger";
	private static org.apache.log4j.Logger MainLogger = org.apache.log4j.Logger.getLogger(loggerName);

	 static
	 {
			setLogger();
	 }

	public static void main(String[] args)
	{
		System.out.println("Starting utility...");
		MainLogger.info("Starting Utility");
		int mainPropFileReadCode = readMainPropFile();

		if(mainPropFileReadCode!=0)
		{
			System.out.println("Error in Readin Main Property FIle");
			MainLogger.error("Error in Readin Main Property FIle "+mainPropFileReadCode);
			return;
		}

		try
		{
			int socketPort =  Integer.parseInt(mainPropMap.get("Utility_Port"));
			if(socketPort==0)
			{
				System.out.println("Not able to Get Utility Port");
				MainLogger.error("Not able to Get Utility Port "+socketPort);
				return;
			}
			ServerSocket serverSocket = new ServerSocket(socketPort);

			CommonConnection.setUsername(mainPropMap.get("UserName"));
			CommonConnection.setPassword(DataEncryption.decrypt(mainPropMap.get("Password")));
			CommonConnection.setJTSIP(mainPropMap.get("JTSIP"));
			CommonConnection.setJTSPort(mainPropMap.get("JTSPort"));
			CommonConnection.setsSMSPort(mainPropMap.get("SMSPort"));
			CommonConnection.setCabinetName(mainPropMap.get("CabinetName"));
			CommonConnection.setsVolumeID(mainPropMap.get("VolumeID"));
			CommonConnection.setsSiteID(mainPropMap.get("SiteID"));
			
			CommonConnection.setOFCabinetName(mainPropMap.get("OFCabinetName"));
			CommonConnection.setOFBAISProcessDefId(mainPropMap.get("OFBAISProcessDefId"));
			CommonConnection.setOFJTSIP(mainPropMap.get("OFJTSIP"));
			CommonConnection.setOFJTSPort(mainPropMap.get("OFJTSPort"));
			CommonConnection.setOFVOLUMNID(mainPropMap.get("OFVOLUMNID"));
			CommonConnection.setOFUserName(mainPropMap.get("OFUserName"));
			CommonConnection.setOFPassword(mainPropMap.get("OFPassword"));
			
			String sessionID = CommonConnection.getSessionID(MainLogger,false);

			if(sessionID==null || sessionID.equalsIgnoreCase("") || sessionID.equalsIgnoreCase("null"))
			{
				MainLogger.info("Could Not Get Session ID "+sessionID);
				return;
			}
			// Hritik 18.8.22 DAO Threads
			if(mainPropMap.get("DAO_NotifyThread")!=null && mainPropMap.get("DAO_NotifyThread").equalsIgnoreCase("Y"))
			{
				Thread NotifyThread = new Thread(new NotifyApplication());
				NotifyThread.start();
				System.out.println("NotifyThread Started");
				MainLogger.info("NotifyThread Started");
			}
			
			if(mainPropMap.get("DAO_Prime_cbs")!=null && mainPropMap.get("DAO_Prime_cbs").equalsIgnoreCase("Y"))
			{
				Thread Prime_cbs_Thread = new Thread(new Prime_cbs());
				Prime_cbs_Thread.start();
				System.out.println("Prime_cbs_Thread Started");
				MainLogger.info("Prime_cbs_Thread Started");
			}
			
			if(mainPropMap.get("DAO_WI_update_done")!=null && mainPropMap.get("DAO_WI_update_done").equalsIgnoreCase("Y"))
			{
				Thread thread = new Thread(new WI_update_done());
				thread.start();
				System.out.println("WI_update_done Started");
				MainLogger.info("WI_update_done Started");
			}
			// Hritik 18.8.22 DAO Threads - END
			
			if(mainPropMap.get("DCC_SystemIntegration")!=null && mainPropMap.get("DCC_SystemIntegration").equalsIgnoreCase("Y"))
			{
				Thread thread = new Thread(new DCC_System_Integration());
				thread.start();
				System.out.println("DCC_SystemIntegration Started");
				MainLogger.info("DCC_SystemIntegration Started");
			}
			
			if(mainPropMap.get("DCC_FIRCO_Integration")!=null && mainPropMap.get("DCC_FIRCO_Integration").equalsIgnoreCase("Y"))
			{
				Thread thread = new Thread(new DCC_FIRCO_Integration());
				thread.start();
				System.out.println("DCC_FIRCO_Integration Started");
				MainLogger.info("DCC_FIRCO_Integration Started");
			}
			if(mainPropMap.get("DCC_EFMSIntegration")!=null && mainPropMap.get("DCC_EFMSIntegration").equalsIgnoreCase("Y"))
			{
				Thread thread = new Thread(new DCC_EFMS_Integration());
				thread.start();
				System.out.println("DCC_EFMSIntegration Started");
				MainLogger.info("DCC_EFMSIntegration Started");
			}
			if(mainPropMap.get("DCC_SystemErrorHandling")!=null && mainPropMap.get("DCC_SystemErrorHandling").equalsIgnoreCase("Y"))
			{
				Thread thread = new Thread(new SystemErrorHandling());
				thread.start();
				System.out.println("DCC_SystemErrorHandling Started");
				MainLogger.info("DCC_SystemErrorHandling Started");
			}

			if(mainPropMap.get("DCC_CardClouser")!=null && mainPropMap.get("DCC_CardClouser").equalsIgnoreCase("Y"))
			{
				Thread thread = new Thread(new DCC_ClouserIntegration());
				thread.start();
				System.out.println("DCC_SystemErrorHandling Started");
				MainLogger.info("DCC_SystemErrorHandling Started");
			}
			
			if(mainPropMap.get("DAO_Extension_P4")!=null && mainPropMap.get("DAO_Extension_P4").equalsIgnoreCase("Y"))
			{
				Thread thread = new Thread(new DAO_ExtensionP4());
				thread.start();
				System.out.println("DAO_ExtensionP4 Started");
				MainLogger.info("DAO_ExtensionP4 Started");
			}
			if(mainPropMap.get("DCC_Update_Assign_CIF")!=null && mainPropMap.get("DCC_Update_Assign_CIF").equalsIgnoreCase("Y"))
			{
				Thread thread = new Thread(new DCC_Update_Assign_CIF_SysIntegration());
				thread.start();
				System.out.println("DCC_Update_Assign_CIF_SysIntegration Started");
				MainLogger.info("DCC_Update_Assign_CIF_SysIntegration Started");
			}
			
			if(mainPropMap.get("DCC_GenCAMReport")!=null && mainPropMap.get("DCC_GenCAMReport").equalsIgnoreCase("Y"))
			{
				Thread thread = new Thread(new Digital_CC_GenCAMReport());
				thread.start();
				System.out.println("DCC_GenCAMReport Started");
				MainLogger.info("DCC_GenCAMReport Started");
			}
			
			//Rubi
			if(mainPropMap.get("DCC_BSR_GenCAMReport")!=null && mainPropMap.get("DCC_BSR_GenCAMReport").equalsIgnoreCase("Y"))
			{
				Thread thread = new Thread(new Digital_CC_BCR_GenCAMReport());
				thread.start();
				System.out.println("DCC_BSR_GenCAMReport Started");
				MainLogger.info("DCC_BSR_GenCAMReport Started");
			}


			if(mainPropMap.get("DCC_Notify_App")!=null && mainPropMap.get("DCC_Notify_App").equalsIgnoreCase("Y"))
			{
				Thread thread = new Thread(new DCC_Notify_App());
				thread.start();
				System.out.println("DCC Notify Started");
				MainLogger.info("DCC Notify Started");
			} 
			if(mainPropMap.get("DCC_FINAL_LIMT")!=null && mainPropMap.get("DCC_FINAL_LIMT").equalsIgnoreCase("Y"))
			{
				Thread thread = new Thread(new DCC_FINAL_LIMIT_INC());
				thread.start();
				System.out.println("DCC FINAL_LIMT Started");
				MainLogger.info("DCC FINAL_LIMT Started");
			} 
			if(mainPropMap.get("DCC_FEE_ENABLE")!=null && mainPropMap.get("DCC_FEE_ENABLE").equalsIgnoreCase("Y"))
			{
				/*TimerTask task = new DCC_AnnualFeeSysIntegration();
				Timer timer = new Timer();
				Calendar today = Calendar.getInstance();
				if(mainPropMap.get("DCC_FEE_Enable_Hour")!=null )
					today.set(Calendar.HOUR_OF_DAY,Integer.parseInt(mainPropMap.get("DCC_FEE_Enable_Hour")));
				if(mainPropMap.get("DCC_FEE_Enable_Minute")!=null )
					today.set(Calendar.MINUTE,Integer.parseInt(mainPropMap.get("DCC_FEE_Enable_Minute")));
				if(mainPropMap.get("DCC_FEE_Enable_Second")!=null )
					today.set(Calendar.SECOND,Integer.parseInt(mainPropMap.get("DCC_FEE_Enable_Second")));
				System.out.println("Fee enable will run at "+today.getTime());
				timer.schedule(task, today.getTime());*/
				Thread thread = new Thread(new DCC_AnnualFeeSysIntegration());
				thread.start();
				System.out.println("DCC_FEE_ENABLE Started");
				MainLogger.info("DCC_FEE_ENABLE Started");
			} 
			
		}
		catch (Exception e)
		{
			if(e.getMessage().toUpperCase().startsWith("Address already in use".toUpperCase()))
			{
				System.out.println("Utility Instance Already Running");
				MainLogger.error("Utility Instance Already Running");
			}
			else
			{
				e.printStackTrace();
				MainLogger.error("Exception Occurred in Main Thread: "+e);
				final Writer result = new StringWriter();
				final PrintWriter printWriter = new PrintWriter(result);
				e.printStackTrace(printWriter);
				MainLogger.error("Exception Occurred in Main Thread : "+result);
			}
			return;
		}
		finally
		{
			System.gc();
		}
	}

	private static void setLogger()
	{
		try
		{
			Date date = new Date();
			DateFormat logDateFormat = new SimpleDateFormat("dd-MM-yyyy");
			Properties p = new Properties();
			p.load(new FileInputStream(System.getProperty("user.dir")+ File.separator + "log4jFiles"+ File.separator+ "Main_log4j.properties"));
			String dynamicLog = null;
			String orgFileName = null;
			File d = null;
			File fl = null;

			dynamicLog = "Logs/Main_Logs/"+logDateFormat.format(date)+"/Main_Log.xml";
			orgFileName = p.getProperty("log4j.appender."+loggerName+".File");
			if(!(orgFileName==null || orgFileName.equalsIgnoreCase("")))
			{
				dynamicLog = orgFileName.substring(0,orgFileName.lastIndexOf("/")+1)+logDateFormat.format(date)+orgFileName.substring(orgFileName.lastIndexOf("/"));
			}
			d = new File(dynamicLog.substring(0,dynamicLog.lastIndexOf("/")));
			d.mkdirs();
			fl = new File(dynamicLog);
			if(!fl.exists())
				fl.createNewFile();
			p.put("log4j.appender."+loggerName+".File", dynamicLog );

			PropertyConfigurator.configure(p);
			//System.out.println("Dynamic Logger Created");
		}
		catch(Exception e)
		{
			System.out.println("Exception in creating dynamic log :"+e);
			e.printStackTrace();
		}
	}

	private static int readMainPropFile()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "Main_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			{
			    String name = (String) names.nextElement();
			    mainPropMap.put(name, p.getProperty(name));
			}

		} catch (Exception e) {

			return -1 ;
		}
		return 0;
	}
}