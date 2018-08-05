package com.wumii.wechat.result;

import com.wumii.wechat.entity.AudioMessage;
import com.wumii.wechat.entity.Message;
import com.wumii.wechat.entity.StringMessage;
import com.wumii.wechat.entity.VideoMessage;

import java.time.Instant;

public class MessageInfo {
    private String textContent;
    private String imageUrl;
    private String videoUrl;
    private String audioUrl;
    private String fromNickName;
    private String toNickName;
    private Instant createTime;

    public MessageInfo(String textContent, String imageUrl, String videoUrl, String audioUrl, String fromNickName, String toNickName, Instant createTime) {
        this.textContent = textContent;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.audioUrl = audioUrl;
        this.fromNickName = fromNickName;
        this.toNickName = toNickName;
        this.createTime = createTime;
    }

    public String getTextContent() {
        return textContent;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public String getFromNickName() {
        return fromNickName;
    }

    public String getToNickName() {
        return toNickName;
    }

    public Instant getCreateTime() {
        return createTime;
    }
}
