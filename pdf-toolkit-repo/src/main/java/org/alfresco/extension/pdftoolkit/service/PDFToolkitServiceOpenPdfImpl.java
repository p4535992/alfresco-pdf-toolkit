package org.alfresco.extension.pdftoolkit.service;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.Vector;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.extension.pdftoolkit.constants.PDFToolkitConstants;
import org.alfresco.extension.pdftoolkit.model.PDFToolkitModel;
import org.alfresco.extension.pdftoolkit.naming.FileNameProvider;
import org.alfresco.extension.pdftoolkit.repo.action.executer.PDFAppendActionExecuter;
import org.alfresco.extension.pdftoolkit.repo.action.executer.PDFCompressActionExecuter;
import org.alfresco.extension.pdftoolkit.repo.action.executer.PDFConvertToArchivableActionExecuter;
import org.alfresco.extension.pdftoolkit.repo.action.executer.PDFEncryptionActionExecuter;
import org.alfresco.extension.pdftoolkit.repo.action.executer.PDFInsertAtPageActionExecuter;
import org.alfresco.extension.pdftoolkit.repo.action.executer.PDFSignatureActionExecuter;
import org.alfresco.extension.pdftoolkit.repo.action.executer.PDFSplitActionExecuter;
import org.alfresco.extension.pdftoolkit.repo.action.executer.PDFSplitAtPageActionExecuter;
import org.alfresco.extension.pdftoolkit.repo.action.executer.PDFWatermarkActionExecuter;
import org.alfresco.extension.pdftoolkit.repo.action.executer.PDFConvertToArchivableActionExecuter.ArchiveLevel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.template.FreeMarkerProcessor;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.examples.pdfa.CreatePDFA;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import org.apache.pdfbox.util.PDFMergerUtility;
import org.apache.pdfbox.util.Splitter;
import org.apache.poi.util.TempFile;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.XmpSerializationException;
import org.apache.xmpbox.xml.XmpSerializer;
import org.mozilla.javascript.NativeObject;
import org.springframework.extensions.surf.util.I18NUtil;

//import com.itextpdf.text.Document;
//import com.itextpdf.text.DocumentException;
//import com.itextpdf.text.Image;
//import com.itextpdf.text.Rectangle;
//import com.itextpdf.text.exceptions.InvalidImageException;
//import com.itextpdf.text.exceptions.UnsupportedPdfException;
//import com.itextpdf.text.pdf.BaseFont;
//import com.itextpdf.text.pdf.PRStream;
//import com.itextpdf.text.pdf.PdfContentByte;
//import com.itextpdf.text.pdf.PdfCopy;
//import com.itextpdf.text.pdf.PdfDictionary;
//import com.itextpdf.text.pdf.PdfName;
//import com.itextpdf.text.pdf.PdfNumber;
//import com.itextpdf.text.pdf.PdfObject;
//import com.itextpdf.text.pdf.PdfReader;
//import com.itextpdf.text.pdf.PdfSignatureAppearance;
//import com.itextpdf.text.pdf.PdfStamper;
//import com.itextpdf.text.pdf.PdfWriter;
//import com.itextpdf.text.pdf.parser.PdfImageObject;
//import com.itextpdf.text.pdf.security.BouncyCastleDigest;
//import com.itextpdf.text.pdf.security.ExternalDigest;
//import com.itextpdf.text.pdf.security.ExternalSignature;
//import com.itextpdf.text.pdf.security.MakeSignature;
//import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
//import com.itextpdf.text.pdf.security.PrivateKeySignature;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
//import InvalidImageException
import com.lowagie.text.exceptions.UnsupportedPdfException;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfImage;

public class PDFToolkitServiceOpenPdfImpl extends PDFToolkitConstants implements PDFToolkitService 
{
    private final static String MSGID_PAGE_NUMBERING_PATTERN_MULTIPLE="pdftoolkit.split-page-numbering-pattern-multiple";
    private final static String MSGID_PAGE_NUMBERING_PATTERN_SINGLE="pdftoolkit.split-page-numbering-pattern-single";
	
	protected ServiceRegistry serviceRegistry;
	protected NodeService ns;
	protected ContentService cs;
	protected FileFolderService ffs;
	protected DictionaryService ds;
	protected PersonService ps;
	protected AuthenticationService as;
	protected FileNameProvider fileNameProvider; 
    
    protected FreeMarkerProcessor freemarkerProcessor = new FreeMarkerProcessor();
    
    private static Log logger = LogFactory.getLog(PDFToolkitServiceOpenPdfImpl.class);
    
    // do we need to apply the encryption aspect when we encrypt?
    private boolean useEncryptionAspect = true;
    
    // do we need to apply the signature aspect when we sign?
    private boolean useSignatureAspect = true;
    
    // when we create a new document, do we actually create a new one, or copy the source?
    private boolean createNew = false;
    
    /**
     * The people make mistake so we force the creation of a new version before any modification to the
     * file for manage roolback
     * @return
     */
    private NodeRef forceCreateNewVersion(NodeRef targetNodeRef){
    	//SET NEW VERSION BEFORE MAKE ANYTHING
		serviceRegistry.getVersionService().ensureVersioningEnabled(targetNodeRef, null);
		if(!ns.hasAspect(targetNodeRef, ContentModel.ASPECT_VERSIONABLE)){
			ns.addAspect(targetNodeRef, ContentModel.ASPECT_VERSIONABLE, null);
		}
		serviceRegistry.getVersionService().createVersion(targetNodeRef, null);
		return targetNodeRef;
    }
    
