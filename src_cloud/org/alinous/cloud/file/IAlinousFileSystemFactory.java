package org.alinous.cloud.file;

import java.io.File;
import java.io.IOException;

public interface IAlinousFileSystemFactory {
		
	public IAlinousFileSystemObject getFileObject(String path);
	public IAlinousFileInputStream getFileInputStream(File file);
	public IAlinousFileOutputStream getFileOutputStream(File file) throws IOException;
	
}
