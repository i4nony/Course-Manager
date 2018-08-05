package com.wumii.wechat.service;

import com.wumii.application.logging.Profiling;
import com.wumii.application.util.JsonUtils;
import com.wumii.wechat.dao.MessageDao;
import com.wumii.wechat.entity.AudioMessage;
import com.wumii.wechat.entity.ImageMessage;
import com.wumii.wechat.entity.Message;
import com.wumii.wechat.entity.StringMessage;
import com.wumii.wechat.entity.VideoMessage;
import com.wumii.wechat.entity.WeChatGroup;
import com.wumii.wechat.entity.WeChatUser;
import com.wumii.wechat.domain.MessagesInfo;
import com.wumii.wechat.result.GroupMessageStatistics;
import com.wumii.wechat.result.MassMessageInfo;
import com.wumii.wechat.result.MessageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Profiling
@Service
public class MessageService {
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    @Autowired
    private WeChatGroupService weChatGroupService;
    @Autowired
    private WeChatUserService weChatUserService;
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private SentMassMessageService sentMassMessageService;
    @Autowired
    private WeChatMessageService weChatMessageService;

    public GroupMessageStatistics getGroupMessageStatistics(String wxId) {
        WeChatGroup weChatGroup = weChatGroupService.getOne(wxId);
        long messageCount = messageDao.countByToWxid(wxId);
        Message message = messageDao.findFirstByToWxidOrderByIdDesc(wxId);
        // TODO: 2018/7/2 返回群头像
        return new GroupMessageStatistics(weChatGroup.getName(), "", messageCount,
                getMessageContent(message), message.getCreationTime());
    }

    private String getMessageContent(Message message) {
        if (message instanceof StringMessage) {
            return ((StringMessage) message).getContent();
        } else if(message instanceof AudioMessage) {
            return "【语音消息】";
        } else if(message instanceof VideoMessage) {
            return "【视频消息】";
        } else if(message instanceof ImageMessage) {
            return "【图片消息】";
        }
        return "【未知消息】";
    }

    public List<MessageInfo> getGroupMessages(String wxId, long maxId, int size) {
        List<Message> messages = messageDao.findByToWxidAndIdLessThanOrderByIdDesc(wxId, maxId, new PageRequest(0, size));
        return createResult(messages);
    }

    public List<MessageInfo> getUserMessages(String userWxId, String teacherWxid, long maxId, int size) {
        String[] wxids = new String[] {userWxId, teacherWxid};
        List<Message> messages = messageDao.findByToWxidInAndFromWxidInAndIdLessThanOrderByIdDesc(wxids, wxids, maxId, new PageRequest(0, size));
        return createResult(messages);
    }

    public void sendMessage(MassMessageInfo massMessageInfo) throws InterruptedException, IOException {
        for(MessagesInfo.MessageItem messageItem : massMessageInfo.getMessagesInfo().getMessageItems()) {
            switch (messageItem.getMessageType()) {
                case STRING:
                    weChatMessageService.sendTextMessage(massMessageInfo.getToWxIds(), messageItem.getContent(), null);
                    break;
                case IMAGE:
                    weChatMessageService.sendImageMessage(massMessageInfo.getToWxIds(), messageItem.getContent());
                    break;
                case AUDIO:
                    break;
                case VIDEO:
                    break;
            }
        }
        sentMassMessageService.addMassMessageHistory(massMessageInfo.getFromWxid(),
                JsonUtils.serialize(massMessageInfo), massMessageInfo.getToWxIds().size());
    }

    private List<MessageInfo> createResult(List<Message> messages) {
        List<MessageInfo> messageInfos = new ArrayList<>();
        for(Message message : messages) {
            WeChatUser fromUser = weChatUserService.getOne(message.getFromWxid());
            String toUserName = message.getToWxid().endsWith("@chatroom") ?
                    weChatGroupService.getOne(message.getToWxid()).getName() :
                    weChatUserService.getOne(message.getToWxid()).getNickName();
            MessageInfo messageInfo = new MessageInfo(
                    message instanceof StringMessage ? ((StringMessage) message).getContent() : null,
                    // TODO: 2018/7/2 返回图片、语音、视频消息
                    message instanceof ImageMessage ? "" : null,
                    message instanceof VideoMessage ? "" : null,
                    message instanceof AudioMessage ? "" : null,
                    fromUser.getNickName(),
                    toUserName,
                    message.getCreationTime()
            );
            messageInfos.add(messageInfo);
        }
        return messageInfos;
    }
}
