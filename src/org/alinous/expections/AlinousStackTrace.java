package org.alinous.expections;

import java.util.Iterator;
import java.util.Stack;

public class AlinousStackTrace {
	private Stack<AlinousStackTraceElement> stack = new Stack<AlinousStackTraceElement>();
	
	public AlinousStackTrace()
	{
		
	}
	
	public void importTrace(AlinousStackTrace trace)
	{
		Iterator<AlinousStackTraceElement> it = trace.stack.iterator();
		
		while(it.hasNext()){
			AlinousStackTraceElement item = it.next();
			
			this.stack.push(item);
		}
	}
	
	public void addStack(int line, String filePath)
	{
		AlinousStackTraceElement el = new AlinousStackTraceElement();
		el.setLine(line);
		el.setFilePath(filePath);
		
		
		if(!this.stack.empty() && this.stack.peek().equals(el)){
			return;
		}
		
		this.stack.push(el);
	}
	
	public String stackTraceHtmlString()
	{
		StringBuffer buff = new StringBuffer();
		
		buff.append("\nAlinous-Core Stack trace...\n");
		
		Iterator<AlinousStackTraceElement> it = this.stack.iterator();
		while(it.hasNext()){
			AlinousStackTraceElement item = it.next();
			
			buff.append(item.getFilePath());
			buff.append(" : at line ");
			buff.append(Integer.toString(item.getLine()));
			buff.append("\n");
		}
		
		return buff.toString();
	}
}
