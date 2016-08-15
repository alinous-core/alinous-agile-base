/**
 * CROSSFIRE JAPAN INCORPORATED
 * This source code is under GPL License.
 * info@crossfire.jp
 * Official web site
 * http://alinous.org
 * 
 *  Copyright (C) 2007 Tomohiro Iizuka
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.alinous.net.mail;

import java.io.PrintWriter;

public class AlinousMailConfig
{
	private String server;
	private int port;
	
	// Language code
	private String langCode = "ISO2022JP";
	
	// authentication
	private String authMethod;
	private String authUser;
	private String authPass;
	private String authDomain;
	
	private String popMethod;
	private String popUser;
	private String popPass;
	private String popServer;
	
	// debug
	private boolean debug = false;
	private String debugReceiver;
	
	public String getAuthMethod()
	{
		return authMethod;
	}
	public void setAuthMethod(String authMethod)
	{
		this.authMethod = authMethod;
	}
	public String getAuthPass()
	{
		return authPass;
	}
	public void setAuthPass(String authPass)
	{
		this.authPass = authPass;
	}
	public String getAuthUser()
	{
		return authUser;
	}
	public void setAuthUser(String authUser)
	{
		this.authUser = authUser;
	}
	public String getLangCode()
	{
		return langCode;
	}
	public void setLangCode(String langCode)
	{
		this.langCode = langCode;
	}
	public int getPort()
	{
		return port;
	}
	public void setPort(int port)
	{
		this.port = port;
	}
	public String getServer()
	{
		return server;
	}
	public void setServer(String server)
	{
		this.server = server;
	}
	public String getPopMethod()
	{
		return popMethod;
	}
	public void setPopMethod(String popMethod)
	{
		this.popMethod = popMethod;
	}
	public String getPopPass()
	{
		return popPass;
	}
	public void setPopPass(String popPass)
	{
		this.popPass = popPass;
	}
	public String getPopUser()
	{
		return popUser;
	}
	public void setPopUser(String popUser)
	{
		this.popUser = popUser;
	}
	public void writeAsString(PrintWriter wr)
	{
		wr.println("	<mail");
		wr.print(" debug=\"");
		wr.print(Boolean.toString(this.debug));
		wr.print("\"");
		
		if(this.debugReceiver != null){
			wr.print(" receiver=\"");
			wr.print(this.debugReceiver);
			wr.print("\"");
		}
		
		wr.print(">");
		
		
		// server setting
		wr.print("		<server>");
		wr.print(this.server);
		wr.println("</server>");
		
		wr.print("		<port>");
		wr.print(this.port);
		wr.println("</port>");
		
		if(this.langCode != null){
			wr.print("		<lang-encode>");
			wr.print(this.langCode);
			wr.println("</lang-encode>");
		}
		
		if(this.authMethod != null && this.authUser != null && this.authPass != null){
			wr.println("		<auth>");
			
			wr.print("			<method>");
			wr.print(this.authMethod);
			wr.println("</method>");
			
			wr.print("			<user>");
			wr.print(this.authUser);
			wr.println("</user>");
			
			wr.print("			<pass>");
			wr.print(this.authPass);
			wr.println("</pass>");
			
			wr.println("		</auth>");
		}
		
		
		if(this.popMethod != null && this.popUser != null && this.popPass != null){
			wr.println("		<pop-auth>");
			
			wr.print("			<method>");
			wr.print(this.popMethod);
			wr.println("</method>");
			
			wr.print("			<user>");
			wr.print(this.popUser);
			wr.println("</user>");
			
			wr.print("			<pass>");
			wr.print(this.popPass);
			wr.println("</pass>");
			
			wr.println("		</pop-auth>");
		}
		
		wr.println("	</mail>");
		
	}
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public String getDebugReceiver() {
		return debugReceiver;
	}
	public void setDebugReceiver(String debugReceiver) {
		this.debugReceiver = debugReceiver;
	}
	public String getPopServer()
	{
		return popServer;
	}
	public void setPopServer(String popServer)
	{
		this.popServer = popServer;
	}
	public String getAuthDomain()
	{
		return authDomain;
	}
	public void setAuthDomain(String authDomain)
	{
		this.authDomain = authDomain;
	}
	
}
