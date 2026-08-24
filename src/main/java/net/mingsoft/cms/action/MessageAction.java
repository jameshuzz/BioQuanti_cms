package net.mingsoft.cms.action;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.annotation.LogAnn;
import net.mingsoft.basic.bean.EUListBean;
import net.mingsoft.basic.constant.e.BusinessTypeEnum;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.cms.biz.IMessageBiz;
import net.mingsoft.cms.entity.MessageEntity;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户留言管理控制层
 * 创建日期：2026-08-24<br/>
 * 历史修订：<br/>
 */
@Tag(name = "后端-留言管理接口")
@Controller("cmsMessageAction")
@RequestMapping("/${ms.manager.path}/cms/message")
public class MessageAction extends BaseAction {

    /**
     * 注入留言业务层
     */
    @Autowired
    private IMessageBiz messageBiz;

    /**
     * 返回留言管理主界面
     */
    @Hidden
    @GetMapping("/index")
    @RequiresPermissions("cms:message:view")
    public String index() {
        return "/cms/message/index";
    }

    /**
     * 查询留言列表接口（分页，支持姓名/邮箱/国家/状态筛选）
     */
    @Operation(summary = "查询留言列表接口")
    @Parameters({
            @Parameter(name = "name", description = "姓名", required = false, in = ParameterIn.QUERY),
            @Parameter(name = "email", description = "邮箱", required = false, in = ParameterIn.QUERY),
            @Parameter(name = "country", description = "国家/地区", required = false, in = ParameterIn.QUERY),
            @Parameter(name = "status", description = "处理状态:0未处理,1已处理", required = false, in = ParameterIn.QUERY),
    })
    @RequestMapping(value = "/list", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @RequiresPermissions("cms:message:view")
    public ResultData list(@ModelAttribute @Parameter(hidden = true) MessageEntity message) {
        BasicUtil.startPage();
        LambdaQueryWrapper<MessageEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(message.getName())) {
            wrapper.like(MessageEntity::getName, message.getName());
        }
        if (StringUtils.isNotBlank(message.getEmail())) {
            wrapper.like(MessageEntity::getEmail, message.getEmail());
        }
        if (StringUtils.isNotBlank(message.getCountry())) {
            wrapper.like(MessageEntity::getCountry, message.getCountry());
        }
        if (message.getStatus() != null) {
            wrapper.eq(MessageEntity::getStatus, message.getStatus());
        }
        wrapper.orderByDesc(MessageEntity::getCreateDate);
        List<MessageEntity> list = messageBiz.list(wrapper);
        return ResultData.build().success(new EUListBean(list, (int) BasicUtil.endPage(list).getTotal()));
    }

    /**
     * 获取留言详情接口
     */
    @Operation(summary = "获取留言详情接口")
    @Parameter(name = "id", description = "编号", required = true, in = ParameterIn.QUERY)
    @GetMapping("/get")
    @RequiresPermissions("cms:message:view")
    @ResponseBody
    public ResultData get(@ModelAttribute @Parameter(hidden = true) MessageEntity message) {
        if (StringUtils.isBlank(message.getId())) {
            return ResultData.build().error(getResString("err.empty", this.getResString("id")));
        }
        MessageEntity _message = messageBiz.getById(message.getId());
        return ResultData.build().success(_message);
    }

    /**
     * 更新留言（处理状态/管理员备注）
     */
    @Operation(summary = "更新留言接口")
    @Parameters({
            @Parameter(name = "id", description = "编号", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "status", description = "处理状态:0未处理,1已处理", required = false, in = ParameterIn.QUERY),
            @Parameter(name = "remark", description = "管理员备注", required = false, in = ParameterIn.QUERY),
    })
    @PostMapping("/update")
    @ResponseBody
    @LogAnn(title = "更新留言", businessType = BusinessTypeEnum.UPDATE)
    @RequiresPermissions("cms:message:update")
    public ResultData update(@RequestBody MessageEntity message) {
        if (StringUtils.isBlank(message.getId())) {
            return ResultData.build().error(getResString("err.empty", this.getResString("id")));
        }
        // 只允许更新状态与备注，客户原始提交信息不可篡改
        MessageEntity entity = new MessageEntity();
        entity.setId(message.getId());
        entity.setStatus(message.getStatus());
        entity.setRemark(message.getRemark());
        messageBiz.updateById(entity);
        return ResultData.build().success();
    }

    /**
     * 批量删除留言
     */
    @Operation(summary = "删除留言接口")
    @Parameter(name = "ids", description = "编号集合", required = true, in = ParameterIn.QUERY)
    @PostMapping("/delete")
    @ResponseBody
    @LogAnn(title = "删除留言", businessType = BusinessTypeEnum.DELETE)
    @RequiresPermissions("cms:message:del")
    public ResultData delete(@RequestBody List<MessageEntity> messages) {
        for (MessageEntity message : messages) {
            messageBiz.removeById(message.getId());
        }
        return ResultData.build().success();
    }
}
