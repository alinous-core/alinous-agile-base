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
package org.alinous.net.pop3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Pop3Protocol
{
	private String popServer;
	private int popPort = 110;
	private Socket con;
	
	private String user;
	private String pass;
	
	private OutputStream outputStream = null;
	private InputStream inputStream = null;
	
	public Pop3Protocol()
	{
		
	}
	
	public void connect() throws UnknownHostException, IOException
	{
		con = new Socket(this.popServer, this.popPort);
		
	}
	
	public void disconnect()
	{
		if(this.outputStream != null){
			try {
				this.outputStream.close();
			} catch (IOException e) {}
		}
		
		if(this.inputStream != null){
			try {
				this.inputStream.close();
			} catch (IOException e) {}
		}
		
		try {
			con.close();
		} catch (IOException ignore) {}
	}
	
	
	public int getPopPort()
	{
		return popPort;
	}
	public void setPopPort(int popPort)
	{
		this.popPort = popPort;
	}
	public String getPopServer()
	{
		return popServer;
	}
	public void setPopServer(String popServer)
	{
		this.popServer = popServer;
	}
	public String getPass()
	{
		return pass;
	}
	public void setPass(String pass)
	{
		this.pass = pass;
	}
	public String getUser()
	{
		return user;
	}
	public void setUser(String user)
	{
		this.user = user;
	}

	public Socket getCon()
	{
		return con;
	}

	public OutputStream getOutputStream() throws IOException
	{
		if(this.outputStream == null){
			this.outputStream = this.con.getOutputStream();
		}
		return outputStream;
	}

	public InputStream getInputStream() throws IOException
	{
		if(this.inputStream == null){
			this.inputStream = this.con.getInputStream();
		}
		return inputStream;
	}
	
	
}
