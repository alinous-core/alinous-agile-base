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
package org.alinous.expections;

import org.alinous.script.validator.ServerValidationRequest;

public class ServerValidationException extends ExecutionException
{
	private ServerValidationRequest request;
	
	public ServerValidationException(ServerValidationRequest request, String msg)
	{
		super(msg);
		this.request = request;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5279469938446199406L;

	public ServerValidationRequest getRequest()
	{
		return request;
	}
}
