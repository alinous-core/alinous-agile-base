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
package org.alinous.net.pop3.format;

public class MailHeader {
	private StringBuffer headerBody;
	
	protected MailHeader()
	{
		
	}
	
	public void parseLine(String line)
	{
		if(this.headerBody != null){
			this.headerBody.append(line);
			return;
		}
		
		this.headerBody = new StringBuffer();
		this.headerBody.append(line);
	}
	
	public String getHeaderName()
	{
		int index = this.headerBody.indexOf(":", 0);
		
		return this.headerBody.substring(0, index);
	}
	
	public String getHeaderBody()
	{
		int index = this.headerBody.indexOf(":", 0);
		
		return this.headerBody.substring(index + 2, this.headerBody.length());
	}
}
