package com.rml.system.controller;

import com.rml.system.dto.ApiResponse;
import com.rml.system.dto.request.CreateDocumentRequest;
import com.rml.system.dto.request.UpdateDocumentRequest;
import com.rml.system.dto.response.DocumentDto;
import com.rml.system.entity.Document;
import com.rml.system.service.DocumentService;
import com.rml.system.util.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final FileStorageService fileStorageService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_DOC_LIST')")
    public ApiResponse<Page<DocumentDto>> listDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.ok(documentService.listDocuments(q, pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_DOC_ADD')")
    public ApiResponse<DocumentDto> createDocument(@Valid @RequestBody CreateDocumentRequest req) {
        return ApiResponse.created(documentService.createDocument(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_DOC_EDIT')")
    public ApiResponse<DocumentDto> updateDocument(@PathVariable String id,
                                                    @RequestBody UpdateDocumentRequest req) {
        return ApiResponse.ok(documentService.updateDocument(id, req));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_DOC_DELETE')")
    public ApiResponse<Void> deleteDocuments(@RequestParam String ids) {
        List<String> idList = Arrays.stream(ids.split(","))
            .map(String::trim).filter(s -> !s.isBlank()).collect(Collectors.toList());
        documentService.deleteDocuments(idList);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/manual")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_DOC_UPLOAD')")
    public ApiResponse<Map<String, String>> uploadManual(@PathVariable String id,
                                                          @RequestParam("file") MultipartFile file)
            throws IOException {
        String filename = file.getOriginalFilename();
        // Remove all whitespace characters from filename (original spec behavior)
        if (filename != null) filename = filename.replaceAll("\\s+", "");
        fileStorageService.store(file, filename);
        documentService.updateManualFilename(id, filename);
        return ApiResponse.ok(Map.of("manualFilename", filename != null ? filename : ""));
    }

    @GetMapping("/{id}/manual")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_DOC_LIST')")
    public ResponseEntity<Resource> downloadManualById(@PathVariable String id) throws IOException {
        Document doc = documentService.getDocumentEntity(id);
        Resource resource = fileStorageService.load(doc.getManualFilename());
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + doc.getManualFilename() + "\"")
            .body(resource);
    }

    @GetMapping("/manual/{filename}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_DOC_LIST')")
    public ResponseEntity<Resource> downloadManualByFilename(@PathVariable String filename) throws IOException {
        Resource resource = fileStorageService.load(filename);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"")
            .body(resource);
    }
}
