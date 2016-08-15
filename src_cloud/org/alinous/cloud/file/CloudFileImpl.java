package org.alinous.cloud.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

import org.alinous.cloud.NotImplementedException;


/**
 * Example of implementation
 * @author iizuka
 *
 */
public class CloudFileImpl {
	
	
	public String getName() {
		
		throw new NotImplementedException();
	}

	
	public String getParent() {
		
		throw new NotImplementedException();
	}

	
	public File getParentFile() {
		
		throw new NotImplementedException();
	}

	
	public String getPath() {
		
		throw new NotImplementedException();
	}

	
	public boolean isAbsolute() {
		
		throw new NotImplementedException();
	}

	
	public String getAbsolutePath() {
		
		throw new NotImplementedException();
	}

	
	public File getAbsoluteFile() {
		
		return null;
	}

	
	public String getCanonicalPath() throws IOException {
		
		throw new NotImplementedException();
	}

	
	public File getCanonicalFile() throws IOException {
		
		throw new NotImplementedException();
	}

	
	public URL toURL() throws MalformedURLException {
		
		throw new NotImplementedException();
	}

	
	public URI toURI() {
		
		throw new NotImplementedException();
	}

	
	public boolean canRead() {
		
		throw new NotImplementedException();
	}

	
	public boolean canWrite() {
		
		throw new NotImplementedException();
	}

	
	public boolean exists() {
		
		throw new NotImplementedException();
	}

	
	public boolean isDirectory() {
		
		throw new NotImplementedException();
	}

	
	public boolean isFile() {
		
		throw new NotImplementedException();
	}

	
	public boolean isHidden() {
		
		throw new NotImplementedException();
	}

	
	public long lastModified() {
		
		throw new NotImplementedException();
	}

	
	public long length() {
		
		throw new NotImplementedException();
	}

	
	public boolean createNewFile() throws IOException {
		
		throw new NotImplementedException();
	}

	
	public boolean delete() {
		
		throw new NotImplementedException();
	}

	
	public void deleteOnExit() {
		
		throw new NotImplementedException();
	}

	
	public String[] list() {
		
		throw new NotImplementedException();
	}

	
	public String[] list(FilenameFilter filter) {
		
		throw new NotImplementedException();
	}

	
	public File[] listFiles() {
		
		throw new NotImplementedException();
	}

	
	public File[] listFiles(FilenameFilter filter) {
		
		throw new NotImplementedException();
	}

	
	public File[] listFiles(FileFilter filter) {
		
		throw new NotImplementedException();
	}

	
	public boolean mkdir() {
		
		throw new NotImplementedException();
	}

	
	public boolean mkdirs() {
		
		throw new NotImplementedException();
	}

	
	public boolean renameTo(File dest) {
		
		throw new NotImplementedException();
	}

	
	public boolean setLastModified(long time) {
		
		throw new NotImplementedException();
	}

	
	public boolean setReadOnly() {
		
		throw new NotImplementedException();
	}

	
	public boolean setWritable(boolean writable, boolean ownerOnly) {
		
		throw new NotImplementedException();
	}

	
	public boolean setWritable(boolean writable) {
		
		throw new NotImplementedException();
	}

	
	public boolean setReadable(boolean readable, boolean ownerOnly) {
		
		throw new NotImplementedException();
	}

	
	public boolean setReadable(boolean readable) {
		
		throw new NotImplementedException();
	}

	
	public boolean setExecutable(boolean executable, boolean ownerOnly) {
		
		throw new NotImplementedException();
	}

	
	public boolean setExecutable(boolean executable) {
		
		throw new NotImplementedException();
	}

	
	public boolean canExecute() {
		
		throw new NotImplementedException();
	}

	
	public long getTotalSpace() {
		
		throw new NotImplementedException();
	}

	
	public long getFreeSpace() {
		
		throw new NotImplementedException();
	}

	
	public long getUsableSpace() {
		
		throw new NotImplementedException();
	}

	
	public int compareTo(File pathname) {
		
		throw new NotImplementedException();

	}

	
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	
	public int hashCode() {
		return super.hashCode();
	}

	
	public String toString() {
		return super.toString();
	}

	public Path toPath() {
		throw new NotImplementedException();
	}
	
}
