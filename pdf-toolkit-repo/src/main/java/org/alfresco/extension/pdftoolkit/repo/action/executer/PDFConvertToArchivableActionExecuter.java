package org.alfresco.extension.pdftoolkit.repo.action.executer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.alfresco.extension.pdftoolkit.constants.PDFToolkitConstants;
import org.alfresco.extension.pdftoolkit.constraints.MapConstraint;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.examples.pdfa.CreatePDFA;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.XmpSerializationException;
import org.apache.xmpbox.xml.XmpSerializer;

public class PDFConvertToArchivableActionExecuter extends BasePDFActionExecuter 
{

	/**
     * The logger
     */
    private static Log         logger                   				  = LogFactory.getLog(PDFConvertToArchivableActionExecuter.class);

    /**
     * Action constants
     */
    public static final String NAME                     				  = "pdf-archive";

    /**
     * Constraints
     */
    public static HashMap<String, String> archiveLevelConstraint          = new HashMap<String, String>();
    
    //private final String PDFA											  = "PDF/A";
  
    /**
     * Add parameter definitions
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PDFToolkitConstants.PARAM_DESTINATION_FOLDER, DataTypeDefinition.NODE_REF, false, getParamDisplayLabel(PDFToolkitConstants.PARAM_DESTINATION_FOLDER)));
        paramList.add(new ParameterDefinitionImpl(PDFToolkitConstants.PARAM_ARCHIVE_LEVEL, DataTypeDefinition.INT, true, getParamDisplayLabel(PDFToolkitConstants.PARAM_ARCHIVE_LEVEL), false, "pdfc-archivelevel"));

        super.addParameterDefinitions(paramList);
    }
    
	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) 
	{
		//TODO ADD CODE FOR CONVERT PDF TO PDF/A
	}

    /**
     * Setter for constraint bean
     * 
     * @param encryptionLevelConstraint
     */
    public void setArchiveLevelConstraint(MapConstraint mc)
    {
        archiveLevelConstraint.putAll(mc.getAllowableValues());
    }
    
	public File convertPdfToPdfA(final byte[] source) throws Exception {
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
					pdfaid.setConformance("B");
					pdfaid.setPart(1);
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
