package com.wumii.wechat.domain;

public class CreateChatRoomMessage extends WeChatMessage {

    private String wxid;

    public CreateChatRoomMessage(String teacherWxid, String wxid) {
        super(MessageType.CREATE_CHAT_ROOM, teacherWxid);
        this.wxid = wxid;
    }

    public String getWxid() {
        return wxid;
    }

    public void setWxid(String wxid) {
        this.wxid = wxid;
    }
}
