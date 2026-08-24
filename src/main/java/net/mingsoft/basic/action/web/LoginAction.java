








package net.mingsoft.basic.action.web;

import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.mingsoft.base.entity.ResultData;
import net.mingsoft.basic.action.BaseAction;
import net.mingsoft.basic.annotation.LogAnn;
import net.mingsoft.basic.biz.IAppBiz;
import net.mingsoft.basic.biz.IManagerBiz;
import net.mingsoft.basic.constant.e.BusinessTypeEnum;
import net.mingsoft.basic.entity.ManagerEntity;
import net.mingsoft.basic.strategy.ILoginStrategy;
import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.config.MSProperties;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @ClassName: LoginAction
 * @Description:TODO(登录的基础应用层)
 * @date: 2015年1月27日 下午3:21:47
 *
 */
@Tag(name = "前端-基础接口")
@Controller
@RequestMapping("/${ms.manager.path}")
public class LoginAction extends BaseAction {

    /**
     * 站点业务层
     */
    @Autowired
    private IAppBiz appBiz;

    @Autowired
    private ILoginStrategy loginStrategy;


    /**
     * 注入管理员业务层
     */
    @Autowired
    private IManagerBiz managerBiz;


    /**
     * 加载管理员登录界面
     *
     * @param request
     *            请求对象
     * @return 管理员登录界面地址
     */
    @Operation(summary =  "加载管理员登录界面")
    @SuppressWarnings("resource")
    @GetMapping("/login")
    public String login(HttpServletRequest request) {
        String managerPath = MSProperties.manager.path;
        Subject currentSubject = SecurityUtils.getSubject();
        if (BasicUtil.getManager() != null && currentSubject.isAuthenticated()) {
            return "redirect:" + managerPath + "/index.do";
        }
        return "/login";
    }

    /**
     * 验证登录
     *
     * @param manager
     *            管理员实体
     * @param request
     *            请求
     * @param response
     *            响应
     */
    @Operation(summary =  "验证登录")
    @Parameters({
            @Parameter(name = "managerName", description = "帐号", required =  true, in = ParameterIn.QUERY),
            @Parameter(name = "managerPassword", description = "密码", required =  true, in = ParameterIn.QUERY),
    })
    @PostMapping("/login")
    @ResponseBody
    @LogAnn(title = "登陆", businessType = BusinessTypeEnum.LOGIN)
    public ResultData login(@ModelAttribute @Parameter(hidden = true) ManagerEntity manager, HttpServletRequest request, HttpServletResponse response) {
        LOG.debug("basic checkLogin");

        //验证码
        if (!(checkRandCode())) {
            return ResultData.build().error(getResString("err.error", this.getResString("rand.code")));
        }
        if(loginStrategy.login(manager)){
            Map map = new HashMap();
            map.put("sessionId", SecurityUtils.getSubject().getSession().getId());
            return ResultData.build().success().data(map);
        }else {
            return ResultData.build().error(getResString("err.error", this.getResString("manager.name.or.password")));
        }

    }

    @Operation(summary = "验证当前是否已经登录管理员")
    @GetMapping("/checkLogin")
    @ResponseBody
    public ResultData checkLogin(HttpServletResponse response, HttpServletRequest request){
        ManagerEntity managerEntity;
        ManagerEntity manager =  BasicUtil.getManager();
        if (manager == null || !SecurityUtils.getSubject().isAuthenticated()) {
            return ResultData.build().error("管理员已失效!");
        }
        managerEntity = managerBiz.getById(manager.getId());
        if (managerEntity != null){
            managerEntity.setManagerPassword("");
        }
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(managerEntity);
        stringObjectMap.put("sessionId",SecurityUtils.getSubject().getSession().getId());
        return ResultData.build().success(stringObjectMap);
    }
}
