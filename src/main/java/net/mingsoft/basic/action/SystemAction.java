
package net.mingsoft.basic.action;

import cn.hutool.system.oshi.CpuInfo;
import cn.hutool.system.oshi.OshiUtil;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import net.mingsoft.base.entity.ResultData;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

/**
 * 系统信息
 * @version
 * 版本号：1.0<br/>
 * 创建日期：2022-8-24 23:40:55<br/>
 * 历史修订：<br/>
 */
@Tag(name = "后端-系统信息")
@Controller
@RequestMapping("/${ms.manager.path}/basic/system")
public class SystemAction extends BaseAction{

    @Autowired
    private DataSource dataSource;
    /**
     * 系统信息
     * @param request
     * @return 表单页面地址
     */
    @Hidden
    @Operation(summary =  "加载UI的表单页面")
    @GetMapping("/index")
    public String index(HttpServletRequest request) {
        return "/basic/system/index";
    }

    /**
     * 获取系统配置信息
     * @param request 请求对象
     * @return true退出成功
     */
    @Operation(summary =  "获取系统配置信息")
    @PostMapping("/info")
    @ResponseBody
    @RequiresPermissions("basic:system:view")
    public ResultData info(HttpServletResponse response, HttpServletRequest request) {

        Properties props = System.getProperties();// 获取当前的系统属性
        HashMap<String, Object> map = new HashMap<>();
        map.put("CPU核数", String.valueOf(Runtime.getRuntime().availableProcessors()));

        // 单位B 转换为M
        map.put("虚拟机内存总量", String.valueOf(Runtime.getRuntime().totalMemory() / 1048576));
        map.put("虚拟机空闲内存量", String.valueOf(Runtime.getRuntime().freeMemory() / 1048576));
        map.put("虚拟机使用最大内存量", String.valueOf(Runtime.getRuntime().maxMemory() / 1048576));

        map.put("系统名称", props.getProperty("os.name"));
        map.put("系统构架", props.getProperty("os.arch"));
        map.put("系统版本", props.getProperty("os.version"));

        map.put("Java版本", props.getProperty("java.version"));
        map.put("Java安装路径", props.getProperty("java.home"));

        CpuInfo cpu = OshiUtil.getCpuInfo();
        map.put("cpu信息", cpu.getCpuModel() + "" + cpu.getCpuNum());
        map.put("内存总量", OshiUtil.getMemory().getTotal() / 1048576);
        map.put("内存可用", OshiUtil.getMemory().getAvailable() / 1048576);

        //数据库
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            DatabaseMetaData mtdt = connection.getMetaData();
            map.put("数据库链接", mtdt.getURL());
            map.put("数据库",mtdt.getDatabaseProductName());
            map.put("数据库版本",mtdt.getDatabaseProductVersion());

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if(connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        ServletContext context = request.getServletContext();
        map.put("web容器",context.getServerInfo());
        map.put("发布路径",context.getRealPath(""));
//
        return ResultData.build().success(map);
    }

}
