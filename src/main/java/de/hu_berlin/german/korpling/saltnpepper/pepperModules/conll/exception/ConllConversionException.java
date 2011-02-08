package de.hu_berlin.german.korpling.saltnpepper.pepperModules.conll.exception;

/**
 * This is the super class for all ConLL converter exceptions
 * @author hildebax
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ConllConversionException extends RuntimeException {
	
	/**
	 * Constructs a new exception with <code>null</code> as its detail message. 
	 */
	ConllConversionException() {
		super();
	}
	
    /**
     * Constructs a new exception with the specified detail message.
     * @param msg the detail message
     */
	ConllConversionException(String msg) {
		super(msg);
	}
	
    /**
     * Constructs a new exception with the specified detail message and cause. 
     * @param msg the detail message
	 * @param cause the cause
	 */
	ConllConversionException(String msg, Throwable cause) {
		super(msg,cause);		
	}

}
