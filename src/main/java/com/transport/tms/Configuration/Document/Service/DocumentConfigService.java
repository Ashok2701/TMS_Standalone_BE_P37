package com.transport.tms.Configuration.Document.Service;

import com.transport.tms.Configuration.Document.Dto.DocumentConfigDTO;

import java.util.List;
import java.util.UUID;

public interface DocumentConfigService {


    DocumentConfigDTO create(
            DocumentConfigDTO dto);


    List<DocumentConfigDTO> getAll();


    DocumentConfigDTO getById(
            UUID id);


    DocumentConfigDTO update(
            UUID id,
            DocumentConfigDTO dto);


    void delete(
            UUID id);

}