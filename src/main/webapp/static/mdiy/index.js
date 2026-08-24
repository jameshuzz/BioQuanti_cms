/**
 * 自定义模块前端工具
 */
(function () {


    let URLS = {
        "config": {
            "get": "/mdiy/config/get.do",
            formURL: {
                update: {
                    "url": "/mdiy/config/data/update.do",
                    "params": {}
                },
                save: {
                    "url": "/mdiy/config/data/update.do",
                    "params": {}
                },
                get: {
                    "url": "/mdiy/config/data/get.do",
                    "params": {}
                }
            },

        },
        "form": {
            "get": "/mdiy/form/get.do",
            formURL: {
                update: {
                    "url": "/mdiy/form/data/update.do",
                    "params": {}
                },
                save: {
                    "url": "/mdiy/form/data/save.do",
                    "params": {}
                },
                get: {
                    "url": "/mdiy/form/data/getData.do",
                    "params": {}
                }
            },
        },
        "model": {
            "get": "/mdiy/model/get.do",
            formURL: {
                update: {
                    "url": "/mdiy/model/data/update.do",
                    "params": {}
                },
                save: {
                    "url": "/mdiy/model/data/save.do",
                    "params": {}
                },
                get: {
                    "url": "/mdiy/model/data.do",
                    "params": {}
                }
            },
        }
    };

    /**
     * 获取自定义配置
     * @param configName:配置名称
     * @paramk key:配置的key值(可选)，key == null 可以获取所有的配置
     * @param isSystem true 后台调用会拼接后台地址 false前台调用
     */
    function config(configName, key,isSystem) {

        var data = {configName: configName};

        if(key != null) {
            data.key = key;
        }

        if(isSystem) {
            return ms.http.get(ms.manager + "/mdiy/config/data/getMap.do",data);
        } else {
            return ms.http.get(ms.base + "/mdiy/config/get.do",data);
        }

    }



    var dict = {
        /**
         *  获取所有字典类型的数组集合
         * @param dictType 字典类型 支持多个类型用英文逗号隔开
         * @param isChild 子业务类型 可选
         */
        list: function(dictType, isChild) {
            var _dict = {
                dictType: dictType,
                isChild: isChild,
            }
            return ms.http.get(ms.base + '/mdiy/dict/list.do', _dict);
        },
        /**
         * 根据字典 value 获取对应的 label
         * @param dictList 字典列表
         * @param value 值
         * @returns {string|string|[{message: string, required: boolean}]|*}
         */
        getLabel: function(dictList, value) {
            var data = dictList.find(function (v) {
                return v.dictValue == value;
            });

            if (data) {
                return data.dictLabel;
            }
            return "";
        },
        /**
         * 根据字典 label 获取对应的 value
         * @param dictList 字典列表
         * @param label 值
         * @returns {string|string|[{message: string, required: boolean}]|*}
         */
        getValue: function(dictList, label) {
            var data = dictList.find(function (v) {
                return v.dictLabel == label;
            });

            if (data) {
                return data.dictValue;
            }
            return "";
        }

    }


    var model = {

        /**
         * 模型地址资源配置
         */

        /**
         * 配置模型
         * @param renderDomId 绑定的domid,表单会显示在dom里面
         * @param model 模型对象 示例：业务调用{modelName:模型名称}，自动业务使用{id:模型编号}
         * @param params 请求参数 示例：{参数名称:值}
         * @param isSystem true 后台调用会拼接后台地址 false前台调用
         * @returns {Promise<custom_model>}
         */
        config:  function(renderDomId, model, params, isSystem) {

            URLS.config.formURL.get.params = params;
            var urlStrs = JSON.stringify(URLS.config.formURL);
            return this.render(renderDomId, model, JSON.parse(urlStrs), URLS["config"].get, isSystem);
        },

        /**
         * 业务表单模型
         * @param renderDomId 绑定的domid,表单会显示在dom里面
         * @param model 模型对象 示例：业务调用{modelName:模型名称}，自动业务使用{id:模型编号}
         * @param params 请求参数 示例：{参数名称:值}
         * @param isSystem true 后台调用会拼接后台地址 false前台调用
         * @returns {Promise<custom_model>}
         */
        form:  function(renderDomId, model, params, isSystem) {
            URLS.form.formURL.get.params = params;
            var urlStrs = JSON.stringify(URLS.form.formURL);

            return this.render(renderDomId, model, JSON.parse(urlStrs), URLS["form"].get, isSystem);
        },

        /**
         * 扩展业务模型
         * @param renderDomId 绑定的domid,表单会显示在dom里面
         * @param model 模型对象 示例：业务调用方式{modelName:模型名称}如配置的使用，自动业务使用{id:模型编号},如内容模型
         * @param params 请求参数 示例：{参数名称:值}
         * @param isSystem true 后台调用会拼接后台地址 false前台调用
         * @returns {Promise<custom_model>}
         */
        extend: function(renderDomId, model, params, isSystem) {

            URLS.model.formURL.get.params = params;
            var urlStrs = JSON.stringify(URLS.model.formURL);

            return this.render(renderDomId, model,JSON.parse(urlStrs) , URLS["model"].get, isSystem);
        },

        /**
         * 前台自定义模型渲染和数据读取
         * @param renderDomId 绑定的domid,表单会显示在dom里面
         * @param model 模型对象 示例：业务调用方式{id:模型id}
         * @param formUrls 请求地址，一般用来定义getDate地址和参数，如{get:{url:"",param:{"modelId":"","linkId":""}}}
         * @param url 模型获取地址，模块下自定义的获取模型接口
         * @returns {Promise<custom_model>}
         */
        getModelData: function(renderDomId, model, formUrls, url) {
            if (formUrls) {
                URLS.model.formURL.get = formUrls;
            }
            var urlStrs = JSON.stringify(URLS.model.formURL);

            return this.render(renderDomId, model, JSON.parse(urlStrs) ,url, false);
        },

        /**
         * iframe方式渲染自定义模型对象
         * @param renderDomId 绑定的domid,表单会显示在dom里面
         * @param model 模型对象 示例：业务调用{modelName:模型名称}，自动业务使用{id:模型编号}
         * @param formUrls 模型接口地址集合，{save:{url:"",param:{}},update:{url:"",param:{}},get:{url:"",param:{}}}
         * @param url 模型获取地址
         * @param isSystem true 后台调用会拼接后台地址 false前台调用
         * @returns {Promise<custom_model>} 返回model对象
         */
        render:  function(renderDomId, model, formUrls, url, isSystem) {
            // if(model.modelName!=undefined || model.modelId!=undefined){
            //     return null;
            // }
            //查模型

            if (isSystem) {
                url = ms.manager + url;
                formUrls.update.url = ms.manager + formUrls.update.url;
                formUrls.save.url = ms.manager + formUrls.save.url;
                formUrls.get.url = ms.manager + formUrls.get.url;

                return new Promise(function(resolve,reject) {

                    if("undefined" != typeof formVue)  {
                        form = formVue;
                    }

                    form.$nextTick(function () {
                        var iframe = document.createElement('iframe');
                        iframe.setAttribute("id", "___custom-model");
                        iframe.setAttribute("border", "0");
                        iframe.setAttribute("frameborder", "no");
                        iframe.setAttribute("style", "height:calc(100vh - 80px)");
                        iframe.width = "100%";

                        iframe.setAttribute("src", ms.manager+"/mdiy/model/formTmpl.do?url="+url+"&formUrls="+encodeURIComponent(JSON.stringify(formUrls))+"&model="+encodeURIComponent(JSON.stringify(model)));
                        var modelDiv = document.getElementById(renderDomId);
                        modelDiv.appendChild(iframe);
                        //触发父窗口的方法
                        window._callbackCustomModel = function (modelObj) {
                            resolve(modelObj);
                        }
                    })

                })

            } else {
                url = ms.base + url;
                formUrls.update.url = ms.base + formUrls.update.url;
                formUrls.save.url = ms.base + formUrls.save.url;
                formUrls.get.url = ms.base + formUrls.get.url;

                var modelId = 0;
                var modelName = "";
                return new Promise(function(resolve,reject) {

                    ms.http.get(url, model).then(function (res) {
                        if (res.result && res.data) {
                            //因为命名规范问做容错处理
                            if("undefined" != typeof formVue)  {
                                form = formVue;
                            }
                            form.mdiyModel = res.data;
                            modelId = res.data.id;
                            modelName = res.data.modelName;
                            var data = JSON.parse(form.mdiyModel.modelJson);
                            form.$nextTick(function () {

                                var modelDom = document.getElementById(renderDomId);
                                modelDom.innerHTML="";
                                var modelDom = document.getElementById(renderDomId);
                                var scriptDom = document.createElement('script');
                                // 如果param有值则获取模型数据
                                if (!formUrls.get.params || !formUrls.get.params.linkId) {
                                    data.script = data.script.replace("this.get(this.form.linkId);",'').replace("that.get(this.form.linkId);",'');
                                }
                                // 处理上传图片组件地址，默认是ms.manager+'/file/upload.do',那么这样前台自定义渲染上传就失效
                                data.html = data.html.replaceAll("ms.manager+'/file/upload.do'", "ms.base+'/file/upload.do'");
                                scriptDom.innerHTML = data.script;
                                var divDom = document.createElement('div');
                                // 增加时间当随机数，防止渲染模型时覆盖
                                divDom.id = 'custom-model' + Date.now();
                                divDom.innerHTML = data.html;

                                modelDom.appendChild(scriptDom);
                                modelDom.appendChild(divDom); //初始化自定义模型并传入关联参数
                                //  promise抛出resolve进行外部调用自定义模型
                                resolve(new custom_model({
                                    data: {
                                        modelName: modelName,
                                        modelId: modelId,
                                        formURL: formUrls,
                                        // 返回模型所有字段数据供外部调用
                                        modelData: res.data,
                                    }
                                }))
                            });

                        }
                    });
                })
            }


        },


        /**
         * 获取相关联自定义模型对象
         * @returns 自定义模型对象
         */
        modelForm: function () {
            return document.getElementById("___custom-model").contentWindow._custom_model;
        }
    }

    var mdiy = {
        dict: dict,
        model: model,
        config: config
    }


    if (typeof ms != "object") {
        window.ms = {};
    }
    window.ms.mdiy = mdiy;
    window.ms.debug = false
}());
