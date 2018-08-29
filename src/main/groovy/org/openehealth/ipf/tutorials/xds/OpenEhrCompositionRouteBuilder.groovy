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


import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.ProvideAndRegisterDocumentSetRequestType
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.RetrieveDocumentSetRequestType
import org.openehealth.ipf.commons.ihe.xds.core.requests.ProvideAndRegisterDocumentSet
import org.openehealth.ipf.commons.ihe.xds.core.requests.QueryRegistry
import org.openehealth.ipf.commons.ihe.xds.core.requests.RetrieveDocumentSet
import org.openehealth.ipf.commons.ihe.xds.core.responses.QueryResponse
import org.openehealth.ipf.commons.ihe.xds.core.responses.Response
import org.openehealth.ipf.commons.ihe.xds.core.responses.RetrievedDocumentSet
import org.openehealth.ipf.commons.ihe.xds.core.responses.Status
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.query.AdhocQueryRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.openehealth.ipf.platform.camel.ihe.xds.XdsCamelValidators.iti18RequestValidator
import static org.openehealth.ipf.platform.camel.ihe.xds.XdsCamelValidators.iti18ResponseValidator
import static org.openehealth.ipf.platform.camel.ihe.xds.XdsCamelValidators.iti41RequestValidator
import static org.openehealth.ipf.platform.camel.ihe.xds.XdsCamelValidators.iti43RequestValidator

/**
 * Route builder for openEHR REST API /composition endpoint.
 */
class OpenEhrCompositionRouteBuilder extends RouteBuilder {
    private final static Logger log = LoggerFactory.getLogger(OpenEhrCompositionRouteBuilder.class)

    @Override
    public void configure() throws Exception {
        errorHandler(noErrorHandler())

        restConfiguration()
            .component("restlet")
            .host("localhost").port("8088")

        // TODO: remove this test
        rest().get("/hello")
        //same as: from("restlet:http://localhost:8088/hello?restletMethod=get")
            .to("direct:hello")

        // TODO: remove this test
        from("direct:hello")
            .transform().simple("Hello!")
            .log(LoggingLevel.INFO, log, "!!!!!!!!!!!test")

        // Entry point for POSTing compositions
        rest().post("/ehr/{ehr_id}/composition")
            .to("direct:postComposition")

        from("direct:postComposition")
            .log(log) {"POST COMPOSITION:" + it.in.getBody(String.class)}
            .process(new OpenEhrToProvideAndRegisterProcessor())
            // convert to and validate if its now a correct request
            .convertBodyTo(ProvideAndRegisterDocumentSetRequestType.class)
            .process(iti41RequestValidator())  // debugging
            .log(log) { 'sending iti41: ' + it.in.getBody(ProvideAndRegisterDocumentSet.class) } // debugging
            // Forward to XDS web service
            .to('xds-iti41:localhost:9091/xds-iti41')
            // Create success response
            .transform ( constant(new Response(Status.SUCCESS)) )

        // Entry point for GETing compositions
        rest().get("/ehr/{ehr_id}/composition/{version_uid}")
                .to("direct:getComposition")

        from("direct:getComposition")
            .process(new OpenEhrToRegistryStoredQueryProcessor())
            .convertBodyTo(AdhocQueryRequest.class)
            .process(iti18RequestValidator()) // debugging
            .to('xds-iti18:localhost:9091/xds-iti18')
            .process(iti18ResponseValidator()) // debugging
            // convert ITI-18 response of type AdhocQueryResponse (ebXML) to simplified model
            .convertBodyTo(QueryResponse.class)
            .process(new QueryResponseToRetrieveDocumentSetProcessor())
            // convert to ebXML representation and forward to retrieval
            .convertBodyTo(RetrieveDocumentSetRequestType.class)
            .process(iti43RequestValidator())
            .to('xds-iti43:localhost:9091/xds-iti43')
            // convert ITI-43 response of type RetrieveDocumentSetResponseType to simplified model
            .convertBodyTo(RetrievedDocumentSet.class)
            // finally extract and return payload
            .process(new RetrievedDocumentSetToOpenEhrProcessor())
            // return body as REST response
            .transform().body()
    }
}
