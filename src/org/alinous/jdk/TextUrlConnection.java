package org.alinous.jdk;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


public class TextUrlConnection extends URLConnection{

	private byte[] byteData;
	
	protected TextUrlConnection(URL url, byte[] byteData)
	{
		super(url);
		this.byteData = byteData;
	}
	
	@Override
	public void connect() throws IOException {

	}
	
	@Override
	public InputStream getInputStream() throws IOException
	{
		ByteArrayInputStream stream = new ByteArrayInputStream(this.byteData);
		
		return stream;
	}

	@Override
	public String getContentType() {
		// 
		//AlinousDebug.debugOut("getContentType()");
		
		return "text/plain";
	}

	@Override
	public boolean getAllowUserInteraction() {
		return super.getAllowUserInteraction();
	}
	
	

}
