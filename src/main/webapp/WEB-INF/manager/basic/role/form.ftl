<!DOCTYPE html>
<html>
<head>
    <title>角色管理</title>
    <#include "../../include/head-file.ftl">
</head>
<body>
<div id="form" v-cloak>
    <el-header class="ms-header ms-tr" height="50px">
        <el-button type="primary" class="iconfont icon-baocun" size="default" @click="save()" :loading="saveDisabled">保存
        </el-button>
        <el-button size="default" class="iconfont icon-fanhui" plain @click="back()">返回</el-button>
    </el-header>
    <el-main class="ms-container" style="overflow: hidden">
        <el-scrollbar class="ms-scrollbar" style="height: 100%;">
            <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="default">
                <el-form-item label="角色名称" prop="roleName">
                    <el-input v-model="form.roleName"
                              :disabled="false"
                              :style="{width:  '20%'}"
                              :clearable="true"
                              maxlength="30"
                              placeholder="请输入角色名称">
                    </el-input>
                    <div class="ms-form-tip">角色名称不能为空,且不能重复</div>
                </el-form-item>
                <el-form-item class="tree-table">
                    <template #label>角色菜单</template>
                    <el-table v-loading="loading" height="calc(100vh - 148px)" border :data="modelChildList"
                              header-row-class-name='ms-table-head' row-class-name='ms-table-row'
                              row-key="id"
                              default-expand-all
                              :tree-props="{children: 'children'}">
                        <template #empty>
                            {{emptyText}}
                        </template>
                        <el-table-column label="模块标题" prop="modelTitle" width="300"></el-table-column>
                        <el-table-column label="功能权限">
                            <template #default="scope" class="ms-row">
                                <div v-if="scope.row.modelChildList.length>0">
                                    <div class='ms-check'>
                                        <!-- 全选/取消全选按钮 -->
                                        <el-button
                                                type="text"
                                                @click="toggleModuleSelection(scope.row)"
                                                style="margin-bottom: 6px;"
                                        >{{ isAllChecked(scope.row) ? '取消全选' : '全选' }}</el-button>
                                        <el-checkbox-group v-model="roleIds" @change="handleCheckedIdsChange">
                                            <el-checkbox v-for="model in scope.row.modelChildList" :label="model.id"
                                                         :value='model.id' :key="model.id">{{model.modelTitle}}
                                            </el-checkbox>
                                        </el-checkbox-group>
                                    </div>
                                </div>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-form-item>
            </el-form>
        </el-scrollbar>
    </el-main>
