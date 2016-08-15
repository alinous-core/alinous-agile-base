package org.alinous.script.functions.system.ide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import org.alinous.AlinousUtils;
import org.alinous.parser.script.AlinousScriptParser;
import org.alinous.parser.script.ParseException;
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptSentence;
import org.alinous.script.basic.AbstractScriptBlock;
import org.alinous.script.basic.Comment;
import org.alinous.script.functions.FuncDeclarations;
import org.alinous.script.functions.FunctionDeclaration;
import org.alinous.script.sql.ddl.ColumnTypeDescriptor;
import org.alinous.script.sql.ddl.CreateIndexSentence;
import org.alinous.script.sql.ddl.CreateTableSentence;
import org.alinous.script.sql.ddl.DdlColumnDescriptor;
import org.alinous.script.sql.ddl.PrimaryKeys;
import org.alinous.script.sql.statement.ColumnIdentifier;
import org.alinous.script.sql.statement.ISQLStatement;

public class DdlScriptCrawler {
	private String alinousHome;
	
	public DdlScriptCrawler(String alinousHome)
	{
		this.alinousHome = alinousHome;
	}
	
	public DdlHolder analyzeAll(String path)
	{
		DdlHolder.getInstance(alinousHome).reset();
		
		String abspath = AlinousUtils.getAbsoluteNotOSPath(this.alinousHome, path);
		
		File current = new File(abspath);
		return analyze(current);
	}
	
	public DdlHolder analyze(File current)
	{
		DdlHolder holder = DdlHolder.getInstance(alinousHome);
		
		if(current.isDirectory()){
			File[] files = current.listFiles();
			
			for (File file : files) {
				analyze(file);
			}
			return holder;
		}
		
		if(!current.getName().endsWith(".alns")){
			return holder;
		}
		
		AlinousScript script  = compileDocument(current);
		if(script == null){
			return DdlHolder.getInstance(alinousHome);
		}
		
		String src = readAllText(current.getAbsolutePath(), "utf-8");
		CommentInspector comInsp = new CommentInspector(src);
		List<Comment> comList = comInsp.parse();
		
		List<IScriptSentence> ddlandsentences = detectCreateTable(script);
		
		
		
		matchComment(comList, ddlandsentences, holder);
		
		makeIndexes(holder, ddlandsentences);
		
		return holder;
	}
	
	private void makeIndexes(DdlHolder holder, List<IScriptSentence> ddlandsentences)
	{
		Iterator<IScriptSentence> it = ddlandsentences.iterator();
		while(it.hasNext()){
			IScriptSentence sentence = it.next();
			
			if(sentence instanceof CreateIndexSentence){
				CreateIndexSentence createIndex = (CreateIndexSentence)sentence;
				
				String tableName = createIndex.getTable().getTableName();
				AlinousTableSchema table = holder.getTable(tableName);
				
				if(table == null){
					continue;
				}
				
				List<ColumnIdentifier> colIds = createIndex.getColumns();
				Iterator<ColumnIdentifier> itCols = colIds.iterator();
				while(itCols.hasNext()){
					ColumnIdentifier columnIdentifier = itCols.next();
					
					String columnName = columnIdentifier.getColumnName();
					AlinousColumn column = table.getColumn(columnName);
					
					if(column == null){
						continue;
					}
					
					column.setIndexed(true);
				}
				
			}
			
		}
	}
	
	private void handleCreateSentenceColumnsWithoutComment(Stack<Comment> stack, CreateTableSentence createTable, AlinousTableSchema tableSchema)
	{
		List<DdlColumnDescriptor> columnList = createTable.getColumnsList();
		Iterator<DdlColumnDescriptor> it = columnList.iterator();
		while(it.hasNext()){
			DdlColumnDescriptor column = it.next();
			
			AlinousColumn alColumn = new AlinousColumn();
			alColumn.setColumnName(column.getName());
			
			ColumnTypeDescriptor typeDesc = column.getTypeDescriptor();
			if(typeDesc != null){
				alColumn.setColumnType(typeDesc.getTypeName());
				
				ISQLStatement lengthStmt = typeDesc.getLength();
				if(lengthStmt != null){
					try {
						String length = lengthStmt.extract(null, null, null, null, null);
						alColumn.setLength(length);
					} catch (Throwable ignore) {
						ignore.printStackTrace(); // never come here
					}
					
				}
				
				ISQLStatement defaultStmt = column.getDefaultValue();
				if(defaultStmt != null){
					try {
						String defaultValue = defaultStmt.extract(null, null, null, null, null);
						alColumn.setDefaultValue(defaultValue);
					} catch (Throwable ignore) {
						ignore.printStackTrace();
					}
				}
				
			}
			
			tableSchema.addColumn(alColumn);
		}
	}
	
