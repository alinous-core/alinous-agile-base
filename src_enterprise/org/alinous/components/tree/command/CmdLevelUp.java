package org.alinous.components.tree.command;

import java.util.Iterator;
import java.util.List;

import org.alinous.AlinousUtils;
import org.alinous.components.tree.model.NodeModel;
import org.alinous.components.tree.model.NodeModelUtils;
import org.alinous.components.tree.model.NodeTreeRenderModel;
import org.alinous.datasrc.DataSrcConnection;
import org.alinous.datasrc.exception.DataSourceException;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.script.runtime.VariableRepository;

public class CmdLevelUp implements INodeTreeCommand
{
	private NodeTreeRenderModel renderer;
	private PostContext context;
	
	public CmdLevelUp(NodeTreeRenderModel renderer, PostContext context)
	{
		this.renderer = renderer;
		this.context = context;
	}
	
	public void execute(VariableRepository valrepo) throws DataSourceException, ExecutionException, RedirectRequestException
	{
		// writing command
		if(!this.renderer.isEditable()){
			return;
		}

		DataSrcConnection con = this.renderer.getDataSrcConnection(this.context);
		
		try{
			NodeModel model = NodeModelUtils.getModelSingle(con, this.renderer.getConfig(), this.renderer.getNodeId());
		
			doLevelUp(con, model);
		}finally{
			con.close();
		}
		
		String path = context.getRequestPath() + ".html";
		path = AlinousUtils.getNotOSPath(path);
		path = context.getFilePath(path);
		
		RedirectRequestException e = new RedirectRequestException(path, "302");
		throw e;
	}
	
	private void doLevelUp(DataSrcConnection con, NodeModel model) throws ExecutionException, DataSourceException
	{
		NodeModel parentModel = NodeModelUtils.getParentModel(con, this.renderer.getConfig(), model);
		NodeModel grandParentModel = NodeModelUtils.getParentModel(con, this.renderer.getConfig(), parentModel);
		
		// make last brothers model's children
		List<NodeModel> lastBrosList = NodeModelUtils.getBrothers(con, this.renderer.getConfig(), model);
		Iterator<NodeModel> itLast = lastBrosList.iterator();
		int pos = 0;
		while(itLast.hasNext()){
			NodeModel brosModel = itLast.next();
			
			if(brosModel.getPosInLevel() > model.getPosInLevel()){
				brosModel.setParentId(model.getId());
				brosModel.setPosInLevel(pos++);
				NodeModelUtils.updateModel(con, this.renderer.getConfig(), brosModel);
				
				model.addNumChildren(1);
				parentModel.addNumChildren(-1);
			}

		}
				
		// parent relations
		if(grandParentModel == null){
			model.setParentId(0);
			parentModel.addNumChildren(-1);
		}else{
			model.setParentId(grandParentModel.getId());
			parentModel.addNumChildren(-1);
			grandParentModel.addNumChildren(1);
			NodeModelUtils.updateModel(con, this.renderer.getConfig(), grandParentModel);
		}
		
		// insert into parent's children
		int nextPos = parentModel.getPosInLevel() + 1;
		
		model.setPosInLevel(nextPos);
		model.addLevel(-1);
		
		// new brothers
		List<NodeModel> brosList = NodeModelUtils.getBrothers(con, this.renderer.getConfig(), parentModel);
		
		Iterator<NodeModel> it = brosList.iterator();
		while(it.hasNext()){
			NodeModel curMd = it.next();
			
			if(curMd.getPosInLevel() >= nextPos){
				curMd.addPosInLevel(1);
				NodeModelUtils.updateModel(con, this.renderer.getConfig(), curMd);
			}
		}
		

		
		// sycn
		NodeModelUtils.updateModel(con, this.renderer.getConfig(), model);
		NodeModelUtils.updateModel(con, this.renderer.getConfig(), parentModel);
	}

	public boolean validate()
	{
		return true;
	}

}
