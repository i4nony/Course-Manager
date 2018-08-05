package com.wumii.wechat.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.wumii.application.util.DateTimeUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity
public class WeChatGroup {

    @Id
    @Column(length = 64)
    private String wxid;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 64)
    private String ownerWxid;

    @Column(nullable = false)
    private String avatarUrlPath;

    @Column(nullable = false)
    private boolean deleted;

    @Column(columnDefinition = "timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private Instant creationTime;

    private WeChatGroup() {}

    public WeChatGroup(String wxid, String name, String ownerWxid, String avatarUrlPath,
                       boolean deleted) {
        this.wxid = wxid;
        this.name = name;
        this.ownerWxid = ownerWxid;
        this.avatarUrlPath = avatarUrlPath;
        this.deleted = deleted;
        this.creationTime = DateTimeUtils.now();
    }

    public String getWxid() {
        return wxid;
    }

    public String getName() {
        return name;
    }

    public String getOwnerWxid() {
        return ownerWxid;
    }

    public String getAvatarUrlPath() {
        return avatarUrlPath;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeChatGroup that = (WeChatGroup) o;
        return deleted == that.deleted &&
                Objects.equal(wxid, that.wxid) &&
                Objects.equal(name, that.name) &&
                Objects.equal(ownerWxid, that.ownerWxid) &&
                Objects.equal(avatarUrlPath, that.avatarUrlPath) &&
                Objects.equal(creationTime, that.creationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(wxid, name, ownerWxid, avatarUrlPath, deleted, creationTime);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("wxid", wxid)
                .add("name", name)
                .add("ownerWxid", ownerWxid)
                .add("avatarUrlPath", avatarUrlPath)
                .add("deleted", deleted)
                .add("creationTime", creationTime)
                .toString();
    }
}
