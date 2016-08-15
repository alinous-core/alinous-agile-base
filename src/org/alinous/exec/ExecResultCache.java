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
import org.alinous.repository.AlinousSystemRepository;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.sql.DeleteSentence;
import org.alinous.script.sql.InsertSentence;
import org.alinous.script.sql.SelectSentence;
import org.alinous.script.sql.UpdateSentence;

public class ExecResultCache extends VariableRepositoryStoreSupport
{
	private AlinousSystemRepository systemRepository;
//	private String sessionId;
//	private AlinousCore alinousCore;
	private static SelectSentence select4Update = new SelectSentence();
	private static UpdateSentence update = new UpdateSentence();
	private static InsertSentence insert = new InsertSentence();
	
	private static SelectSentence select4delete = new SelectSentence();
	private static DeleteSentence delete = new DeleteSentence();
	
	public ExecResultCache(AlinousSystemRepository sysRepo, String sessionId, AlinousCore alinousCore)
	{
		this.systemRepository = sysRepo;
//		this.sessionId = sessionId;
//		this.alinousCore = alinousCore;
	}
	
	
	public void storeResult(PostContext context, VariableRepository repo, InnerModulePath targetPath,
			String filePath, String sessionId)
			throws AlinousException
	{
		//
		//AlinousDebug.debugOut("@@@@@@@@@@@@@@@@ storeResult : " + filePath + " mod : " + targetPath.getStringPath());
		
		Map<IPathElement, ExecResultRecord> variableMap = new HashMap<IPathElement, ExecResultRecord>();
		
		Iterator<String> it = repo.getKeyIterator();
		while(it.hasNext()){
			String key = it.next();
			
			// result
			IPathElement variablePath = PathElementFactory.buildPathElement(key);
			IScriptVariable variable = repo.getVariable(variablePath, context);
			dump(variable, variableMap, context, repo);
		}
		

		try {
			synchronized (context.getCore()) {
				storeIntoSystemRepository(context, variableMap, targetPath, filePath, sessionId);
			}
		} catch (DataSourceException e) {
			throw new AlinousException(e, "Failed to store records."); // i18n
		}
	}
	
