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

    // ── CREATE ────────────────────────────────────────────────
    @Override
    public DocumentConfigDTO create(DocumentConfigDTO dto) {

        validateColorCode(dto.getColorCode());

        DocumentConfig entity = new DocumentConfig();

        entity.setDocumentName(dto.getDocumentName());
        entity.setDocumentType(dto.getDocumentType());
        entity.setDisplayNameEn(dto.getDisplayNameEn());
        entity.setDisplayNameFr(dto.getDisplayNameFr());
        entity.setColorCode(normalizeColorCode(dto.getColorCode()));
        entity.setActive(true);
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedBy(dto.getCreatedBy());
        entity.setUpdatedAt(LocalDateTime.now());

        return map(repository.save(entity));
    }

    // ── GET ALL ───────────────────────────────────────────────
    @Override
    public List<DocumentConfigDTO> getAll() {

        return repository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    // ── GET ALL ACTIVE ────────────────────────────────────────
    @Override
    public List<DocumentConfigDTO> getAllActive() {

        return repository.findByActiveTrue()
                .stream()
                .map(this::map)
                .toList();
    }

    // ── GET BY DOCUMENT TYPE ──────────────────────────────────
    @Override
    public List<DocumentConfigDTO> getByDocumentType(String documentType) {

        return repository.findByDocumentType(documentType)
                .stream()
                .map(this::map)
                .toList();
    }

    // ── GET BY ID ─────────────────────────────────────────────
    @Override
    public DocumentConfigDTO getById(UUID id) {

        DocumentConfig entity = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Document config not found: " + id));

        return map(entity);
    }

    // ── UPDATE ────────────────────────────────────────────────
    @Override
    public DocumentConfigDTO update(UUID id, DocumentConfigDTO dto) {

        validateColorCode(dto.getColorCode());

        DocumentConfig entity = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Document config not found: " + id));

        entity.setDocumentName(dto.getDocumentName());
        entity.setDocumentType(dto.getDocumentType());
        entity.setDisplayNameEn(dto.getDisplayNameEn());
        entity.setDisplayNameFr(dto.getDisplayNameFr());
        entity.setColorCode(normalizeColorCode(dto.getColorCode()));
        entity.setActive(dto.getActive() != null ? dto.getActive() : entity.getActive());
        entity.setUpdatedBy(dto.getUpdatedBy());
        entity.setUpdatedAt(LocalDateTime.now());

        return map(repository.save(entity));
    }

    // ── TOGGLE ACTIVE ─────────────────────────────────────────
    @Override
    public DocumentConfigDTO toggleActive(UUID id) {

        DocumentConfig entity = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Document config not found: " + id));

        entity.setActive(!Boolean.TRUE.equals(entity.getActive()));
        entity.setUpdatedAt(LocalDateTime.now());

        return map(repository.save(entity));
    }

    // ── DELETE ────────────────────────────────────────────────
    @Override
    public void delete(UUID id) {

        if (!repository.existsById(id)) {
            throw new RuntimeException("Document config not found: " + id);
        }

        repository.deleteById(id);
    }

    // ── COLOR CODE VALIDATION ─────────────────────────────────
    // Accepts: #RGB (3-char) or #RRGGBB (6-char) hex
    // Normalises to uppercase: #6366f1 → #6366F1
    private void validateColorCode(String colorCode) {

        if (colorCode == null || colorCode.isBlank()) {
            return; // optional field
        }

        if (!colorCode.matches("^#([0-9A-Fa-f]{3}|[0-9A-Fa-f]{6})$")) {
            throw new RuntimeException(
                    "Invalid color code '" + colorCode
                            + "'. Must be in #RGB or #RRGGBB hex format e.g. #6366F1");
        }
    }

    private String normalizeColorCode(String colorCode) {

        if (colorCode == null || colorCode.isBlank()) {
            return colorCode;
        }
        return colorCode.toUpperCase();
    }

    // ── MAPPER ────────────────────────────────────────────────
    private DocumentConfigDTO map(DocumentConfig entity) {

        DocumentConfigDTO dto = new DocumentConfigDTO();

        dto.setDocumentId(entity.getDocumentId());
        dto.setDocumentName(entity.getDocumentName());
        dto.setDocumentType(entity.getDocumentType());
        dto.setDisplayNameEn(entity.getDisplayNameEn());
        dto.setDisplayNameFr(entity.getDisplayNameFr());
        dto.setColorCode(entity.getColorCode());
        dto.setActive(entity.getActive());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }
}
