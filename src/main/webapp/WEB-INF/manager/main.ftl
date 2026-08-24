<html>

<head>
    <meta charset="utf-8"/>
    <title>工作台</title>
    <#include "../../include/head-file.ftl">
</head>
<body>
<div id="app" v-cloak>
    <div class="ms-console">
        <div class="ms-welcome">
            <div class="ms-welcome-title">欢迎使用后台管理系统</div>
            <div class="ms-welcome-sub">从左侧菜单或下方常用功能开始管理站点内容</div>
        </div>
        <div class="ms-console-left">
            <div class="ms-panel">
                <div class="ms-panel-title">
                    <i class="iconfont icon-zidingyimoxing"></i>
                    <span class="ms-panel-txt">
                            常用功能
                    </span>
                </div>
                <div class="ms-panel-body">
                    <div v-if="markList.length==0" class="menu-item-empty">
                        <el-empty>
                            <div slot="description">
                                展开顶部<b style="color: #E6A23C">功能大全</b>，点击菜单右侧<b style="color: #E6A23C">五角星</b>设置为常用功能
                            </div>
                        </el-empty>
                    </div>
                    <template v-else v-for="item in markList" >
                        <div  class="menu-item" v-if="item.isMark" >
                            <div class="menu-link" @click="openParentMenuInId(item.id)">
                                <i :class="['menu-icon','iconfont',item.modelIcon?item.modelIcon:'icon-zidingyimoxing']"></i>
                                <span class="menu-text">
                                    {{item.modelTitle}}
                            </span>
                            </div>
                        </div>
                    </template>
                </div>
            </div>
        </div>
    </div>
</div>
</body>

</html>

<script>

    const app = new _Vue({
        el: '#app',
        component() {
        },
        data: function () {
            return {
                markList: [], //常用功能列表
                base: ms.base,
                appId: "",
            }

        },
        watch: {},
        methods: {
            openParentMenuInId:function (id){
                window.parent.indexVue.openParentMenuInId(id)
            },
            getApp: function (){
                var that = this;
                ms.http.get(ms.manager + "/basic/app/get.do").then(function (res){
                    if (res.result){
                        that.appId = res.data.id;
                        that.queryMarkList();
                    }
                })
            },
            queryMarkList: function () {
                var that = this;
                var _markList = localStorage.getItem(ms.managerInfo.managerName + "-parent-menu-list-" + that.appId);
                if (_markList) {
                    that.markList = JSON.parse(_markList);
                }
                var hasMark = false;
                that.markList.forEach(function (item) {
                    if(item.isMark) {
                        hasMark = true;
                    }
                });
                if(!hasMark) {
                    that.markList = [];
                }
            }
        },
        computed() {
        },
        created() {
            this.getApp();
        },
        mounted:function () {
        }
    });
</script>
<style scoped>
    [v-cloak] {
        display: none;
    }
    .ms-console {
        display: flex;
        flex-direction: column;
        height: 100vh;
        background-color: #f5f6f8;
        padding: 20px;
        box-sizing: border-box;
        overflow: auto;
    }
    .ms-welcome {
        background: linear-gradient(135deg, #0099ff 0%, #33adff 100%);
        border-radius: 8px;
        padding: 40px 32px;
        margin-bottom: 20px;
        color: #fff;
    }
    .ms-welcome-title {
        font-size: 26px;
        font-weight: bold;
        margin-bottom: 10px;
    }
    .ms-welcome-sub {
        font-size: 14px;
        opacity: .9;
    }
    .ms-console-left {
        flex: 1;
    }
    .ms-panel {
        background-color: #fff;
        border-radius: 8px;
        padding: 20px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.04);
    }
    .ms-panel-title {
        display: flex;
        align-items: center;
        margin-bottom: 16px;
        font-size: 15px;
        font-weight: bold;
        color: #333;
    }
    .ms-panel-title .iconfont {
        margin-right: 6px;
        color: #0099ff;
    }
    .ms-panel-body {
        display: flex;
        flex-wrap: wrap;
    }
    .menu-item {
        width: 20%;
        min-width: 140px;
    }
    .menu-link {
        display: flex;
        align-items: center;
        padding: 14px 12px;
        margin: 6px;
        border-radius: 6px;
        cursor: pointer;
        background-color: #f7f8fa;
        transition: all .2s;
    }
    .menu-link:hover {
        background-color: #e8f4ff;
        color: #0099ff;
    }
    .menu-icon {
        font-size: 20px;
        margin-right: 10px;
        color: #0099ff;
    }
    .menu-text {
        font-size: 14px;
        color: #333;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
    .menu-item-empty {
        width: 100%;
    }
</style>
