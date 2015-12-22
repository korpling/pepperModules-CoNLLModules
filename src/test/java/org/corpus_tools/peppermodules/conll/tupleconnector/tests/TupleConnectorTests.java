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
package org.corpus_tools.peppermodules.conll.tupleconnector.tests;

import java.io.File;
import java.util.Collection;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.corpus_tools.peppermodules.conll.tupleconnector.TupleConnectorFactory;
import org.corpus_tools.peppermodules.conll.tupleconnector.TupleReader;
import org.corpus_tools.peppermodules.conll.tupleconnector.TupleWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class TupleConnectorTests extends TestCase {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	private TupleWriter tWriter = null;

	/**
	 * @param tWriter
	 *            the tWriter to set
	 */
	public void setTWriter(TupleWriter tWriter) {
		this.tWriter = tWriter;
	}

	/**
	 * @return the tWriter
	 */
	public TupleWriter getTWriter() {
		return this.tWriter;
	}

	private TupleReader tReader = null;

	/**
	 * @param tReader
	 *            the tReader to set
	 */
	public void setTReader(TupleReader tReader) {
		this.tReader = tReader;
	}

	/**
	 * @return the tReader
	 */
	public TupleReader getTReader() {
		return tReader;
	}

	@Override
	public void setUp() {
		this.tWriter = TupleConnectorFactory.fINSTANCE.createTupleWriter();
		this.tWriter.setSeperator("\t");
		this.tReader = TupleConnectorFactory.fINSTANCE.createTupleReader();
		this.tReader.setSeperator("\t");
	}

	private File testFolder = new File("./data/tests");

	public void testWriteRead() throws Exception {
		String info = "Testing writing and re-reading tuples...........";
		try {
			// Datei setzen
			File file = new File(testFolder.toString() + "/" + "testWriteRead.tab");
			tWriter.setFile(file);

			// attributenames
			String[] atts = { "attName1", "attName2", "attName3" };
			Collection<String> attCol = new Vector<String>();
			for (String att : atts)
				attCol.add(att);
			tWriter.setAttNames(attCol);
			assertSame("both collections shall be the same.", attCol, tWriter.getAttNames());

			// tuples schreiben und auslesen
			// schreiben
			String allTuples = "attVal11;attVal12;attVal12#" + "attVal21;attVal22;attVal22#" + "attVal31;attVal32;attVal32";
			String[] strTuples = allTuples.split("#");
			Collection<Collection<String>> tuples = new Vector<Collection<String>>();
			atts = null;
			// durch alle Tupel gehen
			for (String strTuple : strTuples) {
				attCol = new Vector<String>();
				atts = strTuple.split(";");
				for (String att : atts)
					attCol.add(att);
				tuples.add(attCol);
				tWriter.addTuple(attCol);
			}
			// tWriter.flush();

			// auslesen
			tReader.setFile(file);
			Collection<Collection<String>> readTuples = new Vector<Collection<String>>();
			Collection<String> tuple;
			while ((tuple = tReader.getTuple()) != null) {
				readTuples.add(tuple);
			}

			// vergleichen
			if (!readTuples.containsAll(tuples))
				fail("not all expected tuples are read.");

		} catch (Exception e) {
			logger.info(info + "FAILED");
			logger.info(e.getMessage());
			e.printStackTrace();
			throw e;
		}
		logger.info(info + "OK");
	}

	public void testTAMgmt() throws Exception {
		String info = "Testing transaction management..................";
		try {
			// Datei setzen
			File file = new File(testFolder.toString() + "/" + "testTAMgmt.tab");
			tWriter.setFile(file);

			// attributenames
			String[] atts = { "attName1", "attName2", "attName3" };
			Collection<String> attCol = new Vector<String>();
			for (String att : atts)
				attCol.add(att);
			tWriter.setAttNames(attCol);
			assertSame("both collections shall be the same.", attCol, tWriter.getAttNames());

			// tuples schreiben und auslesen
			// schreiben
			String allTuples = "(att11;att12;att13#att21;att22;att23#att31;att32;att33)+(att41;att42;att43#att51;att52;att53)-(att61;att62;att63#att71;att72;att73)+";

			Vector<Long> commitList = new Vector<Long>();
			Vector<Collection<String>> commitTuples = new Vector<Collection<String>>();
			Pattern p = Pattern.compile("[(][a-zA-Z0-9_-[;][.]#]+[)]([+]|-)", Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(allTuples);
			while (m.find()) {
				boolean commit = false;
				String match = m.group();
				if (match.endsWith("+"))
					commit = true;
				else if (match.endsWith("-"))
					commit = false;
				match = match.replace("(", "");
				match = match.replace(")", "");
				match = match.replace("+", "");
				match = match.replace("-", "");

				Long taId = tWriter.beginTA();
				String[] strTuples = match.split("#");
				Collection<Collection<String>> tuples = new Vector<Collection<String>>();
				atts = null;
				// durch alle Tupel gehen
				for (String strTuple : strTuples) {
					attCol = new Vector<String>();
					atts = strTuple.split(";");
					for (String att : atts)
						attCol.add(att);
					tuples.add(attCol);
					tWriter.addTuple(taId, attCol);
					if (commit)
						commitTuples.add(attCol);
				}

				if (commit)
					commitList.add(taId);
				else
					tWriter.abortTA(taId);
			}

			for (Long taId : commitList)
				tWriter.commitTA(taId);

			// auslesen
			tReader.setFile(file);
			Collection<Collection<String>> readTuples = new Vector<Collection<String>>();
			Collection<String> tuple;
			while ((tuple = tReader.getTuple()) != null) {
				readTuples.add(tuple);
			}

			// vergleichen
			if (!readTuples.containsAll(commitTuples))
				fail("not all expected tuples are read.");
		} catch (Exception e) {
			logger.info(info + "FAILED");
			logger.info(e.getMessage());
			e.printStackTrace();
			throw e;
		}
		logger.info(info + "OK");
	}

	public void testWriteReadDistinct() throws Exception {
		String info = "Testing writing and re-reading tuples...........";
		try {
			TupleWriter tWriter = TupleConnectorFactory.fINSTANCE.createTupleWriter();
			TupleReader tReader = TupleConnectorFactory.fINSTANCE.createTupleReader();
			// Datei setzen
			File file = new File(this.testFolder.toString() + "/" + "testWriteReadDistinct.tab");
			if (file.delete())
				file.createNewFile();

			tWriter.setFile(file);

			// set distince
			tWriter.setDistinct(true);

			Vector<Vector<String>> distinctTuples = new Vector<Vector<String>>();

			for (int i = 0; i < 10; i++) {
				Vector<String> tuple = new Vector<String>();
				distinctTuples.add(tuple);
				tuple.add("row1.val" + i);
				tuple.add("row2.val" + i);
				tuple.add("row3.val" + i);
				tuple.add("row4.val" + i);
			}

			Long ta = tWriter.beginTA();
			for (Vector<String> tuple : distinctTuples) {
				tWriter.addTuple(ta, tuple);
			}

			for (Vector<String> tuple : distinctTuples) {
				Vector<String> newTuple = new Vector<String>();
				for (String entry : tuple) {
					newTuple.add(new String(entry));
				}
				tWriter.addTuple(ta, newTuple);
			}
			// flush tuple writer
			tWriter.commitTA(ta);

			// set input file
			tReader.setFile(file);
			tReader.readFile();
			if (distinctTuples.size() < tReader.getNumOfTuples())
				fail("to much tuples are stored");
			else if (distinctTuples.size() > tReader.getNumOfTuples())
				fail("to less tuples are stored");

		} catch (Exception e) {
			logger.info(info + "FAILED");
			logger.info(e.getMessage());
			e.printStackTrace();
			throw e;
		}
		logger.info(info + "OK");
	}

	public void testWriteReadNotDistinct() throws Exception {
		String info = "Testing writing and re-reading tuples...........";
		try {
			TupleWriter tWriter = TupleConnectorFactory.fINSTANCE.createTupleWriter();
			TupleReader tReader = TupleConnectorFactory.fINSTANCE.createTupleReader();
			// Datei setzen
			File file = new File(this.testFolder.toString() + "/" + "testWriteReadNotDistinct.tab");
			if (file.delete())
				file.createNewFile();
			tWriter.setFile(file);

			Vector<Vector<String>> distinctTuples = new Vector<Vector<String>>();

			for (int i = 0; i < 10; i++) {
				Vector<String> tuple = new Vector<String>();
				distinctTuples.add(tuple);
				tuple.add("row1.val" + i);
				tuple.add("row2.val" + i);
				tuple.add("row3.val" + i);
				tuple.add("row4.val" + i);
			}

			for (int i = 0; i < 10; i++) {
				Vector<String> tuple = new Vector<String>();
				distinctTuples.add(tuple);
				tuple.add("row1.val" + i);
				tuple.add("row2.val" + i);
				tuple.add("row3.val" + i);
				tuple.add("row4.val" + i);
			}

			Long ta = tWriter.beginTA();

			for (Vector<String> tuple : distinctTuples) {
				tWriter.addTuple(ta, tuple);
			}
			// flush tuple writer
			tWriter.commitTA(ta);

			// set input file
			tReader.setFile(file);
			tReader.readFile();
			if (distinctTuples.size() != tReader.getNumOfTuples())
				fail("not all tuples were stored and read");
		} catch (Exception e) {
			logger.info(info + "FAILED");
			logger.info(e.getMessage());
			e.printStackTrace();
			throw e;
		}
		logger.info(info + "OK");
	}

	public void testPerformance() throws Exception {
		Long timeToRead = null;
		Long timeToWrite = null;
		try {
			File inputFile = new File(this.testFolder.toString() + "/" + "Performance.tab");
			File outputFile = new File(this.testFolder.toString() + "/" + "Performance2.tab");

			TupleWriter tWriter = TupleConnectorFactory.fINSTANCE.createTupleWriter();
			TupleReader tReader = TupleConnectorFactory.fINSTANCE.createTupleReader();

			Collection<Collection<String>> readTuples = new Vector<Collection<String>>();

			{// read file
				// set input file
				tReader.setFile(inputFile);
				tReader.readFile();
				Collection<String> tuple;
				timeToRead = System.nanoTime();
				while ((tuple = tReader.getTuple()) != null) {
					readTuples.add(tuple);
				}
				timeToRead = System.nanoTime() - timeToRead;
			} // read file

			{// write file
				tWriter.setFile(outputFile);
				Long taId = tWriter.beginTA();

				timeToWrite = System.nanoTime();

				for (Collection<String> tuple : readTuples) {
					tWriter.addTuple(taId, tuple);
				}
				tWriter.commitTA(taId);

				timeToWrite = System.nanoTime() - timeToWrite;
			} // write file

			System.out.println("Needed time to read data:\t" + timeToRead / 1000000);
			System.out.println("Needed time to write data:\t" + timeToWrite / 1000000);
			assertTrue("the performance for reading is worser than expected", (50l > timeToRead / 1000000));
			assertTrue("the performance for writing is worser than expected", (2500l > timeToRead / 1000000));

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
