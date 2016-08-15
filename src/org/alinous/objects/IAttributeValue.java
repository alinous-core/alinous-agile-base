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
package org.alinous.objects;

import java.io.IOException;
import java.io.Writer;

import org.alinous.exec.pages.PostContext;
import org.alinous.script.runtime.VariableRepository;

public interface IAttributeValue
{
	public String toString();
	
	public String getValue();
	public void setValue(String value);
	
	public String getParsedValue(PostContext context, VariableRepository valRepo);
	public void renderContents(Writer wr, int n, PostContext context, VariableRepository valRepo) throws IOException;
	public void renderAdjustedUriContents(Writer wr, int n, PostContext context, VariableRepository valRepo) throws IOException;

	// Optimize
	public boolean isDynamic();
}
