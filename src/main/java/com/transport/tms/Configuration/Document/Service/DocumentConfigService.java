package com.transport.tms.Configuration.Document.Service;

import com.transport.tms.Configuration.Document.Dto.DocumentConfigDTO;

import java.util.List;
import java.util.UUID;

public interface DocumentConfigService {

    DocumentConfigDTO create(DocumentConfigDTO dto);

    List<DocumentConfigDTO> getAll();

    List<DocumentConfigDTO> getAllActive();

    List<DocumentConfigDTO> getByDocumentType(String documentType);

    DocumentConfigDTO getById(UUID id);

    DocumentConfigDTO update(UUID id, DocumentConfigDTO dto);

    DocumentConfigDTO toggleActive(UUID id);

    void delete(UUID id);
}
