package org.alinous.script.runtime;

import java.util.HashMap;

import org.alinous.exec.pages.PostContext;

public class WrappedMap extends HashMap<PostContext, FinalRepository>{
	private static final long serialVersionUID = -3734615969933320499L;

	@Override
	public FinalRepository get(Object key) {
		return super.get(key);
	}

	@Override
	public FinalRepository put(PostContext key, FinalRepository value) {
		return super.put(key, value);
	}


}
