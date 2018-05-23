package br.com.esocial;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import br.com.samuelweb.certificado.Certificado;
import br.com.samuelweb.certificado.CertificadoService;
import br.com.samuelweb.certificado.TipoCertificadoA3;
import br.com.samuelweb.certificado.exception.CertificadoException;
import br.gov.esocial.www.servicos.empregador.lote.eventos.envio.consulta.retornoprocessamento.v1_1_0.ServicoConsultarLoteEventosStub;
import br.gov.esocial.www.servicos.empregador.lote.eventos.envio.v1_1_0.ServicoEnviarLoteEventosStub;

public class EsocialUtils {

//	/**
//	 * @see https://github.com/unkascrack/axis-ssl/blob/master/src/org/apache/axis/client/SSLClientAxisEngineConfig.java
//	 * @see https://stackoverflow.com/questions/30827978/how-to-programmatically-set-the-ssl-context-of-a-axis-client
//	 * @param xmlEnvelopado
//	 * @param urlEndpoint
//	 * @return
//	 * @throws XMLStreamException
//	 * @throws RemoteException
//	 */
//	public static String chamarWs() {
//		SOAPEnvelope resp = null;
//		String resposta = null;
//		String urlEndpoint = null;
//		try {
//			SSLClientAxisEngineConfig axisConfig = new SSLClientAxisEngineConfig();
//			axisConfig.setProtocol("TLS");
//			axisConfig.setAlgorithm("SunX509");
//			axisConfig.setKeyStore(facesUtils.getProp("CERTIFICADOS") + emitente.getCNPJ() + ".pfx");
//			axisConfig.setKeyStoreType("PKCS12");
//			axisConfig.setKeyStorePassword(emitente.getSenhaCertificado());
//			axisConfig.setTrustStore(sc.getRealPath("/keystore/keystorepr.jks"));
//			axisConfig.setTrustStoreType("JKS");
//			axisConfig.setTrustStorePassword("changeit");
//			
//			URL soapURL = new URL(urlEndpoint);
//			Service s = new Service(axisConfig);
//			Call call = (Call) s.createCall();
//			call.setTargetEndpointAddress(soapURL);
//			call.setOperationName(operacao);
//			call.setSOAPVersion(SOAPConstants.SOAP12_CONSTANTS);
//			SOAPEnvelope env = new SOAPEnvelope(new ByteArrayInputStream(envelope.getBytes()));
//			resposta = call.invoke(env);
//		} catch (SAXException ex) {
//			ex.printStackTrace();
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		} catch (ServiceException ex) {
//			ex.printStackTrace();
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		return resposta;
//
//	}

	public static String chamarConsultarWs(String xmlEnvelopado, String urlEndpoint){

		try {
			OMElement ome = AXIOMUtil.stringToOM(xmlEnvelopado);
	
			ServicoConsultarLoteEventosStub.Consulta_type0 dadosMsgType0 = new ServicoConsultarLoteEventosStub.Consulta_type0();
			dadosMsgType0.setExtraElement(ome);
	
			ServicoConsultarLoteEventosStub.ConsultarLoteEventos distConsultar = new ServicoConsultarLoteEventosStub.ConsultarLoteEventos();
			distConsultar.setConsulta(dadosMsgType0);
	
			ServicoConsultarLoteEventosStub stub = new ServicoConsultarLoteEventosStub(urlEndpoint);
			ServicoConsultarLoteEventosStub.ConsultarLoteEventosResponse response = stub
					.consultarLoteEventos(distConsultar);
			String xmlRetorno = response.getConsultarLoteEventosResult().getExtraElement().toString();
	
			return xmlRetorno;
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String chamarEnviarWs(String xmlEnvelopado, String urlEndpoint){

		try {
			OMElement ome = AXIOMUtil.stringToOM(xmlEnvelopado);
			ServicoEnviarLoteEventosStub.LoteEventos_type0 dadosMsgType0 = new ServicoEnviarLoteEventosStub.LoteEventos_type0();
			dadosMsgType0.setExtraElement(ome);

			ServicoEnviarLoteEventosStub.EnviarLoteEventos distEnvioEsocial = new ServicoEnviarLoteEventosStub.EnviarLoteEventos();
			distEnvioEsocial.setLoteEventos(dadosMsgType0);

			ServicoEnviarLoteEventosStub stub = new ServicoEnviarLoteEventosStub(urlEndpoint);
			ServicoEnviarLoteEventosStub.EnviarLoteEventosResponse result = stub.enviarLoteEventos(distEnvioEsocial);
			String xmlRetorno = result.getEnviarLoteEventosResult().getExtraElement().toString();
			return xmlRetorno;
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	
	public static String assinarXml(String xml, Certificado certificado){
		String xmlAssinado = null;

		try {
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
	
			xmlAssinado = documentParaString(doc, false);
		}catch (Exception e) {
			throw new RuntimeException(e);
		}

		return xmlAssinado;
	}

	public static String documentParaString(Document doc, boolean declaracaoXml) throws TransformerException {
		StringWriter sw = new StringWriter();
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		if (declaracaoXml) {
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		} else {
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		}
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		// transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		// transformer.setOutputProperty(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT,
		// "2");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.transform(new DOMSource(doc), new StreamResult(sw));
		return sw.toString();
	}

	public static Document stringParaDocument(String xml)
			throws SAXException, IOException, ParserConfigurationException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			// dbf.setNamespaceAware(true);
			xml = xml.replaceAll("[^\\x20-\\x7e\\x0A]", "").replaceAll("\\r\\n", "");
			InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
			Document doc = dbf.newDocumentBuilder().parse(inputStream);
			return doc;
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * O certificado deve estar plugado na entrada USB e ser do tipo ALADDIN. Recomendado ser do tipo eCPF/eCNPJ com a
	 * raiz da Autoridade Certificadora Raiz Brasileira (ICP-Brasil)
	 * 
	 * @see http://portal.esocial.gov.br/manuais/mensagenssistemaesocialv1-3.pdf 148
	 * @param senha
	 */
	public static Certificado getCertificadoA3(String senha) throws CertificadoException {
		Certificado certificado = CertificadoService.certificadoA3(TipoCertificadoA3.TOKEN_ALADDIN.getMarca(),
				TipoCertificadoA3.TOKEN_ALADDIN.getDll(), senha);
		return certificado;
	}

	/**
	 * Recomendado ser do tipo eCPF/eCNPJ com a raiz da Autoridade Certificadora Raiz Brasileira (ICP-Brasil)
	 *
	 * @see http://portal.esocial.gov.br/manuais/mensagenssistemaesocialv1-3.pdf 148
	 * @param caminhoCertificadoA1
	 * @param senhaCertificadoA1
	 */
	public static Certificado getCertificadoA1(String caminhoCertificadoA1, String senhaCertificadoA1)
			throws CertificadoException {
		Certificado certificado = CertificadoService.certificadoPfx(caminhoCertificadoA1, senhaCertificadoA1);
		return certificado;
	}

	/**
	 * Inicializador do SocketDinamico para comunicacao com webservice que necessita de autenticacao com certificado
	 * digital. Depende de um arquivo Cacerts customizado.
	 * 
	 * @see https://github.com/Samuel-Oliveira/Java_Certificado/blob/master/src/main/java/br/com/samuelweb/certificado/util/CacertUtil.java
	 * @see http://www.javac.com.br/jc/posts/list/2736-arquivo...reinf-atualizado-22042018.page
	 * 
	 * @param caminhoCacerts
	 * @param certificado
	 */
	public static void inicializarCertificadSocketDinamico(String caminhoCacerts, Certificado certificado)
			throws FileNotFoundException, CertificadoException {
		InputStream inputStreamCacerts = new FileInputStream(caminhoCacerts);
		certificado.setAtivarProperties(false);
		CertificadoService.inicializaCertificado(certificado, inputStreamCacerts);
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


	/**
	 * Formata XML para facilitar visualizacao e conferencia.
	 * @param xml
	 * @param ommitXmlDeclaration
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static String formatarXml(String xml, Boolean ommitXmlDeclaration) {

		try {
			Document doc = stringParaDocument(xml);
	
			OutputFormat format = new OutputFormat(doc);
			format.setIndenting(true);
			format.setIndent(2);
			format.setOmitXMLDeclaration(ommitXmlDeclaration);
			format.setLineWidth(Integer.MAX_VALUE);
			Writer outxml = new StringWriter();
			XMLSerializer serializer = new XMLSerializer(outxml, format);
			serializer.serialize(doc);
			xml = outxml.toString();
			
	//        TransformerFactory tfactory = TransformerFactory.newInstance();
	//        Transformer serializer;
	//        try {
	//            serializer = tfactory.newTransformer();
	//            //Setup indenting to "pretty print"
	//            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
	//            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
	//             
	//            StringWriter writer = new StringWriter();
	//            serializer.transform(new DOMSource(doc), new StreamResult(writer));
	//            xml =  writer.getBuffer().toString();
	//        } catch (TransformerException e) {
	//            throw new RuntimeException(e);
	//        }
	        if (ommitXmlDeclaration) {
	        	xml = retirarDeclaracaoXml(xml);
	        }
	        return xml;
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Valida um XML de acordo com o XSD.
	 * TODO precisa de ajustes
	 * @param xml
	 * @param caminhoXsd
	 * @throws SAXException
	 * @throws IOException
	 */
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

	public static String xpathFinder(String xml, String xpathQuery){
		// DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// factory.setNamespaceAware(true);
		try {
			Document doc = stringParaDocument(xml);
	
			String resultado = null;
	
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
	
				XPathExpression expr = xpath.compile(xpathQuery);
				NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
				// for (int i = 0; i < nodes.getLength(); i++)
				// list.add(nodes.item(i).getNodeValue());
				if (nodes.getLength() != 0) {
					resultado = nodes.item(0).getNodeValue();
				}
	
			// InputSource source = new InputSource(new StringReader(xml));
			// String resultado = xpath.evaluate(xpathQuery, source);
	
			return resultado;
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Retira tudo o que estiver entre <?xml e ?>
	 * exemplo: <?xml version="1.0" encoding="UTF-8"?>
	 * @param xml
	 * @return
	 */
	public static String retirarDeclaracaoXml(String xml) {
    	return xml.replaceAll("\\<\\?xml(.+?)\\?\\>", "").trim();
	}

}
