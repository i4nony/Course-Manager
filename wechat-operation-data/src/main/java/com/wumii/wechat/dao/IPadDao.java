package com.wumii.wechat.dao;

import com.wumii.wechat.entity.IPad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IPadDao extends JpaRepository<IPad, Long> {
    IPad findFirstByOccupiedIsFalse();
    List<IPad> findByOccupiedIsTrue();
}