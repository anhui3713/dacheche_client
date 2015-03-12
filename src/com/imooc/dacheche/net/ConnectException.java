package com.imooc.dacheche.net;

/**
 * Õ¯¬Á¡¨Ω”“Ï≥£
 * @author Huang Shan
 *
 */
public class ConnectException extends Exception {
	private static final long serialVersionUID = -1993262769449466862L;

	public ConnectException() {
		super();
	}

	public ConnectException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ConnectException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectException(String message) {
		super(message);
	}

	public ConnectException(Throwable cause) {
		super(cause);
	}
}
