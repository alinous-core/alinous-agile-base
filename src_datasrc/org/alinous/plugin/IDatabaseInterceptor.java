package org.alinous.plugin;

import java.sql.Connection;

import org.alinous.datasrc.exception.MaxRecordsException;
import org.alinous.script.sql.FromClause;

public interface IDatabaseInterceptor {
	public void init();
	
	public void interceptInsert(Connection con, String table) throws MaxRecordsException;
	public void interceptSelect(Connection con, FromClause from) throws MaxRecordsException;
}
