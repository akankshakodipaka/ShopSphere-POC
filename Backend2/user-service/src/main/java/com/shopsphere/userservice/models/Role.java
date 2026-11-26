package com.shopsphere.userservice.models;

/**
 * The three kinds of account. The name goes into the token's "role" claim,
 * so the other services see exactly CUSTOMER, ADMIN or SUPER_ADMIN.
 */
public enum Role {
    CUSTOMER,
    ADMIN,
    SUPER_ADMIN
}
