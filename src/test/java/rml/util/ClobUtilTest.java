package rml.util;

import org.junit.Test;

import javax.sql.rowset.serial.SerialClob;
import java.sql.Clob;

import static org.junit.Assert.*;

public class ClobUtilTest {

    // --- getString ---

    @Test
    public void getString_nullClob_returnsEmptyString() {
        String result = ClobUtil.getString(null);
        assertNotNull(result);
        assertEquals("", result);
    }

    @Test
    public void getString_simpleContent_returnsContent() throws Exception {
        Clob clob = new SerialClob("hello world".toCharArray());
        String result = ClobUtil.getString(clob);
        assertEquals("hello world", result);
    }

    @Test
    public void getString_emptyClob_returnsEmptyString() throws Exception {
        Clob clob = new SerialClob(new char[0]);
        String result = ClobUtil.getString(clob);
        assertEquals("", result);
    }

    @Test
    public void getString_multiLineContent_concatenatesWithoutNewlines() throws Exception {
        // BufferedReader.readLine() strips newlines; lines are concatenated directly.
        String content = "line1\nline2\nline3";
        Clob clob = new SerialClob(content.toCharArray());
        String result = ClobUtil.getString(clob);
        assertEquals("line1line2line3", result);
    }

    @Test
    public void getString_chineseCharacters_preservedCorrectly() throws Exception {
        String content = "你好世界";
        Clob clob = new SerialClob(content.toCharArray());
        String result = ClobUtil.getString(clob);
        assertEquals(content, result);
    }

    // --- getClob ---

    @Test
    public void getClob_nullInput_returnsNull() {
        Clob result = ClobUtil.getClob(null);
        assertNull(result);
    }

    @Test
    public void getClob_simpleString_returnsNonNullClob() throws Exception {
        Clob result = ClobUtil.getClob("hello");
        assertNotNull(result);
    }

    @Test
    public void getClob_emptyString_returnsEmptyClob() throws Exception {
        Clob result = ClobUtil.getClob("");
        assertNotNull(result);
        assertEquals("", ClobUtil.getString(result));
    }

    // --- round-trip ---

    @Test
    public void roundTrip_stringToClobAndBack_preservesContent() throws Exception {
        String original = "unit test content 123";
        Clob clob = ClobUtil.getClob(original);
        String recovered = ClobUtil.getString(clob);
        assertEquals(original, recovered);
    }
}
