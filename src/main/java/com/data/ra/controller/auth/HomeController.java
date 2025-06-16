package com.data.ra.controller.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class HomeController {
    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/auth/login";
    }
    @GetMapping("/candidate/information")
    public String showInformation(){
        return "candidate/information";
    }
}
