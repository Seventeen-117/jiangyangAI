package com.signature.service;

import com.jiangyang.base.datasource.annotation.DataSource;

/**
 * 令牌黑名单服务接口
 * 用于管理已撤销的令牌
 */
@DataSource("master")
public interface TokenBlacklistService {

    /**
     * 将令牌加入黑名单
     *
     * @param token 要加入黑名单的令牌
     */
    void blacklistToken(String token);

    /**
     * 检查令牌是否在黑名单中
     *
     * @param token 要检查的令牌
     * @return 是否在黑名单中
     */
    boolean isTokenBlacklisted(String token);

    /**
     * 从黑名单中移除令牌
     *
     * @param token 要移除的令牌
     */
    void removeFromBlacklist(String token);

    /**
     * 清理过期的黑名单条目
     */
    void cleanupExpiredEntries();
}
