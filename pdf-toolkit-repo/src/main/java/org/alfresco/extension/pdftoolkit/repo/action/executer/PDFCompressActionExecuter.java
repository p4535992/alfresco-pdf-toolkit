package org.alfresco.extension.pdftoolkit.repo.action.executer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.extension.pdftoolkit.constants.PDFToolkitConstants;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import com.itextpdf.text.exceptions.InvalidImageException;
import com.itextpdf.text.exceptions.UnsupportedPdfException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.IIOException;
/**
 * Created by peter on 2014-01-10.
 */
public class PDFCompressActionExecuter extends BasePDFActionExecuter {

    /**
     * The logger
     */
    private static Log logger                   = LogFactory.getLog(PDFCompressActionExecuter.class);

    /**
     * Action constants
     */
    public static final String NAME                     = "pdf-compress";

    /**
     * Add parameter definitions
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PDFToolkitConstants.PARAM_DESTINATION_FOLDER, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PDFToolkitConstants.PARAM_DESTINATION_FOLDER)));
        paramList.add(new ParameterDefinitionImpl(PDFToolkitConstants.PARAM_COMPRESSION_LEVEL, DataTypeDefinition.INT, false, getParamDisplayLabel(PDFToolkitConstants.PARAM_COMPRESSION_LEVEL)));
        paramList.add(new ParameterDefinitionImpl(PDFToolkitConstants.PARAM_IMAGE_COMPRESSION_LEVEL, DataTypeDefinition.INT, false, getParamDisplayLabel(PDFToolkitConstants.PARAM_IMAGE_COMPRESSION_LEVEL)));
        
        super.addParameterDefinitions(paramList);
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.repository.NodeRef,
     * org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
    	// Compress the document with the requested options
    	NodeRef result = pdfToolkitService.compressPDF(actionedUponNodeRef, action.getParameterValues());
    	action.setParameterValue(PARAM_RESULT, result);
    }
}
