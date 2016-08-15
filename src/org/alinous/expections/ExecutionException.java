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
package org.alinous.expections;

public class ExecutionException extends AlinousException {

	private static final long serialVersionUID = -8995338354403872601L;

	public ExecutionException(String msg)
	{
		super(msg);
	}
	
	public ExecutionException(String msg, String filePath, int line)
	{
		super(msg, filePath, line);
	}
	
	public ExecutionException(Throwable cause, String message)
	{
		super(cause, message);
	}
	
	public ExecutionException(Throwable cause, String message, String filePath, int line)
	{
		super(cause, message, filePath, line);

	}
	
	@Override
	public String getMessage() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(super.getMessage());
		buffer.append(this.trace.stackTraceHtmlString());
		
		return buffer.toString();
	}

}
