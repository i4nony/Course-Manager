package com.wumii.wechat.dao;

import com.wumii.wechat.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendDao extends JpaRepository<Friend, Long> {
    Friend findByWxidAndFriendWxid(String wxid, String friendWxid);
    List<Friend> findByWxid(String wxid);
}
