package com.kes.ip.service.impl;

import static com.kes.ip.utils.AppConstants.DELETE_EMPLOYEE_EXCEPTION;
import static com.kes.ip.utils.AppConstants.DEPT_CODE;
import static com.kes.ip.utils.AppConstants.DEPT_DESC;
import static com.kes.ip.utils.AppConstants.DEPT_ID;
import static com.kes.ip.utils.AppConstants.DEPT_NAME;
import static com.kes.ip.utils.AppConstants.EADDR;
import static com.kes.ip.utils.AppConstants.EID;
import static com.kes.ip.utils.AppConstants.EMAIL_SUBJECT;
import static com.kes.ip.utils.AppConstants.EMPLOYEE_DELETION_FAILED;
import static com.kes.ip.utils.AppConstants.EMPLOYEE_DELETION_SUCCEEDED;
import static com.kes.ip.utils.AppConstants.EMPLOYEE_EMAIL_BODY_TEMPLATE;
import static com.kes.ip.utils.AppConstants.EMPLOYEE_NOT_FOUND_EXCEPTION;
import static com.kes.ip.utils.AppConstants.ENAME;
import static com.kes.ip.utils.AppConstants.ESAL;
import static com.kes.ip.utils.AppConstants.FETCH_ALL_EMPLOYEES_EXCEPTION;
import static com.kes.ip.utils.AppConstants.SAVE_EMPLOYEE_EXCEPTION;
import static com.kes.ip.utils.AppConstants.TO_ADDR;
import static com.kes.ip.utils.AppConstants.UPDATE_EMPLOYEE_EXCEPTION;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.kes.ip.dto.EmployeeApiResponseDto;
import com.kes.ip.dto.EmployeeRequestDto;
import com.kes.ip.dto.EmployeeResponseDto;
import com.kes.ip.entity.Department;
import com.kes.ip.entity.Employee;
import com.kes.ip.exception.EmployeeServiceBusinessException;
import com.kes.ip.exception.ResourceNotFoundException;
import com.kes.ip.props.AppProperties;
import com.kes.ip.repository.DepartmentRepository;
import com.kes.ip.repository.EmployeeRepository;
import com.kes.ip.service.EmployeeService;
import com.kes.ip.utils.EmailUtil;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

	private EmployeeRepository employeeRepository;

	private ModelMapper modelMapper;

	private EmailUtil emailUtil;

	private AppProperties appProperties;

	private DepartmentRepository departmentRepository;

	@Override
	public EmployeeResponseDto saveEmployee(EmployeeRequestDto employeeRequestDto) {

		Employee employee = null;

		EmployeeResponseDto employeeResponseDto = null;

		try {

			employee = modelMapper.map(employeeRequestDto, Employee.class);

			Department department = departmentRepository.findById(employeeRequestDto.getDepartmentId()).get();

			employee.setDepartment(department);

			employee = employeeRepository.save(employee);

			employeeResponseDto = modelMapper.map(employee, EmployeeResponseDto.class);

			String toAddr = TO_ADDR;

			String subject = appProperties.getMessages().get(EMAIL_SUBJECT);

			String body = readEmailBody(EMPLOYEE_EMAIL_BODY_TEMPLATE, employeeResponseDto);

			// emailUtil.sendEmail(toAddr, subject, body);

		} catch (Exception e) {

			throw new EmployeeServiceBusinessException(
					String.format(appProperties.getMessages().get(SAVE_EMPLOYEE_EXCEPTION), e));
		}
		return employeeResponseDto;
	}

	private String readEmailBody(String fileName, EmployeeResponseDto employeeResponse) {

		String mailBody = null;

		try {

			Path path = Paths.get(fileName);

			mailBody = Files.readString(path);

			mailBody = mailBody.replace(EID, employeeResponse.getId().toString());
			mailBody = mailBody.replace(ENAME, employeeResponse.getEmployeeName());
			mailBody = mailBody.replace(ESAL, employeeResponse.getEmployeeSalary().toString());
			mailBody = mailBody.replace(EADDR, employeeResponse.getEmployeeAddress());

			mailBody = mailBody.replace(DEPT_ID, employeeResponse.getDepartment().getDepartmentId().toString());
			mailBody = mailBody.replace(DEPT_NAME, employeeResponse.getDepartment().getDepartmentName());
			mailBody = mailBody.replace(DEPT_CODE, employeeResponse.getDepartment().getDepartmentCode());
			mailBody = mailBody.replace(DEPT_DESC, employeeResponse.getDepartment().getDepartmentDescription());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return mailBody;
	}

	@Override
	public List<EmployeeResponseDto> getAllEmployees() {

		List<EmployeeResponseDto> employeeResponseDtos = null;

		try {

			List<Employee> employees = employeeRepository.findAll();

			if (!employees.isEmpty()) {
				employeeResponseDtos = employees.stream()
						.map(employee -> modelMapper.map(employee, EmployeeResponseDto.class))
						.collect(Collectors.toList());

			} else {
				employeeResponseDtos = Collections.emptyList();
			}

		} catch (Exception e) {
			throw new EmployeeServiceBusinessException(
					String.format(appProperties.getMessages().get(FETCH_ALL_EMPLOYEES_EXCEPTION), e));
		}

		return employeeResponseDtos;
	}

	@Override
	public EmployeeResponseDto getEmployeeById(Integer employeeId) {

		Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new ResourceNotFoundException(
				String.format(appProperties.getMessages().get(EMPLOYEE_NOT_FOUND_EXCEPTION), employeeId)));

		return modelMapper.map(employee, EmployeeResponseDto.class);
	}

	@Override
	public EmployeeResponseDto updateEmployee(Integer employeeId, EmployeeRequestDto employeeRequestDto) {

		EmployeeResponseDto employeeResponseDto = null;

		Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new ResourceNotFoundException(
				String.format(appProperties.getMessages().get(EMPLOYEE_NOT_FOUND_EXCEPTION), employeeId)));

		try {

			employee.setEmployeeName(employeeRequestDto.getEmployeeName());
			employee.setEmployeeSalary(employeeRequestDto.getEmployeeSalary());
			employee.setEmployeeAddress(employeeRequestDto.getEmployeeAddress());

			employee = employeeRepository.save(employee);

			if (employee.getId() != null) {
				employeeResponseDto = modelMapper.map(employee, EmployeeResponseDto.class);
			} else {
				employeeResponseDto = null;
			}

		} catch (Exception e) {
			throw new EmployeeServiceBusinessException(
					String.format(appProperties.getMessages().get(UPDATE_EMPLOYEE_EXCEPTION), e));
		}

		return employeeResponseDto;
	}

	@Override
	public String deleteEmployee(Integer employeeId) {

		String message = null;

		Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new ResourceNotFoundException(
				String.format(appProperties.getMessages().get(EMPLOYEE_NOT_FOUND_EXCEPTION), employeeId)));

		try {

			if (employee.getId() != null && employeeId != null) {
				employeeRepository.deleteById(employeeId);
				message = appProperties.getMessages().get(EMPLOYEE_DELETION_SUCCEEDED);
			} else {
				message = appProperties.getMessages().get(EMPLOYEE_DELETION_FAILED);
			}

		} catch (Exception e) {
			throw new EmployeeServiceBusinessException(
					String.format(appProperties.getMessages().get(DELETE_EMPLOYEE_EXCEPTION), e));

		}

		return message;
	}

	@Override
	public EmployeeApiResponseDto getAllEmployees(Integer pageNo, Integer pageSize, String sortBy) {

		List<Employee> employees = null;

		List<EmployeeResponseDto> empsList = null;

		EmployeeApiResponseDto employeeApiResponseDto = null;

		try {
			Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));

			Page<Employee> page = employeeRepository.findAll(pageable);

			employees = page.getContent();

			if (!employees.isEmpty()) {
				empsList = employees.stream().map(emp -> modelMapper.map(emp, EmployeeResponseDto.class))
						.collect(Collectors.toList());
			} else {
				empsList = Collections.emptyList();
			}

			employeeApiResponseDto = EmployeeApiResponseDto.builder().employeesList(empsList).pageNo(pageNo)
					.pageSize(pageSize).sortBy(sortBy).totalElements(page.getTotalElements())
					.totalPages(page.getTotalPages()).first(page.isFirst()).last(page.isLast()).build();

		} catch (Exception e) {
			throw new EmployeeServiceBusinessException(
					String.format(appProperties.getMessages().get(FETCH_ALL_EMPLOYEES_EXCEPTION), e));

		}

		return employeeApiResponseDto;
	}
}
