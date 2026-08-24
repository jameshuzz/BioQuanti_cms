






package net.mingsoft.base.util;

import net.mingsoft.base.constant.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 获取国际化资源工具类
 */
public class BundleUtil {

    /*
     * log4j日志记录
     */
    protected static final Logger LOG = LoggerFactory.getLogger(BundleUtil.class);

    /**
     * 获取本地化文件 推荐使用 BundleUtil.getString 方法
     * @param key
     * @param resources 资源文件所在位置
     * @return
     */
    @Deprecated
    public static String getLocaleString(String key, String resources) {
        Locale locale = LocaleContextHolder.getLocale();
        return ResourceBundle.getBundle(resources, locale).getString(key);
    }

    /**
     * 读取国际化资源文件 推荐使用 BundleUtil.getString 方法
     *
     * @param key
     *            键值
     * @return 返回获取到的字符串
     */
    @Deprecated
    public static String getResString(String key) {
        return getLocaleString(key,Const.RESOURCES);
    }

    /**
     * 读取国际化资源文件，优先模块对应的资源文件，如果模块资源文件找不到就会优先基础层 推荐使用 BundleUtil.getString 方法
     *
     * @param key
     *            键值
     * @param rb
     *            模块对应资源文件
     * @return 返回获取到的字符串
     * 推荐： getString
     */
    @Deprecated
    public static String getResString(String key, ResourceBundle rb) {
        try {
            return rb.getString(key);
        } catch (MissingResourceException var4) {
            return getLocaleString(key,Const.RESOURCES);
        }
    }

    /**
     * 读取国际化资源文件 推荐使用 BundleUtil.getString 方法
     *
     * @param key
     *            键值
     * @param fullStrs
     *            需填充的值
     * @return 返回获取到的字符串
     * 推荐使用 getLocalString
     */
    @Deprecated
    public static String getResString(String key, String... fullStrs) {
        String temp = getResString(key);
        for(int i = 0; i < fullStrs.length; ++i) {
            temp = temp.replace("{" + i + "}", fullStrs[i]);
        }

        return temp;
    }



    /**
     * 读取国际化资源文件，优先模块对应的资源文件，如果模块资源文件找不到就会优先基础层 推荐使用 BundleUtil.getString 方法
     *
     * @param key
     *            键值
     * @param rb
     *            模块对应资源文件
     * @return 返回获取到的字符串
     * 推荐： getString
     */
    @Deprecated
    public static String getResString(String key, ResourceBundle rb, String... fullStrs) {
        String temp = "";
        try {
            temp = rb.getString(key);
        } catch (MissingResourceException e) {
            temp = getResString(key);
        }
        for (int i = 0; i < fullStrs.length; i++) {
            temp = temp.replace("{" + i + "}", fullStrs[i]);
        }
        return temp;
    }


    /**
     * 读取国际化资源文件 推荐使用 BundleUtil.getString 方法
     *
     * @param key
     *            键值
     * @param fullStrs
     *            需填充的值
     * @return 返回获取到的字符串
     */
    @Deprecated
    public static String getLocalString(String key, String... fullStrs) {
        String temp = getResString(key);
        for(int i = 0; i < fullStrs.length; ++i) {
            temp = temp.replace("{" + i + "}", fullStrs[i]);
        }

        return temp;
    }

    /**
     * 读取指定目录下国际化资源文件
     * @param resources 资源文件路径，通常是 每个模块包下*.constant 中Const.RESOURCES
     * @param key 国际化文件key
     * @param params 拼接值
     * @return 国际化字符串
     */
    public static String getString(String resources,String key, String... params) {
        Locale locale = LocaleContextHolder.getLocale();
        String temp = "";
            temp = ResourceBundle.getBundle(resources, locale).getString(key);

        //替换占位
        for (int i = 0; i < params.length; i++) {
            temp = temp.replace("{" + i + "}", params[i]);
        }
        return temp;
    }

    /**
     * 读取base包国际化文件，因为base包有通用的基本提示，也经常用到，具体看 net.mingsoft.base.resources.resources
     * @param key 国际化文件key
     * @param params 拼接值
     * @return 国际化字符串
     */
    public static String getBaseString(String key, String... params) {
        Locale locale = LocaleContextHolder.getLocale();
        String temp = "";
        temp = ResourceBundle.getBundle(Const.RESOURCES, locale).getString(key);

        //替换占位
        for (int i = 0; i < params.length; i++) {
            temp = temp.replace("{" + i + "}", params[i]);
        }
        return temp;
    }
}
