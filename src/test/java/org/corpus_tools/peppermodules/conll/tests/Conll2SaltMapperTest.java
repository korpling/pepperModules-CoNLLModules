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
package org.corpus_tools.peppermodules.conll.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.corpus_tools.peppermodules.conll.Conll2SaltMapper;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.semantics.SLemmaAnnotation;
import org.corpus_tools.salt.semantics.SPOSAnnotation;
import org.eclipse.emf.common.util.URI;
import org.junit.Before;
import org.junit.Test;

public class Conll2SaltMapperTest{

	private Conll2SaltMapper fixture = null;
	private Conll2SaltMapper getFixture() {return fixture;}
	private void setFixture(Conll2SaltMapper fixture) {this.fixture = fixture;}

	@Before
	public void setUp() {
		this.setFixture(new Conll2SaltMapper());
		getFixture().setProperties(URI.createFileURI("./src/test/resources/ConllModules_feats.properties"));
		SDocument sDoc= SaltFactory.createSDocument();
		SaltFactory.createIdentifier(sDoc, "doc1");
		getFixture().setDocument(sDoc);
		getFixture().getDocument().setDocumentGraph(SaltFactory.createSDocumentGraph());
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
		for (SPointingRelation rel : token.getGraph().getPointingRelations())
			if (rel.getSource().equals(token))
				return (SToken)rel.getTarget();
		return null;
	}
	
	private String getTokenForm(SToken token)
	{
		if (token!=null)
		{
			for (STextualRelation rel : token.getGraph().getTextualRelations())
				if (token.equals(rel.getSource()))
					 return rel.getTarget().getText().substring(rel.getStart(),rel.getEnd());
		}
		return null;
	}
	
