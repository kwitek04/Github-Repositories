package pl.githubrepositories;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class GithubRepositoryService {

    private final GithubClient githubClient;

    GithubRepositoryService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    List<GithubRepository> listNotForkRepositoriesWithBranches(String username) {
        return githubClient.fetchRepositories(username).stream()
                .filter(repository -> !repository.fork())
                .map(repository -> new GithubRepository(
                        repository.name(),
                        repository.owner().login(),
                        fetchBranches(repository.owner().login(), repository.name())
                ))
                .toList();
    }

    private List<GithubBranch> fetchBranches(String ownerLogin, String repositoryName) {
        return githubClient.fetchBranches(ownerLogin, repositoryName).stream()
                .map(branch -> new GithubBranch(branch.name(), branch.commit().sha()))
                .toList();
    }
}
