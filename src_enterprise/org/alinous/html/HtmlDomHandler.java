package org.alinous.html;

import java.io.StringReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.alinous.objects.DqString;
import org.alinous.objects.IAttribute;
import org.alinous.objects.SqString;
import org.alinous.objects.XMLTagBase;
import org.alinous.objects.html.AlinousTopObject;
import org.alinous.objects.html.CommentObject;
import org.alinous.objects.html.StringContainer;
import org.alinous.parser.object.AlinousObjectParser;
import org.alinous.parser.object.ParseException;
import org.alinous.parser.object.TokenMgrError;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.ScriptArray;
import org.alinous.script.runtime.ScriptDomVariable;

public class HtmlDomHandler
{
	public IScriptVariable html2Dom(String htmlString)
	{
		AlinousTopObject topObj = null;		
		StringReader reader = new StringReader(htmlString);
		
		AlinousObjectParser parser = new AlinousObjectParser(reader);
		try {
			topObj = parser.parse();
		} catch (ParseException e) {
			ScriptDomVariable ret = new ScriptDomVariable("RETURN");
			//ret.setValue(Integer.toString(size));
			ret.setValueType(IScriptVariable.TYPE_NULL);
			return ret;
		} catch(TokenMgrError e){
			if(!e.getMessage().endsWith("Encountered: <EOF> after : \"\"")){
				ScriptDomVariable ret = new ScriptDomVariable("RETURN");
				//ret.setValue(Integer.toString(size));
				ret.setValueType(IScriptVariable.TYPE_NULL);
				return ret;
			}else{
				topObj = parser.topObj;
			}
		}
		
				
		List<XMLTagBase> list = topObj.getInnerTags();
		Iterator<XMLTagBase> it = list.iterator();
		
		ScriptArray rootArray = new ScriptArray();
		
		while(it.hasNext()){
			XMLTagBase tag = it.next();
			
			// ScriptDomVariable
			ScriptDomVariable self = handleSelf(tag);
			rootArray.add(self);
			
			// handle children
			handleChildren(tag, self);
		}
		
		return rootArray;
	}
	
	private void handleChildren(XMLTagBase tag, ScriptDomVariable self)
	{
		ScriptArray childrenArray = null;
		
		Iterator<XMLTagBase> it = tag.getInnerTags().iterator();
		while(it.hasNext()){
			XMLTagBase childTag = it.next();
			
			if(childrenArray == null){
				childrenArray = new ScriptArray("CHILDREN");
				self.put(childrenArray);
			}
			
			// ScriptDomVariable
			ScriptDomVariable childDom = handleSelf(childTag);
			childrenArray.add(childDom);
			
			// handle children
			handleChildren(childTag, childDom);			
		}
	}
	
	private ScriptDomVariable handleSelf(XMLTagBase tag)
	{
		if(tag instanceof StringContainer){
			ScriptDomVariable self = new ScriptDomVariable("string");
			self.setValue(((StringContainer)tag).getStr());
			
			ScriptDomVariable typeDom = new ScriptDomVariable("TAG_TYPE");
			typeDom.setValue("TEXT_STRING");
			
			self.put(typeDom);
			
			return self;
		}
		else if(tag instanceof CommentObject){
			ScriptDomVariable self = new ScriptDomVariable("COMMENT");
			self.setValue(((CommentObject)tag).getCommentStr());
			
			ScriptDomVariable typeDom = new ScriptDomVariable("TAG_TYPE");
			typeDom.setValue("COMMENT");
			
			self.put(typeDom);
			
			return self;			
		}
		
		ScriptDomVariable self = new ScriptDomVariable(tag.getTagName());
		self.setValue(tag.getTagName());
		
		ScriptDomVariable typeDom = new ScriptDomVariable("TAG_TYPE");
		typeDom.setValue(tag.getTagName());
		
		self.put(typeDom);
		
		Hashtable<String, IAttribute> attributes = tag.getAttributes();
		Enumeration<String> enm = attributes.keys();
		while(enm.hasMoreElements()){
			String key = enm.nextElement();
			
			IAttribute attr = attributes.get(key);
			
			if(attr.getValue() instanceof SqString){
				ScriptDomVariable attrDom = new ScriptDomVariable(key);
				attrDom.setValue(((SqString)attr.getValue()).getValue());
				attrDom.setValueType(ScriptDomVariable.TYPE_STRING);
				
				self.put(attrDom);
			}
			else if(attr.getValue() instanceof DqString){
				ScriptDomVariable attrDom = new ScriptDomVariable(key);
				attrDom.setValue(((DqString)attr.getValue()).getValue());
				attrDom.setValueType(ScriptDomVariable.TYPE_STRING);
				
				self.put(attrDom);
			}
			else{
				ScriptDomVariable attrDom = new ScriptDomVariable(key);
				attrDom.setValue(attr.getValue().toString());
				attrDom.setValueType(ScriptDomVariable.TYPE_STRING);
				
				self.put(attrDom);
			}

		}
		
		return self;
	}
}
