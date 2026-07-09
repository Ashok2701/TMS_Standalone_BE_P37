-- ============================================================
-- TABLE: tms.xr_blob
-- PURPOSE: Binary blob storage for vehicle/driver images
--          Same concept as X3's CBLOB table
-- ============================================================
CREATE TABLE IF NOT EXISTS tms.xr_blob (
    id              BIGSERIAL       PRIMARY KEY,
    entity_type     VARCHAR(50)     NOT NULL,   -- 'VEHICLE' | 'DRIVER' | 'SITE'
    entity_code     VARCHAR(50)     NOT NULL,   -- vehicle_code / driver_id / site_code
    blob_type       VARCHAR(20)     NOT NULL DEFAULT 'IMG1',  -- IMG1, IMG2, DOC1 etc.
    file_name       VARCHAR(255)    NULL,
    content_type    VARCHAR(100)    NULL,        -- image/jpeg, image/png etc.
    blob_data       BYTEA           NOT NULL,    -- binary image data
    file_size       BIGINT          NULL,
    created_at      TIMESTAMP       DEFAULT NOW(),
    updated_at      TIMESTAMP       DEFAULT NOW(),
    created_by      VARCHAR(50)     NULL,
    updated_by      VARCHAR(50)     NULL,

    CONSTRAINT uq_xr_blob_entity UNIQUE (entity_type, entity_code, blob_type)
);

CREATE INDEX IF NOT EXISTS idx_xr_blob_entity
    ON tms.xr_blob (entity_type, entity_code);

COMMENT ON TABLE tms.xr_blob IS 'Binary blob storage — mirrors X3 CBLOB pattern';
COMMENT ON COLUMN tms.xr_blob.entity_type IS 'VEHICLE | DRIVER | SITE';
COMMENT ON COLUMN tms.xr_blob.entity_code IS 'PK of the parent entity';
COMMENT ON COLUMN tms.xr_blob.blob_type   IS 'IMG1=primary image, IMG2=secondary, DOC1=document';
COMMENT ON COLUMN tms.xr_blob.blob_data   IS 'Raw binary — BYTEA equivalent of X3 BLOB_0';
