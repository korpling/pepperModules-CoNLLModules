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
package org.corpus_tools.peppermodules.conll.tupleconnector.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import org.corpus_tools.peppermodules.conll.tupleconnector.TupleReader;

public class TupleReaderImpl implements TupleReader {
	/**
	 * input file
	 */
	private File inFile = null;

	/**
	 * seperator which departs attributes
	 */
	private String seperator = "\t";

	/**
	 * encoding in which file is written
	 */
	private String encoding = null;

	// BOM character
	private static final Character utf8BOM = new Character((char) 0xFEFF);

	/**
	 * collection of tuples, tuples are a collection of attributes
	 */
	Collection<Collection<String>> tuples = null;

	/**
	 * stores current position of asked tuple
	 */
	int tuplePtr = 0;

	/**
	 * stores the overall number of characters read
	 */
	private Integer charCount = 0;

	/**
	 * stores the number of characters read per field index
	 */
	private ArrayList<Integer> charCountList = new ArrayList<Integer>();

	@Override
	public void setFile(File inFile) {
		if (inFile == null)
			throw new NullPointerException("Error(TupleReader): the given file-object is empty.");
		if (!inFile.exists())
			throw new NullPointerException("Error(TupleReader): the given file does not exist: " + inFile + ".");
		if (!inFile.isFile())
			throw new NullPointerException("Error(TupleReader): the given file-object is not a file: " + inFile + ".");
		this.inFile = inFile;
	}

	@Override
	public File getFile() {
		return (this.inFile);
	}

	@Override
	public void setSeperator(String seperator) {
		if ((seperator == null) || (seperator.equals("")))
			throw new NullPointerException("Error(TupleReader): the given seperator is empty.");
		this.seperator = seperator;
	}

	@Override
	public String getSeperator() {
		return (this.seperator);
	}

	@Override
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	@Override
	public String getEncoding() {
		return (this.encoding);
	}

	public void readFile() throws IOException {
		if (this.inFile == null)
			throw new NullPointerException("Error(TupleReader): Cannot read from empty file.");
		BufferedReader inReader = new BufferedReader(new InputStreamReader(new FileInputStream(this.inFile), "UTF8"));
		Collection<String> atts = null;
		String input = "";
		tuples = new Vector<Collection<String>>();
		Integer fieldIndex = 0;
		charCount = 0;
		charCountList.clear();
		int fileLineCount = 0;
		while ((input = inReader.readLine()) != null) {
			fileLineCount++;
			// delete BOM if exists
			if ((fileLineCount == 1) && (input.startsWith(utf8BOM.toString())))
				input = input.substring(utf8BOM.toString().length());

			atts = new Vector<String>();
			if (input != null) {
				String[] attStr = input.split(this.seperator);
				fieldIndex = 0;
				for (String att : attStr) {
					atts.add(att);
					try {
						charCountList.set(fieldIndex, charCountList.get(fieldIndex) + att.length());
					} catch (IndexOutOfBoundsException e) {
						charCountList.add(att.length());
					}
					charCount += att.length();
					fieldIndex++;
				}

				tuples.add(atts);
			}
		}
		inReader.close();
	}

	/**
	 * Returns a new tuple. If no more tuples exists, return value is null.
	 */
	public Collection<String> getTuple() throws IOException {
		if (tuples == null)
			this.readFile();
		if (this.tuplePtr >= tuples.size())
			return (null);
		Collection<String> tuple = (Collection<String>) ((Vector<Collection<String>>) this.tuples).get(this.tuplePtr);
		this.tuplePtr++;
		return (tuple);
	}

	@Override
	public Integer getNumOfTuples() {
		Integer retVal = 0;
		if (tuples != null)
			retVal = tuples.size();
		return (retVal);
	}

	/**
	 * Returns all currently read tuples.
	 * 
	 * @return all read tuples.
	 */
	public Collection<Collection<String>> getTuples() {
		return (this.tuples);
	}

	@Override
	public Collection<String> getTuple(Integer index) throws IOException {
		Collection<String> tuple = null;
		if (tuples != null) {
			tuple = ((Vector<Collection<String>>) tuples).get(index);
		}
		return (tuple);
	}

	@Override
	public void restart() {
		this.tuplePtr = 0;
	}

	@Override
	public Integer size() {
		return (this.getNumOfTuples());
	}

	@Override
	public Integer characterSize() {
		return charCount;
	}

	@Override
	/**
	 * @throws IndexOutOfBoundsException
	 */
	public Integer characterSize(Integer fieldIndex) {
		return charCountList.get(fieldIndex);
	}
}
