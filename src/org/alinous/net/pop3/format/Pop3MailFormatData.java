/**
 * CROSSFIRE JAPAN INCORPORATED
 * This source code is under GPL License.
 * info@crossfire.jp
 * Official web site
 * http://alinous.org
 * 
 *  Copyright (C) 2007 Tomohiro Iizuka
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.alinous.net.pop3.format;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class Pop3MailFormatData implements IPopMailData
{
	private int status;
	private Map<String, MailHeader> headers = new Hashtable<String, MailHeader>();
	
	private MailHeader thisHeader = null;
	private StringBuffer mainBody = new StringBuffer();
	
	private List<IPopMailData> parts = new ArrayList<IPopMailData>();
	private IPopMailData thisPart;
	
	public static final String ContentType = "CONTENT-TYPE";
	public static final String Subject = "SUBJECT";
	
	public Pop3MailFormatData()
	{
		this.status = PARSE_HEADER;
		
	}
	
	public int parseLine(String line)
	{
		switch(this.status){
		case PARSE_HEADER:
			this.status = parseHeader(line);
			break;		
		case PARSE_BODY:
			try {
				this.status = parseBody(line);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace(); // ignore
			}
			break;
		case PARSE_MULTI_PART_START:
		case PARSE_MULTI_HEADER:
			this.status = parseMultipartHeader(line);
			break;
		case PARSE_MULTI_BODY:
			this.status = parseMultipartBody(line);
			break;
		case PARSE_MULTI_END:
			break;
		default:
			break;
		}
		
		return 0;
	}
	
	private int parseMultipartHeader(String line)
	{
		if(line.equals("")){
			return PARSE_MULTI_BODY;
		}
		if(line.startsWith("\t") || line.startsWith(" ") || line.indexOf(':') < 0){
			this.thisHeader.parseLine(line);
			
			return PARSE_MULTI_HEADER;
		}
		// make new header
		this.thisHeader = MailHeaderFactory.create(line);
		this.thisHeader.parseLine(line);
		this.thisPart.getHeaders().put(this.thisHeader.getHeaderName().toUpperCase(), this.thisHeader);
		
		return PARSE_MULTI_HEADER;		
	}
	
	private int parseMultipartBody(String line)
	{
		// next multipart
		String start_boundary = getBoundart();
		String end_boundary = start_boundary + "--";
		if(line.equals(start_boundary)){
			this.thisPart = new MultiPartData();
			this.parts.add(this.thisPart);
			
			return PARSE_MULTI_HEADER;
		}
		else if(line.equals(end_boundary)){
			return PARSE_MULTI_END;
		}
		
		this.thisPart.appendMimeData(line);
		
		return PARSE_MULTI_BODY;
	}
	
	
	private int parseBody(String line) throws UnsupportedEncodingException
	{
		String encoding = getEncoding();
		String bodyString = new String(line.getBytes(), encoding);
		
		// multipart or not
		String boundary = getBoundart();
		boolean mul = isMultipart();
		if(mul && line.equals(boundary)){
			this.thisPart = new MultiPartData();
			this.parts.add(this.thisPart);
			
			return PARSE_MULTI_HEADER;
		}
		
		this.mainBody.append(bodyString);
		this.mainBody.append("\n");
		
		return PARSE_BODY;
	}
	
	private int parseHeader(String line)
	{
		if(line.equals("")){
			return PARSE_BODY;
		}
		if(line.startsWith("\t") || line.startsWith(" ") || line.indexOf(':') < 0){
			this.thisHeader.parseLine(line);
			
			return PARSE_HEADER;
		}
		
		// make new header
		this.thisHeader = MailHeaderFactory.create(line);
		this.thisHeader.parseLine(line);
		
		this.headers.put(this.thisHeader.getHeaderName().toUpperCase(), this.thisHeader);
		
		return PARSE_HEADER;
	}
	
	protected String getBoundart()
	{
		ContentTypeHeader contentType = (ContentTypeHeader)this.headers.get(ContentType);
		return "--" + contentType.getBoundary();
	}
	
	public boolean isMultipart()
	{
		MailHeader contentType = this.headers.get(ContentType);
		
		if(contentType == null){
			return false;
		}
		
		if(((ContentTypeHeader)contentType).getContentType() == null){
			return false;
		}
		
		
		return ((ContentTypeHeader)contentType).getContentType().toLowerCase().startsWith("multipart");
	}
	
	public boolean isMultipartFinidhed()
	{
		return this.status == PARSE_MULTI_END;
	}
	
	private String getEncoding()
	{
		MailHeader contentType = this.headers.get(ContentType);
		
		if(contentType == null){
			return "ISO-2022-JP";
		}
		
		String encoding = ((ContentTypeHeader)contentType).getEncoding();
		
		if(encoding == null){
			return "ISO-2022-JP";
		}
		
		return encoding;
	}
	
	public String getSubject()
	{
		MailHeader subject = this.headers.get(Subject);
		
		if(subject == null){
			return null;
		}
		
		return subject.getHeaderBody();
	}
	
	public String getBodyString()
	{
		return this.mainBody.toString();
	}

	public Map<String, MailHeader> getHeaders()
	{
		return this.headers;
	}

	public void appendMimeData(String mimeData)
	{
		
	}
	
}
