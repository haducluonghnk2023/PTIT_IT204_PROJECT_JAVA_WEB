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
            Model model) {

        User currentUser = (User) session.getAttribute("currentCandidate");
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        User existingUser = authService.findById(currentUser.getId());
        if (existingUser == null) {
            model.addAttribute("error", "Tài khoản không tồn tại.");
            return "candidate/information";
        }

        Candidate candidate = candidateService.findByUserId(currentUser.getId());
        boolean isNewCandidate = false;
        if (candidate == null) {
            candidate = new Candidate();
            candidate.setUser(currentUser);
            candidate.setStatus("Active");
            isNewCandidate = true;
        }

        // ✅ Validate định dạng email
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            model.addAttribute("emailError", "Email không hợp lệ.");
            prepareFormModel(model, candidate, technologyIds);
            return "candidate/information";
        }

        // ✅ Validate định dạng số điện thoại Việt Nam (nếu có)
        if (phone != null && !phone.trim().isEmpty()) {
            String vnPhoneRegex = "^(0|\\+84)(3[2-9]|5[6|8|9]|7[06-9]|8[1-9]|9[0-9])\\d{7}$";
            if (!phone.matches(vnPhoneRegex)) {
                model.addAttribute("phoneError", "Số điện thoại không hợp lệ (bắt đầu bằng 0 hoặc +84)");
                prepareFormModel(model, candidate, technologyIds);
                return "candidate/information";
            }
        }

        // ✅ Check email trùng
        User emailCheck = authService.findByEmail(email);
        if (emailCheck != null && !emailCheck.getId().equals(existingUser.getId())) {
            model.addAttribute("emailError", "Email đã được sử dụng bởi tài khoản khác.");
            prepareFormModel(model, candidate, technologyIds);
            return "candidate/information";
        }

        // ✅ Check phone trùng
        if (phone != null && !phone.trim().isEmpty()) {
            Candidate phoneCheck = candidateService.findByPhone(phone);
            if (phoneCheck != null && (candidate.getId() == null || !candidate.getId().equals(phoneCheck.getId()))) {
                model.addAttribute("phoneError", "Số điện thoại đã được sử dụng.");
                prepareFormModel(model, candidate, technologyIds);
                return "candidate/information";
            }
        }

        // ✅ Set dữ liệu
        candidate.setName(name);
        candidate.setEmail(email);
        candidate.setPhone(phone);
        candidate.setExperience(experience);
        candidate.setGender(gender);
        candidate.setDescription(description);

        // ✅ Parse ngày sinh
        if (dobString != null && !dobString.trim().isEmpty()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date dob = dateFormat.parse(dobString);

                Date now = new Date();
                if (dob.after(now)) {
                    model.addAttribute("dobError", "Ngày sinh không được lớn hơn ngày hiện tại.");
                    prepareFormModel(model, candidate, technologyIds);
                    return "candidate/information";
                }

                candidate.setDob(dob);
            } catch (ParseException e) {
                model.addAttribute("dobError", "Sai định dạng ngày sinh.");
                prepareFormModel(model, candidate, technologyIds);
                return "candidate/information";
            }
        }

        // ✅ Xử lý công nghệ
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
                } catch (NumberFormatException ignored) {}
            }
        }
        candidate.setTechnologies(technologies);

        candidateService.save(candidate);

        model.addAttribute("success", isNewCandidate ? "Tạo hồ sơ thành công!" : "Cập nhật thành công!");
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

    private void prepareFormModel(Model model, Candidate candidate, String technologyIds) {
        model.addAttribute("candidate", candidate);
        model.addAttribute("allTechnologies", technologyService.findAlls());

        List<Long> selectedIds = new ArrayList<>();
        if (technologyIds != null) {
            selectedIds = Arrays.stream(technologyIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        }

        model.addAttribute("technologyIds", selectedIds);
    }



}
