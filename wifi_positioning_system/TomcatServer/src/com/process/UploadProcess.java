package com.process;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.postgresql.util.Base64;

import com.bdd.MapsInterface;


/**
 * This class handles upload requests received by the servlet.
 * It retrieves regular parameters and encode the file sent.
 * The file must be "jpg" or "png" type. With all data sent 
 * in the form, it creates a new record in the database.
 */

public class UploadProcess {

	private MapsInterface mapInt= null;
	private int result=0;
	private byte[] imageByte = null;
    private String name="";
    private int px_h =0;
    private int px_w =0;
    private double meters_w=0.0f;
    private double meters_h=0.0f;
	
	public UploadProcess(HttpServletRequest request){
		
		mapInt = new MapsInterface();

		/* find values given in the form and create new DB entry with.*/

		boolean isMultipartContent = ServletFileUpload.isMultipartContent(request);
		if (!isMultipartContent) {
			System.out.println("UPLOAD - You are not trying to upload");
			return;
		}
		
		System.out.println("UPLOAD - You are trying to upload");
		ServletFileUpload upload = new ServletFileUpload();
		
		try {
		
			//analyze each item of the form.
			FileItemIterator it = upload.getItemIterator(request);
			if (!it.hasNext()) {
				System.out.println("UPLOAD - No fields found");
				return;
			}
			
			while (it.hasNext()) {
				FileItemStream fileItem = it.next();
				
				if (fileItem.isFormField()) {
					//find the other parameters(name, width and height)
					System.out.println("UPLOAD - regular form field - FIELD NAME: " + fileItem.getFieldName() + 
							" content type: " + fileItem.getContentType());
					
					InputStream stream = fileItem.openStream();
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					byte[] buf = new byte[8192];
					int len = 0;
					
					while(-1 != (len=stream.read(buf))) {
						out.write(buf, 0, len);
					}
			          
					if(fileItem.getFieldName().equals("name")){
						name = out.toString();
						//System.out.println("name : " + name);
					}else if(fileItem.getFieldName().equals("meters_h")){
						meters_h = Double.valueOf(out.toString());
						//System.out.println("meters_h : " + meters_h);
					}else if(fileItem.getFieldName().equals("meters_w")){
						meters_w = Double.valueOf(out.toString());
						//System.out.println("meters_w : " + meters_w);
					}else{
						System.out.println("UPLOAD - I don't know this parameter : "+out.toString());
					}
			          
				} else {
					//process the image
					if(fileItem.getContentType().equals("image/jpeg")||
								fileItem.getContentType().equals("image/png")){
						
						InputStream in = fileItem.openStream();
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						byte[] buffer = new byte[8096];
						int size;
	
						//copy data of the file in an ByteArray
						while ((size = in.read(buffer, 0, buffer.length)) != -1) {
							out.write(buffer, 0, size);
						}
						
						imageByte = out.toByteArray();
			            
						//re-create an image to get its dimensions (width and height)
						in = new ByteArrayInputStream(imageByte);
						BufferedImage img = ImageIO.read(in);
						px_h = img.getHeight();
						px_w = img.getWidth();
						
						//System.out.println(" image property : px_h=" + px_h+" - px_w="+ px_w);
						
						//create a new maps in the database
						result = mapInt.insertMaps(name, px_w, px_h, meters_w, meters_h, Base64.encodeBytes(imageByte));
					}else{
						System.out.println("UPLOAD - The file uploaded is not an image !");
						return;
					}
				}
			}
		} catch (FileUploadException | IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * the result code after insertion.
	 * @return  0=no map created, 1=one new map in the database.
	 */
	public int getResult(){
		return result;
	}
}
