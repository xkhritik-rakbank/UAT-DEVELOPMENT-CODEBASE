package com.newgen.DPL;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

public class Digital_PL_Log {

	private static String loggerName = "Digital_PL_Log";
    protected static org.apache.log4j.Logger digital_PL_Log = org.apache.log4j.Logger.getLogger(loggerName);;

    static{
    	setLogger(loggerName);
    }
    
    public static org.apache.log4j.Logger getLogger(String logFileName){
    	return org.apache.log4j.Logger.getLogger(logFileName);
    }
    public static void setLogger(String logFileName)
    {
    	try
		{
    		//loggerName = logFileName;
    		Date date = new Date();
			DateFormat logDateFormat = new SimpleDateFormat("dd-MM-yyyy");
			Properties p = new Properties();
			p.load(new FileInputStream(System.getProperty("user.dir")+ File.separator + "log4jFiles"+ File.separator+ "Digital_PL_Log4j.properties"));
			String dynamicLog = null;
			String orgFileName = null;
			File d = null;
			File fl = null;

			dynamicLog = "Logs/Digital_PL_Log/"+logDateFormat.format(date)+"/"+logFileName+".xml";
			orgFileName = p.getProperty("log4j.appender."+logFileName+".File");
			if(!(orgFileName==null || orgFileName.equalsIgnoreCase("")))
			{
				dynamicLog = orgFileName.substring(0,orgFileName.lastIndexOf("/")+1)+logDateFormat.format(date)+orgFileName.substring(orgFileName.lastIndexOf("/"));
				
			}
			d = new File(dynamicLog.substring(0,dynamicLog.lastIndexOf("/")));
			d.mkdirs();
			fl = new File(dynamicLog);
			if(!fl.exists())
				fl.createNewFile();
			
			p.put("log4j.category."+logFileName, "ALL,"+logFileName);
			p.put("log4j.additivity."+logFileName,p.getProperty("log4j.additivity.Digital_PL_Log"));
			p.put("log4j.appender."+logFileName,p.getProperty("log4j.appender.Digital_PL_Log"));
			p.put("log4j.appender."+logFileName+".File", dynamicLog );
			p.put("log4j.appender."+logFileName+".MaxFileSize",p.getProperty("log4j.appender.Digital_PL_Log.MaxFileSize"));
			p.put("log4j.appender."+logFileName+".MaxBackupIndex",p.getProperty("log4j.appender.Digital_PL_Log.MaxBackupIndex"));
			p.put("log4j.appender."+logFileName+".layout",p.getProperty("log4j.appender.Digital_PL_Log.layout"));
			p.put("log4j.appender."+logFileName+".layout.ConversionPattern",p.getProperty("log4j.appender.Digital_PL_Log.layout.ConversionPattern"));
			
			PropertyConfigurator.configure(p);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    }

}
