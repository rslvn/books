package org.example.assessment.exception;

/**
 * Created by resulav on 02.05.2018.
 */
public class BookException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BookException(String reason) {
        super(reason);
    }
    public BookException(String reason, Exception e) {
        super(reason, e);
    }

    public static BookException newInstance(String reason){
       return new BookException(reason);
    }

    public static BookException newInstance(String reason, Exception e){
        return new BookException(reason,e);
    }

}
