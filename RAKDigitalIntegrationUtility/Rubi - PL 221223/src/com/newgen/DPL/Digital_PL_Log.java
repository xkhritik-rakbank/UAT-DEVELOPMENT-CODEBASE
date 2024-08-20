package com.newgen.DPL;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

public class Digital_PL_Log {

	private static String loggerName = "";
    protected static org.apache.log4j.Logger digital_PL_Log = org.apache.log4j.Logger.getLogger(loggerName);;

    
    protected static void setLogger(String logFileName)
    {
    	try
		{
    		loggerName = logFileName;
    		Date date = new Date();
			DateFormat logDateFormat = new SimpleDateFormat("dd-MM-yyyy");
			Properties p = new Properties();
			p.load(new FileInputStream(System.getProperty("user.dir")+ File.separator + "log4jFiles"+ File.separator+ "DCC_System_Integration_Log4j.properties"));
			String dynamicLog = null;
			String orgFileName = null;
			File d = null;
			File fl = null;

			dynamicLog = "Logs/Digital_PL_Log/"+logDateFormat.format(date)+"/"+logFileName;
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
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }

}
