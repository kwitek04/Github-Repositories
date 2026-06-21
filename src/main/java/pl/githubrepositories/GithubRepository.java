package pl.githubrepositories;

import java.util.List;

record GithubRepository(String repositoryName, String ownerLogin, List<GithubBranch> branches) {
}
