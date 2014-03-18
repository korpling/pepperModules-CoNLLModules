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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.conll.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.junit.Before;
import org.junit.Test;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.CoNLLModules.CoNLLImporterProperties;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.conll.Conll2SaltMapper;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SPointingRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltSemantics.SLemmaAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltSemantics.SPOSAnnotation;

public class Conll2SaltMapperTest{

	private Conll2SaltMapper fixture = null;
	private Conll2SaltMapper getFixture() {return fixture;}
	private void setFixture(Conll2SaltMapper fixture) {this.fixture = fixture;}

	@Before
	public void setUp() {
		this.setFixture(new Conll2SaltMapper());
		this.getFixture().setProperties(URI.createFileURI("./src/test/resources/ConllModules_feats.properties"));
		SDocument sDoc= SaltFactory.eINSTANCE.createSDocument();
		sDoc.setSElementId(SaltFactory.eINSTANCE.createSElementId());
		getFixture().setSDocument(sDoc);
		getFixture().getSDocument().setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
	}
	
// the following property values are specific for the input file 

	//private String testFileURI = "src/test/resources/zossen.conll";
	private URI testFileURI = URI.createFileURI("src/test/resources/zossen.conll");
	
	private String[] tokenFormsExpected = {
			"Die",
			"Jugendlichen",
			"in",
			"Zossen",
			"wollen",
			"ein",
			"Musikcafe",
			"."
	};

	private	int rootTokenIndex = 4; // index of root token (has no incoming pointing relations)
	
	private String[][] tokenAnnotationNamesExpected = { 
			{"CPOSTAG","POSTAG","case","num","gender"},		// "Die"	
			{"CPOSTAG","POSTAG","case","num","gender"}, 	// "Jugendlichen"
			{"CPOSTAG","POSTAG"},							// "in"
			{"CPOSTAG","POSTAG","case","num","gender"},		// "Zossen"
			{"CPOSTAG","POSTAG","pers","num","temp","mod"},	// "wollen"
			{"CPOSTAG","POSTAG","case","num","gender"},		// "ein"
			{"CPOSTAG","POSTAG","case","num","gender"},		// "Musikcafe"
			{"CPOSTAG","POSTAG"}							// "."
	}; 
	
	//these include the feature values
	private String[][] tokenAnnotationValueStringsExpected = { 
			{"ART","ART","Nom","Pl","*"},
			{"N","NN","Nom","Pl","*"},
			{"PREP","APPR"},
			{"N","NE","Dat","Sg","Neut"},
			{"V","VMFIN","3","Pl","Pres","Ind"},
			{"ART","ART","Acc","Sg","Neut"},
			{"N","NN","Acc","Sg","Neut"},
			{"$.","$."},
	}; 
	
	private String pointingRelationAnnotationNameExpected = "deprel";
	
	private String[] pointingRelationAnnotationValueStringsExpected = {
			"DET",		"P_DET",
			"SUBJ",		"P_SUBJ",
			"PP",		"P_PP",
			"PN",		"P_PN",
			//here is the root node´s position
			"DET",		"P_DET",
			"OBJA",		"P_OBJA",
			"-PUNCT-",	"P_-PUNCT-"
	}; 
	
	private String pointingRelationAnnotationSTypeNoneProjectiveExpected = "dep";
	private String pointingRelationAnnotationSTypeProjectiveModeTypeExpected = "prodep";
	private String pointingRelationAnnotationSTypeProjectiveModeNamespaceExpected = "dep";

	private String pointingRelationAnnotationNamespaceNoneProjectiveExpected = null;
	private String pointingRelationAnnotationNamespaceProjectiveModeTypeExpected = null;
	private String pointingRelationAnnotationNamespaceProjectiveModeNamespaceExpected = "projective";
	
	private int[] sourceNodeIDExpected = {
			2,2,
			5,5,
			5,5,
			3,3,
			//here is the root node´s position
			7,7,
			5,5,
			7,7
	};

	private String spanNameExpected = "cat";
	
	private	String spanValueStringExpected = "S";

// end of input file specific property values

	private SToken getPointingRelationTarget(SToken token)
	{
		for (SPointingRelation rel : token.getSDocumentGraph().getSPointingRelations())
			if (rel.getSSource().equals(token))
				return (SToken)rel.getSTarget();
		return null;
	}
	
