package sacephor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
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
    // "DataChangeNotif","AlarmObservation","ConfigurationChangeNotification"
    private static String[] ACCEPT_MESSAGES = { "DataChangeNotif", "ConfigurationChangeNotification" };

    private static final String[] FILTERS = new String[]{ "" };

    private static final String FOLDER = "D:/";//

    private static final String LOG_FILE = "SiteManager.log";//

    private static final String SEQUENCE_FILE = "Act_Log_Debug";//

    private static final int PAUSE = 100;

    private static final String NEW_CONNECT_FLAG = "connect/re";

    private static final String DATA_CHANGE_NOTIF_TAG = "DataChangeNotif";

    private static final String OBJECT_DATA_TAG = "_objectData";

    private static final String CONFIG_OPERATION_TYPE_TAG = "_configOperationType";

    private static final String ID_TAG = "_id";

    private static final String REMOVE_INDICATOR = "_R_M_O_V_E_";

    private static final String[] TIME_PATTERNS =
        new String[]{ "\\[[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}\\]\\s*\\[[0-9]{2}.[0-9]{2}.[0-9]{4}\\]",
                     "[0-9]{4}-[0-9]{2}-[0-9]{2}\\s*T[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}Z" };

    public static void main( String[] args ) throws Exception
    {
        BufferedReader fileReader = new BufferedReader( new FileReader( FOLDER + LOG_FILE ) );

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

    private static void generateSequence( String logContent ) throws Exception
    {
        String[] logTraces = getLogItems( logContent );
        String[] latestConnenctionTraces = getLatestConnectionTraces( logTraces );
        writeSequence( latestConnenctionTraces );
    }

    private static String[] getLogItems( String logContent )
    {
        if( LOG_FILE.toLowerCase().endsWith( ".log" ) )
        {
            return getTextLogItems( logContent );
        }
        else if( LOG_FILE.toLowerCase().endsWith( ".xml" ) )
        {
            return getXMLLogItems( logContent );
        }
        else
        {
            throw new UnsupportedOperationException( "current only support text and xml log format." );
        }
    }

    private static String[] getTextLogItems( String logContent )
    {
        return logContent.split( getTimePattern( logContent ) );
    }

    private static String getTimePattern( String logContent )
    {
        for( String pattern : TIME_PATTERNS )
        {
            Matcher m = Pattern.compile( pattern ).matcher( logContent.substring( 0, 150 ) );
            if( m.find() )
            {
                return pattern;
            }
        }
        return null;
    }

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

    private static void writeSequence( String[] logTraces ) throws FileNotFoundException
    {
        Map<String, ObjectModel> map = new HashMap<String, ObjectModel>();
        PrintWriter sequenceWriter = new PrintWriter( FOLDER + SEQUENCE_FILE + ".sequence" );
        try
        {
            writeHeader( sequenceWriter );
            int seqNumber = 0;
            for( String logTrace : logTraces )
            {
                logTrace = logTrace.trim();
                String messageType = acceptedMessage( logTrace );
                if( messageType != null && filterSequence( logTrace ) )
                {
                    String message = null;
                    if( DATA_CHANGE_NOTIF_TAG.equals( messageType ) )
                    {
                        String dataChangeNotifMessage = getWellFormatDataChangeNotifMessage( logTrace );
                        ObjectModel objectModel = getDataChangeNotifObejctModel( map, dataChangeNotifMessage );
                        message = objectModel.toString();
                    }
                    else
                    {
                        int startIndex = logTrace.indexOf( messageType );
                        int endIndex = logTrace.indexOf( "/" + messageType );
                        message = logTrace.substring( startIndex - 1, endIndex + messageType.length() + 2 );
                    }
                    writeSequenceItem( sequenceWriter, messageType + " " + ( ++seqNumber ), message );
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

    private static void writeHeader( PrintWriter sequenceWriter )
    {
        sequenceWriter.println( "<Sequence name=\"" + SEQUENCE_FILE + " - Sequences\">" );
        sequenceWriter.println( "<Pause>0</Pause>" );
    }

    private static String acceptedMessage( String logTrace )
    {
        for( String messageType : ACCEPT_MESSAGES )
        {
            if( hasTag( logTrace, messageType ) )
            {
                return messageType;
            }
        }
        return null;
    }

    private static boolean hasTag( String logTrace, String tag )
    {
        return logTrace.indexOf( "<" + tag + ">" ) >= 0;
    }

    private static boolean filterSequence( String logTrace )
    {
        for( String filter : FILTERS )
        {
            if( logTrace.contains( filter ) )
            {
                return true;
            }
        }
        return FILTERS.length == 0;
    }

    private static String getWellFormatDataChangeNotifMessage( String logTrace )
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

    private static String insertMissingObjectDataStartTag( String logTrace )
    {
        int objectDataIndex = logTrace.indexOf( "<" + OBJECT_DATA_TAG + ">" );
        if( objectDataIndex < 0 )
        {
            return logTrace.replace( "</" + CONFIG_OPERATION_TYPE_TAG + ">", "</" + CONFIG_OPERATION_TYPE_TAG + ">\n<" +
                OBJECT_DATA_TAG + ">" );
        }
        return logTrace;
    }

    private static String getUnitType( String unitMessage )
    {
        int start = unitMessage.indexOf( "<" + OBJECT_DATA_TAG + ">" ) + ( "<" + OBJECT_DATA_TAG + ">" ).length();
        String subMessage = unitMessage.substring( start ).trim();
        int end = subMessage.indexOf( ">" );
        String unitType = subMessage.substring( 1, end ).trim();
        return unitType;
    }

    private static String appendMissingEndTag( String logTrace, String endTag )
    {
        if( logTrace.indexOf( endTag ) < 0 )
        {
            return logTrace + "\n" + endTag;
        }
        return logTrace;
    }

    private static String adapterAdd( String substring )
    {
        return substring.replace( "+<", "<" );
    }

    private static String adapterRemove( String substring )
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

    private static ObjectModel getDataChangeNotifObejctModel( Map<String, ObjectModel> map,
                                                              String dataChangeNotifMessage )
    {
        Element dataChangeNotifElement = parseElementFromString( dataChangeNotifMessage );
        SequenceGenerator.ObjectModel objectModel = new SequenceGenerator.ObjectModel( dataChangeNotifElement );
        String identifyId = objectModel.getIdentifyId();
        if( LOG_FILE.toLowerCase().endsWith( ".log" ) )
        {
            updateTextLogObjectModel( map, objectModel, identifyId );
        }
        else
        {
            updateXMLLogObjectModel( map, objectModel, identifyId );
        }
        return map.get( identifyId );
    }

    private static Element parseElementFromString( String fileName )
    {
        Document doc = null;
        FileInputStream is = null;
        try
        {
            is = new FileInputStream( fileName );
            // Create the new DocumentBuilderFactory
            DocumentBuilderFactory docBF = DocumentBuilderFactory.newInstance();
            // Create the new DocumentBuilder
            DocumentBuilder docBuilder = docBF.newDocumentBuilder();
            doc = docBuilder.parse( is );
            // Normalize text representation
            doc.getDocumentElement().normalize();
            return doc.getDocumentElement();
        }
        catch( Exception ex )
        {
            System.err.println( ex );
        }
        return null;
    }

    private static void updateTextLogObjectModel( Map<String, ObjectModel> map, SequenceGenerator.ObjectModel objectModel,
                                             String identifyId )
    {
        ObjectModel existObjectModel = map.get( identifyId );
        if( existObjectModel != null )
        {
            for( String attributeName : objectModel.getAttributeNames() )
            {
                if( attributeName.startsWith( REMOVE_INDICATOR ) )
                {
                    existObjectModel.removeAttribute( attributeName.replace( REMOVE_INDICATOR, "" ) );
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

    private static void updateXMLLogObjectModel( Map<String, ObjectModel> map, ObjectModel objectModel,
                                                 String identifyId )
    {
        map.put( identifyId, objectModel );
    }

    private static void writeSequenceItem( PrintWriter sequenceWriter, String sequenceName, String content )
    {
        sequenceWriter.println( "<Send>" );
        sequenceWriter.println( "<Message name=\"" + sequenceName + "\" spontaneous=\"yes\">" );
        sequenceWriter.println( content );
        sequenceWriter.println( "</Message>" );
        sequenceWriter.println( "</Send>" );
        if( PAUSE > 0 )
        {
            sequenceWriter.println( "<Pause>" + PAUSE + "</Pause>" );
        }
        sequenceWriter.println( "" );
    }

    private static void writeFooter( PrintWriter sequenceWriter )
    {
        sequenceWriter.println( "</Sequence>" );
    }

    static class ObjectModel
    {
        private static final String CONFIG_OBJECT_TYPE_TAG = "_configObjectType";

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

        public String getOperationType()
        {
            return operationType;
        }

        public void setOperationType( String operationType )
        {
            this.operationType = operationType;
        }

        public String getObjectId()
        {
            return objectId;
        }

        public void setObjectId( String objectId )
        {
            this.objectId = objectId;
        }

        public String getObjectType()
        {
            return objectType;
        }

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
