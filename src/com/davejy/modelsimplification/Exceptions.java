package com.davejy.modelsimplification;

//�����������j3d�г�ȡ����Ϊ�˷���

/**
 * Exception used to indicate that a file of the incorrect
 * type was passed to a loader. 
 */
class IncorrectFormatException extends RuntimeException {

    public IncorrectFormatException() {
	super();
    }

    public IncorrectFormatException(String s) {
	super(s);
    }
}
/**
 * Exception used to indicate that the loader encountered
 * a problem parsing the specified file.
 */
class ParsingErrorException extends RuntimeException {

    public ParsingErrorException() {
	super();
    }

    public ParsingErrorException(String s) {
	super(s);
    }
}