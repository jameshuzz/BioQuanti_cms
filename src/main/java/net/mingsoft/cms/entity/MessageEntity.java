package net.mingsoft.cms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import net.mingsoft.base.entity.BaseEntity;

/**
 * 客户留言实体
 * 创建日期：2026-08-24<br/>
 * 历史修订：<br/>
 */
@TableName("bq_message")
public class MessageEntity extends BaseEntity {

    private static final long serialVersionUID = 1756000000000L;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 姓名/称呼
     */
    private String name;
    /**
     * 公司/单位
     */
    private String company;
    /**
     * 国家/地区
     */
    private String country;
    /**
     * 城市/省份
     */
    private String region;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 电话(含国际区号)
     */
    private String phone;
    /**
     * 微信
     */
    private String wechat;
    /**
     * WhatsApp
     */
    private String whatsapp;
    /**
     * Telegram
     */
    private String telegram;
    /**
     * LinkedIn
     */
    private String linkedin;
    /**
     * 偏好联系方式:email/phone/wechat/whatsapp/telegram
     */
    private String preferredContact;
    /**
     * 留言内容
     */
    private String content;
    /**
     * 提交者IP
     */
    private String ip;
    /**
     * IP归属地
     */
    private String ipRegion;
    /**
     * 来源页面
     */
    private String referer;
    /**
     * 浏览器标识
     */
    private String userAgent;
    /**
     * 处理状态:0未处理,1已处理
     */
    private Integer status;
    /**
     * 管理员备注
     */
    private String remark;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWechat() {
        return wechat;
    }

    public void setWechat(String wechat) {
        this.wechat = wechat;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public String getTelegram() {
        return telegram;
    }

    public void setTelegram(String telegram) {
        this.telegram = telegram;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin;
    }

    public String getPreferredContact() {
        return preferredContact;
    }

    public void setPreferredContact(String preferredContact) {
        this.preferredContact = preferredContact;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIpRegion() {
        return ipRegion;
    }

    public void setIpRegion(String ipRegion) {
        this.ipRegion = ipRegion;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
