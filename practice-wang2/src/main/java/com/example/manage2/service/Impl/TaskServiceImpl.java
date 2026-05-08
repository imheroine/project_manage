package com.example.manage2.service.Impl;

import com.example.manage2.entity.ProjectStageTask;
import com.example.manage2.mapper.ProjectStageTaskMapper;
import com.example.manage2.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private ProjectStageTaskMapper taskMapper;

    // ================== 查询逻辑 ==================

    @Override
    public List<ProjectStageTask> getTaskList(Long projectId) {
        return taskMapper.findByProjectId(projectId);
    }

    // ================== 写入与删除逻辑 ==================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveTask(ProjectStageTask task) {
        // 1. 防御性逻辑：防止前端因意外传回无效的负数或 0 作为 ID
        if (task.getId() != null && task.getId() <= 0) {
            task.setId(null);
        }

        // 2. 根据 ID 是否为空，判断是新增还是更新
        if (task.getId() == null) {
            taskMapper.insertTask(task);
            return true;  // 代表新增成功
        } else {
            taskMapper.updateTask(task);
            return false; // 代表更新成功
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long id) {
        // 物理删除指定任务
        taskMapper.deleteTask(id);
    }
}