package org.anc.lapps.gate.serialization

import org.lappsgrid.vocabulary.Features

//import org.lappsgrid.vocabulary.Features

/**
 * @author Keith Suderman
 */
class FeatureMapper {

    Map<String,String> map = [
            "category":"pos",
            "pos":"category",

            "string":"word",
            "word":"string",

            "kind":"tokenType",
            "tokenType":"kind",

            "root":"lemma",
            "lemma":"root"
    ]

    String get(String name) {
        String mapped = map[name]
        if (mapped) {
            return mapped
        }
        return name
    }

    String get(String type, String name) {
        if (type == 'Token') {
            return get(name)
        }
        if (type == 'Tagger' || type == 'AbnerTagger') {
            if (name == 'type') {
                return Features.NamedEntity.CATEGORY
            }
        }
        return name
    }
}
