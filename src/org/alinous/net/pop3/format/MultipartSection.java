package org.alinous.net.pop3.format;

import java.util.Hashtable;
import java.util.Map;

public class MultipartSection
{
	private int status;
	private Map<String, MailHeader> headers = new Hashtable<String, MailHeader>();
	
	private MailHeader thisHeader = null;
	
	public int parseLine(String line)
	{
		switch(this.status){
		case IPopMailData.PARSE_HEADER:
			this.status = parseHeader(line);
			break;		
		case IPopMailData.PARSE_BODY:
		/*	try {
				//this.status = parseBody(line);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace(); // ignore
			}*/
			break;
		case IPopMailData.PARSE_MULTI_PART_START:
		case IPopMailData.PARSE_MULTI_HEADER:
		case IPopMailData.PARSE_MULTI_BODY:
			//this.status = this.thisPart.parseLine(line);
			break;
		default:
			break;
		}
		
		return 0;
	}
	
	private int parseHeader(String line)
	{
		if(line.startsWith("\t") || line.startsWith(" ") || line.indexOf(':') < 0){
			this.thisHeader.parseLine(line);
			
			return IPopMailData.PARSE_HEADER;
		}
		
		// make new header
		this.thisHeader = MailHeaderFactory.create(line);
		this.thisHeader.parseLine(line);
		
		this.headers.put(this.thisHeader.getHeaderName().toUpperCase(), this.thisHeader);
		
		return IPopMailData.PARSE_HEADER;
	}
}
