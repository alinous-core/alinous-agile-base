package org.alinous.objects.optimize;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.objects.IAlinousObject;
import org.alinous.objects.XMLTagBase;
import org.alinous.script.runtime.VariableRepository;

public class StaticBuffer extends XMLTagBase
{
	private StringWriter writer = new StringWriter();

	public StringWriter getWriter()
	{
		return writer;
	}
	
	public void writeBuffer(Writer wr) throws IOException
	{
		wr.write(this.writer.getBuffer().toString());
	}

	public IAlinousObject fork() throws AlinousException
	{
		return this;
	}

	public void renderContents(PostContext context, VariableRepository valRepo, Writer wr, int n) throws IOException, AlinousException
	{
		writeBuffer(wr);
		// opt chk
		/*AlinousDebug.debugOut("------------------------OPRIMIZATION CHECK----------------------");
		AlinousDebug.debugOut(writer.getBuffer().toString());
		AlinousDebug.debugOut("-----------------------------------------------------------------");*/
	}

	public void optimizeSelf(PostContext context, XMLTagBase owner, boolean forceDynamic) throws IOException, AlinousException
	{
		
	}


	
}
