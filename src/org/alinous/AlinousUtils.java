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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.alinous.cloud.file.AlinousFile;
import org.alinous.exec.pages.ArrayParamValue;
import org.alinous.exec.pages.IParamValue;
import org.alinous.exec.pages.StringParamValue;
import org.alinous.filter.SessionIdRewriteArea;
import org.alinous.objects.AlinousAttrs;
import org.alinous.script.runtime.IScriptVariable;



public class AlinousUtils
{
	public static final String SEPARATOR = "/";
	
	public static String escapeHtml(String formStr)
	{
		formStr = formStr.replaceAll("<", "&lt;");
		formStr = formStr.replaceAll(">", "&gt;");
		formStr = formStr.replaceAll("\"", "&quot;");
		
		return formStr;
	}
	
	public static String getSessionIdFromRequestPath(String urlStrng)
	{
		return "";
	}
	
	public static String addRewriteSessionString(String urlStrng, String sessionId, boolean isStatic, AlinousCore core)
	{
		if(isStatic){
			return urlStrng;
		}
			
		if(urlStrng == null || urlStrng.equals("") || urlStrng.toLowerCase().indexOf("javascript:") >= 0){
			return urlStrng;
		}
		if(urlStrng == null || urlStrng.equals("") || urlStrng.toLowerCase().indexOf("mailto:") >= 0){
			return urlStrng;
		}
		
		
		StringBuffer buffer = new StringBuffer();
		
		String splitUrl[] = urlStrng.split("\\?");
		
		String base = splitUrl[0];

		// rewrite Session Id or SESSION Http Variable
		SessionIdRewriteArea area = core.getConfig().getSessionIdRewriteConfig().getRewriteArea(base);
		if(area != null && area.getParam() != null){
			return addSessionHttpVariable(urlStrng, sessionId, area.getParam());
		}
		
		if(base.indexOf(';') > 0){
			buffer.append(base);
		}else{
			buffer.append(base);
			buffer.append(";jsessionid=");
			buffer.append(sessionId);
		}
		
		// params
		if(splitUrl.length > 1){
			buffer.append("?");
			buffer.append(splitUrl[1]);
		}
		
		
		return buffer.toString();
	}
	
	public static String addSessionHttpVariable(String urlStrng, String sessionId, String paramName)
	{
		StringBuffer buff = new StringBuffer();
		buff.append(urlStrng);
		
		if(urlStrng.indexOf("?") >= 0){
			if(urlStrng.charAt(urlStrng.length() - 1) != '?'){
				buff.append("&");
			}
			
		}else{
			buff.append("?");
		}
		
		buff.append(paramName);
		buff.append("=");
		buff.append(sessionId);
		
		return buff.toString();
	}

	public static String getModuleName(String path)
	{
		// for get method
		String path2[] = path.split("\\?");
		
		// sprit extention
		String pathes[] = path2[0].split("\\.");
		if(pathes.length == 1){
			return pathes[0];
		}
		
		StringBuffer buffer = new StringBuffer();
		
		boolean isFirst = true;
		for(int i = 0; i < pathes.length - 1; i++){
			if(isFirst){
				isFirst = false;
			}else{
				buffer.append(".");
			}
			buffer.append(pathes[i]);
		}
		
		return buffer.toString();	
	}
	
