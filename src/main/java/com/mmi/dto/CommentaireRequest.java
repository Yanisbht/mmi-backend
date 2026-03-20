package com.mmi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentaireRequest {
    @NotBlank
    private String titre;
    @NotBlank
    private String contenu;
}