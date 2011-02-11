/**
 * Copyright 2009 Humboldt University of Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
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
