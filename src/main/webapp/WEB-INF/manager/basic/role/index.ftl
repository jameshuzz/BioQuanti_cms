<!DOCTYPE html>
<html>
<head>
	<title>角色管理</title>
		<#include "../../include/head-file.ftl">
</head>
<body>
<div id="index" v-cloak class="ms-index">
	<el-header class="ms-header" height="50px">
		<el-col :span=12>
			<@shiro.hasPermission name="basic:role:save">
				<el-button type="primary" class="el-icon-plus" size="default" @click="save()" >新增</el-button>
			</@shiro.hasPermission>
			<@shiro.hasPermission name="basic:role:del">
				<el-button type="danger" class="el-icon-delete" size="default" @click="del(selectionList)"  :disabled="!selectionList.length">删除</el-button>
			</@shiro.hasPermission>
		</el-col>
	</el-header>
	<div class="ms-search" style="padding: 20px 10px 0 10px;">
		<el-row>
			<el-form :model="form"  ref="searchForm"  label-width="120px" size="default">
				<el-row>
					<el-col :span=8>
						<el-form-item  label="角色名称" prop="roleName">
							<el-input v-model="form.roleName"
									  :disabled="false"
									  :clearable="true"
									  placeholder="请输入角色名称">
							</el-input>
						</el-form-item>
					</el-col>
					<el-col :span=16 style="text-align: right">
						<el-button type="primary" class="el-icon-search" size="default" @click="currentPage=1;list()">查询</el-button>
						<el-button @click="rest"  class="el-icon-refresh" size="default">重置</el-button>
					</el-col>
				</el-row>
			</el-form>
		</el-row>
	</div>
	<el-main class="ms-container">
		<el-alert
				class="ms-alert-tip"
				title="提示："
				type="info"
				description="给角色分配栏目管理权限后（如栏目查看权限）还需在栏目权限管理中给角色分配具体可管理的栏目">
		</el-alert>
		<el-table v-loading="loading" ref="multipleTable" class="ms-table-pagination" border :data="dataList" tooltip-effect="dark" @selection-change="handleSelectionChange">
			<template #empty>
				{{emptyText}}
			</template>
			<el-table-column type="selection" :selectable="isChecked" width="40"></el-table-column>
			<el-table-column label="角色名称" align="left" prop="roleName" show-overflow-tooltip>
				<template #default="scope">
					{{ scope.row.roleName }}
					<el-tag size="default" v-if="scope.row.notDel != 0" type="success">默认角色</el-tag>
				</template>
			</el-table-column>
			<el-table-column label="操作"  width="120" align="center">
				<template #default="scope">
					<template v-if="scope.row.notDel==0">
						<@shiro.hasPermission name="basic:role:update">
							<el-link type="primary" :underline="false" @click="save(scope.row.id)">编辑</el-link>
						</@shiro.hasPermission>
						<@shiro.hasPermission name="basic:role:del">
							<el-link type="primary" :underline="false" @click="del([scope.row])">删除</el-link>
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
</div>
</body>

</html>
<script>
	var indexVue = new _Vue({
		el: '#index',
		data: function () {
			return {
				dataList: [],
				//角色管理列表
				selectionList: [],
				//角色管理列表选中
				total: 0,
				//总记录数量
				pageSize: 20,
				//页面数量
				currentPage: 1,
				//初始页
				mananger: ms.manager,
				loading: true,
				//加载状态
				emptyText: '',
				//提示文字
				//搜索表单
				form: {
					// 角色名称
					roleName: null
				},
				//历史记录参数
				historyKey:"basic_role_history"
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

				sessionStorage.setItem(this.historyKey,JSON.stringify({
					form: form,
					page: page,
					total: that.total
				}));
				that.loading = true;
				setTimeout(function () {
					ms.http.get(ms.manager + "/basic/role/list.do", Object.assign({},that.form, page)).then(function (data) {
						that.loading = false;
						if (data.data.total <= 0) {
							that.emptyText = '暂无数据';
							that.dataList = [];
						} else {
							that.emptyText = '';
							that.total = data.data.total;
							that.dataList = data.data.rows;
						}
					});
				}, 500);
			},
			//角色管理列表选中
			handleSelectionChange: function (val) {
				this.selectionList = val;
			},
			//不能删除自己
			isChecked: function (row, index) {
				if (row.managerId == 0) {
					return false;
				} else {
					return true;
				}
			},
			//删除
			del: function (row) {
				var that = this;
				that.$confirm('此操作将永久删除所选内容, 是否继续?', '提示', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(function () {
					ms.http.post(ms.manager + "/basic/role/delete.do", row.length ? row : [row], {
						headers: {
							'Content-Type': 'application/json'
						}
					}).then(function (data) {
						if (data.result) {
							that.$notify({
								title: '成功',
								type: 'success',
								message: data.msg,
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
				if (id) {
                    ms.util.openSystemUrl("/basic/role/form.do?id=" + id);
				} else {
                    ms.util.openSystemUrl("/basic/role/form.do");
				}
			},
			//表格数据转换
			//pageSize改变时会触发
			sizeChange: function (pagesize) {
				this.pageSize = pagesize;
				this.list();
			},
			//currentPage改变时会触发
			currentChange: function (currentPage) {
				this.currentPage = currentPage;
				this.list();
			},
			//重置表单
			rest: function () {
				this.currentPage = 1;
				this.$refs.searchForm.resetFields();
				this.list();
			}
		},
		mounted: function () {
			//如果存在历史参数，恢复页面结果
			if(sessionStorage.getItem(this.historyKey) && ms.util.getParameter("isBack")=="true") {
				var _history = JSON.parse(sessionStorage.getItem(this.historyKey))
				this.form = _history.form;
				this.total = parseInt(_history.total);
				this.currentPage = parseInt(_history.page.pageNo);
				this.pageSize = parseInt(_history.page.pageSize);
			}
			this.list();
		}
	});
</script>
<style scoped>
	#index .ms-search {
		padding: 20px 0 0;
	}
</style>
