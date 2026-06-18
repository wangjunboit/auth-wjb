package com.wjb.auth.gateway;

import com.wjb.auth.common.rbac.ApiPermDef;
import com.wjb.auth.gateway.security.ApiPermMatcher;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApiPermMatcherTest {

    private ApiPermMatcher matcher() {
        return new ApiPermMatcher(List.of(
                new ApiPermDef("GET", "/system/user/list", "system:user:list"),
                new ApiPermDef("POST", "/system/user", "system:user:add"),
                new ApiPermDef("PUT", "/system/user", "system:user:edit"),
                new ApiPermDef("DELETE", "/system/user/**", "system:user:remove"),
                new ApiPermDef("GET", "/system/user/*", "system:user:query"),
                new ApiPermDef("GET", "/system/user/*/roles", "system:user:query"),
                new ApiPermDef("POST", "/system/user/assign-roles", "system:user:edit")
        ));
    }

    @Test
    void listBeatsQueryBySpecificity() {
        assertEquals("system:user:list", matcher().requiredPerm("GET", "/system/user/list"));
    }

    @Test
    void detailMatchesQuery() {
        assertEquals("system:user:query", matcher().requiredPerm("GET", "/system/user/5"));
    }

    @Test
    void methodFiltering() {
        assertEquals("system:user:add", matcher().requiredPerm("POST", "/system/user"));
        assertEquals("system:user:edit", matcher().requiredPerm("PUT", "/system/user"));
        assertEquals("system:user:remove", matcher().requiredPerm("DELETE", "/system/user/9"));
    }

    @Test
    void subResourceNotSwallowedBySingleStar() {
        assertEquals("system:user:query", matcher().requiredPerm("GET", "/system/user/5/roles"));
    }

    @Test
    void assignRolesMapsToEdit() {
        assertEquals("system:user:edit", matcher().requiredPerm("POST", "/system/user/assign-roles"));
    }

    @Test
    void unmappedReturnsNull() {
        assertNull(matcher().requiredPerm("GET", "/auth/userinfo"));
    }

    @Test
    void blankUrlOrPermEntriesIgnored() {
        ApiPermMatcher m = new ApiPermMatcher(List.of(
                new ApiPermDef("GET", "", "system:x:y"),
                new ApiPermDef("GET", "/a", "")
        ));
        assertNull(m.requiredPerm("GET", "/a"));
    }
}
