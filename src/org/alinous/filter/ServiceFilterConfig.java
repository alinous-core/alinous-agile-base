package org.alinous.filter;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;

public class ServiceFilterConfig
{
	private String serviceFilterClass;
	private HashMap<String, String> params = new HashMap<String, String>();
	
	public void writeAsString(PrintWriter wr)
	{
		wr.print("	<service-filter>\n");
		wr.print("		<filter-class>" + this.serviceFilterClass + "</filter-class>\n");
		wr.print("		<params>\n");
		
		Iterator<String> it = this.params.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			String value = this.params.get(key);
			
			wr.print("		<" + key + ">" + value + "</" + key + ">\n");
		}
		
		wr.print("		</params>\n");
		wr.print("	</service-filter>\n");
	}
	
	public String getServiceFilterClass()
	{
		return serviceFilterClass;
	}

	public void setServiceFilterClass(String serviceFilterClass)
	{
		this.serviceFilterClass = serviceFilterClass;
	}

	public HashMap<String, String> getParams() {
		return params;
	}
	
	
	
}
