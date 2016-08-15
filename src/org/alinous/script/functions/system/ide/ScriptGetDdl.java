package org.alinous.script.functions.system.ide;

import java.util.Iterator;

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

public class ScriptGetDdl extends AbstractSystemFunction {
	public static String QUALIFIED_NAME = "SCRIPT.GETDDL";
	
	public static final String DDL_DIR = "DDL_DIR";
	
	public ScriptGetDdl()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", DDL_DIR);
		this.argmentsDeclare.addArgument(arg);
	}
	
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(DDL_DIR);
		IScriptVariable dirVariable = newValRepo.getVariable(ipath, context);
		
		String ddlDir = ((ScriptDomVariable)dirVariable).getValue();
		
		DdlScriptCrawler crawler = new DdlScriptCrawler(context.getCore().getHome());
		DdlHolder holder = crawler.analyzeAll(ddlDir);
		
		
		ScriptArray ddls = new ScriptArray("ret");
		
		Iterator<String> it = holder.getTables().keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			AlinousTableSchema table = holder.getTable(key);
			
			ScriptDomVariable domTbl = schemeToDom(table);
			ddls.add(domTbl);
		}
		
		return ddls;
	}
	
	private ScriptDomVariable schemeToDom(AlinousTableSchema table)
	{
		ScriptDomVariable variable = new ScriptDomVariable("table");
		
		ScriptDomVariable ddl = new ScriptDomVariable("ddl");
		ddl.setValueType(IScriptVariable.TYPE_STRING);
		ddl.setValue(table.toString());
		variable.put(ddl);
		
		ScriptDomVariable name = new ScriptDomVariable("name");
		name.setValueType(IScriptVariable.TYPE_STRING);
		name.setValue(table.getTableName());
		variable.put(name);
		
		ScriptDomVariable comment = new ScriptDomVariable("comment");
		comment.setValueType(IScriptVariable.TYPE_STRING);
		comment.setValue(table.getComment());
		variable.put(comment);
		
		ScriptArray columns = new ScriptArray("columns");
		variable.put(columns);
		
		Iterator<AlinousColumn> it = table.getColumns().iterator();
		while(it.hasNext()){
			AlinousColumn column = it.next();
			
			ScriptDomVariable columnDom = new ScriptDomVariable(column.getColumnName());
			ScriptDomVariable v = makeStringDom("name", column.getColumnName());
			columnDom.put(v);
			
			v = makeStringDom("type", column.getColumnType());
			columnDom.put(v);
			
			v = makeStringDom("length", column.getLength());
			columnDom.put(v);
			
			v = makeStringDom("comment", column.getComment());
			columnDom.put(v);
			
			columns.add(columnDom);
		}
		
		return variable;
	}
	
	private ScriptDomVariable makeStringDom(String name, String value)
	{
		ScriptDomVariable dom = new ScriptDomVariable(name);
		dom.setValue(value);
		dom.setValueType(IScriptVariable.TYPE_STRING);
		
		return dom;
	}
	
	
	@Override
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Script.getDdl($ddlDir)";
	}

	@Override
	public String descriptionString() {
		return "Gets DDL from the $ddlDir, and returns array variable.";
	}
	
}
