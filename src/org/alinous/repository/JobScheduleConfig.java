package org.alinous.repository;

import java.util.HashMap;
import java.util.Map;

public class JobScheduleConfig {
	private int interval;
	private String jobPath;
	private Map<String, String> paramMap = new HashMap<String, String>();
	
	
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
	public String getJobPath() {
		return jobPath;
	}
	public void setJobPath(String jobPath) {
		this.jobPath = jobPath;
	}
	public Map<String, String> getParamMap() {
		return paramMap;
	}
	
	
}
