package com.kes.ip.dto;

import com.kes.ip.entity.Department;

import lombok.Data;

@Data
public class EmployeeResponseDto {

	private Integer id;

	private String employeeName;

	private Double employeeSalary;

	private String employeeAddress;

	private Department department;
}
