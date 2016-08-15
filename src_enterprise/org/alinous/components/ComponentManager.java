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
package org.alinous.components;

import org.alinous.AlinousCore;
import org.alinous.components.tree.NodeTreeManager;
import org.alinous.expections.AlinousException;

public class ComponentManager
{
	public static final String KEY_NODE_TREE = "NODETREE";
	
	private AlinousCore alinousCore;
	
	private NodeTreeManager nodeTreeManager;
	
	public ComponentManager(AlinousCore core) throws AlinousException
	{
		this.alinousCore = core;
		
		this.nodeTreeManager = new NodeTreeManager();
		this.nodeTreeManager.setAlinousCore(this.alinousCore);
		this.nodeTreeManager.initComponent(core.getDataSourceManager());
	}

	public IAlinousComponent getComponentManager(String key)
	{
		IAlinousComponent cmp = null;
		
		if(key.toUpperCase().equals(KEY_NODE_TREE)){
			cmp =  this.nodeTreeManager;
		}
		
		return cmp;
	}
}
