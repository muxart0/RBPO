package ru.mtuci.demo.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class RegRequest {
    private String login;
    private String name;
    private String password;
}