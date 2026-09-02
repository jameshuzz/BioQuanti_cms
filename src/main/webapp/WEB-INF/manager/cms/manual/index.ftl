<html xmlns="http://www.w3.org/1999/html">
<head>
    <title>说明书模板管理</title>
    <#include "../../include/head-file.ftl">
    <script src="${base}/static/plugins/vue-codemirror/vue-codemirror.js"></script>
    <style>
        [v-cloak] { display: none; }
        #app { background-color: white; padding: 20px; }
        .manual-tip { color: #909399; font-size: 12px; line-height: 20px; }
        .warn-tag { color: #E6A23C; font-size: 12px; }
        .other-template { color: #F56C6C; }
        #app .vue-codemirror { height: 100%; }
        #app .CodeMirror { border: 1px solid #eee; height: 100%; }
    </style>
</head>
<body class="custom-body">
<div id="app" class="ms-index" v-cloak>
    <el-main class="ms-container">

        <#-- ================= 模板列表 ================= -->
        <div>
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
                <el-table-column label="操作" width="420" fixed="right">
                    <template #default="scope">
                        <@shiro.hasPermission name="cms:manual:bind">
                            <el-button size="small" type="primary" link @click="openBindDialog(scope.row)">绑定产品</el-button>
                        </@shiro.hasPermission>
                        <@shiro.hasPermission name="cms:manual:bind">
                            <el-button size="small" type="success" link @click="generateAttach(scope.row)"
                                       :disabled="scope.row.bindCount == 0 || scope.row.status != '1'">生成附件</el-button>
                        </@shiro.hasPermission>
                        <el-button size="small" link @click="previewTemplate(scope.row)">预览</el-button>
                        <el-button size="small" link @click="downloadTemplate(scope.row)">下载</el-button>
                        <@shiro.hasPermission name="cms:manual:template">
                            <el-button size="small" link @click="openEditDialog(scope.row)">编辑</el-button>
                            <el-button size="small" link @click="openReplaceDialog(scope.row)">替换文件</el-button>
                            <el-button size="small" type="danger" link @click="deleteTemplate(scope.row)"
                                       :disabled="scope.row.bindCount > 0">删除</el-button>
                        </@shiro.hasPermission>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        <#-- ================= 绑定产品弹窗 ================= -->
        <el-dialog v-model="bindDialog.visible" :title="'绑定产品 - ' + (bindDialog.template.templateName || '')"
                   width="1100px" top="5vh" :close-on-click-modal="false">
            <div style="margin-bottom:10px;">
                <el-tag size="small">{{ bindDialog.template.templateLang == 'en' ? '英文' : '中文' }}</el-tag>
                <span style="margin-left:8px;">已绑定 <b>{{ bindDialog.template.bindCount }}</b> 个产品</span>
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
                    绑定后回到模板列表点「生成附件」：自动生成说明书PDF附件并静态化对应产品页面（前台立即显示下载按钮）；解绑后需重新生成产品页面移除按钮
                </span>
            </div>

            <el-table :data="productRows" border stripe v-loading="productLoading" height="380"
                      @selection-change="onSelectionChange" row-key="id">
                <el-table-column type="selection" width="45" :selectable="function(){return true}"></el-table-column>
                <el-table-column label="产品标题" prop="title" min-width="220" show-overflow-tooltip></el-table-column>
                <el-table-column label="货号" prop="catalogNo" width="130"></el-table-column>
                <el-table-column label="栏目" prop="categoryTitle" width="130"></el-table-column>
                <el-table-column label="当前模板" min-width="150">
                    <template #default="scope">
                        <span v-if="!scope.row.templateId" style="color:#909399;">未绑定</span>
                        <span v-else-if="scope.row.templateId == bindDialog.template.id" style="color:#67C23A;">本模板</span>
                        <span v-else class="other-template">{{ scope.row.templateName || '其他模板' }}（将移出）</span>
                    </template>
                </el-table-column>
            </el-table>

            <el-pagination style="margin-top:15px;justify-content:flex-end;" layout="total, prev, pager, next, sizes"
                           :total="productTotal" :current-page="bindQuery.page" :page-size="bindQuery.size"
                           :page-sizes="[20, 50, 100]" @current-change="loadProducts"
                           @size-change="function(s){bindQuery.size=s;loadProducts(1)}"></el-pagination>
            <template #footer>
                <el-button @click="bindDialog.visible = false">关闭</el-button>
            </template>
        </el-dialog>

        <#-- ================= 在线编辑模板弹窗 ================= -->
        <el-dialog v-model="editDialog.visible" :title="'编辑模板 - ' + editDialog.name" width="90%" top="3vh"
                   :close-on-click-modal="false" @opened="refreshEditor">
            <div class="manual-tip" style="margin-bottom:8px;">
                占位符写法 <code v-pre>{{字段名}}</code>（可用字段见"可用字段清单"）；保存前会做PDF渲染校验，语法错误将无法保存；字体需用 NotoSansSC
            </div>
            <div v-loading="editDialog.loading" style="height:calc(78vh - 100px);">
                <codemirror v-if="editDialog.visible && !editDialog.loading" ref="codeEditor"
                            v-model:value="editDialog.content" :options="cmOption"></codemirror>
            </div>
            <template #footer>
                <el-button @click="editDialog.visible = false">取消</el-button>
                <el-button type="primary" :loading="editDialog.saving" @click="saveContent">保存</el-button>
            </template>
        </el-dialog>

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
                listLoading: false,
                templateList: [],
                // 上传弹窗（新建/替换共用）
                uploadDialog: {visible: false, id: '', templateName: '', templateLang: 'cn', remark: '', file: null, saving: false},
                // 字段清单弹窗
                fieldsDialog: {visible: false, list: []},
                // 磁盘治理弹窗
                diskDialog: {visible: false, loading: false, cleaning: false, templates: 0, templateSize: 0, orphans: [], orphanSize: 0},
                // 绑定产品弹窗
                bindDialog: {visible: false, template: {}},
                bindQuery: {categoryId: '', search: '', bindFilter: '', page: 1, size: 20},
                productRows: [], productTotal: 0, productLoading: false,
                selection: [],
                bindSaving: false,
                // 在线编辑弹窗
                editDialog: {visible: false, id: '', name: '', content: '', loading: false, saving: false},
                // 编辑器配置（仿模板管理的htm编辑器）
                cmOption: {
                    tabSize: 4,
                    styleActiveLine: true,
                    lineNumbers: true,
                    line: true,
                    styleSelectedText: true,
                    lineWrapping: true,
                    mode: 'text/html',
                    matchBrackets: true,
                    showCursorWhenSelecting: true,
                    hintOptions: {completeSingle: false}
                },
                // 栏目树
                treeList: [{id: '0', categoryTitle: '顶级栏目', children: []}]
            }
        },
        computed: {
            // 勾选中将从其他模板移出的数量
            moveOutCount: function () {
                var that = this;
                return that.selection.filter(function (r) {
                    return r.templateId && r.templateId != that.bindDialog.template.id;
                }).length;
            }
        },
        methods: {
            // ============ 模板列表 ============
            loadList: function () {
                var that = this;
                that.listLoading = true;
                return ms.http.get(ms.manager + "/cms/manual/list.do").then(function (res) {
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
            // ============ 一键生成附件 ============
            // 对已绑定产品批量生成说明书PDF（回填MANUAL附件字段）并定向静态化产品页面
            generateAttach: function (row) {
                var that = this;
                that.$confirm('将对本模板已绑定的 ' + row.bindCount + ' 个产品生成说明书PDF附件，并同步静态化对应产品页面（页面立即生效）。绑定产品较多时耗时较长，请耐心等待', '一键生成附件', {
                    confirmButtonText: '开始生成', cancelButtonText: '取消', type: 'warning'
                }).then(function () {
                    var loading = that.$notify({
                        title: '正在生成', type: 'info', duration: 0,
                        message: '正在生成说明书附件并静态化页面，请勿关闭页面...'
                    });
                    ms.http.post(ms.manager + '/cms/manual/attach/generate.do', {
                        templateId: row.id
                    }, {
                        timeout: 600000,
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded',
                            'X-Requested-With': 'XMLHttpRequest'
                        }
                    }).then(function (res) {
                        loading.close();
                        if (res.result) {
                            var d = res.data;
                            that.$notify({
                                title: '生成完成', type: 'success', duration: 0,
                                message: '共 ' + d.total + ' 个产品，成功生成附件 ' + d.success + ' 个，静态化页面 ' + d.pages + ' 个，前台详情页已生效'
                            });
                        } else {
                            that.$notify({title: '生成失败', message: res.msg, type: 'error', duration: 0});
                        }
                    });
                });
            },
            // ============ 预览/下载 ============
            // 模板预览：原始模板转PDF，占位符原样显示，无需绑定产品
            previewTemplate: function (row) {
                window.open(ms.manager + '/cms/manual/preview.do?templateId=' + row.id);
            },
            // 下载模板HTML源文件
            downloadTemplate: function (row) {
                window.open(ms.manager + '/cms/manual/download.do?templateId=' + row.id);
            },
            // ============ 在线编辑 ============
            openEditDialog: function (row) {
                var that = this, d = that.editDialog;
                d.id = row.id; d.name = row.templateName; d.content = ''; d.loading = true; d.saving = false;
                d.visible = true;
                ms.http.get(ms.manager + '/cms/manual/content.do', {id: row.id}).then(function (res) {
                    d.loading = false;
                    if (res.result) {
                        d.content = res.data || '';
                    } else {
                        that.$notify({title: '失败', message: res.msg, type: 'warning'});
                        d.visible = false;
                    }
                });
            },
            // 弹窗打开后刷新编辑器尺寸（codemirror在display:none容器中初始化高度异常）
            refreshEditor: function () {
                var that = this;
                that.$nextTick(function () {
                    if (that.$refs.codeEditor && that.$refs.codeEditor.codemirror) {
                        that.$refs.codeEditor.codemirror.refresh();
                    }
                });
            },
            // 保存在线编辑内容（后端先干跑PDF校验）
            saveContent: function () {
                var that = this, d = that.editDialog;
                if (!d.content || !d.content.trim()) {
                    that.$notify({title: '失败', message: '模板内容不能为空', type: 'warning'});
                    return;
                }
                that.$confirm('确定保存模板修改吗？绑定产品下次下载即生效', '保存模板', {
                    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
                }).then(function () {
                    d.saving = true;
                    ms.http.post(ms.manager + '/cms/manual/content/save.do', {
                        id: d.id, content: d.content
                    }).then(function (res) {
                        d.saving = false;
                        if (res.result) {
                            that.$notify({title: '成功', type: 'success', message: '模板已保存，绑定产品下次下载即生效'});
                            d.visible = false;
                            that.loadList();
                        } else {
                            that.$notify({title: '保存失败', message: res.msg, type: 'error', duration: 0});
                        }
                    });
                });
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
            // ============ 绑定管理（弹窗） ============
            openBindDialog: function (row) {
                var that = this;
                that.bindDialog.template = row;
                that.bindQuery = {categoryId: '', search: '', bindFilter: '', page: 1, size: 20};
                that.selection = [];
                that.bindDialog.visible = true;
                that.loadProducts(1);
            },
            loadProducts: function (page) {
                var that = this;
                if (page) that.bindQuery.page = page;
                that.productLoading = true;
                ms.http.get(ms.manager + '/cms/manual/bind/list.do', {
                    templateId: that.bindDialog.template.id,
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
                        ? ('将把 ' + ids.length + ' 个产品绑定到「' + that.bindDialog.template.templateName + '」'
                                + (that.moveOutCount > 0 ? '，其中 ' + that.moveOutCount + ' 个将从其他模板移出' : '')
                                + '，绑定后立即生效（前台下载实时用新模板）')
                        : ('将解除 ' + ids.length + ' 个产品的说明书绑定，解绑后前台下载返回404');
                that.$confirm(tip, bind ? '保存绑定' : '解绑', {
                    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
                }).then(function () {
                    that.bindSaving = true;
                    ms.http.post(ms.manager + '/cms/manual/bind/save.do', {
                        templateId: bind ? that.bindDialog.template.id : '',
                        productIds: ids.join(',')
                    }).then(function (res) {
                        that.bindSaving = false;
                        if (res.result) {
                            that.$notify({
                                title: '成功', type: 'success',
                                message: '已保存 ' + res.data + ' 个产品。提示：首次绑定/解绑的产品需重新生成静态页'
                            });
                            // 刷新弹窗列表和模板列表的绑定数
                            that.loadProducts();
                            that.loadList().then(function () {
                                // loadList刷新对象引用后同步到弹窗头部的绑定数
                                var t = that.templateList.filter(function (x) { return x.id == that.bindDialog.template.id; })[0];
                                if (t) that.bindDialog.template = t;
                            });
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
