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

package org.alinous.components.tree.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alinous.components.tree.NodeConfig;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.Record;
import org.alinous.expections.ExecutionException;
import org.alinous.script.sql.FromClause;
import org.alinous.script.sql.OrderByClause;
import org.alinous.script.sql.SelectColumns;
import org.alinous.script.sql.SetClause;
import org.alinous.script.sql.WhereClause;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.sql.condition.AndExpression;
import org.alinous.script.sql.condition.TwoClauseExpression;
import org.alinous.script.sql.other.ColumnList;
import org.alinous.script.sql.other.SelectColumnElement;
import org.alinous.script.sql.other.SetPair;
import org.alinous.script.sql.other.TableIdentifier;
import org.alinous.script.sql.other.TablesList;
import org.alinous.script.sql.statement.ColumnIdentifier;
import org.alinous.script.sql.statement.ISQLStatement;
import org.alinous.script.sql.statement.Identifier;
import org.alinous.script.sql.statement.SQLFunctionCallArguments;
import org.alinous.script.sql.statement.SQLFunctionCallStatement;
import org.alinous.script.sql.statement.SQLNumericConst;

public class NodeModelUtils
{
	public static NodeModel getModelSingle(DataSrcConnection con, NodeConfig config, String nodeId)
			throws ExecutionException, DataSourceException
	{
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(NodeModel.NODE_ID, nodeId);
		
		List<Record> recList = selectRecord(config.getTableName(), queryParams, null, con);
		
		if(recList.isEmpty()){
			return null;
		}
		
		NodeModel model = new NodeModel(config, recList.get(0));
		
		return model;
	}
	
	public static List<NodeModel> getNodesByQuery(DataSrcConnection con, NodeConfig config,
			Map<String, String> queryParams, String orderColumn) throws ExecutionException, DataSourceException
	{
		List<Record> recList = selectRecord(config.getTableName(), queryParams, orderColumn, con);
		
		return record2Models(config, recList);
	}
	
	public static List<NodeModel> getNodesByQuery(DataSrcConnection con, NodeConfig config,
			Map<String, String> queryParams) throws ExecutionException, DataSourceException
	{
		return getNodesByQuery(con, config, queryParams, null);
	}
	
	public static NodeModel getParentModel(DataSrcConnection con, NodeConfig config, NodeModel model)
				throws ExecutionException, DataSourceException
	{
		if(model.getParentId() == 0){
			return null;
		}
		
		return getModelSingle(con, config, Integer.toString(model.getParentId()));
	}
	
	public static int getDbMaxRootPos(DataSrcConnection con, NodeConfig config) throws ExecutionException, DataSourceException
	{
		SelectColumns col = new SelectColumns();

		SQLFunctionCallArguments args = new SQLFunctionCallArguments();
		ColumnIdentifier colId = new ColumnIdentifier();
		colId.setColumnName(NodeModel.POS_IN_LEVEL);
		args.addArgument(colId);
		SQLFunctionCallStatement funcCall = new SQLFunctionCallStatement();
		funcCall.setName("MAX");
		funcCall.setArguments(args);
		
		SelectColumnElement colEl = new SelectColumnElement();
		colEl.setAsName(new Identifier("MX"));
		colEl.setColumnName(funcCall);

		
		ColumnList colList = new ColumnList();
		colList.addColumns(colEl);
		col.setColumns(colList);
		
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(NodeModel.PARENT_ID, "0");
		

		List<Record> recList = selectRecord(config.getTableName(), queryParams, col, null, con);
		
		String cnt = recList.get(0).getFieldValue("MX");
		
		if(cnt == null){
			return 0;
		}
		
		return Integer.parseInt(cnt);
	}
	
	
	public static List<NodeModel> getBrothers(DataSrcConnection con, NodeConfig config, NodeModel curModel) throws ExecutionException, DataSourceException
	{
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(NodeModel.PARENT_ID, Integer.toString(curModel.getParentId()));
		
		List<Record> recList = selectRecord(config.getTableName(), queryParams, NodeModel.POS_IN_LEVEL, con);
		
		return record2Models(config, recList);
	}
	
	public static List<NodeModel> getChildren(DataSrcConnection con, NodeConfig config, NodeModel curModel)
		throws ExecutionException, DataSourceException
	{
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(NodeModel.PARENT_ID, Integer.toString(curModel.getId()));
		
		List<Record> recList = selectRecord(config.getTableName(), queryParams, NodeModel.POS_IN_LEVEL, con);
		
		return record2Models(config, recList);
	}
	
