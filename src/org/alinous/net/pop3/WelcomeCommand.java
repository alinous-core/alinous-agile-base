package org.alinous.net.pop3;

import java.io.IOException;

import org.alinous.expections.MailException;

public class WelcomeCommand extends AbstractPop3Command
{

	public WelcomeCommand(Pop3Protocol popProtocol)
	{
		super(popProtocol);
	}

	@Override
	public void receiveCommand() throws IOException, MailException
	{
		String res = receive();
		
		if(!res.startsWith("+OK")){
			throw new MailException(res);
		}
	}

	@Override
	public void sendCommand() throws IOException, MailException
	{
		
	}

}
