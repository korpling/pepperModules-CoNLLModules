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

import org.corpus_tools.peppermodules.conll.tupleconnector.TupleConnectorFactory;
import org.corpus_tools.peppermodules.conll.tupleconnector.TupleReader;
import org.corpus_tools.peppermodules.conll.tupleconnector.TupleWriter;

public class TupleConnectorFactoryImpl implements TupleConnectorFactory {
	public static volatile TupleConnectorFactoryImpl instance = null;

	public synchronized static TupleConnectorFactoryImpl init() {
		if (instance == null)
			instance = new TupleConnectorFactoryImpl();
		return (instance);
	}

	private TupleConnectorFactoryImpl() {

	}

	/**
	 * Returns a TupleWriter-object.
	 */
	// @Override
	public Object getObject() throws Exception {
		return (new TupleWriterImpl());
	}

	/**
	 * Returns the class of TupleWriter-object.
	 */
	// @Override
	public Class<TupleWriter> getObjectType() {
		return (TupleWriter.class);
	}

	/**
	 * The produced objects are no singletons.
	 */
	// @Override
	public boolean isSingleton() {
		return false;
	}

	/**
	 * Returns a new TupleWriter-object.
	 * 
	 * @return a new TupleWriter-object
	 */
	public TupleWriter createTupleWriter() {
		TupleWriter tupleWriter = new TupleWriterImpl();
		return (tupleWriter);
	}

	@Override
	public TupleReader createTupleReader() {
		TupleReader tupleReader = new TupleReaderImpl();
		return (tupleReader);
	}

}
