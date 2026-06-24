package com.swedbank.common.application.Dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class BaseDto {

    private UUID id;

    private LocalDateTime created;

    private LocalDateTime updated;

}
