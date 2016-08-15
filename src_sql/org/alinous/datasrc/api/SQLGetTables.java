package org.alinous.datasrc.api;

import java.util.Iterator;
import java.util.Stack;

import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.DataField;
import org.alinous.datasrc.types.DataTable;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class SQLGetTables extends AbstractSystemFunction
{
	public static String QUALIFIED_NAME = "SQL.GETTABLES";
	public static String DATA_SRC = "dataSrc";
	
	public SQLGetTables()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", DATA_SRC);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException
	{
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		
		if(stmtStack.size() != this.argmentsDeclare.getSize()){
			throw new ExecutionException("Number of the function's arguments is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(DATA_SRC);
		IScriptVariable dataSrcVariable = newValRepo.getVariable(ipath, context);
		
		if(!(dataSrcVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String conStr = ((ScriptDomVariable)dataSrcVariable).getValue();
		
		DataSrcConnection con = null;
		

		DataTable[] tables = null;		
		try {
			con = context.getUnit().getConnectionManager().connect(conStr, context);
			
			tables = con.getDataTableList();
		} catch (DataSourceException e) {
			throw new ExecutionException(e, "Failed in operation at " + QUALIFIED_NAME); // i18n
		}finally{
			con.close();
		}
		
		ScriptArray ar = new ScriptArray();
		for (int i = 0; i < tables.length; i++) {
			ScriptDomVariable dom = toDom(tables[i]);
			
			ar.add(dom);
		}
		
		return ar;
	}
	public static ScriptDomVariable toDom(DataTable dt)
	{
		ScriptDomVariable dom = new ScriptDomVariable(dt.getName());
		
		ScriptArray columns = new ScriptArray("columns");
		Iterator<DataField> it = dt.iterator();
		while(it.hasNext()){
			DataField column = it.next();
			
			ScriptDomVariable columnDom = new ScriptDomVariable(column.getName());
			
			columnDom.put(makeStringDom("type", column.getType()));
			//columnDom.put(makeIntDom("length", column.getKeyLength()));
			columnDom.put(makeBooleanDom("notNull", column.isNotnull()));
			columnDom.put(makeBooleanDom("index", column.isIndex()));
			columnDom.put(makeBooleanDom("unique", column.isUnique()));
			columnDom.put(makeBooleanDom("primary", column.isPrimary()));
			
			
			
			columns.add(columnDom);
			
			columnDom.setValue(column.getName());
			columnDom.setValueType(IScriptVariable.TYPE_STRING);
		}
		dom.put(columns);
		
		ScriptArray keys = new ScriptArray("keys");
		Iterator<DataField> dit = dt.getPrimaryKeys().iterator();
		while(dit.hasNext()){
			DataField pkey = dit.next();
			
			ScriptDomVariable primaryDom = new ScriptDomVariable(pkey.getName());
			primaryDom.setValue(pkey.getName());
			primaryDom.setValueType(IScriptVariable.TYPE_STRING);
			
			keys.add(primaryDom);
		}
		dom.put(keys);
		
		dom.put(makeStringDom("name", dt.getName()));
		
		return dom;
	}
	
	private static ScriptDomVariable makeStringDom(String key, String value)
	{
		ScriptDomVariable dom = new ScriptDomVariable(key);
		dom.setValue(value);
		dom.setValueType(IScriptVariable.TYPE_STRING);
		
		return dom;
	}
	/*
	private ScriptDomVariable makeIntDom(String key, int value)
	{
		ScriptDomVariable dom = new ScriptDomVariable(key);
		dom.setValue(Integer.toString(value));
		dom.setValueType(IScriptVariable.TYPE_NUMBER);
		
		return dom;
	}*/
	
	private static ScriptDomVariable makeBooleanDom(String key, boolean value)
	{
		ScriptDomVariable dom = new ScriptDomVariable(key);
		dom.setValue(Boolean.toString(value));
		dom.setValueType(IScriptVariable.TYPE_BOOLEAN);
		
		return dom;
	}
	
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Sql.getTables($dataSrc)";
	}

	@Override
	public String descriptionString() {
		return "Returns tables and the metadata";
	}

}