	@SuppressWarnings("unused")
	private void storeIntoSystemRepository(PostContext context, Map<IPathElement, ExecResultRecord> variableMap,
			InnerModulePath targetPath, String filePath, String sessionId)
				throws AlinousException, DataSourceException
	{
		DataSrcConnection con = null;
		try {
			con = context.getUnit().getConnectionManager().connect(context.getCore().getConfig().getSystemRepositoryConfig().getSystemSrc(), context);
			//con = this.systemRepository.getConnection();
		} catch (DataSourceException e) {
			if(con != null){con.close();}
			throw new AlinousException(e, "Failed in connect on ExecResult cache"); // i18n
		}
		
		try {
			con.begin(Connection.TRANSACTION_READ_COMMITTED);
		}catch(DataSourceException e){
			//con.close();
			//this.systemRepository.closeConnection(con);
			throw e;
		}
			
		// delete records
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(AlinousSystemRepository.SESSION_ID, sessionId);
		queryParams.put(AlinousSystemRepository.MODULE_PATH, targetPath.getStringPath());
		queryParams.put(AlinousSystemRepository.FILE_PATH, filePath);
		
		//queryParams.put(AlinousSystemRepository.MODULE_PATH, AlinousUtils.sqlEscape(targetPath.getStringPath()));
		//queryParams.put(AlinousSystemRepository.FILE_PATH, AlinousUtils.sqlEscape(filePath));
		
		List<String> locked = null;
		try{
			context.getPrecompile().setSqlSentence(select4delete);
			locked = this.systemRepository.existsLockMultiple(context, con, AlinousSystemRepository.VALUES_TABLE, queryParams);
			context.getPrecompile().setSqlSentence(null);
			
			//this.systemRepository.deleteRecord(context, con, AlinousSystemRepository.VALUES_TABLE, queryParams,
			//		select4delete, delete);
			
		}catch(AlinousException e){
			this.systemRepository.closeConnection(con);
			throw e;
		}catch(DataSourceException e){
			this.systemRepository.closeConnection(con);
			throw e;
		}
		
		//int loopCnt = 0;
		int loopKey = 0;
		int recordLengthCounter = 0;
		
		List<Record> recordsList = new ArrayList<Record>();
		StringWriter sw = new StringWriter();
		CsvWriter csvWriter = new CsvWriter(sw);
		Iterator<IPathElement> it = variableMap.keySet().iterator();
		while(it.hasNext()){
			IPathElement pathElement = it.next();
			ExecResultRecord val = variableMap.get(pathElement);
			
			recordLengthCounter += val.getName().length() + val.getType().length() + val.getValueType().length()
									+ val.getValue().length() + "'','','',''\n".length();
			
			/* making CSV
			AlinousDebug.debugOut("--------- loading cache ----------------------------------");
			AlinousDebug.debugOut("********** else **************** val.getName() : " + val.getName());
			AlinousDebug.debugOut("********** else **************** val.getType() : " + val.getType());
			AlinousDebug.debugOut("********** else **************** val.getValueType() : " + val.getValueType());
			AlinousDebug.debugOut("********** else **************** val.getValue() : " + val.getValue());
			*/
			
			if(recordLengthCounter > AlinousCore.MAX_ONE_VALUE_RECORD){
				String csvStr = sw.getBuffer().toString();
				
				// insert records
				Record rec = new Record();
				rec.addFieldValue(AlinousSystemRepository.SESSION_ID, sessionId, IScriptVariable.TYPE_STRING);
				rec.addFieldValue(AlinousSystemRepository.MODULE_PATH, targetPath.getStringPath(), IScriptVariable.TYPE_STRING);
				rec.addFieldValue(AlinousSystemRepository.FILE_PATH, filePath, IScriptVariable.TYPE_STRING);
				
				rec.addFieldValue(AlinousSystemRepository.LOOP_KEY, makeLoopKey(loopKey), IScriptVariable.TYPE_STRING);
				rec.addFieldValue(AlinousSystemRepository.VALUE, csvStr, IScriptVariable.TYPE_STRING);
				rec.addFieldValue(AlinousSystemRepository.CREATE_TIME, AlinousUtils.getNowString(), IScriptVariable.TYPE_STRING);
				
				recordsList.add(rec);
				
			//	loopCnt = 0;
				try {
					sw.close();
				} catch (IOException ignore) {ignore.printStackTrace();}
				
				sw = new StringWriter();
				csvWriter = new CsvWriter(sw);
				
				loopKey++;
				recordLengthCounter = val.getName().length() + val.getType().length() + val.getValueType().length()
							+ val.getValue().length() + "'','','',''\n".length();;
			}
			
			try {
				csvWriter.addField(val.getName());
				csvWriter.addField(val.getType());
				csvWriter.addField(val.getValueType());
				csvWriter.addField(val.getValue());
				csvWriter.endRecord();
			} catch (IOException e) {
				con.close();
				throw new AlinousException(e, "Failed in making csv");
			}
			
			/*
			if(loopCnt > 64){ 
				String csvStr = sw.getBuffer().toString();
				
				// insert records
				Record rec = new Record();
				rec.addFieldValue(AlinousSystemRepository.SESSION_ID, this.sessionId, IScriptVariable.TYPE_STRING);
				rec.addFieldValue(AlinousSystemRepository.MODULE_PATH, targetPath.getStringPath(), IScriptVariable.TYPE_STRING);
				rec.addFieldValue(AlinousSystemRepository.FILE_PATH, filePath, IScriptVariable.TYPE_STRING);
				
				rec.addFieldValue(AlinousSystemRepository.LOOP_KEY, makeLoopKey(loopKey), IScriptVariable.TYPE_STRING);
				rec.addFieldValue(AlinousSystemRepository.VALUE, csvStr, IScriptVariable.TYPE_STRING);
				rec.addFieldValue(AlinousSystemRepository.CREATE_TIME, AlinousUtils.getNowString(), IScriptVariable.TYPE_STRING);
				
				recordsList.add(rec);
				
				loopCnt = 0;
				try {
					sw.close();
				} catch (IOException ignore) {ignore.printStackTrace();}
				
				sw = new StringWriter();
				csvWriter = new CsvWriter(sw);
				
				loopKey++;
			}
			else{
				loopCnt++;
			}
			
			*/
			
		}
		
		// add record finally
		String csvStr = sw.getBuffer().toString();
		
		if(csvStr != null && !csvStr.equals("") && recordLengthCounter != 0){
			Record rec = new Record();
			rec.addFieldValue(AlinousSystemRepository.SESSION_ID, sessionId, IScriptVariable.TYPE_STRING);
			rec.addFieldValue(AlinousSystemRepository.MODULE_PATH, targetPath.getStringPath(), IScriptVariable.TYPE_STRING);
			rec.addFieldValue(AlinousSystemRepository.FILE_PATH, filePath, IScriptVariable.TYPE_STRING);
			
			rec.addFieldValue(AlinousSystemRepository.LOOP_KEY, makeLoopKey(loopKey), IScriptVariable.TYPE_STRING);
			rec.addFieldValue(AlinousSystemRepository.VALUE, csvStr, IScriptVariable.TYPE_STRING);
			rec.addFieldValue(AlinousSystemRepository.CREATE_TIME, AlinousUtils.getNowString(), IScriptVariable.TYPE_STRING);
			recordsList.add(rec);
			
			try {
				sw.close();
			} catch (IOException ignore) {ignore.printStackTrace();}
			
			loopKey++;
		}
		
		loopKey = 0;
		Iterator<Record> itInsert = recordsList.iterator();
		while(itInsert.hasNext()){
			Record rec = itInsert.next();
			ArrayList<Record> instmpList = new ArrayList<Record>();
			instmpList.add(rec);
			
			try {
				if(!locked.contains(makeLoopKey(loopKey))){
					context.getPrecompile().setSqlSentence(insert);
					this.systemRepository.insertRecord(context, con, AlinousSystemRepository.VALUES_TABLE, instmpList);
					context.getPrecompile().setSqlSentence(null);
				}else{
					queryParams.put(AlinousSystemRepository.LOOP_KEY, makeLoopKey(loopKey));
					
					context.getPrecompile().setSqlSentence(ExecResultCache.update);
					this.systemRepository.updateRecord(context, con, AlinousSystemRepository.VALUES_TABLE, instmpList, queryParams);
					context.getPrecompile().setSqlSentence(null);
				}
				
				loopKey++;
			} catch (DataSourceException e) {
				this.systemRepository.closeConnection(con);
				throw new AlinousException(e, "Failed to insert record"); // i18n
			}
		}
		
		try{
			// delete more than loop key
			queryParams.put(AlinousSystemRepository.LOOP_KEY, makeLoopKey(loopKey));
			
			this.systemRepository.deleteRecordWithoutLock(context, con, AlinousSystemRepository.VALUES_TABLE, queryParams,
							delete);
			con.commit(null);
		}finally{
			//this.systemRepository.closeConnection(con);
			con.close();
			
			// 
			context.getUnit().getConnectionManager().clearCurrentDataSrc(context);
		}
	}
	
