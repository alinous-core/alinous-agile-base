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
package org.alinous.script.functions.system;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.jdk.model.ArgumentModel;
import org.alinous.jdk.model.FunctionModel;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.VariableRepository;

public class JavaConnectorFunction extends AbstractSystemFunction
{
	private FunctionModel funcModel;
	private String name;
	
	private IScriptVariable retVal;
	
	public JavaConnectorFunction(String name, FunctionModel funcModel)
	{
		this.name = name;
		this.funcModel = funcModel;
		
		// register arguments
		Iterator<ArgumentModel> it = this.funcModel.getArguments().iterator();
		while(it.hasNext()){
			ArgumentModel argModel = it.next();
			
			if(argModel.getClazz().isArray()){
				ArgumentDeclare arg = new ArgumentDeclare("@", argName(argModel.getIndex()));
				this.argmentsDeclare.addArgument(arg);
			}
			else{
				ArgumentDeclare arg = new ArgumentDeclare("$", argName(argModel.getIndex()));
				this.argmentsDeclare.addArgument(arg);
			}
		}
	}
	
	private String argName(int index)
	{
		return "arg" + Integer.toString(index);
	}
	

	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		IScriptVariable retVal = null;
		
		ArrayList<IScriptVariable> runtimeArg = new ArrayList<IScriptVariable>();
		Iterator<ArgumentModel> it = this.funcModel.getArguments().iterator();
		while(it.hasNext()){
			ArgumentModel argModel = it.next();
			
			IScriptVariable argVal = getVariable(context, newValRepo, argName(argModel.getIndex()));
			runtimeArg.add(argVal);
		}
		
		// ClassLoader
		ClassLoader lastContextClassLoader = Thread.currentThread().getContextClassLoader();
		
		try {
			Thread.currentThread().setContextClassLoader(context.getCore().getJavaConnector().getLoader());
			
			retVal = this.funcModel.invoke(runtimeArg.toArray(new IScriptVariable[runtimeArg.size()]));
		} catch (SecurityException e) {
			throw new ExecutionException(e, "Failed to invoke Native method" + this.name + "."); // i18n
		} catch (IllegalArgumentException e) {
			throw new ExecutionException(e, "Failed to invoke Native method" + this.name + "."); // i18n
		} catch (InstantiationException e) {
			throw new ExecutionException(e, "Failed to invoke Native method" + this.name + "."); // i18n
		} catch (IllegalAccessException e) {
			throw new ExecutionException(e, "Failed to invoke Native method" + this.name + "."); // i18n
		} catch (NoSuchMethodException e) {
			throw new ExecutionException(e, "Failed to invoke Native method" + this.name + "."); // i18n
		} catch (InvocationTargetException e) {
			e.printStackTrace();			
			throw new ExecutionException(e, "Failed to invoke Native method" + this.name + "."); // i18n
		} catch (Throwable e) {
			throw new ExecutionException(e, "Failed to invoke Native method" + this.name + "."); // i18n
		}finally{
			Thread.currentThread().setContextClassLoader(lastContextClassLoader);
		}

		return retVal;
	}

	private IScriptVariable getVariable(PostContext context, VariableRepository valRepo, String name)
			throws ExecutionException, RedirectRequestException
	{
		IPathElement ipath = PathElementFactory.buildPathElement(name);
		IScriptVariable val = valRepo.getVariable(ipath, context);
		
		return val;
	}
	
	
	public String getName()
	{
		return this.name;
	}

	public IScriptVariable getResult()
	{
		return this.retVal;
	}

	@Override
	public String codeAssistString() {
		return null;
	}

	@Override
	public String descriptionString() {
		return null;
	}

}
