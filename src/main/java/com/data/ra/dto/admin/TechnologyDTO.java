package com.data.ra.dto.admin;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
@Getter
@Setter
public class TechnologyDTO {
    private Long id;

    @NotBlank(message = "Tên công nghệ không được để trống")
    @Size(max = 100, message = "Tên công nghệ không được vượt quá 100 ký tự")
    private String name;

    private Boolean isDeleted = false;
}
