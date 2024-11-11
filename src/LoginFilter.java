import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIsForUsers = new ArrayList<>();
    private final ArrayList<String> allowedURIsForAdmins = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginFilter: " + httpRequest.getRequestURI());
        String requestedURI = httpRequest.getRequestURI();

        if (requestedURI.contains("dashboard")) {
            if (this.isURLAllowedWithoutAdminLogin(requestedURI)) {
                chain.doFilter(request, response);
                return;
            }

            if (httpRequest.getSession().getAttribute("admin") == null) {
                httpResponse.sendRedirect(((HttpServletRequest) request).getContextPath() + "/dashboard_login.html");
            } else {
                chain.doFilter(request, response);
            }
        } else {
            if (this.isUrlAllowedWithoutUserLogin(requestedURI)) {
                chain.doFilter(request, response);
                return;
            }

            if (httpRequest.getSession().getAttribute("user") == null) {
                httpResponse.sendRedirect(((HttpServletRequest) request).getContextPath() + "/login.html");
            } else {
                chain.doFilter(request, response);
            }
        }
    }

    private boolean isUrlAllowedWithoutUserLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc.
         */
        return allowedURIsForUsers.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    private boolean isURLAllowedWithoutAdminLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc.
         */
        return allowedURIsForAdmins.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIsForUsers.add("/login.html");
        allowedURIsForUsers.add("/login.js");
        allowedURIsForUsers.add("/api/login");
        allowedURIsForUsers.add("/api/logout");
        allowedURIsForUsers.add(".css");

        allowedURIsForAdmins.add("/dashboard_login.html");
        allowedURIsForAdmins.add("/dashboard_login.js");
        allowedURIsForAdmins.add("/api/_dashboard_login");
        allowedURIsForAdmins.add(".css");
    }

    public void destroy() {
        // ignored.
    }

}