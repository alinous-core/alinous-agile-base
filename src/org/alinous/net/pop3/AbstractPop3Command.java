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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import org.alinous.expections.MailException;

public abstract class AbstractPop3Command
{
	protected Pop3Protocol popProtocol;
	
	public AbstractPop3Command(Pop3Protocol popProtocol)
	{
		this.popProtocol = popProtocol;
	}
	
	protected void sendCommand(String strCommand) throws IOException 
	{
		OutputStream stream = null;
		Writer writer = null;
		try {
			stream = this.popProtocol.getOutputStream();
			writer = new PrintWriter(stream, true);
			
			writer.write(strCommand);
			
		} catch (IOException e) {
			throw e;
		}finally{
			//stream.close();
			if(writer != null){
				writer.flush();
			}
			stream.flush();
		}
		
	}
	
	protected String receive() throws IOException
	{
		InputStream inStream = this.popProtocol.getInputStream();
		
		while(inStream.available() == 0){
			;
		}
		
		byte [] buff = new byte[1024 * 5];
		int nRead = 0;
		
		StringBuffer strBuffer = new StringBuffer();
		
		while(inStream.available() != 0){
			nRead = inStream.read(buff);
			String received = new String(buff, 0, nRead);
			strBuffer.append(received);
			
		}
		
		//inStream.close();
		
		return strBuffer.toString();
	}
	
	public abstract void sendCommand() throws IOException, MailException;
	public abstract void receiveCommand() throws IOException, MailException;	
}
