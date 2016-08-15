package org.alinous.objects.optimize;

import java.io.IOException;
import java.io.Writer;

import org.alinous.exec.pages.PostContext;
import org.alinous.objects.Attribute;
import org.alinous.objects.DqString;
import org.alinous.objects.IAttribute;
import org.alinous.objects.IAttributeValue;
import org.alinous.objects.SqString;
import org.alinous.script.runtime.VariableRepository;

public class StaticAttribute implements IAttribute
{
	private String key;
	private StaticAttributeValue value;
	public String getKey()
	{
		return this.key;
	}
	
	public void setKey(String key)
	{
		this.key = key;
	}

	public IAttributeValue getValue()
	{
		return this.value;
	}

	public void setStaticValue(IAttributeValue strValue)
	{
		this.value = new StaticAttributeValue();
		this.value.setValue(strValue.getValue());
		
		if(strValue instanceof DqString){
			this.value.setQuote("\"");
		}
		else if(strValue instanceof SqString){
			this.value.setQuote("'");
		}
		

	}
	
	public boolean isDynamic()
	{
		return false;
	}
	
	public IAttribute clone() throws CloneNotSupportedException
	{
		Attribute attr = new Attribute();
		attr.setKey(this.key);
		attr.setValue(this.value);
		
		return attr;
	}
	
	public void renderContents(Writer wr, int n, PostContext context, VariableRepository valRepo, boolean adjustUri) throws IOException
	{
		wr.append(this.key);
		
		if(this.value != null && adjustUri){
			wr.append("=");
			this.value.renderAdjustedUriContents(wr, n, context, valRepo);
			return;
		}
		if(this.value != null){
			wr.append("=");
			this.value.renderContents(wr, n, context, valRepo);
		}
	}
	
	public IAttribute toStatic()
	{
		return this;
	}
	
	public class StaticAttributeValue implements IAttributeValue
	{
		private String value;
		private String quote;
		
		public String getParsedValue(PostContext context, VariableRepository valRepo)
		{
			return value;
		}

		public String getValue()
		{
			return value;
		}

		public boolean isDynamic()
		{
			return false;
		}

		public void renderAdjustedUriContents(Writer wr, int n, PostContext context, VariableRepository valRepo) throws IOException
		{
			wr.write(this.quote);
			wr.write(context.getFilePath(this.value));
			wr.write(this.quote);
		}

		public void renderContents(Writer wr, int n, PostContext context, VariableRepository valRepo) throws IOException
		{
			wr.write(toString());
		}

		public void setValue(String value)
		{
			this.value = value;
		}

		public void setQuote(String quote)
		{
			this.quote = quote;
		}
		
		public String toString()
		{
			return this.quote + this.value + this.quote;
		}
		
	}


}
