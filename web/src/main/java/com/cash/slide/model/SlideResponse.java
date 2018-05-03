package com.cash.slide.model;

import java.util.Map;

/**
 * author cash
 * create 2018-05-02-13:30
 **/

public class SlideResponse {
    private int retCode;
    private String errMsg;
    private Map data;

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
