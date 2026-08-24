<template type="text/x-template" id="ms-dict">
    <el-select
            v-model="select"
            :placeholder="placeholder"
            :clearable="clearable"
            :filterable="filterable"
            :disabled="disabled"
            :multiple="multiple"
            :multiple-limit="multipleLimit"
            :loading="loading"
            :style="style"
            @change="handleChange">
        <el-option v-for='(item,index) in dataList' :key="index" :value="item['dictValue']"
                   :label="item['dictLabel']">
        </el-option>
        <@shiro.hasPermission name="mdiy:dict:save">
        <template #footer>
            <el-form ref="dictFormRef" :inline="true" :model="form" :rules="rules">
                <el-form-item :label="dictType" prop="dictLabel">
                    <el-input
                            v-model="form.dictLabel"
                            :placeholder="'请输入' + dictType+ '名称'"
                            size="small"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" size="small" @click="save()">
                        添加
                    </el-button>
                </el-form-item>
                <div class="ms-form-tip">
                    默认{{dictType}}值和{{dictType}}名称是一致,如需要区分,请前往字典管理中修改
                </div>
            </el-form>
        </template>
        </@shiro.hasPermission>
    </el-select>
</template>
<script>
    var MsDict = Vue.defineComponent({
        name: 'MsDict',
        template: '#ms-dict',
        props: {
            // 绑定值
            value: {
                type: [String, Number, Array],
                default: '',
            },
            placeholder: {
                type: String,
                default: '请选择',
            },
            // 是否可以清空
            clearable: {
                type: Boolean,
                default: true,
            },
            // 是否搜索
            filterable: {
                type: Boolean,
                default: false,
            },
            // 是否禁用
            disabled: {
                type: Boolean,
                default: false,
            },
            // 是否多选
            multiple: {
                type: Boolean,
                default: false,
            },
            // 可以多选几个
            multipleLimit: {
                type: Number,
                default: 0,
            },
            // 加载状态
            loading: {
                type: Boolean,
                default: false,
            },
            // 样式
            style: {
                type: Object,
                default: () => ({}),
            },
            // 字典类型
            dictType: {
                type: String,
                required: true,
            }
        },
        data: function () {
            return {
                dataList: [],
                select: this.value,
                //表单数据
                form: {
                    // 标签名
                    dictLabel: '',
                    // 类型
                    dictType: this.dictType,
                    // 数据值
                    dictValue: '',
                    // 子业务数据类型
                    isChild: '',
                    // 启用状态
                    dictEnable: true,
                    // 排序
                    dictSort: '',
                    // 备注信息
                    dictRemarks: ''
                },
                rules: {
                    //标签名
                    dictLabel: [{
                        "required": true,
                        "message": "标签名必须填写"
                    },]
                }
            }
        },
        watch: {
            dictType:function (n, o) {
                if (n) {
                    this.list();
                }
            }
        },
        methods: {
            list: function () {
                var that = this;
                ms.http.get(ms.manager + '/mdiy/dict/list.do', {
                    dictType: that.dictType,
                    pageSize: 99999
                }).then(function (res) {
                    if (res.result) {
                        that.dataList = res.data.rows;
                    }
                });
            },
            save: function () {
                var that = this;

                this.$refs.dictFormRef.validate(function (valid) {
                    if (valid) {
                        that.saveDisabled = true;
                        that.form.dictValue = that.form.dictLabel;
                        var form = JSON.parse(JSON.stringify(that.form));
                        form.dictValue = form.dictLabel;
                        form.dictEnable = true;
                        form.dictType = that.dictType;
                        ms.http.post(ms.manager + "/mdiy/dict/data/save.do", form).then(function (res) {
                            if (res.result) {
                                that.$notify({
                                    title: '成功',
                                    message: '添加成功',
                                    type: 'success'
                                });
                                // 清空字段
                                that.$refs.dictFormRef.resetFields();
                                that.list();
                            } else {
                                that.$notify({
                                    title: '失败',
                                    message: res.msg,
                                    type: 'warning'
                                });
                            }
                        });
                    } else {
                        return false;
                    }
                });
            },
            handleChange(value) {
                this.$emit('change', value);
            },
        },
        created: function () {
            var that = this
            //字典类型拉取字典数据
            if (that.dictType) {
                that.list()
            }
        }
    })
</script>
