package org.alinous.filter;

import java.io.PrintWriter;

public class SessionIdRewriteArea {
	private String area;
	private String param;
	
	public void writeAsString(PrintWriter wr)
	{
		String paramStr = "";
		if(this.param != null && !this.param.equals("")){
			paramStr = " param=\"" + this.param + "\"";
		}
		
		wr.print("	<area" + paramStr + ">" + this.area + "</area>\n");
	}
	
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}
	
}
