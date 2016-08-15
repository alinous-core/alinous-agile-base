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
package org.alinous.net.mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Iterator;

import org.alinous.expections.MailException;
import org.apache.commons.codec.binary.Base64;

public class MailBodyCommand extends AbstractSmptCommand
{
	private String fromAddress;
	
	public MailBodyCommand(SmtpProtocol proto)
	{
		super(proto);
	}

	public void receiveCommand(Socket con) throws IOException, MailException
	{
		String res = receive(con);
		
		if(!res.startsWith("250")){
			throw new MailException(res);
		}
	}

	public void sendCommand(Socket con) throws IOException
	{
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("FROM: " + this.fromAddress + "\r\n");
		appendTo(buffer);
		appendCc(buffer);
		
		// encoding
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		outStream.write(buffer.toString().getBytes(this.proto.getLangEncoding()));
		
		appendBody(outStream);
		
		outStream.flush();
		outStream.close();
		
		sendCommand(outStream.toByteArray(), con);
	}
	
	protected void sendCommand(byte[] byteArray, Socket con) throws IOException 
	{
		OutputStream stream = null;
		try {
			stream = con.getOutputStream();
			
			stream.write(byteArray);
			
		} catch (IOException e) {
			throw e;
		}finally{
			//stream.close();
			stream.flush();
		}
		
	}
	
	private void appendBody(OutputStream stream) throws IOException
	{
		String body = this.proto.getBody();
		String subject = this.proto.getSubject();
		//String contentType = "content-Type: text/plain; charset=\"" + this.proto.getLangEncoding() + "\"";//charset=ISO-8859-1
		String contentType = "content-Type: text/plain; charset=\"ISO-2022-JP\"";
		
		//body.replaceAll("\r\n\\.", "\r\n..");
		body = beforeEmail(body);
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("MIME-Version: 1.0\r\n");
		buffer.append(contentType + "\r\n");
		buffer.append("Content-Transfer-Encoding: 7bit\r\n");
		buffer.append("X-Mailer: Alinous-Core Mailer Engine\r\n");
		
		stream.write(buffer.toString().getBytes(this.proto.getLangEncoding()));
		
		
		if(this.proto.getLangEncoding() != null){
			String subjectStr = "Subject: " + getEncodedSubject(subject) + "\r\n";
			String subjectEnd = "\r\n";
			
			stream.write(subjectStr.getBytes(this.proto.getLangEncoding()));
			stream.write(subjectEnd.getBytes(this.proto.getLangEncoding()));
			
			String bodyEnd = "\r\n.\r\n";
			stream.write(body.getBytes(this.proto.getLangEncoding()));
			stream.write(bodyEnd.getBytes());
		}else{
			buffer = new StringBuffer();
			buffer.append("Subject: " + getEncodedSubject(subject) + "\r\n");
			buffer.append("\r\n");
			
			buffer.append(body);
			buffer.append("\r\n.\r\n");
			
			stream.write(buffer.toString().getBytes(this.proto.getLangEncoding()));
		}
		
		
	}
	
	private String getEncodedSubject(String subject)
	{
		StringBuffer buff = new StringBuffer();
		
		buff.append("=?ISO-2022-JP?B?");
		
		buff.append(base64Encode(beforeEmail(subject), "ISO-2022-JP") + "?=");
		
		return buff.toString();
	}
	
