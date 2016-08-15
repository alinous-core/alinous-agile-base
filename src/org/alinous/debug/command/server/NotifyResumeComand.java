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
package org.alinous.debug.command.server;

import java.io.IOException;
import java.io.Writer;

public class NotifyResumeComand extends AbstractAlinousServerCommand {
	public static final String CMD_STRING = "THREAD_RESUMED";
	
	public NotifyResumeComand(Thread thread) {
		super(thread);
	}
	
	@Override
	public void writeCommand(Writer writer) throws IOException
	{
		writer.write(CMD_STRING);
		writer.flush();
	}
	
	@Override
	public String getName() {
		return CMD_STRING;
	}

}
