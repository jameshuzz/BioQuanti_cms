








package net.mingsoft.basic.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.mingsoft.base.entity.BaseEntity;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.base.exception.BusinessException;
import net.mingsoft.basic.annotation.LogAnn;
import net.mingsoft.basic.bean.EUListBean;
import net.mingsoft.basic.biz.IManagerBiz;
import net.mingsoft.basic.biz.IModelBiz;
import net.mingsoft.basic.biz.IRoleModelBiz;
import net.mingsoft.basic.constant.e.BusinessTypeEnum;
import net.mingsoft.basic.constant.e.ModelIsMenuEnum;
import net.mingsoft.basic.entity.ManagerEntity;
import net.mingsoft.basic.entity.ModelEntity;
import net.mingsoft.basic.entity.RoleModelEntity;
import net.mingsoft.basic.strategy.IModelStrategy;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.basic.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 模块控制层
 * @version
 * 版本号：200-000-000<br/>
 * 创建日期：2014-6-29<br/>
 * 历史修订： 新增 getModelListByManagerSession方法,
 *          修改方法中所有ManagerSession.getRoleId()为getModelListByManagerSession
 *          修改日期: 2022-1-5
 * 2022-1-12 菜单结构调整,使用菜单策略,更改为超级管理员始终拥有所有菜单
 * 2022-1-14 添加菜单排序功能
 * 2023-1-08 优化权限标识及菜单标题校验规则，当菜单为导航链接时不允许标题重复，菜单为功能权限时不允许权限标识重复
 */
@Tag(name = "后端-基础接口")
@Controller
@RequestMapping("/${ms.manager.path}/basic/model")
public class ModelAction extends BaseAction {

    /**
     * 注入模块业务层
     */
    @Autowired
    private IModelBiz modelBiz;

    @Autowired
    private IModelStrategy modelStrategy;

    @Autowired
    private IManagerBiz managerBiz;
    /**
     * 角色模块关联业务层
     */
    @Autowired
    private IRoleModelBiz roleModelBiz;

    /**
     * 返回主界面index
     */
    @Hidden
    @GetMapping("/index")
    @RequiresPermissions("basic:model:view")
    public String index(HttpServletResponse response,HttpServletRequest request,ModelMap mode){
        List<ModelEntity> parentModelList = modelStrategy.list();
        mode.addAttribute("parentModelList", JSONUtil.toJsonStr(parentModelList));
        return "/basic/model/index";
    }


    /**
     * 查询模块表列表
     * @param model 模块表实体
     * <i>model参数包含字段信息参考：</i><br/>
     * id 模块自增长id<br/>
     * modelTitle 模块标题<br/>
     * modelCode 模块编码<br/>
     * modelId 模块的父模块id<br/>
     * modelUrl 模块连接地址<br/>
     * modelDatetime <br/>
     * modelIcon 模块图标<br/>
     * modelSort 模块的排序<br/>
     * modelIsmenu 模块是否是菜单<br/>
     * <dt><span class="strong">返回</span></dt><br/>
     * <dd>[<br/>
     * { <br/>
     * id: 模块自增长id<br/>
     * modelTitle: 模块标题<br/>
     * modelCode: 模块编码<br/>
     * modelId: 模块的父模块id<br/>
     * modelUrl: 模块连接地址<br/>
     * modelDatetime: <br/>
     * modelIcon: 模块图标<br/>
     * modelSort: 模块的排序<br/>
     * modelIsmenu: 模块是否是菜单<br/>
     * }<br/>
     * ]</dd><br/>
     */
    @Operation(summary = "菜单列表接口")
    @GetMapping("/list")
    @ResponseBody
    public ResultData list(@ModelAttribute @Parameter(hidden = true) ModelEntity modelEntity, HttpServletResponse response, HttpServletRequest request, @Parameter(hidden = true) ModelMap model) {
        List<ModelEntity> modelList = modelStrategy.list();
        if(CollectionUtil.isEmpty(modelList)){
            // 该角色在站点中无对应角色
            return ResultData.build().success();
        }
        modelList.sort((o1, o2) -> {
            int sort1 = o1.getModelSort() == null ? 0 : o1.getModelSort();
            int sort2 = o2.getModelSort() == null ? 0 : o2.getModelSort();
            return sort2 - sort1;
        });
        EUListBean _list = new EUListBean(modelList, modelList.size());
        return ResultData.build().success(_list);
    }

