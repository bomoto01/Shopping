package com.shopping.repository;

import com.shopping.entity.Role;
import com.shopping.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByRoleCode(RoleType roleCode);
}
