package com.wumii.wechat.result;

import java.time.Instant;

public class MassMessageTemplateInfo {
    private long id;
    private String contentJson;
    private String name;
    private Instant creationTime;

    public MassMessageTemplateInfo(long id, String contentJson, Instant creationTime) {
        this.id = id;
        this.contentJson = contentJson;
        this.creationTime = creationTime;
    }

    public long getId() {
        return id;
    }

    public String getContentJson() {
        return contentJson;
    }

    public Instant getCreationTime() {
        return creationTime;
    }
}
