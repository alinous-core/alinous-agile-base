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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.MailException;
import org.alinous.net.pop3.PassCommand;
import org.alinous.net.pop3.Pop3Protocol;
import org.alinous.net.pop3.QuitCommand;
import org.alinous.net.pop3.UserCommand;

public class MailWrapper
{
	private SmtpProtocol protocol;
	
	private List<String> bccList = new ArrayList<String>();
	
	private AlinousMailConfig mailconfig;
	
	public MailWrapper(AlinousMailConfig mailconfig)
	{
		this.protocol = new SmtpProtocol();
		
		this.mailconfig = mailconfig;
		
		this.protocol.setSmtpServer(mailconfig.getServer());
		this.protocol.setSmtpPort(mailconfig.getPort());
		
		this.protocol.setLangEncoding(mailconfig.getLangCode());
	}
	
	public void popcheck() throws ExecutionException
	{
		// popcheck()
		Pop3Protocol protocol = new Pop3Protocol();
		
		protocol.setUser(this.mailconfig.getPopUser());
		protocol.setPass(this.mailconfig.getPopPass());
		
		String popServer = this.mailconfig.getServer();
		if(this.mailconfig.getPopServer() != null && !this.mailconfig.getPopServer().equals("")){
			popServer = this.mailconfig.getPopServer();
		}
		
		protocol.setPopServer(popServer);
		
		try {
			protocol.connect();
			
			// User
			UserCommand userCom = new UserCommand(protocol);
			userCom.sendCommand();
			userCom.receiveCommand();
			
			// Pass
			PassCommand passCom = new PassCommand(protocol);
			passCom.sendCommand();
			passCom.receiveCommand();
			
			// Quit
			QuitCommand quitCom = new QuitCommand(protocol);
			quitCom.sendCommand();
			quitCom.receiveCommand();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new ExecutionException(e, "Pop before SMTP error,");// i18n
		} catch (IOException e) {
			e.printStackTrace();
			throw new ExecutionException(e, "Pop before SMTP error,");// i18n
		} catch (MailException e) {
			e.printStackTrace();
			throw new ExecutionException(e, "Pop before SMTP error,");// i18n
		}
		finally{
			protocol.disconnect();
		}
	}
	
	public LinkedList<String> getToArray()
	{
		LinkedList<String> list = new LinkedList<String>();
		Iterator<String> it = this.protocol.getToAddress().iterator();
		while(it.hasNext()){
			String receiver = it.next();
			
			list.add(receiver);
		}
		return list;
	}
	
	public void sendMail(String fromAddress, String subject, String body)
	{
		this.protocol.setSubject(subject);
		this.protocol.setBody(body);
		this.protocol.setFromAddress(fromAddress);
		
		Iterator<String> it = this.protocol.getToAddress().iterator();
		while(it.hasNext()){
			String receiver = it.next();
			
			try {
				this.protocol.sendMultiple(receiver, this.mailconfig.getAuthUser(), 
						this.mailconfig.getAuthPass(), this.mailconfig.getAuthMethod());
			} catch (MailException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		it = this.protocol.getCcAddress().iterator();
		while(it.hasNext()){
			String receiver = it.next();
			
			try {
				this.protocol.sendMultiple(receiver, this.mailconfig.getAuthUser(), 
						this.mailconfig.getAuthPass(), this.mailconfig.getAuthMethod());
			} catch (MailException e) {
				e.printStackTrace();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		it = this.bccList.iterator();
		while(it.hasNext()){
			String receiver = it.next();
			
			try {
				this.protocol.sendMultiple(receiver, this.mailconfig.getAuthUser(), 
						this.mailconfig.getAuthPass(), this.mailconfig.getAuthMethod());
			} catch (MailException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void addTo(String to)
	{
		this.protocol.addToAddress(to);
	}
	
	public void clearTo()
	{
		this.protocol.clearTo();
	}
	
	public void addCc(String cc)
	{
		this.protocol.addCcAddress(cc);
	}
	
	public void clearCc()
	{
		this.protocol.clearCc();
	}
	
	public void addBcc(String bcc)
	{
		this.bccList.add(bcc);
	}
	
	public void clearBcc()
	{
		this.bccList.clear();
	}
	
	public void reset()
	{

		this.bccList.clear();
	}
	
	public void connect() throws AlinousException
	{
		try {
			this.protocol.connect();
		} catch (UnknownHostException e) {
			throw new AlinousException(e ,"Failed in connect to mail server.");
		} catch (IOException e) {
			throw new AlinousException(e ,"Failed in connect to mail server.");
		}
		

		
		// Authentication		
		String authWay = this.mailconfig.getAuthMethod();		
		if(authWay != null && authWay.toUpperCase().equals("LOGIN")){
			this.protocol.setUserName(this.mailconfig.getAuthUser());
			this.protocol.setPassword(this.mailconfig.getAuthPass());
			
			try {
				this.protocol.recv();
				this.protocol.ehloCommand(this.mailconfig.getAuthDomain());
				
				this.protocol.doAuthLogin();
			} catch (IOException e) {
				this.protocol.disconnect();
				throw new AlinousException(e ,"Failed in authentication.");
			}
		}
		// No Authentication	
		else{
			try {
				this.protocol.recv();
				this.protocol.helloCommand();
			} catch (IOException e) {
				this.protocol.disconnect();
				throw new AlinousException(e ,"Failed in connect to mail server.");
			}
		}
		
	}
	
	public void disconnect()
	{
		this.protocol.disconnect();
	}
	
}
