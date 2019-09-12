package com.example.mybatis.model;

/**
 * @auther chen.haitao
 * @date 2019-09-12
 */
public class TempTable {

    private Integer uid;


    private  Long upgradeTime;


    private  Long firstUpgradeTime;


    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Long getUpgradeTime() {
        return upgradeTime;
    }

    public void setUpgradeTime(Long upgradeTime) {
        this.upgradeTime = upgradeTime;
    }

    public Long getFirstUpgradeTime() {
        return firstUpgradeTime;
    }

    public void setFirstUpgradeTime(Long firstUpgradeTime) {
        this.firstUpgradeTime = firstUpgradeTime;
    }
}
