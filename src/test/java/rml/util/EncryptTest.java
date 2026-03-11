package rml.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class EncryptTest {

    // Known-good hashes computed independently
    private static final String MD5_123456  = "e10adc3949ba59abbe56e057f20f883e";
    private static final String SHA_123456  = "7c4a8d09ca3762af61e59520943dc26494f8941b";
    private static final String MD5_ABCDEF  = "e80b5017098950fc58aad83c8c14978e";

    // --- md5 ---

    @Test
    public void md5_knownInput_returnsExpectedHash() {
        assertEquals(MD5_123456, Encrypt.md5("123456"));
    }

    @Test
    public void md5_secondKnownInput_returnsExpectedHash() {
        assertEquals(MD5_ABCDEF, Encrypt.md5("abcdef"));
    }

    @Test
    public void md5_resultIs32HexChars() {
        String result = Encrypt.md5("test");
        assertNotNull(result);
        assertEquals(32, result.length());
        assertTrue(result.matches("[0-9a-f]{32}"));
    }

    @Test
    public void md5_sameInputProducesSameOutput() {
        assertEquals(Encrypt.md5("hello"), Encrypt.md5("hello"));
    }

    @Test
    public void md5_differentInputsProduceDifferentHashes() {
        assertNotEquals(Encrypt.md5("abc"), Encrypt.md5("ABC"));
    }

    // --- sha ---

    @Test
    public void sha_knownInput_returnsExpectedHash() {
        assertEquals(SHA_123456, Encrypt.sha("123456"));
    }

    @Test
    public void sha_resultIs40HexChars() {
        String result = Encrypt.sha("test");
        assertNotNull(result);
        assertEquals(40, result.length());
        assertTrue(result.matches("[0-9a-f]{40}"));
    }

    // --- e (alias for md5) ---

    @Test
    public void e_returnsSameAsMd5() {
        String input = "password";
        assertEquals(Encrypt.md5(input), Encrypt.e(input));
    }

    // --- md5AndSha ---

    @Test
    public void md5AndSha_returnsShaOfMd5() {
        String input = "123456";
        String expected = Encrypt.sha(Encrypt.md5(input));
        assertEquals(expected, Encrypt.md5AndSha(input));
    }

    // --- error cases ---

    @Test(expected = IllegalArgumentException.class)
    public void md5_nullInput_throwsIllegalArgument() {
        Encrypt.md5(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void md5_emptyString_throwsIllegalArgument() {
        Encrypt.md5("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void md5_blankString_throwsIllegalArgument() {
        Encrypt.md5("   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sha_nullInput_throwsIllegalArgument() {
        Encrypt.sha(null);
    }
}
