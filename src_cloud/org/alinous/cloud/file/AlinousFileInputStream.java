package org.alinous.cloud.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.alinous.cloud.AlinousCloudManager;

public class AlinousFileInputStream extends InputStream{
	private FileInputStream fileInputStream;
	private IAlinousFileInputStream alinousFileInputStream;

	public AlinousFileInputStream(File file) throws FileNotFoundException {
		
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			this.alinousFileInputStream = AlinousCloudManager.getInstance().newFileInputStream(file);
		}
		else{
			this.fileInputStream = new FileInputStream(file);
		}		
	}

	@Override
	public int read() throws IOException {
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.alinousFileInputStream.read();
		}
		
		return this.fileInputStream.read();
	}

	@Override
	public int available() throws IOException {
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return this.alinousFileInputStream.available();
		}
		
		return this.fileInputStream.available();
	}

	@Override
	public void close() throws IOException {
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			this.alinousFileInputStream.close();
			return;
		}
		
		this.fileInputStream.close();
	}

	@Override
	public int read(byte[] b) throws IOException {
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return super.read(b);
		}
		
		return this.fileInputStream.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			return super.read(b, off, len);
		}

		return this.fileInputStream.read(b, off, len);
	}
	
	

}