    @Operation(summary = "菜单子集列表")
    @GetMapping("/childList")
    @Parameters({
            @Parameter(name = "modelTitle", description = "菜单名称", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "modelId", description = "父级菜单编号", required =  false, in = ParameterIn.QUERY)
    })
    @RequiresPermissions("basic:model:view")
    @ResponseBody
    public ResultData childList(@ModelAttribute @Parameter(hidden = true) ModelEntity modelEntity, HttpServletResponse response, HttpServletRequest request) {
        List<ModelEntity> list = modelBiz.queryChildList(modelEntity);
        return ResultData.build().success(list);
    }

    @Operation(summary = "菜单导入接口")
    @Parameters({
            @Parameter(name = "menuStr", description = "菜单json", required =  true, in = ParameterIn.QUERY),
            @Parameter(name = "modelId", description = "父级菜单编号", required =  true, in = ParameterIn.QUERY)
    })
    @LogAnn(title = "导入菜单",businessType= BusinessTypeEnum.INSERT)
    @PostMapping("/import")
    @ResponseBody
    @RequiresPermissions("basic:model:save")
    public ResultData importMenu(String menuStr,int modelId) {
        if(StringUtils.isBlank(menuStr)){
            return ResultData.build().error(getResString("err.empty", this.getResString("menu")));
        }
        try{
            List<ModelEntity> list = JSONUtil.toList(menuStr, ModelEntity.class);
            ManagerEntity manager = BasicUtil.getManager();
            assert manager != null;


            // 检查是否有重复的菜单标题或者权限标识
            List<String> modelUrlList = new ArrayList<>();
            // 取出菜单的标题以及非菜单的权限标识
            this.addModelUrlList(list,modelUrlList);

            boolean isRepeat = true; // 是否允许自定义配置菜单标题重复
            // 在站群情况且是自定义配置菜单才会不重复检查，只是标题不检查重复，权限还需要检查，防止越权操作
            if (ObjectUtil.isNotNull(BasicUtil.getWebsiteApp()) && list.size() == 1) {
                // 判断是否是自定义配置菜单。 规则：list长度等于1且modelUrl中包含mdiy/config/data/form.do
                for (ModelEntity modelEntity : list) {
                    if (StringUtils.isBlank(modelEntity.getModelUrl())) {
                        continue;
                    }
                    isRepeat = !modelEntity.getModelUrl().contains("mdiy/config/data/form.do");
                }
            }
            LambdaQueryWrapper<ModelEntity> wrapper = null;
            if (isRepeat) {
                // 只判断同级目录菜单名是否重复
                for (ModelEntity model : list) {
                    wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(ModelEntity::getModelTitle,model.getModelTitle())
                            .eq(ModelEntity::getModelIsMenu,ModelIsMenuEnum.MODEL_MEUN.toInt());
                    // 登录0，说明当前导入是顶级菜单
                    if (modelId == 0) {
                        wrapper.isNull(ModelEntity::getModelId);
                    } else {
                        wrapper.eq(ModelEntity::getModelId, modelId);
                    }
                    long count = modelBiz.count(wrapper);
                    if (count > 0) {
                        LOG.debug("以下标题已存在：{}", model.getModelTitle());
                        return ResultData.build().error(getResString("err.exist", model.getModelTitle() + this.getResString("model.title")));
                    }
                }
            }
            // 判断是否有重复的权限标识
            if (CollectionUtil.isNotEmpty(modelUrlList)){
                wrapper = new LambdaQueryWrapper<>();
                wrapper.in(ModelEntity::getModelUrl,modelUrlList);
                List<ModelEntity> duplicateModelUrls = modelBiz.list(wrapper);
                if (CollectionUtil.isNotEmpty(duplicateModelUrls)){
                    // 已存在的权限标识集合，方便打印日志及相应到页面
                    List<String> collect = duplicateModelUrls.stream().map(ModelEntity::getModelUrl).collect(Collectors.toList());
                    LOG.debug("以下标识已存在：{}",StringUtils.join(collect,","));
                    return ResultData.build().error(getResString("err.exist",this.getResString("model.url"))+": "+StringUtils.join(collect,","));
                }
            }
            String parentIds = String.valueOf(modelId);
            // 不是在顶级菜单导入
            if (modelId != 0){
                ModelEntity model = modelBiz.getById(modelId);
                if (model == null){
                    return ResultData.build().error(getResString("err.not.exist", this.getResString("model.id")));
                }
                // 组织父id
                parentIds = StringUtils.isBlank(model.getModelParentIds())?model.getId():model.getModelParentIds() +","+model.getId();
            }
            // 导入菜单
            for (ModelEntity modelEntity : list){
                if (modelEntity.getModelIsMenu() == 0) {
                    return ResultData.build().error("功能权限按钮不能作为菜单导入!");
                }
                modelBiz.importModel(modelEntity, manager.getRoleId(), parentIds, modelId);
            }

        }catch (BusinessException e) {
            // 手动异常精确返回
            return ResultData.build().error(e.getMsg());
        }catch (RuntimeException e){
            e.printStackTrace();
            return ResultData.build().error(getResString("model.title.or.json"));
        }catch (Exception e){
            return ResultData.build().error(getResString("err.error", this.getResString("menu")));
        }
        modelBiz.updateCache();
        return ResultData.build().success();
    }

