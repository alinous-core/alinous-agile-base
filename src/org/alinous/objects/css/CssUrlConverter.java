package org.alinous.objects.css;


public class CssUrlConverter {
	public static String QUOTE = "'";
	public static String DOUBLE_QUOTE = "\"";
	public static String START_PARENSIS = "(";
	public static String END_PARENSIS = ")";
	public static String URL = "url";
	
	private enum Status {body, after_url, after_url_p, after_url_p_quated};
	private Status currentStatus;
	private CssTokenizer tokenizer;
	
	private String lastQuated;
	private UrlRewriter rewriter;
	
	private CssReplacer replacer;
	
	public CssUrlConverter(String css, String replaceBase)
	{
		this.tokenizer = new CssTokenizer(css);
		
		this.replacer = new CssReplacer(replaceBase);
	}
	
	public String convert()
	{
		StringBuffer buff = new StringBuffer();
		this.currentStatus = Status.body;
		
		CssToken token;
		do{
			token = this.tokenizer.getToken();
			switch(this.currentStatus){
			case body:
				handleBody(buff, token);
				break;
			case after_url:
				handleAfterUrl(buff, token);
				break;
			case after_url_p:
				handleAfterUrlP(buff, token);
				break;
			case after_url_p_quated:
				handleAfterUrlQuated(buff, token);
				break;
			default:
				break;
			}
		}while(!tokenizer.isEof());
		
		return buff.toString();
	}
	
	private void handleBody(StringBuffer buff, CssToken token)
	{
		if(token.getType() == CssToken.TokenType.comment){
			buff.append(token.getToken());
			return;
		}
		
		if(token.getToken().toLowerCase().equals(URL)){
			this.currentStatus = Status.after_url;
			buff.append(token.getToken());
			return;
		}
		
		buff.append(token.getToken());
	}
	
	private void handleAfterUrl(StringBuffer buff, CssToken token)
	{
		if(token.getType() == CssToken.TokenType.comment){
			buff.append(token.getToken());
			return;
		}
		
		if(token.getToken().equals(START_PARENSIS)){
			this.currentStatus = Status.after_url_p;
			buff.append(token.getToken());
			return;
		}
		
		buff.append(token.getToken());
	}
	
	private void handleAfterUrlP(StringBuffer buff, CssToken token)
	{
		if(token.getType() == CssToken.TokenType.comment){
			buff.append(token.getToken());
			return;
		}
		
		if(token.getToken().equals(QUOTE) || token.getToken().equals(DOUBLE_QUOTE)){
			this.currentStatus = Status.after_url_p_quated;
			this.lastQuated = token.getToken();
			this.rewriter = new UrlRewriter(this.replacer);
			buff.append(token.getToken());
			return;
		}
		
		if(token.getToken().equals(END_PARENSIS)){
			this.currentStatus = Status.body;
			buff.append(token.getToken());
			return;
		}

		// not has '' and ""
		this.rewriter = new UrlRewriter(this.replacer);
		this.rewriter.append(token.getToken());
		buff.append(this.rewriter.toString());
		this.rewriter = null;
	}
	
	private void handleAfterUrlQuated(StringBuffer buff, CssToken token)
	{
		if(token.getToken().equals(this.lastQuated)){
			this.currentStatus = Status.after_url_p;
			
			buff.append(this.rewriter.toString());
			this.rewriter = null;
			
			buff.append(token.getToken());
			return;
		}
		
		this.rewriter.append(token.getToken());
	}

	public CssReplacer getReplacer() {
		return replacer;
	}
}
