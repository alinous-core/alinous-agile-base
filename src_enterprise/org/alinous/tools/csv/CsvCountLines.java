package org.alinous.tools.csv;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Stack;

import org.alinous.AlinousUtils;
import org.alinous.cloud.file.AlinousFile;
import org.alinous.cloud.file.AlinousFileInputStream;
import org.alinous.csv.CsvException;
import org.alinous.csv.CsvReader;
import org.alinous.csv.CsvRecord;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.functions.system.AbstractSystemFunction;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class CsvCountLines extends AbstractSystemFunction {
	public static final String QUALIFIED_NAME = "CSV.COUNTLINES";
	public static final String FILE_PATH = "filePath";
	public static final String ENCODING = "encoding";
	
	public CsvCountLines()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", FILE_PATH);
		this.argmentsDeclare.addArgument(arg);
		arg = new ArgumentDeclare("$", ENCODING);
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
		
		IPathElement ipath = PathElementFactory.buildPathElement(FILE_PATH);
		IScriptVariable filePathVariable = newValRepo.getVariable(ipath, context);
		ipath = PathElementFactory.buildPathElement(ENCODING);
		IScriptVariable encodingVariable = newValRepo.getVariable(ipath, context);
		
		if(!(filePathVariable instanceof ScriptDomVariable) ||
				!(encodingVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String filePath = ((ScriptDomVariable)filePathVariable).getValue();
		String encode = ((ScriptDomVariable)encodingVariable).getValue();
		
		String absPath = AlinousUtils.getAbsolutePath(context.getCore().getHome(), filePath);
		AlinousFile file = new AlinousFile(absPath);
		AlinousFileInputStream stream = null;
		
		int count = 0;
		try {
			stream = new AlinousFileInputStream(file);
			
			CsvReader reader = new CsvReader(stream, encode);
			
			CsvRecord rec = reader.readRecord();
			while(!rec.isEmpty()){
				count++;
				
				rec = reader.readRecord();
			}
			
		} catch (FileNotFoundException e) {
			context.getCore().getLogger().reportError(e);
			e.printStackTrace();
			return null;
		} catch (CsvException e) {
			context.getCore().getLogger().reportError(e);
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			context.getCore().getLogger().reportError(e);
			e.printStackTrace();
			return null;
		}

		ScriptDomVariable dom = new ScriptDomVariable("value");
		dom.setValueType(IScriptVariable.TYPE_NUMBER);
		dom.setValue(Integer.toString(count));
		
		return dom;
	}
	
	@Override
	public String getName()
	{
		return QUALIFIED_NAME;
	}

	
	@Override
	public String codeAssistString() {
		return "Csv.countLines($filePath, $encoding)";
	}

	@Override
	public String descriptionString() {
		return "Count lines of the csv file.";
	}

}
