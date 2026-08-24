<!DOCTYPE html>
<html>
<head>
    <title>系统日志</title>
    <#include "../../include/head-file.ftl">
</head>
<body>
<div id="index" class="ms-index" v-cloak>
    <div class="ms-search">
        <el-row>
            <el-form :model="form" ref="searchForm" label-width="120px" size="default">
                <el-row>
                    <el-col :span=8>

                        <el-form-item label="标题" prop="logTitle">
                            <el-input
                                    v-model="form.logTitle"
                                    :disabled="false"
                                    :style="{width:  '100%'}"
                                    :clearable="true"
                                    placeholder="请输入标题">
                            </el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span=8>

                        <el-form-item label="请求地址" prop="logUrl">
                            <el-input
                                    v-model="form.logUrl"
                                    :disabled="false"
                                    :style="{width:  '100%'}"
                                    :clearable="true"
                                    placeholder="请输入请求地址">
                            </el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span=8>

                        <el-form-item label="请求状态" prop="logStatus">
                            <el-select v-model="form.logStatus"
                                       :style="{width: '100%'}"
                                       :filterable="false"
                                       :disabled="false"
                                       :multiple="false" :clearable="true"
                                       placeholder="请选择请求状态">
                                <el-option v-for='item in logStatusOptions' :key="item.value" :value="item.value"
                                           :label="item.label"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span=8>

                        <el-form-item label="业务类型" prop="logBusinessType">
                            <el-select v-model="form.logBusinessType"
                                       :style="{width: '100%'}"
                                       :filterable="false"
                                       :disabled="false"
                                       :multiple="false" :clearable="true"
                                       placeholder="请选择业务类型">
                                <el-option v-for='item in logBusinessTypeOptions' :key="item.value" :value="item.value"
                                           :label="item.label"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span=8>

                        <el-form-item label="操作人员" prop="logUser">
                            <el-input
                                    v-model="form.logUser"
                                    :disabled="false"
                                    :style="{width:  '100%'}"
                                    :clearable="true"
                                    placeholder="请输入操作人员">
                            </el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span=8>
                        <el-form-item label="创建时间" prop="createDateScope">
                            <el-date-picker
                                    v-model="form.createDateScope"
                                    value-format="YYYY-MM-DD HH:mm:ss"
                                    type="datetimerange"
                                    :style="{width:  '100%'}"
                                    start-placeholder="开始时间"
                                    end-placeholder="结束时间">
                            </el-date-picker>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :style="{padding: '10px'}">
                    <el-col :push=16 :span=8 style="text-align: right;">
                        <el-button type="primary" class="el-icon-search" size="default" @click="currentPage=1;list()">搜索
                        </el-button>
                        <el-button @click="rest" class="el-icon-refresh" size="default">重置</el-button>
                    </el-col>
                </el-row>
            </el-form>
        </el-row>
    </div>
    <el-main class="ms-container">
        <el-table  v-loading="loading" class="ms-table-pagination"  ref="multipleTable" border :data="dataList"
                  tooltip-effect="dark" @selection-change="handleSelectionChange">
            <template #empty>
                {{emptyText}}
            </template>
            <el-table-column label="标题" width="200" align="left" prop="logTitle">
            </el-table-column>
            <el-table-column label="请求地址" align="left" prop="logUrl">
            </el-table-column>
            <el-table-column label="请求ip" align="left" prop="logIp">
            </el-table-column>
            <el-table-column label="请求状态" width="100" align="left" prop="logStatus" :formatter="logStatusFormat">
            </el-table-column>
            <el-table-column label="操作人员" width="100" align="left" prop="logUser">
            </el-table-column>
            <el-table-column label="创建时间" width="180" align="left" prop="createDate">
            </el-table-column>
            <el-table-column label="操作" width="60" align="center">
                <template #default="scope">
                    <@shiro.hasPermission name="basic:log:view">
                        <el-link type="primary" :underline="false" @click="save(scope.row.id)">详情</el-link>
                    </@shiro.hasPermission>
                </template>
            </el-table-column>
        </el-table>
        <el-pagination
                background
                :page-sizes="[10,20,30,40,50,100]"
                layout="total, sizes, prev, pager, next, jumper"
                :current-page="currentPage"
                :page-size="pageSize"
                :total="total"
                class="ms-pagination"
                @current-change='currentChange'
                @size-change="sizeChange">
        </el-pagination>
    </el-main>
</div>
</body>

