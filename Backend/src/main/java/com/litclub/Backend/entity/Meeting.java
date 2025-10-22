package com.litclub.Backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "meetings")
@Getter @Setter
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long meetingID;

    @ManyToOne
    @JoinColumn(name = "club_id")
    private Club club;

    @Column(name = "title")
    private String title;

    @Column
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column
    private String location;

    @Column
    private String link;

    @ManyToOne
    @JoinColumn
    private User creator;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
