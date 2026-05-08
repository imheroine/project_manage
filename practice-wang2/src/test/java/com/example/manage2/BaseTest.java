package com.example.manage2;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional // 测试完成后自动回滚数据，不污染数据库
public abstract class BaseTest {
    // 这里可以放通用的配置
}