	private String getTokenForm(SToken token)
	{
		if (token!=null)
		{
			for (STextualRelation rel : token.getSDocumentGraph().getSTextualRelations())
				if (token.equals(rel.getSToken()))
					 return rel.getSTextualDS().getSText().substring(rel.getSStart(),rel.getSEnd());
		}
		return null;
	}
	
	private void printPointingRelations(SDocumentGraph docGraph)
	{
		for (SPointingRelation rel : docGraph.getSPointingRelations())
		{
			SToken src = (SToken) rel.getSSource();
			SToken trg = (SToken) rel.getSTarget();
			System.out.println(
					String.format(
							"%s --(%s:%s)--> %s",
							getTokenForm(src),
							rel.getSAnnotations().get(0).getSName(),
							rel.getSAnnotations().get(0).getSValue(),
							getTokenForm(trg))
			);
		}
	}
	
	
	private void assertToken(SToken token, String form, String lemma, String cpos, String pos, SToken head)
	{
		SDocumentGraph 	 docGraph = token.getSDocumentGraph();
		STextualDS 		 textDS   = null;
		STextualRelation textRel  = null;

		for (STextualRelation rel : docGraph.getSTextualRelations())
		{
			if (token.equals(rel.getSToken()))
			{
				textRel = rel;
				textDS  = textRel.getSTextualDS();
			}
		}
		
		if ((textDS!=null)&&(textRel!=null))
		{
			assertEquals(form, textDS.getSText().subSequence(textRel.getSStart(),textRel.getSEnd()));
			
			SPOSAnnotation     posAnno   = null;
			SLemmaAnnotation   lemmaAnno = null;
			EList<SAnnotation> annos     = new BasicEList<SAnnotation>();
			
			for (SAnnotation anno : token.getSAnnotations())
			{
				if (anno instanceof SPOSAnnotation)
					posAnno = (SPOSAnnotation)anno;
				else if (anno instanceof SLemmaAnnotation)
					lemmaAnno = (SLemmaAnnotation)anno;
				else
					annos.add(anno);
			}
			
			if ((pos!=null)&&(posAnno!=null))
				assertEquals(pos, posAnno.getSValueSTEXT());
			else if ((pos==null)&&(posAnno!=null))
				fail("unexpected pos found");
			else if ((pos!=null)&&(posAnno==null))
				fail("pos expected, but not found");
			
			
			if ((lemma!=null)&&(lemmaAnno!=null))
				assertEquals(lemma, lemmaAnno.getSValueSTEXT());
			else if ((lemma==null)&&(lemmaAnno!=null))
				fail("unexpected lemma found");
			else if ((lemma!=null)&&(lemmaAnno==null))
				fail("lemma expected, but not found");
		}
		
		
		else
			fail("textualDS or textualRelation for token missing");
	}
	
