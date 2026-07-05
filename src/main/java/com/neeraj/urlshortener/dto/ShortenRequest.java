package com.neeraj.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortenRequest {

    @NotBlank(message = "URL must not be blank")
    @URL(message = "Must be a valid URL (include http:// or https://)")
    private String url;

    /**
     * Optional custom alias (alphanumeric + hyphens, 3-20 chars).
     * If omitted, a random code is generated.
     */
    @Pattern(regexp = "^[a-zA-Z0-9-]{3,20}$",
             message = "Custom alias must be 3-20 alphanumeric characters or hyphens")
    private String customAlias;
}
