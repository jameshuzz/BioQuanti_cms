<template type="text/x-template" id="dialog">
    <el-dialog  :append-to-body="true" id="form" v-model="dialogVisible" :close-on-click-modal="false" width="50%" v-cloak>
        <template #header>
            <i class="el-icon-menu">{{(form.modelIsMenu == 1) ? '菜单编辑' : '按钮编辑'}}</i>
        </template>
        <el-form ref="form" :model="form" :rules="rules" label-width="110px" size="default">
            <el-form-item prop="modelIsMenu">
                <template #label>菜单类型</template>
                <el-radio-group v-model="form.modelIsMenu">
                    <el-radio :value="1">导航链接</el-radio>
                    <el-radio :value="0">功能权限</el-radio>
                </el-radio-group>
                <div class="ms-form-tip">
                    <b>导航连接</b>会显示在菜单中，<b>功能权限</b>用来控制页面中按钮功能权限
                </div>
            </el-form-item>
            <el-form-item  label="父级菜单" prop="modelId">
                <el-tree-select
                        v-model="form.modelId"
                        :data="modelList"
                        :props="{
                            value: 'id',
                            label: 'modelTitle',
                            children: 'modelChildList'
                        }"
                        :render-after-expand="false"
                        :check-strictly=true
                        :default-expand-all=true
                        @node-click="getValue"
                        filterable
                ></el-tree-select>
                <div class="ms-form-tip">
                    不能选择功能菜单类型,不能选择自身为父栏目
                </div>
            </el-form-item>
            <el-form-item  label="标题" prop="modelTitle">
                <el-input v-model="form.modelTitle" placeholder="请输入标题"></el-input>
                <div class="ms-form-tip">标题不能为空</div>
            </el-form-item>


            <el-form-item prop="modelUrl">
                <template #label>{{(form.modelIsMenu==1) ? '链接地址' : '权限标识'}}</template>
                <el-input v-model="form.modelUrl" :placeholder="(form.modelIsMenu==1) ? '请输入链接地址' : '请输入权限标识'"></el-input>
                <div class="ms-form-tip"  v-if="form.modelIsMenu == 1">
                    导航链接如“model/index.do”，或http(s)开头的外链地址
                </div>
                <div v-if="form.modelIsMenu == 0" class="ms-form-tip">
                    推荐格式：业务名:update,业务名:del,业务名:view<br>
                    控制层:	@RequiresPermissions("业务名:update") <br/>
                </div>
            </el-form-item>
            <el-form-item  label="图标" prop="modelIcon" v-if="form.modelIsMenu == 1">
                <el-col :span=10>
                    <ms-icon v-model:value="form.modelIcon"></ms-icon>
                </el-col>
            </el-form-item>
            <el-form-item prop="modelSort" label="排序" v-if="form.modelIsMenu == 1">
                <el-col :span=6>
                    <el-input v-model.number="form.modelSort" maxlength="11" placeholder="请输入排序"></el-input>
                    <div class="ms-form-tip" v-if="form.modelIsMenu == 1">
                        导航菜单倒序排序
                    </div>
                </el-col>
            </el-form-item>
            <el-form-item label="扩展业务"  prop="isChild" v-if="form.modelIsMenu == 1">
                <el-input v-model="form.isChild"
                          :disabled="false"
                          :style="{width: '100%'}"
                          :clearable="true"
                          placeholder="请输入系统扩展">
                </el-input>
                <div class="ms-form-tip">
                    例如：业务A定义菜单，业务B定义菜单，通过接口增加参数的方式可以区分开，也减少了重复开发菜单的工作<br/>
                </div>
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button size="default" @click="dialogVisible = false">取 消</el-button>
            <el-button size="default" type="primary" @click="save()" :loading="saveDisabled">保 存</el-button>
        </template>

    </el-dialog>

