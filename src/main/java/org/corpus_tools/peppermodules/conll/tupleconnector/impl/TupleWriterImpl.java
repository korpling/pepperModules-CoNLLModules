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
package org.corpus_tools.peppermodules.conll.tupleconnector.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.corpus_tools.peppermodules.conll.tupleconnector.TupleWriter;
import org.corpus_tools.peppermodules.conll.tupleconnector.exceptions.TupleWriterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TupleWriterImpl implements TupleWriter {
	private Logger logger = LoggerFactory.getLogger(TupleWriterImpl.class);

	private final Lock writerLock = new ReentrantLock();

	private String encoding = "UTF-8";
	private String sperator = "\t";

	private boolean escapeCharacters = false;

	private Hashtable<Character, String> charEscapeTable;

	/**
	 * output file
	 */
	private File outFile = null;
	/**
	 * Names of stored attributes
	 */
	private Collection<String> attNames = null;

	private PrintStream oStream = null;

	/**
	 * Constructor
	 */
	public TupleWriterImpl() {
		charEscapeTable = new Hashtable<Character, String>();
		/**
		 * Standard escaping \t \n \r \ '
		 */
		charEscapeTable.put('\t', "TAB");
		charEscapeTable.put('\n', "NEWLINE");
		charEscapeTable.put('\r', "RETURN");
		charEscapeTable.put('\\', "\\\\");
		charEscapeTable.put('\'', "\\'");
	}

	@Override
	public void addTuple(Collection<String> tuple) throws FileNotFoundException {
		// if (logger!= null)
		// logger.debug("adding tuple for TA: (without ta-control)");
		Long taId = this.beginTA();
		this.addTuple(taId, tuple);
		this.commitTA(taId);
	}

	// ----------------------------- Start TA-Management
	// -----------------------------
	private volatile Long TAId = 0l;
	/**
	 * relates tuple-Ids to Collections of tuples
	 */
	private volatile Map<Long, Collection<Collection<String>>> tupleMap = Collections.synchronizedMap(new HashMap<Long, Collection<Collection<String>>>());

	/**
	 * Returns a new TA-Id
	 * 
	 * @return TAId
	 */
	private synchronized Long getNewTAId() {
		long currTAId = TAId;
		TAId++;
		return (currTAId);
	}

	@Override
	public long beginTA() {
		if (logger != null)
			logger.debug("begin ta of TupleWriter '" + this.getFile() + "' with id: " + TAId);
		Long taId = getNewTAId();
		this.tupleMap.put(taId, new Vector<Collection<String>>());
		return (taId);
	}

	@Override
	public void addTuple(Long TAId, Collection<String> tuple) throws FileNotFoundException {
		// if (logger!= null) logger.debug("adding tuple for TA: "+ TAId);
		Collection<Collection<String>> taIdSlot = this.tupleMap.get(TAId);

		if (taIdSlot == null)
			taIdSlot = new Vector<Collection<String>>();
		if (!this.distinct) {// if values shall be stored also if they are
								// duplicates
			taIdSlot.add(tuple);
		} // if values shall be stored also if they are duplicates
		else {
			Object[] tupleArray = tuple.toArray();
			Boolean tupleExists = false;
			for (Collection<String> existingTuple : taIdSlot) {// walk through
																// all existing
																// tuples
				Object[] existingTupleArray = existingTuple.toArray();
				tupleExists = true;
				for (int i = 0; i < existingTupleArray.length - 1; i++) {
					if (existingTupleArray.length != tupleArray.length)
						throw new TupleWriterException("The given tuple has not the expected number of entries.");
					if ((existingTupleArray[i] == null) && (tupleArray[i] == null))
						;
					else if (((existingTupleArray[i] == null) && (tupleArray[i] != null)) || ((existingTupleArray[i] != null) && (tupleArray[i] == null)) || (!existingTupleArray[i].equals(tupleArray[i]))) {// if
																																																				// one
																																																				// entry
																																																				// is
																																																				// not
																																																				// equal,
																																																				// the
																																																				// tuple
																																																				// is
																																																				// not
																																																				// equal
						tupleExists = false;
						break;
					} // if one entry is not equal, the tuple is not equal
				}
				if (tupleExists)
					break;
			} // walk through all existing tuples
			if (!tupleExists) {
				taIdSlot.add(tuple);
			}
		}
	}

	@Override
	public void commitTA(Long TAId) throws FileNotFoundException {
		if (TAId == null) {
			throw new TupleWriterException("Cannot commit an empty transaction id for TupleWriter controlling file '" + this.getFile() + "'.");
		}
		if (logger != null)
			logger.debug("commiting ta of TupleWriter '" + this.getFile() + "' with id: " + TAId);
		if (this.tupleMap.containsKey(TAId)) {
			this.flush(TAId);
		}
	}

	@Override
	public void abortTA(Long TAId) {
		if (logger != null)
			logger.debug("aborting ta with id: " + TAId);
		this.tupleMap.remove(TAId);

	}

	private void flush(Long TAId) throws FileNotFoundException {
		if (logger != null)
			logger.debug("flushing all tuples of tupleWriter '" + this.getFile().getName() + "' which belong to TA with id: " + TAId);
		if (outFile == null)
			throw new FileNotFoundException("Error(TupleWriter): The datasource is empty.");
		try {

			writerLock.lock();

			StringBuffer tuples = new StringBuffer();
			// zu schreibende tuple ermitteln
			Collection<Collection<String>> printableTuples = this.tupleMap.get(TAId);
			for (Collection<String> tuple : printableTuples) {
				int i = 0;
				for (String att : tuple) {
					// don't escape if attribute is NULL
					if (this.escapeCharacters && att != null) { // if escaping
																// should be
																// done
						StringBuffer escaped = new StringBuffer();
						for (char chr : att.toCharArray()) { // for every char
																// in the atring
							String escapeString = this.charEscapeTable.get(chr);
							if (escapeString != null) { // if there is some
														// escape sequence
								escaped.append(escapeString);
							} else {
								escaped.append(chr);
							}
						}
						tuples.append(escaped.toString());
					} else { // if NO escaping should be done
						tuples.append(att);
					}

					i++;
					if (i < tuple.size())
						tuples.append(this.getSeperator());
				}
				tuples.append("\n");
			}

			if (this.oStream == null)
				oStream = new PrintStream(new FileOutputStream(this.outFile), true, this.encoding);
			this.oStream.print(tuples.toString());
			this.oStream.flush();
			this.tupleMap.remove(TAId);
		} catch (RuntimeException e) {
			throw new TupleWriterException("Cannot commit ta, because writing to file does not worked. ", e);
		} catch (UnsupportedEncodingException e) {
			throw new TupleWriterException("Cannot commit ta, because writing to file does not worked. ", e);
		} finally {
			writerLock.unlock();
		}
	}

	// ----------------------------- End TA-Management
	// -----------------------------

	@Override
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	@Override
	public void setEscaping(boolean escape) {
		this.escapeCharacters = escape;
	}

	@Override
	public void setEscapeTable(Hashtable<Character, String> escapeTable) {
		if (escapeTable == null)
			throw new TupleWriterException("Error(TupleWriter): The given escape table object is null.");
		this.charEscapeTable = escapeTable;
	}

	@Override
	public Hashtable<Character, String> getEscapeTable() {
		return this.charEscapeTable;
	}

	@Override
	public String getEncoding() {
		return (this.encoding);
	}

	@Override
	public void setFile(File out) {
		if (out == null)
			throw new TupleWriterException("Error(TupleWriter): an empty file-object is given.");

		if (out.getParent() == null)
			throw new TupleWriterException("Cannot set the given file, because it has no parent folder: " + out + ".");
		if (!out.getParentFile().exists())
			out.getParentFile().mkdirs();

		this.outFile = out;
	}

	@Override
	public File getFile() {
		return (this.outFile);
	}

	@Override
	public void setAttNames(Collection<String> attNames) {
		if (attNames == null)
			throw new TupleWriterException("ERROR(TupleWriter): The given collection with attribute names is empty.");
		this.attNames = attNames;
	}

	@Override
	public Collection<String> getAttNames() {
		return (this.attNames);
	}

	@Override
	public void setSeperator(String seperator) {
		this.sperator = seperator;
	}

	@Override
	public String getSeperator() {
		return (this.sperator);
	}

	public void finalize() {
		if ((this.tupleMap != null) && (this.tupleMap.size() != 0)) {
			if (logger != null)
				logger.warn("Warning(TupleWriter): Not all TAs are comitted or aborted. There remains " + this.tupleMap.size() + " TAs.");
		}
		this.oStream.close();
	}

	/**
	 * if values shall be stored also if they are duplicates
	 */
	private Boolean distinct = false;

	@Override
	public Boolean isDistinct() {
		return (this.distinct);
	}

	@Override
	public void setDistinct(Boolean distinct) {
		if (distinct != null)
			this.distinct = distinct;
	}
}
