package org.anc.lapps.gate.serialization

import gate.Factory
import gate.AnnotationSet
import gate.Document
import gate.FeatureMap
import gate.util.InvalidOffsetException
import org.anc.lapps.serialization.Annotation
import org.anc.lapps.serialization.Container
import org.anc.lapps.serialization.ProcessingStep
import org.lappsgrid.vocabulary.Metadata
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Keith Suderman
 */
class GateSerializer {
    private static Logger logger = LoggerFactory.getLogger(GateSerializer.class)

    static AnnotationMapper annotationMapper = new AnnotationMapper()
    static FeatureMapper featureMapper = new FeatureMapper()

    static public String toJson(Document document) {
        return convertToContainer(document).toJson()
    }

    static public String toPrettyJson(Document document) {
        return convertToContainer(document).toPrettyJson()
    }

    static public Container convertToContainer(Document document) {

        Container container = new Container()
        container.text = document.content.getContent(0, document.content.size())
        addToContainer(container, document)
        return container
    }

    static public void addToContainer(Container container, Document document) {
        int counter = -1
        ProcessingStep step = new ProcessingStep()
        AnnotationSet aSet = document.getAnnotations()
        counter = addAnnotationSet(aSet, step, counter)
        document.namedAnnotationSets.each { name, set ->
            counter = addAnnotationSet(set, step, counter)
        }
        String producer = document.getFeatures().get(Metadata.PRODUCED_BY)
        if (producer) {
            step.metadata[Metadata.PRODUCED_BY] = producer
        }
        container.steps << step
    }

    private static int addAnnotationSet(AnnotationSet set, ProcessingStep step, int counter) {
        set.each { gateAnnotation ->
            Annotation annotation = new Annotation()
            annotation.metadata.aSet = set.getName()
            annotation.metadata.gateId = gateAnnotation.getId()
            annotation.id = "${++counter}"
            ++counter
            annotation.start = gateAnnotation.startNode.offset.longValue()
            annotation.end = gateAnnotation.endNode.offset.longValue()
            annotation.label = annotationMapper.get(gateAnnotation.type)
            gateAnnotation.features.each { key, value ->
                def mappedKey = featureMapper.get(key)
                annotation.features[mappedKey] = value
            }
            step.annotations << annotation
        }
        return counter
    }

    static public Document convertToDocument(Container container) {
        logger.debug("Converting container to GATE document")
        Document document = Factory.newDocument(container.text)
        logger.debug("Document created.")
        //Map annotationSets = [:]

        List producers = []
        List contains = []
        container.steps.each { step ->
            logger.debug("Processing step.")
            String producer = step.metadata[Metadata.PRODUCED_BY]
            if (producer) {
                producers << producer
            }
            String type = step.metadata[Metadata.CONTAINS]
            if (type) {
                contains << type
            }
            step.annotations.each { annotation ->
                String setName = annotation.metadata.aSet ?: ''
                AnnotationSet annotationSet = document.getAnnotations(setName)
                if (annotationSet == null) {
                    annotationSet = document.getAnnotations()
                }
                Integer id = annotation.metadata.gateId ?: -1
                Long start = annotation.start
                Long end = annotation.end
                String label = annotationMapper.get(annotation.label)
                //println "${start}-${end} ${label}"
//                println "${annotation.label} -> ${label}"
                FeatureMap features = Factory.newFeatureMap()
                annotation.features.each { name, value ->
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
                    logger.error("Unable to add {} at offset {}", label, start);
                    throw e
                }
            }
        }
        if (producers.size() > 0) {
            document.getFeatures().put(Metadata.PRODUCED_BY, producers.join(","));
        }
        if (contains.size() > 0) {
            document.getFeatures().put(Metadata.CONTAINS, contains.join(","));
        }
        return document
    }


}
