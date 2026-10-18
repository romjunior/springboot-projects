package com.estudo.intro_agent.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("helpdesk_tickets")
public class HelpDeskTicket {

    @Id
    private Long id;
    private String username;
    private String issue;
    private String status; //e.g. OPEN, IN_PROGRESS, CLOSED
    private LocalDateTime createdAt;
    private LocalDateTime eta;

    public HelpDeskTicket() {
    }

    public HelpDeskTicket(Long id, String username, String issue, String status, LocalDateTime createdAt, LocalDateTime eta) {
        this.id = id;
        this.username = username;
        this.issue = issue;
        this.status = status;
        this.createdAt = createdAt;
        this.eta = eta;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getEta() {
        return eta;
    }

    public void setEta(LocalDateTime eta) {
        this.eta = eta;
    }
}
