# pitest-git-changes-plugin

## Usage

Add plugin to `pitest` dependency:

```
pitest("com.github.mikesafonov:pitest-git-changes-plugin:0.0.4")
```

### Analyze uncommitted changes

To analyze uncommitted changes just enable `git-changes` feature:

```
features.set(listOf("+git-changes"))
```

### Analyze changes between two branches

To analyze changes between two branches enable `git-changes` feature and pass `source` and `target` parameters:

```
features.set(listOf("+git-changes(source[my-branch] target[master])"))
```

## Mutation report using Github Actions

### Configure actions

Give `write` permissions to `pull-requests` and `checks`

```yaml
...
permissions: 
  contents: read
  pull-requests: write
  checks: write
...
```

Configure `actions/checkout` to checkout not only latest commit:

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
      with:
        fetch-depth: 2
...
```

Configure `pitest` run and pass `GITHUB_TOKEN`:

```yaml
...
    - name: Run pitest
      uses: gradle/gradle-build-action@67421db6bd0bf253fb4bd25b31ebb98943c375e1
      if: github.event_name == 'pull_request'
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
      with:
        arguments: pitest
```

### Configure project

Setup `pitest-git-changes-plugin` and `pitest-git-changes-report-github-plugin` plugins

```
dependecies {
    pitest("com.github.mikesafonov:pitest-git-changes-plugin:0.0.4")
    pitest("com.github.mikesafonov:pitest-git-changes-report-github-plugin:0.0.4")
}

pitest {
    ...
    features.set(listOf("+git-changes(target[HEAD^])"))
    outputFormats.set(listOf("GITHUB"))
    if(System.getenv("CI").toBoolean()) {
        pluginConfiguration.set(
                mapOf(
                        "PROJECT_NAME" to "test-project",
                        "GITHUB_TOKEN" to System.getenv("GITHUB_TOKEN"),
                        "GITHUB_REPOSITORY_ID" to System.getenv("GITHUB_REPOSITORY_ID"),
                        "GITHUB_EVENT_PATH" to System.getenv("GITHUB_EVENT_PATH"),
                        "GITHUB_MUTANT_LEVEL" to "WARNING",
                        "GITHUB_FAIL_IF_MUTANTS_PRESENT" to "false"
                )
        )
    }
}
```

Possible values of `GITHUB_MUTANT_LEVEL` :

- NOTICE
- WARNING
- FAILURE (default value)

### Example

[pitest-git-changes-plugin-example](https://github.com/MikeSafonov/pitest-git-changes-plugin-example) and [PR](https://github.com/MikeSafonov/pitest-git-changes-plugin-example/pull/1)

## Mutation report using Gitlab

### Create API token

Create personal or organization token with `api` permission and store to CI (`Settings`->`CI/CD`->`Variables`)
(for example as `GITLAB_TOKEN`)

### Configure .gitlab-ci.yml

```
pitest:
  stage: test
  script: gradle pitest
```

### Configure project

Setup `pitest-git-changes-plugin` and `pitest-git-changes-report-gitlab-plugin` plugins

```
dependecies {
    pitest("com.github.mikesafonov:pitest-git-changes-plugin:0.0.4")
    pitest("com.github.mikesafonov:pitest-git-changes-report-gitlab-plugin:0.0.4")
}

pitest {
    ...
    features.set(listOf("+git-changes(target[HEAD^])"))
    outputFormats.set(listOf("GITLAB"))
    if(System.getenv("CI").toBoolean()) {
        pluginConfiguration.set(
                mapOf(
                        "PROJECT_NAME" to "test-project",
                        "GITLAB_TOKEN" to System.getenv("GITLAB_TOKEN"),
                        "GITLAB_PROJECT_ID" to System.getenv("CI_PROJECT_ID"),
                        "GITLAB_URL" to System.getenv("CI_SERVER_URL"),
                        "GITLAB_MR_ID" to System.getenv("CI_OPEN_MERGE_REQUESTS")
                )
        )
    }
}
```

### Example

[pitest-changes-example](https://gitlab.com/msafonovmail/pitest-changes-example) and [MR](https://gitlab.com/msafonovmail/pitest-changes-example/-/merge_requests/4)