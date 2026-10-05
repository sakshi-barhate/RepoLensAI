package com.repolens.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "repository_id")
    private Repository repository;

    @Lob
    private String projectSummary;

    @Lob
    private String architectureSummary;

    @Lob
    private String dependencyAnalysis;

    @Lob
    private String apiAnalysis;

    @Lob
    private String databaseAnalysis;

    private LocalDateTime analyzedAt;
}