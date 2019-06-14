package org.alfresco.extension.pdftoolkit.repo.action.executer;


import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.extension.pdftoolkit.naming.FileNameProvider;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.util.PDFMergerUtility;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;


/**
 * Collate PDF action executer
 *
 * @author Bhagya Silva
 */

public class PDFCollateActionExecuter extends BasePDFActionExecuter

{

    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(PDFCollateActionExecuter.class);

    /**
     * Action constants
     */
    public static final String NAME = "pdf-collate";
    public static final String PARAM_TARGET_NODE = "target-node";
//    public static final String PARAM_DESTINATION_FOLDER = "destination-folder";
    public static final String PARAM_DESTINATION_NAME = "destination-name";

    public void setFileNameProvider(FileNameProvider fileNameProvider) {
        this.fileNameProvider = fileNameProvider;
    }

    private FileNameProvider fileNameProvider;

    /**
     * Add parameter definitions
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PARAM_TARGET_NODE, DataTypeDefinition.NODE_REF, false, getParamDisplayLabel(PARAM_TARGET_NODE)));
//        paramList.add(new ParameterDefinitionImpl(PARAM_DESTINATION_FOLDER, DataTypeDefinition.NODE_REF, false, getParamDisplayLabel(PARAM_DESTINATION_FOLDER)));
        paramList.add(new ParameterDefinitionImpl(PARAM_DESTINATION_NAME, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_DESTINATION_NAME)));

        super.addParameterDefinitions(paramList);
    }

    @Override
    protected final void executeImpl(Action action, NodeRef actionedUponNodeRef) {  
    	try{
    		logger.info("START ACION : " + this.getClass().getSimpleName());
	    	//Collate PDF
	    	NodeRef result = pdfToolkitService.collatePDF(actionedUponNodeRef, action.getParameterValues());
	    	action.setParameterValue(PARAM_RESULT, result);
	    	logger.info("END ACION : " + this.getClass().getSimpleName());
    	}catch(Throwable ex){
    		logger.error(ex.getMessage(),ex);
    		throw ex;
    	}
    }

}
