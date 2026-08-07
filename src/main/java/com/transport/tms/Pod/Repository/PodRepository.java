package com.transport.tms.Pod.Repository;

import com.transport.tms.Pod.Entity.ProofOfDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PodRepository extends JpaRepository<ProofOfDelivery, UUID> {

    Optional<ProofOfDelivery> findByDocNum(String docNum);

    boolean existsByDocNum(String docNum);
}
