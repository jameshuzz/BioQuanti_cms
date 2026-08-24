
package net.mingsoft.base.exception;

import org.springframework.http.HttpStatus;


/**
 * 专用于 API / 服务间调用场景的业务异常。
 * <p>
 * 当抛出此异常时，全局异常处理器应强制返回 JSON 格式响应（而非渲染 HTML 错误页面），
 * 以便 服务 调用能正确解析结构化错误信息。
 * </p>
 * 与 {@link BusinessException} 的主要区别：BusinessApiException固定返回json信息，而BusinessException会渲染返会错误页面
 */
public class BusinessApiException extends BusinessException{

    public BusinessApiException(String msg) {
        super(msg);
    }

    public BusinessApiException(HttpStatus code, String msg, Object data) {
        super(code, msg, data);
    }

    public BusinessApiException(String msg, Object data) {
        super(msg, data);
    }

    public BusinessApiException(String msg, String url) {
        super(msg, url);
    }

    public BusinessApiException(HttpStatus code, String msg) {
        super(code, msg);
    }
}
