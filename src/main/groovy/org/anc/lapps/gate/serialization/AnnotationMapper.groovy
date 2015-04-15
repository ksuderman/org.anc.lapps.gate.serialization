package org.anc.lapps.gate.serialization

import static org.lappsgrid.discriminator.Discriminators.Uri
//import org.lappsgrid.vocabulary.*

/**
 * Maps GATE annotation names (types) to LAPPS annotation names anc
 * vice versa
 *
\ * @author Keith Suderman
 */
class AnnotationMapper { //extends HashMap {
    static final Map MAP = [
            'Token':Uri.TOKEN,
            'Sentence':Uri.SENTENCE,
            'Paragraph':Uri.PARAGRAPH,
            'Person':Uri.PERSON,
            'Date':Uri.DATE,
            'Location':Uri.LOCATION,
            'Organization':Uri.ORGANIZATION,
            'NounChunk':Uri.NCHUNK,
            'VerbChunk':Uri.VCHUNK
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
