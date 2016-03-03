package org.thelittlebighand.baac.model;

import java.util.Date;

public class SubjectScore {

    private String name;
    private String score;
    private Double weight;
    private Date date;
    private String note;

    public SubjectScore() {
    }

    public SubjectScore(String name, String score, Double weight, Date date, String note) {
        this.name = name;
        this.score = score;
        this.weight = (weight != 0) ? weight : 1;
        this.date = (date != null) ? date : new Date();
        this.note = note;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScore() {
        return score;
    }

    public double getScoreValue() {
        return score != null && score.length() > 0 ? new Double(score.substring(0, 1)) : 0;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isClassified() {
        return !"N".equals(score);
    }
}
