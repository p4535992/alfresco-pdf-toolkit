package org.alfresco.extension.pdftoolkit.repo.action.executer;

import java.util.List;

import org.alfresco.extension.pdftoolkit.constants.PDFToolkitConstants;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
