package org.alinous.script.functions.system.ide;

import java.util.ArrayList;
import java.util.List;

import org.alinous.script.basic.Comment;
import org.alinous.script.basic.LineComment;


public class CommentInspector {
	
	private ScriptCharReader reader;
	private int status = 0;
	
	private final static int TOKENIZE_STATUS_NORMAL = 0;
	private final static int TOKENIZE_STATUS_IN_COMMENT = 1;
	private final static int TOKENIZE_STATUS_IN_LINE_COMMENT = 2;
	private final static int TOKENIZE_STATUS_IN_DQ = 10;
	private final static int TOKENIZE_STATUS_IN_SQ = 11;
	
	private StringBuffer buff = new StringBuffer();
	
	public CommentInspector(String sourceCode)
	{
		this.reader = new ScriptCharReader(sourceCode);
	}
	
	public List<Comment> parse()
	{
		List<Comment> list = new ArrayList<Comment>();
		
		int line = this.reader.getLine();
		int lastStatus = TOKENIZE_STATUS_NORMAL;
		String token = getToken();
		
		while(token != null){
			
			if(lastStatus == TOKENIZE_STATUS_IN_COMMENT){
				Comment com = new Comment();
				com.setLine(line);
				com.setComment(token);
				com.setType("/*");
				
				list.add(com);
			}
			else if(lastStatus == TOKENIZE_STATUS_IN_LINE_COMMENT){
				Comment com = new LineComment();
				com.setLine(line);
				com.setComment(token);
				com.setType("//");
				
				list.add(com);
			}
			
			
			line = this.reader.getLine();
			lastStatus = this.status;
			token = getToken();
		}
		
		return list;
	}
	
	private String getToken()
	{
		this.buff.setLength(0);
		
		int ch = this.reader.read();
		
		if(ch == -1){
			return null;
		}
		
		while(ch != (char)-1){
			if(this.status == CommentInspector.TOKENIZE_STATUS_NORMAL){
				if(ch == '/'){
					ch = (char) this.reader.read();
					if(ch == '/'){
						this.status =  TOKENIZE_STATUS_IN_LINE_COMMENT;
						break;
					}
					ch = this.reader.unread();
				}
				
				if(ch == '/'){
					ch = (char) this.reader.read();
					if(ch == '*'){
						this.status =  TOKENIZE_STATUS_IN_COMMENT;
						break;
					}
					ch = this.reader.unread();
				}
				
				appnedChar((char) ch);
			}
			else if(this.status == CommentInspector.TOKENIZE_STATUS_IN_COMMENT){
				if(ch == '*'){
					ch = this.reader.read();
					if(ch == '/'){
						this.status =  TOKENIZE_STATUS_NORMAL;
						break;
					}
					ch = this.reader.unread();
				}
				
				appnedChar((char) ch);
			}
			else if(this.status == CommentInspector.TOKENIZE_STATUS_IN_LINE_COMMENT){
				if(ch == '\n'){
					this.status =  TOKENIZE_STATUS_NORMAL;
					break;
				}
				appnedChar((char) ch);
			}
			else if(this.status == CommentInspector.TOKENIZE_STATUS_IN_DQ){
				// escape
				if(ch == '\\'){
					appnedChar((char) ch);
					ch = (char) this.reader.read();
					appnedChar((char) ch);
					ch = (char) this.reader.read();
				}
				
				// change status
				if(ch == '"'){
					this.status =  TOKENIZE_STATUS_NORMAL;
					break;
				}
				
				appnedChar((char) ch);
			}
			else if(this.status == CommentInspector.TOKENIZE_STATUS_IN_SQ){
				// change status
				if(ch == '\''){
					this.status =  TOKENIZE_STATUS_NORMAL;
					break;
				}
				appnedChar((char) ch);
			}
			
			ch = (char) this.reader.read();
		}
		
		return this.buff.toString();
	}
	
	private void appnedChar(char ch)
	{
		if(ch != (char)-1){
			this.buff.append(ch);
		}		
	}
	
	protected class ScriptCharReader{
		private String script;
		private int pos;
		private int line;
		
		public ScriptCharReader(String script)
		{
			this.script = script;
			this.pos = 0;
			this.line = 1;
		}
		
		public int read()
		{
			if(this.pos >= this.script.length()){
				return -1;
			}
			
			char ch = this.script.charAt(this.pos);
			if(this.script.charAt(this.pos) == '\n'){
				this.line++;
			}
			
			this.pos++;
			return ch;
		}
		
		public char unread()
		{
			this.pos--;
			if(this.script.charAt(this.pos) == '\n'){
				this.line--;
			}
			
			char ch = this.script.charAt(this.pos - 1);
			return ch;
		}
		
		public int getLine()
		{
			return this.line;
		}
	}
	
}
