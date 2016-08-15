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
package org.alinous.components.tree;

import java.util.Iterator;

import org.alinous.exec.SessionController;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class NodeTreeSessionManager
{
	public static final String BASE_SESSION_PATH = "SESSION.NODETREE";
	public static final String OPENED = "OPENED";
	public static final String CURRENT = "CURRENT";
	
	private VariableRepository valRepo;
	private PostContext context;
	
	public NodeTreeSessionManager(PostContext context, VariableRepository valRepo)
	{
		this.valRepo = valRepo;
		this.context = context;
	}
	
	
	public void setCurrent(NodeConfig config, String nodeId) throws ExecutionException, RedirectRequestException
	{
		String treeName = config.getId();
		IPathElement variablePath = PathElementFactory.buildPathElement(BASE_SESSION_PATH + "." + treeName + "." + CURRENT);
		
		ScriptDomVariable newValuable = new ScriptDomVariable(CURRENT);
		newValuable.setValue(nodeId);
		newValuable.setValueType(IScriptVariable.TYPE_NUMBER);
		
		this.valRepo.putValue(variablePath, newValuable, this.context);
	}
	
	public void clearCurrent(NodeConfig config) throws ExecutionException, RedirectRequestException
	{
		String treeName = config.getId();
		IPathElement variablePath = PathElementFactory.buildPathElement(BASE_SESSION_PATH + "." + treeName + "." + CURRENT);
		
		this.valRepo.release(variablePath, this.context);
	}
	
	public void closeAll(NodeConfig config) throws ExecutionException, RedirectRequestException
	{
		String treeName = config.getId();
		IPathElement variablePath = PathElementFactory.buildPathElement(BASE_SESSION_PATH + "." + treeName + "." + OPENED);
		
		this.valRepo.release(variablePath, this.context);
	}
	
	public boolean isCurrent(NodeConfig config, String nodeId) throws ExecutionException, RedirectRequestException
	{
		String treeName = config.getId();
		IPathElement variablePath = PathElementFactory.buildPathElement(BASE_SESSION_PATH + "." + treeName + "." + CURRENT);
		
		IScriptVariable val = this.valRepo.getVariable(variablePath, this.context);
		
		if(val == null || !(val instanceof ScriptDomVariable)){
			return false;
		}
		
		ScriptDomVariable domVal = (ScriptDomVariable)val;
		if(domVal.getValue() != null && domVal.getValue().equals(nodeId)){
			return true;
		}
		
		return false;
	}
	
	public void open(NodeConfig config, String nodeId) throws ExecutionException, RedirectRequestException
	{
		if(isOpened(config, Integer.parseInt(nodeId))){
			return;
		}
		
		ScriptArray array = getBaseArray(config.getId());
		
		if(array == null){
			array = new ScriptArray();
			IPathElement path = getPathElement(config.getId());
			
			this.valRepo.putValue(path, array, this.context);
		}
		
		ScriptDomVariable newValuable = new ScriptDomVariable(OPENED);
		newValuable.setValue(nodeId);
		newValuable.setValueType(ScriptDomVariable.TYPE_NUMBER);
		array.add(newValuable);
		
	}
	
	public void close(NodeConfig config, String nodeId) throws ExecutionException, RedirectRequestException
	{
		// if current
		if(isCurrent(config, nodeId)){
			// current
			clearCurrent(config);
		}
		
		// close
		ScriptArray array = getBaseArray(config.getId());
		
		if(array == null){
			return;
		}
		
		ScriptDomVariable val2del = findNodeElement(array, nodeId);
		if(val2del != null){
			array.remove(val2del);
		}
	}
	
	private ScriptDomVariable findNodeElement(ScriptArray array, String nodeId)
	{
		Iterator<IScriptVariable> it = array.iterator();
		while(it.hasNext()){
			IScriptVariable val = it.next();
			
			if(!(val instanceof ScriptDomVariable)){
				continue;
			}
			
			if(((ScriptDomVariable)val).getValue() != null &&
					((ScriptDomVariable)val).getValue().equals(nodeId)){
				return (ScriptDomVariable)val;
			}
			
		}
		
		return null;
	}
	
	
	public void syncSession() throws AlinousException
	{
		SessionController sessionCtrl = this.context.getUnit().getSessionController();
		sessionCtrl.storeSession(this.context, this.valRepo);
	}
	
	public boolean isOpened(NodeConfig config, int nodeId) throws ExecutionException, RedirectRequestException
	{
		//if(isCurrent(config, Integer.toString(nodeId))){
		//	return true;
		//}
		
		ScriptArray opened = getBaseArray(config.getId());
		if(opened == null){
			return false;
		}
		
		// check array
		ScriptDomVariable dom = findNodeElement(opened, Integer.toString(nodeId));
		if(dom == null){
			return false;
		}
		
		return true;
	}
	
	private ScriptArray getBaseArray(String treeName) throws ExecutionException, RedirectRequestException
	{
		IPathElement variablePath = getPathElement(treeName);
		IScriptVariable val = valRepo.getVariable(variablePath, this.context);
		
		if(val instanceof ScriptArray){
			return (ScriptArray)val;
		}
		
		return null;
	}
	
	private IPathElement getPathElement(String treeName)
	{
		return PathElementFactory.buildPathElement(BASE_SESSION_PATH + "." + treeName + "." + OPENED);
	}

	public PostContext getContext()
	{
		return context;
	}
	
	
}
