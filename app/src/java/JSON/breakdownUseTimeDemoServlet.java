/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JSON;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import is203.JWTException;
import is203.JWTUtility;
import is203.controller.breakdownbytimedemoservlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Bingda
 */
@WebServlet(name = "breakdownUseTimeDemoServlet", urlPatterns = {"/json/basic-usetime-demographics-report"})
public class breakdownUseTimeDemoServlet extends HttpServlet {

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    Date date1;
    Date date2;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException, ParseException {
        response.setContentType("application/JSON");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            breakdownbytimedemoservlet bServlet = new breakdownbytimedemoservlet();
            //ArrayList<LinkedHashMap<String, ArrayList<String, ArrayList<String[]>>> toReturn = bServlet.processBreakDownByTimeDemo()
            ArrayList<String> errorList = new ArrayList<String>();
            String order = request.getParameter("order");
            JsonObject results = new JsonObject();
            JsonArray resultsArr = new JsonArray();
            JsonArray errorArr = new JsonArray();
            //JsonArray tempArr = new JsonArray();
            JsonObject temp = new JsonObject();
            String currentToken = null;
            JsonObject problems = new JsonObject();
            HttpSession session = request.getSession();
            String startDate = request.getParameter("startdate");
            String token = request.getParameter("token");
            String endDate = request.getParameter("enddate");
            System.out.println(startDate);
            System.out.println(endDate);

            String error = "";

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (token == null) {
                errorList.add("missing token");
            } else if (request.getHeader("referer") == null) {
                currentToken = (String) request.getParameter("token");
            } else if (token.equals("") || token.isEmpty()) {
                errorList.add("blank token");
            } else {
                try {
                    JWTUtility.verify(currentToken, sharedSecretManager.getSharedSecret());
                } catch (JWTException e) {
                    errorList.add("invalid token");
                }
            }
            if (endDate == null) {
                errorList.add("missing end date");
            } else if (endDate.equals("") || endDate.isEmpty()) {
                errorList.add("blank enddate");
            }
            if (startDate == null) {
                errorList.add("missing start date");
            } else if (startDate.equals("") || startDate.isEmpty()) {
                errorList.add("blank startdate");
            } else {
                try {
                    date1 = sdf.parse(startDate);
                } catch (ParseException ex) {
                    errorList.add("invalid startdate");
                }
                try {
                    date2 = sdf.parse(endDate);

                    Calendar c = Calendar.getInstance();
                    c.setTime(date2);
                    c.add(Calendar.DATE, 1);
                    date2 = c.getTime();
                    long t = date2.getTime();
                    date2 = new Date(t - 1000);

                    // Instance 1: Date 1 after Date 2
                    // Instance 2: Date 2 after today's date
                    if (date1.after(date2)) {
                        errorList.add("invalid startdate");
                    }

                    //int sort1;//Integer.parseInt(sortBy1);
                } catch (ParseException ex) {
                    errorList.add("invalid enddate");

                }
            }

            if (order != null) {
                if (!order.contains(",")) {
                    if (!order.equals("gender") && !order.equals("year") && !order.equals("school") && !order.equals("cca")) {
                        errorList.add("invalid order");
                    }
                } else {
                    String[] orderArr = order.split(",");
                    for (String s : orderArr) {
                        if (!s.equals("gender") && !s.equals("year") && !s.equals("school") && !s.equals("cca")) {
                            errorList.add("invalid order");
                        }
                    }
                }
            }

            if (errorList.size() != 0) {
                for (String s : errorList) {
                    JsonPrimitive element = new JsonPrimitive(s);
                    errorArr.add(element);
                }
                problems.addProperty("status", "error");
                problems.add("breakdown", errorArr);
                out.println(gson.toJson(problems));
            } else {
                ArrayList<LinkedHashMap<String, ArrayList<String[]>>> toReturn = bServlet.processBreakDownByTimeDemo(date1, date2, order, request, response);
                TreeMap<String, ArrayList<String[]>> map1 = null;
                TreeMap<String, ArrayList<String[]>> map2 = null;
                TreeMap<String, ArrayList<String[]>> map3 = null;
                TreeMap<String, ArrayList<String[]>> map4 = null;
                LinkedHashMap<String, ArrayList<String[]>> bd1 = toReturn.get(0);
                if (bd1 != null) {
                    map1 = new TreeMap<String, ArrayList<String[]>>(bd1);
                }

                LinkedHashMap<String, ArrayList<String[]>> bd2 = toReturn.get(1);
                if (bd2 != null) {
                    map2 = new TreeMap<String, ArrayList<String[]>>(bd2);
                }
                LinkedHashMap<String, ArrayList<String[]>> bd3 = toReturn.get(2);
                if (bd3 != null) {
                    map3 = new TreeMap<String, ArrayList<String[]>>(bd3);
                }

                LinkedHashMap<String, ArrayList<String[]>> bd4 = toReturn.get(3);
                if (bd4 != null) {
                    map4 = new TreeMap<String, ArrayList<String[]>>(bd4);
                }

                LinkedHashMap<String, Integer> bauTimeDemoResults = new LinkedHashMap<String, Integer>();
                LinkedHashMap<String, ArrayList<String[]>> tempMap = new LinkedHashMap<String, ArrayList<String[]>>();

                if (bd4 != null) { //filtered by 4
                    tempMap = bd4;
                } else if (bd3 != null) { //filtered by 3
                    tempMap = bd3;
                } else if (bd2 != null) { // filtered by 2
                    tempMap = bd2;
                } else { //filtered by 1
                    tempMap = bd1;
                }

                Set<String> keyTempMap = tempMap.keySet();
                Iterator<String> iterTempMap = keyTempMap.iterator();
                int intense = 0;
                int normal = 0;
                int mild = 0;
                while (iterTempMap.hasNext()) {
                    intense = 0;
                    normal = 0;
                    mild = 0;
                    String key = iterTempMap.next();
                    ArrayList<String[]> arr = tempMap.get(key); // String[]->{macAdd2, usageCat, school, year, gender2, cca2}
                    for (String[] str : arr) {
                        String cat = str[1];
                        if (cat.equals("intense")) {
                            intense++;
                        } else if (cat.equals("normal")) {
                            normal++;
                        } else {
                            mild++;
                        }
                    }

                    bauTimeDemoResults.put(key + ";Mild", mild);
                    bauTimeDemoResults.put(key + ";Normal", normal);
                    bauTimeDemoResults.put(key + ";Intense", intense);
                }
                int totalNumOfUsers = 0;
                if (bauTimeDemoResults != null) {
                    for (String key : bauTimeDemoResults.keySet()) {
                        totalNumOfUsers += bauTimeDemoResults.get(key);
                    }
                    if (totalNumOfUsers == 0) {
                        totalNumOfUsers = 1;
                    }
                }

                if (map4 != null) {
                    for (String key1 : map1.keySet()) {
                        String[] keyArr1 = key1.split("[;]");
                        int currentNum1 = map1.get(key1).size();
                        temp = new JsonObject();
                        JsonArray tempArr = new JsonArray();
                        String[] orderArr = order.split(",");
                        String first = orderArr[0];
                        String firstAns = keyArr1[1];
                        if (firstAns.equals("Male")) {
                            firstAns = ("M");
                        } else if (firstAns.equals("Female")) {
                            firstAns = ("F");
                        }
                        temp.addProperty(first, firstAns);
                        temp.addProperty("count", currentNum1);
                        temp.addProperty("percent", (Math.round((double) currentNum1 / totalNumOfUsers * 100)));
                        temp.add("breakdown", tempArr);

                        for (String key2 : map2.keySet()) {
                            if (key2.contains(key1)) {
                                JsonObject temp2 = new JsonObject();
                                JsonArray tempArr2 = new JsonArray();
                                String[] keyArr2 = key2.split("[;]");
                                int currentNum2 = map2.get(key2).size();

                                String second = orderArr[1];
                                String secondAns = keyArr2[2];
                                if (secondAns.equals("Male")) {
                                    secondAns = ("M");
                                } else if (secondAns.equals("Female")) {
                                    secondAns = ("F");
                                }
                                temp2.addProperty(second, secondAns);
                                temp2.addProperty("count", currentNum2);
                                temp2.addProperty("percent", (Math.round((double) currentNum2 / totalNumOfUsers * 100)));
                                tempArr.add(temp2);
                                temp2.add("breakdown", tempArr2);

                                for (String key3 : map3.keySet()) {
                                    if (key3.contains(key2)) {
                                        JsonObject temp3 = new JsonObject();
                                        JsonArray tempArr3 = new JsonArray();
                                        String[] keyArr3 = key3.split(";");
                                        int currentNum3 = map3.get(key3).size();
                                        String third = orderArr[2];
                                        String thirdAns = keyArr3[3];
                                        if (thirdAns.equals("Male")) {
                                            thirdAns = ("M");
                                        } else if (thirdAns.equals("Female")) {
                                            thirdAns = ("F");
                                        }
                                        temp3.addProperty(third, thirdAns);
                                        temp3.addProperty("count", currentNum3);
                                        temp3.addProperty("percent", (Math.round((double) currentNum3 / totalNumOfUsers * 100)));
                                        tempArr2.add(temp3);
                                        temp3.add("breakdown", tempArr3);
                                        int count = 0;
                                        for (String key4 : map4.keySet()) {
                                            if (key4.contains(key3)) {
                                                count++;
                                                String[] keyArr4 = key4.split(";");
                                                int currentNum4 = map4.get(key4).size();

                                                int mild2 = bauTimeDemoResults.get(key4 + ";Mild");
                                                int normal2 = bauTimeDemoResults.get(key4 + ";Normal");
                                                int intense2 = bauTimeDemoResults.get(key4 + ";Intense");
                                                JsonObject temp4 = new JsonObject();
                                                JsonArray tempArr4 = new JsonArray();

                                                String forth = orderArr[3];
                                                String forthAns = keyArr4[4];
                                                if (forthAns.equals("Male")) {
                                                    forthAns = ("M");
                                                } else if (forthAns.equals("Female")) {
                                                    forthAns = ("F");
                                                }
                                                temp4.addProperty(forth, keyArr4[4]);
                                                temp4.addProperty("count", currentNum4);
                                                temp4.addProperty("percent", (Math.round((double) currentNum4 / totalNumOfUsers * 100)));
                                                tempArr3.add(temp4);
                                                temp4.add("breakdown", tempArr4);
                                                JsonObject intense1 = new JsonObject();
                                                JsonObject normal1 = new JsonObject();
                                                JsonObject mild1 = new JsonObject();
                                                intense1.addProperty("intense-count", intense2);
                                                intense1.addProperty("intense-percent", (Math.round((double) intense / totalNumOfUsers * 100)));
                                                normal1.addProperty("normal-count", normal2);
                                                normal1.addProperty("normal-percent", (Math.round((double) normal / totalNumOfUsers * 100)));
                                                mild1.addProperty("mild-count", mild2);
                                                mild1.addProperty("mild-percent", (Math.round((double) mild / totalNumOfUsers * 100)));
                                                tempArr4.add(intense1);
                                                tempArr4.add(normal1);
                                                tempArr4.add(mild1);

                                            }

                                        }
                                    }
                                }
                            }
                        }
                        resultsArr.add(temp);
                    }
                } else if (map3 != null) {
                    for (String key1 : map1.keySet()) {
                        String[] keyArr1 = key1.split("[;]");
                        int currentNum1 = map1.get(key1).size();
                        temp = new JsonObject();
                        JsonArray tempArr = new JsonArray();
                        String[] orderArr = order.split(",");
                        String first = orderArr[0];
                        String firstAns = keyArr1[1];
                        if (firstAns.equals("Male")) {
                            firstAns = ("M");
                        } else if (firstAns.equals("Female")) {
                            firstAns = ("F");
                        }
                        temp.addProperty(first, firstAns);
                        temp.addProperty("count", currentNum1);
                        temp.addProperty("percent", (Math.round((double) currentNum1 / totalNumOfUsers * 100)));
                        temp.add("breakdown", tempArr);

                        for (String key2 : map2.keySet()) {
                            if (key2.contains(key1)) {
                                JsonObject temp2 = new JsonObject();
                                JsonArray tempArr2 = new JsonArray();
                                String[] keyArr2 = key2.split("[;]");
                                int currentNum2 = map2.get(key2).size();
                                String second = orderArr[1];
                                String secondAns = keyArr2[2];
                                if (secondAns.equals("Male")) {
                                    secondAns = ("M");
                                } else if (secondAns.equals("Female")) {
                                    secondAns = ("F");
                                }
                                temp2.addProperty(second, secondAns);
                                temp2.addProperty("count", currentNum2);
                                temp2.addProperty("percent", (Math.round((double) currentNum2 / totalNumOfUsers * 100)));
                                //tempArr.add(temp2);
                                temp2.add("breakdown", tempArr2);
                                int count = 0;
                                for (String key3 : map3.keySet()) {
                                    if (key3.contains(key2)) {

                                        count++;
                                        JsonObject temp3 = new JsonObject();
                                        JsonArray tempArr3 = new JsonArray();
                                        String[] keyArr3 = key3.split(";");
                                        int currentNum3 = map3.get(key3).size();
                                        int mild2 = bauTimeDemoResults.get(key3 + ";Mild");
                                        int normal2 = bauTimeDemoResults.get(key3 + ";Normal");
                                        int intense2 = bauTimeDemoResults.get(key3 + ";Intense");
                                        String third = orderArr[2];
                                        String thirdAns = keyArr3[3];
                                        if (thirdAns.equals("Male")) {
                                            thirdAns = ("M");
                                        } else if (thirdAns.equals("Female")) {
                                            thirdAns = ("F");
                                        }
                                        temp3.addProperty(third, thirdAns);
                                        temp3.addProperty("count", currentNum3);
                                        temp3.addProperty("percent", (Math.round((double) currentNum3 / totalNumOfUsers * 100)));
                                        tempArr2.add(temp3);
                                        temp3.add("breakdown", tempArr3);
                                        JsonObject intense1 = new JsonObject();
                                        JsonObject normal1 = new JsonObject();
                                        JsonObject mild1 = new JsonObject();
                                        intense1.addProperty("intense-count", intense2);
                                        intense1.addProperty("intense-percent", (Math.round((double) intense2 / totalNumOfUsers * 100)));
                                        normal1.addProperty("normal-count", normal2);
                                        normal1.addProperty("normal-percent", (Math.round((double) normal2 / totalNumOfUsers * 100)));
                                        mild1.addProperty("mild-count", mild2);
                                        mild1.addProperty("mild-percent", (Math.round((double) mild2 / totalNumOfUsers * 100)));
                                        tempArr3.add(intense1);
                                        tempArr3.add(normal1);
                                        tempArr3.add(mild1);
                                        tempArr.add(temp2);
                                    }

                                }
                            }
                        }
                        resultsArr.add(temp);
                    }
                } else if (map2 != null) {
                    for (String key1 : map1.keySet()) {
                        String[] keyArr1 = key1.split("[;]");
                        int currentNum1 = map1.get(key1).size();
                        temp = new JsonObject();

                        //JsonObject temp = new JsonObject();
                        JsonArray tempArr = new JsonArray();
                        String[] orderArr = order.split(",");
                        String first = orderArr[0];
                        String firstAns = keyArr1[1];
                        if (firstAns.equals("Male")) {
                            firstAns = ("M");
                        } else if (firstAns.equals("Female")) {
                            firstAns = ("F");
                        }
                        temp.addProperty(first, firstAns);
                        temp.addProperty("count", currentNum1);
                        temp.addProperty("percent", (Math.round((double) currentNum1 / totalNumOfUsers * 100)));
                        //System.out.println(("here" + Math.round((double) currentNum1 / totalNumOfUsers * 100)));
                        temp.add("breakdown", tempArr);

                        for (String key2 : map2.keySet()) {

                            if (key2.contains(key1)) {
                                JsonObject temp2 = new JsonObject();
                                JsonArray tempArr2 = new JsonArray();
                                String[] keyArr2 = key2.split("[;]");

                                int currentNum2 = map2.get(key2).size();
                                int mild2 = bauTimeDemoResults.get(key2 + ";Mild");
                                int normal2 = bauTimeDemoResults.get(key2 + ";Normal");
                                int intense2 = bauTimeDemoResults.get(key2 + ";Intense");
                                String second = orderArr[1];
                                String secondAns = keyArr2[2];
                                if (secondAns.equals("Male")) {
                                    secondAns = ("M");
                                } else if (secondAns.equals("Female")) {
                                    secondAns = ("F");
                                }
                                temp2.addProperty(second, secondAns);
                                temp2.addProperty("count", currentNum2);
                                temp2.addProperty("percent", (Math.round((double) currentNum2 / totalNumOfUsers * 100)));

                                //temp2.add("breakdown", tempArr2);
                                JsonObject intense1 = new JsonObject();
                                JsonObject normal1 = new JsonObject();
                                JsonObject mild1 = new JsonObject();
                                intense1.addProperty("intense-count", intense2);
                                intense1.addProperty("intense-percent", (Math.round((double) intense2 / totalNumOfUsers * 100)));
                                normal1.addProperty("normal-count", normal2);
                                normal1.addProperty("normal-percent", (Math.round((double) normal2 / totalNumOfUsers * 100)));
                                mild1.addProperty("mild-count", mild2);
                                mild1.addProperty("mild-percent", (Math.round((double) mild2 / totalNumOfUsers * 100)));
                                tempArr2.add(intense1);
                                tempArr2.add(normal1);
                                tempArr2.add(mild1);
                                temp2.add("breakdown", tempArr2);
                                tempArr.add(temp2);

                            }
                        }

                        resultsArr.add(temp);

                    }

                } else if (map1 != null) {
                    for (String key1 : map1.keySet()) {
                        String[] keyArr1 = key1.split("[;]");

                        int currentNum1 = map1.get(key1).size();
                        int mild2 = bauTimeDemoResults.get(key1 + ";Mild");
                        int normal2 = bauTimeDemoResults.get(key1 + ";Normal");
                        int intense2 = bauTimeDemoResults.get(key1 + ";Intense");
                        temp = new JsonObject();
                        JsonArray tempArr = new JsonArray();
                        String[] orderArr = order.split(";");
                        String first = orderArr[0];
                        String firstAns = keyArr1[1];
                        if (firstAns.equals("Male")) {
                            firstAns = ("M");
                        } else if (firstAns.equals("Female")) {
                            firstAns = ("F");
                        }
                        temp.addProperty(first, firstAns);
                        temp.addProperty("count", currentNum1);
                        temp.addProperty("percent", (Math.round((double) currentNum1 / totalNumOfUsers * 100)));
                        temp.add("breakdown", tempArr);
                        JsonObject intense1 = new JsonObject();
                        JsonObject normal1 = new JsonObject();
                        JsonObject mild1 = new JsonObject();
                        intense1.addProperty("intense-count", intense2);
                        intense1.addProperty("intense-percent", (Math.round((double) intense2 / totalNumOfUsers * 100)));
                        normal1.addProperty("normal-count", normal2);
                        normal1.addProperty("normal-percent", (Math.round((double) normal2 / totalNumOfUsers * 100)));
                        mild1.addProperty("mild-count", mild2);
                        mild1.addProperty("mild-percent", (Math.round((double) mild2 / totalNumOfUsers * 100)));
                        tempArr.add(intense1);
                        tempArr.add(normal1);
                        tempArr.add(mild1);
                        resultsArr.add(temp);
                    }
                }
            }
            results.addProperty("status", "success");
            // resultsArr.add(temp);
            results.add("breakdown", resultsArr);
            out.println(gson.toJson(results));
        }
    }

// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);

        } catch (SQLException ex) {
            Logger.getLogger(breakdownUseTimeDemoServlet.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(breakdownUseTimeDemoServlet.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);

        } catch (SQLException ex) {
            Logger.getLogger(breakdownUseTimeDemoServlet.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(breakdownUseTimeDemoServlet.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
