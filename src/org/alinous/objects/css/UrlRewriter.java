package org.alinous.objects.css;

public class UrlRewriter {
	private StringBuffer buff = new StringBuffer();
	private CssReplacer replacer;
	
	public UrlRewriter(CssReplacer replacer)
	{
		this.replacer = replacer;
	}
	
	public void append(String str)
	{
		this.buff.append(str);
	}

	@Override
	public String toString() {
		String originalValue = this.buff.toString();
		
		return this.replacer.replace(originalValue);
	}
	

	
}
