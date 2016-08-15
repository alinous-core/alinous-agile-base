package org.alinous.script.functions.system;

import java.lang.reflect.InvocationTargetException;

import org.alinous.AlinousCore;
import org.alinous.AlinousUtils;
import org.alinous.debug.DebugThread;
import org.alinous.exec.AccessExecutionUnit;
import org.alinous.exec.pages.AlinousExecutableModule;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.jdk.converter.Dom2Native;
import org.alinous.jdk.converter.Native2Dom;
import org.alinous.repository.AlinousModule;
import org.alinous.script.AlinousScript;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class JavaConnectorFunctionCallback {
	private AlinousCore core;
	
	public JavaConnectorFunctionCallback(AlinousCore core)
	{
		this.core = core;
	}
	
	public Object callback(String path, Object[] paramas, Class<?> returnClass) throws AlinousException, SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException
	{
		String moduleName = AlinousUtils.getModuleName(path);
		
		PostContext dummyContext = PostContext.createDummyContext(core);
		core.registerAlinousObject(dummyContext, moduleName);
		
		AlinousModule mod = core.getModuleRepository().getModule(moduleName);
		AlinousScript script = mod.getScript();
	
		//script.setFilePath(path);
		
		Native2Dom n2d = new Native2Dom(paramas);
		IScriptVariable val = n2d.convert();
		val.setName("args");
		
		PostContext context = null;
		try {
			AccessExecutionUnit newExecutableUnit = this.core.createAccessExecutionUnit(path);
			
			AlinousExecutableModule module = new AlinousExecutableModule(moduleName, null, script, 0);
			newExecutableUnit.setExecModule(module);
			
			VariableRepository valRepo = new VariableRepository();
			
			context = new PostContext(core, newExecutableUnit);
			valRepo.putValue(val, context);
			
			DebugThread thread = context.getCore().getAlinousDebugManager().getCurrentThread();
			if(thread == null){
				context.getCore().getAlinousDebugManager().startAlinousOperation(context);
			}
			
			script.execute(context, valRepo, true, false);
			
			
		} catch (ExecutionException e) {
			core.getLogger().reportError(e);
			e.printStackTrace();
		} catch (RedirectRequestException e) {
			core.getLogger().reportError(e);
			e.printStackTrace();
		}finally{			
			context.getCore().getAlinousDebugManager().endAlinousOperation(context);
			
			context.dispose();
		}
		
		IScriptVariable ival = context.getScriptReturnedValue();
		
		if(ival instanceof ScriptDomVariable){
			ScriptDomVariable dom = (ScriptDomVariable)ival;
			
			if(dom.getNumProperties() == 0){
				return dom.getValue();
			}
			
			Dom2Native d2n = new Dom2Native(dom, returnClass);
			return d2n.convert();
		}
		else if(val instanceof ScriptArray){
			ScriptArray array = (ScriptArray)ival;
			
			Dom2Native d2n = new Dom2Native(array, returnClass);
			return d2n.convertArray();
		}
		
		return null;
	}
}
