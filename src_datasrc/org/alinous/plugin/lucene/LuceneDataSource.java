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
package org.alinous.plugin.lucene;

import java.util.List;

import org.alinous.datasrc.IAlinousDataSource;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.Record;
import org.alinous.script.sql.FromClause;
import org.alinous.script.sql.WhereClause;

public abstract class LuceneDataSource implements IAlinousDataSource{
	private String user;
	private String pass;
	private String uri;
	
	
	public void init() throws DataSourceException
	{
		
	}
	
	public Object connect() {
		return null;
	}

	public void close(Object connectObj)
	{
		
	}
	
	public void delete(Object connectionHandle, Record set, String table) throws DataSourceException
	{
		
	}

	public void insert(Object connectionHandle, Record record, String table) throws DataSourceException
	{
		
	}

	public void insert(Object connectionHandle, List<Record> records, String table) throws DataSourceException
	{
	
	}

	public List<Record> select(Object connectionHandle, FromClause from, WhereClause where) throws DataSourceException
	{
		return null;
	}

	// Getter and Setter
	public void setPass(String pass) {
		this.pass = pass;		
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public String getUri() {
		return uri;
	}

	public String getUser() {
		return user;
	}



}
