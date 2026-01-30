package io.github.raphaelmun1z.ChatGepeteco.dtos;

import java.util.List;

public class UserDTO {
    private String username;
    private String name;
    private List<String> roles;

    public UserDTO() {
    }

    public UserDTO(String username, String name, List<String> roles) {
        this.username = username;
        this.name = name;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
