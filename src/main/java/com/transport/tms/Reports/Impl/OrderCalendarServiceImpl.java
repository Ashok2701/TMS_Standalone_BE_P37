package com.transport.tms.Reports.Impl;

import com.transport.tms.Reports.Dto.OrderCalendarDTO;
import com.transport.tms.Reports.Dto.OrderCalendarFilterDTO;
import com.transport.tms.Reports.Entity.OrderCalendarEntity;
import com.transport.tms.Reports.Repository.OrderCalendarRepository;
import com.transport.tms.Reports.Service.OrderCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderCalendarServiceImpl implements OrderCalendarService {

    private final OrderCalendarRepository orderCalendarRepository;

    @Override
    public List<OrderCalendarDTO> getOrderCalendars(OrderCalendarFilterDTO filter) {
        // TODO: query orderCalendarRepository using filter, map entities -> DTOs
        return null;
    }

    @Override
    public OrderCalendarDTO getById(Long id) {
        // TODO: fetch by id, map entity -> DTO, throw not-found if missing
        return null;
    }
}
