package com.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * The application exception supper class. Extends this class for all application exception.
 * When throw an exception, can take the following three parameters.
 * 1. Error code. Unlike ordinary exception taking string as error, the code must be enum type. The enum value must be
 * marked with ErrorDefinition annotation, specify the error comment (default message).
 * 2. Caused exception. Feature provided by JDK exception mechanism (1.4 and later).
 * 3. Error message parameters. The default message defined in ErrorDefinition can take parameters, fill in dynamic contents.
 * <p/>
 * For example, certain class has two error codes, and ServiceException extends CheckedException.
 * <code>
 * public class MockService {
 * public enum ErrorCodes {
 *
 * @ErrorDefinition(comment = "Input string value ${1} is too long!")
 * ERROR_A,
 * @ErrorDefinition(comment = "Input date value ${1} format is invalid, ${2} is expected!")
 * ERROR_B
 * }
 * <p/>
 * public void serviceFun1(...) throws ServiceException {
 * try {
 * ...
 * } catch (... e) {
 * throw new ServiceException(ERROR_B, e, "07/09/2007", "YYYY-MM-DD");
 * }
 * }
 * }
 * </code>
 * The final error message is "Input date value 07/09/2007 format is invalid, YYYY-MM-DD is expected!"
 * <p/>
 * To support I18N, the default message defined in ErrorDefinition is just a comment. Exception message I18N tool
 * will iterate all error code declarations, and extract corresponding information in ErrorDefinition, then generates
 * a I18N message resource (configuration) file template. Afterwards, we can translate these messages to various languages.
 * Hence, such configuration and translation procedure are totally transparent to developers. For more details, see the
 * I18N tool documents.
 * @since 1.0
 */
public class CheckedException extends Exception {
    private static final String DEFAULT_MSG = "Exception default comment.\n  Error code: ";

    private ErrorRecord errorRec;
    private static final List<ErrorRecord> EMPTY_LIST = new ArrayList<ErrorRecord>(0);
    private List<ErrorRecord> errorDetailRecs = EMPTY_LIST;

    public CheckedException(Enum errorCode) {
        errorRec = new ErrorRecord(errorCode);
    }

    public CheckedException(Enum errorCode, Object... para) {
        errorRec = new ErrorRecord(errorCode, para);
    }

    public CheckedException(Enum errorCode, Throwable throwable) {
        super(throwable);
        errorRec = new ErrorRecord(errorCode);
    }

    public CheckedException(Enum errorCode, Throwable throwable, Object... para) {
        super(throwable);
        errorRec = new ErrorRecord(errorCode, para);
    }

    public final Enum getErrorCode() {
        return errorRec.getErrorCode();
    }

    final void setRawErrorMessage(String rawErrorMessage) {
        this.errorRec.setRawErrorMessage(rawErrorMessage);
    }

    public List<ErrorRecord> getErrorDetailRecs() {
        return errorDetailRecs;
    }

    protected void setErrorDetailRecs(List<ErrorRecord> errorDetailRecs) {
        this.errorDetailRecs = errorDetailRecs == null ? EMPTY_LIST : errorDetailRecs;
    }

    public final String getMessage() {
        String raw_msg = errorRec.getParsedRawMessage();
        if (raw_msg == null) {
            raw_msg = new StringBuilder().append(DEFAULT_MSG).append(errorRec.getErrorCode()).
                    append("\n  Message: ").append(errorRec.getParsedErrorComment()).
                    append("\n  Exception ID: ").append(errorRec.getErrorID()).toString();
        }
        String detail = getErrorDetailMessages();
        return raw_msg + (detail == null ? "" : "Details: \n" + detail);
    }

    private String getErrorDetailMessages() {
        if (errorDetailRecs == null || errorDetailRecs.size() == 0) return null;
        StringBuilder sb = new StringBuilder();
        for (ErrorRecord rec : errorDetailRecs) {
            if (rec.getParsedRawMessage() == null) {
                sb.append("\n  [").append(rec.getErrorCode()).append("]: ").append(rec.getParsedErrorComment());
            } else {
                sb.append("\n  [").append(rec.getErrorID()).append("]: ")//Error ID
                        .append(rec.getParsedRawMessage());//Parsed Error Messasge
            }
        }
        return sb.toString();
    }

    public final String getLocalizedMessage() {
        return getMessage();
    }

    @AccessRestriction
    public ErrorRecord getErrorRec() {
        return errorRec;
    }
}
