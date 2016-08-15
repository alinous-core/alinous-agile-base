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
import java.io.InputStreamReader;

import org.alinous.expections.MailException;
import org.alinous.net.pop3.format.Pop3MailFormatData;

public class RetrCommand extends AbstractPop3Command
{
	private int mailNumber;
	private Pop3MailFormatData mailData;
	
	public RetrCommand(Pop3Protocol popProtocol, int mailNumber)
	{
		super(popProtocol);
		this.mailNumber = mailNumber;
	}

	@Override
	public void receiveCommand() throws IOException, MailException
	{
		InputStream inStream = this.popProtocol.getInputStream();
		InputStreamReader in = new InputStreamReader(inStream);
		BufferedReader reader = new BufferedReader(in);
		
		this.mailData = new Pop3MailFormatData();
		
		String lineStr;
		
	//	while(inStream.available() != 0);
		
		lineStr = reader.readLine();
		if(!lineStr.startsWith("+OK")){
			throw new MailException(lineStr);
		}
		
		lineStr = reader.readLine();
		do{
			
			
			this.mailData.parseLine(lineStr);
			
			//String endoded = new String(lineStr.getBytes(), "ISO-2022-JP");
			//System.out.println(endoded);
			
			lineStr = reader.readLine();
		}while(!isFinished(lineStr));
		//while(lineStr != null && isFinished(lineStr) && reader.ready());
		
		//System.out.println(lineStr);
	}
	
	private boolean isFinished(String lineStr)
	{
		if(this.mailData.isMultipart()){
			return this.mailData.isMultipartFinidhed();
		}
		
		return lineStr.equals(".");
	}
	

	public String getFrom()
	{
		return "";
	}
	
	public String getSubject()
	{
		return this.mailData.getSubject();
	}
	
	public String getBodyString()
	{
		
		return this.mailData.getBodyString();
	}
	
	@Override
	public void sendCommand() throws IOException, MailException
	{
		sendCommand("RETR " + this.mailNumber + "\r\n");
		
	}

	public Pop3MailFormatData getMailData() {
		return mailData;
	}
}
