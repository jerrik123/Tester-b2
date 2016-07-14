package org.mangocube.corenut.commons.exception;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * The error details model, contains error message, code, etc.
 * An exception can contains a list of error records, for example, validation exception may contains
 * multiple records caused by a serial of validation errors.
 *
 * @since 1.0
 */
public class ErrorRecord implements Serializable {
    private Enum errorCode;
    private List<Object> errorParameters = new ArrayList<Object>(5);
    private String rawErrorMessage;//The raw error message stored in database or resource file.
    private String errorID;
    private Throwable cause;

    private static final char PREFIX = '{';
    private static final char POSTFIX = '}';
    private static final char SEPARATOR = '$';

    public ErrorRecord(Enum errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorRecord(Enum errorCode, Object... errorParas) {
        this.errorCode = errorCode;
        if (errorParas != null) {
            errorParameters.addAll(Arrays.asList(errorParas));
        }
    }

    public ErrorRecord(Enum errorCode, Throwable cause, Object... errorParas) {
        this.errorCode = errorCode;
        if (errorParas != null) {
            errorParameters.addAll(Arrays.asList(errorParas));
        }
        this.cause = cause;
    }

    public Enum getErrorCode() {
        return errorCode;
    }

    public Throwable getCause() {
        return cause;
    }

    List<Object> getErrorParameters() {
        return errorParameters;
    }

    String getRawErrorMessage() {
        return rawErrorMessage;
    }

    void setRawErrorMessage(String rawErrorMessage) {
        this.rawErrorMessage = rawErrorMessage;
    }

    private ErrorCode getErrCodeAnnotation() {
        try {
            Field f = errorCode.getClass().getField(errorCode.name());
            return f.getAnnotation(ErrorCode.class);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    String getParsedRawMessage() {
        return getRawErrorMessage() == null ? null : generateErrorMessage(getRawErrorMessage());
    }

    private String generateErrorMessage(String raw_msg) {
        if (errorParameters.isEmpty()) return raw_msg;

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < raw_msg.length(); i++) {
            if (SEPARATOR == raw_msg.charAt(i) && PREFIX == raw_msg.charAt(i + 1)) {
                int start = i + 1;
                int end = raw_msg.indexOf(POSTFIX, start);
                if (end == -1) return raw_msg;
                String indexKey = raw_msg.substring(start + 1, end).trim();
                int idx = 0;
                try {
                    //if there is any exception accurred during retrieving value from the
                    //parameter list, then return the raw_msg instead
                    idx = Integer.parseInt(indexKey);
                    result.append(errorParameters.get(idx - 1).toString());
                } catch (Exception ne) {
                    return raw_msg;
                }
                i = end;
            } else {
                result.append(raw_msg.charAt(i));
            }
        }
        return result.toString();
    }

    String getParsedErrorComment() {
        return generateErrorMessage(getErrCodeAnnotation().comment());
    }

    String getErrorID() {
        return errorID == null ? errorCode.toString() : errorID;
    }

    void setErrorID(String errorID) {
        this.errorID = errorID;
    }

    boolean isRecoverable() {
        return getErrCodeAnnotation().recoverable();
    }
}
