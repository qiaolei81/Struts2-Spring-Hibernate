package com.rml.system.controller;

import com.rml.system.dto.ApiResponse;
import com.rml.system.dto.request.CreateMenuRequest;
import com.rml.system.dto.request.UpdateMenuRequest;
import com.rml.system.dto.response.MenuDto;
import com.rml.system.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/tree")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<MenuDto>> getTree() {
        return ApiResponse.ok(menuService.getTree());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_MENU_LIST')")
    public ApiResponse<List<MenuDto>> listAll() {
        return ApiResponse.ok(menuService.listAll());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_MENU_ADD')")
    public ApiResponse<MenuDto> createMenu(@Valid @RequestBody CreateMenuRequest req) {
        return ApiResponse.created(menuService.createMenu(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_MENU_EDIT')")
    public ApiResponse<MenuDto> updateMenu(@PathVariable String id, @RequestBody UpdateMenuRequest req) {
        return ApiResponse.ok(menuService.updateMenu(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PERM_MENU_DELETE')")
    public ApiResponse<Void> deleteMenu(@PathVariable String id) {
        menuService.deleteMenu(id);
        return ApiResponse.ok();
    }
}
