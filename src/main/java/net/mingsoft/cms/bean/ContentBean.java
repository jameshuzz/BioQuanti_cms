

package net.mingsoft.cms.bean;

import net.mingsoft.cms.entity.ContentEntity;

/**
 * 文章实体bean
 */
public class ContentBean extends ContentEntity {

//    /**
//     * 静态化地址
//     */
//    private String staticUrl;

    /**
     * 开始时间
     */
    private String beginTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 属性标记
     */
    private String flag;

    /**
     * 不包含属性标记
     */
    private String noflag;

    /**
     * 栏目类型，用于筛选文章列表
     */
    private String categoryType;

    /**
     * 栏目属性，用于筛选文章列表
     */
    private String categoryFlag;

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public String getCategoryFlag() {
        return categoryFlag;
    }

    public void setCategoryFlag(String categoryFlag) {
        this.categoryFlag = categoryFlag;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getNoflag() {
        return noflag;
    }

    public void setNoflag(String noflag) {
        this.noflag = noflag;
    }
}
