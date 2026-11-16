package com.shopping.entity;

import com.shopping.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_code", nullable = false, unique = true, length = 30)
    private RoleType roleCode;

    @Column(name = "role_name", length = 80)
    private String roleName;
}
