package pt.evolute.dbtransfer;

import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

import pt.evolute.dbtransfer.analyse.Analyser;
import pt.evolute.dbtransfer.constrain.Constrainer;
import pt.evolute.dbtransfer.db.beans.ConnectionDefinitionBean;
import pt.evolute.dbtransfer.db.helper.HelperManager;
import pt.evolute.dbtransfer.db.jdbc.JDBCConnection;
import pt.evolute.dbtransfer.diff.Diff;
import pt.evolute.dbtransfer.transfer.AsyncStatement;
import pt.evolute.dbtransfer.transfer.Mover;

/**
 *
 * @author  lflores
 */
public class Main
{	
	public Main( Properties props )
		throws Exception
	{
		HelperManager.setProperties( props );
		System.out.println( "BEGIN: " + new Date() );
		long start = System.currentTimeMillis();
		
                ConnectionDefinitionBean srcBean = ConnectionDefinitionBean.loadBean( props, Constants.SOURCE_PROPS );
                ConnectionDefinitionBean dstBean = ConnectionDefinitionBean.loadBean( props, Constants.DESTINATION_PROPS );
                
                JDBCConnection.debug = "true".equalsIgnoreCase(props.getProperty( Constants.DEBUG ) );
		if( "true".equalsIgnoreCase(props.getProperty( Constants.ANALYSE ) ) )
		{
			System.out.println( "Analysing" );
			Analyser a = new Analyser( props, srcBean, dstBean );
			a.cloneDB();
		}
		if( "true".equalsIgnoreCase(props.getProperty( Constants.TRANSFER ) ) )
		{
            if( !"true".equalsIgnoreCase( props.getProperty( Constants.TRANSFER_CHECK_DEPS ) ) )
            {
				String s = props.getProperty( Constants.TRANSFER_THREADS );
				try
				{
					int i = Integer.parseInt( s );
					AsyncStatement.PARALLEL_THREADS = i;
				}
				catch( Exception ex )
				{
				}
            }
            System.out.println( "Transfering" );
            Mover m = new Mover( props, srcBean, dstBean );
            try
            {
                    m.moveDB();
            }
            catch( SQLException ex )
            {
                ex.printStackTrace( System.out );
                ex.printStackTrace();
//				ErrorLogger.logException( ex );
                throw ex.getNextException();
            }
		}
		if( "true".equalsIgnoreCase( props.getProperty( Constants.CONSTRAIN ) ) )
		{
			System.out.println( "Constraining" );
			Constrainer c = new Constrainer( props, srcBean, dstBean );
			c.constrainDB();
		}
		if( "true".equalsIgnoreCase( props.getProperty( Constants.DIFF ) ) )
		{
			System.out.println( "Diffing" );
			Diff d = new Diff( props, srcBean, dstBean );
			d.diffDb();
		}
			System.out.println( "END: " + new Date() );
			System.out.println( "Transfer took: " + ( System.currentTimeMillis() - start ) / 1000 + " seconds" );
	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
	{
		if( args.length != 1 )
		{
			System.err.println( "Usage: " + Main.class.getName() + " <props.file>" );
			System.exit( 1 );
		}
		else
		{
			try
			{
				System.out.println( "Loading props: " + args[ 0 ] );
				Properties p = new Properties();
				p.load( new FileInputStream( args[ 0 ] ) );
				p.list( System.out );
				new Main( p );
			}
			catch( Throwable th )
			{
				th.printStackTrace();
				th.printStackTrace( System.out );
				try
				{
					Thread.sleep( 1500 );
				}
				catch( InterruptedException ex )
				{
				}
				System.exit( 2 );				
			}
			System.exit( 0 );
		}
	}
	
}
