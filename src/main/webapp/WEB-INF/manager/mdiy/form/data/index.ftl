<!DOCTYPE html>
<html>
<head>
	<title>自定义表单数据列表</title>
	<#include "../../include/head-file.ftl">
	<script src="${base}/static/mdiy/index.js"></script>
</head>
<body>
<div id="index" v-cloak >
	<div  class="ms-index">
		<el-header class="ms-header" height="50px">
			<el-col :span=12>
				<el-button v-if="ms.util.includes(ms.managerPermissions,'mdiy:formData:save')||ms.util.includes(ms.managerPermissions,'mdiy:formData:'+modelId+':save')" type="primary" class="el-icon-plus" size="default" @click="save()">新增</el-button>
				<el-button v-if="ms.util.includes(ms.managerPermissions,'mdiy:formData:del')||ms.util.includes(ms.managerPermissions,'mdiy:formData:'+modelId+':del')" type="danger" class="el-icon-delete" size="default" @click="del(selectionList)"  :disabled="!selectionList.length">删除</el-button>
			</el-col>
			<!-- 菜单点击到当前页面就没有返回按钮-->
			<el-col :span=12 class="ms-tr" >
				<el-button size="default"  v-if="!isEditor"  plain @click="back()"><i class="iconfont icon-fanhui"></i>返回</el-button>
			</el-col>
		</el-header>
		<div class="ms-search">
			<el-form :model="form"  ref="searchForm"  size="default"  label-width="120px">
				<el-row v-if="hasSearch">
					<template v-for="(field,index) in searchJson" >
						<el-col :span=6  v-if="field.isSearch">
							<el-form-item :label="field.name" v-if="field.type=='input'">
								<el-input
										v-model="form[field.model]"
										:disabled="false"
										:style="{width:  '100%'}"
										:clearable="true">
								</el-input>
							</el-form-item>
							<el-form-item :label="field.name" v-if="field.type=='select'">
								<el-select style="width: 100%;"  v-model="form[field.model]" size="default" :clearable="true">
									<el-option v-for='item in $refs.modelForm.getForm()[field.model+"Options"]' :key="item[field.key]" :value="item[field.key]"
											   :label="item[field.title]">
									</el-option>
								</el-select>
							</el-form-item>
							<el-form-item :label="field.name" v-if="field.type=='date'">
								<el-date-picker
										:clearable="true"
										style="width: 100%;"
										size="default"
										:value-format="field.fmt"
										v-model="form[field.model]">
								</el-date-picker>

							</el-form-item>
						</el-col>
					</template>
				</el-row>
				<div style="display: flex;justify-content: end;">
					<el-button v-if="hasSearch" type="primary" class="el-icon-search" size="default" @click="form.sqlWhere=null;loading=true;currentPage=1;list(true)">查询</el-button>
					<el-button @click="rest"  class="el-icon-refresh" size="default">重置</el-button>
					<ms-search ref="search" @search="search" :search-json="searchJson" :search-key="historyKey" :is-model=true></ms-search>
				</div>
			</el-form>
		</div>
		<el-main class="ms-container">
			<el-table  v-loading="loading" ref="multipleTable" height="calc(100vh-68px)" class="ms-table-pagination" border :data="dataList" tooltip-effect="dark" @selection-change="handleSelectionChange">
				<template #empty>
					<el-empty :description="emptyText"></el-empty>
				</template>
				<el-table-column type="selection" width="40"></el-table-column>

				<template  v-for="(field,index) in tableField" :key="index">
					<template v-if="field.isShow == 'true' || field.isShow==true">
						<el-table-column  min-width="100"  :label="field.name" :prop="field.key" v-if="field.type == 'input' || field.type == 'autoid' || field.type == 'amap'  ">
						</el-table-column>
						<el-table-column min-width="100" :label="field.name" :prop="field.key" v-else-if=" field.type == 'number' " >
						</el-table-column>
						<el-table-column min-width="100" :label="field.name" :prop="field.key" v-else-if=" field.type == 'money' "  :formatter="moneyFormatter">
						</el-table-column>
						<el-table-column min-width="120" :label="field.name" :prop="field.key" v-else-if="field.type == 'select' || field.type == 'radio' || field.type == 'checkbox' || field.type == 'cascader' || field.type=='province_id'"  :formatter="fmt">
						</el-table-column>
						<el-table-column min-width="190" width="200" align="center" :label="field.name" :prop="field.key" v-else-if="field.type == 'date'">

							<template #default="scope">
								<span v-if="field.javaType=='String'" v-text="scope.row[field.key]"></span>
								<span v-else-if="field.endModel" v-html="scope.row[field.key] && ms.util.date.fmt(scope.row[field.key],field.fmt)+' 至 '+ms.util.date.fmt(scope.row[field.endModel],field.fmt)"></span>
								<span v-else-if="field.fmt" v-text="scope.row[field.key] && ms.util.date.fmt(scope.row[field.key],field.fmt)"></span> <!--排除范围-->
								<span v-else v-text="scope.row[field.key] && ms.util.date.fmt(scope.row[field.key],'yyyy-MM-dd')"></span>
							</template>

						</el-table-column>
						<el-table-column min-width="190" width="200" align="center" :label="field.name" :prop="field.key" v-else-if="field.type == 'time'">

							<template #default="scope">
								<span v-if="field.endModel" v-html="ms.util.date.fmt(scope.row[field.key],field.fmt)+' 至 '+ms.util.date.fmt(scope.row[field.endModel],field.fmt)"></span>
								<span v-else-if="field.fmt" v-text="ms.util.date.fmt(scope.row[field.key],field.fmt)"></span> <!--排除范围-->
								<span v-else v-text="ms.util.date.fmt(scope.row[field.key],field.fmt)"></span>
							</template>

						</el-table-column>

						<el-table-column min-width="120" :label="field.name" align="left" :prop="field.key" v-else-if="field.type == 'imgupload' ">
							<template #default="scope">
								<template v-if="scope.row[field.key]">
									<div class="block"  v-for="src in scope.row[field.key]">
										<el-image
												style="width: 50px; height: 50px;float: left;margin-right: 5px;"
												:preview-teleported="true"
												:src="(!src.url.startsWith('http://') && !src.url.startsWith('https://')) ? ms.contextpath + src.url : src.url"
												:preview-src-list="[(!src.url.startsWith('http://') && !src.url.startsWith('https://')) ? ms.contextpath + src.url : src.url]">
										</el-image>
									</div>
								</template>
							</template>
						</el-table-column>

						<el-table-column min-width="100" :label="field.name" :prop="field.key" v-else-if=" field.type == 'slider' " >
						</el-table-column>
						<el-table-column min-width="100" :label="field.name" :prop="field.key" v-else-if=" field.type == 'color' " >
						</el-table-column>
						<el-table-column min-width="80" :label="field.name" :prop="field.key" v-else-if=" field.type == 'rate' " >
						</el-table-column>



					</template>
				</template>

				<el-table-column label="操作" align="center" width="200" fixed="right">
					<template #default="scope">
						<el-link v-if="ms.util.includes(ms.managerPermissions,'mdiy:formData:update')||ms.util.includes(ms.managerPermissions,'mdiy:formData:'+modelId+':update')" type="primary" :underline="false" @click="save(scope.row.id)">编辑</el-link>
						<el-link v-if="ms.util.includes(ms.managerPermissions,'mdiy:formData:del')||ms.util.includes(ms.managerPermissions,'mdiy:formData:'+modelId+':del')" type="primary" :underline="false"  @click="del(scope.row)">删除</el-link>

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
	<ms-mdiy-form style="display: none;" ref="modelForm" type="form" :model-id="modelId"></ms-mdiy-form>
