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
package org.alinous.scan;

import java.io.File;
import java.io.FilenameFilter;

import org.alinous.cloud.file.AlinousFile;

public class DirectoryFilter implements FilenameFilter{

	public boolean accept(File dir, String name) {
		AlinousFile file = new AlinousFile(dir.getPath() + AlinousFile.separator + name);
		
		boolean bl = file.isDirectory();
		
		return bl;
	}


}