    /**
     * 获取模块表
     * @param model 模块表实体
     * <i>model参数包含字段信息参考：</i><br/>
     * id 模块自增长id<br/>
     * modelTitle 模块标题<br/>
     * modelCode 模块编码<br/>
     * modelId 模块的父模块id<br/>
     * modelUrl 模块连接地址<br/>
     * modelDatetime <br/>
     * modelIcon 模块图标<br/>
     * modelSort 模块的排序<br/>
     * modelIsmenu 模块是否是菜单<br/>
     * <dt><span class="strong">返回</span></dt><br/>
     * <dd>{ <br/>
     * id: 模块自增长id<br/>
     * modelTitle: 模块标题<br/>
     * modelCode: 模块编码<br/>
     * modelId: 模块的父模块id<br/>
     * modelUrl: 模块连接地址<br/>
     * modelDatetime: <br/>
     * modelIcon: 模块图标<br/>
     * modelSort: 模块的排序<br/>
     * modelIsmenu: 模块是否是菜单<br/>
     * }</dd><br/>
     */
    @Operation(summary =  "获取模块表")
    @Parameter(name = "id", description = "模块的编号", required =  true, in = ParameterIn.QUERY)
    @GetMapping("/get")
    @RequiresPermissions("basic:model:view")
    @ResponseBody
    public ResultData get(@ModelAttribute @Parameter(hidden = true) ModelEntity modelEntity,HttpServletResponse response, HttpServletRequest request,@Parameter(hidden = true) ModelMap model){
        if(StringUtils.isEmpty(modelEntity.getId())) {
            return ResultData.build().error(getResString("err.error", this.getResString("model.id")));
        }
        //根据父模块id查寻模块
        ModelEntity _model = modelBiz.getById(modelEntity.getId());
        if(_model != null){
            Map<String, ModelEntity> mode = new HashMap<String, ModelEntity>();
            if(_model.getModelId() != null){
                ModelEntity parentModel = modelBiz.getById(_model.getModelId());
                mode.put("parentModel", parentModel);
            }
            mode.put("model", _model);
            return ResultData.build().success(mode);
        }
        return ResultData.build().success(_model);
    }

