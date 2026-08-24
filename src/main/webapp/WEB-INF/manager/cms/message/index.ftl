<!DOCTYPE html>
<html>
<head>
    <title>留言管理</title>
    <#include "../../include/head-file.ftl">
</head>
<body>
<div id="index" v-cloak class="ms-index">

    <el-header class="ms-header" height="50px">
        <el-col :span=12>
            <@shiro.hasPermission name="cms:message:del">
                <el-button type="danger" class="el-icon-delete" size="default" @click="del(selectionList)"
                           :disabled="!selectionList.length">删除
                </el-button>
            </@shiro.hasPermission>
        </el-col>
    </el-header>

    <div class="ms-search">
        <el-form :model="form" ref="searchForm" size="default" label-width="90px">
            <el-row>
                <el-col :span=6>
                    <el-form-item label="姓名">
                        <el-input v-model="form.name" :clearable="true" placeholder="请输入姓名"></el-input>
                    </el-form-item>
                </el-col>
                <el-col :span=6>
                    <el-form-item label="邮箱">
                        <el-input v-model="form.email" :clearable="true" placeholder="请输入邮箱"></el-input>
                    </el-form-item>
                </el-col>
                <el-col :span=6>
                    <el-form-item label="国家/地区">
                        <el-input v-model="form.country" :clearable="true" placeholder="请输入国家/地区"></el-input>
                    </el-form-item>
                </el-col>
                <el-col :span=6>
                    <el-form-item label="处理状态">
                        <el-select v-model="form.status" :clearable="true" placeholder="全部" style="width: 100%">
                            <el-option label="未处理" :value="0"></el-option>
                            <el-option label="已处理" :value="1"></el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
            </el-row>
            <div style="display: flex;justify-content: end;">
                <el-button type="primary" class="el-icon-search" size="default"
                           @click="currentPage=1;loading=true;list()">查询
                </el-button>
                <el-button class="el-icon-refresh" size="default" @click="rest">重置</el-button>
            </div>
        </el-form>
    </div>

    <el-main class="ms-container">
        <el-table v-loading="loading" ref="multipleTable" height="calc(100vh - 230px)" class="ms-table-pagination"
                  border :data="dataList" tooltip-effect="dark" @selection-change="handleSelectionChange">
            <template #empty>
                <el-empty :description="emptyText"></el-empty>
            </template>
            <el-table-column type="selection" width="40"></el-table-column>
            <el-table-column label="姓名" min-width="90" prop="name" show-overflow-tooltip></el-table-column>
            <el-table-column label="公司/单位" min-width="120" prop="company" show-overflow-tooltip></el-table-column>
            <el-table-column label="国家/地区" min-width="100" prop="country" show-overflow-tooltip></el-table-column>
            <el-table-column label="联系方式" min-width="150">
                <template #default="scope">
                    <div v-if="scope.row.email" style="line-height:1.6">
                        <i class="el-icon-message"></i> {{ scope.row.email }}
                    </div>
                    <div v-if="scope.row.phone" style="line-height:1.6">
                        <i class="el-icon-phone"></i> {{ scope.row.phone }}
                    </div>
                    <div v-if="scope.row.wechat" style="line-height:1.6">
                        <i class="el-icon-chat-dot-round"></i> {{ scope.row.wechat }}
                    </div>
                    <div v-if="scope.row.whatsapp" style="line-height:1.6">
                        <i class="el-icon-chat-line-round"></i> {{ scope.row.whatsapp }}
                    </div>
                    <div v-if="scope.row.telegram" style="line-height:1.6">
                        <i class="el-icon-s-promotion"></i> {{ scope.row.telegram }}
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="留言内容" min-width="200" prop="content" show-overflow-tooltip></el-table-column>
            <el-table-column label="IP归属地" min-width="110" prop="ipRegion" show-overflow-tooltip></el-table-column>
            <el-table-column label="状态" width="80" align="center">
                <template #default="scope">
                    <el-tag v-if="scope.row.status == 0" type="danger" size="small">未处理</el-tag>
                    <el-tag v-else type="success" size="small">已处理</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="提交时间" width="160" align="center" prop="createDate"></el-table-column>
            <el-table-column label="操作" fixed="right" align="center" width="140">
                <template #default="scope">
                    <el-link :underline="false" type="primary" size="default" @click="view(scope.row)">详情</el-link>
                    <@shiro.hasPermission name="cms:message:del">
                        <el-link :underline="false" type="primary" style="margin-left: 8px"
                                 @click="del([scope.row])">删除
                        </el-link>
                    </@shiro.hasPermission>
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

    <!-- 详情弹窗 -->
    <el-dialog title="留言详情" v-model="dialogVisible" width="700px" :close-on-click-modal="false" v-cloak>
        <el-descriptions :column="2" border size="default" v-if="current">
            <el-descriptions-item label="姓名">{{ current.name }}</el-descriptions-item>
            <el-descriptions-item label="公司/单位">{{ current.company }}</el-descriptions-item>
            <el-descriptions-item label="国家/地区">{{ current.country }}</el-descriptions-item>
            <el-descriptions-item label="城市/省份">{{ current.region }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ current.email }}</el-descriptions-item>
            <el-descriptions-item label="电话">{{ current.phone }}</el-descriptions-item>
            <el-descriptions-item label="微信">{{ current.wechat }}</el-descriptions-item>
            <el-descriptions-item label="WhatsApp">{{ current.whatsapp }}</el-descriptions-item>
            <el-descriptions-item label="Telegram">{{ current.telegram }}</el-descriptions-item>
            <el-descriptions-item label="LinkedIn">{{ current.linkedin }}</el-descriptions-item>
            <el-descriptions-item label="偏好联系方式">{{ preferredContactText }}</el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ current.createDate }}</el-descriptions-item>
            <el-descriptions-item label="IP">{{ current.ip }}</el-descriptions-item>
            <el-descriptions-item label="IP归属地">{{ current.ipRegion }}</el-descriptions-item>
            <el-descriptions-item label="来源页面" :span="2">{{ current.referer }}</el-descriptions-item>
            <el-descriptions-item label="留言内容" :span="2">{{ current.content }}</el-descriptions-item>
        </el-descriptions>
        <@shiro.hasPermission name="cms:message:update">
            <el-form :model="editForm" label-width="90px" style="margin-top: 20px">
                <el-form-item label="处理状态">
                    <el-radio-group v-model="editForm.status">
                        <el-radio :label="0">未处理</el-radio>
                        <el-radio :label="1">已处理</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="跟进备注">
                    <el-input type="textarea" :rows="3" v-model="editForm.remark"
                              placeholder="记录跟进情况，如：已回复邮件、已添加微信等"></el-input>
                </el-form-item>
            </el-form>
        </@shiro.hasPermission>
        <template #footer>
            <el-button size="default" @click="dialogVisible = false">关 闭</el-button>
            <@shiro.hasPermission name="cms:message:update">
                <el-button size="default" type="primary" @click="update()">保 存</el-button>
            </@shiro.hasPermission>
        </template>
    </el-dialog>
