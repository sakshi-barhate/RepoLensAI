package com.repolens.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "repositories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Repository {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String repoName;

    @Column(nullable = false, unique = true)
    private String repoUrl;

    private String owner;

    private String primaryLanguage;

    private String framework;

    private Integer stars;

    private Integer forks;

    private LocalDateTime analyzedAt;
}