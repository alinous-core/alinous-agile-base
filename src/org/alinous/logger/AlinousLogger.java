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

import java.net.SocketException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.alinous.AlinousConfig;
import org.alinous.AlinousUtils;
import org.alinous.datasrc.basic.ILogProvidor;
import org.alinous.expections.DirectOutputException;

public class AlinousLogger implements ILogProvidor
{
	private AlinousConfig config;
	private AlinousFileLogger log;
	
	public AlinousLogger(AlinousConfig config, String alinousHome)
	{
		this.config = config;
		
		this.log = new AlinousFileLogger();
		this.log.init(alinousHome);
	}
	/*
	public void reportError(AlinousException ex)
	{
		String stackTraceString = AlinousUtils.getStackTraceString(ex);
		
		this.log.reportError(stackTraceString);
	}*/
	
	public void reportError(Throwable ex)
	{
		if(ex instanceof DirectOutputException){
			return;
		}
		if(ex instanceof SocketException){
			return;
		}
		
		
		StringBuffer buff = new StringBuffer();
		
		Long nowLong = System.currentTimeMillis();
		Timestamp now = new Timestamp(nowLong);
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
		
		buff.append("[");
		buff.append(fmt.format(now));
		buff.append("]\n");
		
		String stackTraceString = AlinousUtils.getStackTraceString(ex);
		buff.append(stackTraceString);
		
		buff.append("\n");
		
		this.log.reportError(buff.toString());
	}
	
	public void reportError(String str)
	{
		StringBuffer buff = new StringBuffer();
		
		Long nowLong = System.currentTimeMillis();
		Timestamp now = new Timestamp(nowLong);
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
		
		buff.append("[");
		buff.append(fmt.format(now));
		buff.append("]\n");

		buff.append(str);
		
		buff.append("\n");
		
		this.log.reportError(buff.toString());
	}
	
	public AlinousConfig getConfig()
	{
		return config;
	}

	public void setConfig(AlinousConfig config)
	{
		this.config = config;
	}

	public void reportInfo(String str)
	{
		StringBuffer buff = new StringBuffer();
		
		Long nowLong = System.currentTimeMillis();
		Timestamp now = new Timestamp(nowLong);
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
		
		buff.append("[");
		buff.append(fmt.format(now));
		buff.append("]\n");
		
		buff.append(str);
		
		buff.append("\n");
		
		this.log.reportInfo(buff.toString());
	}
	

}