</div>

</body>

</html>
<script>
    var indexVue = new _Vue({
        el: '#index',
        data: function () {
            return {
                dataList: [],
                selectionList: [],
                total: 0,
                pageSize: 20,
                currentPage: 1,
                loading: true,
                emptyText: '',
                dialogVisible: false,
                current: null,
                editForm: {
                    id: '',
                    status: 0,
                    remark: ''
                },
                form: {
                    name: '',
                    email: '',
                    country: '',
                    status: ''
                }
            }
        },
        computed: {
            preferredContactText: function () {
                var map = {
                    email: '邮箱',
                    phone: '电话',
                    wechat: '微信',
                    whatsapp: 'WhatsApp',
                    telegram: 'Telegram'
                };
                return this.current && map[this.current.preferredContact] || '-';
            }
        },
        methods: {
            // 查询列表
            list: function () {
                var that = this;
                that.loading = true;
                var form = JSON.parse(JSON.stringify(that.form));
                for (var key in form) {
                    if (form[key] === '' || form[key] === null) {
                        delete form[key];
                    }
                }
                form.pageNo = that.currentPage;
                form.pageSize = that.pageSize;
                ms.http.post(ms.manager + "/cms/message/list.do", form).then(function (data) {
                    if (data.result) {
                        that.loading = false;
                        if (data.data.total <= 0) {
                            that.emptyText = '暂无数据';
                            that.dataList = [];
                        } else {
                            that.emptyText = '';
                            that.total = data.data.total;
                            that.dataList = data.data.rows;
                        }
                    } else {
                        that.loading = false;
                        that.emptyText = data.msg || '查询失败';
                        that.dataList = [];
                    }
                });
            },
            // 重置
            rest: function () {
                this.form = {name: '', email: '', country: '', status: ''};
                this.currentPage = 1;
                this.loading = true;
                this.list();
            },
            // 选中
            handleSelectionChange: function (val) {
                this.selectionList = val;
            },
            // 查看详情
            view: function (row) {
                this.current = row;
                this.editForm = {
                    id: row.id,
                    status: row.status,
                    remark: row.remark
                };
                this.dialogVisible = true;
            },
            // 更新状态/备注
            update: function () {
                var that = this;
                ms.http.post(ms.manager + "/cms/message/update.do", that.editForm, {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }).then(function (data) {
                    if (data.result) {
                        that.$notify({title: '成功', message: '保存成功', type: 'success'});
                        that.dialogVisible = false;
                        that.list();
                    } else {
                        that.$notify({title: '失败', message: data.msg, type: 'warning'});
                    }
                });
            },
            // 删除
            del: function (row) {
                var that = this;
                that.$confirm('此操作将永久删除所选留言, 是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(function () {
                    ms.http.post(ms.manager + "/cms/message/delete.do", row.length ? row : [row], {
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    }).then(function (data) {
                        if (data.result) {
                            that.$notify({title: '成功', type: 'success', message: '删除成功!'});
                            that.list();
                        } else {
                            that.$notify({title: '失败', message: data.msg, type: 'warning'});
                        }
                    });
                });
            },
            currentChange: function (currentPage) {
                this.currentPage = currentPage;
                this.loading = true;
                this.list();
            },
            sizeChange: function (pageSize) {
                this.pageSize = pageSize;
                this.currentPage = 1;
                this.loading = true;
                this.list();
            }
        },
        created: function () {
            this.list();
        }
    });
</script>
