package com.data.ra.controller.admin;

import com.data.ra.dto.admin.RecruitmentPositionDTO;
import com.data.ra.entity.admin.RecruitmentPosition;
import com.data.ra.entity.admin.Technology;
import com.data.ra.service.admin.TechnologyService;
import com.data.ra.service.candidate.RecruitmentPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/recruitment-position")
public class RecruitmentPositionController {
    @Autowired
    private RecruitmentPositionService recruitmentPositionService;

    @Autowired
    private TechnologyService technologyService;

    @GetMapping
    public String showRecruitmentPosition(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "search", required = false) String search,
            Model model,
            HttpSession session) {

        Object user = session.getAttribute("currentAdmin");
        if (user == null) {
            return "redirect:/auth/login";
        }
        if (page < 0) page = 0;

        List<RecruitmentPosition> recruitmentPositions;
        long totalItems;

        if (search != null && !search.trim().isEmpty()) {
            recruitmentPositions = recruitmentPositionService.searchByName(search.trim(), page, size);
            totalItems = recruitmentPositionService.countSearchByName(search.trim());
        } else {
            recruitmentPositions = recruitmentPositionService.findAlls(page, size);
            totalItems = recruitmentPositionService.countAll();
        }

        List<RecruitmentPositionDTO> positionDTOs = recruitmentPositions.stream()
                .map(RecruitmentPositionDTO::new)
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) totalItems / size);

        model.addAttribute("positions", positionDTOs);
        model.addAttribute("technologies", technologyService.findAlls());
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search); // để hiển thị lại giá trị đã tìm

        return "admin/recruitment-position";
    }

    @PostMapping("/save")
    public String saveRecruitmentPosition(
            @ModelAttribute RecruitmentPositionDTO dto,
            RedirectAttributes redirectAttributes) {

        boolean isUpdate = dto.getId() != null;
        RecruitmentPosition existingPosition = recruitmentPositionService.findByName(dto.getName());

        // Nếu đang tạo mới hoặc tên đã bị đổi và trùng với tên khác
        if (existingPosition != null &&
                (!isUpdate || (isUpdate && !existingPosition.getId().equals(dto.getId())))) {
            redirectAttributes.addFlashAttribute("error", "Tên vị trí tuyển dụng đã tồn tại.");
            return "redirect:/admin/recruitment-position";
        }

        RecruitmentPosition position = new RecruitmentPosition();
        if (isUpdate) {
            position.setId(dto.getId());
        }

        position.setName(dto.getName());
        position.setMinSalary(dto.getMinSalary());
        position.setMaxSalary(dto.getMaxSalary());
        position.setMinExperience(dto.getMinExperience());
        position.setCreatedDate(dto.getCreatedDate());
        position.setExpiredDate(dto.getExpiredDate());
        position.setDescription(dto.getDescription());
        position.setRequirements(dto.getRequirements());
        position.setEducation(dto.getEducation());
        position.setLocation(dto.getLocation());
        position.setType(dto.getType());

        // Gán công nghệ
        List<Technology> technologies = technologyService.findByIds(dto.getTechnologyIds());
        position.setTechnologies(new HashSet<>(technologies));

        try {
            recruitmentPositionService.save(position);
            redirectAttributes.addFlashAttribute("success", isUpdate ?
                    "Cập nhật vị trí tuyển dụng thành công!" :
                    "Thêm vị trí tuyển dụng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi lưu vị trí tuyển dụng.");
        }

        return "redirect:/admin/recruitment-position";
    }


    @GetMapping("/delete/{id}")
    public String deleteRecruitmentPosition(@PathVariable("id") Long id,
                                            RedirectAttributes redirectAttributes) {
        try {
            recruitmentPositionService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Xóa vị trí tuyển dụng thành công!");
        } catch (Exception e) {
            // Trường hợp dính khóa ngoại: ví dụ đã có ứng viên apply vào vị trí
            redirectAttributes.addFlashAttribute("warning",
                    "Không thể xóa vì vị trí này đã được sử dụng. Đã cập nhật tên thêm '_deleted'.");
        }

        return "redirect:/admin/recruitment-position";
    }

    @GetMapping("/edit/{id}")
    public String editRecruitmentPosition(@PathVariable("id") Long id,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(value = "search", required = false) String search,
                                          Model model) {

        RecruitmentPosition position = recruitmentPositionService.findById(id);
        if (position == null) {
            return "redirect:/admin/recruitment-position?error=notfound";
        }

        // Phần lấy danh sách như GET mặc định
        List<RecruitmentPosition> recruitmentPositions;
        long totalItems;

        if (search != null && !search.trim().isEmpty()) {
            recruitmentPositions = recruitmentPositionService.searchByName(search.trim(), page, size);
            totalItems = recruitmentPositionService.countSearchByName(search.trim());
        } else {
            recruitmentPositions = recruitmentPositionService.findAlls(page, size);
            totalItems = recruitmentPositionService.countAll();
        }

        List<RecruitmentPositionDTO> positionDTOs = recruitmentPositions.stream()
                .map(RecruitmentPositionDTO::new)
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) totalItems / size);

        // ✅ Truyền dữ liệu y hệt GET mặc định
        model.addAttribute("positions", positionDTOs);
        model.addAttribute("technologies", technologyService.findAlls());
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search);

        // ✅ Truyền thêm position đang được sửa
        model.addAttribute("position", new RecruitmentPositionDTO(position));
        model.addAttribute("editMode", true); // cờ xác định là đang edit

        return "admin/recruitment-position";
    }


}
