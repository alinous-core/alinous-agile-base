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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alinous.components.tree.model.NodeModel;
import org.alinous.datasrc.AlinousDataSourceManager;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.DataField;
import org.alinous.datasrc.types.DataTable;
import org.alinous.datasrc.types.Record;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.sql.FromClause;
import org.alinous.script.sql.OrderByClause;
import org.alinous.script.sql.SelectColumns;
import org.alinous.script.sql.WhereClause;
import org.alinous.script.sql.condition.AndExpression;
import org.alinous.script.sql.condition.TwoClauseExpression;
import org.alinous.script.sql.other.ColumnList;
import org.alinous.script.sql.other.SelectColumnElement;
import org.alinous.script.sql.other.TableIdentifier;
import org.alinous.script.sql.other.TablesList;
import org.alinous.script.sql.statement.ISQLStatement;
import org.alinous.script.sql.statement.Identifier;

public class NodeDbAdaptor
{
	private NodeConfig nodeConfig;
	
	public NodeDbAdaptor(NodeConfig node)
	{
		this.nodeConfig = node;
	}
	
	public void initDb(AlinousDataSourceManager dataSourceManager, PostContext context) throws DataSourceException
	{
		DataSrcConnection con = null;
		
		try {
			con = dataSourceManager.connect(this.nodeConfig.getDatastore(), context);
			
			DataTable dataTable = con.getDataTable(this.nodeConfig.getTableName());
			if(dataTable == null){
				createTable(con);
			}
			
			
		} finally{
			if(con != null){
				con.close();
				context.setCurrentDataSrcConnection(null);
			}
			
		}
	}
	
	public void createTable(DataSrcConnection con) throws DataSourceException
	{
		// main table
		DataTable dataTable = new DataTable(this.nodeConfig.getTableName());
		
		dataTable.addField(NodeModel.NODE_ID, DataField.TYPE_INTEGER, true);
		dataTable.addField(NodeModel.LEVEL, DataField.TYPE_INTEGER, true);
		dataTable.addField(NodeModel.PARENT_ID, DataField.TYPE_INTEGER, true);
		dataTable.addField(NodeModel.NUM_CHILDREN, DataField.TYPE_INTEGER, false);
		dataTable.addField(NodeModel.POS_IN_LEVEL, DataField.TYPE_INTEGER, false);
		dataTable.addField(NodeModel.TITLE, DataField.TYPE_TEXT_STRING, false);
		dataTable.addField(NodeModel.DOC_TYPE, DataField.TYPE_TEXT_STRING, false);
		dataTable.addField(NodeModel.DOC_REF, DataField.TYPE_STRING, false, 128);
		dataTable.addField(NodeModel.VISIBLE, DataField.TYPE_STRING, false, 8);
		dataTable.addField(NodeModel.CATEGORY, DataField.TYPE_STRING, false, 64);
		dataTable.setDefaultValue("CATEGORY", "", true);
		
		con.createTable(dataTable);
		
		// table
		dataTable = new DataTable(this.nodeConfig.getTableModelSerialName());
		dataTable.addField(NodeModel.MAX_NODE_ID, DataField.TYPE_INTEGER, true);
		
		con.createTable(dataTable);
		
		Record rec = new Record();
		rec.addFieldValue(NodeModel.MAX_NODE_ID, "1", IScriptVariable.TYPE_NUMBER);
		
		con.insert(rec, this.nodeConfig.getTableModelSerialName(), null, null);
		
	}
	
	public List<NodeModel> getRootNodes(DataSrcConnection con) throws ExecutionException, DataSourceException
	{
		SelectColumns columns = new SelectColumns();
		
		//	from
		FromClause from = new FromClause();
		TablesList tableList = new TablesList();
		TableIdentifier table = new TableIdentifier();
		table.setTableName(this.nodeConfig.getTableName());
		tableList.addTable(table);
		from.setTableList(tableList);
		
		// where
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(NodeModel.LEVEL, "0");
		
		 
		WhereClause where = createWhereClauseFromParamMap(queryParams);
		
		OrderByClause orderBy = new OrderByClause();
		ColumnList orderColumns = new ColumnList();
		SelectColumnElement columnIdentifier = new SelectColumnElement();
		columnIdentifier.setColumnName(NodeModel.POS_IN_LEVEL);
		orderColumns.addColumns(columnIdentifier);
		orderBy.setColumnList(orderColumns);
		
		List<Record> recordList = con.select(null, columns, from, where, null, orderBy, null, null, null, null, null);
		
		List<NodeModel> nodeList = new ArrayList<NodeModel>();
		
		Iterator<Record> it = recordList.iterator();
		while(it.hasNext()){
			Record rec = it.next();
			
			NodeModel model = new NodeModel(this.nodeConfig, rec);
			nodeList.add(model);
			
		}
		
		return nodeList;
	}

	private WhereClause createWhereClauseFromParamMap(Map<String, String> queryParams)
	{
		WhereClause where = new WhereClause();
		AndExpression andExp = new AndExpression();
		Iterator<String> it = queryParams.keySet().iterator();
		while(it.hasNext()){
			String field = it.next();
			String val = queryParams.get(field);
			
			Identifier id = new Identifier();
			id.setName(field);
			
			//SQLStringConst sqVal = new SQLStringConst();
			//sqVal.setStr(val);
			ISQLStatement sqVal = NodeModel.getConst(field, val);
			
			TwoClauseExpression eqExp = new TwoClauseExpression();
			eqExp.setOpe("=");
			eqExp.setLeft(id);
			eqExp.setRight(sqVal);
			
			andExp.addExpressions(eqExp);
		}
		
		where.setExpression(andExp);
		
		return where;
	}
}
