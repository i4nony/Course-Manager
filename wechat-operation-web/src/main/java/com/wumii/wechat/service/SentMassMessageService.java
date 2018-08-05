package com.wumii.wechat.service;

import com.wumii.wechat.dao.SentMassMessageDao;
import com.wumii.wechat.entity.SentMassMessage;
import com.wumii.wechat.entity.WeChatUser;
import com.wumii.wechat.result.SentMassMessageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SentMassMessageService {
    @Autowired
    private SentMassMessageDao sentMassMessageDao;
    @Autowired
    private WeChatUserService weChatUserService;

    public List<SentMassMessageInfo> getAllMassMessageHistory() {
        List<SentMassMessageInfo> sentMassMessageInfos = new ArrayList<>();
        List<SentMassMessage> sentMassMessages = sentMassMessageDao.findAllByOrderByIdDesc();
        for(SentMassMessage sentMassMessage : sentMassMessages) {
            WeChatUser weChatUser = weChatUserService.getOne(sentMassMessage.getWxid());
            SentMassMessageInfo sentMassMessageInfo = new SentMassMessageInfo(
                    sentMassMessage.getId(),
                    weChatUser.getNickName(),
                    sentMassMessage.getContentJson(),
                    sentMassMessage.getPeopleNumber(),
                    sentMassMessage.getCreationTime()
            );
            sentMassMessageInfos.add(sentMassMessageInfo);
        }
        return sentMassMessageInfos;
    }

    public void addMassMessageHistory(String wxid, String contentJson, int peopleNumber) {
        SentMassMessage sentMassMessage = new SentMassMessage(wxid, contentJson, peopleNumber);
        sentMassMessageDao.save(sentMassMessage);
    }
}