</template>
<script>
    var formDialog = Vue.defineComponent({
        template: '#dialog',
        data: function () {
            return {
                modelList: [],
                saveDisabled: false,
                dialogVisible: false,
                //表单数据
                form: {
                    id: 0,
                    modelId: 0,
                    modelTitle: '',
                    modelIcon: '',
                    modelUrl: '',
                    isChild: '',
                    modelCode:'',
                    modelSort: 0,
                    modelIsMenu: 1
                },
                rules: {
                    modelTitle: [{
                        required: true,
                        message: '请输入标题',
                        trigger: 'blur'
                    }, {
                        min: 1,
                        max: 20,
                        message: '长度不能超过20个字符',
                        trigger: 'change'
                    }],
                    modelUrl: [{
                        min: 0,
                        max: 255,
                        message: '长度不能超过255个字符',
                        trigger: 'change'
                    }],
                    modelSort: [{
                        type: 'number',
                        message: '排序必须为数字值'
                    }],
                    isChild: [{
                        min: 0,
                        max: 100,
                        message: '长度不能超过100个字符',
                        trigger: 'change'
                    }]
                },
            };
        },
        watch: {
            'dialogVisible': function (n, o) {
                if (!n) {
                    this.$refs.form.resetFields();
                }
            },

            'form.id': function (n, o) {
                if (this.form.modelId) {
                    //注意：vue3的新插件要求类型必须一致
                    this.form.modelId = this.form.modelId+"";
                } else {
                    this.form.modelId = 0;
                }
            }
        },
        methods: {
            open: function (id) {
                this.form.id = 0;
                this.form.modelId = 0;
                this.form.modelParentIds = null;
                this.form.modelCode = '';

                if (id) {
                    this.get(id);
                }

                this.$nextTick(function () {
                    this.dialogVisible = true;
                });
            },
            save: function () {
                var that = this;
                var url = ms.manager + "/basic/model/save.do";

                if (that.form.id > 0) {
                    url = ms.manager + "/basic/model/update.do";
                } //按钮没有图标


                if (that.form.modelIsMenu == 0) {
                    that.form.modelIcon = '';
                }
                that.$refs.form.validate(function (valid) {
                    if (valid) {
                        that.saveDisabled = true;

                        if (!that.form.modelId) {
                            delete that.form.modelId;
                        }

                        var data = JSON.parse(JSON.stringify(that.form));
                        delete data.modelChildList;

                        ms.http.post(url, data).then(function (data) {
                            if (data.result) {
                                that.$notify({
                                    title: '成功',
                                    message: '保存成功',
                                    type: 'success'
                                });
                                that.saveDisabled = false;
                                that.dialogVisible = false;
                                that.$parent.list();
                            } else {
                                that.$notify({
                                    title: '失败',
                                    message: data.msg,
                                    type: 'warning'
                                });
                                that.saveDisabled = false;
                            }
                        })
                    } else {
                        return false;
                    }
                });
            },
            getValue: function (data) {
                //顶级
                if(data.id == 0) {
                     this.form.modelId = '';
                     this.form.modelParentIds = '';
                }
                if (data.modelParentIds != null) {
                    var parentIndex = data.modelParentIds.split(",").indexOf(this.form.id);
                    if (parentIndex > -1) {
                        this.$notify({
                            title: '提示',
                            message: '不能把自身的子菜单作为父级菜单',
                            type: 'info'
                        });
                        return false;
                    }
                }
                if (data.id == this.form.id && data.id!=0) {
                    this.$notify({
                        title: '提示',
                        message: '不能把自身作为父级菜单',
                        type: 'info'
                    });
                    return false;
                }
                if (data.modelIsMenu == 0) {
                    this.$notify({
                        title: '提示',
                        message: '不能将功能按钮添加为菜单',
                        type: 'info'
                    });
                } else {
                    // 此时数据还没有绑定上，只能通过节点获取id
                    this.form.modelId = data.id;
                }
            },
            //获取当前任务
            get: function (id) {
                var that = this;
                ms.http.get(ms.manager + "/basic/model/get.do", {
                    id: id
                }).then(function (data) {
                    if (data.result) {
                        that.form = data.data.model;
                        delete that.form.modelDatetime;
                    }
                });
            },

        },
        created: function () {

        }
    });
</script>
<style>
    #form .el-select{
        width: 100%;
    }
    .ms-Icons .el-button+.el-button{
      margin-left:10px;
    }
</style>
