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
package org.alinous.net.mail;

import java.io.IOException;
import java.net.Socket;

import org.alinous.expections.MailException;

public class AuthLoginInputPassCommand extends AbstractSmptCommand
{

	public AuthLoginInputPassCommand(SmtpProtocol proto)
	{
		super(proto);
	}

	public void receiveCommand(Socket con) throws IOException, MailException
	{
		String res = receive(con);
		
		if(!res.startsWith("235")){
			throw new MailException(res);
		}
	}

	public void sendCommand(Socket con) throws IOException
	{
		String encodedPass = base64Encode(this.proto.getPassword());
		
		sendCommand(encodedPass, con);
	}

}
