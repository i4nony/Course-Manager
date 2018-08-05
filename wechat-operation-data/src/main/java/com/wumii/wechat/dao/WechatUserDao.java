package com.wumii.wechat.dao;

import com.wumii.wechat.entity.WeChatUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WechatUserDao extends JpaRepository<WeChatUser, String> {

    @Query(value = "SELECT 1 FROM pg_advisory_xact_lock(?1)", nativeQuery = true)
    void lock(String key);

    List<WeChatUser> findByWxidInOrderByNickName(List<String> wxids);

    List<WeChatUser> findByNickName(String nickName);
}
