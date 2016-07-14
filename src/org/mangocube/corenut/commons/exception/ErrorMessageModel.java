
package org.mangocube.corenut.commons.exception;

/**
 * error message model.
 *
 * @since 1.0
 */

class ErrorMessageModel {
    private String uri;
    private String id;
    private String comment;
    private String reason;
    private String message;

    public ErrorMessageModel(String uri, String comment, String reason, String id, String message) {
        this.uri = uri;
        this.comment = comment;
        this.reason = reason;
        this.id = id;
        this.message = message;
    }

    /**
     * get comment
     * @return comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * get id
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * get message 
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * get uri
     * @return uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * get trouble shooting
     * @return trouble shooting
     */
	public String getReason() {
		return reason;
	}
}

