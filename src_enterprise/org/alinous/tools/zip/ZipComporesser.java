/**
 * CROSSFIRE JAPAN INCORPORATED
 * This source code is under GPL License.
 * info@crossfire.jp
 * Official web site
 * http://alinous.org
 * 
 *  Copyright (C) 2007 Tomohiro Iizuka
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.alinous.tools.zip;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.alinous.cloud.file.AlinousFile;
import org.alinous.cloud.file.AlinousFileInputStream;
import org.alinous.cloud.file.AlinousFileOutputStream;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

public class ZipComporesser
{
	private String zipDir; 
	private ZipOutputStream out;
	
	public static void main(String[] args) {
		try {
			new ZipComporesser().createZipFile("JavaCC/", "output/test.zip");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void createZipFile(String zipDir, String destFile) throws IOException
	{
		AlinousFile baseDir = new AlinousFile(zipDir);
		if(!baseDir.isDirectory()){
			return;
		}
		this.zipDir = baseDir.getAbsolutePath();
		
		AlinousFile dstF = new AlinousFile(destFile);
		
		try {
			this.out = new ZipOutputStream(new AlinousFileOutputStream(new AlinousFile(dstF.getAbsolutePath())));
			this.out.setLevel(4);
			this.out.setEncoding("MS932");
			
			File[] files = baseDir.listFiles();
			for(int i = 0; i < files.length; i++){
				addFile(files[i]);
			}
			
		} finally{
			if(this.out != null){
				try {
					this.out.close();
				} catch (IOException e) {}
			}
		}
		

	}
	
	public void addFile(File file) throws IOException
	{
		if(file.isDirectory()){
			
			
			File[] files = file.listFiles();
			for(int i = 0; i < files.length; i++){
				addFile(files[i]);
			}
			
			return;
		}
		
		ZipEntry target = new ZipEntry(getEntryPath(file));
		this.out.putNextEntry(target);
		
		int totalSize = 0;
		byte buf[] = new byte[1024];
		int count;
		CheckedInputStream in = new CheckedInputStream(new BufferedInputStream(new AlinousFileInputStream(file)), new CRC32());
		while ((count = in.read(buf, 0, 1024)) != -1) {
			this.out.write(buf, 0, count);
			totalSize += count;
		}
		
		target.setCrc(in.getChecksum().getValue());
		target.setCompressedSize(totalSize);
		target.setSize(totalSize);
		
		//verbose(target);
		
		in.close();
		this.out.closeEntry();
	}
	/*
	  private static void verbose(Object obj) {

		    if (true) {

		      if (obj instanceof ZipEntry) {
		        ZipEntry zipEntry = (ZipEntry) obj;
		        System.out.println(zipEntry.getName() + "\tSize:"
		            + zipEntry.getSize() + "\tCRC32:" + zipEntry.getCrc());
		      }
		      System.out.println(obj);
		    }
	  }
	*/
	private String getEntryPath(File file) {
		String path = file.getAbsolutePath().replaceAll("\\\\", "/").substring(this.zipDir.length());
		return path.substring(1, path.length());
	}

}
