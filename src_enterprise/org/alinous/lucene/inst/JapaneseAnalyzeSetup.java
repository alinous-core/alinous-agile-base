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
package org.alinous.lucene.inst;

import java.io.IOException;
import java.util.Map;

import org.alinous.AlinousUtils;
import org.alinous.cloud.file.AlinousFile;
import org.alinous.lucene.ILuceneInstanceSetupper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ja.JapaneseAnalyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class JapaneseAnalyzeSetup implements ILuceneInstanceSetupper
{
	public static final String SEN_HOME = "sen-home";
	
	private Map<String, String> props;
	private String baseDir;
	
	public Analyzer getAnalyzer()
	{
		String alinousHome = this.props.get("ALINOUS_HOME");
		
		String senHome = this.props.get(SEN_HOME);
		if(senHome != null){
			if(alinousHome != null){
				senHome = AlinousUtils.getAbsolutePath(alinousHome, senHome);
			}
			
			return new JapaneseAnalyzer(senHome);
		}

		return new JapaneseAnalyzer();
	}

	public Directory getDirectory() throws IOException
	{
		AlinousFile baseDirFile = new AlinousFile(this.baseDir);
		
		Directory dir = null;
		dir = FSDirectory.getDirectory(baseDirFile);

		return dir;
	}

	public void setProperties(Map<String, String> prop)
	{
		this.props = prop;
	}

	public void setBaseDir(String baseDir)
	{
		this.baseDir = baseDir;
	}
	
}
