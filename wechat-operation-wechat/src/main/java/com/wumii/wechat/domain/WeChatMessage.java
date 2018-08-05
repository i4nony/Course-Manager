package com.wumii.wechat.domain;

public class WeChatMessage {

    private MessageType type;
    private String teacherWxid;

    public WeChatMessage(MessageType type, String teacherWxid) {
        this.type = type;
        this.teacherWxid = teacherWxid;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getTeacherWxid() {
        return teacherWxid;
    }

    public void setTeacherWxid(String teacherWxid) {
        this.teacherWxid = teacherWxid;
    }
}
