<!-- 主页 -->
<!DOCTYPE html>
<html>
<head>
    <title>MCms</title>
    <#include '/include/head-file.ftl'/>

    <script src="${base}/static/plugins/sockjs/1.4.0/sockjs.min.js"></script>
    <script src="${base}/static/plugins/stomp/2.3.3/stomp.min.js"></script>

    <style>
        .to-ele {
            font-size: 18px;
            margin: -2px 9px 0px 3px
        }
    </style>
</head>
<body>
<#include 'basic/components/reset-password.ftl'/>
<#include 'basic/components/switch-theme.ftl'/>
<#include 'basic/components/manager-icon.ftl'/>
<div id="index" class="index">
    <el-container  :class="theme">
        <!--左侧菜单-->
        <el-aside class="ms-admin-menu-aside" v-cloak>
            <el-scrollbar style="height:100%">
                <el-menu :default-active="asideMenuActive" class="el-menu-vertical-demo"
                         text-color="rgba(255,255,255,1)" active-text-color="rgba(255,255,255,1)"
                         :collapse="collapseMenu" :unique-opened='true' ref='menu'>
                    <div class="ms-admin-logo" :style="{display: 'flex','padding-left': (collapseMenu?'5px':'22px')}">
                        <img :src="ms.base+'/static/images/bioquanti_logo.png'"/>
                    </div>
                    <template v-for="menu in asideMenuList" :key="menu.id">
                        <el-sub-menu  v-if="!menu.isSingle" :popper-class="'ms-admin-menu-aside-submenu '+theme" :index="menu.id+''">
                            <template #title>
                                <!-- 修改图标样式使其与文字对齐 -->
                                <i :class="['ms-admin-icon','iconfont',menu.modelIcon?menu.modelIcon:'icon-zidingyimoxing']"
                                   style="margin-right: 4px;"></i>
                                <span>{{ menu.modelTitle }}</span>
                            </template>
                            <!-- 子菜单 -->
                            <el-menu-item :index="sub.id+''"
                                          v-for="sub in getSubMenu(menu.id)"
                                          :key="sub.id" v-text="sub.modelTitle"
                                          @click.self='open(sub)'></el-menu-item>
                        </el-sub-menu>
                        <!-- 子菜单 -->
                        <el-menu-item v-else :index="menu.id+''" style="margin-left: 2px"
                                      :key="menu.id"
                                      @click.self='open(menu)'>
                            <i :class="['ms-admin-icon','iconfont',menu.modelIcon?menu.modelIcon:'icon-zidingyimoxing']" style="margin-right: 4px;"></i>
                            <span>{{menu.modelTitle}}</span>
                        </el-menu-item>
                    </template>
                    <!--  当没有菜单显示时显示提示图片  -->
                    <img v-if="asideMenuList.length == 0 " :src="ms.base+'/static/images/tip.png'" />
                    <!-- 收缩按钮 -->
                </el-menu>
            </el-scrollbar>
        </el-aside>
        <!--右侧内容-->
        <el-container class="ms-admin-container" v-cloak>
            <!--头部-->
            <el-header class="ms-admin-header" v-cloak>
                <!--展示合并菜单-->
                <div class="ms-admin-header-menu-shrink" @click="collapseMenu = !collapseMenu">
                    <i class="iconfont"
                       :class="collapseMenu?'icon-liebiaoxiangyou':'icon-shousuo'"></i>
                </div>
                <!--头部menu-->
                <el-menu menu-trigger="hover" class="ms-admin-header-menu"
                         mode="horizontal" :ellipsis="false" >
                    <el-sub-menu index="1" style="height: 100%">
                        <template #title>
                            <i class="iconfont icon-gezi" style="font-size: 17px;margin-right:8PX"></i>
                            <span>功能大全</span>
                        </template>
                        <div class="ms-admin-header-menu-all" v-if="parentMenuList.length > 0">
                            <li class="ms-admin-header-menu-all-item" v-for="item of parentMenuList"
                                :key="item.id">
                                <div  style="display:flex;" @click='openMenu(item,index)'>
                                    <i :class="['iconfont',item.modelIcon?item.modelIcon:'icon-zidingyimoxing']" style="padding-right: 4px"></i>
                                    <div style="width:80px">{{item.modelTitle}}</div>
                                </div>
                                <div style="float: right;width: 18px;">
                                    <!-- 修正：用 toggleMark 方法统一处理收藏变更及 asideMenuList 刷新 -->
                                    <i @click.stop="toggleMark(item)"
                                        :class="item.isMark?'el-icon-star-on ':'el-icon-star-off'"></i>
                                </div>
                            </li>
                        </div>
                        <div style="display: flex; justify-content: center; width: 100%;"  v-else>
                            <el-empty :image-size="50" description="无菜单，请检查站点信息" />
                        </div>
                    </el-sub-menu>
                </el-menu>
                <!--头部右侧-->
                <div class="ms-admin-header-right">
                    <!-- 主题切换 -->
                    <ms-switch-theme :theme="theme" @update-theme="updateTheme"></ms-switch-theme>
                    <!-- 设置 -->
                    <ms-manager-icon :manager="managerInfo">
                        <template #default>
                            <el-menu-item
                                    style="display: flex;align-items: center" @click='$refs.resetPasswordForm.open()' index="reset-password">
                                <i class="el-icon-key"></i>
                                <span slot="title">修改密码</span>
                            </el-menu-item>
                        </template>
                    </ms-manager-icon>
                </div>

            </el-header>
            <!--内容-->
            <el-main class="ms-admin-main">
                <!--选项卡-->
                <el-tabs class="ms-admin-tabs" v-model="currentTab" type="card" closable @tab-remove="closeTab"
                         @tab-click='tabClick' tab-position="top">
                    <el-tab-pane v-for="(item, index) in editableTabs"
                                 :key="index"
                                 :label="item.modelTitle"
                                 :name="item.id">
                        <keep-alive>
                            <iframe :src='item.modelUrl.startsWith("http")||item.modelUrl.startsWith("https")||item.isStore?item.modelUrl:ms.manager+"/"+encodeURI(item.modelUrl)+(item.modelUrl.indexOf("?")==-1?"?":"&")'
                                    :ref="item.id" :style="{background:'url('+ms.base+'/static/images/loading.gif) no-repeat center'}"></iframe>
                        </keep-alive>
                    </el-tab-pane>
                </el-tabs>
            </el-main>
        </el-container>
    </el-container>
    <reset-password ref="resetPasswordForm" :manager="managerInfo"  ></reset-password>
