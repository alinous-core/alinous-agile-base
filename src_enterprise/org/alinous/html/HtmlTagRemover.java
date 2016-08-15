/**
 * CROSSFIRE JAPAN INCORPORATED
 * This source code is under GPL License.
 * info@crossfire.jp
 * Official web site
 * http://alinous.org
 * 
 *  Copyright (C) 2008 Tomohiro Iizuka
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
package org.alinous.html;

public class HtmlTagRemover
{
	public static int STATUS_BODY_START = 0;
	
	private String htmlString;
	
	public HtmlTagRemover(String htmlString)
	{
		this.htmlString = htmlString;
	}
	
	public String removeTags()
	{
		
		StringBuffer bodyStr = new StringBuffer();
		int pos = 0;
		
		while(!isEnd(pos)){
			pos = startBody(bodyStr, pos);
		}
		
		return bodyStr.toString();
	}
	
	public int startBody(StringBuffer bodyStr, int pos)
	{
		pos = addWhile(bodyStr, pos, new char[] {'<'});
		
		pos = skipWhile(bodyStr, pos, new char[] {'>'});
		
		return pos;		
	}
	
	private int addWhile(StringBuffer bodyStr, int pos, char[] tokens)
	{
		char ch = 0;
		while(!isEnd(pos)){
			ch = getCharAt(pos);
			pos++;
			
			if(match(ch, tokens)){
				break;
			}
			else{
				bodyStr.append(ch);
			}
		};
		
		return pos;
	}
	
	private int skipWhile(StringBuffer bodyStr, int pos, char[] tokens)
	{
		char ch = 0;
		while(!isEnd(pos)){
			ch = getCharAt(pos);
			pos++;
			
			if(match(ch, tokens)){
				break;
			}
		};
		
		return pos;
	}
	
	private boolean match(char ch, char[] tokens)
	{
		for(int i = 0; i < tokens.length; i++){
			if(ch == tokens[i]){
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isEnd(int pos)
	{
		if(this.htmlString.length() == pos){
			return true;
		}
		
		return false;
	}
	
	private char getCharAt(int pos)
	{
		char ch = this.htmlString.charAt(pos);
		
		return ch;
	}
}
