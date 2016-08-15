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
package org.alinous.security;

import java.util.List;

public class Zone
{
	private String area;
	private String roles;
	private String errorPage;	
	
	private String loginForm;
	private String confirmForm;
	
	
	public ZoneMatchContext getContext()
	{
		return new ZoneMatchContext(this.area, this);
	}
	
	public boolean checkRole(List<String> rolesList)
	{
		if(this.roles == null){
			return false;
		}
		if(this.roles.trim().equals("*")){
			return true;
		}
		
		String roleArray[] = this.roles.split(",");
		for(int i = 0; i < roleArray.length; i++){
			String roleName = roleArray[i].trim();
			
			if(rolesList.contains(roleName)){
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isUseForm()
	{
		return this.loginForm != null;
	}
	
	public String getArea()
	{
		return area;
	}
	public void setArea(String area)
	{
		this.area = area;
	}
	public String getRoles()
	{
		return roles;
	}
	public void setRoles(String roles)
	{
		this.roles = roles;
	}
	public String getLoginForm()
	{
		return loginForm;
	}
	public void setLoginForm(String loginForm)
	{
		this.loginForm = loginForm;
	}

	public String getConfirmForm()
	{
		return confirmForm;
	}

	public void setConfirmForm(String confirmForm)
	{
		this.confirmForm = confirmForm;
	}

	public String getErrorPage()
	{
		return errorPage;
	}

	public void setErrorPage(String errorPage)
	{
		this.errorPage = errorPage;
	}
	
	
}
