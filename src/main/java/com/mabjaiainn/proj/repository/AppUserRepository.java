package com.mabjaiainn.proj.repository;

import com.mabjaiainn.proj.DTO.AuthUserDetails;
import com.mabjaiainn.proj.entity.AppUser;
import com.mabjaiainn.proj.entity.Role;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    @Query("select new com.mabjaiainn.proj.DTO.AuthUserDetails("
            + "u.username, u.passwordHash, u.displayName, u.role, c.clientId, u.enabled, u.mustChangePassword) "
            + "from AppUser u left join u.client c where u.username = :username")
    Optional<AuthUserDetails> findAuthDetailsByUsername(@Param("username") String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndEnabledTrue(String username);

    boolean existsByRole(Role role);

    boolean existsByClient_ClientId(Long clientId);

    Optional<AppUser> findByClient_ClientId(Long clientId);

    List<AppUser> findAllByClient_ClientIdIn(List<Long> clientIds);

    @Query("select u.client.clientId from AppUser u where u.client is not null")
    List<Long> findClientIdsWithAccounts();

    void deleteByClient_ClientId(Long clientId);
}
