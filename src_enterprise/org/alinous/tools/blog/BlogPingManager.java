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
package org.alinous.tools.blog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class BlogPingManager
{
	public static String TYPE_STRING = "string";
	
	private StringBuffer buffer = new StringBuffer();
	
	// HTTP
	private int port = 80;
	private String httpUrl;
	
	public String ping(String weblogname, String weblogurl)
	{
		return ping(weblogname, weblogurl, null, null);
	}
	
	
	public String ping(String weblogname, String weblogurl, String cangesurl, String categoryname)
	{
		addMetodCall("weblogUpdates.ping");
		
		addParamValue(TYPE_STRING, weblogname);
		addParamValue(TYPE_STRING, weblogurl);
		
		if(cangesurl != null){
			addParamValue(TYPE_STRING, cangesurl);
		}
		if(categoryname != null){
			addParamValue(TYPE_STRING, categoryname);
		}
		
		endMethodCall();
		
		String ret = null;
		
		try {
			ret = sendRequest();
		} catch (IOException e) {
			return null;
		}
		
		return ret;
	}
	
	private void addMetodCall(String methodName)
	{
		this.buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		this.buffer.append("<methodCall>\n");
		this.buffer.append("<methodName>");
		this.buffer.append(methodName);
		this.buffer.append("</methodName>\n");
		this.buffer.append("<params>\n");
	}
	
	private void endMethodCall()
	{
		this.buffer.append("</params>\n</methodCall>\n");
	}
	
	private void addParamValue(String key, String value)
	{
		this.buffer.append("<param><value>");
		this.buffer.append("<");
		this.buffer.append(key);
		this.buffer.append(">");
		this.buffer.append(value);
		this.buffer.append("</");
		this.buffer.append(key);
		this.buffer.append(">");
		this.buffer.append("</value></param>\n");
	}

	
	private String sendRequest() throws IOException
	{
		URL url = new URL(this.httpUrl);
		
		HttpURLConnection urlconn = (HttpURLConnection)url.openConnection();
		urlconn.setRequestMethod("POST");
		urlconn.setInstanceFollowRedirects(false);
		urlconn.setDoOutput(true);
		urlconn.setRequestProperty("Content-Type", "text/xml");

		StringBuffer retBuff = new StringBuffer();
		BufferedReader reader = null;
		try{
			//urlconn.connect();
			
			//System.out.println("--------------");
			//System.out.println(this.buffer.toString());
			//System.out.println("--------------");
			
			// write params
			
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(urlconn.getOutputStream(), "UTF-8"));
			writer.print(this.buffer.toString());
			writer.close();
			
			// read
			reader =
				new BufferedReader(new InputStreamReader(urlconn.getInputStream()));
			
			while (true){
				String line = reader.readLine();
				 if ( line == null ){
					 break;
				 }
				 
				 retBuff.append(line);
				 retBuff.append("\n");
			}
		}
		catch (Exception e) {
			// handle exception
			e.printStackTrace();
		}
		finally{
			try{
				reader.close();
			}catch(Throwable e){}
			
			try{
				urlconn.disconnect();
			}catch(Throwable e){}
		}
		
		return retBuff.toString();
	}
	
	public int getPort()
	{
		return port;
	}


	public void setPort(int port)
	{
		this.port = port;
	}


	public void setHttpUrl(String httpUrl)
	{
		this.httpUrl = httpUrl;
	}



	
}
