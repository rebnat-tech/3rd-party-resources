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

import java.sql.*;
import java.util.*;

// need DataSource from the JDBC 2.0 Optional Package
import javax.sql.DataSource;

// need JNDI Contexts to do lookup
import javax.naming.Context;
import javax.naming.InitialContext;

/**
  * The purpose of this sample is to outline how to use
  * JNDI to obtain JDBC connections using the DataSource
  * interface from the JDBC 2.0 Optional Package. <P>
  *
  * Note that this code is not expected to excecute 
  * successfully until you have set up your environment.
  * For this particular sample, you must: <OL>
  * <LI> Set up an LDAP server
  * <LI> Create an entry on the LDAP server using the
  *      Sybase OIDs
  * <LI> Change the Context.PROVIDER_URL property to
  *      point to your LDAP server
  * <LI> Change the lookup() call to use whatever
  *      search criteria reflects the entry you
  *      created in #2 (above).
  * </OL>
  *
  * This code assumes the LDAP server is running on
  * host 'some_ldap_server' and listening on port 238.
  * It assumes there is an entry with at least this
  * much information (LDIF format):
  *
  * <PRE>
  * dn:servername=myASE, o=MyCompany, c=US
  * 1.3.6.1.4.1.897.4.2.5:TCP#1# mymachine 4000
  * 1.3.6.1.4.1.897.4.2.10:user=me&password=mine
  * objectclass: sybaseServer
  * </PRE>
  *
  * This code differs from most of the other samples
  * in that there are no references to Sybase.  There
  * are no com.sybase.jdbcx interfaces, and there is no
  * JDBC URL with "jdbc:sybase:Tds:hostname:2638".
  * While there are String references to Sybase and Sun
  * in <B>this</B> example, those properties would
  * typically be set as part of the system environment.
  * They only exist in this sample to illustrate what
  * the properties are and how they should be specified.<P>
  *
  * Also note the import statements.  While no Sybase
  * classes are needed, you must download JNDI and the
  * JDBC 2.0 Optional Package, and put them in your
  * classpath in order to compile and run this sample.<P>
  *
  * For more information, please see the <I>jConnect
  * Programmer's Reference</I>.
  */
public class SimpleDataSource extends Sample
{

    public SimpleDataSource()
    {
        // calls super()
    }

    public void sampleCode()
    {

        DataSource dataSource = null;

        // configure the JNDI environment.

        // To keep clients simpler and more reusable, 
        // it is recommended that these properties are
        // set by your environment.  

        Properties props = new Properties();
        props.put(Context.PROVIDER_URL,
            "ldap://some_ldap_server:238/o=MyCompany,c=US");
        props.put(Context.OBJECT_FACTORIES, 
            "com.sybase.jdbc4.jdbc.SybObjectFactory");
        props.put(Context.INITIAL_CONTEXT_FACTORY, 
            "com.sun.jndi.ldap.LdapCtxFactory");

        // others ?? you might want to specify 
        // some security properties or the preferred
        // language...

        try
        {
            output("accessing JNDI\n");

            // If the properties are set by your
            // environment, then this call can become:
            // Context ctx = new InitialContext();
            // which will default to the system properties.
            Context ctx = new InitialContext(props);

            // Using the JNDI properties, pass in the
            // String name identifying the object to
            // be returned.  Since the environment for this
            // sample is using LDAP and the SybObjectFactory,
            // the lookup string is a Relative Distinguished
            // Name (RDN).  The LDAP entry is located by 
            // applying this search criteria and the 
            // Context.PROVIDER_URL setting.  Then, the
            // InitialContext connects to the ldap server and 
            // locates the entry as: 
            // ldap://some_ldap_server:238/servername=myASE,o=MyCompany,c=US
            // Then, that DirContext and the Name for that entry
            // are passed into the SybObjectFactory.  The factory
            // uses that information to construct a new DataSource
            // which is returned from lookup().
            dataSource = (DataSource) ctx.lookup("servername=myASE");
        }
        catch (Exception e)
        {
            error("rats! JNDI lookup failed. " +
                "Your environment is not set up properly.\n" +
                e);

            return;
        }

        output("DataSource reference obtained.\n");

        String query = "SELECT * FROM sysusers";
        try
        {

            // using the DataSource reference retrieved
            // from JNDI, simply request a JDBC connection.
            // Note that you can override the default
            // user and password for this DataSource by
            // using the other getConnection method:
            // Connection conn = dataSource.getConnection("someone", "else");
            Connection conn = dataSource.getConnection();

            output("Connection established.\n");

            // at this point, a typical JDBC connection
            // has been retrieved.  Use the JDBC API
            // just like if the Connection was obtained
            // from the DriverManager.
            Statement stmt = conn.createStatement();

            output("Executing: " + query + "\n\n");
            ResultSet rs = stmt.executeQuery (query);
            dispResultSet(rs);

            conn.close();
            output("\nConnection closed.\n");
        }
        catch (SQLException sqe)
        {
            displaySQLEx(sqe);
        }

        output("done!\n");
    }
}
