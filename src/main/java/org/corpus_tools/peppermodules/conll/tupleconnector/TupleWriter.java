/**
 * Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
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
package org.corpus_tools.peppermodules.conll.tupleconnector;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Hashtable;

/**
 * This interface provides methods to store tuples in a stream, for example a
 * file stream. Although it provides a transactional treatment for writing
 * tuples.
 * 
 * @author Florian Zipser
 *
 */
public interface TupleWriter {
	/**
	 * Sets the encoding in which data shall be stored. Default is utf-8.
	 * 
	 * @param encoding
	 *            in which data shall be stored
	 */
	public void setEncoding(String encoding);

	/**
	 * Sets an internal flag which specifies whether special chars should be
	 * escaped
	 * 
	 * @param escape
	 *            true, if special chars should be escaped and false, if not.
	 */
	public void setEscaping(boolean escape);

	/**
	 * This method sets the table of escape sequences
	 * 
	 * @param escapeTable
	 *            the table
	 */
	public void setEscapeTable(Hashtable<Character, String> escapeTable);

	/**
	 * This method returns the current escape table for this TupleWriter
	 * 
	 * @return the current escape table
	 */
	public Hashtable<Character, String> getEscapeTable();

	/**
	 * Returns the encoding in which data shall be stored.
	 * 
	 * @return encoding in which data shall be stored
	 */
	public String getEncoding();

	/**
	 * Sets the seperator with which the data will be departed on stream.
	 * Default seperator is tab.
	 * 
	 * @param seperator
	 *            with which the data will be departed on stream
	 */
	public void setSeperator(String seperator);

	/**
	 * Returns the seperator with which the data will be departed on stream.
	 * 
	 * @return seperator with which the data will be departed on stream
	 */
	public String getSeperator();

	/**
	 * Sets the names of attributes to which data belong.
	 * 
	 * @param attNames
	 *            - names of attributes to which data belong
	 */
	public void setAttNames(Collection<String> attNames);

	/**
	 * Returns the names of attributes to which data belong.
	 * 
	 * @return attNames - names of attributes to which data belong
	 */
	public Collection<String> getAttNames();

	/**
	 * Set the distinct value for this tuple writer. If distinct= true, no
	 * duplicates will be stored for the same transaction. duplicates means two
	 * or more tuples with the same entries. To compare two tuples, the equals
	 * method of the string value is used (case sensitive). Default value is
	 * false. Attetntion: setting distinct to true makes add(...) very expensive
	 * 
	 * @param distinct
	 *            true if values shall be stored distinct.
	 */
	public void setDistinct(Boolean distinct);

	/**
	 * Returns true if this tuple writer stores values more than one times, if
	 * they are equal
	 * 
	 * @return true, if tuples were stored distinct, default value is false.
	 */
	public Boolean isDistinct();

	/**
	 * Sets the output file to the given.
	 */
	public void setFile(File out);

	/**
	 * Returns the output file.
	 * 
	 * @return the output file
	 */
	public File getFile();

	// ================================= normal business
	// =================================
	/**
	 * Adds a tuple to the stream without TA-Control.
	 * 
	 * @param tuple
	 *            - the tuple which shall be stored
	 */
	public void addTuple(Collection<String> tuple) throws FileNotFoundException;

	/**
	 * Flushs all non transactional controlled data to stream.
	 */
	// public void flush();
	// ================================= TA-Control
	// =================================
	/**
	 * Starts a transaction and returns a unique id for it.
	 * 
	 * @return a unique transactional id
	 */
	public long beginTA();

	/**
	 * Commits the transaction with the given id.
	 * 
	 * @param TAId
	 *            - transactional id which is returned by beginTA()
	 */
	public void commitTA(Long TAId) throws FileNotFoundException;

	/**
	 * Aborts the transaction with the given id.
	 * 
	 * @param TAId
	 *            - transactional id which is returned by beginTA()
	 */
	public void abortTA(Long TAId);

	/**
	 * Adds a tuple to the stream without TA-Control.
	 * 
	 * @param TAId
	 *            - transactional id to which this adding belongs (returned by
	 *            beginTA())
	 * @param tuple
	 *            - the tuple which shall be stored
	 */
	public void addTuple(Long TAId, Collection<String> tuple) throws FileNotFoundException;

}
