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

import java.util.Map;

import org.alinous.exec.pages.IParamValue;

public class InnserStatusContext {
	private boolean useVariableCache;
	private String nextModuleName;
	private boolean useFormValueCache;
	private boolean securityErrorDefaultPage;
	private Map<String, IParamValue> extraParams;
	
	public String getNextModuleName() {
		return nextModuleName;
	}
	public void setNextModuleName(String nextModuleName) {
		this.nextModuleName = nextModuleName;
	}
	public boolean isUseVariableCache() {
		return useVariableCache;
	}
	public void setUseVariableCache(boolean useVariableCache) {
		this.useVariableCache = useVariableCache;
	}
	public boolean isUseFormValueCache()
	{
		return useFormValueCache;
	}
	public void setUseFormValueCache(boolean useFormValueCache)
	{
		this.useFormValueCache = useFormValueCache;
	}
	public boolean isSecurityErrorDefaultPage()
	{
		return securityErrorDefaultPage;
	}
	public void setSecurityErrorDefaultPage(boolean securityErrorDefaultPage)
	{
		this.securityErrorDefaultPage = securityErrorDefaultPage;
	}
	public Map<String, IParamValue> getExtraParams()
	{
		return extraParams;
	}
	public void setExtraParams(Map<String, IParamValue> extraParams)
	{
		this.extraParams = extraParams;
	}
	
	
	
}
