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
import java.util.Iterator;

import org.alinous.datasrc.types.Record;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;

public class InstanceIndexWriter
{
	private Analyzer analyzer;
	private IndexWriter writer;
	
	public InstanceIndexWriter(Analyzer analyzer)
	{
		this.analyzer = analyzer;
	}
	
	public void beginWrite(Directory directory, boolean create) throws CorruptIndexException, LockObtainFailedException, IOException
	{
		try{
			this.writer = new IndexWriter(directory, this.analyzer, create);
		}catch(Throwable e){
			this.writer = new IndexWriter(directory, this.analyzer, true);
		}
	}
	
	public void endWrite() throws CorruptIndexException, IOException
	{
		this.writer.close();
	}
	
	public void addRecord(Record rec) throws CorruptIndexException, IOException
	{
		Document doc = new Document();
		
		Iterator<String> it = rec.getMap().keySet().iterator();
		while(it.hasNext()){
			String fldName = it.next();
			String text = rec.getFieldValue(fldName);
			
			if(text == null){
				continue;
			}
			
			doc.add(new Field(fldName, text, Field.Store.YES,
			        Field.Index.TOKENIZED));	
		}
		
		this.writer.addDocument(doc);
	}
	
	public void optimize() throws CorruptIndexException, IOException
	{
		this.writer.optimize();
	}
}
