package br.com.esocial;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import br.com.esocial.Util.SocketFactoryDinamico;
import br.gov.esocial.www.servicos.empregador.lote.eventos.envio.v1_1_0.ServicoEnviarLoteEventosStub;

public class EnvioXML2 {

	private static final int SSL_PORT = 443;
	private static String HTTPS = "https";

	public static void main(String args[]) throws Exception {
		String caminhoXML = "/Users/Amiguinho/Development/tmp/esocial/S1000.xml";
		String caminhoCacert = "/Users/Amiguinho/Development/tmp/esocial/Cacert-22-04-2018";
		String caminhoCertificadoA1 = "/Users/Amiguinho/Development/tmp/esocial/mpt-cos-hom_mpt_mp_br3.p12";

//		String caminhoXML = "C:/desenv/sandbox/esocial/S1000.xml";
//		String caminhoCacert = "C:/desenv/sandbox/esocial/Cacert-22-04-2018";
//		String caminhoCertificadoA1 = "C:/desenv/sandbox/esocial/mpt-cos-hom_mpt_mp_br3.p12";

		String senhaCertificadoA1 = "mpt";

		String xml = arquivoParaString(caminhoXML);
		// String xml =
		// textoWebParaString("http://suporte.quarta.com.br/eSocial/arquivos/S1000.xml");

		String xmlAssinado = assinarXML(xml, caminhoCertificadoA1, senhaCertificadoA1);
		String xmlEventos = enveloparDummyEventos(xmlAssinado);
		String xmlEnvelopado = enveloparSoap(xmlEventos);

		// xmlEnvelopado = documentParaString(stringParaDocument(xmlEnvelopado));

		// String resultado = xpathFinder("<?xml version=\"1.0\"
		// encoding=\"UTF-8\"?><carro><ford><mustang
		// id=\"1\">cavalo</mustang><ka id=\"2\">errado</ka></ford></carro>",
		// "/carro/ford/mustang");
		// resultado = xpathFinder("<carro><ford><mustang
		// Id=\"1\">cavalo</mustang><ka Id=\"2\">errado</ka></ford></carro>",
		// "/carro/ford/mustang/@Id");
		// System.out.println("\n\n resultado: " + resultado);

		// String idEvtInfoEmpregador = xpathFinder(xmlAssinado,
		// "/*[local-name()='eSocial']/*[local-name()='evtInfoEmpregador']/@Id");
		// System.out.println("idEvtInfoEmpregador: " + idEvtInfoEmpregador);
		//
		// String nrInsc = xpathFinder(xmlAssinado,
		// "/*[local-name()='eSocial']" +
		// "/*[local-name()='evtInfoEmpregador']" +
		// "/*[local-name()='ideEmpregador']"+
		// "/*[local-name()='nrInsc']/text()");
		// System.out.println("nrInsc: " + nrInsc);

//		X509Certificate certificadoA1 = carregarCertificado(caminhoCertificadoA1, senhaCertificadoA1);
//		PrivateKey privateKey = carregarChavePrivada(caminhoCertificadoA1, senhaCertificadoA1);
//		SocketFactoryDinamico socketFactoryDinamico = new SocketFactoryDinamico(certificadoA1, privateKey);
//		socketFactoryDinamico.setFileCacerts(caminhoCacert);
//
//		Protocol protocol = new Protocol(HTTPS, socketFactoryDinamico, SSL_PORT);
//		Protocol.registerProtocol(HTTPS, protocol);

		URL url = new URL(
				"https://webservices.producaorestrita.esocial.gov.br/servicos/empregador/enviarloteeventos/WsEnviarLoteEventos.svc");

		System.out.println(xmlEnvelopado);
		
//		EnviarEsocialServiceClient.enviarLotes(xmlEnvelopado);

		OMElement ome = AXIOMUtil.stringToOM(xmlEnvelopado);
		ServicoEnviarLoteEventosStub.LoteEventos_type0 dadosMsgType0 = new ServicoEnviarLoteEventosStub.LoteEventos_type0();
		dadosMsgType0.setExtraElement(ome);

		ServicoEnviarLoteEventosStub.EnviarLoteEventos distEnvioEsocial = new ServicoEnviarLoteEventosStub.EnviarLoteEventos();
		distEnvioEsocial.setLoteEventos(dadosMsgType0);

		ServicoEnviarLoteEventosStub stub = new ServicoEnviarLoteEventosStub(url.toString());
		ServicoEnviarLoteEventosStub.EnviarLoteEventosResponse result = stub.enviarLoteEventos(distEnvioEsocial);
		result.getEnviarLoteEventosResult().getExtraElement().toString();

		System.out.println(result.getEnviarLoteEventosResult().getExtraElement().toString());

	}

