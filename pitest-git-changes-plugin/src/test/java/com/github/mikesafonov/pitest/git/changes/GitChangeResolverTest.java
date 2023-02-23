package com.github.mikesafonov.pitest.git.changes;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class GitChangeResolverTest {
    @TempDir
    private File repository;
    private final GitChangeResolver resolver = new GitChangeResolver();

    @BeforeEach
    public void setUpRepository() {
        try (TestGit git = new TestGit(repository)) {
            System.out.println(repository.getPath());
            copyResource("/MyClass_master", "MyClass");
            copyResource("/DeletedClass_master", "DeletedClass");
            git.commit("v1");
            git.checkout("change", true);

            copyResource("/MyClass_change", "MyClass");
            copyResource("/NewClass_change", "NewClass");
            removeFile("DeletedClass");
            git.commit("v2");
            copyResource("/MyClass_change2", "MyClass");
            git.commit("v3");
        }
        System.out.println("READY");
    }

    @SneakyThrows
    private void copyResource(String name, String repoFileName) {
        Files.copy(
                new File(GitChangeResolverTest.class.getResource(name).getPath()).toPath(),
                repository.toPath().resolve(repoFileName),
                StandardCopyOption.REPLACE_EXISTING
        );
    }

    @SneakyThrows
    private void removeFile(String name) {
        Files.delete(
                repository.toPath().resolve(name)
        );
    }

    @Test
    void shouldReturnExpectedChanges() {
        List<GitChange> changes = resolver.resolve(repository.getPath() + "/.git", "change", "master").collect(Collectors.toList());
        assertFalse(changes.isEmpty());

        List<GitChange> expected = new ArrayList<>();
        expected.add(new GitChange("MyClass", 4, 6));
        expected.add(new GitChange("MyClass", 8, 8));
        expected.add(new GitChange("NewClass", 1, 6));

        for (GitChange codeChange : changes) {
            assertTrue(expected.contains(codeChange));
        }
    }
}
