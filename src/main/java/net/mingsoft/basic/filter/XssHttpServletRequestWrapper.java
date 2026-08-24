



package net.mingsoft.basic.filter;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.core.io.IoUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import net.mingsoft.base.exception.BusinessException;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.basic.util.JsoupUtil;
import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * XSS 过滤器 用于请求参数的脚本数据
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private HttpServletRequest request = null;


    /**
     * 接收排除的字段名
     */
    private List<String> excludesFiled = new ArrayList<>();
    // 记录30分钟以内的攻击次数
    public static TimedCache<String, Integer> TIMED_XSS_CACHE = CacheUtil.newTimedCache(1000L * 60 * 30);
    // 记录当前请求此次请求是否存在XSS,方便提供业务进行判断（已知ms_ip模块会使用到）
    public static TimedCache<String, Boolean> TIMED_REQ_CACHE = CacheUtil.newTimedCache(1000L * 60 * 30);


    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        this.request = request;
    }

    public XssHttpServletRequestWrapper(HttpServletRequest request, List<String> excludesFiled) {
        super(request);
        this.request = request;
        if (CollectionUtil.isNotEmpty(excludesFiled)) {
            this.excludesFiled.addAll(excludesFiled);
        }
    }

    /**
     * 累加ip的攻击访问次数
     *
     * @param ip 当前访问ip
     */
    private void xssCountAdd(String ip) {
        // 增加攻击次数记录
        if (!TIMED_XSS_CACHE.containsKey(ip)) {
            TIMED_XSS_CACHE.put(ip, 1);
        } else {
            TIMED_XSS_CACHE.put(ip, TIMED_XSS_CACHE.get(ip) + 1);
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        FastByteArrayOutputStream read = IoUtil.read(request.getInputStream());
        // TODO: 2023/1/4 增加上传文件时，json对象的xss处理
        String tmp = read.toString();
        if (JSONUtil.isTypeJSON(tmp)) {
            try {
                if (JSONUtil.isTypeJSONArray(tmp)) {
                    JSONArray jsonArray = JSONUtil.parseArray(tmp);
                    for (int i = 0; i < jsonArray.size(); i++) {
                        Object item = jsonArray.get(i);
                        // 对象数组json
                        if (item instanceof Map) {
                            Map<String, Object> map = (Map<String, Object>) item;
                            Iterator iterator = map.keySet().iterator();
                            while (iterator.hasNext()) {
                                String key = iterator.next().toString();
                                JsoupUtil.cleanOrSqlInjection(key);
                                if (map.get(key) instanceof String && !excludesFiled.contains(key)) {
                                    JsoupUtil.cleanOrSqlInjection(String.valueOf(map.get(key)));
                                }
                            }
                            // 基础类型json String情况，其余基本类型不考虑检测
                        } else if (item instanceof String) {
                            JsoupUtil.cleanOrSqlInjection((String) item);
                        }
                    }
                } else {
                    Map jsonMap = JSONUtil.toBean(tmp, Map.class);
                    Iterator iterator = jsonMap.keySet().iterator();
                    while (iterator.hasNext()) {
                        String key = iterator.next().toString();
                        JsoupUtil.cleanOrSqlInjection(key);
                        if (jsonMap.get(key) instanceof String && !excludesFiled.contains(key)) {
                            JsoupUtil.cleanOrSqlInjection(String.valueOf(jsonMap.get(key)));
                        }
                    }

                }
                //记录当前请求是否存在xss
                TIMED_REQ_CACHE.put(BasicUtil.getIp(), false);
            } catch (BusinessException e) {
                xssCountAdd(BasicUtil.getIp());
                //记录当前请求是否存在xss
                TIMED_REQ_CACHE.put(BasicUtil.getIp(), true);
                throw new BusinessException(e.getMsg());
            }
        } else {
            // 与上面判断互斥，避免排除字段到此依旧被xss拦截处理
            if (JsoupUtil.hasXSS(tmp)) {
                xssCountAdd(BasicUtil.getIp());
                //记录当前请求是否存在xss
                TIMED_REQ_CACHE.put(BasicUtil.getIp(), true);
                throw new BusinessException("上传文件存在xss攻击");
            } else {
                //记录当前请求是否存在xss
                TIMED_REQ_CACHE.put(BasicUtil.getIp(), false);
            }
        }
        return new WrappedServletInputStream(new ByteArrayInputStream(read.toByteArray()));
    }

    /**
     * 覆盖getParameter方法，将参数名和参数值都做xss过滤。
     * <p>
     * 如果需要获得原始的值，则通过super.getParameterValues(name)来获取
     * <p>
     * getParameterNames,getParameterValues和getParameterMap也可能需要覆盖
     */
    @Override
    public String getParameter(String name) {
        String value = null;
        try {
            name = JsoupUtil.cleanOrSqlInjection(name);
            value = super.getParameter(name);
            if (StringUtils.isNotBlank(value)) {
                // 如果参数名是排除的，就不通过clean过滤
                if (!excludesFiled.contains(name)) {
                    value = JsoupUtil.cleanOrSqlInjection(value);
                }
            }
        } catch (BusinessException e) {
            xssCountAdd(BasicUtil.getIp());
            throw new BusinessException(e.getMsg());
        }
        return value;
    }

    @Override
    public Map getParameterMap() {
        Map map = super.getParameterMap();
        // 返回值Map
        Map<String, String> returnMap = new HashMap<String, String>();
        Iterator entries = map.entrySet().iterator();
        Map.Entry entry;
        String name = "";
        String value = "";
        boolean hasXss = false; //只记录一次攻击次数
        while (entries.hasNext()) {
            entry = (Map.Entry) entries.next();
            name = (String) entry.getKey();
            Object valueObj = entry.getValue();
            if (null == valueObj) {
                value = "";
            } else if (valueObj instanceof String[]) {
                String[] values = (String[]) valueObj;
                for (int i = 0; i < values.length; i++) {
                    value = values[i] + ",";
                }
                value = value.substring(0, value.length() - 1);
            } else {
                value = valueObj.toString();
            }
            // 如果参数名是排除的，就不通过clean过滤
            if (excludesFiled.contains(name)) {
                returnMap.put(name, value.trim());
            } else {
                try {
                    returnMap.put(JsoupUtil.cleanOrSqlInjection(name), JsoupUtil.cleanOrSqlInjection(value).trim());
                } catch (BusinessException e) {
                    if (!hasXss) {
                        xssCountAdd(BasicUtil.getIp());
                    }
                    hasXss = true;
                    TIMED_REQ_CACHE.put(BasicUtil.getIp(), hasXss);
                    throw new BusinessException(e.getMsg());
                }
            }
        }

        //记录当前请求是否存在xss
        TIMED_REQ_CACHE.put(BasicUtil.getIp(), false);
        return returnMap;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] arr = super.getParameterValues(name);
        if (arr != null) {
            boolean hasXss = false; //只记录一次攻击次数
            for (int i = 0; i < arr.length; i++) {
                // 如果参数名是排除的，就不通过clean过滤
                if (!excludesFiled.contains(name)) {
                    try {
                        arr[i] = JsoupUtil.cleanOrSqlInjection(arr[i]);
                    } catch (BusinessException e) {
                        if (!hasXss) {
                            xssCountAdd(BasicUtil.getIp());
                        }
                        hasXss = true;
                        throw new BusinessException(e.getMsg());
                    }
                }
            }
            //记录当前请求是否存在xss
            if (hasXss) {
                TIMED_REQ_CACHE.put(BasicUtil.getIp(), true);
            } else {
                TIMED_REQ_CACHE.put(BasicUtil.getIp(), false);
            }
        }
        return arr;
    }


    /**
     * 获取最原始的request
     *
     * @return
     */
    @Override
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * 获取最原始的request的静态方法
     *
     * @return
     */
    public static HttpServletRequest getOrgRequest(HttpServletRequest req) {
        if (req instanceof XssHttpServletRequestWrapper) {
            return ((XssHttpServletRequestWrapper) req).getRequest();
        }

        return req;
    }


    private class WrappedServletInputStream extends ServletInputStream {
        public void setStream(InputStream stream) {
            this.stream = stream;
        }

        private InputStream stream;

        public WrappedServletInputStream(InputStream stream) {
            this.stream = stream;
        }

        @Override
        public int read() throws IOException {
            return stream.read();
        }

        @Override
        public boolean isFinished() {
            return true;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {

        }
    }
}
