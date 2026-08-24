<!DOCTYPE html>
<html>
<head>
	<title>自定义业务</title>
	<#include "../../include/head-file.ftl">

</head>
<body>
<div id="index" v-cloak class="ms-index">
	<el-header class="ms-header" height="50px">
		<el-col :span="24">
			<@shiro.hasPermission name="mdiy:form:del">
				<el-button type="danger" class="el-icon-delete" size="default" @click="del(selectionList)"  :disabled="!selectionList.length">删除</el-button>
			</@shiro.hasPermission>
			<@shiro.hasPermission name="mdiy:form:importJson">
				<el-button type="primary" class="iconfont icon-daoru" size="default" @click="dialogImportVisible=true;impForm.id='';impForm.isWebSubmit=false" style="float: right">导入</el-button>
			</@shiro.hasPermission>
		</el-col>
	</el-header>
	<el-dialog title="导入自定义模型配置json(自定义业务)" v-model="dialogImportVisible" width="600px" :close-on-press-escape="false" :close-on-click-modal="false" append-to-body v-cloak>

		<el-scrollbar class="ms-scrollbar" style="height: 100%;">
			<el-form ref="form" :model="impForm" :rules="rules" size="default" label-width="180px" label-position="top" >
				<el-form-item  label="是否允许前端提交" prop="isWebSubmit">
					<el-radio-group v-model="impForm.isWebSubmit"
									:style="{width: ''}"
									:disabled="false">
						<el-radio :style="{display: true ? 'inline-block' : 'block'}" :value="item.value"
								  v-for='(item, index) in [{"value":false,"label":"不允许"},{"value":true,"label":"允许"}]' :key="item.value + index">
							{{item.label}}
						</el-radio>
					</el-radio-group>
					<div class="ms-form-tip">
						开启之后可以通过接口提交业务数据，可以实现类似：<b>在线留言</b> 数据收集
					</div>
				</el-form-item>
				<el-form-item v-if="impForm.isWebSubmit"  label="是否开启前端验证码校验" prop="isWebCode">
					<el-radio-group v-model="impForm.isWebCode"
									:style="{width: ''}"
									:disabled="!impForm.isWebSubmit">
						<el-radio :style="{display: true ? 'inline-block' : 'block'}" :value="item.value"
								  v-for='(item, index) in [{"value":false,"label":"关闭"},{"value":true,"label":"开启"}]' :key="item.value + index">
							{{item.label}}
						</el-radio>
					</el-radio-group>
					<div class="ms-form-tip">
						开启之后，前端接口将会进行验证码校验
					</div>
				</el-form-item>
				<el-form-item  label="自定义模型json" prop="modelJson">
					<el-input
							type="textarea" :rows="10"
							:disabled="false"
							:readonly="false"
							v-model="impForm.modelJson"
							:style="{width: '100%'}"
							placeholder="请粘贴来自代码生器中自定义模型的配置json">
					</el-input>
					<div class="ms-form-tip">
						将自定义模型的配置json粘贴到当前文本框 <br/>
						<b>注意:</b> 更新模型时会根据新的 <b>自定义模型</b>配置 来修改对应的表结构，包括删除字段、新增字段。字段有任何设置的变化都会进行先删除再新增，所以更新模型时谨慎操作，权限必须分配好，如果出现数据异常第一时间查看日志分析原因
					</div>
				</el-form-item>
			</el-form>
		</el-scrollbar>
		<template #footer>
			<el-button size="default" @click="dialogImportVisible = false">取 消</el-button>
			<el-button size="default" type="primary" @click="imputJson()">确 定</el-button>
		</template>
	</el-dialog>

	<el-dialog title="表单预览" v-model="dialogViewVisible" width="70%" append-to-body v-cloak>
		<el-tabs model-value="first"  @tab-change="handleChange" v-if="dialogViewVisible">
			<el-tab-pane label="显示效果" name="first">
				<#--说明 ms-mdiy-form，组件控制 搜索框 的正常显示，不能去除	-->
				<ms-mdiy-form ref="modelForm" type="form" :model-id="modelId"></ms-mdiy-form>


			</el-tab-pane>
			<el-tab-pane label="表单代码" name="second">
				<codemirror ref="second" v-if="modelFormHtml" v-model:value="modelFormHtml" :options="codemirrorOption" ></codemirror>
				<div class="ms-form-tip">根据实际业务情况，修改表单代码来满足业务需求</div>
			</el-tab-pane>
			<el-tab-pane label="字段信息" name="third">
				<codemirror ref="third" v-if="modelField" v-model:value="modelField" :options="codemirrorOption"></codemirror>
				<div class="ms-form-tip">表单项的 <span class="key">name</span> 对应字段信息的<span class="key"> model</span> 属性</div>
			</el-tab-pane>
		</el-tabs>
	</el-dialog>

	<div class="ms-search" style="padding: 20px 10px 0 10px;">
		<el-row>
			<el-form :model="form"  ref="searchForm"  label-width="85px" size="default">
				<el-row>
					<el-col :span="8">
						<el-form-item  label="业务名称" prop="modelName">
							<el-input v-model="form.modelName"
									  :disabled="false"
									  :style="{width:  '100%'}"
									  :clearable="true"
									  placeholder="请输入完整名称，不支持模糊查询">
							</el-input>

						</el-form-item>
					</el-col>
					<el-col :span="16" style="text-align: right">
						<el-button type="primary" class="el-icon-search" size="default" @click="loading=true;currentPage=1;list(true)">查询</el-button>
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
				description="导入自定义模型，可以快速实现基础后台数据管理，也可以实现表单数据提交，例如：留言反馈">
			<template #title>
				功能介绍
			</template>
		</el-alert>
		<el-table v-loading="loading" ref="multipleTable" height="calc(100vh-68px)" class="ms-table-pagination" border :data="treeList" tooltip-effect="dark" @selection-change="handleSelectionChange">
			<template #empty>
				{{emptyText}}
			</template>
			<el-table-column type="selection" width="40" :selectable="isChecked"></el-table-column>
			<el-table-column  min-width="80" align="left" prop="modelName">
				<template #header>业务名称
					<el-popover placement="top-start" title="提示" trigger="hover" >
						提供给业务中调用 如：ms.mdiy.model.form(divID, "业务名称");
						<template #reference>
							<i class="el-icon-question"></i>
						</template>
					</el-popover>
				</template>
			</el-table-column>
			<el-table-column label="业务表名称" min-width="160" align="left" prop="modelTableName">

			</el-table-column>
			<el-table-column  min-width="100" align="center" label="允许前端提交" prop="modelJson">
				<template #header>允许前端提交
					<el-popover placement="top-start" title="提示" trigger="hover" >
						允许前端提交可以实现表单数据收集的效果，具体查看手册前端调用方式
						<template #reference>
							<i class="el-icon-question"></i>
						</template>
					</el-popover>
				</template>
				<template #default="scope">
					<div v-if="scope.row.modelJson && JSON.parse(scope.row.modelJson).isWebSubmit">
						允许
					</div>
					<span v-else>不允许</span>
				</template>
			</el-table-column>
			<el-table-column  min-width="100" align="center" label="前端验证码校验" prop="modelJson">
				<template #header>前端验证码校验
					<el-popover placement="top-start" title="提示" trigger="hover" >
						开启之后，前端接口将会进行验证码校验
						<template #reference>
							<i class="el-icon-question"></i>
						</template>
					</el-popover>
				</template>
				<template #default="scope">
					<div v-if="scope.row.modelJson && JSON.parse(scope.row.modelJson).isWebCode">
						开启
					</div>
					<span v-else>关闭</span>
				</template>
			</el-table-column>


			<el-table-column  align="center"  width="480">
				<template #header>操作
					<el-popover placement="top-start" title="提示" trigger="hover" >
						复制菜单JSON：复制的数据可以在 权限管理>菜单管理 导入，方便快速创建菜单使用 <br/>
						注意：只能是导入到已有的父菜单中 <br/>
						更新模型：可以通过重复导入JSON配置来修改对应的表结构，更新模型是对原模型进行覆盖 <br/>
						表单预览：显示表单各个控件的效果 <br/>
						数据预览：管理业务数据入口
						<template #reference>
							<i class="el-icon-question"></i>
						</template>
					</el-popover>
				</template>
				<template #default="scope">
					<@shiro.hasPermission name="mdiy:form:update">
						<el-link type="primary" :underline="false"  @click="openDialogImport(scope.row)">更新模型</el-link>
					</@shiro.hasPermission>
					<el-link type="primary" :underline="false" class="copyBtn" :data-clipboard-text="JSON.stringify(menuJson)" @click="copyJson(scope.row)">复制菜单JSON</el-link>
					<el-link type="primary" :underline="false" class="copyBtn" :data-clipboard-text="JSON.stringify(modelJson)" @click="copyModelJson(scope.row)">复制模型JSON</el-link>
					<el-link type="primary" :underline="false"  @click="view(scope.row)">表单预览</el-link>
					<el-link type="primary" :underline="false"  @click="ms.util.openSystemUrl('/mdiy/form/data/index.do?modelId=' + scope.row.id+'&modelName='+scope.row.modelName)" >数据预览</el-link>
					<@shiro.hasPermission name="mdiy:form:del">
						<el-link :underline="false"  v-if="scope.row.notDel == 0" type="primary" @click="del([scope.row])">删除</el-link>
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
</div>
</body>

