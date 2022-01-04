package com.gitee.osinn.boot.securityjwt.security.dto;

import com.gitee.osinn.boot.securityjwt.annotation.API;
import lombok.Data;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

/**
 * 存储白名单url
 */
@Data
@Component
public class SecurityStorage {

    /**
     * 白名单
     */
    private Set<String> permissionAnonymousUrlList;

    /**
     * 基于服务名称认证方式的所有请求接口权限编码
     * key：service的服务名称，value：@API注解或@APIHandlerMethod注解
     */
    private Map<String, API> apiServiceMap;


    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * 判断是否是白名单
     *
     * @param request
     * @return
     */
    public boolean isAnonymousUri(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        // 放行白名单
        for (String url : permissionAnonymousUrlList) {
            boolean match = antPathMatcher.match(url, requestUri);
            if (match || requestUri.equals(url)) {
                return true;
            }
        }

        //OPTIONS请求直接放行
        if (request.getMethod().equals(HttpMethod.OPTIONS.toString())) {
            return true;
        }

        return false;
    }
}
