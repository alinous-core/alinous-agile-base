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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.alinous.cloud.file.AlinousFile;
import org.alinous.cloud.file.AlinousFileOutputStream;


public class ZipExtractor
{
	private ZipFile zip;
	
	public ZipExtractor(AlinousFile in) throws ZipException, IOException{
		//this.in = in;
		this.zip = new ZipFile(in);
	}
	
	public static void main(String[] args) {
		try {
			ZipExtractor ext = new ZipExtractor(new AlinousFile("output/backup.zip"));
			ext.extract("output/ext");
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	public void extract(String dstPath) {
		Enumeration entries = this.zip.entries();
		
		while(entries.hasMoreElements()){
			try{
				ZipEntry entry = (ZipEntry)entries.nextElement();
				
				String destEntryPath = dstPath + "/" +entry.getName();
				dirCheck(destEntryPath);
				
				if(entry.isDirectory()){
					AlinousFile f = new AlinousFile(destEntryPath);
					f.mkdirs();
					continue;
				}
				
				copyInputStream(this.zip.getInputStream(entry),
						   new BufferedOutputStream(new AlinousFileOutputStream(new AlinousFile(destEntryPath))));
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	public void dispose(){
		try {
			this.zip.close();
		} catch (IOException e) {
		}
	}
	
	private void dirCheck(String name){
		String spilted[] = name.split("/");
		
		if(spilted.length <= 1){
			return;
		}
		
		String dir = "";
		for(int i=0; i < spilted.length-1; i++){
			dir += spilted[i] + "/";
		}
		
		AlinousFile file = new AlinousFile(dir);
		if(!file.exists()){
			file.mkdirs();
		}
		
	}

	public void copyInputStream(InputStream in, OutputStream out)
	throws IOException
	{
	  byte[] buffer = new byte[1024];
	  int len;

	  while((len = in.read(buffer)) >= 0)
		out.write(buffer, 0, len);

	  in.close();
	  out.close();
	}
}