	@Test
	public final void testAll() 
	{
		getFixture().setResourceURI(testFileURI);
		SCorpusGraph graph= SaltFactory.eINSTANCE.createSCorpusGraph();
		SDocument sDoc= graph.createSDocument(URI.createURI("c1/d1"));
		getFixture().setSDocument(sDoc);
		getFixture().mapSDocument();

		SToken[] tokens = new SToken[8];
		for (int i=0;i<8;i++)
			tokens[i] = getFixture().getSDocument().getSDocumentGraph().getSTokens().get(i);
		
		assertToken( tokens[0] , "Die"          , null , "ART"  , "ART"   , tokens[2-1] );
		assertToken( tokens[1] , "Jugendlichen" , null , "N"    , "NN"	  , tokens[5-1] );		
		assertToken( tokens[2] , "in"           , null , "PREP" , "APPR"  , tokens[5-1] );
		assertToken( tokens[3] , "Zossen"       , null , "N"    , "NE"    , tokens[3-1] );
		assertToken( tokens[4] , "wollen"       , null , "V"    , "VMFIN" , null        );
		assertToken( tokens[5] , "ein"          , null , "ART"  , "ART"	  , tokens[7-1] );	
		assertToken( tokens[6] , "Musikcafe"    , null , "N"    , "NN"    , tokens[5-1] );
		assertToken( tokens[7] , "."            , null , "$."   , "$."    , tokens[7-1] );
		
		//printPointingRelations(this.getFixture().getSDocumentGraph());
		
	}
	
//	@Test
//	public final void testTokens() {
//		getFixture().setResourceURI(testFileURI);
//		getFixture().mapSDocument();
//		
//		//check whetcher number of tokens is correct
//		assertEquals(8, getFixture().getSDocument().getSDocumentGraph().getSTokens().size());
//		
//		//iterate over tokens
//		int tokenIndex=0;
//		for (SToken token : getFixture().getSDocument().getSDocumentGraph().getSTokens()) {
//			// check sDocumentGraph
//			assertEquals(getFixture().getSDocument().getSDocumentGraph(), token.getSDocumentGraph());
//
//			//check annotations
//			int annotationIndex=0;
//			for (SAnnotation annotation : token.getSAnnotations()) {
//				//check annotations
//				assertEquals(tokenAnnotationNamesExpected[tokenIndex][annotationIndex], annotation.getName());
//				assertEquals(tokenAnnotationValueStringsExpected[tokenIndex][annotationIndex], annotation.getValueString());
//				annotationIndex++;
//			}
//
//			//check associated textual relation (linear order -> same index as token)
//			assertEquals(token, getFixture().getSDocument().getSDocumentGraph().getSTextualRelations().get(tokenIndex).getSource());
//
//			//check associated spanning relation (linear order -> same index as token)
//			assertEquals(token, getFixture().getSDocument().getSDocumentGraph().getSSpanningRelations().get(tokenIndex).getTarget());
//
//			//check associated pointing relations; tests depend on properties...
////			boolean projectivity = getFixture().getProperties().getProperty(CoNLLImporterProperties.PROP_CONSIDER_PROJECTIVITY, "NO").equals("YES");
//			boolean projectivity = getFixture().getProperties().getProperty(CoNLLImporterProperties.PROP_CONSIDER_PROJECTIVITY).getType().equals("YES");
//			int proFactor = 2; //proFactor is the factor that tokenIndex has to be multiplied with if projectivity is considered (double number of pointingRels)
//			if (!projectivity) {
//				proFactor = 1;
//			}
//			int pointingRelationIndex = proFactor * tokenIndex;
//			if (tokenIndex!=rootTokenIndex) {
//				if (tokenIndex>rootTokenIndex) {
//					pointingRelationIndex = proFactor * (tokenIndex-1);
//				}
//				assertEquals(token, getFixture().getSDocument().getSDocumentGraph().getSPointingRelations().get(pointingRelationIndex).getTarget());
//				if (projectivity) {
//					assertEquals(token, getFixture().getSDocument().getSDocumentGraph().getSPointingRelations().get(pointingRelationIndex+1).getTarget());
//				}
//			}
//			tokenIndex++;
//		}
//	}

	
	@Test
	public final void testTextualRelations() {
//		getFixture().map(testFileURI,SaltCommonFactory.eINSTANCE.createSDocument());
//
//		STextualDS textualDS = getFixture().getSDocumentGraph().getSTextualDSs().get(0);
//		
//		int trIndex = 0;
//		for (STextualRelation tr : getFixture().getSDocumentGraph().getSTextualRelations()) {
//			//check sDocumentGraph
//			assertEquals(getFixture().getSDocumentGraph(), tr.getSDocumentGraph());
//			//check associated source token (linear order -> same index as textualRelation)
//			assertEquals(getFixture().getSDocumentGraph().getSTokens().get(trIndex), tr.getSource()); 
//			//check associated target node
//			assertEquals(textualDS, tr.getTarget()); 
//			//check start and end via string comparison in textualDS
//			assertEquals(tokenFormsExpected[trIndex], textualDS.getSText().substring(tr.getSStart(), tr.getSEnd()));
//			trIndex++;
//		};
	}
		
	@Test
	public final void testSpans() {
//		getFixture().map(testFileURI,SaltCommonFactory.eINSTANCE.createSDocument());
//		
//		int spanIndex = 0;
//		for (SSpan span : getFixture().getSDocumentGraph().getSSpans()) {
//			//check sDocumentGraph
//			assertEquals(getFixture().getSDocumentGraph(), span.getSDocumentGraph());
//			//check annotation
//			assertEquals(spanNameExpected, span.getSAnnotations().get(0).getName());
//			assertEquals(spanValueStringExpected, span.getSAnnotations().get(0).getValueString());
//			//check associated spanning relation (linear order -> same index as span)
//			assertEquals(getFixture().getSDocumentGraph().getSSpanningRelations().get(spanIndex).getSSource(), span);
//			spanIndex++;
//		};
	}
			
