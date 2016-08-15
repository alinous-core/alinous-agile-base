package org.alinous.sql.config;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;

public class SqlConfig
{
	private List<SqlConstrainConfig> constrains = new LinkedList<SqlConstrainConfig>();
	private List<SqlTriggerConfig> triffers = new LinkedList<SqlTriggerConfig>();
	
	@SuppressWarnings("unchecked")
	public void addConstrainFromElement(Element constrainElement)
	{
		SqlConstrainConfig constrain = new SqlConstrainConfig();
		
		String tableName = constrainElement.getAttributeValue("table");
		constrain.setTableName(tableName);
		
		List<Element> paramsList = constrainElement.getChildren("param");
		Iterator<Element> it = paramsList.iterator();
		while(it.hasNext()){
			Element el = it.next();
			
			String checktype = el.getAttributeValue("checktype");
			String value = el.getText();
			
			SqlConstrainConfigParam param = new SqlConstrainConfigParam();
			param.setChecktype(checktype);
			param.setValue(value);
			
			constrain.addParam(param);
		}
		
		this.constrains.add(constrain);
	}
	
	public void addTriggerFromElement(Element triggerElement)
	{
		SqlTriggerConfig trigger = new SqlTriggerConfig();
		
		String tableName = triggerElement.getAttributeValue("table");
		trigger.setTableName(tableName);
		
		String timings = triggerElement.getAttributeValue("timing");
		String tm[] = timings.split(",");
		for(int i = 0; i < tm.length; i++){
			trigger.addTiming(tm[i].trim().toLowerCase());
		}
		
		Element modEl = triggerElement.getChild("module");
		if(modEl != null){
			trigger.setModule(modEl.getText());
		}
		
		Element modfunc = triggerElement.getChild("functioncall");
		if(modEl != null){
			trigger.setFunctioncall(modfunc.getText());
		}
		
		this.triffers.add(trigger);
		
	}

	public List<SqlConstrainConfig> getConstrains()
	{
		return constrains;
	}

	public void setConstrains(List<SqlConstrainConfig> constrains)
	{
		this.constrains = constrains;
	}

	public List<SqlTriggerConfig> getTriffers()
	{
		return triffers;
	}

	public void setTriffers(List<SqlTriggerConfig> triffers)
	{
		this.triffers = triffers;
	}
	
}
