package com.bresai.expecto.patronum.core.exception;

/**
 * @version 1.0
 * @author:bresai
 * @date:2019/11/26
 * @content:
 */
public class PatronumException extends Exception {
    private static final long serialVersionUID = 3769339967824377319L;

    public PatronumException() {
    }

    public PatronumException(String message) {
        super(message);
    }

    public PatronumException(String message, Throwable cause) {
        super(message, cause);
    }
}
