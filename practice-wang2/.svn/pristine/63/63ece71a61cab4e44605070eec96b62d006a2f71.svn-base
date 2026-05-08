package com.example.manage2.service.Impl;

import com.example.manage2.entity.ProjectMasterPlan;
import com.example.manage2.mapper.ProjectMasterPlanMapper;
import com.example.manage2.mapper.ProjectMemberRelationMapper;
import com.example.manage2.mapper.ProjectStageTaskMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Mock
    private ProjectMasterPlanMapper projectMasterPlanMapper;
    @Mock
    private ProjectMemberRelationMapper memberRelationMapper;
    @Mock
    private ProjectStageTaskMapper taskMapper;

    @Test
    void testGetProjectList() {
        ProjectMasterPlan plan = new ProjectMasterPlan();
        plan.setProjectName("测试查询项目");

        when(projectMasterPlanMapper.findAllActive(1L, "2026-04-01", "2026-04-30"))
                .thenReturn(Arrays.asList(plan));

        List<ProjectMasterPlan> result = projectService.getProjectList(1L, "2026-04-01", "2026-04-30");

        assertEquals(1, result.size());
        assertEquals("测试查询项目", result.get(0).getProjectName());
    }

    @Test
    void testGenerateProjectCode() {
        List<String> mockExistingCodes = Arrays.asList("2026YF001", "2026YF005", "2026YF脏数据");
        when(projectMasterPlanMapper.findProjectCodesByPrefix("2026YF")).thenReturn(mockExistingCodes);

        String newCode = projectService.generateProjectCode(2026, "研发项目");
        assertEquals("2026YF006", newCode);
    }

    @Test
    void testSaveProject_NewProject() {
        ProjectMasterPlan newProject = new ProjectMasterPlan();
        newProject.setProjectYear(2026);
        newProject.setProjectType("实施项目");
        newProject.setProjectName("新实施项目");
        newProject.setManagerId("10,20");
        newProject.setManagerName("张三,李四");

        when(projectMasterPlanMapper.findProjectCodesByPrefix(anyString())).thenReturn(Collections.emptyList());
        doNothing().when(projectMasterPlanMapper).insertProject(any(ProjectMasterPlan.class));
        doNothing().when(memberRelationMapper).deleteByProjectId(any());
        doNothing().when(memberRelationMapper).insertMember(any(), anyLong(), anyString(), anyInt());

        boolean isNew = projectService.saveProject(newProject);

        assertTrue(isNew);
        assertEquals("2026SS001", newProject.getProjectCode());
        verify(memberRelationMapper, times(2)).insertMember(any(), anyLong(), anyString(), eq(1));
    }

    @Test
    void testSaveProject_WithExistingCode() {
        ProjectMasterPlan importedProject = new ProjectMasterPlan();
        importedProject.setProjectYear(2026);
        importedProject.setProjectType("实施项目");
        importedProject.setProjectCode("2026SS999");
        importedProject.setManagerId("10");
        importedProject.setManagerName("张三");

        doNothing().when(projectMasterPlanMapper).insertProject(any(ProjectMasterPlan.class));
        doNothing().when(memberRelationMapper).deleteByProjectId(any());
        doNothing().when(memberRelationMapper).insertMember(any(), anyLong(), anyString(), anyInt());

        boolean isNew = projectService.saveProject(importedProject);

        assertTrue(isNew);
        assertEquals("2026SS999", importedProject.getProjectCode());
        verify(projectMasterPlanMapper, never()).findProjectCodesByPrefix(anyString());
    }

    @Test
    void testDeleteProject() {
        Long projectId = 999L;

        doNothing().when(projectMasterPlanMapper).deleteProjectById(projectId);
        doNothing().when(memberRelationMapper).deleteByProjectId(projectId);
        doNothing().when(taskMapper).deleteTaskByProjectId(projectId);

        projectService.deleteProject(projectId);

        verify(projectMasterPlanMapper).deleteProjectById(projectId);
        verify(memberRelationMapper).deleteByProjectId(projectId);
        verify(taskMapper).deleteTaskByProjectId(projectId);
    }
}