package org.alinous.script.sql.statement;

import org.alinous.datasrc.types.TypeHelper;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.adjustopt.AdjustSet;
import org.alinous.script.sql.adjustopt.AdjustWhere;

public class JoinSubQueryTarget extends SubQueryStatement{
	private String asName;

	public String getAsName() {
		return asName;
	}

	public void setAsName(String asName) {
		this.asName = asName;
	}
	
	public String extract(PostContext context, VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		TypeHelper newHelper = helper.newHelper(false, this.selectSentence);
		
		return "(" + this.selectSentence.extract(context, providor, null, null, newHelper) + ") AS " + this.asName;
	}

		
	@Override
	public String extractPrecompile(PostContext context,
			VariableRepository providor, AdjustWhere adjustWhere,
			AdjustSet adjustSet, TypeHelper helper) throws ExecutionException
	{
		TypeHelper newHelper = helper.newHelper(false, this.selectSentence);
		
		return "(" + this.selectSentence.extract(context, providor, null, null, newHelper) + ") AS " + this.asName;
	}
}
