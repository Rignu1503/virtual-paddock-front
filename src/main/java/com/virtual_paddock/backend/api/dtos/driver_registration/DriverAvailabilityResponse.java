package com.virtual_paddock.backend.api.dtos.driver_registration;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverAvailabilityResponse {
    private boolean nameAvailable;
    private boolean gamertagAvailable;
    private boolean carNumberAvailable;
    private String nameMessage;
    private String gamertagMessage;
    private String carNumberMessage;
}