	/**
	 * Add comment to detected create table sentence and function declare and so on
	 * @param comList
	 * @param ddlandsentences
	 * @param holder
	 */
	private void matchComment(List<Comment> comList, List<IScriptSentence> ddlandsentences, DdlHolder holder)
	{
		Stack<Comment> stack = new Stack<Comment>();
		
		ListIterator<Comment> lit = comList.listIterator(comList.size());
		while(lit.hasPrevious()){
			Comment com = lit.previous();
			
			stack.push(com);
		}
		
		StringBuffer buff = new StringBuffer();
		Iterator<IScriptSentence> fit = ddlandsentences.iterator();
		while(fit.hasNext() && !stack.isEmpty()){
			IScriptSentence sentence = fit.next();
			buff.setLength(0);
			String commentType = null;
			
			int firstLine = -1;
			while(stack.peek().getLine() <= sentence.getLine() && !stack.isEmpty()){
				if(firstLine == -1){
					firstLine = stack.peek().getLine();
					commentType = stack.peek().getType();
				}
				
				buff.append(stack.peek().getCommentInner());
				
				stack.pop();
				if(stack.isEmpty()){
					break;
				}
			}
			
			if(sentence instanceof CreateTableSentence){
				// create new object
				AlinousTableSchema tableSchema = new AlinousTableSchema();
				CreateTableSentence createTable = (CreateTableSentence) sentence;
				
				String tableName = createTable.getTable().getTableName();
				tableSchema.setTableName(tableName);
				tableSchema.setComment(buff.toString(), commentType);
				
				
				// handle columns
				handleCreateSentenseComumns(stack, (CreateTableSentence)sentence, tableSchema);
				
				// primary keys
				PrimaryKeys keys = createTable.getKeys();
				if(keys != null){
					Iterator<String> keyit = keys.getKey().iterator();
					while(keyit.hasNext()){
						String key = keyit.next();
						
						tableSchema.addPrimaryKey(key);
					}
				}
				
				
				holder.addTableSchema(tableSchema);
			}
			
			if(stack.isEmpty()){
				break;
			}
		}
		
		// after breaked with stack.isEmpty()
		while(fit.hasNext()){
			IScriptSentence sentence = fit.next();
			if(sentence instanceof CreateTableSentence){
				// create new object
				AlinousTableSchema tableSchema = new AlinousTableSchema();
				CreateTableSentence createTable = (CreateTableSentence) sentence;
				
				String tableName = createTable.getTable().getTableName();
				tableSchema.setTableName(tableName);
				
				// handle columns
				handleCreateSentenceColumnsWithoutComment(stack, (CreateTableSentence)sentence, tableSchema);
				
				// primary keys
				PrimaryKeys keys = createTable.getKeys();
				if(keys != null){
					Iterator<String> keyit = keys.getKey().iterator();
					while(keyit.hasNext()){
						String key = keyit.next();
						
						tableSchema.addPrimaryKey(key);
					}
				}
				
				holder.addTableSchema(tableSchema);
				
			}
		}
		
	}
	
