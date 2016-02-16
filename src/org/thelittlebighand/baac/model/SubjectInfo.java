package org.thelittlebighand.baac.model;

public class SubjectInfo {

    private String subject;
    private String message;
    private Double avg;

    public SubjectInfo() {
    }

    public SubjectInfo(String subject, String message, Double avg) {
        this.subject = subject;
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

    public Double getAvg() {
        return avg;
    }

    public void setAvg(Double avg) {
        this.avg = avg;
    }
}

