<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>产品简介模板</title>
    <#include "../../include/head-file.ftl">
    <style>
        [v-cloak] { display: none; }
        #app { background-color: white; padding: 20px; }
        .tpl-tip { color: #909399; font-size: 12px; line-height: 20px; }
        /* 预览样式（与前台 .bq-introtab 一致的简化版） */
        .intro-preview table { border-collapse: collapse; width: 100%; max-width: 640px; }
        .intro-preview th, .intro-preview td { border: 1px solid #ddd; padding: 6px 10px; font-size: 13px; text-align: left; }
        .intro-preview th { width: 160px; background: #f7f8fa; color: #333; font-weight: 600; }
        .intro-preview td { color: #909399; }
    </style>
</head>
<body class="custom-body">
<div id="app" class="ms-index" v-cloak>
    <el-main class="ms-container">

        <div style="margin-bottom:15px;">
            <el-button type="primary" @click="openDialog(null)">新建模板</el-button>
            <span class="tpl-tip" style="margin-left:10px;">
                绑定栏目后，该栏目下的产品在后台编辑时，富文本会自动插入此简介表骨架（左列固定，右侧由编辑填写）；已有骨架的文章不会重复插入
            </span>
        </div>

        <el-table :data="tplList" border stripe v-loading="listLoading">
            <el-table-column label="模板名称" prop="tplName" min-width="180"></el-table-column>
            <el-table-column label="绑定栏目" min-width="240">
                <template #default="scope">{{ catTitles(scope.row.categoryIds) }}</template>
            </el-table-column>
            <el-table-column label="信息行数" width="90">
                <template #default="scope">{{ rowCount(scope.row.content) }}</template>
            </el-table-column>
            <el-table-column label="更新时间" width="160">
                <template #default="scope">{{ scope.row.updateDate || scope.row.createDate || '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
                <template #default="scope">
                    <el-button size="small" type="primary" link @click="openDialog(scope.row)">编辑</el-button>
                    <el-button size="small" type="danger" link @click="delTpl(scope.row)">删除</el-button>
                </template>
            </el-table-column>
        </el-table>

        <#-- ================= 新建/编辑弹窗 ================= -->
        <el-dialog v-model="dialog.visible" :title="dialog.id ? '编辑模板' : '新建模板'"
                   width="860px" top="4vh" :close-on-click-modal="false">
            <el-form label-width="90px">
                <el-form-item label="模板名称" required>
                    <el-input v-model="dialog.tplName" placeholder="如：试剂盒产品简介" style="width:320px;"></el-input>
                </el-form-item>
                <el-form-item label="绑定栏目" required>
                    <el-tree-select v-model="dialog.categoryIds" :data="treeList" multiple
                                    :props="{value: 'id',label: 'categoryTitle',children: 'children'}"
                                    :render-after-expand="false" :check-strictly="true" :default-expand-all="true"
                                    placeholder="可多选；绑定父栏目时其子栏目文章同样生效"
                                    style="width:100%;"></el-tree-select>
                </el-form-item>
                <el-form-item label="简介信息" required>
                    <div style="width:100%;">
                        <el-table :data="dialog.rows" border size="small" max-height="360">
                            <el-table-column label="左列标签" width="240">
                                <template #default="scope">
                                    <el-input v-model="scope.row.label" placeholder="如：货号"></el-input>
                                </template>
                            </el-table-column>
                            <el-table-column label="右侧默认值（可空，由编辑填写）">
                                <template #default="scope">
                                    <el-input v-model="scope.row.value" placeholder="留空则显示空白格"></el-input>
                                </template>
                            </el-table-column>
                            <el-table-column label="操作" width="130" align="center">
                                <template #default="scope">
                                    <el-button size="small" link :disabled="scope.$index==0"
                                               @click="moveRow(scope.$index,-1)">上移</el-button>
                                    <el-button size="small" link :disabled="scope.$index==dialog.rows.length-1"
                                               @click="moveRow(scope.$index,1)">下移</el-button>
                                    <el-button size="small" type="danger" link @click="dialog.rows.splice(scope.$index,1)">删</el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                        <el-button size="small" type="primary" plain style="margin-top:8px;"
                                   @click="dialog.rows.push({label:'',value:''})">添加一行</el-button>
                    </div>
                </el-form-item>
                <el-form-item label="预览">
                    <div class="intro-preview" v-html="previewHtml"></div>
                </el-form-item>
                <el-form-item label="备注">
                    <el-input v-model="dialog.remark" placeholder="选填" style="width:320px;"></el-input>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialog.visible = false">取消</el-button>
                <el-button type="primary" :loading="dialog.saving" @click="saveTpl">保存</el-button>
            </template>
        </el-dialog>

    </el-main>
</div>
</body>
</html>
<script>
    "use strict";

    var app = new _Vue({
        el: '#app',
        data: function () {
            return {
                listLoading: false,
                tplList: [],
                dialog: {visible: false, id: '', tplName: '', categoryIds: [], rows: [], remark: '', saving: false},
                // 栏目树
                treeList: [{id: '0', categoryTitle: '顶级栏目', children: []}],
                // id -> 栏目名（平铺，用于列表显示）
                catMap: {}
            }
        },
        computed: {
            previewHtml: function () {
                var vm = this;
                var rows = this.dialog.rows.filter(function (r) { return r.label && r.label.trim(); });
                if (rows.length == 0) return '<span style="color:#c0c4cc;">暂无内容</span>';
                var h = '<table><tbody>';
                rows.forEach(function (r) {
                    h += '<tr><th>' + vm.esc(r.label) + '</th><td>' + (r.value ? vm.esc(r.value) : '&nbsp;') + '</td></tr>';
                });
                return h + '</tbody></table>';
            }
        },
        methods: {
            esc: function (s) {
                return String(s || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
            },
            loadList: function () {
                var that = this;
                that.listLoading = true;
                ms.http.get(ms.manager + "/cms/introtpl/list.do").then(function (res) {
                    that.listLoading = false;
                    if (res.result) {
                        that.tplList = res.data || [];
                    } else {
                        that.$notify({title: '失败', message: res.msg, type: 'warning'});
                    }
                });
            },
            getTree: function () {
                var that = this;
                ms.http.get(ms.manager + "/cms/generate/list.do", {pageSize: 9999}).then(function (res) {
                    if (res.result) {
                        that.treeList[0].children = ms.util.treeData(res.data.rows, 'id', 'categoryId', 'children');
                        that.flattenTree(that.treeList[0].children);
                    }
                });
            },
            flattenTree: function (nodes) {
                var that = this;
                (nodes || []).forEach(function (n) {
                    that.catMap[n.id] = n.categoryTitle;
                    that.flattenTree(n.children);
                });
            },
            catTitles: function (ids) {
                var vm = this;
                return String(ids || '').split(',').filter(function (x) { return x; })
                    .map(function (id) { return vm.catMap[id] || id; }).join('、');
            },
            rowCount: function (content) {
                if (!content) return 0;
                return (content.match(/<tr>/g) || []).length;
            },
            openDialog: function (row) {
                var vm = this;
                var d = vm.dialog;
                d.visible = true; d.id = ''; d.tplName = ''; d.categoryIds = []; d.remark = '';
                d.rows = [{label: '', value: ''}, {label: '', value: ''}];
                if (row) {
                    d.id = row.id; d.tplName = row.tplName; d.remark = row.remark || '';
                    d.categoryIds = String(row.categoryIds || '').split(',').filter(function (x) { return x; });
                    d.rows = vm.parseRows(row.content);
                }
            },
            parseRows: function (content) {
                var rows = [];
                var div = document.createElement('div');
                div.innerHTML = content || '';
                var trs = div.querySelectorAll('.bq-introtab tbody tr');
                trs.forEach(function (tr) {
                    var th = tr.querySelector('th'), td = tr.querySelector('td');
                    rows.push({
                        label: th ? th.innerHTML.trim() : '',
                        value: td ? (td.innerHTML.trim() == '&nbsp;' ? '' : td.innerHTML.trim()) : ''
                    });
                });
                return rows.length ? rows : [{label: '', value: ''}];
            },
            moveRow: function (i, dir) {
                var rows = this.dialog.rows, t = rows[i];
                rows[i] = rows[i + dir]; rows[i + dir] = t;
            },
            saveTpl: function () {
                var that = this, d = that.dialog;
                if (!d.tplName.trim()) { that.$notify({title: '失败', message: '请填写模板名称', type: 'warning'}); return; }
                if (d.categoryIds.length == 0) { that.$notify({title: '失败', message: '请选择绑定栏目', type: 'warning'}); return; }
                var rows = d.rows.filter(function (r) { return r.label && r.label.trim(); });
                if (rows.length == 0) { that.$notify({title: '失败', message: '请至少添加一行简介信息', type: 'warning'}); return; }
                d.saving = true;
                ms.http.post(ms.manager + '/cms/introtpl/save.do', {
                    id: d.id,
                    tplName: d.tplName.trim(),
                    categoryIds: d.categoryIds.join(','),
                    rows: JSON.stringify(rows),
                    remark: d.remark || ''
                }).then(function (res) {
                    d.saving = false;
                    if (res.result) {
                        that.$notify({title: '成功', type: 'success', message: '保存成功，绑定栏目的产品编辑页即时生效'});
                        d.visible = false;
                        that.loadList();
                    } else {
                        that.$notify({title: '失败', message: res.msg, type: 'warning'});
                    }
                });
            },
            delTpl: function (row) {
                var that = this;
                that.$confirm('确定删除模板「' + row.tplName + '」吗？删除后绑定栏目不再自动插入简介表（已插入文章的骨架不受影响）', '删除模板', {
                    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
                }).then(function () {
                    ms.http.post(ms.manager + '/cms/introtpl/delete.do', {id: row.id}).then(function (res) {
                        if (res.result) {
                            that.$notify({title: '成功', type: 'success', message: '删除成功'});
                            that.loadList();
                        } else {
                            that.$notify({title: '失败', message: res.msg, type: 'warning'});
                        }
                    });
                });
            }
        },
        created: function () {
            this.loadList();
            this.getTree();
        }
    });
</script>
