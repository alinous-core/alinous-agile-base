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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alinous.AlinousCore;
import org.alinous.AlinousUtils;
import org.alinous.csv.CsvException;
import org.alinous.csv.CsvReader;
import org.alinous.csv.CsvRecord;
import org.alinous.csv.CsvWriter;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.Record;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.repository.AlinousSystemRepository;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.InsertSentence;
import org.alinous.script.sql.SelectSentence;
import org.alinous.script.sql.UpdateSentence;

public class SessionController extends VariableRepositoryStoreSupport
{	
	public static final String SESSION = "SESSION";
	
	private static SelectSentence select4Update = new SelectSentence();
	private static SelectSentence select4Lock = new SelectSentence();
	private static UpdateSentence updateSession = new UpdateSentence();
	private static InsertSentence insertSession = new InsertSentence();
	
	//private String sessionId;
	private AlinousSystemRepository systemRepository;
//	private AlinousCore alinousCore;
	
	public SessionController(AlinousSystemRepository systemRepository, String sessionId, AlinousCore alinousCore)
	{
		this.systemRepository = systemRepository;
//		this.sessionId = sessionId;
//		this.alinousCore = alinousCore;
	}
	
	public void storeSession(PostContext context, VariableRepository repo)
		throws AlinousException
	{
		ScriptDomVariable sessionVariable = getSessionVariable(repo, context);
		
		Map<IPathElement, ExecResultRecord> variableMap = new HashMap<IPathElement, ExecResultRecord>();
		if(sessionVariable != null){
			dump(sessionVariable, variableMap, context, repo);
		}
		
		try {
			//synchronized (context.getCore().getSesssionLockManager().getLock(context.getSessionId())) {
				doStore(context, variableMap, context.getSessionId());
			//}
			
		} catch (DataSourceException e) {
			throw new AlinousException(e, "Failed to delete records."); // i18n
		}finally{
			context.getCore().getSesssionLockManager().releaseLock(context.getSessionId());
		}
	}
	
	@SuppressWarnings("unused")
	private void doStore(PostContext context, Map<IPathElement, ExecResultRecord> variableMap, String sessionId) throws DataSourceException, AlinousException
	{
		DataSrcConnection con = null;
		try {
			//con = this.systemRepository.getConnection();
			con = context.getUnit().getConnectionManager().connect(context.getCore().getConfig().getSystemRepositoryConfig().getSystemSrc(), context);
		} catch (DataSourceException e) {
			if(con != null){con.close();}
			throw new AlinousException(e, "Failed in connect on SessionController"); // i18n
		}
		
		try {
			con.begin(Connection.TRANSACTION_READ_COMMITTED);
		}catch(DataSourceException e){
			if(con != null){con.close();}
			throw new AlinousException(e, "Failed to begin TRX on SessionController"); // i18n
		}
		
		// delete records
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(AlinousSystemRepository.SESSION_ID, sessionId);
		boolean exists = false;
		try{
			context.getPrecompile().setSqlSentence(SessionController.select4Lock);
			
			exists = this.systemRepository.existsLock(context, con, AlinousSystemRepository.SESSION_TABLE, queryParams);
		
			context.getPrecompile().setSqlSentence(null);
		}
		catch (DataSourceException e) {
			if(con != null){con.close();}
			throw new AlinousException(e, "Failed to delete SessionCache"); // i18n
		}
		catch (AlinousException e) {
			if(con != null){con.close();}
			throw e;
		}
		
		
		// insert records
		Record rec = new Record();
		rec.addFieldValue(AlinousSystemRepository.SESSION_ID, sessionId, IScriptVariable.TYPE_STRING);
		
		List<Record> recordsList = new ArrayList<Record>();
		StringWriter sw = new StringWriter();
		CsvWriter csvWriter = new CsvWriter(sw);
		Iterator<IPathElement> it = variableMap.keySet().iterator();
		while(it.hasNext()){
			IPathElement pathElement = it.next();
			ExecResultRecord val = variableMap.get(pathElement);
			
			try {
				/*csvWriter.addField(AlinousUtils.sqlEscape(val.getName()));
				csvWriter.addField(val.getType());
				csvWriter.addField(val.getValueType());
				csvWriter.addField(AlinousUtils.sqlEscape(val.getValue()));
				csvWriter.endRecord();*/
				
				csvWriter.addField(val.getName());
				csvWriter.addField(val.getType());
				csvWriter.addField(val.getValueType());
				csvWriter.addField(val.getValue());
				csvWriter.endRecord();
			} catch (IOException e) {
				if(con != null){con.close();}
				throw new AlinousException(e, "Failed in making csv");
			}
			
		}
		
		String csvStr = sw.getBuffer().toString();
		rec.addFieldValue(AlinousSystemRepository.VALUE, csvStr, IScriptVariable.TYPE_STRING);
		
		rec.addFieldValue(AlinousSystemRepository.CREATE_TIME, AlinousUtils.getNowString(), IScriptVariable.TYPE_STRING);
		recordsList.add(rec);
		
		
		if(!recordsList.isEmpty()){
			try {
				if(!exists){
					context.getPrecompile().setSqlSentence(insertSession);
					this.systemRepository.insertRecord(context, con, AlinousSystemRepository.SESSION_TABLE, recordsList);
					context.getPrecompile().setSqlSentence(null);
				}
				else{
					// update record
					context.getPrecompile().setSqlSentence(SessionController.updateSession);
					this.systemRepository.updateRecord(context, con, AlinousSystemRepository.SESSION_TABLE, recordsList, queryParams);
					context.getPrecompile().setSqlSentence(null);
				}
			} catch (DataSourceException e) {
				if(con != null){con.close();}
				throw new AlinousException(e, "Failed to insert record"); // i18n
			}
		}
		
		try{
			con.commit(null);
		} catch (DataSourceException e) {
			throw new AlinousException(e, "Failed to commit Session"); // i18n
		}finally{
			if(con != null){con.close();}
			// 
			context.getUnit().getConnectionManager().clearCurrentDataSrc(context);
		}
	}
	
