package com.kes.ip.service;

import java.util.List;

import com.kes.ip.dto.EmployeeApiResponseDto;
import com.kes.ip.dto.EmployeeRequestDto;
import com.kes.ip.dto.EmployeeResponseDto;

public interface EmployeeService {

	EmployeeResponseDto saveEmployee(EmployeeRequestDto employeeRequestDto);

	List<EmployeeResponseDto> getAllEmployees();

	EmployeeResponseDto getEmployeeById(Integer employeeId);

	EmployeeResponseDto updateEmployee(Integer employeeId, EmployeeRequestDto employeeRequestDto);

	String deleteEmployee(Integer employeeId);

	EmployeeApiResponseDto getAllEmployees(Integer pageNo, Integer pageSize, String sortBy);
}
