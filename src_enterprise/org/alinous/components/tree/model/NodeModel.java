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

import org.alinous.components.tree.NodeConfig;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.Record;
import org.alinous.expections.ExecutionException;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.sql.statement.ISQLStatement;
import org.alinous.script.sql.statement.SQLNumericConst;
import org.alinous.script.sql.statement.SQLStringConst;

public class NodeModel
{
	public static final String NODE_ID = "NODE_ID";
	public static final String LEVEL = "LEVEL";
	public static final String PARENT_ID = "PARENT_ID";
	public static final String NUM_CHILDREN = "NUM_CHILDREN";
	public static final String POS_IN_LEVEL = "POS_IN_LEVEL";
	public static final String TITLE = "TITLE";
	public static final String DOC_TYPE = "DOC_TYPE";
	public static final String DOC_REF = "DOC_REF";
	public static final String VISIBLE = "VISIBLE";
	public static final String CATEGORY = "CATEGORY";
	
	public static final String MAX_NODE_ID = "MAX_NODE_ID";
	public static final String MAX_ROOT_POS = "MAX_ROOT_POS";
	
	private NodeConfig config;
	private Record rec;
	
	public NodeModel(NodeConfig config)
	{
		this.config = config;
		this.rec = new Record();
	}
	
	
	public NodeModel(NodeConfig config, Record rec)
	{
		this.config = config;
		this.rec = rec;
	}
	
	public void initNewModel(DataSrcConnection con, String parentNodeId, String title,
			String lastPosId) throws ExecutionException, DataSourceException
	{
		// publish new id
		int newId = NodeModelUtils.getNextNodeId(con, this.config);
		this.rec.addFieldValue(NODE_ID, Integer.toString(newId), IScriptVariable.TYPE_STRING);
		
		this.rec.addFieldValue(PARENT_ID, parentNodeId, IScriptVariable.TYPE_STRING);
		this.rec.addFieldValue(TITLE, title, IScriptVariable.TYPE_STRING);
		this.rec.addFieldValue(NUM_CHILDREN, "0", IScriptVariable.TYPE_STRING);
		this.rec.addFieldValue(VISIBLE, "true", IScriptVariable.TYPE_STRING);
		
		// doctype
		this.rec.addFieldValue(DOC_TYPE, this.config.getDefaultDoctype().getId(), IScriptVariable.TYPE_STRING);
		//this.rec.addFieldValue(DOC_REF, "0");
		
		// getNode
		if(!parentNodeId.equals("0")){
			NodeModel parentNode = getNodeById(con, parentNodeId);
			
			int level = parentNode.getLevel();
			setLevel(level + 1);
			
		}else{
			setLevel(0);
		}
		
		setPosInLevel(Integer.parseInt(lastPosId) + 1);
	}
	
	public NodeModel getNodeById(DataSrcConnection con, String nodeId) throws ExecutionException, DataSourceException
	{
		return NodeModelUtils.getModelSingle(con, config, nodeId);
	}
	
	public boolean hasChildren()
	{
		if(getNumChildren() > 0){
			return true;
		}
		
		return false;
	}
	
	public int getId()
	{
		return Integer.parseInt(this.rec.getFieldValue(NODE_ID));
	}
	
	public int getLevel()
	{
		return Integer.parseInt(this.rec.getFieldValue(LEVEL));
	}
	
	public void setLevel(int level)
	{
		this.rec.addFieldValue(LEVEL, Integer.toString(level), IScriptVariable.TYPE_NUMBER);
	}
	
	public void addLevel(int def)
	{
		int newLevel = getLevel() + def;
		setLevel(newLevel);
	}
	
	public int getPosInLevel()
	{
		return Integer.parseInt(this.rec.getFieldValue(POS_IN_LEVEL));
	}
	
	public void setPosInLevel(int pos)
	{
		this.rec.addFieldValue(POS_IN_LEVEL, Integer.toString(pos), IScriptVariable.TYPE_NUMBER);
	}
	
