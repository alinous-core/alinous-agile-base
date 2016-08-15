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
package org.alinous.repository;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

public class SystemRepositoryConfig
{
	private String systemSrc;
	private String defaultSrc;
	
	private int uploadMaxsize;
	private String notfoundPage;
	private String encoding;
	private String adminPass;
	private String serial;
	private String errpage;
	
	private int threads = 32;
	
	private JobScheduleConfigList jobs;
	
	@SuppressWarnings("unchecked")
	public void setupJobSchedule(Element domEl)
	{
		this.jobs = new JobScheduleConfigList();
		
		List<Element> jobsList = domEl.getChildren("job");
		Iterator<Element> it = jobsList.iterator();
		while(it.hasNext()){
			Element job = it.next();
			
			String interval = job.getAttributeValue("interval");
			String path = job.getAttributeValue("jobpath");
			
			JobScheduleConfig config = new JobScheduleConfig();
			config.setInterval(Integer.parseInt(interval));
			config.setJobPath(path);
			
			List<Element> paramMap = job.getChildren("param");
			Iterator<Element> paramIt = paramMap.iterator();
			while(paramIt.hasNext()){
				Element paramEl = paramIt.next();
				
				String paramName = paramEl.getAttributeValue("name");
				String paramValue = paramEl.getText();
				
				config.getParamMap().put(paramName, paramValue);
			}
			
			this.jobs.add(config);
		}
		
	}
	
	public String getDefaultSrc()
	{
		return defaultSrc;
	}
	
	public void setDefaultSrc(String defaultSrc)
	{
		this.defaultSrc = defaultSrc;
	}
	
	public String getSystemSrc()
	{
		return systemSrc;
	}
	
	public void setSystemSrc(String systemSrc)
	{
		this.systemSrc = systemSrc;
	}

	public String getNotfoundPage()
	{
		return notfoundPage;
	}

	public void setNotfoundPage(String notfoundPage)
	{
		this.notfoundPage = notfoundPage;
	}

	public int getUploadMaxsize()
	{
		return uploadMaxsize;
	}

	public void setUploadMaxsize(String uploadMaxsize)
	{
		try{
			this.uploadMaxsize = Integer.parseInt(uploadMaxsize);
		}catch(Throwable e){
			this.uploadMaxsize = 10000000;
			return;
		}
		
		//this.uploadMaxsize = uploadMaxsize;
	}

	public String getEncoding()
	{
		return encoding;
	}

	public void setEncoding(String encoding)
	{
		//AlinousDebug.debugOut("************ encoding : " + encoding);
		this.encoding = encoding;
	}

	public String getAdminPass()
	{
		return adminPass;
	}

	public void setAdminPass(String adminPass)
	{
		this.adminPass = adminPass;
	}

	public void setUploadMaxsize(int uploadMaxsize)
	{
		this.uploadMaxsize = uploadMaxsize;
	}

	public void writeAsString(PrintWriter wr)
	{
		wr.println("	<system>");
		
		// sys data store
		if(this.systemSrc != null){
			wr.print("		<system-datastore id=\"");
			wr.print(this.systemSrc);
			wr.println("\"/>");
		}
		
		// default data store
		if(this.defaultSrc != null){
			wr.print("		<default-datastore id=\"");
			wr.print(this.defaultSrc);
			wr.println("\"/>");
		}
		
		// upload
		if(this.uploadMaxsize > 0){
			wr.print("		<upload maxsize=\"");
			wr.print(this.uploadMaxsize);
			wr.println("\"/>");
		}
		
		// parallel
		if(this.threads > 0){
			wr.print("		<parallel threads=\"");
			wr.print(this.threads);
			wr.println("\"/>");
		}
		
		// error notfound page
		if(this.notfoundPage != null){
			wr.print("		<notfound page=\"");
			wr.print(this.notfoundPage);
			wr.println("\"/>");
		}
		
		// admin-pass
		if(this.adminPass != null){
			wr.print("		<adminpass>");
			wr.print(this.adminPass);
			wr.println("</adminpass>");
		}
		
		// serial
		if(this.serial != null){
			wr.print("		<serial>");
			wr.print(this.serial);
			wr.println("</serial>");
		}
		
		// errpage
		if(this.errpage != null){
			wr.print("		<errpage>");
			wr.print(this.errpage);
			wr.println("</errpage>");
		}
		
		// jobs
		if(this.jobs != null){
			this.jobs.writeAsString(wr);
		}
		
		wr.println("	</system>");
	}

	public String getSerial()
	{
		return serial;
	}

	public void setSerial(String serial)
	{
		this.serial = serial;
	}

	public String getErrpage() {
		return errpage;
	}

	public void setErrpage(String errpage) {
		this.errpage = errpage;
	}

	public JobScheduleConfigList getJobs() {
		return jobs;
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}
	
	
}
