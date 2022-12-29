package com.czy.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.czy.reggie.common.BaseContext;
import com.czy.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经完成登陆
 */
//urlPatterns 是拦截路径
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符，用来路径比较，请求的路径与urls数组中的比较
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest)servletRequest;
        HttpServletResponse response=(HttpServletResponse)servletResponse;

        //1.获取本次请求的uri
        String requestURI = request.getRequestURI();

        log.info("拦截请求:{}",requestURI);

        //定义不需要处理的请求路径
        String[] urls=new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/login",
                "/user/sendMsg"
        };

        //2.判断本次请求是否需要处理
        boolean check = check(urls, requestURI);

        //3.如果不需要处理，直接放行
        if (check){
            log.info("本次请求不需要处理:{}",requestURI);
            filterChain.doFilter(request,response);
            return;
        }

        //4-1.如果需要处理，判断登录状态,如果session不为空表示已登录
        if (request.getSession().getAttribute("employee") != null){
            log.info("用户已登录,用户的id为:{}",request.getSession().getAttribute("employee"));

            Long empId = (long)request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);
            return;
        }

        //4-2. 手机端 如果需要处理，判断登录状态,如果session不为空表示已登录
        if (request.getSession().getAttribute("user") != null){
            log.info("用户已登录,用户的id为:{}",request.getSession().getAttribute("user"));

            Long userId = (long)request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }

        log.info("用户未登录");
        //5.如果未登录，返回未登录结果
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));

    }

    /**
     * 路径匹配，检查本次请求是否需要处理
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){
        //对urls遍历，比较requestURL，看是否需要处理
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match){//匹配上，返回true
                return true;
            }

        }
        return false;
    }
}
