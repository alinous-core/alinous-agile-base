package org.alinous.net.pop3.format;

public class ContentTypeHeader extends MailHeader {

	public String getEncoding()
	{
		String headerBody = getHeaderBody();
		
		String contents[] = headerBody.split(";");
		for(int i = 0; i < contents.length; i++){
			String current = contents[i].trim();
			
			if(current.toUpperCase().startsWith("CHARSET=")){
				String ret = current.substring(8, current.length());
				
				if(ret.startsWith("\"")){
					return ret.substring(1, ret.length() - 1);
				}
				
				return ret;
			}
		}
		
		return null;
	}
	
	public String getBoundary()
	{
		String headerBody = getHeaderBody();
		
		String contents[] = headerBody.split(";");
		for(int i = 0; i < contents.length; i++){
			String current = contents[i].trim();
			
			if(current.toLowerCase().startsWith("boundary=")){
				String ret = current.substring("boundary=".length(), current.length());
				
				if(ret.startsWith("\"")){
					return ret.substring(1, ret.length() - 1);
				}
				
				return ret;
			}
		}
		
		return null;
	}
	
	public String getContentType()
	{
		String headerBody = getHeaderBody();
		
		String contents[] = headerBody.split(";");
		for(int i = 0; i < contents.length; i++){
			String current = contents[i].trim();
			
			if(current.toLowerCase().startsWith("multipart")){
				return current;
			}
		}
		
		return null;
	}
}
