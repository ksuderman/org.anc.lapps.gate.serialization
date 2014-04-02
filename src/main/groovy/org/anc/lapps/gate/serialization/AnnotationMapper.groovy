package org.anc.lapps.gate.serialization

import org.lappsgrid.vocabulary.*

/**
 * Maps GATE annotation names (types) to LAPPS annotation names anc
 * vice versa
 *
 * @deprecated The GATE annotation names match the LAPPS vocabulary names.
 *
 * @author Keith Suderman
 */
class AnnotationMapper { //extends HashMap {
    static final Map MAP = [
            'Token':Annotations.TOKEN,
            'Sentence':Annotations.SENTENCE
    ]

    Map map = [:]

    public AnnotationMapper() {
        MAP.each { name, value ->
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
