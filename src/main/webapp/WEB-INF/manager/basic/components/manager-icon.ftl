<template type="text/x-template" id="manager-icon">
    <div class="manager-icon">
        <!-- 设置:ellipsis="false解决使页面变水平模式时变成三个点的问题 -->
        <el-menu menu-trigger="hover" class="ms-admin-header-menu" mode="horizontal" :ellipsis="false">
            <!-- 添加唯一标识index -->
            <el-sub-menu index="2" popper-class="ms-admin-header-menu-item">
                <!-- 图标与用户名 -->
                <template #title >
                    <span class="manager-head" v-text="manager.managerNickName && manager.managerNickName.substr(0, 2)"></span>
                    <span v-text="manager.managerNickName"></span>
                </template>
                <!-- 修改密码显示区 -->
                <slot></slot>
                <!-- 退出显示区 -->
                <el-menu-item style="display: flex; align-items: center" @click="exitSystem" index="exitSystem">
                    <i class="el-icon-switch-button"></i>
                    <template #title>
                        <span>退出</span>
                    </template>
                </el-menu-item>
            </el-sub-menu>
        </el-menu>
    </div>
</template>
<script>
    var MsManagerIcon = Vue.defineComponent({
        template: '#manager-icon',
        props: {
            manager: {
                type: Object,
                default: function () {
                    return {}
                }
            }
        },
        data: function () {
            return {
                theme: 'ms-theme-light',
            };
        },
        watch: {

        },
        methods: {
            exitSystem:function() {
                this.$confirm("是否确认退出账号？", "退出系统", {
                    confirmButtonText: "确定",
                    cancelButtonText: "取消",
                    cancelButtonClass: "el-button--mini",
                    confirmButtonClass: "el-button--mini",
                    type: "warning",
                }).then(function () {
                    ms.http.get(ms.manager + "/loginOut.do").then(
                        function () {
                            ms.util.openSystemUrl("/login.do");
                        }
                    );
                }).catch(()=>{})
            },

        },
        mounted: function () {
            if (localStorage.getItem("theme")) {
                this.theme = localStorage.getItem("theme");
            }
        },
        created: function () {

        }
    });
</script>
<style scoped>
    .manager-icon{ margin-top: 9px}
    .manager-head {
        width: 30px !important;
        height: 30px !important;
        margin-right: 10px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #0099ff;
        color: #fff;
        padding-top: 4px;
    }

    .el-sub-menu.is-active .el-sub-menu__title {
        border-bottom:  none !important;
    }
    :deep(.el-menu--horizontal>.el-sub-menu.is-active .el-sub-menu__title) {
        border-bottom:  none !important;
    }
</style>
