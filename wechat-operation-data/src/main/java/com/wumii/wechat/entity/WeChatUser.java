package com.wumii.wechat.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.wumii.application.util.DateTimeUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity
public class WeChatUser {

    @Id
    @Column(length = 64)
    private String wxid;

    @Column(nullable = false)
    private String avatarUrlPath;

    @Column(nullable = false, length = 100)
    private String nickName;

    @Column(length = 100)
    private String remarkName;

    @Column(columnDefinition = "timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private Instant creationTime;

    @Column(columnDefinition = "timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private Instant updateTime;

    private WeChatUser() {}

    public WeChatUser(String wxid, String avatarUrlPath, String nickName) {
        this.wxid = wxid;
        this.avatarUrlPath = avatarUrlPath;
        this.nickName = nickName;
        this.creationTime = DateTimeUtils.now();
        this.updateTime = DateTimeUtils.now();
    }

    public String getWxid() {
        return wxid;
    }

    public String getAvatarUrlPath() {
        return avatarUrlPath;
    }

    public String getNickName() {
        return nickName;
    }

    public void setAvatarUrlPath(String avatarUrlPath) {
        this.avatarUrlPath = avatarUrlPath;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getRemarkName() {
        return remarkName;
    }

    public void setRemarkName(String remarkName) {
        this.remarkName = remarkName;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public Instant getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Instant updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeChatUser that = (WeChatUser) o;
        return Objects.equal(wxid, that.wxid) &&
                Objects.equal(avatarUrlPath, that.avatarUrlPath) &&
                Objects.equal(nickName, that.nickName) &&
                Objects.equal(remarkName, that.remarkName) &&
                Objects.equal(creationTime, that.creationTime) &&
                Objects.equal(updateTime, that.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(wxid, avatarUrlPath, nickName, remarkName, creationTime, updateTime);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("wxid", wxid)
                .add("avatarUrlPath", avatarUrlPath)
                .add("nickName", nickName)
                .add("remarkName", remarkName)
                .add("creationTime", creationTime)
                .add("updateTime", updateTime)
                .toString();
    }
}
