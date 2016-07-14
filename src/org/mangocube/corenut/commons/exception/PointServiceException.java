package org.mangocube.corenut.commons.exception;

import org.mangocube.corenut.commons.exception.CheckedException;

/**
 * Created by Mango
 * @author huangyongshang
 * Date 2011-3-24
 * Time 上午11:48:45
 * 系统功能 Exception类
 */
public class PointServiceException extends CheckedException {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PointServiceException(Enum errorCode) {
        super(errorCode);
    }

    public PointServiceException(Enum errorCode, Object... para) {
        super(errorCode, para);
    }

    public PointServiceException(Enum errorCode, Throwable throwable) {
        super(errorCode, throwable);
    }

    public PointServiceException(Enum errorCode, Throwable throwable, Object... para) {
        super(errorCode, throwable, para);
    }
}