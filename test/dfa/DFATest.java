package dfa;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DFATest {
    DFA dfa;
    @BeforeEach
    public void prepare() {
    }
    // test string from 0 to 111111111111111
    @Test
    public void testSmall() {
        dfa = new DFA(10);
        for(int i = 0; i < (1<<16); i++) {
            if((i&((1<<10)-1)) != 0) 
                assertTrue(dfa.accept(String.valueOf(i)));
            else 
                assertFalse(dfa.accept(String.valueOf(i)));
        }
    }
    @Test
    public void testHuge() {
        dfa = new DFA(10);
        assertTrue(dfa.accept("000000000000000000000000000000000000000000000000000000000000001"));
        assertTrue(dfa.accept("000000000000000000000000000000000000000000000000000001000000000"));
        assertTrue(dfa.accept("000100000000000000100000000001000000000000000000000001000100000"));
        assertFalse(dfa.accept("00010000000000000010000000000100000000000000000000000000000000"));
        assertFalse(dfa.accept("00010000000000000010000000000100000000000000000000010000000000"));
    }
}