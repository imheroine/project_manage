package com.example.manage2.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ProjectTaskDTO {
    @ExcelProperty("阶段名称")
    private String stageDictCode;

    @ExcelProperty("任务名称")
    private String taskName;

    @ExcelProperty("任务状态")
    private String statusDictCode;

    @ExcelProperty("任务负责人")
    private String responsiblePerson;

    @ExcelProperty("任务参与人")
    private String participants;

    @DateTimeFormat("yyyy-MM-dd")
    @ExcelProperty("计划开始时间")
    private Date planStartDate;

    @DateTimeFormat("yyyy-MM-dd")
    @ExcelProperty("计划结束时间")
    private Date planEndDate;

    @DateTimeFormat("yyyy-MM-dd")
    @ExcelProperty("实际开始时间")
    private Date actualStartDate;

    @DateTimeFormat("yyyy-MM-dd")
    @ExcelProperty("实际结束时间")
    private Date actualEndDate;

    @ExcelProperty("计划工作量")
    private Double planWorkload;

    @ExcelProperty("实际工作量")
    private Double actualWorkload;

    @ExcelProperty("存在问题")
    private String issues;

    @ExcelProperty("整改措施")
    private String rectificationMeasures;

    @ExcelProperty("备注")
    private String remarks;

}