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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.jdom.Element;

public class AlinousDebug {
	public static void println(String str)
	{
		System.out.println(str);
	}
	public static void print(String str)
	{
		System.out.print(str);
	}
	
	public static synchronized String trimLength(String str, int max)
	{
		if(str != null && str.length() > max){
			return str.substring(0, max);
		}
		
		return str;
	}
	
	public static synchronized void dumpStackTrace(AlinousCore core)
	{
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		for (int i = 0; i < stacks.length; i++) {
			StackTraceElement stackTraceElement = stacks[i];
			
			debugOut(core, stackTraceElement.getClassName()
					+ "." + stackTraceElement.getMethodName() + " line : " + stackTraceElement.getLineNumber());
			
		}
	}
	
	public static synchronized void debugOut(AlinousCore core, Object str)
	{
		if(str == null){
			System.out.println("null");
			System.out.flush();
			return;
		}
		System.out.println(str.toString());
		
		System.out.flush();
		
		if(core != null){
			core.getStdio().out(str.toString());
		}
	}
	
	public static synchronized void debugOutFile(Object str, String alinousHome, String fileName)
	{
		if(str == null){
			str = "null";
		}
		
		String path = AlinousUtils.getAbsoluteNotOSPath(alinousHome, fileName);
		
		File file = new File(path);

		FileWriter writer = null;
		
		try {
			writer = new FileWriter(file, true);
			writer.write(str.toString());
			writer.write("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(writer != null){
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
	}
	
	public static void printClientEventAccepted(String clazz)
	{
		System.out.println(clazz + " accepted.");
	}
	
	public static void printBreakpointHit(String file, int line)
	{
		System.out.println("BREAKPOINT : " + file + " at line" + line);
	}
	
	public static void dumpValues(VariableRepository repo, PostContext context) throws ExecutionException, RedirectRequestException
	{
		println("-------------------------- DUMP VARIABLES -------------------------------");
		
		Iterator<String> it = repo.getKeyIterator();
		while(it.hasNext()){
			String key = it.next();
			
			IPathElement path = PathElementFactory.buildPathElement(key);
			
			IScriptVariable variable = repo.getVariable(path, context);
			dump(variable, 0);
		}
		
		println("-------------------------------------------------------------------------");
	}
	
	private static void dump(IScriptVariable variable, int indent)
	{
		if(variable instanceof ScriptDomVariable){
			dumpHashValue((ScriptDomVariable)variable, indent);
		}else{
			dumpArray((ScriptArray)variable, indent);
		}		
	}
	
	private static void dumpHashValue(ScriptDomVariable variable, int indent)
	{
		String name = variable.getName();
		String value = variable.getValue();
		String type = variable.getType();
		
		indent(indent);
		
		// print value
		if(value != null){
			println(name + " = " + value + " [" + type + "]");
		}else{
			println(name + " [" + type + "]");
		}
		
		// print properties
		Iterator<String> it = variable.getPropertiesIterator();
		while(it.hasNext()){
			String prop = it.next();
			IScriptVariable val = variable.get(prop);
			
			indent(indent);
			println("property->" + prop);
			
			dump(val, indent + 1);
		}
	}
	
	private static void dumpArray(ScriptArray array, int indent)
	{
		String name = array.getName();
		String type = array.getType();
		
		indent(indent);
		println("Array @" + name +  " [" + type + "]");
		
		Iterator<IScriptVariable> it = array.iterator();
		while(it.hasNext()){
			IScriptVariable val = it.next();
			
			dump(val, indent + 1);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void dumpElement(AlinousCore core, Element element)
	{
		Iterator<Element> it = element.getChildren().iterator();
		while(it.hasNext()){
			Element el = it.next();
			
			debugOut(core, el.getName());
			
			dumpChildElement(core, el);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void dumpChildElement(AlinousCore core, Element element)
	{
		Iterator<Element> it = element.getChildren().iterator();
		while(it.hasNext()){
			Element el = it.next();
			
			debugOut(core, el.getName());
			
			dumpChildElement(core, el);
		}
	}
	
	private static void indent(int indent)
	{
		for(int i = 0; i < indent; i++){
			print("\t");
		}
	}
}
