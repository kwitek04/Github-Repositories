package pl.githubrepositories;

import java.util.List;

public record GithubRepositoryResponseDto(String repositoryName, String ownerLogin, List<GithubBranchResponseDto> branches) {
}
