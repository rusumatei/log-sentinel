package ubb.bmad.logsentinel.geo;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Singleton service for Geo-Spatial lookups using MaxMind GeoLite2.
 */
public class GeoLookupService implements AutoCloseable {
    private static volatile GeoLookupService instance;
    private static final GeoLookupService NO_OP = new GeoLookupService((DatabaseReader) null);
    private final DatabaseReader reader;

    private GeoLookupService(String dbPath) throws IOException {
        this(new DatabaseReader.Builder(new File(dbPath)).build());
    }

    // Package-private constructor for testing
    GeoLookupService(DatabaseReader reader) {
        this.reader = reader;
    }

    /**
     * Initializes the Singleton instance. Should be called only once at startup.
     */
    public static synchronized void init(String dbPath) throws IOException {
        if (instance == null) {
            instance = new GeoLookupService(dbPath);
        }
    }

    /**
     * Gets the Singleton instance. Returns a No-Op instance if not initialized.
     */
    public static GeoLookupService getInstance() {
        if (instance == null) {
            return NO_OP;
        }
        return instance;
    }

    /**
     * Returns true if the Geo-Spatial service is fully initialized and functional.
     */
    public boolean isEnabled() {
        return reader != null;
    }

    /**
     * Resolves an IP address to a location string (Country, City).
     * 
     * @param ipAddress The IP address to lookup.
     * @return A formatted location string or "Internal/Unknown".
     * @throws IllegalStateException if the service is not initialized and functional.
     */
    public String lookup(String ipAddress) {
        if (!isEnabled()) {
            throw new IllegalStateException("Geo-Spatial lookup attempted on uninitialized service.");
        }

        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            CityResponse response = reader.city(address);
            
            String country = response.getCountry().getName();
            String city = response.getCity().getName();

            if (country == null) country = "Unknown Country";
            if (city == null) city = "Unknown City";

            return country + ", " + city;
        } catch (IOException | GeoIp2Exception e) {
            // Fallback for internal IPs or resolution failures
            return "Internal/Unknown";
        }
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }
}
