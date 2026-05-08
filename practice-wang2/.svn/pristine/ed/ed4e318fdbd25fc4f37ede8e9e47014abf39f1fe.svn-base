package com.example.manage2.controller;

import com.example.manage2.entity.ProjectMasterPlan;
import com.example.manage2.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

// 静态导入 Mockito 和 Spring MVC 的测试组件
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @Test
    void testGetList() throws Exception {
        ProjectMasterPlan plan = new ProjectMasterPlan();
        plan.setProjectName("A项目");

        when(projectService.getProjectList(eq(1L), eq("2026-04-01"), eq("2026-04-30")))
                .thenReturn(Arrays.asList(plan));

        mockMvc.perform(get("/api/project/list")
                .param("userId", "1")
                .param("startDate", "2026-04-01")
                .param("endDate", "2026-04-30")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].projectName").value("A项目"));
    }

    @Test
    void testGenerateCode() throws Exception {
        when(projectService.generateProjectCode(2026, "研发项目")).thenReturn("2026YF001");

        mockMvc.perform(get("/api/project/generateCode")
                .param("year", "2026")
                .param("type", "研发项目"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("2026YF001"));
    }

    @Test
    void testSaveProject() throws Exception {
        ProjectMasterPlan plan = new ProjectMasterPlan();
        plan.setProjectName("测试项目");

        when(projectService.saveProject(any(ProjectMasterPlan.class))).thenReturn(true);

        mockMvc.perform(post("/api/project/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(plan)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("项目立项成功"));
    }

    // 🔴 核心修改位置：测试精美版的分表导出接口
    @Test
    void testExportProjects() throws Exception {
        // 现在的 Service.exportProjects 只需要两个参数：response 和 projectId
        doNothing().when(projectService).exportProjects(any(), anyLong());

        // 发起 GET 请求，参数改为 projectId
        mockMvc.perform(get("/api/project/export")
                .param("projectId", "100"))
                .andExpect(status().isOk());
    }

    @Test
    void testImportExcel() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "content".getBytes()
        );

        when(projectService.importProjects(any(),anyLong())).thenReturn(1L);

        mockMvc.perform(multipart("/api/project/import")
                .file(file)
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("导入成功"));
    }

    @Test
    void testDeleteProject() throws Exception {
        mockMvc.perform(delete("/api/project/delete")
                .param("id", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("项目已废弃"));
    }
}