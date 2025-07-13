package com.example.medicalreportstructurizer.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DeepSeekRequest {
    private String model;
    private List<Message> messages;
    private Double temperature;

    @Data
    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public DeepSeekRequest(String model, List<Message> messages, Double temperature) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
    }
}