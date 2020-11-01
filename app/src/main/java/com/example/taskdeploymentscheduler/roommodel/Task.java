package com.example.taskdeploymentscheduler.roommodel;

import androidx.room.Entity;

public class Task {
    String id;
    String leader;
    String member;
    String title;
    String assignment;
    String deadline;
    String status;

    public Task(String id, String leader, String member, String title, String assignment, String deadline, String status) {
        this.id = id;
        this.leader = leader;
        this.member = member;
        this.title = title;
        this.assignment = assignment;
        this.deadline = deadline;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public String getAssignment() {
        return assignment;
    }

    public void setAssignment(String assignment) {
        this.assignment = assignment;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