</html>
<script>
	var indexVue = new _Vue({
		el: '#index',
		data: function (){
			return {
				modelId: "",
				modelName: "",
				treeList: [],
				//自定义模型列表
				selectionList: [],
				//自定义模型列表选中
				total: 0,
				//总记录数量
				pageSize: 20,
				//页面数量
				currentPage: 1,
				//初始页
				modelTypeOptions: [],
				dialogViewVisible: false,
				dialogImportVisible: false,
				loading: true,
				emptyText: "",
				//自定义模型html
				modelFormHtml: null,
				//表单字段
				modelField: null,
				//搜索表单
				form: {
					// 模型名称
					modelName: "",
				},
				impForm: {
					//模型编号
					id: "",
					// 模型名称
					modelJson: "",
					// 是否允许前端提交
					isWebSubmit: false,
					// 是否开启前端验证码校验
					isWebCode: true,
				},
				//表单验证
				rules: {
					modelJson: [
						{
							required: true,
							message: "json数据不能为空",
							trigger: "blur",
						},
					],
					// 是否允许前端提交
					isWebSubmit: [{ required: true, message: "是否允许前端提交不能为空" }],
				},
				menuJson: [
					{
						modelIsMenu: 1,
						modelTitle: "",
						modelUrl: "",
						modelChildList: [
							{
								modelIsMenu: 0,
								modelTitle: "更新",
								modelUrl: "mdiy:formData:update",
							},
							{
								modelIsMenu: 0,
								modelTitle: "查看",
								modelUrl: "mdiy:formData:view",
							},
							{
								modelIsMenu: 0,
								modelTitle: "保存",
								modelUrl: "mdiy:formData:save",
							},
							{
								modelIsMenu: 0,
								modelTitle: "删除",
								modelUrl: "mdiy:formData:del",
							},
						],
					},
				],
				modelJson: {
					searchJson: "",
					field: "",
					html: "",
					title: "",
					script: "",
					sql: "",
					tableName: "",
				},
				//设置
				codemirrorOption: {
					tabSize: 4,
					styleActiveLine: true,
					lineNumbers: true,
					line: true,
					styleSelectedText: true,
					lineWrapping: true,
					mode: 'text/html',
					matchBrackets: true,
					showCursorWhenSelecting: true,
					hintOptions: {
						completeSingle: false
					}
				},
				historyKey: "mdiy_form_history"
			};
		},
		watch: {
			dialogImportVisible: function (n, o) {
				if (!n) {
					this.$refs.form.resetFields();
					this.form.id = "";
				}
			},
		},
		methods: {

			// 更新模型弹窗
			openDialogImport: function (row) {
				var that = this;
				if (row.id) {
					that.impForm.id=row.id;
					that.$nextTick(function () {
						that.dialogImportVisible=true;
					});
				}
			},

			//查询列表
			list: function (isSearch) {
				var that = this;
				var data = {}; //搜索参数
				that.loading = true;
				var page = {
					pageNo: that.currentPage,
					pageSize: that.pageSize,
				};
				var form = JSON.parse(JSON.stringify(that.form));

				if (isSearch) {
					//删除空字符串
					for (var key in form) {
						if (form[key] === undefined || form[key] === null) {
							delete form[key];
						}
					}
					form.sqlWhere
							? (data = Object.assign({}, { sqlWhere: form.sqlWhere }, page))
							: (data = Object.assign({}, form, page));
				} else {
					data = page;
				}
				sessionStorage.setItem(this.historyKey,JSON.stringify({form: form, page: page}));
				ms.http
						.get(ms.manager + "/mdiy/form/list.do", data)
						.then(function (data) {
							if (data.result) {
								if (data.data.total <= 0) {
									that.loading = false;
									that.emptyText = "暂无数据";
									that.treeList = [];
								} else {
									that.emptyText = "";
									that.loading = false;
									that.treeList = data.data.rows;
								}
								that.total = data.data.total;
							} else {
								that.$notify({
									title: "失败",
									message: data.msg,
									type: "warning",
								});
							}
						});
			},
			//自定义模型列表选中
			handleSelectionChange: function (val) {
				this.selectionList = val;
			},
			//删除
			del: function (row) {
				var that = this;
				that.$confirm("此操作将永久删除所选内容，以及对应业务表, 是否继续?","提示", {
					confirmButtonText: "确定",
					cancelButtonText: "取消",
					type: "warning",
				}).then(function () {
					ms.http.post(ms.manager + "/mdiy/form/delete.do",
									row.length ? row : [row],
									{
										headers: {
											"Content-Type": "application/json",
										},
									}
							).then(function (data) {
								if (data.result) {
									that.$notify({
										title: "成功",
										type: "success",
										message: "删除成功!",
									}); //删除成功，刷新列表
									that.list();
								} else {
									that.$notify({
										title: "失败",
										message: data.msg,
										type: "warning",
									});
								}
							});
						});
			},

			//预览
			view: function (row) {
				var data = JSON.parse(row.modelJson);
				var modelField = row.modelField;
				this.loading = true;
				var that = this;
				this.dialogViewVisible = true;
				window.formVue = this;
				that.modelFormHtml = data.html + "<script>" + data.script + "<\/script>";
				that.modelField = modelField;
				this.loading = false;
				//提供给自定义模型
				this.modelId = row.id;
				this.modelName = row.modelName;
			},
			imputJson: function () {
				var that = this;
				var url = "/mdiy/form/importJson.do";
				if (that.impForm.id && that.impForm.id != "") {
					url = "/mdiy/form/updateJson.do";
				}else {
					// 新导入数据重置到列表第一页
					that.currentPage = 1;
					that.form = {};
				}
				that.$confirm("此操作会进行更新表操作，请谨慎操作", "提示", {
							confirmButtonText: "确定",
							cancelButtonText: "取消",
							type: "warning",
						}).then(function () {
							// 控制前端接口isWebSubmit，保存至json
							that.$refs.form.validate(function (valid) {
								var object = {};
								try {
									object = JSON.parse(that.impForm.modelJson);
									object.isWebSubmit = that.impForm.isWebSubmit;
									object.isWebCode = that.impForm.isWebCode;
									that.impForm.modelJson = JSON.stringify(object);
								} catch (e) {
									that.$notify({
										title: "失败",
										message:
												"json格式不匹配，请粘贴正确的自定义模型JSON代码",
										type: "warning",
									});
								}

								if (valid) {
									ms.http.post(ms.manager + url, that.impForm).then(function (res) {
										if (res.result) {
											that.form = {};
											that.dialogImportVisible = false;
											that.list();
											that.$notify({
												title: "成功",
												message: "更新成功",
												type: "success",
											});
										} else {
											that.$notify({
												title: "失败",
												message: res.msg,
												type: "warning",
											});
										}
									});
								}
							});
						});
			},
			//监听代码预览窗口tab点击
			handleChange: function (tab, event) {
				setTimeout(() => {
					if(tab!="first") {
						this.$refs[tab].resize()
					}
				}, 200)
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
			isChecked: function (row) {
				return row.notDel == 0;
			},
			//重置表单
			rest: function () {
				this.currentPage = 1;
				this.loading = true;
				this.$refs.searchForm.resetFields();
				this.list();
			},
			//复制json
			copyJson: function (row) {
				this.menuJson[0].modelTitle = row.modelName;
				this.menuJson[0].modelChildList[0].modelUrl =
						"mdiy:formData:" + row.id + ":update";
				this.menuJson[0].modelChildList[1].modelUrl =
						"mdiy:formData:" + row.id + ":view";
				this.menuJson[0].modelChildList[2].modelUrl =
						"mdiy:formData:" + row.id + ":save";
				this.menuJson[0].modelChildList[3].modelUrl =
						"mdiy:formData:" + row.id + ":del";
				this.menuJson[0].modelUrl =
						"mdiy/form/data/index.do?modelId=" + row.id+"&isEditor=true";
				var clipboard = new ClipboardJS(".copyBtn");
				var self = this;
				clipboard.on("success", function (e) {
					self.$notify({
						title: "提示",
						message: "菜单json已保存到剪切板，请在菜单管理中已有的父级菜单中导入",
						type: "success",
					});
					clipboard.destroy();
				});
			},
			//复制自定义模型json
			copyModelJson: function (row) {
				var _modelJson = JSON.parse(row.modelJson);
				this.modelJson.searchJson = _modelJson.searchJson;
				this.modelJson.field = row.modelField;
				this.modelJson.html = _modelJson.html;
				this.modelJson.title = row.modelName;
				this.modelJson.script = _modelJson.script;
				this.modelJson.sql = _modelJson.sql;
				this.modelJson.form = _modelJson.form;
				this.modelJson.tableName = _modelJson.tableName;
				this.modelJson.id = _modelJson.id;
				var clipboard = new ClipboardJS(".copyBtn");
				var that = this;
				clipboard.on("success", function (e) {
					that.$notify({
						title: "提示",
						message: "模型json已保存到剪切板",
						type: "success",
					});
					clipboard.destroy();
				});
			},
		},
		mounted: function () {
			//如果存在历史参数，恢复页面结果
			if(sessionStorage.getItem(this.historyKey) && ms.util.getParameter("isBack") == 'true') {
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
<style>
	#index .iconfont{
		font-size: 12px;
		margin-right: 5px;
	}
	.el-dialog__body {
		padding: 0 20px 30px 20px;
	}
	.CodeMirror {
		border: 1px solid #eee;
	}
	/*饿了么滚动样式*/
	.el-scrollbar__wrap{
		margin-right: -30px!important;
	}
	/**
	tab标题会多出空白
	*/
	:deep(*  .el-tabs__item) {
		background-color: unset !important;
	}
</style>

