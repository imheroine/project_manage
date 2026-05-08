package com.example.manage2.mapper;

import com.example.manage2.entity.ProjectStageTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目阶段任务数据库映射接口
 */
@Mapper
public interface ProjectStageTaskMapper {

    /**
     * 查询指定项目下的所有任务
     *
     * @param projectId 项目主键ID
     */
    List<ProjectStageTask> findByProjectId(@Param("projectId") Long projectId);

    /**
     * 插入新任务，并将数据库生成的自增 ID 回填到 task 对象中
     */
    void insertTask(ProjectStageTask task);

    /**
     * 全量更新任务信息
     */
    void updateTask(ProjectStageTask task);

    /**
     * 根据任务主键ID，物理删除单个任务
     */
    void deleteTask(@Param("id") Long id);

    /**
     * 级联删除专供：根据项目主键ID，物理删除该项目下的【所有任务】
     */
    void deleteTaskByProjectId(Long id);
}