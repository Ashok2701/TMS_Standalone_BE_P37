package com.transport.tms.UserManagement.Repository;

import com.transport.tms.UserManagement.Entity.XRUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface XRUserRepository
        extends JpaRepository<XRUser, UUID> {

    Optional<XRUser> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("""
    SELECT u
    FROM XRUser u
    LEFT JOIN FETCH u.role
    LEFT JOIN FETCH u.userType
""")
    List<XRUser> findAllWithDetails();
}
