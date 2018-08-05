package com.wumii.wechat.service;

import com.google.common.base.Preconditions;
import com.google.protobuf.ByteString;
import com.wumii.application.util.JsonUtils;
import com.wumii.wechat.dao.UserGroupDao;
import com.wumii.wechat.dao.WechatGroupDao;
import com.wumii.wechat.domain.CreateChatRoomMessage;
import com.wumii.wechat.dto.Dto;
import com.wumii.wechat.entity.UserGroup;
import com.wumii.wechat.entity.WeChatGroup;
import com.wumii.wechat.exception.WeChatOperationException;
import com.wumii.wechat.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.List;

@Service
public class WeChatChatRoomService {

    private static final Logger logger = LoggerFactory.getLogger(WeChatChatRoomService.class);

    private final WechatGroupDao wechatGroupDao;
    private final UserGroupDao userGroupDao;
    private final WeChatLoginService weChatLoginService;
    private final TransactionTemplate transactionTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public WeChatChatRoomService(WechatGroupDao wechatGroupDao,
                                 UserGroupDao userGroupDao,
                                 WeChatLoginService weChatLoginService,
                                 TransactionTemplate transactionTemplate,
                                 RabbitTemplate rabbitTemplate) {
        this.wechatGroupDao = wechatGroupDao;
        this.userGroupDao = userGroupDao;
        this.weChatLoginService = weChatLoginService;
        this.transactionTemplate = transactionTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    void createChatRoom(WeChatClient client, List<String> wxids) throws IOException {
        Preconditions.checkState(client.inEventLoop());

        Dto.CreateChatRoomRequest request = buildCreateChatRoomRequest(client, wxids);
        Dto.CreateChatRoomResponse response = Dto.CreateChatRoomResponse.parseFrom(
                client.sendRequest("/cgi-bin/micromsg-bin/createchatroom", request.toByteArray(),
                        119, false));
        if (response.getResult().getCode() != 0) {
            throw new WeChatOperationException("create chat room fail");
        }
        // TODO: 7/4/18 群名称 图片服务 班级号
        transactionTemplate.execute(status -> {
            wechatGroupDao.save(new WeChatGroup(response.getWxid().getId(), "TODO",
                    client.getWxid(), "", false));
            userGroupDao.save(new UserGroup(client.getWxid(), response.getWxid().getId()));
            for (Dto.MemberInfo memberInfo : response.getMemberList()) {
                userGroupDao.save(new UserGroup(memberInfo.getWxid().getId(), response.getWxid().getId()));
            }
            return null;
        });
        rabbitTemplate.convertAndSend(Constant.EXCHANGE, "",
                JsonUtils.serialize(new CreateChatRoomMessage(client.getWxid(), response.getWxid().getId())));
    }

    private Dto.CreateChatRoomRequest buildCreateChatRoomRequest(WeChatClient client, List<String> wxids) {
        Dto.CreateChatRoomRequest request = Dto.CreateChatRoomRequest.newBuilder()
                .setBaseRequest(Dto.BaseRequest.newBuilder()
                        .setAesKey(ByteString.copyFrom(client.getSessionKey()))
                        .setDeviceId(ByteString.copyFrom(client.getIpad().getDeviceId()))
                        .setUin(client.getUin())
                        .setOsVersion(Constant.OS_VERSION)
                        .setClientVersion(Constant.CLIENT_VERSION)
                        .setScene(0)
                        .build())
                .setUnknown2(Dto.CreateChatRoomRequest.Unknown2.newBuilder().build())
                .setCount(wxids.size())
                .setUnknown5(0)
                .build();
        for (String wxid : wxids) {
            request = request.toBuilder().addMember(Dto.MemberInfo.newBuilder()
                    .setWxid(Dto.Wxid.newBuilder().setId(wxid).build())
                    .build()).build();
        }
        return request;
    }

    public boolean createChatRoom(List<String> wxids) throws InterruptedException {
        WeChatClient client = weChatLoginService.getLoginClient();
        return client.submit(() -> createChatRoom(client, wxids)).isSuccess();
    }

    public boolean addMember(String groupWxid, List<String> wxids) throws InterruptedException {
        WeChatClient client = weChatLoginService.getLoginClient();
        return client.submit(() -> addMember(client, groupWxid, wxids)).isSuccess();
    }

    void addMember(WeChatClient client, String groupWxid, List<String> wxids) throws IOException {
        Preconditions.checkState(client.inEventLoop());

        Dto.AddChatRoomMemberRequest request = buildAddChatRoomMemberRequest(client, groupWxid, wxids);
        Dto.AddChatRoomMemberResponse response = Dto.AddChatRoomMemberResponse.parseFrom(
                client.sendRequest("/cgi-bin/micromsg-bin/addchatroommember", request.toByteArray(),
                        120, false));
        if (response.getResult().getCode() != 0) {
            throw new WeChatOperationException("add chat room member fail");
        }
        for (String wxid : wxids) {
            userGroupDao.save(new UserGroup(wxid, groupWxid));
        }
    }

    private Dto.AddChatRoomMemberRequest buildAddChatRoomMemberRequest(WeChatClient client, String groupWxid, List<String> wxids) {
        Dto.AddChatRoomMemberRequest request = Dto.AddChatRoomMemberRequest.newBuilder()
                .setBaseRequest(Dto.BaseRequest.newBuilder()
                        .setAesKey(ByteString.copyFrom(client.getSessionKey()))
                        .setDeviceId(ByteString.copyFrom(client.getIpad().getDeviceId()))
                        .setUin(client.getUin())
                        .setOsVersion(Constant.OS_VERSION)
                        .setClientVersion(Constant.CLIENT_VERSION)
                        .setScene(0)
                        .build())
                .setCount(wxids.size())
                .setChatRoomInfo(Dto.AddChatRoomMemberRequest.ChatRoomInfo.newBuilder()
                        .setWxid(groupWxid)
                        .build())
                .setUnknown5(0)
                .build();
        for (String wxid : wxids) {
            request = request.toBuilder().addMember(Dto.MemberInfo.newBuilder()
                    .setWxid(Dto.Wxid.newBuilder().setId(wxid).build())
                    .build()).build();
        }
        return request;
    }

}
