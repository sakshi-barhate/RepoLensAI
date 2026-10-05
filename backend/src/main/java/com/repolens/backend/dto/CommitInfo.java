package com.repolens.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommitInfo {

    private String latestCommitMessage;

    private String latestCommitAuthor;

    private String latestCommitDate;

    private String latestCommitSha;
}