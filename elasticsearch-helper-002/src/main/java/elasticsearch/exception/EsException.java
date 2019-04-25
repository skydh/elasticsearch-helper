package elasticsearch.exception;

/**
 * es异常统一抛出该异常
 * 
 * @author Lenovo
 *
 */
public class EsException extends RuntimeException {

	private static final long serialVersionUID = 3613072238231936125L;

	public EsException(String msg) {
		super(msg);
	}

	public EsException(Throwable e) {
		super(e);
	}

	public EsException(String msg, Throwable e) {
		super(msg, e);
	}

}