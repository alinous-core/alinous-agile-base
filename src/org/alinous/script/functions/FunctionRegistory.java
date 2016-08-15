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
package org.alinous.script.functions;

import java.util.HashMap;
import java.util.Map;

import org.alinous.AlinousCore;
import org.alinous.components.tree.api.NodeTreeCloseAllNodes;
import org.alinous.components.tree.api.NodeTreeCloseNode;
import org.alinous.components.tree.api.NodeTreeGetCategory;
import org.alinous.components.tree.api.NodeTreeGetModels;
import org.alinous.components.tree.api.NodeTreeGetChildrenById;
import org.alinous.components.tree.api.NodeTreeGetNextNode;
import org.alinous.components.tree.api.NodeTreeGetNodeById;
import org.alinous.components.tree.api.NodeTreeGetNodeByTitle;
import org.alinous.components.tree.api.NodeTreeGetNodesUnderTitle;
import org.alinous.components.tree.api.NodeTreeGetPrevNode;
import org.alinous.components.tree.api.NodeTreeGetStaticName;
import org.alinous.components.tree.api.NodeTreeMakeSitemap;
import org.alinous.components.tree.api.NodeTreeOpenNode;
import org.alinous.components.tree.api.NodeTreeSetCategory;
import org.alinous.components.tree.api.NodeTreeSetChildrenCategory;
import org.alinous.components.tree.api.NodeTreeSetCurrent;
import org.alinous.components.tree.api.NodeTreeSetDocRef;
import org.alinous.components.tree.api.NodeTreeSetTitle;
import org.alinous.components.tree.api.NodeTreeSetVisible;
import org.alinous.components.tree.api.NodeTreeWriteStaticHtml;
import org.alinous.components.tree.api.TreeNodeClearCurrent;
import org.alinous.datasrc.api.SQLBackup;
import org.alinous.datasrc.api.SQLGetTableInfo;
import org.alinous.datasrc.api.SQLGetTables;
import org.alinous.datasrc.api.SQLLoadBlob;
import org.alinous.datasrc.api.SQLLoadCsv;
import org.alinous.datasrc.api.SQLStoreBlob;
import org.alinous.datasrc.api.SQLTableExists;
import org.alinous.ftp.api.FtpChangeDir;
import org.alinous.ftp.api.FtpClose;
import org.alinous.ftp.api.FtpConnect;
import org.alinous.ftp.api.FtpDeleteFile;
import org.alinous.ftp.api.FtpDownloadFile;
import org.alinous.ftp.api.FtpFtpHttpConnect;
import org.alinous.ftp.api.FtpListDirectories;
import org.alinous.ftp.api.FtpListFiles;
import org.alinous.ftp.api.FtpLogin;
import org.alinous.ftp.api.FtpPwd;
import org.alinous.ftp.api.FtpRemoveDirectory;
import org.alinous.ftp.api.FtpRename;
import org.alinous.ftp.api.FtpSetFileType;
import org.alinous.ftp.api.FtpSetListHiddenFiles;
import org.alinous.ftp.api.FtpUploadFile;
import org.alinous.html.api.HtmlHtml2Dom;
import org.alinous.html.api.HtmlOutputHtml;
import org.alinous.html.api.HtmlRemoveTags;
import org.alinous.http.api.HttpAccess;
import org.alinous.http.api.HttpFetch;
import org.alinous.http.api.HttpUrlDecode;
import org.alinous.lucene.api.LuceneQuery;
import org.alinous.lucene.api.LuceneSyncTable;
import org.alinous.net.pop3.api.Pop3Close;
import org.alinous.net.pop3.api.Pop3Connect;
import org.alinous.net.pop3.api.Pop3DeleteMail;
import org.alinous.net.pop3.api.Pop3GetMail;
import org.alinous.net.pop3.api.Pop3List;
import org.alinous.parallel.exam.function.ParallelJoin;
import org.alinous.parallel.exam.function.ParallelJoinAll;
import org.alinous.plugin.api.PluginCall;
import org.alinous.poi.api.PoiSetCellValue;
import org.alinous.script.functions.codecheck.CodeCheckSkipOnce;
import org.alinous.script.functions.core.AlinousGetSendmail;
import org.alinous.script.functions.core.AlinousSetSemdmail;
import org.alinous.script.functions.parser.AlinousHtmlParse;
import org.alinous.script.functions.parser.AlinousHtmlcheck;
import org.alinous.script.functions.system.ArraySize;
import org.alinous.script.functions.system.CastToDouble;
import org.alinous.script.functions.system.CastToNumber;
import org.alinous.script.functions.system.MailSend;
import org.alinous.script.functions.system.ScriptEvaluate;
import org.alinous.script.functions.system.StringReplace;
import org.alinous.script.functions.system.SystemGetLine;
import org.alinous.script.functions.system.SystemGetScriptPath;
import org.alinous.script.functions.system.SystemPrint;
import org.alinous.script.functions.system.ThreadExecute;
import org.alinous.script.functions.system.ThreadGetId;
import org.alinous.script.functions.system.ThreadJoin;
import org.alinous.script.functions.system.ThreadSleep;
import org.alinous.script.functions.system.ValidateCheck;
import org.alinous.script.functions.system.VariableGet;
import org.alinous.script.functions.system.VariableGetDomValue;
import org.alinous.script.functions.system.VariableGetPath;
import org.alinous.script.functions.system.VariableIsArray;
import org.alinous.script.functions.system.VariableListNames;
import org.alinous.script.functions.system.VariableRelease;
import org.alinous.script.functions.system.VariableSort;
import org.alinous.script.functions.system.debug.DebuggerCheckHtml;
import org.alinous.script.functions.system.debug.DebuggerCheckScript;
import org.alinous.script.functions.system.debug.DebuggerClearAllBreakpoints;
import org.alinous.script.functions.system.debug.DebuggerGetCurrentLine;
import org.alinous.script.functions.system.debug.DebuggerGetStacks;
import org.alinous.script.functions.system.debug.DebuggerGetStepInCandidates;
import org.alinous.script.functions.system.debug.DebuggerGetVariables;
import org.alinous.script.functions.system.debug.DebuggerGetVariablesFlat;
import org.alinous.script.functions.system.debug.DebuggerRemoveBreakPoint;
import org.alinous.script.functions.system.debug.DebuggerRemoveConsole;
import org.alinous.script.functions.system.debug.DebuggerResigterConsole;
import org.alinous.script.functions.system.debug.DebuggerResume;
import org.alinous.script.functions.system.debug.DebuggerSetBreakPoint;
import org.alinous.script.functions.system.debug.DebuggerStepIn;
import org.alinous.script.functions.system.debug.DebuggerStepOver;
import org.alinous.script.functions.system.debug.DebuggerStepReturn;
import org.alinous.script.functions.system.debug.DebuggerTerminateThread;
import org.alinous.script.functions.system.debug.ScriptDebugEvaluate;
import org.alinous.script.functions.system.ide.ScriptGetDdl;
import org.alinous.script.functions.system.ide.ScriptGetRegisterdFunctions;
import org.alinous.test.Timestamp.DebugsetTimestamp;
import org.alinous.test.record.api.RecordDom2Json;
import org.alinous.test.record.api.RecordDom2Xml;
import org.alinous.tools.blog.api.BlogSendPing;
import org.alinous.tools.csv.CsvAddFields;
import org.alinous.tools.csv.CsvCloseOutputFile;
import org.alinous.tools.csv.CsvCloseReadFile;
import org.alinous.tools.csv.CsvCountLines;
import org.alinous.tools.csv.CsvOpenOutputFile;
import org.alinous.tools.csv.CsvOpenReadFile;
import org.alinous.tools.csv.CsvOutLineEnd;
import org.alinous.tools.csv.CsvReadLine;
import org.alinous.tools.csv.CsvWriteRecords;
import org.alinous.tools.zip.api.ComporessZip;
import org.alinous.tools.zip.api.ExtractZip;

