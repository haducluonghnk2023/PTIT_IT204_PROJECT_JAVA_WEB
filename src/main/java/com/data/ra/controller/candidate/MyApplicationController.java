package com.data.ra.controller.candidate;

import com.data.ra.entity.admin.Application;
import com.data.ra.entity.auth.User;
import com.data.ra.entity.candidate.Candidate;
import com.data.ra.service.admin.CandidateService;
import com.data.ra.service.candidate.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/candidate/my-application")
public class MyApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private CandidateService candidateService;

    @GetMapping
    public String myApplication(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                HttpSession session, Model model) {
        Object currentCandidate = session.getAttribute("currentCandidate");

        if (currentCandidate != null && currentCandidate instanceof User) {
            User user = (User) currentCandidate;

            // Tìm Candidate từ user.id
            Candidate candidate = candidateService.findByUserId(user.getId());

            if (candidate == null) {
                return "redirect:/login"; // không có candidate ứng với user
            }

            Integer candidateId = candidate.getId();

            // ✅ Gọi hàm JDBC lấy danh sách ứng tuyển theo trang
            List<Application> applications = applicationService.findAllByUsers(candidateId, page, size);

            // ✅ Gọi hàm đếm tổng số ứng tuyển
            int totalItems = applicationService.countAllByUser(candidateId);
            int totalPages = (int) Math.ceil((double) totalItems / size);

            // ✅ Đưa vào model
            model.addAttribute("applications", applications);
            model.addAttribute("currentPage", page);
            model.addAttribute("size", size);
            model.addAttribute("page", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("pageSize", size);

            return "candidate/my-application";
        }

        return "redirect:/login";
    }



}
