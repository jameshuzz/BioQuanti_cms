<!DOCTYPE html>
<html>

<head>
    <title>系统信息</title>
    <#include "../../include/head-file.ftl">
</head>

<body>
<div id="form" v-cloak>
    <el-main class="ms-container" v-loading="loading">
        <el-form ref="form" :model="form" :rules="rules" label-width="120px" label-position="right" size="small">
                        <el-form-item label="系统版本" >
                            {{form['系统名称']}} {{form['系统版本']}}
                        </el-form-item>
                        <el-form-item label="系统构架">
                            {{form['系统构架']}}
                        </el-form-item>
                    <el-form-item label="web容器">
                        {{form['web容器']}}
                    </el-form-item>
                    <el-form-item label="发布路径">
                        {{form['发布路径']}}
                    </el-form-item>
                        <el-form-item label="Java版本" >
                            {{form['Java版本']}}
                        </el-form-item>
                        <el-form-item label="Java安装路径">
                            {{form['Java安装路径']}}
                        </el-form-item>
                        <el-form-item label="数据库信息" >
                            {{form['数据库']}} {{form['数据库版本']}}
                        </el-form-item>
                        <el-form-item label="物理内存">
                            总量{{form['内存总量']}}MB/可用{{form['内存可用']}}MB
                        </el-form-item>

                    <el-form-item label="cpu信息">
                        {{form['cpu信息']}}
                    </el-form-item>
                    <el-form-item label="数据库链接"  style="word-break: break-all">
                        {{form['数据库链接']}}
                    </el-form-item>
                    <el-form-item label="Vue.js"  style="word-break: break-all">
                         {{form.vue}}
                    </el-form-item>
                    <el-form-item label="Element Plus "  style="word-break: break-all">
                        {{form.el}}
                    </el-form-item>

        </el-form>
    </el-main>
</div>
</body>

</html>
<script>
    var form = new _Vue({
        el: '#form',
        data: function() {
            return {
                loading: false,
                saveDisabled: false,
                //表单数据
                form: {},
                rules: {},

            }
        },
        watch: {},
        computed: {},
        methods: {
            //获取当前获取系统配置
            get: function() {
                var that = this;
                this.loading = true
                ms.http.post(ms.manager + "/basic/system/info.do").then(function(res) {
                    that.loading = false
                    if (res.result && res.data) {
                        that.form = res.data;
                    }
                    that.form.vue = Vue.version;
                    that.form.el = ElementPlus.version;
                }).catch(function(err) {
                    console.log(err);
                });
            },
        },
        created: function() {
            var that = this;
            that.get()
        }
    });
</script>
<style>
    #form .ms-container {
        height: 100vh;
    }
</style>