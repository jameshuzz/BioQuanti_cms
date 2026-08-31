<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>说明书模板管理</title>
    <#include "../../include/head-file.ftl">
    <style>
        [v-cloak] { display: none; }
        #app { background-color: white; padding: 20px; }
        .manual-tip { color: #909399; font-size: 12px; line-height: 20px; }
        .warn-tag { color: #E6A23C; font-size: 12px; }
        .other-template { color: #F56C6C; }
    </style>
</head>
<body class="custom-body">
<div id="app" class="ms-index" v-cloak>
    <el-main class="ms-container">

        <#-- ================= 视图1：模板列表 ================= -->
        <div v-if="view == 'list'">
            <div style="margin-bottom:15px;">
                <@shiro.hasPermission name="cms:manual:template">
                    <el-button type="primary" @click="openSaveDialog">新建模板</el-button>
                </@shiro.hasPermission>
                <el-button plain @click="openFieldsDialog">可用字段清单</el-button>
                <el-button plain @click="openDiskDialog">磁盘治理</el-button>
                <span class="manual-tip" style="margin-left:10px;">
                    模板为HTML文件，占位符写法 <code v-pre>{{字段名}}</code>（字段名见"可用字段清单"）；替换模板后所有绑定产品下次下载即用新模板，无需重新生成静态页
                </span>
            </div>

            <el-table :data="templateList" border stripe v-loading="listLoading">
                <el-table-column label="模板名称" prop="templateName" min-width="160"></el-table-column>
                <el-table-column label="语言" width="70">
                    <template #default="scope">
                        <el-tag size="small" :type="scope.row.templateLang == 'en' ? 'warning' : 'success'">
                            {{ scope.row.templateLang == 'en' ? '英文' : '中文' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="占位符" width="90">
                    <template #default="scope">
                        <el-tooltip placement="top" :disabled="!scope.row.placeholders">
                            <template #content>
                                <div v-for="p in (scope.row.placeholders || '').split(',')" :key="p" v-text="'{{' + p + '}}'"></div>
                            </template>
                            <span>{{ (scope.row.placeholders || '').split(',').filter(function(p){return p}).length }}个</span>
                        </el-tooltip>
                    </template>
                </el-table-column>
                <el-table-column label="绑定产品" prop="bindCount" width="90">
                    <template #default="scope">
                        <el-tag size="small" :type="scope.row.bindCount > 0 ? 'primary' : 'info'">{{ scope.row.bindCount }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="大小" width="90">
                    <template #default="scope">{{ fmtSize(scope.row.templateSize) }}</template>
                </el-table-column>
                <el-table-column label="状态" width="80">
                    <template #default="scope">
                        <@shiro.hasPermission name="cms:manual:template">
                            <el-switch v-model="scope.row.status" active-value="1" inactive-value="0"
                                       @change="enableTemplate(scope.row)"></el-switch>
                        </@shiro.hasPermission>
                    </template>
                </el-table-column>
                <el-table-column label="更新时间" width="160">
                    <template #default="scope">{{ scope.row.updateDate || '-' }}</template>
                </el-table-column>
                <el-table-column label="操作" width="280" fixed="right">
                    <template #default="scope">
                        <@shiro.hasPermission name="cms:manual:bind">
                            <el-button size="small" type="primary" link @click="openBindView(scope.row)">绑定产品</el-button>
                        </@shiro.hasPermission>
                        <el-button size="small" link @click="previewTemplate(scope.row)"
                                   :disabled="scope.row.bindCount == 0 || scope.row.status != '1'">预览</el-button>
                        <@shiro.hasPermission name="cms:manual:template">
                            <el-button size="small" link @click="openReplaceDialog(scope.row)">替换文件</el-button>
                            <el-button size="small" type="danger" link @click="deleteTemplate(scope.row)"
                                       :disabled="scope.row.bindCount > 0">删除</el-button>
                        </@shiro.hasPermission>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        <#-- ================= 视图2：绑定产品 ================= -->
        <div v-if="view == 'bind'">
            <div style="margin-bottom:15px;">
                <el-button @click="view = 'list'; loadList();">&lt; 返回模板列表</el-button>
                <span style="margin-left:10px;font-weight:bold;">
                    模板：<el-tag size="small">{{ bindTemplate.templateName }}</el-tag>
                    （{{ bindTemplate.templateLang == 'en' ? '英文' : '中文' }}，已绑定 {{ bindTemplate.bindCount }} 个产品）
                </span>
            </div>

            <el-form :inline="true" style="margin-bottom:10px;">
                <el-form-item label="栏目">
                    <el-tree-select v-model="bindQuery.categoryId" :data="treeList"
                                    :props="{value: 'id',label: 'categoryTitle',children: 'children'}"
                                    :render-after-expand="false" :check-strictly="true" :default-expand-all="true"
                                    placeholder="全部栏目" filterable clearable style="width:220px;"
                                    @change="loadProducts(1)"></el-tree-select>
                </el-form-item>
                <el-form-item label="搜索">
                    <el-input v-model="bindQuery.search" placeholder="货号 / 产品标题" clearable
                              style="width:200px;" @clear="loadProducts(1)"
                              @keyup.enter="loadProducts(1)"></el-input>
                </el-form-item>
                <el-form-item label="绑定状态">
                    <el-radio-group v-model="bindQuery.bindFilter" @change="loadProducts(1)">
                        <el-radio-button value="">全部</el-radio-button>
                        <el-radio-button value="unbind">未绑定</el-radio-button>
                        <el-radio-button value="bind">已绑定</el-radio-button>
                        <el-radio-button value="other">已绑其他模板</el-radio-button>
                    </el-radio-group>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="loadProducts(1)">查询</el-button>
                </el-form-item>
            </el-form>

            <div style="margin-bottom:10px;">
                <@shiro.hasPermission name="cms:manual:bind">
                    <el-button type="primary" :disabled="selection.length == 0" @click="saveBind(true)"
                               v-loading="bindSaving">绑定选中 {{ selection.length }} 个产品到本模板</el-button>
                    <el-button type="warning" plain :disabled="selection.length == 0" @click="saveBind(false)">解绑选中</el-button>
                </@shiro.hasPermission>
                <span class="warn-tag" v-if="moveOutCount > 0" style="margin-left:10px;">
                    ⚠ 当前勾选中 {{ moveOutCount }} 个产品将从其他模板移出（一个产品只能绑定一个模板）
                </span>
                <span class="manual-tip" style="margin-left:10px;">
                    首次绑定/解绑后需到"静态化-生成文章"重新生成对应产品页面（前台才显示下载按钮）；模板替换无需重新生成
                </span>
            </div>

            <el-table :data="productRows" border stripe v-loading="productLoading"
                      @selection-change="onSelectionChange" row-key="id">
                <el-table-column type="selection" width="45" :selectable="function(){return true}"></el-table-column>
                <el-table-column label="产品标题" prop="title" min-width="220" show-overflow-tooltip></el-table-column>
                <el-table-column label="货号" prop="catalogNo" width="130"></el-table-column>
                <el-table-column label="栏目" prop="categoryTitle" width="130"></el-table-column>
                <el-table-column label="当前模板" min-width="150">
                    <template #default="scope">
                        <span v-if="!scope.row.templateId" style="color:#909399;">未绑定</span>
                        <span v-else-if="scope.row.templateId == bindTemplate.id" style="color:#67C23A;">本模板</span>
                        <span v-else class="other-template">{{ scope.row.templateName || '其他模板' }}（将移出）</span>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="80">
                    <template #default="scope">
                        <el-button size="small" link @click="previewProduct(scope.row)"
                                   :disabled="!scope.row.templateId">预览</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <el-pagination style="margin-top:15px;justify-content:flex-end;" layout="total, prev, pager, next, sizes"
                           :total="productTotal" :current-page="bindQuery.page" :page-size="bindQuery.size"
                           :page-sizes="[20, 50, 100]" @current-change="loadProducts"
                           @size-change="function(s){bindQuery.size=s;loadProducts(1)}"></el-pagination>
        </div>

        <#-- ================= 新建/替换模板弹窗 ================= -->
        <el-dialog v-model="uploadDialog.visible" :title="uploadDialog.id ? '替换模板文件' : '新建说明书模板'"
                   width="560px" :close-on-click-modal="false">
            <el-form label-width="100px">
                <el-form-item label="模板名称" v-if="!uploadDialog.id">
                    <el-input v-model="uploadDialog.templateName" placeholder="如：ELISA试剂盒说明书(中文)"></el-input>
                </el-form-item>
                <el-form-item label="语言" v-if="!uploadDialog.id">
                    <el-radio-group v-model="uploadDialog.templateLang">
                        <el-radio value="cn">中文</el-radio>
                        <el-radio value="en">英文</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="模板文件">
                    <el-upload drag :auto-upload="false" :limit="1" accept=".html,.htm"
                               :on-change="onFileChange" :on-remove="function(){uploadDialog.file=null}"
                               style="width:100%;">
                        <div style="padding:20px 0;">
                            拖拽或点击选择 .html 模板文件<br>
                            <span class="manual-tip">占位符写法 <code v-pre>{{CATALOG_NO}}</code>，可用字段见"可用字段清单"（必含 style，字体用 NotoSansSC）</span>
                        </div>
                    </el-upload>
                </el-form-item>
                <el-form-item label="备注">
                    <el-input v-model="uploadDialog.remark" type="textarea" :rows="2"
                              placeholder="选填，如模板用途说明"></el-input>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="uploadDialog.visible = false">取消</el-button>
                <el-button type="primary" :loading="uploadDialog.saving" @click="submitUpload">确定</el-button>
            </template>
        </el-dialog>

        <#-- ================= 可用字段清单弹窗 ================= -->
        <el-dialog v-model="fieldsDialog.visible" title="模板可用字段（占位符清单）" width="600px">
            <div class="manual-tip" style="margin-bottom:10px;">
                模板中写 <code v-pre>{{字段名}}</code> 即可取产品规格表对应值；规格表新增字段后此清单自动更新（无需改代码）。值为空时渲染为"-"
            </div>
            <el-table :data="fieldsDialog.list" border max-height="400">
                <el-table-column label="字段名" prop="key" width="180">
                    <template #default="scope">
                        <el-tag size="small" type="info" v-text="'{{' + scope.row.key + '}}'"></el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="显示名" prop="name"></el-table-column>
            </el-table>
        </el-dialog>

        <#-- ================= 磁盘治理弹窗 ================= -->
        <el-dialog v-model="diskDialog.visible" title="磁盘治理（说明书模板文件）" width="640px">
            <div v-loading="diskDialog.loading">
                <div style="margin-bottom:10px;">
                    模板文件：{{ diskDialog.templates || 0 }} 个（{{ fmtSize(diskDialog.templateSize) }}）；
                    孤儿文件：{{ (diskDialog.orphans || []).length }} 个（{{ fmtSize(diskDialog.orphanSize) }}，可回收）
                </div>
                <el-table :data="diskDialog.orphans || []" border max-height="300"
                          v-if="(diskDialog.orphans || []).length > 0">
                    <el-table-column label="文件" prop="name"></el-table-column>
                    <el-table-column label="大小" width="100">
                        <template #default="scope">{{ fmtSize(scope.row.size) }}</template>
                    </el-table-column>
                    <el-table-column label="修改时间" width="170">
                        <template #default="scope">{{ scope.row.lastModified }}</template>
                    </el-table-column>
                </el-table>
                <div class="manual-tip" v-else>无孤儿文件（孤儿=DB无记录且修改超过1天的文件，通常因替换/删除中断产生）</div>
            </div>
            <template #footer>
                <el-button @click="diskDialog.visible = false">关闭</el-button>
                <@shiro.hasPermission name="cms:manual:template">
                    <el-button type="warning" :loading="diskDialog.cleaning"
                               :disabled="(diskDialog.orphans || []).length == 0" @click="cleanDisk">清理孤儿文件</el-button>
                </@shiro.hasPermission>
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
                // 当前视图 list=模板列表 bind=绑定管理
                view: 'list',
                listLoading: false,
                templateList: [],
                // 上传弹窗（新建/替换共用）
                uploadDialog: {visible: false, id: '', templateName: '', templateLang: 'cn', remark: '', file: null, saving: false},
                // 字段清单弹窗
                fieldsDialog: {visible: false, list: []},
                // 磁盘治理弹窗
                diskDialog: {visible: false, loading: false, cleaning: false, templates: 0, templateSize: 0, orphans: [], orphanSize: 0},
                // 绑定视图
                bindTemplate: {},
                bindQuery: {categoryId: '', search: '', bindFilter: '', page: 1, size: 20},
                productRows: [], productTotal: 0, productLoading: false,
                selection: [],
                bindSaving: false,
                // 栏目树
                treeList: [{id: '0', categoryTitle: '顶级栏目', children: []}]
            }
        },
        computed: {
            // 勾选中将从其他模板移出的数量
            moveOutCount: function () {
                var that = this;
                return that.selection.filter(function (r) {
                    return r.templateId && r.templateId != that.bindTemplate.id;
                }).length;
            }
        },
        methods: {
            // ============ 模板列表 ============
            loadList: function () {
                var that = this;
                that.listLoading = true;
                ms.http.get(ms.manager + "/cms/manual/list.do").then(function (res) {
                    that.listLoading = false;
                    if (res.result) {
                        that.templateList = res.data || [];
                    } else {
                        that.$notify({title: '失败', message: res.msg, type: 'warning'});
                    }
                });
            },
            openSaveDialog: function () {
                var d = this.uploadDialog;
                d.visible = true; d.id = ''; d.templateName = ''; d.templateLang = 'cn'; d.remark = ''; d.file = null;
            },
            openReplaceDialog: function (row) {
                var d = this.uploadDialog;
                d.visible = true; d.id = row.id; d.templateName = row.templateName;
                d.templateLang = row.templateLang; d.remark = row.remark; d.file = null;
            },
            onFileChange: function (file) {
                this.uploadDialog.file = file.raw;
            },
            // 提交新建/替换（FormData走XHR，绕开ms.http的表单编码头）
            submitUpload: function () {
                var that = this, d = that.uploadDialog;
                if (!d.id && !d.templateName.trim()) {
                    that.$notify({title: '失败', message: '请填写模板名称', type: 'warning'});
                    return;
                }
                if (!d.file) {
                    that.$notify({title: '失败', message: '请选择模板HTML文件', type: 'warning'});
                    return;
                }
                var url = d.id ? ms.manager + '/cms/manual/update.do' : ms.manager + '/cms/manual/save.do';
                var fd = new FormData();
                fd.append('file', d.file);
                if (d.id) {
                    fd.append('id', d.id);
                } else {
                    fd.append('templateName', d.templateName.trim());
                    fd.append('templateLang', d.templateLang);
                }
                fd.append('remark', d.remark || '');
                d.saving = true;
                var xhr = new XMLHttpRequest();
                xhr.open('POST', url);
                xhr.onload = function () {
                    d.saving = false;
                    var res;
                    try { res = JSON.parse(xhr.responseText); } catch (e) { res = {result: false, msg: '响应解析失败'}; }
                    if (res.result) {
                        that.$notify({title: '成功', type: 'success', message: d.id ? '模板已替换，绑定产品下次下载即用新模板' : '模板创建成功'});
                        d.visible = false;
                        that.loadList();
                    } else {
                        that.$notify({title: '失败', message: res.msg || '保存失败', type: 'warning'});
                    }
                };
                xhr.onerror = function () {
                    d.saving = false;
                    that.$notify({title: '失败', message: '网络错误', type: 'warning'});
                };
                xhr.send(fd);
            },
            enableTemplate: function (row) {
                var that = this;
                ms.http.post(ms.manager + '/cms/manual/enable.do', {
                    id: row.id, status: row.status
                }).then(function (res) {
                    if (res.result) {
                        that.$notify({title: '成功', type: 'success', message: row.status == '1' ? '已启用' : '已停用（前台下载将返回404）'});
                    } else {
                        that.$notify({title: '失败', message: res.msg, type: 'warning'});
                        that.loadList();
                    }
                });
            },
            deleteTemplate: function (row) {
                var that = this;
                that.$confirm('确定删除模板「' + row.templateName + '」吗？模板文件将一并删除', '删除模板', {
                    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
                }).then(function () {
                    ms.http.post(ms.manager + '/cms/manual/delete.do', {id: row.id}).then(function (res) {
                        if (res.result) {
                            that.$notify({title: '成功', type: 'success', message: '删除成功'});
                            that.loadList();
                        } else {
                            that.$notify({title: '失败', message: res.msg, type: 'warning'});
                        }
                    });
                });
            },
            // ============ 预览 ============
            // 模板列表预览：取第一个绑定产品
            previewTemplate: function (row) {
                var that = this;
                ms.http.get(ms.manager + '/cms/manual/bind/list.do', {
                    templateId: row.id, bindFilter: 'bind', page: 1, size: 1
                }).then(function (res) {
                    if (res.result && res.data.rows && res.data.rows.length > 0) {
                        window.open(ms.manager + '/cms/manual/preview.do?templateId=' + row.id
                                + '&productId=' + res.data.rows[0].id);
                    } else {
                        that.$notify({title: '提示', message: '该模板暂无绑定产品，请先绑定', type: 'warning'});
                    }
                });
            },
            previewProduct: function (row) {
                window.open(ms.manager + '/cms/manual/preview.do?templateId=' + row.templateId
                        + '&productId=' + row.id);
            },
            // ============ 字段清单 ============
            openFieldsDialog: function () {
                var that = this;
                that.fieldsDialog.visible = true;
                if (that.fieldsDialog.list.length == 0) {
                    ms.http.get(ms.manager + '/cms/manual/fields.do').then(function (res) {
                        if (res.result) {
                            that.fieldsDialog.list = res.data || [];
                        }
                    });
                }
            },
            // ============ 磁盘治理 ============
            openDiskDialog: function () {
                var that = this;
                that.diskDialog.visible = true;
                that.scanDisk();
            },
            scanDisk: function () {
                var that = this;
                that.diskDialog.loading = true;
                ms.http.get(ms.manager + '/cms/manual/disk/scan.do').then(function (res) {
                    that.diskDialog.loading = false;
                    if (res.result) {
                        that.diskDialog.templates = res.data.templates;
                        that.diskDialog.templateSize = res.data.templateSize;
                        that.diskDialog.orphans = res.data.orphans || [];
                        that.diskDialog.orphanSize = res.data.orphanSize;
                    }
                });
            },
            cleanDisk: function () {
                var that = this;
                that.$confirm('确定清理孤儿文件吗？', '清理', {type: 'warning'}).then(function () {
                    that.diskDialog.cleaning = true;
                    ms.http.post(ms.manager + '/cms/manual/disk/clean.do', {}).then(function (res) {
                        that.diskDialog.cleaning = false;
                        if (res.result) {
                            that.$notify({title: '成功', type: 'success', message: '已清理 ' + res.data + ' 个文件'});
                            that.scanDisk();
                        } else {
                            that.$notify({title: '失败', message: res.msg, type: 'warning'});
                        }
                    });
                });
            },
            // ============ 绑定管理 ============
            openBindView: function (row) {
                var that = this;
                that.bindTemplate = row;
                that.bindQuery = {categoryId: '', search: '', bindFilter: '', page: 1, size: 20};
                that.selection = [];
                that.view = 'bind';
                that.loadProducts(1);
            },
            loadProducts: function (page) {
                var that = this;
                if (page) that.bindQuery.page = page;
                that.productLoading = true;
                ms.http.get(ms.manager + '/cms/manual/bind/list.do', {
                    templateId: that.bindTemplate.id,
                    categoryId: that.bindQuery.categoryId || '',
                    search: that.bindQuery.search || '',
                    bindFilter: that.bindQuery.bindFilter || '',
                    page: that.bindQuery.page,
                    size: that.bindQuery.size
                }).then(function (res) {
                    that.productLoading = false;
                    if (res.result) {
                        that.productRows = res.data.rows || [];
                        that.productTotal = res.data.total || 0;
                    } else {
                        that.$notify({title: '失败', message: res.msg, type: 'warning'});
                    }
                });
            },
            onSelectionChange: function (rows) {
                this.selection = rows;
            },
            // 保存绑定（bind=true绑到本模板，false=解绑）
            saveBind: function (bind) {
                var that = this;
                var ids = that.selection.map(function (r) { return r.id; });
                var tip = bind
                        ? ('将把 ' + ids.length + ' 个产品绑定到「' + that.bindTemplate.templateName + '」'
                                + (that.moveOutCount > 0 ? '，其中 ' + that.moveOutCount + ' 个将从其他模板移出' : '')
                                + '，绑定后立即生效（前台下载实时用新模板）')
                        : ('将解除 ' + ids.length + ' 个产品的说明书绑定，解绑后前台下载返回404');
                that.$confirm(tip, bind ? '保存绑定' : '解绑', {
                    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
                }).then(function () {
                    that.bindSaving = true;
                    ms.http.post(ms.manager + '/cms/manual/bind/save.do', {
                        templateId: bind ? that.bindTemplate.id : '',
                        productIds: ids.join(',')
                    }).then(function (res) {
                        that.bindSaving = false;
                        if (res.result) {
                            that.$notify({
                                title: '成功', type: 'success',
                                message: '已保存 ' + res.data + ' 个产品。提示：首次绑定/解绑的产品需重新生成静态页'
                            });
                            // 刷新绑定视图和模板列表的绑定数
                            that.loadProducts();
                            that.loadList();
                        } else {
                            that.$notify({title: '失败', message: res.msg, type: 'warning'});
                        }
                    });
                });
            },
            // ============ 栏目树 ============
            getTree: function () {
                var that = this;
                ms.http.get(ms.manager + "/cms/generate/list.do", {pageSize: 9999}).then(function (res) {
                    if (res.result) {
                        that.treeList[0].children = ms.util.treeData(res.data.rows, 'id', 'categoryId', 'children');
                    }
                });
            },
            fmtSize: function (bytes) {
                if (!bytes && bytes !== 0) return '-';
                if (bytes < 1024) return bytes + ' B';
                if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
                return (bytes / 1024 / 1024).toFixed(2) + ' MB';
            }
        },
        created: function () {
            this.loadList();
            this.getTree();
        }
    });
</script>
