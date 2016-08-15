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
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.alinous.expections.MailException;

public class ListCommand extends AbstractPop3Command {
	private int nn;
	//private HashMap<String, String> messageIdMap = new HashMap<String, String>();
	private List<ListResult> messages = new ArrayList<ListResult>();
	
	public ListCommand(Pop3Protocol popProtocol) {
		super(popProtocol);
		
		this.nn = -1;
	}
	
	public ListCommand(Pop3Protocol popProtocol, int nn) {
		super(popProtocol);
		
		this.nn = nn;
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
				
				// OK Logged in.
				if(tmpLine == null || tmpLine.startsWith("+OK")){
					continue;
				}
				
				String ids[] = tmpLine.split(" ");
				if(ids.length == 2){
					//this.messageIdMap.put(ids[0], ids[1]);
					
					try{
						ListResult listResult = new ListResult(Integer.parseInt(ids[0]), Integer.parseInt(ids[1]));
						this.messages.add(listResult);
					}catch(Throwable e){
						e.printStackTrace();
					}
				}else{
					//System.out.println(tmpLine);
				}
				//System.out.println(tmpLine);
			}
			
		}
		finally{
			reader.close();
			stringReader.close();
		}
	}
	
	

	@Override
	protected String receive() throws IOException
	{
		InputStream inStream = this.popProtocol.getInputStream();
		
		while(inStream.available() == 0);
		
		StringBuffer stringBuffer = new StringBuffer();
		byte [] buff = new byte[1024];
		int nRead = 0;
		while(inStream.available() != 0){
			nRead = inStream.read(buff);
			String tmp = new String(buff, 0, nRead);
			
			stringBuffer.append(tmp);
			
			if(tmp.endsWith(".\r\n")){
				break;
			}
			
			nRead = inStream.read(buff);
			
		}
		
		//inStream.close();
		
		return stringBuffer.toString();
	}

	@Override
	public void sendCommand() throws IOException, MailException {
		if(this.nn > 0){
			sendCommand("LIST " + this.nn + "\r\n");
		}else{
			sendCommand("LIST\r\n");
		}
	}

	public List<ListResult> getMessages()
	{
		return messages;
	}
	
	
}