	public static String assinarXML(String xml, String caminhoCertificadoA1, String senhaCertificadoA1)
			throws Exception {
		String xmlAssinado = null;

		final String C14N_TRANSFORM_METHOD = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";

		XMLSignatureFactory sig = null;
		SignedInfo si = null;
		KeyInfo ki = null;

		X509Certificate cert = carregarCertificado(caminhoCertificadoA1, senhaCertificadoA1);

		Document doc = stringParaDocument(xml);

		sig = XMLSignatureFactory.getInstance("DOM");
		ArrayList<Transform> transformList = new ArrayList<Transform>();
		Transform enveloped = sig.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
		Transform c14n = sig.newTransform(C14N_TRANSFORM_METHOD, (TransformParameterSpec) null);
		transformList.add(enveloped);
		transformList.add(c14n);

		Reference r = sig.newReference("", sig.newDigestMethod("http://www.w3.org/2001/04/xmlenc#sha256", null),
				transformList, null, null);
		si = sig.newSignedInfo(
				sig.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
				sig.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null),
				Collections.singletonList(r));

		KeyInfoFactory kif = sig.getKeyInfoFactory();
		List<X509Certificate> x509Content = new ArrayList<X509Certificate>();
		x509Content.add(cert);
		X509Data xd = kif.newX509Data(x509Content);
		ki = kif.newKeyInfo(Collections.singletonList(xd));
		PrivateKey privateKey = carregarChavePrivada(caminhoCertificadoA1, senhaCertificadoA1);
		DOMSignContext dsc = new DOMSignContext(privateKey, doc.getDocumentElement());
		XMLSignature signature = sig.newXMLSignature(si, ki);
		signature.sign(dsc); // neste momento é solicitada a senha do token

		xmlAssinado = documentParaString(doc);

		return xmlAssinado;
	}

	public static X509Certificate carregarCertificado(String caminhoCertificadoA1, String senhaCertificadoA1)
			throws Exception {
		X509Certificate crtCertificado = null;
		KeyStore.PrivateKeyEntry keyEntry;
		try {
			KeyStore ks = funcKeyStore(caminhoCertificadoA1, senhaCertificadoA1);
			String alias = getAlias(ks);
			keyEntry = (PrivateKeyEntry) ks.getEntry(alias,
					new KeyStore.PasswordProtection(senhaCertificadoA1.toCharArray()));
			crtCertificado = (X509Certificate) keyEntry.getCertificate();
		} catch (KeyStoreException ex) {
			throw ex;
		}
		return crtCertificado;
	}

	public static PrivateKey carregarChavePrivada(String caminhoCertificadoA1, String senhaCertificadoA1)
			throws Exception {
		KeyStore ks = null;
		PrivateKey privateKey = null;
		ks = funcKeyStore(caminhoCertificadoA1, senhaCertificadoA1);
		String alias = getAlias(ks);
		privateKey = (PrivateKey) ks.getKey(alias, senhaCertificadoA1.toCharArray());
		return privateKey;
	}

	private static String getAlias(KeyStore ks) throws KeyStoreException {
		String alias = null;
		Enumeration<String> aliasesEnum = ks.aliases();
		while (aliasesEnum.hasMoreElements()) {
			alias = (String) aliasesEnum.nextElement();
			break;
			// System.out.println("alias: " + alias);
			// if (ks.isKeyEntry(alias)) {
			// break;
			// }
		}
		return alias;
	}

	public static KeyStore funcKeyStore(String caminhoCertificadoA1, String senhaCertificadoA1) throws Exception {
		InputStream entrada = new FileInputStream(caminhoCertificadoA1);
		KeyStore ks = KeyStore.getInstance("pkcs12");
		try {
			ks.load(entrada, senhaCertificadoA1.toCharArray());
		} catch (IOException e) {
			throw new Exception("Senha do Certificado Digital esta incorreta ou Certificado inválido.");
		}
		return ks;
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

	public static String xpathFinder(String xml, String xpathQuery)
			throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		// DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// factory.setNamespaceAware(true);
		Document doc = stringParaDocument(xml);

		String resultado = null;

		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		try {
			XPathExpression expr = xpath.compile(xpathQuery);
			NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			// for (int i = 0; i < nodes.getLength(); i++)
			// list.add(nodes.item(i).getNodeValue());
			if (nodes.getLength() != 0) {
				resultado = nodes.item(0).getNodeValue();
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		// InputSource source = new InputSource(new StringReader(xml));
		// String resultado = xpath.evaluate(xpathQuery, source);

		return resultado;
	}

	public static String textoWebParaString(String url) throws ParserConfigurationException, MalformedURLException,
			SAXException, IOException, TransformerException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new URL(url).openStream());

		String texto = documentParaString(doc);
		return texto;
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
}
