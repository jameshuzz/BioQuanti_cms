<html>
<head>
    <meta charset="utf-8" />
    <title>MCms</title>
    <#include "/include/head-file.ftl"/>
    <link rel="stylesheet" type="text/css" href="${base}/static/plugins/TextInputEffects/css/normalize.css" />
    <link rel="stylesheet" type="text/css" href="${base}/static/plugins/TextInputEffects/css/set1.css" />
    <style>
        [v-cloak]{
            display: none;
        }
    </style>
</head>
<body class="custom-body">
<div id="app" v-cloak>
    <!--大容器开始-->
    <div  class="class-1" @keydown.13='login'>
<#--        <img src="${base}/static/images/yanhua.png" style="position: absolute;"/>-->

        <!--大容器开始-->
        <div  class="class-2" style="position: absolute">
            <!--大容器开始-->
            <div  class="class-3" style="position: relative">
                <!--图片开始-->
                <img src="${base}/static/images/login-banner.png" class="class-4" />
                <!--图片结束-->
            </div>
            <!--大容器结束-->
            <!--大容器开始-->
            <div  class="class-5">
                <!--小容器开始-->
                <div  class="class-6" >
                    <!--文本开始-->
                    <div class="class-7" style="position: relative">
                        <div>登录</div>
                    </div>
                    <!--文本结束-->
                    <!--小容器开始-->
                    <el-form :model="form" ref="form" :rules="rules" @submit.native.prevent>
                        <div  class="class-8" >
                            <el-form-item prop="managerName">
                                <span class="input input--hoshi">
                                    <input v-model="form.managerName" class="input__field input__field--hoshi" type="text"  id="input-name" />
                                    <label class="input__label input__label--hoshi input__label--hoshi-color-1" for="input-name">
                                        <span class="input__label-content input__label-content--hoshi">账号</span>
                                    </label>
                                </span>
                            </el-form-item>
                        </div>
                        <!--小容器结束-->
                        <!--小容器开始-->
                        <div  class="class-13" >
                            <!--文本开始-->
                            <el-form-item prop="managerPassword">
                                <span class="input input--hoshi">
                                    <input v-model="form.managerPassword" class="input__field input__field--hoshi" type="password" id="input-password" autocomplete="off" />
                                    <label class="input__label input__label--hoshi input__label--hoshi-color-1" for="input-password">
                                        <span class="input__label-content input__label-content--hoshi">密码</span>
                                    </label>
                                </span>
                            </el-form-item>
                        </div>
                        <!--小容器结束-->
                        <!--小容器开始-->
                        <div  class="class-16" >
                            <!--小容器开始-->
                            <div  class="class-17" >
                                <!--文本开始-->
                                <el-form-item prop="rand_code">
                                    <span class="input input--hoshi">
                                        <input v-model="form.rand_code" class="input__field input__field--hoshi" type="text" link id="input-rand-code" />
                                        <label class="input__label input__label--hoshi input__label--hoshi-color-1" for="input-rand-code">
                                            <span class="input__label-content input__label-content--hoshi">验证码</span>
                                        </label>
                                    </span>
                                </el-form-item>
                            </div>
                            <!--小容器结束-->
                            <!--大容器开始-->
                            <div class="class-20" >
                                <img :src="ms.base+verifyCode" class="code-img" @click="code" />
                            </div>
                            <!--大容器结束-->
                            <!--小容器开始-->
                            <div  class="class-21" >
                                <!--小容器开始-->
                                <div @click="code"
                                     class="class-22" >
                                    <!--文本开始-->
                                    <div class="class-23">

                                        看不清？

                                    </div>
                                    <!--文本结束-->
                                    <!--文本开始-->
                                    <div class="class-24">

                                        换一张

                                    </div>
                                    <!--文本结束-->
                                </div>
                                <!--小容器结束-->
                            </div>
                            <!--小容器结束-->
                        </div>
                        <!--小容器结束-->

                        <!--小容器开始-->
                        <div  class="class-25" >
                        </div>
                        <!--小容器结束-->
                        <!--按钮开始-->
                        <el-button @click="login" type="primary" :loading="loading" native-type="submit"
                                   class="class-26">
                            {{loading?'登录中':'登录'}}
                        </el-button>
                        <!--按钮结束-->
                        <!--小容器开始-->
                        <div  class="class-27" >
                            <el-checkbox v-model="form.rememberMe">记住我</el-checkbox>
                            <!--文本结束-->
                        </div>
                    </el-form>
                    <!--小容器结束-->
                </div>
                <!--小容器结束-->
            </div>
            <!--大容器结束-->
        </div>
        <!--大容器结束-->
    </div>
    <!--大容器结束-->
