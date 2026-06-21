# GitHub Repositories Proxy

Task solution: API that lists non-fork GitHub repositories for a given user.

## Stack

- Java 25
- Spring Boot 4
- Gradle (Kotlin DSL)
- WireMock

## What this API does

For a given GitHub username, endpoint returns repositories that are not forks with:

- repository name
- owner login
- branch name
- last commit SHA for each branch

## Run

```bash
./gradlew bootRun
```

On Windows:

```bash
.\gradlew.bat bootRun
```

## Endpoint

`GET /api/v1/github/{username}/repositories`

### How to send a request

Once the application is running (by default on port 8080), you can test it using cURL:

```bash
curl.exe -v http://localhost:8080/api/v1/github/kwitek04/repositories
```

### 200 OK example

```json
[
  {
    "repositoryName": "repo-one",
    "ownerLogin": "octocat",
    "branches": [
      {
        "name": "main",
        "lastCommitSha": "abc123"
      }
    ]
  }
]
```

### 404 example (GitHub user does not exist)

```json
{
  "status": 404,
  "message": "GitHub user 'missing-user' not found"
}
```

## Test

```bash
./gradlew test
```

On Windows:

```bash
.\gradlew.bat test
```

