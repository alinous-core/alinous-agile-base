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

import org.alinous.security.Zone;

public class AlinousSecurityException extends AlinousException
{
	private static final long serialVersionUID = 1L;
	
	public static final int REASON_TOP = 1;
	public static final int REASON_INNER = 2;
	
	private Zone zone;
	private int reason;
	
	public AlinousSecurityException(int reason, Zone zone)
	{
		this.reason = reason;
		this.zone = zone;
	}
	
	public AlinousSecurityException(Zone z){
		this.zone = z;
	}
	
	public String getMessage(){
		return "Security Error";
	}

	public Zone getZone()
	{
		return zone;
	}

	public void setZone(Zone zone)
	{
		this.zone = zone;
	}

	public int getReason()
	{
		return reason;
	}

	public void setReason(int reason)
	{
		this.reason = reason;
	}
	
	
}
