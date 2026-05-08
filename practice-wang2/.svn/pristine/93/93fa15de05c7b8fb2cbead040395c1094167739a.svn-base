package com.example.manage2.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectMemberRelationMapper {

    // 清空某项目的所有旧成员关系
    @Delete("DELETE FROM \"project_member_relation\" WHERE \"project_id\" = #{projectId}")
    void deleteByProjectId(Long projectId);

    // 插入新成员及角色 (member_role: 1-负责人/管理员, 2-普通成员)
    @Insert("INSERT INTO \"project_member_relation\" (\"project_id\", \"user_id\", \"user_name\", \"member_role\") " +
            "VALUES (#{projectId}, #{userId}, #{userName}, #{memberRole})")
    void insertMember(@Param("projectId") Long projectId,
                      @Param("userId") Long userId,
                      @Param("userName") String userName,
                      @Param("memberRole") Integer memberRole);
}