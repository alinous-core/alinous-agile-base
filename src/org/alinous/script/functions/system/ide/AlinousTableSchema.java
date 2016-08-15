package org.alinous.script.functions.system.ide;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AlinousTableSchema {
	private String tableName;
	private List<AlinousColumn> columns = new ArrayList<AlinousColumn>();
	
	private List<String> primaryKeys = new ArrayList<String>();
	
	private String comment;
	private String commentType;
	
	public void addPrimaryKey(String key)
	{
		this.primaryKeys.add(key);
		
		Iterator<AlinousColumn> it = this.columns.iterator();
		while(it.hasNext()){
			AlinousColumn col = it.next();
			
			if(this.primaryKeys.contains(col.getColumnName().toUpperCase())){
				col.setPrimaryKey(true);
			}
		}
	}
	
	public void addColumn(AlinousColumn col)
	{
		this.columns.add(col);
		
		if(this.primaryKeys.contains(col.getColumnName().toUpperCase())){
			col.setPrimaryKey(true);
		}
	}
	
	public AlinousColumn getColumn(String columnName)
	{
		String colName = columnName.toUpperCase();
		
		Iterator<AlinousColumn> it = this.columns.iterator();
		while(it.hasNext()){
			AlinousColumn col = it.next();
			if(col.getColumnName().toUpperCase().equals(colName)){
				return col;
			}
		}
		
		return null;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment, String commentType) {
		this.comment = comment;
		this.commentType = commentType;
	}

	public List<AlinousColumn> getColumns() {
		return columns;
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		
		if(this.comment != null){
			buff.append(this.comment);
		}
		
		buff.append("\n");
		buff.append("@DDL\n");
		buff.append("CREATE TABLE ");
		buff.append(this.tableName);
		buff.append("(\n");
		
		boolean first = true;
		Iterator<AlinousColumn> it = this.columns.iterator();
		while(it.hasNext()){
			AlinousColumn col = it.next();
			
			if(first){
				first = false;
			}else{
				buff.append(",\n");
			}
			
			buff.append("\t");
			buff.append(col.toString());
		}
		
		
		// primary keys
		buff.append("\n\n\tprimary key(");
		
		first = true;
		Iterator<String> keyIt = this.primaryKeys.iterator();
		while(keyIt.hasNext()){
			String key = keyIt.next();
			
			if(first){
				first = false;
			}else{
				buff.append(", ");
			}
			
			buff.append(key);
		}
		buff.append(")\n");
		
		buff.append(")");
		
		return buff.toString();
	}

	public String getCommentType()
	{
		return commentType;
	}
	
}
