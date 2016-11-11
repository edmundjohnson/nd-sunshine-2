package com.example.android.sunshine.app.data;

import android.test.AndroidTestCase;

public class TestPractice extends AndroidTestCase {
//    /*
//     * This gets run before every test.
//     */
//    @Override
//    protected void setUp() throws Exception {
//        super.setUp();
//    }

    public void testThatDemonstratesAssertions() throws Throwable {
        int a = 5;
        int b = 3;
        int c = 5;
        int d = 10;

        assertEquals("a and c should be equal", a, c);
        assertTrue("'d > a' should be true", d > a);
        assertFalse("'a == b + 1' should be false", a == b + 1);

        if (b > d) {
            fail("XX should never happen");
        }
        // Test that fails
        //if (b < d) {
        //    fail("YY should never happen");
        //}
    }

//    @Override
//    protected void tearDown() throws Exception {
//        super.tearDown();
//    }
}
