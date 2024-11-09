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
            System.out.println(requestedURI + " contains dashboard");
            if (this.isURLAllowedWithoutAdminLogin(requestedURI)) {
                System.out.println(requestedURI + " does not need admin login");
                chain.doFilter(request, response);
                return;
            }

            if (httpRequest.getSession().getAttribute("admin") == null) {
                System.out.println(requestedURI + " needs admin login, does not have it");
                httpResponse.sendRedirect("dashboard_login.html");
            } else {
                System.out.println(requestedURI + " has admin login");
                chain.doFilter(request, response);
            }
        } else {
            System.out.println(requestedURI + " does not contain dashboard");
            if (this.isUrlAllowedWithoutUserLogin(requestedURI)) {
                System.out.println(requestedURI + " does not need user login");
                chain.doFilter(request, response);
                return;
            }

            if (httpRequest.getSession().getAttribute("user") == null) {
                System.out.println(requestedURI + " needs user login, does not have it");
                httpResponse.sendRedirect("login.html");
            } else {
                System.out.println(requestedURI + " has user login");
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
        allowedURIsForUsers.add("login.html");
        allowedURIsForUsers.add("login.js");
        allowedURIsForUsers.add("api/login");
        allowedURIsForUsers.add("api/logout");
        allowedURIsForUsers.add(".css");

        allowedURIsForAdmins.add("dashboard_login.html");
        allowedURIsForAdmins.add("dashboard_login.js");
        allowedURIsForAdmins.add("api/dashboard_login");
        allowedURIsForAdmins.add(".css");
    }

    public void destroy() {
        // ignored.
    }

}