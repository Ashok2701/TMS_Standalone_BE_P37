package com.transport.tms.Reports.Service;

import com.transport.tms.Reports.Dto.RouteListDTO;
import com.transport.tms.Reports.Dto.RouteListFilterDTO;

import java.util.List;

public interface RouteListService {

    List<RouteListDTO> getRouteLists(RouteListFilterDTO filter);

    RouteListDTO getById(Long id);
}
