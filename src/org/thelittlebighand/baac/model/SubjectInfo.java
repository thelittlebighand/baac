package org.thelittlebighand.baac.model;

import java.util.List;

public class SubjectInfo {

    private String subject;
    private List<SubjectScore> scores;
    private String message;
    private Double avg;

    public SubjectInfo() {
    }

    public SubjectInfo(String subject, List<SubjectScore> scores, String message) {
        this.subject = subject;
        this.scores = scores;
        this.message = message;

        double sum = 0, sumw = 0;
        for (SubjectScore score : scores) {
            if (score.isClassified()) {
                sum += score.getScoreValue() * score.getWeight();
                sumw += score.getWeight();
            }
        }
        this.avg = sum / (sumw == 0 ? 1 : sumw);
    }

    public SubjectInfo(String subject, List<SubjectScore> scores, String message, double avg) {
        this.subject = subject;
        this.scores = scores;
        this.message = message;
        this.avg = avg;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<SubjectScore> getScores() {
        return scores;
    }

    public void setScores(List<SubjectScore> scores) {
        this.scores = scores;
    }

    public Double getAvg() {
        return avg;
    }

    public void setAvg(Double avg) {
        this.avg = avg;
    }
}