</div>
</body>

</html>
<script>

    const indexVue = new _Vue({
        el: "#index",
        components:{
            MsManagerIcon,
            MsSwitchTheme,
            ResetPassword
        },
        data: function () {
            return {
                menuList: [], //菜单接口数据
                asideMenuList: [], //侧边菜单
                parentMenuList: [], //一级菜单
                subMenuList: [], //二级菜单 所有
                asideMenuActive: "", //左侧选中菜单
                headMenuActive: '', //头部菜单激活
                editableTabs: [{"modelTitle": "工作台", "id": "0", "modelUrl": "main.do"}], //当前打开的tab页面
                collapseMenu: false, //菜单折叠，false不折叠
                currentTab: '0', //当前激活tab的name，初始值为工作台id
                //登录用户信息
                managerInfo: {
                    managerName: '', //账号
                    managerNickName: '',
                },
                //主题
                theme: localStorage.getItem("theme") || 'ms-theme-light',
                appId:'',
                managerPermissions: [], // 管理员拥有的权限
            }
        },
        computed: {
        },
        watch: {
            parentMenuList: {
                handler: function (n, o) {
                    localStorage.setItem(this.managerInfo.managerName + "-parent-menu-list-" + this.appId, JSON.stringify(n))
                },
                deep: true
            },
            editableTabs: {
                handler: function (n, o) {
                    if (n.length) {
                        var that = this;
                        if (!document.querySelector('.el-icon-refresh')) {
                            var i = document.createElement('i');
                            i.className = "el-icon-refresh ms-admin-refresh"
                            i.title = "点击刷新当前页"
                            i.addEventListener('click', function () {
                                var index = null
                                Object.keys(that.$refs).forEach(function (item, i) {
                                    item.indexOf(that.currentTab) > -1 ? index = i : ''
                                }, that)
                                if(that.$refs[Object.keys(that.$refs)[index]][0].contentDocument!=null) {
                                    that.$refs[Object.keys(that.$refs)[index]][0].contentDocument.location.reload(true)
                                }

                            })
                            document.querySelector('.el-tabs__header').appendChild(i, document.querySelector('.el-tabs__nav-wrap'))
                        }
                    } else {
                        if (document.querySelector('.ms-admin-refresh')) {
                            document.querySelector('.el-tabs__header').removeChild(document.querySelector('.ms-admin-refresh'))
                        }
                    }
                },
                deep: true
            }
        },
        methods: {
            /**
             * 获取当前应用
             */
            getApp: function (){
                var that = this;
                ms.http.get(ms.manager + "/basic/app/get.do").then(function (res){
                    if (res.result){
                        that.appId = res.data.id;
                        // 修改标题
                        document.title = res.data.appName;
                        that.list();
                    }
                })
            },
            /**
             * 获取菜单列表
             */
            list: function () {
                var that = this;
                ms.http.get(ms.manager + "/basic/model/list.do")
                    .then(function (res) {
                        that.menuList = res.data.rows;

                        // 【修复关键】每次请求前先清空对应菜单数组，避免数据重复叠加
                        that.subMenuList = [];
                        that.parentMenuList = [];
                        that.asideMenuList = [];
                        that.managerPermissions = [];

                        //获取收藏
                        var markList = localStorage.getItem(that.managerInfo.managerName + "-parent-menu-list-" + that.appId);

                        //组织顶部功能大全显示菜单
                        that.menuList.forEach(function (item, index) {
                            //如果没有收藏，默认全部显示
                            if(markList == null) {
                                item.isMark = true;
                            } else {
                                item.isMark = false;
                            }
                            //判断是否是一级菜单，简单判断通过url,如果菜单有地址就表示为一级菜单
                            if(item.modelUrl) {
                                item.isSingle = true;
                            }

                            //如果是顶级菜单增加
                            item.modelId ? that.subMenuList.push(item) : that.parentMenuList.push(item);
                            // 添加管理员具有的权限
                            if(item.modelIsMenu===0){
                                that.managerPermissions.push(item.modelUrl);
                            }
                        })
                        // 每次进主页都更新管理员拥有的菜单权限
                        sessionStorage.setItem("manager-permission-list", JSON.stringify(that.managerPermissions))
                        //如果收藏就优先显示收藏
                        if(markList) {
                            var markMenuList = JSON.parse(markList);
                            // 遍历最新的菜单覆盖缓存的菜单，避免菜单变化导致缓存菜单没更新
                            that.parentMenuList.forEach(function (item){
                                markMenuList.forEach(function (markItem){
                                    if (item.id === markItem.id){
                                        item.isMark = markItem.isMark;
                                        //更新缓存中的菜单数据
                                        markItem = item;
                                    }
                                })
                            })
                            //更新缓存
                            localStorage.setItem(that.managerInfo.managerName + "-parent-menu-list-" + that.appId, JSON.stringify(markMenuList))
                        }

                        //简单深度复制一下，否则数据无法双向绑定
                        that.parentMenuList = JSON.parse(JSON.stringify(that.parentMenuList));

                        //左侧菜单也显示收藏的菜单
                        that.parentMenuList.forEach(function (item) {
                            if(item.isMark) {
                                that.asideMenuList.push(item);
                            }
                        })

                    }, function (err) {
                        that.$notify({
                            title: '错误',
                            message: err,
                            type: 'error'
                        });
                    })
            },

            /**
             * 获取管理员名称列表，便于格式化createBy
             */
            queryManager:function (){
                ms.http.get(ms.manager + "/basic/manager/queryManager.do")
                    .then(function (res) {
                        sessionStorage.setItem("manager-list", JSON.stringify(res.data))
                    })
            },

            /**
             * 收藏/取消收藏功能
             * @param item 菜单对象
             */
            toggleMark: function(item) {
                item.isMark = !item.isMark;

                // 更新 localStorage
                try {
                    localStorage.setItem(this.managerInfo.managerName + "-parent-menu-list-" + this.appId, JSON.stringify(this.parentMenuList));
                } catch(e) {}

                // 重新生成 asideMenuList
                this.asideMenuList = [];
                this.parentMenuList.forEach((parentItem) => {
                    if(parentItem.isMark) {
                        this.asideMenuList.push(parentItem);
                    }
                });
            },

            /**
             * 左侧菜单打开页面
             * @param sub
             */
            open: function (sub) {
                var that = this
                var result = '';
                result = this.editableTabs.some(function (item, index) {
                    return item.id == sub.id
                })

                if (sub.syncStoreUrl) {
                    // 商城入口已移除，跳过商城菜单
                    return;
                }
                !result ? this.editableTabs.push(sub) : ""
                this.currentTab = sub.id;
                this.headMenuActive = sub.modelId
                this.$nextTick(function () {
                    that.asideMenuActive = sub.id;
                })
                // 处理其他逻辑
                setTimeout(function () {
                    if (document.querySelector('.el-tabs__nav-prev')) {
                        document.querySelector('.el-tabs__nav-wrap').style.padding = '0 40px'
                    } else {
                        document.querySelector('.el-tabs__nav-wrap').style.padding = '0'
                    }
                }, 16)
            },
            /**
             * 点击选项卡切换左侧菜单
             * @param tab tab页
             */
            tabClick: function (tab,e) {
                var _menu = this.menuList.filter(m=>{
                    return m.id == tab.props.name;
                })
                if(_menu.length > 0) {
                    this.asideMenuActive = _menu[0].id;
                    this.headMenuActive = _menu[0].modelId;
                }

            },
            /**
             * 获取当前菜单的子菜单
             * @param id 父菜单
             * @returns {*[]}
             */
            getSubMenu: function (id) {
                var result = [];
                var that = this;
                that.subMenuList && that.subMenuList.forEach(function (item) {
                    item.modelId == id ? result.push(item) : ''
                })
                return result;
            },
            /**
             * 关闭tab
             * @param targetName 菜单
             */
            closeTab: function (targetName) {
                var that = this;
                // 关闭的面板是当前激活面板
                if (that.currentTab == targetName) {
                    var modelId = null
                    that.editableTabs.forEach(function (tab, index, arr) {
                        if (tab.id == targetName) {
                            modelId = arr[index].modelId
                            var nextTab = arr[index + 1] || arr[index - 1];
                            if (nextTab) {
                                that.currentTab = nextTab.id
                                that.asideMenuActive = nextTab.id
                                that.headMenuActive = nextTab.modelId
                            }
                        }
                    })
                }
                // 去掉关闭的tab
                that.editableTabs = that.editableTabs.filter(function (tab) {
                    return tab.id !== targetName
                })

                // 关闭左侧父菜单
                if (that.editableTabs.length) {
                    var result = that.editableTabs.every(function (item) {
                        return item.modelId !== modelId
                    })
                    if (result) {
                        that.asideMenuList.forEach(function (menu, index, arr) {
                            if (menu.id == modelId) {
                                var flag = false;
                                that.markList && that.markList.forEach(function (item, index, array) {
                                    if (item.title == menu.modelTitle) {
                                        flag = true;
                                    }
                                })
                                if (!flag && that.markList) {
                                    arr.splice(index, 1);
                                }
                            }
                        })
                    }
                } else {
                    that.asideMenuList = []
                }

                // 判断是否出现左右箭头
                setTimeout(function () {
                    if (document.querySelector('.el-tabs__nav-prev')) {
                        document.querySelector('.el-tabs__nav-wrap').style.padding = '0 40px'
                    } else {
                        document.querySelector('.el-tabs__nav-wrap').style.padding = '0'
                    }
                }, 16)
            },

            /**
             * 功能大全打开菜单
             * @param menu 菜单
             */
            openMenu: function (menu) {
                var that = this;
                this.asideMenuList.some(function (item, index) {
                    return item.id == menu.id
                }) || this.asideMenuList.push(menu)
                var children = [];
                this.menuList.forEach(function (tab) {
                    if (tab.modelId == menu.id) {
                        children.push(tab)
                    }
                })
                // 若子菜单不是功能导航
                if(children.length>0 && children[0].modelIsMenu) {
                    //默认打开第一个子菜单
                    this.currentTab = children[0] && children[0].id;
                    this.open(children[0]);
                    menu.isSingle = false;

                    that.$nextTick(function () {
                        that.$refs.menu.open(String(menu.id))
                    })
                } else if(menu.modelUrl) {
                    this.open(menu);
                    menu.isSingle = true;

                    that.$nextTick(function () {
                        that.$refs.menu.open(String(menu.id))
                    })

                }

            },
            /**
             * 获取管理员信息
             */
            getManagerInfo: function () {
                var that = this;
                ms.http.get(ms.manager + "/basic/manager/info.do")
                    .then(function (res) {
                        that.managerInfo = res.data;
                        sessionStorage.setItem("manager-info", JSON.stringify(res.data))
                        // 请求菜单数据
                        that.getApp();
                    }, function (err) {
                        that.$notify({
                            title: '错误',
                            message: err,
                            type: 'error'
                        });
                    }).catch( function(err){
                    that.$notify({
                        title: '错误',
                        message: err,
                        type: 'error'
                    });
                });
            },
            /**
             * 主界面常用菜单，点击打开左侧菜单
             * main.ftl 里面使用
             * @param id 菜单编号（一级或二级菜单 id）
             */
            openParentMenuInId: function (id) {
                var that = this;
                var parent = this.parentMenuList.find(function (menu) {
                    return menu.id == id || menu.modelTitle == id
                })

                if (parent) {
                    this.openMenu(parent)
                    return
                }
                var sub = this.subMenuList.find(function (menu) {
                    return menu.id == id || menu.modelTitle == id
                }) || this.menuList.find(function (menu) {
                    return (menu.id == id || menu.modelTitle == id) && menu.modelId
                })

                if (!sub) {
                    return
                }
                parent = this.parentMenuList.find(function (menu) {
                    return menu.id == sub.modelId
                })
                if (!parent) {
                    return
                }
                this.asideMenuList.some(function (item) {
                    return item.id == parent.id
                }) || this.asideMenuList.push(parent)
                parent.isSingle = false
                this.open(sub)
                this.$nextTick(function () {
                    that.$refs.menu.open(String(parent.id))
                })
            },
            /**
             * 更新主题
             */
            updateTheme:function(){
                this.theme = localStorage.getItem("theme") || 'ms-theme-light';
            }

        },
        created: function () {
        },
        mounted: function () {
            top.location != self.location?(top.location = self.location):'';
            //获取登录用户信息,成功后获取菜单信息
            this.getManagerInfo();
            //获取管理员名称列表，便于格式化createBy
            this.queryManager();
        },
    })
    //标准版需要
    window.indexVue = indexVue;
