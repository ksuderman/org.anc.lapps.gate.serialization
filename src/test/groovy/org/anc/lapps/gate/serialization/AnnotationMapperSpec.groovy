package org.anc.lapps.gate.serialization

import org.junit.*
import org.lappsgrid.discriminator.Discriminators
import org.lappsgrid.serialization.lif.Annotation
import static org.lappsgrid.discriminator.Discriminators.*

import spock.lang.Specification

import static org.junit.Assert.*

/**
 * @author Keith Suderman
 */
class AnnotationMapperSpec extends Specification {

    AnnotationMapper mapper = new AnnotationMapper()

    def "test array style access"() {
        expect:
        uri == mapper[type]

        where:
        uri             | type
        Uri.TOKEN       | 'Token'
        Uri.SENTENCE    | 'Sentence'
        Uri.NE          | 'NamedEntity'
        Uri.PERSON      | 'Person'
        Uri.DATE        | 'Date'
        Uri.ORGANIZATION| 'Organization'
        Uri.LOCATION    | 'Location'
        Uri.NCHUNK      | 'NounChunk'
        Uri.VCHUNK      | 'VerbChunk'
        'Foo'           | 'Foo'
        'foo'           | 'foo'
        'FOO'           | 'FOO'
    }

    def "test get method"() {
        expect:
        uri == mapper.get(type)

        where:
        uri             | type
        Uri.TOKEN       | 'Token'
        Uri.SENTENCE    | 'Sentence'
        Uri.NE          | 'NamedEntity'
        Uri.PERSON      | 'Person'
        Uri.DATE        | 'Date'
        Uri.ORGANIZATION| 'Organization'
        Uri.LOCATION    | 'Location'
        Uri.NCHUNK      | 'NounChunk'
        Uri.VCHUNK      | 'VerbChunk'
        'Foo'           | 'Foo'
        'foo'           | 'foo'
        'FOO'           | 'FOO'
    }

    def "create named entity annotations"() {
        given:
        Annotation a = mapper.create(type)

        expect:
        a.atType == Uri.NE
        a.features.category == type.toUpperCase()

        where:
        type << [ 'Organization', 'Location', 'Person', 'Date']
    }

    def "get Gate Annotation"() {
        given:
        Annotation a = mapper.create(type)

        expect:
        a.atType == Uri.NE
        a.features.category == type.toUpperCase()
        type == mapper.get(a)
        a.features.category == null

        where:
        type << [ 'Organization', 'Location', 'Person', 'Date']
    }
}
