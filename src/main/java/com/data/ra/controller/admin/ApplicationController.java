package com.data.ra.controller.admin;

import com.data.ra.dto.admin.ApplicationDTO;
import com.data.ra.entity.admin.Application;
import com.data.ra.service.candidate.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
                                  @RequestParam(value = "size", defaultValue = "5") int size) {

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



    @PostMapping("/candidate/mark-handling")
    public String markHandling(@RequestParam("id") Integer id,
                               RedirectAttributes redirectAttributes) {
        try {
            Application app = applicationService.findById(id);
            if (app == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy ứng viên.");
                return "redirect:/admin/application";
            }

            // Kiểm tra trạng thái phải là PENDING
            if (!"PENDING".equalsIgnoreCase(app.getProgress().name())) {
                redirectAttributes.addFlashAttribute("error", "Chỉ có thể cập nhật khi trạng thái là PENDING.");
                return "redirect:/admin/application";
            }

            applicationService.markHandling(id);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi cập nhật trạng thái.");
        }
        return "redirect:/admin/application";
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




