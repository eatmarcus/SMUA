<%-- 
    Document   : protectadmin
    Created on : Sep 30, 2015, 8:03:34 PM
    Author     : Clifford
--%>
<%
    String userName = (String)session.getAttribute("user");
    if(userName != "admin"){
        response.sendRedirect("studentlogin.jsp");
    }
    
%>
