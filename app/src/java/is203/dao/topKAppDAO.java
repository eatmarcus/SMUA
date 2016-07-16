/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package is203.dao;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;

public class topKAppDAO {

    /**
     * Given start date, end date and school filter, retrieve email, app name
     * and timestamp that falls within start date, end date & matches filter
     *
     * @param date1
     * @param date2
     * @param chosenFilter
     * @return a LinkedHashMap with email as key and arraylist of String [] 
     * containing app name and time as value
     * @throws IOException
     */
    public LinkedHashMap<String, ArrayList<String[]>> getTopAppsWithFilterBySchool(Date date1, Date date2, String chosenFilter) throws IOException {
        LinkedHashMap<String, ArrayList<String[]>> emailLinkMap = new LinkedHashMap<String, ArrayList<String[]>>();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String stringDate1 = df.format(date1);
        String stringDate2 = df.format(date2);
        ResultSet rs = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        // In General: Required to print app_name and rank
        // Needs app_lookup and app

        try {
            Set<String> emailSet = emailLinkMap.keySet();
            Iterator<String> emailIter = emailSet.iterator();
            conn = ConnectionManager.getConnection();
            String query = "select email, app_name, timeStamp from applookup a1, app a, demographics d where (timestamp between  '" + stringDate1 + "' and '" + stringDate2 + "') and email like '%" + chosenFilter + "%' and a1.app_id = a.app_id and d.mac_address = a.mac_address ORDER BY email, timestamp asc";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {

                String email = rs.getString(1);
                String appName = rs.getString(2);

                String time = rs.getString(3);
                if (emailSet.contains(email)) {
                    ArrayList<String[]> currentList = emailLinkMap.get(email);
                    String[] temp = {appName, time};
                    currentList.add(temp);
                    emailLinkMap.put(email, currentList);
                } else {
                    String[] temp = {appName, time};
                    ArrayList<String[]> newList = new ArrayList<String[]>();
                    newList.add(temp);
                    emailLinkMap.put(email, newList);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return emailLinkMap;

    }

    /**
     * Given start date, end date and category filter retrieve mac-address, name
     * and timestamp from SQL
     *
     * @param date1
     * @param date2
     * @param chosenFilter
     * @return a LinkedHashMap with mac-address as key and an arraylist of 
     * string [] containing name & timestamp as value
     */
    public LinkedHashMap<String, ArrayList<String[]>> getTopKStudentsGivenCat(Date date1, Date date2, String chosenFilter) {
       Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String stringDate1 = df.format(date1);
        String stringDate2 = df.format(date2);
        LinkedHashMap<String, ArrayList<String[]>> macNameTimeAppMap = new LinkedHashMap<String, ArrayList<String[]>>();

        try {
            conn = ConnectionManager.getConnection();
            String query = "select d.mac_Address, name, timestamp, al.app_category from applookup al, app a, demographics d where al.app_id = a.app_id and d.mac_Address = a.mac_Address and timestamp between '" + stringDate1 + "' and '" + stringDate2 + "' order by d.mac_address, timestamp";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String macAddress = rs.getString(1);
                String name = rs.getString(2);
                String timeStamp = rs.getString(3);
                String appCat = rs.getString(4);
                //if there is no such macAddress in the map
                if (macNameTimeAppMap.get(macAddress) == null) {
                    String[] temp = {name, timeStamp,appCat};
                    //creating a new list to put into the map
                    ArrayList<String[]> newList = new ArrayList<String[]>();
                    // add the current array into the arrayList
                    newList.add(temp);
                    macNameTimeAppMap.put(macAddress, newList);
                } else {
                    ArrayList<String[]> currentList = macNameTimeAppMap.get(macAddress);
                    String[] temp = {name, timeStamp,appCat};
                    currentList.add(temp);
                    macNameTimeAppMap.put(macAddress, currentList);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return macNameTimeAppMap;
    }
    
    /**
     * Given start date, end date and category filter retrieve mac-address, 
     * email and timestamp from SQL to process top schools given category
     * @param date1
     * @param date2
     * @param chosenFilter
     * @return a LinkedHashMap with email as key and arrylist of string[] 
     * containing timestamp and app category as value
     * @throws IOException 
     */
    public LinkedHashMap<String, ArrayList<String[]>> getTopKSchoolsGivenCat(Date date1, Date date2, String chosenFilter) throws IOException {
        ResultSet rs = null;

        Connection conn = null;
        PreparedStatement stmt = null;

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String stringDate1 = df.format(date1);
        String stringDate2 = df.format(date2);
        LinkedHashMap<String, ArrayList<String[]>> emailTimeMap = new LinkedHashMap<String, ArrayList<String[]>>();
        int count = 0;
        try {
            conn = ConnectionManager.getConnection();
            String query = "select d.mac_Address, email, a.timestamp, al.app_category from applookup al, app a, demographics d where al.app_id = a.app_id and d.mac_Address = a.mac_Address and a.timestamp between '" + stringDate1 + "' and '" + stringDate2 + "' order by d.mac_address, timestamp";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {

                String email = rs.getString(2);
                String timeStamp = rs.getString(3);
                String appCat = rs.getString(4);
                if (emailTimeMap.get(email) != null) {

                    ArrayList<String[]> timeList = emailTimeMap.get(email);
                    String[] temp = {timeStamp, appCat};
                    timeList.add(temp);
                    emailTimeMap.put(email, timeList);
                } else {
                    ArrayList<String[]> newList = new ArrayList<String[]>();
                    String[] temp = {timeStamp, appCat};
                    newList.add(temp);
                    emailTimeMap.put(email, newList);
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return emailTimeMap;
    }
}
