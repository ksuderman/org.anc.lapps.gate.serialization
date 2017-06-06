package org.anc.lapps.gate.serialization

import org.lappsgrid.serialization.lif.Annotation

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
            'VerbChunk':Uri.VCHUNK,
            'NamedEntity': Uri.NE
    ]
    static final Set NE = [ 'Person', 'Date', 'Location', 'Organization' ] as HashSet

    Map map = [:]

    public AnnotationMapper() {
        MAP.each { name, value ->
            map[name] = value
            map[value] = name
        }
    }

    Annotation create(String type) {
        Annotation annotation = new Annotation()
        if (NE.contains(type)) {
            annotation.atType = Uri.NE
            annotation.features.category = type.toUpperCase()
        }
        else {
            annotation.atType = get(type)
        }
        return annotation
    }

    String get(Annotation a) {
        String type = a.atType
        if (type.endsWith('NamedEntity')) {
            if (a.features.category) {
                type = a.features.category.toLowerCase().capitalize()
                a.features.remove('category')
            }
            else {
                type = 'NamedEntity'
            }
        }
        else {
            int index = a.atType.lastIndexOf('/') + 1
            type = a.atType.substring(index)
        }
        return type
    }

    String get(String key) {
        return map[key] ?: key
    }

//    String getAt(String key) {
//        return get(key)
//    }
}
