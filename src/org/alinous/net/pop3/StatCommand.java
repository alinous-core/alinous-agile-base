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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.alinous.expections.MailException;

public class StatCommand extends AbstractPop3Command{
	private int numMails;
	private int numBytes;
	
	public StatCommand(Pop3Protocol popProtocol) {
		super(popProtocol);
	}

	@Override
	public void receiveCommand() throws IOException, MailException {
		String res = receive();
		
		if(!res.startsWith("+OK")){
			throw new MailException(res);
		}
		
		StringReader stringReader = new StringReader(res);
		BufferedReader reader = new BufferedReader(stringReader);
		
		try{
			String tmpLine = reader.readLine();
			
			while(tmpLine != null && !tmpLine.equals(".")){
				tmpLine = reader.readLine();
				
				if(tmpLine == null){
					break;
				}
				
				// OK Logged in.
				if(tmpLine.startsWith("+OK Logged")){
					continue;
				}
				String ansers[] = tmpLine.split(" ");
				
				this.numMails = Integer.parseInt(ansers[1]);
				this.numBytes = Integer.parseInt(ansers[2].trim());
				
				break;
			}
		
		}
		finally{
			reader.close();
			stringReader.close();
		}
		

		
	}

	@Override
	public void sendCommand() throws IOException, MailException
	{
		sendCommand("STAT\r\n");
	}

	public int getNumMails() {
		return numMails;
	}

	public int getNumBytes() {
		return numBytes;
	}
}
