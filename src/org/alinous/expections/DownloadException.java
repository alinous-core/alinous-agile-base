package org.alinous.expections;

public class DownloadException extends AlinousException
{

	private static final long serialVersionUID = 1L;
	
	private String downloadAddress;
	private String contentType;
	private String fileName;
	
	public DownloadException(String downloadAddress)
	{
		this.downloadAddress = downloadAddress;
	}
	
	public String getRedirectAddress()
	{
		return downloadAddress;
	}

	public String getContentType()
	{
		return contentType;
	}

	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}
	
}

