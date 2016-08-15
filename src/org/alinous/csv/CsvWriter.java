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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class CsvWriter
{
	private Writer writer;
	private int col;
	
	public CsvWriter(Writer writer)
	{
		this.writer = writer;
	}
	
	public CsvWriter(OutputStream stream)
	{
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(stream, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		this.writer = writer;
	}
	
	public void addField(String strField) throws IOException
	{
		if(this.col > 0){
			this.writer.write(",");
		}
		
		this.writer.write("\"");
		if(strField != null){
			strField = strField.replace("\"", "\"\"");
			this.writer.write(strField);
		}
		this.writer.write("\"");
		
		this.col = this.col + 1;
	}
	
	public void endRecord() throws IOException
	{
		this.col = 0;
		this.writer.write("\n");
		this.writer.flush();
	}
	
}
