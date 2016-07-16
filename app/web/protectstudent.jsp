<%-- 
    Document   : protect
    Created on : Sep 30, 2015, 7:50:50 PM
    Author     : Clifford
--%>
<%
    String userName = (String)session.getAttribute("user");
    if(userName != "student"){
        response.sendRedirect("login.jsp");
    }
%>