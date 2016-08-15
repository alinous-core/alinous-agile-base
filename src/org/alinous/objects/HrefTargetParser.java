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
package org.alinous.objects;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.alinous.exec.InnerModulePath;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.objects.html.AlinousTopObject;
import org.alinous.objects.html.FormTagObject;

public class HrefTargetParser
{
	public static final String THIS_TARGET_ID = "this";
	
	private String href;
	private String tagId;
	private String toptopPage;
	private AlinousTopObject thisPage;
	
	private String pageSection;
	private String paramSection;
	private Map<String, String> paramsMap = new HashMap<String, String>();
	
	public HrefTargetParser(String href)
	{
		this.href = href;
	}
	
	
	public void setTargetTagId(String tagId)
	{
		this.tagId = tagId;
	}
	
	public void setTopTopPage(String topPage)
	{
		this.toptopPage = topPage;
	}
	
	public void setThisPagePath(AlinousTopObject thisPage)
	{
		this.thisPage = thisPage;
	}

	
	public String getString(PostContext context) throws AlinousException
	{
		parse();
		
		String formTargetPath = getTargetPath(context);
		this.paramsMap.put(FormTagObject.HIDDEN_FORM_ACTION, this.pageSection);
		this.paramsMap.put(FormTagObject.HIDDEN_FORM_TARGET_TAGID, formTargetPath);
		
		StringBuffer buff = new StringBuffer();
		
		// specify top page
		if(!formTargetPath.equals("")){
			buff.append(this.toptopPage);
		}else{
			buff.append(this.pageSection);
		}
		
		buff.append("?");
		
		// params
		boolean first = true;
		Iterator<String> it = this.paramsMap.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			String val = this.paramsMap.get(key);
			
			// ignore if not necessary
			if(key.toUpperCase().equals(FormTagObject.HIDDEN_FORM_ACTION.toUpperCase())
					&& formTargetPath.equals("")){
				continue;
			}
			if(key.toUpperCase().equals(FormTagObject.HIDDEN_FORM_TARGET_TAGID.toUpperCase())
					&& formTargetPath.equals("")){
				continue;
			}
			
			if(first){
				first = false;
			}
			else{
				buff.append("&");
			}
			try {
				key = URLEncoder.encode(key, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			buff.append(key);
			buff.append("=");
			buff.append(val);
		}
		
		return buff.toString();
	}
	
	private String getTargetPath(PostContext context) throws AlinousException
	{
		InnerModulePath modPath = context.getModulePath().deepClone();
		
		// handle this tag value
		if(!this.tagId.equals(THIS_TARGET_ID)){
			modPath.addPath(this.thisPage.getPath());
			modPath.addTarget(this.tagId);
		}
		
		return modPath.getStringPath();
	}
	
	private void parse() throws ExecutionException
	{
		String sections[] = this.href.split("\\?");
		
		this.pageSection = sections[0];
		if(this.pageSection.endsWith("/")){
			this.pageSection = this.pageSection + "index.html";
		}
		
		if(sections.length > 1){
			this.paramSection = sections[1];
		}
		else{
			return;
		}
		
		String params[] = this.paramSection.split("&");
		for(int i = 0; i < params.length; i++){
			String mapStrs[] = params[i].split("=");
			
			if(mapStrs.length != 2){
				//throw new ExecutionException("URL format is wrong."); // i18n
				continue;
			}
			
			//add
			this.paramsMap.put(mapStrs[0], mapStrs[1]);
		}
		
	}
	
}
