package com.wumii.wechat.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@DiscriminatorValue("IMAGE")
public class ImageMessage extends Message {

    private String imageUrlPath;

    private ImageMessage(){
    }

    public ImageMessage(String fromWxid, String toWxid, String imageUrlPath) {
        super(fromWxid, toWxid);
        this.imageUrlPath = imageUrlPath;
    }

    public String getImageUrlPath() {
        return imageUrlPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ImageMessage that = (ImageMessage) o;
        return Objects.equal(imageUrlPath, that.imageUrlPath);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), imageUrlPath);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("imageUrlPath", imageUrlPath)
                .toString();
    }
}
