<!-- 修改密码 -->
<template type="text/x-template" id="reset-password">
    <div id="reset-password" class="reset-password">
        <!-- :visible.sync统一替换成v-model -->
        <el-dialog
                v-model="dialogVisible"
                width="30%"
                :close-on-click-modal="false"
                :append-to-body="true"
        >
            <template #header>
                <i class="el-icon-key">修改密码</i>
            </template>
            <!-- 修改密码弹出框主体内容 -->
            <!-- .native修饰符vue3已经移除 -->
            <el-form
                    :model="resetPasswordForm"
                    ref="resetPasswordFormRef"
                    :rules="resetPasswordFormRule"
                    label-width="100px"
                    size="default"
                    @keyup.enter="updatePassword"
            >
                <el-form-item label="账号">
                    <el-input
                            v-model="resetPasswordForm.managerName"
                            size="default"
                            autocomplete="off"
                            readonly
                            disabled
                    ></el-input>
                </el-form-item>
                <el-form-item label="旧密码" prop="oldManagerPassword">
                    <el-input
                            v-model="resetPasswordForm.oldManagerPassword"
                            size="default"
                            autocomplete="off"
                            show-password
                    ></el-input>
                    <div class="ms-form-tip">长度在 6 到 30 字符</div>
                </el-form-item>
                <el-form-item label="新密码" prop="newManagerPassword">
                    <el-input
                            v-model="resetPasswordForm.newManagerPassword"
                            size="default"
                            autocomplete="off"
                            show-password
                    ></el-input>
                    <div class="ms-form-tip">长度在 6 到 30 字符</div>
                </el-form-item>
                <el-form-item
                        label="确认新密码"
                        prop="newConfirmManagerPassword"
                        style="margin-bottom: 30px"
                >
                    <el-input
                            v-model="resetPasswordForm.newConfirmManagerPassword"
                            size="default"
                            autocomplete="off"
                            show-password
                    ></el-input>
                    <div class="ms-form-tip">新密码必须和确认密码一致</div>
                </el-form-item>
            </el-form>
            <!-- 修改密码弹出框底部按钮 -->
            <template #footer>
        <span class="dialog-footer">
          <el-button
                  size="default"
                  @click="
              dialogVisible = false;
              resetPasswordForm.oldManagerPassword = '';
              resetPasswordForm.newManagerPassword = '';
            "
          >取 消</el-button
          >
          <el-button type="primary" size="default" @click="updatePassword"
          >更新密码</el-button
          >
        </span>
            </template>
        </el-dialog>
    </div>
</template>
<script>
    var ResetPassword = Vue.defineComponent({
        template: '#reset-password',
        props: {
            manager: {
                type: Object,
                default: function () {
                    return {}
                }
            }
        },
        data: function () {
            var validatorComfirmPassword = (rule, value, callback) => {
                if (this.resetPasswordForm.newManagerPassword === value) {
                    callback();
                } else {
                    callback("新密码和确认新密码不一致");
                }
            }
            var validatorPassword = (rule, value, callback) => {
                if (/(?!^(\d+|[a-zA-Z]+|[~!@#$%^&*?]+)$)^[\w~!@#$%^&*?]{6,30}$/.exec(value) == null
                ) {
                    callback("密码必须含有英文字母和数字组成,且6-30位长度！");
                }
                if (this.resetPasswordForm.oldManagerPassword === value) {
                    callback("新密码必须与旧密码不一致");
                } else {
                    callback();
                }
            }
            return {
                // 模态框的显示
                dialogVisible: false,
                resetPasswordForm: {
                    managerName: "",
                    oldManagerPassword: "",
                    newManagerPassword: "",
                    newConfirmManagerPassword: "", //确认新密码
                },
                resetPasswordFormRule: {
                    oldManagerPassword: [
                        {
                            required: true,
                            message: "请输入旧密码",
                            trigger: "blur",
                        },
                        {
                            min: 6,
                            max: 30,
                            message: "长度在 6 到 30 个字符",
                            trigger: "blur",
                        },
                    ],
                    newManagerPassword: [
                        {
                            required: true,
                            message: "请输入新密码",
                            trigger: "blur",
                        },
                        {
                            min: 6,
                            max: 30,
                            message: "长度在 6 到 30 个字符",
                            trigger: "blur",
                        },
                        {
                            validator: validatorPassword,
                        },
                    ],
                    newConfirmManagerPassword: [
                        {
                            required: true,
                            message: "请再次输入确认密码",
                            trigger: "blur",
                        },
                        {
                            min: 6,
                            max: 30,
                            message: "长度在 6 到 30 个字符",
                            trigger: "blur",
                        },
                        {
                            validator: validatorComfirmPassword,
                        },
                    ],
                },
            };
        },
        watch:{
            'dialogVisible': function (n, o) {
                if (!n) {
                    this.$refs.resetPasswordFormRef.resetFields();
                }
            },
        },

        methods: {
            open: function (id) {
                this.dialogVisible = true;
                this.resetPasswordForm.managerName = this.manager.managerName;
            },
            // 更新密码
            updatePassword: function () {
                var that = this;
                this.$refs.resetPasswordFormRef.validate(function (valid) {
                    if (valid) {
                        ms.http.post(ms.manager + "/updatePassword.do", that.resetPasswordForm).then(function (res) {
                            if (res.result) {
                                that.resetPasswordForm.oldManagerPassword = '';
                                that.resetPasswordForm.newManagerPassword = '';
                                that.dialogVisible = false;
                                that.$notify({
                                    title: '提示',
                                    message: "修改成功,重新登录系统！",
                                    type: 'success'
                                });
                                location.reload();
                            } else {
                                that.$notify({
                                    title: '错误',
                                    message: res.msg,
                                    type: 'error'
                                });
                            }
                        }, function (err) {
                            that.$notify({
                                title: '错误',
                                message: err,
                                type: 'error'
                            });
                        });
                    }
                });
            }
        }
    });
</script>
