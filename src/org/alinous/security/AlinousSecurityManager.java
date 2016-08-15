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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.alinous.AlinousConfig;
import org.alinous.AlinousCore;
import org.alinous.AlinousUtils;
import org.alinous.datasrc.AlinousDataSourceManager;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.datasrc.types.Record;
import org.alinous.exec.SessionController;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.AlinousSecurityException;
import org.alinous.repository.AlinousSystemRepository;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;

public class AlinousSecurityManager
{
	static final String USER_NAME_PATH = "SESSION.AUTH.USER";
	static final String USER_ROLE_PATH = "SESSION.AUTH.ROLES";
	static final String BASIC_USER_ROLE_PATH = "SESSION.BASIC.AUTH.USER";
	static final String BASIC_ROLES_ROLE_PATH = "SESSION.BASIC.AUTH.ROLES";
	
	
	private AlinousCore alinousCore;
	private SecurityRelmManager relm;
	private AlinousSystemRepository sysRepo;
	private AlinousConfig config;
	
	//private String name;
	//private String nameBasic;
	//private List<String> roles = new ArrayList<String>();
	//private List<String> basicRoles = new ArrayList<String>();
	
	
	public AlinousSecurityManager(AlinousCore alinousCore, AlinousDataSourceManager dataSourceManager,
			AlinousSystemRepository sysRepo,
			AlinousConfig config)
	{
		this.relm = new SecurityRelmManager(dataSourceManager, config);
		this.config = config;
		this.sysRepo = sysRepo;
	}
	
	public void init() throws AlinousException
	{
		SecurityConfig secConfig = this.config.getSecurityConfig();
		
		if(secConfig == null){
			return;
		}
		
		this.relm.initRelmTable();
	}
	
	
	public void checkSecurity(String path, AlinousConfig config, PostContext context) 
		throws AlinousException
	{
		SecurityConfig securityConfig = config.getSecurityConfig();
		
		path = path.replaceAll("\\\\", "/");
		
		// Security is not used.
		if(securityConfig == null){
			return;
		}
		
		if(isPassedPage(securityConfig, path)){
			return;
		}
		
		String innerPath = null;
		if(context != null){
			innerPath = context.getNextAction();
		}
		
		Zone zone = findLongestMatch(path, securityConfig, false);
		Zone zoneBasic = findLongestMatch(path, securityConfig, true);
		Zone innserZone = null;
		Zone innserZoneBasic = null;
		if(innerPath != null){
			String innerModuleName = AlinousUtils.getModuleName(innerPath);
			innserZone = findLongestMatch(innerModuleName, securityConfig, false);
			innserZoneBasic = findLongestMatch(innerModuleName, securityConfig, true);
		}
		
		if(zone == null && innserZone == null && zoneBasic == null && innserZoneBasic == null){
			return;
		}

		// Get Authentication INFO From session
		InitContext initCon = initAuthenticatedInfo(context);
		
		// If no Login info, require authentication
		if(initCon.getNameBasic() == null && !(zoneBasic == null && innserZoneBasic == null)){
			int reason = 0;
			Zone reasonZone = null;
			
			if(zoneBasic != null){
				reason = AlinousSecurityException.REASON_TOP;
				reasonZone = zoneBasic;
			}else if(innserZoneBasic != null){
				reason = AlinousSecurityException.REASON_INNER;
				reasonZone = innserZoneBasic;
			}
			
			throw new AlinousSecurityException(reason, reasonZone);
		}
		

		// check top(basic)
		if(innserZoneBasic != null && !innserZoneBasic.checkRole(initCon.getBasicRoles())){
			//If login info is wrong, require authentication
			throw new AlinousSecurityException(AlinousSecurityException.REASON_INNER, innserZoneBasic);
		}
		// check inner
		if(zoneBasic != null && !zoneBasic.checkRole(initCon.getBasicRoles())){
			//If login info is wrong, require authentication
			throw new AlinousSecurityException(AlinousSecurityException.REASON_TOP, zoneBasic);
		}
		
		// Form authentication
		if((initCon.getName() == null && !(zone == null && innserZone == null))){
			int reason = 0;
			Zone reasonZone = null;
			if(zone != null){
				reason = AlinousSecurityException.REASON_TOP;
				reasonZone = zone;
			}else if(innserZone != null){
				reason = AlinousSecurityException.REASON_INNER;
				reasonZone = innserZone;
			}

			throw new AlinousSecurityException(reason, reasonZone);
		}
				
		if(zone != null && !zone.checkRole(initCon.getRoles())){
			//If login info is wrong, require authentication
			throw new AlinousSecurityException(AlinousSecurityException.REASON_TOP, zone);
		}
		if(innserZone != null && !innserZone.checkRole(initCon.getRoles())){
			//If login info is wrong, require authentication
			throw new AlinousSecurityException(AlinousSecurityException.REASON_INNER, innserZone);
		}
	}
	
