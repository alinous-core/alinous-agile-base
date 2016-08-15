/**
 * CROSSFIRE JAPAN INCORPORATED
 * This source code is under GPL License.
 * info@crossfire.jp
 * Official web site
 * http://alinous.org
 * 
 *  Copyright (C) 2008 Tomohiro Iizuka
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
package org.alinous.html.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import org.alinous.AlinousCore;
import org.alinous.AlinousUtils;
import org.alinous.cloud.file.AlinousFile;
import org.alinous.cloud.file.AlinousFileInputStream;
import org.alinous.cloud.file.AlinousFileOutputStream;
import org.alinous.components.tree.seo.StaticHtmlWriter;
import org.alinous.exec.pages.IParamValue;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.script.runtime.VariableRepository;

public class HtmlOutputHtmlThread extends Thread
{
	private String tmpletePath;
	private String outPath;
	private String encode;
	private String sessionId;
	private AlinousCore alinousCore;
	private PostContext context;
	private Map<String, IParamValue> paramMap;
	
	public HtmlOutputHtmlThread(String tmpletePath, String outPath, String encode,
			String sessionId, AlinousCore alinousCore, PostContext context,
			Map<String, IParamValue> paramMap)
	{
		this.tmpletePath = tmpletePath;
		this.outPath = outPath;
		this.encode = encode;
		this.sessionId = sessionId;
		this.alinousCore = alinousCore;
		this.context = context;
		this.paramMap = paramMap;
	}

	@Override
	public void run()
	{
		// debug start
		if(AlinousCore.debug(this.context)){
			this.alinousCore.getAlinousDebugManager().startAlinousOperation(this.context);
		}
		
		try {
			doRun();
		} catch (ExecutionException e) {
			this.alinousCore.reportError(e);
		}finally{
			if(AlinousCore.debug(this.context)){
				this.alinousCore.getAlinousDebugManager().endAlinousOperation(this.context);
			}
			
		}
	}
	
	private String getTmpFileName()
	{
		StringBuffer buff = new StringBuffer();
		
		String tmpPath = AlinousUtils.getAbsolutePath(this.alinousCore.getHome(), "/tmp/");
		buff.append(tmpPath);
		
		String strOutPath = this.outPath.replaceAll("/", "_");
		strOutPath = strOutPath.replaceAll("\\\\", "_");
		buff.append(strOutPath);
		
		return buff.toString();
	}
	
	public void doRun() throws ExecutionException
	{
		Writer writer = null;
		OutputStream stream = null;
		String tmpPath = getTmpFileName();
		StaticHtmlWriter htmlWriter = new StaticHtmlWriter(this.context.getCore());
		
		try {
			AlinousFile file = new AlinousFile(tmpPath);
			stream = new AlinousFileOutputStream(file);
			writer = new OutputStreamWriter(stream, this.encode);
			
			String tmpleteModulePath = AlinousUtils.getModuleName(this.tmpletePath);
			
			htmlWriter.writeHtml(tmpleteModulePath, writer, this.context, new VariableRepository(), this.paramMap, this.sessionId);
		}catch (IOException e) {
			throw new ExecutionException(e, "Failed in generating HTML.");
		} catch (AlinousException e) {
			throw new ExecutionException(e, "Failed in generating HTML.");
		} finally{
			if(writer != null){
				try {
					writer.close();
					stream.close();
				} catch (IOException e) {e.printStackTrace();}
			}
		}
		
		this.outPath = AlinousUtils.getAbsolutePath(this.context.getCore().getHome(), this.outPath);
		AlinousFile outFile = new AlinousFile(this.outPath);
		AlinousFile file = new AlinousFile(tmpPath);
		AlinousFileInputStream inStream = null;
		try {
			if(outFile.exists()){
				outFile.delete();
			}
			stream = new AlinousFileOutputStream(outFile);
			
			inStream = new AlinousFileInputStream(file);
			
			int n = 0;
			byte[] buff = new byte[1024];
			do{
				n = inStream.read(buff, 0, buff.length);
				
				if(n > 0){
					stream.write(buff, 0, n);
				}
			}while(n > 0);
			
			
		} catch (FileNotFoundException e) {
			throw new ExecutionException(e, "Failed in generating HTML.");
		} catch (IOException e) {
			throw new ExecutionException(e, "Failed in generating HTML.");
		}finally{
			if(inStream != null){
				try {
					inStream.close();
				} catch (IOException e) {e.printStackTrace();	}
			}
			
			if(stream != null){
				try {
					stream.close();
				} catch (IOException e) {e.printStackTrace();	}
			}
			
			if(file.exists()){
				file.delete();
			}
		}
		
	}
	
	
}
