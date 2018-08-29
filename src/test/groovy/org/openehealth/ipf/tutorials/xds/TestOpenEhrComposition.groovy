/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openehealth.ipf.tutorials.xds

import org.apache.commons.io.IOUtils
import org.apache.cxf.transport.servlet.CXFServlet
import org.junit.BeforeClass
import org.junit.Test
import org.openehealth.ipf.commons.ihe.xds.core.SampleData
import org.openehealth.ipf.commons.ihe.xds.core.metadata.AvailabilityStatus
import org.openehealth.ipf.commons.ihe.xds.core.requests.DocumentReference
import org.openehealth.ipf.commons.ihe.xds.core.requests.QueryRegistry
import org.openehealth.ipf.commons.ihe.xds.core.requests.RetrieveDocumentSet
import org.openehealth.ipf.commons.ihe.xds.core.requests.query.FindDocumentsQuery
import org.openehealth.ipf.commons.ihe.xds.core.requests.query.QueryReturnType
import org.openehealth.ipf.commons.ihe.xds.core.responses.QueryResponse
import org.openehealth.ipf.commons.ihe.xds.core.responses.Response
import org.openehealth.ipf.commons.ihe.xds.core.responses.RetrievedDocumentSet
import org.openehealth.ipf.commons.ihe.xds.core.responses.Status
import org.openehealth.ipf.platform.camel.ihe.ws.StandardTestContainer
import com.jayway.restassured.RestAssured

import javax.activation.DataHandler

import static com.jayway.restassured.RestAssured.given
import static org.junit.Assert.assertArrayEquals
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

/**
 * Tests against openEHR REST API /composition endpoint.
 */
class TestOpenEhrComposition extends StandardTestContainer {

    @BeforeClass
    static void classSetUp() {
        startServer(new CXFServlet(), 'context.xml', false, 9091)
    }

    // Simple 'GET hello' test
    @Test
    void testGetHello() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = 8088

        com.jayway.restassured.response.Response response =
            given()
                //.header("Ehr-Session", "3h8q0f")
            .when()
                .get("/hello")

        assertNotNull(response)
        assertEquals(response.toString(), 200, response.statusCode())
        assertEquals(response.toString(), "Hello!", response.getBody().asString())
    }

    // 'POST composition' test
    @Test
    void testPostComposition() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = 8088

        com.jayway.restassured.response.Response response =
            given()
                .header("Ehr-Session", "3h8q0f")
                .body("{\"content\":\"yesthisiscontent\"}")
            .when()
                .post("/ehr/12345/composition")

        assertNotNull(response)
        assertEquals(response.toString(), 200, response.statusCode())
    }

    // 'GET composition test
    @Test
    void testGetComposition() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = 8088

        def compositionBody = "{\"content\":\"yesthisiscontent\"}"

        // first create composition
        com.jayway.restassured.response.Response response =
                given()
                    .header("Ehr-Session", "3h8q0f")
                    .body(compositionBody)
                .when()
                    .post("/ehr/12345/composition")

        assertNotNull(response)
        assertEquals(response.toString(), 200, response.statusCode())

        // next retrieve composition // TODO: temp: assuming composition id == document id. correct?!
        com.jayway.restassured.response.Response postResponse =
            given()
            .when()
                .get("/ehr/12345/composition/6789")

        assertNotNull(postResponse)
        assertEquals(postResponse.toString(), 200, postResponse.statusCode())
        assertEquals("retrieved composition not equal written one", compositionBody, postResponse.body.asString())
    }
}
