package sacephor;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/*
 *  Copyright (c) 2008 Nokia Siemens Networks. All rights reserved.
 *
 *  Revision History:
 *
 *  DATE/AUTHOR         COMMENT
 *  ---------------------------------------------------------------------
 *  xx.xx.xxxx/ESe                            
 */

/**
 * @author <a href="mailto:Weibing.zhao@nsn.com">Ivan zhao</a>
 */
public class SequenceGenerator
{
    /**
     * 
     */
    private static final String NEW_CONNECT_FLAG = "connect/re";

    /**
     * 
     */
    private static final String OBJECT_DATA_TAG = "_objectData";

    private static final String DATA_CHANGE_NOTIF_TAG = "DataChangeNotif";

    private static final String CONFIG_OPERATION_TYPE_TAG = "_configOperationType";

    private static final String REMOVE_INDICATOR = "_R_M_O_V_E_";

    private static final String ID_TAG = "_id";

    private static enum LogType
    {
        XML, LOG
    }

    private static final int PAUSE = 100;

    private static String FILTER_UNIT_ID = "";

    //the log type which used by generate sequence according different log format.
    private static LogType currentLogType = LogType.LOG;

    private static String LOG_FILE = "D:/SiteManager.log";//

    private static String SEQUENCE_FILE = "D:/Log_Debug.sequence";//

    /**
     * @param args
     */
    public static void main( String[] args ) throws Exception
    {
        BufferedReader fileReader = new BufferedReader( new FileReader( LOG_FILE ) );

        try
        {
            String logContent = readLogContentFromFile( fileReader );
            generateSequence( logContent );
        }
        finally
        {
            if( fileReader != null )
            {
                fileReader.close();
            }
        }
    }

    /**
     * @param fileReader
     * @return
     */
    private static String readLogContentFromFile( BufferedReader fileReader ) throws Exception
    {
        StringBuilder logBuffer = new StringBuilder();
        String lineData = null;
        while( ( lineData = fileReader.readLine() ) != null )
        {
            logBuffer.append( lineData + "\n" );
        }
        return logBuffer.toString();
    }

    public static void generateSequence( String logContent ) throws Exception
    {
        String[] logTraces = getLogItems( logContent, currentLogType );
        String[] latestConnenctionTraces = getLatestConnectionTraces( logTraces );
        writeSequence( latestConnenctionTraces );
    }

    /**
     * @param logTraces
     * @return
     */
    private static String[] getLatestConnectionTraces( String[] logTraces )
    {
        int connectionStarted = 0;
        int index = 0;
        for( String logTrace : logTraces )
        {
            logTrace = logTrace.trim();
            if( logTrace.contains( NEW_CONNECT_FLAG ) )
            {
                connectionStarted = index;
            }
            index++;
        }
        return Arrays.copyOfRange( logTraces, connectionStarted, logTraces.length );
    }

    /**
     * @param logTraces
     * @throws FileNotFoundException
     */
    private static void writeSequence( String[] logTraces ) throws FileNotFoundException
    {
        int sequenceId = 0;
        Map<String, ObjectModel> map = new HashMap<String, ObjectModel>();
        PrintWriter sequenceWriter = new PrintWriter( SEQUENCE_FILE );
        try
        {
            writeHeader( sequenceWriter );
            for( String logTrace : logTraces )
            {
                logTrace = logTrace.trim();
                if( !isDataChangeNotifLog( logTrace ) )//not data change notification
                {
                    continue;
                }
                String dataChangeNotifMessage = getWellFormatDataChangeNotifMessage( logTrace );
                ObjectModel objectModel = getDataChangeNotifObejctModel( map, dataChangeNotifMessage );
                if( filterSequenceByUnitId( logTrace ) )
                {
                    writeSequenceItem( sequenceWriter, ++sequenceId, objectModel.toString() );
                }
            }
            writeFooter( sequenceWriter );
        }
        finally
        {
            if( sequenceWriter != null )
            {
                sequenceWriter.close();
            }
        }
    }

    /**
     * @param logTrace
     * @return
     */
    private static boolean filterSequenceByUnitId( String logTrace )
    {
        return logTrace.contains( FILTER_UNIT_ID );
    }

    /**
     * @param logContent
     * @return
     */
    protected static String[] getLogItems( String logContent, LogType type )
    {
        if( type == LogType.LOG )
        {
            return getTextLogItems( logContent );
        }
        else if( type == LogType.XML )
        {
            return getXMLLogItems( logContent );
        }
        else
        {
            throw new UnsupportedOperationException( "current only support text and xml log format." );
        }
    }

