package org.alinous.script.sql.ddl;

import java.util.ArrayList;
import java.util.List;

import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.TypeHelper;
import org.alinous.debug.StepInCandidates;
import org.alinous.exec.IExecutable;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.script.AlinousScript;
import org.alinous.script.ISQLSentence;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.sql.other.TableIdentifier;
import org.alinous.script.sql.statement.ColumnIdentifier;
import org.alinous.script.sql.statement.Identifier;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.alinous.test.coverage.LineCoverage;
import org.jdom.Element;

public class CreateIndexSentence implements ISQLSentence {
	private int line;
	private int linePosition;
	private String filePath;
	
	private Identifier indexName;
	
	private TableIdentifier table;
	private List<ColumnIdentifier> columns = new ArrayList<ColumnIdentifier>();
	
	private Identifier usingAlgo;
	
	@Override
	public String getFilePath()
	{
		return this.filePath;
	}

	@Override
	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}

	@Override
	public StepInCandidates getStepInCandidates()
	{
		return null;
	}

	@Override
	public IScriptVariable getReturnedVariable(PostContext context)
	{
		return null;
	}

	@Override
	public int getLine()
	{
		return this.line;
	}

	@Override
	public void setLine(int line)
	{
		this.line = line;
	}

	@Override
	public int getLinePosition()
	{
		return this.linePosition;
	}

	@Override
	public void setLinePosition(int linePosition)
	{
		this.linePosition = linePosition;
	}

	@Override
	public boolean execute(PostContext context, VariableRepository valRepo)
			throws ExecutionException
	{
		context.getCore().reporttExecuted(this);
		
		String dataSrc = context.getDataSrc();
		if(dataSrc == null){
			dataSrc = context.getCore().getConfig().getSystemRepositoryConfig().getDefaultSrc();
		}
		
		DataSrcConnection con = null;
		try {
			con = context.getUnit().getConnectionManager().connect(dataSrc, context);
		} catch (DataSourceException e) {
			throw new ExecutionException(e, e.getMessage());
		}
		
		
		try {
			con.createIndex(this.indexName, this.table, this.columns, this.usingAlgo);
		} catch (DataSourceException e) {
			e.printStackTrace();
			context.getCore().getLogger().reportError(e);
		}
		
		return true;
	}

	@Override
	public void exportIntoJDomElement(Element parent) throws AlinousException
	{
		Element selectElement = new Element(IExecutable.TAG_EXECUTABLE);
		
		selectElement.setAttribute(IExecutable.ATTR_CLASS, this.getClass().getName());
		
		parent.addContent(selectElement);
	}

	@Override
	public void importFromJDomElement(Element threadElement)
			throws AlinousException
	{
		
	}

	@Override
	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList)
	{
		
	}

	@Override
	public String extract(PostContext context, VariableRepository valRepo,
			AdjustWhere adjustWhere, AdjustSet adjustSet, TypeHelper helper)
			throws ExecutionException
	{
		return null;
	}

	@Override
	public boolean isReady(PostContext context, VariableRepository valRepo,
			AdjustWhere adjustWhere) throws ExecutionException
	{
		return true;
	}

	@Override
	public boolean isPrecompilable()
	{
		return false;
	}

	@Override
	public void setPrecompilable(boolean b)
	{
		
	}

	public Identifier getIndexName()
	{
		return indexName;
	}

	public void setIndexName(Identifier indexName)
	{
		this.indexName = indexName;
	}

	public TableIdentifier getTable()
	{
		return table;
	}

	public void setTable(TableIdentifier table)
	{
		this.table = table;
	}


	public Identifier getUsingAlgo()
	{
		return usingAlgo;
	}

	public void setUsingAlgo(Identifier usingAlgo)
	{
		this.usingAlgo = usingAlgo;
	}

	public List<ColumnIdentifier> getColumns()
	{
		return columns;
	}
	
	public void addColumn(ColumnIdentifier col)
	{
		this.columns.add(col);
	}

	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		
		
	}
	
	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		LineCoverage lineCoverage = new LineCoverage(this.line, this.linePosition, this);
		coverage.addLineCoverage(lineCoverage);
		
	}
}
