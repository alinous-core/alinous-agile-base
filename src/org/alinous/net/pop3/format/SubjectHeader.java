package org.alinous.net.pop3.format;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.QuotedPrintableCodec;

public class SubjectHeader extends MailHeader {
	public static final String SECTION_START = "=?";
	public static final String SECTION_END = "?=";
	
	public String getHeaderBody() {
		String body = super.getHeaderBody();
		
		StringBuffer buffer = new StringBuffer();
		
		parseMimeSubject(body, buffer);
		
		//[SPAM] =?iso-2022-jp?Q?=1B$B%F%9%H%a!=3C%k=1B=28B?=
		//=?ISO-2022-JP?B?GyRCJDMkcyRLJEEkTyEiJCo4NTUkJEckORsoQg==?=
		//=?ISO-2022-JP?B?GyRCJCshKUtNJGI4NTUkJEo1JCQsJDckShsoQg==?=
		// =?ISO-2022-JP?B?GyRCJCQkRyRPJEokJCRHJDkhIxsoQg==?=
		
		return buffer.toString();
	}
	
	private void parseMimeSubject(String body, StringBuffer buffer)
	{
		int pos = 0;
		while(pos < body.length()){
			int index = body.indexOf(SECTION_START, pos);
			if(index < 0){
				buffer.append(body.substring(pos, body.length()));
				break;
			}
			
			// there is unencoded string between pos and index
			String unencodedStr = body.substring(pos, index);
			if(unencodedStr.startsWith("\t")){
				unencodedStr = unencodedStr.substring(1);
			}
			buffer.append(unencodedStr);
			
			//pos is too little
			int indexEnd = body.indexOf(SECTION_END, index);
			
			String section = null;
			String testSec = body.substring(index, indexEnd + SECTION_END.length());
			
			if(testSec.toUpperCase().startsWith("=?UTF-8?Q?")){
				indexEnd = body.indexOf(SECTION_END, index + "=?UTF-8?Q?".length());
			}
			else if(testSec.toUpperCase().startsWith("=?ISO-2022-JP?Q?")){
				indexEnd = body.indexOf(SECTION_END, index + "=?ISO-2022-JP?Q?".length());
			}

			section = body.substring(index, indexEnd + SECTION_END.length());
			pos = indexEnd + SECTION_END.length();

			
			String sectionStr = decodeBase64Section(section);
			buffer.append(sectionStr);
		}
		
		// =?UTF-8?Q?=E3=80=90Yahoo?=!=?UTF-8?Q?=E3=83=AA=E3=82=B9=E3=83=86=E3=82=A3=E3=83=B3=E3=82=B0?==?UTF-8?Q?=E5=BA=83=E5=91=8A=E3=80=91=E3=82=A2=E3=82=AB=E3=82=A6?==?UTF-8?Q?=E3=83=B3=E3=83=88=E3=81=AB=E9=96=A2=E3=81=99=E3=82=8B?==?UTF-8?Q?=E3=81=8A=E7=9F=A5=E3=82=89=E3=81=9B?=: '=?UTF-8?Q?=E6=A0=AA=E5=BC=8F=E4=BC=9A=E7=A4=BE?= =?UTF-8?Q?=E3=82=AF=E3=83=AA=E3=82=A2=E3=83=BC?=' [20423602618]
		// =?ISO-2022-JP?B?GyRCOCE6dz90JCxCZ0l9JEtBfSQoJD8bKEIyMDEwGyRCRy8lUhsoQg==?=
	}
	
	
	
	protected String decodeBase64Section(String section)
	{
		int pos = section.indexOf('?', SECTION_START.length());
		String encoding = section.substring(SECTION_START.length(), pos);
		
		int bend = section.indexOf('?', pos + 1);
		String encodeType = section.substring(pos + 1, bend);
		bend++;
		
		int bodyEnd = section.indexOf(SECTION_END, bend);
		if(bend + 1 > bodyEnd){
			return "";
		}
		
		String body = section.substring(bend, bodyEnd);
		
		String decoded = "";
		if(encodeType.toUpperCase().equals("B")){
			try {
				decoded = base64Decode(body, encoding);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		else{
			try {
				decoded = decodeQuotedPrintableCodec(body, encoding);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (DecoderException e) {
				e.printStackTrace();
			}
		}		
		
		return decoded;
	}
	
	protected String decodeQuotedPrintableCodec(String str, String encoding) throws UnsupportedEncodingException, DecoderException
	{
		QuotedPrintableCodec codec = new QuotedPrintableCodec();
		
		String retStr = codec.decode(str, encoding);
		
		return retStr;
	}
	
	protected String base64Decode(String str, String encoding) throws UnsupportedEncodingException
	{
		String retStr = new String(Base64.decodeBase64(str.getBytes()), encoding);
		
		return retStr;
	}
}
