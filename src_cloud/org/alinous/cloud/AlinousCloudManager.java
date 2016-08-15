package org.alinous.cloud;

import java.io.File;
import java.io.IOException;

import org.alinous.cloud.file.IAlinousFileInputStream;
import org.alinous.cloud.file.IAlinousFileOutputStream;
import org.alinous.cloud.file.IAlinousFileSystemFactory;
import org.alinous.cloud.file.IAlinousFileSystemObject;
import org.alinous.datasrc.basic.ILogProvidor;

public class AlinousCloudManager {
	private static AlinousCloudManager instance;
	private static IAlinousFileSystemFactory fileFactory;
	
	private AlinousCloudManager(){}
	
	private boolean coudEnabled = false;
	private String cloudType = "none";
	private String cloudFileRoot = "/";
	private boolean cloudThreadEnabled = true;
	private ILogProvidor logProvidor;
	
	
	public static void setFileFactory(IAlinousFileSystemFactory fc)
	{
		AlinousCloudManager.fileFactory = fc;
	}
	
	public IAlinousFileSystemObject newFileObject(String path)
	{
		return AlinousCloudManager.fileFactory.getFileObject(path);
	}
	
	public IAlinousFileInputStream newFileInputStream(File file)
	{
		return AlinousCloudManager.fileFactory.getFileInputStream(file);
	}
	
	public IAlinousFileOutputStream newFileOutputStream(File file) throws IOException
	{
		return AlinousCloudManager.fileFactory.getFileOutputStream(file);
	}
	
	public static AlinousCloudManager getInstance()
	{
		if(instance == null){
			instance = new AlinousCloudManager();
		}
		
		return instance;
	}


	public boolean isCoudEnabled() {
		return coudEnabled;
	}


	public void setCoudEnabled(boolean coudEnabled) {
		this.coudEnabled = coudEnabled;
	}


	public String getCloudType() {
		return cloudType;
	}


	public void setCloudType(String cloudType) {
		this.cloudType = cloudType;
	}

	public String getCloudFileRoot() {
		return cloudFileRoot;
	}

	public void setCloudFileRoot(String cloudFileRoot) {
		this.cloudFileRoot = cloudFileRoot;
	}

	public boolean isCloudThreadEnabled()
	{
		return cloudThreadEnabled;
	}

	public void setCloudThreadEnabled(boolean cloudThreadEnabled)
	{
		this.cloudThreadEnabled = cloudThreadEnabled;
	}

	public ILogProvidor getLogProvidor()
	{
		return logProvidor;
	}

	public void setLogProvidor(ILogProvidor logProvidor)
	{
		this.logProvidor = logProvidor;
	}


	
}
