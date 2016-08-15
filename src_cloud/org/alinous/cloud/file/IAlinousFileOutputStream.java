package org.alinous.cloud.file;

import java.io.IOException;

public interface IAlinousFileOutputStream {
	public void write(int b) throws IOException;
	public void close() throws IOException;
	public void flush() throws IOException;
	
	public void setAppend(boolean append) throws IOException;
}
