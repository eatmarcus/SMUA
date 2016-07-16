<%    String name1 = "";
    if (session.getAttribute("email") != null) {
        name1 = (String) session.getAttribute("email");
    }
%>

<!-- Sidebar -->
<div id="sidebar-wrapper">
    <ul class="sidebar-nav">
        <li class="sidebar-brand a">
            SMUA
        </li>
        <li class="img">
            <img src="img/download.jpg" class="img-circle" width="100" height="100" >
        </li>
        <li class ="img-caption">
            <span><%=name1%></span>
        </li>
        <li class ="text">
            <a href="studentlogin.jsp">Home</a>
        </li>
        <li class ="text-a">
            <a href="basicappusageform.jsp">Basic App Usage</a>
        </li>
        <li class ="text-a">
            <a href="topKAppUsageForm.jsp">Top K App Usage</a>
        </li>
        <li class ="text-aa">
            <a href="SmartphoneOveruseForm.jsp">Smartphone Overuse</a>
        </li>
        <li class ="text">
            <a href= "logout.jsp">Logout</a>
        </li>
    </ul>
</div>
<!-- /#sidebar-wrapper -->                                