	private static List<NodeModel> record2Models(NodeConfig config, List<Record> recList)
	{
		List<NodeModel> retList = new ArrayList<NodeModel>();
		
		Iterator<Record> it = recList.iterator();
		while(it.hasNext()){
			Record rec = it.next();
			NodeModel mod = new NodeModel(config, rec);
			
			retList.add(mod);
		}
		
		return retList;
	}
	
	
	public static void insertModel(DataSrcConnection con, NodeConfig config, NodeModel model) throws DataSourceException
	{
		con.insert(model.getRec(), config.getTableName(), null, null);
	}
	
	public static void updateModel(DataSrcConnection con, NodeConfig config, NodeModel model) throws ExecutionException, DataSourceException
	{
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(NodeModel.NODE_ID, Integer.toString(model.getId()));
		WhereClause where = createWhereClauseFromParamMap(queryParams);
		
		SetClause set = new SetClause();
		addSetParam(set, NodeModel.LEVEL, model.getLevel());
		addSetParam(set, NodeModel.NUM_CHILDREN, model.getNumChildren());
		addSetParam(set, NodeModel.PARENT_ID, model.getParentId());
		addSetParam(set, NodeModel.POS_IN_LEVEL, model.getPosInLevel());
		addSetParam(set, NodeModel.TITLE, model.getTitle());
		addSetParam(set, NodeModel.DOC_TYPE, model.getDocType());
		addSetParam(set, NodeModel.DOC_REF, model.getDocRef());
		addSetParam(set, NodeModel.VISIBLE, model.getVisible());
		addSetParam(set, NodeModel.CATEGORY, model.getCategory());
		
		con.update(config.getTableName(), set, where, null, null, null, null);
	}
	
	public static void deleteModel(DataSrcConnection con, NodeConfig config, NodeModel model) throws ExecutionException, DataSourceException
	{
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(NodeModel.NODE_ID, Integer.toString(model.getId()));
		WhereClause where = createWhereClauseFromParamMap(queryParams);
		
		con.delete("FROM " + config.getTableName(), where, null, null, null);
	}
	
	private static void addSetParam(SetClause set, String key, int value)
	{
		addSetParam(set, key, Integer.toString(value));
	}
	
	private static void addSetParam(SetClause set, String key, String value)
	{
		SetPair p = new SetPair();
		
		ColumnIdentifier col = new ColumnIdentifier();
		col.setColumnName(key);
		p.setColumn(col);
		
		if(value == null){
			return;
		}
		
		// values's type
		ISQLStatement val = NodeModel.getConst(key, value);
		
		p.setValue(val);
		
		set.addSet(p);
	}
	
	
	public static int getNextNodeId(DataSrcConnection con, NodeConfig config) throws ExecutionException, DataSourceException
	{
		Map<String, String> queryParams = new HashMap<String, String>();
		
		List<Record> recordList =selectRecord(config.getTableModelSerialName(), queryParams, null, con);
		if(recordList.isEmpty()){
			throw new ExecutionException("Serial table of treenode is broken"); // i18n
		}
		
		Record rec = recordList.get(0);
		String strMax = rec.getFieldValue(NodeModel.MAX_NODE_ID);
		int cur = Integer.parseInt(strMax);
		int next = cur + 1;
		
		SetClause set = new SetClause();
		SetPair p = new SetPair();
		ColumnIdentifier colId = new ColumnIdentifier();
		colId.setColumnName(NodeModel.MAX_NODE_ID);
		SQLNumericConst num = new SQLNumericConst();
		num.setNumber(Integer.toString(next));
		
		p.setColumn(colId);
		p.setValue(num);
		
		set.addSet(p);
		
		con.update(config.getTableModelSerialName(), set, null, null, null, null, null);
		
		return cur;
	}
	
	public static List<Record> selectRecord(String tableName, Map<String, String> queryParams,
			String orderByColumn, DataSrcConnection con)
			throws DataSourceException, ExecutionException
	{
		return selectRecord(tableName, queryParams, null, orderByColumn, con);
	}
	
	public static List<Record> selectRecord(String tableName, Map<String, String> queryParams,
			SelectColumns selectColumns, String orderByColumn, DataSrcConnection con)
			throws DataSourceException, ExecutionException
	{
		List<Record> recordList =null;
		con.setOutSql(false);
		
		// from
		FromClause from = new FromClause();
		TablesList tableList = new TablesList();
		TableIdentifier table = new TableIdentifier();
		table.setTableName(tableName);
		tableList.addTable(table);
		from.setTableList(tableList);
		
		// order by
		OrderByClause orderBy = new OrderByClause();
		
		if(orderByColumn != null){
			ColumnList orderColumns = new ColumnList();
			SelectColumnElement columnIdentifier = new SelectColumnElement();
			columnIdentifier.setColumnName(orderByColumn);
			orderColumns.addColumns(columnIdentifier);
			orderBy.setColumnList(orderColumns);
		}
		
		SelectColumns columns = null;
		if(selectColumns == null){
			columns = new SelectColumns();
		}else{
			columns = selectColumns;
		}
		
		WhereClause where = createWhereClauseFromParamMap(queryParams);
		AdjustWhere adjWhere = new AdjustWhere();
		adjWhere.setValue("TRUE");
		
		try{
			recordList = con.select(null, columns, from, where, null, orderBy, null, null, null, null, adjWhere);
		}catch(DataSourceException e){
			throw e;
		}catch(ExecutionException e){
			throw e;
		}
		
		return recordList;
	}
	
