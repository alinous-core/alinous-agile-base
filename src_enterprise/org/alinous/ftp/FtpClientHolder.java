package org.alinous.ftp;

import java.sql.Timestamp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPHTTPClient;

public class FtpClientHolder
{
	private FTPClient ftp;
	private Timestamp lastUpdate;
	
	public FtpClientHolder()
	{
		long mill = System.currentTimeMillis();
		this.lastUpdate = new Timestamp(mill);
	}
	
	public FTPClient getFtp()
	{
		if(this.ftp == null){
			this.ftp = new FTPClient();
		}
		
		long mill = System.currentTimeMillis();
		this.lastUpdate = new Timestamp(mill);
		return ftp;
	}
	public FTPClient createHttpFtp(String connectUrl, int proxyPort, String username, String password)
	{
		if(this.ftp == null){
			this.ftp = new FTPHTTPClient(connectUrl, proxyPort, username, password);
		}
		
		long mill = System.currentTimeMillis();
		this.lastUpdate = new Timestamp(mill);
		return ftp;
	}
	
	public void setFtp(FTPClient ftp)
	{
		this.ftp = ftp;
		long mill = System.currentTimeMillis();
		
		this.lastUpdate = new Timestamp(mill);
	}
	public Timestamp getLastUpdate()
	{
		return lastUpdate;
	}
	
	public boolean isExpired(Timestamp expire)
	{
		return this.lastUpdate.after(expire);
	}
	
	
}
