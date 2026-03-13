package com.rml.system.service;

import com.rml.system.dto.request.CreateEquipmentRequest;
import com.rml.system.dto.request.UpdateEquipmentRequest;
import com.rml.system.dto.response.EquipmentDto;
import com.rml.system.entity.Equipment;
import com.rml.system.exception.AppException;
import com.rml.system.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public Page<EquipmentDto> listEquipment(String q, Pageable pageable) {
        return equipmentRepository.search(q, pageable).map(this::toDto);
    }

    @Transactional
    public EquipmentDto createEquipment(CreateEquipmentRequest req) {
        Equipment e = new Equipment();
        e.setModel(req.getModel());
        e.setName(req.getName());
        e.setProducer(req.getProducer());
        e.setQuantity(req.getQuantity());
        e.setDescription(req.getDescription());
        return toDto(equipmentRepository.save(e));
    }

    @Transactional
    public EquipmentDto updateEquipment(String id, UpdateEquipmentRequest req) {
        Equipment e = equipmentRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("Equipment not found: " + id));
        if (req.getModel() != null) e.setModel(req.getModel());
        if (req.getName() != null) e.setName(req.getName());
        if (req.getProducer() != null) e.setProducer(req.getProducer());
        e.setQuantity(req.getQuantity());
        if (req.getDescription() != null) e.setDescription(req.getDescription());
        return toDto(equipmentRepository.save(e));
    }

    @Transactional
    public void deleteEquipment(List<String> ids) {
        if (ids == null || ids.isEmpty()) return;
        equipmentRepository.deleteAllById(ids);
    }

    public byte[] exportToExcel() throws IOException {
        List<Equipment> all = equipmentRepository.findAll();
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Equipment");
            CellStyle headerStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row header = sheet.createRow(0);
            String[] cols = {"Model", "Name", "Producer", "Quantity", "Description"};
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
            }
            int rowIdx = 1;
            for (Equipment eq : all) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(eq.getModel());
                row.createCell(1).setCellValue(eq.getName() != null ? eq.getName() : "");
                row.createCell(2).setCellValue(eq.getProducer() != null ? eq.getProducer() : "");
                row.createCell(3).setCellValue(eq.getQuantity() != null ? eq.getQuantity() : 0);
                row.createCell(4).setCellValue(eq.getDescription() != null ? eq.getDescription() : "");
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    private EquipmentDto toDto(Equipment e) {
        EquipmentDto dto = new EquipmentDto();
        dto.setId(e.getId());
        dto.setModel(e.getModel());
        dto.setName(e.getName());
        dto.setProducer(e.getProducer());
        dto.setQuantity(e.getQuantity());
        dto.setDescription(e.getDescription());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
