
package net.mingsoft.base.biz;

import java.util.concurrent.CompletableFuture;

/**
 * 异步执行sql
 */
public interface IAsyncSqlBiz {


    /**
     * 异步执行ddl sql
     * @param sql ddl sql
     * @return future 根据future 回调处理成功、异常结果
     */
    CompletableFuture<Void> executeDDL(String sql);

}
