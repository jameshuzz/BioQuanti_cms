# BioQuanti CMS（MCMS）CentOS 生产环境部署文档

> 依据铭飞官方部署手册（https://doc.mingsoft.net/server/ ）、MCMS 使用手册（https://doc.mingsoft.net/mcms/ ）及 Gitee 仓库说明（https://gitee.com/mingSoft/MCMS ），结合本项目（MCMS 6.2.1 / Spring Boot 3.5.11 / Java 17）实际结构整理。

## 一、部署架构（官方推荐：动静分离）

```
浏览器 ──> Nginx(:80/:443)
              ├── /html/、/template/、/upload/、/static/  → 直接返回磁盘静态文件（不动 Java 服务）
              └── 其余请求（后台 /ms、动态搜索 /mcms/search.do 等）→ 反向代理 → Spring Boot(:8080)
Spring Boot(:8080) ──> MySQL 8(:3306，仅本机/内网)
```

官方要求的环境基线：

| 项目 | 要求 |
|---|---|
| 操作系统 | Windows / Linux（本文 CentOS 7.9+/Stream 8/9） |
| 服务器配置 | 最低 2 核 CPU / 8G 内存 / 200G 硬盘（官方手册） |
| JDK | ≥ 17 |
| MySQL | ≥ 8，**必须开启忽略表名大小写（lower_case_table_names=1）** |
| 带宽 | 最低 2M |
| 对外端口 | 80、443（8080/3306 不对公网开放） |

---

## 二、服务器基础准备

```bash
# 时区（JDBC 连接串使用了 Asia/Shanghai，服务器时区需一致）
timedatectl set-timezone Asia/Shanghai

# 关闭 SELinux（避免 Nginx 读取 /opt/mcms 静态文件被拦截）
sed -i 's/^SELINUX=enforcing/SELINUX=permissive/' /etc/selinux/config
setenforce 0

# 防火墙只放行 80/443，SSH 按需限制来源 IP
firewall-cmd --permanent --add-service=http --add-service=https
firewall-cmd --reload
```

---

## 三、安装 JDK 17

```bash
# CentOS Stream 8/9、Alibaba Cloud Linux：
yum install -y java-17-openjdk java-17-openjdk-devel

# CentOS 7（yum 源无 OpenJDK 17，用压缩包）：
cd /opt
curl -LO https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-x64_bin.tar.gz
tar -zxvf openjdk-17.0.2_linux-x64_bin.tar.gz

# 配置环境变量（压缩包方式需要；yum 方式一般已自动配置）
cat >> /etc/profile <<'EOF'
export JAVA_HOME=/opt/jdk-17.0.2
export PATH=$JAVA_HOME/bin:$PATH
EOF
source /etc/profile

# 验证
java -version   # openjdk version "17.x.x"
```

---

## 四、安装并配置 MySQL 8

### 4.1 关键前置：lower_case_table_names=1

铭飞官方明确要求 MySQL 开启忽略表名大小写（Windows 默认开启，Linux 默认关闭）。
**MySQL 8 只允许在数据目录初始化之前设置该参数，初始化后无法修改**。因此必须在 `mysqld` 首次启动前写好配置；若已初始化过，需要停库、清空数据目录重新初始化。

### 4.2 安装（以 CentOS Stream / yum 方式为例）

```bash
yum install -y mysql-server

# 初始化前先写配置（重点！）
cat >> /etc/my.cnf <<'EOF'
[mysqld]
lower_case_table_names=1
character-set-server=utf8mb4
collation-server=utf8mb4_general_ci
default-time-zone='+8:00'
max_connections=1000
EOF

# 首次启动（此时完成数据目录初始化，lower_case_table_names 生效）
systemctl enable --now mysqld

# 安全初始化（设置 root 密码等）
mysql_secure_installation

# 验证
mysql -uroot -p -e "SHOW VARIABLES LIKE 'lower_case_table_names';"   # 必须为 1
mysql -uroot -p -e "SHOW VARIABLES LIKE 'character%';"              # utf8mb4
```

> CentOS 7 无 mysql-server（是 mariadb），需先安装 MySQL 官方 yum 源：
> `yum install -y https://repo.mysql.com/mysql80-community-release-el7-11.noarch.rpm` 后再执行上述步骤。

### 4.3 建库、建账号、导入数据

```bash
mysql -uroot -p <<'EOF'
CREATE DATABASE mcms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
-- 建独立账号，不用 root 跑应用；只授权本机访问
CREATE USER 'mcms'@'localhost' IDENTIFIED BY '改成强密码';
GRANT ALL PRIVILEGES ON mcms.* TO 'mcms'@'localhost';
FLUSH PRIVILEGES;
EOF

# 导入项目数据库脚本（doc/mcms.sql 为当前完整库）
mysql -umcms -p mcms < /opt/mcms/doc/mcms.sql
```

