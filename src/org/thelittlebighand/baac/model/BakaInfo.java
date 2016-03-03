package org.thelittlebighand.baac.model;

import java.util.List;

public class BakaInfo {

    private String name;
    private List<SubjectInfo> subjects;

    public BakaInfo() {
    }

    public BakaInfo(String name, List<SubjectInfo> subjects) {
        this.name = name;
        this.subjects = subjects;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SubjectInfo> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<SubjectInfo> subjects) {
        this.subjects = subjects;
    }
}
