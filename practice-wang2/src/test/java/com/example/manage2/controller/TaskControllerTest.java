package com.example.manage2.controller;

import com.example.manage2.entity.ProjectStageTask;
import com.example.manage2.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false) // 关掉 JWT 拦截器
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService; // Mock 掉刚建好的 Service

    @Test
    void testGetList() throws Exception {
        ProjectStageTask task = new ProjectStageTask();
        task.setTaskName("前端页面重构");

        when(taskService.getTaskList(100L)).thenReturn(Arrays.asList(task));

        mockMvc.perform(get("/api/task/list")
                .param("projectId", "100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].taskName").value("前端页面重构"));
    }

    @Test
    void testSaveTask_ReturnInsertMessage() throws Exception {
        ProjectStageTask task = new ProjectStageTask();

        // 模拟 Service 返回 true (代表是新增)
        when(taskService.saveTask(any(ProjectStageTask.class))).thenReturn(true);

        mockMvc.perform(post("/api/task/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("新增成功")); // 验证Controller分支判断
    }

    @Test
    void testSaveTask_ReturnUpdateMessage() throws Exception {
        ProjectStageTask task = new ProjectStageTask();

        // 模拟 Service 返回 false (代表是更新)
        when(taskService.saveTask(any(ProjectStageTask.class))).thenReturn(false);

        mockMvc.perform(post("/api/task/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("更新成功")); // 验证Controller分支判断
    }

    @Test
    void testDelete() throws Exception {
        doNothing().when(taskService).deleteTask(88L);

        mockMvc.perform(delete("/api/task/delete")
                .param("id", "88")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}