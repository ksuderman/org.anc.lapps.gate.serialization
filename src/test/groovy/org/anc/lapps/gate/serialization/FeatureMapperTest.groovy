package org.anc.lapps.gate.serialization

import org.junit.*
import static org.junit.Assert.*

/**
 * @author Keith Suderman
 */
@Ignore
class FeatureMapperTest {
    FeatureMapper mapper

    @Before
    void setup() {
        mapper = new FeatureMapper()
    }

    @After
    void tearDown() {
        mapper = null
    }

    @Test
    void testCategory() {
        def expected = 'pos'
        def actual = mapper['category']
        assertTrue("Expected: ${expected} Actual: ${actual}", actual == expected)
    }

    @Test
    void testBase() {
        def expected = 'lemma'
        def actual = mapper['base']
        assertTrue("Expected: ${expected} Actual: ${actual}", actual == expected)
    }

    @Test
    void testUndefined() {
        def expected = 'length'
        def actual = mapper[expected]
        assertTrue("Expected: ${expected} Actual: ${actual}", actual == expected)
    }
}
