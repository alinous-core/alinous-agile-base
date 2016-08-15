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
package org.alinous.lucene;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.expections.AlinousException;
import org.alinous.lucene.extif.IDataStoreProvidor;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.LockObtainFailedException;

public class LuceneManager
{
	private LuceneConfig config;
	private Map<String, LuceneInstance> instRegMap = new HashMap<String, LuceneInstance>();
	private IDataStoreProvidor dataStoreProvidor;
		
	public LuceneManager(IDataStoreProvidor dataStoreProvidor)
	{
		this.dataStoreProvidor = dataStoreProvidor;
	}
	
	public void init(LuceneConfig config)
	{
		this.config = config;
		
		// init instances
		Iterator<LuceneInstanceConfig> it = this.config.iterator();
		while(it.hasNext()){
			LuceneInstanceConfig instConfig = it.next();
			
			LuceneInstance inst = new LuceneInstance(instConfig, this.dataStoreProvidor);
			
			try {
				inst.initInstance();
			} catch (AlinousException e) {
				e.printStackTrace();
				continue;
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			
			// succeeded in init
			this.instRegMap.put(inst.getName(), inst);
		}
	}
	
	public void initDirectory(String name) throws AlinousException
	{
		LuceneInstance inst = this.instRegMap.get(name);
		
		if(inst == null){
			throw new AlinousException("Failed in getting Lucene instance named " + name); // i18n
		}
		
		try {
			inst.initDirectory();
		} catch (CorruptIndexException e) {
			throw new AlinousException(e, "Failed while init directory " + name); // i18n
		} catch (LockObtainFailedException e) {
			throw new AlinousException(e, "Failed while init directory " + name); // i18n
		} catch (IOException e) {
			throw new AlinousException(e, "Failed while init directory " + name); // i18n
		}
		
	}
	
	public void syncTable(String name) throws AlinousException
	{
		LuceneInstance inst = this.instRegMap.get(name);
		
		if(inst == null){
			throw new AlinousException("Failed in getting Lucene instance named " + name); // i18n
		}
		
		try {
			inst.syncTable();
		} catch (DataSourceException e) {
			throw new AlinousException(e, "Failed while sync table " + name); // i18n
		}
	}
	
	public void optimize(String name) throws AlinousException
	{
		LuceneInstance inst = this.instRegMap.get(name);
		
		if(inst == null){
			throw new AlinousException("Failed in getting Lucene instance named " + name); // i18n
		}
		
		try {
			inst.optimize();
		} catch (CorruptIndexException e) {
			throw new AlinousException(e, "Failed while optimize " + name); // i18n
		} catch (IOException e) {
			throw new AlinousException(e, "Failed while optimize " + name); // i18n
		}
	}
	
	public List<Document> readRecords(String name, String queryString, String key, int limit, int offset) throws AlinousException
	{
		LuceneInstance inst = this.instRegMap.get(name);
		
		if(inst == null){
			throw new AlinousException("Failed in getting Lucene instance named " + name); // i18n
		}
		
		try {
			return inst.readIndex(queryString, key, limit, offset);
		} catch (CorruptIndexException e) {
			throw new AlinousException(e, "Failed in searching"); // i18n
		} catch (IOException e) {
			throw new AlinousException(e, "Failed in searching"); // i18n
		} catch (ParseException e) {
			throw new AlinousException(e, "Failed in searching"); // i18n
		}
		
	}
}
