package org.alinous.cloud.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.alinous.cloud.AlinousCloudManager;

public class AlinousFileOutputStream extends OutputStream{
	private FileOutputStream fileOutputStream;
	private IAlinousFileOutputStream alinousStream;

	public AlinousFileOutputStream(File file) throws IOException {
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			this.alinousStream = AlinousCloudManager.getInstance().newFileOutputStream(file);
			this.alinousStream.setAppend(false);
			return;
		}
		
		this.fileOutputStream = new FileOutputStream(file);
	}
	
	public AlinousFileOutputStream(File file, boolean append) throws IOException {
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			this.alinousStream = AlinousCloudManager.getInstance().newFileOutputStream(file);
			this.alinousStream.setAppend(append);
			return;
		}
		
		this.fileOutputStream = new FileOutputStream(file, append);
	}
	

	@Override
	public void write(int b) throws IOException {
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			this.alinousStream.write(b);
			return;
		}
		
		this.fileOutputStream.write(b);
	}


	@Override
	public void close() throws IOException {
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			this.alinousStream.close();
			return;
		}
		
		this.fileOutputStream.close();
	}

	@Override
	public void flush() throws IOException {
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			this.alinousStream.flush();
			return;
		}		
		
		this.fileOutputStream.flush();
	}

	@Override
	public void write(byte[] b) throws IOException {
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			super.write(b);
			return;
		}
		
		this.fileOutputStream.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if(AlinousCloudManager.getInstance().isCoudEnabled()){
			super.write(b, off, len);
			return;
		}
		
		this.fileOutputStream.write(b, off, len);
	}
	
	
	

}
