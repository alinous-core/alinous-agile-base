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
package org.alinous;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.alinous.cloud.AlinousCloudManager;
import org.alinous.cloud.file.AlinousFile;
import org.alinous.cloud.file.AlinousFileInputStream;
import org.alinous.components.tree.DoctypeConfig;
import org.alinous.components.tree.NodeConfig;
import org.alinous.components.tree.NodeTreeConfig;
import org.alinous.datasrc.DataSourceConfig;
import org.alinous.datasrc.DataSourceConfigCollection;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ConfigException;
import org.alinous.filter.HttpCacheConfig;
import org.alinous.filter.ServiceFilterConfig;
import org.alinous.filter.SessionIdRewriteConfig;
import org.alinous.lucene.LuceneConfig;
import org.alinous.lucene.LuceneInstanceConfig;
import org.alinous.net.mail.AlinousMailConfig;
import org.alinous.repository.SystemRepositoryConfig;
import org.alinous.security.SecurityConfig;
import org.alinous.security.Zone;
import org.alinous.sql.config.SqlConfig;
import org.alinous.test.coverage.CoverageConfig;
import org.alinous.web.filter.FilterConfig;
import org.alinous.web.filter.FilterZone;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class AlinousConfig
{
	public static final String ALINOUS_CONFIG = "alinous-config.xml";
	public static final String DATA_SOURCE_CONFIG = "datasources";
	public static final String SYSTEM = "system";
	public static final String SYSTEM_REPOSITORY = "system-datastore";
	public static final String DEFAULT_REPOSITORY = "default-datastore";
	public static final String NOT_FOUND_PAGE = "not-found-page";
	
	public static final String MAIL = "mail";
	public static final String SERVER = "server";
	public static final String PORT = "port";
	public static final String LANG_ENCODE = "lang-encode";
	public static final String AUTH = "auth";
	public static final String METHOD = "method";
	public static final String USER = "user";
	public static final String PASS = "pass";
	public static final String POP_AUTH = "pop-auth";
	
	public static final String BASIC_AUTH = "basic-auth";
	public static final String RELM = "relm";
	public static final String RELM_DATASRC = "datastore";
	public static final String RELM_TABLE = "table";
	public static final String RELM_USERS = "users";
	public static final String RELM_PASSWORDS = "passwords";
	public static final String RELM_ROLES = "roles";
	public static final String ZONES = "zones";
	public static final String ZONE = "zone";
	public static final String AREA = "area";
	public static final String ROLES = "roles";
	public static final String ERROR_PAGE = "error-page";
	
	public static final String FORM_AUTH = "form-auth";
	public static final String LOGIN_FORM = "login";
	public static final String CONFIRM_FORM = "confirm";
	
	public static final String FILTER = "filter";;
	public static final String IN = "in";
	public static final String OUT = "out";
	
	public static final String TREENODE = "treenodes";
	public static final String NODE = "node";
	public static final String ID = "id";
	public static final String DISPLAY_NEME = "display-name";
	public static final String DOC_TYPE = "doc-type";
	public static final String EDITPAGE = "editpage";
	public static final String SHOWPAGE = "showpage";
	public static final String DELETEPAGE = "deletepage";
	public static final String FOLDER_IMAGE = "folderimg";
	
	public static final String LUCENE = "lucene";
	public static final String INSTANCE = "instance";
	public static final String SETUPPER = "setupper";
	public static final String TMPPATH = "tmppath";
	public static final String BASEPATH = "basepath";
	public static final String DATASRC = "datasrc";
	public static final String TABLE = "table";
	public static final String PARAMS = "params";
	
	public static final String VIRTUAL_HOST = "virtualhost";
	public static final String VIRTUAL_SERVER = "server";
	public static final String ALINOUS_HOME = "alinoushome";
	public static final String HOSTS = "hosts";
	
	public static final String ADMIN_PASSWORD = "admin-password";
	
	public static final String SERVICE_FILTER = "service-filter";
	public static final String SESSION_ID = "sessionid";
	
	public static final String SQL_CONFIG = "sql";
	public static final String SQL_CONSTRAINS = "constrains";
	public static final String SQL_CONSTRAIN = "constrain";
	public static final String SQL_PARAM = "param";
	public static final String SQL_TRIGGERS = "triggers";
	public static final String SQL_TABLE = "table";
	public static final String SQL_TIMING = "timing";
	public static final String SQL_TRIGGER = "trigger";
	public static final String SQL_MODULE = "module";
	public static final String SQL_FUNCTIONCALL = "functioncall";
	
	public static final String TEST = "test";
	public static final String COVERAGE = "coverage";
	public static final String INCLUDE = "include";
	public static final String EXCLUDE = "exclude";
	
	public static final String HTTP_CACHE = "http-cache-control";
	public static final String HTTP_CACHE_CONTROL = "control";
	public static final String HTTP_CACHE_CONTROL_MATCH = "match";
	
	private String alinousHome;
	private DataSourceConfigCollection dataSourceConfigs;
	private SystemRepositoryConfig systemRepositoryConfig;
	private AlinousMailConfig mailconfig;
	private SecurityConfig securityConfig;
	private FilterConfig filterConfig;
	private NodeTreeConfig nodeTreeConfig = new NodeTreeConfig();
	private LuceneConfig luceneConfig = new LuceneConfig();
	private VirtualHostConfigCollection virtualHostConfig = new VirtualHostConfigCollection();
	private ServiceFilterConfig serviceFilterConfig;
	private SessionIdRewriteConfig sessionIdRewriteConfig;
	private SqlConfig sqlConfig;
	private CoverageConfig coverageConfig;
	private HttpCacheConfig cacheConfig = new HttpCacheConfig();
	
	public AlinousConfig(String alinousHome) throws AlinousException
	{
		AlinousFile file = new AlinousFile(alinousHome);
		this.alinousHome = file.getPath();
		
		if(this.alinousHome == null){
			throw new AlinousException("ALINOUS_HOME is unset."); // i18n			
		}
	}

	@SuppressWarnings("rawtypes")
	public void parseConfig(AlinousCore core) throws AlinousException
	{
		AlinousCloudManager cloudMgr = AlinousCloudManager.getInstance();
		
		String configPath = AlinousUtils.getAbsolutePath(alinousHome, ALINOUS_CONFIG);
		configPath = configPath.replaceAll("\n", "");
		configPath = configPath.replaceAll("//", "/");
		
		//System.out.println("Config file is : " + configPath);
		
		AlinousFile configFile = new AlinousFile(configPath);
		
	//	System.out.println("alinousHome : " + alinousHome);
		
		
		if(!configFile.exists()){
			throw new ConfigException("alinous-config.xml does not exists. : " + configPath); // i18n
		}
		
		Document doc = null;
		AlinousFileInputStream inStream = null;
		try {
			if(!cloudMgr.isCoudEnabled()){
				doc = new SAXBuilder().build(configFile);
			}
			else{
				inStream = new AlinousFileInputStream(configFile);
				doc = new SAXBuilder().build(inStream);
			}
			
		} catch (JDOMException e) {
			throw new AlinousException(e, null);
		} catch (IOException e) {
			throw new AlinousException(e, "alinous-config.xml is broken");
		}
		
		Element root = doc.getRootElement();
		
		Element debugRoot = root.getChild("debug");
		if(AlinousCore.debug(null) && debugRoot != null){
			root = debugRoot;
			AlinousDebug.debugOut(core, "use <debug> configuration in alinous-config.xml");
		}
		
		
		List dataSrcList = root.getChildren(DATA_SOURCE_CONFIG);
		parseDataSourceConfig(dataSrcList);
		
		// get system tag
		Element systemEl = root.getChild(SYSTEM);
		if(systemEl == null){
			throw new ConfigException("<" + SYSTEM + "> does not exists."); //i18n
		}
		
		parseSystemConfig(systemEl);
		
		// get mail Tag
		Element mailTag = root.getChild(MAIL);
		if(mailTag != null){
			parseMailConfig(mailTag);
		}
		
		// get BASIC_AUTH
		Element basicAuth = root.getChild(BASIC_AUTH);
		if(basicAuth != null){
			parseBasicAuthConfig(basicAuth);
		}
		
		// getFilterConfig
		Element filter = root.getChild(FILTER);
		if(filter != null){
			parseFilter(filter);
		}
		
		// getTreeNodeConfig
		Element treeNode = root.getChild(TREENODE);
		if(treeNode != null){
			parseTreeNode(treeNode);
		}
		
		// get Lucene config
		Element lucene = root.getChild(LUCENE);
		if(lucene != null){
			parseLuceneConfig(lucene);
		}
		
		// get Virtual host config
		Element hosts = root.getChild(HOSTS);
		if(hosts != null){
			perseHostConfig(hosts);
		}
		
		// service filter
		Element serviceFilter = root.getChild(SERVICE_FILTER);
		if(serviceFilter != null){
			parseServiceFilterConfig(serviceFilter);
		}
		
		// sessionid
		Element sessionId = root.getChild(SESSION_ID);
		if(sessionId != null){
			parseSessionIdRewriteConfig(sessionId);
		}
		
		// sql Config
		Element pslConfig = root.getChild(SQL_CONFIG);
		if(pslConfig != null){
			parseSqlConfig(pslConfig);
		}
		
		// test Config
		Element testConfig = root.getChild(TEST);
		if(testConfig != null){
			parseTestConfig(testConfig);
		}
		
		// Http Cache Control
		Element httpCache = root.getChild(HTTP_CACHE);
		if(httpCache != null){
			parseHttpCacheConfig(httpCache);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void parseHttpCacheConfig(Element httpCache)
	{
		List<Element> childList = httpCache.getChildren(HTTP_CACHE_CONTROL);
		Iterator<Element> it = childList.iterator();
		while(it.hasNext()){
			Element controlElement = it.next();
			
			String match = controlElement.getAttributeValue(HTTP_CACHE_CONTROL_MATCH);
			String ctrl = controlElement.getText();
			
			this.cacheConfig.addCtrl(match, ctrl);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void parseTestConfig(Element testConfig)
	{
		Element coverageElement = testConfig.getChild(COVERAGE);
		if(coverageElement == null){
			return;
		}
		
		this.coverageConfig = new CoverageConfig();
		
		List<Element> childList = coverageElement.getChildren(INCLUDE);
		Iterator<Element> it = childList.iterator();
		while(it.hasNext()){
			Element includeElement = it.next();
			
			this.coverageConfig.parseIncludeFolder(includeElement);
		}
		
		childList = coverageElement.getChildren(EXCLUDE);
		it = childList.iterator();
		while(it.hasNext()){
			Element excludeElement = it.next();
			
			this.coverageConfig.parseExcludeFolder(excludeElement);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void parseSqlConfig(Element pslConfig)
	{
		this.sqlConfig = new SqlConfig();
		
		List<Element> childList = pslConfig.getChildren(SQL_CONSTRAINS);
		Iterator<Element> it = childList.iterator();
		while(it.hasNext()){
			Element constrains = it.next();
			
			List<Element> constrainsList = constrains.getChildren(SQL_CONSTRAIN);
			Iterator<Element> itch = constrainsList.iterator();
			while(itch.hasNext()){
				Element constrainElement = itch.next();
				
				this.sqlConfig.addConstrainFromElement(constrainElement);
			}
		}
		
		childList = pslConfig.getChildren(SQL_TRIGGERS);
		it = childList.iterator();
		while(it.hasNext()){
			Element triggers = it.next();
			
			List<Element> triggersList = triggers.getChildren(SQL_TRIGGERS);
			Iterator<Element> itch = triggersList.iterator();
			while(itch.hasNext()){
				Element triggerElement = itch.next();
				
				this.sqlConfig.addTriggerFromElement(triggerElement);
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private void parseSessionIdRewriteConfig(Element sessionId)
	{
		this.sessionIdRewriteConfig = new SessionIdRewriteConfig();
		
		List<Element> childList = sessionId.getChildren(AREA);
		Iterator<Element> it = childList.iterator();
		while(it.hasNext()){
			Element area = it.next();
			
			String param = area.getAttributeValue("param");
			
			this.sessionIdRewriteConfig.addArea(area.getText(), param);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private void parseServiceFilterConfig(Element serviceFilter)
	{
		String clazz = serviceFilter.getChild("filter-class").getText();
		this.serviceFilterConfig = new ServiceFilterConfig();
		this.serviceFilterConfig.setServiceFilterClass(clazz);
		
		Element paramsEl = serviceFilter.getChild("params");
		Iterator<Element> it = paramsEl.getChildren().iterator();
		while(it.hasNext()){
			Element param = it.next();
			
			this.serviceFilterConfig.getParams().put(param.getName(), param.getText());
		}
	}
	
	@SuppressWarnings("unchecked")
	private void perseHostConfig(Element hosts) throws ConfigException
	{
		this.virtualHostConfig = new VirtualHostConfigCollection();
		
		List<Element> nodeList = hosts.getChildren(VIRTUAL_HOST);
		Iterator<Element> it = nodeList.iterator();
		while(it.hasNext()){
			Element nodeEl = it.next();
			
			VirtualHostConfig vConfig = new VirtualHostConfig();
			
			Element vServerEl = nodeEl.getChild(VIRTUAL_SERVER);
			if(vServerEl == null){
				throw new ConfigException("<" + VIRTUAL_SERVER + "> does not exists in lucene config."); //i18n
			}
			vConfig.setHostName(vServerEl.getText());
			
			Element alinousHomeEl = nodeEl.getChild(ALINOUS_HOME);
			if(alinousHomeEl == null){
				throw new ConfigException("<" + ALINOUS_HOME + "> does not exists in lucene config."); //i18n
			}
			vConfig.setAlinousHome(alinousHomeEl.getText());
			
			this.virtualHostConfig.addVirtualHostConfig(vConfig);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void parseLuceneConfig(Element lucene) throws ConfigException
	{
		this.luceneConfig = new LuceneConfig();
		
		List<Element> nodeList = lucene.getChildren(INSTANCE);
		Iterator<Element> it = nodeList.iterator();
		while(it.hasNext()){
			Element nodeEl = it.next();
			
			LuceneInstanceConfig instConfig = new LuceneInstanceConfig();

			instConfig.setAlinousHome(this.alinousHome);
			
			Element idEl = nodeEl.getChild(ID);
			if(idEl == null){
				throw new ConfigException("<" + ID + "> does not exists in lucene config."); //i18n
			}
			instConfig.setId(idEl.getText());
			
			Element setUpperEl = nodeEl.getChild(SETUPPER);
			if(setUpperEl == null){
				throw new ConfigException("<" + SETUPPER + "> does not exists in lucene config."); //i18n
			}
			instConfig.setSetupper(setUpperEl.getText());
			
			Element tmpPathEl = nodeEl.getChild(TMPPATH);
			if(tmpPathEl == null){
				throw new ConfigException("<" + TMPPATH + "> does not exists in lucene config."); //i18n
			}
			instConfig.setTmpPath(tmpPathEl.getText());
			
			Element basePathEl = nodeEl.getChild(BASEPATH);
			if(basePathEl == null){
				throw new ConfigException("<" + BASEPATH + "> does not exists in lucene config."); //i18n
			}
			instConfig.setBasePath(basePathEl.getText());
			
			Element datasrcEl = nodeEl.getChild(DATASRC);
			if(datasrcEl == null){
				throw new ConfigException("<" + DATASRC + "> does not exists in lucene config."); //i18n
			}
			instConfig.setDataSrc(datasrcEl.getText());
			
			Element tableEl = nodeEl.getChild(TABLE);
			if(tableEl == null){
				throw new ConfigException("<" + TABLE + "> does not exists in lucene config."); //i18n
			}
			instConfig.setTableName(tableEl.getText());
			
			// params
			Element paramEl = nodeEl.getChild(PARAMS);
			
			if(paramEl != null){
				List<Element> paramElements = paramEl.getChildren();
				Iterator<Element> paramIt = paramElements.iterator();
				while(paramIt.hasNext()){
					Element param = paramIt.next();
					instConfig.addProperty(param.getName(), param.getText());
				}
			}
			
			this.luceneConfig.addConfig(instConfig);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void parseTreeNode(Element treeNode) throws ConfigException
	{
		this.nodeTreeConfig = new NodeTreeConfig();
		
		List<Element> nodeList = treeNode.getChildren(NODE);
		Iterator<Element> it = nodeList.iterator();
		while(it.hasNext()){
			Element nodeEl = it.next();
			
			NodeConfig config = new NodeConfig();
			
			Element idEl = nodeEl.getChild(ID);
			if(idEl == null){
				throw new ConfigException("<" + ID + "> does not exists in NODETREE."); //i18n
			}
			config.setId(idEl.getText());
			
			Element dataSrcEl = nodeEl.getChild(RELM_DATASRC);
			if(dataSrcEl == null){
				throw new ConfigException("<" + RELM_DATASRC + "> does not exists in NODETREE."); //i18n
			}
			config.setDatastore(dataSrcEl.getText());
			
			
			List<Element> docTypes = nodeEl.getChildren(DOC_TYPE);
			Iterator<Element> nodeIt = docTypes.iterator();
			while(nodeIt.hasNext()){
				Element docTypeEl = nodeIt.next();
				
				parseDocType(docTypeEl, config);
			}
			
			
			this.nodeTreeConfig.addNode(config);
		}
	}
	
	private void parseDocType(Element docTypeEl, NodeConfig config) throws ConfigException
	{
		DoctypeConfig docConfig = new DoctypeConfig();
		
		Element idEl = docTypeEl.getChild(ID);
		if(idEl == null){
			throw new ConfigException("<" + ID + "> does not exists in doc-type."); //i18n
		}
		docConfig.setId(idEl.getText());
		
		Element dispNameEl = docTypeEl.getChild(DISPLAY_NEME);
		if(dispNameEl != null){
			docConfig.setDisplayName(dispNameEl.getText());
		}else{
			docConfig.setDisplayName(docConfig.getId());
		}
		
		Element editPageEl = docTypeEl.getChild(EDITPAGE);
		if(editPageEl == null){
			throw new ConfigException("<" + EDITPAGE + "> does not exists in doc-type."); //i18n
		}
		docConfig.setEditPage(editPageEl.getText());
		
		Element showPageEl = docTypeEl.getChild(SHOWPAGE);
		if(showPageEl == null){
			throw new ConfigException("<" + SHOWPAGE + "> does not exists in doc-type."); //i18n
		}
		docConfig.setShowPage(showPageEl.getText());
		
		Element deletePageEl = docTypeEl.getChild(DELETEPAGE);
		if(deletePageEl == null){
			throw new ConfigException("<" + DELETEPAGE + "> does not exists in doc-type."); //i18n
		}
		docConfig.setDeletePage(deletePageEl.getText());
		
		
		Element folderImgEl = docTypeEl.getChild(FOLDER_IMAGE);
		if(folderImgEl != null && folderImgEl.getText() != null && !folderImgEl.getText().equals("")){
			docConfig.setFolderImage(folderImgEl.getText());
		}
		
		config.addDocType(docConfig);
		if(config.getDefaultDoctype() == null){
			config.setDefaultDoctype(docConfig);
		}
	}
	
	
	@SuppressWarnings({ "unchecked", "unused" })
	private void parseFilter(Element filter) throws ConfigException
	{
		this.filterConfig = new FilterConfig();
		
		List<Element> zoneList = filter.getChildren(ZONE);
		Iterator<Element> it = zoneList.iterator();
		while(it.hasNext()){
			Element zoneEl = it.next();
			
			Element areaEl = zoneEl.getChild(AREA);
			
			if(areaEl == null){
				continue;
			}
			FilterZone zone = new FilterZone();
			String areaText = areaEl.getText();
			if(!areaText.endsWith("/")){
				areaText = areaText + "/";
			}
			zone.setArea(areaText);
			
			if(areaEl == null){
				throw new ConfigException("<" + AREA + "> does not exists."); //i18n
			}
			
			Element inEl = zoneEl.getChild(IN);
			if(inEl != null){
				String inFilter = inEl.getText();
				
				zone.setInFilterClass(inFilter);
			}
			
			Element outEl = zoneEl.getChild(OUT);
			if(outEl != null){
				String outFilter = outEl.getText();
				
				zone.setOutFilterClass(outFilter);
			}
			
			// add zone
			this.filterConfig.addFilterZone(zone);
		}
		
	}
	
	private void parseBasicAuthConfig(Element basicAuth) throws ConfigException
	{
		this.securityConfig = new SecurityConfig();
		
		Element relmEl = basicAuth.getChild(RELM);
		if(relmEl != null){
			// throw new ConfigException("<" + RELM + "> does not exists."); //i18n
			
			Element datasourceEl = relmEl.getChild(RELM_DATASRC);
			if(datasourceEl == null){
				throw new ConfigException("<" + RELM_DATASRC + "> does not exists."); //i18n
			}
			this.securityConfig.setRelmDataSource(datasourceEl.getText());
			
			Element tableEl = relmEl.getChild(RELM_TABLE);
			if(tableEl == null){
				throw new ConfigException("<" + RELM_TABLE + "> does not exists."); //i18n
			}
			this.securityConfig.setRelmTable(tableEl.getText());
			
			Element usersEl = relmEl.getChild(RELM_USERS);
			if(usersEl == null){
				throw new ConfigException("<" + RELM_USERS + "> does not exists."); //i18n
			}
			this.securityConfig.setRelmUsers(usersEl.getText());
			
			Element passEl = relmEl.getChild(RELM_PASSWORDS);
			if(passEl == null){
				throw new ConfigException("<" + RELM_PASSWORDS + "> does not exists."); //i18n
			}
			this.securityConfig.setRelmPasswords(passEl.getText());
			
			Element rolesEl = relmEl.getChild(RELM_ROLES);
			if(rolesEl == null){
				throw new ConfigException("<" + RELM_ROLES + "> does not exists."); //i18n
			}
			this.securityConfig.setRelmRoles(rolesEl.getText());
		}
		

		
		// Zones
		Element zonesEl = basicAuth.getChild(ZONES);
		if(zonesEl != null){
			parseZones(zonesEl);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void parseZones(Element zonesEl) throws ConfigException
	{
		Iterator<Element> it = (Iterator<Element>) zonesEl.getChildren(ZONE).iterator();
		while(it.hasNext()){
			Element zoneEl = it.next();
			
			Zone zone = new Zone();
			
			Element areaEl = zoneEl.getChild(AREA);
			if(areaEl == null){
				throw new ConfigException("<" + AREA + "> does not exists."); //i18n
			}
			zone.setArea(areaEl.getText());
			
			Element rolesEl = zoneEl.getChild(ROLES);
			if(rolesEl == null){
				throw new ConfigException("<" + ROLES + "> does not exists."); //i18n
			}
			zone.setRoles(rolesEl.getText());
			
			Element errPageEl = zoneEl.getChild(ERROR_PAGE);
			if(errPageEl != null){
				zone.setErrorPage(errPageEl.getText());
			}
			
			// for Form Authentication
			Element formAuthEl = zoneEl.getChild(FORM_AUTH);
			if(formAuthEl != null){
				parseFormAuth(formAuthEl, zone);
			}
			
			this.securityConfig.addZone(zone);			
		}
	}
	
	private void parseFormAuth(Element formAuthEl, Zone zone) throws ConfigException
	{
		Element loginEl = formAuthEl.getChild(LOGIN_FORM);
		if(loginEl == null){
			throw new ConfigException("<" + LOGIN_FORM + "> does not exists."); //i18n
		}
		zone.setLoginForm(loginEl.getText());
		
		Element confirmEl = formAuthEl.getChild(CONFIRM_FORM);
		if(confirmEl == null){
			throw new ConfigException("<" + LOGIN_FORM + "> does not exists."); //i18n
		}
		zone.setConfirmForm(confirmEl.getText());
	}
	
	private void parseMailConfig(Element mailTag) throws ConfigException
	{
		this.mailconfig = new AlinousMailConfig();
		
		// debug or not
		Attribute debugAttr = mailTag.getAttribute("debug");
		if(debugAttr != null){
			String strDebug = debugAttr.getValue().toLowerCase();
			if(strDebug.equals("true")){
				this.mailconfig.setDebug(true);
			}
		}
		
		Attribute debugReceiver = mailTag.getAttribute("receiver");
		if(debugReceiver != null){
			String strReceiver = debugReceiver.getValue().toLowerCase();
			this.mailconfig.setDebugReceiver(strReceiver);
		}
		
		Element server = mailTag.getChild(SERVER);
		if(server == null){
			throw new ConfigException("<" + SERVER + "> does not exists."); //i18n
		}
		this.mailconfig.setServer(server.getText());
		
		Element portEl = mailTag.getChild(PORT);
		if(portEl == null){
			throw new ConfigException("<" + PORT + "> does not exists."); //i18n
		}
		
		int po = 0;
		try{
			po = Integer.parseInt(portEl.getText());
		}catch(Throwable e){
			throw new ConfigException("<" + PORT + "> must be a number."); //i18n
		}
		this.mailconfig.setPort(po);
		
		Element langEncEl = mailTag.getChild(LANG_ENCODE);
		if(langEncEl != null){
			this.mailconfig.setLangCode(langEncEl.getText());
		}
		
		Element authEl = mailTag.getChild(AUTH);
		if(authEl != null){
			mailAuth(authEl);
		}
		
		
		// popconfig
		Element popEl = mailTag.getChild(POP_AUTH);
		if(popEl != null){
			popAuth(popEl);
		}
	}
	
	private void popAuth(Element popEl) throws ConfigException
	{
		Element methodEL = popEl.getChild(METHOD);
		if(methodEL == null){
			throw new ConfigException(POP_AUTH + " <" + METHOD + "> does not exists."); //i18n
		}
		this.mailconfig.setPopMethod(methodEL.getText());
		
		Element userEl = popEl.getChild(USER);
		if(userEl == null){
			throw new ConfigException(POP_AUTH + " <" + USER + "> does not exists."); //i18n
		}
		this.mailconfig.setPopUser(userEl.getText());
		
		Element passEl = popEl.getChild(PASS);
		if(passEl == null){
			throw new ConfigException(POP_AUTH + " <" + PASS + "> does not exists."); //i18n
		}
		this.mailconfig.setPopPass(passEl.getText());
		
		Element srvEL = popEl.getChild("server");
		if(srvEL != null){
			this.mailconfig.setServer(srvEL.getText());
		}
	}
	
	private void mailAuth(Element authEl) throws ConfigException
	{
		Element methodEL = authEl.getChild(METHOD);
		if(methodEL == null){
			throw new ConfigException("<" + METHOD + "> does not exists."); //i18n
		}
		this.mailconfig.setAuthMethod(methodEL.getText());
		
		Element userEl = authEl.getChild(USER);
		if(userEl == null){
			throw new ConfigException("<" + USER + "> does not exists."); //i18n
		}
		this.mailconfig.setAuthUser(userEl.getText());
		
		Element passEl = authEl.getChild(PASS);
		if(passEl == null){
			throw new ConfigException("<" + PASS + "> does not exists."); //i18n
		}		
		this.mailconfig.setAuthPass(passEl.getText());
		
		Element domainEl = authEl.getChild("domain");
		if(domainEl != null){
			this.mailconfig.setAuthDomain(domainEl.getText());
		}
	}
	
	
	private void parseSystemConfig(Element systemEl) throws ConfigException
	{
		// get system-datastore
		Element systemDataStore = systemEl.getChild(SYSTEM_REPOSITORY);
		if(systemDataStore == null){
			throw new ConfigException("<" + SYSTEM_REPOSITORY + "> does not exists."); //i18n
		}
		
		String id = systemDataStore.getAttributeValue("id");
		if(id == null){
			throw new ConfigException("id attribute in <" + SYSTEM_REPOSITORY + "> does not exists."); //i18n
		}
		
		this.systemRepositoryConfig = new SystemRepositoryConfig();
		this.systemRepositoryConfig.setSystemSrc(id);
		
		
		// default data store
		Element defaultDataStore = systemEl.getChild(DEFAULT_REPOSITORY);
		if(defaultDataStore == null){
			throw new ConfigException("<" + DEFAULT_REPOSITORY + "> does not exists."); //i18n
		}
		
		id = defaultDataStore.getAttributeValue("id");
		if(id == null){
			throw new ConfigException("id attribute in <" + DEFAULT_REPOSITORY + "> does not exists."); //i18n
		}
		
		// upload
		Element uploadEl = systemEl.getChild("upload");
		if(uploadEl != null){
			this.systemRepositoryConfig.setUploadMaxsize(uploadEl.getAttributeValue("maxsize"));
		}else{
			this.systemRepositoryConfig.setUploadMaxsize("10000000");
		}
		
		// parallel
		Element parallelEl = systemEl.getChild("parallel");
		if(parallelEl != null){
			String threads = parallelEl.getAttributeValue("threads");
			if(threads != null && AlinousUtils.isNumber(threads)){
				this.systemRepositoryConfig.setThreads(Integer.parseInt(threads));
			}
			
		}	
		
		// notfound
		Element notfoundEl = systemEl.getChild("notfound");
		if(notfoundEl != null){
			this.systemRepositoryConfig.setNotfoundPage(notfoundEl.getAttributeValue("page"));
		}
		
		this.systemRepositoryConfig.setDefaultSrc(id);
		
		// encoding
		Element encodeingEl = systemEl.getChild("encoding");
		if(encodeingEl != null){
			this.systemRepositoryConfig.setEncoding(encodeingEl.getText());
		}else{
			// default value is utf-8
			this.systemRepositoryConfig.setEncoding("utf-8");
		}
		
		// admin-pass
		Element adminPassEl = systemEl.getChild("adminpass");
		if(adminPassEl != null){
			this.systemRepositoryConfig.setAdminPass(adminPassEl.getText());
		}
		
		// serial
		Element serialEl = systemEl.getChild("serial");
		if(serialEl != null){
			this.systemRepositoryConfig.setSerial(serialEl.getText());
		}
		
		// errpage
		Element errpageEl = systemEl.getChild("errpage");
		if(errpageEl != null){
			this.systemRepositoryConfig.setErrpage(errpageEl.getText());
		}
		
		// parse job
		Element jobEl = systemEl.getChild("jobs");
		if(jobEl != null){
			this.systemRepositoryConfig.setupJobSchedule(jobEl);
		}
	}
	
	private void parseDataSourceConfig(List<?> dataSrcList)
	{
		Iterator<?> it = dataSrcList.iterator();
		while(it.hasNext()){
			Element el = (Element)it.next();
			String id = el.getAttributeValue("id");
			String clazz = el.getAttributeValue("class");
			String strResultupper = el.getAttributeValue("resultupper");
			
			Element elconnect = el.getChild("connect");
			Element eluser = el.getChild("user");
			Element elpass = el.getChild("pass");
			
			String connect = elconnect != null ? elconnect.getText() : null;
			String maxclients = elconnect.getAttributeValue("maxclients");
			String user = eluser != null ? eluser.getText() : null;
			String pass = elpass != null ? elpass.getText() : null;
			
			
			DataSourceConfig newConfig = new DataSourceConfig();
			newConfig.setId(id);
			newConfig.setClazz(clazz);
			
			if(maxclients != null && AlinousUtils.isNumber(maxclients)){
				newConfig.setMaxclients(Integer.parseInt(maxclients));
			}
			
			boolean blResultUpper = false;
			if(strResultupper != null && strResultupper.toLowerCase().equals("true")){
				blResultUpper = true;
			}
			newConfig.setResultUpper(blResultUpper);
			
			newConfig.setUri(connect);
			newConfig.setUser(user);
			newConfig.setPass(pass);
			
			if(this.dataSourceConfigs == null){
				this.dataSourceConfigs = new DataSourceConfigCollection();
			}
			this.dataSourceConfigs.addDataSourceConfig(newConfig);			
		}
	}
	
	public void writeAsString(PrintWriter wr)
	{
		wr.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		wr.println("<alinous-config>");
		
		// System
		if(this.systemRepositoryConfig != null){
			this.systemRepositoryConfig.writeAsString(wr);
		}
		
		// datasources
		if(this.dataSourceConfigs != null){
			this.dataSourceConfigs.writeAsString(wr);
		}
		
		// authentication
		if(this.securityConfig != null){
			this.securityConfig.writeAsString(wr);
		}
		
		// mail
		if(this.mailconfig != null){
			this.mailconfig.writeAsString(wr);
		}
		
		// filter
		if(this.filterConfig != null){
			this.filterConfig.writeAsString(wr);
		}
		
		// nodeTree
		if(this.nodeTreeConfig != null){
			this.nodeTreeConfig.writeAsString(wr);
		}
		
		// lucene
		if(this.luceneConfig != null){
			this.luceneConfig.writeAsString(wr);
		}
		
		// virtual hosts
		if(this.virtualHostConfig != null){
			this.virtualHostConfig.writeAsString(wr);
		}
		
		// service-filter
		if(this.serviceFilterConfig != null){
			this.serviceFilterConfig.writeAsString(wr);
		}
		
		// sessionId
		if(this.sessionIdRewriteConfig != null){
			this.sessionIdRewriteConfig.writeAsString(wr);
		}
		
		// test coverate
		if(this.coverageConfig != null){
			this.coverageConfig.writeAsString(wr);
		}
		
		wr.println("</alinous-config>");
	}
	
	
	// Getter and Setter
	public DataSourceConfigCollection getDataSourceConfig()
	{
		return dataSourceConfigs;
	}
	
	
	
	public DataSourceConfigCollection getDataSourceConfigs()
	{
		return dataSourceConfigs;
	}


	public void setDataSourceConfigs(DataSourceConfigCollection dataSourceConfigs)
	{
		this.dataSourceConfigs = dataSourceConfigs;
	}


	public AlinousMailConfig getMailconfig()
	{
		return mailconfig;
	}


	public void setMailconfig(AlinousMailConfig mailconfig)
	{
		this.mailconfig = mailconfig;
	}


	public void setFilterConfig(FilterConfig filterConfig)
	{
		this.filterConfig = filterConfig;
	}


	public void setSecurityConfig(SecurityConfig securityConfig)
	{
		this.securityConfig = securityConfig;
	}


	public void setSystemRepositoryConfig(
			SystemRepositoryConfig systemRepositoryConfig)
	{
		this.systemRepositoryConfig = systemRepositoryConfig;
	}


	public SystemRepositoryConfig getSystemRepositoryConfig()
	{
		return this.systemRepositoryConfig;
	}
	
	public AlinousMailConfig getMailConfig()
	{
		return this.mailconfig;
	}
	
	public SecurityConfig getSecurityConfig()
	{
		return this.securityConfig;
	}


	public FilterConfig getFilterConfig()
	{
		return filterConfig;
	}


	public NodeTreeConfig getNodeTreeConfig()
	{
		return nodeTreeConfig;
	}


	public void setNodeTreeConfig(NodeTreeConfig nodeTreeConfig)
	{
		this.nodeTreeConfig = nodeTreeConfig;
	}


	public LuceneConfig getLuceneConfig()
	{
		return luceneConfig;
	}

	public void setLuceneConfig(LuceneConfig luceneConfig)
	{
		this.luceneConfig = luceneConfig;
	}


	public VirtualHostConfigCollection getVirtualHostConfig()
	{
		return virtualHostConfig;
	}


	public void setVirtualHostConfig(VirtualHostConfigCollection virtualHostConfig)
	{
		this.virtualHostConfig = virtualHostConfig;
	}


	public ServiceFilterConfig getServiceFilterConfig()
	{
		return serviceFilterConfig;
	}


	public SessionIdRewriteConfig getSessionIdRewriteConfig() {
		return sessionIdRewriteConfig;
	}

	public SqlConfig getSqlConfig()
	{
		return sqlConfig;
	}


	public void setSqlConfig(SqlConfig sqlConfig)
	{
		this.sqlConfig = sqlConfig;
	}

	public CoverageConfig getCoverageConfig()
	{
		return coverageConfig;
	}

	public HttpCacheConfig getCacheConfig() {
		return cacheConfig;
	}

	public void setCacheConfig(HttpCacheConfig cacheConfig) {
		this.cacheConfig = cacheConfig;
	}
	
	
}
