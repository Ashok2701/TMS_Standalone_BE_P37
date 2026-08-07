package com.transport.tms.Pod.Service;

import com.transport.tms.Pod.Dto.DriverLoginRequestDTO;
import com.transport.tms.Pod.Dto.DriverLoginResponseDTO;

public interface DriverAuthService {

    DriverLoginResponseDTO login(DriverLoginRequestDTO dto);
}
