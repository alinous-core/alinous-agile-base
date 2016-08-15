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
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class AlinousServerDebugHttpResponse {
	public static final String TAG_ROOT = "ALINOUS_DEBUG";
	public static final String TAG_HOT_THREAD = "HOT_THREAD";
	
	public static final String ATTR_THREAD_ID = "threadId";
	
	private int result;
	private List<DebugThread> threadList = new CopyOnWriteArrayList<DebugThread>();
	private boolean shutdown;
	private long hotThread;
	
	public AlinousServerDebugHttpResponse(int result)
	{
		this.shutdown = false;
		this.result = result;
		this.hotThread = -1;
	}

	public int getResult() {
		return result;
	}

	public List<DebugThread> getThreadList() {
		return threadList;
	}
	
	public void addThread(DebugThread thread)
	{
		threadList.add(thread);
	}
	
	public String exportAsXml() throws IOException, ExecutionException
	{
		Element root = new Element(TAG_ROOT);
		
		// Hot Thread
		Element hotThreadElement = new Element(TAG_HOT_THREAD);
		hotThreadElement.setAttribute(ATTR_THREAD_ID, Long.toString(this.hotThread));
		
		root.addContent(hotThreadElement);
		
		// Threads
		Iterator<DebugThread> it = this.threadList.iterator();
		while(it.hasNext()){
			DebugThread thread = it.next();
			thread.exportIntoJDomElement(root);
		}
		
		Document doc = new Document(root);
		StringWriter writer = new StringWriter();
		
		try {
			new XMLOutputter().output(doc, writer);
		} catch (IOException e) {
			throw e;
		}
		
		String xmlStr = writer.toString();
		
		return xmlStr;
	}
	
	@SuppressWarnings("rawtypes")
	public void importFromXmlString(InputStream inStream) throws JDOMException, IOException, AlinousException
	{
		Document doc = null;
		
		doc = new SAXBuilder().build(inStream);
		
		Element root = doc.getRootElement();
		
		// set HotThread
		Element hotThreadElement = root.getChild(TAG_HOT_THREAD);
		if(hotThreadElement != null){
			String strHotThreadId = hotThreadElement.getAttributeValue(ATTR_THREAD_ID);
			if(strHotThreadId != null){
				this.hotThread = Long.parseLong(strHotThreadId);
			}
		}
		
		// import threads
		this.threadList.clear();
		Iterator it = root.getChildren(DebugThread.TAG_THREAD).iterator();
		while(it.hasNext()){
			Element threadElement = (Element)it.next();
			DebugThread thread = new DebugThread(0);
			
			thread.importFromJDomElement(threadElement);
			
			this.threadList.add(thread);
		}
		
	}

	public boolean isShutdown() {
		return shutdown;
	}

	public void setShutdown(boolean shutdown) {
		this.shutdown = shutdown;
	}

	public long getHotThread() {
		return hotThread;
	}

	public void setHotThread(long hotThread) {
		this.hotThread = hotThread;
	}
	
	public boolean containsThread(long threadId)
	{
		Iterator<DebugThread> it = this.threadList.iterator();
		while(it.hasNext()){
			DebugThread th = it.next();
			if(th.getThreadId() == threadId){
				return true;
			}
		}
		
		return false;
	}
	
	
}
