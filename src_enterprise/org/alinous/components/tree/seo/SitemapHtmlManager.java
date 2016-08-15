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
package org.alinous.components.tree.seo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.alinous.cloud.file.AlinousFile;
import org.alinous.components.tree.NodeConfig;
import org.alinous.components.tree.model.NodeModel;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.expections.ExecutionException;

public class SitemapHtmlManager
{
	private String outDir;
	private String outPath;
	private Writer writer;
	private int level;
	
	public SitemapHtmlManager(String outPath, String outDir, String encode) throws FileNotFoundException, UnsupportedEncodingException
	{
		this.outDir = outDir;
		
		if(!outPath.endsWith("/")){
			outPath = outPath + "/";
		}
		
		this.outPath = outPath + "sitemap.html";
		
		AlinousFile file = new AlinousFile(this.outPath);
		this.writer = new PrintWriter(file, encode);
	}
	
	public void writeHeader() throws IOException
	{
		this.writer.write("<html>\n");
		this.writer.write("<body>\n");
		
		this.level = -1;
	}
	
	public void appendModel(NodeModel model, DataSrcConnection con, NodeConfig config) throws IOException, ExecutionException, DataSourceException
	{
		writeUlOpen(model.getLevel());
		writeUlClose(model.getLevel());
		
		printLevelTab();
		this.writer.write("<li>");
		
		if(model.getDocRef() != null && !model.getDocRef().equals("")){
			this.writer.write("<a href=\"");
			this.writer.write(getDocumentLoc(model, con, config));
			this.writer.write("\">");
		}
		
		this.writer.write(model.getTitle());
		
		if(model.getDocRef() != null && !model.getDocRef().equals("")){
			this.writer.write("</a>");
		}
		
		this.writer.write("</li>\n");
		
		
	}
	
	private String getDocumentLoc(NodeModel model, DataSrcConnection con, NodeConfig config) throws ExecutionException, DataSourceException
	{
		OutPathNamingManager manager = new OutPathNamingManager(con, config);
		
		String path = manager.getName(model, outDir);
		
		return path;
	}
	
	private void writeUlOpen(int curLevel) throws IOException
	{
		if(curLevel > this.level){
			printLevelTab();
			this.writer.append("<ul>\n");
			this.level++;
		}
	}
	
	public void writeUlClose(int curLevel) throws IOException
	{
		while(curLevel < this.level){
			printLevelTab();
			this.writer.append("</ul>\n");
			this.level--;
		}
	}
	
	private void printLevelTab() throws IOException
	{
		int i = this.level;
		while(i > 0){
			this.writer.write("\t");
			i--;
		}
	}
	
	public void writeFooter() throws IOException
	{
		writeUlClose(-1);
		
		this.writer.write("</body>\n");
		this.writer.write("</html>\n");
	}
	
	public void dispose()
	{
		if(writer == null){
			return;
		}
		try {
			this.writer.close();
		} catch (IOException e) {}
	}
	
	public String getOutPath()
	{
		return outPath;
	}
	
}
