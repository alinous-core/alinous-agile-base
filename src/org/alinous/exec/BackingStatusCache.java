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
import org.alinous.repository.AlinousSystemRepository;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.sql.InsertSentence;
import org.alinous.script.sql.SelectSentence;

public class BackingStatusCache
{
	private AlinousSystemRepository systemRepository;
	//private String sessionId;
	//private AlinousCore alinousCore;
	private static SelectSentence select4Update = new SelectSentence();
	private static SelectSentence updateStatus = new SelectSentence();
	private static InsertSentence insert = new InsertSentence();
	
	public BackingStatusCache(AlinousSystemRepository systemRepository, String sessionId, AlinousCore alinousCore)
	{
		this.systemRepository = systemRepository;
		//this.sessionId = sessionId;
//		this.alinousCore = alinousCore;
	}
	
	public String getLastPath(PostContext context, InnerModulePath modulePath, String filePath, String sessionId) throws AlinousException
	{
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(AlinousSystemRepository.SESSION_ID, sessionId);
		queryParams.put(AlinousSystemRepository.MODULE_PATH, modulePath.getStringPath());
		queryParams.put(AlinousSystemRepository.FILE_PATH, filePath);
		
		List<Record> list = null;
		try {
			context.getPrecompile().setSqlSentence(BackingStatusCache.select4Update);
			
			list = systemRepository.selectRecord(context, AlinousSystemRepository.BACKING_STATUS_TABLE, queryParams);
			
			context.getPrecompile().setSqlSentence(null);
		} catch (DataSourceException e) {
			throw new AlinousException(e, "Failed to retrieve InnerStatusCache."); // i18n
		}
		
		if(list.size() > 0){
			return list.get(0).getFieldValue(AlinousSystemRepository.LAST_FILE_PATH);
		}
		
		return null;
	}
	
	public void storeLastPath(PostContext context, InnerModulePath modulePath, String filePath, String lastFilePath, String sessionId) throws AlinousException
	{
		//synchronized (this.alinousCore.getSesssionLockManager().getLock(this.sessionId)) {
			doStoreLastPath(context, modulePath, filePath, lastFilePath, sessionId);
		//}
	}
	
	@SuppressWarnings("unused")
	public void doStoreLastPath(PostContext context, InnerModulePath modulePath, String filePath, String lastFilePath, String sessionId) throws AlinousException
	{
		DataSrcConnection con = null;
		try {
			con = context.getUnit().getConnectionManager().connect(context.getCore().getConfig().getSystemRepositoryConfig().getSystemSrc(), context);
		} catch (DataSourceException e) {
			if(con != null){con.close();}
			throw new AlinousException(e, "Failed in connect on handling Backing cache"); // i18n
		}
		
		// delete
		boolean blInsertRec;
		Map<String, String> queryParams = null;
		try{
			con.begin(Connection.TRANSACTION_READ_COMMITTED);
			
			queryParams = clear(con, modulePath, filePath, sessionId);
			blInsertRec = this.systemRepository.existsLock(context, con, AlinousSystemRepository.BACKING_STATUS_TABLE, queryParams);
		}catch(AlinousException e){
			if(con != null){con.close();}
			throw e;
		} catch (DataSourceException e) {
			if(con != null){con.close();}
			throw new AlinousException(e, "Failed in clear Backing cache"); // i18n
		}
		
		// store
		Record rec = new Record();
		
		rec.addFieldValue(AlinousSystemRepository.SESSION_ID, sessionId, IScriptVariable.TYPE_STRING);
		rec.addFieldValue(AlinousSystemRepository.MODULE_PATH, modulePath.getStringPath(), IScriptVariable.TYPE_STRING);
		rec.addFieldValue(AlinousSystemRepository.FILE_PATH, filePath, IScriptVariable.TYPE_STRING);
		rec.addFieldValue(AlinousSystemRepository.LAST_FILE_PATH, lastFilePath, IScriptVariable.TYPE_STRING);
		rec.addFieldValue(AlinousSystemRepository.CREATE_TIME, AlinousUtils.getNowString(), IScriptVariable.TYPE_STRING);
		
		
		List<Record> recordsList = new ArrayList<Record>();
		recordsList.add(rec);
		try {
			if(!blInsertRec){
				context.getPrecompile().setSqlSentence(insert);
				this.systemRepository.insertRecord(context, con, AlinousSystemRepository.BACKING_STATUS_TABLE, recordsList);
				context.getPrecompile().setSqlSentence(null);
			}else{
				context.getPrecompile().setSqlSentence(BackingStatusCache.updateStatus);
				this.systemRepository.updateRecord(context, con, AlinousSystemRepository.BACKING_STATUS_TABLE, recordsList, queryParams);
				context.getPrecompile().setSqlSentence(null);
			}
			
			con.commit(null);
		} catch (DataSourceException e) {
			throw new AlinousException(e, "Failed to insert FromValueCache"); // i18n
		}finally{
			if(con != null){con.close();}
			
			// 
			context.getUnit().getConnectionManager().clearCurrentDataSrc(context);
		}
	}
	
	private Map<String, String> clear(DataSrcConnection con, InnerModulePath modulePath, String filePath, String sessionId) throws AlinousException
	{
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(AlinousSystemRepository.SESSION_ID, sessionId);
		queryParams.put(AlinousSystemRepository.MODULE_PATH, modulePath.getStringPath());
		queryParams.put(AlinousSystemRepository.FILE_PATH, filePath);
		
		return queryParams;
		
		/*
		try {
			this.systemRepository.deleteRecord(con, AlinousSystemRepository.BACKING_STATUS_TABLE, queryParams);
		} catch (DataSourceException e) {
			throw new AlinousException(e, "Failed to delete FromValueCache"); // i18n
		}*/
	}
}
