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
public class MassMessageTemplate extends IdEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigserial")
    private long id;

    @Column(columnDefinition = "text")
    private String contentJson;

    @Column(columnDefinition = "timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private Instant creationTime;

    MassMessageTemplate() {
    }

    public MassMessageTemplate(String contentJson) {
        this.contentJson = contentJson;
        this.creationTime = DateTimeUtils.now();
    }

    @Override
    public long getId() {
        return id;
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
        MassMessageTemplate that = (MassMessageTemplate) o;
        return id == that.id &&
                Objects.equal(contentJson, that.contentJson) &&
                Objects.equal(creationTime, that.creationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, contentJson, creationTime);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("contentJson", contentJson)
                .add("creationTime", creationTime)
                .toString();
    }
}
