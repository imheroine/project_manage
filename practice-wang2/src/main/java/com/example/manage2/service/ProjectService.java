package com.example.manage2.service;

import com.example.manage2.entity.ProjectMasterPlan;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 项目总计划业务逻辑层接口
 * 负责处理项目的增删改查、编号生成及导入导出功能
 */
public interface ProjectService {

    // ================== 查询类接口 ==================

    /**
     * 获取用户有权限查看的项目列表（支持按时间段筛选）
     *
     * @param userId    当前登录用户ID
     * @param startDate 计划开始日期 (可选)
     * @param endDate   计划结束日期 (可选)
     * @return 项目列表
     */
    List<ProjectMasterPlan> getProjectList(Long userId, String startDate, String endDate);

    /**
     * 自动生成下一个项目编号 (例如：2026YF001)
     *
     * @param year 年度 (如: 2026)
     * @param type 项目类型 (如: 研发项目)
     * @return 格式化后的项目编号
     */
    String generateProjectCode(Integer year, String type);


    // ================== 操作类接口 ==================

    /**
     * 保存或更新项目，并同步刷新人员权限关联表
     *
     * @param project 项目实体对象
     * @return boolean 返回 true 表示新增项目，false 表示更新项目
     */
    boolean saveProject(ProjectMasterPlan project);

    /**
     * 物理删除项目 (级联删除任务与权限)
     *
     * @param id 项目主键ID
     */
    void deleteProject(Long id);


    // ================== 导入导出接口 ==================

    /**
     * 导出项目明细至 Excel (主从表复合结构：上半部分为项目，下半部分为任务)
     *
     * @param response  HttpServletResponse，用于写入文件流
     * @param projectId 需要导出的项目ID
     * @throws IOException IO异常
     */
    void exportProjects(HttpServletResponse response, Long projectId) throws IOException;

    /**
     * 从 Excel 导入项目及其关联任务
     *
     * @param file   前端上传的 Excel 文件
     * @param userId 当前操作人ID（作为默认创建人和负责人）
     * @throws IOException IO异常
     */
    Long importProjects(MultipartFile file, Long userId) throws IOException;
}
