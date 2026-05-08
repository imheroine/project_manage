package com.example.manage2.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ProjectStageTask {
    private Long id;
    private Long projectId;               // 关联的项目id
    private String stageDictCode;         // 阶段名称
    private String taskName;              // 任务名称
    private Date planStartDate;           // 计划开始日期
    private Date planEndDate;             // 计划结束日期
    private BigDecimal planWorkload;      // 计划工作量
    private Date actualStartDate;         // 实际开始日期
    private Date actualEndDate;           // 实际结束日期
    private BigDecimal actualWorkload;    // 实际工作量
    private String responsiblePerson;     // 任务负责人姓名
    private String responsibleId;         // 任务负责人id
    private String participants;          // 参与人员
    private String participantIds;        // 参与人员id
    private String statusDictCode;        // 任务状态
    private String issues;                // 存在问题
    private String rectificationMeasures; // 整改措施
    private String remarks;               // 备注
    private String attachmentUrls;        // 附件信息
    private Date updateTime;              // 最后更新时间
}