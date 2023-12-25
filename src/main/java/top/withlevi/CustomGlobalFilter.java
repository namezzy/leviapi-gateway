package top.withlevi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.withlevi.leviapiclientsdk.utils.SignUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created on 12/25/2023 3:16 PM
 * 全局过滤
 *
 * @author Levi
 */

@Component
@Slf4j
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1","http://localhost");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 请求日志
        ServerHttpRequest request = exchange.getRequest();
        log.info("请求唯一标识: " + request.getId());
        log.info("请求路径: " + request.getPath().value());
        log.info("请求方法: " + request.getMethod());
        log.info("请求参数: " + request.getQueryParams());
        String sourceAddress = request.getLocalAddress().getHostString();
        log.info("请求来源地址: " + sourceAddress);
        log.info("请求来源地址: " + request.getRemoteAddress());

        // 拿到响应对象
        ServerHttpResponse response = exchange.getResponse();

        // 2. 访问控制-黑白名单
        if (!IP_WHITE_LIST.contains(sourceAddress)) {
            // 设置响应状态码403 Forbidden(禁止访问)
            response.setStatusCode(HttpStatus.FORBIDDEN);
            // 返回处理完成的响应
            return response.setComplete();
        }
        // 3. 用户鉴权(判断AK,SK是否合法)
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");
        String body = headers.getFirst("body");


        // todo 实际情况应该要去数据库中进行查询是否已经分配给用户
        if (!accessKey.equals("levi")) {
            return handleNoAuth(response);
        }

        if (Long.parseLong(nonce) > 10000) {
            return handleNoAuth(response);
        }

        // 时间和当前时间不能超过5分钟
        Long currentTime = System.currentTimeMillis() / 1000;
        final long FIVE_MINUTES = 60 * 5L;
        if ((currentTime - Long.parseLong(timestamp)) >= FIVE_MINUTES) {
            return handleNoAuth(response);
        }

        // 实际情况中是从数据库查出 secretKey

        String serverSign = SignUtils.genSign(body, "levi-key");
        if (!sign.equals(serverSign)) {
            return handleNoAuth(response);
        }
        // 4. 请求的模拟接口是否存在
        // todo 从数据库中查询模拟接口是否存在，以及请求方法是否匹配（还可以校验请求参数）
        // 5. 请求转发，调用模拟接口
        Mono<Void> filter = chain.filter(exchange);

        // 6. 响应日志
        log.info("响应: " + response.getStatusCode());
        // 7. 调用成功, 调用接口次数+1
        if (response.getStatusCode() == HttpStatus.OK) {

        } else {
            // 8. 调用失败, 返回一个规范的错误码
            return handleInvokeError(response);

        }
        log.info("Custom global filter.");
        return filter;
    }

    @Override
    public int getOrder() {
        return 0;
    }


    public Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }


    public Mono<Void> handleInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }
}
