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
package org.alinous.lucene.api;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.lucene.LuceneConfig;
import org.alinous.lucene.LuceneManager;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

public class LuceneQuery extends AbstractSystemFunction
{
	public static final String QUALIFIED_NAME = "LUCENE.QUERY";
	public static final String INSTANCE_ID = "instanceId";
	public static final String KEY = "key";
	public static final String QUERY_STRING = "queryString";
	public static final String LIMIT = "limit";
	public static final String OFFSET = "offset";
	
	public LuceneQuery()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", INSTANCE_ID);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", KEY);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", QUERY_STRING);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", LIMIT);
		this.argmentsDeclare.addArgument(arg);
		
		arg = new ArgumentDeclare("$", OFFSET);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		ScriptArray result;
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		if(stmtStack.size() != this.argmentsDeclare.getSize()){
			throw new ExecutionException("Number of the function's arguments is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(INSTANCE_ID);
		IScriptVariable instIdVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(KEY);
		IScriptVariable keyVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(QUERY_STRING);
		IScriptVariable queryStringVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(LIMIT);
		IScriptVariable limitVariable = newValRepo.getVariable(ipath, context);
		
		ipath = PathElementFactory.buildPathElement(OFFSET);
		IScriptVariable offsetVariable = newValRepo.getVariable(ipath, context);
		
		if(!(instIdVariable instanceof ScriptDomVariable) ||
				!(keyVariable instanceof ScriptDomVariable) ||
				!(queryStringVariable instanceof ScriptDomVariable) ||
				!(limitVariable instanceof ScriptDomVariable) ||
				!(offsetVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String instanceId = ((ScriptDomVariable)instIdVariable).getValue();
		String key = ((ScriptDomVariable)keyVariable).getValue();
		String queryString = ((ScriptDomVariable)queryStringVariable).getValue();
		String strLimit = ((ScriptDomVariable)limitVariable).getValue();
		String strOffset = ((ScriptDomVariable)offsetVariable).getValue();
		
		LuceneManager luceneManager = new LuceneManager(context.getUnit().getConnectionManager());
		LuceneConfig luceneConfig = context.getCore().getConfig().getLuceneConfig();
		
		luceneManager.init(luceneConfig);
		
		int limit = 100;
		int offset = 0;
		
		try{
			limit = Integer.parseInt(strLimit);
			offset = Integer.parseInt(strOffset);
		}catch(NumberFormatException e){
			context.getCore().getLogger().reportError(e);
		}
		
		try {
			List<Document> resultList = luceneManager.readRecords(instanceId, queryString, key, limit, offset);
			result = setResult(resultList);
		} catch (AlinousException e) {
			throw new ExecutionException(e, "Failed in sync table."); // i18n
		}
		
		return result;
	}
	
	private ScriptArray setResult(List<Document> docList)
	{
		ScriptArray array = new ScriptArray();
		
		Iterator<Document> it = docList.iterator();
		while(it.hasNext()){
			Document doc = it.next();
			
			ScriptDomVariable val = toDomVariable(doc);
			array.add(val);
		}
		
		return array;
	}
	
	@SuppressWarnings("unchecked")
	private ScriptDomVariable toDomVariable(Document doc)
	{
		ScriptDomVariable val = new ScriptDomVariable("doc");
		
		Iterator<Field> it = doc.getFields().iterator();
		while(it.hasNext()){
			Field fld = it.next();
			
			ScriptDomVariable domVal = new ScriptDomVariable(fld.name());
			domVal.setValue(fld.stringValue());
			domVal.setValueType(IScriptVariable.TYPE_STRING);
			
			val.put(domVal);
		}

		
		return val;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
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
