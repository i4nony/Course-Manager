package com.wumii.wechat.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("VIDEO")
public class VideoMessage extends Message {

    private String videoUrlPath;

    private VideoMessage() {}

    public VideoMessage(String fromWxid, String toWxid, String videoUrlPath) {
        super(fromWxid, toWxid);
        this.videoUrlPath = videoUrlPath;
    }

    public String getVideoUrlPath() {
        return videoUrlPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        VideoMessage that = (VideoMessage) o;
        return Objects.equal(videoUrlPath, that.videoUrlPath);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), videoUrlPath);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("videoUrlPath", videoUrlPath)
                .toString();
    }
}
