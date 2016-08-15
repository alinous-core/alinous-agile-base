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
package org.alinous.exec.pages;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.alinous.AlinousCore;
import org.alinous.AlinousUtils;
import org.alinous.cloud.file.AlinousFile;
import org.alinous.cloud.file.AlinousFileInputStream;
import org.alinous.cloud.file.AlinousFileOutputStream;
import org.alinous.csv.CsvReader;

public class FileResourceManager
{
	private Map<String, InputStream> inputMap = new HashMap<String, InputStream>();
	private Map<String, Writer> outputMap = new HashMap<String, Writer>();
	private Map<String, CsvReader> csvReaderMap = new HashMap<String, CsvReader>();
	
	private AlinousCore core;
	
	public FileResourceManager(AlinousCore core)
	{
		this.core = core;
	}
	
	public InputStream openReadFile(String path) throws FileNotFoundException
	{
		InputStream stream = this.inputMap.get(path);
		
		if(stream == null){
			String absPath = AlinousUtils.getAbsolutePath(this.core.getHome(), path);
			
			AlinousFile file = new AlinousFile(absPath);
			stream = new AlinousFileInputStream(file);
			
			this.inputMap.put(path, stream);
		}
		
		return stream;
	}
	
	public Writer openWriteFile(String path, String encode) throws IOException
	{
		Writer writer = this.outputMap.get(path);
		
		if(writer == null){
			String absPath = AlinousUtils.getAbsolutePath(this.core.getHome(), path);
			
			AlinousFile file = new AlinousFile(absPath);
			OutputStream stream = new AlinousFileOutputStream(file);
			writer = new OutputStreamWriter(stream, encode);
			
			this.outputMap.put(path, writer);
		}
		
		return writer;
	}
	
	public CsvReader openCsvReader(String path, String encode) throws FileNotFoundException
	{
		CsvReader reader = this.csvReaderMap.get(path);
		
		if(reader == null){
			String absPath = AlinousUtils.getAbsolutePath(this.core.getHome(), path);
			AlinousFile file = new AlinousFile(absPath);
			AlinousFileInputStream stream = new AlinousFileInputStream(file);
			
			reader = new CsvReader(stream, encode);
			
			this.csvReaderMap.put(path, reader);	
		}
		
		return reader;
	}
	
	public void closeCsvReader(String path)
	{
		CsvReader reader = this.csvReaderMap.get(path);
		
		if(reader == null){
			return;
		}
		
		this.csvReaderMap.remove(path);
		
		reader.close();
	}
	
	public void closeWriteFile(String path)
	{
		Writer writer = this.outputMap.get(path);
		
		if(writer == null){
			return;
		}
		
		this.outputMap.remove(path);
		try {
			writer.close();
		} catch (IOException e) {e.printStackTrace();	}
		
	}
	
	public void closeReadFile(String path)
	{
		InputStream stream = this.inputMap.get(path);
		
		if(stream == null){
			return;
		}
		
		this.inputMap.remove(path);
		try {
			stream.close();
		} catch (IOException e) {e.printStackTrace();	}
		
		closeCsvReader(path);
	}
	
	public void dispose()
	{
		Iterator<String> it = this.inputMap.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
						
			closeReadFile(key);			
		}
		
		it = this.outputMap.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			
			closeWriteFile(key);
		}
		
		it = this.csvReaderMap.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			
			closeCsvReader(key);
		}
	}
}
