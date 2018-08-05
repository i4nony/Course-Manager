package com.wumii.wechat.result;

import java.time.Instant;

public class GroupMessageStatistics {
    private String nickName;
    private String avatarUrl;
    private long messageCout;
    private String content;
    private Instant createTime;

    public GroupMessageStatistics(String nickName, String avatarUrl, long messageCout, String content, Instant createTime) {
        this.nickName = nickName;
        this.avatarUrl = avatarUrl;
        this.messageCout = messageCout;
        this.content = content;
        this.createTime = createTime;
    }

    public String getNickName() {
        return nickName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public long getMessageCout() {
        return messageCout;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreateTime() {
        return createTime;
    }
}
