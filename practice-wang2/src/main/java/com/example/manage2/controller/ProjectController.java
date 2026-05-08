package com.example.manage2.controller;

import com.example.manage2.common.Result;
import com.example.manage2.entity.ProjectMasterPlan;
import com.example.manage2.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 项目大纲控制器 (REST API)
 * 提供前端界面所需的各项接口交互
 */
@RestController
@RequestMapping("/api/project")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * 拉取项目树列表
     *
     * @param userId    当前用户ID (必填，用于权限隔离)
     * @param startDate 查询范围起点 (非必填)
     * @param endDate   查询范围终点 (非必填)
     */
    @GetMapping("/list")
    public Result<List<ProjectMasterPlan>> getList(
            @RequestParam Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<ProjectMasterPlan> list = projectService.getProjectList(userId, startDate, endDate);
        return Result.success(list);
    }

    /**
     * 前端实时生成并预览项目编号
     */
    @GetMapping("/generateCode")
    public Result<String> generateCode(@RequestParam Integer year, @RequestParam String type) {
        String code = projectService.generateProjectCode(year, type);
        return Result.success(code, "生成成功");
    }

    /**
     * 保存/更新项目信息
     * 响应体会返回携带了最新自增 ID 的实体对象，辅助前端页面重渲染
     */
    @PostMapping("/save")
    public Result<?> saveProject(@RequestBody ProjectMasterPlan project) {
        boolean isNew = projectService.saveProject(project);
        return Result.success(project, isNew ? "项目立项成功" : "项目更新成功");
    }

    /**
     * 物理删除项目
     */
    @DeleteMapping("/delete")
    public Result<?> deleteProject(@RequestParam Long id) {
        projectService.deleteProject(id);
        return Result.success(null, "项目已彻底删除");
    }

    /**
     * 导出项目明细为 Excel 文件 (文件流响应)
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response, @RequestParam Long projectId) throws IOException {
        projectService.exportProjects(response, projectId);
    }

    /**
     * 接收前端上传的 Excel 文件进行数据导入
     */
    @PostMapping("/import")
    public Result<?> importExcel(@RequestParam("file") MultipartFile file,
                                 @RequestParam Long userId) {
        try {
            // 接收 Service 返回的新项目 ID，并传给前端
            Long newProjectId = projectService.importProjects(file, userId);
            return Result.success(newProjectId, "导入成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("导入失败，请检查 Excel 模板格式是否被破坏");
        }
    }
}