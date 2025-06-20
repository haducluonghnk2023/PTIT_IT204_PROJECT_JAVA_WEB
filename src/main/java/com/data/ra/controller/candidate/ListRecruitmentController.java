package com.data.ra.controller.candidate;

import com.data.ra.entity.admin.RecruitmentPosition;
import com.data.ra.entity.auth.User;
import com.data.ra.entity.candidate.Candidate;
import com.data.ra.service.admin.CandidateService;
import com.data.ra.service.candidate.ApplicationService;
import com.data.ra.service.candidate.RecruitmentPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/candidate/list-recruitment")
public class ListRecruitmentController {
    @Autowired
    private RecruitmentPositionService recruitmentPositionService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private CandidateService candidateService;
    @GetMapping
    public String showListRecruitment() {
        return "candidate/list-recruitment";
    }

    @GetMapping("/details")
    public String showRecruitmentDetails(@RequestParam("id") Long id, Model model) {
        RecruitmentPosition recruitment = recruitmentPositionService.findById(id);
        List<RecruitmentPosition> allRecruitments = recruitmentPositionService.findAll();

        List<RecruitmentPosition> similarRecruitments = allRecruitments.stream()
                .filter(r -> !r.getId().equals(id))
                .collect(Collectors.toList());
        Collections.shuffle(similarRecruitments);
        similarRecruitments = similarRecruitments.stream().limit(4).collect(Collectors.toList());

        // Mô tả & yêu cầu
        List<String> descriptionList = Arrays.stream(recruitment.getDescription().split("\\."))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s + ".")
                .collect(Collectors.toList());

        List<String> requirementsList = Arrays.stream(recruitment.getRequirements().split("\\."))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s + ".")
                .collect(Collectors.toList());

        model.addAttribute("descriptionList", descriptionList);
        model.addAttribute("requirementsList", requirementsList);
        model.addAttribute("recruitment", recruitment);
        model.addAttribute("recruitments", similarRecruitments); // random

        return "candidate/recruitment-detail";
    }


    @PostMapping("/apply")
    public String applyRecruitment(@RequestParam("recruitmentId") Long recruitmentId,
                                   @RequestParam("cvUrl") String cvUrl,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("currentCandidate");
        Candidate candidate = candidateService.findByUserId(user.getId());

        // ✅ Kiểm tra URL hợp lệ
        if (!isValidUrl(cvUrl)) {
            redirectAttributes.addFlashAttribute("error", "Đường dẫn CV không hợp lệ!");
            return "redirect:/candidate/my-application";
        }

        // ✅ Kiểm tra đã ứng tuyển chưa
        boolean alreadyApplied = applicationService.existsByCandidateIdAndRecruitmentId(candidate.getId(), recruitmentId);
        if (alreadyApplied) {
            redirectAttributes.addFlashAttribute("warning", "Bạn đã ứng tuyển vị trí này rồi!");
            return "redirect:/candidate/my-application";
        }

        // ✅ Lưu ứng tuyển
        applicationService.save(candidate.getId(), recruitmentId, cvUrl);
        redirectAttributes.addFlashAttribute("success", "Ứng tuyển thành công!");
        return "redirect:/candidate/my-application";
    }


    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url).toURI(); // kiểm tra chuẩn URL và URI
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
