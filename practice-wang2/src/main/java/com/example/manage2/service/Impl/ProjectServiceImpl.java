package com.example.manage2.service.Impl;

import com.example.manage2.dto.ProjectMainDTO;
import com.example.manage2.dto.ProjectTaskDTO;
import com.example.manage2.entity.ProjectMasterPlan;
import com.example.manage2.mapper.ProjectMasterPlanMapper;
import com.example.manage2.mapper.ProjectMemberRelationMapper;
import com.example.manage2.mapper.ProjectStageTaskMapper;
import com.example.manage2.mapper.SysUserMapper;
import com.example.manage2.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectMasterPlanMapper projectMasterPlanMapper;
    @Autowired
    private ProjectMemberRelationMapper memberRelationMapper;
    @Autowired
    private ProjectStageTaskMapper taskMapper;
    @Autowired
    private SysUserMapper sysUserMapper;

    // =====================================================================
    // 模块 1：基础查询与业务逻辑 (Read & Write)
    // =====================================================================

    @Override
    public List<ProjectMasterPlan> getProjectList(Long userId, String startDate, String endDate) {
        return projectMasterPlanMapper.findAllActive(userId, startDate, endDate);
    }

    @Override
    public String generateProjectCode(Integer year, String type) {
        if (year == null || type == null || type.isEmpty()) {
            return "";
        }

        // 1. 根据中文类型匹配编号前缀缩写
        String typeCode = "QT"; // 默认：其他 (Qi Ta)
        switch (type) {
            case "研发项目": typeCode = "YF"; break;
            case "实施项目": typeCode = "SS"; break;
            case "维护项目": typeCode = "WH"; break;
        }

        String prefix = year + typeCode;
        List<String> existingCodes = projectMasterPlanMapper.findProjectCodesByPrefix(prefix);

        // 2. 遍历现有编号，提取出最大流水号
        int maxSeq = 0;
        for (String code : existingCodes) {
            try {
                String seqStr = code.substring(prefix.length());
                int seq = Integer.parseInt(seqStr);
                if (seq > maxSeq) {
                    maxSeq = seq;
                }
            } catch (Exception e) {
                // 忽略非标准格式的脏数据，防止程序崩溃
            }
        }
        // 3. 返回新编号，流水号补齐 3 位 (如 001)
        return prefix + String.format("%03d", maxSeq + 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveProject(ProjectMasterPlan project) {
        boolean isNew = (project.getId() == null);

        // ---------------- 1. 处理主表数据 ----------------
        if (isNew) {
            // 兜底设置创建人为当前负责人中的第一个，或者系统管理员(1L)
            if (project.getCreatedBy() == null) {
                Long creatorId = (project.getManagerId() != null && !project.getManagerId().isEmpty())
                        ? Long.valueOf(project.getManagerId().split(",")[0])
                        : 1L;
                project.setCreatedBy(creatorId);
            }
            // 若前端未传入编号(例如导入时未填)，则自动生成
            if (project.getProjectCode() == null || project.getProjectCode().trim().isEmpty()) {
                project.setProjectCode(generateProjectCode(project.getProjectYear(), project.getProjectType()));
            }
            projectMasterPlanMapper.insertProject(project);
        } else {
            projectMasterPlanMapper.updateProject(project);
        }

        Long projectId = project.getId();

        // ---------------- 2. 处理关联权限表 ----------------
        // 策略：先全量物理删除旧权限，再重新插入新权限，保证数据最干净
        memberRelationMapper.deleteByProjectId(projectId);

        // 插入负责人 (roleType = 1)
        if (project.getManagerId() != null && !project.getManagerId().isEmpty()) {
            String[] ids = project.getManagerId().split(",");
            String[] names = project.getManagerName().split(",");
            for (int i = 0; i < ids.length; i++) {
                if (i < names.length) {
                    memberRelationMapper.insertMember(projectId, Long.valueOf(ids[i]), names[i], 1);
                }
            }
        }

        // 插入参与人 (roleType = 2)
        if (project.getParticipantIds() != null && !project.getParticipantIds().isEmpty()) {
            String[] ids = project.getParticipantIds().split(",");
            String[] names = project.getParticipants().split(",");
            for (int i = 0; i < ids.length; i++) {
                if (i < names.length) {
                    memberRelationMapper.insertMember(projectId, Long.valueOf(ids[i]), names[i], 2);
                }
            }
        }

        return isNew;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long id) {
        // 注意：必须严格遵守“先删子表，后删父表”的顺序，防止触发数据库外键约束报错！
        taskMapper.deleteTaskByProjectId(id);          // 1. 物理删除关联任务
        memberRelationMapper.deleteByProjectId(id);    // 2. 物理删除关联权限
        projectMasterPlanMapper.deleteProjectById(id); // 3. 物理删除项目主表
    }


    // =====================================================================
    // 模块 2：导入与导出功能 (Import & Export)
    // =====================================================================

    @Override
    public void exportProjects(HttpServletResponse response, Long projectId) throws IOException {
        // 1. 拉取数据
        ProjectMasterPlan project = projectMasterPlanMapper.findById(projectId);
        List<com.example.manage2.entity.ProjectStageTask> tasks = taskMapper.findByProjectId(projectId);

        // 2. 数据转换 (DO -> DTO)
        ProjectMainDTO mainDto = new ProjectMainDTO();
        org.springframework.beans.BeanUtils.copyProperties(project, mainDto);
        List<ProjectMainDTO> mainList = Collections.singletonList(mainDto);

        List<ProjectTaskDTO> taskList = tasks.stream().map(t -> {
            ProjectTaskDTO dto = new ProjectTaskDTO();
            org.springframework.beans.BeanUtils.copyProperties(t, dto);
            if (t.getPlanWorkload() != null) {
                dto.setPlanWorkload(t.getPlanWorkload().doubleValue());
            }
            return dto;
        }).collect(Collectors.toList());

        // 3. 配置 HTTP 响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(project.getProjectName() + "_项目明细", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 4. 多重表格写入同一个 Sheet
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();
        WriteSheet writeSheet = EasyExcel.writerSheet("项目数据").build();

        // 写入主表 (占据上半部分)
        WriteTable table1 = EasyExcel.writerTable(1).head(ProjectMainDTO.class).build();
        excelWriter.write(mainList, writeSheet, table1);

        // 写入任务表 (占据下半部分)
        WriteTable table2 = EasyExcel.writerTable(2).head(ProjectTaskDTO.class).build();
        excelWriter.write(taskList, writeSheet, table2);

        excelWriter.finish();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long importProjects(MultipartFile file, Long userId) throws IOException {
        // 缓存文件流，因为我们需要对同一个文件进行“两次读取”
        byte[] fileBytes = file.getBytes();

        // ================= 步骤一：读取并保存项目主表 =================
        List<ProjectMainDTO> mainList = new java.util.ArrayList<>();
        EasyExcel.read(new ByteArrayInputStream(fileBytes), ProjectMainDTO.class, new com.alibaba.excel.read.listener.ReadListener<ProjectMainDTO>() {
            @Override
            public void invoke(ProjectMainDTO data, com.alibaba.excel.context.AnalysisContext context) {
                // 只收集第 2 行（索引为1）的项目主表数据
                if (context.readRowHolder().getRowIndex() == 1) {
                    mainList.add(data);
                }
            }
            @Override
            public void doAfterAllAnalysed(com.alibaba.excel.context.AnalysisContext context) {}

            @Override
            public void onException(Exception exception, com.alibaba.excel.context.AnalysisContext context) throws Exception {
                // 核心防御：当解析器跑到第 3 行(任务表头)时会抛出类型转换异常。
                // 我们直接将异常吞掉，只有在解析第 1 行主数据报错时才抛出。
                if (context.readRowHolder() != null && context.readRowHolder().getRowIndex() == 1) {
                    throw exception;
                }
            }
        }).sheet().headRowNumber(1).doRead();

        if (mainList.isEmpty()) {
            throw new RuntimeException("未能读取到项目数据，请检查模板格式");
        }
        ProjectMainDTO mainDto = mainList.get(0);

        // 校验项目编号防重复
        if (mainDto.getProjectCode() != null && projectMasterPlanMapper.checkExistByCode(mainDto.getProjectCode()) > 0) {
            throw new RuntimeException("发现已存在的项目，停止导入！重复编号: " + mainDto.getProjectCode());
        }

        // 保存主表
        ProjectMasterPlan project = new ProjectMasterPlan();
        org.springframework.beans.BeanUtils.copyProperties(mainDto, project);
        project.setCreatedBy(userId);

        // 使用翻译机：将 Excel 中的主表姓名翻译为系统用户 ID
        String realManagerIds = convertNamesToIds(mainDto.getManagerName());
        String realParticipantIds = convertNamesToIds(mainDto.getParticipants());

        project.setManagerId(!realManagerIds.isEmpty() ? realManagerIds : String.valueOf(userId));
        project.setParticipantIds(realParticipantIds);

        // 复用通用的保存逻辑
        this.saveProject(project);
        Long newProjectId = project.getId();


        // ================= 步骤二：读取并保存阶段任务 =================
        // headRowNumber(3) 自动跳过前 3 行主表区域，直达任务明细
        List<ProjectTaskDTO> taskList = EasyExcel.read(new ByteArrayInputStream(fileBytes))
                .head(ProjectTaskDTO.class).sheet().headRowNumber(3).doReadSync();

        if (taskList != null && !taskList.isEmpty()) {
            for (ProjectTaskDTO row : taskList) {
                // 跳过空行
                if (row.getTaskName() == null || row.getTaskName().trim().isEmpty()) continue;

                com.example.manage2.entity.ProjectStageTask task = new com.example.manage2.entity.ProjectStageTask();
                task.setProjectId(newProjectId);
                org.springframework.beans.BeanUtils.copyProperties(row, task);

                // 🔴 核心修复：为任务人员也使用翻译机，将中文名转为系统 ID！
                String realTaskResponsibleIds = convertNamesToIds(row.getResponsiblePerson());
                String realTaskParticipantIds = convertNamesToIds(row.getParticipants());

                // 将翻译出来的 ID 赋值给 task 实体
                task.setResponsibleId(realTaskResponsibleIds);
                task.setParticipantIds(realTaskParticipantIds);

                // 类型转换容错
                task.setPlanWorkload(row.getPlanWorkload() != null ? BigDecimal.valueOf(row.getPlanWorkload()) : BigDecimal.ZERO);
                task.setStatusDictCode(task.getStatusDictCode() == null ? "未开始" : task.getStatusDictCode());

                taskMapper.insertTask(task);
            }
        }
        return newProjectId;
    }


    // =====================================================================
    // 模块 3：私有辅助方法 (Private Helpers)
    // =====================================================================

    /**
     * 智能人员名称翻译机
     * 作用：将逗号分隔的姓名（如 "张三,李四"）转换为数据库对应的用户 ID（如 "10,20"）
     * @param names 逗号分隔的姓名字符串
     * @return 逗号分隔的用户ID字符串
     */
    private String convertNamesToIds(String names) {
        if (names == null || names.trim().isEmpty()) {
            return "";
        }
        String[] nameArray = names.split(",");
        java.util.List<String> idList = new java.util.ArrayList<>();

        for (String name : nameArray) {
            String cleanName = name.trim();
            if (cleanName.isEmpty()) continue;

            Long userId = sysUserMapper.findIdByRealName(cleanName);
            if (userId != null) {
                idList.add(String.valueOf(userId));
            } else {
                // 如果数据库查不到这个人，直接阻断整个导入流程，保证数据准确性
                throw new RuntimeException("导入失败：系统中不存在人员【" + cleanName + "】，请检查 Excel 拼写！");
            }
        }
        return String.join(",", idList);
    }
}