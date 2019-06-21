package org.anc.lapps.gate.serialization

import org.lappsgrid.serialization.lif.Annotation
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View

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
            'NamedEntity': Uri.NE,
            'gene':'http://vocab.lappsgrid.org/Gene'
    ]
    static final Set NE = [ 'Person', 'Date', 'Location', 'Organization', 'AbnerTagger' ] as HashSet

    Map map
    FeatureMapper featureMapper

    public AnnotationMapper() {
        featureMapper = new FeatureMapper()
        map = [:]
        MAP.each { name, value ->
            map[name] = value
            map[value] = name
        }
    }

    Annotation create(final gate.Annotation a, Container container) {
        String lappsType
        String category = a.features.type
        String type = "unknown"
        boolean addCategory = false
        if ("AbnerTagger" == type) {
            lappsType = Uri.NE
            addCategory = true
            type = "bio:abner"
        }
        else if (NE.contains(a.type)) {
            lappsType = Uri.NE
            addCategory = true
        }
        else {
            lappsType = get(a.type)
        }
        View view = null
        List<View> views = container.findViewsThatContain(lappsType)
        if (views == null || views.size() == 0) {
            view = container.newView()
            view.addContains(lappsType, 'GATE', type)
            println "Creating view ${view.id} for $lappsType"
        }
        else {
            view = views[-1]
        }

        Annotation annotation = view.newAnnotation()
        if (addCategory) {
            annotation.features.category = category
        }
        annotation.id = a.getId()
        annotation.start = a.startNode.offset.longValue()
        annotation.end = a.endNode.offset.longValue()
        annotation.label = category
        annotation.atType = lappsType
        a.features.each { key, value ->
            def mappedKey = featureMapper.get(a.type, key)
            annotation.features[mappedKey] = value
        }

        return annotation
    }

    @Deprecated
    Annotation create(final String type, Container container) {
        String lappsType
        String category = type //.toUpperCase()
        boolean addCategory = false
        if ("AbnerTagger" == type) {
            lappsType = Uri.NE
            category = 'Protein'
            addCategory = true
        }
        else if (NE.contains(type)) {
            lappsType = Uri.NE
            addCategory = true
        }
        else {
            lappsType = get(type)
        }
        View view = null
        List<View> views = container.findViewsThatContain(lappsType)
        if (views == null || views.size() == 0) {
            view = container.newView()
            view.addContains(lappsType, 'GATE', 'unknown')
            println "Creating view ${view.id} for $lappsType"
        }
        else {
            view = views[-1]
        }

        Annotation annotation = view.newAnnotation()
        if (addCategory) {
            annotation.features.category = category
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