    /**
     * 保存模块表实体
     * @param model 模块表实体
     * <i>model参数包含字段信息参考：</i><br/>
     * id 模块自增长id<br/>
     * modelTitle 模块标题<br/>
     * modelCode 模块编码<br/>
     * modelId 模块的父模块id<br/>
     * modelUrl 模块连接地址<br/>
     * modelDatetime <br/>
     * modelIcon 模块图标<br/>
     * modelSort 模块的排序<br/>
     * modelIsmenu 模块是否是菜单<br/>
     * <dt><span class="strong">返回</span></dt><br/>
     * <dd>{ <br/>
     * id: 模块自增长id<br/>
     * modelTitle: 模块标题<br/>
     * modelCode: 模块编码<br/>
     * modelId: 模块的父模块id<br/>
     * modelUrl: 模块连接地址<br/>
     * modelDatetime: <br/>
     * modelIcon: 模块图标<br/>
     * modelSort: 模块的排序<br/>
     * modelIsmenu: 模块是否是菜单<br/>
     * }</dd><br/>
     */
    @Operation(summary =  "保存模块表实体")
    @Parameters({
            @Parameter(name = "modelTitle", description = "模块的标题", required =  true, in = ParameterIn.QUERY),
            @Parameter(name = "modelCode", description = "模块编码", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "modelId", description = "模块父id", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "modelUrl", description = "链接地址", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "modelIcon", description = "模块图标", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "modelSort", description = "模块排序", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "modelIsMenu", description = "是否是菜单,0:不是 1:是", required =  true, in = ParameterIn.QUERY),
            @Parameter(name = "isChild", description = "菜单类型", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "modelParentIds", description = "父级编号集合", required =  false, in = ParameterIn.QUERY),
    })
    @LogAnn(title = "保存模块表实体",businessType= BusinessTypeEnum.INSERT)
    @PostMapping("/save")
    @ResponseBody
    @RequiresPermissions("basic:model:save")
    public ResultData save(@ModelAttribute @Parameter(hidden = true) ModelEntity model, HttpServletResponse response, HttpServletRequest request) {
        //模块标题验证
        if(StringUtils.isBlank(model.getModelTitle())){
            return ResultData.build().error(getResString("err.empty", this.getResString("model.title")));
        }
        if(!StringUtil.checkLength(model.getModelTitle()+"", 1, 10)){
            return ResultData.build().error(getResString("err.length", this.getResString("model.title"), "1", "10"));
        }

        //菜单类型验证
        if(!StringUtil.checkLength(model.getIsChild()+"", 0, 300)){
            return ResultData.build().error(getResString("err.length", this.getResString("model.is.child"), "0", "300"));
        }
        //模块编码验证
        if(!StringUtil.checkLength(model.getModelCode()+"", 0, 255)){
            return ResultData.build().error(getResString("err.length", this.getResString("model.code"), "0", "255"));
        }
        //模块图标验证
        if(!StringUtil.checkLength(model.getModelIcon()+"", 0, 120)){
            return ResultData.build().error(getResString("err.length", this.getResString("model.icon"), "0", "120"));
        }
        //父级编号集合验证
        if(!StringUtil.checkLength(model.getModelParentIds()+"", 0, 300)){
            return ResultData.build().error(getResString("err.length", this.getResString("model.parent.ids"), "0", "120"));
        }
        //链接地址验证
        if(!StringUtil.checkLength(model.getModelUrl()+"", 0, 255)){
            return ResultData.build().error(getResString("err.length", this.getResString("model.url"), "0", "255"));
        }
        //判断菜单名称不能相同
        if(model.getModelIsMenu() == ModelIsMenuEnum.MODEL_MEUN.toInt()){
            LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ModelEntity::getModelTitle,model.getModelTitle()).eq(ModelEntity::getModelIsMenu,ModelIsMenuEnum.MODEL_MEUN.toInt());
            List<ModelEntity> list = modelBiz.list(wrapper);
            if(CollectionUtil.isNotEmpty(list)){
                return ResultData.build().error(getResString("err.exist",this.getResString("model.title")));
            }
        }
        // 判断菜单url不能为空且不能相同
        if (model.getModelIsMenu()==ModelIsMenuEnum.MODEL_NOTMENU.toInt()){//权限标识的情况下
            //对菜单权限标识进行去空格处理
            model.setModelUrl(model.getModelUrl().trim());
            if (StringUtils.isBlank(model.getModelUrl()))
            return ResultData.build().error(getResString("err.empty",this.getResString("model.url")));
            List<ModelEntity> modelList = modelBiz.list(new LambdaQueryWrapper<ModelEntity>().eq(ModelEntity::getModelUrl, model.getModelUrl()));
            if (CollectionUtil.isNotEmpty(modelList)){
                return ResultData.build().error(getResString("err.exist",this.getResString("model.url")));
            }
        }