	private void handleCreateSentenseComumns(Stack<Comment> stack, CreateTableSentence createTable, AlinousTableSchema tableSchema)
	{
		if(stack.isEmpty()){
			handleCreateSentenceColumnsWithoutComment(stack, createTable, tableSchema);
			return;
		}
		
		StringBuffer buff = new StringBuffer();
		String aliasComment = null;
		String foreignKey = null;
		
		List<DdlColumnDescriptor> columnList = createTable.getColumnsList();
		Iterator<DdlColumnDescriptor> it = columnList.iterator();
		while(it.hasNext() && !stack.isEmpty()){
			DdlColumnDescriptor column = it.next();
			buff.setLength(0);
			aliasComment = null;
			foreignKey = null;
			
			int firstLine = -1;
			while(stack.peek().getLine() <= column.getLine() && !stack.isEmpty()){
				if(firstLine == -1){
					firstLine = stack.peek().getLine();
				}
				
				if(stack.peek().getLine() == column.getLine()){
					aliasComment = stack.peek().getCommentInner();
					
					
				}
				else{
					buff.append(stack.peek().getCommentInner());
					
					String fk = stack.peek().getFkAnnotation();
					if(fk != null){
						foreignKey = fk;
					}
				}
				
				stack.pop();
				if(stack.isEmpty()){
					break;
				}
			}
			
			AlinousColumn alColumn = new AlinousColumn();
			alColumn.setColumnName(column.getName());
			alColumn.setComment(buff.toString());
			alColumn.setAliasComment(aliasComment);
			alColumn.setForeignKey(foreignKey);
			
			// type and basic information of this column
			ColumnTypeDescriptor typeDesc = column.getTypeDescriptor();
			if(typeDesc != null){
				alColumn.setColumnType(typeDesc.getTypeName());
				alColumn.setNotnull(column.isNotnull());				
				
				// length
				ISQLStatement lengthStmt = typeDesc.getLength();
				if(lengthStmt != null){
					try {
						String length = lengthStmt.extract(null, null, null, null, null);
						alColumn.setLength(length);
					} catch (Throwable ignore) {
						ignore.printStackTrace(); // never come here
					}
					
				}
				
				// default
				ISQLStatement defaultStmt = column.getDefaultValue();
				if(defaultStmt != null){
					try {
						String defaultValue = defaultStmt.extract(null, null, null, null, null);
						alColumn.setDefaultValue(defaultValue);
					} catch (Throwable ignore) {
						ignore.printStackTrace();
					}
				}
	
			}
			
			tableSchema.addColumn(alColumn);
		}
		
		// without comment
		while(it.hasNext()){
			DdlColumnDescriptor column = it.next();
			
			AlinousColumn alColumn = new AlinousColumn();
			alColumn.setColumnName(column.getName());
			
			
			// type and basic information of this column
			ColumnTypeDescriptor typeDesc = column.getTypeDescriptor();
			if(typeDesc != null){
				alColumn.setColumnType(typeDesc.getTypeName());
				alColumn.setNotnull(column.isNotnull());
				
				ISQLStatement lengthStmt = typeDesc.getLength();
				if(lengthStmt != null){
					try {
						String length = lengthStmt.extract(null, null, null, null, null);
						alColumn.setLength(length);
					} catch (Throwable ignore) {
						ignore.printStackTrace(); // never come here
					}
					
				}
				
				ISQLStatement defaultStmt = column.getDefaultValue();
				if(defaultStmt != null){
					try {
						String defaultValue = defaultStmt.extract(null, null, null, null, null);
						alColumn.setDefaultValue(defaultValue);
					} catch (Throwable ignore) {
						ignore.printStackTrace();
					}
				}
				
			}
			
			tableSchema.addColumn(alColumn);
		}
	}
	
	
	private List<IScriptSentence> detectCreateTable(AlinousScript script)
	{
		List<IScriptSentence> ddlandsentences = new ArrayList<IScriptSentence>();
		List<IScriptSentence> list = script.getSentences();
		Iterator<IScriptSentence> it = list.iterator();
		while(it.hasNext()){
			IScriptSentence sentence = it.next(); 
			
			if(sentence instanceof AbstractScriptBlock){
				detectInBlock(ddlandsentences, (AbstractScriptBlock) sentence);
			}
			
			ddlandsentences.add(sentence);
		}
		
		// function declare
		FuncDeclarations declares = script.getFuncDeclarations();
		if(declares == null){
			return ddlandsentences;
		}
		
		Iterator<String> keyIt = declares.iterateFuncNames();
		while(keyIt.hasNext()){
			String key = keyIt.next();
			
			FunctionDeclaration funcDeclare = declares.findFunctionDeclare(key);
			detectInBlock(ddlandsentences, funcDeclare);
		}
		
		return ddlandsentences;
	}
	
	private void detectInBlock(List<IScriptSentence> ddlandsentences, AbstractScriptBlock block)
	{
		List<IScriptSentence> list = block.getSentences();
		Iterator<IScriptSentence> it = list.iterator();
		while(it.hasNext()){
			IScriptSentence sentence = it.next(); 
			
			if(sentence instanceof AbstractScriptBlock){
				detectInBlock(ddlandsentences, (AbstractScriptBlock) sentence);
			}
			
			ddlandsentences.add(sentence);
		}
	}
	
	private AlinousScript compileDocument(File file)
	{
		AlinousScriptParser parser = null;
		AlinousScript script = null;
		try {
			FileInputStream is = new FileInputStream(file);
			InputStreamReader reader = new InputStreamReader(is, "utf-8");
			
			
			parser = new AlinousScriptParser(reader);
			
			script = parser.parse();
		} catch (ParseException e) {
			//e.printStackTrace();
			script = parser.getLastScript();
		} catch (Throwable throwable){
			//throwable.printStackTrace();
			script = parser.getLastScript();
		}
		
		return script;
	}
	
	private String readAllText(String srcPath, String encode)
	{
		StringBuffer buff = new StringBuffer();
		
		File file = new File(srcPath);
		FileInputStream fStream = null;
		InputStreamReader reader = null;
		try {
			fStream = new FileInputStream(file);
			reader = new InputStreamReader(fStream, encode);
			
			char readBuffer[] = new char[1024];
			int n = 1;
			
			while(n > 0){
				n = reader.read(readBuffer, 0, readBuffer.length);
				
				if(n > 0){
					buff.append(readBuffer, 0, n);
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {e.printStackTrace();}
			}
			if(fStream != null){
				try {
					fStream.close();
				} catch (IOException e) {e.printStackTrace();}
			}
		}
		
		return buff.toString();	
	}
	
}
