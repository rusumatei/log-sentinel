package ubb.bmad.logsentinel.geo;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GeoLookupServiceTest {

    @Mock
    private DatabaseReader mockReader;

    private GeoLookupService service;

    @BeforeEach
    void setUp() throws Exception {
        // Reset the singleton instance using reflection for clean tests
        Field instanceField = GeoLookupService.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        // Create the service instance using reflection to bypass file check
        Constructor<GeoLookupService> constructor = GeoLookupService.class.getDeclaredConstructor(DatabaseReader.class);
        constructor.setAccessible(true);
        service = constructor.newInstance(mockReader);

        // Set the singleton instance
        instanceField.set(null, service);
    }

    @Test
    void testLookupSuccess() throws Exception {
        CityResponse mockResponse = mock(CityResponse.class);
        Country mockCountry = mock(Country.class);
        City mockCity = mock(City.class);

        when(mockCountry.getName()).thenReturn("Romania");
        when(mockCity.getName()).thenReturn("Cluj-Napoca");
        when(mockResponse.getCountry()).thenReturn(mockCountry);
        when(mockResponse.getCity()).thenReturn(mockCity);
        when(mockReader.city(any(InetAddress.class))).thenReturn(mockResponse);

        String result = service.lookup("8.8.8.8");
        assertEquals("Romania, Cluj-Napoca", result);
    }

    @Test
    void testLookupInternalIP() throws Exception {
        when(mockReader.city(any(InetAddress.class))).thenThrow(new com.maxmind.geoip2.exception.AddressNotFoundException("Not found"));

        String result = service.lookup("127.0.0.1");
        assertEquals("Internal/Unknown", result);
    }

    @Test
    void testLookupWithNullNames() throws Exception {
        CityResponse mockResponse = mock(CityResponse.class);
        Country mockCountry = mock(Country.class);
        City mockCity = mock(City.class);

        when(mockCountry.getName()).thenReturn(null);
        when(mockCity.getName()).thenReturn(null);
        when(mockResponse.getCountry()).thenReturn(mockCountry);
        when(mockResponse.getCity()).thenReturn(mockCity);
        when(mockReader.city(any(InetAddress.class))).thenReturn(mockResponse);

        String result = service.lookup("8.8.8.8");
        assertEquals("Unknown Country, Unknown City", result);
    }

    @Test
    void testUninitializedService() throws Exception {
        // Force instance to null
        Field instanceField = GeoLookupService.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        // Should return NO_OP, not null
        GeoLookupService result = GeoLookupService.getInstance();
        assertNotNull(result);
        assertFalse(result.isEnabled());
        
        // Lookup should throw IllegalStateException
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
            result.lookup("8.8.8.8");
        });
        
        // Creating a service with null reader using reflection to verify enforcement
        Constructor<GeoLookupService> constructor = GeoLookupService.class.getDeclaredConstructor(com.maxmind.geoip2.DatabaseReader.class);
        constructor.setAccessible(true);
        GeoLookupService nullService = constructor.newInstance(new Object[]{null});
        
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
            nullService.lookup("8.8.8.8");
        });
    }

    @Test
    void testIsEnabled() {
        assertTrue(service.isEnabled());
    }

    @Test
    void testInitAndGetInstance() throws Exception {
        // Reset instance
        Field instanceField = GeoLookupService.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        // We can't easily test a successful init() without a real file,
        // but we can test that getInstance() returns our mocked instance if we set it.
        instanceField.set(null, service);
        assertEquals(service, GeoLookupService.getInstance());
    }

    @Test
    void testInitWithMissingFile() {
        // Reset instance
        try {
            Field instanceField = GeoLookupService.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception e) {}

        org.junit.jupiter.api.Assertions.assertThrows(java.io.FileNotFoundException.class, () -> {
            GeoLookupService.init("non_existent_file.mmdb");
        });
    }

    @Test
    void testInitTwice() throws Exception {
        // Set instance
        Field instanceField = GeoLookupService.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, service);

        // Should do nothing and not throw
        GeoLookupService.init("any_path");
        assertEquals(service, GeoLookupService.getInstance());
    }

    @Test
    void testInitWithInvalidFile() throws Exception {
        java.io.File tempFile = java.io.File.createTempFile("dummy", ".mmdb");
        tempFile.deleteOnExit();
        
        // Reset instance
        try {
            Field instanceField = GeoLookupService.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception e) {}

        // This should throw because the file is not a valid MaxMind DB
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
            GeoLookupService.init(tempFile.getAbsolutePath());
        });
    }

    @Test
    void testCloseWithNullReader() throws Exception {
        Constructor<GeoLookupService> constructor = GeoLookupService.class.getDeclaredConstructor(DatabaseReader.class);
        constructor.setAccessible(true);
        GeoLookupService nullService = constructor.newInstance(new Object[]{null});
        nullService.close(); // Should not throw
    }

    @Test
    void testClose() throws Exception {
        service.close();
        org.mockito.Mockito.verify(mockReader).close();
    }
}
