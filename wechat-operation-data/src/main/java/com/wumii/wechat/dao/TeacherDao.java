package com.wumii.wechat.dao;

import com.wumii.wechat.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherDao extends JpaRepository<Teacher, String> {
    List<Teacher> findByDeletedIsFalseOrderByCreationTimeDesc();
    List<Teacher> findBySessionKeyIsNotNullAndDeletedIsFalse();
}
