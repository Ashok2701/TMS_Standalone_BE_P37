package com.transport.tms.Configuration.Document.Controller;


import com.transport.tms.Configuration.Document.Dto.DocumentConfigDTO;
import com.transport.tms.Configuration.Document.Service.DocumentConfigService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/document-config")
@RequiredArgsConstructor
public class DocumentConfigController {


    private final DocumentConfigService service;

    // CREATE DOCUMENT CONFIG

    @PostMapping
    public DocumentConfigDTO create(
            @RequestBody DocumentConfigDTO dto) {


        return service.create(dto);

    }




    // GET ALL DOCUMENT CONFIGS

    @GetMapping
    public List<DocumentConfigDTO> getAll() {


        return service.getAll();

    }




    // GET BY ID

    @GetMapping("/{id}")
    public DocumentConfigDTO getById(
            @PathVariable UUID id) {


        return service.getById(id);

    }





    // UPDATE

    @PutMapping("/{id}")
    public DocumentConfigDTO update(

            @PathVariable UUID id,

            @RequestBody DocumentConfigDTO dto) {


        return service.update(
                id,
                dto
        );

    }






    // DELETE

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id) {


        service.delete(id);

    }


}