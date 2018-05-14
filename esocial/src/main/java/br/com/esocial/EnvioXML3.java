package br.com.esocial;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import br.com.samuelweb.certificado.Certificado;
import br.com.samuelweb.certificado.CertificadoService;
import br.gov.esocial.www.servicos.empregador.lote.eventos.envio.v1_1_0.ServicoEnviarLoteEventosStub;

public class EnvioXML3 {

	public static void main(String args[]) throws Exception {
		System.out.println("java version: " + System.getProperty("java.version"));

		String caminhoXML = "/Users/Amiguinho/Development/tmp/esocial/S1000.xml";
		String caminhoCacert = "/Users/Amiguinho/Development/tmp/esocial/Cacert-22-04-2018_2";
		String caminhoCertificadoA1 = "/Users/Amiguinho/Development/tmp/esocial/mpt-cos-hom_mpt_mp_br3.p12";

//		String caminhoXML = "C:/desenv/sandbox/esocial/S1000.xml";
//		String caminhoCacert = "C:/desenv/sandbox/esocial/keystore/Cacert-22-04-2018_3_certs_eSocial";
//		String caminhoCertificadoA1 = "C:/desenv/sandbox/esocial/mpt-cos-hom_mpt_mp_br3.p12";
		String senhaCertificadoA1 = "mpt";

		// -- carrega o certificado, cacert custom e inicializa o proxy dimanico
		InputStream isCacert = new FileInputStream(caminhoCacert);
		Certificado certificado = CertificadoService.certificadoPfx(caminhoCertificadoA1, senhaCertificadoA1);
		certificado.setAtivarProperties(false);
		CertificadoService.inicializaCertificado(certificado, isCacert);

		// -- assina o xml
		String xml = arquivoParaString(caminhoXML);
		//validar(xml, "C:\\desenv\\sandbox\\esocial\\EsquemasXSDv2.4.02\\evtInfoEmpregador.xsd");
		String xmlAssinado = assinarXML(xml, certificado);
		String xmlEventos = enveloparDummyEventos(xmlAssinado);
		String xmlEnvelopado = enveloparSoap(xmlEventos);
		xmlEnvelopado = format(xmlEnvelopado, false);

		URL url = new URL(
				"https://webservices.producaorestrita.esocial.gov.br/servicos/empregador/enviarloteeventos/WsEnviarLoteEventos.svc");

		System.out.println(xmlEnvelopado);

		OMElement ome = AXIOMUtil.stringToOM(xmlEnvelopado);
		ServicoEnviarLoteEventosStub.LoteEventos_type0 dadosMsgType0 = new ServicoEnviarLoteEventosStub.LoteEventos_type0();
		dadosMsgType0.setExtraElement(ome);

		ServicoEnviarLoteEventosStub.EnviarLoteEventos distEnvioEsocial = new ServicoEnviarLoteEventosStub.EnviarLoteEventos();
		distEnvioEsocial.setLoteEventos(dadosMsgType0);

		ServicoEnviarLoteEventosStub stub = new ServicoEnviarLoteEventosStub(url.toString());
		ServicoEnviarLoteEventosStub.EnviarLoteEventosResponse result = stub.enviarLoteEventos(distEnvioEsocial);
		result.getEnviarLoteEventosResult().getExtraElement().toString();

		System.out.println(
				"\n\nRETORNO DO ESOCIAL \n");
		String retorno = result.getEnviarLoteEventosResult().getExtraElement().toString();
		retorno = format(retorno, false);
		System.out.println(retorno);

	}

	public static String assinarXML(String xml, Certificado certificado) throws Exception {
		String xmlAssinado = null;

		final String C14N_TRANSFORM_METHOD = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";

		KeyStore keyStore = CertificadoService.getKeyStore(certificado);
		X509Certificate x509Certificado = CertificadoService.getCertificate(certificado, keyStore);

		Document doc = stringParaDocument(xml);

		XMLSignatureFactory sig = XMLSignatureFactory.getInstance("DOM");
		ArrayList<Transform> transformList = new ArrayList<Transform>();
		Transform enveloped = sig.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
		Transform c14n = sig.newTransform(C14N_TRANSFORM_METHOD, (TransformParameterSpec) null);
		transformList.add(enveloped);
		transformList.add(c14n);

		Reference r = sig.newReference("", sig.newDigestMethod("http://www.w3.org/2001/04/xmlenc#sha256", null),
				transformList, null, null);
		SignedInfo si = sig.newSignedInfo(
				sig.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
				sig.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null),
				Collections.singletonList(r));

