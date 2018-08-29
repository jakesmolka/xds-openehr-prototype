package org.openehealth.ipf.tutorials.xds

import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.openehealth.ipf.commons.ihe.ws.utils.LargeDataSource
import org.openehealth.ipf.commons.ihe.xds.core.SampleData
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Address
import org.openehealth.ipf.commons.ihe.xds.core.metadata.AssigningAuthority
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Association
import org.openehealth.ipf.commons.ihe.xds.core.metadata.AssociationLabel
import org.openehealth.ipf.commons.ihe.xds.core.metadata.AssociationType
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Author
import org.openehealth.ipf.commons.ihe.xds.core.metadata.AvailabilityStatus
import org.openehealth.ipf.commons.ihe.xds.core.metadata.CXiAssigningAuthority
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Code
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Document
import org.openehealth.ipf.commons.ihe.xds.core.metadata.DocumentEntry
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Folder
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Identifiable
import org.openehealth.ipf.commons.ihe.xds.core.metadata.LocalizedString
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Name
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Organization
import org.openehealth.ipf.commons.ihe.xds.core.metadata.PatientInfo
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Person
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Recipient
import org.openehealth.ipf.commons.ihe.xds.core.metadata.ReferenceId
import org.openehealth.ipf.commons.ihe.xds.core.metadata.SubmissionSet
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Telecom
import org.openehealth.ipf.commons.ihe.xds.core.metadata.XpnName
import org.openehealth.ipf.commons.ihe.xds.core.requests.ProvideAndRegisterDocumentSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.activation.DataHandler

/**
 * Processor to convert openEHR REST API request into valid ITI-41 request.
 * TODO: Static sample test data only right now!
 */
public class OpenEhrToProvideAndRegisterProcessor implements Processor{
    private final static Logger log = LoggerFactory.getLogger(OpenEhrToProvideAndRegisterProcessor.class)

    public void process(Exchange exchange) throws Exception {
        // add document with input from POST as payload
        // TODO: later: this needs to be generalized, so above test can be removed
        String compositionPayload = exchange.getIn().getBody(String.class)
        def dataHandler = new DataHandler(compositionPayload, "text/plain")
        def request = sampleProvideAndRegisterDocumentSet(dataHandler)

        exchange.getIn().setBody(request)
    }

    /**
     * Creates sample ITI-41 request copied from ...ipf.commons.ihe.xds.core.SampleData.
     * (Same goes for following methods used by this one.)
     * Allows to work with mock data and step by step replace it with real data.
     *
     * @return Sample {@Link ProvideAndRegisterDocumentSet}
     */
    private ProvideAndRegisterDocumentSet sampleProvideAndRegisterDocumentSet(DataHandler dataHandler) {
        Identifiable patientID = new Identifiable("id3", new AssigningAuthority("1.3"));

        SubmissionSet submissionSet = createSubmissionSet(patientID)
        DocumentEntry docEntry = createDocumentEntry(patientID)
        Folder folder = createFolder(patientID)

        Association docAssociation = createAssociationDocEntryToSubmissionSet()
        Association folderAssociation = createAssociationFolderToSubmissionSet()
        Association docFolderAssociation = createAssociationDocEntryToFolder()

        // sample not needed anymore
        //def dataHandler = new DataHandler(new LargeDataSource())

        // modified to take given data instead of sample
        Document doc = new Document(docEntry, dataHandler)
        // necessary?
        docEntry.patientId.id = UUID.randomUUID().toString()
        docEntry.uniqueId = '4.3.2.1'
        // overwrite sample hash and size
        docEntry.hash = ContentUtils.sha1(dataHandler)
        docEntry.size = ContentUtils.size(dataHandler)

        ProvideAndRegisterDocumentSet request = new ProvideAndRegisterDocumentSet();
        request.setSubmissionSet(submissionSet);
        request.getDocuments().add(doc);
        request.getFolders().add(folder);
        request.getAssociations().add(docAssociation);
        request.getAssociations().add(folderAssociation);
        request.getAssociations().add(docFolderAssociation);

        request.setTargetHomeCommunityId("urn:oid:1.2.3.4.5.6.2333.23");
        request
    }

    private Association createAssociationDocEntryToFolder() {
        Association docFolderAssociation = new Association();
        docFolderAssociation.setAssociationType(AssociationType.HAS_MEMBER);
        docFolderAssociation.setSourceUuid("folder01");
        docFolderAssociation.setTargetUuid("document01");
        docFolderAssociation.setEntryUuid("docFolderAss");
        docFolderAssociation
    }

    private Association createAssociationFolderToSubmissionSet() {
        Association folderAssociation = new Association();
        folderAssociation.setAssociationType(AssociationType.HAS_MEMBER);
        folderAssociation.setSourceUuid("submissionSet01");
        folderAssociation.setTargetUuid("folder01");
        folderAssociation.setEntryUuid("folderAss");
        folderAssociation.setPreviousVersion("110");
        folderAssociation
    }

    private Association createAssociationDocEntryToSubmissionSet() {
        Association docAssociation = new Association();
        docAssociation.setAssociationType(AssociationType.HAS_MEMBER);
        docAssociation.setSourceUuid("submissionSet01");
        docAssociation.setTargetUuid("document01");
        docAssociation.setLabel(AssociationLabel.ORIGINAL);
        docAssociation.setEntryUuid("docAss");
        docAssociation.setPreviousVersion("111");
        docAssociation
    }

