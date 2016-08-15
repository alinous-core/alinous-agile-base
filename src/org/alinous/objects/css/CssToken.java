package org.alinous.objects.css;

public class CssToken {
	public enum TokenType {body, comment};
	private String token;
	private TokenType type;
	
	public CssToken(String token, CssToken.TokenType type)
	{
		this.token = token;
		this.type = type;
	}

	public String getToken() {
		return token;
	}

	public TokenType getType() {
		return type;
	}
}