	private static WhereClause createWhereClauseFromParamMap(Map<String, String> queryParams)
	{
		WhereClause where = new WhereClause();
		AndExpression andExp = new AndExpression();
		Iterator<String> it = queryParams.keySet().iterator();
		while(it.hasNext()){
			String field = it.next();
			String val = queryParams.get(field);
			
			Identifier id = new Identifier();
			id.setName(field);
			
			// value type
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
	
	public static List<NodeModel> getRootModels(DataSrcConnection con, NodeConfig nodeConfig) throws ExecutionException, DataSourceException
	{
		SelectColumns columns = new SelectColumns();
		
		//	from
		FromClause from = new FromClause();
		TablesList tableList = new TablesList();
		TableIdentifier table = new TableIdentifier();
		table.setTableName(nodeConfig.getTableName());
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
			
			NodeModel model = new NodeModel(nodeConfig, rec);
			nodeList.add(model);
			
		}
		
		return nodeList;

	}
	
	public static NodeModel getNextNode(DataSrcConnection con, NodeConfig config, String nodeId)
		throws ExecutionException, DataSourceException
	{
		NodeModel nodeModel = getModelSingle(con, config, nodeId);
		
		if(nodeModel.hasChildren()){
			List<NodeModel> childrenModel = getChildren(con, config, nodeModel);
			
			if(!childrenModel.isEmpty()){
				return childrenModel.get(0);
			}else{
				return null;
			}
		}
		
		List<NodeModel> brosList = getBrothers(con, config, nodeModel);
		
		// last bros
		if(brosList.size() - 1 == nodeModel.getPosInLevel()){
			// root
			if(nodeModel.getLevel() == 0){
				return null;
			}
			
			NodeModel parentModel = getParentModel(con, config, nodeModel);
			brosList = getBrothers(con, config, parentModel);
			
			return getNextFromBros(brosList, parentModel);
		}
		
		// normal case
		return getNextFromBros(brosList, nodeModel);
	}
	
	private static NodeModel getNextFromBros(List<NodeModel> brosList, NodeModel nodeModel)
	{
		if(brosList.size() - 1 == nodeModel.getPosInLevel()){
			return null;
		}
		
		Iterator<NodeModel> it = brosList.iterator();
		while(it.hasNext()){
			NodeModel model = it.next();
			
			if(model.getPosInLevel() > nodeModel.getPosInLevel()){
				return model;
			}
		}
		
		return null;
	}
	
	public static NodeModel getPrevNodeModel(DataSrcConnection con, NodeConfig config, String nodeId)
	throws ExecutionException, DataSourceException
	{
		NodeModel nodeModel = getModelSingle(con, config, nodeId);
		
		// if first
		if(nodeModel.getPosInLevel() == 0){
			NodeModel parentModel = getParentModel(con, config, nodeModel);
			
			return parentModel;
		}
		
		List<NodeModel> brosList = getBrothers(con, config, nodeModel);
		
		// normal case
		NodeModel prevRoot = getprevModelFromBros(brosList, nodeModel);
		
		return getTopChild(con, config, prevRoot);
	}
	
	private static NodeModel getTopChild(DataSrcConnection con, NodeConfig config, NodeModel nodeModel)
			throws ExecutionException, DataSourceException
	{
		if(!nodeModel.hasChildren()){
			return nodeModel;
		}
		
		List<NodeModel> childrenModel = getChildren(con, config, nodeModel);
		
		if(childrenModel.size() == 0){
			return null;
		}
		
		return getTopChild(con, config, childrenModel.get(childrenModel.size() - 1));
	}
	
	private static NodeModel getprevModelFromBros(List<NodeModel> brosList, NodeModel nodeModel)
	{
		if(nodeModel.getPosInLevel() == 0){
			return null;
		}
		
		NodeModel lastModel = null;
		Iterator<NodeModel> it = brosList.iterator();
		while(it.hasNext()){
			NodeModel model = it.next();
			
			if(model.getPosInLevel() == nodeModel.getPosInLevel()){
				return lastModel;
			}
			
			lastModel = model;
		}
		
		return null;
	}
}
