package com.commercex.config;

import com.commercex.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleService roleService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeData() {
        roleService.createDefaultRoles();
    }
}