    private Folder createFolder(Identifiable patientID) {
        Folder folder = new Folder();
        folder.setAvailabilityStatus(AvailabilityStatus.APPROVED);
        folder.getCodeList().add(new Code("code7", new LocalizedString("code7"), "scheme7"));
        folder.setComments(new LocalizedString("comments3"));
        folder.setEntryUuid("folder01");
        folder.setLastUpdateTime("19820910121315");
        folder.setPatientId(patientID);
        folder.setTitle(new LocalizedString("Folder 01", "en-US", "UTF8"));
        folder.setUniqueId("48574589");
        folder
    }

    private DocumentEntry createDocumentEntry(Identifiable patientID) {
        Author author2 = new Author();
        Name name = new XpnName();
        name.setFamilyName("Norbi");
        author2.setAuthorPerson(new Person(new Identifiable("id2", new AssigningAuthority("1.2")), name));
        author2.getAuthorInstitution().add(new Organization("authorOrg", null, null));
        author2.getAuthorRole().add(new Identifiable("role1", new AssigningAuthority("1.2.3.1", "ISO")));
        author2.getAuthorRole().add(new Identifiable("role2", null));
        author2.getAuthorSpecialty().add(new Identifiable("spec1", new AssigningAuthority("1.2.3.3", "ISO")));
        author2.getAuthorSpecialty().add(new Identifiable("spec2", null));
        author2.getAuthorTelecom().add(new Telecom("author1@acme.org"));
        author2.getAuthorTelecom().add(new Telecom("author2@acme.org"));

        Address address = new Address();
        address.setStreetAddress("hier");

        PatientInfo patientInfo = new PatientInfo();
        patientInfo.getAddresses().add(address);
        patientInfo.setDateOfBirth("1980");
        patientInfo.setGender("M");
        patientInfo.getNames().add(new XpnName("Susi", null, null, null, null, null));

        DocumentEntry docEntry = new DocumentEntry();
        docEntry.getAuthors().add(author2);
        docEntry.setAvailabilityStatus(AvailabilityStatus.APPROVED);
        docEntry.setClassCode(new Code("code2", new LocalizedString("code2"), "scheme2"));
        docEntry.setComments(new LocalizedString("comment2"));
        docEntry.getConfidentialityCodes().add(new Code("code8", new LocalizedString("code8"), "scheme8"));
        docEntry.setCreationTime("1981");
        docEntry.setEntryUuid("document01");
        docEntry.getEventCodeList().add(new Code("code9", new LocalizedString("code9"), "scheme9"));
        docEntry.setFormatCode(new Code("code3", new LocalizedString("code3"), "scheme3"));
        docEntry.setHash("1234567890123456789012345678901234567890");
        docEntry.setHealthcareFacilityTypeCode(new Code("code4", new LocalizedString("code4"), "scheme4"));
        docEntry.setLanguageCode("en-US");
        docEntry.setLegalAuthenticator(new Person(new Identifiable("legal", new AssigningAuthority("1.7")),
                new XpnName("Gustav", null, null, null, null, null)));
        docEntry.setMimeType("application/octet-stream");
        docEntry.setPatientId(patientID);
        docEntry.setPracticeSettingCode(new Code("code5", new LocalizedString("code5"), "scheme5"));
        docEntry.setRepositoryUniqueId("1.2.3.4");
        docEntry.setServiceStartTime("198012");
        docEntry.setServiceStopTime("198101");
        docEntry.setSize(123L);
        docEntry.setSourcePatientId(new Identifiable("source", new AssigningAuthority("4.1")));
        docEntry.setSourcePatientInfo(patientInfo);
        docEntry.setTitle(new LocalizedString("Document 01", "en-US", "UTF8"));
        docEntry.setTypeCode(new Code("code6", new LocalizedString("code6"), "scheme6"));
        docEntry.setUniqueId("32848902348");
        docEntry.setUri("http://hierunten.com");
        docEntry.getReferenceIdList().add(new ReferenceId(
                "ref-id-1", new CXiAssigningAuthority("ABCD", "1.1.2.3", "ISO"),
                ReferenceId.ID_TYPE_CODE_ORDER));
        docEntry.getReferenceIdList().add(new ReferenceId(
                "ref-id-2", new CXiAssigningAuthority("DEFG", "2.1.2.3", "ISO"),
                "vendor-defined"));
        docEntry
    }

    private SubmissionSet createSubmissionSet(Identifiable patientID) {
        Recipient recipient = new Recipient();
        recipient.setOrganization(new Organization("org", null, null));

        Author author = new Author();
        author.setAuthorPerson(new Person(new Identifiable("id1", new AssigningAuthority("1.1")),
                new XpnName("Otto", null, null, null, null, null)));

        SubmissionSet submissionSet = new SubmissionSet();
        submissionSet.getAuthors().add(author);
        submissionSet.setAvailabilityStatus(AvailabilityStatus.APPROVED);
        submissionSet.setComments(new LocalizedString("comments1"));
        submissionSet.setContentTypeCode(new Code("code1", new LocalizedString("code1"), "scheme1"));
        submissionSet.setEntryUuid("submissionSet01");
        submissionSet.getIntendedRecipients().add(recipient);
        submissionSet.setPatientId(patientID);
        submissionSet.setSourceId("1.2.3");
        submissionSet.setSubmissionTime("1980");
        submissionSet.setTitle(new LocalizedString("Submission Set 01", "en-US", "UTF8"));
        submissionSet.setUniqueId("123");
        submissionSet.setHomeCommunityId("urn:oid:1.2.3.4.5.6.2333.23");
        submissionSet
    }
}
