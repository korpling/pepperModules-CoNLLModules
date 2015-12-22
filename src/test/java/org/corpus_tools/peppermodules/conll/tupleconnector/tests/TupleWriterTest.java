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

import org.corpus_tools.peppermodules.conll.tupleconnector.TupleConnectorFactory;
import org.corpus_tools.peppermodules.conll.tupleconnector.TupleWriter;
import org.corpus_tools.peppermodules.conll.tupleconnector.impl.TupleWriterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class TupleWriterTest extends TestCase {
	private Logger logger = LoggerFactory.getLogger(TupleWriterImpl.class);

	private TupleWriter fixture = null;

	public TupleWriter getFixture() {
		return (this.fixture);
	}

	public void setFixture(TupleWriter fixture) {
		this.fixture = fixture;
	}

	@Override
	public void setUp() {
		this.setFixture(TupleConnectorFactory.fINSTANCE.createTupleWriter());
	}

	public void testAllSetters() throws Exception {
		String info = "Testing all setters and checking with getters...";
		try {
			// encoding
			String enc = "UTF-8";
			assertTrue("both encodings has to be the same. ", this.getFixture().getEncoding().equalsIgnoreCase(enc));

			// seperator
			String sep = "\t";
			assertTrue("both sperator has to be the same. ", this.getFixture().getSeperator().equalsIgnoreCase(sep));

			// seperator
			File file = new File("./newFile.tab");
			this.getFixture().setFile(file);
			assertEquals("both files shall be the same.", this.getFixture().getFile(), file);

			// attributenames
			String[] atts = { "att1", "att2", "att3" };
			Collection<String> attCol = new Vector<String>();
			for (String att : atts)
				attCol.add(att);
			this.getFixture().setAttNames(attCol);
			assertSame("both collections shall be the same.", attCol, this.getFixture().getAttNames());

		} catch (Exception e) {
			logger.info(info + "FAILED");
			logger.info(e.getMessage());
			e.printStackTrace();
			throw e;
		}
		logger.info(info + "OK");
	}
}
