package com.transport.tms.Blob;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Binary image storage — mirrors X3's CBLOB pattern
 *
 * Upload:  POST /api/v1/blob/{entityType}/{entityCode}/image
 * Fetch:   GET  /api/v1/blob/{entityType}/{entityCode}/image
 * Delete:  DELETE /api/v1/blob/{entityType}/{entityCode}/image
 *
 * entityType: VEHICLE | DRIVER | SITE
 * entityCode: vehicle_code | driver_id | site_code
 */
@RestController
@RequestMapping("/api/v1/blob")
@RequiredArgsConstructor
public class BlobController {

    private final XrBlobRepository repo;

    // ── UPLOAD image ──────────────────────────────────────────
    @PostMapping(value = "/{entityType}/{entityCode}/image",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(
            @PathVariable String entityType,
            @PathVariable String entityCode,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "blobType", defaultValue = "IMG1") String blobType,
            @RequestParam(value = "userCode", defaultValue = "SYSTEM") String userCode) {

        try {
            String type = entityType.toUpperCase();

            // Upsert — find existing or create new
            XrBlob blob = repo
                    .findByEntityTypeAndEntityCodeAndBlobType(type, entityCode, blobType)
                    .orElse(new XrBlob());

            blob.setEntityType(type);
            blob.setEntityCode(entityCode);
            blob.setBlobType(blobType);
            blob.setFileName(file.getOriginalFilename());
            blob.setContentType(file.getContentType());
            blob.setBlobData(file.getBytes());
            blob.setFileSize(file.getSize());
            blob.setUpdatedAt(LocalDateTime.now());
            blob.setUpdatedBy(userCode);

            if (blob.getId() == null) {
                blob.setCreatedAt(LocalDateTime.now());
                blob.setCreatedBy(userCode);
            }

            repo.save(blob);

            return ResponseEntity.ok(Map.of(
                "message",    "Image uploaded successfully",
                "entityType", type,
                "entityCode", entityCode,
                "blobType",   blobType,
                "fileName",   file.getOriginalFilename(),
                "fileSize",   file.getSize(),
                "contentType", file.getContentType()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ── FETCH image as binary ─────────────────────────────────
    @GetMapping("/{entityType}/{entityCode}/image")
    public ResponseEntity<byte[]> getImage(
            @PathVariable String entityType,
            @PathVariable String entityCode,
            @RequestParam(value = "blobType", defaultValue = "IMG1") String blobType) {

        return repo.findByEntityTypeAndEntityCodeAndBlobType(
                        entityType.toUpperCase(), entityCode, blobType)
                .map(blob -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE,
                                blob.getContentType() != null
                                        ? blob.getContentType()
                                        : MediaType.IMAGE_JPEG_VALUE)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "inline; filename=\"" + blob.getFileName() + "\"")
                        .body(blob.getBlobData()))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── CHECK if image exists ─────────────────────────────────
    @GetMapping("/{entityType}/{entityCode}/image/exists")
    public ResponseEntity<?> imageExists(
            @PathVariable String entityType,
            @PathVariable String entityCode,
            @RequestParam(value = "blobType", defaultValue = "IMG1") String blobType) {

        boolean exists = repo
                .findByEntityTypeAndEntityCodeAndBlobType(
                        entityType.toUpperCase(), entityCode, blobType)
                .isPresent();

        return ResponseEntity.ok(Map.of("exists", exists));
    }

    // ── DELETE image ──────────────────────────────────────────
    @DeleteMapping("/{entityType}/{entityCode}/image")
    public ResponseEntity<?> deleteImage(
            @PathVariable String entityType,
            @PathVariable String entityCode) {

        repo.deleteByEntityTypeAndEntityCode(entityType.toUpperCase(), entityCode);
        return ResponseEntity.ok(Map.of("message", "Image deleted"));
    }
}
