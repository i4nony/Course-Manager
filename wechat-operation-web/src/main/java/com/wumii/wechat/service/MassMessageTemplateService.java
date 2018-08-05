package com.wumii.wechat.service;

import com.wumii.application.logging.Profiling;
import com.wumii.wechat.dao.MassMessageTemplateDao;
import com.wumii.wechat.entity.MassMessageTemplate;
import com.wumii.wechat.result.MassMessageTemplateInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Profiling
@Service
public class MassMessageTemplateService {
    @Autowired
    private MassMessageTemplateDao massMessageTemplateDao;
    @Autowired
    private WeChatUserService weChatUserService;

    public List<MassMessageTemplateInfo> findAllMassMessageTemplate() {
        List<MassMessageTemplateInfo> massMessageTemplateInfos = new ArrayList<>();
        List<MassMessageTemplate> massMessageTemplates = massMessageTemplateDao.findAll();
        for(MassMessageTemplate massMessageTemplate : massMessageTemplates) {
            MassMessageTemplateInfo massMessageTemplateInfo = new MassMessageTemplateInfo(
                    massMessageTemplate.getId(),
                    massMessageTemplate.getContentJson(),
                    massMessageTemplate.getCreationTime());
            massMessageTemplateInfos.add(massMessageTemplateInfo);
        }
        return massMessageTemplateInfos;
    }

    public MassMessageTemplate addMassMessageTemplate(String contentJson) {
        return massMessageTemplateDao.save(new MassMessageTemplate(contentJson));
    }

    public void deleteMassMessageTemplate(long id) {
        massMessageTemplateDao.delete(id);
    }
}
