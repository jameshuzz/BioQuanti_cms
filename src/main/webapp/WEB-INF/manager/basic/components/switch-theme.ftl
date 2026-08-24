<template type="text/x-template" id="switch-theme">
    <el-menu menu-trigger="hover" class="el-menu-demo" mode="horizontal" :ellipsis="false">
        <!-- 添加唯一标识index -->
        <el-sub-menu index="2" style="height: 100%" popper-class="ms-admin-header-menu-item">
            <!-- 图标 -->
            <template #title>
                <i class="iconfont icon-pifu" style="font-size: 18px;"></i>
            </template>
            <!-- 下拉菜单 -->
            <el-menu-item @click="handleCommand('ms-theme-light')" style="display: flex; align-items: center" index="2-1">
                <div style="
                                    height: 18px;
                                    width: 18px;
                                    background-color: rgba(64, 158, 255, 1);
                                    margin-right: 7px;
                                    border-radius: 2px;
                                  "></div>
                <span>浅色系</span>
            </el-menu-item >
            <el-menu-item @click="handleCommand('ms-theme-dark')" style="display: flex; align-items: center" index="2-2">
                <div style="
                                    height: 18px;
                                    width: 18px;
                                    background-color: rgba(56, 58, 63, 1);
                                    margin-right: 7px;
                                    border-radius: 2px;
                                  "></div>
                <span>深色系</span>
            </el-menu-item>
        </el-sub-menu>
    </el-menu>
</template>
<script>
    var MsSwitchTheme = Vue.defineComponent({
        template: '#switch-theme',
        data: function () {
            return {
                theme: 'ms-theme-light',
            };
        },
        watch: {

        },
        methods: {

            handleCommand: function (theme) {
                this.theme = theme;
                localStorage.setItem("theme", theme);
                this.$emit('update-theme');
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
<style>

</style>