	private String makeLoopKey(int loop)
	{
		return String.format("%06d", loop);
		
	}
	
	public void getResultCache(PostContext context, VariableRepository cache, InnerModulePath targetPath, String filePath,
			String sessionId)
					throws AlinousException
	{
		//VariableRepository cache = new VariableRepository();
		
		HashMap<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(AlinousSystemRepository.SESSION_ID, sessionId);
		queryParams.put(AlinousSystemRepository.MODULE_PATH, targetPath.getStringPath());
		queryParams.put(AlinousSystemRepository.FILE_PATH, filePath);
		
		List<Record> recordsList = null;
		try {
			context.getPrecompile().setSqlSentence(ExecResultCache.select4Update);
			
			recordsList = this.systemRepository.selectRecord(context, AlinousSystemRepository.VALUES_TABLE, queryParams);
		
			context.getPrecompile().setSqlSentence(null);
		} catch (DataSourceException e) {
			throw new AlinousException(e, "Failed to read ExecutionResultCache"); // i18n
		}
		
		// customize here
		Iterator<Record> it = recordsList.iterator();
		while(it.hasNext()){
			Record rec = it.next();
			
			String namePath = null;
			String type = null;
			String valueType = null;
			String value = null;
			
			String csvStr = rec.getFieldValue(AlinousSystemRepository.VALUE);
			StringReader reader = new StringReader(csvStr);
			CsvReader csvReader = new CsvReader(reader);
			CsvRecord csvRec = null;
			
			do{
				try {
					csvRec = csvReader.readRecord();
					if(csvRec.isEmpty()){
						break;
					}
					
					namePath = csvRec.get(0);
					type = csvRec.get(1);
					valueType = csvRec.get(2);
					value = csvRec.get(3);
					
				} catch (CsvException e) {
					throw new AlinousException(e, "Failed in reading cache.");
				} catch (IOException e) {
					throw new AlinousException(e, "Failed in reading cache.");
				}
				
				// Ignore empty array. Array will be created automatically by path-name
				if(type.equals(IScriptVariable.TYPE_HASH)){
					cache.putValue(namePath, value, valueType, context);
				}
				else if(type.equals(IScriptVariable.TYPE_ARRAY)){
					/* input Array
					AlinousDebug.debugOut("--------- loading cache ----------------------------------");
					AlinousDebug.debugOut("************************** namePath : " + namePath);
					AlinousDebug.debugOut("************************** type : " + type);
					AlinousDebug.debugOut("************************** valueType : " + valueType);
					AlinousDebug.debugOut("************************** value : " + value);
					*/
				}else{
					/* input Array
					AlinousDebug.debugOut("--------- loading cache ----------------------------------");
					AlinousDebug.debugOut("********** else **************** namePath : " + namePath);
					AlinousDebug.debugOut("********** else **************** type : " + type);
					AlinousDebug.debugOut("********** else **************** valueType : " + valueType);
					AlinousDebug.debugOut("********** else **************** value : " + value);
					*/
				}
				
				
			}while(!csvRec.isEmpty());
			
		}
		
		
		//return cache;
	}


	public AlinousSystemRepository getSystemRepository()
	{
		return systemRepository;
	}

}
