package com.repolens.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HealthScore {

    private int score;

    private String grade;

    private boolean readme;

    private boolean license;

    private boolean gitignore;

    private boolean docker;

    private boolean dockerCompose;

    private boolean githubActions;

    private boolean tests;

    private boolean buildFile;
}