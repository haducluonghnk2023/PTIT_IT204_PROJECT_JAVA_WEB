package com.data.ra.controller.auth;

import com.data.ra.dto.admin.RecruitmentPositionDTO;
import com.data.ra.entity.admin.RecruitmentPosition;
import com.data.ra.entity.admin.Technology;
import com.data.ra.service.candidate.RecruitmentPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private RecruitmentPositionService recruitmentPositionService;

    // Trang chủ hiển thị top 9 vị trí
    @GetMapping("/")
    public String home(Model model) {
        List<RecruitmentPosition> allPositions = recruitmentPositionService.findAll();

        // Sắp xếp theo ngày tạo mới nhất
        List<RecruitmentPositionDTO> top9 = allPositions.stream()
                .sorted(Comparator.comparing(RecruitmentPosition::getCreatedDate).reversed())
                .limit(9)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        model.addAttribute("recruitmentPosition", top9);
        return "candidate/home";
    }


    @GetMapping("/recruitment/search")
    public String search(@RequestParam(value = "keyword", required = false) String keyword,
                         Model model) {
        List<RecruitmentPosition> results;

        if (keyword != null && !keyword.trim().isEmpty()) {
            results = recruitmentPositionService.searchByName(keyword.trim());
        } else {
            results = recruitmentPositionService.findAll();
        }

        List<RecruitmentPositionDTO> dtos = results.stream()
                .limit(9)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        model.addAttribute("recruitmentPosition", dtos);
        model.addAttribute("keyword", keyword);

        // Gửi cờ nếu không có kết quả
        if (dtos.isEmpty()) {
            model.addAttribute("notFound", true);
        }

        return "candidate/home";
    }

    @GetMapping("/recruitment/filter")
    public String filter(@RequestParam(value = "location", required = false) String location,
                         @RequestParam(value = "type", required = false) String type,
                         @RequestParam(value = "keyword", required = false) String keyword,
                         Model model) {

        List<RecruitmentPosition> filteredList = recruitmentPositionService
                .filterByLocationTypeAndKeyword(location, type, keyword);

        List<RecruitmentPositionDTO> dtos = filteredList.stream()
                .limit(9)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        model.addAttribute("recruitmentPosition", dtos);
        model.addAttribute("selectedLocation", location);
        model.addAttribute("selectedType", type);
        model.addAttribute("keyword", keyword);

        if (dtos.isEmpty()) {
            model.addAttribute("notFound", true);
        }

        return "candidate/home";
    }



    private RecruitmentPositionDTO convertToDTO(RecruitmentPosition r) {
        RecruitmentPositionDTO dto = new RecruitmentPositionDTO();

        dto.setId(r.getId());
        dto.setName(r.getName());
        dto.setCreatedDate(r.getCreatedDate());
        dto.setExpiredDate(r.getExpiredDate());
        dto.setMinSalary(r.getMinSalary());
        dto.setMaxSalary(r.getMaxSalary());
        dto.setMinExperience(r.getMinExperience());
        dto.setDescription(r.getDescription());
        dto.setRequirements(r.getRequirements());
        dto.setEducation(r.getEducation());
        dto.setLocation(r.getLocation());
        dto.setType(r.getType());

        if (r.getCreatedDate() != null) {
            dto.setCreatedDateStr(r.getCreatedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }

        // Tính thời gian đăng
        LocalDateTime created = r.getCreatedDate().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(created, now);
        Period period = Period.between(r.getCreatedDate(), LocalDate.now());

        if (period.getYears() > 0) {
            dto.setTimeAgo(period.getYears() + " năm trước");
        } else if (period.getMonths() > 0) {
            dto.setTimeAgo(period.getMonths() + " tháng trước");
        } else if (period.getDays() > 0) {
            dto.setTimeAgo(period.getDays() + " ngày trước");
        } else {
            long hours = duration.toHours();
            dto.setTimeAgo(hours > 0 ? hours + " giờ trước"
                    : (duration.toMinutes() > 0 ? duration.toMinutes() + " phút trước" : "Vừa xong"));
        }

        // Lấy công nghệ
        List<Technology> techs = r.getTechnologies().stream()
                .sorted(Comparator.comparingLong(Technology::getId))
                .collect(Collectors.toList());

        dto.setTechnologiesHtml(techs); // full entity dùng hiển thị
        dto.setTechnologyIds(techs.stream().map(Technology::getId).collect(Collectors.toList())); // id dùng post

        if (!techs.isEmpty()) {
            dto.setFirstTechnology(techs.get(0).getName());
            dto.setRemainingTechnologies(
                    techs.stream().skip(1).map(Technology::getName).collect(Collectors.toList()));
        }

        return dto;
    }

}