    /**
     * @param logContent
     */
    protected static String[] getTextLogItems( String logContent )
    {
//        String dateFormat = "\\[[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}\\]\\s*\\[[0-9]{2}.[0-9]{2}.[0-9]{4}\\]";
        String dateFormat = "[0-9]{4}-[0-9]{2}-[0-9]{2}\\s*T[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}Z";

        return logContent.split( dateFormat );
    }

    /**
     * @param logContent
     * @return
     */
    private static String[] getXMLLogItems( String logContent )
    {
        String dataStartTag = "<![CDATA[";
        String dataEndTage = "]]>";
        String[] result = logContent.split( dataEndTage );
        for( int i = 0; i < result.length; i++ )
        {
            if( !result[i].contains( dataStartTag ) )
            {
                result[i] = "";
            }
            else
            {
                result[i] = result[i].substring( result[i].indexOf( dataStartTag ) + dataStartTag.length() );
            }
        }
        return result;
    }

    /**
     * @param sequenceWriter
     */
    protected static void writeHeader( PrintWriter sequenceWriter )
    {
        sequenceWriter.println( "<Sequence name=\"Log Debug - Data Change Notifications\">" );
        sequenceWriter.println( "<Pause>0</Pause>" );
    }

    /**
     * @param start
     * @return
     */
    protected static boolean isDataChangeNotifLog( String logTrace )
    {
        return logTrace.indexOf( "<" + DATA_CHANGE_NOTIF_TAG + ">" ) >= 0;
    }

    protected static String getWellFormatDataChangeNotifMessage( String logTrace )
    {
        String result = logTrace;
        int start = result.indexOf( "<" + DATA_CHANGE_NOTIF_TAG + ">" );
        int end = result.indexOf( "</" + DATA_CHANGE_NOTIF_TAG + ">" );
        if( end >= 0 )//full data change notification
        {
            end += ( "</" + DATA_CHANGE_NOTIF_TAG + ">" ).length();
        }
        else if( end < 0 )//partial data change notification
        {
            result = insertMissingObjectDataStartTag( result );
            String objectTypeTag = "</" + getUnitType( result ) + ">";
            result = appendMissingEndTag( result, objectTypeTag );
            result = appendMissingEndTag( result, "</" + OBJECT_DATA_TAG + ">" );
            result = appendMissingEndTag( result, "</" + DATA_CHANGE_NOTIF_TAG + ">" );
            end = result.length();
        }
        return adapterRemove( adapterAdd( result.substring( start, end ) ) );
    }

    /**
     * @param logTrace
     * @return
     */
    protected static String insertMissingObjectDataStartTag( String logTrace )
    {
        int objectDataIndex = logTrace.indexOf( "<" + OBJECT_DATA_TAG + ">" );
        if( objectDataIndex < 0 )
        {
            return logTrace.replace( "</" + CONFIG_OPERATION_TYPE_TAG + ">", "</" + CONFIG_OPERATION_TYPE_TAG + ">\n<" +
                OBJECT_DATA_TAG + ">" );
        }
        return logTrace;
    }

    /**
     * @param unitMessage
     * @return
     */
    protected static String getUnitType( String unitMessage )
    {
        int start = unitMessage.indexOf( "<" + OBJECT_DATA_TAG + ">" ) + ( "<" + OBJECT_DATA_TAG + ">" ).length();
        String subMessage = unitMessage.substring( start ).trim();
        int end = subMessage.indexOf( ">" );
        String unitType = subMessage.substring( 1, end ).trim();
        return unitType;
    }

    /**
     * @param logTrace
     * @return
     */
    protected static String appendMissingEndTag( String logTrace, String endTag )
    {
        if( logTrace.indexOf( endTag ) < 0 )
        {
            return logTrace + "\n" + endTag;
        }
        return logTrace;
    }

    /**
     * @param replace
     * @return
     */
    protected static String adapterAdd( String substring )
    {
        return substring.replace( "+<", "<" );
    }

    /**
     * @param adapterAdd
     * @return
     */
    protected static String adapterRemove( String substring )
    {
        String removeAttributeRegex = "-<\\w+[\\.\\w*]*\\w*/?>";
        Matcher matcher = Pattern.compile( removeAttributeRegex ).matcher( substring );
        String result = substring;
        while( matcher.find() )
        {
            String attributeName = matcher.group().replace( "-<", "" ).replace( "/>", "" ).replace( ">", "" );
            result =
                result.replace( "-<" + attributeName + "/>", "<" + REMOVE_INDICATOR + attributeName + "/>" ).replace(
                    "-<" + attributeName + ">", "<" + REMOVE_INDICATOR + attributeName + ">" ).replace(
                    "</" + attributeName + ">", "</" + REMOVE_INDICATOR + attributeName + ">" );
        }
        return result;
    }

