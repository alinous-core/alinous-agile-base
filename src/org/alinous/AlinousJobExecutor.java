package org.alinous;

import java.util.Iterator;

import org.alinous.exec.AccessExecutionUnit;
import org.alinous.exec.pages.PostContext;
import org.alinous.exec.pages.StringParamValue;
import org.alinous.expections.AlinousException;
import org.alinous.expections.DirectOutputException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.repository.JobScheduleConfig;
import org.alinous.repository.JobScheduleConfigList;
import org.alinous.script.runtime.VariableRepository;

public class AlinousJobExecutor {
	private AlinousCore core;
	
	public AlinousJobExecutor(AlinousCore core)
	{
		this.core = core;
	}
	
	public void schedule(int clock)
	{
		if(core.getConfig().getSystemRepositoryConfig().getJobs() == null){
			return;
		}
		
		JobScheduleConfigList configList = core.getConfig().getSystemRepositoryConfig().getJobs();
		Iterator<JobScheduleConfig> it = configList.getJobs().iterator();
		while(it.hasNext()){
			JobScheduleConfig jobConfig = it.next();
			
			if(clock % jobConfig.getInterval() == 0){
				JobRunner ruuner = new JobRunner(jobConfig);
				Thread th = new Thread(ruuner);
				th.start();				
			}

		}
	}
	
	
	class JobRunner implements Runnable{
		private JobScheduleConfig jobConfig;
		
		public JobRunner(JobScheduleConfig jobConfig)
		{
			this.jobConfig = jobConfig;
		}
		
		public void run() {
			String path = this.jobConfig.getJobPath();
			
			//AlinousDebug.debugOut("Job is runnning.... -> " + path);
			
			
			AccessExecutionUnit exec = core.createAccessExecutionUnit("batchSessionId");
			PostContext context = new PostContext(core, exec);
			
			String moduleName = AlinousUtils.getModuleName(path);
			try {
				// debug start
				if(AlinousCore.debug(context)){
					core.getAlinousDebugManager().startAlinousOperation(context);
				}
				
				core.registerAlinousObject(context, moduleName);
				
				Iterator<String> it = this.jobConfig.getParamMap().keySet().iterator();
				while(it.hasNext()){
					String key = it.next();
					String val = this.jobConfig.getParamMap().get(key);
					
					AlinousDebug.debugOut(null, "params " + key + " = " + val);
									
					context.getParamMap().put(key, new StringParamValue(val));
				}
				

				
				exec.gotoPage(moduleName, context, new VariableRepository());
				
			} catch (DirectOutputException de) {
				// do nothins
			} catch (RedirectRequestException re) {
				// do nothins
			}catch (AlinousException e) {
				e.printStackTrace();
				core.reportError(e);
			}finally{
				// debug end
				if(AlinousCore.debug(context)){
					core.getAlinousDebugManager().endAlinousOperation(context);
				}
				
				exec.dispose();
				context.dispose();
			}
			

			
		}
		
	}
}
