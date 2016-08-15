package org.alinous.objects.css;

public class CssTokenizer {
	private String css;
	private int pos = 0;
	
	private char stopChars[] = {'@', ';', ':', '{', '}', '(', ')', '"', '\'', '\t', '\n', ' '};
	
	private enum Status {body, comment};
	private Status currentStatus = Status.body;
	
	private String reservedToken;
	
	public CssTokenizer(String css)
	{
		this.css = css;
	}
	
	private CssToken skipComment()
	{
		StringBuffer token = new StringBuffer();
		while(true){
			if(isEof()){
				return new CssToken(token.toString(), CssToken.TokenType.body);
			}
			
			char ch = getChar();
			
			if(ch == '*'){
				if(isEof()){
					token.append(ch);
					return new CssToken(token.toString(), CssToken.TokenType.body);
				}
				token.append(ch);
				ch = getChar();
				if(ch == '/'){
					token.append(ch);
					this.currentStatus = Status.body;
					return new CssToken(token.toString(), CssToken.TokenType.comment);
				}
			}
			token.append(ch);
		}
	}
	
	public CssToken getToken()
	{
		if(this.currentStatus == Status.comment){
			return skipComment();
		}
		
		if(this.reservedToken != null){
			String lastToken = this.reservedToken;
			this.reservedToken = null;
			return new CssToken(lastToken, CssToken.TokenType.body);
		}
		StringBuffer token = new StringBuffer();
		
		while(true){
			if(isEof()){
				return new CssToken(token.toString(), CssToken.TokenType.body);
			}
			
			char ch = getChar();
			
			// special ch starts or not
			if(enterSpecialToken(ch)){
				this.pos--;
				this.currentStatus = Status.comment;
				
				if(token.length() == 0){
					return skipComment();
				}
				
				return new CssToken(token.toString(), CssToken.TokenType.body);
			}
			
			
			if(isStopchar(ch)){
				if(token.length() == 0){
					return new CssToken(new String(new char[]{ch}), CssToken.TokenType.body);
				}
				this.reservedToken = new String(new char[]{ch});
				break;
			}
			
			token.append(ch);
		}
		
		return new CssToken(token.toString(), CssToken.TokenType.body);
	}
	
	private boolean enterSpecialToken(char ch)
	{
		if(ch != '/' || isEof()){
			return false;
		}
		
		// read ahead
		char nextCh = getChar();
		if(nextCh == '*'){
			this.pos--;
			return true;
		}
		
		this.pos--;
		return false;
	}
	
	public boolean isEof()
	{
		if(reservedToken != null){
			return false;
		}
		return pos == css.length();
	}
	
	private boolean isStopchar(char ch)
	{
		for (int i = 0; i < stopChars.length; i++) {
			if(ch == stopChars[i]){
				return true;
			}
		}
		
		return false;
	}
	
	private char getChar()
	{
		char ch = this.css.charAt(this.pos++);
		
		return ch;
	}
}
