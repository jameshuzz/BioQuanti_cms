<!DOCTYPE html>
<html>
<head>
    <title>应用设置</title>
    <#include "../../include/head-file.ftl">
</head>
<body>
<div id="form" v-cloak>
    <el-header class="ms-header ms-tr" height="50px">
        <el-button type="success" plain style=" margin-right: 8px"
                   class="el-icon-refresh-left" size="default"
                   @click="refreshCache()">刷新缓存
        </el-button>
        <el-button type="primary" class="iconfont icon-baocun" size="default" @click="save()" :loading="saveDisabled">保存</el-button>

    </el-header>
    <el-main class="ms-container">
        <el-scrollbar class="ms-scrollbar" style="height: 100%;">
        <el-form ref="form" :model="form" :rules="rules" label-width="140px" size="default">
            <el-row>
                <el-col :span=12>
                    <el-form-item label="网站标题" prop="appName">
                        <el-input v-model="form.appName"
                                  :disabled="false"
                                  :style="{width:  '100%'}"
                                  :clearable="true"
                                  placeholder="请输入网站标题">
                        </el-input>
                        <div class="ms-form-tip">
                         标签{ms:global.name/}
                        </div>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span=12>
                    <el-form-item  label="站点风格" prop="appStyle">
                        <el-select v-model="form.appStyle"
                                   :style="{width: '100%'}"
                                   :filterable="false"
                                   :disabled="false"
                                   :multiple="false" :clearable="true"
                                   placeholder="请选择站点风格">
                            <el-option v-for='item in templateFolderNameList' :key="item" :value="item"
                                       :label="item"></el-option>
                        </el-select>
                        <div class="ms-form-tip">
                         标签{ms:global.style/}，
                            下拉框的内容来自于模板管理中的模板,如果站点下有多套模版，切换模版保存后，需要重新静态化前台才会生效
                        </div>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row
                    :gutter=0
                    justify="start" align="top">
                <el-col :span=12>

                    <el-form-item  label="网站生成目录" prop="appDir">
                        <el-input
                                v-model="form.appDir"
                                :disabled="false"
                                :readonly="false"
                                :style="{width:  '100%'}"
                                :clearable="false"
                                placeholder="请输入网站生成目录">
                        </el-input>
                        <div class="ms-form-tip">
                            application.yml中配置ms.diy.html-dir了父目录，这里配置的是站点在父目录中生成的文件夹。开启短链并且没有站群的情况下，不会拼接这层路径<br/>
                            动静分离参考，
                        </div>
                    </el-form-item>
                </el-col>
                <el-col :span=12>
                </el-col>
            </el-row>
            <el-form-item  label="网站Logo" prop="appLogo">
                <el-upload
                        :file-list="form.appLogo"
                        :action="ms.manager+'/file/upload.do'"
                        :on-remove="appLogoHandleRemove"
                        :style="{width:''}"
                        :limit="1"
                        :before-upload="beforeImageUpload"
                        :on-exceed="handleExceed"
                        :disabled="false"
                        :data="{uploadPath:'/app','isRename':true,'appId':true}"
                        :on-success="appLogoSuccess"
                        :on-error="onUploadError"
                        :on-preview="appLogoHandLePreview"
                        accept="image/*"
                        list-type="picture-card">
                    <i class="el-icon-plus"></i>
                    <template #tip>
                        <div class="el-upload__tip">
                            支持jpg,png格式，最多上传1张图片
                        </div>
                    </template>
                </el-upload>
                <div class="ms-form-tip">
                {@ms:file global.logo/}，注意这里的获取图片标签会采用ms.file方式获取
                </div>
            </el-form-item>
            <el-form-item  label="网站Ico" prop="appIco">
                <el-upload
                        :file-list="form.appIco"
                        :action="ms.manager+'/file/upload.do'"
                        :on-remove="appIcoHandleRemove"
                        :style="{width:''}"
                        :limit="1"
                        :before-upload="handleBeforeUpload"
                        :on-exceed="handleExceed"
                        :disabled="false"
                        :data="{uploadPath:'/app','isRename':true,'appId':true}"
                        :on-success="appIcoSuccess"
                        :on-error="onUploadError"
                        :on-preview="appLogoHandLePreview"
                        accept="image/*"
                        list-type="picture-card">
                    <i class="el-icon-plus"></i>
                    <template #tip>
                        <div class="el-upload__tip">
                            支持jpg,png,ico格式，最多上传1张图片，如果非ico格式上传后会自动转换为ico格式
                        </div>
                    </template>
                </el-upload>
                <div class="ms-form-tip">
                    {@ms:file global.ico/}，注意这里的获取图片标签会采用ms.file方式获取
                </div>
            </el-form-item>
            <el-form-item label="关键字" prop="appKeyword">

                <el-input
                        type="textarea" :rows="5"
                        :disabled="false"
                        v-model="form.appKeyword"
                        :style="{width: '100%'}"
                        placeholder="请输入关键字">
                </el-input>
                <div class="ms-form-tip">
                 标签{ms:global.keyword/},
                    用于SEO优化
                </div>
            </el-form-item>


            <el-form-item label="描述" prop="appDescription">
                <el-input
                        type="textarea" :rows="5"
                        :disabled="false"
                        v-model="form.appDescription"
                        :style="{width: '100%'}"
                        placeholder="请输入描述">
                </el-input>
                <div class="ms-form-tip">
                 标签{ms:global.descrip/}，
                    用于SEO优化
                </div>
            </el-form-item>
            <el-form-item label="版权信息" prop="appCopyright">
                <el-input
                        type="textarea" :rows="5"
                        :disabled="false"
                        v-model="form.appCopyright"
                        :style="{width: '100%'}"
                        placeholder="请输入版权信息">
                </el-input>
                <div class="ms-form-tip">
                 标签{ms:global.copyright/}，
                    设置网站底部的版权信息
                </div>
            </el-form-item>
        </el-form>
        </el-scrollbar>
    </el-main>
