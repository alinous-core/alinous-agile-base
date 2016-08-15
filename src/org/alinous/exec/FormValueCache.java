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
import org.alinous.exec.pages.ArrayParamValue;
import org.alinous.exec.pages.FormValues;
import org.alinous.exec.pages.IParamValue;
import org.alinous.exec.pages.PostContext;
import org.alinous.exec.pages.StringParamValue;
import org.alinous.expections.AlinousException;
import org.alinous.repository.AlinousSystemRepository;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.sql.DeleteSentence;
import org.alinous.script.sql.InsertSentence;
import org.alinous.script.sql.SelectSentence;

public class FormValueCache {
	private AlinousSystemRepository systemRepository;
//	private String sessionId;
//	private AlinousCore alinousCore;
	private static SelectSentence select4Update = new SelectSentence();
	private static InsertSentence insert = new InsertSentence();
	
	private static SelectSentence select4delete = new SelectSentence();
	private static DeleteSentence delete = new DeleteSentence();
	
	public FormValueCache(AlinousSystemRepository repo, String sessionId, AlinousCore alinousCore)
	{
		this.systemRepository = repo;
//		this.sessionId = sessionId;
//		this.alinousCore = alinousCore;
	}
	
	public void storeFormValue(PostContext context, InnerModulePath innerModPath, Map<String, IParamValue> params, 
			String filePath, String formId, String sessionId)
				throws AlinousException
	{
		//synchronized (this.alinousCore.getSesssionLockManager().getLock(sessionId)) {
			doStoreFormValue(context, innerModPath, params, filePath, formId, sessionId);
		//}
	}
	
	@SuppressWarnings("unused")
	public void doStoreFormValue(PostContext context, InnerModulePath innerModPath, Map<String, IParamValue> params, 
						String filePath, String formId, String sessionId)
			throws AlinousException
	{
		DataSrcConnection con = null;
		try {
			//con = this.systemRepository.getConnection();
			con = context.getUnit().getConnectionManager().connect(context.getCore().getConfig().getSystemRepositoryConfig().getSystemSrc(), context);
		} catch (DataSourceException e) {
			if(con != null){con.close();}
			throw new AlinousException(e, "Failed in connect on FormValue cache"); // i18n
		}
		
		try {
			con.begin(Connection.TRANSACTION_READ_COMMITTED);
		}catch(DataSourceException e){
			if(con != null){con.close();}
			throw new AlinousException(e, "Failed to begin TRX on FormValue"); // i18n
		}
		
		// delete last value
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(AlinousSystemRepository.SESSION_ID, sessionId);
		queryParams.put(AlinousSystemRepository.MODULE_PATH, innerModPath.getStringPath());
		queryParams.put(AlinousSystemRepository.FILE_PATH, filePath);
		
		try {
			this.systemRepository.deleteRecord(context, con, AlinousSystemRepository.FORM_VALUS_TABLE, queryParams,
					select4delete, delete);
		} catch (DataSourceException e) {
			if(con != null){con.close();}
			throw new AlinousException(e, "Failed to delete FromValueCache"); // i18n
		}
		
		// insert records
		Record rec = new Record();

		rec.addFieldValue(AlinousSystemRepository.SESSION_ID, sessionId, IScriptVariable.TYPE_STRING);
		rec.addFieldValue(AlinousSystemRepository.MODULE_PATH, innerModPath.getStringPath(), IScriptVariable.TYPE_STRING);
		rec.addFieldValue(AlinousSystemRepository.FILE_PATH, filePath, IScriptVariable.TYPE_STRING);

		List<Record> recordsList = new ArrayList<Record>();
		StringWriter sw = new StringWriter();
		CsvWriter csvWriter = new CsvWriter(sw);
		Iterator<String> it = params.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			IParamValue val = params.get(key);
			
			// store 
			try {
				csvWriter.addField(key);
				if(formId != null){
					csvWriter.addField(formId);
				}else{
					csvWriter.addField("");
				}
				csvWriter.addField(Integer.toString(val.getType()));
				csvWriter.addField(val.toString());
				csvWriter.endRecord();
				
				// store form values
				/*
				AlinousDebug.debugOut("--------- store cache ----------------------------------");
				AlinousDebug.debugOut("********** store **************** key : " + key);
				AlinousDebug.debugOut("********** store **************** val.getType() : " + val.getType());
				*/
			} catch (IOException e) {
				throw new AlinousException(e, "Failed in making csv");
			}
		}
		
		String csvStr = sw.getBuffer().toString();
		rec.addFieldValue(AlinousSystemRepository.VALUE, csvStr, IScriptVariable.TYPE_STRING);
		
		rec.addFieldValue(AlinousSystemRepository.CREATE_TIME, AlinousUtils.getNowString(), IScriptVariable.TYPE_STRING);
		recordsList.add(rec);
		
		try {
			context.getPrecompile().setSqlSentence(insert);
			systemRepository.insertRecord(context, con, AlinousSystemRepository.FORM_VALUS_TABLE, recordsList);
			context.getPrecompile().setSqlSentence(null);
		} catch (DataSourceException e) {
			if(con != null){con.close();}
			throw new AlinousException(e, "Failed to insert FromValueCache"); // i18n
		}
		
		try{
			con.commit(null);
		} catch (DataSourceException e) {
			throw new AlinousException(e, "Failed to delete FromValueCache"); // i18n
		}finally{
			if(con != null){con.close();}
			
			// 
			context.getUnit().getConnectionManager().clearCurrentDataSrc(context);
		}
		
	}
	
	public FormValues loadFormValues(PostContext context, InnerModulePath innerModPath,
									String filePath, String sessionId) throws AlinousException
	{
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put(AlinousSystemRepository.SESSION_ID, sessionId);
		queryParams.put(AlinousSystemRepository.MODULE_PATH, innerModPath.getStringPath());
		queryParams.put(AlinousSystemRepository.FILE_PATH, filePath);
		
		List<Record> records = null;
		try {
			context.getPrecompile().setSqlSentence(FormValueCache.select4Update);
			
			records = this.systemRepository.selectRecord(context, AlinousSystemRepository.FORM_VALUS_TABLE,
							queryParams);
			
			context.getPrecompile().setSqlSentence(null);
		} catch (DataSourceException e) {
			throw new AlinousException(e, "Failed to insert FromValueCache"); // i18n
		}
		
		
		FormValues formValues = new FormValues();
		Iterator<Record> it = records.iterator();
		while(it.hasNext()){
			Record rec = it.next();

			// start
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
				
				String key = csvRec.get(0);
				String formId = csvRec.get(1);
				String type = csvRec.get(2);
				String val = csvRec.get(3);
				
				Map<String, IParamValue> map = formValues.getMap(formId);
				
				if(type.equals("1")){
					map.put(key, new StringParamValue(AlinousUtils.escapeHtml(val)));
				}else{ // means "2"
					ArrayParamValue arrayParam = new ArrayParamValue(val);
					map.put(key, arrayParam);
				}
			}while(!csvRec.isEmpty());
			
		}
		
		return formValues;
	}

}
