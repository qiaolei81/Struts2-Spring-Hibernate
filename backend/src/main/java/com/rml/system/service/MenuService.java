package com.rml.system.service;

import com.rml.system.dto.request.CreateMenuRequest;
import com.rml.system.dto.request.UpdateMenuRequest;
import com.rml.system.dto.response.MenuDto;
import com.rml.system.entity.Menu;
import com.rml.system.exception.AppException;
import com.rml.system.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    @Transactional(readOnly = true)
    public List<MenuDto> getTree() {
        List<Menu> roots = menuRepository.findByParentIsNullOrderBySequenceAsc();
        return roots.stream().map(this::toDtoWithChildren).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenuDto> listAll() {
        return menuRepository.findAll().stream().map(this::toFlatDto).collect(Collectors.toList());
    }

    @Transactional
    public MenuDto createMenu(CreateMenuRequest req) {
        Menu menu = new Menu();
        menu.setName(req.getName());
        menu.setUrl(req.getUrl());
        menu.setIconClass(req.getIconClass());
        menu.setSequence(req.getSequence());
        if (req.getParentId() != null && !req.getParentId().isBlank()) {
            Menu parent = menuRepository.findById(req.getParentId())
                .orElseThrow(() -> AppException.notFound("Parent menu not found"));
            menu.setParent(parent);
        }
        return toFlatDto(menuRepository.save(menu));
    }

    @Transactional
    public MenuDto updateMenu(String id, UpdateMenuRequest req) {
        Menu menu = menuRepository.findById(id)
            .orElseThrow(() -> AppException.notFound("Menu not found: " + id));
        if (req.getName() != null) menu.setName(req.getName());
        if (req.getUrl() != null) menu.setUrl(req.getUrl());
        if (req.getIconClass() != null) menu.setIconClass(req.getIconClass());
        menu.setSequence(req.getSequence());
        if (req.getParentId() != null && !req.getParentId().isBlank()) {
            Menu parent = menuRepository.findById(req.getParentId())
                .orElseThrow(() -> AppException.notFound("Parent menu not found"));
            menu.setParent(parent);
        }
        return toFlatDto(menuRepository.save(menu));
    }

    @Transactional
    public void deleteMenu(String id) {
        menuRepository.deleteById(id);
    }

    private MenuDto toDtoWithChildren(Menu menu) {
        MenuDto dto = toFlatDto(menu);
        dto.setChildren(menu.getChildren().stream()
            .map(this::toDtoWithChildren)
            .collect(Collectors.toList()));
        return dto;
    }

    private MenuDto toFlatDto(Menu menu) {
        MenuDto dto = new MenuDto();
        dto.setId(menu.getId());
        dto.setName(menu.getName());
        dto.setIconClass(menu.getIconClass());
        dto.setUrl(menu.getUrl());
        dto.setSequence(menu.getSequence());
        dto.setParentId(menu.getParent() != null ? menu.getParent().getId() : null);
        return dto;
    }
}
