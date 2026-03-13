package com.rml.system.service;

import com.rml.system.dto.request.CreateDocumentRequest;
import com.rml.system.dto.request.UpdateDocumentRequest;
import com.rml.system.dto.response.DocumentDto;
import com.rml.system.entity.Document;
import com.rml.system.exception.AppException;
import com.rml.system.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    public Page<DocumentDto> listDocuments(String q, Pageable pageable) {
        return documentRepository.search(q, pageable).map(this::toDto);
    }

    @Transactional
    public DocumentDto createDocument(CreateDocumentRequest req) {
        Document d = new Document();
        d.setModel(req.getModel());
        d.setName(req.getName());
        d.setProducer(req.getProducer());
        d.setQuantity(req.getQuantity());
        return toDto(documentRepository.save(d));
    }

    @Transactional
    public DocumentDto updateDocument(String id, UpdateDocumentRequest req) {
        Document d = documentRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("Document not found: " + id));
        if (req.getModel() != null) d.setModel(req.getModel());
        if (req.getName() != null) d.setName(req.getName());
        if (req.getProducer() != null) d.setProducer(req.getProducer());
        d.setQuantity(req.getQuantity());
        return toDto(documentRepository.save(d));
    }

    @Transactional
    public void deleteDocuments(List<String> ids) {
        if (ids == null || ids.isEmpty()) return;
        documentRepository.deleteAllById(ids);
    }

    @Transactional
    public void updateManualFilename(String id, String filename) {
        Document d = documentRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("Document not found: " + id));
        d.setManualFilename(filename);
        documentRepository.save(d);
    }

    public Document getDocumentEntity(String id) {
        return documentRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("Document not found: " + id));
    }

    private DocumentDto toDto(Document d) {
        DocumentDto dto = new DocumentDto();
        dto.setId(d.getId());
        dto.setModel(d.getModel());
        dto.setName(d.getName());
        dto.setProducer(d.getProducer());
        dto.setQuantity(d.getQuantity());
        dto.setManualFilename(d.getManualFilename());
        dto.setCreatedAt(d.getCreatedAt());
        return dto;
    }
}
