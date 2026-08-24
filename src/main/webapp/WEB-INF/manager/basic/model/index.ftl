<!DOCTYPE html>
<html>
<head>
	<title>菜单</title>
	<meta charset="utf-8">
	<#include "../../include/head-file.ftl">
	<#include "/basic/model/form.ftl">
</head>
<body>
<div id="index" v-cloak class="ms-index">
	<el-header class="ms-header" height="50px">
		<el-col :span=24>
			<@shiro.hasPermission name="basic:model:save">
				<el-button type="primary" class="el-icon-plus" size="default" @click="openForm()">新增</el-button>
			</@shiro.hasPermission>
			<@shiro.hasPermission name="basic:model:del">
				<el-button type="danger" class="el-icon-delete" size="default" @click="del(selectionList)"  :disabled="!selectionList.length">删除</el-button>
			</@shiro.hasPermission>
			<@shiro.hasPermission name="basic:model:save">
				<el-button type="primary" class="iconfont icon-daoru" size="default" @click="dialogImportVisible=true" style="float: right">导入</el-button>
			</@shiro.hasPermission>
		</el-col>
	</el-header>

	<el-dialog :close-on-click-modal="false" v-model="dialogImportVisible" width="600px" append-to-body v-cloak>
		<template #header>
			<i class="iconfont icon-daoru"> 导入菜单JSON </i>
		</template>
		<el-form label-width="110px"  label-position="top" >
			<el-form-item >
				<el-input :rows=10 type="textarea" v-model="form.menuStr"></el-input>
				<div class="ms-form-tip">
					粘贴来自<b>自定义配置、自定义业务</b> 的<b>复制菜单JSON</b>功能数据
				</div>
			</el-form-item>
		</el-form>

		<template #footer>
			<el-button size="default" @click="dialogImportVisible = false">取 消</el-button>
			<el-button size="default" :loading="saveDisabled" :disabled="form.menuStr==''" type="primary" @click="imputJson()">确 定</el-button>
		</template>
	</el-dialog>

	<el-main class="ms-container">
		<el-table ref="multipleTable" v-loading="loading"
				  border :data="dataList"
				  tooltip-effect="dark"
				  @selection-change="handleSelectionChange"
				  row-key="id"
				  :tree-props="{children: 'modelChildList'}">
			<template style="width:100%" #empty>
				{{emptyText}}
			</template>
			<el-table-column type="selection" width="40"></el-table-column>
			<el-table-column label="菜单标题" width="200" align="left" prop="modelTitle" show-overflow-tooltip>
			</el-table-column>
			<el-table-column label="菜单图标" width="100" align="center" prop="modelIcon">
				<template #default="scope">
					<i style="font-size: 24px !important;" class="iconfont" :class="scope.row.modelIcon"></i>
				</template>
			</el-table-column>
			<el-table-column label="菜单链接地址" align="left" prop="modelUrl" show-overflow-tooltip>
			</el-table-column>

			<el-table-column label="菜单排序" width="90" align="right" prop="modelSort">
			</el-table-column>
			<el-table-column label="菜单类型"  width="130" align="center" prop="modelIsMenu">
				<template #default="scope">
					<span v-if="scope.row.modelIsMenu == 1">导航链接</span>
					<span v-else>功能权限</span>
				</template>
			</el-table-column>
			<el-table-column label="系统扩展" width="100" align="left" prop="isChild" show-overflow-tooltip>
			</el-table-column>
			<@shiro.hasAnyPermissions name="basic:model:update,basic:model:del">
				<el-table-column label="操作"  align="center" width="250">
					<template #default="scope">
						<@shiro.hasPermission name="basic:model:update"><el-button size="default" type="primary" link @click="openForm(scope.row.id)">编辑</el-button></@shiro.hasPermission>
						<@shiro.hasPermission name="basic:model:del"><el-button size="default" type="primary" link @click="del([scope.row])">删除</el-button></@shiro.hasPermission>
						<@shiro.hasPermission name="basic:model:save"><el-button size="default" type="primary"  link v-if="scope.row.modelIsMenu == 1"  @click="dialogImportVisible=true;form.modelId = scope.row.id">导入子菜单</el-button></@shiro.hasPermission>
						<el-button v-if="scope.row.modelIsMenu == 1" type="primary" link class="copyBtn" :underline="false"
							 :data-clipboard-text="menuStr" @click="copyMenu(scope.row)">复制菜单
						</el-button>
				</template>
			</el-table-column>
			</@shiro.hasAnyPermissions>
		</el-table>
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

		data(){
			return {
				dataList: [],
				//列表
				selectionList: [],
				dialogImportVisible: false,
				form: {
					menuStr: '',
					modelId: 0,
				},
				saveDisabled: false,
				emptyText: '',
				menuStr: '',
				loading: true,//加载状态
			}
		},
		watch: {
			'dialogImportVisible': function (n, o) {
				if (!n) {
					this.form.menuStr = '';
					this.form.modelId = 0;
				}
			}
		},
		methods: {
			//查询列表
			list: function () {
				var that = this;
				that.loading = true;
				ms.http.get(ms.manager + "/basic/model/list.do", {}).then(function (res) {
					if (res.data.total <= 0) {
						that.emptyText = '暂无数据';
						that.dataList = [];
					} else {
						that.emptyText = '';
						that.dataList = ms.util.treeData(res.data.rows, 'id', 'modelId', 'modelChildList');

						//组织新的菜单提供给弹窗下拉框使用，下拉的数据需求去掉功能菜单选项
						let formModelList = [];
						res.data.rows.forEach(function(menu) {
							if(menu.modelIsMenu==1) {
								//复制菜单列表提供给表单使用
								formModelList.push(menu);
							}
						});

						if (that.$refs.form) {
							that.$refs.form.modelList = [
								{
									modelTitle: '顶级菜单',
									id: 0,
									modelChildList: ms.util.treeData(formModelList, 'id', 'modelId', 'modelChildList')
								}
							]
						}
					}
					that.loading = false;
				});
			},
			//列表选中
			handleSelectionChange: function (val) {
				this.selectionList = val;
			},
			//删除
			del: function (row) {
				var that = this;
				that.$confirm('删除选中菜单，如果有子菜单也会一并删除', '提示', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(function () {
					ms.http.post(ms.manager + "/basic/model/delete.do", row.length ? row : [row], {
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
			//新增或编辑
			openForm: function (id) {
				// 定义that方便脚手架迁移
				var that = this;
				that.$refs.form.open(id);
			},
			// 复制菜单
			copyMenu: function (menu) {
				var that = this;
				that.menuStr = JSON.stringify(new Array(menu))
				var clipboard = new ClipboardJS('.copyBtn');
				clipboard.on('success', function (e) {
					that.$message.success('菜单数据已保存到剪切板');
					clipboard.destroy();
				});
			},
			imputJson: function () {
				var that = this;
				this.saveDisabled = true
				ms.http.post(ms.manager + "/basic/model/import.do", this.form).then(function (data) {
					if (data.result) {
						that.list();
						that.saveDisabled = false
						that.dialogImportVisible = false;
						that.$notify({
							title: '成功',
							message: "请刷新当前页面，查看菜单",
							type: 'success'
						});
					} else {
						that.$notify({
							title: '失败',
							message: data.msg,
							type: 'warning'
						});
						that.saveDisabled = false
					}
				}).finally(function () {
					that.saveDisabled = false
				});
			}
		},
		mounted: function () {
			this.list();
		}
	});


</script>
<style scoped>
	#index .ms-search {
		background: #fff;
	}

	#index .iconfont {
		font-size: 12px !important;
		margin-right: 4px;
	}

	#index .ms-search .ms-search-footer {
		line-height: 60px;
		text-align: center;
	}</style>
