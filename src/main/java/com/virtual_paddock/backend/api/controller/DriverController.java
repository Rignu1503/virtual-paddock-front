package com.virtual_paddock.backend.api.controller;

import com.virtual_paddock.backend.api.dtos.ApiResponse;
import com.virtual_paddock.backend.api.dtos.PageResponse;
import com.virtual_paddock.backend.api.dtos.driver.*;
import com.virtual_paddock.backend.infrastructure.abstract_service.IDriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final IDriverService driverService;

    @PostMapping
    public ResponseEntity<ApiResponse<DriverResponse>> create(@Valid @RequestBody DriverRequest request) {
        DriverResponse response = driverService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Piloto creado exitosamente", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DriverResponse>> getById(@PathVariable Long id) {
        DriverResponse response = driverService.getById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DriverBasicResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<DriverBasicResponse> response = driverService.getAll(page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<ApiResponse<PageResponse<DriverBasicResponse>>> getByTeamId(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<DriverBasicResponse> response = driverService.getByTeamId(teamId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DriverResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody DriverUpdate update) {
        DriverResponse response = driverService.update(id, update);
        return ResponseEntity.ok(ApiResponse.ok("Piloto actualizado exitosamente", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        driverService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Piloto eliminado exitosamente", null));
    }
}
