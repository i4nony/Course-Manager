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
public class SentMassMessage extends IdEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigserial")
    private long id;

    @Column(nullable = false, length = 64)
    private String wxid;

    @Column(columnDefinition = "text")
    private String contentJson;

    private int peopleNumber;

    @Column(columnDefinition = "timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private Instant creationTime;

    SentMassMessage() {
    }

    public SentMassMessage(String wxid, String contentJson, int peopleNumber) {
        this.wxid = wxid;
        this.contentJson = contentJson;
        this.peopleNumber = peopleNumber;
        this.creationTime = DateTimeUtils.now();
    }

    @Override
    public long getId() {
        return id;
    }

    public String getWxid() {
        return wxid;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SentMassMessage that = (SentMassMessage) o;
        return id == that.id &&
                peopleNumber == that.peopleNumber &&
                Objects.equal(wxid, that.wxid) &&
                Objects.equal(contentJson, that.contentJson) &&
                Objects.equal(creationTime, that.creationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, wxid, contentJson, peopleNumber, creationTime);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("wxid", wxid)
                .add("contentJson", contentJson)
                .add("peopleNumber", peopleNumber)
                .add("creationTime", creationTime)
                .toString();
    }
}
