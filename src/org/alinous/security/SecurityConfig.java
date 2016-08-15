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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SecurityConfig
{
	private String relmDataSource;
	private String relmTable;
	private String relmUsers;
	private String relmPasswords;
	private String relmRoles;
	
	private List<Zone> zones = new CopyOnWriteArrayList<Zone>();
	private List<String> exceptionalPages = new ArrayList<String>();

	public void addZone(Zone zn)
	{
		this.zones.add(zn);
		
		if(zn.getErrorPage() != null){
			exceptionalPages.add(zn.getErrorPage().toLowerCase());
		}
		if(zn.getLoginForm() != null){
			exceptionalPages.add(zn.getLoginForm().toLowerCase());
		}
		if(zn.getConfirmForm() != null){
			exceptionalPages.add(zn.getConfirmForm().toLowerCase());
		}
	}
	
	public Iterator<Zone> getZoneIterator()
	{
		return this.zones.iterator();
	}
	
	public Zone getZoneByArea(String area)
	{
		Iterator<Zone> it = this.zones.iterator();
		while(it.hasNext()){
			Zone zo = it.next();
			
			if(area.equals(zo.getArea())){				
				return zo;
			}
		}
		
		return null;
	}
	
	public void removeZoneByArea(String area)
	{
		Iterator<Zone> it = this.zones.iterator();
		while(it.hasNext()){
			Zone zo = it.next();
			
			if(area.equals(zo.getArea())){
				this.zones.remove(zo);
			}
		}
	}
	
	
	
	public boolean isExceptionalPage(String path)
	{
		return this.exceptionalPages.contains(path.toLowerCase());
	}
	
	public String getRelmDataSource()
	{
		return relmDataSource;
	}

	public void setRelmDataSource(String relmDataSource)
	{
		this.relmDataSource = relmDataSource;
	}

	public String getRelmPasswords()
	{
		return relmPasswords;
	}

	public void setRelmPasswords(String relmPasswords)
	{
		this.relmPasswords = relmPasswords;
	}

	public String getRelmRoles()
	{
		return relmRoles;
	}

	public void setRelmRoles(String relmRoles)
	{
		this.relmRoles = relmRoles;
	}

	public String getRelmTable()
	{
		return relmTable;
	}

	public void setRelmTable(String relmTable)
	{
		this.relmTable = relmTable;
	}

	public String getRelmUsers()
	{
		return relmUsers;
	}

	public void setRelmUsers(String relmUsers)
	{
		this.relmUsers = relmUsers;
	}

	public List<Zone> getZones()
	{
		return zones;
	}

	public void writeAsString(PrintWriter wr)
	{
		wr.println("	<basic-auth>");
		
		// relm
		writeRelm(wr);
		
		// zones
		writeZones(wr);
		
		wr.println("	</basic-auth>");
	}
	
	private void writeZones(PrintWriter wr)
	{
		wr.println("		<zones>");
		
		Iterator<Zone> it = this.zones.iterator();
		while(it.hasNext()){
			Zone zone = it.next();
			
			wr.println("			<zone>");
			
			wr.print("				<area>");
			wr.print(zone.getArea());
			wr.println("</area>");
			
			wr.print("				<roles>");
			wr.print(zone.getRoles());
			wr.println("</roles>");
			
			if(zone.getErrorPage() != null){
				wr.print("				<error-page>");
				wr.print(zone.getErrorPage());
				wr.println("</error-page>");
			}
			
			if(zone.isUseForm()){
				wr.println("				<form-auth>");
				
				wr.print("					<login>");
				wr.print(zone.getLoginForm());
				wr.println("</login>");
				
				wr.print("					<confirm>");
				wr.print(zone.getConfirmForm());
				wr.println("</confirm>");
				
				
				wr.println("				</form-auth>");
			}
			
			wr.println("			</zone>");
			
		}
		
		wr.println("		</zones>");
	}
	
	private void writeRelm(PrintWriter wr)
	{
		if(this.relmDataSource == null && this.relmTable == null &&
				this.relmUsers == null && this.relmPasswords == null 
				&& this.relmRoles == null){
			return;
		}
		
		wr.println("		<relm>");
		
		wr.print("			<datastore>");
		wr.print(this.relmDataSource);
		wr.println("</datastore>");
		
		wr.print("			<table>");
		wr.print(this.relmTable);
		wr.println("</table>");	
		
		wr.print("			<users>");
		wr.print(this.relmUsers);
		wr.println("</users>");	
		
		wr.print("			<passwords>");
		wr.print(this.relmPasswords);
		wr.println("</passwords>");	
		
		wr.print("			<roles>");
		wr.print(this.relmRoles);
		wr.println("</roles>");
		
		wr.println("		</relm>");
	}
}
