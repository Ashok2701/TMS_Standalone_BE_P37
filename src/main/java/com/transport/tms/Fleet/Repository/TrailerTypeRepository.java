package com.transport.tms.Fleet.Repository;



import com.transport.tms.Fleet.Entity.TrailerType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrailerTypeRepository extends CrudRepository<TrailerType, Long> {
    List<TrailerType> findAll();
    TrailerType findByTrailerCode(String trailerCode);
    void deleteByTrailerCode(String trailerCode);
    boolean existsByTrailerCode(String trailerCode);
}
