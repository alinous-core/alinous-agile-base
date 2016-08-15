package org.alinous.net.pop3;

import java.io.IOException;

import org.alinous.expections.MailException;

public class DeleCommand extends AbstractPop3Command
{
	private int nn;
	
	public DeleCommand(Pop3Protocol popProtocol, int nn)
	{
		super(popProtocol);
		this.nn = nn;
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
		sendCommand("DELE " + this.nn + "\r\n");		
	}

}
