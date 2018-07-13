package org.openehealth.ipf.tutorials.xds

import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.openehealth.ipf.commons.ihe.xds.core.SampleData
import org.openehealth.ipf.platform.camel.ihe.xds.core.converters.EbXML30Converters

import javax.activation.DataHandler

/**
 * Processor to convert openEHR REST API request into XDS compatible exchange.
 * TODO: Static sample test data only right now!
 */
public class OpenEhr2XDSProcessor implements Processor{
    public void process(Exchange exchange) throws Exception {
        String compositionPayload = exchange.getIn().getBody(String.class)

        // TODO: make following test data include given data from exchange (see above)
        def provideAndRegisterDocSet = SampleData.createProvideAndRegisterDocumentSet()
        //provide.getDocuments().add(compositionPayload as entry)
        def docEntry = provideAndRegisterDocSet.documents[0].documentEntry
        def patientId = docEntry.patientId
        patientId.id = UUID.randomUUID().toString()
        docEntry.uniqueId = '4.3.2.1'
        docEntry.hash = ContentUtils.sha1(provideAndRegisterDocSet.documents[0].getContent(DataHandler))
        docEntry.size = ContentUtils.size(provideAndRegisterDocSet.documents[0].getContent(DataHandler))

        exchange.getIn().setBody(provideAndRegisterDocSet)

        // shouldn't be necessary since test doesn't convert too, right?
        //def converted = EbXML30Converters.convert(provideAndRegisterDocSet)
        //exchange.getIn().setBody(converted)
    }
}
