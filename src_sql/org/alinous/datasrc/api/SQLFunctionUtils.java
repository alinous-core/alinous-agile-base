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

package org.alinous.datasrc.api;

import org.alinous.AlinousCore;
import org.alinous.cloud.file.AlinousFile;

public class SQLFunctionUtils
{
	public static String getAbsolutePath(String alinousPath, AlinousCore core)
	{
		String tmp = null;
		if(!core.getHome().endsWith(AlinousFile.separator)){
			tmp = core.getHome() + AlinousFile.separator;
		}else{
			tmp = core.getHome();
		}
		
		if(alinousPath.startsWith(AlinousFile.separator)){
			tmp = tmp + alinousPath.substring(1);
		}else{
			tmp = tmp + alinousPath;
		}
		
		return tmp;
	}
}
