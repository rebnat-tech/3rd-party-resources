/*
Confidential property of Sybase, Inc.
Copyright 2001, 2012
Sybase, Inc.  All rights reserved.
Unpublished rights reserved under U.S. copyright laws.

This software contains confidential and trade secret information of Sybase,
Inc.   Use,  duplication or disclosure of the software and documentation by
the  U.S.  Government  is  subject  to  restrictions set forth in a license
agreement  between  the  Government  and  Sybase,  Inc.  or  other  written
agreement  specifying  the  Government's rights to use the software and any
applicable FAR provisions, for example, FAR 52.227-19.
Sybase, Inc. One Sybase Drive, Dublin, CA 94568, USA
*/

package sample2;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * MyDriver class Demonstrates the use of the Driver and DriverPropertyInfo
 * classes.<br>
 *
 * <P>MyDriver may be invoked with the optional parameters:<br>
 * -U username<br>
 * -P password<br>
 * -D debuglibraries<br>
 * -S server<p>
 *
 *  @see Sample
 */
public class MyDriver extends Sample
{
    static Driver _driver = null; 
    static String _url = null; 
    static Properties _props = null;

    MyDriver()
    {
        super();
    }

    public void run()
    {

        try
        {

            addMoreProps(_cmdline);

            // Save off the data we will need in SampleCode()

            _url = _cmdline._props.getProperty("server");
            _props = _cmdline._props;

            // Load the Driver

            DriverManager.registerDriver((Driver)
                Class.forName("com.sybase.jdbc4.jdbc.SybDriver").newInstance());

            _driver = DriverManager.getDriver(_url);


            //Connect using the Driver.connect()

            _con = _driver.connect(_url, _props);

            // Check for, and display and warnings generated by the connect.
            checkForWarning (_con.getWarnings ());

            // Run the sample specific code
            sampleCode();

            // Close the connection
            _con.close();
        }
        catch (SQLException ex)
        {
            displaySQLEx(ex);
        }
        catch (java.lang.Exception ex)
        {
            /*
          *  Got some other type of exception.  Dump it.
          */
            ex.printStackTrace ();
        }
    }

    public void sampleCode()
    {


        try
        {

            // Display the key info from the driver

            output("acceptsURL(" + _url +")= " +
                _driver.acceptsURL(_url) + "\n");
            output("getMajorVersion()= " + _driver.getMajorVersion() 
                + "\n");
            output("getMinorVersion()= " + _driver.getMinorVersion() 
                + "\n");
            output("jdbcCompliant()= " + _driver.jdbcCompliant() 
                + "\n");
            DriverPropertyInfo dpi[] = _driver.getPropertyInfo(
                _url, _props);

            // Display the jConnect properties

            output("jConnect Driver Properties\n");
            for (int i = 0; i < dpi.length; i++)
            {
                output(dpi[i].name +"\t\t");
                output(dpi[i].value +"\t\t");
                output(dpi[i].description  + "\n");
            }



        }
        catch (Exception ex)
        {

            ex.printStackTrace ();
        }
    }
}
