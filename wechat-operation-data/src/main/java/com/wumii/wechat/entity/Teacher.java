package com.wumii.wechat.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.wumii.application.util.DateTimeUtils;
import com.wumii.wechat.entity.base.TeacherStatus;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.Instant;

@Entity
public class Teacher {
    @Id
    @Column(length = 64)
    private String wxid;

    private String qrcodeUrlPath;

    @Column(nullable = false)
    private boolean deleted;

    private byte[] syncKey;

    private String timeLineMD5;

    @Column(columnDefinition = "bigint NOT NULL DEFAULT 0")
    private long timeLineId;

    @Column(columnDefinition = "int NOT NULL DEFAULT 0")
    private int uin;

    private byte[] sessionKey;

    private byte[] cookie;

    private byte[] autoLoginKey;

    @Column(columnDefinition = "timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private Instant creationTime;

    private Teacher() { }

    public Teacher(String wxid) {
        this.wxid = wxid;
        this.deleted = false;
        this.creationTime = DateTimeUtils.now();
    }

    public String getWxid() {
        return wxid;
    }

    public String getQrcodeUrlPath() {
        return qrcodeUrlPath;
    }

    public void setQrcodeUrlPath(String qrcodeUrlPath) {
        this.qrcodeUrlPath = qrcodeUrlPath;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public byte[] getSyncKey() {
        return syncKey;
    }

    public void setSyncKey(byte[] syncKey) {
        this.syncKey = syncKey;
    }

    public String getTimeLineMD5() {
        return timeLineMD5;
    }

    public void setTimeLineMD5(String timeLineMD5) {
        this.timeLineMD5 = timeLineMD5;
    }

    public long getTimeLineId() {
        return timeLineId;
    }

    public void setTimeLineId(long timeLineId) {
        this.timeLineId = timeLineId;
    }

    public int getUin() {
        return uin;
    }

    public void setUin(int uin) {
        this.uin = uin;
    }

    public byte[] getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(byte[] sessionKey) {
        this.sessionKey = sessionKey;
    }

    public byte[] getAutoLoginKey() {
        return autoLoginKey;
    }

    public void setAutoLoginKey(byte[] autoLoginKey) {
        this.autoLoginKey = autoLoginKey;
    }

    public byte[] getCookie() {
        return cookie;
    }

    public void setCookie(byte[] cookie) {
        this.cookie = cookie;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Teacher teacher = (Teacher) o;
        return deleted == teacher.deleted &&
                timeLineId == teacher.timeLineId &&
                uin == teacher.uin &&
                Objects.equal(wxid, teacher.wxid) &&
                Objects.equal(qrcodeUrlPath, teacher.qrcodeUrlPath) &&
                Objects.equal(syncKey, teacher.syncKey) &&
                Objects.equal(timeLineMD5, teacher.timeLineMD5) &&
                Objects.equal(sessionKey, teacher.sessionKey) &&
                Objects.equal(cookie, teacher.cookie) &&
                Objects.equal(autoLoginKey, teacher.autoLoginKey) &&
                Objects.equal(creationTime, teacher.creationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(wxid, qrcodeUrlPath, deleted, syncKey, timeLineMD5, timeLineId, uin, sessionKey, cookie, autoLoginKey, creationTime);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("wxid", wxid)
                .add("qrcodeUrlPath", qrcodeUrlPath)
                .add("deleted", deleted)
                .add("syncKey", syncKey)
                .add("timeLineMD5", timeLineMD5)
                .add("timeLineId", timeLineId)
                .add("uin", uin)
                .add("sessionKey", sessionKey)
                .add("cookie", cookie)
                .add("autoLoginKey", autoLoginKey)
                .add("creationTime", creationTime)
                .toString();
    }
}
