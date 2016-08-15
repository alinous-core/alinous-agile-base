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
package org.alinous.debug.command.client;

import java.util.Map;

import org.alinous.debug.AlinousDebugManager;
import org.alinous.debug.AlinousServerDebugHttpResponse;
import org.alinous.exec.pages.PostContext;

public interface IClientRequest {
	public static final String CMD_TERMINATE = "CMD_TERMINATE";
	public static final String CMD_STATUS_THREAD = "CMD_STATUS_THREAD";
	public static final String CMD_CLEAR_BREAKPOINTS = "CMD_CLEAR_BREAKPOINTS";
	public static final String CMD_SETUP_ALL_BREAKPOINTS = "CMD_SETUP_ALL_BREAKPOINTS";
	public static final String CMD_ADD_BREAKPOINTS = "CMD_ADD_BREAKPOINTS";
	public static final String CMD_RESUME = "CMD_RESUME";
	public static final String CMD_STEP_OVER = "CMD_STEP_OVER";
	public static final String CMD_STEP_IN= "CMD_STEP_IN";
	public static final String CMD_STEP_RETURN= "CMD_STEP_RETURN";
	
	public String getCommand();
	
	/**
	 * Convert this request into POST parameters
	 * @return
	 */
	public Map<String, String> getParamMap();
	
	/**
	 * Convert POST parameters from instance
	 * @param params
	 */
	public void importParams(Map<String, String> params);
	
	/**
	 * Server side operation
	 * @param debugManager
	 * @return
	 */
	public AlinousServerDebugHttpResponse executeRequest(AlinousDebugManager debugManager, PostContext context);
	
}
