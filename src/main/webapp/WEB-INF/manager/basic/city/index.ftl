<!DOCTYPE html>
<html>
<head>
    <title>城市</title>
    <#include "../../include/head-file.ftl">
    <#include "/basic/city/form.ftl">
</head>
<body>
<div id="index" class="ms-index" v-cloak>
    <el-header class="ms-header" height="50px">
        <el-col :span=24>
            <@shiro.hasPermission name="city:save">
                <el-button type="primary" class="el-icon-plus" size="default" @click="save()">新增</el-button>
            </@shiro.hasPermission>
            <@shiro.hasPermission name="city:del">
                <el-button type="danger" class="el-icon-delete" size="default" @click="del(selectionList)"
                           :disabled="!selectionList.length">删除
                </el-button>
            </@shiro.hasPermission>
            <el-button v-if="cityLabel != '省'" size="default" class="iconfont icon-fanhui" plain style="float: right;" @click="goBack()">返回</el-button>
        </el-col>
    </el-header>



    <div class="ms-search">
        <el-row>
            <el-form :model="form"  ref="searchForm"  label-width="120px" size="default">
                <el-row>
                    <el-col :span=8>
                        <!--村-->
                        <div>
                                <!--省-->
                                <el-form-item v-if="cityLabel == '省'" label="省" prop="provinceName">
                                    <el-input
                                            v-model="form.provinceName"
                                            :disabled="false"
                                            :readonly="false"
                                            :style="{width:  '100%'}"
                                            :clearable="true"
                                            placeholder="请输入省">
                                    </el-input>
                                </el-form-item>
                                <!--市-->
                                <el-form-item v-if="cityLabel == '市'" label="市" prop="cityName">
                                    <el-input
                                            v-model="form.cityName"
                                            :disabled="false"
                                            :readonly="false"
                                            :style="{width:  '100%'}"
                                            :clearable="true"
                                            placeholder="请输入市">
                                    </el-input>
                                </el-form-item>
                                <!--县-->
                                <el-form-item v-if="cityLabel == '县'" label="县" prop="countyName">
                                    <el-input
                                            v-model="form.countyName"
                                            :disabled="false"
                                            :readonly="false"
                                            :style="{width:  '100%'}"
                                            :clearable="true"
                                            placeholder="请输入县">
                                    </el-input>
                                </el-form-item>
                        <!--镇-->
                        <el-form-item v-if="cityLabel == '镇'" label="镇" prop="townName">
                            <el-input
                                    v-model="form.townName"
                                    :disabled="false"
                                    :readonly="false"
                                    :style="{width:  '100%'}"
                                    :clearable="true"
                                    placeholder="请输入镇">
                            </el-input>
                        </el-form-item>
                        <!--村-->
                        <el-form-item v-if="cityLabel == '村'" label="村" prop="villageName">
                            <el-input
                                    v-model="form.villageName"
                                    :disabled="false"
                                    :readonly="false"
                                    :style="{width:  '100%'}"
                                    :clearable="true"
                                    placeholder="请输入村">
                            </el-input>
                        </el-form-item>

                        </div>
                    </el-col>
                    <el-col :span=16 style="text-align: right;">
                        <el-button type="primary" class="el-icon-search" size="default" @click="currentPage=1;list(true)">搜索</el-button>
                        <el-button @click="rest"  class="el-icon-refresh" size="default">重置</el-button>
                    </el-col>
                </el-row>
            </el-form>
        </el-row>
    </div>


    <el-main class="ms-container">
        <el-table height="calc(100vh - 68px)" v-loading="loading" ref="multipleTable" border :data="dataList"
                  tooltip-effect="dark" @selection-change="handleSelectionChange">
            <template #empty>
                {{emptyText}}
            </template>
            <el-table-column type="selection" width="40" :selectable="isChecked"></el-table-column>
            <el-table-column  :label="cityLabel" align="left" >
                <template #default="scope">
                    <@shiro.hasPermission name="city:view">
                        <el-link type="primary" :underline="false" v-if="!scope.row.villageId || scope.row.villageId == '' || scope.row.villageId == null" @click="show(scope.row)">{{getCityName(scope.row)}}</el-link>
                        <span v-else>{{getCityName(scope.row)}}</span>
                    </@shiro.hasPermission>
                    <i class="el-icon-loading" v-if="scope.row.state == 0 || scope.row.state == 3"></i>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center">
                <template #default="scope">
                    <@shiro.hasPermission name="city:update">
                        <el-link type="primary" :underline="false" @click="save(scope.row)">编辑</el-link>
                    </@shiro.hasPermission>
                    <@shiro.hasPermission name="city:del">
                        <el-link type="primary" :underline="false" @click="del([scope.row])" v-if="scope.row.del!=3">
                            删除
                        </el-link>
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
    <form-dialog ref="form"></form-dialog>
</div>
</body>

