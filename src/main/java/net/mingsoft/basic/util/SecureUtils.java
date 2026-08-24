




package net.mingsoft.basic.util;

import cn.hutool.crypto.digest.DigestUtil;
import net.mingsoft.base.constant.Const;
import org.apache.shiro.crypto.hash.SimpleHash;

/**
 * 管理员安全工具类
 *
 * @author 铭软
 * @version 版本号：200-000-000<br/>
 * 创建日期：2012-03-15<br/>
 * 历史修订：<br/>
 */
public class SecureUtils {

    /**
     * 默认加密方式
     * @return
     */
    public static String getHashAlgorithmName() {
        return "MD5";
    }

    /**
     * 密码加密
     *
     * @param password 密码
     * @param salt     盐
     * @return 字符串
     */
    public static String password(String password, String salt) {
        return DigestUtil.md5Hex(password, Const.UTF8);
    }


    /**
     * 获取对应盐，主要是对盐进行md5+散列
     * 为了方便其他模块覆盖调用
     * @param salt 盐
     * @return
     */
    public static SimpleHash getSalt(String salt) {
        return new SimpleHash(getHashAlgorithmName(), salt,  1);
    }

    /**
     * 获取对应盐，主要是对盐进行md5+散列
     *
     * @param salt               盐
     * @param saltHashIterations 盐的散列次数
     * @return
     */
    public static SimpleHash getSalt(String salt, int saltHashIterations) {
        return new SimpleHash(getHashAlgorithmName(),salt, null, saltHashIterations);
    }


}
