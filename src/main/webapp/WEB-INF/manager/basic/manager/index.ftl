<!DOCTYPE html>
<html>
<head>
	<title>管理员管理</title>
	<#include "../../include/head-file.ftl">
	<#include "/basic/manager/form.ftl">
</head>
<body>
<div id="index" v-cloak class="ms-index">
	<el-header class="ms-header" height="50px">
		<el-col :span=12>
			<@shiro.hasPermission name="basic:manager:save">
				<el-button type="primary" class="el-icon-plus" size="default" @click="save()">新增</el-button>
			</@shiro.hasPermission>
			<@shiro.hasPermission name="basic:manager:del">
				<el-button type="danger" class="el-icon-delete" size="default" @click="del(selectionList)"  :disabled="!selectionList.length">删除</el-button>
			</@shiro.hasPermission>
		</el-col>
	</el-header>
	<div class="ms-search" style="padding: 20px 10px 0 10px;">
		<el-row>
			<el-form :model="form"  ref="searchForm"  label-width="120px" size="default">
				<el-row>
					<el-col :span=8>
						<el-form-item  label="管理员账号" prop="managerName">
							<el-input v-model="form.managerName"
									  :disabled="false"
									  :clearable="true"
									  placeholder="请输入管理员账号">
							</el-input>
						</el-form-item>
					</el-col>
					<el-col :span=8>
						<el-form-item  label="管理员昵称" prop="managerNickName">
							<el-input v-model="form.managerNickName"
									  :disabled="false"
									  :clearable="true"
									  placeholder="请输入管理员昵称">
							</el-input>
						</el-form-item>
					</el-col>
					<el-col :span=8 style="text-align: right">
						<el-button type="primary" class="el-icon-search" size="default" @click="currentPage=1;list()">查询</el-button>
						<el-button @click="rest"  class="el-icon-refresh" size="default">重置</el-button>
					</el-col>
				</el-row>
			</el-form>
		</el-row>
	</div>
	<el-main class="ms-container">
		<el-table v-loading="loading"  ref="multipleTable" class="ms-table-pagination" border :data="dataList" tooltip-effect="dark" @selection-change="handleSelectionChange">
			<template #empty>
				{{emptyText}}
			</template>
			<el-table-column type="selection" :selectable="isChecked" width="40" ></el-table-column>
			<el-table-column label="账号" min-width="120" align="left" prop="managerName" show-overflow-tooltip>
				<template #default="scope">
					{{ scope.row.managerName }}
					<el-tag size="default" v-if="scope.row.managerAdmin == 'super'" type="success">超级管理员</el-tag>
					<el-tag size="default" v-if="scope.row.id == manager.id" >当前登陆管理员</el-tag>
				</template>
			</el-table-column>
			<el-table-column label="昵称" min-width="120" align="left" prop="managerNickName" show-overflow-tooltip>
			</el-table-column>
			<el-table-column label="角色名称" min-width="120"  align="left" prop="roleName" show-overflow-tooltip>
			</el-table-column>
			<el-table-column label="创建时间" width="180" align="center" prop="createDate">
			</el-table-column>
			<el-table-column label="操作"  align="center" width="180">
				<template #default="scope" >
					<template v-if="manager.id != scope.row.id && scope.row.managerAdmin != 'super'">
						<@shiro.hasPermission name="basic:manager:update">
							<el-link type="primary" :underline="false"  @click="save(scope.row.id)">编辑</el-link>
						</@shiro.hasPermission>
						<@shiro.hasPermission name="basic:manager:del">
							<el-link type="primary" :underline="false"  @click="del([scope.row])">删除</el-link>
						</@shiro.hasPermission>
					</template>
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
			"form-dialog": formDialog
		},
		data: function () {
			return {
				dataList: [],
				//管理员管理列表
				selectionList: [],
				//管理员管理列表选中
				total: 0,
				//总记录数量
				pageSize: 10,
				//页面数量
				currentPage: 1,
				// 当前管理员信息
				manager: ms.managerInfo,
				//加载状态
				loading: true,
				//表格空值提示文字
				emptyText: '',
				//搜索表单
				form: {},
			}
		},
		methods: {
			//查询列表
			list: function () {
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

				that.loading = true;
				ms.http.get(ms.manager + "/basic/manager/list.do", Object.assign({},that.form, page)).then(function (data) {
					that.loading = false;
					if (data.data.total <= 0) {
						that.emptyText = '暂无数据';
						that.dataList = [];
						that.total = 0;
					} else {
						that.emptyText = '';
						that.total = data.data.total;
						that.dataList = data.data.rows;
					}
				});

			},
			//管理员管理列表选中
			handleSelectionChange: function (val) {
				this.selectionList = val;
			},
			//不能删除自己
			isChecked: function (row, index) {
				return !(row.managerAdmin == 'super' || this.manager.id == row.id);
			},
			//删除
			del: function (row) {
				var that = this;
				that.$confirm('此操作将永久删除所选内容, 是否继续?', '提示', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(function () {
					ms.http.post(ms.manager + "/basic/manager/delete.do", row.length ? row : [row], {
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
				})
			},
			//新增
			save: function (id) {
				// 定义that方便脚手架迁移
				var that = this;
				that.$refs.form.open(id);
			},
			//表格数据转换
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
				this.loading = true;
				this.$refs.searchForm.resetFields();
				this.list();
			},
		},
		created: function () {
			var that = this
			that.list();
		}
	});
</script>
<style>
</style>
