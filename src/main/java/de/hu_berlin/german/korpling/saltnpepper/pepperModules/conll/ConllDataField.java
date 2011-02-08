package de.hu_berlin.german.korpling.saltnpepper.pepperModules.conll;

public enum ConllDataField {
	
	ID		( 1,true),
	FORM	( 2,true),
	LEMMA	( 3,false,"_"),
	CPOSTAG ( 4,true),
	POSTAG	( 5,true),
	FEATS	( 6,false,"_"),
	HEAD	( 7,true),
	DEPREL	( 8,true),
	PHEAD	( 9,false,"_"),
	PDEPREL	(10,false,"_");

	private final int fieldNum;
	private final boolean mandatory;
	private final String dummyValue;
	
	ConllDataField(int fieldNum, boolean mandatory) {
		this(fieldNum,mandatory,"");
	}

	ConllDataField(int fieldNum, boolean mandatory, String dummyValue) {
		this.fieldNum = fieldNum;
		this.mandatory = mandatory;
		this.dummyValue = dummyValue;
	}
	
	public int getFieldNum() {
		return fieldNum;
	}
	
	public boolean isMandatory() {
		return mandatory;
	}
	
	public String getDummyValue() {
		return dummyValue;
	}
	
	public String getPropertyKey_Name() {
		return getPropertyKey_Name_byFieldNum(fieldNum);
	}
	
	public static ConllDataField getFieldByNum(int fieldNum) {
		switch (fieldNum) {
			case  1: return ID;
			case  2: return FORM;
			case  3: return LEMMA;
			case  4: return CPOSTAG;
			case  5: return POSTAG;
			case  6: return FEATS;
			case  7: return HEAD;
			case  8: return DEPREL;
			case  9: return PHEAD;
			case 10: return PDEPREL;
			default: return null;
		}
	}
	
	public static String getPropertyKey_Name_byFieldNum(int fieldNum) {
		return "conll.field"+fieldNum+".name"; 
	}
}