        // 获取模块保存时间
        model.setModelDatetime(new Timestamp(System.currentTimeMillis()));
        //判断图标是否为空，不为空去掉,图标地址中含有的“|”
        //空值判断
        if(!StringUtils.isBlank(model.getModelIcon())) {
            model.setModelIcon( model.getModelIcon().replace("|", ""));
        }
        //重复判断，modelCode不能重复
        if(StringUtils.isNotBlank(model.getModelCode())){
            ModelEntity _model = modelBiz.getEntityByModelCode(model.getModelCode());
            if (_model != null){
                return ResultData.build().error(getResString("err.exist",this.getResString("modelCode")));
            }
        }
        if(model.getModelSort() == null){
            model.setModelSort(0);
        }

        // 防止最顶级栏目为空时报NP异常
        if (model.getModelId() != null){
            // 获取到父级model实体
            ModelEntity modelEntity = modelBiz.getById(model.getModelId());
            // 如果父级getModelParentIds为空则必然为顶级
            if (StringUtils.isBlank(modelEntity.getModelParentIds())) {
                model.setModelParentIds(model.getModelId().toString());
            }else {
                model.setModelParentIds(modelEntity.getModelParentIds()+","+model.getModelId().toString());
            }
        }

        modelBiz.saveModel(model);
        //返回模块id到页面
        return ResultData.build().success(model.getId());
    }


    @Operation(summary =  "批量删除模块表")
    @Parameter(name = "ids", description = "模块编号，多个以逗号隔开", required =  false, in = ParameterIn.QUERY)
    @LogAnn(title = "批量删除模块表",businessType= BusinessTypeEnum.DELETE)
    @PostMapping("/delete")
    @ResponseBody
    @RequiresPermissions("basic:model:del")
    public ResultData delete(@RequestBody List<ModelEntity> modelEntityList, HttpServletResponse response, HttpServletRequest request) {
        if (CollUtil.isEmpty(modelEntityList)) {
            return ResultData.build().error(getResString("err.empty",this.getResString("id")));
        }
        modelBiz.delete(modelEntityList);
        return ResultData.build().success();
    }

    /**
     * 更新模块表信息模块表
     * @param model 模块表实体
     * <i>model参数包含字段信息参考：</i><br/>
     * id 模块自增长id<br/>
     * modelTitle 模块标题<br/>
     * modelCode 模块编码<br/>
     * modelId 模块的父模块id<br/>
     * modelUrl 模块连接地址<br/>
     * modelDatetime <br/>
     * modelIcon 模块图标<br/>
     * modelSort 模块的排序<br/>
     * modelIsmenu 模块是否是菜单<br/>
     * <dt><span class="strong">返回</span></dt><br/>
     * <dd>{ <br/>
     * id: 模块自增长id<br/>
     * modelTitle: 模块标题<br/>
     * modelCode: 模块编码<br/>
     * modelId: 模块的父模块id<br/>
     * modelUrl: 模块连接地址<br/>
     * modelDatetime: <br/>
     * modelIcon: 模块图标<br/>
     * modelSort: 模块的排序<br/>
     * modelIsmenu: 模块是否是菜单<br/>
     * }</dd><br/>
     */
    @Operation(summary =  "更新模块表信息模块表")
    @Parameters({
            @Parameter(name = "id", description = "模块的编号", required =  true, in = ParameterIn.QUERY),
            @Parameter(name = "modelTitle", description = "模块的标题", required =  true, in = ParameterIn.QUERY),
            @Parameter(name = "modelCode", description = "模块编码", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "modelId", description = "模块父id", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "modelUrl", description = "链接地址", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "modelIcon", description = "模块图标", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "modelSort", description = "模块排序", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "isChild", description = "菜单类型", required =  false, in = ParameterIn.QUERY),
            @Parameter(name = "modelParentIds", description = "父级编号集合", required =  false, in = ParameterIn.QUERY),
    })
    @LogAnn(title = "更新模块表信息模块表",businessType= BusinessTypeEnum.UPDATE)
    @PostMapping("/update")
    @RequiresPermissions("basic:model:update")
    @ResponseBody
    public ResultData update(@ModelAttribute @Parameter(hidden = true) ModelEntity model, HttpServletResponse response,
                             HttpServletRequest request) {
        //模块标题验证
        if(StringUtils.isBlank(model.getModelTitle())){
            return ResultData.build().error(getResString("err.empty", this.getResString("model.title")));
        }
        if(!StringUtil.checkLength(model.getModelTitle()+"", 1, 10)){
            return ResultData.build().error(getResString("err.length", this.getResString("model.title"), "1", "10"));
        }
        //菜单类型验证
        if(!StringUtil.checkLength(model.getIsChild()+"", 0, 300)){
            return ResultData.build().error(getResString("err.length", this.getResString("model.is.child"), "0", "300"));
        }
        //模块编码验证
        if(!StringUtil.checkLength(model.getModelCode()+"", 0, 255)){
            return ResultData.build().error(getResString("err.length", this.getResString("model.code"), "0", "255"));
        }
        //模块图标验证
        if(!StringUtil.checkLength(model.getModelIcon()+"", 0, 120)){
            return ResultData.build().error(getResString("err.length", this.getResString("model.icon"), "0", "120"));
        }
        //链接地址验证
        if(!StringUtil.checkLength(model.getModelUrl()+"", 0, 255)){
            return ResultData.build().error(getResString("err.length", this.getResString("model.url"), "0", "255"));
        }
        //判断菜单名称不能相同
        if(model.getModelIsMenu() == ModelIsMenuEnum.MODEL_MEUN.toInt()){
            LambdaQueryWrapper<ModelEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ModelEntity::getModelTitle,model.getModelTitle()).eq(ModelEntity::getModelIsMenu,ModelIsMenuEnum.MODEL_MEUN.toInt());
            List<ModelEntity> list = modelBiz.list(wrapper);
            // 查出数据不为空，且集合元素大于一个或者有一个但不是自己
            if(CollectionUtil.isNotEmpty(list) && (list.size()>1 || !list.get(0).getId().equals(model.getId()))){
                return ResultData.build().error(getResString("err.exist",this.getResString("model.title")));
            }
        }
        //判断当前修改的菜单是否是三级菜单
        ModelEntity _model = modelBiz.getById(model.getId());
        if(_model.getModelIsMenu() == 1 && model.getModelIsMenu() == 0){
            return ResultData.build().error(this.getResString("model.is.menu"));
        }
        // 判断菜单url不能为空且不能相同
        if (model.getModelIsMenu()==ModelIsMenuEnum.MODEL_NOTMENU.toInt()){
            //对菜单权限标识进行去空格处理
            model.setModelUrl(model.getModelUrl().trim());
            if (StringUtils.isBlank(model.getModelUrl()))
                return ResultData.build().error(getResString("err.empty",this.getResString("model.url")));
            List<ModelEntity> modelList = modelBiz.list(new LambdaQueryWrapper<ModelEntity>().eq(ModelEntity::getModelUrl, model.getModelUrl()));
            if (CollectionUtil.isNotEmpty(modelList) && !modelList.get(0).getId().equals(model.getId())){
                return ResultData.build().error(getResString("err.exist",this.getResString("model.url")));
            }
        }
        //判断图标是否为空，不为空去掉,图标地址中含有的“|”
        //空值判断
        if(!StringUtils.isBlank(model.getModelIcon())) {
            model.setModelIcon( model.getModelIcon().replace("|", ""));
        }
        // 防止用户传空字符串，导致超管无法查询该菜单
        if (StringUtils.isBlank(_model.getAppId())){
            model.setAppId(null);
        } else {
            // 不让更改
            model.setAppId(_model.getAppId());
        }
        modelBiz.updateEntity(model);
        return ResultData.build().success(model.getId());
    }

    /**
     * 根据管理员ID查询模块集合
     * @param managerId 管理员id
     * @param request 请求对象
     * @param response 响应对象
     */
    @Operation(summary =  "根据管理员ID查询模块集合")
    @Parameter(name = "managerId", description = "管理员id", required = true, in = ParameterIn.PATH)
    @GetMapping("/{managerId}/queryModelByRoleId")
    @ResponseBody
    public ResultData queryModelByRoleId(@PathVariable @Parameter(hidden = true) int managerId,HttpServletRequest request, HttpServletResponse response) {
        ManagerEntity manager =(ManagerEntity) managerBiz.getEntity(managerId);
        if(manager==null){
            return ResultData.build().error();
        }
        HashSet<ModelEntity> modelSet = new HashSet<>();
        for (String roleId : manager.getRoleIds().split(",")) {
            modelSet.addAll(modelBiz.queryModelByRoleId(Integer.parseInt(roleId)));
        }
        List<ModelEntity> modelList = new ArrayList<>(modelSet);
        return ResultData.build().success(modelList);
    }

    /**
     * 查询模块表列表
     * @param model 模块表实体
     * <i>model参数包含字段信息参考：</i><br/>
     * id 模块自增长id<br/>
     * modelTitle 模块标题<br/>
     * modelCode 模块编码<br/>
     * modelId 模块的父模块id<br/>
     * modelUrl 模块连接地址<br/>
     * modelDatetime <br/>
     * modelIcon 模块图标<br/>
     * modelSort 模块的排序<br/>
     * modelIsmenu 模块是否是菜单<br/>
     * <dt><span class="strong">返回</span></dt><br/>
     * <dd>[<br/>
     * { <br/>
     * id: 模块自增长id<br/>
     * modelTitle: 模块标题<br/>
     * modelCode: 模块编码<br/>
     * modelId: 模块的父模块id<br/>
     * modelUrl: 模块连接地址<br/>
     * modelDatetime: <br/>
     * modelIcon: 模块图标<br/>
     * modelSort: 模块的排序<br/>
     * modelIsmenu: 模块是否是菜单<br/>
     * }<br/>
     * ]</dd><br/>
     */
    @Operation(summary =  "查询模块表列表")
    @Parameter(name = "roleId", description = "角色编号", required =  true, in = ParameterIn.QUERY)
    @GetMapping("/modelList")
    @ResponseBody
    public ResultData modelList(@ModelAttribute @Parameter(hidden = true) ModelEntity modelEntity,HttpServletResponse response, HttpServletRequest request,@Parameter(hidden = true) ModelMap model) {
        int roleId = BasicUtil.getInt("roleId");
        ManagerEntity managerSession = BasicUtil.getManager();
        boolean updateFlag = roleId != 0;
        //新增角色roleId为0，默认当前管理员的roleId
        List<ModelEntity> modelList = modelStrategy.list();

        List<ModelEntity> _modelList = new ArrayList<>();
        List<RoleModelEntity> roleModelList = new ArrayList<>();
        if(roleId>0){
            roleModelList = roleModelBiz.queryByRoleId(roleId);
        }else {
            HashSet<RoleModelEntity> roleSet = new HashSet<>();
            for (String id : managerSession.getRoleIds().split(",")) {
                roleSet.addAll(roleModelBiz.queryByRoleId(Integer.parseInt(id)));
            }
            roleModelList.addAll(roleSet);
        }
        List<ModelEntity> childModelList = new ArrayList<>();
        //将菜单和功能区分开
        for(BaseEntity base : modelList){
            ModelEntity _model = (ModelEntity) base;
            if(_model.getModelIsMenu() == 1){
                _model.setModelChildList(new ArrayList<ModelEntity>());
                _modelList.add(_model);
            }else if(_model.getModelIsMenu() == 0){
                childModelList.add(_model);
            }
        }
        //菜单和功能一一匹配
        for(ModelEntity _modelEntity : _modelList){
            for(ModelEntity childModel : childModelList){
                if(childModel.getModelId() == Integer.parseInt(_modelEntity.getId())){
                    _modelEntity.getModelChildList().add(childModel);
                    //选中状态
                    for(RoleModelEntity roleModelEntity : roleModelList){
                        if(roleModelEntity.getModelId() == Integer.parseInt(childModel.getId()) && updateFlag){
                            childModel.setChick(1);
                        }
                    }

                }
            }
        }
        EUListBean _list = new EUListBean(_modelList, _modelList.size());
        return ResultData.build().success(_list);
    }


    /**
     * 递归遍历菜单实体集合，将非菜单的权限标识分别添加modelUrlList并检测菜单标题在同级是否有相同菜单和相同权限
     * 递归的执行条件为 当前遍历到的实体子菜单集合不为空
     * @param modelEntityList 菜单实体集合 不允许为空
     * @param modelUrlList 菜单权限标识集合 不允许为空
     * @throws BusinessException 实体标题长度不合格，或者实体不是菜单且实体的权限标识为空
     */
    private void addModelUrlList(List<ModelEntity> modelEntityList,List<String> modelUrlList){
        // 空判断,集合中没有元素则直接返回
        if (CollectionUtil.isEmpty(modelEntityList) || modelUrlList == null){
            return;
        }
        // 临时存储同级菜单标题
        List<String> modelTitleListTemp = new ArrayList<>();
        // 遍历菜单集合
        for (ModelEntity model : modelEntityList) {
            // 不合规的标题直接抛出异常
            if (!StringUtil.checkLength(model.getModelTitle()+"", 1, 20)){
                throw new BusinessException(getResString("err.length", this.getResString("model.title"), "1", "20"));
            }
            // 当实体不为菜单且权限标识不为空，则向modelUrlList添加一条记录;实体为菜单则向modelTitleList添加一条记录
            if (model.getModelIsMenu()==ModelIsMenuEnum.MODEL_NOTMENU.toInt()){
                if (StringUtils.isBlank(model.getModelUrl())){
                    throw new BusinessException(getResString("err.empty", this.getResString("model.url")));
                }
                // 本次导入有相同权限提示用户
                if (CollUtil.contains(modelUrlList, model.getModelUrl())){
                    throw new BusinessException(getResString("err.exist", model.getModelUrl() + this.getResString("model.url")));
                }
                //对菜单权限标识进行去空格处理
                model.setModelUrl(model.getModelUrl().trim());
                modelUrlList.add(model.getModelUrl());
            }else {
                // 判断同级是否有相同菜单
                if (CollUtil.contains(modelTitleListTemp, model.getModelTitle())) {
                    throw new BusinessException(getResString("err.exist",model.getModelTitle() + this.getResString("model.title")));
                }
                modelTitleListTemp.add(model.getModelTitle());
            }
            // 当前实体有子菜单，则递归执行
            if (CollectionUtil.isNotEmpty(model.getModelChildList())){
                this.addModelUrlList(model.getModelChildList(),modelUrlList);
            }
        }
    }

}