    @Override
    public NodeRef appendPDF(NodeRef targetNodeRef, Map<String, Serializable> params)
    {
    	forceCreateNewVersion(targetNodeRef);
    	PDDocument pdf = null;
        PDDocument pdfTarget = null;
        InputStream is = null;
        InputStream tis = null;
        File tempDir = null;
        ContentWriter writer = null;
        NodeRef destinationNode = null;
        
        try
        {
        	NodeRef toAppend = (NodeRef)params.get(PARAM_TARGET_NODE);
        	Boolean inplace = Boolean.valueOf(String.valueOf(params.get(PARAM_INPLACE)));
        	ContentReader append = getReader(toAppend);
            is = append.getContentInputStream();
            
            ContentReader targetReader = getReader(targetNodeRef);
            tis = targetReader.getContentInputStream();
            
            String fileName = getFilename(params, targetNodeRef);
            
            // stream the document in
            pdf = PDDocument.load(is);
            pdfTarget = PDDocument.load(tis);
            
            // Append the PDFs
            PDFMergerUtility merger = new PDFMergerUtility();
            merger.appendDocument(pdfTarget, pdf);
            merger.setDestinationFileName(fileName);
            merger.mergeDocuments();

            // build a temp dir name based on the ID of the noderef we are
            // importing
            File alfTempDir = TempFileProvider.getTempDir();
            tempDir = new File(alfTempDir.getPath() + File.separatorChar + targetNodeRef.getId());
            tempDir.mkdir();
            
            pdfTarget.save(tempDir + "" + File.separatorChar + fileName);

            for (File file : tempDir.listFiles())
            {
                try
                {
                    if (file.isFile())
                    {
                        // Get a writer and prep it for putting it back into the repo
                        destinationNode = createDestinationNode(fileName, 
                        		params.get(PARAM_DESTINATION_FOLDER), targetNodeRef, inplace);
                        writer = cs.getWriter(destinationNode, ContentModel.PROP_CONTENT, true);
                        
                        writer.setEncoding(targetReader.getEncoding()); // original
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

            if (tempDir != null)
            {
                tempDir.delete();
            }
        }
        
        return destinationNode;
    }
    
	@Override
	public NodeRef encryptPDF(NodeRef targetNodeRef, Map<String, Serializable> params) 
	{
		forceCreateNewVersion(targetNodeRef);
		PdfStamper stamp = null;
        File tempDir = null;
        ContentWriter writer = null;
        ContentReader targetReader = null;
        NodeRef destinationNode = null;
        
        try
        {
            // get the parameters
            String userPassword = (String)params.get(PARAM_USER_PASSWORD);
            String ownerPassword = (String)params.get(PARAM_OWNER_PASSWORD);
            Boolean inplace = Boolean.valueOf(String.valueOf(params.get(PARAM_INPLACE)));
            int permissions = buildPermissionMask(params);
            int encryptionType = Integer.parseInt((String)params.get(PARAM_ENCRYPTION_LEVEL));

            // if metadata is excluded, alter encryption type
            if ((Boolean)params.get(PARAM_EXCLUDE_METADATA))
            {
                encryptionType = encryptionType | PdfWriter.DO_NOT_ENCRYPT_METADATA;
            }

            // get temp file
            File alfTempDir = TempFileProvider.getTempDir();
            tempDir = new File(alfTempDir.getPath() + File.separatorChar + targetNodeRef.getId());
            tempDir.mkdir();
            File file = new File(tempDir, ffs.getFileInfo(targetNodeRef).getName());

            // get the PDF input stream and create a reader for iText
            targetReader = getReader(targetNodeRef);
            PdfReader reader = new PdfReader(targetReader.getContentInputStream());
            stamp = new PdfStamper(reader, new FileOutputStream(file));

            // encrypt PDF
            stamp.setEncryption(userPassword.getBytes(Charset.forName("UTF-8")), ownerPassword.getBytes(Charset.forName("UTF-8")), permissions, encryptionType);
            stamp.close();

            String fileName = getFilename(params, targetNodeRef);
            
            // write out to destination
            destinationNode = createDestinationNode(fileName, 
            		params.get(PARAM_DESTINATION_FOLDER), targetNodeRef, inplace);
            writer = cs.getWriter(destinationNode, ContentModel.PROP_CONTENT, true);

            writer.setEncoding(targetReader.getEncoding());
            writer.setMimetype(FILE_MIMETYPE);
            writer.putContent(file);
            file.delete();
            
            //if useAspect is true, store some additional info about the signature in the props
            if(useEncryptionAspect)
            {
            	ns.addAspect(destinationNode, PDFToolkitModel.ASPECT_ENCRYPTED, new HashMap<QName, Serializable>());
            	ns.setProperty(destinationNode, PDFToolkitModel.PROP_ENCRYPTIONDATE, new java.util.Date());
            	ns.setProperty(destinationNode, PDFToolkitModel.PROP_ENCRYPTEDBY, AuthenticationUtil.getRunAsUser());
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
        
        return destinationNode;
	}

	@Override 
	public NodeRef decryptPDF(NodeRef targetNodeRef, Map<String, Serializable> params)
	{
		forceCreateNewVersion(targetNodeRef);
		PdfStamper stamp = null;
        File tempDir = null;
        ContentWriter writer = null;
        ContentReader targetReader = null;
        NodeRef destinationNode = null;
        
        try
        {
            // get the parameters
            String ownerPassword = (String)params.get(PARAM_OWNER_PASSWORD);
            Boolean inplace = Boolean.valueOf(String.valueOf(params.get(PARAM_INPLACE)));

            // get temp file
            File alfTempDir = TempFileProvider.getTempDir();
            tempDir = new File(alfTempDir.getPath() + File.separatorChar + targetNodeRef.getId());
            tempDir.mkdir();
            File file = new File(tempDir, ffs.getFileInfo(targetNodeRef).getName());

            // get the PDF input stream and create a reader for iText
            targetReader = getReader(targetNodeRef);
            PdfReader reader = new PdfReader(targetReader.getContentInputStream(), ownerPassword.getBytes());
            stamp = new PdfStamper(reader, new FileOutputStream(file));
            stamp.close();

            String fileName = getFilename(params, targetNodeRef);
            
            // write out to destination
            destinationNode = createDestinationNode(fileName, 
            		params.get(PARAM_DESTINATION_FOLDER), targetNodeRef, inplace);
            writer = cs.getWriter(destinationNode, ContentModel.PROP_CONTENT, true);

            writer.setEncoding(targetReader.getEncoding());
            writer.setMimetype(FILE_MIMETYPE);
            writer.putContent(file);
            file.delete();
            
            //if useAspect is true, store some additional info about the signature in the props
            if(useEncryptionAspect)
            {
            	ns.removeAspect(destinationNode, PDFToolkitModel.ASPECT_ENCRYPTED);
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
        
        return destinationNode;
	}
	
	@Override
	public NodeRef signPDF(NodeRef targetNodeRef, Map<String, Serializable> params) 
	{
		forceCreateNewVersion(targetNodeRef);
		NodeRef privateKey = (NodeRef)params.get(PARAM_PRIVATE_KEY);
		String location = (String)params.get(PARAM_LOCATION);
		String position = (String)params.get(PARAM_POSITION);
		String reason = (String)params.get(PARAM_REASON);
		String visibility = (String)params.get(PARAM_VISIBILITY);
		String keyPassword = (String)params.get(PARAM_KEY_PASSWORD);
		String keyType = (String)params.get(PARAM_KEY_TYPE);
		int height = getInteger(params.get(PARAM_HEIGHT));
		int width = getInteger(params.get(PARAM_WIDTH));
		int pageNumber = getInteger(params.get(PARAM_PAGE));
		
		// By default, append the signature as a new PDF revision to avoid
		// invalidating any signatures that might already exist on the doc
		boolean appendToExisting = true;
		if (params.get(PARAM_NEW_REVISION) != null) {
			appendToExisting = Boolean.valueOf(String.valueOf(params.get(PARAM_NEW_REVISION)));
		}
		
		// New keystore parameters
		String alias = (String)params.get(PARAM_ALIAS);
		String storePassword = (String)params.get(PARAM_STORE_PASSWORD);

		int locationX = getInteger(params.get(PARAM_LOCATION_X));
		int locationY = getInteger(params.get(PARAM_LOCATION_Y));

		Boolean inplace = Boolean.valueOf(String.valueOf(params.get(PARAM_INPLACE)));

		File tempDir = null;
		ContentWriter writer = null;
		KeyStore ks = null;

		NodeRef destinationNode = null;
		
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
			ContentReader pdfReader = getReader(targetNodeRef);
			PdfReader reader = new PdfReader(pdfReader.getContentInputStream());

			// If the page number is 0 because it couldn't be parsed or for
			// some other reason, set it to the first page, which is 1.
			// If the page number is negative, assume the intent is to "wrap".
			// For example, -1 would always be the last page.
			int numPages = reader.getNumberOfPages();
			if (pageNumber < 1 && pageNumber == 0) {
				pageNumber = 1; // use the first page
			} else {
				// page number is negative
				pageNumber = numPages + 1 + pageNumber;
				if (pageNumber <= 0) pageNumber = 1;
			}
			
			// if the page number specified is more than the num of pages,
			// use the last page
			if (pageNumber > numPages) {
				pageNumber = numPages;
			}
			
			// create temp dir to store file
			File alfTempDir = TempFileProvider.getTempDir();
			tempDir = new File(alfTempDir.getPath() + File.separatorChar + targetNodeRef.getId());
			tempDir.mkdir();
			File file = new File(tempDir, ffs.getFileInfo(targetNodeRef).getName());

			FileOutputStream fout = new FileOutputStream(file);
			
			// When adding a second signature, append must be called on PdfStamper.createSignature
			// to avoid invalidating previous signatures
			PdfStamper stamp = null;
			if (appendToExisting) {
				stamp = PdfStamper.createSignature(reader, fout, '\0', tempDir, true);
			} else {
				stamp = PdfStamper.createSignature(reader, fout, '\0');
			}
			PdfSignatureAppearance sap = stamp.getSignatureAppearance();
			
			//OPEN PDF
			sap.setCrypto(key, chain, null, PdfSignatureAppearance.WINCER_SIGNED);
			
			//ITEXT 5.5.11
			//final ExternalSignature es = new PrivateKeySignature(key, "SHA-256", "BC");
	        //final ExternalDigest digest = new BouncyCastleDigest();			
			//MakeSignature.signDetached(sap, digest, es, chain, null, null, null, 0, CryptoStandard.CMS);
			
			//ITEXT 5.1.3
			//sap.setCrypto(key, chain, null, PdfSignatureAppearance.WINCER_SIGNED);
			
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

			String fileName = getFilename(params, targetNodeRef);

			destinationNode = createDestinationNode(fileName, 
					params.get(PARAM_DESTINATION_FOLDER), targetNodeRef, inplace);
			writer = cs.getWriter(destinationNode, ContentModel.PROP_CONTENT, true);

			writer.setEncoding(pdfReader.getEncoding());
			writer.setMimetype(FILE_MIMETYPE);
			writer.putContent(file);

			file.delete();

			//if useAspect is true, store some additional info about the signature in the props
			if(useSignatureAspect)
			{
				ns.addAspect(destinationNode, PDFToolkitModel.ASPECT_SIGNED, new HashMap<QName, Serializable>());
				ns.setProperty(destinationNode, PDFToolkitModel.PROP_REASON, reason);
				ns.setProperty(destinationNode, PDFToolkitModel.PROP_LOCATION, location);
				ns.setProperty(destinationNode, PDFToolkitModel.PROP_SIGNATUREDATE, new java.util.Date());
				ns.setProperty(destinationNode, PDFToolkitModel.PROP_SIGNEDBY, AuthenticationUtil.getRunAsUser());
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
		} catch (GeneralSecurityException e) {
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
		
		return destinationNode;
	}

	@Override
	public NodeRef watermarkPDF(NodeRef targetNodeRef, Map<String, Serializable> params) 
	{
		forceCreateNewVersion(targetNodeRef);
		NodeRef destinationNode = null;
		
        try
        {
        	ContentReader targetReader = getReader(targetNodeRef);
        	
            if (params.get(PARAM_WATERMARK_TYPE) != null
                && params.get(PARAM_WATERMARK_TYPE).equals(TYPE_IMAGE))
            {

                NodeRef watermarkNodeRef = (NodeRef)params.get(PARAM_WATERMARK_IMAGE);
                ContentReader watermarkContentReader = getReader(watermarkNodeRef);
                destinationNode = this.imageAction(params, targetNodeRef, watermarkNodeRef, targetReader, watermarkContentReader);

            }
            else if (params.get(PARAM_WATERMARK_TYPE) != null
                     && params.get(PARAM_WATERMARK_TYPE).equals(TYPE_TEXT))
            {
                destinationNode = this.textAction(params, targetNodeRef, targetReader);
            }
        }
        catch (AlfrescoRuntimeException e)
        {
            throw new AlfrescoRuntimeException(e.getMessage(), e);
        }
        
        return destinationNode;
	}

	@Override
	public NodeRef splitPDF(NodeRef targetNodeRef, Map<String, Serializable> params) 
	{
		forceCreateNewVersion(targetNodeRef);
        PDDocument pdf = null;
        InputStream is = null;
        File tempDir = null;
        ContentWriter writer = null;
        NodeRef destinationFolder;
        
        try
        {
        	//destinationFolder = (NodeRef)params.get(PARAM_DESTINATION_FOLDER);
            if(params.get(PARAM_DESTINATION_FOLDER)==null){
            	destinationFolder = ns.getPrimaryParent(targetNodeRef).getParentRef();
            }else{
            	destinationFolder = (NodeRef)params.get(PARAM_DESTINATION_FOLDER);
            }
        	ContentReader targetReader = getReader(targetNodeRef);
        	
            // Get the split frequency
            int splitFrequency = 0;

            String splitFrequencyString = params.get(PARAM_SPLIT_FREQUENCY).toString();
            if (!splitFrequencyString.equals(""))
            {
                splitFrequency = Integer.valueOf(splitFrequencyString);
            }

            // Get contentReader inputStream
            is = targetReader.getContentInputStream();
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
            tempDir = new File(alfTempDir.getPath() + File.separatorChar + targetNodeRef.getId());
            tempDir.mkdir();

            while (it.hasNext())
            {
                // Get the split document and save it into the temp dir with new
                // name
                PDDocument splitpdf = (PDDocument)it.next();

                int pagesInPDF = splitpdf.getNumberOfPages();

                if (splitFrequency > 0)
                {
                    endPage = endPage + pagesInPDF;
                }

                // put together the name and save the PDF
                String fileNameSansExt = getFilenameSansExt(targetNodeRef, FILE_EXTENSION);
                splitpdf.save(tempDir + "" + File.separatorChar + fileNameSansExt + formatPageNumbering(page, endPage) + FILE_EXTENSION);

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
                        		destinationFolder, targetNodeRef, false);
                        writer = cs.getWriter(destinationNode, ContentModel.PROP_CONTENT, true);
                        
                        writer.setEncoding(targetReader.getEncoding()); // original
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
        
        return destinationFolder;
	}

	@Override
	public NodeRef splitPDFAtPage(NodeRef targetNodeRef, Map<String, Serializable> params) 
	{
		forceCreateNewVersion(targetNodeRef);
		PDDocument pdf = null;
		InputStream is = null;
		File tempDir = null;
		ContentWriter writer = null;
		NodeRef destinationFolder;

		try
		{
			//destinationFolder = (NodeRef)params.get(PARAM_DESTINATION_FOLDER);
            if(params.get(PARAM_DESTINATION_FOLDER)==null){
            	destinationFolder = ns.getPrimaryParent(targetNodeRef).getParentRef();
            }else{
            	destinationFolder = (NodeRef)params.get(PARAM_DESTINATION_FOLDER);
            }
			ContentReader targetReader = getReader(targetNodeRef);

			// Get the split frequency
			int splitPageNumber = 0;

			String splitPage = params.get(PARAM_PAGE).toString();
			if (!splitPage.equals(""))
			{
				try
				{
					splitPageNumber = Integer.valueOf(splitPage);
				}
				catch (NumberFormatException e)
				{
					throw new AlfrescoRuntimeException(e.getMessage(), e);
				}
			}

			// Get contentReader inputStream
			is = targetReader.getContentInputStream();
			// stream the document in
			pdf = PDDocument.load(is);
			// split the PDF and put the pages in a list
			Splitter splitter = new Splitter();
			// Need to adjust the input value to get the split at the right page
			splitter.setSplitAtPage(splitPageNumber - 1);

			// Split the pages
			List<PDDocument> pdfs = splitter.split(pdf);

			// Start page split numbering at
			int page = 1;

			// build a temp dir, name based on the ID of the noderef we are
			// importing
			File alfTempDir = TempFileProvider.getTempDir();
			tempDir = new File(alfTempDir.getPath() + File.separatorChar + targetNodeRef.getId());
			tempDir.mkdir();

			// FLAG: This is ugly.....get the first PDF.
			PDDocument firstPDF = (PDDocument)pdfs.remove(0);

			int pagesInFirstPDF = firstPDF.getNumberOfPages();

			int lastPage = 0;

			if (pagesInFirstPDF > 1)
			{
				lastPage = pagesInFirstPDF;
			}

			String fileNameSansExt = getFilenameSansExt(targetNodeRef, FILE_EXTENSION);
			firstPDF.save(tempDir + "" + File.separatorChar + fileNameSansExt + formatPageNumbering(page, lastPage) + FILE_EXTENSION);

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
				lastPage = pagesInSecondPDF + pagesInFirstPDF;
			}
			else
			{
				lastPage = 0;
			}

			// This is where we should save the appended PDF
			// put together the name and save the PDF
			secondPDF.save(tempDir + "" + File.separatorChar + fileNameSansExt + formatPageNumbering(splitPageNumber, lastPage) + FILE_EXTENSION);

			for (File file : tempDir.listFiles())
			{
				try
				{
					if (file.isFile())
					{
						// Get a writer and prep it for putting it back into the
						// repo
						NodeRef destinationNode = createDestinationNode(file.getName(), 
								destinationFolder, targetNodeRef, false);
						writer = cs.getWriter(destinationNode, ContentModel.PROP_CONTENT, true);

						writer.setEncoding(targetReader.getEncoding()); // original
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
		return destinationFolder;
	}

	@Override
	public NodeRef insertPDF(NodeRef targetNodeRef, Map<String, Serializable> params) 
	{
		forceCreateNewVersion(targetNodeRef);
		PDDocument pdf = null;
        PDDocument insertContentPDF = null;
        InputStream is = null;
        InputStream cis = null;
        File tempDir = null;
        ContentWriter writer = null;
        NodeRef destinationNode = null;
        
        try
        {

        	ContentReader targetReader = getReader(targetNodeRef);
        	ContentReader insertReader = getReader((NodeRef)params.get(PARAM_INSERT_CONTENT));
            int insertAt = Integer.valueOf((String)params.get(PARAM_PAGE)).intValue();
            Boolean inplace = Boolean.valueOf(String.valueOf(params.get(PARAM_INPLACE)));
            
            // Get contentReader inputStream
            is = targetReader.getContentInputStream();
            // Get insertContentReader inputStream
            cis = insertReader.getContentInputStream();
            // stream the target document in
            pdf = PDDocument.load(is);
            // stream the insert content document in
            insertContentPDF = PDDocument.load(cis);

            // split the PDF and put the pages in a list
            Splitter splitter = new Splitter();

            // Split the pages
            List<PDDocument> pdfs = splitter.split(pdf);

            // Build the output PDF
            PDFMergerUtility merger = new PDFMergerUtility();
            
            PDDocument newDocument = new PDDocument();
            
            for (int i = 0; i < pdfs.size(); i++) {
            	
            	if (i == insertAt -1) {
            		merger.appendDocument(newDocument, insertContentPDF);
            	}
            	
            	merger.appendDocument(newDocument, (PDDocument)pdfs.get(i));
            }
            
            merger.setDestinationFileName(params.get(PARAM_DESTINATION_NAME).toString());
            merger.mergeDocuments();

            // build a temp dir, name based on the ID of the noderef we are
            // importing
            File alfTempDir = TempFileProvider.getTempDir();
            tempDir = new File(alfTempDir.getPath() + File.separatorChar + targetNodeRef.getId());
            tempDir.mkdir();

            String fileName = params.get(PARAM_DESTINATION_NAME).toString();
            
            PDDocument completePDF = newDocument;

            completePDF.save(tempDir + "" + File.separatorChar + fileName + FILE_EXTENSION);

            try
            {
                completePDF.close();
                newDocument.close();
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
                        destinationNode = createDestinationNode(file.getName(), 
                        		params.get(PARAM_DESTINATION_FOLDER), targetNodeRef, inplace);
                        writer = cs.getWriter(destinationNode, ContentModel.PROP_CONTENT, true);
                        
                        writer.setEncoding(targetReader.getEncoding()); // original
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
        
        return destinationNode;
    }

	@Override
	public NodeRef deletePagesFromPDF(NodeRef targetNodeRef, Map<String, Serializable> params) 
	{
		forceCreateNewVersion(targetNodeRef);
		String pages = String.valueOf(params.get(PARAM_PAGE));
		return subsetPDFDocument(targetNodeRef, params, pages, true);
	}

	@Override
	public NodeRef extractPagesFromPDF(NodeRef targetNodeRef, Map<String, Serializable> params) 
	{
		forceCreateNewVersion(targetNodeRef);
		String pages = String.valueOf(params.get(PARAM_PAGE));
		return subsetPDFDocument(targetNodeRef, params, pages, false);
	}

	@Override
	public NodeRef rotatePDF(NodeRef targetNodeRef, Map<String, Serializable> params) 
	{
		forceCreateNewVersion(targetNodeRef);
		InputStream is = null;
        File tempDir = null;
        ContentWriter writer = null;
        PdfReader pdfReader = null;
        NodeRef destinationNode = null;
        
        try
        {
        	ContentReader targetReader = getReader(targetNodeRef);
            is = targetReader.getContentInputStream();

            File alfTempDir = TempFileProvider.getTempDir();
            tempDir = new File(alfTempDir.getPath() + File.separatorChar + targetNodeRef.getId());
            tempDir.mkdir();
            
            Boolean inplace = Boolean.valueOf(String.valueOf(params.get(PARAM_INPLACE)));
            Integer degrees = Integer.valueOf(String.valueOf(params.get(PARAM_DEGREES)));
            String pages = String.valueOf(params.get(PARAM_PAGE));
            
            if(degrees % 90 != 0)
            {
            	throw new AlfrescoRuntimeException("Rotation degres must be a multiple of 90 (90, 180, 270, etc)");
            }
            
            String fileName = getFilename(params, targetNodeRef);
            
            File file = new File(tempDir, ffs.getFileInfo(targetNodeRef).getName());

            pdfReader = new PdfReader(is);
            PdfStamper stamp = new PdfStamper(pdfReader, new FileOutputStream(file));
            
            int rotation = 0;
            PdfDictionary pageDictionary;
            int numPages = pdfReader.getNumberOfPages();
            for (int pageNum = 1; pageNum <= numPages; pageNum++) 
            {
                // only apply stamp to requested pages
                if (checkPage(pages, pageNum, numPages))
                {
                	rotation = pdfReader.getPageRotation(pageNum);
                	pageDictionary = pdfReader.getPageN(pageNum);
                    pageDictionary.put(PdfName.ROTATE, new PdfNumber(rotation + degrees));
                }
            }
            
            stamp.close();
            pdfReader.close();

            destinationNode = createDestinationNode(fileName, params.get(PARAM_DESTINATION_FOLDER), targetNodeRef, inplace);
            
            writer = cs.getWriter(destinationNode, ContentModel.PROP_CONTENT, true);

            writer.setEncoding(targetReader.getEncoding());
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
        return destinationNode;
	}
	
	@Override
	public NodeRef compressPDF(NodeRef targetNodeRef, Map<String, Serializable> params)
    {
	  forceCreateNewVersion(targetNodeRef);
	  if (serviceRegistry.getNodeService().exists(targetNodeRef) == false)
      {
          // node doesn't exist - can't do anything
          return null;
      }
	  
	  ContentReader actionedUponContentReader = getReader(targetNodeRef);
	  
      if (actionedUponContentReader != null)
      {
          // Compress the document with the requested options
    	  Map<String, Object> options = new HashMap<String, Object>();
          options.put(PARAM_DESTINATION_FOLDER, params.get(PARAM_DESTINATION_FOLDER));
          options.put(PARAM_COMPRESSION_LEVEL, params.get(PARAM_COMPRESSION_LEVEL));
          options.put(PARAM_IMAGE_COMPRESSION_LEVEL, params.get(PARAM_IMAGE_COMPRESSION_LEVEL));
          options.put(PARAM_INPLACE, params.get(PARAM_INPLACE));

          try
          {
              PdfStamper stamper = null;
              File tempDir = null;
              ContentWriter writer = null;

              float Factor = 0.5f;

              switch ((Integer)options.get(PARAM_IMAGE_COMPRESSION_LEVEL))
              {
                  case 9:
                      Factor = 0.1f;
                      break;
                  case 8:
                      Factor = 0.2f;
                      break;
                  case 7:
                      Factor = 0.3f;
                      break;
                  case 6:
                      Factor = 0.4f;
                      break;
                  case 5:
                      Factor = 0.5f;
                      break;
                  case 4:
                      Factor = 0.6f;
                      break;
                  case 3:
                      Factor = 0.7f;
                      break;
                  case 2:
                      Factor = 0.8f;
                      break;
                  case 1:
                      Factor = 0.9f;
                      break;
              }

              try
              {

                  // get temp file
                  File alfTempDir = TempFileProvider.getTempDir();
                  tempDir = new File(alfTempDir.getPath() + File.separatorChar + targetNodeRef.getId());
                  tempDir.mkdir();
                  File file = new File(tempDir, serviceRegistry.getFileFolderService().getFileInfo(targetNodeRef).getName());

                  int compression_level= (Integer)options.get(PARAM_COMPRESSION_LEVEL);

                  Boolean inplace = Boolean.valueOf(String.valueOf(options.get(PARAM_INPLACE)));

                  PdfReader reader = new PdfReader(actionedUponContentReader.getContentInputStream());
                  Document.compress = true;

                  int n = reader.getXrefSize();
                  PdfObject object;
                  PRStream stream;
                  // Look for image and manipulate image stream
                  for (int i = 0; i < n; i++) {
                      object = reader.getPdfObject(i);
                      if (object == null || !object.isStream())
                          continue;
                      stream = (PRStream)object;

                      PdfObject pdfsubtype = stream.get(PdfName.SUBTYPE);

                      if (pdfsubtype != null && pdfsubtype.toString().equals(PdfName.IMAGE.toString())) {
                          try
                          {
                        	  //ITEXT 5
                              //PdfImageObject image = new PdfImageObject(stream);
                        	  //BufferedImage bi = image.getBufferedImage();
                        	 
                        	  //OPENPDF
                        	  File fImage = copyToTemporaryFile(stream);                            	  
                              BufferedImage bi = ImageIO.read(fImage);
                              
                              if (bi == null) continue;
                              int width = (int)(bi.getWidth() * Factor);
                              int height = (int)(bi.getHeight() * Factor);

                              BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                              AffineTransform at = AffineTransform.getScaleInstance(Factor, Factor);
                              Graphics2D g = img.createGraphics();
                              g.drawRenderedImage(bi, at);
                              ByteArrayOutputStream imgBytes = new ByteArrayOutputStream();
                              ImageIO.write(img, "JPG", imgBytes);

                              //stream.clear();
                              stream.setData(imgBytes.toByteArray(), false, compression_level);
                              stream.put(PdfName.TYPE, PdfName.XOBJECT);
                              stream.put(PdfName.SUBTYPE, PdfName.IMAGE);
                              stream.put(PdfName.FILTER, PdfName.DCTDECODE);
                              stream.put(PdfName.WIDTH, new PdfNumber(width));
                              stream.put(PdfName.HEIGHT, new PdfNumber(height));
                              stream.put(PdfName.BITSPERCOMPONENT, new PdfNumber(8));
                              stream.put(PdfName.COLORSPACE, PdfName.DEVICERGB);
                          }
                          //catch(InvalidImageException e)
                          //{
                          //    continue;
                          //}
                          catch(UnsupportedPdfException e)
                          {
                              continue;
                          }
                          catch(IIOException e)
                          {
                              continue;
                          }

                      }
                  }



                  stamper = new PdfStamper(reader, new FileOutputStream(file), PdfWriter.VERSION_1_7);

                  if(compression_level < 9)
                  {
                      stamper.getWriter().setCompressionLevel(compression_level);
                  }
                  else
                  {
                      stamper.getWriter().setCompressionLevel(9);
                      stamper.setFullCompression();
                  }

                  if (logger.isDebugEnabled())
                  {
                      logger.debug("Executing: \n" + "   node: " + targetNodeRef + "\n" + "   reader: "
                              + actionedUponContentReader + "\n" + "   action: " + this + "\n" + "   compression: " + compression_level);
                  }

                  int total = reader.getNumberOfPages() +  1;
                  for (int i = 1; i < total; i++) {
                      reader.setPageContent(i, reader.getPageContent(i));
                  }

                  stamper.close();


                  // write out to destination
                  NodeRef destinationNode = createDestinationNode(file.getName(),
                          params.get(PARAM_DESTINATION_FOLDER), targetNodeRef, inplace);

                  writer = serviceRegistry.getContentService().getWriter(destinationNode, ContentModel.PROP_CONTENT, true);

                  writer.setEncoding(actionedUponContentReader.getEncoding());
                  writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
                  writer.putContent(file);
                  file.delete();
                  
                  return destinationNode;

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

                  if (stamper != null)
                  {
                      try
                      {
                          stamper.close();
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
      else
      {
          logger.error("Can't execute rule: \n" + "   node: " + targetNodeRef + "\n" + "   reader: "
                      + actionedUponContentReader + "\n" + "   action: " + this);             
          return null;
      }
      //set a noderef as the result
      //action.setParameterValue(PARAM_RESULT, actionedUponNodeRef);     
    }

	@Override
	public NodeRef collatePDF(NodeRef targetNodeRef, Map<String, Serializable> params)
    {		
    	if (!serviceRegistry.getNodeService().exists(targetNodeRef)) {
            // node doesn't exist - can't do anything
            return null;
        }
        targetNodeRef = getDestinationNodeRef(targetNodeRef,params);
        forceCreateNewVersion(targetNodeRef);
        if (!serviceRegistry.getNodeService().exists(targetNodeRef)) {
            // target node doesn't exist - can't do anything
            return null;
        }

        // Do the work....split the PDF
        Map<String, Object> options = new HashMap<>();
        options.put(PARAM_TARGET_NODE, targetNodeRef);
        options.put(PARAM_DESTINATION_NAME, params.get(PARAM_DESTINATION_NAME));
        //options.put(PARAM_INPLACE, ruleAction.getParameterValue(PARAM_INPLACE));

        try {
                   	
            String destinationFileName = options.get(PARAM_DESTINATION_NAME).toString();

            //actionedUponNodeRef is a file, create a list of all the PDF files contained in the Actioned Upon NodeRef
            List<NodeRef> pdfFilesToMerge = new ArrayList<NodeRef>();
            List<FileInfo> filesInFolder = serviceRegistry.getFileFolderService().listFiles(targetNodeRef);
            for (FileInfo fileInfo : filesInFolder) {
                NodeRef childFileNodeRef = fileInfo.getNodeRef();
                String contentType = ((ContentData) serviceRegistry.getNodeService().getProperty(childFileNodeRef, ContentModel.PROP_CONTENT)).getMimetype();
                if (MimetypeMap.MIMETYPE_PDF.equals(contentType)) {
                    pdfFilesToMerge.add(childFileNodeRef);
                }
            }

            //TODO: add configurable sort parameter or options to sort from the end user interface
            //sort the list
            Collections.sort(pdfFilesToMerge, new Comparator<NodeRef>() {
                @Override
                public int compare(NodeRef o1, NodeRef o2) {
                    return serviceRegistry.getFileFolderService().getFileInfo(o1).getName().compareTo(serviceRegistry.getFileFolderService().getFileInfo(o2).getName());
                }
            });

            String fileName;
            if (!StringUtils.isBlank(destinationFileName)) {
                fileName = String.valueOf(destinationFileName) + ".pdf";
            } else {
                fileName = String.valueOf(serviceRegistry.getNodeService().getProperty(targetNodeRef, ContentModel.PROP_NAME)) + ".pdf";
            }

            List<InputStream> streamsToMerge = new ArrayList<>();

            FileInfo fileInfo = serviceRegistry.getFileFolderService().create(targetNodeRef, fileNameProvider.getFileName(fileName, targetNodeRef), ContentModel.TYPE_CONTENT);
            NodeRef destinationNode = fileInfo.getNodeRef();

            for (NodeRef nodeRef : pdfFilesToMerge) {
                streamsToMerge.add(getReader(nodeRef).getContentInputStream());
            }

            PDFMergerUtility merger = new PDFMergerUtility();
            merger.addSources(streamsToMerge);
            ContentWriter writer = serviceRegistry.getContentService().getWriter(destinationNode, ContentModel.PROP_CONTENT, true);
            writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
            merger.setDestinationStream(writer.getContentOutputStream());
            merger.mergeDocuments();
            
            return destinationNode;
        
        } catch (AlfrescoRuntimeException e) {
            throw new AlfrescoRuntimeException(e.getMessage(), e);
        } catch (COSVisitorException | IOException e) {
            e.printStackTrace();
            throw new AlfrescoRuntimeException(e.getMessage(), e);
        }

    }
	
	@Override
	public NodeRef archivablePDF(NodeRef actionedUponNodeRef, Map<String, Serializable> params)
    {
    	if (!serviceRegistry.getNodeService().exists(actionedUponNodeRef)) {
            // node doesn't exist - can't do anything
            return null;
        }

        NodeRef targetNodeRef = getDestinationNodeRef(actionedUponNodeRef,params);
        
		//PDFX1A2001,PDFA1A,PDFA1B
         
        
        forceCreateNewVersion(targetNodeRef);
        if (!serviceRegistry.getNodeService().exists(targetNodeRef)) {
            // target node doesn't exist - can't do anything
            return null;
        }
        
        File file = null;
        File pdfa = null;
       
        try {
        	file =  nodeRefToTempFile(targetNodeRef);
        	ArchiveLevel archiveLevel = ArchiveLevel.fromValue((String) params.get(PARAM_ARCHIVE_LEVEL));
			pdfa = convertPdfToPdfA(FileUtils.readFileToByteArray(file),archiveLevel);
			
	        ContentWriter writer = cs.getWriter(targetNodeRef, ContentModel.PROP_CONTENT, true);	            
	        writer.setEncoding(writer.getEncoding());
	        writer.setMimetype(FILE_MIMETYPE);
	        // Put it in the repo
	        writer.putContent(file);
	        return targetNodeRef;
		} catch (Exception e) {
			throw new AlfrescoRuntimeException(e.getMessage(),e);
		}finally {
			FileUtils.deleteQuietly(file);
			FileUtils.deleteQuietly(pdfa);
		}
    }
	
	private NodeRef subsetPDFDocument(NodeRef targetNodeRef, Map<String, Serializable> params, String pages, boolean delete) 
	{		
		InputStream is = null;
	    File tempDir = null;
	    ContentWriter writer = null;
	    PdfReader pdfReader = null;
	    NodeRef destinationNode = null;
	    
	    try
	    {
	    	ContentReader targetReader = getReader(targetNodeRef);
	        is = targetReader.getContentInputStream();
	
	        File alfTempDir = TempFileProvider.getTempDir();
	        tempDir = new File(alfTempDir.getPath() + File.separatorChar + targetNodeRef.getId());
	        tempDir.mkdir();
	        
	        Boolean inplace = Boolean.valueOf(String.valueOf(params.get(PARAM_INPLACE)));
	        
	        String fileName = getFilename(params, targetNodeRef);
	        
	        File file = new File(tempDir, ffs.getFileInfo(targetNodeRef).getName());
	
	        pdfReader = new PdfReader(is);
	        Document doc = new Document(pdfReader.getPageSizeWithRotation(1));
	        PdfCopy copy = new PdfCopy(doc, new FileOutputStream(file));
	        doc.open();
	
	        List<Integer> pagelist = parsePageList(pages);
	        
	        for (int pageNum = 1; pageNum <= pdfReader.getNumberOfPages(); pageNum++) 
	        {
	        	if (pagelist.contains(pageNum) && !delete) 
	        	{
	        		copy.addPage(copy.getImportedPage(pdfReader, pageNum));
	        	}
	        	else if (!pagelist.contains(pageNum) && delete)
	        	{
	        		copy.addPage(copy.getImportedPage(pdfReader,  pageNum));
	        	}
	        }
	        doc.close();
	
	        destinationNode = createDestinationNode(fileName, 
	        		params.get(PARAM_DESTINATION_FOLDER), targetNodeRef, inplace);
	        writer = cs.getWriter(destinationNode, ContentModel.PROP_CONTENT, true);
	
	        writer.setEncoding(targetReader.getEncoding());
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
	    return destinationNode;
	}
	


	protected ContentReader getReader(NodeRef nodeRef)
    {
		// first, make sure the node exists
		if (ns.exists(nodeRef) == false)
        {
            // node doesn't exist - can't do anything
            throw new AlfrescoRuntimeException("NodeRef: " + nodeRef + " does not exist");
        }
		
        // Next check that the node is a sub-type of content
        QName typeQName = ns.getType(nodeRef);
        if (ds.isSubClass(typeQName, ContentModel.TYPE_CONTENT) == false)
        {
            // it is not content, so can't transform
            throw new AlfrescoRuntimeException("The selected node is not a content node");
        }

        // Get the content reader.  If it is null, can't do anything here
        ContentReader contentReader = cs.getReader(nodeRef, ContentModel.PROP_CONTENT);

        if(contentReader == null)
        {
        	throw new AlfrescoRuntimeException("The content reader for NodeRef: " + nodeRef + "is null");
        }
        
        return contentReader;
    }
	
    /**
     * @param ruleAction
     * @param filename
     * @return
     */
    protected NodeRef createDestinationNode(String filename, Serializable destinationParent, NodeRef target, boolean inplace)
    {

        NodeRef destinationFolder = null;
        if(destinationParent==null){
        	destinationFolder = ns.getPrimaryParent(target).getParentRef();
        }else{
        	destinationFolder = (NodeRef)destinationParent;
        }
    	NodeRef destinationNode;
    	
    	// if inplace mode is turned on, the destination for the modified content
    	// is the original node
    	if(inplace)
    	{
    		return target;
    	}
    	
    	if(createNew)
    	{
	    	//create a file in the right location
	        FileInfo fileInfo = ffs.create(destinationFolder, filename, ContentModel.TYPE_CONTENT);
	        destinationNode = fileInfo.getNodeRef();
    	}
    	else
    	{
    		try 
    		{
	    		FileInfo fileInfo = ffs.copy(target, destinationFolder, filename);
	    		destinationNode = fileInfo.getNodeRef();
    		}
    		catch(FileNotFoundException fnf)
    		{
    			throw new AlfrescoRuntimeException(fnf.getMessage(), fnf);
    		}
    	}

        return destinationNode;
    }
    
    protected int getInteger(Serializable val)
    {
    	if(val == null)
    	{ 
    		return 0;
    	}
    	try
    	{
    		return Integer.parseInt(val.toString());
    	}
    	catch(NumberFormatException nfe)
    	{
    		return 0;
    	}
    }
    
    protected File getTempFile(NodeRef nodeRef)
    {
    	File alfTempDir = TempFileProvider.getTempDir();
        File toolkitTempDir = new File(alfTempDir.getPath() + File.separatorChar + nodeRef.getId());
        toolkitTempDir.mkdir();
        File file = new File(toolkitTempDir, ffs.getFileInfo(nodeRef).getName());
        
        return file;
    }
    
    private String getFilename(Map<String, Serializable> params, NodeRef targetNodeRef)
    {
    	String providedName = (String)params.get(PARAM_DESTINATION_NAME);
        String fileName = null;
        if(StringUtils.isNotBlank(providedName))
        {
        	fileName = String.valueOf(providedName);
        	if(!fileName.endsWith(FILE_EXTENSION))
        	{
        		fileName = fileName + FILE_EXTENSION;
        	}
        }
        else
        {
        	fileName = String.valueOf(ns.getProperty(targetNodeRef, ContentModel.PROP_NAME));
        }
        return fileName;
    }
    
    /**
	 * Parses the list of pages or page ranges to delete and returns a list of page numbers 
	 * 
	 * @param list
	 * @return
	 */
	private List<Integer> parsePageList(String list)
	{
		List<Integer> pages = new ArrayList<Integer>();
		String[] tokens = list.split(",");
		for(String token : tokens)
		{
			//parse each, if one is not an int, log it but keep going
			try 
			{
				pages.add(Integer.parseInt(token));
			}
			catch(NumberFormatException nfe)
			{
				logger.warn("Page list contains non-numeric values");
			}
		}
		return pages;
	}
	
    /**
     * Build the permissions mask for iText
     * 
     * @param options
     * @return
     */
    private int buildPermissionMask(Map<String, Serializable> options)
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
    		r = new Rectangle(pageWidth - width, height, pageWidth, 0);
    	}
    	else if (position.equals(POSITION_TOPLEFT))
    	{
    		r = new Rectangle(0, pageHeight, width, pageHeight - height);
    	}
    	else if (position.equals(POSITION_TOPRIGHT))
    	{
    		r = new Rectangle(pageWidth - width, pageHeight, pageWidth, pageHeight - height);
    	}
    	else if (position.equals(POSITION_CENTER))
    	{
    		r = new Rectangle((pageWidth / 2) - (width / 2), (pageHeight / 2) - (height / 2),
    				(pageWidth / 2) + (width / 2), (pageHeight / 2) + (height / 2));
    	}

    	return r;
    }
    
    /**
     * @param fileName
     * @param extension
     * @return
     */
    private String removeExtension(String fileName, String extension)
    {
        // Does the file have the extension?
        if (fileName != null && fileName.contains(extension))
        {
            // Where does the extension start?
            int extensionStartsAt = fileName.indexOf(extension);
            // Get the Filename sans the extension
            return fileName.substring(0, extensionStartsAt);
        }

        return fileName;
    }

    private String getFilename(NodeRef targetNodeRef)
    {
        FileInfo fileInfo = ffs.getFileInfo(targetNodeRef);
        String filename = fileInfo.getName();

        return filename;
    }

    protected String getFilenameSansExt(NodeRef targetNodeRef, String extension)
    {
        String filenameSansExt;
        filenameSansExt = removeExtension(getFilename(targetNodeRef), extension);

        return filenameSansExt;
    }
    
    /**
     * Applies an image watermark
     * 
     * @param reader
     * @param writer
     * @param options
     * @throws Exception
     */
    private NodeRef imageAction(Map<String, Serializable> options, NodeRef targetNodeRef, NodeRef watermarkNodeRef,
            ContentReader actionedUponContentReader, ContentReader watermarkContentReader)
    {

        PdfStamper stamp = null;
        File tempDir = null;
        ContentWriter writer = null;
        NodeRef destinationNode = null;
        
        try
        {
            File file = getTempFile(targetNodeRef);

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
            int locationX = getInteger(options.get(PARAM_LOCATION_X));
            int locationY = getInteger(options.get(PARAM_LOCATION_Y));
            
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
            
            String fileName = getFilename(options, targetNodeRef);
            
            // Get a writer and prep it for putting it back into the repo
            //can't use BasePDFActionExecuter.getWriter here need the nodeRef of the destination
            destinationNode = createDestinationNode(fileName, 
            		options.get(PARAM_DESTINATION_FOLDER), targetNodeRef, inplace);
            writer = cs.getWriter(destinationNode, ContentModel.PROP_CONTENT, true);
            
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
        
        return destinationNode;
    }


    /**
     * Applies a text watermark (current date, user name, etc, depending on
     * options)
     * 
     * @param reader
     * @param writer
     * @param options
     */
    private NodeRef textAction(Map<String, Serializable> options, NodeRef targetNodeRef, ContentReader actionedUponContentReader)
    {

        PdfStamper stamp = null;
        File tempDir = null;
        ContentWriter writer = null;
        String watermarkText;
        StringTokenizer st;
        Vector<String> tokens = new Vector<String>();
        NodeRef destinationNode = null;
        
        try
        {
            File file = getTempFile(targetNodeRef);

            // get the PDF input stream and create a reader for iText
            PdfReader reader = new PdfReader(actionedUponContentReader.getContentInputStream());
            stamp = new PdfStamper(reader, new FileOutputStream(file));
            PdfContentByte pcb;

            // get the PDF pages and position
            String pages = (String)options.get(PARAM_PAGE);
            String position = (String)options.get(PARAM_POSITION);
            String depth = (String)options.get(PARAM_WATERMARK_DEPTH);
            int locationX = getInteger(options.get(PARAM_LOCATION_X));
            int locationY = getInteger(options.get(PARAM_LOCATION_Y));
            Boolean inplace = Boolean.valueOf(String.valueOf(options.get(PARAM_INPLACE)));

            // create the base font for the text stamp
            BaseFont bf = BaseFont.createFont((String)options.get(PARAM_WATERMARK_FONT), BaseFont.CP1250, BaseFont.EMBEDDED);

            // get watermark text and process template with model
            String templateText = (String)options.get(PARAM_WATERMARK_TEXT);
            Map<String, Object> model = buildWatermarkTemplateModel(targetNodeRef);
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

            String fileName = getFilename(options, targetNodeRef);
            
            // Get a writer and prep it for putting it back into the repo
            //can't use BasePDFActionExecuter.getWriter here need the nodeRef of the destination
            destinationNode = createDestinationNode(fileName, 
            		options.get(PARAM_DESTINATION_FOLDER), targetNodeRef, inplace);
            writer = cs.getWriter(destinationNode, ContentModel.PROP_CONTENT, true);
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
        
        return destinationNode;
    }


    /**
     * Writes text watermark to one of the 5 preconfigured locations
     * 
     * @param pcb
     * @param r
     * @param tokens
     * @param size
     * @param position
     */
    protected void writeAlignedText(PdfContentByte pcb, Rectangle r, Vector<String> tokens, float size, 
    		String position, int locationX, int locationY)
    {
        // get the dimensions of our 'rectangle' for text
        float height = size * tokens.size();
        float width = 0;
        float centerX = 0, startY = 0;
        for (int i = 0; i < tokens.size(); i++)
        {
            if (pcb.getEffectiveStringWidth(tokens.get(i), false) > width)
            {
                width = pcb.getEffectiveStringWidth(tokens.get(i), false);
            }
        }

        // now that we have the width and height, we can calculate the center
        // position for
        // the rectangle that will contain our text.
        if (position.equals(POSITION_BOTTOMLEFT))
        {
        	centerX = width / 2 + PAD;
            startY = 0 + PAD + height;
        }
        else if (position.equals(POSITION_BOTTOMRIGHT))
        {
            centerX = r.getWidth() - (width / 2) - PAD;
            startY = 0 + PAD + height;
        }
        else if (position.equals(POSITION_TOPLEFT))
        {
            centerX = width / 2 + PAD;
            startY = r.getHeight() - (PAD * 2);
        }
        else if (position.equals(POSITION_TOPRIGHT))
        {
            centerX = r.getWidth() - (width / 2) - PAD;
            startY = r.getHeight() - (PAD * 2);
        }
        else if (position.equals(POSITION_CENTER))
        {
            centerX = r.getWidth() / 2;
            startY = (r.getHeight() / 2) + (height / 2);
        }
        else if (position.equals(POSITION_MANUAL))
        {
        	centerX = r.getWidth() / 2 - locationX;
        	startY = locationY;
        }

        // apply text to PDF
        pcb.beginText();

        for (int t = 0; t < tokens.size(); t++)
        {
            pcb.showTextAligned(PdfContentByte.ALIGN_CENTER, tokens.get(t), centerX, startY - (size * t), 0);
        }

        pcb.endText();

    }

    /**
     * Builds a freemarker model which supports a subset of the default model.
     * 
     * @param ref
     * @return
     */
    protected Map<String, Object> buildWatermarkTemplateModel(NodeRef ref)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        NodeRef person = ps.getPerson(as.getCurrentUserName());
        model.put("person", new TemplateNode(person, serviceRegistry, null));
        NodeRef homespace = (NodeRef)ns.getProperty(person, ContentModel.PROP_HOMEFOLDER);
        model.put("userhome", new TemplateNode(homespace, serviceRegistry, null));
        model.put("document", new TemplateNode(ref, serviceRegistry, null));
        NodeRef parent = ns.getPrimaryParent(ref).getParentRef();
        model.put("space", new TemplateNode(parent, serviceRegistry, null));
        model.put("date", new Date());

        //also add all of the node properties to the model
        model.put("properties", ns.getProperties(ref));
        
        return model;
    }
    
    /**
     * Determines whether or not a watermark should be applied to a given page
     * 
     * @param pages
     * @param current
     * @param numpages
     * @return
     */
    protected boolean checkPage(String pages, int current, int numpages)
    {

    	
        boolean markPage = false;

        if (pages.equals(PAGE_EVEN))
        {
            if (current % 2 == 0)
            {
                markPage = true;
            }
        }
        else if (pages.equals(PAGE_ODD))
        {
            if (current % 2 != 0)
            {
                markPage = true;
            }
        }
        else if (pages.equals(PAGE_FIRST))
        {
            if (current == 1)
            {
                markPage = true;
            }
        }
        else if (pages.equals(PAGE_LAST))
        {
            if (current == numpages)
            {
                markPage = true;
            }
        }
        else if (pages.equals(PAGE_ALL))
        {
            markPage = true;
        }
        else
        {
        	// if we get here, a scheme wasn't selected, so we can treat this like a page list
        	List<Integer> pageList = parsePageList(pages);
        	if(pageList.contains(current))
        	{
        		markPage = true;
        	}
        }

        return markPage;
    }

    /**
     * Gets the X value for centering the watermark image
     * 
     * @param r
     * @param img
     * @return
     */
    protected float getCenterX(Rectangle r, Image img)
    {
        float x = 0;
        float pdfwidth = r.getWidth();
        float imgwidth = img.getWidth();

        x = (pdfwidth - imgwidth) / 2;

        return x;
    }

    /**
     * Gets the Y value for centering the watermark image
     * 
     * @param r
     * @param img
     * @return
     */
    protected float getCenterY(Rectangle r, Image img)
    {
        float y = 0;
        float pdfheight = r.getHeight();
        float imgheight = img.getHeight();

        y = (pdfheight - imgheight) / 2;

        return y;
    }

    /**
     * Format the page numbers according to the localized string in messages
     * 
     * @param currentPage
     * @param lastPage
     * @return
     */
    private String formatPageNumbering(int currentPage, int lastPage)
    {
    	String text = "";
    	if (lastPage==0) 
    	{
    		text = I18NUtil.getMessage(MSGID_PAGE_NUMBERING_PATTERN_SINGLE, new Object[]{currentPage});
    	}
    	else
    	{
    		text = I18NUtil.getMessage(MSGID_PAGE_NUMBERING_PATTERN_MULTIPLE, new Object[]{currentPage, lastPage});
    	}
    	return text;
    }
    
    /**
     * @param serviceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
        ns = serviceRegistry.getNodeService();
        cs = serviceRegistry.getContentService();
        ffs = serviceRegistry.getFileFolderService();
        ds = serviceRegistry.getDictionaryService();
        ps = serviceRegistry.getPersonService();
        as = serviceRegistry.getAuthenticationService();
    }
    
    /**
     * Sets whether a PDF action creates a new empty node or copies the source node, preserving
     * the content type, applied aspects and properties
     * 
     * @param createNew
     */
    public void setCreateNew(boolean createNew)
    {
    	this.createNew = createNew;
    }
    
    public void setUseSignatureAspect(boolean useSignatureAspect)
    {
    	this.useSignatureAspect = useSignatureAspect;
    }
    
    public void setUseEncryptionAspect(boolean useEncryptionAspect)
    {
    	this.useEncryptionAspect = useEncryptionAspect;
    }

	public void setFileNameProvider(FileNameProvider fileNameProvider) {
		this.fileNameProvider = fileNameProvider;
	}

	// =======================
	// LOFTUX LAB METHOD ADDED
	// =======================

	private String PARAM_TARGET = "target";
	

    public void jsConstructor()
    {
    }


    public String getClassName()
    {
        return "PDFToolkitService";
    }


    /**
     * Wrapper for the encrypt PDF action. This calls the PDFEncryptionActionExecuter
     * 
     * When used in a JS context, this code expects a JSON object to with the following structure:
     * 
     * 	{
     * 		target : "workspace:SpacesStore://node-uuid",
     * 		destination-folder : "workspace:SpacesStore://node-uuid",
     * 		user-password : "password",
     *  	owner-password : "password",
     *  	allow-print : true,
     *  	allow-copy : true,
     *  	allow-content-modification : true,
     *  	allow-annotation-modification : true,
     *  	allow-form-fill : true,
     *  	allow-screen-reader : true,
     *  	allow-degraded-print : true,
     *  	allow-assembly : true,
     *  	encryption-level : "0",
     *  	exclude-metadata : true
     * 	}
     * 
     * For the available options for encryption-level, look at the constraint pdfc-encryptionlevel 
     * in module-context.xml
     */
    public void encryptPDF(NativeObject obj)
    {

    	Map<String, Serializable> params = buildParamMap(obj);
    	NodeRef toEncrypt = getActionTargetNode(params);
    	this.executePDFAction(PDFEncryptionActionExecuter.NAME, params, toEncrypt);
    }

    /**
     * Wrapper for the sign PDF action. This calls the PDFSignatureActionExecuter
     * 
     * When used in a JS context, this code expects a JSON object to with the following structure:
     * 
     * 	{
     * 		target : "workspace:SpacesStore://node-uuid",
     * 		destination-folder : "workspace:SpacesStore://node-uuid",
     * 		private-key : "workspace:SpacesStore://node-uuid",
     * 		location : "location",
     *  	reason : "reason",
     *  	key-password : "keypassword",
     *  	width : "200",
     *  	height : "50",
     *  	key-type : "default",
     *  	alias : "alias",
     *  	store-password : "storepassword",
     *  	visibility : "visible",
     *  	position : "center",
     *  	location-x : "50",
     *  	location-y : "50"
     * 	}
     * 
     * For the available options for visibility, look at the constraint pdfc-visibility
     * in module-context.xml
     * 
     * For the available options for key-type, look at the constraint pdfc-keytype
     * in module-context.xml
     * 
     * For the available options for position, look at the constraint pdfc-position
     * in module-context.xml
     */
    public void signPDF(NativeObject obj)
    {
    	Map<String, Serializable> params = buildParamMap(obj);
    	
    	//check and make sure we have a valid ref for the private key
    	NodeRef key = getDependentNode(params, PDFToolkitConstants.PARAM_PRIVATE_KEY);
    	params.put(PDFToolkitConstants.PARAM_PRIVATE_KEY, key);

    	NodeRef toSign = getActionTargetNode(params);
    	this.executePDFAction(PDFSignatureActionExecuter.NAME, params, toSign);
    }

    /**
     * Wrapper for the watermark PDF action. This calls the PDFWatermarkActionExecuter
     * 
     * When used in a JS context, this code expects a JSON object to with the following structure:
     * 
     * 	{
     * 		target : "workspace:SpacesStore://node-uuid",
     * 		destination-folder : "workspace:SpacesStore://node-uuid",
     * 		watermark-image : "workspace:SpacesStore://node-uuid",
     * 		position : "center",
     *  	location-x : "50",
     *  	location-y : "50",
     *  	watermark-type : "image",
     *  	watermark-pages : "all",
     *  	watermark-depth : "under",
     *  	watermark-text : "Text to use as watermark",
     *  	watermark-font : "Courier",
     *  	watermark-size : "18"
     * 	}
     * 
     * For the available options for position, look at the constraint pdfc-position
     * in module-context.xml
     * 
     * For the available options for watermark-type, look at the constraint pdfc-watermarktype
     * in module-context.xml
     * 
     * For the available options for watermark-pages, look at the constraint pdfc-page
     * in module-context.xml
     * 
     * For the available options for watermark-depth, look at the constraint pdfc-depth
     * in module-context.xml
     * 
     * For the available options for watermark-font, look at the constraint pdfc-font
     * in module-context.xml
     */
    public void watermarkPDF(NativeObject obj)
    {
    	Map<String, Serializable> params = buildParamMap(obj);
    	NodeRef toWatermark = getActionTargetNode(params);
    	
    	//if this is an image watermark, verify that the node exists and add it to the
    	//params as a noderef instead of a string
    	if(params.get(PDFToolkitConstants.PARAM_WATERMARK_TYPE)
    			.toString().equalsIgnoreCase(PDFToolkitConstants.TYPE_IMAGE))
    	{
    		NodeRef image = getDependentNode(params, PDFToolkitConstants.PARAM_WATERMARK_IMAGE);
    		params.put(PDFToolkitConstants.PARAM_WATERMARK_IMAGE, image);
    	}
    	
    	this.executePDFAction(PDFWatermarkActionExecuter.NAME, params, toWatermark);
    }

    /**
     * Wrapper for the compress PDF action. This calls the PDFCompressActionExecuter
     *
     * When used in a JS context, this code expects a JSON object to with the following structure:
     *
     * 	{
     * 		target : "workspace:SpacesStore://node-uuid",
     * 		destination-folder : "workspace:SpacesStore://node-uuid",
     * 		compression-level : 9 ,
     * 	    image-compression-level : 6
     * 	}
     *
     * Compression levels are 1-9 (low compression - high compression).
     * Image compression level has biggest impact on Scanned pdf:s, general compression for the
     * pdf impacts stored fonts etc that are removed.
     *
     */
    public void compressPDF(NativeObject obj)
    {
        Map<String, Serializable> params = buildParamMap(obj);
        NodeRef toCompress = getActionTargetNode(params);
        this.executePDFAction(PDFCompressActionExecuter.NAME, params, toCompress);
    }

    /**
     * Wrapper for the split PDF action. This calls the PDFSplitActionExecuter
     * 
     * When used in a JS context, this code expects a JSON object to with the following structure:
     * 
     * 	{
     * 		target : "workspace:SpacesStore://node-uuid",
     * 		destination-folder : "workspace:SpacesStore://node-uuid",
     * 		split-frequency : "1"
     * 	}
     * 
     */
    public void splitPDF(NativeObject obj)
    {
    	Map<String, Serializable> params = buildParamMap(obj);
    	NodeRef toSplit = getActionTargetNode(params);
    	this.executePDFAction(PDFSplitActionExecuter.NAME, params, toSplit);
    }

    /**
     * Wrapper for the split at page PDF action. This calls the PDFSplitAtPageActionExecuter
     * 
     * When used in a JS context, this code expects a JSON object to with the following structure:
     * 
     * 	{
     * 		target : "workspace:SpacesStore://node-uuid",
     * 		destination-folder : "workspace:SpacesStore://node-uuid",
     * 		split-at-page : "1"
     * 	}
     * 
     */
    public void splitPDFAtPage(NativeObject obj)
    {
    	Map<String, Serializable> params = buildParamMap(obj);
    	NodeRef toSplit = getActionTargetNode(params);
    	this.executePDFAction(PDFSplitAtPageActionExecuter.NAME, params, toSplit);
    }
    
    /**
     * Wrapper for the append PDF action. This calls the PDFAppendActionExecuter
     * 
     * When used in a JS context, this code expects a JSON object to with the following structure:
     * 
     * 	{
     * 		target : "workspace:SpacesStore://node-uuid",
     * 		destination-folder : "workspace:SpacesStore://node-uuid",
     * 		append-content : "workspace:SpacesStore://node-uuid",
     * 		destination-name : "new_file_name.pdf"
     * 	}
     * 
     */
    public void appendPDF(NativeObject obj)
    {
    	Map<String, Serializable> params = buildParamMap(obj);
    	NodeRef appendTo = getActionTargetNode(params);
    	
    	//check and make sure we have a valid ref for the pdf to append
    	NodeRef toAppend = getDependentNode(params, "append-content");
    	params.put("append-content", toAppend);
    	
    	this.executePDFAction(PDFAppendActionExecuter.NAME, params, appendTo);
    }

    /**
     * Wrapper for the insert PDF action. This calls the PDFInsertAtPageActionExecuter
     * 
     * When used in a JS context, this code expects a JSON object to with the following structure:
     * 
     * 	{
     * 		target : "workspace:SpacesStore://node-uuid",
     * 		destination-folder : "workspace:SpacesStore://node-uuid",
     * 		insert-content : "workspace:SpacesStore://node-uuid",
     * 		destination-name : "new_file_name.pdf",
     * 		insert-at-page : "1"
     * 	}
     * 
     */
    public void insertPDF(NativeObject obj)
    {
    	Map<String, Serializable> params = buildParamMap(obj);
    	NodeRef insertInto = getActionTargetNode(params);
    	
    	//check and make sure we have a valid ref for the pdf to insert
    	NodeRef toInsert= getDependentNode(params, PDFToolkitConstants.PARAM_INSERT_CONTENT);
    	params.put(PDFToolkitConstants.PARAM_INSERT_CONTENT, toInsert);
    	
    	this.executePDFAction(PDFInsertAtPageActionExecuter.NAME, params, insertInto);
    }
    
    /**
     * Executes a specific PDF action called by the service
     * 
     * @param name
     * @param params
     * @param actioned
     */
    private void executePDFAction(String name, Map<String, Serializable> params, NodeRef actioned)
    {
    	ActionService actionService = serviceRegistry.getActionService();
    	Action toExecute = actionService.createAction(name, params);
    	actionService.executeAction(toExecute, actioned);
    }
    
    
    public void archivablePDF(NativeObject obj)
    {
    	Map<String, Serializable> params = buildParamMap(obj);
    	NodeRef insertInto = getActionTargetNode(params);
    	
    	//check and make sure we have a valid ref for the pdf to insert
    	NodeRef toInsert= getDependentNode(params, PDFToolkitConstants.PARAM_INSERT_CONTENT);
    	params.put(PDFToolkitConstants.PARAM_INSERT_CONTENT, toInsert);
    	
    	this.executePDFAction(PDFConvertToArchivableActionExecuter.NAME, params, insertInto);
    }
    
    /**
     * Finds a named String parameter and converts it to a NodeRef
     * 
     * @param params
     * @param name
     * @return
     */
    private NodeRef getDependentNode(Map<String, Serializable> params, String name)
    {
    	NodeService nodeService = serviceRegistry.getNodeService();
    	
    	//grab the target node and make sure it exists
    	if(params.get(name) == null)
    	{
    		throw new ServiceException("Object property " + name + " must be provided");
    	}
    	String nodeString = params.get(name).toString();

    	
    	NodeRef dep = new NodeRef(nodeString);
    	if(!nodeService.exists(dep))
    	{
    		throw new ServiceException("Object property " + name + " must be a valid node reference");
    	}
    	
    	return dep;
    }
    
    /**
     * Get a NodeRef to the target node, defined by the "node" property of the Javascript object
     * passed to the service
     * 
     * @param params
     * @return
     */
    private NodeRef getActionTargetNode(Map<String, Serializable> params)
    {
    	
    	return getDependentNode(params, PARAM_TARGET);
    }
    
    /**
     * Build a proper parameters map suitable for passing to the ActionService
     * 
     * @param obj
     * @return
     */
    private Map<String, Serializable> buildParamMap(NativeObject obj)
    {
    	Map<String, Serializable> params = nativeObjectToMap(obj);
    	
    	NodeRef destination = getDependentNode(params, PDFToolkitConstants.PARAM_DESTINATION_FOLDER);
    	
    	//add the noderef back to the param map
    	params.put(PDFToolkitConstants.PARAM_DESTINATION_FOLDER, destination);
    	
    	return params;
    }
    
    /**
     * Can't cast to Map, as Alfresco's Rhino version is WAY out of date and 
     * NativeObject doesn't implement Map.  So, we do this instead.
     * 
     * @param obj
     */
    private Map<String, Serializable> nativeObjectToMap(NativeObject obj)
    {
    	Map<String, Serializable> map = new HashMap<String, Serializable>();
    	Object[] keys = obj.getAllIds();
    	for(Object key : keys)
    	{
    		Object value = NativeObject.getProperty(obj, key.toString());
    		map.put(key.toString(), (Serializable)value);
    	}
    	return map;
    }
    
    protected NodeRef getDestinationNodeRef(NodeRef actionedUponNodeRef,Map<String,Serializable> params) {
        NodeRef targetNodeRef;Serializable targetNodeRefStr = params.get(PARAM_TARGET_NODE);
        if (targetNodeRefStr != null) {
            targetNodeRef = (NodeRef) targetNodeRefStr;
        }else{
            targetNodeRef = serviceRegistry.getNodeService().getPrimaryParent(actionedUponNodeRef).getParentRef();
        }
        return targetNodeRef;
    }
    
    protected File nodeRefToTempFile(NodeRef nodeRef)
    {
    	ContentService cs = serviceRegistry.getContentService();
        File tempFromFile = TempFileProvider.createTempFile("PDFAConverter-", nodeRef.getId()+ ".tmp");
        ContentReader reader = cs.getReader(nodeRef, ContentModel.PROP_CONTENT);
        reader.getContent(tempFromFile);
        return tempFromFile;
    }
    
    /**
     * Applies a text watermark (current date, user name, etc, depending on
     * options)
     * 
     * @param reader
     * @param writer
     * @param options
     */
    private void textAction(Action ruleAction, NodeRef actionedUponNodeRef, ContentReader actionedUponContentReader,
            Map<String, Object> options)
    {

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
            		ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER), actionedUponNodeRef, inplace);
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
    
    public static File copyToTemporaryFile(PRStream prs) throws IOException {
        File tmpFile = TempFile.createTempFile(PDFToolkitServiceOpenPdfImpl.class.getSimpleName()+System.currentTimeMillis(), ".tmp");
        logger.debug("Created output temporary buffer " + tmpFile);

        ByteArrayInputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(PdfReader.getStreamBytes(prs));
            FileUtils.copyInputStreamToFile(inputStream, tmpFile);
            logger.debug("Attachment unpacked to temporary buffer");
        } catch (IOException e) {
            throw new IOException("Unable to copy attachment to temporary file.", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return tmpFile;
    }
    
	protected File convertPdfToPdfA(final byte[] source,ArchiveLevel archiveLevel) throws Exception {
		File tempFile = null;       
		File pdfAFile = null;
		OutputStream output = null;
		try {
			tempFile = TempFileProvider.createTempFile("pre_pdfa", ".pdf");           
			FileUtils.writeByteArrayToFile(tempFile, source);          
			pdfAFile = TempFileProvider.createTempFile("digitalSigning-" + System.currentTimeMillis(), ".pdf");
			//METHOD WITH ITEXT 5
			/*
            java.net.URL url = getClass().getResource("/org/alfresco/plugin/digitalSigning/service/sRGB_CS_profile.icm");
        	byte[] bytes = IOUtils.toByteArray(url.openStream());
        	final ICC_Profile icc = ICC_Profile.getInstance(bytes);

            //Reads a PDF document. 
            PdfReader reader = new PdfReader(source); 
            //PdfStamper: Applies extra content to the pages of a PDF document. This extra content can be all the objects allowed in 
            //PdfContentByte including pages from other Pdfs. 
            //A generic Document class. 
            Document document = new Document(); 
            // we create a writer that listens to the document
            PdfAWriter writer = PdfAWriter.getInstance(document, new FileOutputStream(pdfAFile), PdfAConformanceLevel.PDF_A_1A);       
            int numberPages = reader.getNumberOfPages(); 

            //PdfDictionary:A dictionary is an associative table containing pairs of objects. 
            //The first element of each pair is called the key and the second  element is called the value 
            //<CODE>PdfName</CODE> is an object that can be used as a name in a PDF-file 
            //PdfDictionary outi = new PdfDictionary(PdfName.OUTPUTINTENT); 
            //outi.put(PdfName.OUTPUTCONDITIONIDENTIFIER, new PdfString("sRGB IEC61966-2.1")); 
            //outi.put(PdfName.INFO, new PdfString("sRGB IEC61966-2.1")); 
            //outi.put(PdfName.S, PdfName.GTS_PDFA1); 
            //writer.getExtraCatalog().put(PdfName.OUTPUTINTENTS, new PdfArray(outi)); 

            writer.setTagged();
            writer.createXmpMetadata();
            document.open(); 

            writer.setOutputIntents("Custom", "", "http://www.color.org", "sRGB IEC61966-2.1", icc);

            //Add pages 
            PdfImportedPage p = null; 
            Image image; 
            for(int i=0; i< numberPages; i++){ 
            	document.newPage(); 
                p = writer.getImportedPage(reader, i+1); 
                image = Image.getInstance(p);
                // Scale PDF page to fit with PDF/A format
                image.scaleAbsolute(writer.getPageSize());
                // Center the image into the PDF/A page
                image.setAlignment(Element.ALIGN_CENTER);

                // Set the position of the image into the PDF/A page
                image.setAbsolutePosition(0, 0);
                document.setMargins(0, 0, 0, 0);
                document.add(image); 
            }

            document.close(); 
            writer.flush();

            return pdfAFile;
			*/
			//METHOD WITH PDFBOX 1.8.10
			//https://apache.googlesource.com/pdfbox/+/4df9353eaac3c4ee2124b09da05312376f021b2c/examples/src/main/java/org/apache/pdfbox/examples/pdfa/CreatePDFA.java
			PDDocument doc = null;
			try{
				doc = PDDocument.load(tempFile);
			}catch(IOException ex){
				if(ex.getMessage().contains("expected='endstream'")){
					//https://issues.apache.org/jira/browse/PDFBOX-1541
					//https://www.programcreek.com/java-api-examples/?code=jmrozanec/pdf-converter/pdf-converter-master/src/main/java/pdf/converter/txt/TxtCreator.java				
					File tmpfile = File.createTempFile(String.format("txttmp-%s", UUID.randomUUID().toString()), null);
					try{
						org.apache.pdfbox.io.RandomAccessFile raf = new org.apache.pdfbox.io.RandomAccessFile(tmpfile, "rw");
						doc = PDDocument.loadNonSeq(tmpfile,raf);	   	
					}finally{
						FileUtils.deleteQuietly(tmpfile);
					}
				}else{
					throw ex;
				}
			}   
			try
			{           
				//	                // load the font from pdfbox.jar
				//	                InputStream fontStream = CreatePDFA.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/ArialMT.ttf");
				//	                PDFont font = PDTrueTypeFont.loadTTF(doc, fontStream);
				//	                
				//	                
				//	                // create a page with the message where needed
				//	                PDPageContentStream contentStream = new PDPageContentStream(doc, page);
				//	                contentStream.beginText();
				//	                contentStream.setFont( font, 12 );
				//	                contentStream.moveTextPositionByAmount( 100, 700 );
				//	                contentStream.drawString( message );
				//	                contentStream.endText();
				//	                contentStream.saveGraphicsState();
				//	                contentStream.close();

				PDDocumentCatalog cat = doc.getDocumentCatalog();
				PDMetadata metadata = new PDMetadata(doc);
				cat.setMetadata(metadata);

				XMPMetadata xmp = XMPMetadata.createXMPMetadata();
				try
				{
					PDFAIdentificationSchema pdfaid = xmp.createAndAddPFAIdentificationSchema();
					pdfaid.setConformance(archiveLevel.getConformance());
					pdfaid.setPart(archiveLevel.getPart());
					if(archiveLevel.getAmdId()!=null){
						pdfaid.setAmd(archiveLevel.getAmdId());
					}
					pdfaid.setAboutAsSimple("PDFBox PDFA sample");
					XmpSerializer serializer = new XmpSerializer();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					serializer.serialize(xmp, baos, true);
					metadata.importXMPMetadata( baos.toByteArray() );
				}
				catch(BadFieldValueException badFieldexception)
				{
					// can't happen here, as the provided value is valid
				}
				catch(XmpSerializationException xmpException)
				{
					System.err.println(xmpException.getMessage());
				}
				//COLOR SPACE BUG
				InputStream colorProfile = CreatePDFA.class.getResourceAsStream("/org/apache/pdfbox/resources/pdfa/sRGB Color Space Profile.icm");
				//InputStream colorProfile = getClass().getResourceAsStream("/org/alfresco/plugin/digitalSigning/service/sRGB_CS_profile.icm");
				// create output intent
				PDOutputIntent oi = new PDOutputIntent(doc, colorProfile); 
				oi.setInfo("sRGB IEC61966-2.1"); 
				oi.setOutputCondition("sRGB IEC61966-2.1");
				oi.setOutputConditionIdentifier("sRGB IEC61966-2.1"); 
				oi.setRegistryName("http://www.color.org"); 
				cat.addOutputIntent(oi);

				doc.save(pdfAFile);				
			}
			finally
			{
				if( doc != null )
				{
					doc.close();
				}
			}
			
			return pdfAFile;
		}
		catch (Exception ex) {
			logger.error("Can't convert PDF to PDF/A.  Error during conversion to PDF/A.", ex);
			throw new Exception("Can't convert PDF to PDF/A.  Error during conversion to PDF/A.", ex);
		}   
		finally {
			if (tempFile != null) {
				tempFile.delete();
			}
			if(output!=null){
				try {
					output.close();
				} catch (IOException e) {					
				}
			}
		}
	}

	


}
