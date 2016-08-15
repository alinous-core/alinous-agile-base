package org.alinous.cloud.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;


public interface IAlinousFileSystemObject {
	
	public void setPath(String path);
	
	public String getName();
	public String getParent();
	public File getParentFile();
	public String getPath();
	public boolean isAbsolute();
	public String getAbsolutePath();
	public File getAbsoluteFile();
	public String getCanonicalPath();
	public File getCanonicalFile() throws IOException;
	public URL toURL() throws MalformedURLException;
	public URI toURI();
	public boolean canRead();
	public boolean canWrite();
	public boolean exists();
	public boolean isDirectory();
	public boolean isFile();
	public boolean isHidden();
	public long lastModified();
	public long length();
	public boolean createNewFile() throws IOException;
	public boolean delete();
	public void deleteOnExit();
	public String[] list();
	public String[] list(FilenameFilter filter);
	public File[] listFiles();
	public File[] listFiles(FilenameFilter filter);
	public File[] listFiles(FileFilter filter);
	public boolean mkdir();
	public boolean mkdirs();
	public boolean renameTo(File dest);
	public boolean setLastModified(long time);
	public boolean setReadOnly();
	public boolean setWritable(boolean writable, boolean ownerOnly);
	public boolean setWritable(boolean writable);
	public boolean setReadable(boolean readable, boolean ownerOnly);
	public boolean setReadable(boolean readable);
	public boolean setExecutable(boolean executable, boolean ownerOnly);
	public boolean setExecutable(boolean executable);
	public boolean canExecute();
	public long getTotalSpace();
	public long getFreeSpace();
	public long getUsableSpace();
	public int compareTo(File pathname);
	public boolean equals(Object obj);
	public int hashCode();
	public String toString();
	public Path toPath();
}
