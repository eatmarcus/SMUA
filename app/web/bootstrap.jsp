<%-- 
    Document   : bootstrap
    Created on : Nov 3, 2015, 5:12:08 PM
    Author     : Clifford
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <form action="/json/bootstrap"  method="post" enctype="multipart/form-data">
            File:
            <input type="file" name="bootstrap-file" /><br />
            <input type='text' name='token' value='' />
            <!-- substitute the above value with a valid token -->
            <input type="submit" value="Bootstrap" />
        </form>
    </body>
</html>

