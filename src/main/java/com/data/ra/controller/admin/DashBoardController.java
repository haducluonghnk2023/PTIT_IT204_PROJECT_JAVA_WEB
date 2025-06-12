package com.data.ra.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/dashboard")
public class DashBoardController {
    @RequestMapping
    public String dashboard() {
        // model.addAttribute("page", "dashboard");
        return "admin/dashboard";
    }
}
