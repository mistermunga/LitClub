package com.litclub.Backend.entity;

import com.litclub.Backend.security.roles.GlobalRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter @Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userID;

    @Column(nullable = false, unique = true)
    private String username;

    @Column
    private String passwordHash;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "second_name")
    private String secondName;

    @Column
    private String email;

    @ElementCollection(targetClass = GlobalRole.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_global_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<GlobalRole> globalRoles = new HashSet<>();

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<ClubMembership> memberships = new HashSet<>();

    public User () {}

    public User (
            String username,
            String firstName,
            String surname,
            String email,
            boolean isAdmin
    ) {

        this.username = username;
        this.firstName = firstName;
        this.secondName = surname;
        this.email = email;
        this.globalRoles.add(GlobalRole.USER);

        if (isAdmin) {this.globalRoles.add(GlobalRole.ADMINISTRATOR);}

    }
}
