package com.transport.tms.Fleet.Repository;
import com.transport.tms.Fleet.Entity.Trailer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrailerRepository extends CrudRepository<Trailer, Long> {
    List<Trailer> findAll();
    Trailer findByTrailer(String trailer);
    void deleteByTrailer(String trailer);
    boolean existsByTrailer(String trailer);
}
