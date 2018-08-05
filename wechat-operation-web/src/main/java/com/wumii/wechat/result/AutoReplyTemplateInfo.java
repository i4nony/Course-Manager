package com.wumii.wechat.result;

import com.wumii.wechat.entity.base.ReplyType;

public class AutoReplyTemplateInfo {
    private long id;
    private String name;
    private ReplyType replyType;
    private String contentJson;

    public AutoReplyTemplateInfo(long id, String name, ReplyType replyType, String contentJson) {
        this.id = id;
        this.name = name;
        this.replyType = replyType;
        this.contentJson = contentJson;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ReplyType getReplyType() {
        return replyType;
    }

    public String getContentJson() {
        return contentJson;
    }
}