---

## 五、项目打包与服务器目录规划

### 5.1 本地打包

```bash
mvn clean package -DskipTests
# 产物：target/ms-mcms.jar（Spring Boot 可执行 fat jar）
```

> pom.xml 中已留有官方建议的生产打包方式：将 `static/`、`html/`、`upload/`、`template/` 排除出 jar（避免打进包里），生产环境把这几个目录放在 jar 旁的运行目录下，便于实时修改模板/同步静态页。
> 保持 pom 现状（不排除）也能跑，但模板改动必须重新打包；生产推荐按上述方式调整。

### 5.2 服务器目录规划

本项目的静态资源映射是按 **jar 运行时的工作目录** 相对解析的（WebConfig 中 `file:template/`、`file:upload/`、`file:html/`），因此目录布局如下：

```
/opt/mcms/
├── ms-mcms.jar              # 应用 jar
├── config/
│   └── application-prod.yml # 生产配置（Spring Boot 自动加载 ./config/，优先级高于 jar 内）
├── template/                # 模板（src/main/webapp/template 同步过来）
├── upload/                  # 上传资源（src/main/webapp/upload 同步过来）
├── html/                    # 静态页面（静态化生成到 html/web/cn、html/web/en）
├── static/                  # 后台静态资源（src/main/webapp/static）
└── log/                     # 运行日志（mcms.log、error.log，保留 90 天自动滚动）
```

### 5.3 上传文件

```bash
# 上传 jar
scp target/ms-mcms.jar root@服务器IP:/opt/mcms/

# 同步模板、静态资源、上传目录（upload 不要带 --delete，避免误删服务器已上传文件）
rsync -avz src/main/webapp/template/ root@服务器IP:/opt/mcms/template/
rsync -avz src/main/webapp/static/   root@服务器IP:/opt/mcms/static/
rsync -avz src/main/webapp/upload/   root@服务器IP:/opt/mcms/upload/
rsync -avz src/main/webapp/html/     root@服务器IP:/opt/mcms/html/
scp doc/mcms.sql root@服务器IP:/opt/mcms/doc/
```

### 5.4 生产配置文件

`/opt/mcms/config/application-prod.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mcms?useUnicode=true&serverTimezone=Asia/Shanghai&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&autoReconnect=true&allowMultiQueries=true&useSSL=false&allowPublicKeyRetrieval=true
    username: mcms
    password: 上面设置的生产数据库密码
    type: com.alibaba.druid.pool.DruidDataSource

# 生产安全项（覆盖 application.yml 中的开发配置）
springdoc:
  api-docs:
    enabled: false      # 生产关闭 swagger 接口文档（官方提示：生产务必关掉）
  swagger-ui:
    enabled: false

ms:
  manager:
    check-code: true    # 生产开启后台登录验证码
```

> 若需修改后台访问路径（默认 `/ms`），可在此文件追加 `ms.manager.path: /自定义路径`，修改后后台地址变为 `http://域名/自定义路径/login.do`。

---

## 六、部署服务（手动启停脚本）

创建 `/opt/mcms/mcms.sh`：

