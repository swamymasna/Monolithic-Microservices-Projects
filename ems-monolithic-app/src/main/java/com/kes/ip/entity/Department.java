package com.kes.ip.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "DEPARTMENT_TBL")
public class Department {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer departmentId;

	@Column(name = "DEPT_NAME")
	private String departmentName;

	@Column(name = "DEPT_CODE")
	private String departmentCode;

	@Column(name = "DEPT_DESC")
	private String departmentDescription;

	@JsonIgnore
	@OneToMany(mappedBy = "department")
	private List<Employee> employees;
}
