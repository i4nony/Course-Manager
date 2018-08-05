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
public class UserGroup extends IdEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigserial")
    private long id;

    @Column(nullable = false, length = 64)
    private String userWxid;

    @Column(nullable = false, length = 64)
    private String groupWxid;

    @Column(columnDefinition = "timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private Instant creationTime;

    private UserGroup() {}

    public UserGroup(String userWxid, String groupWxid) {
        this.userWxid = userWxid;
        this.groupWxid = groupWxid;
        this.creationTime = DateTimeUtils.now();
    }

    @Override
    public long getId() {
        return id;
    }

    public String getUserWxid() {
        return userWxid;
    }

    public String getGroupWxid() {
        return groupWxid;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserGroup userGroup = (UserGroup) o;
        return id == userGroup.id &&
                Objects.equal(userWxid, userGroup.userWxid) &&
                Objects.equal(groupWxid, userGroup.groupWxid) &&
                Objects.equal(creationTime, userGroup.creationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, userWxid, groupWxid, creationTime);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("userWxid", userWxid)
                .add("groupWxid", groupWxid)
                .add("creationTime", creationTime)
                .toString();
    }
}
