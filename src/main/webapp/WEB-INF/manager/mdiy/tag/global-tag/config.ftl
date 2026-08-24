<!DOCTYPE html>
<html>
<head>
    <title>自定义全局标签配置</title>
    <#include "../../include/head-file.ftl">
    <script src="${base}/static/mdiy/index.js"></script>
</head>
<body>
<div id="form" v-cloak class="ms-index">
    <el-header class="ms-header ms-tr" height="50px">
        <@shiro.hasPermission name="mdiy:form:importJson">
            <el-button type="primary" class="iconfont icon-daoru" size="default" @click="dialogImportVisible=true;impForm.id=modelId;impForm.isWebSubmit=false" style="float: right">导入</el-button>
        </@shiro.hasPermission>
        <el-button type="primary" class="iconfont icon-baocun" size="default" @click="save()" :loading="saveDisabled">保存</el-button>
    </el-header>
    <el-dialog title="导入自定义全局标签配置json" v-model="dialogImportVisible" width="600px"  :close-on-press-escape="false" :close-on-click-modal="false" :append-to-body="true" v-cloak>
        <el-scrollbar class="ms-scrollbar" style="height: 100%;">
            <el-form ref="jsonForm" :model="impForm" :rules="rules" size="default" label-width="130px" label-position="top" >
                <el-form-item prop="modelJson">
                    <template #label>
                        <span>自定义全局标签json</span>
                    </template>
                    <el-input :rows="10" type="textarea" v-model="impForm.modelJson"  placeholder="请粘贴来自代码生器中自定义模型的配置json"></el-input>
                    <div class="ms-form-tip">
                        将自定义模型的配置json粘贴到当前文本框 <br/>
                        <b>注意:</b> 更新配置模型时会删除原有录入的数据，所以更新模型时谨慎操作。
                    </div>
                </el-form-item>
            </el-form>

        </el-scrollbar>
        <template #footer>
            <el-button size="default" @click="dialogImportVisible = false">取 消</el-button>
            <el-button size="default" type="primary" @click="importJson()">确 定</el-button>
        </template>
    </el-dialog>
    <el-main class="ms-container" v-loading="loading">
        <div>
            <el-alert
                    class="ms-alert-tip"
                    title="功能介绍"
                    type="info">
                <template #title>
                    功能介绍
                </template>
                <template #default>
                    全局标签组成：{ms:global.模型名称.字段名称}
                </template>
            </el-alert>
        </div>
        <ms-mdiy-form ref="modelForm" type="tag" :model-id="modelId" :id="id"></ms-mdiy-form>

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
                dialogImportVisible:false,
                impForm: {
                    //模型编号
                    id:'',
                    // 是否允许前端获取数据，避免新增bean的字段，沿用isWebSubmit
                    isWebSubmit:false,
                    // 模型名称
                    modelJson: '',
                    // 标签Id
                    tagId: ''
                },
                //配置验证
                rules: {
                    modelJson: [{
                        required: true,
                        message: 'json数据不能为空',
                        trigger: 'blur'
                    }]
                }
            }
        },
        watch: {
            'dialogImportVisible': function (n, o) {
                if (!n) {
                    this.$refs.jsonForm.resetFields();
                    this.form.id = "";
                }
            },
        },
        methods: {
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
            importJson: function(){
                var that = this;
                var url = "/mdiy/tag/globalTag/importJson.do";
                if(that.impForm.id &&  that.impForm.id!=''){
                    url = "/mdiy/tag/globalTag/updateJson.do";
                }

                that.$confirm('此操作会进行更新表操作，请谨慎操作', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(function () {
                    // 控制前端接口isWebSubmit，保存至json
                    that.$refs.jsonForm.validate(function (valid) {
                        var object = {};
                        try {
                            object = JSON.parse(that.impForm.modelJson);
                            object.isWebSubmit = that.impForm.isWebSubmit;
                            that.impForm.modelJson = JSON.stringify(object);
                        }catch(e) {
                            that.$notify({
                                title: '失败',
                                message: "json格式不匹配，请粘贴正确的自定义模型JSON代码",
                                type: 'warning'
                            });
                            return;
                        };
                        if (valid) {
                            ms.http.post(ms.manager + url, that.impForm).then(function (data) {
                                if (data.result) {
                                    that.form = {};
                                    that.dialogImportVisible=false;
                                    var msg = "更新模型成功";
                                    if(!(that.impForm.id &&  that.impForm.id!='')){
                                        msg = "导入模型成功"
                                    };
                                    that.$notify({
                                        title: '成功',
                                        message: msg,
                                        type: 'success'
                                    });
                                    location.reload();
                                } else {
                                    that.$notify({
                                        title: '失败',
                                        message: data.msg,
                                        type: 'warning'
                                    });
                                }
                            })
                        }
                    });
                });
            },
        },
        created: function () {
            this.form.id = ms.util.getParameter("id");
            //点击菜单为配置编辑模式，不显示返回按钮
            this.isEditor = ms.util.getParameter("isEditor")==null?false:JSON.parse(ms.util.getParameter("isEditor"));
            this.id = ms.util.getParameter("id");
            this.modelName = ms.util.getParameter("modelName");
            this.modelId = ms.util.getParameter("modelId");
        }
    });
</script>
