package com.wjb.auth.gateway.security;

import com.wjb.auth.common.rbac.ApiPermDef;
import org.springframework.http.server.PathContainer;
import org.springframework.util.StringUtils;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.ArrayList;
import java.util.List;

/**
 * 不可变的「接口→权限码」匹配器:把映射编译成 PathPattern,按具体度排序(最具体优先),
 * requiredPerm 返回首个「方法匹配且路径命中」的权限码,无匹配返回 null(=只需登录)。
 */
public final class ApiPermMatcher {

    private static final PathPatternParser PARSER = PathPatternParser.defaultInstance;

    private final List<Entry> entries;

    public ApiPermMatcher(List<ApiPermDef> defs) {
        List<Entry> list = new ArrayList<>();
        for (ApiPermDef d : defs) {
            if (d == null || !StringUtils.hasText(d.url()) || !StringUtils.hasText(d.perm())) {
                continue;
            }
            list.add(new Entry(PARSER.parse(d.url()), d.method(), d.perm()));
        }
        list.sort((a, b) -> PathPattern.SPECIFICITY_COMPARATOR.compare(a.pattern(), b.pattern()));
        this.entries = List.copyOf(list);
    }

    /** 返回该请求所需权限码;无任何映射命中返回 null */
    public String requiredPerm(String method, String path) {
        PathContainer pc = PathContainer.parsePath(path);
        for (Entry e : entries) {
            if (methodMatches(e.method(), method) && e.pattern().matches(pc)) {
                return e.perm();
            }
        }
        return null;
    }

    private static boolean methodMatches(String entryMethod, String requestMethod) {
        if (!StringUtils.hasText(entryMethod) || "*".equals(entryMethod)) {
            return true;
        }
        return entryMethod.equalsIgnoreCase(requestMethod);
    }

    private record Entry(PathPattern pattern, String method, String perm) {}
}
