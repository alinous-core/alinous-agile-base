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
package org.alinous.net.pop3;

import java.io.IOException;

import org.alinous.expections.MailException;

public class UserCommand extends AbstractPop3Command
{

	public UserCommand(Pop3Protocol popProtocol)
	{
		super(popProtocol);
	}

	public void receiveCommand() throws IOException, MailException
	{
		String res = receive();
		
		if(!res.startsWith("+OK")){
			throw new MailException(res);
		}
	}

	public void sendCommand() throws IOException, MailException
	{
		sendCommand("USER " + this.popProtocol.getUser() + "\r\n");
		
	}

}