	private void printPointingRelations(SDocumentGraph docGraph)
	{
		for (SPointingRelation rel : docGraph.getPointingRelations())
		{
			SToken src = (SToken) rel.getSource();
			SToken trg = (SToken) rel.getTarget();
			System.out.println(
					String.format(
							"%s --(%s:%s)--> %s",
							getTokenForm(src),
							rel.getAnnotations().iterator().next().getName(),
							rel.getAnnotations().iterator().next().getValue(),
							getTokenForm(trg))
			);
		}
	}
	
	
	private void assertToken(SToken token, String form, String lemma, String cpos, String pos, SToken head)
	{
		SDocumentGraph 	 docGraph = token.getGraph();
		STextualDS 		 textDS   = null;
		STextualRelation textRel  = null;

		for (STextualRelation rel : docGraph.getTextualRelations())
		{
			if (token.equals(rel.getSource()))
			{
				textRel = rel;
				textDS  = textRel.getTarget();
			}
		}
		
		if ((textDS!=null)&&(textRel!=null))
		{
			assertEquals(form, textDS.getText().subSequence(textRel.getStart(),textRel.getEnd()));
			
			SPOSAnnotation     posAnno   = null;
			SLemmaAnnotation   lemmaAnno = null;
			List<SAnnotation> annos     = new ArrayList<SAnnotation>();
			
			for (SAnnotation anno : token.getAnnotations())
			{
				if (anno instanceof SPOSAnnotation)
					posAnno = (SPOSAnnotation)anno;
				else if (anno instanceof SLemmaAnnotation)
					lemmaAnno = (SLemmaAnnotation)anno;
				else
					annos.add(anno);
			}
			
			if ((pos!=null)&&(posAnno!=null))
				assertEquals(pos, posAnno.getValue_STEXT());
			else if ((pos==null)&&(posAnno!=null))
				fail("unexpected pos found");
			else if ((pos!=null)&&(posAnno==null))
				fail("pos expected, but not found");
			
			
			if ((lemma!=null)&&(lemmaAnno!=null))
				assertEquals(lemma, lemmaAnno.getValue_STEXT());
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
		SCorpusGraph graph= SaltFactory.createSCorpusGraph();
		SDocument sDoc= graph.createDocument(URI.createURI("c1/d1"));
		getFixture().setDocument(sDoc);
		getFixture().mapSDocument();

		SToken[] tokens = new SToken[8];
		for (int i=0;i<8;i++)
			tokens[i] = getFixture().getDocument().getDocumentGraph().getTokens().get(i);
		
		assertToken( tokens[0] , "Die"          , null , "ART"  , "ART"   , tokens[2-1] );
		assertToken( tokens[1] , "Jugendlichen" , null , "N"    , "NN"	  , tokens[5-1] );		
		assertToken( tokens[2] , "in"           , null , "PREP" , "APPR"  , tokens[5-1] );
		assertToken( tokens[3] , "Zossen"       , null , "N"    , "NE"    , tokens[3-1] );
		assertToken( tokens[4] , "wollen"       , null , "V"    , "VMFIN" , null        );
		assertToken( tokens[5] , "ein"          , null , "ART"  , "ART"	  , tokens[7-1] );	
		assertToken( tokens[6] , "Musikcafe"    , null , "N"    , "NN"    , tokens[5-1] );
		assertToken( tokens[7] , "."            , null , "$."   , "$."    , tokens[7-1] );
		
		//printPointingRelations(getFixture().getDocumentGraph());
		
	}
	
//	@Test
//	public final void testTokens() {
//		getFixture().setResourceURI(testFileURI);
//		getFixture().mapSDocument();
//		
//		//check whetcher number of tokens is correct
//		assertEquals(8, getFixture().getDocument().getDocumentGraph().getTokens().size());
//		
//		//iterate over tokens
//		int tokenIndex=0;
//		for (SToken token : getFixture().getDocument().getDocumentGraph().getTokens()) {
//			// check sDocumentGraph
//			assertEquals(getFixture().getDocument().getDocumentGraph(), token.getDocumentGraph());
//
//			//check annotations
//			int annotationIndex=0;
//			for (SAnnotation annotation : token.getAnnotations()) {
//				//check annotations
//				assertEquals(tokenAnnotationNamesExpected[tokenIndex][annotationIndex], annotation.getName());
//				assertEquals(tokenAnnotationValueStringsExpected[tokenIndex][annotationIndex], annotation.getValueString());
//				annotationIndex++;
//			}
//
//			//check associated textual relation (linear order -> same index as token)
//			assertEquals(token, getFixture().getDocument().getDocumentGraph().getTextualRelations().get(tokenIndex).getSource());
//
//			//check associated spanning relation (linear order -> same index as token)
//			assertEquals(token, getFixture().getDocument().getDocumentGraph().getSpanningRelations().get(tokenIndex).getTarget());
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
//				assertEquals(token, getFixture().getDocument().getDocumentGraph().getPointingRelations().get(pointingRelationIndex).getTarget());
//				if (projectivity) {
//					assertEquals(token, getFixture().getDocument().getDocumentGraph().getPointingRelations().get(pointingRelationIndex+1).getTarget());
//				}
//			}
//			tokenIndex++;
//		}
//	}

	
	@Test
	public final void testTextualRelations() {
//		getFixture().map(testFileURI,SaltCommonFactory.eINSTANCE.createSDocument());
//
//		STextualDS textualDS = getFixture().getDocumentGraph().getTextualDSs().get(0);
//		
//		int trIndex = 0;
//		for (STextualRelation tr : getFixture().getDocumentGraph().getTextualRelations()) {
//			//check sDocumentGraph
//			assertEquals(getFixture().getDocumentGraph(), tr.getDocumentGraph());
//			//check associated source token (linear order -> same index as textualRelation)
//			assertEquals(getFixture().getDocumentGraph().getTokens().get(trIndex), tr.getSource()); 
//			//check associated target node
//			assertEquals(textualDS, tr.getTarget()); 
//			//check start and end via string comparison in textualDS
//			assertEquals(tokenFormsExpected[trIndex], textualDS.getText().substring(tr.getStart(), tr.getEnd()));
//			trIndex++;
//		};
	}
		
	@Test
	public final void testSpans() {
//		getFixture().map(testFileURI,SaltCommonFactory.eINSTANCE.createSDocument());
//		
//		int spanIndex = 0;
//		for (SSpan span : getFixture().getDocumentGraph().getSpans()) {
//			//check sDocumentGraph
//			assertEquals(getFixture().getDocumentGraph(), span.getDocumentGraph());
//			//check annotation
//			assertEquals(spanNameExpected, span.getAnnotations().get(0).getName());
//			assertEquals(spanValueStringExpected, span.getAnnotations().get(0).getValueString());
//			//check associated spanning relation (linear order -> same index as span)
//			assertEquals(getFixture().getDocumentGraph().getSpanningRelations().get(spanIndex).getSource(), span);
//			spanIndex++;
//		};
	}
			
	@Test
	public final void testSpanningRelations() {
//		getFixture().map(testFileURI,SaltCommonFactory.eINSTANCE.createSDocument());		
//		
//		int spanRelIndex = 0;
//		for (SSpanningRelation spanRel : getFixture().getDocumentGraph().getSpanningRelations()) {
//			//check sDocumentGraph
//			assertEquals(getFixture().getDocumentGraph(), spanRel.getDocumentGraph());
//			//check associated span (linear order -> same index as spanning relation)
//			assertEquals(getFixture().getDocumentGraph().getSpans().get(spanRelIndex), spanRel.getSource());
//			//check associated token (linear order -> same index as spanning relation)
//			assertEquals(getFixture().getDocumentGraph().getTokens().get(spanRelIndex), spanRel.getTarget());
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
//		for (SPointingRelation pointRel : getFixture().getDocumentGraph().getPointingRelations()) {
//			//check sDocumentGraph
//			assertEquals(getFixture().getDocumentGraph(), pointRel.getDocumentGraph());
//			//check annotation and stype
//			assertEquals(pointingRelationAnnotationNameExpected, pointRel.getAnnotations().get(0).getName());
//			assertEquals(pointingRelationAnnotationValueStringsExpected[pointRelIndex*proFactor], pointRel.getAnnotations().get(0).getValueString());
//			if (projectivity) {
//				// is index even or odd? pointing relation is "normal" if true, projective if false
//				if ((pointRelIndex*proFactor)%2==0) { //even
//					assertEquals(pointingRelationAnnotationSTypeNoneProjectiveExpected,pointRel.getSTypes().get(0));
//					assertEquals(pointingRelationAnnotationNamespaceNoneProjectiveExpected,pointRel.getAnnotations().get(0).getNamespace());
//				}
//				else { //odd
//					if (projectiveModeIsType) {
//						assertEquals(pointingRelationAnnotationSTypeProjectiveModeTypeExpected,pointRel.getSTypes().get(0));
//						assertEquals(pointingRelationAnnotationNamespaceProjectiveModeTypeExpected,pointRel.getAnnotations().get(0).getNamespace());
//					}
//					else {
//						assertEquals(pointingRelationAnnotationSTypeProjectiveModeNamespaceExpected,pointRel.getSTypes().get(0));
//						assertEquals(pointingRelationAnnotationNamespaceProjectiveModeNamespaceExpected,pointRel.getAnnotations().get(0).getNamespace());						
//					}
//				}
//			}
//			else {
//				assertEquals(pointingRelationAnnotationSTypeNoneProjectiveExpected,pointRel.getSTypes().get(0));
//				assertEquals(pointingRelationAnnotationNamespaceNoneProjectiveExpected,pointRel.getAnnotations().get(0).getNamespace());				
//			}
//				
//			//check source node
//			assertEquals(getFixture().getDocumentGraph().getTokens().get(sourceNodeIDExpected[pointRelIndex*proFactor]-1),pointRel.getSource());
//			//check target node
//			int targetTokenIndex = pointRelIndex;
//			if (projectivity) {
//				targetTokenIndex /= 2;
//			}
//			if (targetTokenIndex>=rootTokenIndex) {
//				targetTokenIndex++;
//			}
//			assertEquals(getFixture().getDocumentGraph().getTokens().get(targetTokenIndex),pointRel.getTarget());
//
//			pointRelIndex++;
//		}

	}	
}
