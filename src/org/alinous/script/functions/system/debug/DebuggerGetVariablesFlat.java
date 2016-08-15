package org.alinous.script.functions.system.debug;

import java.util.Iterator;

import org.alinous.debug.AlinousDebugManager;
import org.alinous.debug.DebugThread;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class DebuggerGetVariablesFlat extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "DEBUGGER.GETVARIABLESFLAT";
	public static final String THREAD_ID = "THREAD_ID";
	
	public DebuggerGetVariablesFlat()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", THREAD_ID);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(THREAD_ID);
		IScriptVariable threadIdVariable = newValRepo.getVariable(ipath, context);
		
		String strThreadId = ((ScriptDomVariable)threadIdVariable).getValue();
		
		long threadId = Long.parseLong(strThreadId);
		
		AlinousDebugManager manager = context.getCore().getAlinousDebugManager();
		DebugThread dthread = getDebugThread(manager, threadId);
		
		if(dthread == null){
			return null;
		}
		
		VariableRepository stackRepo = dthread.getTopStackFrame().getRepo();
		
		ScriptArray retArray = new ScriptArray("ret");
		
		int level = 0;
		Iterator<String> it = stackRepo.getKeyIterator();
		while(it.hasNext()){
			String key = it.next();
			
			IScriptVariable val = stackRepo.getValue(key);
			handleVariable(retArray, -1, val, "", level);
		}
		
		return retArray;
	}
	
	private void handleVariable(ScriptArray destArray, int arrayIndex, IScriptVariable val, String parentId, int level)
	{
		if(val instanceof ScriptArray){
			ScriptArray ar = (ScriptArray)val;
			
			ScriptDomVariable record = getArrayVariable(parentId, arrayIndex, level + 1, ar);
			destArray.add(record);
			
			String nextParentId = ((ScriptDomVariable)record.get("id")).getValue();
			
			int i = 0;
			Iterator<IScriptVariable> it = ar.iterator();
			while(it.hasNext()){
				IScriptVariable member = it.next();
				handleVariable(destArray, i, member, nextParentId, level + 1);
				i++;
			}
			return;
		}
		
		ScriptDomVariable dom = (ScriptDomVariable) val;
		
		ScriptDomVariable record = getVariable(parentId, arrayIndex, level, dom);
		destArray.add(record);
		
		Iterator<String> it = dom.getPropertiesIterator();
		while(it.hasNext()){
			String key = it.next();
			IScriptVariable child = dom.get(key);
			
			String nextParentId = ((ScriptDomVariable)record.get("id")).getValue();
			handleVariable(destArray, -1, child, nextParentId, level + 1);
		}
	}
	
	private ScriptDomVariable getVariable(String parentId, int arrayIndex, int level, ScriptDomVariable dom)
	{
		ScriptDomVariable val = new ScriptDomVariable("val");
		
		// id
		String domId = null;
		if(arrayIndex < 0){
			if(parentId == ""){
				domId = parentId + dom.getName();
			}else{
				domId = parentId + "." + dom.getName();
			}
		}else{
			domId = parentId + "<" + arrayIndex + ">";
		}
		addProperty("id", domId, IScriptVariable.TYPE_STRING, val);
		
		//parentId
		addProperty("parent", parentId, IScriptVariable.TYPE_STRING, val);
		//level
		addProperty("level", Integer.toString(level), IScriptVariable.TYPE_NUMBER, val);
		
		// value
		addProperty("value", dom.getValue(), IScriptVariable.TYPE_STRING, val);
		
		// type
		addProperty("type", "dom", IScriptVariable.TYPE_STRING, val);
		
		// disp
		addProperty("disp", dom.getName(), IScriptVariable.TYPE_STRING, val);
		
		return val;
	}
	
	private ScriptDomVariable getArrayVariable(String parentId, int arrayIndex, int level, ScriptArray dom)
	{
		ScriptDomVariable ar = new ScriptDomVariable("array");
		
		// id
		String domId = null;
		if(arrayIndex < 0){
			if(parentId == ""){
				domId = parentId + dom.getName();
			}else{
				domId = parentId + "." + dom.getName();
			}
		}else{
			domId = parentId + "<" + arrayIndex + ">";
		}
		addProperty("id", domId, IScriptVariable.TYPE_STRING, ar);
		
		//parentId
		addProperty("parent", parentId, IScriptVariable.TYPE_STRING, ar);
		//level
		addProperty("level", Integer.toString(level), IScriptVariable.TYPE_NUMBER, ar);
		
		// value
		addProperty("value", "", IScriptVariable.TYPE_STRING, ar);
		
		// type
		addProperty("type", "array", IScriptVariable.TYPE_STRING, ar);
		
		// disp
		addProperty("disp", dom.getName(), IScriptVariable.TYPE_STRING, ar);
		
		return ar;
	}
	
	
	private void addProperty(String prop, String value, String type, ScriptDomVariable val)
	{
		ScriptDomVariable newVal = new ScriptDomVariable(prop);
		newVal.setValue(value);
		newVal.setValueType(type);
		
		val.put(newVal);
	}
	
	private DebugThread getDebugThread(AlinousDebugManager manager, long threadId)
	{
		DebugThread[] threads = manager.getThreads();
		for (int i = 0; i < threads.length; i++) {
			if(threads[i].getThreadId() == threadId){
				return threads[i];
			}
		}
		
		return null;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Debugger.getVariablesFlat($threadId)";
	}

	@Override
	public String descriptionString() {
		return "Getting the variagbles in the stackframe of the thread.";
	}
}
