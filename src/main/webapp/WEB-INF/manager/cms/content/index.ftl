<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>文章</title>
	<#include "../../include/head-file.ftl">
	<#include "/cms/content/form.ftl">
	<#include "/cms/content/list.ftl">
</head>
<body style="overflow: hidden">
<div id="index"  v-cloak>
	<!--左侧-->
	<el-container class="index-menu">
		<div class="left-tree" style="position:relative;">
			<div style="padding:10px 10px 0;">
				<el-radio-group v-model="lang" size="small" @change="changeLang">
					<el-radio-button label="zh">中文</el-radio-button>
					<el-radio-button label="en">English</el-radio-button>
					<el-radio-button label="all">全部</el-radio-button>
				</el-radio-group>
			</div>
			<el-scrollbar style="height: calc(100% - 42px);">
				<el-tree
						ref="treeRef"
						:indent="5"
						node-key="id"
						v-loading="loading"
						highlight-current
						:expand-on-click-node="false"
						default-expand-all
						:empty-text="emptyText"
						:data="treeData"
						:props="defaultProps"
						@node-click="handleNodeClick"
						style="padding: 10px;height: 100%;">
						<template #default="{ node, data }" >
							<span class="custom-tree-node" >
								<span :style="data.categoryType == '3' ? 'color: #dcdfe6' : ''" :title="data.categoryTitle">{{ data.categoryTitle }}</span>
							</span>
						</template>
				</el-tree>
			</el-scrollbar>
		</div>
		<content-list v-if="action=='list'" :key="'list-'+lang" :category-id="categoryId" :leaf=leaf :lang="lang" @form="form"></content-list>
		<content-form v-else :key="'form-'+lang" :category-id="categoryId" :category-type="categoryType" :id="id" :default-lang="lang" @back="back"></content-form>
	</el-container>
</div>
</body>
</html>
<script>
	var indexVue = new _Vue({
		el: "#index",
		components:{
			"content-list": contentList,
			"content-form": contentForm
		},
		data: function () {
			return {
				action: 'list',
				categoryId:'', //栏目id
				categoryType:'2', //1列表，2单篇
				leaf:true, //true子栏目
				id:null,//文章编号
				lang:'zh',//语言Tab：zh中文(默认) en英文 all全部
				defaultProps: {
					children: 'children',
					label: 'categoryTitle'
				},
				treeData: [],
				loading: true,
				emptyText: '',
				historyKey:"cms_content_history"
			}
		},
		watch:{
        },
		methods: {
			handleNodeClick: function (data) {
				if(data.id == 0) { //全部
					this.action = 'list';
					this.categoryId = 0;
					this.leaf = true;
				} else if (data.categoryType == '1') {
					this.action = 'list';
					this.categoryId = data.id;
					this.leaf = data.leaf;
				} else if(data.categoryType == '3'){
					this.$refs.treeRef.setCurrentKey(null);
					return;
				} else {
					this.id = null;
					this.action = 'form';
					this.categoryId = data.id;
					this.categoryType = data.categoryType;
				}
				//新增数据重置到列表第一页
				sessionStorage.setItem(this.historyKey,JSON.stringify({form:{}, page: {pageNo:1}}));
			},
			form: function(id) {
				this.action = 'form';
				this.id = id;
				this.categoryType = "1"; //列表
			},
			back: function(id) {
				this.action = 'list';
			},
			//语言Tab切换：切回列表视图并重建栏目树（列表/表单组件通过 :key 随 lang 重建自动刷新）
			changeLang: function () {
				this.action = 'list';
				this.treeList();
			},
			//按语言过滤栏目树：zh→cn/ en→en/（categoryPath 前缀），all 不过滤
			filterTreeByLang: function (rows, lang) {
				if (lang == 'all' || !rows) return rows;
				var prefix = (lang == 'zh' ? 'cn' : 'en') + '/';
				return rows.filter(function (row) {
					return row.categoryPath && row.categoryPath.indexOf(prefix) == 0;
				});
			},
			treeList: function () {
				var that = this;
				this.loadState = false;
				this.loading = true;
				ms.http.get(ms.manager + "/cms/category/list.do").then(function (res) {
					if (that.loadState) {
						that.loading = false;
					} else {
						that.loadState = true;
					}

					if (!res.result || res.data.total <= 0) {
						that.emptyText = '暂无数据';
						that.treeData = [];
					} else {
						that.emptyText = '';
						that.treeData = that.filterTreeByLang(res.data.rows, that.lang);
						that.treeData = ms.util.treeData(that.treeData, 'id', 'categoryId', 'children');
						that.treeData = [{
							id: 0,
							categoryTitle: '全部',
							children: that.treeData
						}];
					}
				});
				setTimeout(function () {
					if (that.loadState) {
						that.loading = false;
					} else {
						that.loadState = true;
					}
				}, 500);
			}
		},
		mounted: function () {
			this.treeList();
		},
		created:function (){
			//默认列表第一页
			sessionStorage.setItem(this.historyKey,JSON.stringify({form:{}, page: {pageNo:1}}));
		},
	});
</script>
<style scoped>
	#index .index-menu {
		height: 100vh;
		min-height: 100vh;
		min-width: 140px;
	}
	#index .ms-iframe-style {
		width: 100%;
		height: 100vh;
		border: 0;
	}

	/*脚手架需要此样式*/
	#index >>> .ms-iframe-style {
		height: 92vh;
	}

	#index >>> .ms-index {
		height: 100vh;
	}


	#index .left-tree{
		min-height: 100vh;
		background: #fff;
		width: 220px;
		border-right: solid 1px #e6e6e6;
		flex-shrink: 0;
	}

	#index .index-menu .el-main .index-menu-menu .el-menu-item {
		min-width: 140px;
		width: 100%;
	}
	#index .index-menu .el-main .index-material-item {
		min-width: 100% !important
	}
	#index .index-menu-menu-item , .el-submenu__title {
		height: 40px !important;
		line-height: 46px !important;
	}
	#index .el-tree--highlight-current .el-tree-node.is-current>.el-tree-node__content {
		background-color: rgb(137 140 145);
		color: #fff;
		border-radius: 2px;
	}
	body{
		overflow: hidden;
	}

</style>
