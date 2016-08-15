package org.alinous;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alinous.expections.AlinousException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class MimeManager
{
	private Map<String, String> contentTypes = new HashMap<String, String>();
	
	
	
	public MimeManager(InputStream stream) throws AlinousException
	{
		Document doc = null;
		try {
			doc = new SAXBuilder().build(stream);
		} catch (JDOMException e) {
			throw new AlinousException(e, null);
		} catch (IOException e) {
			throw new AlinousException(e, "mimetypes.xml is broken");
		}
		
		
		Element root = doc.getRootElement();
		try{
			parseElements(root);
		}finally{
			doc.clone();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void parseElements(Element root)
	{
		List<Element> mimeMappings = root.getChildren("mime-mapping");
		Iterator<Element> it = mimeMappings.iterator();
		while(it.hasNext()){
			Element el = it.next();
			
			Element extEl = el.getChild("extension");
			Element contEl = el.getChild("mime-type");
			
			if(extEl != null && contEl != null){
				String extention = extEl.getText();
				String content = contEl.getText();
				
				this.contentTypes.put(extention, content);
				
				//AlinousDebug.debugOut("Accepted contentType: " + extention + " -> " + content);
			}
			
		}
	}
	
	public String getContentType(String extention)
	{
		String co = this.contentTypes.get(extention);
		
		if(co != null){
			return co;
		}
		else{
			return "text/html";
		}
	}
}
