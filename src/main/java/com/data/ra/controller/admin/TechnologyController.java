package com.data.ra.controller.admin;

import com.data.ra.dto.admin.TechnologyDTO;
import com.data.ra.entity.admin.Technology;
import com.data.ra.service.admin.TechnologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/technology")
public class TechnologyController {

    @Autowired
    private TechnologyService technologyService;

    // ================================
    // === Helper: DTO <-> Entity ===
    // ================================
    private TechnologyDTO toDto(Technology entity) {
        TechnologyDTO dto = new TechnologyDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }

    private Technology toEntity(TechnologyDTO dto) {
        Technology entity = new Technology();
        entity.setId(dto.getId());
        entity.setName(dto.getName().trim());
        return entity;
    }

    private void addCommonAttributes(Model model, int page, int size, String search) {
        long total = technologyService.count(search);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("search", search);
        model.addAttribute("total", total);
        model.addAttribute("totalPages", (int) Math.ceil((double) total / size));
        model.addAttribute("pageUrl", "/admin/technology");
    }

    // ==========================================
    // === GET: Danh sách + form (modal) ========
    // ==========================================
    @GetMapping
    public String listTechnologies(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String search,
                                   Model model,
                                   @ModelAttribute("technology") TechnologyDTO dto,
                                   HttpSession session) {

        Object user = session.getAttribute("currentAdmin");
        if (user == null) {
            return "redirect:/auth/login";
        }
        List<TechnologyDTO> technologies = technologyService.findAll(page, size, search)
                .stream().map(this::toDto).collect(Collectors.toList());

        model.addAttribute("technologies", technologies);
        model.addAttribute("technology", dto);
        model.addAttribute("editMode", dto.getId() != null);

        // Đảm bảo BindingResult luôn tồn tại nếu có lỗi validation
        if (!model.containsAttribute("org.springframework.validation.BindingResult.technology")) {
            model.addAttribute("org.springframework.validation.BindingResult.technology",
                    new BeanPropertyBindingResult(dto, "technology"));
        }

        model.addAttribute("showModal", model.containsAttribute("showModal"));
        addCommonAttributes(model, page, size, search);
        return "admin/technology";
    }

    // ==========================================
    // === GET: Mở modal thêm mới ==============
    // ==========================================
    @GetMapping("/add")
    public String showAddForm(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String search,
                              RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("technology", new TechnologyDTO());
        redirectAttributes.addFlashAttribute("showModal", true);
        redirectAttributes.addFlashAttribute("page", page);
        redirectAttributes.addFlashAttribute("size", size);
        redirectAttributes.addFlashAttribute("search", search);

        return "redirect:/admin/technology?page=" + page + "&size=" + size +
                (search != null ? "&search=" + search : "");
    }

    // ==========================================
    // === GET: Mở modal chỉnh sửa =============
    // ==========================================
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String search,
                               RedirectAttributes redirectAttributes) {
        Technology tech = technologyService.findById(id);
        if (tech != null) {
            redirectAttributes.addFlashAttribute("technology", toDto(tech));
            redirectAttributes.addFlashAttribute("showModal", true);
        }

        // Redirect lại kèm theo phân trang và search
        String redirectUrl = String.format("redirect:/admin/technology?page=%d&size=%d%s",
                page, size, (search != null && !search.isEmpty()) ? "&search=" + search : "");

        return redirectUrl;
    }

    // ==========================================
    // === POST: Lưu hoặc cập nhật công nghệ ====
    // ==========================================
    @PostMapping("/save")
    public String saveTechnology(@Valid @ModelAttribute("technology") TechnologyDTO dto,
                                 BindingResult result,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String search,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        // Check trùng tên
        boolean isDuplicate = technologyService.isNameDuplicate(dto.getName(), dto.getId());
        if (isDuplicate) {
            result.rejectValue("name", "error.technologyDTO", "Tên công nghệ đã tồn tại.");
        }

        // Nếu có lỗi validation
        if (result.hasErrors()) {
            List<TechnologyDTO> technologies = technologyService.findAll(page, size, search)
                    .stream().map(this::toDto).collect(Collectors.toList());

            model.addAttribute("technologies", technologies);
            model.addAttribute("technology", dto);
            model.addAttribute("editMode", dto.getId() != null);
            model.addAttribute("showModal", true);
            addCommonAttributes(model, page, size, search);
            model.addAttribute("org.springframework.validation.BindingResult.technology", result);

            return "admin/technology"; // Không redirect khi có lỗi
        }

        // Nếu hợp lệ => lưu
        try {
            technologyService.save(toEntity(dto));
            String msg = (dto.getId() == null)
                    ? "Thêm công nghệ thành công!" : "Cập nhật công nghệ thành công!";
            redirectAttributes.addFlashAttribute("successMessage", msg);
        } catch (RuntimeException e) {
            result.rejectValue("name", "error.technologyDTO", e.getMessage());

            List<TechnologyDTO> technologies = technologyService.findAll(page, size, search)
                    .stream().map(this::toDto).collect(Collectors.toList());

            model.addAttribute("technologies", technologies);
            model.addAttribute("technology", dto);
            model.addAttribute("editMode", dto.getId() != null);
            model.addAttribute("showModal", true);
            addCommonAttributes(model, page, size, search);
            model.addAttribute("org.springframework.validation.BindingResult.technology", result);

            return "admin/technology";
        }

        if (dto.getId() == null) {
            return String.format("redirect:/admin/technology?page=0&size=%d%s",
                    size, (search != null && !search.isEmpty()) ? "&search=" + search : "");
        } else {
            return String.format("redirect:/admin/technology?page=%d&size=%d%s",
                    page, size, (search != null && !search.isEmpty()) ? "&search=" + search : "");
        }
    }

    // ==========================================
    // === GET: Xoá công nghệ ===================
    // ==========================================
    @GetMapping("/delete/{id}")
    public String deleteTechnology(@PathVariable Long id,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String search,
                                   RedirectAttributes redirectAttributes) {
        try {
            technologyService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xoá công nghệ thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xoá công nghệ.");
        }

        return String.format("redirect:/admin/technology?page=%d&size=%d%s",
                page, size, (search != null && !search.isEmpty()) ? "&search=" + search : "");
    }
}
