<!DOCTYPE html>
<html>
<head>
    <title>自定义表单</title>
    <#include "../../include/head-file.ftl">
    <script src="${base}/static/mdiy/index.js"></script>
</head>
<body>
<div id="form" v-cloak>
    <el-header class="ms-header ms-tr" height="50px">
        <el-button v-if="(id==null && (ms.util.includes(ms.managerPermissions,'mdiy:formData:save')||ms.util.includes(ms.managerPermissions,'mdiy:formData:'+modelId+':save')))
        ||(id && (ms.util.includes(ms.managerPermissions,'mdiy:formData:update')||ms.util.includes(ms.managerPermissions,'mdiy:formData:'+modelId+':update')))"
                   type="primary" class="iconfont icon-baocun" size="default" @click="save()" :loading="saveDisabled">保存</el-button>
        <el-button size="default"  class="iconfont icon-fanhui" plain @click="back()">返回</el-button>
    </el-header>
    <el-main class="ms-container" v-loading="loading">
        <el-scrollbar class="ms-scrollbar" style="height: 100%;">
        <ms-mdiy-form ref="modelForm" type="form" :model-id="modelId" :id="id"></ms-mdiy-form>
        </el-scrollbar>
    </el-main>
</div>
</body>
</html>
<script>
    var form = new _Vue({
        el: '#form',
        data: function (){
            return {
                loading:false,
                saveDisabled: false,
                modelName:'',
                modelId:'',
                id:'',
                isEditor:false,
            }
        },
        methods: {
            back: function (){
                ms.util.openSystemUrl("/mdiy/form/data/index.do?modelName="+this.modelName+"&modelId="+this.modelId+"&isEditor="+this.isEditor,true)
            },
            save: function () {
                var that = this;
                that.saveDisabled = true;
                this.$refs.modelForm.getForm().save(function (res) {
                    if(res.result){
                        that.$notify({
                            title: '成功',
                            type: 'success',
                            message: '保存成功!'
                        });
                        ms.util.openSystemUrl("/mdiy/form/data/index.do?modelName="+that.modelName+"&modelId="+that.modelId+"&isEditor="+that.isEditor,false)
                    }else if (res.code == 'PARAMERR'){  // FormDataAction.PARAM_ERR
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
        created:function() {
			this.modelId = ms.util.getParameter("modelId");
            this.id = ms.util.getParameter("id");
            this.modelName = ms.util.getParameter("modelName");
            this.isEditor = ms.util.getParameter("isEditor")==null?false:JSON.parse(ms.util.getParameter("isEditor"));
        }
    });
</script>
