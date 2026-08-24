
package net.mingsoft.base.biz.impl;

import net.mingsoft.base.biz.IAsyncSqlBiz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncSqlBizImpl implements IAsyncSqlBiz {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Async
    public CompletableFuture<Void> executeDDL(String sql) {
        LOG.debug("异步线程:{} 执行 sql:{}",Thread.currentThread().getName(),sql);
        jdbcTemplate.execute(sql);
        return CompletableFuture.completedFuture(null);
    }
}