	protected String base64Encode(String str, String encoding)
	{
		// encode
		String encodedStr = "";
		try {
			// encoding
			//encodedStr = new String(Base64.encodeBase64(str.getBytes(this.proto.getLangEncoding())), this.proto.getLangEncoding());
			encodedStr = new String(Base64.encodeBase64(str.getBytes(encoding)));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return encodedStr;
	}
	
	private void appendTo(StringBuffer buffer)
	{
		if(this.proto.getToAddress().isEmpty()){
			return;
		}
		
		buffer.append("TO: ");
		
		boolean first = true;
		Iterator<String> it = this.proto.getToAddress().iterator();
		while(it.hasNext()){
			String addr = it.next();
			
			if(first){
				first = false;
			}else{
				buffer.append(",");
			}
			
			buffer.append(addr);
		}
		
		
		buffer.append("\r\n");
	}
	
	private void appendCc(StringBuffer buffer)
	{
		if(this.proto.getCcAddress().isEmpty()){
			return;
		}
		
		buffer.append("CC: ");
		
		boolean first = true;
		Iterator<String> it = this.proto.getCcAddress().iterator();
		while(it.hasNext()){
			String addr = it.next();
			
			if(first){
				first = false;
			}else{
				buffer.append(",");
			}
			
			buffer.append(addr);
		}
		
		buffer.append("\r\n");
	}

	public String getFromAddress()
	{
		return fromAddress;
	}

	public void setFromAddress(String fromAddress)
	{
		this.fromAddress = fromAddress;
	}
	
    public static String beforeEmail(String str) {    //
        StringBuilder buf = new StringBuilder("");
        for (int pos = 0; pos < str.length(); pos += 1) {
            char ch = str.charAt(pos);
            switch (ch) {
            case 0x2015:
                buf.append(java.lang.Character.toChars(0x2014));
                break;
            case 0x2116:
                buf.append("No.");
                break;
            case 0x2121:
                buf.append("TEL");
                break;
            case 0x2160:
                buf.append("I");
                break;
            case 0x2161:
                buf.append("II");
                break;
            case 0x2162:
                buf.append("III");
                break;
            case 0x2163:
                buf.append("IV");
                break;
            case 0x2164:
                buf.append("V");
                break;
            case 0x2165:
                buf.append("VI");
                break;
            case 0x2166:
                buf.append("VII");
                break;
            case 0x2167:
                buf.append("VIII");
                break;
            case 0x2168:
                buf.append("IX");
                break;
            case 0x2169:
                buf.append("X");
                break;
            case 0x2170:
                buf.append("i");
                break;
            case 0x2171:
                buf.append("ii");
                break;
            case 0x2172:
                buf.append("iii");
                break;
            case 0x2173:
                buf.append("iv");
                break;
            case 0x2174:
                buf.append("v");
                break;
            case 0x2175:
                buf.append("vi");
                break;
            case 0x2176:
                buf.append("vii");
                break;
            case 0x2177:
                buf.append("viii");
                break;
            case 0x2178:
                buf.append("ix");
                break;
            case 0x2179:
                buf.append("x");
                break;
            case 0x2211:
                buf.append("Σ");
                break;
            case 0x221f:
                buf.append("L");
                break;
            case 0x2225:
                buf.append(java.lang.Character.toChars(0x2016));
                break;
            case 0x2460:
                buf.append("(1)");
                break;
            case 0x2461:
                buf.append("(2)");
                break;
            case 0x2462:
                buf.append("(3)");
                break;
            case 0x2463:
                buf.append("(4)");
                break;
            case 0x2464:
                buf.append("(5)");
                break;
            case 0x2465:
                buf.append("(6)");
                break;
            case 0x2466:
                buf.append("(7)");
                break;
            case 0x2467:
                buf.append("(8)");
                break;
            case 0x2468:
                buf.append("(9)");
                break;
            case 0x2469:
                buf.append("(10)");
                break;
            case 0x246a:
                buf.append("(11)");
                break;
            case 0x246b:
                buf.append("(12)");
                break;
            case 0x246c:
                buf.append("(13)");
                break;
            case 0x246d:
                buf.append("(14)");
                break;
            case 0x246e:
                buf.append("(15)");
                break;
            case 0x246f:
                buf.append("(16)");
                break;
            case 0x2470:
                buf.append("(17)");
                break;
            case 0x2471:
                buf.append("(18)");
                break;
            case 0x2472:
                buf.append("(19)");
                break;
            case 0x2473:
                buf.append("(20)");
                break;
            case 0x301f:
                buf.append("\"");
                break;
            case 0x3231:
                buf.append("(株)");
                break;
            case 0x3239:
                buf.append("(代)");
                break;
            case 0x32a4:
                buf.append("(上)");
                break;
            case 0x32a5:
                buf.append("(中)");
                break;
            case 0x32a6:
                buf.append("(下)");
                break;
            case 0x32a7:
                buf.append("(左)");
                break;
            case 0x32a8:
                buf.append("(右)");
                break;
            case 0x3303:
                buf.append("アール");
                break;
            case 0x330d:
                buf.append("カロリー");
                break;
            case 0x3314:
                buf.append("キロ");
                break;
            case 0x3318:    
                buf.append("グラム");
                break;
            case 0x3322:    
                buf.append("センチ");
                break;
            case 0x3323:
                buf.append("セント");
                break;
            case 0x3326:
                buf.append("ドル");
                break;
            case 0x3327:
                buf.append("トン");
                break;
            case 0x332b:
                buf.append("パーセント");
                break;
            case 0x3336:
                buf.append("ヘクタール");
                break;
            case 0x333b:    
                buf.append("ページ");
                break;
            case 0x3349:    
                buf.append("ミリ");
                break;
            case 0x334d:
                buf.append("メートル");
                break;
            case 0x3351:
                buf.append("リットル");
                break;
            case 0x3357:
                buf.append("ワット");
                break;
            case 0x334a:
                buf.append("ミリバール");
                break;
            case 0x337b:
                buf.append("平成");
                break;
            case 0x337c:
                buf.append("昭和");
                break;
            case 0x337d:
                buf.append("大正");
                break;
            case 0x337e:
                buf.append("明治");
                break;
            case 0x338e:
                buf.append("mg");
                break;
            case 0x338f:
                buf.append("kg");
                break;
            case 0x339c:
                buf.append("mm");
                break;
            case 0x339d:
                buf.append("cm");
                break;
            case 0x339e:
                buf.append("km");
                break;
            case 0x33a1:
                buf.append("m2");
                break;
            case 0x33c4:
                buf.append("cc");
                break;
            case 0x33cd:
                buf.append("K.K.");
                break;
            case 0xff0d:  // '−':全角マイナス
                buf.append(java.lang.Character.toChars(0x2212));
                break;
            case 0xff5e:  // '〜':全角チルダ
                buf.append(java.lang.Character.toChars(0x301c));
                break;
            case 0xffe0:  // '¢':全角通貨記号セント
                buf.append(java.lang.Character.toChars(0x00a2));
                break;
            case 0xffe1:  // '£':全角ポンドサイン
                buf.append(java.lang.Character.toChars(0x00a3));
                break;
            case 0xffe2:  // '¬':全角否定
                buf.append(java.lang.Character.toChars(0x00ac));
                break;
            default:
                buf.append(ch);
            }
        }
        return buf.toString();
    }

	
}
