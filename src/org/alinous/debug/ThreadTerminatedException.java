package org.alinous.debug;

import org.alinous.expections.ExecutionException;


public class ThreadTerminatedException extends ExecutionException {
	public ThreadTerminatedException()
	{
		super(null, "Stopped by the debugger");
	}
	
	public ThreadTerminatedException(Throwable cause, String message) {
		super(cause, message);
	}

	private static final long serialVersionUID = 1194901492904952670L;
	
}
