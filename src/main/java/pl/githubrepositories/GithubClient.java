package pl.githubrepositories;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
class GithubClient {

    private static final ParameterizedTypeReference<List<GithubExternalRepositoryDto>> REPOSITORIES_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<List<GithubExternalBranchDto>> BRANCHES_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient githubRestClient;

    GithubClient(RestClient githubRestClient) {
        this.githubRestClient = githubRestClient;
    }

    List<GithubExternalRepositoryDto> fetchRepositories(String username) {
        try {
            return githubRestClient.get()
                    .uri("/users/{username}/repos", username)
                    .retrieve()
                    .body(REPOSITORIES_TYPE);
        } catch (HttpClientErrorException.NotFound e) {
            throw new GithubUserNotFoundException(username);
        }
    }

    List<GithubExternalBranchDto> fetchBranches(String ownerLogin, String repositoryName) {
        return githubRestClient.get()
                .uri("/repos/{ownerLogin}/{repositoryName}/branches", ownerLogin, repositoryName)
                .retrieve()
                .body(BRANCHES_TYPE);
    }
}
