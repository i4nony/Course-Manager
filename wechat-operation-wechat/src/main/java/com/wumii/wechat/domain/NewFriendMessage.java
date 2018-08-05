package com.wumii.wechat.domain;

public class NewFriendMessage extends WeChatMessage {

    private String wxid;

    public NewFriendMessage(String teacherWxid, String wxid) {
        super(MessageType.NEW_FRIEND, teacherWxid);
        this.wxid = wxid;
    }

    public String getWxid() {
        return wxid;
    }

    public void setWxid(String wxid) {
        this.wxid = wxid;
    }
}
