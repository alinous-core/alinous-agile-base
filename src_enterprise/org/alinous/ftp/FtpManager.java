package org.alinous.ftp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alinous.cloud.file.AlinousFile;
import org.alinous.cloud.file.AlinousFileInputStream;
import org.alinous.cloud.file.AlinousFileOutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;


public class FtpManager
{
	private Map<String, FtpClientHolder> instances = new HashMap<String, FtpClientHolder>();
	
	private static final Pattern PWD_REPLY_MESSAGE = Pattern.compile("257 .*\"(.*)\".*");
	
	private static FtpManager inst = null;
	public static FtpManager getInstance()
	{
		if(FtpManager.inst == null){
			FtpManager.inst = new FtpManager();
		}
		
		return FtpManager.inst;
	}
	
	public void maintain()
	{
		ArrayList<String> removeKeys = new ArrayList<String>();
		
		long mill = System.currentTimeMillis();
		Timestamp expire = new Timestamp(mill);
		
		Iterator<String> it = this.instances.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			FtpClientHolder hd = this.instances.get(key);
			
			if(hd.isExpired(expire)){
				removeKeys.add(key);
			}
		}
		
		it = removeKeys.iterator();
		while(it.hasNext()){
			String key = it.next();
			
			this.instances.remove(key);
		}
		
	}
	
	public void connect(String sessionId, String connectUrl, int port) throws IOException
	{
		FtpClientHolder holder = new FtpClientHolder();
		this.instances.put(sessionId, holder);
		
		FTPClient ftp = holder.getFtp();

		ftp.connect(connectUrl, port);
		
		//ftp.enterRemoteActiveMode(InetAddress.getByName(connectUrl), port);
	}
	
	public void connect(String sessionId, String connectUrl, int proxyPort, String username, String password) throws SocketException, IOException
	{
		FtpClientHolder holder = new FtpClientHolder();
		this.instances.put(sessionId, holder);
		
		FTPClient ftp = holder.createHttpFtp(connectUrl, proxyPort, username, password);
		
		ftp.connect(connectUrl);
	}
	
	public boolean login(String sessionId, String username, String password) throws IOException
	{
		FTPClient ftp = this.instances.get(sessionId).getFtp();
		
		boolean result = ftp.login(username, password);
		
		if(!result){
			this.instances.remove(sessionId);
		}
		
		ftp.pasv();
        printFtpReply(ftp);
		
		return result;
	}
	
    private static void printFtpReply(FTPClient ftpClient){
    	 
        System.out.print(ftpClient.getReplyString());
 
        int replyCode = ftpClient.getReplyCode();
 
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            System.out.println("Filed in sending ftp command.");
        }
    }
	
	public void close(String sessionId) throws IOException
	{
		FTPClient ftp = this.instances.get(sessionId).getFtp();
		
		try{
			ftp.disconnect();
		}
		finally{
			this.instances.remove(sessionId);
		}
	}
	
	public FTPFile[] listDirectories(String sessionId) throws IOException
	{
		FTPClient ftp = this.instances.get(sessionId).getFtp();
		
		FTPFile[] files = ftp.listDirectories();
		
		return files;
	}
	
	public FTPFile[] listFiles(String sessionId) throws IOException
	{
		FTPClient ftp = this.instances.get(sessionId).getFtp();
		
		//ftp.enterRemotePassiveMode();
		/*int resN = ftp.pasv();
		System.out.println(resN);
		cur = ftp.getReplyString();
		System.out.println(cur);
		*/
		FTPFile[] files = ftp.listFiles();
		
		return files;
	}
	
	
	public void setFileType(String sessionId, String typeName) throws IOException
	{
		FTPClient ftp = this.instances.get(sessionId).getFtp();
		if(typeName.toLowerCase().equals("ascii")){
			ftp.setFileType(FTP.ASCII_FILE_TYPE);
		}
		else{
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
		}
	}
	
	public void setListHiddenFiles(String sessionId, boolean listHiddenFiles)
	{
		FTPClient ftp = this.instances.get(sessionId).getFtp();
		ftp.setListHiddenFiles(listHiddenFiles);
	}
	
	public void uploadFile(String sessionId, String path, String remote) throws IOException
	{
		AlinousFileInputStream stream = new AlinousFileInputStream(new AlinousFile(path));
		
		FTPClient ftp = this.instances.get(sessionId).getFtp();
		
		try{
			ftp.storeFile(remote, stream);
		}
		finally{
			stream.close();
		}
	}
	
	public void downloadFile(String sessionId, String remote, String path) throws IOException
	{
		FTPClient ftp = this.instances.get(sessionId).getFtp();
		
		OutputStream stream = new AlinousFileOutputStream(new AlinousFile(path));
		
		try{
			ftp.retrieveFile(remote, stream);
		}
		finally{
			stream.close();
		}
	}
	
	public void rename(String sessionId, String from, String to) throws IOException
	{
		FTPClient ftp = this.instances.get(sessionId).getFtp();
		
		ftp.rename(from, to);
	}
	
	public int changeDir(String sessionId, String remote) throws IOException
	{
		FTPClient ftp = this.instances.get(sessionId).getFtp();
		
		int b = ftp.cwd(remote);
		//String cur = ftp.getReplyString();
		
		//System.out.println(cur);
		
		return b;
	}
	
	public void deleteFile(String sessionId, String pathname) throws IOException
	{
		FTPClient ftp = this.instances.get(sessionId).getFtp();
		ftp.deleteFile(pathname);
	}
	
	public void removeDirectory(String sessionId, String pathname) throws IOException
	{
		FTPClient ftp = this.instances.get(sessionId).getFtp();
		ftp.removeDirectory(pathname);
	}
	
	public String pwd(String sessionId) throws IOException
	{
		FTPClient ftp = this.instances.get(sessionId).getFtp();
		ftp.pwd();
		String cur = ftp.getReplyString();
		
		Matcher match = PWD_REPLY_MESSAGE.matcher(cur);
		if (!match.find() || match.groupCount() != 1) {
			return null;
		}
		return match.group(1);
	}
	
	public static void main(String[] args)
	{
		FtpManager mgr = new FtpManager();
		
		String sessionId = "test";
		String host = "192.168.1.112";
		int port = 22;
		
		try {
			mgr.connect(sessionId, host, port);
			
			boolean res = mgr.login(sessionId, "administrator", "KuRaYa");
			System.out.println("Login Result : " + res);
			
			mgr.setListHiddenFiles(sessionId, false);
			
			mgr.listFiles(sessionId);
			
			String cur = mgr.pwd(sessionId);
			System.out.println(cur);
			
			mgr.changeDir(sessionId, "/ifto_ec/yotei/nyuuka");
			cur = mgr.pwd(sessionId);
			System.out.println(cur);
			
			mgr.listFiles(sessionId);
			
			mgr.uploadFile(sessionId, "GPLv3-LICENSE.txt", "GPLv3-LICENSE.txt");
			
			mgr.rename(sessionId, "GPLv3-LICENSE.txt", "test.txt");
			
			mgr.downloadFile(sessionId, "test.txt", "test.txt");
			
			FTPFile[] dirs = mgr.listFiles(sessionId);
			System.out.println("dirs Result : " + dirs);
			
			mgr.close(sessionId);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
	}
	/*
	
	public static void main(String[] args)
	{
		FtpManager mgr = new FtpManager();
		
		String sessionId = "test";
		String host = "180.222.178.159";
		
		try {
			mgr.connect(sessionId, host);
			
			boolean res = mgr.login(sessionId, "test", "marunage");
			System.out.println("Login Result : " + res);
			
			mgr.setListHiddenFiles(sessionId, false);
			
			mgr.listFiles(sessionId);
			
			String cur = mgr.pwd(sessionId);
			System.out.println(cur);
			
			mgr.changeDir(sessionId, "/home/test/newdir");
			
			mgr.listFiles(sessionId);
			
			mgr.uploadFile(sessionId, "GPLv3-LICENSE.txt", "GPLv3-LICENSE.txt");
			
			mgr.rename(sessionId, "GPLv3-LICENSE.txt", "test.txt");
			
			mgr.downloadFile(sessionId, "test.txt", "test.txt");
			
			FTPFile[] dirs = mgr.listFiles(sessionId);
			System.out.println("dirs Result : " + dirs);
			
			mgr.close(sessionId);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
	}*/
}
