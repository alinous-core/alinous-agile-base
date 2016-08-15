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
package org.alinous.jdk;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;


public class AlinousURLStreamHandler extends URLStreamHandler
{
	private byte[] byteData;
	
	@Override
	protected URLConnection openConnection(URL u) throws IOException
	{
		// debug
		//AlinousDebug.debugOut("openConnection() this.byteData : " + this.byteData);
		
		URLConnection con = null;
		
		if(u.getPath().endsWith(".properties") || u.getPath().endsWith(".xml")
				|| u.getPath().endsWith(".map") || u.getPath().endsWith(".providers")){
			// debug
			//AlinousDebug.debugOut("properties this.byteData : " + new String(this.byteData));
			//AlinousDebug.debugOut("properties getContextClassLoader : " + Thread.currentThread().getContextClassLoader());
			//AlinousDebug.debugOut("properties getStackTrace : " + Thread.currentThread().getStackTrace());
			
			con = new TextUrlConnection(u, this.byteData);
			
			
		}
		else{
			con = new ByteEntryURLConnection(u, this.byteData);
		}
		
		return con;
	}

	public byte[] getByteData()
	{
		return byteData;
	}

	public void setByteData(byte[] byteData)
	{
		this.byteData = byteData;
	}
	
	
}
