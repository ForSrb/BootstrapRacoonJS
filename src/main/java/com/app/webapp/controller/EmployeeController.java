package com.app.webapp.controller;

import com.app.webapp.model.Employee;
import com.app.webapp.service.IDepartmentService;
import com.app.webapp.service.IEmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
public class EmployeeController {
    private final IEmployeeService employeeService;
    private final IDepartmentService departmentService;

    public EmployeeController(IEmployeeService employeeService, IDepartmentService departmentService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    @GetMapping(value = "/employees")
    public ModelAndView employees(@RequestParam(value = "page", required = false, defaultValue = "1") Integer page) {
        ModelAndView modelAndView = new ModelAndView("employee/employees");
        return modelAndView.addObject("employees", employeeService.findAllEmployees(page));
    }

    @GetMapping("/employee/create")
    public ModelAndView createEmployee() {
        ModelAndView modelAndView = new ModelAndView("employee/create");
        modelAndView.addObject("employee", new Employee());
        modelAndView.addObject("departments", departmentService.findAll());

        return modelAndView;
    }

    @PostMapping("/employee/create")
    public ModelAndView createEmployee(@Valid @ModelAttribute("employee") Employee employee, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        if (bindingResult.hasErrors()) {
            modelAndView.addObject("departments", departmentService.findAll());
            modelAndView.setViewName("employee/create");
        } else {
            employeeService.createEmployee(employee);
            modelAndView.setViewName("redirect:/employees");
        }

        return modelAndView;
    }

    @GetMapping("/employee/edit/{id}")
    public String editEmployee(@PathVariable("id") Long id, Model model) {
        if (!model.containsAttribute("employee"))
            model.addAttribute("employee", employeeService.findEmployeeById(id));
        model.addAttribute("departments", departmentService.findAll());

        return "employee/edit";
    }

    @PostMapping("/employee/edit")
    public ModelAndView editEmployee(@Valid @ModelAttribute("employee") Employee employee, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        ModelAndView modelAndView = new ModelAndView();
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.employee", bindingResult);
            redirectAttributes.addFlashAttribute("employee", employee);
            modelAndView.setViewName("redirect:/employee/edit/" + employee.getId());
        } else {
            employeeService.editEmployee(employee);
            modelAndView.setViewName("redirect:/employees");
        }

        return modelAndView;
    }

    @GetMapping("/employee/delete/{id}")
    public String deleteEmployee(@PathVariable("id") Long id) {
        employeeService.deleteById(id);
        return "redirect:/employees";
    }
}