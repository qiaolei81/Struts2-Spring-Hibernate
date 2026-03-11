package rml.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IpUtilTest {

    @Mock
    private HttpServletRequest request;

    // --- x-forwarded-for header ---

    @Test
    public void getIpAddr_xForwardedForPresent_returnsThatIp() {
        when(request.getHeader("x-forwarded-for")).thenReturn("203.0.113.5");
        assertEquals("203.0.113.5", IpUtil.getIpAddr(request));
    }

    @Test
    public void getIpAddr_xForwardedForUnknown_fallsThrough() {
        when(request.getHeader("x-forwarded-for")).thenReturn("unknown");
        when(request.getHeader("Proxy-Client-IP")).thenReturn("10.0.0.1");
        assertEquals("10.0.0.1", IpUtil.getIpAddr(request));
    }

    @Test
    public void getIpAddr_xForwardedForEmpty_fallsThrough() {
        when(request.getHeader("x-forwarded-for")).thenReturn("");
        when(request.getHeader("Proxy-Client-IP")).thenReturn("10.0.0.2");
        assertEquals("10.0.0.2", IpUtil.getIpAddr(request));
    }

    // --- Proxy-Client-IP header ---

    @Test
    public void getIpAddr_proxyClientIpPresent_returnsThatIp() {
        when(request.getHeader("x-forwarded-for")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn("192.168.1.50");
        assertEquals("192.168.1.50", IpUtil.getIpAddr(request));
    }

    @Test
    public void getIpAddr_proxyClientIpUnknown_fallsThrough() {
        when(request.getHeader("x-forwarded-for")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn("unknown");
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn("172.16.0.1");
        assertEquals("172.16.0.1", IpUtil.getIpAddr(request));
    }

    // --- WL-Proxy-Client-IP header ---

    @Test
    public void getIpAddr_wlProxyClientIpPresent_returnsThatIp() {
        when(request.getHeader("x-forwarded-for")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn("172.16.0.2");
        assertEquals("172.16.0.2", IpUtil.getIpAddr(request));
    }

    // --- remote address fallback ---

    @Test
    public void getIpAddr_allHeadersNull_returnsRemoteAddr() {
        when(request.getHeader("x-forwarded-for")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        assertEquals("127.0.0.1", IpUtil.getIpAddr(request));
    }

    @Test
    public void getIpAddr_allHeadersUnknown_returnsRemoteAddr() {
        when(request.getHeader("x-forwarded-for")).thenReturn("unknown");
        when(request.getHeader("Proxy-Client-IP")).thenReturn("unknown");
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn("unknown");
        when(request.getRemoteAddr()).thenReturn("192.0.2.1");
        assertEquals("192.0.2.1", IpUtil.getIpAddr(request));
    }

    // --- IPv6 loopback detection ---

    @Test
    public void getIpAddr_ipv6Loopback_returnsLocal() {
        when(request.getHeader("x-forwarded-for")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        // IPv6 loopback contains "0:" so should be mapped to "local"
        when(request.getRemoteAddr()).thenReturn("0:0:0:0:0:0:0:1");
        assertEquals("local", IpUtil.getIpAddr(request));
    }

    @Test
    public void getIpAddr_regularIpv4_notMappedToLocal() {
        when(request.getHeader("x-forwarded-for")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("10.20.30.40");
        String result = IpUtil.getIpAddr(request);
        assertNotEquals("local", result);
        assertEquals("10.20.30.40", result);
    }
}
