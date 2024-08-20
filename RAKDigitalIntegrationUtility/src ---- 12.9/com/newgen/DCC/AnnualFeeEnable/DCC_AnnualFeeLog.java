package com.newgen.DCC.AnnualFeeEnable;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.apache.log4j.*;

import org.apache.log4j.PropertyConfigurator;

public class DCC_AnnualFeeLog {
	private static String loggerName = "DCC_AnnualFee_Log";
    protected static org.apache.log4j.Logger DCC_AnnualFeeLogger = org.apache.log4j.Logger.getLogger(loggerName);;

    static
    {
    	setLogger();
    }

    protected static void setLogger()
    {
    	try
		{
    		Date date = new Date();
			DateFormat logDateFormat = new SimpleDateFormat("dd-MM-yyyy");
			Properties p = new Properties();
			p.load(new FileInputStream(System.getProperty("user.dir")+ File.separator + "log4jFiles"+ File.separator+ "DCC_AnnualFee_Log4j.properties"));
			String dynamicLog = null;
			String orgFileName = null;
			File d = null;
			File fl = null;

			dynamicLog = "Logs/DCC_AnnualFee_Log/"+logDateFormat.format(date)+"/DCC_AnnualFee_Log.xml";
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
