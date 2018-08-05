package com.wumii.wechat.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.wumii.application.entity.IdEntity;
import com.wumii.application.util.DateTimeUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.Instant;

@Entity
public class Friend extends IdEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigserial")
    private long id;

    @Column(nullable = false, length = 64)
    private String wxid;

    @Column(nullable = false, length = 64)
    private String friendWxid;

    @Column(columnDefinition = "timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private Instant creationTime;

    Friend() {
    }

    public Friend(String wxid, String friendWxid) {
        this.wxid = wxid;
        this.friendWxid = friendWxid;
        this.creationTime = DateTimeUtils.now();
    }

    @Override
    public long getId() {
        return id;
    }

    public String getWxid() {
        return wxid;
    }

    public String getFriendWxid() {
        return friendWxid;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Friend friend = (Friend) o;
        return id == friend.id &&
                Objects.equal(wxid, friend.wxid) &&
                Objects.equal(friendWxid, friend.friendWxid) &&
                Objects.equal(creationTime, friend.creationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, wxid, friendWxid, creationTime);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("wxid", wxid)
                .add("friendWxid", friendWxid)
                .add("creationTime", creationTime)
                .toString();
    }
}
