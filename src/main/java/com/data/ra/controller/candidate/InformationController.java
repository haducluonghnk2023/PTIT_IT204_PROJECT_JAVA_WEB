package com.data.ra.controller.candidate;

import com.data.ra.entity.admin.Technology;
import com.data.ra.entity.auth.User;
import com.data.ra.entity.candidate.Candidate;
import com.data.ra.service.admin.CandidateService;
import com.data.ra.service.admin.TechnologyService;
import com.data.ra.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/candidate/information")
public class InformationController {
    @Autowired
    private CandidateService candidateService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @GetMapping
    public String showInformation(HttpSession session, Model model){
        User currentUser = (User) session.getAttribute("currentCandidate");

        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        // Lấy đầy đủ thông tin Candidate từ DB
        Candidate candidate = candidateService.findByUserId(currentUser.getId());

        if (candidate == null) {
            // Nếu chưa tạo Candidate thì tạo mới hoặc trả về form trống
            candidate = new Candidate();
            candidate.setUser(currentUser);
            candidate.setName(currentUser.getUsername());
            candidate.setEmail(currentUser.getEmail());
        }


        model.addAttribute("candidate", candidate);
        model.addAttribute("technologyIds", candidate.getTechnologies()
                .stream()
                .map(Technology::getId)
                .collect(Collectors.toList()));
        model.addAttribute("allTechnologies", technologyService.findAlls());
        return "candidate/information";
    }

    @PostMapping("/update")
    public String updateInformation(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam("experience") int experience,
            @RequestParam("gender") String gender,
            @RequestParam("description") String description,
            @RequestParam(value = "technologyIds", required = false) String technologyIds,
            @RequestParam(value = "dobString", required = false) String dobString,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = (User) session.getAttribute("currentCandidate");

            if (currentUser == null) {
                return "redirect:/auth/login";
            }

            // Lấy candidate hiện tại từ DB
            Candidate candidate = candidateService.findByUserId(currentUser.getId());

            if (candidate == null) {
                // Tạo mới candidate
                candidate = new Candidate();
                candidate.setUser(currentUser);
                candidate.setStatus("Active"); // Set default status
            }

            // Update thông tin candidate
            candidate.setName(name);
            candidate.setEmail(email);
            candidate.setPhone(phone);
            candidate.setExperience(experience);
            candidate.setGender(gender);
            candidate.setDescription(description);

            // Xử lý Date of Birth
            if (dobString != null && !dobString.trim().isEmpty()) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date dob = dateFormat.parse(dobString);
                    candidate.setDob(dob);
                } catch (ParseException e) {
                    redirectAttributes.addFlashAttribute("error", "Invalid date format");
                    return "redirect:/candidate/information";
                }
            }

            // Xử lý Technologies
            Set<Technology> technologies = new HashSet<>();
            if (technologyIds != null && !technologyIds.trim().isEmpty()) {
                String[] techIdArray = technologyIds.split(",");
                for (String techIdStr : techIdArray) {
                    try {
                        Long techId = Long.parseLong(techIdStr.trim());
                        Technology tech = technologyService.findById(techId);
                        if (tech != null) {
                            technologies.add(tech);
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
            candidate.setTechnologies(technologies);

            // Lưu candidate
            candidateService.save(candidate);

            redirectAttributes.addFlashAttribute("success", "Information updated successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to update information: " + e.getMessage());
        }

        return "redirect:/candidate/information";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User currentUser = (User) session.getAttribute("currentCandidate");

        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        // Kiểm tra xác nhận mật khẩu
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorPassword", "New password and confirm password do not match.");
            return "redirect:/candidate/information";
        }

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            redirectAttributes.addFlashAttribute("errorPassword", "Old password is incorrect.");
            return "redirect:/candidate/information";
        }

        // Cập nhật mật khẩu mới
        String hashedNewPassword = passwordEncoder.encode(newPassword);
        currentUser.setPassword(hashedNewPassword);
        authService.save(currentUser);

        redirectAttributes.addFlashAttribute("successPassword", "Password changed successfully.");
        return "redirect:/candidate/information";
    }

}