</div>
</body>
</html>
<script>
    var form = new _Vue({
        el: '#form',
        data: function () {
            return {
                saveDisabled: false,
                //表单数据
                form: {
                    // 站点名称
                    appName: '',
                    // 站点风格
                    appStyle: [],
                    // 网站Logo
                    appLogo: [],
                    // 网站ico
                    appIco: [],
                    // 关键字
                    appKeyword: '',
                    // 描述
                    appDescription: '',
                    // 版权信息
                    appCopyright: '',
                    // 网站生成目录
                    appDir:'',
                },
                templateFolderNameList: [],//当前站点模版文件夹下所有的模版
                rules: {
                    // 网站标题
                    appName: [{
                        "required": true,
                        "message": "网站标题必须填写"
                    }, {
                        "min": 1,
                        "max": 50,
                        "message": "站点名称长度必须为10-150"
                    }],
                    appDescription: [{
                        "min": 0,
                        "max": 1000,
                        "message": "描述长度必须小于1000"
                    }],
                    appKeyword: [{
                        "min": 0,
                        "max": 1000,
                        "message": "关键字长度必须小于1000"
                    }],
                    appCopyright: [{
                        "min": 0,
                        "max": 1000,
                        "message": "版权信息长度必须小于1000"
                    }],
                    // 网站生成目录
                    appDir: [
                        {"required":true,"message":"网站生成目录不能为空"},
                        {"min":0,"max":10,"message":"网站生成目录长度必须为0-10"},
                        {
                            "pattern": /^[^[!@#$"'%^&*()_+-/~?！@#￥%…&*（）——+—？》《：“‘’]+$/,
                            "message": "网站生成目录格式不匹配"
                        }
                    ],
                }
            };
        },
        watch: {},
        computed: {},
        methods: {
            save: function () {
                var that = this;
                var url = ms.manager + "/basic/app/update.do";
                this.$refs.form.validate(function(valid) {
                    if (valid) {
                        that.saveDisabled = true;
                        var data = JSON.parse(JSON.stringify(that.form));
                        if(data.appLogo){
                            data.appLogo.forEach(function (value) {
                                value.url = value.url.replace(new RegExp('^'+ms.contextpath), "");
                            });
                            data.appLogo = JSON.stringify(data.appLogo);
                        }
                        if (data.appIco) {
                            data.appIco.forEach(function (value) {
                                value.url = value.url.replace(new RegExp('^'+ms.contextpath), "");
                            });
                            data.appIco = JSON.stringify(data.appIco);
                        }
                        ms.http.post(url, data).then(function (data) {
                            if (data.result) {
                                that.$notify({
                                    title: '成功',
                                    message: '保存成功',
                                    type: 'success'
                                });
                            } else {
                                that.$notify({
                                    title: '失败',
                                    message: data.msg,
                                    type: 'warning'
                                });
                            }
                            that.saveDisabled = false;
                        });
                    } else {
                        return false;
                    }
                });
            },
            //获取当前应用表
            get: function () {
                var that = this;
                this.loading = true
                ms.http.get(ms.manager + "/basic/app/get.do").then(function (res) {
                    that.loading = false
                    if(res.result && res.data){
                        if(res.data.appLogo){
                            res.data.appLogo = JSON.parse(res.data.appLogo);
                            res.data.appLogo.forEach(function(value){
                                if(!value.url.startsWith("http://") && !value.url.startsWith("https://")) {
                                    value.url = ms.contextpath + value.url;
                                }
                            })
                        }else{
                            res.data.appLogo=[]
                        }
                        if(res.data.appIco){
                            res.data.appIco = JSON.parse(res.data.appIco);
                            res.data.appIco.forEach(function(value){
                                if(!value.url.startsWith("http://") && !value.url.startsWith("https://")) {
                                    value.url = ms.contextpath + value.url;
                                }
                            })
                        }else{
                            res.data.appIco=[]
                        }
                        that.form = res.data;
                    }
                });
            },
            //上传超过限制
            handleExceed: function (files, fileList) {
                this.$notify({title: '提示', message: '当前最多上传1张图片', type: 'warning'});
            },
            appLogoHandleRemove:function(file, files) {
                var index = -1;
                index = this.form.appLogo.findIndex(function(e){return e.uid == file.uid} );
                if (index != -1) {
                    this.form.appLogo.splice(index, 1);
                }
            },
            appLogoHandLePreview:function (file){
                window.open(file.url)
            },
            //获取appStyle数据源
            queryAppTemplateSkin: function () {
                var that = this;
                ms.http.get(ms.manager + '/basic/template/queryAppTemplateSkin.do', {
                    pageSize: 99999
                }).then(function (data) {
                    that.templateFolderNameList = data.data.appTemplates;
                });
            },
            //appLogo文件上传完成回调
            appLogoSuccess: function (response, file, fileList) {
                if(response.result){
                    if(!response.data.startsWith("http://") && !response.data.startsWith("https://")) {
                        file.url = ms.contextpath + response.data;
                    }else{
                        file.url = response.data;
                    }
                    this.form.appLogo.push({url:response.data,name:file.name,uid:file.uid});
                }else {
                    this.$notify({
                        title: '失败',
                        message: response.msg,
                        type: 'warning'
                    });
                }

            },
            //通用文件上传失败回调
            onUploadError: function (response, file, fileList) {
                response = JSON.parse(response.message);
                this.$notify({
                    title: '失败',
                    message: response.msg,
                    type: 'warning'
                });
            },
            //appIco文件上传完成回调
            appIcoSuccess: function (response, file, fileList) {
                if(response.result){
                    if(!response.data.startsWith("http://") && !response.data.startsWith("https://")) {
                        file.url = ms.contextpath + response.data;
                    }else{
                        file.url = response.data;
                    }
                    this.form.appIco.push({url:response.data,name:file.name,uid:file.uid});
                }else {
                    this.$notify({
                        title: '失败',
                        message: response.msg,
                        type: 'warning'
                    });
                }

            },
            //appIco文件上传前操作
            handleBeforeUpload: async function (file) {
                // 1. 如果本身就是 ICO 格式，直接允许上传，不进行后续处理
                // 注意：ICO 的 MIME 类型通常是 'image/x-icon' 或 'image/vnd.microsoft.icon'
                if (file.type === 'image/x-icon' || file.type === 'image/vnd.microsoft.icon' || file.name.endsWith('.ico')) {
                    return true;
                }

                // 2. 格式校验逻辑
                const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png'];
                const isAllowed = allowedTypes.includes(file.type);

                if (!isAllowed) {
                    this.$notify({
                        title: '失败',
                        message: '格式错误！只允许上传 JPG, JPEG 或 PNG 图片',
                        type: 'warning'
                    });
                    return false; // 拦截上传
                }

                // 3. 开始 ICO 转换
                try {
                    const img = await this.fileToImage(file);
                    const icoBlob = await this.canvasToIcoBlob(img);

                    // 返回处理后的 ICO 文件对象
                    return new File([icoBlob], file.name.split('.')[0] + '.ico', {
                        type: 'image/x-icon'
                    });
                } catch (e) {
                    this.$notify({
                        title: '失败',
                        message: '转换失败',
                        type: 'warning'
                    });
                    return false;
                }
            },
            // 读取文件为图片
            fileToImage: function(file) {
                return new Promise((resolve) => {
                    const reader = new FileReader();
                    reader.onload = (e) => {
                        const img = new Image();
                        img.onload = () => resolve(img);
                        img.src = e.target.result;
                    };
                    reader.readAsDataURL(file);
                });
            },
            // 转换图片为 ICO
            canvasToIcoBlob: function(img) {
                const canvas = document.createElement('canvas');
                canvas.width = 32;
                canvas.height = 32;
                const ctx = canvas.getContext('2d');
                ctx.drawImage(img, 0, 0, 32, 32);

                return new Promise((resolve) => {
                    canvas.toBlob(async (pngBlob) => {
                        const pngBuffer = await pngBlob.arrayBuffer();
                        const pngArray = new Uint8Array(pngBuffer);
                        const view = new DataView(new ArrayBuffer(22 + pngArray.length));

                        // ICO 文件头
                        view.setUint16(0, 0, true);
                        view.setUint16(2, 1, true);
                        view.setUint16(4, 1, true);
                        // 目录项
                        view.setUint8(6, 32);
                        view.setUint8(7, 32);
                        view.setUint16(12, 32, true);
                        view.setUint32(14, pngArray.length, true);
                        view.setUint32(18, 22, true);

                        const final = new Uint8Array(view.buffer);
                        final.set(pngArray, 22);
                        resolve(new Blob([final], { type: 'image/x-icon' }));
                    }, 'image/png');
                });
            },
            appIcoHandleRemove:function(file, files) {
                var index = -1;
                index = this.form.appIco.findIndex(function(e){return e.uid == file.uid} );
                if (index != -1) {
                    this.form.appIco.splice(index, 1);
                }
            },
            //刷新缓存
            refreshCache: function() {
                var that = this;
                ms.http.post(ms.manager + "/basic/app/refreshCache.do").then(function (data) {
                    if (data.result) {
                        that.$notify({
                            title: '成功',
                            type: 'success',
                            message: '刷新成功!'
                        });
                        that.get();
                    } else {
                        that.$notify({
                            title: '失败',
                            message: data.msg,
                            type: 'warning'
                        });
                    }
                });

            },
            // 上传之前检测是否是图片类型
            beforeImageUpload: function (file) {
                var type = file.type;
                if (type) {
                    var isImage = type.startsWith('image/');
                    if (!isImage) {
                        this.$notify({
                            title: '提示',
                            message: '请上传图片文件',
                            type: 'warning'
                        });
                    }
                    return isImage;
                }
                return false;
            }
        },
        created: function () {
            this.queryAppTemplateSkin();
            this.get();
        }
    });
</script>
