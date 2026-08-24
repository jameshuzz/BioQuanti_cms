



package net.mingsoft.config;

import cn.hutool.core.util.RandomUtil;
import net.mingsoft.base.MSVersion;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 自动注入ms的环境配置（特别注意：只有开源用到，更高级的插件推荐使用自定义配置），使用该类的值会脱离自定义配置
 * @author 铭软开发团队
 * @date 2021年12月31日
 */
@ConfigurationProperties(prefix = "ms")
@Component
public class MSProperties {

    MSProperties() {
        System.out.println("___  ____             _____        __ _    ___  ___ _____ ");
        System.out.println("|  \\/  (_)           /  ___|      / _| |   |  \\/  |/  ___|");
        System.out.println("| .  . |_ _ __   __ _\\ `--.  ___ | |_| |_  | .  . |\\ `--. ");
        System.out.println("| |\\/| | | '_ \\ / _` |`--. \\/ _ \\|  _| __| | |\\/| | `--. \\");
        System.out.println("| |  | | | | | | (_| /\\__/ / (_) | | | |_  | |  | |/\\__/ /");
        System.out.println("\\_|  |_/_|_| |_|\\__, \\____/ \\___/|_|  \\__| \\_|  |_/\\____/ ");
        System.out.println("                 __/ |                                    ");
        System.out.println("                |___/                                     ");
        System.out.println(" :: BioQuanti CMS ::                  (v" + MSVersion.getVersion() + ")");
    }



    /**
     * 上传配置配置
     */
    public static UploadProperties upload = new UploadProperties();


    /**
     * manager管理配置
     */
    public static ManagerProperties manager = new ManagerProperties();

    /**
     * people管理配置
     */
    public static PeopleProperties people = new PeopleProperties();

    /**
     * diy管理配置
     */
    public static DiyProperties diy = new DiyProperties();



    /**
     * shiro-key cookie加密的密钥
     */
    public static String shiroKey = RandomUtil.randomString(16);

    /**
     * shiro-key cookie加密的密钥
     */
    public static String cookieName = "SHIRO_SESSION_ID";



    public static class UploadProperties {

        /**
         * 是否开启上传
         */
        public boolean enableWeb = true;

        /**
         * 模板文件夹
         */
        public String template = "template";

        /**
         * 文件上传路径，可以根据实际写绝对路径
         */
        public String path = "upload";
        /**
         * 上传文件保存的路径
         *
         */
        public String mapping = "/upload/**";

        /**
         * 上传过滤的文件类型
         */
        public String denied = "exe,jsp";
        /**
         * 备份文件夹
         */
        public String backUp = "/upload_back";

        public MultipartProperties multipart = new MultipartProperties();

        public boolean isEnableWeb() {
            return enableWeb;
        }

        public void setEnableWeb(boolean enableWeb) {
            this.enableWeb = enableWeb;
        }

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getMapping() {
            return mapping;
        }

        public void setMapping(String mapping) {
            this.mapping = mapping;
        }

        public String getDenied() {
            return denied;
        }

        public void setDenied(String denied) {
            this.denied = denied;
        }

        public String getBackUp() {
            return backUp;
        }

        public void setBackUp(String backUp) {
            this.backUp = backUp;
        }

        public void denied() {
        }

        public MultipartProperties getMultipart() {
            return multipart;
        }

        public void setMultipart(MultipartProperties multipart) {
            this.multipart = multipart;
        }
    }

    public static class MultipartProperties{
        /**
         * 文件大小
         */
        public long maxFileSize = 1024;
        /**
         * 最大请求大小
         */
        public long maxRequestSize = 10240;
        /**
         * 开启延时加载
         */
        public boolean resolveLazily = false;
        /**
         * 文件编码
         */
        public String defaultEncoding =  "ISO-8859-1";
        /**
         * 文件临时存放目录  springboot3 设置临时目录是String类型
         */
        public String uploadTempDir = "temp";
        /**
         * 临时文件大小
         */
        public int maxInMemorySize = 4096;

        public long getMaxFileSize() {
            return maxFileSize;
        }

        public void setMaxFileSize(long maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        public long getMaxRequestSize() {
            return maxRequestSize;
        }

        public void setMaxRequestSize(long maxRequestSize) {
            this.maxRequestSize = maxRequestSize;
        }

        public boolean isResolveLazily() {
            return resolveLazily;
        }

        public void setResolveLazily(boolean resolveLazily) {
            this.resolveLazily = resolveLazily;
        }

        public String getDefaultEncoding() {
            return defaultEncoding;
        }

        public void setDefaultEncoding(String defaultEncoding) {
            this.defaultEncoding = defaultEncoding;
        }

        public String getUploadTempDir() {
            return uploadTempDir;
        }

        public void setUploadTempDir(String uploadTempDir) {
            this.uploadTempDir = uploadTempDir;
        }

        public int getMaxInMemorySize() {
            return maxInMemorySize;
        }

        public void setMaxInMemorySize(int maxInMemorySize) {
            this.maxInMemorySize = maxInMemorySize;
        }
    }


    public static class ManagerProperties {
        /**
         * 后台访问的路径
         */
        public String path = "/ms";
        /**
         * 台视图层路径配置
         */
        public String viewPath = "/WEB-INF/manager";
        /**
         * 是否开启验证码认证
         * 默认开启验证码验证，false验证码不验证
         */
        public boolean checkCode = true;

        /**
         * 后台登录地址
         */
        public String loginPath = "/login.do";

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getViewPath() {
            return viewPath;
        }

        public void setViewPath(String viewPath) {
            this.viewPath = viewPath;
        }

        public boolean isCheckCode() {
            return checkCode;
        }

        public void setCheckCode(boolean checkCode) {
            this.checkCode = checkCode;
        }

        public String getLoginPath() {
            return loginPath;
        }

        public void setLoginPath(String loginPath) {
            this.loginPath = loginPath;
        }
    }

    public static class PeopleProperties {

        /**
         * 会员登录地址
         */
        public String loginUrl = "/mdiyPage/pc/login.do";

        public String getLoginUrl() {
            return loginUrl;
        }

        public void setLoginUrl(String loginUrl) {
            this.loginUrl = loginUrl;
        }
    }

    public static class DiyProperties {

        /**
         * 生成html文件夹的路径
         */
        public  String htmlDir = "html";

        public String getHtmlDir() {
            return htmlDir;
        }

        public void setHtmlDir(String htmlDir) {
            this.htmlDir = htmlDir;
        }
    }



    public String getShiroKey() {
        return shiroKey;
    }

    public void setShiroKey(String shiroKey) {
        MSProperties.shiroKey = shiroKey;
    }

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        MSProperties.cookieName = cookieName;
    }

    public ManagerProperties getManager() {
        return manager;
    }

    public void setManager(ManagerProperties manager) {
        MSProperties.manager = manager;
    }

    public UploadProperties getUpload() {
        return upload;
    }

    public void setUpload(UploadProperties upload) {
        MSProperties.upload = upload;
    }

    public PeopleProperties getPeople() {
        return people;
    }

    public void setPeople(PeopleProperties people) {
        MSProperties.people = people;
    }

    public DiyProperties getDiy() {
        return diy;
    }

    public void setDiy(DiyProperties diy) {
        MSProperties.diy = diy;
    }
}
