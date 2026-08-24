<template type="text/x-template" id="dialog">
<el-dialog  :append-to-body="true" id="form" :close-on-click-modal="false" v-cloak title="城市" v-model="dialogVisible"
           width="50%">
    <el-form ref="form" :model="form" :rules="rules" label-width="120px" label-position="right">
        <!--省-->
        <el-form-item :label="cityLabel" prop="name">
            <el-input
                    v-model="form.name"
                    :disabled="false"
                    :readonly="false"
                    :style="{width:  '100%'}"
                    :clearable="true"
                    placeholder="请输入名称">
            </el-input>
        </el-form-item>
    </el-form>
    <template #footer>
        <el-button size="default" @click="dialogVisible = false">取消</el-button>
        <el-button size="default" type="primary" @click="save()" :loading="saveDisabled">保存</el-button>
    </template>

</el-dialog>
</template>
<script>
    var formDialog = Vue.defineComponent({
        template: '#dialog',
        data: function () {
            return {
                loading: false,
                saveDisabled: false,
                dialogVisible: false,
                //表单数据
                form: {
                    sqlWhere: null,
                    // 省
                    provinceName: '',
                    provinceId: '',
                    // 市
                    cityName: '',
                    cityId: '',
                    // 县
                    countyName: '',
                    countyId: '',
                    // 镇
                    townName: '',
                    townId: '',
                    // 村
                    villageName: '',
                    villageId: '',

					name:'',//通用名称

                },
                rules: {
                    name: [{"min": 0, "max": 255, "message": "长度必须为0-255"}],

                },
				cityLabel: '省',
                isSave:false

            }
        },
        watch: {

            dialogVisible: function (v) {
                if (!v) {
                    this.$refs.form.id = null;
                    this.$refs.form.resetFields();
                }
            }
        },
        components: {},
        computed: {},
        methods: {

            open: function (row, isSave,cityLabel) {
                
            	var that = this
				
                if (!isSave) {
                    this.get(row);
                }else {
                    that.form = {}
					if (!row.cityId){
						that.form.provinceId = row.provinceId
					}else if(!row.countyId){
						that.form.cityId = row.cityId
					}else if(!row.townId){
						that.form.countyId = row.countyId
					}else if(!row.villageId){
						that.form.townId = row.townId
					}
				}
                that.cityLabel = cityLabel
                that.isSave = isSave
                this.$nextTick(function () {
                    this.dialogVisible = true;

                })
            },
            save: function () {
                var that = this;
                var url = ms.manager + "/basic/city/save.do"
                if (!that.isSave) {
                    url = ms.manager + "/basic/city/update.do";
                }
                if(!that.form.name){
                   that.$notify({
                       title: "错误",
                       message: "地区不能为空",
                       type: 'warning'
                   });
                   return;
                }
                if(that.form.villageId){
                    if (that.isSave){

                    }else {
                        that.form.villageName = that.form.name
                    }
                }else if(that.form.townId){
                    if (that.isSave){
                        that.form.villageName = that.form.name

                    }else {
                        that.form.townName = that.form.name
                    }
                }else if(that.form.countyId){
                    if (that.isSave){
                        that.form.townName = that.form.name

                    }else {
                        that.form.countyName = that.form.name

                    }
                }else if (that.form.cityId){
                    if (that.isSave){
                        that.form.countyName = that.form.name
                    }else {
                        that.form.cityName = that.form.name
                    }
                }else if (that.form.provinceId){
                    if (that.isSave){
                        that.form.cityName = that.form.name
                    }else {
                        that.form.provinceName = that.form.name
                    }
                } else if (!that.form.provinceId){
                    if (that.isSave){
                        that.form.provinceName = that.form.name
                    }
                }












                //
                // if (that.form.provinceId){
                //     if (that.isSave){
                //         that.form.cityName = that.form.name
                //     }else {
                //         that.form.provinceName = that.form.name
                //     }
                // }else if (that.form.cityId){
                //     if (that.isSave){
                //         that.form.countyName = that.form.name
                //     }else {
                //         that.form.cityName = that.form.name
                //     }
                // }else if(that.form.countyId){
                //     if (that.isSave){
                //         that.form.townName = that.form.name
                //
                //     }else {
                //         that.form.countyName = that.form.name
                //
                //     }
                // }else if(that.form.townId){
                //     if (that.isSave){
                //         that.form.villageName = that.form.name
                //
                //     }else {
                //         that.form.townName = that.form.name
                //     }
                // }else if(that.form.villageId){
                //     if (that.isSave){
                //
                //     }else {
                //         that.form.villageName = that.form.name
                //     }
                // } else if (!that.form.provinceId){
                //     if (that.isSave){
                //         that.form.provinceName = that.form.name
                //     }
                // }
                this.$refs.form.validate(function (valid) {
                    if (valid) {
                        that.saveDisabled = true;
                        var form = JSON.parse(JSON.stringify(that.form));
                        ms.http.post(url, form).then(function (res) {
                            if (res.result) {
                                that.$notify({
                                    title: "成功",
                                    message: "保存成功",
                                    type: 'success'
                                });
                                that.dialogVisible = false;
                                that.$parent.list();
                            } else {
                                that.$notify({
                                    title: "错误",
                                    message: res.msg,
                                    type: 'warning'
                                });
                            }

                            that.saveDisabled = false;
                        }).catch(function (err) {
                            console.err(err);
                            that.saveDisabled = false;
                        });
                    } else {
                        return false;
                    }
                })
            },

            //获取当前城市
            get: function (row) {
            	
                var that = this;
				if (row.provinceId){
					that.form.provinceId = row.provinceId
                    that.form.cityId = ''
                    that.form.name = row.provinceName
				}else if (row.cityId){
					that.form.cityId = row.cityId
                    that.form.countyId = ''
                    that.form.name = row.cityName
				}else if(row.countyId){
					that.form.countyId = row.countyId
                    that.form.townId = ''
                    that.form.name = row.countyName
				}else if(row.townId){
					that.form.townId = row.townId
                    that.form.villageId = ''
					that.form.name = row.townName
				}else if(row.villageId){
					that.form.villageId = row.villageId
					that.form.name = row.villageName
				}
            },
        },
        created: function () {
            var that = this;
        }
    });

</script>