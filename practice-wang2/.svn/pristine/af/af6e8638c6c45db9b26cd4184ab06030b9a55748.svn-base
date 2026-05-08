package com.example.manage2.mapper;

import com.example.manage2.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统用户数据库映射接口
 */
@Mapper
public interface SysUserMapper {

    /**
     * 根据登录账号精确查询用户信息
     *
     * @param username 登录账号
     * @return 用户实体
     */
    SysUser findByUsername(@Param("username") String username);

    /**
     * 插入新用户数据，并将数据库生成的自增 ID 回填到 user 对象中
     *
     * @param user 用户实体
     */
    void insertUser(SysUser user);

    /**
     * 获取所有状态正常的系统用户 (status = 1)
     *
     * @return 用户集合 (主要返回 id, username, real_name)
     */
    List<SysUser> findAllNormalUsers();

    /**
     * [辅助功能] 根据用户的真实姓名反查用户 ID
     * (主要用于 Excel 导入时，将填写的中文名翻译为系统 ID)
     *
     * @param realName 用户真实中文名
     * @return 用户主键ID
     */
    Long findIdByRealName(@Param("realName") String realName);
}