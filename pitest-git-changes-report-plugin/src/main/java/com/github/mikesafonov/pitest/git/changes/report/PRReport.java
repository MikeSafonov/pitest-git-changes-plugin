package com.github.mikesafonov.pitest.git.changes.report;

import lombok.Value;
import org.pitest.classinfo.ClassName;

import java.util.List;
import java.util.Map;

@Value
public class PRReport {
    private int total;
    private int killed;
    private int survived;

    private Map<ClassName, List<PRMutant>> mutants;
}
