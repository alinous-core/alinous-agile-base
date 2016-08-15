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

package org.alinous.exec.lock;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionLockManager
{
	private Map<String, SessionLock> lockMap = new ConcurrentHashMap<String, SessionLock>();
	
	public SessionLock getLock(String sessionId)
	{
		SessionLock lock = this.lockMap.get(sessionId);
		
		if(lock == null){
			lock = new SessionLock(sessionId);
			this.lockMap.put(sessionId, lock);
		}
		
		return lock;
	}
	
	public void releaseLock(String sessionId)
	{
		if(!this.lockMap.containsKey(sessionId)){
			return;
		}
		this.lockMap.remove(sessionId);
	}
	
	public void clean()
	{
		Long nowLong = System.currentTimeMillis();
		Timestamp now = new Timestamp(nowLong);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		cal.add(Calendar.HOUR, -1);
		
		Timestamp limitTm = new Timestamp(cal.getTimeInMillis());
		
		Iterator<String> it = this.lockMap.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			SessionLock lock = this.lockMap.get(key);
			
			if(lock.getTimestamp().before(limitTm)){
				this.lockMap.remove(key);
			}
			
		}
	}
	
}