```bash
#!/bin/bash
# BioQuanti CMS (MCMS 6.2.1) 启停脚本
# 用法: ./mcms.sh {start|stop|restart|status|log}

APP_NAME="BioQuanti-CMS"
APP_DIR="/opt/mcms"
JAR_FILE="$APP_DIR/ms-mcms.jar"
# 注意: log/mcms.log、log/error.log 由应用 log4j 自行滚动管理，
# nohup 标准输出重定向到单独的 stdout.log，避免与 log4j 混写同一文件
LOG_FILE="$APP_DIR/log/stdout.log"
PID_FILE="$APP_DIR/mcms.pid"
JAVA_OPTS="-Xms512m -Xmx2g -Duser.timezone=Asia/Shanghai"

# 取进程 PID：优先读 pid 文件，失效则用 jps 兜底
get_pid() {
    if [ -f "$PID_FILE" ]; then
        local pid
        pid=$(cat "$PID_FILE")
        if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
            echo "$pid"; return 0
        fi
        rm -f "$PID_FILE"
    fi
    jps -l 2>/dev/null | grep "$JAR_FILE" | awk '{print $1}'
}

start() {
    local pid
    pid=$(get_pid)
    if [ -n "$pid" ]; then
        echo "[$APP_NAME] 已在运行 (PID: $pid)"; exit 0
    fi
    if [ ! -f "$JAR_FILE" ]; then
        echo "[$APP_NAME] 错误: 找不到 $JAR_FILE"; exit 1
    fi
    mkdir -p "$APP_DIR/log"
    # 关键: 工作目录必须是 /opt/mcms，模板/上传/静态页均按此目录相对解析
    cd "$APP_DIR" || exit 1
    nohup java $JAVA_OPTS -jar "$JAR_FILE" --spring.profiles.active=prod \
        >> "$LOG_FILE" 2>&1 &
    echo $! > "$PID_FILE"
    echo "[$APP_NAME] 启动中 (PID: $!)，日志: tail -f $LOG_FILE"
}

stop() {
    local pid
    pid=$(get_pid)
    if [ -z "$pid" ]; then
        echo "[$APP_NAME] 未在运行"; rm -f "$PID_FILE"; exit 0
    fi
    echo "[$APP_NAME] 停止中 (PID: $pid)..."
    kill "$pid" 2>/dev/null
    # 最多等 30 秒优雅退出
    for i in $(seq 1 30); do
        kill -0 "$pid" 2>/dev/null || break
        sleep 1
    done
    # 仍未退出则强杀
    if kill -0 "$pid" 2>/dev/null; then
        echo "[$APP_NAME] 优雅停止超时，强制结束"
        kill -9 "$pid" 2>/dev/null
    fi
    rm -f "$PID_FILE"
    echo "[$APP_NAME] 已停止"
}

status() {
    local pid
    pid=$(get_pid)
    if [ -n "$pid" ]; then
        echo "[$APP_NAME] 运行中 (PID: $pid)"
    else
        echo "[$APP_NAME] 未运行"
    fi
}

case "$1" in
    start)   start ;;
    stop)    stop ;;
    restart) stop; sleep 2; start ;;
    status)  status ;;
    log)     tail -f "$LOG_FILE" ;;
    *)       echo "用法: $0 {start|stop|restart|status|log}"; exit 1 ;;
esac
```

```bash
chmod +x /opt/mcms/mcms.sh

# 启动
/opt/mcms/mcms.sh start

# 查看状态 / 实时日志
/opt/mcms/mcms.sh status
/opt/mcms/mcms.sh log

# 看启动日志确认无异常
tail -f /opt/mcms/log/mcms.log
```

---

## 七、安装配置 Nginx（动静分离 + 反向代理）

```bash
yum install -y nginx
```

创建 `/etc/nginx/conf.d/mcms.conf`：

```nginx
server {
    listen 80;
    server_name 你的域名或公网IP;

    # 上传文件大小上限，需 ≥ 后台配置的上传限制（本项目默认约 1GB）
    client_max_body_size 1000m;

    gzip on;
    gzip_min_length 1k;
    gzip_types text/plain text/css application/javascript application/json image/svg+xml;

    # ---- 动静分离：静态资源直接读磁盘，不经过 Java ----
    location /html/     { alias /opt/mcms/html/; }
    location /template/ { alias /opt/mcms/template/; }
    location /upload/   { alias /opt/mcms/upload/; }
    location /static/   { alias /opt/mcms/static/; }

    # 网站根路径：返回 html/web/index.html（中英文跳转页）
    location = / {
        root /opt/mcms/html/web;
        try_files /index.html =404;
    }

    # ---- 动态请求反代到 Spring Boot ----
    # 后台 /ms/**、动态搜索 /mcms/search.do、其他 .do 接口
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;                 # 留言管理取真实IP依赖此头
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
nginx -t && systemctl enable --now nginx
systemctl reload nginx
```

> HTTPS：准备好证书后另加 443 server 块（`ssl_certificate` / `ssl_certificate_key`），并把 80 做301跳转即可；若使用 Cloudflare 等 CDN，注意在应用侧取真实 IP 需增加对 CDN 请求头（如 CF-Connecting-IP）的支持。

---

## 八、首次部署后必做操作（官方强调）

1. 访问后台 `http://域名/ms/login.do` 登录管理员账号。
2. 进入 **内容管理 → 静态化**：
   - **生成主页**：后台"主页位置"填 `cn/index`（不带 .html 后缀）生成中文首页；切换为 `en/index` 再生成英文首页；
   - **生成栏目**、**生成文章** 各执行一遍。
3. 官方手册明确：**首次启动如不生成静态页面，前端访问会 404**；之后每次修改模板或文章内容，都必须重新执行对应静态化。

验证：

| 地址 | 预期 |
|---|---|
| `http://域名/` | 跳转中/英文首页 |
| `http://域名/html/web/cn/index.html` | 中文首页 |
| `http://域名/html/web/en/index.html` | 英文首页 |
| `http://域名/ms/login.do` | 后台登录页 |
| `http://域名/mcms/search.do?tmpl=cn/search.htm&content_title=ELISA` | 中文搜索结果 |

