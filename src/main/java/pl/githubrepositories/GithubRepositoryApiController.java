package pl.githubrepositories;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/github")
class GithubRepositoryApiController {

    private final GithubRepositoryService githubRepositoryService;

    GithubRepositoryApiController(GithubRepositoryService githubRepositoryService) {
        this.githubRepositoryService = githubRepositoryService;
    }

    @GetMapping("/{username}/repositories")
    List<GithubRepositoryResponseDto> listUserRepositories(@PathVariable String username) {
        return githubRepositoryService.listNotForkRepositoriesWithBranches(username).stream()
                .map(repository -> new GithubRepositoryResponseDto(
                        repository.repositoryName(),
                        repository.ownerLogin(),
                        repository.branches().stream()
                                .map(branch -> new GithubBranchResponseDto(branch.name(), branch.lastCommitSha()))
                                .toList()
                ))
                .toList();
    }
}
