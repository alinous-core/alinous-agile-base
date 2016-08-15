package org.alinous.repository;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class JobScheduleConfigList {
	private List<JobScheduleConfig> list = new CopyOnWriteArrayList<JobScheduleConfig>();
	
	public void writeAsString(PrintWriter wr)
	{
		wr.println("		<jobs>");
		
		Iterator<JobScheduleConfig> it = this.list.iterator();
		while(it.hasNext()){
			JobScheduleConfig conf = it.next();
			
			wr.print("		<job");
			wr.print(" interval=\"");
			wr.print(conf.getInterval());
			wr.print("\" jobpath=\"");
			wr.print(conf.getJobPath());
			wr.print("\">\n");
			
			Iterator<String> itParam = conf.getParamMap().keySet().iterator();
			while(itParam.hasNext()){
				String key = itParam.next();
				String val = conf.getParamMap().get(key);
				
				// params
				wr.print("			<param name=\"");
				wr.print(key);
				wr.print("\">");
				
				wr.print(val);
				wr.print("</param>\n");
			}
			
			wr.print("		</job>\n");
		}
		
		wr.println("		</jobs>\n");
	}
	
	public void add(JobScheduleConfig job)
	{
		this.list.add(job);
	}
	
	public List<JobScheduleConfig> getJobs()
	{
		return this.list;
	}
	
}
