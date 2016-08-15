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

package org.alinous;

import java.io.PrintWriter;

public class VirtualHostConfig
{
	private String hostName;
	private String alinousHome;
	private int numThreads;
	private int millStepInterval;
	private int limitStepsCoutner;
	
	public String getAlinousHome()
	{
		return alinousHome;
	}
	public void setAlinousHome(String alinousHome)
	{
		this.alinousHome = alinousHome;
	}
	public String getHostName()
	{
		return hostName;
	}
	public void setHostName(String hostName)
	{
		this.hostName = hostName;
	}
	public int getLimitStepsCoutner()
	{
		return limitStepsCoutner;
	}
	public void setLimitStepsCoutner(int limitStepsCoutner)
	{
		this.limitStepsCoutner = limitStepsCoutner;
	}
	public int getMillStepInterval()
	{
		return millStepInterval;
	}
	public void setMillStepInterval(int millStepInterval)
	{
		this.millStepInterval = millStepInterval;
	}
	public int getNumThreads()
	{
		return numThreads;
	}
	public void setNumThreads(int numThreads)
	{
		this.numThreads = numThreads;
	}
	
	public void writeAsString(PrintWriter wr)
	{
		wr.write("		<virtualhost>\n");
		
		wr.write("			<server>");
		wr.write(this.hostName);
		wr.write("</server>\n");
		
		wr.write("			<alinoushome>");
		wr.write(this.alinousHome);
		wr.write("</alinoushome>\n");
		
		wr.write("		</virtualhost>\n");
	}
	
	
}
