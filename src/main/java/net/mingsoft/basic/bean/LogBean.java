






package net.mingsoft.basic.bean;

import net.mingsoft.basic.entity.LogEntity;

/**
 * @Author: xierz
 * @Description:
 * @Date: Create in 2021/01/04 14:16
 */
public class LogBean extends LogEntity {

    private String startTime;

    private String endTime;

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}

