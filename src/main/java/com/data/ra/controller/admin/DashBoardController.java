package com.data.ra.controller.admin;

import com.data.ra.service.admin.CandidateService;
import com.data.ra.service.admin.TechnologyService;
import com.data.ra.service.candidate.ApplicationService;
import com.data.ra.service.candidate.RecruitmentPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/dashboard")
public class DashBoardController {

    @Autowired
    private RecruitmentPositionService recruitmentPositionService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private ApplicationService applicationService;


    @GetMapping
    public String dashboard(Model model, HttpSession session) {
        Object user = session.getAttribute("currentAdmin");
        if (user == null) {
            return "redirect:/auth/login";
        }

        long totalRequirement = recruitmentPositionService.countAll();
        long totalTechnology = technologyService.countAll();
        long totalCandidate = candidateService.countAll();
        long totalApplication = applicationService.countAll();

        model.addAttribute("totalRequirement", totalRequirement);
        model.addAttribute("totalTechnology", totalTechnology);
        model.addAttribute("totalCandidate", totalCandidate);
        model.addAttribute("totalApplication", totalApplication);

        return "admin/dashboard";
    }

}
