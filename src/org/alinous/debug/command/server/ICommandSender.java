package org.alinous.debug.command.server;

import java.io.IOException;

import org.alinous.debug.AbstractAlinousDebugManager;
import org.alinous.exec.pages.PostContext;

public interface ICommandSender {
	public void sendCommand(IServerCommand command, PostContext context) throws IOException;
	public AbstractAlinousDebugManager getDebugManager();
	void debugOut(String str);
}
