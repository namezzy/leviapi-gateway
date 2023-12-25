package top.withlevi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Created on 12/25/2023 3:16 PM
 * 全局过滤
 *
 * @author Levi
 */

@Component
@Slf4j
public class CustomGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 请求日志

        // 3. (黑白名单)
        // 4. 用户鉴权(判断AK,SK是否合法)
        // 5. 请求的模拟接口是否存在
        // 6. 请求转发，调用模拟接口
        // 7. 响应日志
        // 8. 调用成功, 调用接口次数+1
        // 9. 调用失败, 返回一个规范的错误码


        log.info("Custom global filter.");
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
