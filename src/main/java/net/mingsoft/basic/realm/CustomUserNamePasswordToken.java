
package net.mingsoft.basic.realm;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * 因改造people使用shiro登录，扩展shiro UserNamePasswordToken，增加AuthType，登录时区分管理员和会员登录
 */
public class CustomUserNamePasswordToken extends UsernamePasswordToken{

    /**
     * token类型 现在有 管理员 和 会员
     */
    private final AuthType authType;


    public CustomUserNamePasswordToken(String username, String password, AuthType type) {
        super(username, password);
        this.authType = type;
    }

    public CustomUserNamePasswordToken(String username, String password, boolean rememberMe, AuthType type) {
        super(username, password,rememberMe);
        this.authType = type;
    }

    public CustomUserNamePasswordToken(String username, char[] password, boolean rememberMe, AuthType type) {
        super(username, password,rememberMe);
        this.authType = type;
    }


    public AuthType getAuthType() {
        return authType;
    }



    public enum AuthType {
        MANAGER,
        PEOPLE
    }



}
