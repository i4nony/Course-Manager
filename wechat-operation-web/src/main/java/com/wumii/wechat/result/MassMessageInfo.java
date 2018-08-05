package com.wumii.wechat.result;

import com.wumii.wechat.domain.MessagesInfo;

import java.util.List;

public class MassMessageInfo {
    private String fromWxid;

    private List<String> toWxIds;

    private MessagesInfo messagesInfo;

    public String getFromWxid() {
        return fromWxid;
    }

    public void setFromWxid(String fromWxid) {
        this.fromWxid = fromWxid;
    }

    public List<String> getToWxIds() {
        return toWxIds;
    }

    public void setToWxIds(List<String> toWxIds) {
        this.toWxIds = toWxIds;
    }

    public MessagesInfo getMessagesInfo() {
        return messagesInfo;
    }

    public void setMessagesInfo(MessagesInfo messagesInfo) {
        this.messagesInfo = messagesInfo;
    }
}