	@Test
	public final void testSpanningRelations() {
//		getFixture().map(testFileURI,SaltCommonFactory.eINSTANCE.createSDocument());		
//		
//		int spanRelIndex = 0;
//		for (SSpanningRelation spanRel : getFixture().getSDocumentGraph().getSSpanningRelations()) {
//			//check sDocumentGraph
//			assertEquals(getFixture().getSDocumentGraph(), spanRel.getSDocumentGraph());
//			//check associated span (linear order -> same index as spanning relation)
//			assertEquals(getFixture().getSDocumentGraph().getSSpans().get(spanRelIndex), spanRel.getSource());
//			//check associated token (linear order -> same index as spanning relation)
//			assertEquals(getFixture().getSDocumentGraph().getSTokens().get(spanRelIndex), spanRel.getTarget());
//			spanRelIndex++;
//		}
	}
	
	@Test
	public final void testPointingRelations() {
//		getFixture().map(testFileURI,SaltCommonFactory.eINSTANCE.createSDocument());
//		
//		boolean projectivity = getFixture().getProperties().getProperty(Conll2SaltMapper.PROPERTYKEY_PROJECTIVITY, Conll2SaltMapper.FALSE).equals(Conll2SaltMapper.TRUE);
//		int proFactor = 1; 
//		//proFactor is for calculating list indexes
//		if (!projectivity) {
//			proFactor = 2;
//		}
//		
//		boolean projectiveModeIsType = getFixture().getProperties().getProperty(Conll2SaltMapper.PROPERTYKEY_PROJECTIVEMODE, Conll2SaltMapper.TYPE).equals(Conll2SaltMapper.TYPE);
//		
//		int pointRelIndex = 0;
//		for (SPointingRelation pointRel : getFixture().getSDocumentGraph().getSPointingRelations()) {
//			//check sDocumentGraph
//			assertEquals(getFixture().getSDocumentGraph(), pointRel.getSDocumentGraph());
//			//check annotation and stype
//			assertEquals(pointingRelationAnnotationNameExpected, pointRel.getSAnnotations().get(0).getName());
//			assertEquals(pointingRelationAnnotationValueStringsExpected[pointRelIndex*proFactor], pointRel.getSAnnotations().get(0).getValueString());
//			if (projectivity) {
//				// is index even or odd? pointing relation is "normal" if true, projective if false
//				if ((pointRelIndex*proFactor)%2==0) { //even
//					assertEquals(pointingRelationAnnotationSTypeNoneProjectiveExpected,pointRel.getSTypes().get(0));
//					assertEquals(pointingRelationAnnotationNamespaceNoneProjectiveExpected,pointRel.getSAnnotations().get(0).getNamespace());
//				}
//				else { //odd
//					if (projectiveModeIsType) {
//						assertEquals(pointingRelationAnnotationSTypeProjectiveModeTypeExpected,pointRel.getSTypes().get(0));
//						assertEquals(pointingRelationAnnotationNamespaceProjectiveModeTypeExpected,pointRel.getSAnnotations().get(0).getNamespace());
//					}
//					else {
//						assertEquals(pointingRelationAnnotationSTypeProjectiveModeNamespaceExpected,pointRel.getSTypes().get(0));
//						assertEquals(pointingRelationAnnotationNamespaceProjectiveModeNamespaceExpected,pointRel.getSAnnotations().get(0).getNamespace());						
//					}
//				}
//			}
//			else {
//				assertEquals(pointingRelationAnnotationSTypeNoneProjectiveExpected,pointRel.getSTypes().get(0));
//				assertEquals(pointingRelationAnnotationNamespaceNoneProjectiveExpected,pointRel.getSAnnotations().get(0).getNamespace());				
//			}
//				
//			//check source node
//			assertEquals(getFixture().getSDocumentGraph().getSTokens().get(sourceNodeIDExpected[pointRelIndex*proFactor]-1),pointRel.getSource());
//			//check target node
//			int targetTokenIndex = pointRelIndex;
//			if (projectivity) {
//				targetTokenIndex /= 2;
//			}
//			if (targetTokenIndex>=rootTokenIndex) {
//				targetTokenIndex++;
//			}
//			assertEquals(getFixture().getSDocumentGraph().getSTokens().get(targetTokenIndex),pointRel.getTarget());
//
//			pointRelIndex++;
//		}

	}	
}
