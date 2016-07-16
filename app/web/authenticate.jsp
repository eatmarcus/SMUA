
<%@page import="java.util.HashMap"%>
<%@page import="java.util.ArrayList"%>
<%@page import="is203.dao.DemographicsDAO"%>

<%  String username = request.getParameter("username");
    String password = request.getParameter("password");
    DemographicsDAO dDAO = new DemographicsDAO();
    HashMap<String, String> forLogin = dDAO.retrieveAllUserPW();
    try {
        if (username.equals("") || password.equals("")) {
            String errorMsg = "Invalid username/password.";
            session.setAttribute("errorMsg", errorMsg);
            response.sendRedirect("login.jsp");
        } else {
            if (username.equals("admin") && password.equals("12345678")) {
                session.setAttribute("user", "admin");
                response.sendRedirect("Admin.jsp");
            } else {
                String inputUserPW = forLogin.get(username);
                if (inputUserPW == null) {
                    String errorMsg = "Invalid username/password.";
                    session.setAttribute("errorMsg", errorMsg);
                    response.sendRedirect("login.jsp");
                } else {
                    if (password.equals(inputUserPW)) {
                        session.setAttribute("user", "student");
                        session.setAttribute("email", username);
                        response.sendRedirect("studentlogin.jsp");
                    } else {
                        String errorMsg = "Invalid username/password.";
                        session.setAttribute("errorMsg", errorMsg);
                        response.sendRedirect("login.jsp");
                    }
                }
            }
        }
    } catch (NullPointerException e) {
        String errorMsg = "Invalid username/password.";
        session.setAttribute("errorMsg", errorMsg);
        response.sendRedirect("login.jsp");
    }


%>