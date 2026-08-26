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

# 小内存服务器（2G）加 2G swap 兜底，防瞬时尖峰直接 OOM 杀进程
# （云服务器若用云盘可选关机直接扩容内存代替；已有 swap 可跳过）
swapon --show    # 若无输出则执行下面命令
dd if=/dev/zero of=/swapfile bs=1M count=2048 status=progress
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo '/swapfile swap swap defaults 0 0' >> /etc/fstab
# 降低 swap 使用倾向（内存充足时尽量不用 swap）
echo 'vm.swappiness=10' >> /etc/sysctl.conf
sysctl -p
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

### 4.1 核心陷阱：lower_case_table_names 必须在初始化前配置

铭飞官方明确要求 MySQL 开启忽略表名大小写（Windows 默认开启，Linux 默认关闭），否则应用查询表时会因大小写不一致报"表不存在"。

**MySQL 8 的坑**：该参数在数据目录初始化时写进数据字典，**初始化之后改配置会导致 MySQL 直接启动失败**，典型报错：

```
[ERROR] [MY-011087] Different lower_case_table_names settings for server ('1') and data dictionary ('0').
```

所以正确的顺序是：**装包（不启动）→ 写配置 → 首次启动（此时才初始化数据目录）**。
`yum install` 本身不会初始化数据目录，只要中间没有执行过 `systemctl start mysqld` 就安全。

**如果 mysqld 已经被启动过（数据目录已初始化）**，只能清空数据目录重来：

```bash
systemctl stop mysqld
rm -rf /var/lib/mysql/*        # 危险操作：清空全部数据库数据，仅在全新安装阶段允许执行！
systemctl start mysqld         # 带上正确配置重新初始化
```

### 4.2 安装（CentOS Stream 8/9 方式）

```bash
# 1. 安装（不会自动启动，安全）
yum install -y mysql-server

# 2. 确认装的是 MySQL 8 而不是 MariaDB（CentOS 默认模块可能拉 MariaDB）
rpm -qa | grep -Ei 'mysql|mariadb'
mysqld --version               # 应显示 Ver 8.0.x；若显示 MariaDB 说明装错了

# 3. 启动前写配置（重点！顺序不能反）
#    注: innodb_buffer_pool_size / max_connections 为小内存机器（2核2G）约束值；
#    4G 以上机器可放宽为 innodb_buffer_pool_size=512M~1G、max_connections=500
cat >> /etc/my.cnf <<'EOF'
[mysqld]
lower_case_table_names=1
character-set-server=utf8mb4
collation-server=utf8mb4_general_ci
default-time-zone='+8:00'
innodb_buffer_pool_size=256M
max_connections=200
EOF

# 4. 首次启动（此时完成数据目录初始化，lower_case_table_names=1 生效）
systemctl enable --now mysqld

# 5. 确认启动成功、参数正确（任何一步不对都回头按 4.1 的恢复流程处理）
mysql -uroot -e "SHOW VARIABLES LIKE 'lower_case_table_names';"   # 必须为 1（AppStream 版 root 初始为空密码）
mysql -uroot -e "SHOW VARIABLES LIKE 'character%';"              # utf8mb4

# 6. 安全初始化（设置 root 密码、删测试库、禁远程 root）
mysql_secure_installation
```

> AppStream 的 mysql-server 首次启动后 root 为**空密码**，直接回车即可登录；`mysql_secure_installation` 中按提示设置 root 强密码。

### 4.3 安装（CentOS 7 方式）

CentOS 7 已于 2024-06 停止维护，且默认源里没有 MySQL（是 MariaDB），需要处理两个前置问题：

```bash
# 0a. 官方镜像已失效，yum 报 mirrorlist 错误时，先切到 vault 归档源
sed -i -e 's|^mirrorlist=|#mirrorlist=|' \
       -e 's|^#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|' \
       /etc/yum.repos.d/CentOS-*.repo

# 0b. 安装 MySQL 官方 yum 源（8.0 社区版）
yum install -y https://repo.mysql.com/mysql80-community-release-el7-11.noarch.rpm
# GPG 校验失败时导入新密钥（2023 年官方换过密钥）
rpm --import https://repo.mysql.com/RPM-GPG-KEY-mysql-2023

# 1. 安装社区版服务端
yum install -y mysql-community-server

# 2. 同样：启动前先写配置（内容同 4.2 第 3 步）
# 3. 首次启动
systemctl enable --now mysqld
```