		KeyInfoFactory kif = sig.getKeyInfoFactory();
		List<X509Certificate> x509Content = new ArrayList<X509Certificate>();
		x509Content.add(x509Certificado);
		X509Data xd = kif.newX509Data(x509Content);
		KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));
		PrivateKey privateKey = (PrivateKey) keyStore.getKey(certificado.getNome(),
				certificado.getSenha().toCharArray());
		DOMSignContext dsc = new DOMSignContext(privateKey, doc.getDocumentElement());
		XMLSignature signature = sig.newXMLSignature(si, ki);
		signature.sign(dsc); // neste momento é solicitada a senha do token

		xmlAssinado = documentParaString(doc);

		return xmlAssinado;
	}

	public static String documentParaString(Document doc) throws TransformerException {
		StringWriter sw = new StringWriter();
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "2");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.transform(new DOMSource(doc), new StreamResult(sw));
		return sw.toString();
	}

	public static Document stringParaDocument(String xml)
			throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// dbf.setNamespaceAware(true);
		InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
		Document doc = dbf.newDocumentBuilder().parse(inputStream);
		return doc;
	}

	/**
	 * StandardCharsets.UTF_8, pode ser trocado por Charset.defaultCharset()
	 */
	public static String arquivoParaString(String caminhoArquivo) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(caminhoArquivo));
		return new String(encoded, StandardCharsets.UTF_8);
	}

	public static String enveloparSoap(String xml) {

		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		sb.append("<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
		sb.append(" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"");
		sb.append(" xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">");
		sb.append("<soap:Header/>");
		sb.append("<soap:Body>");
		sb.append(xml);
		sb.append("</soap:Body>");
		sb.append("</soap:Envelope>");

		return sb.toString();

	}

	public static String enveloparDummyEventos(String xml) {
		StringBuilder sb = new StringBuilder();
		sb.append("<eSocial xmlns=\"http://www.esocial.gov.br/schema/lote/eventos/envio/v1_1_1\">");
		sb.append("<envioLoteEventos grupo=\"1\">");
		sb.append("     <ideEmpregador>");
		sb.append("         <tpInsc>1</tpInsc>");
		sb.append("         <nrInsc>12345678</nrInsc>");
		sb.append("     </ideEmpregador>");
		sb.append("     <ideTransmissor>");
		sb.append("         <tpInsc>2</tpInsc>");
		sb.append("         <nrInsc>12345678901</nrInsc>");
		sb.append("     </ideTransmissor>");
		sb.append("     <eventos>");
		sb.append("         <evento Id=\"ID1123456780000002018050716254600001\">");
		sb.append(xml);
		sb.append("         </evento>");
		sb.append("     </eventos>");
		sb.append("</envioLoteEventos>");
		sb.append("</eSocial>");

		return sb.toString();

	}

	public static String format(String xml, Boolean ommitXmlDeclaration)
			throws IOException, SAXException, ParserConfigurationException {

		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = db.parse(new InputSource(new StringReader(xml)));

		OutputFormat format = new OutputFormat(doc);
		format.setIndenting(true);
		format.setIndent(2);
		format.setOmitXMLDeclaration(ommitXmlDeclaration);
		format.setLineWidth(Integer.MAX_VALUE);
		Writer outxml = new StringWriter();
		XMLSerializer serializer = new XMLSerializer(outxml, format);
		serializer.serialize(doc);

		return outxml.toString();

	}

	public static void validar(String xml, String caminhoXsd) throws SAXException, IOException {
		File schemaFile = new File(caminhoXsd);
		Source xmlFile = new StreamSource(new StringReader(xml));
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			Schema schema = schemaFactory.newSchema(schemaFile);
			Validator validator = schema.newValidator();
			validator.validate(xmlFile);
			System.out.println(xmlFile.getSystemId() + " eh valido");
		} catch (SAXException e) {
			System.out.println(xmlFile.getSystemId() + " NAO EH valido :" + e);
			throw e;
		} catch (IOException e) {
			throw e;
		}

	}

}
