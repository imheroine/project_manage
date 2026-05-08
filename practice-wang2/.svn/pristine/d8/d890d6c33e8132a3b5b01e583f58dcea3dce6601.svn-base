package com.example.manage2.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ProjectMainDTO {
    @ExcelProperty("所属年度")
    private Integer projectYear;

    @ExcelProperty("项目类型")
    private String projectType;

    @ExcelProperty("项目编号")
    private String projectCode;

    @ExcelProperty("项目名称")
    private String projectName;

    @ExcelProperty("项目地点")
    private String location;

    @ExcelProperty("项目负责人")
    private String managerName;

    @ExcelProperty("项目参与人")
    private String participants;

    @DateTimeFormat("yyyy-MM-dd")
    @ExcelProperty("项目计划开始")
    private Date planStartDate;

    @DateTimeFormat("yyyy-MM-dd")
    @ExcelProperty("项目计划结束")
    private Date planEndDate;

    @ExcelProperty("计划天数")
    private Integer planDays;

    @DateTimeFormat("yyyy-MM-dd")
    @ExcelProperty("项目实际开始")
    private Date actualStartDate;

    @DateTimeFormat("yyyy-MM-dd")
    @ExcelProperty("项目实际结束")
    private Date actualEndDate;

    @ExcelProperty("实际天数")
    private Integer actualDays;
}