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
package org.alinous.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class CsvReader
{
	public static final char EOF = 0xFFFF;

	public Reader reader;
	public InputStream stream;
	public char lastChar;
	
	public CsvReader(InputStream stream)
	{
		this(stream, "utf-8");
	}
	
	
	
	public CsvReader(InputStream stream, String encode)
	{
		this.stream = stream;
		
		try {
			reader = new InputStreamReader(stream, encode);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		this.lastChar = 0;
	}
	
	public CsvReader(Reader reader)
	{
		this.reader = reader;
		this.lastChar = 0;
	}
	
	public CsvRecord readRecord() throws CsvException, IOException
	{
		CsvRecord record = new CsvRecord();
		
		String fld = null;
		while(true){
			fld = readField();
			
			if(fld == null){
				break;
			}
			
			record.addField(fld);
		}
		
		return record;
	}
	
	private String readField() throws CsvException, IOException
	{
		StringBuffer buff = new StringBuffer();
		char ch = 0;
		
		// read while '"'
		ch = readStart();
		if(ch == '\n' || ch == EOF){
			return null;
		}
		
		// csv reader
		boolean quoted = (ch == '"');
		while(true){
			ch = read();
			
			if(ch == '"'){
				ch = read();
				
				if(ch == '"'){
					buff.append("\"");
					continue;
				}else if(ch == ','){
					break;
				}else{
					pushBack(ch);
					break;
				}
			}
			else if(!quoted && ch == ','){
				break;
			}
			else if(!quoted && ch == '\r'){
				continue;
			}
			else if(!quoted && ch == '\n'){
				pushBack(ch);
				break;
			}
			else if(ch == EOF){
				break;
			}
			else{
				buff.append(ch);
			}
		}
		
		return buff.toString();
	}
	
	private char readStart() throws IOException
	{
		char ch = 0;
		while(true){
			ch = read();

			if(ch == '"' ){
				break;
			}
			else if(ch == '\n'){
				break;
			}
			else if(ch == EOF){
				break;
			}else if(ch == ' '){
				continue;
			}else if(ch == ','){
				pushBack(ch);
				break;
			}else {
				pushBack(ch);
				break;
			}
		}
		
		return ch;
	}
	
	private void pushBack(char ch)
	{
		this.lastChar = ch;
	}
	
	private char read() throws IOException
	{
		if(this.lastChar != 0){
			char ch = this.lastChar;
			this.lastChar = 0;
			return ch;
		}
		
		char ch = (char)this.reader.read();
		return ch;
	}
	
	public void close()
	{
		try {
			this.reader.close();
		} catch (IOException e1) {}
		
		if(this.stream != null){
			try {
				this.stream.close();
			} catch (IOException e) {}
		}
	}
}
