







package net.mingsoft.basic.util;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import org.apache.commons.lang3.StringUtils;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.InputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @ClassName: IpUtils
 * @Description: TODO(ip工具类)
 * @author 铭软开发团队
 * @date 2020年7月2日
 * 修订记录 2022-3-14  getRealAddressByIp() 从ip.ws.126.net获取改为本地获取
 */
public class IpUtils {
    private static final Logger LOG = LoggerFactory.getLogger(IpUtils.class);

    public static final String IP_URL = "https://www.ip.cn/api/index?ip=%s&type=1";

    public static boolean internalIp(String ip) {
        byte[] addr = textToNumericFormatV4(ip);
        return internalIp(addr) || "127.0.0.1".equals(ip);
    }

    private static boolean internalIp(byte[] addr) {
        if (ObjectUtil.isNull(addr) || addr.length < 2) {
            return true;
        }
        final byte b0 = addr[0];
        final byte b1 = addr[1];
        // 10.x.x.x/8
        final byte SECTION_1 = 0x0A;
        // 172.16.x.x/12
        final byte SECTION_2 = (byte) 0xAC;
        final byte SECTION_3 = (byte) 0x10;
        final byte SECTION_4 = (byte) 0x1F;
        // 192.168.x.x/16
        final byte SECTION_5 = (byte) 0xC0;
        final byte SECTION_6 = (byte) 0xA8;
        switch (b0) {
            case SECTION_1:
                return true;
            case SECTION_2:
                if (b1 >= SECTION_3 && b1 <= SECTION_4) {
                    return true;
                }
            case SECTION_5:
                switch (b1) {
                    case SECTION_6:
                        return true;
                    default:
                        //throw new IllegalStateException("Unexpected value: " + b1);
                        //如果不在范围全部都可以理解内网ip，不能直接跑出上面的异常否则会导致登录失败
                        return  true;
                }
            default:
                return false;
        }
    }


    /**
     * 通过IP获取地理位置
     * @param ip
     * @return
     */
    public static String getRealAddressByIp(String ip) {
        String address = "XX XX";
        // 内网不查询
        if (IpUtils.internalIp(ip)) {
            return "内网IP";
        }
        try {

            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("ip2region.db");
            if (resources.length > 0 && resources[0].exists()){//若有db文件则本地获取
                LOG.debug("通过ip2region.db获取ip");
                InputStream inputStream =resources[0].getInputStream();
                byte[] dbBinStr = IoUtil.readBytes(inputStream);
                //根据ip进行位置信息搜索
                DbConfig config = new DbConfig();
                //获取ip库的位置（放在resources下）
                DbSearcher searcher = new DbSearcher(config, dbBinStr);
                //采用Btree搜索
                DataBlock block = searcher.memorySearch(ip);
                String region = block.getRegion();
                String[] city = region.split("\\|");
                if (city[2].equals("0") || city[3].equals("0")) {
                    LOG.debug("获取地理位置异常 {}", ip);
                    return "地址库未收录";
                }
                address = region.replaceAll("0","").replaceAll("\\|"," ");
            }else {//通过ip.ws.126.net获取
                LOG.debug("通过{}获取ip",IP_URL);
                String rest = HttpUtil.get(String.format(IP_URL,ip));
                if (StringUtils.isEmpty(rest)) {
                    LOG.debug("获取地理位置异常 {}", ip);
                    return address;
                }

                address = JSONUtil.parseObj(rest).get("address").toString();
            }
            LOG.debug("ip{}",ip);
        } catch (Exception e) {
            LOG.error("获取地理位置异常 {}", ip);
            e.printStackTrace();
        }
        return address;
    }

