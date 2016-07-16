<%-- 
    Document   : login
    Created on : Sep 30, 2015, 4:56:54 PM
    Author     : User
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Log In</title>
        <link href="css/bootstrap.min.css" rel="stylesheet">

        <!-- Custom styles for this template -->
        <link href="css/signin.css" rel="stylesheet">

        <script src="http://code.jquery.com/jquery.js"></script>
        <script src="//cdnjs.cloudflare.com/ajax/libs/handlebars.js/2.0.0/handlebars.min.js"></script>
    </head>
    <body>
        
        <div class="container">
            <div class="row">
                <div class="col-md-6 col-md-offset-3">
                    <div class="panel panel-default">
                        <div class="panel-heading"> <strong class="">Login</strong>
                        </div>
                        <div class="panel-body">
                            <form class="form-horizontal" role="form" action="authenticate.jsp" method="post">
                                <%--<span class ='glyphicon glyphicon-user' aria-hidden='true'></span>--%>
                                <div class="form-group">
                                    
                                    <label for="username" class="col-sm-3 control-label"><i class= "glyphicon glyphicon-user"></i>&nbsp;&nbsp;Username</label>                                
                                        
                                    <div class="col-sm-9">
                                        <input type="text" class ="form-control" name="username" placeholder="Username">
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label for="password" class="col-sm-3 control-label"><i class= "glyphicon glyphicon-lock"></i>&nbsp;&nbsp;Password</label>
                                    <div class="col-sm-9">
                                        <input type="password" class="form-control" name="password"  placeholder="Password">
                                    </div>
                                </div>
                                
                                <%  String errorMsg = (String) session.getAttribute("errorMsg");
                                    if (errorMsg != null) {
                                        out.println("<div class='alert alert-danger' role='alert'><strong>");
                                        out.println("<span class='glyphicon glyphicon-exclamation-sign' aria-hidden='true'></span><span class='sr-only'></span>");
                                        out.println(" "+ errorMsg);
                                        out.println("</string></div>");
                                    }

                                    if (session.getAttribute("user") != null) {
                                        String toGo = (String) session.getAttribute("user");
                                        if (toGo.equals("student")) {
                                            response.sendRedirect("studentlogin.jsp");
                                        } else {
                                            response.sendRedirect("Admin.jsp");
                                        }
                                    }
                                %>
                                <div class="form-group last">
                                    <div class="col-sm-offset-3 col-md-10">
                                        <button type="submit" class="btn btn-success btn-sm"><i class="glyphicon glyphicon-log-in"></i>&nbsp; Sign in</button>
                                    </div>
                                </div>
                                <div id="table-area" class="box col-md-1">


                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
