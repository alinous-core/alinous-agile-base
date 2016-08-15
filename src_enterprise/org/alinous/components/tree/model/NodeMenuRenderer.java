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
package org.alinous.components.tree.model;

import java.io.IOException;
import java.io.Writer;

import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.script.runtime.VariableRepository;

public class NodeMenuRenderer
{
	public static void renderEditMenu(Writer wr, VariableRepository valRepo,
			PostContext context, NodeModel model, String spnId,  String divId
			, NodeTreeRenderModel renderer) throws IOException, DataSourceException, AlinousException
	{
		wr.write("<span id=\"" + spnId + "\"");
		
		SpanAttrWriter attrwr = new SpanAttrWriter();
		String url = context.getFilePath("/alinous-common/treenode/menu.jsp");
		
		MenuParamDataWriter dataWriter = new MenuParamDataWriter();
		dataWriter.initParam(context, model, renderer);
		String params = dataWriter.getParamData();
		
		StringBuffer script = new StringBuffer();
		script.append("alnsAjaxUtils.showPopup('");
		script.append(divId);
		script.append("', '" + spnId + "', '" + params + "', '" + url + "')");
				
		attrwr.addEventHandler("onclick", script.toString());
		attrwr.render(wr);		
		
		wr.write(">");
		
		String imgEdit = context.getFilePath("/alinous-common/treenode/img/write_obj.gif");
		wr.write("<img src=\"" + imgEdit + "\" border=\"0\">");
		
		wr.write("</span>");
	}
	
	public static void renderPositionMenu(Writer wr, VariableRepository valRepo,
			PostContext context, NodeModel model, String spnId,  String divId
			, NodeTreeRenderModel renderer) throws IOException, DataSourceException, AlinousException
	{
		wr.write("<span id=\"" + spnId + "\"");
		
		SpanAttrWriter attrwr = new SpanAttrWriter();
		String url = context.getFilePath("/alinous-common/treenode/posMenu.jsp");
		
		MenuParamDataWriter dataWriter = new MenuParamDataWriter();
		dataWriter.initParam(context, model, renderer);
		String params = dataWriter.getParamData();
		
		StringBuffer script = new StringBuffer();
		script.append("alnsAjaxUtils.showPopup('");
		script.append(divId);
		script.append("', '" + spnId + "', '" + params + "', '" + url + "')");
				
		attrwr.addEventHandler("onclick", script.toString());
		//attrwr.addEventHandler("onclick", "alert('test')");
		attrwr.render(wr);		
		
		wr.write(">");
		
		String imgPosEdit = context.getFilePath("/alinous-common/treenode/img/synced.gif");
		wr.write("<img src=\"" + imgPosEdit + "\" border=\"0\">");
		
		wr.write("</span>");
		
	}
	
}
