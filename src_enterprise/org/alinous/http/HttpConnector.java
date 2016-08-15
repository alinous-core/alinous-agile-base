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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpConnector
{
	private StringBuffer strBuffer = new StringBuffer();
	protected Map<String, List<String>> headers;
	
	public void access(String urlStr, String method, Map<String, String> postParams,
				String encode, String userAgent, String referer, CookieManager manager) throws URISyntaxException, IOException, KeyManagementException, NoSuchAlgorithmException
	{
		if(urlStr.startsWith("https:")){
			accessHttps(urlStr, method, postParams, encode, userAgent, referer, manager);
			return;
		}
		URL url = new URL(urlStr);
		
		HttpURLConnection httpConnecton = null;
		InputStreamReader reader = null;
		try{
			httpConnecton = (HttpURLConnection) url.openConnection();
			
			if(userAgent != null && !userAgent.equals("")){
				httpConnecton.setRequestProperty("User-Agent", userAgent);
			}
			if(referer != null && !referer.equals("")){
				httpConnecton.setRequestProperty("Referer", referer);
			}
			
			
			httpConnecton.setRequestMethod(method);
			if(method.toUpperCase().equalsIgnoreCase("POST")){
				httpConnecton.setDoInput(true);
				httpConnecton.setDoOutput(true);
								
				handlePostParams(httpConnecton, postParams, encode);
			}
			
			httpConnecton.connect();
			
			// headers
			handleHeaders(httpConnecton);
			
			reader = new InputStreamReader(httpConnecton.getInputStream(), encode);
			
			this.strBuffer = new StringBuffer();
			
			char buffer[] = new char[256];
			int n = 0;
			do{
				n = reader.read(buffer);
				if(n > 0){
					this.strBuffer.append(buffer, 0, n);
				}
			}while(n > 0);
			
			
		}finally{
			if(reader != null){
				reader.close();
			}
			if(httpConnecton != null){
				httpConnecton.disconnect();
			}
		}
		
		
	}
	
	protected void accessHttps(String urlStr, String method, Map<String, String> postParams,
			String encode, String userAgent, String referer, CookieManager manager) throws NoSuchAlgorithmException, KeyManagementException, IOException
	{
		TrustManager[] tm = { new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		}};
		
		HttpsURLConnection httpConnecton = null;
		InputStreamReader reader = null;
		
        try {
        	SSLContext sslcontext = SSLContext.getInstance("SSL");
			sslcontext.init(null, tm, null);
			
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
 				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}
            });
			
			
			URL url = new URL(urlStr);
			
			httpConnecton = (HttpsURLConnection) url.openConnection();
			
			if(userAgent != null && !userAgent.equals("")){
				httpConnecton.setRequestProperty("User-Agent", userAgent);
			}
			if(referer != null && !referer.equals("")){
				httpConnecton.setRequestProperty("Referer", referer);
			}
			
			
			httpConnecton.setRequestMethod(method);
			if(method.toUpperCase().equalsIgnoreCase("POST")){
				httpConnecton.setDoInput(true);
				httpConnecton.setDoOutput(true);
								
				handlePostParams(httpConnecton, postParams, encode);
			}
			
			httpConnecton.setSSLSocketFactory(sslcontext.getSocketFactory());
			
			httpConnecton.connect();
			
			
			// headers
			handleHeaders(httpConnecton);
			
			reader = new InputStreamReader(httpConnecton.getInputStream(), encode);
			
			this.strBuffer = new StringBuffer();
			
			char buffer[] = new char[256];
			int n = 0;
			do{
				n = reader.read(buffer);
				if(n > 0){
					this.strBuffer.append(buffer, 0, n);
				}
			}while(n > 0);
		} finally{
			if(reader != null){
				reader.close();
			}
			if(httpConnecton != null){
				httpConnecton.disconnect();
			}
		}
        
	}
	
	protected void handleHeaders(HttpURLConnection httpConnecton)
	{
		this.headers = httpConnecton.getHeaderFields();
	}
	
	public Iterator<String> getHeaderIterator()
	{
		return this.headers.keySet().iterator();
	}
	
	public List<String> getHeaderValue(String key)
	{
		return this.headers.get(key);
	}
	
	private void handlePostParams(HttpURLConnection httpConnecton, Map<String, String> postParams,
			String encode) throws IOException
	{
		OutputStreamWriter osw = null;
		try {
			osw = new OutputStreamWriter(httpConnecton.getOutputStream(), encode);
			
			boolean first = true;
			Iterator<String> it = postParams.keySet().iterator();
			while(it.hasNext()){
				String key = it.next();
				
				if(first){
					first = false;
				}else{
					osw.write("&");
				}
				
				osw.write(URLEncoder.encode(key , encode));
				osw.write("=");
				osw.write(URLEncoder.encode(postParams.get(key), encode));
				
			}
			
			osw.flush();
		} finally{
			if(osw != null){
				osw.close();
			}
		}
	}

	public StringBuffer getStrBuffer()
	{
		return strBuffer;
	}
	
	public static void main(String[] args) {
		HttpConnector connector = new HttpConnector();
		
		String urlString = "https://www.alinous.org/";
		Map<String, String> params = new HashMap<String, String>();
		
		try {
			connector.access(urlString, "GET", params, "utf-8", null, null, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
				e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	
	}
	
	
}
