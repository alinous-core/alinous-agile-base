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
package org.alinous;

import org.alinous.datasrc.basic.ILogProvidor;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.expections.ExecutionException;
import org.alinous.repository.AlinousSystemRepository;

public class AlinousMaintainThreadManager implements Runnable
{
	private boolean stop;
	private AlinousSystemRepository systemRepo;
	private AlinousCore alinousCore;
	private Thread thread;
	private AlinousJobExecutor executor;
	
	private ILogProvidor log;
	
	public AlinousMaintainThreadManager(AlinousSystemRepository systemRepo, ILogProvidor log, AlinousCore core)
	{
		this.systemRepo = systemRepo;
		this.log = log;
		this.alinousCore = core;
		this.executor = new AlinousJobExecutor(core);
	}
	
	public void start()
	{
		this.thread = new Thread(this);
		
		this.thread.start();
	}
	
	public void stop()
	{
		this.stop = true;
	}
	
	public void run()
	{
		synchronized (this) {
			loop();
		}
	}
	
	private void loop()
	{
		int sleepMilli =  1000 * 60;
		int i = 0;
		while(!stop){
			if(i % sleepMilli == 0){
				try {
					this.systemRepo.cleanOldSessionData(this.alinousCore);
				} catch (ExecutionException e) {
					this.log.reportError(e);
				} catch (DataSourceException e) {
					this.log.reportError(e);
				}
				
				maintainSessionLock();
				
				this.alinousCore.getModuleRepository().clean();
				
			}
			
			// exec scheduled job
			this.executor.schedule(i);
			
			if(i > 100000000){
				i = 0;
			}
			i++;
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private void maintainSessionLock()
	{
		this.alinousCore.getSesssionLockManager().clean();
	}
}
