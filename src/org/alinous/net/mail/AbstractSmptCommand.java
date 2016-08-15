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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.Socket;

import org.apache.commons.codec.binary.Base64;


public abstract class AbstractSmptCommand implements ISmtpCommand
{
	protected SmtpProtocol proto;
	
	public AbstractSmptCommand(SmtpProtocol proto)
	{
		this.proto = proto;
	}
	
	protected void sendCommand(String strCommand, Socket con) throws IOException 
	{
		OutputStream stream = null;
		Writer writer = null;
		try {
			stream = con.getOutputStream();
			writer = new PrintWriter(stream, true);
			
			writer.write(strCommand);
			
		} catch (IOException e) {
			throw e;
		}finally{
			//stream.close();
			writer.flush();
			stream.flush();
		}
		
	}
	
	protected String receive(Socket con) throws IOException
	{
		InputStream inStream = con.getInputStream();
		
		byte [] buff = new byte[256];
		int nRead = inStream.read(buff);
		
		String received = new String(buff, 0, nRead);
		
		return received;
	}
	
	protected String base64Encode(String str)
	{
		/*BASE64Encoder encoder = new BASE64Encoder();
		
		String encodedStr;
		try {
			encodedStr = encoder.encodeBuffer(str.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		*/
		
		// encode
		String encodedStr = "";
		try {
			encodedStr = new String(Base64.encodeBase64(str.getBytes("UTF-8")), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		if(str.endsWith("\r\n") || !encodedStr.endsWith("\r\n")){
			encodedStr = encodedStr + "\r\n";
		}
		
		return encodedStr;
	}
}
