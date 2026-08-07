package com.transport.tms.Pod.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transport.tms.Fleet.Entity.Driver;
import com.transport.tms.Pod.Dto.*;
import com.transport.tms.Pod.Entity.ProofOfDelivery;
import com.transport.tms.Pod.Repository.PodRepository;
import com.transport.tms.Pod.Service.CurrentDriver;
import com.transport.tms.Pod.Service.PodService;
import com.transport.tms.Trip.Entity.XrTrip;
import com.transport.tms.Trip.Repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PodServiceImpl implements PodService {

    private static final Set<String> VALID_STATUSES = Set.of("DELIVERED", "PARTIAL", "FAILED", "REFUSED");

    private final TripRepository tripRepository;
    private final PodRepository podRepository;
    private final CurrentDriver currentDriver;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PodTripSummaryDTO> getMyTrips(LocalDate date) {
        Driver driver = currentDriver.require();
        LocalDate effectiveDate = date != null ? date : LocalDate.now();

        return tripRepository.findByDriverIdAndDocDateOrderByStartTimeAsc(driver.getDriverId(), effectiveDate)
                .stream()
                .map(this::toTripSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PodStopSummaryDTO> getTripStops(String tripCode) {
        Driver driver = currentDriver.require();
        XrTrip trip = tripRepository.findByTripCode(tripCode)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + tripCode));

        requireOwnership(driver, trip);

        return parseStops(trip.getStopObjectsJson()).stream()
                .map(this::toStopSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PodStopDetailDTO getStopDetail(String docNum) {
        Driver driver = currentDriver.require();
        Map<String, Object> stop = findStopOrThrow(driver, docNum);
        return toStopDetail(stop);
    }

    @Override
    public PodResponseDTO completeStop(String docNum, PodCompleteRequestDTO req) {
        Driver driver = currentDriver.require();
        Map<String, Object> stop = findStopOrThrow(driver, docNum);
        String tripCode = String.valueOf(stop.get("_tripCode"));

        if (req.getStatus() == null || !VALID_STATUSES.contains(req.getStatus().toUpperCase())) {
            throw new RuntimeException("status must be one of " + VALID_STATUSES);
        }
        String status = req.getStatus().toUpperCase();
        boolean needsReason = status.equals("FAILED") || status.equals("REFUSED");
        if (needsReason && (req.getFailureReason() == null || req.getFailureReason().isBlank())) {
            throw new RuntimeException("failureReason is required when status is " + status);
        }

        ProofOfDelivery pod = podRepository.findByDocNum(docNum).orElseGet(ProofOfDelivery::new);
        pod.setDocNum(docNum);
        pod.setTripCode(tripCode);
        pod.setDriverId(driver.getDriverId());
        pod.setStatus(status);
        pod.setRecipientName(req.getRecipientName());
        pod.setRecipientRelation(req.getRecipientRelation());
        pod.setSignature(req.getSignatureBase64());
        pod.setPhotosJson(toJson(req.getPhotosBase64()));
        pod.setRemarks(req.getRemarks());
        pod.setFailureReason(needsReason ? req.getFailureReason() : null);
        pod.setLatitude(req.getLatitude());
        pod.setLongitude(req.getLongitude());
        pod.setDeliveredAt(LocalDateTime.now());

        return toResponseDTO(podRepository.save(pod));
    }

    @Override
    @Transactional(readOnly = true)
    public PodResponseDTO getPod(String docNum) {
        Driver driver = currentDriver.require();
        // Ownership check — throws if this docNum isn't on any trip
        // assigned to this driver, same as the other endpoints.
        findStopOrThrow(driver, docNum);

        return podRepository.findByDocNum(docNum)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new RuntimeException("No POD submitted yet for " + docNum));
    }

    // ── Helpers ──────────────────────────────────────────

    private void requireOwnership(Driver driver, XrTrip trip) {
        if (trip.getDriverId() == null || !trip.getDriverId().equals(driver.getDriverId())) {
            throw new RuntimeException("This trip is not assigned to you");
        }
    }

    /** Finds the stop with this docNum across the driver's own trips,
     *  throwing if it doesn't exist or belongs to a trip that isn't
     *  theirs — this is the actual per-driver data scoping described in
     *  CurrentDriver's javadoc: nothing here trusts a driverId from the
     *  request, only from the validated token. */
    private Map<String, Object> findStopOrThrow(Driver driver, String docNum) {
        for (XrTrip trip : tripRepository.findByDriverId(driver.getDriverId())) {
            for (Map<String, Object> stop : parseStops(trip.getStopObjectsJson())) {
                String id = str(stop.get("id"));
                String txn = str(stop.get("txn"));
                if (docNum.equals(id) || docNum.equals(txn)) {
                    stop.put("_tripCode", trip.getTripCode());
                    return stop;
                }
            }
        }
        throw new RuntimeException("Document " + docNum + " was not found on any of your trips");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseStops(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        } catch (Exception e) {
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private PodTripSummaryDTO toTripSummary(XrTrip t) {
        return PodTripSummaryDTO.builder()
                .tripCode(t.getTripCode())
                .site(t.getSite())
                .vehicleCode(t.getVehicleCode())
                .driverName(t.getDriverName())
                .status(t.getOptiStatus())
                .depSite(t.getDepSite())
                .arrSite(t.getArrSite())
                .startTime(t.getStartTime())
                .stops(t.getStops())
                .drops(t.getDrops())
                .pickups(t.getPickups())
                .build();
    }

    private PodStopSummaryDTO toStopSummary(Map<String, Object> s) {
        String docNum = str(s.get("id")) != null ? str(s.get("id")) : str(s.get("txn"));
        return PodStopSummaryDTO.builder()
                .docNum(docNum)
                .seq(num(s.get("seq")) != null ? num(s.get("seq")).intValue() : null)
                .type(str(s.get("type")))
                .docType(str(s.get("doctype")))
                .clientName(str(s.get("client")))
                .bpCode(str(s.get("bpcode")))
                .address(str(s.get("address")))
                .city(str(s.get("city")))
                .postalCity(str(s.get("postalCity")))
                .qty(num(s.get("qty")))
                .weight(num(s.get("netweight")))
                .weightUnit(str(s.get("weightUnit")))
                .status(podStatusFor(docNum))
                .build();
    }

    @SuppressWarnings("unchecked")
    private PodStopDetailDTO toStopDetail(Map<String, Object> s) {
        String docNum = str(s.get("id")) != null ? str(s.get("id")) : str(s.get("txn"));
        List<Map<String, Object>> rawProducts = (List<Map<String, Object>>) s.getOrDefault("products", List.of());
        List<PodProductDTO> products = rawProducts == null ? List.of() : rawProducts.stream()
                .map(p -> PodProductDTO.builder()
                        .itemCode(str(p.get("itemCode")))
                        .description(String.join(" ", nonNull(str(p.get("itemDesc1"))), nonNull(str(p.get("itemDesc2")))).trim())
                        .qtyOrdered(num(p.get("qtyOrdered")))
                        .netWeight(num(p.get("netWeight")))
                        .weightUnit(str(p.get("weightUnit")))
                        .volume(num(p.get("volume")))
                        .volumeUnit(str(p.get("volumeUnit")))
                        .build())
                .toList();

        return PodStopDetailDTO.builder()
                .docNum(docNum)
                .seq(num(s.get("seq")) != null ? num(s.get("seq")).intValue() : null)
                .type(str(s.get("type")))
                .docType(str(s.get("doctype")))
                .clientName(str(s.get("client")))
                .bpCode(str(s.get("bpcode")))
                .address(str(s.get("address")))
                .city(str(s.get("city")))
                .postalCity(str(s.get("postalCity")))
                .arrivalTime(str(s.get("arrivalTime")))
                .departureTime(str(s.get("departureTime")))
                .qty(num(s.get("qty")))
                .weight(num(s.get("netweight")))
                .weightUnit(str(s.get("weightUnit")))
                .status(podStatusFor(docNum))
                .products(products)
                .build();
    }

    private String podStatusFor(String docNum) {
        return podRepository.findByDocNum(docNum).map(ProofOfDelivery::getStatus).orElse("PENDING");
    }

    @SuppressWarnings("unchecked")
    private PodResponseDTO toResponseDTO(ProofOfDelivery pod) {
        List<String> photos;
        try {
            photos = pod.getPhotosJson() == null ? List.of()
                    : objectMapper.readValue(pod.getPhotosJson(), List.class);
        } catch (Exception e) {
            photos = List.of();
        }
        return PodResponseDTO.builder()
                .podId(pod.getPodId() != null ? pod.getPodId().toString() : null)
                .docNum(pod.getDocNum())
                .tripCode(pod.getTripCode())
                .driverId(pod.getDriverId())
                .status(pod.getStatus())
                .recipientName(pod.getRecipientName())
                .recipientRelation(pod.getRecipientRelation())
                .signatureBase64(pod.getSignature())
                .photosBase64(photos)
                .remarks(pod.getRemarks())
                .failureReason(pod.getFailureReason())
                .latitude(pod.getLatitude())
                .longitude(pod.getLongitude())
                .deliveredAt(pod.getDeliveredAt())
                .build();
    }

    private String toJson(Object o) {
        try { return objectMapper.writeValueAsString(o == null ? List.of() : o); }
        catch (Exception e) { return "[]"; }
    }

    private String str(Object o) { return o == null ? null : String.valueOf(o); }
    private String nonNull(String s) { return s == null ? "" : s; }
    private Double num(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(o)); } catch (Exception e) { return null; }
    }
}