public class FunctionRegistory {
	private Map<String, IFunction> functions = new HashMap<String, IFunction>();
	
	public static FunctionRegistory instance;
	
	private FunctionRegistory()
	{
		// init registory
		registerFunction(new SystemPrint());
		registerFunction(new ArraySize());
		registerFunction(new CastToNumber());
		registerFunction(new CastToDouble());
		
		registerFunction(new MailSend());
		
		registerFunction(new VariableRelease());
		registerFunction(new VariableGet());
		registerFunction(new VariableIsArray());
		registerFunction(new VariableSort());
		registerFunction(new VariableListNames());
		registerFunction(new VariableGetPath());
		registerFunction(new VariableGetDomValue());
		
		registerFunction(new ScriptEvaluate());
		registerFunction(new ScriptGetDdl());
		registerFunction(new ScriptGetRegisterdFunctions());
		
		registerFunction(new StringReplace());
	
		registerFunction(new ThreadExecute());
		registerFunction(new ThreadJoin());
		registerFunction(new ThreadSleep());
		registerFunction(new ThreadGetId());
		
		registerFunction(new ParallelJoin());
		registerFunction(new ParallelJoinAll());
		
		registerFunction(new ValidateCheck());
		
		registerFunction(new SystemGetLine());
		registerFunction(new SystemGetScriptPath());
		
		if(AlinousCore.enterprise){
			// node tree APIs
			registerFunction(new NodeTreeSetDocRef());
			registerFunction(new NodeTreeSetTitle());
			registerFunction(new NodeTreeSetVisible());
			registerFunction(new NodeTreeGetNodeByTitle());
			registerFunction(new NodeTreeGetNodesUnderTitle());
			registerFunction(new NodeTreeWriteStaticHtml());
			registerFunction(new NodeTreeGetModels());
			registerFunction(new NodeTreeMakeSitemap());
			registerFunction(new NodeTreeSetCurrent());
			registerFunction(new TreeNodeClearCurrent());
			registerFunction(new NodeTreeOpenNode());
			registerFunction(new NodeTreeCloseNode());
			registerFunction(new NodeTreeGetStaticName());
			registerFunction(new NodeTreeGetCategory());
			registerFunction(new NodeTreeSetCategory());
			registerFunction(new NodeTreeSetChildrenCategory());
			registerFunction(new NodeTreeGetChildrenById());
			registerFunction(new NodeTreeGetNodeById());
			registerFunction(new NodeTreeGetNextNode());
			registerFunction(new NodeTreeGetPrevNode());
			
			// added
			registerFunction(new NodeTreeCloseAllNodes());
			
			// Lucene
			registerFunction(new LuceneQuery());
			registerFunction(new LuceneSyncTable());
			

		}
		// Zip APIs
		registerFunction(new ComporessZip());
		registerFunction(new ExtractZip());
		
		// SQL Functions
		registerFunction(new SQLLoadBlob());
		registerFunction(new SQLStoreBlob());
		registerFunction(new SQLBackup());
		registerFunction(new SQLLoadCsv());
		registerFunction(new SQLTableExists());
		registerFunction(new SQLGetTables());
		registerFunction(new SQLGetTableInfo());
		
		
		// html
		registerFunction(new HtmlRemoveTags());
		registerFunction(new HtmlOutputHtml());
		registerFunction(new HtmlHtml2Dom());
		
		// plugin
		registerFunction(new PluginCall());
		
		// csv
		registerFunction(new CsvWriteRecords());
		registerFunction(new CsvOpenOutputFile());
		registerFunction(new CsvCloseOutputFile());
		registerFunction(new CsvAddFields());
		registerFunction(new CsvOutLineEnd());
		registerFunction(new CsvOpenReadFile());
		registerFunction(new CsvCloseReadFile());
		registerFunction(new CsvReadLine());
		registerFunction(new CsvCountLines());
		
		// blog
		registerFunction(new BlogSendPing());
		
		// POI
		registerFunction(new PoiSetCellValue());
		
		// HTTP
		registerFunction(new HttpAccess());
		registerFunction(new HttpUrlDecode());
		registerFunction(new HttpFetch());
		
		// Pop3
		registerFunction(new Pop3Close());
		registerFunction(new Pop3Connect());
		registerFunction(new Pop3DeleteMail());
		registerFunction(new Pop3GetMail());
		registerFunction(new Pop3List());
		
		// Ftp
		registerFunction(new FtpChangeDir());
		registerFunction(new FtpClose());
		registerFunction(new FtpConnect());
		registerFunction(new FtpFtpHttpConnect());
		registerFunction(new FtpDeleteFile());
		registerFunction(new FtpDownloadFile());
		registerFunction(new FtpListDirectories());
		registerFunction(new FtpListFiles());
		registerFunction(new FtpLogin());
		registerFunction(new FtpPwd());
		registerFunction(new FtpRemoveDirectory());
		registerFunction(new FtpRename());
		registerFunction(new FtpSetFileType());
		registerFunction(new FtpSetListHiddenFiles());
		registerFunction(new FtpUploadFile());
		
		// Alinous
		registerFunction(new AlinousHtmlParse());
		registerFunction(new AlinousHtmlcheck());
		
		registerFunction(new AlinousSetSemdmail());
		registerFunction(new AlinousGetSendmail());
		
		registerFunction(new CodeCheckSkipOnce());
		
		// Test
		registerFunction(new RecordDom2Xml());
		registerFunction(new RecordDom2Json());
		registerFunction(new DebugsetTimestamp());
		
		// Debugger API
		registerFunction(new DebuggerRemoveBreakPoint());
		registerFunction(new DebuggerSetBreakPoint());
		registerFunction(new DebuggerStepIn());
		registerFunction(new DebuggerStepOver());
		registerFunction(new DebuggerStepReturn());
		registerFunction(new DebuggerResume());
		registerFunction(new ScriptDebugEvaluate());
		
		registerFunction(new DebuggerClearAllBreakpoints());
		registerFunction(new DebuggerGetStacks());
		registerFunction(new DebuggerGetVariables());
		registerFunction(new DebuggerGetVariablesFlat());
		registerFunction(new DebuggerGetCurrentLine());
		registerFunction(new DebuggerGetStepInCandidates());
		registerFunction(new DebuggerTerminateThread());
		
		registerFunction(new DebuggerCheckScript());
		registerFunction(new DebuggerCheckHtml());
		
		registerFunction(new DebuggerResigterConsole());
		registerFunction(new DebuggerRemoveConsole());
		
	}
	
	private void registerFunction(IFunction func)
	{
		this.functions.put(func.getName().toUpperCase(), func);
	}
	
	public static FunctionRegistory getInstance()
	{
		if(instance == null){
			instance = new FunctionRegistory();
		}
		
		return instance;
	}
	
	public void registerFunction(FunctionDeclaration declare)
	{
		this.functions.put(declare.getName(), declare);
	}
	
	public IFunction findDeclaration(String qualifiedName)
	{
		return this.functions.get(qualifiedName.toUpperCase());
	}

	public Map<String, IFunction> getFunctions() {
		return functions;
	}
	
	public boolean hasFunction(String funcName)
	{
		IFunction func = this.functions.get(funcName.toUpperCase());
		
		if(func != null){
			return true;
		}
		
		return false; 
	}
	
}
