package org.alinous.script.basic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.alinous.debug.StepInCandidates;
import org.alinous.exec.ScriptCheckContext;
import org.alinous.exec.ScriptError;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.script.AlinousScript;
import org.alinous.script.IScriptSentence;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.statement.FunctionCall;
import org.alinous.test.coverage.FileCoverage;
import org.jdom.Element;

public class Comment implements IScriptSentence {
	
	private int line;
	private int linePosition;
	private String filePath;
	private String type;
	
	private String comment;
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getCommentInner()
	{
		if(this.type.equals("//")){
			if(this.comment != null && this.comment.contains("@FK")){
				return "";
			}
			return this.comment;
		}
		
		StringReader strreader = new StringReader(this.comment);
		BufferedReader reader = new BufferedReader(strreader);
		
		StringBuffer buff = new StringBuffer();
		String line;
		try {
			line = reader.readLine();
		} catch (IOException e) {
			return buff.toString();
		}
		try {
			
			int indent = -1;
			boolean first = true;
			while(line != null){
				if(line.contains("@FK")){
					line = reader.readLine();
					continue;
				}
				
				
				if(first){
					first = false;
									
					if(!isIgnoreLine(line)){
						if(indent < 0){
							indent = getIndent(line);
						}
						buff.append(cutIndent(line, indent));
					}
				}
				else{
					if(indent < 0){
						indent = getIndent(line);
					}
					
					buff.append(cutIndent(line, indent));
					buff.append("\n");
				}
				
	
				line = reader.readLine();
	
			}
		} catch (IOException e) {
			e.printStackTrace();

		}
		
		trimLastLf(buff);
		
		return buff.toString();
	}
	
	public String getFkAnnotation()
	{
		StringReader strreader = new StringReader(this.comment);
		BufferedReader reader = new BufferedReader(strreader);
		
		String line;
		
		try {
			line = reader.readLine();
			while(line != null){
				if(line.contains("@FK")){
					return doGetFkAnnotation(line);
				}
				
				line = reader.readLine();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	private String doGetFkAnnotation(String line)
	{
		int pos = line.indexOf("@FK");
		if(pos < 0){
			return null;
		}
		
		line = line.trim();
		String vals[] = line.split(" ");
		
		for(int i = 0; i < vals.length; i++){
			if(vals[i].equals("@FK") && i + 1 < vals.length){
				return vals[i + 1];
			}
		}
		
		return null;
	}
	
	private void trimLastLf(StringBuffer buff)
	{
		while(buff.length() > 0 && (buff.charAt(buff.length() - 1) == '\n' || buff.charAt(buff.length() - 1) == '\r') ){
			buff.deleteCharAt(buff.length() - 1);
		}		
	}
	
	private String cutIndent(String line, int indent)
	{
		if(line.length() < indent){
			StringBuffer buff = new StringBuffer();
			for(int i = 0; i < line.length(); i++){
				if(line.charAt(i) == '\t'){
					continue;
				}
				
				buff.append(line.charAt(i));
			}
			
			return buff.toString();
		}
		
		return line.substring(indent);
	}
	
	private int getIndent(String line)
	{
		int indent = 0;
		
		for(int i = 0; i < line.length(); i++){
			char ch = line.charAt(i);
			if(ch != '\t'){
				return indent;
			}
			
			indent++;
		}
		
		return indent;
	}
	
	private boolean isIgnoreLine(String line)
	{
		String str = line.trim();
		
		for(int i = 0; i < str.length(); i++){
			char ch = str.charAt(i);
			if(ch != '*'){
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public int getLine() {
		return this.line;
	}

	@Override
	public void setLine(int line) {
		this.line = line;
	}

	@Override
	public int getLinePosition() {
		return this.linePosition;
	}

	@Override
	public void setLinePosition(int linePosition) {
		this.linePosition = linePosition;
	}

	@Override
	public boolean execute(PostContext context, VariableRepository valRepo)
			throws ExecutionException {
		return true;
	}

	@Override
	public void exportIntoJDomElement(Element parent) throws AlinousException {
		
	}

	@Override
	public void importFromJDomElement(Element threadElement)
			throws AlinousException {
	}

	@Override
	public void checkStaticErrors(ScriptCheckContext scContext,
			List<ScriptError> errorList) {
		
	}

	@Override
	public String getFilePath() {
		return this.filePath;
	}

	@Override
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public StepInCandidates getStepInCandidates() {
		return null;
	}

	@Override
	public IScriptVariable getReturnedVariable(PostContext context) {
		return null;
	}

	@Override
	public String toString() {
		return this.comment;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	@Override
	public void getFunctionCall(ScriptCheckContext scContext, List<FunctionCall> call, AlinousScript script)
	{
		
	}

	@Override
	public void setupCoverage(FileCoverage coverage)
	{
		
	}
}
