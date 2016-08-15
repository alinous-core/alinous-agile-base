package org.alinous.net.pop3.api;

import java.io.IOException;
import java.net.UnknownHostException;

import org.alinous.exec.pages.IExtResource;
import org.alinous.expections.MailException;
import org.alinous.net.pop3.ListCommand;
import org.alinous.net.pop3.PassCommand;
import org.alinous.net.pop3.Pop3Protocol;
import org.alinous.net.pop3.QuitCommand;
import org.alinous.net.pop3.UserCommand;
import org.alinous.net.pop3.WelcomeCommand;

public class Pop3Connection implements IExtResource
{
	public static String POP3_RESOURCE_TYPE_NAME = "POP3_RESOURCE_TYPE_NAME";
	
	private Pop3Protocol protocol = new Pop3Protocol();
	private String name;
	
	public Pop3Connection(String user, String pass, String popServer, int popPort)
	{
		this.protocol.setUser(user);
		this.protocol.setPass(pass);
		this.protocol.setPopServer(popServer);
		this.protocol.setPopPort(popPort);
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}

	public String getType()
	{
		return POP3_RESOURCE_TYPE_NAME;
	}
	
	public void connect() throws UnknownHostException, IOException, MailException
	{
		this.protocol.connect();
		
		// welcome first read
		WelcomeCommand welCom = new WelcomeCommand(protocol);
		welCom.sendCommand();
		welCom.receiveCommand();
		
		// User
		UserCommand userCom = new UserCommand(this.protocol);
		userCom.sendCommand();
		userCom.receiveCommand();
		
		// Pass
		PassCommand passCom = new PassCommand(this.protocol);
		passCom.sendCommand();
		passCom.receiveCommand();
	}

	public void discard()
	{
		// Quit
		QuitCommand quitCom = new QuitCommand(protocol);
		try {
			quitCom.sendCommand();
			quitCom.receiveCommand();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MailException e) {
			e.printStackTrace();
		}
		
		
		this.protocol.disconnect();
		
	}
	
	public ListCommand list() throws MailException, IOException
	{
		// List
		ListCommand listCom = new ListCommand(this.protocol);
		listCom.sendCommand();
		listCom.receiveCommand();
		
		return listCom;
	}

	public Pop3Protocol getProtocol()
	{
		return protocol;
	}

}
