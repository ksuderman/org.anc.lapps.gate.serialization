package org.anc.lapps.gate.serialization

import org.anc.util.Counter

import org.junit.*
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.DataContainer

import static org.lappsgrid.discriminator.Discriminators.*
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View
import org.lappsgrid.serialization.lif.Annotation

import static org.junit.Assert.*

//import org.anc.lapps.serialization.*

import gate.*
import org.anc.resource.*

/**
 * @author Keith Suderman
 */
public class GateSerializerTest {

    public static boolean initialized = false

    @BeforeClass
    static void init() {
        File GATE_HOME = new File('/usr/local/lapps/gate_abner/')
        assert GATE_HOME.exists()
        Gate.setGateHome(GATE_HOME)
        Gate.init()
    }

    @Before
    void setup() {

//        if (initialized) return
//
//        initialized = true
        //TODO this is obviously non-portable...
    }

    @Ignore
    void gateToJsonTest() {
        //setup()
        Document document = getDocument() //Factory.newDocument(new File('org.anc.lapps.serialization.gate/src/test/resources/test-file.xml').toURI().toURL())
        Container container = GateSerializer.convertToContainer(document)
        println Serializer.toPrettyJson(container)
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

    @Ignore
    void testBionlpData() {
        // This is not a valid test document.
        InputStream stream = this.class.getResourceAsStream('/example-npe.lif')
        assert null != stream
        Data data = Serializer.parse(stream.text)
        Container container = new Container((Map)data.payload)
        Document document = GateSerializer.convertToDocument(container)
        assert null != document

    }

    @Test
    void jsonToGateTokenTest() {
        Container container = new Container()
        container.text = "hello world"
        container.language = "en-US"
        View view = container.newView()
        view.newAnnotation("w1", Uri.TOKEN, 0, 5)
        view.newAnnotation("w2", Uri.TOKEN, 6, 11)

        assert 2 == view.annotations.size()
        Annotation a = view.findById('w1')
        assert null != a
        assert 'w1' == a.id
        assert Uri.TOKEN == a.atType
        assert 0 == a.start
        assert 5 == a.end

        // Round trip the container
        Document document = GateSerializer.convertToDocument(container)
        container = GateSerializer.convertToContainer(document)

        println Serializer.toPrettyJson(container)

        // Validate the new container
        assert 1 == container.views.size()
        View v = container.views[0]
        assert 2 == v.annotations.size()
        a = v.annotations[0]
        assert Uri.TOKEN == a.atType
        assert 0 == a.start
        assert 5 == a.end
        a = v.annotations[1]
        assert Uri.TOKEN == a.atType
        assert 6 == a.start
        assert 11 == a.end
    }

    @Test
    void testLemmaConversion() {
        URL url = this.class.getResource("/morphological.xml")
        assert url != null
        Document document = Factory.newDocument(url);
        Container container = GateSerializer.convertToContainer(document);

        assert 2 == container.views.size()
        List<View> views = container.findViewsThatContain(Uri.LEMMA)
        assert 1 == views.size()

        View view = views[0]
        List<Annotation> tokens = view.annotations.findAll { it.atType == Uri.TOKEN };
        assert 6 == tokens.size()
        tokens.each { Annotation a ->
            assert a.features.lemma != null
        }
        println Serializer.toPrettyJson(container)
    }

    @Test
    void ner() {
        InputStream stream = this.class.getResourceAsStream("/gate-ner.lif")
        assert stream != null

        Data data = Serializer.parse(stream.text)
        assert Uri.GATE == data.discriminator

        String xml = data.payload.toString()
        Document document = Factory.newDocument(xml)
        Container container = GateSerializer.convertToContainer(document)
        assert 3 == container.views.size()

        // The old types that should no appear in LIF anymore.
        List<String> forbiddenTypes = [ Uri.PERSON, Uri.LOCATION, Uri.DATE, Uri.ORGANIZATION ]

        // Check the forbidden types do not appear in the metadata.
        forbiddenTypes.each { String type ->
            assert 0 == container.findViewsThatContain(type).size()
        }

        List<View> views = container.findViewsThatContain(Uri.NE)
        assert 1 == views.size()

        // Check that the forbidden types do not appear as annotations.
        View view = views[0]
        forbiddenTypes.each { String forbidden ->
            assert 0 == view.annotations.findAll { it.atType == forbidden }.size()
        }
        println Serializer.toPrettyJson(container)
    }

    @Test
    void format() {
        InputStream stream = this.class.getResourceAsStream("/gate-ner.lif")
        assert stream != null

        Data data = Serializer.parse(stream.text)
        assert Uri.GATE == data.discriminator

        String xml = data.payload.toString()
        Document document = Factory.newDocument(xml)

        String json = GateSerializer.toJson(document)
        data = Serializer.parse(json)
        assert Uri.LIF == data.discriminator
    }

    @Test
    void prettyFormat() {
        InputStream stream = this.class.getResourceAsStream("/gate-ner.lif")
        assert stream != null

        Data data = Serializer.parse(stream.text)
        assert Uri.GATE == data.discriminator

        String xml = data.payload.toString()
        Document document = Factory.newDocument(xml)

        String json = GateSerializer.toPrettyJson(document)
        data = Serializer.parse(json)
        assert Uri.LIF == data.discriminator
    }

    @Ignore
    void jsonToGateTest() {
        String json = ResourceLoader.loadString('gate-document.lif')
        assert null != json
        DataContainer dc = Serializer.parse(json, DataContainer)
        Container container = dc.payload
        assert null != container.text
        Document document = GateSerializer.convertToDocument(container)
        println document.toXml()

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
                increment(containerMap, annotation.atType)
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

    @Test
    void convertAbner() {
        InputStream stream = this.class.getResourceAsStream("/gate-abner.xml")
        assert null != stream
//        Data data = new Data(Uri.LIF, stream.text)
        Document document = Factory.newDocument(stream.text)
        println GateSerializer.toPrettyJson(document)
    }
}
