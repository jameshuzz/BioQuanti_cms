<template type="text/x-template" id="dialog">
<el-dialog :append-to-body="true" v-cloak id="form" title="自定义页面" v-model="dialogVisible" width="50%">
    <el-form ref="form" :model="form" :rules="rules" label-width="130px" size="default">
        <el-row>
            <el-col :span=24>
                <el-form-item  label="标题" prop="pageTitle">
                    <el-input v-model="form.pageTitle"
                              :disabled="false"
                              :style="{width:  '100%'}"
                              :clearable="true"
                              placeholder="请输入自定义页面标题">
                    </el-input>
                </el-form-item>
            </el-col></el-row>
        <el-row>
            <el-row>
                <el-col :span=24>
                    <el-form-item  label="分类" prop="pageType">
                        <el-select v-model="form.pageType"
                                   :style="{width: '100%'}"
                                   :filterable="false"
                                   :disabled="false"
                                   :multiple="false" :clearable="true"
                                   placeholder="请选择分类">
                            <el-option v-for='item in pageTypeOptions' :key="item.dictValue" :value="item.dictValue"
                                       :label="item.dictLabel"></el-option>
                        </el-select>
                        <div class="ms-form-tip">
                            可以通过 <b>自定义字典</b> 配置，根据不同的业务模块定义，方便业务开发管理。
                        </div>

                    </el-form-item>
                </el-col>
            </el-row>
            <el-col :span=24>
                <el-form-item  label="绑定模板" prop="pagePath">
                    <el-select v-model="form.pagePath"
                               :style="{width:  '100%'}"
                               :disabled="false"
                               filterable
                               :multiple="false" :clearable="true"
                               placeholder="请选择绑定模板">
                        <el-option v-for='item in pagePathOptions' :key="item" :value="item"
                                   :label="item"></el-option>
                    </el-select>
                </el-form-item>
            </el-col>
        </el-row>

        <el-form-item  label="路径关键字" prop="pageKey">
            <el-input v-model="form.pageKey"
                      :disabled="false"
                      :style="{width:  '100%'}"
                      :clearable="true"
                      placeholder="请输入自定义页面访问路径">
            </el-input>
            <div class="ms-form-tip">
                路径关键字决定了访问地址的路径<br/>
                例如：输入"login"对应访问的地址为 "域名/mdiyPage/login.do"，<br/>
                注意：路径关键字不能含有"/"，否则会导致无法访问。如果是需要会员登录后访问的路径，必须带有"people/"前缀，people前不能有"/"，需要先安装会员插件才能使用<br/>
                如：个人中心"people/center",对应访问地址："域名/people/center.do"
            </div>
        </el-form-item>
    </el-form>
    <template #footer>
        <el-button size="default" @click="dialogVisible = false">取 消</el-button>
        <el-button size="default" type="primary" @click="save()" :loading="saveDisabled">保存</el-button>
    </template>
</el-dialog>
</template>
<script>
    var form = Vue.defineComponent({
        template: '#dialog',
        data: function () {
            return {
                saveDisabled: false,
                dialogVisible: false,
                //表单数据
                form: {
                    pageType: '',
                    // 自定义页面标题
                    pageTitle: '',
                    // 绑定模板
                    pagePath: '',
                    // 自定义页面访问路径
                    pageKey: ''
                },
                pagePathOptions: [],
                pageTypeOptions: [],
                rules: {
                    pageTitle: [{
                        "required": true,
                        "message": "标题必须填写"
                    }, {
                        "min": 1,
                        "max": 30,
                        "message": "标题长度必须为1-30"
                    }],
                    // 绑定模板
                    pagePath: [{
                        "required": true,
                        "message": "绑定模板必须填写"
                    }],
                    // 访问路径
                    pageKey: [{
                        "required": true,
                        "message": "访问路径必须填写"
                    }, {
                        "min": 1,
                        "max": 300,
                        "message": "访问路径长度必须为1-300"
                    }],
                    // 分类
                    pageType: [{
                        "required": true,
                        "message": "请选择分类"
                    }]
                }
            };
        },
        watch: {
            dialogVisible: function (v) {
                if (!v) {
                    this.$refs.form.resetFields();
                    this.form.id = 0;
                }
            }
        },
        computed: {},
        methods: {
            open: function (id) {
                if (id) {
                    this.get(id);
                }

                this.pagePathOptionsGet();
                this.pageTypeOptionsGet();
                this.$nextTick(function () {
                    this.dialogVisible = true;
                });
            },
            save: function () {
                var that = this;
                var url = ms.manager + "/mdiy/page/save.do";

                if (that.form.id > 0) {
                    url = ms.manager + "/mdiy/page/update.do";
                }else {
                    //新增数据重置到列表第一页
                    that.$parent.currentPage = 1;
                    that.$parent.form = {};
                }

                this.$refs.form.validate(function (valid) {
                    if (valid) {
                        that.saveDisabled = true;
                        var data = JSON.parse(JSON.stringify(that.form));
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
                            }else {
                                that.$notify({
                                    title: '失败',
                                    message: data.msg,
                                    type: 'warning'
                                });
                                that.saveDisabled = false;
                            }

                        });
                    } else {
                        return false;
                    }
                });
            },
            //获取当前自定义页面
            get: function (id) {
                var that = this;
                ms.http.get(ms.manager + "/mdiy/page/get.do", {
                    "id": id
                }).then(function (data) {
                    if (data.result) {
                        that.form = data.data;
                    }
                });
            },
            //获取pagePath数据源
            pagePathOptionsGet: function () {
                var that = this;
                ms.http.get(ms.manager + "/basic/template/queryTemplateFileForColumn.do", {}).then(function (data) {
                    that.pagePathOptions = data.data;
                });
            },
            //获取pageType数据源
            pageTypeOptionsGet: function () {
                var that = this;
                ms.http.get(ms.base + '/mdiy/dict/list.do', {
                    dictType: '自定义页面类型',
                    pageSize: 99999
                }).then(function (data) {
                    if(data.result){
                        data = data.data;
                        that.pageTypeOptions = data.rows;
                    }
                });
            }
        },
        created: function () {}
    });
</script>