</div>
</body>
</html>
<script src="${base}/static/plugins/TextInputEffects/js/classie.js"></script>
<script>
    var app = new _Vue({
        el: '#app',
        watch:{
            /**
             * 监听 app 数据。确保在站点初始化请求完成后再获取验证码。
             * 解决竞态条件 (Race Condition)问题：
             * 若 HTML 自动加载图片，会与初始化 API 同时发起无 Cookie 请求。
             * 后端会因此并行生成两个独立 Session，导致最终浏览器存储的 SessionID 与验证码存储的不一致。
             */
            'app': function (val, oldVal) {
                this.code();
            }
        },
        data: function () {
            return {
                base: ms.base,
                loading: false,
                app: {
                    appTitle: '后台管理系统',
                    appLogo: '',
                    appCopyright: '',
                    appUrl: '',
                },
                form: {
                    managerName: '',
                    managerPassword: '',
                    rand_code: '',
                    rememberMe: true,
                },
                rules: {
                    managerName: [
                        {required: true, message: '请输入账号', trigger: 'blur'},
                        {min: 1, max: 30, message: '长度不能超过30个字符', trigger: 'change'}
                    ],
                    managerPassword: [
                        {required: true, message: '请输入密码', trigger: 'blur'},
                        {min: 1, max: 30, message: '长度不能超过30个字符', trigger: 'change'}
                    ],
                    rand_code: [
                        {required: true, message: '请输入验证码', trigger: 'blur'},
                        {min: 1, max: 4, message: '长度不能超过4个字符', trigger: 'change'}
                    ],
                },
                verifyCode: ""
            }
        },
        methods: {
            //登录输入框下边框动画
            loginIptAnimation:function() {
                this.$nextTick(function () {
                    const label = document.querySelectorAll('.input__label-content--hoshi')
                    const ipt = document.querySelectorAll('.input__field--hoshi')
                    ipt.forEach((item, index) => {
                        if (!!item.value) {
                            label[index].style.transform = 'translate3d(0, -40%, 0)'
                        } else {
                            label[index].style.transform = 'translate3d(0, 0, 0)'
                        }
                    })
                })
            },
            //登录
            login:function () {
                var that = this;
                that.$refs.form.validate(function(valid){
                    if (valid) {
                        that.loading = true;
                        ms.http.post(ms.manager + "/login.do", {
                            managerName:that.form.managerName,
                            managerPassword:that.form.managerPassword,
                            rand_code:that.form.rand_code,
                            rememberMe:that.form.rememberMe
                        }).then(function (res) {
                            if(res.result){
                                if(that.form.rememberMe) {
                                    localStorage.setItem('managerName',that.form.managerName);
                                    localStorage.setItem('managerPassword',that.form.managerPassword);
                                } else {
                                    localStorage.setItem('managerName','');
                                    localStorage.setItem('managerPassword','');
                                }
                                localStorage.setItem('rememberMe',that.form.rememberMe);
                                ms.util.openSystemUrl("/index.do");
                                that.loading = false;
                            }else {
                                that.$notify({
                                    title: '失败',
                                    message: res.msg,
                                    type: 'error'
                                });
                                that.code();
                                that.loading = false;
                            }
                        }).catch(function () {
                            that.code();
                            that.loading = false;
                        });
                    }
                });
            },
            //获取验证码
            code:function(){
                this.verifyCode = "/code.do?t=" + new Date().getTime();
            },
            //初始
            initial:function(){
                this.form.managerName = localStorage.getItem('managerName');
                this.form.managerPassword =  localStorage.getItem('managerPassword');
                if(localStorage.getItem('rememberMe')==='true') {
                    this.form.rememberMe = true  ;
                } else {
                    this.form.rememberMe = false ;
                }

                top.location != self.location?(top.location = self.location):'';
            },
            get: function (){
                var that = this;
                ms.http.get(ms.base + "/basic/app/get.do").then(function (res) {
                    if (res.result) {
                        that.app = res.data;
                        document.title = that.app.name;
                    }
                })
            }
        },
        created:function(){
            this.get();
            this.initial();
        }
    })
