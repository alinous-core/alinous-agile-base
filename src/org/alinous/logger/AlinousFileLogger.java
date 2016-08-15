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
package org.alinous.logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.alinous.AlinousUtils;
import org.alinous.cloud.file.AlinousFile;
import org.alinous.cloud.file.AlinousFileOutputStream;

public class AlinousFileLogger
{
	public static final String LIB_DIR = "/log/";
	private String alinousHome;
	
	public void reportError(String str)
	{
		String fileName = getFileName("error");
		try {
			writeLog(fileName, str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reportInfo(String str)
	{
		String fileName = getFileName("alinous");
		try {
			writeLog(fileName, str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void init(String alinousHome)
	{
		this.alinousHome = alinousHome;
		
		String libDir = AlinousUtils.getAbsolutePath(this.alinousHome, LIB_DIR);
		
		AlinousFile file = new AlinousFile(libDir);
		
		if(!file.exists()){
			file.mkdirs();
		}
	}
	
	private String getFileName(String prefix)
	{
		Long nowLong = System.currentTimeMillis();
		
		Timestamp now = new Timestamp(nowLong);
		
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd'.log'");
		
		String target = LIB_DIR + prefix + fmt.format(now);
		return AlinousUtils.getAbsolutePath(this.alinousHome, target);
	}
	
	
	
	private synchronized void writeLog(String fileName, String message)
				throws IOException
	{
		AlinousFile file = new AlinousFile(fileName);
		
		
		OutputStream out = new AlinousFileOutputStream(file, true);
		OutputStreamWriter wr = new OutputStreamWriter(out, "utf-8");
			
		try {
			wr.write(message);
		} finally{
			wr.close();
			out.close();
		}
	}
}