</div>
</body>
</html>
<script>
    var form = new _Vue({
        el: '#form',
        data: function () {
            return {
                saveDisabled: false,
                //表单数据
                form: {
                    // 角色名称
                    roleName: '',
                    roleId: 0,
                    //角色编号
                    ids: "" //菜单编号集合

                },
                modelList: [],
                //菜单集合
                roleIds: [],
                //选择的菜单
                modelChildList: [],
                //组菜单
                loading: true,
                emptyText: '',
                buttonList: [],
                rules: {
                    // 角色名称
                    roleName: [{"required":true,"message":"角色名称不能为空"},{"min":0,"max":30,"message":"角色名称长度必须为0-30"}]
                }
            };
        },
        watch: {},
        computed: {
        },
        methods: {
            back: function (){
                ms.util.openSystemUrl("/basic/role/index.do",true);
            },
            save: function () {
                var that = this;
                var url = ms.manager + "/basic/role/saveOrUpdateRole.do";
                that.form.ids = '';
                var modelParentIds = '';

                for (var i = 0; i < that.roleIds.length; i++) {
                    //获取每个选中按钮权限的所有父级modelid
                    if (that.buttonList.filter(function (f) {
                        return f && f['id'] == that.roleIds[i];
                    }).length > 0) {
                        modelParentIds = that.buttonList.filter(function (f) {
                            return f && f['id'] == that.roleIds[i];
                        })[0].modelParentIds;

                        if (modelParentIds) {
                            that.roleIds = that.roleIds.concat(modelParentIds.split(",").map(Number));
                        }
                    }
                } //去重父级modelid
                that.roleIds = Array.from(new Set(that.roleIds));
                that.form.ids = that.roleIds.join(",");
                if (that.form.ids != "") {
                    //是否选择了菜单
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
                                    ms.util.openSystemUrl("/basic/role/index.do",that.form.id > 0);
                                } else {
                                    that.$notify({
                                        title: '失败',
                                        message: data.msg,
                                        type: 'warning'
                                    });
                                }
                                that.saveDisabled = false;
                            });
                        } else {
                            return false;
                        }
                    });
                } else {
                    that.$notify({
                        title: '提示',
                        message: "请选择功能权限",
                        type: 'warning'
                    });
                }
            },
            //判断当前模块按钮是否全部选中
            isAllChecked: function(row) {
                if (!row || !row.modelChildList || !row.modelChildList.length) return false;
                // row.modelChildList 的id全部在roleIds中
                var ids = row.modelChildList.map(function(c){return c.id});
                for(var i=0;i<ids.length;i++){
                    if(this.roleIds.indexOf(ids[i])==-1) {
                        return false;
                    }
                }
                return true;
            },
            // 全选/取消全选当前模块的功能权限
            toggleModuleSelection: function(row) {
                if (!row || !row.modelChildList) return;
                var ids = row.modelChildList.map(function(c){return c.id});
                if (this.isAllChecked(row)) {
                    // 全部选中，去除这些
                    this.roleIds = this.roleIds.filter(function(id){
                        return ids.indexOf(id)===-1;
                    });
                } else {
                    // 没全选，加进去不存在的
                    var merged = this.roleIds.concat(ids.filter(function(id){
                        return !~this.roleIds.indexOf(id)
                    },this));
                    // 去重
                    this.roleIds = Array.from(new Set(merged));
                }
            },
            //获取当前角色管理
            get: function (id) {
                var that = this;
                ms.http.get(ms.manager + "/basic/role/get.do", {
                    "id": id
                }).then(function (data) {
                    if (data.data.roleName) {
                        that.form = data.data;
                    }
                });
            },
            //单元格事件
            handleCheckedIdsChange: function (value) {
                var that = this;
                that.roleIds = value;
            },
            //菜单列表
            list: function (id) {
                var that = this;
                setTimeout(function () {
                    ms.http.get(ms.manager + "/basic/model/modelList.do", {
                        "roleId": id
                    }).then(function (data) {
                        if (data.data.total > 0) {
                            that.modelChildList = ms.util.treeData(data.data.rows.filter(function (f) {
                                return f['modelIsMenu'] == 1;
                            }), 'id', 'modelId', 'children'); //循环数组

                            for (var i = 0; i < data.data.rows.length; i++) {
                                if (data.data.rows[i].modelChildList.length > 0) {
                                    for (var j = 0; j < data.data.rows[i].modelChildList.length; j++) {
                                        //判断是否选中
                                        if (data.data.rows[i].modelChildList[j].chick == 1) {
                                            that.roleIds.push(data.data.rows[i].modelChildList[j].id);
                                        }
                                    }
                                }
                            } //获取所有按钮菜单


                            var _iteratorNormalCompletion = true;
                            var _didIteratorError = false;
                            var _iteratorError = undefined;

                            try {
                                for (var _iterator = data.data.rows.filter(function (f) {
                                    return f['modelIsMenu'] == 1;
                                })[Symbol.iterator](), _step; !(_iteratorNormalCompletion = (_step = _iterator.next()).done); _iteratorNormalCompletion = true) {
                                    var item = _step.value;
                                    that.buttonList = that.buttonList.concat(item.modelChildList);
                                }
                            } catch (err) {
                                _didIteratorError = true;
                                _iteratorError = err;
                            } finally {
                                try {
                                    if (!_iteratorNormalCompletion && _iterator.return != null) {
                                        _iterator.return();
                                    }
                                } finally {
                                    if (_didIteratorError) {
                                        throw _iteratorError;
                                    }
                                }
                            }

                            that.emptyText = '';
                            that.loading = false;
                        } else {
                            that.loading = false;
                            that.emptyText = '暂无数据';
                            that.modelChildList = [];
                        }
                    });
                }, 500);
            },
            remote: function (row, callback) {
                callback(this.modelChildList.filter(function (f) {
                    return f['modelId'] == row['id'];
                }));
            }
        },
        created: function () {
            this.form.id = ms.util.getParameter("id");

            if (this.form.id) {
                this.get(this.form.id);
            }

            this.list(this.form.id);
        }
    });
</script>
<style>
    .tree-table > div:nth-of-type(1) {
        text-align: right;
    }

    .tree-table > div:nth-of-type(2) {
        margin-left: -10px;
        padding-right: 5px;
    }

    .el-form .ms-table-head {
        line-height: 0px;
    }

    .tree-table .cell.el-tooltip .ms-check {
        display: inline-block;
        margin-top: 3px;
        margin-right: 11px;
    }
</style>