    /**
     * 将IPv4地址转换成字节
     *
     * @param text IPv4地址
     * @return byte 字节
     */
    public static byte[] textToNumericFormatV4(String text) {
        if (text.length() == 0) {
            return null;
        }

        byte[] bytes = new byte[4];
        String[] elements = text.split("\\.", -1);
        try {
            long l;
            int i;
            switch (elements.length) {
                case 1:
                    l = Long.parseLong(elements[0]);
                    if ((l < 0L) || (l > 4294967295L)) {
                        return null;
                    }
                    bytes[0] = (byte) (int) (l >> 24 & 0xFF);
                    bytes[1] = (byte) (int) ((l & 0xFFFFFF) >> 16 & 0xFF);
                    bytes[2] = (byte) (int) ((l & 0xFFFF) >> 8 & 0xFF);
                    bytes[3] = (byte) (int) (l & 0xFF);
                    break;
                case 2:
                    l = Integer.parseInt(elements[0]);
                    if ((l < 0L) || (l > 255L)) {
                        return null;
                    }
                    bytes[0] = (byte) (int) (l & 0xFF);
                    l = Integer.parseInt(elements[1]);
                    if ((l < 0L) || (l > 16777215L)) {
                        return null;
                    }
                    bytes[1] = (byte) (int) (l >> 16 & 0xFF);
                    bytes[2] = (byte) (int) ((l & 0xFFFF) >> 8 & 0xFF);
                    bytes[3] = (byte) (int) (l & 0xFF);
                    break;
                case 3:
                    for (i = 0; i < 2; ++i) {
                        l = Integer.parseInt(elements[i]);
                        if ((l < 0L) || (l > 255L)) {
                            return null;
                        }
                        bytes[i] = (byte) (int) (l & 0xFF);
                    }
                    l = Integer.parseInt(elements[2]);
                    if ((l < 0L) || (l > 65535L)) {
                        return null;
                    }
                    bytes[2] = (byte) (int) (l >> 8 & 0xFF);
                    bytes[3] = (byte) (int) (l & 0xFF);
                    break;
                case 4:
                    for (i = 0; i < 4; ++i) {
                        l = Integer.parseInt(elements[i]);
                        if ((l < 0L) || (l > 255L)) {
                            return null;
                        }
                        bytes[i] = (byte) (int) (l & 0xFF);
                    }
                    break;
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return bytes;
    }

    /**
     * 获取本机IP地址
     *
     * @return 返回本机IP地址字符串，如果获取失败则返回默认的回环地址"127.0.0.1"
     */
    public static String getHostIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
        }
        return "127.0.0.1";
    }

    /**
     * 获取本地主机的主机名
     *
     * @return 返回本地主机的主机名，如果获取失败则返回"未知"
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
        }
        return "未知";
    }

    /**
     * 是否为IPv4
     *
     * @param ip 待测试的IP地址
     * @return 是否为IPv4
     */
    public static boolean isIpv4(String ip) {
        if (StringUtils.isBlank(ip) || ip.contains(":")){
            return false;
        }
        try {
            return InetAddress.getByName(ip) instanceof Inet4Address;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * 是否为IPv6
     *
     * @param ip 待测试的IP地址
     * @return 是否为IPv6
     */
    public static boolean isIpv6(String ip) {
        if (StringUtils.isBlank(ip) || ip.contains(".")) {
            return false;
        }
        try {
            return InetAddress.getByName(ip) instanceof Inet6Address;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    /**
     * 判断IP地址是否为内部地址
     * @param host IP地址
     * @return 是否为内部地址
     */
    public static boolean isInternalIp(String host) {
        try {
            InetAddress inetAddress = InetAddress.getByName(host);
            // 是否是环回地址 (127.0.0.1) 或 链路本地地址 (169.254.x.x) 或 私有地址 (10.x, 172.16.x, 192.168.x)
            return inetAddress.isLoopbackAddress()
                    || inetAddress.isLinkLocalAddress()
                    || inetAddress.isSiteLocalAddress();
        } catch (UnknownHostException e) {
            // 无法解析的主机，视为风险地址
            return true;
        }
    }
}
