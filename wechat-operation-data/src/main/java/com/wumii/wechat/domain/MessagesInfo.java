package com.wumii.wechat.domain;

import com.wumii.wechat.entity.base.MessageType;

import java.util.List;

public class MessagesInfo {

    private List<MessageItem> messageItems;

    public List<MessageItem> getMessageItems() {
        return messageItems;
    }

    public void setMessageItems(List<MessageItem> messageItems) {
        this.messageItems = messageItems;
    }

    public static class MessageItem {
        private MessageType messageType;
        private String content;

        public MessageType getMessageType() {
            return messageType;
        }

        public void setMessageType(MessageType messageType) {
            this.messageType = messageType;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
