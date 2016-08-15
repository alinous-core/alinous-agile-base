package org.alinous.components.tree;

import java.io.PrintWriter;

public class DoctypeConfig
{
	private String id;
	private String displayName;
	private String editPage;
	private String showPage;
	private String deletePage;
	private String spanClass;
	
	private String folderImage = "/alinous-common/treenode/img/toc_open.gif";
	
	public String getDisplayName()
	{
		return displayName;
	}
	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}
	public String getEditPage()
	{
		return editPage;
	}
	public void setEditPage(String editPage)
	{
		this.editPage = editPage;
	}
	public String getId()
	{
		return id;
	}
	public void setId(String id)
	{
		this.id = id;
	}
	public String getShowPage()
	{
		return showPage;
	}
	public void setShowPage(String showPage)
	{
		this.showPage = showPage;
	}
	public String getDeletePage()
	{
		return deletePage;
	}
	public void setDeletePage(String deletePage)
	{
		this.deletePage = deletePage;
	}
	public String getFolderImage()
	{
		return folderImage;
	}
	public void setFolderImage(String folderImage)
	{
		this.folderImage = folderImage;
	}
	
	public String getSpanClass()
	{
		return spanClass;
	}
	
	public void setSpanClass(String spanClass)
	{
		this.spanClass = spanClass;
	}
	
	public void writeAsString(PrintWriter wr)
	{
		wr.println("			<doc-type>");
		
		// id
		wr.print("				<id>");
		wr.print(bl(getId()));
		wr.println("</id>");
		
		// display-name
		wr.print("				<display-name>");
		wr.print(bl(getDisplayName()));
		wr.println("</display-name>");
		
		// editpage
		wr.print("				<editpage>");
		wr.print(bl(getEditPage()));
		wr.println("</editpage>");
		
		// showpage
		wr.print("				<showpage>");
		wr.print(bl(getShowPage()));
		wr.println("</showpage>");
		
		// deletepage
		wr.print("				<deletepage>");
		wr.print(bl(getDeletePage()));
		wr.println("</deletepage>");
		
		// spanclass
		wr.print("				<spanclass>");
		wr.print(bl(getSpanClass()));
		wr.println("</spanclass>");
		
		// folderimg
		wr.print("				<folderimg>");
		wr.print(bl(getFolderImage()));
		wr.println("</folderimg>");
		
		wr.println("			</doc-type>");
	}
	
	public static String bl(String str)
	{
		return str != null ? str : "";
	}
}
