package org.anc.lapps.gate.serialization

import gate.AnnotationSet
import gate.Document
import gate.FeatureMap
import gate.util.InvalidOffsetException
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.lif.Annotation
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.lappsgrid.discriminator.Discriminators.Uri

/**
 * @author Keith Suderman
 */
class GateSerializer {
    private static Logger logger = LoggerFactory.getLogger(GateSerializer.class)

    static AnnotationMapper annotationMapper = new AnnotationMapper()
    static FeatureMapper featureMapper = new FeatureMapper()
    static final Set IGNORED = [ 'SpaceToken', 'Split', 'Lookup',  ] as HashSet
    static final Set<String> INCLUDE = ['Token','Sentence','NounChunk', 'VerbChunk','NamedEntity','Person','Location','Organization','Date','gene']

    static public String toJson(Document document) {
        logger.debug("Generating JSON")
        Container container = convertToContainer(document)
        Data<Container> data = new Data<>(Uri.LIF, container)
        return data.asJson();
    }

    static public String toPrettyJson(Document document) {
        logger.debug("Generating pretty JSON")
        Container container = convertToContainer(document)
        Data<Container> data = new Data<>(Uri.LIF, container)
        return data.asPrettyJson();
    }

    static public Container convertToContainer(Document document) {
        logger.debug("Creating container.")
        Container container = new Container()
        container.text = document.content.getContent(0, document.content.size())
        addToContainer(container, document)
        logger.debug("Container created with {} views", container.views.size())
        return container
    }

    static public void addToContainer(Container container, Document document) {

        // TODO Need to filter out the document features added by GATE from the
        // features we are interested in.
        logger.debug("Processing document features.")
        FeatureMap features = document.getFeatures()
        if (features) {
            logger.trace("There are {} document features.", features.size())
            features.each { String key, value ->
                logger.trace("Feature {} = \"{}\"", key, value)
                if (key.startsWith("lapps:") && value instanceof String) try {
                    key = key[6..-1]
                    String[] parts = value.split(' ')
                    if (parts.size() == 3) {
                        // TODO the stepNumber should be used to order the processing
                        // steps in the container.
                        String type = key
                        int index = type.indexOf('#')
                        if (index > 0) {
                            type = type.substring(0, index)
                        }
                        View view
                        List<View> views = container.findViewsThatContain(type)
                        if (views == null || views.size() == 0) {
//                            println "Creating view for $type"
                            view = container.newView()
                        }
                        else {
                            view = views[-1]
//                            println "Using view ${view.id}"
                        }
                        String stepNumber = parts[0]
                        view.metadata.step = stepNumber as Integer
                        String producer = parts[1]
                        type = parts[2]
                        view.addContains(key, producer, type)
                    }
                }
                catch (Throwable e) {
                    logger.error("Unable to save document feature.", e)
                }
            }
        }

        AnnotationSet set = document.getAnnotations()
        addAnnotationSet(set, container)
        document.namedAnnotationSets.each { name, aset ->
            addAnnotationSet(aset, container)
        }

        List<View> deleteme = []
        container.views.each { View view ->
            if (view.annotations.size() == 0) {
                deleteme.add(view)
            }
        }
        if (deleteme.size() > 0) {
            deleteme.each { View view ->
                logger.debug("Removing empty view {}", view.id)
                container.views.remove(view)
            }
        }
    }

    private View getView(Container container, String type) {
        int index = type.indexOf('#')
        if (index > 0) {
            type = type.substring(0, index)
        }
        View result
        List<View> views = container.findViewsThatContain(type)
        if (views == null || views.size() == 0) {
            return container.newView()
        }
        return views[-1]
    }

    private static void addAnnotationSet(AnnotationSet set, Container container) {
        set.each { gateAnnotation ->
            if (INCLUDE.contains(gateAnnotation.type)) {
                Annotation annotation = annotationMapper.create(gateAnnotation.type, container)
                String setName = set.getName()
                if (setName != null) {
                    annotation.metadata['gate:set'] = setName
                }
                annotation.id = gateAnnotation.getId()
                annotation.start = gateAnnotation.startNode.offset.longValue()
                annotation.end = gateAnnotation.endNode.offset.longValue()
                annotation.label = gateAnnotation.type
                gateAnnotation.features.each { key, value ->
                    def mappedKey = featureMapper.get(key)
                    annotation.features[mappedKey] = value
                }
            }
        }
    }

    static public Document convertToDocument(Container container) {
        Document document = gate.Factory.newDocument(container.text)

        List list = []
        int i = 0
        container.views.each { step ->
            //logger.debug("Processing step ${++i}.")
            Map map = step.metadata.contains
            if (map) {
                list << new ListData(map:map, step:i);
            }
            step.annotations.each { annotation ->
                String setName = annotation.metadata?.aSet ?: ''
                AnnotationSet annotationSet = document.getAnnotations(setName)
                if (annotationSet == null) {
                    annotationSet = document.getAnnotations()
                }
                Integer id = annotation.metadata?.gateId ?: -1
                Long start = annotation.start
                Long end = annotation.end
                // TODO map annotation names
                String label = annotationMapper.get(annotation)
                FeatureMap features = gate.Factory.newFeatureMap()
                annotation.features.each { name, value ->
                    // TODO map feature names
                    features.put(featureMapper.get(name), value)
                }
                try {
                    if (id > 0) {
                        annotationSet.add(id, start, end, label, features)
                    }
                    else {
                        annotationSet.add(start, end, label, features)
                    }
                }
                catch (InvalidOffsetException e) {
                    //logger.error("Unable to add {} at offset {}", label, start);
                    throw e
                }
            }
        }
        FeatureMap features = document.getFeatures()
        list.each { data ->
            data.map.each { key,contains->
                String feature = "${data.step} ${contains.producer} ${contains.type}"
                features.put("lapps:${key}".toString(), feature)
            }
        }
        return document
    }

}

class ListData {
    Map map
    int step
}
