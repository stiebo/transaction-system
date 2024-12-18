package dev.stiebo.app.data;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);
    boolean existsByUsernameIgnoreCase(String username);
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
            "LOWER(u.role.name) LIKE LOWER(CONCAT('%', :filter, '%'))")
    List<User> findByNameOrUsernameOrRoleNameContainingIgnoreCase(String filter, Pageable pageable);
    @Query("SELECT COUNT(u) FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
            "LOWER(u.role.name) LIKE LOWER(CONCAT('%', :filter, '%'))")
    long countByNameOrUsernameOrRoleNameContainingIgnoreCase(String filter);

}
