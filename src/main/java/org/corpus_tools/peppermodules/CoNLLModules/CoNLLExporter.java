package org.corpus_tools.peppermodules.CoNLLModules;

import org.corpus_tools.pepper.impl.PepperExporterImpl;
import org.corpus_tools.pepper.modules.PepperExporter;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.peppermodules.conll.Salt2ConllMapper;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

@Component(name = "CoNLLExporterComponent", factory = "PepperExporterComponentFactory")
public class CoNLLExporter extends PepperExporterImpl implements PepperExporter {
	// -------------------------------------------------------------------------
	public static final String NAME = "CoNLLExporter";
	public static final String FORMATNAME = "CoNLL";
	public static final String FORMATVERSION = "1.0"; // TODO: What version?
	// -------------------------------------------------------------------------

	public CoNLLExporter() {
		super();
		// setting name of module
		this.setName(NAME);
		setSupplierContact(URI.createURI("saltnpepper@lists.hu-berlin.de"));
		setSupplierHomepage(URI.createURI("https://github.com/korpling/pepperModules-CoNLLModules"));
		setDesc("This exporter transforms a Salt model to data in CoNLL format.");
		// set list of formats supported by this module
		this.addSupportedFormat(FORMATNAME, FORMATVERSION, null);
		setProperties(new CoNLLExporterProperties());
	}

	/**
	 * Creates a mapper of type {@link Salt2ConllMapperMapper}.
	 * {@inheritDoc PepperModule#createPepperMapper(Identifier)}
	 */
	@Override
	public PepperMapper createPepperMapper(Identifier sElementId) {
		Salt2ConllMapper mapper = new Salt2ConllMapper();
		if (sElementId.getIdentifiableElement() instanceof SDocument) {
			mapper.setResourceURI(getCorpusDesc().getCorpusPath());
		}
		return (mapper);
	}
}
