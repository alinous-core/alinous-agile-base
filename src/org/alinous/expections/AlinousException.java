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

public class AlinousException extends Exception {
	private static final long serialVersionUID = -1569463441504809L;
	
	private String message;
	
	protected AlinousStackTrace trace = new AlinousStackTrace();
	
	public AlinousException()
	{
		super();
	}
	public AlinousException(String msg)
	{
		super(msg);
	}
	
	public AlinousException(Throwable cause, String message)
	{
		super(cause);
		
		if(cause instanceof AlinousException){
			this.trace.importTrace(((AlinousException)cause).trace);
		}
		
		this.message = message;
	}
	
	public AlinousException(String msg, String filePath, int line)
	{
		super(msg);
		
		this.trace.addStack(line, filePath);
	}
	
	public void addStackTrace(String filePath, int line)
	{
		this.trace.addStack(line, filePath);
	}
	
	public AlinousException(Throwable cause, String message, String filePath, int line)
	{
		super(message, cause);
		
		if(cause instanceof AlinousException){
			this.trace.importTrace(((AlinousException)cause).trace);
		}
		
		this.trace.addStack(line, filePath);
	}
	
	public String getMessage()
	{
		if(this.message != null){
			return this.message;
		}
		
		return super.getMessage();
	}
}
