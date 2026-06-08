package com.transport.tms.UserManagement.Repository;

import com.transport.tms.UserManagement.Entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
    @Query("SELECT u FROM User u")
    public List<User> findAll();
    public User findByXloginAndXpswd(String userName, String password);
    public Optional<User> findByXlogin(String xlogin);
}
