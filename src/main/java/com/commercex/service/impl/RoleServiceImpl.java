package com.commercex.service.impl;

import com.commercex.entity.Role;
import com.commercex.entity.enums.RoleName;
import com.commercex.exception.RoleNotFoundException;
import com.commercex.repository.RoleRepository;
import com.commercex.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public Role findByName(RoleName name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new RoleNotFoundException("Role " + name + " not found in database."));
    }

    @Override
    @Transactional(readOnly = true)
    public Role getDefaultCustomerRole() {
        return findByName(RoleName.ROLE_CUSTOMER);
    }

    @Override
    @Transactional
    public void createDefaultRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
            }
        }
    }
}
