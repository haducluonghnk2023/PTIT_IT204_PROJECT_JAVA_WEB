package com.data.ra.controller.candidate;

import com.data.ra.dto.admin.CandidateDTO;
import com.data.ra.entity.admin.Technology;
import com.data.ra.entity.candidate.Candidate;
import com.data.ra.service.admin.CandidateService;
import com.data.ra.service.admin.TechnologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/candidate")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private TechnologyService technologyService;

    @GetMapping
    public String filterCandidates(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer experience,
            @RequestParam(required = false) String age,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String technology,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            HttpSession session,
            Model model
    ) {
        // 👉 Lưu và lấy lại pageSize từ session
        if (size != null) {
            session.setAttribute("candidatePageSize", size);
        } else {
            size = (Integer) session.getAttribute("candidatePageSize");
            if (size == null) size = 10;
        }

        // 👉 Parse khoảng tuổi từ chuỗi "min-max"
        int minAge = 0, maxAge = 200; // Default full range
        if (age != null && age.matches("\\d+-\\d+")) {
            String[] parts = age.split("-");
            minAge = Integer.parseInt(parts[0]);
            maxAge = Integer.parseInt(parts[1]);
        }

        // 👉 Gọi service lọc ứng viên
        List<Candidate> candidates = candidateService.filterCandidates(
                name, experience, minAge, maxAge, gender, technology, page, size
        );

        List<CandidateDTO> candidateDTOs = candidates.stream()
                .map(CandidateDTO::new)
                .collect(Collectors.toList());

        int totalItems = candidateService.countFilteredCandidates(
                name, experience, minAge, maxAge, gender, technology
        );
        int totalPages = (int) Math.ceil((double) totalItems / size);

        // 👉 Lấy danh sách công nghệ cho select box
        List<Technology> technologies = technologyService.findAlls();

        // 👉 Truyền dữ liệu sang view
        model.addAttribute("candidates", candidateDTOs);
        model.addAttribute("technologies", technologies);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("pageUrl", buildPageUrl(name, experience, age, gender, technology, size));

        // 👉 Giữ lại filter đã chọn trong form
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);
        paramMap.put("experience", experience);
        paramMap.put("age", age);
        paramMap.put("gender", gender);
        paramMap.put("technology", technology);

        model.addAttribute("param", paramMap);

        return "candidate/candidate";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Long id,
                               @RequestParam boolean status,
                               @RequestParam(required = false) String name,
                               @RequestParam(required = false) String experience,
                               @RequestParam(required = false) String age,
                               @RequestParam(required = false) String gender,
                               @RequestParam(required = false) String technology,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(required = false) Integer size,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        // ✅ Nếu không có size từ request thì lấy từ session
        if (size == null) {
            Integer savedSize = (Integer) session.getAttribute("candidatePageSize");
            size = (savedSize != null) ? savedSize : 10;
        }

        candidateService.updateStatusById(id, status);

        redirectAttributes.addAttribute("name", name);
        redirectAttributes.addAttribute("experience", experience);
        redirectAttributes.addAttribute("age", age);
        redirectAttributes.addAttribute("gender", gender);
        redirectAttributes.addAttribute("technology", technology);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);

        return "redirect:/admin/candidate";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam Long id,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        String newPassword = candidateService.resetPasswordById(id);
        redirectAttributes.addFlashAttribute("message", "Mật khẩu mới là: " + newPassword);

        return "redirect:/admin/candidate";
    }

    private String buildPageUrl(String name, Integer experience, String age, String gender, String technology, int size) {
        StringBuilder sb = new StringBuilder("/admin/candidate");

        List<String> params = new ArrayList<>();
        if (name != null && !name.isEmpty()) params.add("name=" + name);
        if (experience != null) params.add("experience=" + experience);
        if (age != null && !age.isEmpty()) params.add("age=" + age);
        if (gender != null && !gender.isEmpty()) params.add("gender=" + gender);
        if (technology != null && !technology.isEmpty()) params.add("technology=" + technology);
        params.add("size=" + size);

        sb.append("?").append(String.join("&", params));

        return sb.toString();
    }

}