	public static String getRelationalPath(String baseAlinousPath, String srcAlinousPath)
	{
		if(srcAlinousPath.startsWith("/")){
			return srcAlinousPath;
		}
		StringBuffer buff = new StringBuffer();
		
		// base elements
		String dir = getDirectory(baseAlinousPath);
		Stack<String> baseStack = new Stack<String>();
		
		String elements[] = dir.split("/");
		for (int i = 0; i < elements.length; i++) {
			if(elements[i].equals("")){
				continue;
			}
			
			baseStack.push(elements[i]);
		}
		
		elements = srcAlinousPath.split("/");
		for (int i = 0; i < elements.length; i++) {
			if(elements[i].equals("")){
				continue;
			}
			
			if(elements[i].equals(".")){
				continue;
			}
			else if(elements[i].equals("..")){
				baseStack.pop();
			}
			else{
				baseStack.push(elements[i]);
			}
		}
		
		Iterator<String> it = baseStack.iterator();
		while(it.hasNext()){
			String p = it.next();
			
			buff.append("/");
			buff.append(p);
		}
		
		return buff.toString();
	}
	/*
	public static void main(String[] args) {
		String test = "/http://localhost:8080/";
		//String modName = getModuleName(test);
		
		String osPath = getOSPath(test);
		
		AlinousDebug.debugOut(osPath);
		
	}
	*/
	public static Map<String, IParamValue> getParamsFromPath(String path)
	{
		Map<String, IParamValue> retValue = new HashMap<String, IParamValue>();
		
		String pathParts[] = path.split("\\?");
		
		if(pathParts.length != 2){
			return retValue;
		}
		
		String valueStrList[] = pathParts[1].split("&");
		for(int i = 0; i < valueStrList.length; i++){
			String nvPair[] = valueStrList[i].split("=");
			
			if(nvPair.length != 2){
				continue;
			}
			
			String name = nvPair[0];
			String value= nvPair[1];
			
			if(name.endsWith("[]")){
				IParamValue paramVal = retValue.get(name);
				if(paramVal == null || paramVal instanceof ArrayParamValue){
					paramVal = new ArrayParamValue();
					retValue.put(name, paramVal);
				}
				
				((ArrayParamValue)paramVal).addValue(value);
			}
			else{
				retValue.put(name, new StringParamValue(value));
			}
		}
		
		return retValue;
	}
	
	
	public static String getDirectory(String absPath)
	{	
		String schemne = "";
		int pos = absPath.indexOf("//");
		if(pos >= 0){
			int npos = absPath.indexOf("/", 6 + 2);
			
			schemne = absPath.substring(0, npos);
			absPath = absPath.substring(npos, absPath.length());
		}
		
		if(absPath.endsWith(SEPARATOR)){
			return absPath;
		}
		
		String pathes[] = absPath.split(SEPARATOR);
		StringBuffer buffer = new StringBuffer();
		
		for(int i = 1; i < pathes.length - 1; i++){
			buffer.append(SEPARATOR);
			buffer.append(pathes[i]);
		}
		
		buffer.append(SEPARATOR);
		
		return schemne + buffer.toString();
	}
	
	public static String getWebDirectory(String absPath)
	{		
		if(absPath.endsWith("/")){
			return absPath;
		}
		
		String pathes[] = absPath.split("/");
		StringBuffer buffer = new StringBuffer();
		
		for(int i = 1; i < pathes.length - 1; i++){
			buffer.append("/");
			buffer.append(pathes[i]);
		}
		
		buffer.append("/");
		
		return buffer.toString();
	}
	
	public static String getOSDirectory(String absPath)
	{		
		if(absPath.endsWith(AlinousFile.separator)){
			return absPath;
		}
		
		String regSep = "/";
		if(AlinousFile.separator.equals("\\")){
			regSep = "\\\\";
		}
		
		String pathes[] = absPath.split(regSep);
		StringBuffer buffer = new StringBuffer();
		
		for(int i = 1; i < pathes.length - 1; i++){
			buffer.append(AlinousFile.separator);
			buffer.append(pathes[i]);
		}
		
		buffer.append(AlinousFile.separator);
		
		return buffer.toString();
	}
	
	public static String getHomeBasedPath(String home, String target)
	{
		if(!home.endsWith(AlinousFile.separator)){
			home = home + AlinousFile.separator;
		}
		
		if(!target.startsWith(home)){
			return target;
		}
		
		return target.substring(home.length());		
	}
	
	public static String getAbsoluteNotOSPath(String home, String target)
	{
		if(!home.endsWith("/")){
			home = home + "/";
		}
		
		if(target.startsWith("/")){
			target = target.substring(1);
		}
		
		return home + target;
	}
	
	public static String getAbsolutePath(String home, String target)
	{
		if(!home.endsWith(AlinousFile.separator)){
			home = home + AlinousFile.separator;
		}
		
		if(target.startsWith(AlinousFile.separator)){
			target = target.substring(1);
		}
		
		return home + target;
	}
	
	public static String formatAbsolute(String path)
	{
		if(path.startsWith("/")){
			return path;
		}
		
		return "/" + path;
	}
	
	public static boolean isAlinousContent(String path)
	{
		return path.endsWith(".html") || path.endsWith(".rss") ||
				path.endsWith(".alns");
	}
	
