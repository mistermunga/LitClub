package com.litclub.Backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "discussion_prompts")
@Getter @Setter
public class DiscussionPrompt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long promptID;

    @ManyToOne
    @JoinColumn(name = "poster_id", nullable = false)
    private User poster;

    @Column(nullable = false)
    private String prompt;

    @CreationTimestamp
    @Column(name = "posted_at")
    private LocalDateTime postedAt;

}
