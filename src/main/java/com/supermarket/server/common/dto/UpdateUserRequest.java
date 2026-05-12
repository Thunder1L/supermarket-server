package com.supermarket.server.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate; // 记得导包

@Data
public class UpdateUserRequest {
    private Long id;
    private String username;
    private String avatar;
    private String phone;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

}