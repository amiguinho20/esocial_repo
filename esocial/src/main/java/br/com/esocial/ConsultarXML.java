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
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
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


/**
 * 
 * @author eduardo.csilva 
 *
 * Dependencia: 
 *  1) um arquivo "cacaerts" customizado com os enderecos de dominios dos webservices \n
 *     @see http://www.javac.com.br/jc/posts/list/2736-arquivo...reinf-atualizado-22042018.page
 *     @see https://github.com/Samuel-Oliveira/Java_Certificado/blob/master/src/main/java/br/com/samuelweb/certificado/util/CacertUtil.java 
 *  2) um certificado digital (A1 ou A3) do tipo e-CPF ou e-CNPJ, com raiz do ICP Brasil, para assinatura de eventos e comunicação segura com o webservice.
 *  3) eventualmente, podera ser necessario a instalacao dos certificados 3 ultimos certificados do ICP Brasil: https://certificados.serpro.gov.br/serproacf/certificate-chain
 *
 */
public class ConsultarXML {
	
	private static final String URL = "https://webservices.producaorestrita.esocial.gov.br/servicos/empregador/consultarloteeventos/WsConsultarLoteEventos.svc";
	
	public static void main(String[] args) throws CertificadoException, XMLStreamException, IOException, SAXException, ParserConfigurationException, SOAPException, TransformerException {
		
		String caminhoCacerts = "C:/desenv/certificados/cacert/Cacert-22-04-2018";
//		String caminhoCacerts = "C:/desenv/jdk/jdk-8u172x64/jre/lib/security/cacerts";
		String caminhoCertificadoA1 = "C:/desenv/sandbox/esocial/mpt-cos-hom_mpt_mp_br3.p12";
		String senhaCertificadoA1 = args[0];		
		String senhaCertificadoA3 = args[1];

		Certificado certificado = EsocialUtils.getCertificadoA1(caminhoCertificadoA1, senhaCertificadoA1);
		//Certificado certificado = EsocialUtils.getCertificadoA3(senhaCertificadoA3);
		EsocialUtils.inicializarCertificadSocketDinamico(caminhoCacerts, certificado);

		String protocoloEnvio = "1.2.201805.0000000000004781215";
		String xml = enveloparDummyConsulta(protocoloEnvio);
		String xmlEnvelopado = EsocialUtils.enveloparSoap(xml);
		
		System.out.println(EsocialUtils.formatarXml(xmlEnvelopado, false));
		String retorno = EsocialUtils.chamarWs(xmlEnvelopado, URL);
		
		System.out.println(
				"\n\nRETORNO DA CONSULTA DO ESOCIAL \n");
		System.out.println(EsocialUtils.formatarXml(retorno, false));
		
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



}
