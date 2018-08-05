package com.wumii.wechat.service;

import com.wumii.application.logging.Profiling;
import com.wumii.wechat.dao.WechatUserDao;
import com.wumii.wechat.entity.WeChatUser;
import com.wumii.wechat.result.WeChatUserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Profiling
@Service
public class WeChatUserService {
    @Autowired
    private WechatUserDao wechatUserDao;

    public WeChatUser getOne(String wxId) {
        return wechatUserDao.findOne(wxId);
    }

    public List<WeChatUserInfo> getWechatUserInfos(List<String> wxIds) {
        List<WeChatUser> weChatUsers = wechatUserDao.findByWxidInOrderByNickName(wxIds);
        return weChatUsers.stream().
                map(wechatUser -> new WeChatUserInfo(wechatUser.getWxid(), wechatUser.getNickName())).collect(Collectors.toList());
    }

    public List<WeChatUser> getByNickName(String nickName) {
        return wechatUserDao.findByNickName(nickName);
    }
}
