package com.example.auth.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * 修复 Spring Boot 的 server.servlet.encoding.force=true 导致所有响应
 * Content-Type 被追加 ";charset=UTF-8" 的问题。
 * 对于已知二进制类型（Excel、PDF 等），在写入响应体之前将 charset 从 Content-Type 中移除。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BinaryResponseFilter implements Filter {

    private static final String[] BINARY_TYPES = {
            "application/vnd.openxmlformats-officedocument",
            "application/vnd.ms-excel",
            "application/pdf",
            "application/zip",
            "image/",
            "application/octet-stream"
    };

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // 用包装器拦截 setHeader，避免后续代码 setContentType 时被追加 charset
        chain.doFilter(request, new HttpServletResponseWrapper(response) {
            @Override
            public void setContentType(String type) {
                if (type != null && isBinaryType(type)) {
                    // 去掉末尾可能的 ";charset=UTF-8"
                    String clean = type.replaceAll(";\\s*charset=[^;]*", "");
                    super.setContentType(clean);
                } else {
                    super.setContentType(type);
                }
            }
        });
    }

    private boolean isBinaryType(String contentType) {
        if (contentType == null) return false;
        String lower = contentType.toLowerCase();
        for (String prefix : BINARY_TYPES) {
            if (lower.startsWith(prefix.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
