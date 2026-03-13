package com.rml.system.controller;

import com.rml.system.dto.ApiResponse;
import com.rml.system.dto.request.CreateEquipmentRequest;
import com.rml.system.dto.request.UpdateEquipmentRequest;
import com.rml.system.dto.response.EquipmentDto;
import com.rml.system.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_EQUIP_LIST')")
    public ApiResponse<Page<EquipmentDto>> listEquipment(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.ok(equipmentService.listEquipment(q, pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_EQUIP_ADD')")
    public ApiResponse<EquipmentDto> createEquipment(@Valid @RequestBody CreateEquipmentRequest req) {
        return ApiResponse.created(equipmentService.createEquipment(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_EQUIP_EDIT')")
    public ApiResponse<EquipmentDto> updateEquipment(@PathVariable String id,
                                                      @RequestBody UpdateEquipmentRequest req) {
        return ApiResponse.ok(equipmentService.updateEquipment(id, req));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_EQUIP_DELETE')")
    public ApiResponse<Void> deleteEquipment(@RequestParam String ids) {
        List<String> idList = Arrays.stream(ids.split(","))
            .map(String::trim).filter(s -> !s.isBlank()).collect(Collectors.toList());
        equipmentService.deleteEquipment(idList);
        return ApiResponse.ok();
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_EQUIP_LIST')")
    public ResponseEntity<byte[]> exportExcel() throws IOException {
        byte[] data = equipmentService.exportToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "equipment-export.xlsx");
        return ResponseEntity.ok().headers(headers).body(data);
    }
}
