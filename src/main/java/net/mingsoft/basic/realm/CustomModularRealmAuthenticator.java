


package net.mingsoft.basic.realm;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;

import java.util.Collection;

/**
 * 自定义多realm认证
 */
public class CustomModularRealmAuthenticator extends ModularRealmAuthenticator {

	/**
	 * 重写多realm认证
	 * @param realms 领域
	 * @param token 令牌
	 * @return {@link AuthenticationInfo}
	 */
	@Override
	protected AuthenticationInfo doMultiRealmAuthentication(Collection<Realm> realms, AuthenticationToken token) {
		// 匹配Realm名称
		for (Realm realm : realms) {
			if (realm.supports(token)) {
				return super.doSingleRealmAuthentication(realm, token);
			}
		}
		String msg = "No matching realm found, [" + token + "]";
		throw new IllegalArgumentException(msg);
	}

}
