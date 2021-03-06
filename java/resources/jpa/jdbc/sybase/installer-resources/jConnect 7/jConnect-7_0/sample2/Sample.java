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
import com.sybase.jdbcx.*;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * The Sample class is an abstract class subclassed by all 
 * of the sample programs within the JDBC_HOME/sample/ directory
 * It contains various methods used by all samples, as
 * well as the abstract sampleCode method, which is
 * defined within each sample in order to demonstrate how to  
 * perform a specific task.
 * This enables the code to be clear, and not burden
 * the user with setup.
 *
 * Sample may be invoked with the optional parameters:<br>
 * -U username<br>
 * -P password<br>
 * -D debuglibraries<br>
 * -S server<br>
 * ..... /any other options may go here too<br>
 *
 */



public abstract class Sample extends java.lang.Thread
{
    Connection _con;
    static String _query;
    static String _className; //name of the sample class to be run
    static String _dir = "";
    static boolean _anApplet = false;
    static CommandLine _cmdline;
    static SybSample _sybSample;
    static SybDriver _sybDriver;

    /**
     * Allow for any extra settings passed in by the SybSample driver
     * to be made before executing the sample
     * @param sybSample  so that the streams can be set to the specific 
     *      SybSample object that called it 
     * @param dir  If an applet, the document base: http://<host>:<port>/sample.
     *      Otherwise it will be whichever directory we are running from: 
     *      either JDBC_HOME/sample or JDBC_HOME
     * @param anApplet  whether we are running these samples as applets or 
     *      applications.  This is important for those applications trying to 
     *      access files for reading.
     * @cmndline - command line ops passed in for connection.
     */
    public void settings(SybSample sybSample, SybDriver sybDriver, String dir, boolean anApplet, CommandLine cmdline)
    {
        _dir = dir;
        _anApplet = anApplet;
        _cmdline = cmdline;
        _sybSample = sybSample;
        _sybDriver = sybDriver;
    }
    // Because of threading, we need to keep track of what SybSample
    // we were sending output to; otherwise the first sample's 
    // output streams will be garbled.
    /**
     * @see SybSample#output
     * @param message  a string you want output to SybSample.output
     */
    public void output(String message)
    {
        _sybSample.output(message);
    }
    /**
     * @see SybSample#error
     * @param message  a string you want output to SybSample.error
     */
    public void error(String message)
    {
        _sybSample.error(message);
    }
    /**
     * Executes the sample by registering the driver, making a connection, 
     * allowing for adding more connection properties, running the sample
     * specific code, checking for warnings, and finally closing the connection
     */
    public void run()
    {
        try 
        {
            addMoreProps(_cmdline);

            // Attempt to connect to a driver.
            _con = DriverManager.getConnection(
                _cmdline._props.getProperty("server"), _cmdline._props);

            // If we were unable to connect, an exception
            // would have been thrown.  So, if we get here,
            // we are successfully connected to the URL


            // Check for and display any warnings generated by the connect.
            checkForWarning (_con.getWarnings ());
            // Run the sample specific code
            sampleCode();
        }
        catch (SQLException ex) 
        {
            // A SQLException was generated.  Catch it and
            //  display the error information.  Note that there
            //  could be multiple error objects chained together
            displaySQLEx(ex);
        }
        catch (java.lang.Exception ex) 
        {
            // Got some other type of exception.  Dump it.
            ex.printStackTrace ();
        }
        finally
        {
            try
            {
                // Close the connection
                if (_con != null)
                _con.close();
            }
            catch(SQLException sqe) 
            {
            }
            SybSample.error(" Closing connection on sample\n");
            SybSample.error(" Sample Finished Executing\n");
            stop();
        }
    }

    /**
     * This is the main code of each sample program,
     * which will be implemented differently within 
     * each subclass of Sample.<p> 
     */
    public abstract void  sampleCode();


