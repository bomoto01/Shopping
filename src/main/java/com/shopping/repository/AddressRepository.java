package com.shopping.repository;

import com.shopping.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUser_UserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);

    Optional<Address> findByAddressIdAndUser_UserId(Long addressId, Long userId);

    Optional<Address> findByUser_UserIdAndIsDefaultTrue(Long userId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.userId = :userId")
    void resetDefaultAddressForUser(@Param("userId") Long userId);

    long countByUser_UserId(Long userId);
}