	public void addPosInLevel(int def)
	{
		int pos = getPosInLevel() + def;
		setPosInLevel(pos);
	}
	
	public String getTitle()
	{
		return this.rec.getFieldValue(TITLE);
	}
	
	public void setTitle(String title)
	{
		this.rec.addFieldValue(TITLE, title, IScriptVariable.TYPE_STRING);
	}
	
	public void setNumChildern(int numChildren)
	{
		this.rec.addFieldValue(NUM_CHILDREN, Integer.toString(numChildren), IScriptVariable.TYPE_NUMBER);
	}
	
	public int getNumChildren()
	{
		return Integer.parseInt(this.rec.getFieldValue(NUM_CHILDREN));
	}
	
	public void addNumChildren(int def)
	{
		int num = getNumChildren() + def;
		setNumChildern(num);
	}
	
	public void setParentId(int id)
	{
		this.rec.addFieldValue(PARENT_ID, Integer.toString(id), IScriptVariable.TYPE_NUMBER);
	}
	
	public int getParentId()
	{
		return Integer.parseInt(this.rec.getFieldValue(PARENT_ID));
	}
	
	public boolean hasParent()
	{
		if(getParentId() > 0){
			return true;
		}else{
			return false;
		}
	}
	
	public String getDocType()
	{
		return this.rec.getFieldValue(DOC_TYPE);
	}
	
	public void setDocType(String docType)
	{
		this.rec.addFieldValue(DOC_TYPE, docType, IScriptVariable.TYPE_STRING);
	}
	
	public void setDocRef(String ref)
	{
		this.rec.addFieldValue(DOC_REF, ref, IScriptVariable.TYPE_STRING);
	}
	
	public String getDocRef()
	{
		return this.rec.getFieldValue(DOC_REF);
	}
	
	public String getVisible()
	{
		return this.rec.getFieldValue(VISIBLE);
	}
	
	public void setVisible(String visible)
	{
		this.rec.addFieldValue(VISIBLE, visible, IScriptVariable.TYPE_STRING);
	}
	
	public String getCategory()
	{
		return this.rec.getFieldValue(CATEGORY);
	}
	
	public void setCategory(String category)
	{
		this.rec.addFieldValue(CATEGORY, category, IScriptVariable.TYPE_STRING);
	}
	
	public boolean isExistsInTree()
	{
		if(hasChildren()){
			return true;
		}
		
		return isVisible();
	}
	
	public boolean isVisible()
	{
		String vi = getVisible();
		
		if(vi != null && vi.equals("false")){
			return false;
		}
		return true;
	}
	
	public Record getRec()
	{
		return rec;
	}
	
	public String toString()
	{
		StringBuffer buff = new StringBuffer();
		
		appendRecValue(NODE_ID, buff);
		buff.append(",");
		appendRecValue(LEVEL, buff);
		buff.append(",");
		appendRecValue(PARENT_ID, buff);
		buff.append(",");
		appendRecValue(NUM_CHILDREN, buff);
		buff.append(",");
		appendRecValue(POS_IN_LEVEL, buff);
		buff.append(",");
		appendRecValue(TITLE, buff);
		buff.append(",");
		appendRecValue(DOC_TYPE, buff);
		buff.append(",");
		appendRecValue(VISIBLE, buff);
		
		return buff.toString();
	}
	
	private void appendRecValue(String key, StringBuffer buff)
	{
		buff.append(key);
		buff.append("=");
		buff.append(this.rec.getFieldValue(key));
	}
	
	public static ISQLStatement getConst(String key, String val)
	{ 
		if(key.equals(NODE_ID) || key.equals(LEVEL) || key.equals(PARENT_ID) ||
				key.equals(NUM_CHILDREN) || key.equals(POS_IN_LEVEL)){
			SQLNumericConst numConst = new SQLNumericConst();
			numConst.setNumber(val);
			
			return numConst;
		}
		
		SQLStringConst strConst = new SQLStringConst();
		strConst.setStr(val);
		
		return strConst;
	}
}
