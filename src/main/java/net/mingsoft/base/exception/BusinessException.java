



package net.mingsoft.base.exception;

import org.springframework.http.HttpStatus;

/**
 * 业务异常处理
 *
 * @author 铭软开发团队-Administrator
 * @date 2018年7月6日
 * 历史修订： 2023-06-13 业务异常移至base包，暂不删除basic的异常类
 */

public class BusinessException extends RuntimeException {

    private HttpStatus code = HttpStatus.INTERNAL_SERVER_ERROR;
    private String msg;
    private Object data;
    private String url;

    public BusinessException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public BusinessException(HttpStatus code, String msg, Object data) {
        super(msg);
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public BusinessException(String msg, Object data) {
        super(msg);
        this.msg = msg;
        this.data = data;
    }

    public BusinessException(String msg, String url) {
        super(msg);
        this.msg = msg;
        this.url = url;
    }

    /**
     * 自定义异常信息
     *
     * @param code 错误码 HttpStatus
     * @param msg  错误信息
     */
    public BusinessException(HttpStatus code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public HttpStatus getCode() {
        return code;
    }

    public void setCode(HttpStatus code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
