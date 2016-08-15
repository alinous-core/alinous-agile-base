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

import org.alinous.AlinousUtils;
import org.alinous.exec.IExecutable;

public class AlinousDebugEvent
{
	public final static int STARTED = 0;
	public final static int BEFORE_SENTENCE = 1;
	public final static int BEFORE_STATEMENT = 2;
	public final static int BEFORE_CREATE_STACKFRAME = 3;
	public final static int AFTER_CALLED_ARGUMENT = 4;	
	
	private int eventType;
	private int line;
	private String filePath;
	private DebugThread thread;
	
	private IExecutable sentence;
	
	public AlinousDebugEvent(int eventType, int line, String filePath, DebugThread thread)
	{
		this.eventType = eventType;
		
		this.line = line;
		this.filePath = AlinousUtils.forceUnixPath(filePath);
		
		this.thread = thread;
	}

	public int getEventType() {
		return eventType;
	}

	public String getFilePath() {
		return filePath;
	}

	public int getLine()
	{
		return line;
	}

	public IExecutable getSentence()
	{
		return sentence;
	}

	public void setSentence(IExecutable sentence)
	{
		this.sentence = sentence;
	}

	public DebugThread getThread() {
		return thread;
	}
	
	public String toString()
	{
		String strEvent = "UNKNOWN";
		switch(this.eventType){
		case STARTED:
			strEvent = "STARTED";
			break;
		case BEFORE_SENTENCE:
			strEvent = "BEFORE_SENTENCE";
			break;
		case BEFORE_STATEMENT:
			strEvent = "BEFORE_STATEMENT";
			break;
		case BEFORE_CREATE_STACKFRAME:
			strEvent = "BEFORE_CREATE_STACKFRAME";
			break;
		case AFTER_CALLED_ARGUMENT:
			strEvent = "AFTER_CALLED_ARGUMENT";
			break;
		default:
			break;
		}
		
		return "{eventType : " + strEvent + " line : " + line + " filePath : " + filePath + "}";
	}

}
