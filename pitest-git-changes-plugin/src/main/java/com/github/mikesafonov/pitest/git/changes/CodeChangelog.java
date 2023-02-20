package com.github.mikesafonov.pitest.git.changes;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class CodeChangelog implements Iterable<CodeChange> {
    private final List<CodeChange> changes;

    public CodeChangelog(List<CodeChange> changes) {
        this.changes = new ArrayList<>(Objects.requireNonNull(changes));
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean isNotEmpty() {
        return size() > 0;
    }

    public int size() {
        return changes.size();
    }

    public boolean contains(String clazz, int line) {
        return changes.stream()
                .anyMatch(change -> change.getClassName().equals(clazz) && change.getLineFrom() >= line && change.getLineTo() <= line);
    }

    @Override
    public Iterator<CodeChange> iterator() {
        return changes.iterator();
    }
}
