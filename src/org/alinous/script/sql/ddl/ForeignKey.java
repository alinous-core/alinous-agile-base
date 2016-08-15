package org.alinous.script.sql.ddl;

import org.alinous.datasrc.types.TypeHelper;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.sql.other.ColumnList;
import org.alinous.script.sql.other.TableIdentifier;

public class ForeignKey
{
	TableIdentifier refTable = null;
	private ColumnList foreignKeys;
	private ColumnList referencedKey;
	
	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		StringBuffer buff = new StringBuffer();
		
		buff.append("FOREIGN KEY(");
		buff.append(this.foreignKeys.extract(context, providor, adjustWhere, adjustSet, helper));
		buff.append(") ");
		
		buff.append("REFERENCES ");
		buff.append(refTable.extract(context, providor, adjustWhere, adjustSet, helper));
		buff.append("(");
		buff.append(this.referencedKey.extract(context, providor, adjustWhere, adjustSet, helper));
		buff.append(")");
		
		return buff.toString();
	}
	
	public TableIdentifier getRefTable()
	{
		return refTable;
	}
	public void setRefTable(TableIdentifier refTable)
	{
		this.refTable = refTable;
	}
	public ColumnList getForeignKeys()
	{
		return foreignKeys;
	}
	public void setForeignKeys(ColumnList foreignKeys)
	{
		this.foreignKeys = foreignKeys;
	}
	public ColumnList getReferencedKey()
	{
		return referencedKey;
	}
	public void setReferencedKey(ColumnList referenceKey)
	{
		this.referencedKey = referenceKey;
	}
	
	
}
