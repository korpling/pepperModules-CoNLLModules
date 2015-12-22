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
import java.io.IOException;
import java.util.Collection;

/**
 * Classes which implements this interface can read a stream which contains
 * tuples. Although tuples will be read in whole and gave back tuple by tuple.
 * 
 * @author Florian Zipser
 *
 */
public interface TupleReader {
	/**
	 * Sets the file from which the tuples shall come, so called datasource.
	 * 
	 * @param inFile
	 *            - datasource
	 */
	public void setFile(File inFile);

	/**
	 * Returns the
	 * 
	 * @return file from which the tuples shall come, so called datasource.
	 * @return datasource
	 */
	public File getFile();

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
	 * Sets the encoding in which data shall be stored. Default is utf-8.
	 * 
	 * @param encoding
	 *            in which data shall be stored
	 */
	public void setEncoding(String encoding);

	/**
	 * Returns the encoding in which data shall be stored.
	 * 
	 * @return encoding in which data shall be stored
	 */
	public String getEncoding();

	/**
	 * Returns all currently read tuples.
	 * 
	 * @return all read tuples.
	 */
	public Collection<Collection<String>> getTuples();

	/**
	 * Returns a the tuple which is next in datasource. Datasource will be read
	 * one time and stored internal. a pointer is set to data and incremented
	 * when this method is called.
	 * 
	 * @return current tuple
	 */
	public Collection<String> getTuple() throws IOException;

	/**
	 * Returns the number of read tuples.
	 * 
	 * @return number of tuples
	 */
	public Integer getNumOfTuples();

	/**
	 * Returns the tuple at position index.
	 * 
	 * @param index
	 *            position of tuple to return
	 * @return tuple at position index
	 * @throws IOException
	 */
	public Collection<String> getTuple(Integer index) throws IOException;

	/**
	 * Sets internal tuple pointer to start, so that getTuple() returns all
	 * tuples from beginning.
	 */
	public void restart();

	/**
	 * Returns the number of tuples contained in this tuple reader.
	 * 
	 * @return number of tuples
	 * @return
	 */
	public Integer size();

	/**
	 * Returns the number of characters contained in this tuple reader.
	 * 
	 * @return number of characters
	 * @author hildebax
	 */
	public Integer characterSize();

	/**
	 * Returns the number of characters contained in the field given by
	 * <code>fieldIndex</code> of this tuple reader.
	 * 
	 * @param fieldIndex
	 *            the index of the field
	 * @return number of characters in the field
	 * @author hildebax
	 */
	public Integer characterSize(Integer fieldIndex);

	/**
	 * Reads the given file and creates an internal list of read tuples.
	 * 
	 * @throws IOException
	 */
	public void readFile() throws IOException;
}
