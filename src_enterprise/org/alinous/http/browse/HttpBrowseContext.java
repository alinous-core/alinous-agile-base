package org.alinous.http.browse;

public class HttpBrowseContext
{
	private StringBuffer body;
	private String sessionId;
	
	public StringBuffer getBody()
	{
		return body;
	}
	public void setBody(StringBuffer body)
	{
		this.body = body;
	}
	public String getSessionId()
	{
		return sessionId;
	}
	public void setSessionId(String sessionId)
	{
		this.sessionId = sessionId;
	}
	
	
}
