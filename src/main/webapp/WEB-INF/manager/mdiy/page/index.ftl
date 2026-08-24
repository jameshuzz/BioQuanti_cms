<!DOCTYPE html>
<html>
<head>
	<title>自定义页面</title>
	<#include "../../include/head-file.ftl">
	<#include "/mdiy/page/form.ftl">
</head>
<body>
	<div id="index" v-cloak class="ms-index">

		<el-header class="ms-header" height="50px">

			<el-col :span=12>
				<@shiro.hasPermission name="mdiy:page:save">
					<el-button type="primary" class="el-icon-plus" size="default" @click="save()">新增</el-button>
				</@shiro.hasPermission>
			<@shiro.hasPermission name="mdiy:page:del">
					<el-button type="danger" class="el-icon-delete" size="default" @click="del(selectionList)"  :disabled="!selectionList.length">删除</el-button>
			</@shiro.hasPermission>
			</el-col>
		</el-header>
		<div class="ms-search" style="padding: 20px 10px 0 10px;">
			<el-row>
				<el-form :model="form"  ref="searchForm"  label-width="60px" size="default">
					<el-row>
						<el-col :span=8>
						   <el-form-item  label="标题" prop="pageTitle">
								<el-input v-model="form.pageTitle"
										  :disabled="false"
										  :style="{width:  '100%'}"
										  :clearable="true"
										  placeholder="请输入自定义页面标题">
								</el-input>
						   </el-form-item>
						</el-col>
						<el-col :span=8>
							<el-form-item  label="分类" prop="pageType" label-width="100px">
								<el-select v-model="form.pageType"
										   :style="{width: '100%'}"
										   :filterable="true"
										   :disabled="false"
										   :multiple="false" :clearable="true"
										   placeholder="请选择分类">
									<el-option v-for='item in pageTypeOptions' :key="item.dictValue" :value="item.dictValue"
											   :label="item.dictLabel"></el-option>
								</el-select>

							</el-form-item>
						</el-col>
						<el-col :span=8 style="text-align: right">
								<el-button type="primary" class="el-icon-search" size="default" @click="loading=true;currentPage=1;list()">查询</el-button>
								<el-button @click="rest"  class="el-icon-refresh" size="default">重置</el-button>
						</el-col>
					</el-row>
				</el-form>
			</el-row>
		</div>

		<el-main class="ms-container">
			<el-alert
					class="ms-alert-tip"
					title="功能介绍"
					type="info"
					description="自定义页面主要是用来满足用户对一些常见的动态页面的需要，比如说登录、注册、修改密码页面等（自定义页面不需要后台生成可即时预览最新效果)">
				<template #title>
					功能介绍
				</template>
			</el-alert>
			<el-table v-loading="loading" ref="multipleTable"   class="ms-table-pagination" border :data="treeList" tooltip-effect="dark" @selection-change="handleSelectionChange">
			<template #empty>
				{{emptyText}}
			</template>
			<el-table-column type="selection" :selectable="isChecked" width="40"></el-table-column>
            <el-table-column label="标题" min-width="80px" align="left" prop="pageTitle">
            </el-table-column>
				<el-table-column label="分类" width="80px" :formatter="pageTypeFormat" align="left" prop="pageType">
				</el-table-column>
            <el-table-column label="绑定模板" min-width="100px" align="left" prop="pagePath">
            </el-table-column>
            <el-table-column label="路径关键字" min-width="100px" align="left" prop="pageKey">
            </el-table-column>
			<el-table-column label="访问地址" min-width="250px" align="left" >
				<template #default="scope">
					<el-link :underline="false"
							 target="_blank"
							 type="primary"
							 size="default"
							 :href="locationOrigin+ms.base+'/'+(scope.row.pageKey.startsWith('people/')?scope.row.pageKey:'mdiyPage/'+scope.row.pageKey)+'.do'">
						{{locationOrigin+ms.base+'/'+(scope.row.pageKey.startsWith('people/')?scope.row.pageKey:'mdiyPage/'+scope.row.pageKey)+'.do'}}</el-link>
				</template>
			</el-table-column>
				<el-table-column label="操作" align="center" width="180">
					<template #default="scope">
						<@shiro.hasPermission name="mdiy:page:update">
						<el-link :underline="false" type="primary" size="default"  @click="save(scope.row.id)">编辑</el-link>
						</@shiro.hasPermission>
                        <@shiro.hasPermission name="mdiy:page:del">
                        <el-link :underline="false" type="primary"  v-if="scope.row.notDel == 0" @click="del([scope.row])">删除</el-link>
                        </@shiro.hasPermission>
                    </template>
				</el-table-column>
			</el-table>
            <el-pagination
					background
					:page-sizes="[5, 10, 20, 50, 100]"
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
			formDialog:form
		},
		data: function () {
			return {
				treeList: [],
				//自定义页面列表
				selectionList: [],
				//自定义页面列表选中
				total: 0,
				//总记录数量
				pageSize: 20,
				//页面数量
				currentPage: 1,
				//初始页
				mananger: ms.manager,
				loading: true,
				emptyText: '',
				pageTypeOptions: [],
				//搜索表单
				form: {
					// 自定义页面标题
					pageTitle: '',
					// 分类
					pageType: null
				},
				locationOrigin:'',
			}
		},
		methods: {

			//查询列表
			list: function() {
				var that = this;
				var page = {
					pageNo: that.currentPage,
					pageSize: that.pageSize
				};
				var form = JSON.parse(JSON.stringify(that.form));

				for (var key in form) {
					if (!form[key]) {
						delete form[key];
					}
				}

				setTimeout(function () {
					ms.http.get(ms.manager + "/mdiy/page/list.do", Object.assign({}, that.form, page)).then(function (data) {
						if (data.result){
							if (data.data.total <= 0) {
								that.loading = false;
								that.emptyText = '暂无数据';
								that.treeList = [];
							} else {
								that.emptyText = '';
								that.loading = false;
								that.treeList = data.data.rows;
							}
							that.total = data.data.total;
						}
					});
				}, 500);
			},
			//自定义页面列表选中
			handleSelectionChange: function(val) {
				this.selectionList = val;
			},
			//删除
			del: function(row) {
				var that = this;
				that.$confirm('此操作将永久删除所选内容, 是否继续?', '提示', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(function () {
					ms.http.post(ms.manager + "/mdiy/page/delete.do", row.length ? row : [row], {
						headers: {
							'Content-Type': 'application/json'
						}
					}).then(function (data) {
						if (data.result) {
							that.$notify({
								title: '成功',
								type: 'success',
								message: '删除成功!'
							}); //删除成功，刷新列表
							that.list();
						} else {
							that.$notify({
								title: '失败',
								message: data.msg,
								type: 'warning'
							});
						}
					});
				});
			},
			//新增
			save: function (id) {
				// 定义that方便脚手架迁移
				var that = this;
				that.$refs.form.open(id);
			},
			isChecked: function (row) {
				return row.notDel == 0
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
			pageTypeFormat: function (row, column, cellValue, index) {
				//根据 数据值 获取对应 标签名
				return this.getLabel(this.pageTypeOptions,cellValue);
			},
			getLabel: function(dictList, value) {
				var data = dictList.find(function (v) {
					return v.dictValue == value;
				});

				if (data) {
					return data.dictLabel;
				}
				return "";
			},
			//获取pageType数据源
			pageTypeOptionsGet: function () {
				var that = this;

				//获取  自定义页面类型 字典数据
				ms.mdiy.dict.list('自定义页面类型').then(function (res) {
					if(res.result){
						that.pageTypeOptions = res.data.rows;
					}
				})


			},
			//重置表单
			rest: function () {
				this.currentPage = 1;
				this.loading = true;
				this.$refs.searchForm.resetFields();
				this.list();
			},
			//更新状态
			update: function (index) {
				var that = this;
				ms.http.post(ms.manager + "/mdiy/page/update.do", that.treeList[index]).then(function (data) {
					if (data.result) {
						that.$notify({
							title: '成功',
							message: '更新成功',
							type: 'success'
						});
					} else {
						that.$notify({
							title: '失败',
							message: data.msg,
							type: 'warning'
						});
					}
				});
			},
		},
		created: function () {
			//适配IE无法获取location.origin
			if (!window.location.origin) {
				window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port: '');
			}
			this.locationOrigin = location.origin;
			this.pageTypeOptionsGet();

			this.list();
		}
	});
</script>
<style>
    #index .ms-search{
		padding: 20px 10px 0 10px;
    }
</style>
