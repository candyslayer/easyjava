package com.easyjava;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        String prop = "isProp";
        if (prop.startsWith("is")) {
            System.out.println(prop.substring(2));
        }
        assertTrue(true);
    }
    
    @Test
    public void TestConcatString() {
        String str1 = "sotre";
        String str2 = "get";
        StringBuffer stringbuf = new StringBuffer();
        for (int i = 0; i < 10; i++) {
            stringbuf.append("\"" + str1 + "\"" + " + \" : \"" + " + " + "\"" + str2 + "\"");
            stringbuf.append(" + ");
        }

        String str3 = stringbuf.substring(0, stringbuf.lastIndexOf("+"));

        System.out.println(str3);
    }

    @Test
    public void GetIntValue() {
        
    }
}
