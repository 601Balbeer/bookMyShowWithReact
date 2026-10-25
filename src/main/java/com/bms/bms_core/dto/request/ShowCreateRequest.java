package com.bms.bms_core.dto.request;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowCreateRequest {
    private String showName;
    private String shortDescription;
    private String artists;
    private LocalDateTime showStartTime;
    private LocalDateTime showEndTime;
}
