/**
 * CROSSFIRE JAPAN INCORPORATED
 * This source code is under GPL License.
 * info@crossfire.jp
 * Official web site
 * http://alinous.org
 * 
 *  Copyright (C) 2007 Tomohiro Iizuka
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
package org.alinous.tools.img;

import java.awt.Graphics;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.alinous.cloud.file.AlinousFile;
import org.alinous.cloud.file.AlinousFileOutputStream;



public class ImageManager
{
	static final float[][] operator={
	    { 0.0125f/4f,  0.0125f/4f,  0.0125f/4f,  //operator[0] 平滑化
	        0.0125f/4f,  0.975f,  0.0125f/4f,
	        0.0125f/4f,  0.0125f/4f,  0.0125f/4f},

	    { 0.0125f/2f,  0.0125f/2f,  0.0125f/2f,  //operator[0] 平滑化
	        0.0125f/2f,  0.95f,  0.0125f/2f,
	        0.0125f/2f,  0.0125f/2f,  0.0125f/2f},
	     
        { // 隣り合う画素の濃度値を大きくする
            -0.1f, -0.1f, -0.1f, -0.1f, -0.1f,
            -0.1f, -0.1f, -0.1f, -0.1f, -0.1f,
            -0.1f, -0.1f,  3.4f, -0.1f, -0.1f,
            -0.1f, -0.1f, -0.1f, -0.1f, -0.1f,
            -0.1f, -0.1f, -0.1f, -0.1f, -0.1f,
          },
          { // 隣り合う画素の濃度値を大きくする
              0f, -0.25f,  0f,
             -0.25f,  2f, -0.25f,
              0f, -0.25f,  0f,
           },
          { // 隣り合う画素の濃度値を大きくする
              0f, -0.125f,  0f,
             -0.125f,  1.5f, -0.125f,
              0f, -0.125f,  0f,
           }


	};

    
	HashMap<String, ImageReader> imageReaders = new HashMap<String, ImageReader>();

	public ImageManager()
	{
	}
/*
	private ImageReader getImageReaders(String filename)
	{
		imageReaders = new HashMap<String, ImageReader>();
		Iterator<ImageReader> tmpReaders = ImageIO
				.getImageReadersByFormatName(filename.substring(filename
						.lastIndexOf('.') + 1));
		ImageReader imageReader = null;
		
		while (tmpReaders.hasNext()) {
			imageReader = tmpReaders.next();
			imageReaders.put(imageReader.getClass().getName(), imageReader);
		}
		
		return imageReader;
	}
	*/
	private BufferedImage getImage(String fileName) throws IOException
	{
		AlinousFile f = new AlinousFile(fileName);
		BufferedImage im = null;
		
		im = ImageIO.read(f);
		 
		return im;
	}
	
	private void writeImage(BufferedImage im, String dstFile, String pictType)
	{
		AlinousFile f = new AlinousFile(dstFile);
        OutputStream out = null;
        try {
            out = new AlinousFileOutputStream(f);
            ImageIO.write(im, pictType, out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                out.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

	}
	
	public ImgDescriptor getDescriptor(String fileName) throws IOException
	{
		BufferedImage img = getImage(fileName);
		
		int width = img.getWidth();
		int height = img.getHeight();
		
		ImgDescriptor desc = new ImgDescriptor();
		desc.setHeight(height);
		desc.setWidth(width);
		
		return desc;
	}
	
	public void resizeImage(String fileName, String dstFile, int dstWidth, int dstHeight) throws IOException
	{
		int extIdx = dstFile.lastIndexOf('.') + 1;
		String type = dstFile.substring(extIdx, dstFile.length());
		
		BufferedImage srcImg = getImage(fileName);
		/*
		double width = srcImg.getWidth();
		double height = srcImg.getHeight();
		*/
		double sx = ((double)dstWidth) / ((double)srcImg.getWidth());
		//double xy = ((double)dstHeight) / ((double)srcImg.getHeight());
		
		if(sx >= 1){
			resizeImageLarger(fileName, dstFile, dstWidth, dstHeight);
			return;
		}
		
		
		/*
		// into smaller image
		double phaseR[] = {0.8, 0.64, 0.512, 0.496, 0.32768};
		
		if(sx < phaseR[0]){
			srcImg = doAffine(srcImg, width, height, width*phaseR[0], height * phaseR[0]);
			width = srcImg.getWidth()*phaseR[0];
			height = srcImg.getWidth()*phaseR[0];
			
			srcImg = normalize(srcImg);
			srcImg = makeConvolution(srcImg, operator[0], 0);
			srcImg = makeConvolution(srcImg, operator[4], 0);
		}
		if(sx < phaseR[1]){
			srcImg = doAffine(srcImg, width, height, width*phaseR[1]/phaseR[0], height * phaseR[1]/phaseR[0]);
			width = width * phaseR[1]/phaseR[0];
			height = height * phaseR[1]/phaseR[0];
			
			//srcImg = normalize(srcImg);
			//srcImg = makeConvolution(srcImg, operator[1], 0);
			//srcImg = makeConvolution(srcImg, operator[3], 0);
		}
		if(sx < phaseR[2]){
			srcImg = doAffine(srcImg, width, height, width* phaseR[2]/(phaseR[1]/phaseR[0]), height * phaseR[2]/(phaseR[1]/phaseR[0]));
			width = width * phaseR[2]/(phaseR[1]/phaseR[0]);
			height = height * phaseR[2]/(phaseR[1]/phaseR[0]);
			
			//srcImg = normalize(srcImg);
			//srcImg = makeConvolution(srcImg, operator[1], 0);
			//srcImg = makeConvolution(srcImg, operator[3], 0);
		}
		if(sx < phaseR[3]){
			srcImg = doAffine(srcImg, width, height, width* phaseR[3]/(phaseR[2]/(phaseR[1]/phaseR[0])), height * phaseR[3]/(phaseR[2]/(phaseR[1]/phaseR[0])));
			width = width *  phaseR[3]/(phaseR[2]/(phaseR[1]/phaseR[0]));
			height = height *  phaseR[3]/(phaseR[2]/(phaseR[1]/phaseR[0]));
		}/*
		if(sx < phaseR[4]){
			srcImg = doAffine(srcImg, width, height, width* phaseR[4]/(phaseR[3]/(phaseR[2]/(phaseR[1]/phaseR[0]))), height * phaseR[4]/(phaseR[3]/(phaseR[2]/(phaseR[1]/phaseR[0]))));
			width = width *  phaseR[4]/(phaseR[3]/(phaseR[2]/(phaseR[1]/phaseR[0])));
			height = height *  phaseR[4]/(phaseR[3]/(phaseR[2]/(phaseR[1]/phaseR[0])));
		}*/
		/*
		srcImg = doAffine(srcImg, width, height, dstWidth, dstWidth);
		
		srcImg = normalize(srcImg);
		srcImg = makeConvolution(srcImg, operator[1], 0);
		srcImg = makeConvolution(srcImg, operator[3], 0);
		//srcImg = sharpness(srcImg, operator[2], 0);
		*/
		srcImg = doAffine(srcImg, srcImg.getWidth(), srcImg.getHeight(), dstWidth, dstWidth);
		
		BufferedImage destImg = new BufferedImage(dstWidth, dstHeight, srcImg.getType());	
		
		Graphics g = destImg.createGraphics();
		
		g.drawImage(srcImg, 0, 0, dstWidth, dstHeight, 0, 0, dstWidth, dstHeight, null);		
		
		g.dispose();
		
		writeImage(destImg, dstFile, type);
	}
	
	public void resizeImageLarger(String fileName, String dstFile, int dstWidth, int dstHeight) throws IOException
	{
		int extIdx = dstFile.lastIndexOf('.') + 1;
		String type = dstFile.substring(extIdx, dstFile.length());
		
		BufferedImage img = getImage(fileName);
		
		BufferedImage destImg = new BufferedImage(dstWidth, dstHeight, img.getType());
		
		Graphics g = destImg.createGraphics();
		
		g.drawImage(img, 0, 0, dstWidth, dstHeight, null);
		
		g.dispose();
		
		
		writeImage(destImg, dstFile, type);
	}
	/*
	private BufferedImage normalize(BufferedImage src){
		    // ImagingOpException がでるとイヤなので、BufferedImage を作りなおす
		    // in thread "main" java.awt.image.ImagingOpException: Unable to convolve src image
		    //   at java.awt.image.ConvolveOp.filter(ConvolveOp.java:180)
		BufferedImage dst =
		      new BufferedImage(
		      src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = dst.createGraphics();
		g.drawImage(src, 0, 0, null);
		g.dispose();
		return dst;
	}
*/
	
	private BufferedImage doAffine(BufferedImage srcImg, double srcWidth, double srcHeight, double destWidth, double destHeight)
	{
		AffineTransformOp ato = null;
		
		double sx = ((double)destWidth) / ((double)srcWidth);
		double sy = ((double)destHeight) / ((double)srcWidth);
		
		HashMap<Key, Object> hints = new HashMap<Key, Object>();
		hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
		
		ato = new AffineTransformOp(
                AffineTransform.getScaleInstance(sx, sy), new RenderingHints(hints));

		
		BufferedImage destImg = new BufferedImage(srcImg.getWidth(), srcImg.getHeight(), srcImg.getType());
		ato.filter(srcImg, destImg);
		
		return destImg;
	}
	/*
	private BufferedImage sharpness(BufferedImage bimg, float[] operator,int type)
	{
		Kernel kernel=new Kernel(5,5,operator);
		ConvolveOp convop;
	      if(type==1) 
	    	  convop=new ConvolveOp(kernel, ConvolveOp.EDGE_ZERO_FILL, null);
	      else
	         convop=new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
	    
	    BufferedImage bimg_dest = convop.filter(bimg, null);
	    
	    return bimg_dest;
	}*/
	
	public BufferedImage makeConvolution(BufferedImage bimg, float[] operator,int type)
	{	
		Kernel kernel=new Kernel(3,3,operator);
		ConvolveOp convop;
	      if(type==1) 
	    	  convop=new ConvolveOp(kernel, ConvolveOp.EDGE_ZERO_FILL, null);
	      else
	         convop=new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
	    
	    BufferedImage bimg_dest = convop.filter(bimg, null);
	    
	    return bimg_dest;
	}
	/*
	private int getBufferedImageType(String type)
	{
		if(type.toUpperCase().equals("JPEG") || type.toUpperCase().equals("JPG")){
			return BufferedImage.TYPE_INT_RGB;
		}
		
		return BufferedImage.TYPE_INT_ARGB;
	}*/
}
