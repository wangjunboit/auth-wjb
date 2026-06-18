package com.wjb.auth;

import com.wjb.auth.common.rbac.ApiPermDef;
import com.wjb.auth.entity.SysMenu;
import com.wjb.auth.rbac.ApiPermPublisher;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiPermPublisherTest {

    private SysMenu menu(String perm, String url, String method) {
        SysMenu m = new SysMenu();
        m.setPerm(perm);
        m.setApiUrl(url);
        m.setApiMethod(method);
        return m;
    }

    @Test
    void buildDefs_filtersBlankPermOrUrl_andMapsFields() {
        List<SysMenu> rows = List.of(
                menu("system:user:list", "/system/user/list", "GET"),
                menu("", "/system/user", "POST"),          // 空 perm,丢弃
                menu("system:user:add", "", "POST"),       // 空 url,丢弃
                menu("system:user:query", "/system/user/*", null) // method 允许 null
        );
        List<ApiPermDef> defs = ApiPermPublisher.buildDefs(rows);
        assertEquals(2, defs.size());
        assertEquals("GET", defs.get(0).method());
        assertEquals("/system/user/list", defs.get(0).url());
        assertEquals("system:user:list", defs.get(0).perm());
        assertTrue(defs.stream().anyMatch(d -> d.perm().equals("system:user:query") && d.method() == null));
    }
}
