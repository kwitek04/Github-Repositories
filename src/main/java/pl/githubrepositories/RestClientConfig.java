package pl.githubrepositories;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
class RestClientConfig {

    @Bean
    RestClient githubRestClient(GithubApiProperties githubApiProperties) {
        return RestClient.builder()
                .baseUrl(githubApiProperties.baseUrl())
                .build();
    }
}