	private ScriptDomVariable getSessionVariable(VariableRepository repo, PostContext context)
		throws ExecutionException
	{
		IPathElement variablePath = PathElementFactory.buildPathElement(SESSION);
		IScriptVariable sessionValue = null;
		try {
			sessionValue = repo.getVariable(variablePath, context);
		} catch (RedirectRequestException e) {
			e.printStackTrace();
		}
		
		if(!(sessionValue instanceof ScriptDomVariable)){
			return null;
		}
		
		return (ScriptDomVariable)sessionValue;
	}
	
	public void updateSession(VariableRepository repo, PostContext context)
		throws AlinousException
	{
		if(repo == null){
			return;
		}
		
		// read session
		repo.release(SESSION, context);
		
		HashMap<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(AlinousSystemRepository.SESSION_ID, context.getSessionId());
		
		List<Record> recordsList = null;
		try {
			context.getPrecompile().setSqlSentence(SessionController.select4Update);
			
			//synchronized (this.alinousCore.getSesssionLockManager().getLock(this.sessionId)) {
				recordsList = this.systemRepository.selectRecord(context, AlinousSystemRepository.SESSION_TABLE, queryParams);
			//}
				
			context.getPrecompile().setSqlSentence(null);
		} catch (DataSourceException e) {
			throw new AlinousException(e, "Failed to read ExecutionResultCache"); // i18n
		}
		
		// 
		repo.putValue(SESSION, context.getSessionId(), IScriptVariable.TYPE_STRING, context);
		
		// reading		
		Iterator<Record> it = recordsList.iterator();
		while(it.hasNext()){
			Record rec = it.next();
			
			String csvString = rec.getFieldValue(AlinousSystemRepository.VALUE);
			
			StringReader reader = new StringReader(csvString);
			CsvReader csvReader = new CsvReader(reader);
			CsvRecord csvRec = null;

			do{
				try {
					csvRec = csvReader.readRecord();
					if(csvRec.isEmpty()){
						break;
					}
					
				} catch (CsvException e) {
					throw new AlinousException(e, "Failed in reading cache.");
				} catch (IOException e) {
					throw new AlinousException(e, "Failed in reading cache.");
				}

				String namePath = csvRec.get(0);
				String type = csvRec.get(1);
				String valueType = csvRec.get(2);
				String value = csvRec.get(3);
				
				if(type.equals(IScriptVariable.TYPE_HASH)){
					repo.putValue(namePath, value, valueType, context);
				}
			}while(!csvRec.isEmpty());
		}
		
	}

}
