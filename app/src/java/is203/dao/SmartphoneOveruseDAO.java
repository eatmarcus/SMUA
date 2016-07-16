/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package is203.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SmartphoneOveruseDAO {
    
    /**
     * Retrieves Mac-Address from SQL given Username
     * @param username
     * @return String Mac-Address
     */
    public String retrieveMacAdd(String username) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String macAdd = null;

        try {
            conn = ConnectionManager.getConnection();
            String query = "select mac_address from demographics where email like '" + username + "%'";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                macAdd = rs.getString(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, rs);
            return macAdd;
        }
    }
    /**
     * Given start date & end date and mac-address
     * retrieve all the relevant timestamps from SQL
     * @param macAdd
     * @param date1
     * @param date2
     * @return an ArrayList containing String [] of timestamp and App-Id
     */
    public ArrayList<String[]> retrieveTimestamps(String macAdd, Date date1, Date date2) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<String[]> userList = new ArrayList<>();
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String d1 = df.format(date1);
            String d2 = df.format(date2);

            conn = ConnectionManager.getConnection();
            String query = "select timestamp, app_id from app where (timestamp between '" + d1 + "' and '" + d2 + "') and mac_address = '" + macAdd + "' GROUP BY timestamp, app_id";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                userList.add(new String[]{rs.getString(1), rs.getString(2)});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userList;
    }
    
    /**
     * Given start date, end date and Mac-Address
     * retrieve timestamps of the user using apps
     * that fall under the category of "Games" from SQL
     * @param macAdd
     * @param date1
     * @param date2
     * @return an ArrayList containing Strings of timestamps
     */
    public ArrayList<String> retrieveGameTimeStamps(String macAdd, Date date1, Date date2) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<String> toReturn = new ArrayList<>();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String d1 = df.format(date1);
        String d2 = df.format(date2);

        try {
            conn = ConnectionManager.getConnection();
            String query = "select a.timestamp from app a, applookup alu where a.app_id = alu.app_id and (timestamp between '" + d1 + "' and '" + d2 + "') and mac_address = '" + macAdd + "' and alu.app_category ='Games' GROUP BY timestamp";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                toReturn.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return toReturn;
    }
}
