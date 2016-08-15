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
package org.alinous.debug;

public class ServerBreakpoint
{
	private String filePath;
	private int line;
	
	public ServerBreakpoint(String filePath, int line)
	{
		this.filePath = filePath;
		this.line = line;
	}
	
	public ServerBreakpoint(String source)
	{
		setupFromString(source);
	}

	public String getFilePath() {
		return filePath;
	}

	public int getLine() {
		return line;
	}
	
	public String toString()
	{
		return this.filePath + "=" + Integer.toString(this.line);
	}
	
	public void setupFromString(String source)
	{
		String elements[] = source.split("=");
		
		this.filePath = elements[0];
		this.line = Integer.parseInt(elements[1]);
	}
	
}
