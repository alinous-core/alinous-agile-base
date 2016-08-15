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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.alinous.expections.MailException;

public class SmtpProtocol
{
	public static final String AUTH_LOGIN = "AUTH_LOGIN";
	public static final String ISO2022JP = "ISO2022JP";
	
	private String smtpServer;
	private int smtpPort = 25;
	
	private String fromAddress;
	
	private List<String> toAddress = new ArrayList<String>();
	private List<String> ccAddress = new ArrayList<String>();
	
	private String subject;
	private String body;

	private String langEncoding;
	
	// Option
	private String userName;
	private String password;
	
	private Socket con;
	
	public SmtpProtocol()
	{
		
	}
	
	public void connect() throws UnknownHostException, IOException
	{
		//InetAddress addr = InetAddress.getByName(this.smtpServer);
		
		//con = new Socket(this.smtpServer, this.smtpPort);
		
		this.con = new Socket();
		
		InetSocketAddress addr = new InetSocketAddress(this.smtpServer, this.smtpPort);
		this.con.connect(addr, 4000);
		
	}
	
	public void disconnect()
	{
		try {
			con.close();
		} catch (IOException ignore) {}
	}
	
	// Sequence of sending one mail
	public void sendMail(String receiver) throws MailException, IOException
	{
		recv();
		helloCommand();
		mailCommand(this.fromAddress);
		rcptCommand(receiver);
		
		dataCommand(this.fromAddress);
		
		try{
			quitCommand();
		}catch(Throwable ignore){ }
		
	}
	
	public void sendMailWithAuth(String receiver, String user, String pass, String authWay, String eheloDomain)
				throws MailException, IOException
	{
		this.userName = user;
		this.password = pass;
		
		recv();
		ehloCommand(eheloDomain);
		
		// Authentication
		if(authWay != null && authWay.toUpperCase().equals(AUTH_LOGIN)){
			doAuthLogin();
		}
		
		mailCommand(this.fromAddress);
		rcptCommand(receiver);
		
		dataCommand(this.fromAddress);		
		
		try{
			quitCommand();
		}catch(Throwable ignore){}
		
	}
	
	public void sendMultiple(String receiver, String user, String pass, String authWay)
		throws MailException, IOException
	{
		this.userName = user;
		this.password = pass;
		
		sendResetCommand();
		//helloCommand();
		//ehloCommand();
		
		mailCommand(this.fromAddress);
		rcptCommand(receiver);
		
		dataCommand(this.fromAddress);		
		
	}
	
	public void doAuthLogin() throws IOException, MailException
	{
		AuthLoginCommand login = new AuthLoginCommand(this);
		login.sendCommand(con);
		login.receiveCommand(con);
		
		AuthLoginInputUserCommand userCom = new AuthLoginInputUserCommand(this);
		userCom.sendCommand(con);
		userCom.receiveCommand(con);
		
		AuthLoginInputPassCommand passCom = new AuthLoginInputPassCommand(this);
		passCom.sendCommand(con);
		passCom.receiveCommand(con);
	}
	
	// Command	
	public void recv() throws MailException, IOException
	{
		ISmtpCommand cmd = new NullCommand(this);
		
		recvCommand(cmd);
		
	}
	
	public void helloCommand() throws IOException, MailException
	{
		ISmtpCommand cmd = new HelloCommand(this);
		
		sendCommand(cmd);
		recvCommand(cmd);
	}
	
	public void ehloCommand(String eheloDomain) throws IOException, MailException
	{
		ISmtpCommand cmd = new EhloCommand(this, eheloDomain);
		
		sendCommand(cmd);
		recvCommand(cmd);
	}
	
	public void mailCommand(String fromAddr) throws IOException, MailException
	{
		MailComand cmd = new MailComand(this);
		
		cmd.setFromAddress(fromAddr);
		
		sendCommand(cmd);
		recvCommand(cmd);
	}
	
	public void rcptCommand(String receiver) throws IOException, MailException
	{
		RcptCommand cmd = new RcptCommand(this);
		
		cmd.setReceiver(receiver);
		
		sendCommand(cmd);
		recvCommand(cmd);
	}
	
	public void dataCommand(String fromAddress) throws IOException
	{
		DataCommand cmd = new DataCommand(this);
		
		cmd.sendCommand(con);
		cmd.receive(con);
		
		MailBodyCommand mailCmd = new MailBodyCommand(this);
		mailCmd.setFromAddress(fromAddress);
		
		mailCmd.sendCommand(con);
		mailCmd.receive(con);
	}
	
	public void quitCommand() throws IOException, MailException
	{
		ISmtpCommand cmd = new QuitCommand(this);
		
		sendCommand(cmd);
	}
	
	public void sendResetCommand() throws IOException, MailException
	{
		ISmtpCommand cmd = new ResetCommand(this);
		
		sendCommand(cmd);
		recvCommand(cmd);
	}
	
	private void sendCommand(ISmtpCommand cmd) throws IOException
	{
		cmd.sendCommand(this.con);
	}
	private void recvCommand(ISmtpCommand cmd) throws IOException, MailException
	{
		cmd.receiveCommand(this.con);
	}
	
	public void addToAddress(String addr)
	{
		this.toAddress.add(addr);
	}
	
	public void clearTo()
	{
		this.toAddress.clear();
	}
	
	public void addCcAddress(String addr)
	{
		this.ccAddress.add(addr);
	}
	
	public void clearCc()
	{
		this.ccAddress.clear();
	}
	
	public List<String> getCcAddress()
	{
		return ccAddress;
	}

	public List<String> getToAddress()
	{
		return toAddress;
	}

	public void reset()
	{
		this.toAddress.clear();
		this.ccAddress.clear();
	}
	
	// Getter and Setter
	public String getFromAddress()
	{
		return fromAddress;
	}

	public void setFromAddress(String mailAddress)
	{
		this.fromAddress = mailAddress;
	}

	public int getSmtpPort()
	{
		return smtpPort;
	}

	public void setSmtpPort(int smtpPort)
	{
		this.smtpPort = smtpPort;
	}

	public String getSmtpServer()
	{
		return smtpServer;
	}

	public void setSmtpServer(String smtpServer)
	{
		this.smtpServer = smtpServer;
	}

	public String getBody()
	{
		return body;
	}

	public void setBody(String body)
	{
		this.body = body;
	}

	public String getSubject()
	{
		return subject;
	}

	public void setSubject(String subject)
	{
		this.subject = subject;
	}

	public String getPassword()
	{
		return password;
	}

	public String getUserName()
	{
		return userName;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public String getLangEncoding()
	{
		return langEncoding;
	}

	public void setLangEncoding(String langEncoding)
	{
		this.langEncoding = langEncoding;
	}
	
}
