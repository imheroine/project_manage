package com.example.manage2.service;

import com.example.manage2.entity.ProjectStageTask;

import java.util.List;

/**
 * 项目阶段任务业务逻辑层接口
 * 负责子任务的查询、新增、修改与删除
 */
public interface TaskService {

    /**
     * 根据项目ID获取对应的所有任务列表
     *
     * @param projectId 所属项目的主键ID
     * @return 任务列表集合
     */
    List<ProjectStageTask> getTaskList(Long projectId);

    /**
     * 保存或更新任务信息
     *
     * @param task 任务实体对象
     * @return boolean 返回 true 表示这是一次新增操作，false 表示这是一次更新操作
     */
    boolean saveTask(ProjectStageTask task);

    /**
     * 根据任务的主键ID删除单个指定任务
     *
     * @param id 任务主键ID
     */
    void deleteTask(Long id);
}