package com.mmi.dto;

import com.mmi.enums.Matiere;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProjetRequest {

    @NotBlank
    private String titre;

    private String description;
    private Matiere matiere;

    // ── Compat anciens champs ──────────────────────────────
    private String urlMedia;
    private String thumbnailUrl;
    private String fichierUrl;

    // ── Nouveaux champs ────────────────────────────────────
    private Boolean competition;

    private List<String> fichiersUrls = new ArrayList<>();
    private List<String> liensUrls    = new ArrayList<>();
    private List<Long>   equipeIds    = new ArrayList<>();
}