	public static String getOSPath(String webPath)
	{
		return webPath.replace("/", AlinousFile.separator);
	}
	
	public static String getNotOSPath(String osPath)
	{
		return osPath.replace( AlinousFile.separator, "/");
	}
	
	public static String forceUnixPath(String path)
	{
		if(path == null){
			return null;
		}
		return path.replace("\\", "/");
	}
	
	public static String getStackTraceString(Throwable e)
	{
		StringWriter stringWriter = new StringWriter();
		PrintWriter wr = new PrintWriter(stringWriter);
	
		e.printStackTrace(wr);
		wr.flush();
		
		return stringWriter.toString();
	}
	

	
	// Timestamp
	public static String getNowString()
	{
		long n = System.currentTimeMillis();
		Timestamp stmp = new Timestamp(n);
		
		return stmp.toString();
	}
	
	public static String md5(String str)
	{
		byte[] md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5").digest(str.getBytes());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < md5.length; i++) {
			sb.append(Integer.toHexString((md5[i] >> 4) & 0x0f));
			sb.append(Integer.toHexString( md5[i] & 0x0f));
		}
		
		return new String(sb);
	}
	
	public static String sqlEscape(String str)
	{
		// 
		//AlinousDebug.debugOut("sqlEscape : " + str);
		
		if(str == null){
			return str;
		}
		str = str.replaceAll("'", "''");
		str = str.replaceAll("\\\\", "\\\\\\\\");
		
		
		// 
		//AlinousDebug.debugOut("sqlEscape after : " + str);
				
		return str;
	}

	public static String csvEscape(String str)
	{
		if(str == null){
			return str;
		}
		str = str.replaceAll("\"", "\"\"");
		
		return str;
	}
	
	public static String dqEscape(String str)
	{
		if(str == null){
			return str;
		}
		str = str.replaceAll("\"", "\\\"");
		
		return str;
	}
	
	
	public static boolean isUriReleavant(String attr)
	{
		if(attr == null){
			return false;
		}
		
		attr = attr.toLowerCase();
		
		return attr.equals("href") || attr.equals("src") ||
		attr.equals("action") || attr.equals("href");
	}
	
	public static String toAlinousHttpHeaderName(String name)
	{
		name = name.toUpperCase();
		name = name.replace('-', '_');
		
		return name;
	}
	
	public static String getParamType(String key, String value, Map<String, IParamValue> paramMap)
	{
		String typeParamName = AlinousAttrs.ALINOUS_TYPE + ":" + key;
		IParamValue typeValue = paramMap.get(typeParamName);
		if(typeValue != null && typeValue instanceof StringParamValue){
			String typeName = ((StringParamValue)typeValue).getValue();
			
			return typeName.toUpperCase();
		}
		
		if(isNumber(value)){
			return IScriptVariable.TYPE_NUMBER;
		}
		
		boolean hit = true;
		try{
			Double.parseDouble(value);
		}catch(Exception e){
			hit = false;
		}
		if(hit){
			return IScriptVariable.TYPE_DOUBLE;
		}
		
		return IScriptVariable.TYPE_STRING;
	}
	
	public static boolean isNumber(String value)
	{
		if(value == null){
			return false;
		}
		if(value.length() == 0){
			return false;
		}
		
		for(int i = 0; i < value.length(); i++){
			char ch = value.charAt(i);
			
			if(ch < '0' || ch > '9'){
				return false;
			}
		}
		
		return true;
	}
	
	public static String getFileName(String srcPath)
	{
		String nonOsPath = getNotOSPath(srcPath);
		
		String pathEls[] = nonOsPath.split("/");
		
		return pathEls[pathEls.length - 1];
	}
	
	// used only from eclipse plugin
	@SuppressWarnings("resource")
	public static void copyTransfer(String srcPath, String destPath) 
	    throws IOException {
	    
	    FileChannel srcChannel = new
	        FileInputStream(srcPath).getChannel();
	    FileChannel destChannel = new
	        FileOutputStream(destPath).getChannel();
	    try {
	        srcChannel.transferTo(0, srcChannel.size(), destChannel);
	    } finally {
	    	srcChannel.close();
	        destChannel.close();
	    }

	}

}
