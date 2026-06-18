package com.wjb.auth.rbac;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wjb.auth.common.constant.SecurityConstants;
import com.wjb.auth.common.rbac.ApiPermDef;
import com.wjb.auth.entity.SysMenu;
import com.wjb.auth.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 把 sys_menu 中的 接口→权限码 映射同步到 Redis,并通知网关刷新。启动时与菜单变更后各发布一次。
 * 整体容错:查库/写 Redis 任一失败只记 WARN,不中断启动(符合"映射不可用则退化为只需登录"的设计)。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiPermPublisher implements ApplicationRunner {

    private final SysMenuMapper menuMapper;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) {
        refresh();
    }

    /** 查表 → 组装 → 写 Redis → 发刷新消息;任何异常只记 WARN 不抛出 */
    public void refresh() {
        try {
            List<SysMenu> rows = menuMapper.selectList(
                    new LambdaQueryWrapper<SysMenu>().isNotNull(SysMenu::getApiUrl));
            List<ApiPermDef> defs = buildDefs(rows);
            String json = objectMapper.writeValueAsString(defs);
            redis.opsForValue().set(SecurityConstants.API_PERM_KEY, json);
            redis.convertAndSend(SecurityConstants.API_PERM_REFRESH_CHANNEL, "1");
            log.info("已发布 API 权限映射 {} 条", defs.size());
        } catch (Exception e) {
            log.warn("发布 API 权限映射失败(网关将退化为仅校验登录)", e);
        }
    }

    /** 纯函数:从菜单行组装映射,过滤 perm/url 为空者(便于单测) */
    public static List<ApiPermDef> buildDefs(List<SysMenu> rows) {
        List<ApiPermDef> defs = new ArrayList<>();
        for (SysMenu m : rows) {
            if (StringUtils.hasText(m.getApiUrl()) && StringUtils.hasText(m.getPerm())) {
                defs.add(new ApiPermDef(m.getApiMethod(), m.getApiUrl(), m.getPerm()));
            }
        }
        return defs;
    }
}
