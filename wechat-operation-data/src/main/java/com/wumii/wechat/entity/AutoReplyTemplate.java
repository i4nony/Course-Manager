package com.wumii.wechat.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.wumii.application.entity.IdEntity;
import com.wumii.application.util.DateTimeUtils;
import com.wumii.wechat.entity.base.ReplyType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.Instant;

@Entity
public class AutoReplyTemplate extends IdEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigserial")
    private long id;

    @Column(length = 64)
    private String wxid;

    @Enumerated(EnumType.STRING)
    private ReplyType replyType;

    @Column(columnDefinition = "text")
    private String contentJson;

    @Column(columnDefinition = "timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private Instant creationTime;

    AutoReplyTemplate(){
    }

    public AutoReplyTemplate(String wxid, ReplyType replyType, String contentJson) {
        this.wxid = wxid;
        this.replyType = replyType;
        this.contentJson = contentJson;
        this.creationTime = DateTimeUtils.now();
    }

    @Override
    public long getId() {
        return id;
    }

    public String getWxid() {
        return wxid;
    }

    public ReplyType getReplyType() {
        return replyType;
    }

    public String getContentJson() {
        return contentJson;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AutoReplyTemplate that = (AutoReplyTemplate) o;
        return id == that.id &&
                Objects.equal(wxid, that.wxid) &&
                replyType == that.replyType &&
                Objects.equal(contentJson, that.contentJson) &&
                Objects.equal(creationTime, that.creationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, wxid, replyType, contentJson, creationTime);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("wxid", wxid)
                .add("replyType", replyType)
                .add("contentJson", contentJson)
                .add("creationTime", creationTime)
                .toString();
    }
}