    /**
     * @param map
     * @param objectModel
     */
    protected static ObjectModel getDataChangeNotifObejctModel( Map<String, ObjectModel> map,
                                                              String dataChangeNotifMessage )
    {
        Element dataChangeNotifElement = parseElementFromString( dataChangeNotifMessage );
        SequenceGenerator.ObjectModel objectModel = new SequenceGenerator.ObjectModel( dataChangeNotifElement );
        String identifyId = objectModel.getIdentifyId();
        if( currentLogType == LogType.LOG )
        {
            updateTextLogObjectModel( map, objectModel, identifyId );
        }
        else
        {
            updateXMLLogObjectModel( map, objectModel, identifyId );
        }
        return map.get( identifyId );
    }

    /**
     * @param map
     * @param objectModel
     * @param identifyId
     */
    protected static void updateTextLogObjectModel( Map<String, ObjectModel> map, SequenceGenerator.ObjectModel objectModel,
                                             String identifyId )
    {
        ObjectModel existObjectModel = map.get( identifyId );
        if( existObjectModel != null )
        {
            for( String attributeName : objectModel.getAttributeNames() )
            {
                if( attributeName.startsWith( REMOVE_INDICATOR ) )
                {
                    existObjectModel.removeAttribute( attributeName );
                }
                else
                {
                    existObjectModel.putAttribute( attributeName, objectModel.getAttributeValue( attributeName ) );
                }
            }
            existObjectModel.setOperationType( objectModel.getOperationType() );
        }
        else
        {
            map.put( identifyId, objectModel );
        }
    }

    /**
     * @param map
     * @param objectModel
     * @param identifyId
     */
    private static void updateXMLLogObjectModel( Map<String, ObjectModel> map, ObjectModel objectModel,
                                                 String identifyId )
    {
        map.put( identifyId, objectModel );
    }

    /**
     * @param sequenceId
     * @param map
     * @param sequenceWriter
     * @param objectModel
     * @return
     */
    protected static void writeSequenceItem( PrintWriter sequenceWriter, int sequenceId, String content )
    {
        sequenceWriter.println( "<Send>" );
        sequenceWriter.println( "<Message name=\"DataChangeNotification " + sequenceId + "\" spontaneous=\"yes\">" );
        sequenceWriter.println( content );
        sequenceWriter.println( "</Message>" );
        sequenceWriter.println( "</Send>" );
        sequenceWriter.println( "<Pause>" + PAUSE + "</Pause>" );
        sequenceWriter.println( "" );
    }

    /**
     * @param sequenceWriter
     */
    protected static void writeFooter( PrintWriter sequenceWriter )
    {
        sequenceWriter.println( "</Sequence>" );
    }

    private static final Element parseElementFromString( String nodeString )
    {
        InputSource source = new InputSource( new StringReader( nodeString ) );
        Document doc = null;
        try
        {
            DocumentBuilderFactory docBF = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBF.newDocumentBuilder();
            doc = docBuilder.parse( source );
            doc.getDocumentElement().normalize();
            return doc.getDocumentElement();
        }
        catch( Exception ex )
        {
            ex.printStackTrace();
        }
        return null;
    }

    static class ObjectModel
    {
        /**
         * 
         */
        private static final String CONFIG_OBJECT_TYPE_TAG = "_configObjectType";

        /**
         * 
         */
        private static final String CONFIG_OBJECT_ID_TAG = "_configObjectId";

        private String operationType = "";

        private String configObjectId = "";

        private String configObjectType = "";

        private String objectType = "";

        private String objectId = "";

        private boolean isObjectModel = false;

        private Map<String, String> attributes = new HashMap<String, String>();

        public ObjectModel( Element xmlNode )
        {
            NodeList nodes = xmlNode.getChildNodes();
            for( int i = 0; i < nodes.getLength(); i++ )
            {
                Node node = nodes.item( i );
                if( CONFIG_OPERATION_TYPE_TAG.equals( node.getNodeName() ) )
                {
                    operationType = node.getTextContent().trim();
                }
                //
                else if( CONFIG_OBJECT_ID_TAG.equals( node.getNodeName() ) )
                {
                    configObjectId = node.getTextContent().trim();
                }
                else if( CONFIG_OBJECT_TYPE_TAG.equals( node.getNodeName() ) )
                {
                    configObjectType = node.getTextContent().trim();
                }
                //
                else if( OBJECT_DATA_TAG.equals( node.getNodeName() ) )
                {
                    isObjectModel = true;
                    NodeList objectNodes = node.getChildNodes();
                    for( int j = 0; j < objectNodes.getLength(); j++ )
                    {
                        Node objectNode = objectNodes.item( j );
                        if( objectNode.getNodeName().contains( "#text" ) )
                        {
                            continue;
                        }
                        praseObjectModel( objectNode );
                    }
                }
            }
        }

