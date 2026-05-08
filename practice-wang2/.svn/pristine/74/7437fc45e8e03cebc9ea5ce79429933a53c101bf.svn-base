package com.example.manage2.controller;

import com.example.manage2.common.Result;
import com.example.manage2.entity.ProjectStageTask;
import com.example.manage2.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目阶段任务控制器 (REST API)
 * 提供前端界面操作任务所需的增删改查接口
 */
@RestController
@RequestMapping("/api/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * 拉取指定项目下的所有任务列表
     */
    @GetMapping("/list")
    public Result<List<ProjectStageTask>> getList(@RequestParam Long projectId) {
        List<ProjectStageTask> list = taskService.getTaskList(projectId);
        return Result.success(list);
    }

    /**
     * 保存或更新任务信息
     */
    @PostMapping("/save")
    public Result<?> saveTask(@RequestBody ProjectStageTask task) {
        // 调用 Service 层逻辑，利用返回值判断给前端反馈什么提示语
        boolean isNew = taskService.saveTask(task);
        return Result.success(null, isNew ? "新增成功" : "更新成功");
    }

    /**
     * 删除指定任务
     */
    @DeleteMapping("/delete")
    public Result<?> delete(@RequestParam Long id) {
        taskService.deleteTask(id);
        return Result.success("删除成功");
    }
}
