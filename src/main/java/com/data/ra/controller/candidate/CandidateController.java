package com.data.ra.controller.candidate;

import com.data.ra.entity.candidate.Candidate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/candidate")
public class CandidateController {
    @GetMapping
    public String showPageCandidate(Model model) {
        model.addAttribute("title", new Candidate());
        return "candidate/candidate";
    }
}
