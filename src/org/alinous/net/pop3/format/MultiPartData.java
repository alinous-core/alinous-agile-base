package org.alinous.net.pop3.format;

import java.util.Hashtable;
import java.util.Map;

public class MultiPartData implements IPopMailData
{
	private Map<String, MailHeader> headers = new Hashtable<String, MailHeader>();
	
	private StringBuffer mimeRowData = new StringBuffer();
	
	public MultiPartData()
	{
		
	}

	public Map<String, MailHeader> getHeaders()
	{
		return headers;
	}
	
	public void appendMimeData(String mimeData)
	{
		this.mimeRowData.append(mimeData);
		this.mimeRowData.append("\n");
	}


}
