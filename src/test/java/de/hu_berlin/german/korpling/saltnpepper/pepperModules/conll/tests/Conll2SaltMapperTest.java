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

import java.lang.reflect.Array;

import junit.framework.TestCase;

import org.eclipse.emf.common.util.URI;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.conll.Conll2SaltMapper;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltCommonFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SPointingRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;

public class Conll2SaltMapperTest extends TestCase {

	private Conll2SaltMapper fixture = null;
	private Conll2SaltMapper getFixture() {return fixture;}
	private void setFixture(Conll2SaltMapper fixture) {this.fixture = fixture;}

	public void setUp() {
		this.setFixture(new Conll2SaltMapper());
		this.getFixture().setProperties(URI.createFileURI("./src/test/resources/ConllModules_feats.properties"));
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

	
	
	public final void testTokens() {
//		getFixture().map(testFileURI, SaltCommonFactory.eINSTANCE.createSDocument());
//		
//		//check whetcher number of tokens is correct
//		assertEquals(Array.getLength(tokenAnnotationNamesExpected), getFixture().getSDocumentGraph().getSTokens().size());
//		
//		//iterate over tokens
//		int tokenIndex=0;
//		for (SToken token : getFixture().getSDocumentGraph().getSTokens()) {
//			// check sDocumentGraph
//			assertEquals(getFixture().getSDocumentGraph(), token.getSDocumentGraph());
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
//			assertEquals(token, getFixture().getSDocumentGraph().getSTextualRelations().get(tokenIndex).getSource());
//
//			//check associated spanning relation (linear order -> same index as token)
//			assertEquals(token, getFixture().getSDocumentGraph().getSSpanningRelations().get(tokenIndex).getTarget());
//
//			//check associated pointing relations; tests depend on properties...
//			boolean projectivity = getFixture().getProperties().getProperty(Conll2SaltMapper.PROPERTYKEY_PROJECTIVITY, "NO").equals("YES");
//			int proFactor = 2; //proFactor is the factor that tokenIndex has to be multiplied with if projectivity is considered (double number of pointingRels)
//			if (!projectivity) {
//				proFactor = 1;
//			}
//			int pointingRelationIndex = proFactor * tokenIndex;
//			if (tokenIndex!=rootTokenIndex) {
//				if (tokenIndex>rootTokenIndex) {
//					pointingRelationIndex = proFactor * (tokenIndex-1);
//				}
//				assertEquals(token, getFixture().getSDocumentGraph().getSPointingRelations().get(pointingRelationIndex).getTarget());
//				if (projectivity) {
//					assertEquals(token, getFixture().getSDocumentGraph().getSPointingRelations().get(pointingRelationIndex+1).getTarget());
//				}
//			}
//			tokenIndex++;
//		}
	}

	
	
	public final void testTextualRelations() {
		getFixture().map(testFileURI,SaltCommonFactory.eINSTANCE.createSDocument());

		STextualDS textualDS = getFixture().getSDocumentGraph().getSTextualDSs().get(0);
		
		int trIndex = 0;
		for (STextualRelation tr : getFixture().getSDocumentGraph().getSTextualRelations()) {
			//check sDocumentGraph
			assertEquals(getFixture().getSDocumentGraph(), tr.getSDocumentGraph());
			//check associated source token (linear order -> same index as textualRelation)
			assertEquals(getFixture().getSDocumentGraph().getSTokens().get(trIndex), tr.getSource()); 
			//check associated target node
			assertEquals(textualDS, tr.getTarget()); 
			//check start and end via string comparison in textualDS
			assertEquals(tokenFormsExpected[trIndex], textualDS.getSText().substring(tr.getSStart(), tr.getSEnd()));
			trIndex++;
		};
	}
		
	
	public final void testSpans() {
		getFixture().map(testFileURI,SaltCommonFactory.eINSTANCE.createSDocument());
		
		int spanIndex = 0;
		for (SSpan span : getFixture().getSDocumentGraph().getSSpans()) {
			//check sDocumentGraph
			assertEquals(getFixture().getSDocumentGraph(), span.getSDocumentGraph());
			//check annotation
			assertEquals(spanNameExpected, span.getSAnnotations().get(0).getName());
			assertEquals(spanValueStringExpected, span.getSAnnotations().get(0).getValueString());
			//check associated spanning relation (linear order -> same index as span)
			assertEquals(getFixture().getSDocumentGraph().getSSpanningRelations().get(spanIndex).getSSource(), span);
			spanIndex++;
		};
	}
			
	
	public final void testSpanningRelations() {
		getFixture().map(testFileURI,SaltCommonFactory.eINSTANCE.createSDocument());		
		
		int spanRelIndex = 0;
		for (SSpanningRelation spanRel : getFixture().getSDocumentGraph().getSSpanningRelations()) {
			//check sDocumentGraph
			assertEquals(getFixture().getSDocumentGraph(), spanRel.getSDocumentGraph());
			//check associated span (linear order -> same index as spanning relation)
			assertEquals(getFixture().getSDocumentGraph().getSSpans().get(spanRelIndex), spanRel.getSource());
			//check associated token (linear order -> same index as spanning relation)
			assertEquals(getFixture().getSDocumentGraph().getSTokens().get(spanRelIndex), spanRel.getTarget());
			spanRelIndex++;
		}
	}
	
	
	public final void testPointingRelations() {
		getFixture().map(testFileURI,SaltCommonFactory.eINSTANCE.createSDocument());
		
		boolean projectivity = getFixture().getProperties().getProperty(Conll2SaltMapper.PROPERTYKEY_PROJECTIVITY, Conll2SaltMapper.FALSE).equals(Conll2SaltMapper.TRUE);
		int proFactor = 1; 
		//proFactor is for calculating list indexes
		if (!projectivity) {
			proFactor = 2;
		}
		
		boolean projectiveModeIsType = getFixture().getProperties().getProperty(Conll2SaltMapper.PROPERTYKEY_PROJECTIVEMODE, Conll2SaltMapper.TYPE).equals(Conll2SaltMapper.TYPE);
		
		int pointRelIndex = 0;
		for (SPointingRelation pointRel : getFixture().getSDocumentGraph().getSPointingRelations()) {
			//check sDocumentGraph
			assertEquals(getFixture().getSDocumentGraph(), pointRel.getSDocumentGraph());
			//check annotation and stype
			assertEquals(pointingRelationAnnotationNameExpected, pointRel.getSAnnotations().get(0).getName());
			assertEquals(pointingRelationAnnotationValueStringsExpected[pointRelIndex*proFactor], pointRel.getSAnnotations().get(0).getValueString());
			if (projectivity) {
				// is index even or odd? pointing relation is "normal" if true, projective if false
				if ((pointRelIndex*proFactor)%2==0) { //even
					assertEquals(pointingRelationAnnotationSTypeNoneProjectiveExpected,pointRel.getSTypes().get(0));
					assertEquals(pointingRelationAnnotationNamespaceNoneProjectiveExpected,pointRel.getSAnnotations().get(0).getNamespace());
				}
				else { //odd
					if (projectiveModeIsType) {
						assertEquals(pointingRelationAnnotationSTypeProjectiveModeTypeExpected,pointRel.getSTypes().get(0));
						assertEquals(pointingRelationAnnotationNamespaceProjectiveModeTypeExpected,pointRel.getSAnnotations().get(0).getNamespace());
					}
					else {
						assertEquals(pointingRelationAnnotationSTypeProjectiveModeNamespaceExpected,pointRel.getSTypes().get(0));
						assertEquals(pointingRelationAnnotationNamespaceProjectiveModeNamespaceExpected,pointRel.getSAnnotations().get(0).getNamespace());						
					}
				}
			}
			else {
				assertEquals(pointingRelationAnnotationSTypeNoneProjectiveExpected,pointRel.getSTypes().get(0));
				assertEquals(pointingRelationAnnotationNamespaceNoneProjectiveExpected,pointRel.getSAnnotations().get(0).getNamespace());				
			}
				
			//check source node
			assertEquals(getFixture().getSDocumentGraph().getSTokens().get(sourceNodeIDExpected[pointRelIndex*proFactor]-1),pointRel.getSource());
			//check target node
			int targetTokenIndex = pointRelIndex;
			if (projectivity) {
				targetTokenIndex /= 2;
			}
			if (targetTokenIndex>=rootTokenIndex) {
				targetTokenIndex++;
			}
			assertEquals(getFixture().getSDocumentGraph().getSTokens().get(targetTokenIndex),pointRel.getTarget());

			pointRelIndex++;
		}
		
	
		
		
		
		
		
	}
	
	
	
	
	
}
