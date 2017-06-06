package org.anc.lapps.gate.serialization

import spock.lang.Specification

import static org.junit.Assert.*

/**
 * @author Keith Suderman
 */
class FeatureMapperSpec extends Specification {
    FeatureMapper mapper = new FeatureMapper()

    def "test mappings()"(String a, String b) {
        expect:
        a == mapper[b]
        b == mapper[a]

        where:
        a           | b
        'category'  | 'pos'
        'base'      | 'lemma'
        'string'    | 'word'
        'kind'      | 'tokenType'
        'length'    | 'length'
    }
}
