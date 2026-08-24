<!DOCTYPE html>
<html>
<head>
	<title>自定义配置</title>
	<#include "../../include/head-file.ftl">
	<script src="${base}/static/plugins/clipboard/clipboard.js"></script>
</head>
<body>
<div id="index" v-cloak class="ms-index">
	<el-header class="ms-header" height="50px">
		<el-col :span=24>
			<@shiro.hasPermission name="mdiy:config:del">
				<el-button type="danger" class="el-icon-delete" size="default" @click="del(selectionList)"  :disabled="!selectionList.length" >删除</el-button>
			</@shiro.hasPermission>
			<@shiro.hasPermission name="mdiy:config:importJson">
				<el-button type="primary"  class="iconfont icon-daoru" size="default" @click="dialogImportVisible=true;impForm.id=''" style="float: right;">导入</el-button>
			</@shiro.hasPermission>
			<el-tooltip class="item" effect="dark" content="如果配置数据有修改，数据获取不正确，尝试刷新缓存" placement="bottom" style="float: right; margin-right: 8px">
				<el-button type="success" plain style="float: right; margin-right: 8px"
						   class="el-icon-refresh-left" size="default"
						   @click="updateCache()">刷新缓存
				</el-button>
			</el-tooltip>

		</el-col>
	</el-header>

	<el-dialog title="导入自定义模型配置json" v-model="dialogImportVisible" width="600px"  :close-on-press-escape="false" :close-on-click-modal="false" append-to-body v-cloak>
		<el-scrollbar class="ms-scrollbar" style="height: 100%;">
			<el-form ref="form" :model="impForm" :rules="rules" size="default" label-width="130px" label-position="top" >
				<el-form-item label="是否允许前端获取数据" prop="isWebSubmit">
					<el-radio-group v-model="impForm.isWebSubmit"
									:style="{width: ''}">
						<el-radio :style="{display: true ? 'inline-block' : 'block'}" :value="item.value"
								  v-for='(item, index) in [{"value":false,"label":"不允许"},{"value":true,"label":"允许"}]' :key="item.value + index">
							{{item.label}}
						</el-radio>
					</el-radio-group>
					<div class="ms-form-tip">
						允许之后，前端可以通过接口获取配置信息
					</div>
				</el-form-item>
				<el-form-item prop="modelJson">
					<template #label>
						<span>自定义模型json</span>
					</template>
					<el-input :rows="10" type="textarea" v-model="impForm.modelJson"  placeholder="请粘贴来自代码生器中自定义模型的配置json"></el-input>
					<div class="ms-form-tip">
						将自定义模型的配置json粘贴到当前文本框 <br/>
						<b>注意:</b> 更新配置模型时会删除原有录入的数据，所以更新模型时谨慎操作。权限必须分配好，如果出现数据异常第一时间查看日志分析原因
					</div>
				</el-form-item>

			</el-form>

		</el-scrollbar>
		<template #footer>
			<el-button size="default" @click="dialogImportVisible = false">取 消</el-button>
			<el-button size="default" type="primary" @click="imputJson()">确 定</el-button>
		</template>
	</el-dialog>
	<el-dialog title="预览" v-model="dialogViewVisible" width="70%" append-to-body v-cloak>
		<el-tabs model-value="first"  @tab-change="handleClick" v-if="dialogViewVisible">
			<el-tab-pane label="显示效果" name="first">
				<ms-mdiy-form ref="modelForm" type="config" :model-id="modelId" :id="id"></ms-mdiy-form>

			</el-tab-pane>
			<el-tab-pane label="表单代码" name="second">
				<codemirror ref="second" v-if="modelFormHtml" v-model:value="modelFormHtml" :options="codemirrorOption">
				</codemirror>
				<div class="ms-form-tip">根据实际业务情况，修改表单代码来满足业务需求</div>
			</el-tab-pane>
			<el-tab-pane label="字段信息" name="third">
				<codemirror ref="third" v-if="modelField" v-model:value="modelField" :options="codemirrorOption">
				</codemirror>
				<div class="ms-form-tip">表单项的 <span class="key">name</span> 对应字段信息的<span class="key"> model</span> 属性
				</div>
			</el-tab-pane>
		</el-tabs>
	</el-dialog>
	<div class="ms-search" style="padding: 20px 10px 0 10px;">
		<el-row>
			<el-form :model="form"  ref="searchForm"  label-width="85px" size="default">
				<el-row>
					<el-col :span=8>
						<el-form-item  label="配置名称" prop="modelName">
							<el-input v-model="form.modelName"
									  :disabled="false"
									  :style="{width:  '100%'}"
									  :clearable="true"
									  placeholder="请输入完整配置名称，不支持模糊查询">
							</el-input>
						</el-form-item>
					</el-col>
					<el-col :span=16 style="text-align: right">
						<el-button type="primary" class="el-icon-search" size="default" @click="loading=true;currentPage=1;list(true)">查询</el-button>
						<el-button class="el-icon-refresh" @click="rest" size="default">重置</el-button>
					</el-col>
				</el-row>
			</el-form>
		</el-row>
	</div>

	<el-main class="ms-container">
		<el-alert
				class="ms-alert-tip"
				title="功能介绍"
				type="info">
			<template #title>
				功能介绍
			</template>
			满足配置化数据的业务管理，例如：上传设置、会员设置、支付宝设置。<br/>
			注意:如果给管理员赋予其中一个配置更新权限,管理员会拥有所有配置的更新权限,请谨慎赋予查看权限
		</el-alert>
		<el-table v-loading="loading" ref="multipleTable" height="calc(100vh-68px)" class="ms-table-pagination" border :data="dataList" tooltip-effect="dark" @selection-change="handleSelectionChange">
			<template #empty>
				{{emptyText}}
			</template>
			<el-table-column :selectable="isChecked" type="selection" width="40"></el-table-column>
			<el-table-column align="left" prop="modelName">
				<template #header>
					配置名称
					<el-popover placement="top-start" title="提示" trigger="hover" >
						提供给业务中调用 如：ConfigUtil.getString("配置名称","字段名称")
						<template #reference>
							<i class="el-icon-question"></i>
						</template>
					</el-popover>
				</template>
			</el-table-column>
			<el-table-column  min-width="100" align="center" label="允许前端获取" prop="modelJson">
				<template #header>允许前端获取
					<el-popover placement="top-start" title="提示" trigger="hover" >
						只有前端必要获取的配置才允许,默认不允许
						<template #reference>
							<i class="el-icon-question"></i>
						</template>
					</el-popover>
				</template>
				<template #default="scope">
					<div v-if="scope.row.modelJson && JSON.parse(scope.row.modelJson).isWebSubmit">
						允许
					</div>
					<span v-else>—</span>
				</template>
			</el-table-column>

			<el-table-column align="center" width="500">
				<template #header>操作
					<el-popover placement="top-start" title="提示" trigger="hover" >
						复制菜单JSON：复制的数据可以在 权限管理>菜单管理 导入，方便快速创建菜单使用 <br/>
						更新模型：可以通过重复导入JSON配置来修改对应的表结构 <br/>
						表单预览：显示表单各个控件的效果 <br/>
						数据预览：管理业务数据入口
						<template #reference>
							<i class="el-icon-question"></i>
						</template>
					</el-popover>
				</template>
				<template #default="scope" >
					<@shiro.hasPermission name="mdiy:config:del">
						<el-link :underline="false" v-if="scope.row.notDel == 0" type="primary" @click="del([scope.row])">删除</el-link>
					</@shiro.hasPermission>
					<@shiro.hasPermission name="mdiy:config:update">
						<el-link type="primary" :underline="false"  @click="dialogImportVisible=true;impForm.id=scope.row.id">更新模型</el-link>
					</@shiro.hasPermission>
					<el-link type="primary" :underline="false" class="copyBtn" :data-clipboard-text="JSON.stringify(modelJson)" @click="copyModelJson(scope.row)">复制模型JSON</el-link>
					<el-link type="primary" :underline="false" class="copyBtn" :data-clipboard-text="JSON.stringify(menuJson)" @click="copyJson(scope.row)">复制菜单JSON</el-link>
					<el-link type="primary" :underline="false"  @click="view(scope.row)">表单预览</el-link>
					<el-link type="primary" :underline="false" @click="ms.util.openSystemUrl('/mdiy/config/data/form.do?modelId='+scope.row.id)">数据预览</el-link>
				</template>
			</el-table-column>
		</el-table>
		<el-pagination
				background
				:page-sizes="[10, 20, 50, 100]"
				layout="total, sizes, prev, pager, next, jumper"
				:current-page="currentPage"
				:page-size="pageSize"
				:total="total"
				class="ms-pagination"
				@current-change='currentChange'
				@size-change="sizeChange">
		</el-pagination>
	</el-main>

