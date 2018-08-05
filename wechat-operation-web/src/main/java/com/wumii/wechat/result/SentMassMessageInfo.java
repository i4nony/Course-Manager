package com.wumii.wechat.result;

import java.time.Instant;

public class SentMassMessageInfo {
    private long id;
    private String teacherNickName;
    private String contentJson;
    private int peopleNumber;
    private Instant creationTime;

    public SentMassMessageInfo(long id, String teacherNickName, String contentJson, int peopleNumber, Instant creationTime) {
        this.id = id;
        this.teacherNickName = teacherNickName;
        this.contentJson = contentJson;
        this.peopleNumber = peopleNumber;
        this.creationTime = creationTime;
    }

    public long getId() {
        return id;
    }

    public String getTeacherNickName() {
        return teacherNickName;
    }

    public String getContentJson() {
        return contentJson;
    }

    public int getPeopleNumber() {
        return peopleNumber;
    }

    public Instant getCreationTime() {
        return creationTime;
    }
}
