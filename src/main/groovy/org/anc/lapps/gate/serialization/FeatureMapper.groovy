package org.anc.lapps.gate.serialization

import org.lappsgrid.vocabulary.Features

/**
 * @author Keith Suderman
 */
class FeatureMapper {
    private static final Map FEATURES = [
            'category':Features.PART_OF_SPEECH,
            'base':Features.LEMMA
    ]

    Map map = [:]

    public FeatureMapper() {
        FEATURES.each { name, value ->
            map[name] = value
            map[value] = name
        }
    }

    String get(String key) {
        return map[key] ?: key
    }

//    String getAt(String key) {
//        return get(key)
//    }
}