</html>
<script>
    var indexVue = new _Vue({
        el: '#index',
        components:{
            "form-dialog":formDialog
        },
        data: function () {
            return {
                createDisabled: false,
                dataList: [], //城市列表
                selectionList: [],//城市列表选中
                total: 0, //总记录数量
                pageSize: 50, //页面数量
                currentPage: 1, //初始页
                manager: ms.manager,
                cityLabel: '省',
                loading: true,//加载状态
                emptyText: '',//提示文字
                //搜索表单
                form: {
                    sqlWhere: null,
                    // 省
                    provinceName: '',
                    provinceId: '',
                    // 市
                    cityName: '',
                    cityId: '',
                    // 县
                    countyName: '',
                    countyId: '',
                    // 镇
                    townName: '',
                    townId: '',
                    // 村
                    villageName: '',
                    villageId: '',
                },
            }
        },
        watch: {},
        methods: {
            isChecked: function (row) {
                if (row.del == 3) {
                    return false;
                }
                return true;
            },
            goBack: function () {
                var that = this

                if (that.form.townId){
                    that.form.townId = ''
                    that.$refs.form.townId = ''
                }else if(that.form.countyId){
                    that.form.countyId = ''
                    that.$refs.form.countyId = ''
                }else if(that.form.cityId){
                    that.form.cityId = ''
                    that.$refs.form.cityId = ''
                }else if(that.form.provinceId){
                    that.form.provinceId = ''
                    that.$refs.form.provinceId = ''
                }
                that.$refs.form.name = ''
                that.list()
            },
            show: function (row) {
                var that = this
                if (row.provinceId){
                    that.form.provinceId = row.provinceId
                    that.form.provinceName = ''
                }else if (row.cityId){
                    that.form.cityId = row.cityId
                    that.form.cityName = ''

                }else if(row.countyId){
                    that.form.countyId = row.countyId
                    that.form.countyName = ''

                }else if(row.townId){
                    that.form.townId = row.townId
                    that.form.townName = ''

                }else if(row.villageId){
                    that.form.villageId = row.villageId
                    that.form.villageName = ''

                }
                that.currentPage = 1
                that.list()
            },
            //查询列表
            list: function (isSearch) {

                var that = this;
                var data = {}; //搜索参数
                that.loading = true;
                var page = {
                    pageNo: that.currentPage,
                    pageSize: that.pageSize
                }
                var form = JSON.parse(JSON.stringify(that.form))
                if (!isSearch){
                   switch (that.cityLabel){
                       case "县": form.countyName = ''
                           break
                       case "市": form.cityName = ''
                           break
                       case "村": form.villageName = ''
                           break
                       case "省": form.provinceName = ''
                           break
                       case "镇": form.townName = ''
                           break
                   }
                }
                    for (var key in form) {
                        if (form[key] === undefined || form[key] === null) {
                            delete form[key]
                        }
                    }
                    form.sqlWhere ? data = Object.assign({}, {sqlWhere: form.sqlWhere}, page) : data = Object.assign({}, form, page)
                ms.http.post(ms.manager + "/basic/city/list.do", data).then(
                    function (res) {


                        if (!res.result || res.data.total <= 0) {
                            that.emptyText = "暂无数据"
                            that.dataList = [];
                            that.total = 0;
                        } else {
                            that.emptyText = '';
                            that.total = res.data.total;
                            that.dataList = res.data.rows;
                        }
                        that.loading = false;
                    }).catch(function (err) {
                    that.loading = false;
                    console.log(err);
                });

                that.getCityLabel()

            },
            //城市列表选中
            handleSelectionChange: function (val) {
                this.selectionList = val;
            },
            /**
             * 获取对象的具体地址名
             * @param row
             * @returns {string}
             */
            getCityName:function (row) {
                var cityName = '';
                if (row.provinceId){
                    cityName = row.provinceName

                }else if (row.cityId){
                    cityName = row.cityName

                }else if(row.countyId){
                    cityName = row.countyName

                }else if(row.townId){
                    cityName = row.townName

                }else if(row.villageId){
                    cityName = row.villageName

                }
                return cityName
            },
            /**
             * 获取当前具体层级
             * @returns {string}
             */
            getCityLabel:function () {
                var that = this
                if (that.form.townId){
                    that.cityLabel = "村"
                }else if(that.form.countyId){
                    that.cityLabel = "镇"
                }else if(that.form.cityId){
                    that.cityLabel = "县"
                }else if(that.form.provinceId){
                    that.cityLabel = "市"
                }else {
                    that.cityLabel = "省"
                }
            },
            //删除
            del: function (row) {
                var that = this;
                that.$confirm("此操作将永久删除所选内容, 是否继续", "提示", {
                    confirmButtonText: "确认",
                    cancelButtonText: "取消",
                    type: 'warning'
                }).then(function () {
                    ms.http.post(ms.manager + "/basic/city/delete.do", row.length ? row : [row], {
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    }).then(
                        function (res) {
                            if (res.result) {
                                that.$notify({
                                    title: '提示',
                                    type: 'success',
                                    message: "删除成功"
                                });
                                //删除成功，刷新列表
                                that.list();
                            } else {
                                that.$notify({
                                    title: "错误",
                                    message: res.msg,
                                    type: 'warning'
                                });
                            }
                        });
                }).catch(function (err) {
                    //删除如果用户取消会抛出异常，所以需要catch一下
                });
            },
            //新增
            save: function (row) {
                var that = this
                if (!row){
                    row =  JSON.parse(JSON.stringify(that.form))
                    that.$refs.form.open(row,true,that.cityLabel);
                }else {
                    that.$refs.form.open(row,false,that.cityLabel);
                }
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
                var that = this
                this.currentPage = 1;
                this.form.sqlWhere = null;
                this.$refs.searchForm.resetFields();
                // this.form = {}
                switch (that.cityLabel){
                    case "县": that.form.countyName = ''
                        break
                    case "市": that.form.cityName = ''
                        break
                    case "村": that.form.villageName = ''
                        break
                    case "省": that.form.provinceName = ''
                        break
                    case "镇": that.form.townName = ''
                        break
                }
                this.list();
            },

        },
        created: function () {
            this.list();
        },
    })
</script>
<style>
    #index .ms-container {
        height: calc(100vh - 141px);
    }
</style>
