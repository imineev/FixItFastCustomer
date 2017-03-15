package oracle.mobile.cloud.sample.fif.customer.app.images;

import oracle.adfmf.framework.exception.AdfException;


/**
 * Thrown when MAF to MCS Collection communication fails or when an operation within a FiFImageHandler class fails
 */
public class ImageHandlerException extends AdfException {
    
    public ImageHandlerException(Throwable throwable, String severity) {
        super(throwable, severity);
    }

    public ImageHandlerException(Throwable throwable) {
        super(throwable);
    }

    public ImageHandlerException(String message, String severity) {
        super(message, severity);
    }

    public ImageHandlerException() {
        super();
    }
}
