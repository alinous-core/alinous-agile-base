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
package org.alinous.debug;

import org.alinous.exec.pages.PostContext;

public interface IThreadEventListner {
	public final static int REASON_BREAKPOINT = 1;
	public final static int REASON_STEP_IN = 2;
	public final static int REASON_STEP_OVER = 3;
	public final static int REASON_STEP_RETURN = 4;
	
	public void fireThreadAboutToSuspend(int reason, PostContext context);
	public void fireThreadResumed(PostContext context);
}
