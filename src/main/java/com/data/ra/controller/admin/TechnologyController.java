package com.data.ra.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/technology")
public class TechnologyController {
    @RequestMapping
    public String technology() {
        return "admin/technology";
    }
}
