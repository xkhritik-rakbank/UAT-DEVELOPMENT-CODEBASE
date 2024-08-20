package com.newgen.DPL.prime;

import javax.inject.Inject;

import com.newgen.DPL.Digital_PL_CommomMethod;
import com.newgen.DPL.Digital_PL_Log;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.omni.wf.util.excp.NGException;

public class Prime_cbs extends Digital_PL_Log {

	
	private static String queueID = null;

	static NGEjbClient ngEjbClient;
	
	private static org.apache.log4j.Logger logger;
	
	@Inject
	Digital_PL_CommomMethod commomMethod;// = new Digital_PL_CommomMethod();
	private String NOTIFY_DEH_IDENTIFIER="";
	private String STL_Notify_Count="";

	Prime_cbs()  throws NGException{
		
		Digital_PL_Log.setLogger(getClass().getSimpleName());
		this.ngEjbClient = NGEjbClient.getSharedInstance();
		logger = Digital_PL_Log.getLogger(getClass().getSimpleName());
	}
}
