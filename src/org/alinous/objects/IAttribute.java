package org.alinous.objects;

import java.io.IOException;
import java.io.Writer;

import org.alinous.exec.pages.PostContext;
import org.alinous.script.runtime.VariableRepository;

public interface IAttribute
{
	public String getKey();
	public IAttributeValue getValue();
	public boolean isDynamic();
	public void renderContents(Writer wr, int n, PostContext context, VariableRepository valRepo,
			boolean adjustUri)throws IOException;
	public IAttribute toStatic();
	
	public IAttribute clone() throws CloneNotSupportedException;
}
