package org.anc.lapps.gate.serialization

import org.lappsgrid.vocabulary.Features

/**
 * @author Keith Suderman
 */
class FeatureMapper {

    Map<String,String> map = [
            "category":"pos",
            "pos":"category",
            "string":"word",
            "word":"string"
    ]

    String get(String name) {
        String mapped = map[name]
        if (mapped) {
            return mapped
        }
        return name
    }
}
