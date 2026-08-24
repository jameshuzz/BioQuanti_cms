<template type="text/x-template" id="ms-picdetail-upload">
    <div class="ms-picdetail-upload">
        <el-upload
                ref="upload"
                :file-list="fileListProp"
                :action="action"
                :headers="headers"
                :method="method"
                :limit="limit"
                :multiple="false"
                :disabled="disabled"
                :data="data"
                :on-preview="handleOnPreview"
                :on-exceed="handleOnExceed"
                :on-success="handleOnSuccess"
                :on-error="handleOnError"
                :on-remove="handleOnRemove"
                :accept="accept"
                :before-upload="handleBeforeUpload"
                :before-remove="beforeRemove"
                list-type="picture-card">
            <i class="el-icon-plus"></i>
            <template #tip>
                <slot name="tip">
                    <div class="el-upload__tip">最多上传{{limit}}个文件</div>
                </slot>
            </template>
            <template #file="{ file }">
                <img class="el-upload-list__item-thumbnail" :src="file.url" alt=""/>
                <span class="el-upload-list__item-actions">
                <span class="el-upload-list__item-preview" @click="handleOnPreview(file)">
                    <i class="el-icon-zoom-in"></i>
                </span>
                <span class="el-upload-list__item-delete" @click="handleOnRemove(file)">
                    <i class="el-icon-delete"></i>
                </span>
            </span>
                <!-- 显示描述文本 -->
                <el-input @input="addDesc(file.desc, file.uid)" v-model="file.desc" style=" padding: 4px; flex:1" :rows="6" type="textarea"
                          placeholder="请求输入图片描述" @keydown="preventBackspaceOnDescInput($event)"/>
            </template>
        </el-upload>
    </div>
</template>
<script>
	var MsPicdetailUpload = Vue.defineComponent({
    name: 'MsPicdetailUpload',
		template: '#ms-picdetail-upload',
		props: {
			// 绑定值
			fileList: {
				type: Array,
				default: () => [],
			},
			data: {
				type: Object
			},
			action: {
				type: String,
				required: true

			},
			multiple: {
				type: Boolean,
				default: false
			},
			disabled: {
				type: Boolean,
				default: false
			},
			limit: {
				type: Number,
				default: 0
			},
			listType: {
				type: String,
				default: ''
			},
			accept: {
				type: String,
				default: ''
			},
			headers: {
				type: Object
			},
			method: {
				type: String,
				default: 'post'
			},
			onPreview: {
				type: Function
			},
			// 移除文件
			onRemove: {
				type: Function
			},
			//上传成功后回调
			onSuccess: {
				type: Function
			},
			//上传失败后回调
			onError: {
				type: Function
			},
			onProgress: {
				type: Function
			},
			//文件状态改变
			onChange: {
				type: Function
			},
			//上传超过限制
			onExceed: {
				type: Function
			},
			//上传前验证
			beforeUpload: {
				type: Function
			},
			beforeRemove: {
				type: Function
			},


		},
		data: function () {
			return {
				fileListProp: this.fileList,
				//用于追踪哪些文件正在被悬停
				isHovered: {},
			}
		},
		watch: {
			fileList: {
				deep: true,
				handler: function (n,o){
					if (n) {
						// 当父组件更新文件列表时同步更新
						this.fileListProp = [...n];
					}
				}
			}
		},
		methods: {
			// 更新文件描述
			updateFileDesc: function (uid, newDesc) {
				const file = this.fileListProp.find(item => item.uid === uid);
				if (file) file.desc = newDesc;
			},

			// 添加上传前验证
			handleBeforeUpload(file) {
				// 如果父组件有beforeUpload方法，则调用父组件方法
				if (this.beforeUpload instanceof Function) {
					return this.beforeUpload(file);
				}
				return true;
			},
			// 删除
			handleOnRemove: function (file, files) {
				if (this.onRemove && this.onRemove instanceof Function) {
					return this.onRemove(file, files);
				}
				var index = -1;
				index = this.fileListProp.findIndex(function (text) {
					return text.uid == file.uid;
				});
				if (index != -1) {
					this.fileListProp.splice(index, 1);
				}
			},
			//imgUpload上传超过限制
			handleOnExceed: function (files, fileList) {
				var that = this;
				if (this.onExceed instanceof Function) {
					this.onExceed(files, fileList);
				}
				// 如果为0表示不限制
				if (this.limit === 0) {
					return;
				}
				this.$notify({
					title: '失败',
					message: '当前最多上传'+ that.limit +'个文件',
					type: 'warning'
				});
			},
			//预览
			handleOnPreview: function (file) {
				if (this.onPreview && this.onPreview instanceof Function) {
					return this.onPreview(file);
				}
				window.open(file.url);
			},
			//上传成功
			handleOnSuccess: function (response, file, fileList) {
				if (this.onSuccess && this.onSuccess instanceof Function) {
					return this.onSuccess(response, file, fileList);
				}
				if (response.result) {
                    if(!response.data.startsWith("http://") && !response.data.startsWith("https://")) {
                        file.url = ms.contextpath + response.data;
                    }else{
                        file.url = response.data;
                    }
                    this.fileListProp.push({
                        url: file.url,
                        name: file.name,
                        uid: file.uid,
                        desc: ''
                    });
				} else {
					this.$notify({
						title: '失败',
						message: response.msg,
						type: 'warning'
					});
				}
			},
			//上传失败
			handleOnError: function (response, file, fileList) {
				if (this.onError && this.onError instanceof Function) {
					return this.onError(response, file, fileList);
				}
				response = response.toString().replace("Error: ", "")
				response = JSON.parse(response);
				this.$notify({
					title: '失败',
					message: response.msg,
					type: 'warning'
				});
			},

			preventBackspaceOnDescInput(event) {
				if (event.key === 'Backspace' && event.target.value) {
					event.stopPropagation()
				}
			},
			addDesc:function (desc, uid) {
				this.fileList.forEach(function (file) {
					if (file.uid === uid) {
						file.desc = desc;
					}
				});
			}
		},
		created: function () {
			var that = this
		}
	})
</script>
<style scoped>

    .ms-picdetail-upload   .el-upload-list--picture-card .el-upload-list__item  {
        width: 100%;
        height: unset;
    }

    .ms-picdetail-upload  .el-upload-list--picture-card  .el-upload-list__item-thumbnail {
        width: 240px;
        max-height: 144px;
    }
    .ms-picdetail-upload  .el-upload-list--picture-card  .el-upload-list__item-actions {
        justify-content: start;
        padding-left: 8%;
    }
    .ms-picdetail-upload  .el-upload-list--picture-card {
        width:100%
    }
</style>
