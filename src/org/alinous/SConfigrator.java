package org.alinous;

import java.util.HashMap;
import java.util.Map;

public class SConfigrator
{
	private String ver;
	private Map<String, Map<String, String> > configMap = new HashMap<String, Map<String, String> >();
	
	public static final String OPENEC_FREE = "openec-free";
	public static final String OPENEC_ENTRY = "openec-entry";
	public static final String OPENEC_LITE = "openec-basic";
	public static final String OPENEC_STD = "openec-standard";
	public static final String DEVEL = "development";
	public static final String ENTERPRISE = "enterprise";
	
	public static final String usePrecompileSelect = "usePrecompileSelect";
	public static final String usePrecompileUpdate = "usePrecompileUpdate";
	public static final String usePrecompileInsert = "usePrecompileInsert";
	public static final String usePrecompileDelete = "usePrecompileDelete";
	
	public SConfigrator(String ver)
	{
		this.ver = ver;
		
		// FREE
		HashMap<String, String> m = new HashMap<String, String>();
		m.put("debug", "false");
		m.put("showad", "false");
		m.put("adImg", "");
		m.put("adUrl", "");
		m.put("adAlt", "");
		//m.put("DB_INTERCEPTOR_CLASS", AlinousCore.DB_MIN_INTERCEPTOR);
		m.put("enableVirtualHost", "true");
		m.put("enterprise", "false");
		
		m.put(usePrecompileSelect, "true");
		m.put(usePrecompileUpdate, "true");
		m.put(usePrecompileInsert, "true");
		m.put(usePrecompileDelete, "true");
		
		m.put("coverageTest", "false");
		
		this.configMap.put(OPENEC_FREE, m);
		
		// ENTRY
		m = new HashMap<String, String>();
		m.put("debug", "false");
		m.put("showad", "false");
		m.put("adImg", "");
		m.put("adUrl", "");
		m.put("adAlt", "");
		m.put("DB_INTERCEPTOR_CLASS", AlinousCore.DB_ENTRY_INTERCEPTOR);
		m.put("enableVirtualHost", "false");
		m.put("enterprise", "false");
		
		m.put("coverageTest", "false");
		
		this.configMap.put(OPENEC_ENTRY, m);
		
		// LITE
		m = new HashMap<String, String>();
		m.put("debug", "false");
		m.put("showad", "false");
		m.put("adImg", "");
		m.put("adUrl", "");
		m.put("adAlt", "");
		//m.put("DB_INTERCEPTOR_CLASS", AlinousCore.DB_LITE_INTERCEPTOR);
		m.put("enableVirtualHost", "false");
		m.put("enterprise", "false");
		
		m.put("coverageTest", "false");
		
		this.configMap.put(OPENEC_LITE, m);
		
		// OPENEC_STD
		m = new HashMap<String, String>();
		m.put("debug", "false");
		m.put("showad", "false");
		m.put("adImg", "");
		m.put("adUrl", "");
		m.put("adAlt", "");
		//m.put("DB_INTERCEPTOR_CLASS", AlinousCore.DB_LITE_INTERCEPTOR); null
		m.put("enableVirtualHost", "false");
		m.put("enterprise", "false");
		
		m.put("coverageTest", "false");
		
		this.configMap.put(OPENEC_STD, m);
		
		// DEVEL
		m = new HashMap<String, String>();
		m.put("debug", "true");
		m.put("showad", "false");
		m.put("adImg", "");
		m.put("adUrl", "");
		m.put("adAlt", "");
		//m.put("DB_INTERCEPTOR_CLASS", AlinousCore.DB_LITE_INTERCEPTOR); null
		m.put("enableVirtualHost", "true");
		m.put("enterprise", "true");

		m.put(usePrecompileSelect, "true");
		m.put(usePrecompileUpdate, "true");
		m.put(usePrecompileInsert, "true");
		m.put(usePrecompileDelete, "true");
		
		m.put("coverageTest", "true");

		
		this.configMap.put(DEVEL, m);
		
		// ENTERPRISE
		m = new HashMap<String, String>();
		m.put("debug", "false");
		m.put("showad", "false");
		m.put("adImg", "");
		m.put("adUrl", "");
		m.put("adAlt", "");
		//m.put("DB_INTERCEPTOR_CLASS", AlinousCore.DB_LITE_INTERCEPTOR); null
		m.put("enableVirtualHost", "true");
		m.put("enterprise", "true");
		
		m.put(usePrecompileSelect, "true");
		m.put(usePrecompileUpdate, "true");
		m.put(usePrecompileInsert, "true");
		m.put(usePrecompileDelete, "true");
		
		m.put("coverageTest", "false");
		
		this.configMap.put(ENTERPRISE, m);
	}
	
	public boolean getUsePrecompileSelect()
	{
		return returnBool(SConfigrator.usePrecompileSelect);
	}
	
	public boolean getUsePrecompileDelete()
	{
		return returnBool(SConfigrator.usePrecompileDelete);
	}
	
	public boolean getUsePrecompileInsert()
	{
		return returnBool(SConfigrator.usePrecompileInsert);
	}
	public boolean getUsePrecompileUpdate()
	{
		return returnBool(SConfigrator.usePrecompileUpdate);
	}
	
	public boolean getEnterprise()
	{
		return returnBool("enterprise");
	}
	
	public boolean getEnableVirtualHost()
	{
		return returnBool("enableVirtualHost");
	}
	
	public String getAdAlt()
	{
		return returnString("adAlt");
	}
	
	public String getAdUrl()
	{
		return returnString("adUrl");
	}
	
	public String getAdImg()
	{
		return returnString("adImg");
	}
	
	public boolean getShowad()
	{
		return returnBool("showad");
	}
	
	public boolean getDebug()
	{
		return returnBool("debug");
	}
	
	public String getInterceptor()
	{
		return returnString("DB_INTERCEPTOR_CLASS");
	}
	
	public boolean getCoverageTest()
	{
		return returnBool("coverageTest");
	}
	
	private String returnString(String key)
	{
		Map<String, String> m = this.configMap.get(this.ver);
		return m.get(key);
	}
	
	private boolean returnBool(String key)
	{
		Map<String, String> m = this.configMap.get(this.ver);
		String bl = m.get(key);
		
		if(bl == null){
			return false;
		}
		return Boolean.parseBoolean(bl);
	}
	
}
