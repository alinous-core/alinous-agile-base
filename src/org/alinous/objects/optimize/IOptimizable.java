package org.alinous.objects.optimize;

import java.io.IOException;

import org.alinous.exec.pages.PostContext;
import org.alinous.expections.AlinousException;
import org.alinous.objects.XMLTagBase;


public interface IOptimizable
{
	public void optimizeSelf(PostContext context, XMLTagBase owner, boolean forceDynamic) throws IOException, AlinousException;
}
