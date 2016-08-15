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
package org.alinous.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alinous.cloud.file.AlinousFile;
import org.alinous.cloud.file.AlinousFileOutputStream;

public class HttpDownloadConnector
{
	protected Map<String, List<String>> headers;
	
	public void download(String urlStr, String method, Map<String, String> postParams, String outFile
			, String userAgent, String referer, CookieManager manager)
		throws URISyntaxException, IOException
	{
		// download
		
		URL url = new URL(urlStr);
		HttpURLConnection httpConnecton = null;
		OutputStream outStream = null;
		try{
			httpConnecton = (HttpURLConnection) url.openConnection();
			
			httpConnecton.setRequestMethod(method);
			if(method.toUpperCase().equalsIgnoreCase("POST")){
				httpConnecton.setDoInput(true);
				httpConnecton.setDoOutput(true);
				
				handlePostParams(httpConnecton, postParams);
			}
			
			if(userAgent != null && !userAgent.equals("")){
				httpConnecton.setRequestProperty("User-Agent", userAgent);
			}
			if(referer != null && !referer.equals("")){
				httpConnecton.setRequestProperty("Referer", referer);
			}
			
			httpConnecton.connect();
			
			// headers
			handleHeaders(httpConnecton);
			
			AlinousFile outF = new AlinousFile(outFile);
			outStream = new AlinousFileOutputStream(outF);
			InputStream inStream = httpConnecton.getInputStream();
			byte buffer[] = new byte[1024];
			int n = 0;
			do{
				n = inStream.read(buffer);
				if(n > 0){
					outStream.write(buffer, 0, n);
				}
			}while(n > 0);
		}
		finally{
			if(httpConnecton != null){
				httpConnecton.disconnect();
			}
			if(outStream != null){
				try{
					outStream.close();
				}catch(Throwable ignore){}
			}
		}
		
	}
	
	protected void handleHeaders(HttpURLConnection httpConnecton)
	{
		this.headers = httpConnecton.getHeaderFields();
	}
	
	private void handlePostParams(HttpURLConnection httpConnecton, Map<String, String> postParams) throws IOException
	{
		OutputStreamWriter osw = null;
		try {
			osw = new OutputStreamWriter(httpConnecton.getOutputStream());
			
			boolean first = true;
			Iterator<String> it = postParams.keySet().iterator();
			while(it.hasNext()){
				String key = it.next();
				
				if(first){
					first = false;
				}else{
					osw.write("&");
				}
				
				osw.write(key);
				osw.write("=");
				osw.write(postParams.get(key));
			}
			
			osw.flush();
		} finally{
			if(osw != null){
				osw.close();
			}
		}
	}

	public Map<String, List<String>> getHeaders()
	{
		return headers;
	}
	
	
}
