package com.wumii.wechat.dao;

import com.wumii.wechat.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageDao extends JpaRepository<Message, Long> {
    long countByToWxid(String wxId);
    Message findFirstByToWxidOrderByIdDesc(String wxId);
    List<Message> findByToWxidAndIdLessThanOrderByIdDesc(String wxId, long id, Pageable pageable);
    List<Message> findByToWxidInAndFromWxidInAndIdLessThanOrderByIdDesc(String[] wxids1, String[] wxids2, long maxId, Pageable pageable);
}
