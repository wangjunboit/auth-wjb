package com.wjb.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wjb.auth.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId} AND deleted = 0")
    List<Long> selectMenuIdsByRoleId(Long roleId);

    @Select("SELECT COUNT(1) FROM sys_role_menu WHERE menu_id = #{menuId} AND deleted = 0")
    long countByMenuId(Long menuId);
}
