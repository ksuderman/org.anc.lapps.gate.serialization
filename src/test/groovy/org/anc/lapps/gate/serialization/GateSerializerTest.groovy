package org.anc.lapps.gate.serialization

import org.anc.util.Counter

//import org.anc.lapps.client.RemoteService
//import org.lappsgrid.api.*
//import org.lappsgrid.discriminator.Types

import org.junit.*
import org.lappsgrid.serialization.Container
import org.lappsgrid.serialization.View

import static org.junit.Assert.*

import org.anc.lapps.serialization.*

import gate.*
import org.anc.resource.*

/**
 * @author Keith Suderman
 */
public class GateSerializerTest {

    public static boolean initialized = false

    @Before
    void setup() {
        if (initialized) return

        initialized = true
        Gate.setGateHome(new File('/Applications/GATE-7.1'))
        Gate.init()
    }

    @Ignore
    void gateToJsonTest() {
        //setup()
        Document document = getDocument() //Factory.newDocument(new File('org.anc.lapps.serialization.gate/src/test/resources/test-file.xml').toURI().toURL())
        Container container = GateSerializer.convertToContainer(document)
        println container.toJson()
    }

    @Ignore
    void testRoundTrip() {
        setup()
        Document document = getDocument() //Factory.newDocument(new File('org.anc.lapps.serialization.gate/src/test/resources/test-file.xml').toURI().toURL())
        Container container = GateSerializer.convertToContainer(document)
        document = GateSerializer.convertToDocument(container)
        println document.toXml()

    }

    private void increment(Map map, String key) {
        Counter count = map[key]
        if (count == null) {
            count = new Counter()
            map[key] = count
        }
        count.increment()
    }

    @Test
    void jsonToGateTest() {
        String json = ResourceLoader.loadString('test_file.json')
        assertTrue(json != null)
        Container container = new Container(json)
        assertNotNull(container.text)
        //println container.toPrettyJson()
        Document document = GateSerializer.convertToDocument(container)
//        Map sets = document.getNamedAnnotationSets()
//        println "The document contains ${sets.size()} annotation sets."
//        sets.each { String name, AnnotationSet set ->
//            println "${name} contains ${set.size()} annotations"
//        }
        AnnotationMapper mapper = new AnnotationMapper()
        Map documentMap = [:] //[(Annotations.SENTENCE):0, (Annotations.TOKEN):0]
        Map containerMap = [:] //[(Annotations.SENTENCE):0, (Annotations.TOKEN):0]
        AnnotationSet set = document.getAnnotations()
        Iterator it = set.iterator()
        while (it.hasNext()) {
            increment(documentMap, it.next().type)
        }

        container.views.each { View view ->
            view.annotations.each { annotation ->
                increment(containerMap, annotation.label)
            }
        }

        documentMap.each { name,value ->
//            assertNotNull("containerMap does not contain a value for ${name}", containerMap[mapper[name]])
            assertNotNull("containerMap does not contain a value for ${name}", containerMap[name])
            assertTrue("${name} values differ.", value == containerMap[name])
        }

//        containerMap.each { name,value ->
//            assertNotNull("documentMap does not contain a value for ${name}", documentMap[mapper[name]])
//            assertTrue("${name} values differ.", value == documentMap[mapper[name]])
//        }
    }

    Document getDocument() {
        ClassLoader loader = Thread.currentThread().contextClassLoader
        if (loader == null) {
            loader = GateSerializer.class.classLoader
        }
        URL url = loader.getResource('test-file.xml')
        if (url == null) {
            throw new NullPointerException('Unable to load test file.')
        }
        return Factory.newDocument(url)
    }

    /*
    @Test
    public void testWithServices() {
        setup()
        Document document = Factory.newDocument(new File('org.anc.lapps.serialization.gate/src/test/resources/test-file.xml').toURI().toURL())
        String base = "http://grid.anc.org:8080/service_manager/invoker"
        String user = "operator"
        String pass = "operator"
        RemoteService splitter = new RemoteService("${base}/anc:GATE_SPLITTER", user, pass)
        RemoteService tokenizer = new RemoteService("${base}/anc:GATE_TOKENZIER", user, pass)
        RemoteService tagger = new RemoteService("${base}/anc:GATE_TAGGER", user, pass)

        Data data = new Data(Types.GATE, document.toXml())
        data = splitter.execute(data)
        data = tokenizer.execute(data)
        data = tagger.execute(data)

        if (data.discriminator == Types.ERROR) {
            println data.payload
        }
        else {
            document = Factory.newDocument(data.payload)
            println GateSerializer.toPrettyJson(document)
        }
    }
    */
}
