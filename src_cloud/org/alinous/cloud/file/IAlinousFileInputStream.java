package org.alinous.cloud.file;

import java.io.IOException;

public interface IAlinousFileInputStream {
	public int read() throws IOException;
	public int available() throws IOException;
	public void close() throws IOException;
}
