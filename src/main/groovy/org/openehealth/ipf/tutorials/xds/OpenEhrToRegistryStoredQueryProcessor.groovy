package org.openehealth.ipf.tutorials.xds

import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.openehealth.ipf.commons.ihe.xds.core.SampleData
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Document
import org.openehealth.ipf.commons.ihe.xds.core.requests.QueryRegistry
import org.openehealth.ipf.commons.ihe.xds.core.requests.query.FindDocumentsQuery
import org.openehealth.ipf.commons.ihe.xds.core.requests.query.GetDocumentsQuery
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.activation.DataHandler

/**
 * Processor to convert openEHR REST API request into valid ITI-18 request.
 * TODO: Static sample test data only right now!
 */
public class OpenEhrToRegistryStoredQueryProcessor implements Processor{
    private final static Logger log = LoggerFactory.getLogger(OpenEhrToProvideAndRegisterProcessor.class)

    public void process(Exchange exchange) throws Exception {
        // TODO: take requests data to generate QueryRegistry

        // TODO: temp: gets overwritten in next step anyway
        def query = new GetDocumentsQuery()
        List<String> ids = "4.3.2.1" as List<String>
        query.setUniqueIds(ids)
        def queryRegistry = new QueryRegistry(query)

        exchange.getIn().setBody(queryRegistry)
    }
}
