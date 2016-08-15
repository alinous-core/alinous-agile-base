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
package org.alinous.plugin.mysql;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import org.alinous.datasrc.exception.DataSourceException;
import org.apache.commons.pool.BasePoolableObjectFactory;

public class MySQLConnectionFactory extends BasePoolableObjectFactory
{
	private Driver driver;
	private String user;
	private String pass;
	private String uri;
	
	public MySQLConnectionFactory(Driver driver, String user, String pass, String uri)
	{
		this.driver = driver;
		
		this.uri = uri;
		this.user = user;
		this.pass = pass;
	}
	
	
	@Override
	public Object makeObject()
		throws Exception {

		Properties info = new Properties();
		if(this.user != null && !this.user.equals("")){
			info.put("user", this.user);
		}
		if(this.pass != null && !this.pass.equals("")){
			info.put("password", this.pass);
		}
	
		Connection con = null;
		try {
			con = this.driver.connect(this.uri, info);
		} catch (SQLException e) {
			throw new DataSourceException(e);
		} catch(Throwable e){
			throw new DataSourceException(e);
		}
		
		return con;
	}


	@Override
	public void passivateObject(Object arg0) throws Exception {
		
		Connection con = (Connection)arg0;
		
		if(!con.getAutoCommit()){
			con.rollback();
			con.setAutoCommit(true);
		}
		
		
		//AlinousDebug.debugOut("*************About passivateObject(arg0);");
		super.passivateObject(arg0);
	}


	@Override
	public void activateObject(Object arg0) throws Exception
	{
		super.activateObject(arg0);
	}


	@Override
	public void destroyObject(Object arg0) throws Exception
	{
		Connection con = (Connection)arg0;
		
		try{
			con.close();
		}catch (Exception e) {
			throw e;
		}
		finally{
			//AlinousDebug.debugOut("*************About destroyObject(Object arg0);");
			super.destroyObject(arg0);
	
		}
	}


	@Override
	public boolean validateObject(Object obj)
	{
		Connection con = (Connection)obj;
		
		try {
			return !con.isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
