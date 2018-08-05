package com.wumii.wechat.service;

import com.wumii.application.logging.Profiling;
import com.wumii.wechat.dao.AutoReplayTemplateDao;
import com.wumii.wechat.entity.AutoReplyTemplate;
import com.wumii.wechat.entity.WeChatUser;
import com.wumii.wechat.entity.base.ReplyType;
import com.wumii.wechat.result.AutoReplyTemplateInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Profiling
@Service
public class AutoReplyTemplateService {
    @Autowired
    private AutoReplayTemplateDao autoReplayTemplateDao;
    @Autowired
    private WeChatUserService weChatUserService;


    public List<AutoReplyTemplateInfo> findAllAutoReplyTemplate() {
        List<AutoReplyTemplateInfo> autoReplyTemplateInfos = new ArrayList<>();
        List<AutoReplyTemplate> autoReplyTemplates = autoReplayTemplateDao.findAll();
        for(AutoReplyTemplate autoReplyTemplate : autoReplyTemplates) {
            WeChatUser weChatUser = weChatUserService.getOne(autoReplyTemplate.getWxid());
            AutoReplyTemplateInfo autoReplyTemplateInfo = new AutoReplyTemplateInfo(
                    autoReplyTemplate.getId(), weChatUser.getNickName(),
                    autoReplyTemplate.getReplyType(), autoReplyTemplate.getContentJson());
            autoReplyTemplateInfos.add(autoReplyTemplateInfo);
        }
        return autoReplyTemplateInfos;
    }

    public AutoReplyTemplate addAutoReplyTemplate(String wxid, ReplyType replyType, String contentJson) {
        return autoReplayTemplateDao.save(new AutoReplyTemplate(wxid, replyType, contentJson));
    }

    public void deleteAutoReplyTemplate(long id) {
        autoReplayTemplateDao.delete(id);
    }

}