</div>

</body>
</html>

<script>

	var indexVue = new _Vue({
		el: '#index',
		provide() {
			return {
				searchParent: this //筛选使用
			};
		},
		data: function () {
			return {
				mananger: ms.manager, //后台管理路径
				loading: true,//加载状态
				searchJson:[], //搜索条件配置，用在搜索、过滤查询
				hasSearch: false,//根据searchJson的字段isSearch属性来判断是否存在顶部搜索选项
				modelId: null, //模型编号
				modelName: '', //模型名称
				tableField: [], //业务的表字典，用在表格渲染

				dataList: [], //自定义业务数据
				selectionList: [], //表格选中记录

				emptyText: '', //表格空值提示
				form: {}, //搜索表单

				total: 0,
				//总记录数量
				pageSize: 50,
				//页面数量
				currentPage: 1,
				//初始页
				isEditor:false,//菜单点击到自定义业务页面就不显示返回按钮
				historyKey: "mdiy_form_data_",
			}
		},
		mixins: [],
		watch: {
			// vue3 脚手架需要切换跳转
			'$route':{
				handler(to) {
					this.modelName = ms.util.getParameter("modelName");
					this.historyKey = this.historyKey + this.modelName + "_history";
					this.modelId = ms.util.getParameter("modelId");
					this.isEditor = ms.util.getParameter("isEditor")==null?false:JSON.parse(ms.util.getParameter("isEditor"));
					this.getTableField();
					//如果存在历史参数，恢复页面结果
					if(sessionStorage.getItem(this.historyKey)) {
						var _history = JSON.parse(sessionStorage.getItem(this.historyKey))
						this.form = _history.form;
						this.total = parseInt(_history.total);
						this.currentPage = parseInt(_history.page.pageNo);
						this.pageSize = parseInt(_history.page.pageSize);
					}
					this.list();
				}
			},
			//检查是否存在顶部搜索选项
			searchJson: function () {
				var temp = this.searchJson.filter(function (item) {
					return item.isSearch!=undefined && item.isSearch=='true';
				});
				this.hasSearch = temp.length!=0;
			}
		},
		methods: {
			back: function (){
				ms.util.openSystemUrl("/mdiy/form/index.do",true);
			},
			fmt:function(row, column, cellValue, index) {
				var fieldFmt = this.tableField.filter(item => item.field.toLowerCase()==column.property.toLowerCase())
				if(fieldFmt.length>0){
					try {
						return eval('this.$refs.modelForm.getForm().'+fieldFmt[0].model+'Format').call(null,row, column, cellValue, index);
					} catch (e) {
						return cellValue;
					}

				}else {
					return cellValue;
				}
			},


			//新增
			save: function (id) {
				if (id) {
					// location.href = this.mananger + "/mdiy/formData/form.do?id=" + id+"&modelName="+this.modelName;
					ms.util.openSystemUrl("/mdiy/form/data/form.do?id=" + id+"&modelName="+this.modelName+"&modelId="+this.modelId+"&isEditor="+this.isEditor);
				} else {
					// location.href = this.mananger + "/mdiy/formData/form.do?modelName="+this.modelName;
					ms.util.openSystemUrl("/mdiy/form/data/form.do?modelName="+this.modelName+"&modelId="+this.modelId+"&isEditor="+this.isEditor);
				}
			},

			//获取表格字段
			getTableField: function () {
				if(!this.modelId && !this.modelName) {
					return;
				}
				var that = this;
				ms.http.get(ms.manager + "/mdiy/form/get.do",{id:this.modelId}).then(function (res) {
					if(res.result){
						//得到的内容为json字符串，需要转对象
						//创建时间
						var createDate = {
							"model": "createDate",
							"key": "create_date",
							"field": "CREATE_DATE",
							"javaType": "date",
							"jdbcType": "date",
							"name": "创建时间",
							"type": "date",
							"isShow": true
						}

						that.modelId = res.data.id;
						that.tableField = JSON.parse(res.data.modelField);
						that.tableField.push(createDate);

						var _modelJson = JSON.parse(res.data.modelJson);
						// 删除最后一个,
						_modelJson.searchJson = _modelJson.searchJson.replace(/,\s*]/g, ']');
						that.searchJson = JSON.parse(_modelJson.searchJson);

						that.tableField.forEach(function (item) {
							item.key = item.key.toLowerCase();
						})

						that.list();


					}else {
						that.$notify({
							title: '失败',
							message: res.msg,
							type: 'warning'
						});
					}
				});
			},

			// 小写转换
			lowerJSONKey: function (jsonObj){
				for (var key in jsonObj){
					jsonObj[key.toLowerCase()] = jsonObj[key];
					//排除小写字段 如：id
					if(key.toLowerCase()!=key) {
						delete(jsonObj[key]);
					}
				}
				return jsonObj;
			},

			//查询列表
			list: function (isSearch) {
				if(!this.modelId && !this.modelName) {
					return;
				}
				var that = this;
				var data = null; //搜索参数
				that.loading = true;
				var page={
					pageNo: that.currentPage,
					pageSize : that.pageSize
				}
				var form = JSON.parse(JSON.stringify(that.form))



				if(isSearch) {
					//删除空字符串
					for (var key in form){
						if(form[key] === undefined || form[key] === null || form[key] === ''){
							delete  form[key]
						}
					}
					form.sqlWhere ? data = Object.assign({modelId:that.modelId,modelName:that.modelName}, {sqlWhere: form.sqlWhere}, page) : data = Object.assign({modelId:that.modelId,modelName:that.modelName}, form, page)

				} else {
					data = Object.assign( {modelId:that.modelId,modelName:that.modelName}, page);
				}

				sessionStorage.setItem(this.historyKey,JSON.stringify({form: form, page: page}));
				ms.http.post(ms.manager + "/mdiy/form/data/queryData.do",data).then(function (data) {
					if (data.result){
						data = data.data;
						if (data.total <= 0) {
							that.emptyText = '暂无数据';
							that.dataList = [];
							that.total = 0;
						} else {
							that.total = data.total;
							data.rows.forEach(function (item) {
								item = that.lowerJSONKey(item);
								Object.keys(item).forEach(function (field) {
									field = field.toLowerCase()
									try {
										if (item[field] != "" && JSON.parse(item[field]).length > 0) {
											var picture = [];
											JSON.parse(item[field]).forEach(function (img) {
												picture.push(img);
											});
											item[field] = picture;
										}
									} catch (e) {
										// console.log(e);
									}
								});
							});
							that.dataList = data.rows;
						}
					}else {
						that.$notify({
							title: '失败',
							message: data.msg,
							type: 'warning'
						});
					}
					that.loading = false;
				});
			},

			//删除
			del: function (row) {
				var that = this;
				var ids = [];
				if (row.length > 0) {
					row.forEach(function (item, index) {
						ids.push(item.id);
					});
				} else {
					ids.push(row.id);
				}
				that.$confirm('此操作将永久删除所选内容, 是否继续?', '提示', {
					confirmButtonText: '确定',
					cancelButtonText: '取消',
					type: 'warning'
				}).then(function () {
					ms.http.post(ms.manager + "/mdiy/form/data/delete.do", {
						modelId: that.modelId,
						ids: ids.join(',')
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
			//自定义模型列表选中
			handleSelectionChange: function (val) {
				this.selectionList = val;
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
			// 筛选搜索
			search:function(data){
				this.form.sqlWhere = JSON.stringify(data);
				this.list(true);
			},
			//重置表单
			rest: function () {
				var that = this;
				that.currentPage = 1;
				that.loading = true;
				// 搜索表单置空
				that.tableField.forEach(function (field){
					if (field.isSearch) {
						delete that.form[field.model];
					}
				})
				delete that.form.sqlWhere;
				that.list();
			},
		},
		created: function () {
			this.modelName = ms.util.getParameter("modelName");
			this.historyKey = this.historyKey + this.modelName + "_history";
			this.modelId = ms.util.getParameter("modelId");
			this.isEditor = ms.util.getParameter("isEditor")==null?false:JSON.parse(ms.util.getParameter("isEditor"));
			this.getTableField();
			//如果存在历史参数，恢复页面结果
			if(sessionStorage.getItem(this.historyKey) && ms.util.getParameter("isBack") == 'true') {
				var _history = JSON.parse(sessionStorage.getItem(this.historyKey))
				this.form = _history.form;
				this.total = parseInt(_history.total);
				this.currentPage = parseInt(_history.page.pageNo);
				this.pageSize = parseInt(_history.page.pageSize);
			}
		},
		mounted() {

		}
	});
</script>
<style>
	#index .iconfont{
		font-size: 12px;
		margin-right: 5px;
	}
</style>