</script>
<style>
    .ms-admin-logo {
        display: flex;
        align-items: center;
        overflow: hidden;
        border-bottom: 1px solid rgba(0, 0, 0, 0.06);
    }

    .ms-admin-logo img {
        padding: 14px 0;
        width: 110px;
        max-width: 100%;
    }

    .ms-admin-logo>div {
        display: flex;
        align-items: center;
    }

    .ms-admin-logo>div span {
        margin-top: -6px;
        position: absolute;
        margin-left: 10px;
        font-size: 12px;
    }

    .ms-admin-logo .iconfont {
        color: #fff;
        margin-left: auto;
        margin-right: 20px;
        cursor: pointer;
    }

    .ms-admin-header-right {
        margin-left: auto;
        min-width: 200px;
        display: flex;
        align-items: center;
        justify-content: flex-end;
        padding: 0;
        line-height: 1;
    }

    .ms-admin-header-menu-shrink {
        width: 64px;
        display: flex;
        justify-content: center;
        align-items: center;
        line-height: 50px;
        border-right: 1px solid rgba(238, 238, 238, 1);
        cursor: pointer;
    }

    .ms-admin-header-menu-shrink:hover {
        background: rgba(250, 250, 250, 0.2);
    }

    .el-menu-vertical-demo:not(.el-menu--collapse) {
        width: 180px;
        min-height: 100vh;
    }

    .el-menu--collapse {
        height: 100vh;
    }

    .ms-admin-menu-aside {
        width: auto !important;
        height: 100vh;
        position: relative;
        overflow: visible;
    }

    .ms-admin-menu-aside .ms-admin-menu .el-menu--inline .is-active {
        border-left: 2px solid #0099ff;
    }

    .ms-admin-menu-aside .ms-menu-expand i {
        font-weight: bolder;
        font-size: 14px;
        color: #333;
        position: absolute;
        transform: rotate(90deg);
        right: 40%;
    }

    .ms-admin-menu-aside .ms-menu-expand::before {
        border-color: transparent transparent #eee;
        border-style: none solid solid;
        border-width: 0 30px 22px;
        content: "";
        display: block;
        height: 0;
        left: -10px;
        width: 30px;
    }

    .ms-admin-menu-aside .el-sub-menu__title,
    .ms-admin-menu-aside .el-menu-item {
        color: rgba(255, 255, 255, 1);
        height: 40px;
        line-height: 40px;
    }

    .ms-admin-menu-aside .el-sub-menu__title i {
        color: inherit;
    }

    .ms-admin-menu-aside .el-sub-menu__title .iconfont {
        font-size: 19px !important;
    }

    .ms-admin-menu-aside .el-sub-menu.is-active .el-sub-menu__title {
        color: rgba(255, 255, 255, 1) !important;
    }

    .ms-admin-menu-aside .el-sub-menu__title:hover {
        background-color: rgba(255,255,255,0.3) !important;
    }

    .ms-admin-header {
        display: flex;
        padding: 0;
        background-color: rgba(255, 255, 255, 1);
        height: 50px !important;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        z-index: 0;
        overflow: hidden;
    }

    .ms-admin-header .ms-admin-header-menu {
        border-bottom: none;
        position: relative;
    }

    .ms-admin-header .ms-admin-header-menu>.ms-admin-menu-item>* {
        height: 50px !important;
        line-height: 50px !important;
        border-bottom: none !important;
        font-size: 1em;
    }

    .ms-admin-header .ms-admin-header-menu .ms-admin-shortcut-menu>li {
        margin: 0;
        padding: 0 20px;
        text-align: left;
        display: -webkit-inline-box;
        display: inline-block;
        height: 50px;
        line-height: 50px;
        font-weight: normal;
        font-size: 14px;
        color: #333;
    }

    .ms-admin-header .ms-admin-header-menu .ms-admin-shortcut-menu>li:hover {
        cursor: pointer;
        color: #0099ff;
    }

    .ms-admin-header-menu .el-sub-menu__title {
        height: 50px !important;
        line-height: 50px !important;
        display: flex;
        align-items: center;
    }

    .ms-admin-header-menu .el-sub-menu__title:hover {
        background-color: #f2f6fc !important;
        color: #409eff !important;
    }

    .ms-admin-header-menu .el-sub-menu__title:hover i {
        color: #409eff !important;
    }

    .ms-admin-header-menu .el-sub-menu__icon-arrow {
        margin-top: -4px !important;
    }

    .ms-admin-header-menu-item .el-menu-item:hover {
        background-color: #f2f6fc !important;
        color: #409eff !important;
    }

    .ms-admin-header-menu-item .el-menu-item:hover i {
        color: #409eff !important;
    }

    .ms-admin-header-menu-all {
        width: 560px;
        height: auto;
        background: rgba(255, 255, 255, 1);
        border-radius: 2px;
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        line-height: 40px;
    }

    .ms-admin-header-menu-all .iconfont {
        font-size: 17px;
    }

    .ms-admin-header-menu-all .ms-admin-header-menu-all-item {
        display: flex;
        width: 25%;
        justify-content: center;
        align-items: center;
        cursor: pointer;
    }

    .ms-admin-header-menu-all .ms-admin-header-menu-all-item .el-icon-star-on {
        color: #ccc;
        font-size: 17px;
        margin-left: -1px;
        margin-top: 1px;
    }

    .ms-admin-header-menu-all .ms-admin-header-menu-all-item .el-icon-star-on:hover {
        color: rgba(64, 158, 255, 1);
    }

    .ms-admin-header-menu-all .ms-admin-header-menu-all-item .el-icon-star-off {
        color: #ccc;
    }

    .ms-admin-header-menu-all .ms-admin-header-menu-all-item .el-icon-star-off:hover {
        color: rgba(64, 158, 255, 1);
    }

    .ms-admin-header-menu-all .ms-admin-header-menu-all-item:hover {
        color: rgba(64, 158, 255, 1);
    }

    .ms-admin-menu-aside-submenu .el-menu-item {

        line-height: 40px;
        height: 40px;
    }

    .el-sub-menu__title * {
        vertical-align: top;
    }

    .ms-admin-login-theme .el-dropdown-menu__item {
        display: flex;
        flex-direction: row;
        justify-content: center;
        align-items: center;
    }

    .el-tabs__nav .el-tabs__item:nth-child(1) i {
        display: none;
    }

    .el-tabs__item.is-active {
        background-color: rgba(255, 255, 255, 1);
    }

    .el-menu {
        border-right: 0px;
    }

    .ms-admin-logo .class-1 {
        color: white;
        padding-top: 8px;
        color: #ffffff;
        word-wrap: break-word;
        font-family: MicrosoftYaHei-Bold;
        font-weight: bold;
        font-style: italic;
    }

    .ms-admin-logo .class-2 {
        font-size: 12px;
        font-weight: normal;
    }

    .top-operate-select .el-menu--popup {
        width: 162px;
        min-width: 162px;
    }

    .ms-admin-container {
        height: auto;
    }

    .ms-admin-container>.ms-admin-main {
        padding: 0;
        background-color: #fff;
        z-index: 0;
        overflow: hidden;
    }

    .ms-admin-container>.ms-admin-main .ms-admin-tabs {
        height: calc(100vh - 50px);
        display: flex;
        flex-direction: column;
    }

    .ms-admin-container>.ms-admin-main .ms-admin-tabs .el-tabs__content {
        height: 100%;
    }

    .ms-admin-container>.ms-admin-main .ms-admin-tabs .el-tabs__content .el-tab-pane {
        height: 100%;
    }

    .ms-admin-container>.ms-admin-main .ms-admin-tabs .el-tabs__header {
        background: #fafafa;
        margin-bottom: 0;
    }

    .ms-admin-container>.ms-admin-main .ms-admin-tabs .el-tabs__header .el-tabs__nav-scroll .el-tabs__nav {
        border-left: none;
        border-radius: 0;
    }

    .ms-admin-container>.ms-admin-main .ms-admin-tabs .ms-admin-refresh {
        float: right;
        width: 40px;
        height: 40px;
        text-align: center;
        border-left: 1px solid #e6e6e6;
        cursor: pointer;
        position: relative;
    }

    .ms-admin-container>.ms-admin-main .ms-admin-tabs .ms-admin-refresh::before {
        position: absolute;
        left: 34%;
        top: 50%;
        transform: translateY(-50%);
        color: #999;
    }

    .ms-admin-container>.ms-admin-main .ms-admin-tabs .el-tabs__nav-next,
    .ms-admin-container>.ms-admin-main .ms-admin-tabs .el-tabs__nav-prev {
        width: 40px;
        height: 40px;
        display: flex;
        justify-content: center;
        align-items: center;
    }

    .ms-admin-container>.ms-admin-main .ms-admin-tabs .el-tabs__nav-next {
        border-left: 1px solid #e6e6e6;
    }

    .ms-admin-container>.ms-admin-main .ms-admin-tabs .el-tabs__nav-prev {
        border-right: 1px solid #e6e6e6;
    }

    .ms-admin-container>.ms-admin-main iframe {
        width: 100%;
        height: 100%;
        border: none !important;
    }

    .el-tabs__item:focus-visible {
        box-shadow:unset;
    }

    /**tab页标题白色 **/

    /** 表单提示 **/
    .el-form-item__content {
        display: unset !important;
    }
</style>
