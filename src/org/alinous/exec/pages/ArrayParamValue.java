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

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alinous.csv.CsvException;
import org.alinous.csv.CsvReader;
import org.alinous.csv.CsvRecord;
import org.alinous.expections.AlinousException;



public class ArrayParamValue implements IParamValue
{
	private List<String> values = new ArrayList<String>();
	
	public ArrayParamValue()
	{
		
	}
	
	public ArrayParamValue(String values) throws AlinousException
	{
		StringReader stringReader = new StringReader(values + ",\n");
		CsvReader reader = new CsvReader(stringReader);
		
		try {
			CsvRecord rec = reader.readRecord();
			
			Iterator<String> it = rec.iterator();
			while(it.hasNext()){
				String val = it.next();
				
				if(val == null){
					addValue("");
				}
				else{
					val = URLDecoder.decode(val, "utf-8");
					
					addValue(val);
				}
			}
		} catch (CsvException e) {
			e.printStackTrace();
			throw new AlinousException(e, "Error on csv array param");
		} catch (IOException e) {
			e.printStackTrace();
			throw new AlinousException(e, "Error on csv array param");
		}
		finally{
			reader.close();
			stringReader.close();
		}

	}
	
	public int getType()
	{
		return IParamValue.TYPE_ARRAY;
	}
	
	public void addValue(String value)
	{
		this.values.add(value);
	}
	
	public Iterator<String> getIterator()
	{
		return this.values.iterator();
	}
	
	public String getValueAt(int i)
	{
		return this.values.get(i);
	}
	
	public int size()
	{
		return this.values.size();
	}
	
	public String toString()
	{
		StringBuffer buff = new StringBuffer();
		
		boolean first = true;
		Iterator<String> it = this.values.iterator();
		while(it.hasNext()){
			String value = it.next();
			
			if(first){
				first = false;
			}else{
				buff.append(",");
			}
			
			try {
				value = URLEncoder.encode(value, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			buff.append(value);
		}
		
		return buff.toString();
	}
	
	public boolean containsValue(String value)
	{
		return this.values.contains(value);
	}
}
