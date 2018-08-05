package com.wumii.wechat.service;

import com.wumii.application.logging.Profiling;
import com.wumii.wechat.dao.FriendDao;
import com.wumii.wechat.entity.Friend;
import com.wumii.wechat.entity.WeChatUser;
import com.wumii.wechat.result.WeChatUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Profiling
@Service
public class FriendService {
    @Autowired
    private WeChatUserService weChatUserService;
    @Autowired
    private FriendDao friendDao;
    @Autowired
    private WeChatFriendService weChatFriendService;
    @Autowired
    private WeChatLoginService weChatLoginService;
    @Autowired
    private ImageHelper imageHelper;
    private final static Logger logger = LoggerFactory.getLogger(FriendService.class);

    private static final int MIN_DIFFERENCE = 5;

    public List<WeChatUserInfo> getFriendInfos(String wxId) {
        List<Friend> friends = friendDao.findByWxid(wxId);
        return weChatUserService.getWechatUserInfos(friends.stream().map(Friend::getFriendWxid)
                .collect(Collectors.toList()));
    }

    public void changeRemark(String teacherWxid, String friendWxid, String name) throws InterruptedException {
        weChatLoginService.setCurrentWxid(teacherWxid);
        weChatFriendService.changeRemarkName(friendWxid, name);
    }

    public Optional<String> getWxid(String nickName, String avatarUrl) {
        List<WeChatUser> weChatUsers = weChatUserService.getByNickName(nickName);
        if (weChatUsers.size() <= 0) {
            return Optional.empty();
        }
        int minDifference = Integer.MAX_VALUE;
        WeChatUser resultUse = null;
        for(WeChatUser weChatUser : weChatUsers) {
            // TODO: 2018/7/18 url需要加上域名
            try {
                int difference = imageHelper.compareHashCode(avatarUrl, weChatUser.getAvatarUrlPath());
                if (minDifference > difference && difference <= MIN_DIFFERENCE) {
                    minDifference = difference;
                    resultUse = weChatUser;
                }
            } catch (IOException e) {
                logger.error("compareHashCode fail:" + avatarUrl + "," + weChatUser.getAvatarUrlPath() + e);
            }
        }
        if (resultUse != null) {
            return Optional.of(resultUse.getWxid());
        }
        return Optional.empty();
    }
}