    /**
     * Checks for and displays warnings.  Returns true if a warning
     * was found<br>
     * @param warn SQLWarning object
     * @exception SQLException .<p>
     */
    public boolean checkForWarning (SQLWarning warn) throws SQLException
    {
        boolean rc = false;

        // If a SQLWarning object was given, display the warning messages.  
        // Note that there could be multiple warnings chained together

        if (warn != null) 
        {
            SybSample.error("\n *** Warning ***\n");
            rc = true;
            while (warn != null) 
            {
                SybSample.error("SQLState: " + warn.getSQLState ()+"\n");
                SybSample.error("Message:  " + warn.getMessage ()+"\n");
                SybSample.error("Vendor:   " + warn.getErrorCode ()+"\n");
                warn = warn.getNextWarning ();
            }
        }
        return rc;
    }
    /**
     * This method displays driver name and version.
     * @return None
     * @exception SQLException .
     */
    public void printDriverInfo()
        throws SQLException
    {
        // Get the DatabaseMetaData object and display
        // some information about the connection
        DatabaseMetaData dma = _con.getMetaData();

        SybSample.output("Driver       " + dma.getDriverName() + "\n");
        SybSample.output("Version      " + dma.getDriverVersion() + "\n");
        SybSample.output("-------------------------------------\n");

    }
    // end of printDriverInfo


    /**
     * Execute a DDL or a DML statement that does not return a ResultSet.<br>
     * @param cmd  the command to execute<p>
     * @exception SQLException .
     */
    public void execDDL(String cmd) throws SQLException
    {
        Statement stmt = _con.createStatement();
        SybSample.error("Executing: " + cmd +"\n");
        stmt.executeUpdate(cmd);
        checkForWarning(stmt.getWarnings());
        stmt.close();
    }


    /**
     * Process a ResultSet displaying all of the rows and columns.  Also
     * use ResultSetMetaData to obtain the column headers<br>
     * @param rs  the current result set<p>
     * @exception SQLException .
     */
    public void dispResultSet (ResultSet rs) throws SQLException
    {
        int i;

        // Get the ResultSetMetaData.  This will be used for the column headings

        ResultSetMetaData rsmd = rs.getMetaData ();

        //  Get the number of columns in the result set

        int numCols = rsmd.getColumnCount ();

        // Display column headings

        for (i=1; i<=numCols; i++) 
        {
            if (i > 1)  
            SybSample.output("\t\t");
            SybSample.output(rsmd.getColumnLabel(i));
        }
        SybSample.output("\n");

        // Display data, fetching until end of the result set

        while (rs.next ()) 
        {
            // Loop through each column, getting the column data and displaying
            for (i=1; i<=numCols; i++) 
            {
                if (i > 1) 
                SybSample.output("\t\t");

                String foobar = rs.getString(i);
                if(rs.wasNull())
                SybSample.output("NULL");
                else
                SybSample.output(foobar);
            }
            SybSample.output("\n");

            // Fetch the next result set row

        }
    }
    /**
     *  A SQLException was generated.  Catch it and
     *  display the error information.  Note that there
     *  could be multiple error objects chained
     *  together.<br>
     *  @param ex  info will be displayed for this exception <p>
     */
    public void displaySQLEx(SQLException ex)
    {
        SybSample.error("\n*** SQLException caught ***\n");
        while (ex != null) 
        {
            SybSample.error("SQLState: " + ex.getSQLState ()+"\n");
            SybSample.error("Message:  " + ex.getMessage ()+"\n");
            SybSample.error("Vendor:   " + ex.getErrorCode ()+"\n");
            ex = ex.getNextException ();
            SybSample.error("\n");
        }
    }

    /**
     * Location where you can add commandline properties to the connection
     * The Super class will call this function before creating the
     * connection
     * @param cmdline commandline options
     * @see CommandLine
     */
    public void addMoreProps(CommandLine cmdline) 
    {
        //leave it up to child to fill it in
    }
}
