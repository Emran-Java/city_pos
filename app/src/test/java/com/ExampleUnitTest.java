package com;


import org.junit.Test;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {


    @Test
    public void addition_isCorrect() {
        String tokenEr = "ER00002 00";
        String tokenEx = "EX0006801D7772811E6212C3D0E2558413314A20102012345678AE00000CC37E000EEB2DAF7";

        String encryptedKey = tokenEx.substring(8,40);
        String KSN = tokenEx.substring(20,40);



        System.out.println(encryptedKey);
        System.out.println(KSN);
    }

}