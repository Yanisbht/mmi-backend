package com.mmi.dto;

import com.mmi.enums.Matiere;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjetRequest {
    @NotBlank
    private String titre;
    private String description;
    private Matiere matiere;
    private String urlMedia;
}