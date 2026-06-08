package com.transport.tms.Configuration.Document.Controller;

import com.transport.tms.Configuration.Document.Dto.DocumentConfigDTO;
import com.transport.tms.Configuration.Document.Service.DocumentConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/document-config")
@RequiredArgsConstructor
public class DocumentConfigController {

    private final DocumentConfigService service;

    // ── CREATE ────────────────────────────────────────────────
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentConfigDTO create(
            @RequestBody DocumentConfigDTO dto) {

        return service.create(dto);
    }

    // ── GET ALL (active + inactive) ───────────────────────────
    @GetMapping
    public List<DocumentConfigDTO> getAll() {

        return service.getAll();
    }

    // ── GET ALL ACTIVE ONLY ───────────────────────────────────
    @GetMapping("/active")
    public List<DocumentConfigDTO> getAllActive() {

        return service.getAllActive();
    }

    // ── GET BY DOCUMENT TYPE ──────────────────────────────────
    // e.g. GET /api/document-config/by-type/SON
    @GetMapping("/by-type/{documentType}")
    public List<DocumentConfigDTO> getByDocumentType(
            @PathVariable String documentType) {

        return service.getByDocumentType(documentType);
    }

    // ── GET BY ID ─────────────────────────────────────────────
    @GetMapping("/{id}")
    public DocumentConfigDTO getById(
            @PathVariable UUID id) {

        return service.getById(id);
    }

    // ── UPDATE ────────────────────────────────────────────────
    @PutMapping("/{id}")
    public DocumentConfigDTO update(
            @PathVariable UUID id,
            @RequestBody DocumentConfigDTO dto) {

        return service.update(id, dto);
    }

    // ── TOGGLE ACTIVE / INACTIVE ──────────────────────────────
    @PatchMapping("/{id}/toggle-active")
    public DocumentConfigDTO toggleActive(
            @PathVariable UUID id) {

        return service.toggleActive(id);
    }

    // ── DELETE ────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id) {

        service.delete(id);
    }
}
