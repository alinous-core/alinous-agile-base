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
package org.alinous.script.runtime;

import java.io.StringReader;

import org.alinous.parser.script.attr.AlinousAttrScriptParser;
import org.alinous.script.attribute.ParsedAttribute;

public class PathElementFactory
{
	public static IPathElement buildPathElement(String path)
	{
		// if contains -
		if(path.indexOf('-') >= 0){
			return buildPathElement2(path);
		}
		
		ParsedAttribute ret = null;
		StringReader reader = new StringReader("<{$" + path + "}>");
		
		AlinousAttrScriptParser parser = new AlinousAttrScriptParser(reader);
		
		try {
			ret = parser.parse();
		} catch (Throwable e) {
			//e.printStackTrace();
			return buildPathElement2(path);
		}
		
		return ret.getPathElement();
		
		/*
		String pathes[] = path.split("\\.");
		IPathElement current = null;
		
		for(int i = 0; i < pathes.length; i++){
			String p = pathes[i];
			IPathElement newElement = getOnePathElement(p);
			
			if(newElement instanceof ArrayPathElement){
				IPathElement pe = newElement.getParent();
				
				pe.setParent(current);
				current.setChild(pe);
			}else{
				newElement.setParent(current);
				if(current != null){
					current.setChild(newElement);
				}
			}
			
			current = newElement;
		}
		
		while(current.getParent() != null){
			current = current.getParent();
		}
		
		return current;*/
	}
	
	public static IPathElement buildPathElement2(String path)
	{
		String pathes[] = path.split("\\.");
		IPathElement current = null;
		
		for(int i = 0; i < pathes.length; i++){
			String p = pathes[i];
			
			IPathElement newElement = getOnePathElement(p);
			
			if(newElement instanceof ArrayPathElement){
				IPathElement pe = newElement.getParent();
				
				pe.setParent(current);
				current.setChild(pe);
			}else{
				newElement.setParent(current);
				if(current != null){
					current.setChild(newElement);
				}
			}
			
			current = newElement;
		}
		
		while(current.getParent() != null){
			current = current.getParent();
		}
		
		return current;
	}
	
	
	/**
	 * returns leaf
	 * @param path
	 * @return
	 */
	public static IPathElement getOnePathElement(String path)
	{
		int idx = path.indexOf("[");
		if(idx < 0){
			return new PathElement(path);
		}
		
		// xxxx[1]
		String body = path.substring(0, idx);
		String number = path.substring(idx + 1, path.length() -  1);
		
		PathElement element = new PathElement(body);
		ArrayPathElement array = new ArrayPathElement(number);
		
		element.setChild(array);
		array.setParent(element);
		
		return array;
	}
}
