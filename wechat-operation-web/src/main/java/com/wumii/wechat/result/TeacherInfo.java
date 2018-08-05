package com.wumii.wechat.result;

import com.wumii.wechat.entity.base.TeacherStatus;

public class TeacherInfo {
    private String wxid;
    private String nickName;
    private String  qrCodeUrl;
    private TeacherStatus teacherStatus;

    public String getWxid() {
        return wxid;
    }

    public String getNickName() {
        return nickName;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public TeacherStatus getTeacherStatus() {
        return teacherStatus;
    }

    public TeacherInfo(String wxid, String nickName, String qrCodeUrl, TeacherStatus teacherStatus) {
        this.wxid = wxid;
        this.nickName = nickName;
        this.qrCodeUrl = qrCodeUrl;
        this.teacherStatus = teacherStatus;
    }
}