<!--仅为导入配置时初始化自定义配置值所用-->
<div id="configModel"></div>
</div>
</body>

</html>
<script>
	var form = new _Vue({
		el: '#index',
		data: function () {
			return {
				//自定义模型列表
				dataList: [],
				//自定义模型列表选中
				selectionList: [],
				//总记录数量
				total: 0,
				//页面数量
				pageSize: 20,
				//初始页
				currentPage: 1,
				modelTypeOptions: [],
				dialogViewVisible: false,
				dialogImportVisible: false,
				loading: true,
				//自定义模型html
				modelFormHtml: '',
				modelId: '',
				modelName: '',
				//表单字段
				modelField: '',
				emptyText: '',
				//搜索配置
				form: {
					// 模型名称
					modelName: '',
				},
				impForm: {
					//模型编号
					id:'',
					// 是否允许前端获取数据，避免新增bean的字段，沿用isWebSubmit
					isWebSubmit:false,
					// 模型名称
					modelJson: '',
				},
				//配置验证
				rules: {
					modelJson: [{
						required: true,
						message: 'json数据不能为空',
						trigger: 'blur'
					}]
				},
				menuJson: [
					{
						modelIsMenu: 1,
						modelTitle: "",
						modelUrl: "",
						appId: "",
						modelChildList: [
							{
								modelIsMenu: 0,
								modelTitle: "查看",
								modelUrl: "mdiy:configData:view",
								appId: "",
							},
							{
								modelIsMenu: 0,
								modelTitle: "更新",
								modelUrl: "mdiy:configData:update",
								appId: "",
							}
						]
					}
				],
				modelJson: {
					searchJson: '',
					field: '',
					html: '',
					title: '',
					script: '',
					sql: '',
					tableName: ''
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
					},
					tags: {
						style: [["type", /^text\/(x-)?scss$/, "text/x-scss"],
							[null, null, "css"]],
						custom: [[null, null, "customMode"]]
					}
				},

				historyKey: "mdiy_config_history"
			}
		},
		watch: {
			'dialogImportVisible': function (n, o) {
				if (!n) {
					this.$refs.form.resetFields();
					this.form.id = "";
				}
			},
		},
		methods: {
			//设置禁止选中的条件： 若返回为 true， 则可以选中，否则禁止选中。
			isChecked: function(row) {
				return row.notDel == 0
			},
			//查询列表
			list: function (isSearch) {
				var that = this;
				var data = {}; //搜索参数
				that.loading = true;
				var page={
					pageNo: that.currentPage,
					pageSize : that.pageSize
				}
				var form = JSON.parse(JSON.stringify(that.form))


				if(isSearch) {
					//删除空字符串
					for (var key in form){
						if(form[key] === undefined || form[key] === null){
							delete  form[key]
						}
					}
					form.sqlWhere ? data = Object.assign({}, {sqlWhere: form.sqlWhere}, page) : data = Object.assign({}, form, page)
				} else {
					data = page;
				}

				sessionStorage.setItem(this.historyKey,JSON.stringify({form:form,page:page}));
				ms.http.get(ms.manager + "/mdiy/config/list.do", data).then(function (data) {
					if(data.result){
						if (data.data.total <= 0) {
							that.loading = false;
							that.emptyText = '暂无数据';
							that.dataList = [];
						} else {
							that.emptyText = '';
							that.loading = false;
							that.dataList = data.data.rows;
						}
						that.total = data.data.total;
					}
				});
			},
			//自定义模型列表选中
			handleSelectionChange: function (val) {
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
					ms.http.post(ms.manager + "/mdiy/config/delete.do", row.length ? row : [row], {
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
			//预览
			view: function(row) {
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
				this.id = row.id;
				this.modelName = row.modelName;
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
				this.modelJson.tableName = _modelJson.tableName;
				var clipboard = new ClipboardJS('.copyBtn');
				var that = this;
				clipboard.on('success', function (e) {
					that.$notify({
						title: '提示',
						message: '模型json已保存到剪切板,请在菜单管理中已有的父级菜单中导入',
						type: 'success'
					});
					clipboard.destroy();
				});
			},
			imputJson: function () {
				var that = this;
				var url = "/mdiy/config/importJson.do";
				if(that.impForm.id &&  that.impForm.id!=''){
					url = "/mdiy/config/updateJson.do";
				}else {
					// 新导入数据重置到列表第一页
					that.currentPage = 1;
					that.form = {};
				}

				that.$confirm('此操作会进行更新表操作，请谨慎操作', '提示', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(function () {
					// 控制前端接口isWebSubmit，保存至json
					that.$refs.form.validate(function (valid) {
						var object = {};
						try {
							object = JSON.parse(that.impForm.modelJson);
							object.isWebSubmit = that.impForm.isWebSubmit;
							that.impForm.modelJson = JSON.stringify(object);
						}catch(e) {
							that.$notify({
								title: '失败',
								message: "json格式不匹配，请粘贴正确的自定义模型JSON代码",
								type: 'warning'
							});
						};

						if (valid) {
							ms.http.post(ms.manager + url, that.impForm).then(function (data) {
								if (data.result) {
									that.form = {};
									that.dialogImportVisible=false;
									var msg = "更新模型成功";
									if(!(that.impForm.id &&  that.impForm.id!='')){
										msg = "导入模型成功"
									};

									that.list();
									that.$notify({
										title: '成功',
										message: msg,
										type: 'success'
									});
								} else {
									that.$notify({
										title: '失败',
										message: data.msg,
										type: 'warning'
									});
								}
							})
						}
					});
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
			//重置配置
			rest: function () {
				this.currentPage = 1;
				this.loading = true;
				this.$refs.searchForm.resetFields();
				this.list();
			},
			//监听代码预览窗口tab点击
			handleClick: function (tab, event) {
				setTimeout(() => {
					if(tab!="first") {
						this.$refs[tab].resize()
					}
				}, 200)
			},
			//复制json
			copyJson: function (row) {
				// 设置id当权限标识
				this.menuJson[0].modelChildList[0].modelUrl = 'mdiy:configData:'+row.id+':view'
				this.menuJson[0].modelChildList[0].appId = row.appId;
				this.menuJson[0].modelChildList[1].modelUrl = 'mdiy:configData:'+row.id+':update'
				this.menuJson[0].modelChildList[1].appId = row.appId;
				this.menuJson[0].modelTitle=row.modelName;
				this.menuJson[0].modelUrl="mdiy/config/data/form.do?modelId="+row.id+"&isEditor=true";
				this.menuJson[0].appId = row.appId;
				var clipboard = new ClipboardJS('.copyBtn');
				var self = this;
				clipboard.on('success', function (e) {
					self.$notify({
						title: '提示',
						message: '菜单json已保存到剪切板,请在菜单管理中已有的父级菜单中导入',
						type: 'success'
					});
					clipboard.destroy();
				});
			},
			//刷新缓存
			updateCache: function () {
				var that = this;
				ms.http.post(ms.manager + "/mdiy/config/updateCache.do").then(function (data) {
					if (data.result) {
						that.$notify({
							title: '成功',
							type: 'success',
							message: '刷新成功!'
						});
						that.list();
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
		mounted: function () {
			//如果存在历史参数，恢复页面结果
			if(sessionStorage.getItem(this.historyKey) && ms.util.getParameter("isBack") == 'true') {
				var _history = JSON.parse(sessionStorage.getItem(this.historyKey))
				this.form = _history.form;
				this.total = parseInt(_history.total);
				this.currentPage = parseInt(_history.page.pageNo);
				this.pageSize = parseInt(_history.page.pageSize);
			}
			this.list(true);
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
	.el-scrollbar__wrap {
		margin-right: -30px !important;
	}

	#configModel {
		display: none;
	}
	/**
    tab标题会多出空白
    */
	:deep(*  .el-tabs__item) {
		background-color: unset !important;
	}

</style>
