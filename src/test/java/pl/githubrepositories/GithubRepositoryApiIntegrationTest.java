package pl.githubrepositories;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GithubRepositoryApiIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("github.api.base-url", wireMock::baseUrl);
    }

    @LocalServerPort
    private int port;

    @Test
    void shouldReturnOnlyNotForkRepositoriesWithBranches() throws Exception {
        wireMock.stubFor(get(urlEqualTo("/users/octocat/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "name": "repo-one",
                                    "fork": false,
                                    "owner": { "login": "octocat" }
                                  },
                                  {
                                    "name": "repo-fork",
                                    "fork": true,
                                    "owner": { "login": "octocat" }
                                  }
                                ]
                                """)));

        wireMock.stubFor(get(urlEqualTo("/repos/octocat/repo-one/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  { "name": "main", "commit": { "sha": "abc123" } },
                                  { "name": "develop", "commit": { "sha": "def456" } }
                                ]
                                """)));

        HttpResponse<String> response = httpGet("/api/v1/github/octocat/repositories");
        String body = response.body();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(body).contains("\"repositoryName\":\"repo-one\"");
        assertThat(body).contains("\"ownerLogin\":\"octocat\"");
        assertThat(body).contains("\"name\":\"main\"");
        assertThat(body).contains("\"lastCommitSha\":\"abc123\"");
        assertThat(body).doesNotContain("repo-fork");
    }

    @Test
    void shouldReturn404WhenGithubUserDoesNotExist() throws Exception {
        wireMock.stubFor(get(urlEqualTo("/users/missing-user/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                { "message": "Not Found" }
                                """)));

        HttpResponse<String> response = httpGet("/api/v1/github/missing-user/repositories");
        String body = response.body();

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(body).contains("\"status\":404");
        assertThat(body).contains("\"message\":\"GitHub user 'missing-user' not found\"");
    }

    @Test
    void shouldReturnEmptyListWhenUserHasOnlyForkRepositories() throws Exception {
        wireMock.stubFor(get(urlEqualTo("/users/fork-only/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "name": "fork-a",
                                    "fork": true,
                                    "owner": { "login": "fork-only" }
                                  }
                                ]
                                """)));

        HttpResponse<String> response = httpGet("/api/v1/github/fork-only/repositories");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("[]");
    }

    private HttpResponse<String> httpGet(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }
}
