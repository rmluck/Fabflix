import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

// Declaring a WebServlet called SessionServlet, which maps to url "/session"
@WebServlet(name = "SessionServlet", urlPatterns="/api/session")
public class SessionServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        UserSession userSession = (UserSession) session.getAttribute("userSession");

        if (userSession == null) {
            userSession = new UserSession();
            session.setAttribute("userSession", userSession);
        }

        Integer accessCount = (Integer) session.getAttribute("accessCount");
        if (accessCount == null) {
            accessCount = 0;
        } else {
            accessCount++;
        }
        session.setAttribute("accessCount", accessCount);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.write(new Gson().toJson(userSession));
        out.flush();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        UserSession userSession = (UserSession) session.getAttribute("userSession");

        if (userSession == null) {
            userSession = new UserSession();
            session.setAttribute("userSession", userSession);
        }

        userSession.setCurrentPage(Integer.parseInt(request.getParameter("currentPage")));
        userSession.setMoviesPerPage(Integer.parseInt(request.getParameter("moviesPerPage")));
        userSession.setSearchQuery(request.getParameter("searchQuery"));
        userSession.setTitleSortDirection(request.getParameter("titleSortDirection"));
        userSession.setRatingSortDirection(request.getParameter("ratingSortDirection"));
        userSession.setSortPriority(request.getParameter("sortPriority"));

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.write(new Gson().toJson(userSession));
        out.flush();
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if (session != null) {
            UserSession userSession = (UserSession) session.getAttribute("userSession");
            if (userSession != null) {
                userSession.setCurrentPage(1);
                userSession.setMoviesPerPage(10);
                userSession.setSearchQuery("");
                userSession.setTitleSortDirection("desc");
                userSession.setRatingSortDirection("desc");
                userSession.setSortPriority("r");

                session.setAttribute("userSession", userSession);
            }
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.write("{\"message\": \"Session cleared.\"}");
        out.flush();
    }
}
