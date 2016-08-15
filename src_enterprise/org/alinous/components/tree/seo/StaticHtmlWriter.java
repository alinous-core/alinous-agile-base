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

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.alinous.AlinousCore;
import org.alinous.exec.AccessExecutionUnit;
import org.alinous.exec.pages.IDesign;
import org.alinous.exec.pages.IParamValue;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.script.runtime.VariableRepository;

public class StaticHtmlWriter
{
	private AlinousCore core;
	
	public StaticHtmlWriter(AlinousCore core)
	{
		this.core = core;
	}
	
	public void writeHtml(String moduleName, Writer writer, PostContext oldContext, VariableRepository valRepo
			,Map<String, IParamValue> params, String sessionId) throws AlinousException, IOException
	{
		AccessExecutionUnit exec = null;
		PostContext context = null;
		try{
			exec = this.core.createAccessExecutionUnit(sessionId, oldContext);
			
			context = new PostContext(this.core, exec);
			context.setContextPath(oldContext.getContextPath());
			context.setServletPath(oldContext.getServletPath());
			context.setStatic(true);
			
			context.setRequestPath(moduleName);
			this.core.registerAlinousObject(context, moduleName);
			
			// params
			context.initParams(moduleName, params);
			
			IDesign design = null;
			design = exec.gotoPage(moduleName, context, valRepo);
			
			design.renderContents(context, valRepo, writer, 0);
		}finally{
			// Do not dispose. Because of reuse of the Connection Manager
			/*if(exec != null){
				exec.dispose();
			}*/
			
			context.dispose();
		}
	}
}
