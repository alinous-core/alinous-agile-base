package org.alinous.script.sql.ddl;

import org.alinous.datasrc.types.TypeHelper;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.ISQLScriptObject;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;
import org.alinous.script.sql.condition.ISQLExpression;

public class CheckDefinition implements ISQLScriptObject
{
	private ISQLExpression exp;

	public String extract(PostContext context, VariableRepository valRepo,
			AdjustWhere adjustWhere, AdjustSet adjustSet, TypeHelper helper)
			throws ExecutionException
	{
		StringBuffer buff = new StringBuffer();
		
		buff.append("CHECK(");
		
		buff.append(this.exp.extract(context, valRepo, adjustWhere, adjustSet, helper));
		
		buff.append(")");
		
		return buff.toString();
	}

	public boolean isReady(PostContext context, VariableRepository valRepo,
			AdjustWhere adjustWhere) throws ExecutionException
	{
		return exp.isReady(context, valRepo, adjustWhere);
	}

	public ISQLExpression getExp()
	{
		return exp;
	}

	public void setExp(ISQLExpression exp)
	{
		this.exp = exp;
	}
	
	
	
}
