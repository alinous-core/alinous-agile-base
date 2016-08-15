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
package org.alinous.exec;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alinous.AlinousCore;
import org.alinous.AlinousUtils;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.Record;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.repository.AlinousSystemRepository;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.sql.InsertSentence;
import org.alinous.script.sql.SelectSentence;
import org.alinous.script.sql.UpdateSentence;

public class InnerStatusCache
{
	private AlinousSystemRepository systemRepository;
	//private String sessionId;
	//private AlinousCore alinousCore;
	private static SelectSentence select4Update = new SelectSentence();
	private static UpdateSentence update = new UpdateSentence();
	private static InsertSentence insert = new InsertSentence();
	
	public InnerStatusCache(AlinousSystemRepository sysRepo, String sessionId,  AlinousCore alinousCore)
	{
		this.systemRepository = sysRepo;
	//	this.sessionId = sessionId;
	//	this.alinousCore = alinousCore;
	}
	
	public String getLastPath(PostContext context, InnerModulePath modulePath, String sessionId) throws AlinousException
	{
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(AlinousSystemRepository.SESSION_ID, sessionId);
		queryParams.put(AlinousSystemRepository.MODULE_PATH, modulePath.getStringPath());
		
		List<Record> list = null;
		try {
			context.getPrecompile().setSqlSentence(InnerStatusCache.select4Update);
			
			list = systemRepository.selectRecord(context, AlinousSystemRepository.INNER_STATUS_TABLE, queryParams);
			
			context.getPrecompile().setSqlSentence(null);
		} catch (DataSourceException e) {
			throw new AlinousException(e, "Failed to retrieve InnerStatusCache."); // i18n
		}
		
		if(list.size() > 0){
			return list.get(0).getFieldValue(AlinousSystemRepository.FILE_PATH);
		}
		
		return null;
	}
	
	public void storeLastPath(PostContext context, InnerModulePath modulePath, String filePath, String sessionId) throws AlinousException
	{
		//synchronized (this.alinousCore.getSesssionLockManager().getLock(sessionId)) {
			doStoreLastPath(context, modulePath, filePath, sessionId);
		//}
	}
	
	
	@SuppressWarnings("unused")
	private void doStoreLastPath(PostContext context, InnerModulePath modulePath, String filePath, String sessionId) throws AlinousException
	{
		DataSrcConnection con = null;
		try {
			//con = this.systemRepository.getConnection();
			con = context.getUnit().getConnectionManager().connect(context.getCore().getConfig().getSystemRepositoryConfig().getSystemSrc(), context);
		} catch (DataSourceException e) {
			if(con != null){con.close();}
			throw new AlinousException(e, "Failed in connect on InnserStatusCache"); // i18n
		}
		
		try {
			con.begin(Connection.TRANSACTION_READ_COMMITTED);
		}catch(DataSourceException e){
			if(con != null){con.close();}
			throw new AlinousException(e, "Failed to begin TRX on InnserStatusCache"); // i18n
		}
		
		boolean blInsertRec;
		Map<String, String> queryParams = null;
		try{
			queryParams = clear(con, modulePath, sessionId);
			
			context.getPrecompile().setSqlSentence(InnerStatusCache.select4Update);
			
			blInsertRec = this.systemRepository.existsLock(context, con, AlinousSystemRepository.INNER_STATUS_TABLE, queryParams);
			
			context.getPrecompile().setSqlSentence(null);
		
		}
		catch (DataSourceException e) {
			if(con != null){con.close();}
			throw new AlinousException(e, "Failed to delete InnserStatusCache"); // i18n
		}
		catch (AlinousException e) {
			if(con != null){con.close();}
			throw e;
		}
		
		Record rec = new Record();
		
		rec.addFieldValue(AlinousSystemRepository.SESSION_ID, sessionId, IScriptVariable.TYPE_STRING);
		rec.addFieldValue(AlinousSystemRepository.MODULE_PATH, modulePath.getStringPath(), IScriptVariable.TYPE_STRING);
		rec.addFieldValue(AlinousSystemRepository.FILE_PATH, filePath, IScriptVariable.TYPE_STRING);
		rec.addFieldValue(AlinousSystemRepository.CREATE_TIME, AlinousUtils.getNowString(), IScriptVariable.TYPE_STRING);
		
		List<Record> recordsList = new ArrayList<Record>();
		recordsList.add(rec);
		try {
			if(!blInsertRec){
				context.getPrecompile().setSqlSentence(insert);
				this.systemRepository.insertRecord(context, con, AlinousSystemRepository.INNER_STATUS_TABLE, recordsList);
				context.getPrecompile().setSqlSentence(null);
			}else{
				context.getPrecompile().setSqlSentence(update);
				this.systemRepository.updateRecord(context, con, AlinousSystemRepository.INNER_STATUS_TABLE, recordsList, queryParams);
				context.getPrecompile().setSqlSentence(null);
			}
		} catch (DataSourceException e) {
			if(con != null){con.close();}
			throw new AlinousException(e, "Failed to delete InnserStatusCache"); // i18n
		}
		
		try{
			con.commit(null);
		} catch (DataSourceException e) {
			throw new AlinousException(e, "Failed to delete InnserStatusCache"); // i18n
		}finally{
			if(con != null){con.close();}
			
			// 
			context.getUnit().getConnectionManager().clearCurrentDataSrc(context);
		}
	}
	
	public Map<String, String> clear(DataSrcConnection con, InnerModulePath modulePath, String sessionId) throws ExecutionException, DataSourceException
	{
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(AlinousSystemRepository.SESSION_ID, sessionId);
		queryParams.put(AlinousSystemRepository.MODULE_PATH, modulePath.getStringPath());
		
		return queryParams;
		
		//this.systemRepository.deleteRecord(con, AlinousSystemRepository.INNER_STATUS_TABLE, queryParams);
		/*
		try {
			this.systemRepository.deleteRecord(AlinousSystemRepository.INNER_STATUS_TABLE, queryParams);
		} catch (DataSourceException e) {
			throw new AlinousException(e, "Failed to delete FromValueCache"); // i18n
		}*/
	}
}
