package com.transport.tms.Reports.Controller;

import com.transport.tms.Reports.Dto.OrderCalendarDTO;
import com.transport.tms.Reports.Dto.OrderCalendarFilterDTO;
import com.transport.tms.Reports.Service.OrderCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports/order-calendar")
@RequiredArgsConstructor
public class OrderCalendarController {

    private final OrderCalendarService orderCalendarService;

    // GET /api/reports/order-calendar
    // Accepts EITHER a JSON body (Axios, Postman, etc.) OR query params
    // (fetch(), plain browser calls). Body wins if the client sent one;
    // otherwise falls back to whatever was bound from query params.
    // Both resolve to the same OrderCalendarFilterDTO, so the service never
    // needs to know which path the request came in on.
    @GetMapping
    public ResponseEntity<List<OrderCalendarDTO>> getOrderCalendars(
            @RequestBody(required = false) OrderCalendarFilterDTO body,
            @ModelAttribute OrderCalendarFilterDTO queryParams) {
        OrderCalendarFilterDTO filter = (body != null) ? body : queryParams;
        return ResponseEntity.ok(orderCalendarService.getOrderCalendars(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderCalendarDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderCalendarService.getById(id));
    }
}
