package top.withlevi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LeviapiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeviapiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // path_route:匹配 /levi 路径,转发到 https://withlevi.top
                .route("path_route", r -> r.path("/levi")
                        //主要是通过 .filters(f -> f.stripPrefix(1)) 这一段配置添加了一个过滤器,调用了 stripPrefix 方法移除了路径中的第一级目录。
                        //这样配置后,原先 "/levi/xxx" 的请求会把 "/levi" 去除,只保留 "/xxx" 路径然后转发到 "https://withlevi.top"。
                        .filters(f -> f.stripPrefix(1))
                        .uri("https://blog.withlevi.top"))
                .route("host_route", r -> r.path("/blog")
                        .uri("https://blog.withlevi.top"))
                .build();
    }

}
