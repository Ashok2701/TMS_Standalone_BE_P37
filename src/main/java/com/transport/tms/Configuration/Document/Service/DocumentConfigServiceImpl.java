package com.transport.tms.Configuration.Document.Service;


import com.transport.tms.Configuration.Document.Dto.DocumentConfigDTO;
import com.transport.tms.Configuration.Document.Entity.DocumentConfig;
import com.transport.tms.Configuration.Document.Repository.DocumentConfigRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class DocumentConfigServiceImpl
        implements DocumentConfigService {



    private final DocumentConfigRepository repository;



    @Override
    public DocumentConfigDTO create(
            DocumentConfigDTO dto) {


        DocumentConfig entity =
                new DocumentConfig();


        entity.setDocumentName(
                dto.getDocumentName()
        );


        entity.setDocumentType(
                dto.getDocumentType()
        );


        entity.setDisplayNameEn(
                dto.getDisplayNameEn()
        );


        entity.setDisplayNameFr(
                dto.getDisplayNameFr()
        );


        entity.setColorCode(
                dto.getColorCode()
        );


        entity.setActive(true);


        entity.setCreatedAt(
                LocalDateTime.now()
        );


        DocumentConfig saved =
                repository.save(entity);


        return map(saved);

    }






    @Override
    public List<DocumentConfigDTO> getAll() {


        return repository.findAll()
                .stream()
                .map(this::map)
                .toList();

    }






    @Override
    public DocumentConfigDTO getById(
            UUID id) {


        DocumentConfig entity =
                repository.findById(id)
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Document not found"
                                        )
                        );


        return map(entity);

    }







    @Override
    public DocumentConfigDTO update(
            UUID id,
            DocumentConfigDTO dto) {



        DocumentConfig entity =
                repository.findById(id)
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Document not found"
                                        )
                        );



        entity.setDocumentName(
                dto.getDocumentName()
        );


        entity.setDocumentType(
                dto.getDocumentType()
        );


        entity.setDisplayNameEn(
                dto.getDisplayNameEn()
        );


        entity.setDisplayNameFr(
                dto.getDisplayNameFr()
        );


        entity.setColorCode(
                dto.getColorCode()
        );


        entity.setActive(
                dto.getActive()
        );


        entity.setUpdatedAt(
                LocalDateTime.now()
        );



        DocumentConfig updated =
                repository.save(entity);


        return map(updated);

    }







    @Override
    public void delete(
            UUID id) {


        repository.deleteById(id);

    }








    private DocumentConfigDTO map(
            DocumentConfig entity){



        DocumentConfigDTO dto =
                new DocumentConfigDTO();


        dto.setDocumentId(
                entity.getDocumentId()
        );


        dto.setDocumentName(
                entity.getDocumentName()
        );


        dto.setDocumentType(
                entity.getDocumentType()
        );


        dto.setDisplayNameEn(
                entity.getDisplayNameEn()
        );


        dto.setDisplayNameFr(
                entity.getDisplayNameFr()
        );


        dto.setColorCode(
                entity.getColorCode()
        );


        dto.setActive(
                entity.getActive()
        );


        return dto;

    }


}