package com.litclub.Backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "clubs")
@Getter @Setter
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "club_id")
    private Long clubID;

    @Column(nullable = false, name = "club_name")
    private String clubName;

    @Column
    private String description;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
