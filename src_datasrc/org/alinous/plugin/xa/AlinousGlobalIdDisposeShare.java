package org.alinous.plugin.xa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alinous.AlinousCore;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;

public class AlinousGlobalIdDisposeShare {
	private List<AlinousRegisterdGlobalId> globalIdList = new ArrayList<AlinousRegisterdGlobalId>();
	private List<Object> holders = new ArrayList<Object>();
	
	
	public void addHolder(Object holder)
	{
		this.holders.add(holder);
	}
	
	
	/**
	 * application register
	 * @param holder
	 * @param gid
	 */
	public void register(Object holder, AlinousRegisterdGlobalId gid)
	{
		this.globalIdList.add(gid);
	}
	
	/**
	 * application remove
	 * @param holder
	 * @param gid
	 */
	public void remove(Object holder, AlinousRegisterdGlobalId gid)
	{
		this.globalIdList.remove(gid);
	}
	
	/**
	 * PostContext Dispose
	 * @param holder
	 * @param core
	 * @throws ExecutionException 
	 */
	public void removeWithDispose(Object holder, AlinousCore core) throws ExecutionException
	{
		this.holders.remove(holder);
		if(!this.holders.isEmpty()){
			return;
		}
		
		Iterator<AlinousRegisterdGlobalId> it = this.globalIdList.iterator();
		while(it.hasNext()){
			AlinousRegisterdGlobalId gid = it.next();
			PostContext context = PostContext.createDummyContext(core);
			 
			DataSrcConnection con = null;
			try {
				con = core.getDataSourceManager().connect(gid.getDataSource(), context);
			} catch (DataSourceException e) {
				throw new ExecutionException(e, "Connection failed on dispose global transaction");
			}
			
			try {
				con.rollback(gid.getGlobalTrxId());
			} catch (DataSourceException e) {
				core.getLogger().reportError(e);
			}
			
			core.getLogger().reportInfo("WARNING DELETED PREPARED TRANSACTION : " + gid.getGlobalTrxId());
			
			it.remove();
		}
		
	}
	
	public String getStatusString()
	{
		StringBuffer buff = new StringBuffer();
		
		
		Iterator<AlinousRegisterdGlobalId> it = this.globalIdList.iterator();
		while(it.hasNext()){
			AlinousRegisterdGlobalId gid = it.next();
			
			buff.append("gid: ").append(gid.getDataSource()).append("->").append(gid.getGlobalTrxId())
			.append(" owners :");
			
			Iterator<Object> oit = this.holders.iterator();
			while(oit.hasNext()){
				Object holder = oit.next();
				
				buff.append(" ").append(holder);
			}
			
			buff.append("\n");
		}
		
		
		return buff.toString();
	}
	
}