</html>
<script>
    var indexVue = new _Vue({
        el: '#index',
        data: function () {
            return {
                dataList: [], //系统日志列表
                selectionList: [],//系统日志列表选中
                total: 0, //总记录数量
                pageSize: 10, //页面数量
                currentPage: 1, //初始页
                manager: ms.manager,
                loadState: false,
                loading: true,//加载状态
                emptyText: '',//提示文字
                logRequestMethodOptions: [{"value": "get"}, {"value": "post"}, {"value": "put"}],
                logStatusOptions: [{"value": "success", "label": "成功"}, {"value": "error", "label": "失败"}],
                logBusinessTypeOptions: [],
                logUserTypeOptions: [{"value": "other", "label": "其他"}, {
                    "value": "manage",
                    "label": "管理员"
                }, {"value": "people", "label": "会员"}],
                //搜索表单
                form: {
                    sqlWhere: null,
                    // 标题
                    logTitle: null,
                    // 请求地址
                    logUrl: null,
                    // 请求状态
                    logStatus: null,
                    // 业务类型
                    logBusinessType: null,
                    // 操作人员
                    logUser: null,
                    createDateScope: null,
                },
                historyKey:"basic_log_history"
            }
        },
        watch: {},
        methods: {
            //查询列表
            list: function () {
                var that = this;
                that.loading = true;
                that.loadState = false;
                var page = {
                    pageNo: that.currentPage,
                    pageSize: that.pageSize
                }
                var form = JSON.parse(JSON.stringify(that.form))
                for (var key in form) {
                    if (!form[key]) {
                        delete form[key]
                    }
                }

                //处理时间范围
                if (form.createDateScope) {
                    form.startTime = form.createDateScope[0];
                    form.endTime = form.createDateScope[1];
                }

                sessionStorage.setItem(this.historyKey,JSON.stringify({form: form, page: page}));
                ms.http.post(ms.manager + "/basic/log/list.do", form.sqlWhere ? Object.assign({}, {sqlWhere: form.sqlWhere}, page)
                    : Object.assign({}, form, page)).then(
                    function (res) {
                        if (that.loadState) {
                            that.loading = false;
                        } else {
                            that.loadState = true
                        }
                        if (!res.result || res.data.total <= 0) {
                            that.emptyText = "暂无数据"
                            that.dataList = [];
                            that.total = 0;
                        } else {
                            that.emptyText = '';
                            that.total = res.data.total;
                            that.dataList = res.data.rows;
                        }
                    });
                setTimeout(function () {
                    if (that.loadState) {
                        that.loading = false;
                    } else {
                        that.loadState = true
                    }
                }, 500);
            },
            //系统日志列表选中
            handleSelectionChange: function (val) {
                this.selectionList = val;
            },

            //新增
            save: function (id) {
                if (id) {
                    ms.util.openSystemUrl("/basic/log/form.do?id=" + id);
                } else {
                    ms.util.openSystemUrl("/basic/log/form.do");
                }
            },
            //表格数据转换
            logStatusFormat: function (row, column, cellValue, index) {
                var value = "";
                if (cellValue) {
                    var data = this.logStatusOptions.find(function (value) {
                        return value.value == cellValue;
                    })
                    if (data && data.label) {
                        value = data.label;
                    }
                }
                return value;
            },
            logBusinessTypeList: function () {
                var that = this;
                ms.http.post(this.manager + "/basic/log/queryLogType.do").then(function (res) {
                    if (res.result) {
                        that.logBusinessTypeOptions = res.data;
                    } else {
                        that.$notify({
                            title: "错误",
                            message: res.msg,
                            type: 'warning'
                        });
                    }
                });
            },
            //pageSize改变时会触发
            sizeChange: function (pagesize) {
                this.loading = true;
                this.pageSize = pagesize;
                this.list();
            },
            //currentPage改变时会触发
            currentChange: function (currentPage) {
                this.loading = true;
                this.currentPage = currentPage;
                this.list();
            },
            //重置表单
            rest: function () {
                this.currentPage = 1;
                this.form.sqlWhere = null;
                this.$refs.searchForm.resetFields();
                this.list();
            },
        },
        mounted: function () {
            this.logBusinessTypeList();
            //如果存在历史参数，恢复页面结果
            if(sessionStorage.getItem(this.historyKey) && ms.util.getParameter("isBack")=="true") {
                var _history = JSON.parse(sessionStorage.getItem(this.historyKey))
                this.form = _history.form;
                this.total = parseInt(_history.total);
                this.currentPage = parseInt(_history.page.pageNo);
                this.pageSize = parseInt(_history.page.pageSize);
            }
            this.list();
        },
    })
</script>
<style>
    #index .ms-container {
        height: calc(100vh - 141px);
    }
</style>
