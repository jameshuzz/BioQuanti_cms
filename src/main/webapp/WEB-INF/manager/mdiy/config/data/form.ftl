<!DOCTYPE html>
<html>
<head>
    <title>自定义配置</title>
    <#include "../../include/head-file.ftl">
    <script src="${base}/static/mdiy/index.js"></script>
</head>
<body>
<div id="form" v-cloak>
    <el-header class="ms-header ms-tr" height="50px">
        <el-button v-if="ms.util.includes(ms.managerPermissions,'mdiy:configData:update')||ms.util.includes(ms.managerPermissions,'mdiy:configData:'+modelId+':update')" type="primary" class="iconfont icon-baocun" size="default" @click="save()" :loading="saveDisabled">保存
        </el-button>
        <el-button size="default" v-if="!isEditor"   plain @click="back()"><i class="iconfont icon-fanhui"></i>返回</el-button>
    </el-header>
    <el-main class="ms-container" v-loading="loading">
        <ms-mdiy-form ref="modelForm" type="config" :model-id="modelId" :id="id"></ms-mdiy-form>
    </el-main>
</div>
</body>
</html>
<script>
    var form = new _Vue({
        el: '#form',
        data: function () {
            return {
                loading: false,
                saveDisabled: false,
                //自定义模型实例
                configModel: undefined,
                modelName:'',
                modelId:'',
                id:'',
                //表单数据
                form: {
                    id:''
                },
                isEditor:false,//是否是编辑状态，如果是就不显示返回按钮
            }
        },
        watch:{
            // vue3 脚手架需要切换跳转
            '$route':{
                handler(to) {
                    this.modelId = ms.util.getParameter("modelId");
                    //点击菜单为配置编辑模式，不显示返回按钮
                    this.isEditor = ms.util.getParameter("isEditor")==null?false:JSON.parse(ms.util.getParameter("isEditor"));
                }
            }
        },
        methods: {
            back:function (){
                ms.util.openSystemUrl("/mdiy/config/index.do",true)
            },
            save: function () {
                var that = this;
                        that.saveDisabled = true;
                that.$refs.modelForm.getForm().save(function (res) {
                            if(res.result){
                                that.$notify({
                                    title: '成功',
                                    type: 'success',
                                    message: '保存成功!'
                                });
                            }else {
                                that.$notify({
                                    title: '失败',
                                    message: res.msg,
                                    type: 'warning'
                                });
                            }
                            that.saveDisabled = false;
                        });

            },
        },
        created: function () {
            //点击菜单为配置编辑模式，不显示返回按钮
            this.isEditor = ms.util.getParameter("isEditor")==null?false:JSON.parse(ms.util.getParameter("isEditor"));
            this.modelId = ms.util.getParameter("modelId");
        }
    });
</script>
