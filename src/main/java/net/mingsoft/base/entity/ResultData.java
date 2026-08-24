








package net.mingsoft.base.entity;


import cn.hutool.json.JSONUtil;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName: ResultJson
 * @Description:TODO(json数据返回数据格式)
 * @date: 2018年3月19日 下午3:41:53
 */
public class ResultData extends HashMap<String, Object> {

    /**
     * 状态码
     */
    public static final String CODE_KEY = "code";
    /**
     * 数据
     */
    public static final String DATA_KEY = "data";
    /**
     * 信息
     */
    public static final String MSG_KEY = "msg";
    /**
     * 请求状态
     */
    public static final String RESULT_KEY = "result";


    public static ResultData build() {
        return new ResultData();
    }

    /**
     * 设置web响应码
     * @param code http响应码
     * @return 只有200响应码 result才为true 其余都为false
     */
    public ResultData code(HttpStatus code) {
        return add(RESULT_KEY, code == HttpStatus.OK).add(CODE_KEY, code.value());
    }

    /**
     * 设置ms自身业务的响应码 如 双因子验证 code("doubleFactorEnable")
     * @param code ms自身业务code
     * @return 只有200响应码 result才为true 其余都为false
     */
    public ResultData code(String code) {
        return add(RESULT_KEY, code.equalsIgnoreCase(HttpStatus.OK.toString())).add(CODE_KEY, code);
    }


    /**
     * 返回信息
     *
     * @param msg
     * @return
     */
    public ResultData msg(String msg) {
        return add(MSG_KEY, msg);
    }

    /**
     * 返回数据
     *
     * @param data
     * @return
     */
    public ResultData data(Object data) {
        return add(DATA_KEY, data);
    }

    /**
     * 成功返回
     *
     * @return
     */
    public ResultData success() {
        return code(HttpStatus.OK);
    }

    /**
     * 成功返回
     *
     * @return
     */
    public ResultData success(Object data) {
        return success().data(data);
    }

    /**
     * 成功返回
     *
     * @return
     */
    public ResultData success(String msg, Object data) {
        return success().msg(msg).data(data);
    }

    /**
     * 错误返回
     *
     * @return
     */
    public ResultData error() {
        return code(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 错误返回
     *
     * @return
     */
    public ResultData error(String msg) {
        return error().msg(msg);
    }

    /**
     * 添加返回参数
     *
     * @param key
     * @param value
     * @return
     */
    public ResultData add(String key, Object value) {
        this.put(key, value);
        return this;
    }

    /**
     * 获取data对象
     * @param c 对象类型
     * @param <T> 泛性
     * @return null 可能转换失败
     */
    public <T> T getData(Class<T> c) {
        Object obj = this.get(DATA_KEY);
        //主要是优化微服务转换，默认微服务是不能直接转换实体，需要将map 转 实体,
        if(obj instanceof Map) {
            return  JSONUtil.toBean(JSONUtil.toJsonStr(obj),c);
        }
        return (T)obj;
    }

    /**
     * 获取文本提示信息
     * @return 不为null 返回信息；否则 返回 ""
     */
    public String getMsg() {
        return Objects.isNull(this.get(MSG_KEY))? "" : this.get(MSG_KEY).toString();
    }

    /**
     * 获取编码
     * @return
     */
    public int getCode() {
        return Integer.parseInt(this.get(CODE_KEY)+"");
    }

    /**
     * 判断接口成功状态
     * @return true:成功
     */
    public boolean isSuccess() {
        return Boolean.parseBoolean(this.get(RESULT_KEY)+"");
    }
}
