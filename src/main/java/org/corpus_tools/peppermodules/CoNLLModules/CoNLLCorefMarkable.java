package org.corpus_tools.peppermodules.CoNLLModules;

/*
 * Copyright 2017 GU.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;

/**
 *
 * @author Amir Zeldes
 */public class CoNLLCorefMarkable {
     
    private ArrayList<SToken> tokens;
    private ArrayList<SAnnotation> annotations;
    private String nodeName;
    private String text;
    private String group;
    private String annoString;
    private String edgeType = "ident";
    private int start;
    private int end;
    public CoNLLCorefMarkable antecedent;

    public int getStart() {
        return start;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getAnnoString() {
        return annoString;
    }

    public void setAnnoString(String annoString) {
        this.annoString = annoString;
    }
    
    public String getEdgeType() {
        return edgeType;
    }

    public void setEdgeType(String edgeType) {
        this.edgeType = edgeType;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getNodeName() {
        return nodeName;
    }
    
    public CoNLLCorefMarkable(){

        this.tokens = new ArrayList<>();
        this.annotations = new ArrayList<>();
        this.antecedent = null;
             
    }

    public CoNLLCorefMarkable(int start){

        this.tokens = new ArrayList<>();
        this.annotations = new ArrayList<>();
        this.start = start;
        this.antecedent = null;
             
    }
    
    
    public CoNLLCorefMarkable(ArrayList<SToken> tokens) {
        this.tokens = tokens;
        this.antecedent = null;
       
    }

    public ArrayList<SAnnotation> getAnnotations() {
        return annotations;
    }


    public ArrayList<SToken> getTokens() {
        return tokens;
    }

    public void addAnnotation(String namespace, String annoName, String annoValue){
        SAnnotation sAnno = SaltFactory.createSAnnotation();
        sAnno.setNamespace(namespace);
        sAnno.setName(annoName);
        sAnno.setValue(annoValue);
        this.annotations.add(sAnno);
    }
    
    public void addToken(SToken tok){
        if (!this.tokens.contains(tok)){
            this.tokens.add(tok);
        }
    }

    void setNodeName(String name) {
        this.nodeName = name;
    }

    
    @Override
    public String toString(){
        
        StringBuilder coveredText = new StringBuilder();
        for (SToken tok : this.tokens){
            coveredText.append(tok.getId() + " ");
        }
        
        return "CoNLLCorefMarkable in group "+this.getGroup()+": " + 
                this.nodeName +  " > " + coveredText.toString() + " at token " + this.start;
              
    }
    
}