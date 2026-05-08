package com.example.manage2.service.Impl;

import com.example.manage2.entity.SysUser;
import com.example.manage2.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// 使用 Mockito 扩展
@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

    // @InjectMocks 会自动将下面 @Mock 的对象注入到这个类中
    @InjectMocks
    private SysUserServiceImpl sysUserService;

    // @Mock 模拟一个假的 Mapper，不会真实连接数据库
    @Mock
    private SysUserMapper sysUserMapper;

    @Test
    void testRegister_Success() {
        // 1. 准备测试数据 (Arrange)
        SysUser newUser = new SysUser();
        newUser.setUsername("testUser");
        newUser.setPassword("123456");

        // 模拟数据库行为：当根据账号查询时，返回 null (代表账号未被注册)
        when(sysUserMapper.findByUsername("testUser")).thenReturn(null);

        // 模拟插入操作不抛出异常 (void 方法默认什么都不做，这行可以省略)
        doNothing().when(sysUserMapper).insertUser(any(SysUser.class));

        // 2. 执行测试目标方法 (Act)
        assertDoesNotThrow(() -> sysUserService.register(newUser), "正常注册不应抛出异常");

        // 3. 验证结果 (Assert)
        assertEquals(1, newUser.getStatus(), "注册后用户状态应为 1");
        assertNotNull(newUser.getCreateTime(), "注册后应自动生成创建时间");

        // 验证 Mapper 的 insertUser 方法确实被调用了 1 次
        verify(sysUserMapper, times(1)).insertUser(newUser);
    }

    @Test
    void testRegister_UserAlreadyExists() {
        // 1. 准备测试数据
        SysUser existingUser = new SysUser();
        existingUser.setUsername("testUser");

        // 模拟数据库行为：查询时返回了一个已存在的用户
        when(sysUserMapper.findByUsername("testUser")).thenReturn(new SysUser());

        // 2 & 3. 执行并验证抛出了指定的异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            sysUserService.register(existingUser);
        });

        assertEquals("该账号已被注册，请更换账号名", exception.getMessage());

        // 验证因为抛出异常，插入方法绝对没有被调用
        verify(sysUserMapper, never()).insertUser(any());
    }
}