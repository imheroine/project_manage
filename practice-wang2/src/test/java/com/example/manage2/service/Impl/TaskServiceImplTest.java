package com.example.manage2.service.Impl;

import com.example.manage2.entity.ProjectStageTask;
import com.example.manage2.mapper.ProjectStageTaskMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @InjectMocks
    private TaskServiceImpl taskService;

    @Mock
    private ProjectStageTaskMapper taskMapper; // Mock 掉 Mapper，防止真实写库

    @Test
    void testGetTaskList() {
        ProjectStageTask task = new ProjectStageTask();
        task.setTaskName("联调测试");

        when(taskMapper.findByProjectId(100L)).thenReturn(Arrays.asList(task));

        List<ProjectStageTask> result = taskService.getTaskList(100L);
        assertEquals(1, result.size());
        assertEquals("联调测试", result.get(0).getTaskName());
    }

    @Test
    void testSaveTask_Insert() {
        ProjectStageTask task = new ProjectStageTask(); // id为null

        doNothing().when(taskMapper).insertTask(any());

        boolean isNew = taskService.saveTask(task);

        assertTrue(isNew, "返回true代表新增");
        verify(taskMapper, times(1)).insertTask(task); // 验证调了 insert
        verify(taskMapper, never()).updateTask(any()); // 验证没调 update
    }

    @Test
    void testSaveTask_Update() {
        ProjectStageTask task = new ProjectStageTask();
        task.setId(50L); // id 不为 null

        doNothing().when(taskMapper).updateTask(any());

        boolean isNew = taskService.saveTask(task);

        assertFalse(isNew, "返回false代表更新");
        verify(taskMapper, times(1)).updateTask(task); // 验证调了 update
        verify(taskMapper, never()).insertTask(any()); // 验证没调 insert
    }

    @Test
    void testDeleteTask() {
        doNothing().when(taskMapper).deleteTask(88L);

        taskService.deleteTask(88L);

        verify(taskMapper, times(1)).deleteTask(88L);
    }
}