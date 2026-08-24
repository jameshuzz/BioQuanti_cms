
<html>
<head>
    <title>系统错误</title>
</head>
<style>
    .u_button{
        display: inline-block;
        padding: 0 20px;
        height: 30px;
        border-radius: 3px;
        -webkit-border-radius: 3px;
        -moz-border-radius: 3px;
        border: none;
        vertical-align: middle;
        text-align: center;
        text-decoration: none;
        font-size: 12px;
        line-height: 30px;
        cursor: pointer;
    }
    .u_button_gray {
        background-color: #BDC3C7;
    }
    a.u_button_gray {
        color: #FFF;
    }
    .u_button_blue {
        background-color: #3498DB;
        color: #FFF;
    }
</style>
<body style="background-color: #E4E7EA;padding-top:5%">
<center>
    <#-- onerror可自定义 避免ip拦截器死循环 -->
    <img src="${base}/static/images/${code}.png" onerror=""/>
    <div>
        <h3 style="  clear: both;  line-height: 40px;  font-size: 40px;  color: #333;">系统错误</h3>
        <p style="  clear: both;  margin: 30px auto 20px auto;  line-height: 35px;  font-size: 20px;  color: #999;">提示：${msg?html}</p>
        <a href="javascript:location.reload();" class="u_button u_button_gray">刷新该页</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;or&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a class="u_button u_button_blue" href="/">返回首页</a>
    </div>
</center>
</body>
</html>
