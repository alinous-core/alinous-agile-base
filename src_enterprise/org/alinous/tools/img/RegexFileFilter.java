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
package org.alinous.tools.img;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;
/*
 * RegexFileFilter.java
 *
 * Created on 2006/08/10, 16:33
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author mnagaku
 */
public class RegexFileFilter implements FileFilter {
    boolean passDir = false;
    Pattern p;
    
    /** Creates a new instance of RegexFileFilter */
    public RegexFileFilter(String match, boolean passDir) {
        p = Pattern.compile(match);
        this.passDir = passDir;
    }
    
    public boolean accept(File pathname) {
        if(pathname.isDirectory())
            return passDir;
        else if(p.matcher(pathname.getName()).matches())
            return true;
        else
            return false;
    }
}
