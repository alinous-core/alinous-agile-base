package org.alinous.script.functions.system.ide;

public class AlinousColumn {
	private String columnName;
	private String columnType;
	private String defaultValue;
	
	private String comment;
	private String aliasComment;
	private String foreignKey;	
	
	private boolean notnull;
	private boolean unique;
	private boolean primaryKey;
	private boolean indexed;
	
	private String length;
	
	public String getInfoString()
	{
		StringBuffer buff = new StringBuffer();
		
		if(this.comment != null && !this.comment.equals("")){
			buff.append(this.comment);
			buff.append("\n");
		}
		
		if(this.aliasComment != null){
			buff.append(this.aliasComment);
			buff.append("\n");
		}
		
		buff.append("@DECLARATION\n");
		
		buff.append(toString());
		
		return buff.toString();
	}
	
	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		
		buff.append(this.columnName);
		buff.append(" ");
		buff.append(this.columnType);
		if(this.length != null){
			buff.append("(");
			buff.append(this.length);
			buff.append(")");
		}
		
		if(this.unique){
			buff.append(" unique");
		}
		if(this.notnull){
			buff.append(" not null");
		}
		
		if(this.defaultValue != null){
			buff.append(" default ");
			buff.append(this.defaultValue);
		}
		
		
		return buff.toString();
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getColumnType() {
		return columnType;
	}
	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getLength() {
		return length;
	}
	public void setLength(String length) {
		this.length = length;
	}
	public boolean isNotnull() {
		return notnull;
	}
	public void setNotnull(boolean notnull) {
		this.notnull = notnull;
	}
	public boolean isUnique() {
		return unique;
	}
	public void setUnique(boolean unique) {
		this.unique = unique;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getAliasComment()
	{
		return aliasComment;
	}

	public void setAliasComment(String aliasComment)
	{
		this.aliasComment = aliasComment;
	}

	public boolean isPrimaryKey()
	{
		return primaryKey;
	}

	public void setPrimaryKey(boolean primaryKey)
	{
		this.primaryKey = primaryKey;
	}

	public boolean isIndexed()
	{
		return indexed;
	}

	public void setIndexed(boolean indexed)
	{
		this.indexed = indexed;
	}

	public String getForeignKey()
	{
		return foreignKey;
	}

	public void setForeignKey(String foreignKey)
	{
		this.foreignKey = foreignKey;
	}
	
	
}
