package com.wumii.wechat.result;

import com.wumii.wechat.domain.MessagesInfo;
import com.wumii.wechat.entity.base.ReplyType;

public class AutoReplyTemplyInfo {
    private String wxid;
    private ReplyType replyType;
    private MessagesInfo messagesInfo;

    public String getWxid() {
        return wxid;
    }

    public void setWxid(String wxid) {
        this.wxid = wxid;
    }

    public ReplyType getReplyType() {
        return replyType;
    }

    public void setReplyType(ReplyType replyType) {
        this.replyType = replyType;
    }

    public MessagesInfo getMessagesInfo() {
        return messagesInfo;
    }

    public void setMessagesInfo(MessagesInfo messagesInfo) {
        this.messagesInfo = messagesInfo;
    }
}
