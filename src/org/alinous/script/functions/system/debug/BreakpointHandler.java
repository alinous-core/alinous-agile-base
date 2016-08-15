package org.alinous.script.functions.system.debug;

import org.alinous.debug.AlinousDebugManager;
import org.alinous.debug.ServerBreakpoint;

public class BreakpointHandler {
	private AlinousDebugManager manager;
	
	public BreakpointHandler(AlinousDebugManager manager)
	{
		this.manager = manager;
	}
	
	public void addBreakpoint(String filePath, int line)
	{
		ServerBreakpoint breakPoint = new ServerBreakpoint(filePath, line);
		this.manager.addBreakPoint(breakPoint);
	}
	
	public void clearBreakpoint(String filePath)
	{
		this.manager.clearBreakpoints(filePath);
	}
	
	public void removeBreakpoint(String filePath, int line)
	{
		this.manager.removeBreakpoint(filePath, line);
	}
}
