package com.example.manage2.mapper;

import com.example.manage2.entity.ProjectMasterPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectMasterPlanMapper {
    List<ProjectMasterPlan> findAllActive(@Param("userId") Long userId,
                                          @Param("startDate") String startDate,
                                          @Param("endDate") String endDate);


    void insertProject(ProjectMasterPlan project);

    void updateProject(ProjectMasterPlan project);

    void deleteProjectById(@Param("id") Long id);

    // 新增：根据编号前缀（如 "2026YF"）查询库里所有相关的编号，用于计算最大流水号
    List<String> findProjectCodesByPrefix(@Param("prefix") String prefix);

    // 新增：根据项目编号精确查找是否存在，用于导入校验
    int checkExistByCode(@Param("projectCode") String projectCode);

    // 根据 ID 查询单个项目
    ProjectMasterPlan findById(@Param("id") Long id);


}