package org.alinous.filter;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;



public class SessionIdRewriteConfig{
	private CopyOnWriteArrayList<SessionIdRewriteArea> areaList = new CopyOnWriteArrayList<SessionIdRewriteArea>();
	
	public void addArea(String areaStr, String param)
	{
		SessionIdRewriteArea area =  new SessionIdRewriteArea();
		area.setArea(areaStr);
		area.setParam(param);
		
		this.areaList.add(area);
	}
	
	public void writeAsString(PrintWriter wr)
	{
		wr.print("	<sessionid>\n");
		
		Iterator<SessionIdRewriteArea> it = this.areaList.iterator();
		while(it.hasNext()){
			SessionIdRewriteArea areaStr = it.next();
			
			areaStr.writeAsString(wr);
		}
		
		wr.print("	</sessionid>\n");
	}
	
	public boolean isInsideArea(String path)
	{
		if(path == null){
			return false;
		}
		
		Iterator<SessionIdRewriteArea> it = this.areaList.iterator();
		while(it.hasNext()){
			SessionIdRewriteArea area = it.next();
			
			if(path.startsWith(area.getArea())){
				
				return true;
			}
		}

		return false;
	}
	
	public SessionIdRewriteArea getRewriteArea(String path)
	{
		if(path == null){
			return null;
		}
		
		Iterator<SessionIdRewriteArea> it = this.areaList.iterator();
		while(it.hasNext()){
			SessionIdRewriteArea area = it.next();
			
			if(path.startsWith(area.getArea())){
				return area;
			}
		}
		
		return null;
	}
};