---

## 九、安全加固清单

- [x] swagger 已关闭（application-prod.yml 中 `springdoc.*.enabled=false`，官方提示生产务必关闭）
- [x] 后台验证码已开启（`ms.manager.check-code=true`）
- [x] 8080 端口不对公网开放，仅 Nginx 本机反代（firewalld 未放行 8080）
- [x] MySQL 仅监听 localhost，应用使用独立账号（非 root）
- [x] 生产数据库密码为强密码，与开发环境不同
- [ ] 可选：修改后台路径 `ms.manager.path`
- [ ] 可选：限制 SSH 来源 IP、配置 fail2ban

---

## 十、日常运维与更新流程

```bash
# 服务管理（手动脚本）
/opt/mcms/mcms.sh start      # 启动
/opt/mcms/mcms.sh stop       # 停止
/opt/mcms/mcms.sh restart    # 重启
/opt/mcms/mcms.sh status     # 查看运行状态
/opt/mcms/mcms.sh log        # 实时查看标准输出日志

# 业务日志（log4j 滚动，保留 90 天/约三个月）
tail -f /opt/mcms/log/mcms.log     # 全量 INFO+
tail -f /opt/mcms/log/error.log    # 仅 ERROR
tail -f /opt/mcms/log/stdout.log   # 启动控制台输出（排查启动失败先看这个）
```

> **注意**：手动脚本方式**服务器重启后应用不会自动拉起**，需登录服务器手动执行 `/opt/mcms/mcms.sh start`。MySQL、Nginx 仍建议 `systemctl enable` 开机自启。

**更新发布流程**（改代码/模板后）：

```bash
# 1. 本地打包
mvn clean package -DskipTests
# 2. 替换 jar（模板类改动则同步 template/）
scp target/ms-mcms.jar root@服务器IP:/opt/mcms/
rsync -avz src/main/webapp/template/ root@服务器IP:/opt/mcms/template/
# 3. 重启
ssh root@服务器IP "/opt/mcms/mcms.sh restart"
# 4. 后台重新静态化（主页/栏目/文章），并把 html/、upload/ 保持同步
```

> 注意：`html/`（生成的静态页）和 `upload/`（上传资源）是两套独立产物，**迁移或换服务器时必须两套都带走**，否则出现页面在、图片 404 的问题。

---

## 十一、常见问题

| 现象 | 原因与处理 |
|---|---|
| 前端页面 404 | 未执行静态化，或 `html/` 目录未同步/被清空 → 后台重新生成主页、栏目、文章 |
| 启动报 Unknown table / 表不存在 | `lower_case_table_names` 不是 1 → 必须在 MySQL 数据目录初始化前配置好并重新初始化 |
| 页面能开但图片 404 | `upload/` 未同步；或 Nginx 缺少 `/upload/` 的 location 配置 |
| 中文乱码 | 数据库非 utf8mb4，或 JDBC 串缺 `characterEncoding=utf-8` |
| 上传大文件报 413 | Nginx `client_max_body_size` 太小，调大后 reload |
| 留言 IP 显示为内网/代理 IP | Nginx 未传 `X-Real-IP` / `X-Forwarded-For` 头（本系统取 IP 优先级：X-Real-IP > X-Forwarded-For 首个 > remoteAddr） |
| 时间差 8 小时 | 服务器时区/JVM 时区未设为 Asia/Shanghai（服务已加 `-Duser.timezone=Asia/Shanghai`） |

---

## 附一：官方 Docker 部署方式（官方推荐，可作为替代方案）

官方提供一体化镜像（含 MySQL），适合快速起环境：

```bash
docker run -p 3316:3306 -p 8181:8080 --name mcms --privileged=true \
  -e TZ=Asia/Shanghai --restart=always \
  -e MYSQL_ROOT_PASSWORD=123456 -d docker.1ms.run/mingsoft/mcms:6.0.0 \
  --lower-case-table-names=1 --max-connections=1000

# 容器启动成功后执行启动脚本
docker exec mcms /home/start.sh

# 后台：http://localhost:8181/ms/login.do
```

生产使用建议将 `/home/mcms` 挂载到宿主机目录，便于更新系统文件与持久化。本项目已做源码合并与商业化改造，**建议按本文主体流程（jar + 手动启停脚本 + Nginx）部署**，避免官方镜像版本（6.0.0）与本项目（6.2.1 改造版）不一致。

## 附二：官方参考链接

- 部署手册：https://doc.mingsoft.net/server/
- MCMS 使用手册：https://doc.mingsoft.net/mcms/
- Gitee 源码仓库：https://gitee.com/mingSoft/MCMS
- 版本说明：https://www.mingsoft.net/banben.html
