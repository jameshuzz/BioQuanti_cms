#!/bin/bash
export JAVA_HOME=/opt/jdk17
export PATH=$JAVA_HOME/bin:$PATH
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
JAVA_OPTS="-Xms512m -Xmx1024m -XX:MaxMetaspaceSize=256m -Duser.timezone=Asia/Shanghai"

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
    echo "[$APP_NAME] 正在停止 (PID: $pid)..."
    kill "$pid" 2>/dev/null
    local i=0
    while kill -0 "$pid" 2>/dev/null && [ $i -lt 30 ]; do
        sleep 1; i=$((i+1))
    done
    if kill -0 "$pid" 2>/dev/null; then
        echo "[$APP_NAME] 优雅停机超时，强制结束"
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
        ps -o pid,vsz,rss,%cpu,etime -p "$pid" --no-headers
    else
        echo "[$APP_NAME] 未运行"
    fi
}

log() {
    tail -f "$LOG_FILE"
}

case "$1" in
    start)   start ;;
    stop)    stop ;;
    restart) stop; sleep 2; start ;;
    status)  status ;;
    log)     log ;;
    *) echo "用法: $0 {start|stop|restart|status|log}"; exit 1 ;;
esac
