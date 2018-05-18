package br.com.esocial;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.rmi.RemoteException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import br.com.samuelweb.certificado.Certificado;
import br.com.samuelweb.certificado.CertificadoService;
import br.com.samuelweb.certificado.TipoCertificadoA3;
import br.com.samuelweb.certificado.exception.CertificadoException;
import br.gov.esocial.www.servicos.empregador.lote.eventos.envio.consulta.retornoprocessamento.v1_1_0.ServicoConsultarLoteEventosStub;

public class ConsultarXML {
	
	private static final String URL = "https://webservices.producaorestrita.esocial.gov.br/servicos/empregador/consultarloteeventos/WsConsultarLoteEventos.svc";
	
	public static void main(String[] args) throws CertificadoException, XMLStreamException, IOException, SAXException, ParserConfigurationException {
		
		String caminhoCacert = "C:/desenv/certificados/cacert/Cacert-22-04-2018";
		String caminhoCertificadoA1 = "C:/desenv/sandbox/esocial/mpt-cos-hom_mpt_mp_br3.p12";
		String senhaCertificadoA1 = "mpt";

		// -- carrega o certificado, cacert custom e inicializa o proxy dimanico
		InputStream isCacert = new FileInputStream(caminhoCacert);
//		Certificado certificado = CertificadoService.certificadoPfx(caminhoCertificadoA1, senhaCertificadoA1);
		Certificado certificado = CertificadoService.certificadoA3(TipoCertificadoA3.TOKEN_ALADDIN.getMarca(), TipoCertificadoA3.TOKEN_ALADDIN.getDll(), "Luci@no10");
		certificado.setAtivarProperties(false);
		CertificadoService.inicializaCertificado(certificado, isCacert);

		String protocoloEnvio = "1.2.201805.0000000000004275005";
		String xml = enveloparDummyConsulta(protocoloEnvio);
		String xmlEnvelopado = enveloparSoap(xml);
		
		System.out.println(format(xmlEnvelopado, false));
		String retorno = chamarWs(xmlEnvelopado);
		
		System.out.println(
				"\n\nRETORNO DA CONSULTA DO ESOCIAL \n");
		retorno = format(retorno, false);
		System.out.println(retorno);

		
		
	}
	
	private static String chamarWs(String xmlEnvelopado) throws XMLStreamException, RemoteException {

		OMElement ome = AXIOMUtil.stringToOM(xmlEnvelopado);
		
		ServicoConsultarLoteEventosStub.Consulta_type0 dadosMsgType0 = new ServicoConsultarLoteEventosStub.Consulta_type0();
		dadosMsgType0.setExtraElement(ome);

		ServicoConsultarLoteEventosStub.ConsultarLoteEventos distConsultar = new ServicoConsultarLoteEventosStub.ConsultarLoteEventos();
		distConsultar.setConsulta(dadosMsgType0);
		
		ServicoConsultarLoteEventosStub stub = new ServicoConsultarLoteEventosStub(URL);
		ServicoConsultarLoteEventosStub.ConsultarLoteEventosResponse response = stub.consultarLoteEventos(distConsultar);
		String retorno = response.getConsultarLoteEventosResult().getExtraElement().toString();
		
		return retorno;
		
		
	}
	
	public static String enveloparDummyConsulta(String protocoloEnvio) {
		StringBuilder sb = new StringBuilder();
		sb.append("<eSocial xmlns=\"http://www.esocial.gov.br/schema/lote/eventos/envio/consulta/retornoProcessamento/v1_0_0\">");
		sb.append("<consultaLoteEventos>");
		sb.append("<protocoloEnvio>");
		sb.append(protocoloEnvio);
		sb.append("</protocoloEnvio>");
		sb.append("</consultaLoteEventos>");
		sb.append("</eSocial>");
		return sb.toString();
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
