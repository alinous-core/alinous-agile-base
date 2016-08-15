package org.alinous.cloud.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

import org.alinous.cloud.AlinousCloudManager;

public class AlinousFile extends File {
	private IAlinousFileSystemObject cloudFileObj;
	
	public AlinousFile(String pathname)
	{
		super(pathname);
		
		out("AlinousFile(String pathname) : " + pathname);
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			this.cloudFileObj = AlinousCloudManager.getInstance().newFileObject(pathname);
		}
	}
	
	
	public AlinousFile(File parent, String child)
	{
		super(parent, child);
		
		out("AlinousFile(File parent, String child) : child :" + child + " parent : " + parent.getPath());
		
	
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			this.cloudFileObj = AlinousCloudManager.getInstance().newFileObject(getPath());
		}
		
	}

	 public AlinousFile(String parent, String child)
	 {
		 super(parent, child);
		 
		 out("AlinousFile(String parent, String child) : child :" + child + " parent : " + parent);
		 
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			this.cloudFileObj = AlinousCloudManager.getInstance().newFileObject(super.getPath());
		}
		 
	 }
	 
	 public AlinousFile(String name, File parent)
	 {
		 super(parent, name);
		 
		 out("AlinousFile(String parent, String child) : child :" + name + " parent : " + parent.getPath());
		 
		 if(AlinousCloudManager.getInstance().isCoudEnabled()){
			 this.cloudFileObj = AlinousCloudManager.getInstance().newFileObject(getPath());
		 }
	 }
	/**
	 * 
	 */
	private static final long serialVersionUID = 3910452778258949133L;

	@Override
	public String getName() {
		functionCalled("getName");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			this.cloudFileObj.getName();
		}
		return super.getName();
	}

	@Override
	public String getParent() {
		functionCalled("getParent");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			this.cloudFileObj.getParent();
		}
		return super.getParent();
	}

	@Override
	public File getParentFile() {
		functionCalled("getPath");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.getParentFile();
		}
		return super.getParentFile();
	}

	@Override
	public String getPath() {
		functionCalled("getPath");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.getPath();
		}
		return super.getPath();
	}

	@Override
	public boolean isAbsolute() {
		functionCalled("isAbsolute");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.isAbsolute();
		}
		return super.isAbsolute();
	}

	@Override
	public String getAbsolutePath() {
		functionCalled("getAbsolutePath");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.getAbsolutePath();
		}
		return super.getAbsolutePath();
	}

	@Override
	public File getAbsoluteFile() {
		functionCalled("getAbsoluteFile");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.getAbsoluteFile();
		}
		return super.getAbsoluteFile();
	}

	@Override
	public String getCanonicalPath() throws IOException {
		functionCalled("getCanonicalPath");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.getCanonicalPath();
		}
		return super.getCanonicalPath();
	}

	@Override
	public File getCanonicalFile() throws IOException {
		functionCalled("getCanonicalFile");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.getCanonicalFile();
		}
		return super.getCanonicalFile();
	}

	@SuppressWarnings("deprecation")
	@Override
	public URL toURL() throws MalformedURLException {
		functionCalled("toURL");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.toURL();
		}
		return super.toURL();
	}

	@Override
	public URI toURI() {
		functionCalled("toURI");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.toURI();
		}
		return super.toURI();
	}

	@Override
	public boolean canRead() {
		functionCalled("canRead");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.canRead();
		}
		return super.canRead();
	}

	@Override
	public boolean canWrite() {
		functionCalled("canWrite");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.canWrite();
		}
		return super.canWrite();
	}

	@Override
	public boolean exists() {
		functionCalled("exists");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.exists();
		}
		return super.exists();
	}

	@Override
	public boolean isDirectory() {
		functionCalled("isDirectory");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.isDirectory();
		}
		return super.isDirectory();
	}

	@Override
	public boolean isFile() {
		functionCalled("isFile");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.isFile();
		}
		return super.isFile();
	}

	@Override
	public boolean isHidden() {
		functionCalled("isHidden");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.isHidden();
		}
		return super.isHidden();
	}

	@Override
	public long lastModified() {
		functionCalled("lastModified");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.lastModified();
		}
		return super.lastModified();
	}

	@Override
	public long length() {
		functionCalled("length");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.length();
		}
		return super.length();
	}

	@Override
	public boolean createNewFile() throws IOException {
		functionCalled("createNewFile");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.createNewFile();
		}
		return super.createNewFile();
	}

	@Override
	public boolean delete() {
		functionCalled("delete");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.delete();
		}
		return super.delete();
	}

	@Override
	public void deleteOnExit() {
		functionCalled("deleteOnExit");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			this.cloudFileObj.deleteOnExit();
		}
		super.deleteOnExit();
	}

	@Override
	public String[] list() {
		functionCalled("list");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.list();
		}
		return super.list();
	}

	@Override
	public String[] list(FilenameFilter filter) {
		functionCalled("list");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.list(filter);
		}
		return super.list(filter);
	}

	@Override
	public File[] listFiles() {
		functionCalled("listFiles");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.listFiles();
		}
		return super.listFiles();
	}

	@Override
	public File[] listFiles(FilenameFilter filter) {
		functionCalled("listFiles");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.listFiles(filter);
		}
		return super.listFiles(filter);
	}

	@Override
	public File[] listFiles(FileFilter filter) {
		functionCalled("filter");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.listFiles(filter);
		}
		return super.listFiles(filter);
	}

	@Override
	public boolean mkdir() {
		functionCalled("mkdir");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.mkdir();
		}
		return super.mkdir();
	}

	@Override
	public boolean mkdirs() {
		functionCalled("mkdirs");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.mkdirs();
		}
		return super.mkdirs();
	}

	@Override
	public boolean renameTo(File dest) {
		functionCalled("renameTo");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.renameTo(dest);
		}
		return super.renameTo(dest);
	}

	@Override
	public boolean setLastModified(long time) {
		functionCalled("setLastModified");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.setLastModified(time);
		}
		return super.setLastModified(time);
	}

	@Override
	public boolean setReadOnly() {
		functionCalled("setReadOnly");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.setReadOnly();
		}
		return super.setReadOnly();
	}

	@Override
	public boolean setWritable(boolean writable, boolean ownerOnly) {
		functionCalled("setWritable");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.setWritable(writable, ownerOnly);
		}
		return super.setWritable(writable, ownerOnly);
	}

	@Override
	public boolean setWritable(boolean writable) {
		functionCalled("setWritable");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.setWritable(writable);
		}
		return super.setWritable(writable);
	}

	@Override
	public boolean setReadable(boolean readable, boolean ownerOnly) {
		functionCalled("setReadable");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.setReadable(readable, ownerOnly);
		}
		return super.setReadable(readable, ownerOnly);
	}

	@Override
	public boolean setReadable(boolean readable) {
		functionCalled("setReadable");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.setReadable(readable);
		}
		return super.setReadable(readable);
	}

	@Override
	public boolean setExecutable(boolean executable, boolean ownerOnly) {
		functionCalled("setExecutable");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.setExecutable(executable, ownerOnly);
		}
		return super.setExecutable(executable, ownerOnly);
	}

	@Override
	public boolean setExecutable(boolean executable) {
		functionCalled("setExecutable");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.setExecutable(executable);
		}
		return super.setExecutable(executable);
	}

	@Override
	public boolean canExecute() {
		functionCalled("canExecute");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.canExecute();
		}
		return super.canExecute();
	}

	@Override
	public long getTotalSpace() {
		functionCalled("getTotalSpace");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.getTotalSpace();
		}
		return super.getTotalSpace();
	}

	@Override
	public long getFreeSpace() {
		functionCalled("getFreeSpace");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.getFreeSpace();
		}
		return super.getFreeSpace();
	}

	@Override
	public long getUsableSpace() {
		functionCalled("getUsableSpace");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.getUsableSpace();
		}
		return super.getUsableSpace();
	}

	@Override
	public int compareTo(File pathname) {
		functionCalled("compareTo");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.cloudFileObj.compareTo(pathname);
		}
		return super.compareTo(pathname);
	}


	@Override
	public Path toPath() {
		functionCalled("toPath");
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			
			return this.cloudFileObj.toPath();
		}
		return super.toPath();
	}
	
	
	private void functionCalled(String funcName)
	{
		out("AlinouFile -> function : " + funcName + "()");
	}
	
	private void out(String str)
	{
		//System.out.println(str);
	}
	
}
