package org.alinous.datasrc.api;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.alinous.datasrc.DataSrcConnection;
import org.alinous.script.ISQLSentence;

public class PrecompileBuffer {
	private Map<String, PreCompiledSentence> sentences = new HashMap<String, PrecompileBuffer.PreCompiledSentence>();
	
	
	public void clearPreCompile(String table, DataSrcConnection con)
	{
		PreCompiledSentence last = this.sentences.get(table);
		if(last != null){
			con.clearPreCompile(last.getSentence());
			
			this.sentences.remove(table);
		}		
	}
	
	public ISQLSentence getSentence(String table, ISQLSentence defaultSentence)
	{
		PreCompiledSentence sentence = this.sentences.get(table);
		
		if(sentence == null){
			sentence = new PreCompiledSentence(defaultSentence);
			this.sentences.put(table, sentence);
		}
		
		long mill = System.currentTimeMillis();
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(mill);
		cal.add(Calendar.MINUTE, 10);
		
		mill = cal.getTimeInMillis();
		
		if(mill < sentence.getTime()){
			sentence = new PreCompiledSentence(defaultSentence);
			this.sentences.put(table, sentence);
		}
		
		return sentence.getSentence();
	}
	
	private class PreCompiledSentence
	{
		private ISQLSentence sentence;
		private long time;
		
		public PreCompiledSentence(ISQLSentence sentence)
		{
			this.sentence = sentence;
			this.time = System.currentTimeMillis();
		}
		
		public ISQLSentence getSentence()
		{
			return sentence;
		}

		public long getTime()
		{
			return time;
		}
		
	}
}
