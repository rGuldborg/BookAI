package com.example.bookai.dto;

public class ChatRequestDTO {

    private String model;
    private Message[] messages;

    public ChatRequestDTO(String model, Message[] messages) {
        this.model = model;
        this.messages = messages;
    }

    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public String getContent() { return content; }
    }

    public String getModel() { return model; }
    public Message[] getMessages() { return messages; }
}
