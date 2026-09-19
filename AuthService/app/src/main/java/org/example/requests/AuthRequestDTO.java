package org.example.requests;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequestDTO {
    @JsonAlias({"user_name", "userName"})
    private String username;
    private String password;
}
