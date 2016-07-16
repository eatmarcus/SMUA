<%-- 
    Document   : logout
    Created on : Sep 30, 2015, 7:40:30 PM
    Author     : Clifford
--%>


  <% if(session!=null){
            session.invalidate();
            response.sendRedirect("login.jsp");
        }
        %>   
