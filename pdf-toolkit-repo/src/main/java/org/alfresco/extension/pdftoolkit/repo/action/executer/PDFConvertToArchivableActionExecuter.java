package org.alfresco.extension.pdftoolkit.repo.action.executer;

import java.util.HashMap;
import java.util.List;

import org.alfresco.extension.pdftoolkit.constants.PDFToolkitConstants;
import org.alfresco.extension.pdftoolkit.constraints.MapConstraint;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    	try{
    		logger.info("START ACION : " + this.getClass().getSimpleName());
            RetryingTransactionCallback<NodeRef> callback = new RetryingTransactionCallback<NodeRef>() {
                @Override
                public NodeRef execute() throws Throwable {
        	    	// Convert the document to archivable with the requested options
        	    	NodeRef result = pdfToolkitService.archivablePDF(actionedUponNodeRef, action.getParameterValues());
        	    	action.setParameterValue(PARAM_RESULT, result);
                    return result;
                }
            };
            pdfToolkitService.executeInNewTransaction(callback);
	    	logger.info("END ACION : " + this.getClass().getSimpleName());
    	}catch(Throwable ex){
    		logger.error(ex.getMessage(),ex);
    		throw ex;
    	}
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

    public static enum ArchiveLevel{
    	PDFX1A2001("PDFX1A2001"),
    	PDFA1A("PDFA1A"),
    	PDFA1B("PDFA1B");
    	
		private String value;

		ArchiveLevel(String value) {
	        this.value = value;
	    }

	    public String value() {
	        return value;
	    }
    	
    	public static ArchiveLevel fromValue(String value) throws IllegalArgumentException {
			String tipoFattura = value.trim().replaceAll(" +", " ");
			for (ArchiveLevel b : ArchiveLevel.values()) {
		      if (b.value.equalsIgnoreCase(tipoFattura)) {
		        return b;
		      }
		    }
			throw new IllegalArgumentException("Non è stato trovato alcun '"+ArchiveLevel.class+"' per la stringa:" + value);			
		}
	    
	    public static boolean containsValue(String value) throws IllegalArgumentException {
			String tipoFattura = value.trim().replaceAll(" +", " ");
			for (ArchiveLevel b : ArchiveLevel.values()) {
		      if (b.value.equalsIgnoreCase(tipoFattura)) {
		        return true;
		      }
		    }
			return false;			
		}	
    	
    	
    	public String getConformance(){
    		switch(this){
	    		case PDFA1A:
	    		case PDFX1A2001:{
	    			return "A";
	    		}
	    		case PDFA1B:{
	    			return "B";
	    		}
	    		default:{
	    			throw new IllegalArgumentException("Non è stato trovato alcun '"+ArchiveLevel.class+"'");	
	    		}
    		}
    	}
    	
    	public Integer getPart(){
    		switch(this){
	    		case PDFA1A:
	    		case PDFX1A2001:{
	    			return 1;
	    		}
	    		case PDFA1B:{
	    			return 1;
	    		}
	    		default:{
	    			throw new IllegalArgumentException("Non è stato trovato alcun '"+ArchiveLevel.class+"'");	
	    		}
    		}
    	}
    	
    	public String getAmdId(){
    		switch(this){
	    		case PDFX1A2001:{
	    			return "2001";
	    		}
	    		case PDFA1A:
	    		case PDFA1B:{
	    			return null;
	    		}
	    		default:{
	    			throw new IllegalArgumentException("Non è stato trovato alcun '"+ArchiveLevel.class+"'");	
	    		}
    		}
    	}
    	
    	 
    }
}
