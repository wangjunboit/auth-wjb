package com.wjb.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wjb.auth.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /** 查某用户可见的目录/菜单(menu_type M/C, visible=1),用于侧边栏;按 sort 升序 */
    @Select("""
            SELECT DISTINCT m.*
            FROM sys_user_role ur
            JOIN sys_role_menu rm ON rm.role_id = ur.role_id AND rm.deleted = 0
            JOIN sys_menu m ON m.id = rm.menu_id AND m.deleted = 0
            WHERE ur.user_id = #{userId}
              AND ur.deleted = 0
              AND m.menu_type IN ('M','C')
              AND m.visible = 1
            ORDER BY m.sort ASC
            """)
    List<SysMenu> selectVisibleMenusByUserId(Long userId);
}
