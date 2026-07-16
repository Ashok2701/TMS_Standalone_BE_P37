package com.transport.tms.Reports.Controller;

import com.transport.tms.Reports.Dto.RouteListDTO;
import com.transport.tms.Reports.Dto.RouteListFilterDTO;
import com.transport.tms.Reports.Service.RouteListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports/route-list")
@RequiredArgsConstructor
public class RouteListController {

    private final RouteListService routeListService;

    // GET /api/reports/route-list
    // Accepts EITHER a JSON body (Axios, Postman, etc.) OR query params
    // (fetch(), plain browser calls). Body wins if the client sent one;
    // otherwise falls back to whatever was bound from query params.
    // Both resolve to the same RouteListFilterDTO, so the service never
    // needs to know which path the request came in on.
    @GetMapping
    public ResponseEntity<List<RouteListDTO>> getRouteLists(
            @RequestBody(required = false) RouteListFilterDTO body,
            @ModelAttribute RouteListFilterDTO queryParams) {
        RouteListFilterDTO filter = (body != null) ? body : queryParams;
        return ResponseEntity.ok(routeListService.getRouteLists(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteListDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(routeListService.getById(id));
    }
}
