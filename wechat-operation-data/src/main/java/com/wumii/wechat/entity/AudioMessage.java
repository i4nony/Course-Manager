package com.wumii.wechat.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("AUDIO")
public class AudioMessage extends Message {

    private String audioUrlPath;

    private AudioMessage() {}

    public AudioMessage(String fromWxid, String toWxid, String audioUrlPath) {
        super(fromWxid, toWxid);
        this.audioUrlPath = audioUrlPath;
    }


    public String getAudioUrlPath() {
        return audioUrlPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AudioMessage that = (AudioMessage) o;
        return Objects.equal(audioUrlPath, that.audioUrlPath);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), audioUrlPath);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("audioUrlPath", audioUrlPath)
                .toString();
    }
}
