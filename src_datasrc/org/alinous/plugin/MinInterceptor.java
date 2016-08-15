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
package org.alinous.plugin;

import java.util.ArrayList;
import java.util.List;

import org.alinous.plugin.openec.OpenEcSetupper;


public class MinInterceptor extends NumberInterceptor{
	
	private List<String> tableList = new ArrayList<String>();
	
	public MinInterceptor()
	{
		OpenEcSetupper.setup(this.tableList);
	}
	
	@Override
	protected int getMaxRecord()
	{
		return 100;
	}

	@Override
	protected boolean isUnlimitedTable(String tableName)
	{
		if(tableName == null){
			return false;
		}
		
		String upperName = tableName.toUpperCase();
		boolean isUnlimit = this.tableList.contains(upperName);
		if(isUnlimit){
			return true;
		}
		
		if(upperName.startsWith("CUSTOM_MULTIPLEVALUE_")){
			return true;
		}
		if(upperName.startsWith("CUSTOM_TABLE")){
			return true;
		}
		
		return false;
	}
}
