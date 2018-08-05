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
public abstract class Message extends IdEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigserial")
    private long id;

    @Column(nullable = false, length = 64)
    private String fromWxid;

    @Column(nullable = false, length = 64)
    private String toWxid;

    private Instant creationTime;

    Message() {}

    public Message(String fromWxid, String toWxid) {
        this.fromWxid = fromWxid;
        this.toWxid = toWxid;
        this.creationTime = DateTimeUtils.now();
    }

    @Override
    public long getId() {
        return id;
    }

    public String getFromWxid() {
        return fromWxid;
    }

    public String getToWxid() {
        return toWxid;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return id == message.id &&
                Objects.equal(fromWxid, message.fromWxid) &&
                Objects.equal(toWxid, message.toWxid) &&
                Objects.equal(creationTime, message.creationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, fromWxid, toWxid, creationTime);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("fromWxid", fromWxid)
                .add("toWxid", toWxid)
                .add("creationTime", creationTime)
                .toString();
    }
}