**社区版的坑：root 是随机临时密码**（不是空密码），必须先从日志取出来：

```bash
# 取临时密码
grep 'temporary password' /var/log/mysqld.log
# 形如：A temporary password is generated for root@localhost: xxxxxx

# 用临时密码登录后必须先改密码，否则任何 SQL 都会被拒绝
mysql -uroot -p
ALTER USER 'root'@'localhost' IDENTIFIED BY '新的强密码';
# 注意：社区版默认启用 validate_password 组件，密码必须包含大小写、数字、特殊字符
```

之后同样执行 `mysql_secure_installation` 和参数验证（同 4.2 第 5、6 步）。

### 4.4 建库、建账号、导入数据

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
# JVM 内存: 2G 内存服务器用 512m（防 OOM）; 4G 及以上可放宽到 -Xms1g -Xmx2g
JAVA_OPTS="-Xms256m -Xmx512m -XX:MaxMetaspaceSize=192m -Duser.timezone=Asia/Shanghai"

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

### 6.1 小内存服务器（2核2G）内存控制要点

2G 内存同机部署（系统+MySQL+Java+Nginx）属于压线运行，以下三项已在文档各章节落实，此处汇总便于检查：

| 组件 | 控制项 | 值 | 位置 |
|---|---|---|---|
| Java | 堆内存 `-Xmx512m`，元空间上限 192m | 进程总内存约 700~800MB | mcms.sh 的 `JAVA_OPTS` |
| MySQL | `innodb_buffer_pool_size=256M`、`max_connections=200` | 进程总内存约 400~500MB | /etc/my.cnf |
| 系统 | 2G swap + `vm.swappiness=10` | 瞬时尖峰兜底 | 第二章基础准备 |

**验证内存水位（部署完成后执行）：**

```bash
# 总览：available 长期低于 200MB 就要警惕
free -h

# 各进程实际占用（Java 进程 RSS 应在 800MB 以内，MySQL 500MB 以内）
ps aux --sort=-%mem | head -6

# swap 使用量（USED 持续增长说明内存不足，偶尔几十 MB 属正常）
free -h | grep -i swap
```

**内存不足的典型征兆与处理：**

| 征兆 | 原因 | 处理 |
|---|---|---|
| 后台批量静态化时卡死/进程消失 | JVM OOM（2000+ 页面生成内存尖峰） | 分批静态化（按栏目逐个生成）；仍不行则升配 4G |
| 站点整体变慢、磁盘 IO 高 | 开始频繁用 swap | 检查 `free -h`，考虑升配 |
| MySQL 自动重启 | 被 OOM killer 杀掉 | `dmesg | grep -i kill` 确认，降低 buffer pool 或升配 |

> 日常对外是纯静态站（Nginx 直接吐 HTML），2核2G 扛几千日 PV 无压力；瓶颈只出现在后台批量静态化和 MySQL 并发时刻。升配 4G 后，把 mcms.sh 的 JAVA_OPTS 放宽到 `-Xms1g -Xmx2g`、my.cnf 的 buffer pool 调到 512M~1G 即可。

---

## 七、安装配置 Nginx（动静分离 + 反向代理）

```bash
yum install -y nginx
```

创建 `/etc/nginx/conf.d/mcms.conf`：

```nginx
# 按浏览器 Accept-Language 头映射语言目录：zh 开头 → cn，其余 → en
map $http_accept_language $lang {
    default en;
    ~*^zh     cn;
}

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

    # 网站根路径：302 按浏览器语言跳中/英文首页（不依赖物理跳转页，
    # 后台重新生成静态页清空 html 目录也不受影响）
    location = / {
        return 302 /html/web/$lang/index.html;
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
