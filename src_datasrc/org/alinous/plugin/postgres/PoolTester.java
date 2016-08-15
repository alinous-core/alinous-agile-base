package org.alinous.plugin.postgres;

import java.io.File;

import org.alinous.AlinousDebug;
import org.alinous.AlinousUtils;
import org.alinous.expections.AlinousException;


public class PoolTester {
	public static PoolTester instance = null;
	
	private PoolTester(){}
	
	private static String alinousHome;
	private static String dir;
	private static String dir_parallel;
	
	
	public static PoolTester getInstance()
	{
		synchronized (PoolTester.class) {
			if(instance == null){
				instance = new PoolTester();
				
				dir = alinousHome + "/log/con";
				File f = new File(dir);
				f.mkdirs();
				
				dir_parallel = alinousHome + "/log/con_parallel";
				f = new File(dir_parallel);
				f.mkdirs();
			}
		}
		
		return instance;
	}
	
	public static void init(String ah)
	{
		alinousHome = ah;
	}
	
	public void notifyOpened(Object con)
	{
		String openFile = "/" + con.toString() + ".txt";
		String trace = makeStackSrace();		
		
		AlinousDebug.debugOutFile(trace, dir, openFile);
		
		// parallel
		File f = new File(AlinousUtils.getAbsoluteNotOSPath(dir_parallel, openFile));
		if(f.exists()){
			throw new RuntimeException("File already exists = running : " + f.getAbsolutePath());
		}
		
		AlinousDebug.debugOutFile(trace, dir_parallel, openFile);
	}
	
	public void notifyClosed(Object con)
	{
		if(con == null){
			AlinousException e = new AlinousException();
			try{
				throw e;
			}catch(AlinousException e1){
				e1.printStackTrace();
			}
			
			
			return;
		}
		String trace = makeStackSrace();
		
		String openFile = "/" + con.toString() + "_closed.txt";
		AlinousDebug.debugOutFile(trace, dir, openFile);
		
		// pararell
		openFile = "/" + con.toString() + ".txt";
		File f = new File(AlinousUtils.getAbsoluteNotOSPath(dir_parallel, openFile));
		if(!f.exists()){
			throw new RuntimeException("File not exists logging is wrong = running : " + f.getAbsolutePath());
		}
		
		f.delete();
	}
	
	private String makeStackSrace()
	{
		StringBuffer buff = new StringBuffer();
		StackTraceElement elements[] = Thread.currentThread().getStackTrace();
		for (int i = 0; i < elements.length; i++) {
			buff.append(elements[i].getClassName());
			buff.append(".");
			buff.append(elements[i].getMethodName());
			buff.append(" line at ");
			buff.append(elements[i].getLineNumber());
			buff.append("\n");
		}
		
		return buff.toString();
	}
}
