![SaltNPepper project](./gh-site/img/SaltNPepper_logo2010.png)
# pepperModules-CoNLLModules
This project provides an importer to support the CoNLL format for the linguistic converter framework Pepper (see https://u.hu-berlin.de/saltnpepper). A detailed description of the importer can be found in section [CoNLLImporter](#details).

Pepper is a pluggable framework to convert a variety of linguistic formats (like [TigerXML](http://www.ims.uni-stuttgart.de/forschung/ressourcen/werkzeuge/TIGERSearch/doc/html/TigerXML.html), the [EXMARaLDA format](http://www.exmaralda.org/), [PAULA](http://www.sfb632.uni-potsdam.de/paula.html) etc.) into each other. Furthermore Pepper uses Salt (see https://github.com/korpling/salt), the graph-based meta model for linguistic data, which acts as an intermediate model to reduce the number of mappings to be implemented. That means converting data from a format _A_ to format _B_ consists of two steps. First the data is mapped from format _A_ to Salt and second from Salt to format _B_. This detour reduces the number of Pepper modules from _n<sup>2</sup>-n_ (in the case of a direct mapping) to _2n_ to handle a number of n formats.

![n:n mappings via SaltNPepper](./gh-site/img/puzzle.png)

In Pepper there are three different types of modules:
* importers (to map a format _A_ to a Salt model)
* manipulators (to map a Salt model to a Salt model, e.g. to add additional annotations, to rename things to merge data etc.)
* exporters (to map a Salt model to a format _B_).

For a simple Pepper workflow you need at least one importer and one exporter.

## Requirements
Since the here provided module is a plugin for Pepper, you need an instance of the Pepper framework. If you do not already have a running Pepper instance, click on the link below and download the latest stable version (not a SNAPSHOT):

> Note:
> Pepper is a Java based program, therefore you need to have at least Java 7 (JRE or JDK) on your system. You can download Java from https://www.oracle.com/java/index.html or http://openjdk.java.net/ .


## Install module
If this Pepper module is not yet contained in your Pepper distribution, you can easily install it. Just open a command line and enter one of the following program calls:

**Windows**
```
pepperStart.bat 
```

**Linux/Unix**
```
bash pepperStart.sh 
```

Then type in command *is* and the path from where to install the module:
```
pepper> update de.hu_berlin.german.korpling.saltnpepper::pepperModules-pepperModules-CoNLLModules::https://korpling.german.hu-berlin.de/maven2/
```

## Usage
To use this module in your Pepper workflow, put the following lines into the workflow description file. Note the fixed order of xml elements in the workflow description file: &lt;importer/>, &lt;manipulator/>, &lt;exporter/>. The CoNLLImporter is an importer module, which can be addressed by one of the following alternatives.
A detailed description of the Pepper workflow can be found on the [Pepper project site](https://u.hu-berlin.de/saltnpepper). 

### a) Identify the module by name

```xml
<importer name="CoNLLImporter" path="PATH_TO_CORPUS"/>
```

### b) Identify the module by formats
```xml
<importer formatName="CoNLL," formatVersion="1.0" path="PATH_TO_CORPUS"/>
```

### c) Use properties
```xml
<importer name="CoNLLImporter" path="PATH_TO_CORPUS">
  <property key="PROPERTY_NAME">PROPERTY_VALUE</property>
</importer>
```

## Contribute
Since this Pepper module is under a free license, please feel free to fork it from github and improve the module. If you even think that others can benefit from your improvements, don't hesitate to make a pull request, so that your changes can be merged.
If you have found any bugs, or have some feature request, please open an issue on github. If you need any help, please write an e-mail to saltnpepper@lists.hu-berlin.de .

## Funders
This project has been funded by the [department of corpus linguistics and morphology](https://www.linguistik.hu-berlin.de/institut/professuren/korpuslinguistik/) of the Humboldt-Universität zu Berlin, the Institut national de recherche en informatique et en automatique ([INRIA](www.inria.fr/en/)) and the [Sonderforschungsbereich 632](https://www.sfb632.uni-potsdam.de/en/). 

## License
  Copyright 2009 Humboldt-Universität zu Berlin, INRIA.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.


# <a name="details">CoNLLImporter</a>
## supported CoNLL format
A CoNLL file as defined here contains one tab separated row per token. Each row contains exactly 10 columns. No blank characters are allowed in column entries. Sentences are separated by an empty row. 

|   |       |																					|	
|---|-------|-----------------------------------------------------------------------------------|
|1	|ID	    |The number of the token in the current sentence, starting with 1					|
|2	|FORM	|The form of the token																|
|3	|LEMMA	|The lemma of the token																|
|4  |CPOSTAG|Coarse-grained part-of-speech tag													|
|5	|POSTAG	|Fine-grained part-of-speech tag													|
|6	|FEATS	|Syntactic/morphological/miscellaneous features, separated by the pipe character	|
|7	|HEAD	|The ID of this token´s head token (or 0 for none)									|
|8	|DEPREL	|Dependency relation to HEAD														|
|9	|PHEAD	|The projective head of the token: an ID or 0 for none								|
|10 |PDEPREL|Dependency relation to PHEAD														|

All fields except 1, 2 and 7 may contain dummy value (an underscore _). 

```
1  Cathy    Cathy  N     N     eigen|ev|neut         2  su     _  _
2  zag      zie    V     V     trans|ovt|1of2of3|ev  0  ROOT   _  _
3  hen      hen    Pron  Pron  per|3|mv|datofacc     2  obj1   _  _
4  wild     wild   Adj   Adj   attr|stell|onverv     5  mod    _  _
5  zwaaien  zwaai  N     N     soort|mv|neut         2  vc     _  _
6  .        .      Punc  Punc  punct                 5  punct  _  _
```

## enhanced dependencies format
The importer also supports enhanced dependencies with secondary edges in column 9 and possible reconstructed/ellipsis tokens with decimal IDs, as in the following example (see the Universal Dependencies documentation for more details).

```
1	She	she	PRON	PRP	Case=Nom|Gender=Fem|Number=Sing|Person=3|PronType=Prs	3	nsubj:pass	3:nsubj:pass|5:nsubj:xsubj|11.1:nsubj:pass|13:nsubj:pass	_
2	is	be	AUX	VBZ	Mood=Ind|Number=Sing|Person=3|Tense=Pres|VerbForm=Fin	3	aux:pass	3:aux:pass	_
3	called	call	VERB	VBN	Tense=Past|VerbForm=Part|Voice=Pass	0	root	0:root	_
4	"	"	PUNCT	``	_	5	punct	5:punct	SpaceAfter=No
5	martyr	martyr	NOUN	NN	Number=Sing	3	xcomp	3:xcomp	SpaceAfter=No
6	"	"	PUNCT	''	_	5	punct	5:punct	_
7	by	by	ADP	IN	_	10	case	10:case	_
8	several	several	ADJ	JJ	Degree=Pos	10	amod	10:amod	_
9	Arab	arab	ADJ	JJ	Degree=Pos	10	amod	10:amod	_
10	leaders	leader	NOUN	NNS	Number=Plur	3	obl	3:obl:by	_
11	and	and	CCONJ	CC	_	13	cc	13:cc	_
11.1	called	call	VERB	VBN	Tense=Past|VerbForm=Part|Voice=Pass	_	_	3:conj:and	CopyOf=3
12	"	"	PUNCT	``	_	13	punct	13:punct	SpaceAfter=No
13	activist	activist	NOUN	NN	Number=Sing	3	conj	3:conj:and|11.1:xcomp	SpaceAfter=No
14	"	"	PUNCT	''	_	13	punct	13:punct	_
15	by	by	ADP	IN	_	18	case	18:case	_
16	the	the	DET	DT	Definite=Def|PronType=Art	18	det	18:det	_
17	European	european	ADJ	JJ	Degree=Pos	18	amod	18:amod	_
18	press	press	NOUN	NN	Number=Sing	13	orphan	11.1:obl:by	SpaceAfter=No
19	.	.	PUNCT	.	_	3	punct	3:punct	_
```

## Properties

The following table contains an overview of all usable properties to customize the behaviour of this pepper module. The following section contains a close description to each single property and describes the resulting differences in the mapping to the salt model. 
pepper modules contained in this project

|Name of property			|possible values		|default value|	
|---------------------------|-----------------------|-------------|
|conll.SPOS					|POSTAG, CPOSTAG, NONE	|POSTAG|
|conll.SLEMMA				|LEMMA, NONE			|LEMMA|
|conll.EDGE.TYPE		| String	| dep |
|conll.considerProjectivity	|TRUE, FALSE			|FALSE|
|conll.projectiveMode		|TYPE, NAMESPACE		|TYPE|
|conll.field6.POSTAG.TAG	|TRUE, FALSE			|TRUE|
|conll.field6.CPOSTAG.TAG	|any					| |
|conll.SECOND.POS.NAME	|String					| |
|conll.MISC.NAMESPACE	|String					| |
|conll.field6.default		|any					| |
|conll.splitFeatures		|a single category name or a pipe separated sequence of category names	| morph|
|conll.KeyValFeatures		| TRUE, FALSE	| FALSE|
|conll.FEATURES.NAMESPACE		| String	| |
|conll.SENTENCE		        |TRUE, FALSE	| TRUE|
|conll.split.edeps		        |TRUE, FALSE	| FALSE|
|conll.no.duplicate.edeps		        |TRUE, FALSE	| FALSE|
|conll.ellipsis.tok.annotation		        |String	| |
|conll.meta.prefix            |String| meta:: |
|conll.markable.namespace            |String|  |
|conll.markable.annotation           |String|  |
|conll.markable.labels           |String| entity-GRP-identity |
|conll.sentence.annotations           |String|  |

### conll.SPOS
Usage: conll.SPOS=[FIELD](,[ALTERNATIVEFIELD])
Possible values are POSTAG, CPOSTAG, NONE
Default value for this attribute is POSTAG
States which CoNLL field´s data (see ) to use for the SPOSAnnotation of salt tokens, or, if [FIELD] is NONE, not to create SPOSAnnotations at all. If the field designated by [FIELD] contains no data, [ALTERNATIVEFIELD] (if given), is used. If that field contains no data, no SPOSAnnotation is created for the particular salt token.
Example:
```
conll.SPOS=POSTAG,CPOSTAG
```
With this example setting, all input tokens with a value for the field POSTAG will be assigned a SPOSAnnotation with that value in their salt representation. If there is no value for the field POSTAG, the value of the field CPOSTAG is used. If that is empty, too, no SPOSAnnotation is created.

### conll.SLEMMA
Usage: conll.SLEMMA=[FIELD]
Possible values are LEMMA, NONE
Default value for this attribute is LEMMA
States which CoNLL field´s data to use for the SLemmaAnnotation of salt tokens, or, if [FIELD] is NONE, not to create SLemmaAnnotations at all. If the field designated by [FIELD] contains no data, no SLemmaAnnotation is created for the particular salt token. The default value for this attribute is LEMMA.
Example:
```
conll.SLEMMA=LEMMA
```
With this example setting, all input tokens with a value for the field LEMMA will be assigned a SLemmaAnnotation with that value in their salt representation.

### conll.considerProjectivity
Usage: conll.considerProjectivity=[VALUE]
Possible values are TRUE and FALSE
Default value for this attribute is FALSE
States whether to create a salt pointing relation from projective head of tokens to the dependent.

### conll.projectiveMode
This attribute only applies if  is set TRUE!
Usage: conll.projectiveMode=[VALUE]
Possible values are TYPE and NAMESPACE
Default value for this attribute is TYPE
  configures how projectivity is modelled in the salt representation. Generally, there will be a salt pointing relation and an annotation with the name "deprel" on that relation. If the mode is set TYPE, the relation´s type will be "prodep". If the mode is set NAMESPACE, the relation´s type will be "dep" and the annotation´s namespace will be set to "projective". 

### conll.field6.POSTAG.TAG
Note: The name segment field6 refers to sixth of the CoNLL fields (FEATS, see )
Usage: conll.field6.POSTAG.[TAG]=[VALUE]
This is not only a single property, but a class of properties. Multiple entries of this type may be given in a properties file, but [TAG] must be unique.
A property of this type applies for any input data row that contains the given [TAG] as value for the POSTAG field. The corresponding salt token will get a SAnnotation with [VALUE] as name and the input data row´s FEATS field as value.
Example:
```
conll.field6.POSTAG.NE=case|number|gender 
conll.field6.POSTAG.VF=tense|person|number
```
input data row excerpt:
```
2  mag    mögen  V    VF  pres|3|sg    0  _  _  _
3  Peter  Peter  N    NE  acc|sg|masc  2  _  _  _
4  sehr   sehr   Adv  Adv _            2  _  _  _
```
 Since the POSTAG of the first input data row matches the second property´s TAG (both are VF), the salt token that represents this data row will get an Annotation that has "tense|person|number" as name and "pres|3|sg" as value. The second data row´s TAG matches the first property´s TAG, so the corresponding salt token will get an Annotation with "case|number|gender" and "acc|sg|masc". There is no entry for the TAG "Adv" of the third input row. In that case, a default value is used (see ). 
If the  property is set FALSE, a single SAnnotation is created. If it is set TRUE, both VALUE and the FEATS value are split into their pipe separated elements. Each element will be represented as one SAnnotation. For the first data row above, three SAnnotations are created: 'tense':'gen', 'person':'3' and 'number':'sg'.

### conll.field6.CPOSTAG.TAG
 This attribute works like , but instead of POSTAG, the CPOSTAG value of data rows is utilized. 
 Note: A configuration may contain both attribute types, even the same [TAG]s can be used, but if a data row is matching conditions for both a POSTAG and a CPOSTAG attribute, the one for the POSTAG attribute is used! 

### conll.SECOND.POS.NAME
Usage: conll.SECOND.POS.NAME=[VALUE]
A string specifying a valid annotation name for the second POS annotation, if desired, for example if xpos is the primary POS column, we can choose 'upos' for the other column

### conll.MISC.NAMESPACE
Usage: conll.MISC.NAMESPACE=[VALUE]
Namespace to assign to feature annotations in column 10.

### conll.field6.default
Usage: conll.field6.default=[VALUE]
Allowed values are any single category name or pipe separated sequences of category names
The default value for this attribute is morph (for 'morphological annotation').
If no / attribute is defined for both the POSTAG and the CPOSTAG value of a data row, the default name [VALUE] is used for the Annotation (or Annotations, see ) of the corresponding salt token. The data row´s FEATS field is used as the value of the annotation(s). 

### conll.POS.NAME
Usage: conll.POS.NAME=[VALUE]
A string specifying a valid annotation name for the POS annotation. Allowed values are any valid Salt annotation name. If not used and POS or CPOS are being used, then a default Salt SPOSAnnotation will be created and named 'pos'

### conll.POS.LEMMA
Usage: conll.POS.LEMMA=[VALUE]
A string specifying a valid annotation name for the lemma annotation. Allowed values are any valid Salt annotation name. If not used and conll.SLEMMA is being used, then a default Salt SLemmaAnnotation will be created and named 'lemma', provided that column 3 contains non-empty lemma values.

### conll.splitFeatures
Usage: conll.splitFeatures=[VALUE]
 If [VALUE] is set TRUE, any data row´s FEATS field will be split into it´s pipe separated elements to create multiple annotations on the corresponding salt token (see POSTAG, CPOSTAG and default). If a field contains a different number of pipe separated elements than defined in the POSTAG, CPOSTAG or default attribute, the lesser number of annotations will be created, while the additional elements will be lost! 
If VALUE is FALSE, no splitting is done.

### conll.KeyValFeatures
Usage: conll.KeyValFeatures=[VALUE]
 If [VALUE] is set TRUE, the features column (col6) is expected to contain pipedelimited pairs of annotation names and values, for example `Case=Gen|Number=Plur`.

 ### conll.FEATURES.NAMESPACE
Usage: conll.FEATURES.NAMESPACE=[VALUE]
Sets a separate namespace for annotations in the features column (col6).

 ### conll.EDGE.TYPE
Usage: conll.EDGE.TYPE=[VALUE]
Manually sets the edge type for dependency edges (default:dep). This overrides automatic settings from the conll.projectiveMode customization.

### conll.SENTENCE
Usage: conll.SENTENCE=[VALUE]
 If [VALUE] is set TRUE add a sentence annotation (cat=S) to the data.

### conll.split.edeps
Usage: conll.split.edeps=[VALUE]
 If [VALUE] is set TRUE split enhanced dependency relation labels into multiple edges based on the ':' separator (e.g. `obl:with` becomes `obl` and `with`).

### conll.no.duplicate.edeps
Usage: conll.no.duplicate.edeps=[VALUE]
 If [VALUE] is set TRUE then enhanced dependencies which have corresponding regular dependencies with the same source, target and label are ignored.

### conll.ellipsis.tok.annotation
Usage: conll.ellipsis.tok.annotation=[VALUE]
If set, ellipsis token values are imported as annotations, and replaced in base text by a blank space. Use colon to specify a namespace.

### conll.meta.prefix
Usage: conll.meta.prefix=[VALUE]
Comment lines with key value structure: `# meta::key = value` will be interpreted as metadata if the key begins with this string. Default: meta::

### conll.markable.namespace 
Usage: conll.markable.namespace =[VALUE]
Namespace for CoNLL-Coref-style markables in MISC field if present.

### conll.markable.annotation
Usage: conll.markable.annotation=[VALUE]
Annotation key containing CoNLL-Coref-style markables in MISC field, e.g. Entity in Entity=(person

### conll.markable.labels
Usage: conll.markable.labels=[VALUE]
Annotation key names (hyphen-separated) for CoNLL-Coref-style markables in MISC field if present.  GRP denotes edge cluster, EDGE denotes edge type. Default: entity-GRP-identity

### conll.sentence.annotations
Usage: conll.sentence.annotations=[VALUE]
Comma separated list of sentence hashtag key-value annotations to import, for example: s_type,speaker


