package org.alfresco.extension.pdftoolkit.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.extension.pdftoolkit.constants.PDFToolkitConstants;
import org.alfresco.extension.pdftoolkit.model.PDFToolkitModel;
import org.alfresco.extension.pdftoolkit.repo.action.executer.BasePDFStampActionExecuter;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFMergerUtility;
import org.apache.pdfbox.util.Splitter;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * This class is not used is a remidner of the old method
 * @deprecated use instead {@link PDFToolkitServiceImpl}
 * @author 4535992
 *
 */
public class PDFToolkitUtils extends PDFToolkitServiceImpl{
	
	  private static Log logger = LogFactory.getLog(PDFToolkitUtils.class);
	
	  /**
	   * @param reader
	   * @param writer
	   * @param options
	   * @throws Exception
	   */
	  protected final void appendPDFOLD(Action ruleAction, NodeRef actionedUponNodeRef, NodeRef targetNodeRef, ContentReader reader,
	          ContentReader targetContentReader, Map<String, Object> options)
	  {
	      PDDocument pdf = null;
	      PDDocument pdfTarget = null;
	      InputStream is = null;
	      InputStream tis = null;
	      
	      try
	      {
	          is = reader.getContentInputStream();
	          tis = targetContentReader.getContentInputStream();
	          // stream the document in
	          pdf = PDDocument.load(is);
	          pdfTarget = PDDocument.load(tis);
	          // Append the PDFs
	          PDFMergerUtility merger = new PDFMergerUtility();
	          merger.appendDocument(pdfTarget, pdf);
	          merger.setDestinationFileName(options.get(PDFToolkitConstants.PARAM_DESTINATION_NAME).toString());
	          merger.mergeDocuments();
	          boolean inplace = (boolean) options.get(PDFToolkitConstants.PARAM_INPLACE);
	          updateMergedPdfInRepository(ruleAction, actionedUponNodeRef, targetNodeRef, pdfTarget, reader.getEncoding(), inplace);
	
	      }
	      catch (IOException e)
	      {
	          throw new AlfrescoRuntimeException(e.getMessage(), e);
	      }
	      catch (COSVisitorException e)
	      {
	          throw new AlfrescoRuntimeException(e.getMessage(), e);
	      }
	
	      finally
	      {
	          if (pdf != null)
	          {
	              try
	              {
	                  pdf.close();
	              }
	              catch (IOException e)
	              {
	                  throw new AlfrescoRuntimeException(e.getMessage(), e);
	              }
	          }
	          if (pdfTarget != null)
	          {
	              try
	              {
	                  pdfTarget.close();
	              }
	              catch (IOException e)
	              {
	                  throw new AlfrescoRuntimeException(e.getMessage(), e);
	              }
	          }
	          if (is != null)
	          {
	              try
	              {
	                  is.close();
	              }
	              catch (IOException e)
	              {
	                  throw new AlfrescoRuntimeException(e.getMessage(), e);
	              }
	          }
	
	      }
	  }
	
	  public void updateMergedPdfInRepository(Action ruleAction, NodeRef actionedUponNodeRef,
	          NodeRef targetNodeRef, PDDocument mergedPdfDoc, String encoding, boolean inplace)
	  {
	      NodeService ns = serviceRegistry.getNodeService();
	      ContentWriter writer = null;
	      // build a temp dir name based on the ID of the noderef we are
	      // importing
	      File alfTempDir = TempFileProvider.getTempDir();
	      File tempDir = new File(alfTempDir.getPath() + File.separatorChar + actionedUponNodeRef.getId());
	      tempDir.mkdir();
	
	      Serializable providedName = ruleAction.getParameterValue(PDFToolkitConstants.PARAM_DESTINATION_NAME);
	      String fileName = null;
	      if(providedName != null)
	      {
	          fileName = String.valueOf(providedName) + ".pdf";
	      }
	      else
	      {
	          fileName = String.valueOf(ns.getProperty(targetNodeRef, ContentModel.PROP_NAME));
	      }
	
	
	      try
	      {
	          mergedPdfDoc.save(tempDir + "" + File.separatorChar + fileName);
	          File file = tempDir.listFiles()[0];
	          if (file.isFile())
	          {
	              // Get a writer and prep it for putting it back into the
	              // repo
	              NodeRef destinationNode = createDestinationNode(fileName, 
	                      (NodeRef)ruleAction.getParameterValue(PDFToolkitConstants.PARAM_DESTINATION_FOLDER), targetNodeRef, inplace);
	              writer = serviceRegistry.getContentService().getWriter(destinationNode, ContentModel.PROP_CONTENT, true);
	
	              writer.setEncoding(encoding);
	              // encoding
	              writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
	
	              // Put it in the repo
	              writer.putContent(file);
	
	              // Clean up
	              file.delete();
	          }
	      }
	      catch (ArrayIndexOutOfBoundsException e)
	      {
	          throw new AlfrescoRuntimeException("Failed to process file.", e);
	      }
	      catch (FileExistsException e)
	      {
	          throw new AlfrescoRuntimeException("Failed to process file.", e);
	      }
	      catch (COSVisitorException e)
	      {
	          throw new AlfrescoRuntimeException(e.getMessage(), e);
	      }
	      catch (IOException e)
	      {
	          throw new AlfrescoRuntimeException("Failed to process file.", e);
	      }
	      finally 
	      {
	          if (tempDir != null)
	          {
	              tempDir.delete();
	          }
	      }
	
	  }
	  
	  
	  // ===================================================================================
	  
