package org.alinous.script.functions.system;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.alinous.AlinousUtils;
import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.expections.ExecutionException;
import org.alinous.expections.RedirectRequestException;
import org.alinous.objects.IAttribute;
import org.alinous.objects.XMLTagBase;
import org.alinous.objects.html.AlinousTopObject;
import org.alinous.objects.html.FormTagObject;
import org.alinous.repository.AlinousModule;
import org.alinous.script.basic.type.IStatement;
import org.alinous.script.functions.ArgumentDeclare;
import org.alinous.script.runtime.IPathElement;
import org.alinous.script.runtime.IScriptVariable;
import org.alinous.script.runtime.PathElementFactory;
import org.alinous.script.runtime.ScriptDomVariable;
import org.alinous.script.runtime.VariableRepository;
import org.alinous.script.validator.ServerValidationRequest;

public class ValidateCheck extends AbstractSystemFunction{
	
	public static String QUALIFIED_NAME = "VALIDATE.CHECK";
	public static String HTML_PATH = "htmlPath";
	public static String FORM_NAME = "formName";
	
	public ValidateCheck()
	{
		ArgumentDeclare arg = new ArgumentDeclare("$", HTML_PATH);
		this.argmentsDeclare.addArgument(arg);
		arg = new ArgumentDeclare("$", FORM_NAME);
		this.argmentsDeclare.addArgument(arg);
	}
	
	@Override
	public IScriptVariable executeFunction(PostContext context, VariableRepository valRepo)
			throws ExecutionException, RedirectRequestException
	{
		Stack<IStatement> stmtStack = context.getFuncArgStack();
		if(stmtStack.size() != this.argmentsDeclare.getSize()){
			throw new ExecutionException("Number of the function's arguments is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		VariableRepository newValRepo = new VariableRepository();
		handleArguments(context, valRepo, newValRepo);
		
		IPathElement ipath = PathElementFactory.buildPathElement(HTML_PATH);
		IScriptVariable htmlPathVariable = newValRepo.getVariable(ipath, context);
		
		if(!(htmlPathVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		ipath = PathElementFactory.buildPathElement(FORM_NAME);
		IScriptVariable formNameVariable = newValRepo.getVariable(ipath, context);
		
		if(!(formNameVariable instanceof ScriptDomVariable)){
			throw new ExecutionException("Type of the function's argument is wrong : " + QUALIFIED_NAME);// i18n
		}
		
		String moduleName = ((ScriptDomVariable)htmlPathVariable).getValue();
		moduleName = AlinousUtils.getModuleName(moduleName);
		
		String formName = ((ScriptDomVariable)formNameVariable).getValue();
		
		AlinousModule mod;
		try {
			mod = context.getCore().getModuleRepository().getModule(moduleName);
		} catch (AlinousException e) {
			throw new ExecutionException(e, "The html path or the module is worong.");
		}
		AlinousTopObject obj = mod.getDesign();
		
		FormTagObject form = getForm(obj, formName);
		
		if(form == null){
			throw new ExecutionException("Form for validation does not exists."); // i18n
		}
		
		List<ServerValidationRequest> validatorList =  gatherValidators(form, formName, context, valRepo);
		
		// if validation fails, throw ExecutionExeption
		executeValidator(validatorList, moduleName + ".html", context, valRepo);
		
		return null;
	}
	
	
	private void executeValidator(List<ServerValidationRequest> validatorList, String validationHtmlPath,
			PostContext context, VariableRepository valRepo) throws ExecutionException, RedirectRequestException
	{
		Iterator<ServerValidationRequest> it = validatorList.iterator();
		while(it.hasNext()){
			ServerValidationRequest req = it.next();
			req.executeValidation(context, valRepo, validationHtmlPath);
			
			
		}
	}
	
	private List<ServerValidationRequest> gatherValidators(FormTagObject form, String formName,
			PostContext context, VariableRepository valRepo)
	{
		List<ServerValidationRequest> list = new ArrayList<ServerValidationRequest>();
		
		IAttribute attr = form.getAttribute("action");
		if(attr == null){
			return list;
		}
		String frmAction = attr.getValue().getParsedValue(context, valRepo);
		
		Stack<XMLTagBase> stack = new Stack<XMLTagBase>();
		stack.push(form);
		
		while(!stack.isEmpty()){
			XMLTagBase tag = stack.pop();
			
			// get Validator
			List<ServerValidationRequest> reqList = ServerValidationRequest.createRequest(tag, formName, frmAction, context, valRepo);
			if(reqList != null && !reqList.isEmpty()){
				list.addAll(reqList);
			}
			
			// children
			Iterator<XMLTagBase> it = tag.getInnerTags().iterator();
			while(it.hasNext()){
				XMLTagBase chTag = it.next();
				stack.push(chTag);
			}
		}
		
		
		return list;
	}
	
	private FormTagObject getForm(AlinousTopObject obj, String formName)
	{
		Stack<XMLTagBase> stack = new Stack<XMLTagBase>();
		stack.push(obj);
		
		while(!stack.isEmpty()){
			XMLTagBase tag = stack.pop();
			
			// check the tag itself
			if(tag instanceof FormTagObject){
				String name = ((FormTagObject)tag).getName();
				
				if(name != null && name.equals(formName)){
					return (FormTagObject)tag;
				}
			}
			
			// put childern on the top of the stack
			Iterator<XMLTagBase> it = tag.getInnerTags().iterator();
			while(it.hasNext()){
				XMLTagBase chTag = it.next();
				
				stack.push(chTag);
			}
		}
		
		
		return null;
	}
	
	@Override
	public String getName()
	{
		return QUALIFIED_NAME;
	}
	
	@Override
	public String codeAssistString() {
		return "Validate.check($htmlPath, $formName)";
	}

	@Override
	public String descriptionString() {
		return "Validate http or https parameters with the form's validation setting.";
	}

}
