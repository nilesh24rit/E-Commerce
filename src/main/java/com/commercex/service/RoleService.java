package com.commercex.service;

import com.commercex.entity.Role;
import com.commercex.entity.enums.RoleName;

public interface RoleService {
    Role findByName(RoleName name);
    Role getDefaultCustomerRole();
    void createDefaultRoles();
}