	  	/**
		 * Delete the requested pages from the PDF doc and save it to the destination location.
		 * 
		 * @param action
		 * @param actionedUponNodeRef
		 * @param reader
		 */
		public void doDelete(Action action, NodeRef actionedUponNodeRef, ContentReader reader)
		{
			InputStream is = null;
	        File tempDir = null;
	        ContentWriter writer = null;
	        PdfReader pdfReader = null;
	        NodeService ns = serviceRegistry.getNodeService();
	        
	        try
	        {
	            is = reader.getContentInputStream();

	            File alfTempDir = TempFileProvider.getTempDir();
	            tempDir = new File(alfTempDir.getPath() + File.separatorChar + actionedUponNodeRef.getId());
	            tempDir.mkdir();
	            
	            Serializable providedName = action.getParameterValue(PARAM_DESTINATION_NAME);
	            Boolean inplace = Boolean.valueOf(String.valueOf(action.getParameterValue(PARAM_INPLACE)));
	            
	            String fileName = null;
	            if(providedName != null)
	            {
	            	fileName = String.valueOf(providedName);
	            }
	            else
	            {
	            	fileName = String.valueOf(ns.getProperty(actionedUponNodeRef, ContentModel.PROP_NAME)) + "-pagesDeleted";
	            }
	            
	            File file = new File(tempDir, serviceRegistry.getFileFolderService().getFileInfo(actionedUponNodeRef).getName());

	            pdfReader = new PdfReader(is);
	            Document doc = new Document(pdfReader.getPageSizeWithRotation(1));
	            PdfCopy copy = new PdfCopy(doc, new FileOutputStream(file));
	            doc.open();

	            List<Integer> toDelete = parseDeleteList(action.getParameterValue(PARAM_PAGE).toString());
	            
	            for (int pageNum = 1; pageNum <= pdfReader.getNumberOfPages(); pageNum++) 
	            {
	            	if (!toDelete.contains(pageNum)) {
	            		copy.addPage(copy.getImportedPage(pdfReader, pageNum));
	            	}
	            }
	            doc.close();

	            NodeRef destinationNode = createDestinationNode(fileName, 
	            		(NodeRef)action.getParameterValue(PARAM_DESTINATION_FOLDER), actionedUponNodeRef, inplace);
	            writer = serviceRegistry.getContentService().getWriter(destinationNode, ContentModel.PROP_CONTENT, true);

	            writer.setEncoding(reader.getEncoding());
	            writer.setMimetype(FILE_MIMETYPE);

	            // Put it in the repository
	            writer.putContent(file);

	            // Clean up
	            file.delete();

	        }
	        catch (IOException e)
	        {
	            throw new AlfrescoRuntimeException(e.getMessage(), e);
	        } 
	        catch (DocumentException e) 
	        {
				throw new AlfrescoRuntimeException(e.getMessage(), e);
			}
	        catch (Exception e)
	        {
	        	throw new AlfrescoRuntimeException(e.getMessage(), e);
	        }
	        finally
	        {
	            if (pdfReader != null)
	            {
	            	pdfReader.close();
	            }
	            if (is != null)
	            {
	                try
	                {
	                    is.close();
	                }
	                catch (IOException e)
	                {
	                    throw new AlfrescoRuntimeException(e.getMessage(), e);
	                }
	            }

	            if (tempDir != null)
	            {
	                tempDir.delete();
	            }
	        }
		}
		
		/**
		 * Parses the list of pages or page ranges to delete and returns a list of page numbers 
		 * 
		 * @param list
		 * @return
		 */
		private List<Integer> parseDeleteList(String list)
		{
			List<Integer> toDelete = new ArrayList<Integer>();
			String[] tokens = list.split(",");
			for(String token : tokens)
			{
				//parse each, if one is not an int, log it but keep going
				try 
				{
					toDelete.add(Integer.parseInt(token));
				}
				catch(NumberFormatException nfe)
				{
					logger.warn("Delete list contains non-numeric values");
				}
			}
			return toDelete;
		}
	  
		   /**
	     * @see org.alfresco.repo.action.executer.TransformActionExecuter#doTransform(org.alfresco.service.cmr.action.Action,
	     * org.alfresco.service.cmr.repository.ContentReader,
	     * org.alfresco.service.cmr.repository.ContentWriter)
	     */
	    public void doEncrypt(Action ruleAction, NodeRef actionedUponNodeRef, ContentReader actionedUponContentReader,boolean useAspect)
	    {

	        Map<String, Object> options = new HashMap<String, Object>();

	        options.put(PARAM_DESTINATION_FOLDER, ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER));
	        options.put(PARAM_USER_PASSWORD, ruleAction.getParameterValue(PARAM_USER_PASSWORD));
	        options.put(PARAM_OWNER_PASSWORD, ruleAction.getParameterValue(PARAM_OWNER_PASSWORD));
	        options.put(PARAM_ALLOW_PRINT, ruleAction.getParameterValue(PARAM_ALLOW_PRINT));
	        options.put(PARAM_ALLOW_COPY, ruleAction.getParameterValue(PARAM_ALLOW_COPY));
	        options.put(PARAM_ALLOW_CONTENT_MODIFICATION, ruleAction.getParameterValue(PARAM_ALLOW_CONTENT_MODIFICATION));
	        options.put(PARAM_ALLOW_ANNOTATION_MODIFICATION, ruleAction.getParameterValue(PARAM_ALLOW_ANNOTATION_MODIFICATION));
	        options.put(PARAM_ALLOW_FORM_FILL, ruleAction.getParameterValue(PARAM_ALLOW_FORM_FILL));
	        options.put(PARAM_ALLOW_SCREEN_READER, ruleAction.getParameterValue(PARAM_ALLOW_SCREEN_READER));
	        options.put(PARAM_ALLOW_DEGRADED_PRINT, ruleAction.getParameterValue(PARAM_ALLOW_DEGRADED_PRINT));
	        options.put(PARAM_ALLOW_ASSEMBLY, ruleAction.getParameterValue(PARAM_ALLOW_ASSEMBLY));
	        options.put(PARAM_ENCRYPTION_LEVEL, ruleAction.getParameterValue(PARAM_ENCRYPTION_LEVEL));
	        options.put(PARAM_EXCLUDE_METADATA, ruleAction.getParameterValue(PARAM_EXCLUDE_METADATA));
	        options.put(PARAM_OPTIONS_LEVEL, ruleAction.getParameterValue(PARAM_OPTIONS_LEVEL));
	        options.put(PARAM_INPLACE, ruleAction.getParameterValue(PARAM_INPLACE));
	        
