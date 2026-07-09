package com.transport.tms.Blob;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "xr_blob", schema = "tms",
       uniqueConstraints = @UniqueConstraint(columnNames = {"entity_type","entity_code","blob_type"}))
@Getter
@Setter
public class XrBlob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;   // VEHICLE | DRIVER | SITE

    @Column(name = "entity_code", nullable = false, length = 50)
    private String entityCode;   // vehicle_code / driver_id / site_code

    @Column(name = "blob_type", nullable = false, length = 20)
    private String blobType = "IMG1";  // IMG1=primary image

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "content_type", length = 100)
    private String contentType;  // image/jpeg, image/png

    @Column(name = "blob_data", nullable = false)
    private byte[] blobData;     // raw binary — equivalent to X3 BLOB_0

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;
}
