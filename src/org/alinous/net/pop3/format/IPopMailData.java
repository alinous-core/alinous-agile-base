package org.alinous.net.pop3.format;

import java.util.Map;

public interface IPopMailData
{
	public static final int PARSE_HEADER = 1;
	public static final int PARSE_BODY = 2;
	public static final int PARSE_MULTI_PART_START = 100;
	public static final int PARSE_MULTI_HEADER = 101;
	public static final int PARSE_MULTI_BODY = 102;
	public static final int PARSE_MULTI_END  = 103;
	
	public Map<String, MailHeader> getHeaders();
	public void appendMimeData(String mimeData);
	
}
