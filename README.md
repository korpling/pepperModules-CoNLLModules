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
  <property key="PROPERTY_NAME">PROPERTY_VALUE</key>
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
            
## Properties

The following table contains an overview of all usable properties to customize the behaviour of this pepper module. The following section contains a close description to each single property and describes the resulting differences in the mapping to the salt model. 
pepper modules contained in this project

|Name of property			|possible values		|default value|	
|---------------------------|-----------------------|-------------|
|conll.SPOS					|POSTAG, CPOSTAG, NONE	|POSTAG|
|conll.SLEMMA				|LEMMA, NONE			|LEMMA|
|conll.considerProjectivity	|TRUE, FALSE			|TRUE|
|conll.projectiveMode		|TYPE, NAMESPACE		|TYPE|
|conll.field6.POSTAG.TAG	|TRUE, FALSE			|TRUE|
|conll.field6.CPOSTAG.TAG	|any					| |
|conll.field6.default		|any					| |
|conll.splitFeatures		|a single category name or a pipe separated sequence of category names	| morph|

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
Default value for this attribute is TRUE
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

### conll.field6.POSTAG.NE=case|number|gender conll.field6.POSTAG.VF=tense|person|number
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

### conll.field6.default
Usage: conll.field6.default=[VALUE]
Allowed values are any single category name or pipe separated sequences of category names
The default value for this attribute is morph (for 'morphological annotation').
If no / attribute is defined for both the POSTAG and the CPOSTAG value of a data row, the default name [VALUE] is used for the Annotation (or Annotations, see ) of the corresponding salt token. The data row´s FEATS field is used as the value of the annotation(s). 

### conll.splitFeatures
Usage: conll.splitFeatures=[VALUE]
 If [VALUE] is set TRUE, any data row´s FEATS field will be split into it´s pipe separated elements to create multiple annotations on the corresponding salt token (see POSTAG, CPOSTAG and default). If a field contains a different number of pipe separated elements than defined in the POSTAG, CPOSTAG or default attribute, the lesser number of annotations will be created, while the additional elements will be lost! 
If VALUE is FALSE, no splitting is done.
