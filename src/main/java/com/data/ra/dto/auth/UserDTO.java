package com.data.ra.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    @NotBlank(message = "Email không được để trống")
    private String email;
    @NotBlank(message = "Password không được để trống")
    private String password;
}
