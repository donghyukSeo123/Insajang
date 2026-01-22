package com.project.insajang.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "contents")
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contents_seq")
    @SequenceGenerator(
            name = "contents_seq",
            sequenceName = "contents_content_id_seq",
            allocationSize = 1
    )
    private Long contentId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
}
