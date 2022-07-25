package com.confluent;

import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SAPDestinationDataProvider implements DestinationDataProvider {
    Map<String, Properties> propertiesForDestinationName = new HashMap<String, Properties>();

    public void addDestination( String destinationName, Properties properties )
    {
        propertiesForDestinationName.put( destinationName, properties );
    }

    public Properties getDestinationProperties( String destinationName )
    {
        if ( propertiesForDestinationName.containsKey( destinationName ) )
        {
            return propertiesForDestinationName.get( destinationName );
        }
        else
        {
            throw new RuntimeException( "JCo destination not found: " + destinationName );
        }
    }

    public void setDestinationDataEventListener(DestinationDataEventListener eventListener )
    {
        // nothing to do
    }

    public boolean supportsEvents()
    {
        return false;
    }
}
