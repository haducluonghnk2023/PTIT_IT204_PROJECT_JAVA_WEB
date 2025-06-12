package com.data.ra.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/application")
public class ApplicationController {
    @RequestMapping
    public String showApplication() {
        return "admin/application";
    }
}
