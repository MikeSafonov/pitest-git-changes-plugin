# pitest-git-changes-plugin

## Usage

Add plugin to `pitest` dependency:

```
pitest("com.github.mikesafonov:pitest-git-changes-plugin:0.0.1)
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