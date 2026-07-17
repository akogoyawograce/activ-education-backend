package tg.edtch.activEducation.datahub.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tg.edtch.activEducation.datahub.domain.dto.DataHubResponse;
import tg.edtch.activEducation.datahub.domain.service.DataHubService;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/datahub")
@PreAuthorize("hasRole('ADMIN')")
public class DataHubController {

    private final DataHubService service;

    public DataHubController(DataHubService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<DataHubResponse> getDataHub() {
        return ResponseEntity.ok(service.getDataHub());
    }

    @GetMapping("/ville/{ville}")
    public ResponseEntity<DataHubResponse> getDataHubParVille(@PathVariable String ville) {
        return ResponseEntity.ok(service.getDataHubParVille(ville));
    }
}