	private boolean isPassedPage(SecurityConfig securityConfig, String path)
	{
		Iterator<Zone> it = securityConfig.getZoneIterator();
		while(it.hasNext()){
			Zone zn = it.next();
			
			if(zn != null && zn.getLoginForm() != null){
				String loginMod = AlinousUtils.getModuleName(zn.getLoginForm());
				
				if(loginMod.equals(path)){
					return true;
				}
			}
			if(zn != null && zn.getConfirmForm() != null){
				String confirmMod = AlinousUtils.getModuleName(zn.getConfirmForm());

				if(confirmMod.equals(path)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	private InitContext initAuthenticatedInfo(PostContext context) throws AlinousException
	{
		String name = null;
		String nameBasic = null;
		List<String> roles = new ArrayList<String>();
		List<String> basicRoles = new ArrayList<String>();
		
		AlinousCore core = context.getCore();
		SessionController sessionControleler = new SessionController(core.getSystemRepository(),
				context.getSessionId(), context.getCore());
		
		VariableRepository valRepo = new VariableRepository();
		sessionControleler.updateSession(valRepo, context);
		
		// get Name
		IPathElement path = PathElementFactory.buildPathElement(USER_NAME_PATH);
		IScriptVariable val = valRepo.getVariable(path, context);
		
		if(val instanceof ScriptDomVariable){
			name = ((ScriptDomVariable)val).getValue();
		}
		
		// get Role
		path = PathElementFactory.buildPathElement(USER_ROLE_PATH);
		val = valRepo.getVariable(path, context);
		
		if(val instanceof ScriptDomVariable){
			roles.add(((ScriptDomVariable)val).getValue());
		}else if(val instanceof ScriptArray){
			ScriptArray ar = (ScriptArray)val;
			int size = ar.getSize();
			
			for(int i = 0; i < size; i++){
				IScriptVariable roleVal = ar.get(i);
				if(roleVal instanceof ScriptDomVariable){
					roles.add(((ScriptDomVariable)roleVal).getValue());
				}
			}
		}
		
		// get Basic Name nameBasic
		path = PathElementFactory.buildPathElement(BASIC_USER_ROLE_PATH);
		val = valRepo.getVariable(path, context);
		
		if(val instanceof ScriptDomVariable){
			nameBasic = ((ScriptDomVariable)val).getValue();
		}
		
		// get Basic Role
		path = PathElementFactory.buildPathElement(BASIC_ROLES_ROLE_PATH);
		val = valRepo.getVariable(path, context);
		
		if(val instanceof ScriptDomVariable){
			basicRoles.add(((ScriptDomVariable)val).getValue());
		}else if(val instanceof ScriptArray){
			ScriptArray ar = (ScriptArray)val;
			int size = ar.getSize();
			
			for(int i = 0; i < size; i++){
				IScriptVariable roleVal = ar.get(i);
				if(roleVal instanceof ScriptDomVariable){
					basicRoles.add(((ScriptDomVariable)roleVal).getValue());
				}
			}
		}
		
		return new InitContext(valRepo, name, nameBasic, roles, basicRoles);
	}
	
	class InitContext{
		VariableRepository repo;
		String name = null;
		String nameBasic = null;
		List<String> roles = null;
		List<String> basicRoles = null;
		
		public InitContext(VariableRepository repo, String name, String nameBasic, List<String> roles, List<String> basicRoles)
		{
			this.repo = repo;
			this.name = name;
			this.nameBasic = nameBasic;
			this.roles = roles;
			this.basicRoles = basicRoles;
		}

		public VariableRepository getRepo()
		{
			return repo;
		}

		public String getName()
		{
			return name;
		}

		public String getNameBasic()
		{
			return nameBasic;
		}

		public List<String> getRoles()
		{
			return roles;
		}

		public List<String> getBasicRoles()
		{
			return basicRoles;
		}
		
		
	}
	
	private boolean isExceptionalPage(String path, SecurityConfig securityConfig)
	{
		return securityConfig.isExceptionalPage(path + ".html");
	}
	
	public Zone findLongestMatch(String path, SecurityConfig securityConfig, boolean isBasic)
	{
		if(!path.startsWith("/")){
			path = "/" + path;
		}
		if(path.endsWith("/")){
			path = path + "index";
		}
		
		if(isExceptionalPage(path, securityConfig)){
			return null;
		}
		
		ZoneMatchContext currentContext = null;
		
		Iterator<Zone> it = securityConfig.getZones().iterator();
		while(it.hasNext()){
			Zone zone = it.next();
			
			ZoneMatchContext context =  zone.getContext();
			if(context.match(path)){
				if(isBasic && context.getZone().isUseForm()){
					continue;
				}
				if(!isBasic && !context.getZone().isUseForm()){
					continue;
				}
				
				if(currentContext == null){
					currentContext = context;
					continue;
				}
				
				if(currentContext.getNumSegments() < context.getNumSegments()){
					currentContext = context;
				}
			}
		}
		
		
		if(currentContext == null){
			return null;
		}
		
		return currentContext.getZone();
	}
	
	
	public void authenticate(String user, String password, String sessionId, AlinousCore alinousCore)
			throws AlinousException
	{
		if(user == null || password == null){
			return;
		}
		
		SecurityConfig secConfig = this.config.getSecurityConfig();
		if(secConfig == null){
			return;
		}
		
		String roleColumn = secConfig.getRelmRoles();
		if(roleColumn == null){
			return;
		}
		
		List<Record> recList = null;
		try {
			recList = this.relm.findRecords(user, password, null);
		} 
		catch (DataSourceException e) {
			throw new AlinousException(e, ""); //i18n
		}
		
		LinkedList<String> roles = new LinkedList<String>();
		
		Iterator<Record> it = recList.iterator();
		while(it.hasNext()){
			Record rec = it.next();
			
			String role = rec.getFieldValue(roleColumn);
			
			if(role != null && !role.equals("")){
				roles.add(role);
			}
		}
		
		updateSession(user, password, roles, sessionId, alinousCore);
	}
	
	private synchronized void updateSession(String user, String password, LinkedList<String> roles, 
			String sessionId, AlinousCore alinousCore) throws AlinousException
	{
		if(roles.size() <= 0){
			return;
		}
		if(sessionId == null){
			return;
		}
		
		SessionController sessionController = new SessionController(this.sysRepo, sessionId, alinousCore);
		VariableRepository valRepo = new VariableRepository();
		PostContext context  = new PostContext(this.alinousCore, null);
		
		sessionController.updateSession(valRepo, context);
		
		valRepo.putValue(BASIC_USER_ROLE_PATH, user, IScriptVariable.TYPE_STRING, null);
		
		int index = 0;
		Iterator<String> it = roles.iterator();
		while(it.hasNext()){
			String role = it.next();
			
			String pathStr = BASIC_ROLES_ROLE_PATH + "[" + index + "]";
			valRepo.putValue(pathStr, role, IScriptVariable.TYPE_STRING, null);
			
			index = index + 1;
		}
		
		sessionController.storeSession(context, valRepo);
		
		context.dispose();
	}
	
	public static void writeErrorPage(Writer writer) throws IOException
	{
		writer.append("<html>");
		writer.append("<head>");
		writer.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		writer.append("<title>Authentication Error</title>");
		writer.append("</head>");
		writer.append("<body>");
		
		writeErrorInner(writer);
		
		writer.append("</body>");
		writer.append("</html>");
		
	}
	
	public static void writeErrorInner(Writer writer) throws IOException
	{
		writer.append("<H1>Authentication Error</H1>");
		writer.append("<HR>");
		
		writer.append("The authentication has failed.<BR><BR><BR><BR>");
		
		writer.append("<HR>");
		writer.append("Alinous-Core");
	}

}
