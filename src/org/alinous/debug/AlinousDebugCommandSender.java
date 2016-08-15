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
package org.alinous.debug;

import java.io.IOException;
import java.util.Map;

import org.alinous.debug.command.client.IClientRequest;
import org.alinous.expections.AlinousException;
import org.jdom.JDOMException;

public class AlinousDebugCommandSender {
	public static final String COMMAND = "COMMAND";
	
	private IClientRequest request;
	private IHttpAccessMethod httpAccessMethod;
	private String httpHost;
	
	public AlinousDebugCommandSender(String httpHost, IHttpAccessMethod httpAccessMethod)
	{
		this.httpAccessMethod = httpAccessMethod;
		this.httpHost = httpHost;
	}
	
	public AlinousServerDebugHttpResponse sendCommand(IClientRequest request) throws IOException, JDOMException, AlinousException
	{
		this.request = request;
		
		Map<String, String> map = this.request.getParamMap();
		map.put(COMMAND, request.getCommand());
		
		return this.httpAccessMethod.httpPost(this.httpHost, map);
	}
	
}
