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
package org.alinous.plugin.postgres;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.alinous.AlinousCore;
import org.alinous.datasrc.exception.DataSourceException;
import org.apache.commons.pool.BasePoolableObjectFactory;

public class PostgreSQLConnectionFactory extends BasePoolableObjectFactory
{
	private Driver driver;
	private DataSource dataSource;
	private String user;
	private String pass;
	private String uri;
	private int maxclients;
	private boolean resultUpper;
	private AlinousCore core;
	
	public PostgreSQLConnectionFactory(AlinousCore core, Driver driver, String user, String pass, String uri,
			int maxclients, boolean resultUpper)  throws Exception
	{
		this.core = core;
		
		this.uri = uri;
		this.user = user;
		this.pass = pass;
		
		this.maxclients = maxclients;
		this.resultUpper = resultUpper;
		
		if(uri.startsWith("java:")){
			 Context initialContext = new InitialContext();
			 this.dataSource = (DataSource) initialContext.lookup(uri);
		}else{
			this.driver = driver;
		}
	}
	
	
	public int getMaxclients() {
		return maxclients;
	}


	@Override
	public Object makeObject()
		throws Exception {
		Connection con = null;
		
		// jndi
		if(this.dataSource != null){
			con = this.dataSource.getConnection();
			return new PostgreSqlConnection(core, con, this.resultUpper);
		}
		
		
		Properties info = new Properties();
		if(this.user != null && !this.user.equals("")){
			info.put("user", this.user);
		}
		if(this.pass != null && !this.pass.equals("")){
			info.put("password", this.pass);
		}
		
		
		try {
			con = this.driver.connect(this.uri, info);
		} catch (SQLException e) {
			throw new DataSourceException(e);
		}
		
		return new PostgreSqlConnection(core, con, this.resultUpper);
	}


	@Override
	public void passivateObject(Object arg0) throws Exception {
		PostgreSqlConnection con = (PostgreSqlConnection)arg0;
		
		con.clearCachedByCount(200);
		
		
		if(!con.getAutoCommit()){
			con.rollback();
			
			con.setAutoCommit(true);
		}
		
		//con.rollback();
		
		super.passivateObject(arg0);
	}


	@Override
	public synchronized void activateObject(Object arg0) throws Exception
	{
		super.activateObject(arg0);
	}


	@Override
	public synchronized void destroyObject(Object arg0) throws Exception
	{
		PostgreSqlConnection con = (PostgreSqlConnection)arg0;
		
		try{
			con.close();
		}catch(SQLException ignore){
			ignore.printStackTrace();
		}
		
		super.destroyObject(arg0);
	}


	@Override
	public boolean validateObject(Object obj) {
		PostgreSqlConnection con = (PostgreSqlConnection)obj;
		
		try {
		//	con.setAutoCommit(false);
		//	con.rollback();
			con.setAutoCommit(true);
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	

}
