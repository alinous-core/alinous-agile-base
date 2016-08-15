package org.alinous;

import java.util.Iterator;
import java.util.Stack;

public class PathUtils
{
	
	public static void main(String[] args)
	{
		String path = getAbsPath("/test/", "../css/test.css");
		
		System.out.println(path);
	}
	
	
	public static String getAbsPath(String base, String path)
	{
		if(path.startsWith("/")){
			return path;
		}
		
		// list
		Stack<String> baseList = toList(base);
		Stack<String> ref = toList(path);
		
		if(!base.endsWith("/")){
			baseList.pop();
		}
		
		
		Iterator<String> it = ref.iterator();
		while(it.hasNext()){
			String pa = it.next();
			if(pa.equals("..")){
				baseList.pop();
			}
			else if(pa.equals(".")){
				continue;
			}
			else{
				baseList.push(pa);
			}
			
		}
		
		StringBuffer buffer = new StringBuffer();
		
		it = baseList.iterator();
		while(it.hasNext()){
			String pa = it.next();
			
			buffer.append("/");
			buffer.append(pa);
		}
		
		return buffer.toString();
	}
	
	private static Stack<String> toList(String path)
	{
		String elements[] = path.split("/");
		
		Stack<String> baseList = new Stack<String>();
		
		for(int i = 0; i < elements.length; i++){
			if(!elements[i].equals("")){
				baseList.add(elements[i]);
			}
		}
		
		return baseList;
	}
}
