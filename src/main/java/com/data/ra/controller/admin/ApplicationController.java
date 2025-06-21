package com.data.ra.controller.admin;

import com.data.ra.dto.admin.ApplicationDTO;
import com.data.ra.entity.admin.Application;
import com.data.ra.entity.admin.Progress;
import com.data.ra.service.candidate.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/application")
public class ApplicationController {
    @Autowired
    private ApplicationService applicationService;

    @GetMapping
    public String showApplication(Model model,
                                  @RequestParam(value = "keyword", required = false) String keyword,
                                  @RequestParam(value = "process", required = false) String progress,
                                  @RequestParam(value = "page", defaultValue = "1") int page,
                                  @RequestParam(value = "size", defaultValue = "10") int size) {

        List<ApplicationDTO> dtos = applicationService.findAll().stream()
                .filter(app -> {
                    boolean matchesKeyword = (keyword == null || keyword.isEmpty()) ||
                            app.getRecruitmentPosition().getName().toLowerCase().contains(keyword.toLowerCase());

                    boolean matchesProcess = (progress == null || progress.isEmpty()) ||
                            (app.getProgress() != null && app.getProgress().name().equalsIgnoreCase(progress));

                    return matchesKeyword && matchesProcess;
                })
                .map(ApplicationDTO::new)
                .collect(Collectors.toList());

        int totalItems = dtos.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, totalItems);
        List<ApplicationDTO> paginated = dtos.subList(fromIndex, toIndex);

        model.addAttribute("applications", paginated);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("keyword", keyword);
        model.addAttribute("progress", progress);

        return "admin/application";
    }

    @GetMapping("/candidate/view-cv/{id}")
    public String viewCVModalContent(@PathVariable("id") Integer appId, Model model) {
        Application application = applicationService.findById(appId);

        if (application == null) {
            model.addAttribute("error", "Không tìm thấy thông tin ứng tuyển.");
            return "fragments/cv-error :: content";
        }

        String cvUrl = application.getCvUrl();
        if (cvUrl == null || cvUrl.trim().isEmpty()) {
            model.addAttribute("error", "CV không tồn tại.");
            return "fragments/cv-error :: content";
        }

        boolean loadSuccess = false;
        String viewName;

        // ✅ Nếu có chứa "pdf" trong URL thì xem là PDF (Cloudinary lưu đúng loại nhưng không có đuôi)
        if (cvUrl.toLowerCase().contains(".pdf")) {
            model.addAttribute("cvUrl", cvUrl);
            loadSuccess = true;
            viewName = "fragments/cv-pdf :: content";
        } else {
            // ✅ Mặc định còn lại là TXT
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new URL(cvUrl).openStream(), StandardCharsets.UTF_8))) {

                String content = reader.lines()
                        .map(line -> line.replaceAll("^(\\s*[IVXLCDM]+\\.\\s.*)$", "<strong>$1</strong>"))
                        .collect(Collectors.joining("<br>"));

                model.addAttribute("cvText", content);
                loadSuccess = true;
                viewName = "fragments/cv-text :: content";
            } catch (IOException e) {
                model.addAttribute("error", "Không thể tải nội dung CV.");
                return "fragments/cv-error :: content";
            }
        }

        // ✅ Cập nhật trạng thái nếu load thành công
        if (loadSuccess && application.getProgress() == Progress.pending) {
            application.setProgress(Progress.handling);
            application.setUpdateAt(new Date());
            applicationService.save(application);
        }

        return viewName;
    }



    @PostMapping("/schedule-interview")
    public String scheduleInterview(@RequestParam("applicationId") Integer id,
                                    @RequestParam("link") String interviewLink,
                                    @RequestParam("time") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date interviewDate,
                                    RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra link có đúng định dạng Google Meet hoặc Zoom không
            if (!isValidInterviewLink(interviewLink)) {
                redirectAttributes.addFlashAttribute("error", "Chỉ chấp nhận link từ Google Meet hoặc Zoom.");
                return "redirect:/admin/application";
            }

            Date now = new Date();
            if (interviewDate.before(now)) {
                redirectAttributes.addFlashAttribute("error", "Thời gian phỏng vấn không được ở quá khứ.");
                return "redirect:/admin/application";
            }

            applicationService.scheduleInterview(id, interviewDate, interviewLink);
            redirectAttributes.addFlashAttribute("success", "Lên lịch phỏng vấn thành công!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi lên lịch phỏng vấn.");
        }
        return "redirect:/admin/application";
    }

    private boolean isValidInterviewLink(String link) {
        if (link == null) return false;
        return link.startsWith("https://meet.google.com/") || link.startsWith("https://zoom.us/");
    }

    @PostMapping("/approve-interview")
    public String approveInterview(@RequestParam("applicationId") Integer id,
                                   RedirectAttributes redirectAttributes) {
        try {
            applicationService.approveInterview(id);
            redirectAttributes.addFlashAttribute("success", "Duyệt phỏng vấn thành công!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi duyệt phỏng vấn.");
        }
        return "redirect:/candidate/my-application";
    }

    @PostMapping("/approve-result")
    public String approveResult(@RequestParam("applicationId") Integer id,
                                @RequestParam("result") String result,
                                @RequestParam("resultNote") String resultNote,
                                RedirectAttributes redirectAttributes) {
        try {
            if (result == null || result.trim().isEmpty() || result.trim().equalsIgnoreCase("chua co") ||
                    resultNote == null || resultNote.trim().isEmpty() || resultNote.trim().equalsIgnoreCase("chua co")) {
                throw new IllegalArgumentException("Vui lòng nhập đầy đủ kết quả và ghi chú.");
            }

            applicationService.saveInterviewResult(id, result, resultNote);
            redirectAttributes.addFlashAttribute("success", "Lưu kết quả phỏng vấn thành công!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi lưu kết quả.");
        }
        return "redirect:/admin/application";
    }


    @PostMapping("/destroy-interview")
    public String destroyInterview(@RequestParam("applicationId") Integer id,
                                   @RequestParam("reason") String reason,
                                   RedirectAttributes redirectAttributes) {
        try {
            applicationService.destroyInterview(id, reason);
            redirectAttributes.addFlashAttribute("success", "Hủy phỏng vấn thành công!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi hủy phỏng vấn.");
        }
        return "redirect:/admin/application";
    }

}