</script>


<style>
    .el-form-item{
        margin-bottom: 0px;
    }
    .el-form-item__content{
        line-height: initial;
    }
    .custom-body {
    }
    .class-1
    {
        color:#333333;
        background-image:url(${base}/static/images/login-bg.jpg);
        outline:none;
        outline-offset:-1px;
        background-size:cover;
        background-position:center;
        height:100%;
        max-width:100%;
        align-items:center;
        flex-direction:row;
        display:flex;
        justify-content:center;
        animation-duration:1s;
        width:100%;
        background-repeat:no-repeat;
    }
    .class-2
    {
        box-shadow:0 2px 12px 0 rgba(0, 0, 0, 0.1);
        color:#333333;
        outline:none;
        outline-offset:-1px;
        height:540px;
        max-width:100%;
        background-color:rgba(255, 255, 255, 1);
        flex-direction:row;
        display:flex;
        animation-duration:1s;
        border-radius:12px;
        width:1000px;
        background-repeat:no-repeat;
    }
    .class-3
    {
        color:#333333;
        outline:none;
        outline-offset:-1px;
        height:100%;
        max-width:100%;
        align-items:flex-start;
        flex-direction:row;
        display:flex;
        justify-content:flex-start;
        animation-duration:1s;
        width:460px;
        background-repeat:no-repeat;
    }
    .class-4
    {
        height:100%;
        animation-duration:1s;
        width:100%;
    }
    .class-5
    {
        color:#333333;
        outline:none;
        padding-bottom:20px;
        outline-offset:-1px;
        flex:1;
        padding-top:20px;
        height:100%;
        max-width:100%;
        align-items:center;
        flex-direction:column;
        display:flex;
        justify-content:flex-start;
        animation-duration:1s;
        width:200px;
        background-repeat:no-repeat;
    }
    .class-6
    {
        color:#333333;
        outline:none;
        outline-offset:-1px;
        max-width:100%;
        flex-direction:column;
        display:flex;
        animation-duration:1s;
        width:330px;
        background-repeat:no-repeat;
        margin-top:20px;
    }
    .class-7
    {
        color:#333333;
        word-wrap:break-word;
        display:inline-block;
        animation-duration:1s;
        font-size:36px;
        line-height:1.4;
        margin-bottom:20px;
    }
    .class-8
    {
        color:#333333;
        outline:none;
        outline-offset:-1px;
        height:80px;
        max-width:100%;
        flex-direction:column;
        display:flex;
        justify-content:flex-end;
        animation-duration:1s;
        width:100%;
        background-repeat:no-repeat;
    }
    .class-9
    {
        color:#BBBBBB;
        word-wrap:break-word;
        display:inline-block;
        animation-duration:1s;
        font-size:12px;
        line-height:1.4;
    }
    .class-10
    {
        color:#333333;
        outline:none;
        outline-offset:-1px;
        height:40px;
        max-width:100%;
        align-items:center;
        flex-direction:row;
        display:flex;
        animation-duration:1s;
        width:100%;
        background-repeat:no-repeat;
    }
    .class-11
    {
        color:#333333;
        word-wrap:break-word;
        display:inline-block;
        animation-duration:1s;
        font-size:14px;
        line-height:1.4;
    }
    .class-12
    {
        margin-right:auto;
        animation-duration:1s;
        background-color:#eee;
        border-radius:1px;
        width:100%;
        height:1px;
        margin-left:auto;
    }
    .class-13
    {
        color:#333333;
        outline:none;
        outline-offset:-1px;
        height:80px;
        max-width:100%;
        flex-direction:column;
        display:flex;
        justify-content:flex-end;
        animation-duration:1s;
        width:100%;
        background-repeat:no-repeat;
    }
    .class-14
    {
        color:#BBBBBB;
        word-wrap:break-word;
        padding-bottom:10px;
        display:inline-block;
        animation-duration:1s;
        font-size:14px;
        line-height:1.4;
    }
    .class-15
    {
        margin-right:auto;
        animation-duration:1s;
        background-color:#eee;
        border-radius:1px;
        width:100%;
        height:1px;
        margin-left:auto;
    }
    .class-16
    {
        color:#333333;
        outline:none;
        outline-offset:-1px;
        height:80px;
        max-width:100%;
        align-items:flex-end;
        flex-direction:row;
        display:flex;
        justify-content:flex-start;
        animation-duration:1s;
        background-repeat:no-repeat;
    }
    .class-17
    {
        color:#333333;
        outline:none;
        outline-offset:-1px;
        flex:1;
        height:80px;
        max-width:100%;
        flex-direction:column;
        display:flex;
        justify-content:flex-end;
        animation-duration:1s;
        width:200px;
        background-repeat:no-repeat;
    }
    .class-18
    {
        color:#BBBBBB;
        word-wrap:break-word;
        display:inline-block;
        animation-duration:1s;
        font-size:14px;
        line-height:1.4;
        margin-bottom:10px;
    }
    .class-19
    {
        margin-right:auto;
        animation-duration:1s;
        background-color:#eee;
        border-radius:1px;
        width:100%;
        height:1px;
        margin-left:auto;
    }
    .class-20
    {
        cursor:pointer;
        color:#333333;
        margin-right:10px;
        outline-offset:-1px;
        height:40px;
        max-width:100%;
        align-items:center;
        flex-direction:row;
        display:flex;
        justify-content:center;
        margin-left:10px;
        animation-duration:1s;
        width:88px;
        background-repeat:no-repeat;
        margin-bottom: 0.85em;
    }
    .class-21
    {
        color:#333333;
        outline:none;
        outline-offset:-1px;
        max-width:100%;
        align-items:flex-end;
        flex-direction:column;
        display:flex;
        justify-content:flex-end;
        animation-duration:1s;
        background-repeat:no-repeat;
        margin-bottom: 0.85em;
    }
    .class-22
    {
        color:#333333;
        outline:none;
        outline-offset:-1px;
        max-width:100%;
        flex-direction:column;
        display:flex;
        animation-duration:1s;
        background-repeat:no-repeat;
    }
    .class-23
    {
        color:#BBBBBB;
        word-wrap:break-word;
        display:inline-block;
        animation-duration:1s;
        font-size:12px;
        line-height:1.4;
    }
    .class-24
    {
        cursor:pointer;
        color:#0099FF;
        word-wrap:break-word;
        display:inline-block;
        animation-duration:1s;
        font-size:12px;
        line-height:1.4;
    }
    .class-25
    {
        color:#333333;
        outline:none;
        outline-offset:-1px;
        height:40px;
        max-width:100%;
        flex-direction:row;
        display:flex;
        animation-duration:1s;
        width:100px;
        background-repeat:no-repeat;
    }
    .class-26
    {
        background-color:#0099ff;
        width: 100%;
        height: 40px;
        line-height: 40px;
    }
    .class-27
    {
        color:#333333;
        outline:none;
        outline-offset:-1px;
        max-width:100%;
        align-items:center;
        flex-direction:row;
        display:flex;
        animation-duration:1s;
        width:100px;
        background-repeat:no-repeat;
        margin-top:20px;
    }
    .class-28
    {
        color:#333333;
        outline:1px dashed hsla(0, 0%, 66.7%, 0.7);
        outline-offset:-1px;
        height:14px;
        max-width:100%;
        flex-direction:row;
        display:flex;
        animation-duration:1s;
        width:14px;
        background-repeat:no-repeat;
    }
    .class-29
    {
        color:#999999;
        word-wrap:break-word;
        display:inline-block;
        margin-left:10px;
        animation-duration:1s;
        font-size:14px;
        line-height:1.4;
    }
    @media (max-width: 768px){
    }
    .input__label--hoshi::before{
        content: '';
        position: absolute;
        top: 1px;
        left: 0;
        width: 100%;
        height: calc(100% - 10px);
        border-bottom: 1px solid #B9C1CA;
    }
</style>
