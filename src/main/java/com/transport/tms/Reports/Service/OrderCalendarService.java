package com.transport.tms.Reports.Service;

import com.transport.tms.Reports.Dto.OrderCalendarDTO;
import com.transport.tms.Reports.Dto.OrderCalendarFilterDTO;

import java.util.List;

public interface OrderCalendarService {

    List<OrderCalendarDTO> getOrderCalendars(OrderCalendarFilterDTO filter);

    OrderCalendarDTO getById(Long id);
}