        /**
         * @param item
         */
        private void praseObjectModel( Node objectNode )
        {
            objectType = objectNode.getNodeName();
            NodeList attributeNodes = objectNode.getChildNodes();
            for( int i = 0; i < attributeNodes.getLength(); i++ )
            {
                Node attributeNode = attributeNodes.item( i );
                if( attributeNode.getNodeName().contains( "#text" ) )
                {
                    continue;
                }
                if( ID_TAG.equals( attributeNode.getNodeName() ) )
                {
                    objectId = attributeNode.getTextContent().trim();
                }
                else
                {
                    attributes.put( attributeNode.getNodeName(), attributeNode.getTextContent().trim() );
                }
            }
        }

        /**
         * @return the operationType
         */
        public String getOperationType()
        {
            return operationType;
        }

        /**
         * @param operationType the operationType to set
         */
        public void setOperationType( String operationType )
        {
            this.operationType = operationType;
        }

        /**
         * @return the objectId
         */
        public String getObjectId()
        {
            return objectId;
        }

        /**
         * @param objectId the objectId to set
         */
        public void setObjectId( String objectId )
        {
            this.objectId = objectId;
        }

        /**
         * @return the objectType
         */
        public String getObjectType()
        {
            return objectType;
        }

        /**
         * @param objectType the objectType to set
         */
        public void setObjectType( String objectType )
        {
            this.objectType = objectType;
        }

        public Set<String> getAttributeNames()
        {
            return attributes.keySet();
        }

        public String getAttributeValue( String name )
        {
            return attributes.get( name );
        }

        public void putAttribute( String name, String value )
        {
            attributes.put( name, value );
        }

        public void removeAttribute( String name )
        {
            attributes.remove( name );
        }

        public String getIdentifyId()
        {
            return isObjectModel ? getObjectType() + getObjectId() : getOperationType() + configObjectType +
                configObjectId;
        }

        public String toString()
        {
            StringBuffer notificationMessage = new StringBuffer();
            notificationMessage.append( "<" + DATA_CHANGE_NOTIF_TAG + ">" );
            notificationMessage.append( "\n" );
            notificationMessage.append( "<" + CONFIG_OPERATION_TYPE_TAG + ">" );
            notificationMessage.append( getOperationType() );
            notificationMessage.append( "</" + CONFIG_OPERATION_TYPE_TAG + ">" );
            notificationMessage.append( "\n" );
            if( !configObjectId.isEmpty() )
            {
                notificationMessage.append( "<" + CONFIG_OBJECT_ID_TAG + ">" );
                notificationMessage.append( configObjectId );
                notificationMessage.append( "</" + CONFIG_OBJECT_ID_TAG + ">" );
            }

            if( !configObjectType.isEmpty() )
            {
                notificationMessage.append( "<" + CONFIG_OBJECT_TYPE_TAG + ">" );
                notificationMessage.append( configObjectType );
                notificationMessage.append( "</" + CONFIG_OBJECT_TYPE_TAG + ">" );
            }

            if( isObjectModel )
            {
                notificationMessage.append( "<" + OBJECT_DATA_TAG + ">" );
                notificationMessage.append( "\n" );
                notificationMessage.append( "<" + getObjectType() + ">" );
                notificationMessage.append( "\n" );
                notificationMessage.append( "<" + ID_TAG + ">" );
                notificationMessage.append( objectId );
                notificationMessage.append( "</" + ID_TAG + ">" );
                notificationMessage.append( "\n" );
                for( String name : attributes.keySet() )
                {
                    notificationMessage.append( "<" + name + ">" );
                    notificationMessage.append( attributes.get( name ) );
                    notificationMessage.append( "</" + name + ">" );
                    notificationMessage.append( "\n" );
                }

                notificationMessage.append( "</" + getObjectType() + ">" );
                notificationMessage.append( "\n" );
                notificationMessage.append( "</" + OBJECT_DATA_TAG + ">" );
                notificationMessage.append( "\n" );
            }
            notificationMessage.append( "</" + DATA_CHANGE_NOTIF_TAG + ">" );
            return notificationMessage.toString();
        }
    }
}
