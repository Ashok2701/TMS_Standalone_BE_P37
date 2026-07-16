package com.transport.tms.Reports.Impl;

import com.transport.tms.Reports.Dto.RouteListDTO;
import com.transport.tms.Reports.Dto.RouteListFilterDTO;
import com.transport.tms.Reports.Entity.RouteListEntity;
import com.transport.tms.Reports.Repository.RouteListRepository;
import com.transport.tms.Reports.Service.RouteListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteListServiceImpl implements RouteListService {

    private final RouteListRepository routeListRepository;

    @Override
    public List<RouteListDTO> getRouteLists(RouteListFilterDTO filter) {
        // TODO: query routeListRepository using filter, map entities -> DTOs
        return null;
    }

    @Override
    public RouteListDTO getById(Long id) {
        // TODO: fetch by id, map entity -> DTO, throw not-found if missing
        return null;
    }
}
