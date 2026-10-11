# 配置文件归档说明

本文件夹为生产服务器（47.110.52.76）关键配置文件的留存副本，与
[02-生产环境部署手册.md](../02-生产环境部署手册.md) 配套使用。

**留存时间：2026-10-11**。若服务器配置已变更，以服务器上的实际文件为准。

## 文件清单

| 本地文件 | 服务器原始路径 | 用途 |
|---|---|---|
| mcms/mcms.sh | /opt/mcms/mcms.sh | 应用启停脚本（含 JVM 参数） |
| mcms/application-prod.yml | /opt/mcms/config/application-prod.yml | 生产环境配置（数据库、验证码开关等） |
| nginx/nginx.conf | /etc/nginx/nginx.conf | Nginx 主配置 |
| nginx/conf.d-mcms.conf | /etc/nginx/conf.d/mcms.conf | 站点 Nginx 配置（短链、HTTPS、SEO 路由） |
| mysql/my.cnf | /etc/my.cnf | MySQL 配置（2核2G 优化参数） |
| seo/robots.txt | /opt/mcms/seo/robots.txt | 蜘蛛抓取规则（指向 zh.bioquanti.com） |
| seo/sitemap.xml | /opt/mcms/seo/sitemap.xml | 站点地图（1756 条短路径 URL） |
| seo/baidu_verify_codeva-w8B2Pkbh5u.html | /opt/mcms/seo/ | 百度站长平台验证文件 |
| ssl/zh.bioquanti.com_bundle.crt | /etc/nginx/cert/ | 中文站证书链 |
| ssl/zh.bioquanti.com.key | /etc/nginx/cert/ | 中文站证书私钥 |
| ssl/bioquanti.com_bundle.crt | /etc/nginx/cert/ | 英文站证书链 |
| ssl/bioquanti.com.key | /etc/nginx/cert/ | 英文站证书私钥 |

## 恢复方法

1. **mcms.sh / application-prod.yml**：直接覆盖原路径后执行
   `/opt/mcms/mcms.sh restart`。
2. **Nginx 配置**：覆盖后执行 `nginx -t` 校验语法，通过后
   `systemctl reload nginx`（无需重启应用）。
3. **SEO 文件**：覆盖到 `/opt/mcms/seo/` 即时生效，无需重启任何服务。
4. **证书更换**：覆盖后执行 `nginx -t && systemctl reload nginx`；
   续期流程详见部署手册第 5 节。

## 安全提醒

- `ssl/*.key` 为证书私钥、`application-prod.yml` 含数据库密码，**仅限内部留存，严禁外传或提交到公开仓库**。
- 服务器账号信息见部署手册"服务器概况"一节，同样属敏感信息。