	        try
	        {
		        PdfStamper stamp = null;
		        File tempDir = null;
		        ContentWriter writer = null;

		        try
		        {
		            // get the parameters
		            String userPassword = (String)options.get(PARAM_USER_PASSWORD);
		            String ownerPassword = (String)options.get(PARAM_OWNER_PASSWORD);
		            Boolean inplace = Boolean.valueOf(String.valueOf(options.get(PARAM_INPLACE)));
		            int permissions = buildPermissionMask(options);
		            int encryptionType = Integer.parseInt((String)options.get(PARAM_ENCRYPTION_LEVEL));

		            // if metadata is excluded, alter encryption type
		            if ((Boolean)options.get(PARAM_EXCLUDE_METADATA))
		            {
		                encryptionType = encryptionType | PdfWriter.DO_NOT_ENCRYPT_METADATA;
		            }

		            // get temp file
		            File alfTempDir = TempFileProvider.getTempDir();
		            tempDir = new File(alfTempDir.getPath() + File.separatorChar + actionedUponNodeRef.getId());
		            tempDir.mkdir();
		            File file = new File(tempDir, serviceRegistry.getFileFolderService().getFileInfo(actionedUponNodeRef).getName());

		            // get the PDF input stream and create a reader for iText
		            PdfReader reader = new PdfReader(actionedUponContentReader.getContentInputStream());
		            stamp = new com.itextpdf.text.pdf.PdfStamper(reader, new FileOutputStream(file));

		            // encrypt PDF
		            stamp.setEncryption(userPassword.getBytes(Charset.forName("UTF-8")), ownerPassword.getBytes(Charset.forName("UTF-8")), permissions, encryptionType);
		            stamp.close();

		            // write out to destination
		            NodeRef destinationNode = createDestinationNode(file.getName(), 
		            		(NodeRef)ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER), actionedUponNodeRef, inplace);
		            writer = serviceRegistry.getContentService().getWriter(destinationNode, ContentModel.PROP_CONTENT, true);

		            writer.setEncoding(actionedUponContentReader.getEncoding());
		            writer.setMimetype(FILE_MIMETYPE);
		            writer.putContent(file);
		            file.delete();
		            
		            //if useAspect is true, store some additional info about the signature in the props
		            if(useAspect)
		            {
		            	serviceRegistry.getNodeService().addAspect(destinationNode, PDFToolkitModel.ASPECT_ENCRYPTED, new HashMap<QName, Serializable>());
		            	serviceRegistry.getNodeService().setProperty(destinationNode, PDFToolkitModel.PROP_ENCRYPTIONDATE, new java.util.Date());
		            	serviceRegistry.getNodeService().setProperty(destinationNode, PDFToolkitModel.PROP_ENCRYPTEDBY, AuthenticationUtil.getRunAsUser());
		            }
		            
		        }
		        catch (IOException e)
		        {
		            throw new AlfrescoRuntimeException(e.getMessage(), e);
		        }
		        catch (DocumentException e)
		        {
		            throw new AlfrescoRuntimeException(e.getMessage(), e);
		        }
		        finally
		        {
		            if (tempDir != null)
		            {
		                try
		                {
		                    tempDir.delete();
		                }
		                catch (Exception ex)
		                {
		                    throw new AlfrescoRuntimeException(ex.getMessage(), ex);
		                }
		            }

		            if (stamp != null)
		            {
		                try
		                {
		                    stamp.close();
		                }
		                catch (Exception ex)
		                {
		                    throw new AlfrescoRuntimeException(ex.getMessage(), ex);
		                }
		            }
		        }
	        }
	        catch (AlfrescoRuntimeException e)
	        {
	            throw new AlfrescoRuntimeException(e.getMessage(), e);
	        }
	    }
	    

	    /**
	     * Build the permissions mask for iText
	     * 
	     * @param options
	     * @return
	     */
	    private int buildPermissionMask(Map<String, Object> options)
	    {
	        int permissions = 0;

	        if ((Boolean)options.get(PARAM_ALLOW_PRINT))
	        {
	            permissions = permissions | PdfWriter.ALLOW_PRINTING;
	        }
	        if ((Boolean)options.get(PARAM_ALLOW_COPY))
	        {
	            permissions = permissions | PdfWriter.ALLOW_COPY;
	        }
	        if ((Boolean)options.get(PARAM_ALLOW_CONTENT_MODIFICATION))
	        {
	            permissions = permissions | PdfWriter.ALLOW_MODIFY_CONTENTS;
	        }
	        if ((Boolean)options.get(PARAM_ALLOW_ANNOTATION_MODIFICATION))
	        {
	            permissions = permissions | PdfWriter.ALLOW_MODIFY_ANNOTATIONS;
	        }
	        if ((Boolean)options.get(PARAM_ALLOW_SCREEN_READER))
	        {
	            permissions = permissions | PdfWriter.ALLOW_SCREENREADERS;
	        }
	        if ((Boolean)options.get(PARAM_ALLOW_DEGRADED_PRINT))
	        {
	            permissions = permissions | PdfWriter.ALLOW_DEGRADED_PRINTING;
	        }
	        if ((Boolean)options.get(PARAM_ALLOW_ASSEMBLY))
	        {
	            permissions = permissions | PdfWriter.ALLOW_ASSEMBLY;
	        }
	        if ((Boolean)options.get(PARAM_ALLOW_FORM_FILL))
	        {
	            permissions = permissions | PdfWriter.ALLOW_FILL_IN;
	        }

	        return permissions;
	    }
	    
	    /**
	     * 
	     * Build out the insert call
	     * 
	     */
	    public void doInsert(Action ruleAction, NodeRef actionedUponNodeRef, ContentReader contentReader,
	            ContentReader insertContentReader)
	    {
	        //ContentReader contentReader = getReader(actionedUponNodeRef);
	        //ContentReader insertContentReader = getReader((NodeRef)ruleAction.getParameterValue(PARAM_INSERT_CONTENT));

	        Map<String, Object> options = new HashMap<String, Object>();
	        options.put(PARAM_DESTINATION_NAME, ruleAction.getParameterValue(PARAM_DESTINATION_NAME));
	        options.put(PARAM_DESTINATION_FOLDER, ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER));
	        options.put(PARAM_PAGE, ruleAction.getParameterValue(PARAM_PAGE));
	        //options.put(PARAM_INSERT_AT_PAGE, ruleAction.getParameterValue(PARAM_INSERT_AT_PAGE));
	        options.put(PARAM_DESTINATION_NAME, ruleAction.getParameterValue(PARAM_DESTINATION_NAME));
	        options.put(PARAM_INPLACE, ruleAction.getParameterValue(PARAM_INPLACE));
	        
	        try
	        {
	            PDDocument pdf = null;
		        PDDocument insertContentPDF = null;
		        InputStream is = null;
		        InputStream cis = null;
		        File tempDir = null;
		        ContentWriter writer = null;
		        ContentReader reader = getReader(actionedUponNodeRef);
		        try
		        {

		            int insertAt = Integer.valueOf((String)options.get(PARAM_PAGE)).intValue();

		            // Get contentReader inputStream
		            is = reader.getContentInputStream();
		            // Get insertContentReader inputStream
		            cis = insertContentReader.getContentInputStream();
		            // stream the target document in
		            pdf = PDDocument.load(is);
		            // stream the insert content document in
		            insertContentPDF = PDDocument.load(cis);

		            // split the PDF and put the pages in a list
		            Splitter splitter = new Splitter();
		            // Need to adjust the input value to get the split at the right page
		            splitter.setSplitAtPage(insertAt - 1);

		            // Split the pages
		            List<PDDocument> pdfs = splitter.split(pdf);

		            // Build the output PDF
		            PDFMergerUtility merger = new PDFMergerUtility();
		            merger.appendDocument((PDDocument)pdfs.get(0), insertContentPDF);
		            merger.appendDocument((PDDocument)pdfs.get(0), (PDDocument)pdfs.get(1));
		            merger.setDestinationFileName(options.get(PARAM_DESTINATION_NAME).toString());
		            merger.mergeDocuments();

		            // build a temp dir, name based on the ID of the noderef we are
		            // importing
		            File alfTempDir = TempFileProvider.getTempDir();
		            tempDir = new File(alfTempDir.getPath() + File.separatorChar + actionedUponNodeRef.getId());
		            tempDir.mkdir();

		            String fileName = options.get(PARAM_DESTINATION_NAME).toString();
		            Boolean inplace = Boolean.valueOf(String.valueOf(options.get(PARAM_INPLACE)));
		            
		            PDDocument completePDF = (PDDocument)pdfs.get(0);

		            completePDF.save(tempDir + "" + File.separatorChar + fileName + FILE_EXTENSION);

		            try
		            {
		                completePDF.close();
		            }
		            catch (IOException e)
		            {
		                throw new AlfrescoRuntimeException(e.getMessage(), e);
		            }


		            for (File file : tempDir.listFiles())
		            {
		                try
		                {
		                    if (file.isFile())
		                    {

		                        // Get a writer and prep it for putting it back into the
		                        // repo
		                        NodeRef destinationNode = createDestinationNode(file.getName(), 
		                        		(NodeRef)ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER), actionedUponNodeRef, inplace);
		                        writer = serviceRegistry.getContentService().getWriter(destinationNode, ContentModel.PROP_CONTENT, true);
		                        
		                        writer.setEncoding(reader.getEncoding()); // original
		                        // encoding
		                        writer.setMimetype(FILE_MIMETYPE);

		                        // Put it in the repo
		                        writer.putContent(file);

		                        // Clean up
		                        file.delete();
		                    }
		                }
		                catch (FileExistsException e)
		                {
		                    throw new AlfrescoRuntimeException("Failed to process file.", e);
		                }
		            }
		        }
		        // TODO add better handling
		        catch (COSVisitorException e)
		        {
		            throw new AlfrescoRuntimeException(e.getMessage(), e);
		        }
		        catch (IOException e)
		        {
		            throw new AlfrescoRuntimeException(e.getMessage(), e);
		        }

		        finally
		        {
		            if (pdf != null)
		            {
		                try
		                {
		                    pdf.close();
		                }
		                catch (IOException e)
		                {
		                    throw new AlfrescoRuntimeException(e.getMessage(), e);
		                }
		            }
		            if (is != null)
		            {
		                try
		                {
		                    is.close();
		                }
		                catch (IOException e)
		                {
		                    throw new AlfrescoRuntimeException(e.getMessage(), e);
		                }
		            }

		            if (tempDir != null)
		            {
		                tempDir.delete();
		            }
		        }
	        }
	        catch (AlfrescoRuntimeException e)
	        {
	            throw new AlfrescoRuntimeException(e.getMessage(), e);
	        }
	    }

	    /**
	     * 
	     * @param ruleAction
	     * @param actionedUponNodeRef
	     * @param actionedUponContentReader
	     */
	    public void doSignature(Action ruleAction, NodeRef actionedUponNodeRef, ContentReader actionedUponContentReader,boolean useAspect)
	    {

	        NodeRef privateKey = (NodeRef)ruleAction.getParameterValue(PARAM_PRIVATE_KEY);
	        String location = (String)ruleAction.getParameterValue(PARAM_LOCATION);
	        String position = (String)ruleAction.getParameterValue(PARAM_POSITION);
	        String reason = (String)ruleAction.getParameterValue(PARAM_REASON);
	        String visibility = (String)ruleAction.getParameterValue(PARAM_VISIBILITY);
	        String keyPassword = (String)ruleAction.getParameterValue(PARAM_KEY_PASSWORD);
	        String keyType = (String)ruleAction.getParameterValue(PARAM_KEY_TYPE);
	        int height = getInteger(ruleAction.getParameterValue(PARAM_HEIGHT));
	        int width = getInteger(ruleAction.getParameterValue(PARAM_WIDTH));
	        int pageNumber = getInteger(ruleAction.getParameterValue(PARAM_PAGE));
	        
	        // New keystore parameters
	        String alias = (String)ruleAction.getParameterValue(PARAM_ALIAS);
	        String storePassword = (String)ruleAction.getParameterValue(PARAM_STORE_PASSWORD);
	        
	        int locationX = getInteger(ruleAction.getParameterValue(PARAM_LOCATION_X));
	        int locationY = getInteger(ruleAction.getParameterValue(PARAM_LOCATION_Y));
	        
	        Boolean inplace = Boolean.valueOf(String.valueOf(ruleAction.getParameterValue(PARAM_INPLACE)));
	        
	        File tempDir = null;
	        ContentWriter writer = null;
	        KeyStore ks = null;

	        try
	        {
	            // get a keystore instance by
	            if (keyType == null || keyType.equalsIgnoreCase(KEY_TYPE_DEFAULT))
	            {
	                ks = KeyStore.getInstance(KeyStore.getDefaultType());
	            }
	            else if (keyType.equalsIgnoreCase(KEY_TYPE_PKCS12))
	            {
	                ks = KeyStore.getInstance("pkcs12");
	            }
	            else
	            {
	                throw new AlfrescoRuntimeException("Unknown key type " + keyType + " specified");
	            }

	            // open the reader to the key and load it
	            ContentReader keyReader = getReader(privateKey);
	            ks.load(keyReader.getContentInputStream(), storePassword.toCharArray());

	            // set alias
	            // String alias = (String) ks.aliases().nextElement();

	            PrivateKey key = (PrivateKey)ks.getKey(alias, keyPassword.toCharArray());
	            Certificate[] chain = ks.getCertificateChain(alias);

	            // open original pdf
	            ContentReader pdfReader = getReader(actionedUponNodeRef);
	            PdfReader reader = new PdfReader(pdfReader.getContentInputStream());

	            // create temp dir to store file
	            File alfTempDir = TempFileProvider.getTempDir();
	            tempDir = new File(alfTempDir.getPath() + File.separatorChar + actionedUponNodeRef.getId());
	            tempDir.mkdir();
	            File file = new File(tempDir, serviceRegistry.getFileFolderService().getFileInfo(actionedUponNodeRef).getName());

	            FileOutputStream fout = new FileOutputStream(file);
	            PdfStamper stamp = PdfStamper.createSignature(reader, fout, '\0');
	            PdfSignatureAppearance sap = stamp.getSignatureAppearance();

	            // sap.setCrypto(key, chain, null, PdfSignatureAppearance.WINCER_SIGNED);
	            // TODO: Fix encryption, the above is old method, below what you need to use now.
	            // Probably must include option for what you are allowed to do.
	            //stamp.setEncryption();

	            // set reason for signature and location of signer
	            sap.setReason(reason);
	            sap.setLocation(location);

	            if (visibility.equalsIgnoreCase(VISIBILITY_VISIBLE))
	            {
	            	//create the signature rectangle using either the provided position or
	            	//the exact coordinates, if provided
	            	if(position != null && !position.trim().equalsIgnoreCase("") 
	            			&& !position.trim().equalsIgnoreCase(POSITION_MANUAL))
	            	{
	            		Rectangle pageRect = reader.getPageSizeWithRotation(pageNumber);
	            		sap.setVisibleSignature(positionSignature(position, pageRect, width, height), pageNumber, null);
	            	}
	            	else
	            	{
	            		sap.setVisibleSignature(new Rectangle(locationX, locationY, locationX + width, locationY - height), pageNumber, null);
	            	}
	            }

	            stamp.close();

	            //can't use BasePDFActionExecuter.getWriter here need the nodeRef of the destination
	            NodeRef destinationNode = createDestinationNode(file.getName(), 
	            		(NodeRef)ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER), actionedUponNodeRef, inplace);
	            writer = serviceRegistry.getContentService().getWriter(destinationNode, ContentModel.PROP_CONTENT, true);
	            
	            writer.setEncoding(actionedUponContentReader.getEncoding());
	            writer.setMimetype(FILE_MIMETYPE);
	            writer.putContent(file);

	            file.delete();
	            
	            //if useAspect is true, store some additional info about the signature in the props
	            if(useAspect)
	            {
	            	serviceRegistry.getNodeService().addAspect(destinationNode, PDFToolkitModel.ASPECT_SIGNED, new HashMap<QName, Serializable>());
	            	serviceRegistry.getNodeService().setProperty(destinationNode, PDFToolkitModel.PROP_REASON, reason);
	            	serviceRegistry.getNodeService().setProperty(destinationNode, PDFToolkitModel.PROP_LOCATION, location);
	            	serviceRegistry.getNodeService().setProperty(destinationNode, PDFToolkitModel.PROP_SIGNATUREDATE, new java.util.Date());
	            	serviceRegistry.getNodeService().setProperty(destinationNode, PDFToolkitModel.PROP_SIGNEDBY, AuthenticationUtil.getRunAsUser());
	            }
	            
	        }
	        catch (IOException e)
	        {
	            throw new AlfrescoRuntimeException(e.getMessage(), e);
	        }
	        catch (KeyStoreException e)
	        {
	            throw new AlfrescoRuntimeException(e.getMessage(), e);
	        }
	        catch (ContentIOException e)
	        {
	            throw new AlfrescoRuntimeException(e.getMessage(), e);
	        }
	        catch (NoSuchAlgorithmException e)
	        {
	            throw new AlfrescoRuntimeException(e.getMessage(), e);
	        }
	        catch (CertificateException e)
	        {
	            throw new AlfrescoRuntimeException(e.getMessage(), e);
	        }
	        catch (UnrecoverableKeyException e)
	        {
	            throw new AlfrescoRuntimeException(e.getMessage(), e);
	        }
	        catch (DocumentException e)
	        {
	            throw new AlfrescoRuntimeException(e.getMessage(), e);
	        }
	        finally
	        {
	            if (tempDir != null)
	            {
	                try
	                {
	                    tempDir.delete();
	                }
	                catch (Exception ex)
	                {
	                    throw new AlfrescoRuntimeException(ex.getMessage(), ex);
	                }
	            }
	        }
	    }

	    /**
	     * Create a rectangle for the visible signature using the selected position and signature size
	     * 
	     * @param position
	     * @param width
	     * @param height
	     * @return
	     */
	    private Rectangle positionSignature(String position, Rectangle pageRect, int width, int height)
	    {

	    	float pageHeight = pageRect.getHeight();
	    	float pageWidth = pageRect.getWidth();
	    	
	    	Rectangle r = null;
	    	
	    	if (position.equals(POSITION_BOTTOMLEFT))
	    	{
	    		r = new Rectangle(0, height, width, 0);
	    	}
	    	else if (position.equals(POSITION_BOTTOMRIGHT))
	    	{
	    		r = new Rectangle(pageWidth - width, pageHeight, pageWidth, pageHeight - height);
	    	}
	    	else if (position.equals(POSITION_TOPLEFT))
	    	{
	    		r = new Rectangle(0, pageHeight, width, pageHeight - height);
	    	}
	    	else if (position.equals(POSITION_TOPRIGHT))
	    	{
	    		r = new Rectangle(pageWidth - width, height, pageWidth, 0);
	    	}
	    	else if (position.equals(POSITION_CENTER))
	    	{
	    		r = new Rectangle((pageWidth / 2) - (width / 2), (pageHeight / 2) - (height / 2),
	    				(pageWidth / 2) + (width / 2), (pageHeight / 2) + (height / 2));
	    	}

	    	return r;
	    }
	    
	    /**
	     * @see org.alfresco.repo.action.executer.TransformActionExecuter#doTransform(org.alfresco.service.cmr.action.Action,
	     * org.alfresco.service.cmr.repository.ContentReader,
	     * org.alfresco.service.cmr.repository.ContentWriter)
	     */
	    public void doSplit(Action ruleAction, NodeRef actionedUponNodeRef, ContentReader contentReader)
	    {
	        Map<String, Object> options = new HashMap<String, Object>();
	        options.put(PARAM_DESTINATION_FOLDER, ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER));
	        options.put(PARAM_SPLIT_FREQUENCY, ruleAction.getParameterValue(PARAM_SPLIT_FREQUENCY));
	        
	        try
	        {
	        	PDDocument pdf = null;
	 	        InputStream is = null;
	 	        File tempDir = null;
	 	        ContentWriter writer = null;

	 	        try
	 	        {
	 	            // Get the split frequency
	 	            int splitFrequency = 0;

	 	            String splitFrequencyString = options.get(PARAM_SPLIT_FREQUENCY).toString();
	 	            if (!splitFrequencyString.equals(""))
	 	            {
	 	                splitFrequency = Integer.valueOf(splitFrequencyString);
	 	            }

	 	            // Get contentReader inputStream
	 	            is = contentReader.getContentInputStream();
	 	            // stream the document in
	 	            pdf = PDDocument.load(is);
	 	            // split the PDF and put the pages in a list
	 	            Splitter splitter = new Splitter();
	 	            // if the default split is not every page, then set it to the right
	 	            // frequency
	 	            if (splitFrequency > 0)
	 	            {
	 	                splitter.setSplitAtPage(splitFrequency);
	 	            }
	 	            // Split the pages
	 	            List<PDDocument> pdfs = splitter.split(pdf);

	 	            // Lets get reading to walk the list
	 	            Iterator<PDDocument> it = pdfs.iterator();

	 	            // Start page split numbering at
	 	            int page = 1;
	 	            int endPage = 0;

	 	            // build a temp dir name based on the ID of the noderef we are
	 	            // importing
	 	            File alfTempDir = TempFileProvider.getTempDir();
	 	            tempDir = new File(alfTempDir.getPath() + File.separatorChar + actionedUponNodeRef.getId());
	 	            tempDir.mkdir();

	 	            while (it.hasNext())
	 	            {
	 	                // Pulling together the right string split pages
	 	                String pagePlus = "";
	 	                String pg = "_pg";

	 	                // Get the split document and save it into the temp dir with new
	 	                // name
	 	                PDDocument splitpdf = (PDDocument)it.next();

	 	                int pagesInPDF = splitpdf.getNumberOfPages();

	 	                if (splitFrequency > 0)
	 	                {
	 	                    endPage = endPage + pagesInPDF;

	 	                    pagePlus = "-" + endPage;
	 	                    pg = "_pgs";

	 	                }

	 	                // put together the name and save the PDF
	 	                String fileNameSansExt = getFilenameSansExt(actionedUponNodeRef, FILE_EXTENSION);
	 	                splitpdf.save(tempDir + "" + File.separatorChar + fileNameSansExt + pg + page + pagePlus + FILE_EXTENSION);

	 	                // increment page count
	 	                if (splitFrequency > 0)
	 	                {
	 	                    page = (page++) + pagesInPDF;
	 	                }
	 	                else
	 	                {
	 	                    page++;
	 	                }

	 	                try
	 	                {
	 	                    splitpdf.close();
	 	                }
	 	                catch (IOException e)
	 	                {
	 	                    throw new AlfrescoRuntimeException(e.getMessage(), e);
	 	                }

	 	            }

	 	            for (File file : tempDir.listFiles())
	 	            {
	 	                try
	 	                {
	 	                    if (file.isFile())
	 	                    {

	 	                        // Get a writer and prep it for putting it back into the
	 	                        // repo
	 	                        NodeRef destinationNode = createDestinationNode(file.getName(), 
	 	                        		(NodeRef)ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER), actionedUponNodeRef, false);
	 	                        writer = serviceRegistry.getContentService().getWriter(destinationNode, ContentModel.PROP_CONTENT, true);
	 	                        
	 	                        writer.setEncoding(contentReader.getEncoding()); // original
	 	                                                                  // encoding
	 	                        writer.setMimetype(FILE_MIMETYPE);

	 	                        // Put it in the repo
	 	                        writer.putContent(file);

	 	                        // Clean up
	 	                        file.delete();
	 	                    }
	 	                }
	 	                catch (FileExistsException e)
	 	                {
	 	                    throw new AlfrescoRuntimeException("Failed to process file.", e);
	 	                }
	 	            }
	 	        }
	 	        catch (COSVisitorException e)
	 	        {
	 	            throw new AlfrescoRuntimeException(e.getMessage(), e);
	 	        }
	 	        catch (IOException e)
	 	        {
	 	            throw new AlfrescoRuntimeException(e.getMessage(), e);
	 	        }

	 	        finally
	 	        {
	 	            if (pdf != null)
	 	            {
	 	                try
	 	                {
	 	                    pdf.close();
	 	                }
	 	                catch (IOException e)
	 	                {
	 	                    throw new AlfrescoRuntimeException(e.getMessage(), e);
	 	                }
	 	            }
	 	            if (is != null)
	 	            {
	 	                try
	 	                {
	 	                    is.close();
	 	                }
	 	                catch (IOException e)
	 	                {
	 	                    throw new AlfrescoRuntimeException(e.getMessage(), e);
	 	                }
	 	            }

	 	            if (tempDir != null)
	 	            {
	 	                tempDir.delete();
	 	            }
	 	        }
	        }
	        catch (AlfrescoRuntimeException e)
	        {
	            throw new AlfrescoRuntimeException(e.getMessage(), e);
	        }
	    }

	    /**
	     * @see org.alfresco.repo.action.executer.TransformActionExecuter#doTransform(org.alfresco.service.cmr.action.Action,
	     * org.alfresco.service.cmr.repository.ContentReader,
	     * org.alfresco.service.cmr.repository.ContentWriter)
	     */
	    public void doSplitAtPage(Action ruleAction, NodeRef actionedUponNodeRef, ContentReader contentReader)
	    {
	        Map<String, Object> options = new HashMap<String, Object>();
	        options.put(PARAM_DESTINATION_FOLDER, ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER));
	        options.put(PARAM_PAGE, ruleAction.getParameterValue(PARAM_PAGE));

	        try
	        {
	        	PDDocument pdf = null;
		        InputStream is = null;
		        File tempDir = null;
		        ContentWriter writer = null;

		        try
		        {
		            // Get the split frequency
		            int splitFrequency = 0;

		            String splitFrequencyString = options.get(PARAM_PAGE).toString();
		            if (!splitFrequencyString.equals(""))
		            {
		                try
		                {
		                    splitFrequency = Integer.valueOf(splitFrequencyString);
		                }
		                catch (NumberFormatException e)
		                {
		                    throw new AlfrescoRuntimeException(e.getMessage(), e);
		                }
		            }

		            // Get contentReader inputStream
		            is = contentReader.getContentInputStream();
		            // stream the document in
		            pdf = PDDocument.load(is);
		            // split the PDF and put the pages in a list
		            Splitter splitter = new Splitter();
		            // Need to adjust the input value to get the split at the right page
		            splitter.setSplitAtPage(splitFrequency - 1);

		            // Split the pages
		            List<PDDocument> pdfs = splitter.split(pdf);

		            // Start page split numbering at
		            int page = 1;

		            // build a temp dir, name based on the ID of the noderef we are
		            // importing
		            File alfTempDir = TempFileProvider.getTempDir();
		            tempDir = new File(alfTempDir.getPath() + File.separatorChar + actionedUponNodeRef.getId());
		            tempDir.mkdir();

		            // FLAG: This is ugly.....get the first PDF.
		            PDDocument firstPDF = (PDDocument)pdfs.remove(0);

		            int pagesInFirstPDF = firstPDF.getNumberOfPages();

		            String lastPage = "";
		            String pg = "_pg";

		            if (pagesInFirstPDF > 1)
		            {
		                pg = "_pgs";
		                lastPage = "-" + pagesInFirstPDF;
		            }

		            String fileNameSansExt = getFilenameSansExt(actionedUponNodeRef, FILE_EXTENSION);
		            firstPDF.save(tempDir + "" + File.separatorChar + fileNameSansExt + pg + page + lastPage + FILE_EXTENSION);

		            try
		            {
		                firstPDF.close();
		            }
		            catch (IOException e)
		            {
		                throw new AlfrescoRuntimeException(e.getMessage(), e);
		            }

		            // FLAG: Like I said: "_UGLY_" ..... and it gets worse
		            PDDocument secondPDF = null;

		            Iterator<PDDocument> its = pdfs.iterator();

		            int pagesInSecondPDF = 0;

		            while (its.hasNext())
		            {
		                if (secondPDF != null)
		                {
		                    // Get the split document and save it into the temp dir with
		                    // new name
		                    PDDocument splitpdf = (PDDocument)its.next();

		                    int pagesInThisPDF = splitpdf.getNumberOfPages();
		                    pagesInSecondPDF = pagesInSecondPDF + pagesInThisPDF;

		                    PDFMergerUtility merger = new PDFMergerUtility();
		                    merger.appendDocument(secondPDF, splitpdf);
		                    merger.mergeDocuments();


		                    try
		                    {
		                        splitpdf.close();
		                    }
		                    catch (IOException e)
		                    {
		                        throw new AlfrescoRuntimeException(e.getMessage(), e);
		                    }

		                }
		                else
		                {
		                    secondPDF = (PDDocument)its.next();

		                    pagesInSecondPDF = secondPDF.getNumberOfPages();
		                }
		            }

		            if (pagesInSecondPDF > 1)
		            {

		                pg = "_pgs";
		                lastPage = "-" + (pagesInSecondPDF + pagesInFirstPDF);

		            }
		            else
		            {
		                pg = "_pg";
		                lastPage = "";
		            }

		            // This is where we should save the appended PDF
		            // put together the name and save the PDF
		            secondPDF.save(tempDir + "" + File.separatorChar + fileNameSansExt + pg + splitFrequency + lastPage + FILE_EXTENSION);

		            for (File file : tempDir.listFiles())
		            {
		                try
		                {
		                    if (file.isFile())
		                    {
		                        // Get a writer and prep it for putting it back into the
		                        // repo
		                        NodeRef destinationNode = createDestinationNode(file.getName(), 
		                        		(NodeRef)ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER), actionedUponNodeRef, false);
		                        writer = serviceRegistry.getContentService().getWriter(destinationNode, ContentModel.PROP_CONTENT, true);

		                        writer.setEncoding(contentReader.getEncoding()); // original
		                                                                  // encoding
		                        writer.setMimetype(FILE_MIMETYPE);

		                        // Put it in the repo
		                        writer.putContent(file);

		                        // Clean up
		                        file.delete();
		                    }
		                }
		                catch (FileExistsException e)
		                {
		                    throw new AlfrescoRuntimeException("Failed to process file.", e);
		                }
		            }
		        }
		        catch (COSVisitorException e)
		        {
		            throw new AlfrescoRuntimeException(e.getMessage(), e);
		        }
		        catch (IOException e)
		        {
		            throw new AlfrescoRuntimeException(e.getMessage(), e);
		        }

		        finally
		        {
		            if (pdf != null)
		            {
		                try
		                {
		                    pdf.close();
		                }
		                catch (IOException e)
		                {
		                    throw new AlfrescoRuntimeException(e.getMessage(), e);
		                }
		            }
		            if (is != null)
		            {
		                try
		                {
		                    is.close();
		                }
		                catch (IOException e)
		                {
		                    throw new AlfrescoRuntimeException(e.getMessage(), e);
		                }
		            }

		            if (tempDir != null)
		            {
		                tempDir.delete();
		            }
		        }
	        }
	        catch (AlfrescoRuntimeException e)
	        {
	            throw new AlfrescoRuntimeException(e.getMessage(), e);
	        }
	    }
	    /**
	     * @see org.alfresco.repo.action.executer.TransformActionExecuter#doWatermark(org.alfresco.service.cmr.action.Action,
	     * org.alfresco.service.cmr.repository.ContentReader,
	     * org.alfresco.service.cmr.repository.ContentWriter)
	     */
	    protected void doWatermark(Action ruleAction, NodeRef actionedUponNodeRef, ContentReader actionedUponContentReader)
	    {

	        Map<String, Object> options = new HashMap<String, Object>();

	        options.put(PARAM_DESTINATION_FOLDER, ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER));
	        options.put(PARAM_PAGE, ruleAction.getParameterValue(PARAM_PAGE));
	        options.put(PARAM_POSITION, ruleAction.getParameterValue(PARAM_POSITION));
	        options.put(PARAM_WATERMARK_DEPTH, ruleAction.getParameterValue(PARAM_WATERMARK_DEPTH));
	        options.put(PARAM_INPLACE, ruleAction.getParameterValue(PARAM_INPLACE));
	        
	        try
	        {
	            if (ruleAction.getParameterValue(PARAM_WATERMARK_TYPE) != null
	                && ruleAction.getParameterValue(PARAM_WATERMARK_TYPE).equals(TYPE_IMAGE))
	            {

	                NodeRef watermarkNodeRef = (NodeRef)ruleAction.getParameterValue(PARAM_WATERMARK_IMAGE);
	                ContentReader watermarkContentReader = getReader(watermarkNodeRef);

	                // add additional options only used by this specific watermark
	                // type
	                options.put(PARAM_WATERMARK_IMAGE, ruleAction.getParameterValue(PARAM_WATERMARK_IMAGE));

	                //Applies an image watermark
	                PdfStamper stamp = null;
	    	        File tempDir = null;
	    	        ContentWriter writer = null;

	    	        try
	    	        {
	    	            File file = getTempFile(actionedUponNodeRef);

	    	            // get the PDF input stream and create a reader for iText
	    	            PdfReader reader = new PdfReader(actionedUponContentReader.getContentInputStream());
	    	            stamp = new PdfStamper(reader, new FileOutputStream(file));
	    	            PdfContentByte pcb;

	    	            // get a com.itextpdf.text.Image object via java.imageio.ImageIO
	    	            Image img = Image.getInstance(ImageIO.read(watermarkContentReader.getContentInputStream()), null);

	    	            // get the PDF pages and position
	    	            String pages = (String)options.get(PARAM_PAGE);
	    	            String position = (String)options.get(PARAM_POSITION);
	    	            String depth = (String)options.get(PARAM_WATERMARK_DEPTH);
	    	            Boolean inplace = Boolean.valueOf(String.valueOf(options.get(PARAM_INPLACE)));

	    	            // get the manual positioning options (if provided)
	    	            int locationX = getInteger(ruleAction.getParameterValue(PARAM_LOCATION_X));
	    	            int locationY = getInteger(ruleAction.getParameterValue(PARAM_LOCATION_Y));
	    	            
	    	            // image requires absolute positioning or an exception will be
	    	            // thrown
	    	            // set image position according to parameter. Use
	    	            // PdfReader.getPageSizeWithRotation
	    	            // to get the canvas size for alignment.
	    	            img.setAbsolutePosition(100f, 100f);

	    	            // stamp each page
	    	            int numpages = reader.getNumberOfPages();
	    	            for (int i = 1; i <= numpages; i++)
	    	            {
	    	                Rectangle r = reader.getPageSizeWithRotation(i);
	    	                // set stamp position
	    	                if (position.equals(POSITION_BOTTOMLEFT))
	    	                {
	    	                    img.setAbsolutePosition(0, 0);
	    	                }
	    	                else if (position.equals(POSITION_BOTTOMRIGHT))
	    	                {
	    	                    img.setAbsolutePosition(r.getWidth() - img.getWidth(), 0);
	    	                }
	    	                else if (position.equals(POSITION_TOPLEFT))
	    	                {
	    	                    img.setAbsolutePosition(0, r.getHeight() - img.getHeight());
	    	                }
	    	                else if (position.equals(POSITION_TOPRIGHT))
	    	                {
	    	                    img.setAbsolutePosition(r.getWidth() - img.getWidth(), r.getHeight() - img.getHeight());
	    	                }
	    	                else if (position.equals(POSITION_CENTER))
	    	                {
	    	                    img.setAbsolutePosition(getCenterX(r, img), getCenterY(r, img));
	    	                }
	    	                else if (position.equals(POSITION_MANUAL))
	    	                {
	    	                	img.setAbsolutePosition(locationX, locationY);
	    	                }

	    	                // if this is an under-text stamp, use getUnderContent.
	    	                // if this is an over-text stamp, usse getOverContent.
	    	                if (depth.equals(DEPTH_OVER))
	    	                {
	    	                    pcb = stamp.getOverContent(i);
	    	                }
	    	                else
	    	                {
	    	                    pcb = stamp.getUnderContent(i);
	    	                }

	    	                // only apply stamp to requested pages
	    	                if (checkPage(pages, i, numpages))
	    	                {
	    	                    pcb.addImage(img);
	    	                }
	    	            }

	    	            stamp.close();
	    	            
	    	            // Get a writer and prep it for putting it back into the repo
	    	            //can't use BasePDFActionExecuter.getWriter here need the nodeRef of the destination
	    	            NodeRef destinationNode = createDestinationNode(file.getName(), 
	    	            		(NodeRef)ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER), actionedUponNodeRef, inplace);
	    	            writer = serviceRegistry.getContentService().getWriter(destinationNode, ContentModel.PROP_CONTENT, true);
	    	            
	    	            writer.setEncoding(actionedUponContentReader.getEncoding());
	    	            writer.setMimetype(FILE_MIMETYPE);

	    	            // Put it in the repo
	    	            writer.putContent(file);

	    	            // delete the temp file
	    	            file.delete();
	    	        }
	    	        catch (IOException e)
	    	        {
	    	            throw new AlfrescoRuntimeException(e.getMessage(), e);
	    	        }
	    	        catch (DocumentException e)
	    	        {
	    	            throw new AlfrescoRuntimeException(e.getMessage(), e);
	    	        }
	    	        finally
	    	        {
	    	            if (tempDir != null)
	    	            {
	    	                try
	    	                {
	    	                    tempDir.delete();
	    	                }
	    	                catch (Exception ex)
	    	                {
	    	                    throw new AlfrescoRuntimeException(ex.getMessage(), ex);
	    	                }
	    	            }

	    	            if (stamp != null)
	    	            {
	    	                try
	    	                {
	    	                    stamp.close();
	    	                }
	    	                catch (Exception ex)
	    	                {
	    	                    throw new AlfrescoRuntimeException(ex.getMessage(), ex);
	    	                }
	    	            }
	    	        }

	            }
	            else if (ruleAction.getParameterValue(PARAM_WATERMARK_TYPE) != null
	                     && ruleAction.getParameterValue(PARAM_WATERMARK_TYPE).equals(TYPE_TEXT))
	            {

	                // add additional options only used by text types
	                options.put(PARAM_WATERMARK_TEXT, ruleAction.getParameterValue(PARAM_WATERMARK_TEXT));

	                options.put(PARAM_WATERMARK_FONT, ruleAction.getParameterValue(PARAM_WATERMARK_FONT));

	                options.put(PARAM_WATERMARK_SIZE, ruleAction.getParameterValue(PARAM_WATERMARK_SIZE));

	                // Applies a text watermark (current date, user name, etc, depending on options)
	               
	                
	                PdfStamper stamp = null;
	                File tempDir = null;
	                ContentWriter writer = null;
	                String watermarkText;
	                StringTokenizer st;
	                Vector<String> tokens = new Vector<String>();

	                try
	                {
	                    File file = getTempFile(actionedUponNodeRef);

	                    // get the PDF input stream and create a reader for iText
	                    PdfReader reader = new PdfReader(actionedUponContentReader.getContentInputStream());
	                    stamp = new PdfStamper(reader, new FileOutputStream(file));
	                    PdfContentByte pcb;

	                    // get the PDF pages and position
	                    String pages = (String)options.get(PARAM_PAGE);
	                    String position = (String)options.get(PARAM_POSITION);
	                    String depth = (String)options.get(PARAM_WATERMARK_DEPTH);
	                    int locationX = getInteger(ruleAction.getParameterValue(PARAM_LOCATION_X));
	                    int locationY = getInteger(ruleAction.getParameterValue(PARAM_LOCATION_Y));
	                    Boolean inplace = Boolean.valueOf(String.valueOf(options.get(PARAM_INPLACE)));

	                    // create the base font for the text stamp
	                    BaseFont bf = BaseFont.createFont((String)options.get(PARAM_WATERMARK_FONT), BaseFont.CP1250, BaseFont.EMBEDDED);


	                    // get watermark text and process template with model
	                    String templateText = (String)options.get(PARAM_WATERMARK_TEXT);
	                    Map<String, Object> model = buildWatermarkTemplateModel(actionedUponNodeRef);
	                    StringWriter watermarkWriter = new StringWriter();
	                    freemarkerProcessor.processString(templateText, model, watermarkWriter);
	                    watermarkText = watermarkWriter.getBuffer().toString();

	                    // tokenize watermark text to support multiple lines and copy tokens
	                    // to vector for re-use
	                    st = new StringTokenizer(watermarkText, "\r\n", false);
	                    while (st.hasMoreTokens())
	                    {
	                        tokens.add(st.nextToken());
	                    }

	                    // stamp each page
	                    int numpages = reader.getNumberOfPages();
	                    for (int i = 1; i <= numpages; i++)
	                    {
	                        Rectangle r = reader.getPageSizeWithRotation(i);

	                        // if this is an under-text stamp, use getUnderContent.
	                        // if this is an over-text stamp, use getOverContent.
	                        if (depth.equals(DEPTH_OVER))
	                        {
	                            pcb = stamp.getOverContent(i);
	                        }
	                        else
	                        {
	                            pcb = stamp.getUnderContent(i);
	                        }

	                        // set the font and size
	                        float size = Float.parseFloat((String)options.get(PARAM_WATERMARK_SIZE));
	                        pcb.setFontAndSize(bf, size);

	                        // only apply stamp to requested pages
	                        if (checkPage(pages, i, numpages))
	                        {
	                            writeAlignedText(pcb, r, tokens, size, position, locationX, locationY);
	                        }
	                    }

	                    stamp.close();

	                    // Get a writer and prep it for putting it back into the repo
	                    //can't use BasePDFActionExecuter.getWriter here need the nodeRef of the destination
	                    NodeRef destinationNode = createDestinationNode(file.getName(), 
	                    		(NodeRef)ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER), actionedUponNodeRef, inplace);
	                    writer = serviceRegistry.getContentService().getWriter(destinationNode, ContentModel.PROP_CONTENT, true);
	                    writer.setEncoding(actionedUponContentReader.getEncoding());
	                    writer.setMimetype(FILE_MIMETYPE);

	                    // Put it in the repo
	                    writer.putContent(file);

	                    // delete the temp file
	                    file.delete();
	                }
	                catch (IOException e)
	                {
	                    throw new AlfrescoRuntimeException(e.getMessage(), e);
	                }
	                catch (DocumentException e)
	                {
	                    throw new AlfrescoRuntimeException(e.getMessage(), e);
	                }
	                finally
	                {
	                    if (tempDir != null)
	                    {
	                        try
	                        {
	                            tempDir.delete();
	                        }
	                        catch (Exception ex)
	                        {
	                            throw new AlfrescoRuntimeException(ex.getMessage(), ex);
	                        }
	                    }

	                    if (stamp != null)
	                    {
	                        try
	                        {
	                            stamp.close();
	                        }
	                        catch (Exception ex)
	                        {
	                            throw new AlfrescoRuntimeException(ex.getMessage(), ex);
	                        }
	                    }
	                }
	            }
	        }
	        catch (AlfrescoRuntimeException e)
	        {
	            throw new AlfrescoRuntimeException(e.getMessage(), e);
	        }
	    }


}
