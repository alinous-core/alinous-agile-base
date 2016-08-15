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
package org.alinous.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.DataField;
import org.alinous.datasrc.types.DataTable;
import org.alinous.datasrc.types.Record;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.lucene.extif.IDataStoreProvidor;
import org.alinous.script.sql.FromClause;
import org.alinous.script.sql.LimitOffsetClause;
import org.alinous.script.sql.OrderByClause;
import org.alinous.script.sql.SelectColumns;
import org.alinous.script.sql.WhereClause;
import org.alinous.script.sql.other.ColumnList;
import org.alinous.script.sql.other.SelectColumnElement;
import org.alinous.script.sql.other.TableIdentifier;
import org.alinous.script.sql.other.TablesList;
import org.alinous.script.sql.statement.ColumnIdentifier;
import org.alinous.script.sql.statement.Identifier;
import org.alinous.script.sql.statement.SQLFunctionCallArguments;
import org.alinous.script.sql.statement.SQLFunctionCallStatement;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;

public class LuceneInstance
{
	private LuceneInstanceConfig config;
	
	private Analyzer analyzer;
	private Directory directory;
	private IDataStoreProvidor dataStoreProvidor;
	
	private InstanceIndexWriter indexWriter;
	
	public LuceneInstance(LuceneInstanceConfig config, IDataStoreProvidor dataStoreProvidor)
	{
		this.config = config;
		this.dataStoreProvidor = dataStoreProvidor;
	}
	
	public String getName()
	{
		return this.config.getId();
	}
	
	public void initInstance() throws AlinousException, IOException
	{
		String setupperClass = this.config.getSetupper();
		
		ILuceneInstanceSetupper setUpper = null;
		try {
			setUpper
				= (ILuceneInstanceSetupper) Class.forName(setupperClass).newInstance();
		} catch (InstantiationException e) {
			throw new AlinousException(e, "Failed in loading class"); // i18n
		} catch (IllegalAccessException e) {
			throw new AlinousException(e, "Failed in loading class"); // i18n
		} catch (ClassNotFoundException e) {
			throw new AlinousException(e, "Failed in loading class"); // i18n
		}
		
		// setProperty
		setUpper.setProperties(this.config.getProperties());
		setUpper.setBaseDir(this.config.getBasePath());
		
		// setup
		this.analyzer = setUpper.getAnalyzer();
		this.directory = setUpper.getDirectory();
		
		// writer
		this.indexWriter = new InstanceIndexWriter(this.analyzer);
	}
	
	public void optimize() throws CorruptIndexException, IOException
	{
		this.indexWriter.optimize();
	}
	
	public Document query()
	{
		Document doc = new Document();
		
		
		
		return doc;
	}
	
	public void addRecord(Map<String, String> mapDoc)
	{
		
	}
	
	public void initDirectory() throws CorruptIndexException, LockObtainFailedException, IOException
	{
		this.indexWriter.beginWrite(this.directory, true);
		this.indexWriter.endWrite();
	}
	
	public void syncTable() throws DataSourceException, AlinousException
	{
		DataSrcConnection con = this.dataStoreProvidor.connect(this.config.getDataSrc(), null);
		
		try{
			DataTable tableMetaData = con.getDataTable(config.getTableName());
			
			int count = getCount(con);
			int offset = 0;
			int limit = 10;
			while(offset <= count){
				List<Record> records = selectRecords(con, tableMetaData, null, offset, limit);
				
				// add index
				try {
					writeRecords(records);
				} catch (CorruptIndexException e) {
					throw new AlinousException(e, "Failed in writing record"); // i18n
				} catch (LockObtainFailedException e) {
					throw new AlinousException(e, "Failed in writing record"); // i18n
				} catch (IOException e) {
					throw new AlinousException(e, "Failed in writing record"); // i18n
				}
				
				offset = offset + limit;
			}
			
		}finally{
			con.close();
		}
	}
	
	private synchronized void writeRecords(List<Record> records) throws CorruptIndexException, LockObtainFailedException, IOException
	{
		this.indexWriter.beginWrite(this.directory, false);
		
		try{
			Iterator<Record> it = records.iterator();
			while(it.hasNext()){
				Record rec = it.next();
				
				this.indexWriter.addRecord(rec);
			}
			
			this.indexWriter.optimize();
			
		}finally{
			this.indexWriter.endWrite();
		}
	}
	
	private List<Record> selectRecords(DataSrcConnection con, DataTable tableMetaData, WhereClause clause, int offset, int limit) throws ExecutionException, DataSourceException
	{
		// columns
		SelectColumns columns = new SelectColumns();
		
		// from
		FromClause from = new FromClause();
		TablesList tableList = new TablesList();
		TableIdentifier table = new TableIdentifier();
		table.setTableName(this.config.getTableName());
		tableList.addTable(table);
		from.setTableList(tableList);
		
		// limit offset
		LimitOffsetClause limitOffset = new LimitOffsetClause();
		limitOffset.setLimit(new Identifier(Integer.toString(limit)));
		limitOffset.setOffset(new Identifier(Integer.toString(offset)));
		
		// Order by
		OrderByClause orderBy = new OrderByClause();
		ColumnList orderColumns = new ColumnList();
		Iterator<DataField> it = tableMetaData.getPrimaryKeys().iterator();
		while(it.hasNext()){
			DataField fld = it.next();
			
			SelectColumnElement columnIdentifier = new SelectColumnElement();
			columnIdentifier.setColumnName(fld.getName());
			
			orderColumns.addColumns(columnIdentifier);
		}
		orderBy.setColumnList(orderColumns);
		
		List<Record> list = con.select(null, columns, from, null, null, orderBy, limitOffset, null, null, null, null);
		return list;
	}
	
	private int getCount(DataSrcConnection con) throws ExecutionException, DataSourceException
	{
		FromClause from = new FromClause();
		TablesList tableList = new TablesList();
		TableIdentifier table = new TableIdentifier();
		table.setTableName(this.config.getTableName());
		tableList.addTable(table);
		from.setTableList(tableList);
		
		SQLFunctionCallStatement funcCall = new SQLFunctionCallStatement();
		funcCall.setName("COUNT");
		
		SQLFunctionCallArguments args = new SQLFunctionCallArguments();
		ColumnIdentifier colId = new ColumnIdentifier();
		colId.setColumnName("*");
		args.addArgument(colId);
		
		funcCall.setArguments(args);
		
		SelectColumnElement colEl = new SelectColumnElement();
		colEl.setAsName(new Identifier("CNT"));
		colEl.setColumnName(funcCall);
		
		ColumnList colList = new ColumnList();
		colList.addColumns(colEl);
		
		SelectColumns columns = new SelectColumns();
		columns.setColumns(colList);
		
		List<Record> recordsList= con.select(null, columns, from, null, null, null, null, null, null, null, null);
		
		String cntStr = recordsList.get(0).getFieldValue("CNT");
		
		return Integer.parseInt(cntStr);
	}
	
	public List<Document> readIndex(String queryString, String key, int limit, int offset) throws CorruptIndexException, IOException, ParseException
	{
		List<Document> list = new ArrayList<Document>();
		
		IndexSearcher searcher = new IndexSearcher(this.directory);
		
		QueryParser queryParser = new QueryParser(key, this.analyzer);
		Query query = queryParser.parse(queryString);
		
		Hits hits = searcher.search(query);
		int limitCount = 0;
		for(int i = offset; i < hits.length(); i++){
			Document doc = hits.doc(i);
			list.add(doc);			
			
			limitCount++;
			if(limitCount >= limit){
				break;
			}
		}
		
		return list;
	}
}
