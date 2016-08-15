package org.alinous.script.runtime;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;

public class FinalRepository extends VariableRepository{

	@Override
	public void putValue(IScriptVariable value, PostContext context)
			throws ExecutionException, RedirectRequestException {
		super.putValue(value, context);
	}

	@Override
	public void putValue(String variablePath, String value, String valueType,
			PostContext context) throws ExecutionException,
			RedirectRequestException {
		super.putValue(variablePath, value, valueType, context);
	}

	@Override
	public void putFinalValue(IPathElement path, IScriptVariable value,
			PostContext context) throws ExecutionException,
			RedirectRequestException {
		super.putFinalValue(path, value, context);
	}

	@Override
	public void putValue(IPathElement path, IScriptVariable value,
			PostContext context) throws ExecutionException,
			RedirectRequestException {
		super.putValue(path, value, context);
	}

	@Override
	public void putAlias(IScriptVariable variable, String name) {
		super.putAlias(variable, name);
	}

}
